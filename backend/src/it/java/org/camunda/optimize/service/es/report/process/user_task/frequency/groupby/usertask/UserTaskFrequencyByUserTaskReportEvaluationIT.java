/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.report.process.user_task.frequency.groupby.usertask;

import com.google.common.collect.ImmutableList;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.optimize.dto.engine.ProcessDefinitionEngineDto;
import org.camunda.optimize.dto.optimize.ReportConstants;
import org.camunda.optimize.dto.optimize.query.report.single.configuration.AggregationType;
import org.camunda.optimize.dto.optimize.query.report.single.configuration.FlowNodeExecutionState;
import org.camunda.optimize.dto.optimize.query.report.single.configuration.UserTaskDurationTime;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.filter.ProcessFilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.filter.util.ProcessFilterBuilder;
import org.camunda.optimize.dto.optimize.query.report.single.process.result.ProcessCountReportMapResultDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.view.ProcessViewEntity;
import org.camunda.optimize.dto.optimize.query.report.single.process.view.ProcessViewProperty;
import org.camunda.optimize.dto.optimize.query.report.single.result.MapResultEntryDto;
import org.camunda.optimize.dto.optimize.query.report.single.configuration.sorting.SortOrder;
import org.camunda.optimize.dto.optimize.query.report.single.configuration.sorting.SortingDto;
import org.camunda.optimize.dto.optimize.rest.report.AuthorizedProcessReportEvaluationResultDto;
import org.camunda.optimize.rest.engine.dto.ProcessInstanceEngineDto;
import org.camunda.optimize.service.es.report.process.AbstractProcessDefinitionIT;
import org.camunda.optimize.test.util.ProcessReportDataBuilder;
import org.camunda.optimize.test.util.ProcessReportDataType;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.camunda.optimize.dto.optimize.query.report.single.configuration.sorting.SortingDto.SORT_BY_KEY;
import static org.camunda.optimize.dto.optimize.query.report.single.configuration.sorting.SortingDto.SORT_BY_LABEL;
import static org.camunda.optimize.dto.optimize.query.report.single.configuration.sorting.SortingDto.SORT_BY_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@RunWith(JUnitParamsRunner.class)
public class UserTaskFrequencyByUserTaskReportEvaluationIT extends AbstractProcessDefinitionIT {

  private static final String START_EVENT = "startEvent";
  private static final String END_EVENT = "endEvent";
  private static final String PROCESS_DEFINITION_KEY = "123";
  private static final String USER_TASK_1 = "userTask1";
  private static final String USER_TASK_2 = "userTask2";

  @Test
  public void reportEvaluationForOneProcess() {
    // given
    ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();
    ProcessInstanceEngineDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    final ProcessReportDataDto reportData = createReport(processDefinition);

    // when
    final AuthorizedProcessReportEvaluationResultDto<ProcessCountReportMapResultDto> evaluationResponse =
      evaluateCountMapReport(reportData);

    // then
    final ProcessReportDataDto resultReportDataDto = evaluationResponse.getReportDefinition().getData();
    assertThat(resultReportDataDto.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(resultReportDataDto.getDefinitionVersions(), contains(processDefinition.getVersionAsString()));
    assertThat(resultReportDataDto.getView(), is(notNullValue()));
    assertThat(resultReportDataDto.getView().getEntity(), is(ProcessViewEntity.USER_TASK));
    assertThat(resultReportDataDto.getView().getProperty(), is(ProcessViewProperty.FREQUENCY));

    final ProcessCountReportMapResultDto result = evaluationResponse.getResult();
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(1L)
    );
    assertThat(
      result.getDataEntryForKey(USER_TASK_2).get().getValue(),
      is(1L)
    );
    assertThat(result.getProcessInstanceCount(), is(1L));
  }

