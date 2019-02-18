package org.camunda.optimize.service.es.report.command.process.util;

import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.ProcessGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.parameters.ProcessPartDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.view.ProcessViewDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.view.ProcessViewOperation;

import static org.camunda.optimize.service.es.report.command.process.util.ProcessGroupByDtoCreator.createGroupByFlowNode;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessGroupByDtoCreator.createGroupByNone;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessGroupByDtoCreator.createGroupByStartDateDto;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessGroupByDtoCreator.createGroupByVariable;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createAverageFlowNodeDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createAverageProcessInstanceDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createCountFlowNodeFrequencyView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createCountProcessInstanceFrequencyView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createMaxFlowNodeDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createMaxProcessInstanceDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createMedianFlowNodeDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createMedianProcessInstanceDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createMinFlowNodeDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createMinProcessInstanceDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createRawDataView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createUserTaskIdleDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createUserTaskTotalDurationView;
import static org.camunda.optimize.service.es.report.command.process.util.ProcessViewDtoCreator.createUserTaskWorkDurationView;

public class ProcessReportDataCreator {

  public static ProcessReportDataDto createAverageFlowNodeDurationGroupByFlowNodeReport() {
    ProcessViewDto view = createAverageFlowNodeDurationView();
    ProcessGroupByDto groupByDto = createGroupByFlowNode();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMinFlowNodeDurationGroupByFlowNodeReport() {
    ProcessViewDto view = createMinFlowNodeDurationView();
    ProcessGroupByDto groupByDto = createGroupByFlowNode();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMaxFlowNodeDurationGroupByFlowNodeReport() {
    ProcessViewDto view = createMaxFlowNodeDurationView();
    ProcessGroupByDto groupByDto = createGroupByFlowNode();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMedianFlowNodeDurationGroupByFlowNodeReport() {
    ProcessViewDto view = createMedianFlowNodeDurationView();
    ProcessGroupByDto groupByDto = createGroupByFlowNode();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createAverageUserTaskIdleDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskIdleDurationView(ProcessViewOperation.AVG));
  }

  public static ProcessReportDataDto createMinUserTaskIdleDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskIdleDurationView(ProcessViewOperation.MIN));
  }

