package org.camunda.optimize.service.es.report;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.optimize.dto.optimize.query.DateFilterDto;
import org.camunda.optimize.dto.optimize.query.FilterMapDto;
import org.camunda.optimize.dto.optimize.query.flownode.ExecutedFlowNodeFilterBuilder;
import org.camunda.optimize.dto.optimize.query.flownode.ExecutedFlowNodeFilterDto;
import org.camunda.optimize.dto.optimize.query.report.GroupByDto;
import org.camunda.optimize.dto.optimize.query.report.ReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.ViewDto;
import org.camunda.optimize.dto.optimize.query.report.result.NumberReportResultDto;
import org.camunda.optimize.dto.optimize.query.variable.VariableFilterDto;
import org.camunda.optimize.rest.engine.dto.ProcessInstanceEngineDto;
import org.camunda.optimize.test.it.rule.ElasticSearchIntegrationTestRule;
import org.camunda.optimize.test.it.rule.EmbeddedOptimizeRule;
import org.camunda.optimize.test.it.rule.EngineIntegrationRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.optimize.service.es.report.command.util.ReportConstants.GROUP_BY_NONE_TYPE;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.VIEW_COUNT_OPERATION;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.VIEW_FREQUENCY_PROPERTY;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.VIEW_PROCESS_INSTANCE_ENTITY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/it/it-applicationContext.xml"})
public class CountTotalProcessInstanceFrequencyReportEvaluationIT {

  public EngineIntegrationRule engineRule = new EngineIntegrationRule();
  public ElasticSearchIntegrationTestRule elasticSearchRule = new ElasticSearchIntegrationTestRule();
  public EmbeddedOptimizeRule embeddedOptimizeRule = new EmbeddedOptimizeRule();

  private static final String TEST_ACTIVITY = "testActivity";

  @Rule
  public RuleChain chain = RuleChain
    .outerRule(elasticSearchRule).around(engineRule).around(embeddedOptimizeRule);

  @Test
  public void reportEvaluationForOneProcess() throws Exception {

    // given
    ProcessInstanceEngineDto processInstanceDto = deployAndStartSimpleServiceTaskProcess();
    embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities();
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();

    // when
    String processDefinitionId = processInstanceDto.getDefinitionId();
    ReportDataDto reportData = createDefaultReportData(processDefinitionId);
    NumberReportResultDto result = evaluateReport(reportData);

    // then
    assertThat(result.getProcessDefinitionId(), is(processDefinitionId));
    assertThat(result.getView(), is(notNullValue()));
    assertThat(result.getView().getOperation(), is(VIEW_COUNT_OPERATION));
    assertThat(result.getView().getEntity(), is(VIEW_PROCESS_INSTANCE_ENTITY));
    assertThat(result.getView().getProperty(), is(VIEW_FREQUENCY_PROPERTY));
    assertThat(result.getGroupBy().getType(), is(GROUP_BY_NONE_TYPE));
    assertThat(result.getResult(), is(notNullValue()));
    assertThat(result.getResult(), is(1L));
  }

  @Test
  public void evaluateReportForMultipleEvents() throws Exception {
    // given
    ProcessInstanceEngineDto engineDto = deployAndStartSimpleServiceTaskProcess(TEST_ACTIVITY);
    engineRule.startProcessInstance(engineDto.getDefinitionId());
    engineRule.startProcessInstance(engineDto.getDefinitionId());
    embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities();
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();

    // when
    ReportDataDto reportData = createDefaultReportData(engineDto.getDefinitionId());
    NumberReportResultDto result = evaluateReport(reportData);

    // then
    assertThat(result.getResult(), is(3L));
  }

  @Test
  public void otherProcessDefinitionsDoNoAffectResult() throws Exception {
    // given
    ProcessInstanceEngineDto engineDto = deployAndStartSimpleServiceTaskProcess(TEST_ACTIVITY);
    engineRule.startProcessInstance(engineDto.getDefinitionId());
    deployAndStartSimpleServiceTaskProcess(TEST_ACTIVITY);
    embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities();
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();

    // when
    ReportDataDto reportData = createDefaultReportData(engineDto.getDefinitionId());
    NumberReportResultDto result = evaluateReport(reportData);

    // then
    assertThat(result.getResult(), is(2L));
  }

  @Test
  public void dateFilterInReport() throws Exception {
    // given
    ProcessInstanceEngineDto processInstance = deployAndStartSimpleServiceTaskProcess();
    Date past = engineRule.getHistoricProcessInstance(processInstance.getId()).getStartTime();
    String processDefinitionId = processInstance.getDefinitionId();
    embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities();
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();

    // when
    ReportDataDto reportData = createDefaultReportData(processDefinitionId);
    reportData.setFilter(createDateFilter("<", "start_date", past));
    NumberReportResultDto result = evaluateReport(reportData);

    // then
    assertThat(result.getResult(), is(notNullValue()));
    assertThat(result.getResult(), is(0L));

    // when
    reportData = createDefaultReportData(processDefinitionId);
    reportData.setFilter(createDateFilter(">=", "start_date", past));
    result = evaluateReport(reportData);

    // then
    assertThat(result.getResult(), is(notNullValue()));
    assertThat(result.getResult(), is(1L));
  }

  private FilterMapDto createDateFilter(String operator, String type, Date dateValue) {
    FilterMapDto filter ;
    filter = new FilterMapDto();
    filter.setDates(new ArrayList<>());

    DateFilterDto date = new DateFilterDto();
    date.setOperator(operator);
    date.setType(type);
    date.setValue(dateValue);
    filter.getDates().add(date);
    return filter;
  }