  @Test
  public void reportEvaluationForSeveralProcesses() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();
    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData().size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );
    assertThat(
      result.getDataEntryForKey(USER_TASK_2).get().getValue(),
      is(2L)
    );
    assertThat(result.getProcessInstanceCount(), is(2L));
  }

  @Test
  public void evaluateReportForMultipleEvents_resultLimitedByConfig() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    embeddedOptimizeRule.getConfigurationService().setEsAggregationBucketLimit(1);

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    final ProcessCountReportMapResultDto resultDto = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(resultDto.getProcessInstanceCount(), is(2L));
    assertThat(resultDto.getData(), is(notNullValue()));
    assertThat(resultDto.getData().size(), is(2));
    assertThat(getExecutedFlowNodeCount(resultDto), is(1L));
    assertThat(resultDto.getIsComplete(), is(false));
  }

  @Test
  public void testCustomOrderOnResultKeyIsApplied() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    reportData.getConfiguration().setSorting(new SortingDto(SORT_BY_KEY, SortOrder.DESC));
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    final List<MapResultEntryDto<Long>> resultData = result.getData();
    assertThat(resultData.size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    final List<String> resultKeys = resultData.stream().map(MapResultEntryDto::getKey).collect(Collectors.toList());
    assertThat(
      resultKeys,
      // expect ascending order
      contains(resultKeys.stream().sorted(Comparator.reverseOrder()).toArray())
    );
  }

  @Test
  public void testCustomOrderOnResultLabelIsApplied() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    reportData.getConfiguration().setSorting(new SortingDto(SORT_BY_LABEL, SortOrder.DESC));
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    final List<MapResultEntryDto<Long>> resultData = result.getData();
    assertThat(resultData.size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    final List<String> resultLabels = resultData.stream()
      .map(MapResultEntryDto::getLabel)
      .collect(Collectors.toList());
    assertThat(
      resultLabels,
      // expect ascending order
      contains(resultLabels.stream().sorted(Comparator.reverseOrder()).toArray())
    );
  }


  @Test
  public void testCustomOrderOnResultValueIsApplied() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition.getId());
    engineRule.finishAllRunningUserTasks(processInstanceDto2.getId());

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    reportData.getConfiguration().setSorting(new SortingDto(SORT_BY_VALUE, SortOrder.ASC));
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData().size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    assertCorrectValueOrdering(result);
  }

  @Test
  public void allVersionsRespectLatestNodesOnlyWhereLatestHasMoreNodes() {
    //given
    final ProcessDefinitionEngineDto firstDefinition = deployOneUserTasksDefinition();
    final ProcessDefinitionEngineDto latestDefinition = deployTwoUserTasksDefinition();
    assertThat(latestDefinition.getVersion(), is(2));

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(firstDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(latestDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    //when
    final ProcessReportDataDto reportData = createReport(latestDefinition.getKey(), ReportConstants.ALL_VERSIONS);
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    //then
    assertThat(result.getData().size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );
    assertThat(
      result.getDataEntryForKey(USER_TASK_2).get().getValue(),
      is(1L)
    );
  }

  @Test
  public void multipleVersionsRespectLatestNodesOnlyWhereLatestHasMoreNodes() {
    //given
    final ProcessDefinitionEngineDto firstDefinition = deployOneUserTasksDefinition();
    deployOneUserTasksDefinition();
    final ProcessDefinitionEngineDto latestDefinition = deployTwoUserTasksDefinition();
    assertThat(latestDefinition.getVersion(), is(3));

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(firstDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(latestDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    //when
    final ProcessReportDataDto reportData =
      createReport(
        latestDefinition.getKey(),
        ImmutableList.of(firstDefinition.getVersionAsString(), latestDefinition.getVersionAsString())
      );
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    //then
    assertThat(result.getData().size(), is(2));
    assertThat(getExecutedFlowNodeCount(result), is(2L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );
    assertThat(
      result.getDataEntryForKey(USER_TASK_2).get().getValue(),
      is(1L)
    );
  }

  @Test
  public void allVersionsRespectLatestNodesOnlyWhereLatestHasLessNodes() {
    //given
    final ProcessDefinitionEngineDto firstDefinition = deployTwoUserTasksDefinition();
    final ProcessDefinitionEngineDto latestDefinition = deployOneUserTasksDefinition();
    assertThat(latestDefinition.getVersion(), is(2));

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(firstDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(latestDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    //when
    final ProcessReportDataDto reportData = createReport(latestDefinition.getKey(), ReportConstants.ALL_VERSIONS);
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    //then
    assertThat(result.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result), is(1L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );
  }

  @Test
  public void multipleVersionsRespectLatestNodesOnlyWhereLatestHasLessNodes() {
    //given
    final ProcessDefinitionEngineDto firstDefinition = deployTwoUserTasksDefinition();
    deployTwoUserTasksDefinition();
    final ProcessDefinitionEngineDto latestDefinition = deployOneUserTasksDefinition();
    assertThat(latestDefinition.getVersion(), is(3));

    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(firstDefinition.getId());
    finishAllUserTasks(processInstanceDto1);

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(latestDefinition.getId());
    finishAllUserTasks(processInstanceDto2);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    //when
    final ProcessReportDataDto reportData =
      createReport(
        latestDefinition.getKey(),
        ImmutableList.of(firstDefinition.getVersionAsString(), latestDefinition.getVersionAsString())
      );
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    //then
    assertThat(result.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result), is(1L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );
  }

  @Test
  public void otherProcessDefinitionsDoNotInfluenceResult() {
    // given
    final ProcessDefinitionEngineDto processDefinition1 = deployOneUserTasksDefinition();
    final ProcessInstanceEngineDto processInstanceDto1 = engineRule.startProcessInstance(processDefinition1.getId());
    finishAllUserTasks(processInstanceDto1);
    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition1.getId());
    finishAllUserTasks(processInstanceDto2);

    final ProcessDefinitionEngineDto processDefinition2 = deployOneUserTasksDefinition();
    final ProcessInstanceEngineDto processInstanceDto3 = engineRule.startProcessInstance(processDefinition2.getId());
    finishAllUserTasks(processInstanceDto3);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData1 = createReport(processDefinition1);
    final ProcessCountReportMapResultDto result1 = evaluateCountMapReport(reportData1).getResult();
    final ProcessReportDataDto reportData2 = createReport(processDefinition2);
    final ProcessCountReportMapResultDto result2 = evaluateCountMapReport(reportData2).getResult();

    // then
    assertThat(result1.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result1), is(1L));
    assertThat(
      result1.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );

    assertThat(result2.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result2), is(1L));
    assertThat(
      result2.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(1L)
    );
  }

  @Test
  public void reportEvaluationSingleBucketFilteredBySingleTenant() {
    // given
    final String tenantId1 = "tenantId1";
    final String tenantId2 = "tenantId2";
    final List<String> selectedTenants = newArrayList(tenantId1);
    final String processKey = deployAndStartMultiTenantUserTaskProcess(
      newArrayList(null, tenantId1, tenantId2)
    );

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    ProcessReportDataDto reportData = createReport(processKey, ReportConstants.ALL_VERSIONS);
    reportData.setTenantIds(selectedTenants);
    ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getProcessInstanceCount(), is((long) selectedTenants.size()));
  }

  @Test
  public void noUserTaskMatchesReturnsEmptyResult() {
    // when
    final ProcessReportDataDto reportData = createReport(
      "nonExistingProcessDefinitionId", "1"
    );
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData().size(), is(0));
  }

  @Data
  @AllArgsConstructor
  static class ExecutionStateTestValues {
    FlowNodeExecutionState executionState;
    Map<String, Long> expectedFrequencyValues;
  }

  private static Map<String, Long> getExpectedResultsMap(Long userTask1Results, Long userTask2Results) {
    Map<String, Long> result = new HashMap<>();
    result.put(USER_TASK_1, userTask1Results);
    result.put(USER_TASK_2, userTask2Results);
    return result;
  }

  protected static Object[] getExecutionStateExpectedValues() {


    return new Object[]{
      new ExecutionStateTestValues(FlowNodeExecutionState.RUNNING, getExpectedResultsMap(1L, 1L)),
      new ExecutionStateTestValues(FlowNodeExecutionState.COMPLETED, getExpectedResultsMap(1L, null)),
      new ExecutionStateTestValues(FlowNodeExecutionState.ALL, getExpectedResultsMap(2L, 1L))
    };
  }


  @Test
  @Parameters(method = "getExecutionStateExpectedValues")
  public void evaluateReportWithExecutionState(ExecutionStateTestValues testValues) {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployTwoUserTasksDefinition();
    final ProcessInstanceEngineDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId());
    // finish first running task, second now runs but unclaimed
    engineRule.finishAllRunningUserTasks(processInstanceDto.getId());

    final ProcessInstanceEngineDto processInstanceDto2 = engineRule.startProcessInstance(processDefinition.getId());
    // claim first running task
    engineRule.claimAllRunningUserTasks(processInstanceDto2.getId());

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    reportData.getConfiguration().setFlowNodeExecutionState(testValues.executionState);
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData().size(), is(2));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(testValues.getExpectedFrequencyValues().get(USER_TASK_1))
    );
    assertThat(
      result.getDataEntryForKey(USER_TASK_2).get().getValue(),
      is(testValues.getExpectedFrequencyValues().get(USER_TASK_2))
    );
    assertThat(result.getProcessInstanceCount(), is(2L));
  }

  @Test
  public void processDefinitionContainsMultiInstanceBody() {
    // given
    BpmnModelInstance processWithMultiInstanceUserTask = Bpmn
      // @formatter:off
        .createExecutableProcess("processWithMultiInstanceUserTask")
        .startEvent()
          .userTask(USER_TASK_1).multiInstance().cardinality("2").multiInstanceDone()
        .endEvent()
        .done();
    // @formatter:on

    final ProcessDefinitionEngineDto processDefinition = engineRule.deployProcessAndGetProcessDefinition(
      processWithMultiInstanceUserTask
    );
    final ProcessInstanceEngineDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId());
    engineRule.finishAllRunningUserTasks(processInstanceDto.getId());

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result), is(1L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(2L)
    );
  }

  @Test
  public void evaluateReportForMoreThanTenEvents() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployOneUserTasksDefinition();

    for (int i = 0; i < 11; i++) {
      final ProcessInstanceEngineDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId());
      engineRule.finishAllRunningUserTasks(processInstanceDto.getId());
    }

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    final ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result), is(1L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(11L)
    );
  }

  @Test
  public void filterInReport() {
    // given
    final ProcessDefinitionEngineDto processDefinition = deployOneUserTasksDefinition();
    final ProcessInstanceEngineDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId());
    engineRule.finishAllRunningUserTasks(processInstanceDto.getId());

    final OffsetDateTime processStartTime = engineRule.getHistoricProcessInstance(processInstanceDto.getId())
      .getStartTime();

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    ProcessReportDataDto reportData = createReport(processDefinition);
    reportData.setFilter(createStartDateFilter(null, processStartTime.minusSeconds(1L)));
    ProcessCountReportMapResultDto result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result), is(0L));

    // when
    reportData = createReport(processDefinition);
    reportData.setFilter(createStartDateFilter(processStartTime, null));
    result = evaluateCountMapReport(reportData).getResult();

    // then
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().size(), is(1));
    assertThat(getExecutedFlowNodeCount(result), is(1L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(1L)
    );
  }

  @Test
  public void canAlsoBeEvaluatedIfAggregationTypeAndUserTaskDurationTimeIsDifferentFromDefault() {
    // given
    ProcessDefinitionEngineDto processDefinition = deployOneUserTasksDefinition();
    ProcessInstanceEngineDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId());
    finishAllUserTasks(processInstanceDto);

    embeddedOptimizeRule.importAllEngineEntitiesFromScratch();
    elasticSearchRule.refreshAllOptimizeIndices();

    // when
    final ProcessReportDataDto reportData = createReport(processDefinition);
    reportData.getConfiguration().setAggregationType(AggregationType.MAX);
    reportData.getConfiguration().setUserTaskDurationTime(UserTaskDurationTime.IDLE);
    final AuthorizedProcessReportEvaluationResultDto<ProcessCountReportMapResultDto> evaluationResponse =
      evaluateCountMapReport(reportData);

    // then
    final ProcessReportDataDto resultReportDataDto = evaluationResponse.getReportDefinition().getData();

    final ProcessCountReportMapResultDto result = evaluationResponse.getResult();
    assertThat(result.getProcessInstanceCount(), is(1L));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(getExecutedFlowNodeCount(result), is(1L));
    assertThat(
      result.getDataEntryForKey(USER_TASK_1).get().getValue(),
      is(1L)
    );
  }

  @Test
  public void optimizeExceptionOnViewEntityIsNull() {
    // given
    final ProcessReportDataDto dataDto = createReport(PROCESS_DEFINITION_KEY, "1");
    dataDto.getView().setEntity(null);

    //when
    final Response response = evaluateReportAndReturnResponse(dataDto);

    // then
    assertThat(response.getStatus(), is(500));
  }

  @Test
  public void optimizeExceptionOnViewPropertyIsNull() {
    // given
    final ProcessReportDataDto dataDto = createReport(PROCESS_DEFINITION_KEY, "1");
    dataDto.getView().setProperty(null);

    //when
    final Response response = evaluateReportAndReturnResponse(dataDto);

    // then
    assertThat(response.getStatus(), is(500));
  }

  @Test
  public void optimizeExceptionOnGroupByTypeIsNull() {
    // given
    final ProcessReportDataDto dataDto = createReport(PROCESS_DEFINITION_KEY, "1");
    dataDto.getGroupBy().setType(null);

    //when
    final Response response = evaluateReportAndReturnResponse(dataDto);

    // then
    assertThat(response.getStatus(), is(400));
  }

  protected ProcessReportDataDto createReport(final String processDefinitionKey, final String version) {
    return createReport(processDefinitionKey, ImmutableList.of(version));
  }

  protected ProcessReportDataDto createReport(final String processDefinitionKey, final List<String> versions) {
    return ProcessReportDataBuilder
      .createReportData()
      .setProcessDefinitionKey(processDefinitionKey)
      .setProcessDefinitionVersions(versions)
      .setReportDataType(ProcessReportDataType.USER_TASK_FREQUENCY_GROUP_BY_FLOW_NODE)
      .build();
  }

  private ProcessReportDataDto createReport(final ProcessDefinitionEngineDto processDefinition) {
    return createReport(processDefinition.getKey(), String.valueOf(processDefinition.getVersion()));
  }

  private List<ProcessFilterDto> createStartDateFilter(OffsetDateTime startDate, OffsetDateTime endDate) {
    return ProcessFilterBuilder.filter().fixedStartDate().start(startDate).end(endDate).add().buildList();
  }

  private void finishAllUserTasks(final ProcessInstanceEngineDto processInstanceDto1) {
    // finish first task
    engineRule.finishAllRunningUserTasks(processInstanceDto1.getId());
    // finish second task
    engineRule.finishAllRunningUserTasks(processInstanceDto1.getId());
  }

  private String deployAndStartMultiTenantUserTaskProcess(final List<String> deployedTenants) {
    final String processKey = "multiTenantProcess";
    deployedTenants.stream()
      .filter(Objects::nonNull)
      .forEach(tenantId -> engineRule.createTenant(tenantId));
    deployedTenants
      .forEach(tenant -> {
        final ProcessDefinitionEngineDto processDefinitionEngineDto = deployOneUserTasksDefinition(processKey, tenant);
        engineRule.startProcessInstance(processDefinitionEngineDto.getId());
      });

    return processKey;
  }

  private ProcessDefinitionEngineDto deployOneUserTasksDefinition() {
    return deployOneUserTasksDefinition("aProcess", null);
  }

  private ProcessDefinitionEngineDto deployOneUserTasksDefinition(String key, String tenantId) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(key)
      .startEvent(START_EVENT)
      .userTask(USER_TASK_1)
      .endEvent(END_EVENT)
      .done();
    return engineRule.deployProcessAndGetProcessDefinition(modelInstance, tenantId);
  }

  private ProcessDefinitionEngineDto deployTwoUserTasksDefinition() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("aProcess")
      .startEvent(START_EVENT)
      .userTask(USER_TASK_1)
      .userTask(USER_TASK_2)
      .endEvent(END_EVENT)
      .done();
    return engineRule.deployProcessAndGetProcessDefinition(modelInstance);
  }

  private long getExecutedFlowNodeCount(ProcessCountReportMapResultDto resultList) {
    return resultList.getData()
      .stream()
      .map(MapResultEntryDto::getValue)
      .filter(Objects::nonNull)
      .count();
  }

  private void assertCorrectValueOrdering(ProcessCountReportMapResultDto result) {
    List<MapResultEntryDto<Long>> resultData = result.getData();
    final List<Long> bucketValues = resultData.stream()
      .map(MapResultEntryDto::getValue)
      .collect(Collectors.toList());
    final List<Long> bucketValuesWithoutNullValue = bucketValues.stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    assertThat(
      bucketValuesWithoutNullValue,
      contains(bucketValuesWithoutNullValue.stream().sorted(Comparator.naturalOrder()).toArray())
    );
    long notExecutedFlowNodes = resultData.size() - getExecutedFlowNodeCount(result);
    for (int i = resultData.size() - 1; i > getExecutedFlowNodeCount(result) - 1; i--) {
      assertThat(bucketValues.get(i), nullValue());
    }
  }

}