  public static ProcessReportDataDto createMaxUserTaskIdleDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskIdleDurationView(ProcessViewOperation.MAX));
  }

  public static ProcessReportDataDto createMedianUserTaskIdleDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskIdleDurationView(ProcessViewOperation.MEDIAN));
  }

  public static ProcessReportDataDto createAverageUserTaskTotalDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskTotalDurationView(ProcessViewOperation.AVG));
  }

  public static ProcessReportDataDto createMinUserTaskTotalDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskTotalDurationView(ProcessViewOperation.MIN));
  }

  public static ProcessReportDataDto createMaxUserTaskTotalDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskTotalDurationView(ProcessViewOperation.MAX));
  }

  public static ProcessReportDataDto createMedianUserTaskTotalDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskTotalDurationView(ProcessViewOperation.MEDIAN));
  }

  public static ProcessReportDataDto createAverageUserTaskWorkDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskWorkDurationView(ProcessViewOperation.AVG));
  }

  public static ProcessReportDataDto createMinUserTaskWorkDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskWorkDurationView(ProcessViewOperation.MIN));
  }

  public static ProcessReportDataDto createMaxUserTaskWorkDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskWorkDurationView(ProcessViewOperation.MAX));
  }

  public static ProcessReportDataDto createMedianUserTaskWorkDurationGroupByUserTaskReport() {
    return createUserTaskReportWithView(createUserTaskWorkDurationView(ProcessViewOperation.MEDIAN));
  }

  private static ProcessReportDataDto createUserTaskReportWithView(final ProcessViewDto view) {
    final ProcessGroupByDto groupByDto = createGroupByFlowNode();

    final ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createAverageProcessInstanceDurationGroupByNoneReport() {
    ProcessViewDto view = createAverageProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createAverageProcessInstanceDurationGroupByNoneWithProcessPartReport() {
    ProcessViewDto view = createAverageProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMinProcessInstanceDurationGroupByNoneReport() {
    ProcessViewDto view = createMinProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMinProcessInstanceDurationGroupByNoneWithProcessPartReport() {
    ProcessViewDto view = createMinProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMaxProcessInstanceDurationGroupByNoneReport() {
    ProcessViewDto view = createMaxProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMaxProcessInstanceDurationGroupByNoneWithProcessPartReport() {
    ProcessViewDto view = createMaxProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMedianProcessInstanceDurationGroupByNoneReport() {
    ProcessViewDto view = createMedianProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMedianProcessInstanceDurationGroupByNoneWithProcessPartReport() {
    ProcessViewDto view = createMedianProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createAverageProcessInstanceDurationGroupByStartDateReport() {
    ProcessViewDto view = createAverageProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createAverageProcessInstanceDurationGroupByStartDateWithProcessPartReport() {
    ProcessViewDto view = createAverageProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMinProcessInstanceDurationGroupByStartDateReport() {
    ProcessViewDto view = createMinProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMinProcessInstanceDurationGroupByStartDateWithProcessPartReport() {
    ProcessViewDto view = createMinProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMaxProcessInstanceDurationGroupByStartDateReport() {
    ProcessViewDto view = createMaxProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMaxProcessInstanceDurationGroupByStartDateWithProcessPartReport() {
    ProcessViewDto view = createMaxProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMedianProcessInstanceDurationGroupByStartDateReport() {
    ProcessViewDto view = createMedianProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMedianProcessInstanceDurationGroupByStartDateWithProcessPartReport() {
    ProcessViewDto view = createMedianProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createAverageProcessInstanceDurationGroupByVariableReport() {
    ProcessViewDto view = createAverageProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByVariable();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createAverageProcessInstanceDurationGroupByVariableWithProcessPartReport() {
    ProcessReportDataDto reportData = createAverageProcessInstanceDurationGroupByVariableReport();
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMinProcessInstanceDurationGroupByVariableReport() {
    ProcessViewDto view = createMinProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByVariable();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMinProcessInstanceDurationGroupByVariableWithProcessPartReport() {
    ProcessReportDataDto reportData = createMinProcessInstanceDurationGroupByVariableReport();
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMaxProcessInstanceDurationGroupByVariableReport() {
    ProcessViewDto view = createMaxProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByVariable();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMaxProcessInstanceDurationGroupByVariableWithProcessPartReport() {
    ProcessReportDataDto reportData = createMaxProcessInstanceDurationGroupByVariableReport();
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createMedianProcessInstanceDurationGroupByVariableReport() {
    ProcessViewDto view = createMedianProcessInstanceDurationView();
    ProcessGroupByDto groupByDto = createGroupByVariable();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createMedianProcessInstanceDurationGroupByVariableWithProcessPartReport() {
    ProcessReportDataDto reportData = createMedianProcessInstanceDurationGroupByVariableReport();
    reportData.getParameters().setProcessPart(new ProcessPartDto());
    return reportData;
  }

  public static ProcessReportDataDto createRawDataReport() {
    ProcessViewDto view = createRawDataView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createCountProcessInstanceFrequencyGroupByNoneReport() {
    ProcessViewDto view = createCountProcessInstanceFrequencyView();
    ProcessGroupByDto groupByDto = createGroupByNone();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createCountProcessInstanceFrequencyGroupByStartDateReport() {
    ProcessViewDto view = createCountProcessInstanceFrequencyView();
    ProcessGroupByDto groupByDto = createGroupByStartDateDto();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createCountProcessInstanceFrequencyGroupByVariableReport() {
    ProcessViewDto view = createCountProcessInstanceFrequencyView();
    ProcessGroupByDto groupByDto = createGroupByVariable();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }

  public static ProcessReportDataDto createCountFlowNodeFrequencyGroupByFlowNodeReport() {
    ProcessViewDto view = createCountFlowNodeFrequencyView();
    ProcessGroupByDto groupByDto = createGroupByFlowNode();

    ProcessReportDataDto reportData = new ProcessReportDataDto();
    reportData.setView(view);
    reportData.setGroupBy(groupByDto);
    return reportData;
  }


}