  @Test
  public void variableFilterInReport() throws Exception {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("var", true);
    ProcessInstanceEngineDto processInstance = deployAndStartSimpleServiceTaskProcessWithVariables(variables);
    String processDefinitionId = processInstance.getDefinitionId();
    engineRule.startProcessInstance(processDefinitionId);
    embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities();
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();

    // when
    ReportDataDto reportData = createDefaultReportData(processDefinitionId);
    reportData.setFilter(createVariableFilter("var"));
    NumberReportResultDto result = evaluateReport(reportData);

    // then
    assertThat(result.getResult(), is(1L));
  }

  private FilterMapDto createVariableFilter(String variableName) {
    VariableFilterDto filter = new VariableFilterDto();
    filter.setName(variableName);
    filter.setType("boolean");
    filter.setOperator("=");
    filter.setValues(Collections.singletonList("true"));
    FilterMapDto filterMapDto = new FilterMapDto();
    filterMapDto.setVariables(Collections.singletonList(filter));
    return filterMapDto;
  }

  @Test
  public void flowNodeFilterInReport() throws Exception {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("goToTask1", true);
    String processDefinitionId = deploySimpleGatewayProcessDefinition();
    engineRule.startProcessInstance(processDefinitionId, variables);
    variables.put("goToTask1", false);
    engineRule.startProcessInstance(processDefinitionId, variables);
    embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities();
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();

    // when
    ReportDataDto reportData = createDefaultReportData(processDefinitionId);
    FilterMapDto mapDto = new FilterMapDto();
    List<ExecutedFlowNodeFilterDto> flowNodeFilter = ExecutedFlowNodeFilterBuilder.construct()
          .id("task1")
          .build();
    mapDto.setExecutedFlowNodes(flowNodeFilter);
    reportData.setFilter(mapDto);
    NumberReportResultDto result = evaluateReport(reportData);

    // then
    assertThat(result.getResult(), is(1L));
  }


  @Test
  public void optimizeExceptionOnViewPropertyIsNull() throws Exception {
    // given
    ReportDataDto dataDto = createDefaultReportData("123");
    dataDto.getView().setProperty(null);

    //when
    Response response = evaluateReportAndReturnResponse(dataDto);

    // then
    assertThat(response.getStatus(), is(500));
  }

  @Test
  public void optimizeExceptionOnGroupByTypeIsNull() throws Exception {
    // given
    ReportDataDto dataDto = createDefaultReportData("123");
    dataDto.getGroupBy().setType(null);

    //when
    Response response = evaluateReportAndReturnResponse(dataDto);

    // then
    assertThat(response.getStatus(), is(500));
  }

  private ProcessInstanceEngineDto deployAndStartSimpleServiceTaskProcess() {
    return deployAndStartSimpleServiceTaskProcess(TEST_ACTIVITY);
  }

  private ProcessInstanceEngineDto deployAndStartSimpleServiceTaskProcess(String activityId) {
    BpmnModelInstance processModel = Bpmn.createExecutableProcess("aProcess")
      .name("aProcessName")
      .startEvent()
      .serviceTask(activityId)
      .camundaExpression("${true}")
      .endEvent()
      .done();
    return engineRule.deployAndStartProcess(processModel);
  }

  private ProcessInstanceEngineDto deployAndStartSimpleServiceTaskProcessWithVariables(Map<String, Object> variables) {
    return deployAndStartSimpleServiceTaskProcessWithVariables(TEST_ACTIVITY, variables);
  }

  private ProcessInstanceEngineDto deployAndStartSimpleServiceTaskProcessWithVariables(String activityId,
                                                                                       Map<String, Object> variables) {
    BpmnModelInstance processModel = Bpmn.createExecutableProcess("aProcess")
      .name("aProcessName")
      .startEvent()
      .serviceTask(activityId)
      .camundaExpression("${true}")
      .endEvent()
      .done();
    return engineRule.deployAndStartProcessWithVariables(processModel, variables);
  }

  private String deploySimpleGatewayProcessDefinition() throws Exception {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess()
      .startEvent("startEvent")
      .exclusiveGateway("splittingGateway")
        .name("Should we go to task 1?")
        .condition("yes", "${goToTask1}")
        .serviceTask("task1")
          .camundaExpression("${true}")
      .exclusiveGateway("mergeGateway")
        .endEvent("endEvent")
      .moveToNode("splittingGateway")
        .condition("no", "${!goToTask1}")
        .serviceTask("task2")
          .camundaExpression("${true}")
        .connectTo("mergeGateway")
      .done();
    String processDefinitionId = engineRule.deployProcessAndGetId(modelInstance);
    return processDefinitionId;
  }

  private ReportDataDto createDefaultReportData(String processDefinitionId) {
    ReportDataDto reportData = new ReportDataDto();
    reportData.setProcessDefinitionId(processDefinitionId);
    reportData.setVisualization("heat");
    ViewDto view = new ViewDto();
    view.setOperation(VIEW_COUNT_OPERATION);
    view.setEntity(VIEW_PROCESS_INSTANCE_ENTITY);
    view.setProperty(VIEW_FREQUENCY_PROPERTY);
    reportData.setView(view);
    GroupByDto groupByDto = new GroupByDto();
    groupByDto.setType(GROUP_BY_NONE_TYPE);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  private NumberReportResultDto evaluateReport(ReportDataDto reportData) {
    Response response = evaluateReportAndReturnResponse(reportData);
    assertThat(response.getStatus(), is(200));

    return response.readEntity(NumberReportResultDto.class);
  }

  private Response evaluateReportAndReturnResponse(ReportDataDto reportData) {
    String token = embeddedOptimizeRule.getAuthenticationToken();
    return embeddedOptimizeRule.target("report/evaluate")
      .request()
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
      .post(Entity.json(reportData));
  }


}
