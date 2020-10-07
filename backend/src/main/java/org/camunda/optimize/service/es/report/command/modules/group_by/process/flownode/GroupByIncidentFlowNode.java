/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.report.command.modules.group_by.process.flownode;

import lombok.RequiredArgsConstructor;
import org.camunda.optimize.dto.optimize.DefinitionType;
import org.camunda.optimize.dto.optimize.ProcessDefinitionOptimizeDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.FlowNodesGroupByDto;
import org.camunda.optimize.service.DefinitionService;
import org.camunda.optimize.service.es.report.command.exec.ExecutionContext;
import org.camunda.optimize.service.es.report.command.modules.group_by.GroupByPart;
import org.camunda.optimize.service.es.report.command.modules.result.CompositeCommandResult;
import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.camunda.optimize.service.es.schema.index.ProcessInstanceIndex.INCIDENTS;
import static org.camunda.optimize.service.es.schema.index.ProcessInstanceIndex.INCIDENT_ACTIVITY_ID;
import static org.elasticsearch.search.aggregations.AggregationBuilders.nested;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

@RequiredArgsConstructor
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupByIncidentFlowNode extends GroupByPart<ProcessReportDataDto> {
  private static final String NESTED_INCIDENT_AGGREGATION = "nestedIncidentAggregation";
  private static final String GROUPED_BY_FLOW_NODE_ID_AGGREGATION = "groupedByFlowNodeIdAggregation";

  private final ConfigurationService configurationService;
  private final DefinitionService definitionService;

  @Override
  public List<AggregationBuilder> createAggregation(final SearchSourceBuilder searchSourceBuilder,
                                                    final ExecutionContext<ProcessReportDataDto> context) {
    return Collections.singletonList(
      nested(NESTED_INCIDENT_AGGREGATION, INCIDENTS)
        .subAggregation(
          terms(GROUPED_BY_FLOW_NODE_ID_AGGREGATION)
            .size(configurationService.getEsAggregationBucketLimit())
            .field(INCIDENTS + "." + INCIDENT_ACTIVITY_ID)
            .subAggregation(distributedByPart.createAggregation(context))
        )
    );
  }

  @Override
  public void addQueryResult(final CompositeCommandResult compositeCommandResult,
                             final SearchResponse response,
                             final ExecutionContext<ProcessReportDataDto> context) {
    final Nested nestedAgg = response.getAggregations().get(NESTED_INCIDENT_AGGREGATION);
    final Terms groupedByFlowNodeId = nestedAgg.getAggregations().get(GROUPED_BY_FLOW_NODE_ID_AGGREGATION);

    final Map<String, String> flowNodeNames = getFlowNodeNames(context.getReportData());
    final List<CompositeCommandResult.GroupByResult> groupedData = new ArrayList<>();
    for (Terms.Bucket flowNodeBucket : groupedByFlowNodeId.getBuckets()) {
      final String flowNodeKey = flowNodeBucket.getKeyAsString();
      if (flowNodeNames.containsKey(flowNodeKey)) {
        final List<CompositeCommandResult.DistributedByResult> singleResult =
          distributedByPart.retrieveResult(response, flowNodeBucket.getAggregations(), context);
        String label = flowNodeNames.get(flowNodeKey);
        groupedData.add(CompositeCommandResult.GroupByResult.createGroupByResult(flowNodeKey, label, singleResult));
        flowNodeNames.remove(flowNodeKey);
      }
    }

    // enrich data with flow nodes that haven't been executed, but should still show up in the result
    flowNodeNames.keySet().forEach(flowNodeKey -> {
      CompositeCommandResult.GroupByResult emptyResult =
        CompositeCommandResult.GroupByResult.createResultWithEmptyDistributedBy(flowNodeKey);
      emptyResult.setLabel(flowNodeNames.get(flowNodeKey));
      groupedData.add(emptyResult);
    });

    compositeCommandResult.setGroups(groupedData);
      compositeCommandResult.setIsComplete(groupedByFlowNodeId.getSumOfOtherDocCounts() == 0L);
  }

  private Map<String, String> getFlowNodeNames(final ProcessReportDataDto reportData) {
    return definitionService
      .getLatestDefinition(
        DefinitionType.PROCESS,
        reportData.getDefinitionKey(),
        reportData.getDefinitionVersions(),
        reportData.getTenantIds()
      )
      .map(def -> ((ProcessDefinitionOptimizeDto) def).getFlowNodeNames())
      .orElse(Collections.emptyMap());
  }

  @Override
  protected void addGroupByAdjustmentsForCommandKeyGeneration(final ProcessReportDataDto reportData) {
    reportData.setGroupBy(new FlowNodesGroupByDto());
  }
}
