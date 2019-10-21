/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.alert;

import org.camunda.optimize.AbstractAlertIT;
import org.camunda.optimize.dto.engine.ProcessDefinitionEngineDto;
import org.camunda.optimize.dto.optimize.query.IdDto;
import org.camunda.optimize.dto.optimize.query.alert.AlertCreationDto;
import org.camunda.optimize.dto.optimize.query.alert.AlertDefinitionDto;
import org.camunda.optimize.dto.optimize.query.alert.AlertInterval;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessVisualization;
import org.camunda.optimize.dto.optimize.query.report.single.process.SingleProcessReportDefinitionDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.FlowNodesGroupByDto;
import org.camunda.optimize.rest.engine.dto.ProcessInstanceEngineDto;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class AlertReminderSchedulerIT extends AbstractAlertIT {

  @Before
  public void cleanUp() throws Exception {
    embeddedOptimizeExtensionRule.getAlertService().getScheduler().clear();
  }

  @Test
  public void reminderJobsAreRemovedOnAlertDeletion() throws Exception {

    // given
    AlertCreationDto simpleAlert = createBasicAlertWithReminder();
    String id = createAlert(simpleAlert);
    triggerAndCompleteCheckJob(id);

    //when
    embeddedOptimizeExtensionRule
      .getRequestExecutor()
      .buildDeleteAlertRequest(id)
      .execute();

    //then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(0)
    );
  }

  @Test
  public void reminderJobsAreRemovedOnReportDeletion() throws Exception {
    // given
    AlertCreationDto simpleAlert = createBasicAlertWithReminder();
    String id = createAlert(simpleAlert);
    triggerAndCompleteCheckJob(id);

    //when
    embeddedOptimizeExtensionRule
      .getRequestExecutor()
      .buildDeleteReportRequest(simpleAlert.getReportId(), true)
      .execute();
    //then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(0)
    );
  }

  @Test
  public void reminderIsNotCreatedOnStartupIfNotDefinedInAlert() throws Exception {
    //given
    AlertCreationDto alert = setupBasicProcessAlert();
    createAlert(alert);

    //when
    embeddedOptimizeExtensionRule.stopOptimize();
    embeddedOptimizeExtensionRule.startOptimize();


    //then
    assertThat(getAllAlerts().get(0).getReminder(), is(nullValue()));
  }


  private List<AlertDefinitionDto> getAllAlerts() {
    return embeddedOptimizeExtensionRule
      .getRequestExecutor()
      .buildGetAllAlertsRequest()
      .executeAndReturnList(AlertDefinitionDto.class, 200);
  }

  @Test
  public void reminderJobsAreRemovedOnReportUpdate() throws Exception {
    ProcessDefinitionEngineDto processDefinition = deploySimpleServiceTaskProcess();
    engineIntegrationExtensionRule.startProcessInstance(processDefinition.getId());

    embeddedOptimizeExtensionRule.importAllEngineEntitiesFromScratch();
    elasticSearchIntegrationTestExtensionRule.refreshAllOptimizeIndices();

    String reportId = createAndStoreProcessNumberReport(processDefinition);
    AlertCreationDto simpleAlert = createSimpleAlert(reportId);

    AlertInterval reminderInterval = new AlertInterval();
    reminderInterval.setValue(1);
    reminderInterval.setUnit("Seconds");
    simpleAlert.setReminder(reminderInterval);

    String id = createAlert(simpleAlert);

    triggerAndCompleteCheckJob(id);

    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(2)
    );

    //when
    SingleProcessReportDefinitionDto report = getProcessNumberReportDefinitionDto(
      processDefinition.getKey(), String.valueOf(processDefinition.getVersion())
    );
    report.getData().setGroupBy(new FlowNodesGroupByDto());
    report.getData().setVisualization(ProcessVisualization.HEAT);
    updateSingleProcessReport(simpleAlert.getReportId(), report);

    //then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(0)
    );
  }

  @Test
  public void reminderJobsAreRemovedOnEvaluationResultChange() throws Exception {
    //given
    long daysToShift = 0L;
    long durationInSec = 2L;

    ProcessInstanceEngineDto processInstance = deployWithTimeShift(daysToShift, durationInSec);

    // when
    String reportId =
      createAndStoreDurationNumberReport(processInstance);
    AlertCreationDto simpleAlert = createSimpleAlert(reportId, 10, "Seconds");
    AlertInterval reminderInterval = new AlertInterval();
    reminderInterval.setValue(1);
    reminderInterval.setUnit("Minutes");
    simpleAlert.setReminder(reminderInterval);

    simpleAlert.setThreshold(1500);

    String id = createAlert(simpleAlert);

    triggerAndCompleteCheckJob(id);

    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(greaterThanOrEqualTo(2))
    );
    //when
    engineIntegrationExtensionRule.startProcessInstance(processInstance.getDefinitionId());
    embeddedOptimizeExtensionRule.importAllEngineEntitiesFromScratch();
    elasticSearchIntegrationTestExtensionRule.refreshAllOptimizeIndices();

    triggerAndCompleteReminderJob(id);

    //then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(1)
    );
  }

  @Test
  public void reminderJobsAreRemovedOnAlertDefinitionChange() throws Exception {
    //given
    AlertCreationDto simpleAlert = createBasicAlertWithReminder();

    String id = createAlert(simpleAlert);

    triggerAndCompleteCheckJob(id);

    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(greaterThanOrEqualTo(2))
    );
    //when
    simpleAlert.getCheckInterval().setValue(30);

    embeddedOptimizeExtensionRule
      .getRequestExecutor()
      .buildUpdateAlertRequest(id, simpleAlert)
      .execute();

    //then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(1)
    );

    //when
    triggerAndCompleteCheckJob(id);

    //then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(2)
    );
  }

  @Test
  public void reminderJobsAreScheduledOnAlertCreation() throws Exception {
    //given
    AlertCreationDto simpleAlert = createBasicAlertWithReminder();

    // when
    String id = embeddedOptimizeExtensionRule
      .getRequestExecutor()
      .buildCreateAlertRequest(simpleAlert)
      .execute(IdDto.class, 200)
      .getId();

    triggerAndCompleteCheckJob(id);

    // then
    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(2)
    );
  }

  @Test
  public void reminderJobsAreScheduledCorrectly() throws Exception {
    //given
    AlertCreationDto simpleAlert = createBasicAlertWithReminder();
    AlertInterval checkInterval = new AlertInterval();
    checkInterval.setUnit("Minutes");
    checkInterval.setValue(10);
    simpleAlert.setCheckInterval(checkInterval);

    // when
    String id = embeddedOptimizeExtensionRule
      .getRequestExecutor()
      .buildCreateAlertRequest(simpleAlert)
      .execute(IdDto.class, 200)
      .getId();

    triggerAndCompleteCheckJob(id);

    // then
    OffsetDateTime nextTimeReminderIsExecuted = getNextReminderExecutionTime(id);
    OffsetDateTime upperBoundary = OffsetDateTime.now().plusSeconds(2L); // 1 second is too unstable
    assertTrue(nextTimeReminderIsExecuted.isBefore(upperBoundary));
  }

  @Test
  public void reminderJobsAreScheduledAfterRestart() throws Exception {
    //given
    AlertCreationDto simpleAlert = createBasicAlertWithReminder();

    String id = createAlert(simpleAlert);

    triggerAndCompleteCheckJob(id);

    // when
    embeddedOptimizeExtensionRule.stopOptimize();
    embeddedOptimizeExtensionRule.startOptimize();

    assertThat(
      embeddedOptimizeExtensionRule.getAlertService().getScheduler().getJobGroupNames().size(),
      is(2)
    );
  }


}
