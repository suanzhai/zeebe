package org.camunda.optimize.dto.optimize.query.report.single;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.camunda.optimize.dto.optimize.query.report.Combinable;
import org.camunda.optimize.dto.optimize.query.report.ReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.ViewDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.FilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.group.GroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.parameters.ParametersDto;
import org.camunda.optimize.dto.optimize.query.report.single.parameters.ProcessPartDto;
import org.camunda.optimize.service.es.report.command.util.ReportUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// TODO OPT-1612 ensures new empty parameters never win over legacy processPart being set
@JsonPropertyOrder({ "parameters", "processPart" })
public class SingleReportDataDto implements ReportDataDto, Combinable {

  protected String processDefinitionKey;
  protected String processDefinitionVersion;
  protected List<FilterDto> filter = new ArrayList<>();
  protected ViewDto view;
  protected GroupByDto groupBy;
  protected String visualization;
  protected Object configuration;
  protected ParametersDto parameters = new ParametersDto();

  public List<FilterDto> getFilter() {
    return filter;
  }

  public void setFilter(List<FilterDto> filter) {
    this.filter = filter;
  }

  public ViewDto getView() {
    return view;
  }

  public void setView(ViewDto view) {
    this.view = view;
  }

  public GroupByDto getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(GroupByDto groupBy) {
    this.groupBy = groupBy;
  }

  public String getVisualization() {
    return visualization;
  }

  public void setVisualization(String visualization) {
    this.visualization = visualization;
  }

  public Object getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Object configuration) {
    this.configuration = configuration;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public void setProcessDefinitionVersion(String processDefinitionVersion) {
    this.processDefinitionVersion = processDefinitionVersion;
  }

  // TODO OPT-1612 just here for rest api backwardscompatibility
  @Deprecated
  public ProcessPartDto getProcessPart() {
    return Optional.ofNullable(getParameters())
      .flatMap(parameters -> Optional.ofNullable(parameters.getProcessPart()))
      .orElse(null);
  }

  // TODO OPT-1612 just here for rest api backwardscompatibility
  @Deprecated
  public void setProcessPart(ProcessPartDto processPart) {
    if (getParameters() != null) {
      getParameters().setProcessPart(processPart);
    } else {
      setParameters(new ParametersDto(processPart));
    }
  }

  public ParametersDto getParameters() {
    return parameters;
  }

  public void setParameters(ParametersDto parameters) {
    this.parameters = parameters;
  }

  @JsonIgnore
  public String createCommandKey() {
    String viewCommandKey = view == null ? "null" : view.createCommandKey();
    String groupByCommandKey = groupBy == null ? "null" : groupBy.createCommandKey();
    String processPartCommandKey = getProcessPart() == null ? "null" : getProcessPart().createCommandKey();
    return viewCommandKey + "_" + groupByCommandKey + "_" + processPartCommandKey;
  }

  @Override
  public boolean isCombinable(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SingleReportDataDto)) {
      return false;
    }
    SingleReportDataDto that = (SingleReportDataDto) o;
    return ReportUtil.isCombinable(view, that.view) &&
      ReportUtil.isCombinable(groupBy, that.groupBy) &&
      Objects.equals(visualization, that.visualization);
  }

  @Override
  public String toString() {
    return "SingleReportDataDto{" +
      "processDefinitionKey='" + processDefinitionKey + '\'' +
      ", processDefinitionVersion='" + processDefinitionVersion + '\'' +
      ", filter=" + filter +
      ", view=" + view +
      ", groupBy=" + groupBy +
      ", visualization='" + visualization + '\'' +
      ", parameters=" + parameters +
      '}';
  }
}
