package org.camunda.optimize.service.es.reader;

import org.apache.lucene.search.join.ScoreMode;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.optimize.dto.optimize.query.BranchAnalysisDto;
import org.camunda.optimize.dto.optimize.query.BranchAnalysisOutcomeDto;
import org.camunda.optimize.dto.optimize.query.BranchAnalysisQueryDto;
import org.camunda.optimize.service.es.mapping.DateFilterHelper;
import org.camunda.optimize.service.es.mapping.VariableFilterHelper;
import org.camunda.optimize.service.es.schema.type.ProcessInstanceType;
import org.camunda.optimize.service.util.ConfigurationService;
import org.camunda.optimize.service.util.ValidationHelper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * @author Askar Akhmerov
 */
@Component
public class BranchAnalysisReader {

  private final Logger logger = LoggerFactory.getLogger(BranchAnalysisReader.class);

  private static final String GATEWAY_ACTIVITY_TYPE = "exclusiveGateway";

  @Autowired
  private TransportClient esclient;
  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private ProcessDefinitionReader processDefinitionReader;

  @Autowired
  private DateFilterHelper dateFilterHelper;

  @Autowired
  private VariableFilterHelper variableFilterHelper;

  public BranchAnalysisDto branchAnalysis(BranchAnalysisQueryDto request) {
    ValidationHelper.validate(request);
    logger.debug("Performing branch analysis on process definition: {}", request.getProcessDefinitionId());
    
    BranchAnalysisDto result = new BranchAnalysisDto();
    List<FlowNode> gatewayOutcomes = fetchGatewayOutcomes(request.getProcessDefinitionId(), request.getGateway());

    for (FlowNode activity : gatewayOutcomes) {
      Set<String> activitiesToExcludeFromBranchAnalysis = extractActivitiesToExclude(gatewayOutcomes, activity.getId());
      BranchAnalysisOutcomeDto branchAnalysis = branchAnalysis(activity, request, activitiesToExcludeFromBranchAnalysis);
      result.getFollowingNodes().put(branchAnalysis.getActivityId(), branchAnalysis);
    }

    result.setEndEvent(request.getEnd());
    result.setTotal(calculateActivityCount(request.getEnd(), request, Collections.emptySet()));

    return result;
  }

  private Set<String> extractActivitiesToExclude(List<FlowNode> gatewayOutcomes, String currentActivityId) {
    Set<String> activitiesToExcludeFromBranchAnalysis = new HashSet<>();
    for (FlowNode gatewayOutgoingNode : gatewayOutcomes) {
      String activityType = gatewayOutgoingNode.getElementType().getTypeName();
      if (!activityType.equals(GATEWAY_ACTIVITY_TYPE)) {
        activitiesToExcludeFromBranchAnalysis.add(gatewayOutgoingNode.getId());
      }
    }
    activitiesToExcludeFromBranchAnalysis.remove(currentActivityId);
    return activitiesToExcludeFromBranchAnalysis;
  }

  private BranchAnalysisOutcomeDto branchAnalysis(FlowNode flowNode, BranchAnalysisQueryDto request, Set<String> activitiesToExclude) {

    BranchAnalysisOutcomeDto result = new BranchAnalysisOutcomeDto();
    result.setActivityId(flowNode.getId());
    result.setActivityCount(calculateActivityCount(flowNode.getId(), request, activitiesToExclude));
    result.setActivitiesReached(calculateReachedEndEventActivityCount(flowNode.getId(), request, activitiesToExclude));

    return result;
  }

  private long calculateReachedEndEventActivityCount(String activityId, BranchAnalysisQueryDto request, Set<String> activitiesToExclude) {
    BoolQueryBuilder query = boolQuery()
      .must(termQuery("processDefinitionId", request.getProcessDefinitionId()))
      .must(createMustMatchActivityIdQuery(request.getGateway()))
      .must(createMustMatchActivityIdQuery(activityId))
      .must(createMustMatchActivityIdQuery(request.getEnd())
      );
    excludeActivities(activitiesToExclude, query);

    return executeQuery(request, query);
  }

  private long calculateActivityCount(String activityId, BranchAnalysisQueryDto request, Set<String> activitiesToExclude) {
    BoolQueryBuilder query = boolQuery()
      .must(termQuery("processDefinitionId", request.getProcessDefinitionId()))
      .must(createMustMatchActivityIdQuery(request.getGateway()))
      .must(createMustMatchActivityIdQuery(activityId));
    excludeActivities(activitiesToExclude, query);

    return executeQuery(request, query);
  }

  private void excludeActivities(Set<String> activitiesToExclude, BoolQueryBuilder query) {
    for (String excludeActivityId : activitiesToExclude) {
      query
        .mustNot(createMustMatchActivityIdQuery(excludeActivityId));
    }
  }

  private NestedQueryBuilder createMustMatchActivityIdQuery(String activityId) {
    return nestedQuery(
      ProcessInstanceType.EVENTS,
      termQuery("events.activityId", activityId),
      ScoreMode.None
    );
  }

  private long executeQuery(BranchAnalysisQueryDto request, BoolQueryBuilder query) {
    if (request.getFilter() != null) {
      query = dateFilterHelper.addFilters(query, request.getFilter());
      query = variableFilterHelper.addFilters(query, request.getFilter());
    }

    SearchResponse sr = esclient
        .prepareSearch(configurationService.getOptimizeIndex())
        .setTypes(configurationService.getProcessInstanceType())
        .setQuery(query)
        .setFetchSource(false)
        .setSize(0)
        .get();

    return sr.getHits().totalHits();
  }

  private List<FlowNode> fetchGatewayOutcomes(String processDefinitionId, String gatewayActivityId) {
    List<FlowNode> result = new ArrayList<>();
    String xml = processDefinitionReader.getProcessDefinitionXml(processDefinitionId);
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(xml.getBytes()));
    FlowNode flowNode = bpmnModelInstance.getModelElementById(gatewayActivityId);
    for (SequenceFlow sequence : flowNode.getOutgoing()) {
      result.add(sequence.getTarget());
    }
    return result;
  }
}
