/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.dto.optimize.query.report.single.process.filter.util;

import org.camunda.optimize.dto.optimize.query.report.single.process.filter.ProcessFilterDto;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.optimize.util.SuppressionConstants.UNCHECKED_CAST;

public class ProcessFilterBuilder {

  private final List<ProcessFilterDto<?>> filters = new ArrayList<>();

  public static ProcessFilterBuilder filter() {
    return new ProcessFilterBuilder();
  }

  public ExecutedFlowNodeFilterBuilder executedFlowNodes() {
    return ExecutedFlowNodeFilterBuilder.construct(this);
  }

  public ExecutingFlowNodeFilterBuilder executingFlowNodes() {
    return ExecutingFlowNodeFilterBuilder.construct(this);
  }

  public CanceledFlowNodeFilterBuilder canceledFlowNodes() {
    return CanceledFlowNodeFilterBuilder.construct(this);
  }

  public CanceledInstancesOnlyFilterBuilder canceledInstancesOnly() {
    return CanceledInstancesOnlyFilterBuilder.construct(this);
  }

  public NonCanceledInstancesOnlyFilterBuilder nonCanceledInstancesOnly() {
    return NonCanceledInstancesOnlyFilterBuilder.construct(this);
  }

  public SuspendedInstancesOnlyFilterBuilder suspendedInstancesOnly() {
    return SuspendedInstancesOnlyFilterBuilder.construct(this);
  }

  public NonSuspendedInstancesOnlyFilterBuilder nonSuspendedInstancesOnly() {
    return NonSuspendedInstancesOnlyFilterBuilder.construct(this);
  }

  public CompletedInstancesOnlyFilterBuilder completedInstancesOnly() {
    return CompletedInstancesOnlyFilterBuilder.construct(this);
  }

  public RunningInstancesOnlyFilterBuilder runningInstancesOnly() {
    return RunningInstancesOnlyFilterBuilder.construct(this);
  }

  public OpenIncidentFilterBuilder withOpenIncident() {
    return OpenIncidentFilterBuilder.construct(this);
  }

  public ResolvedIncidentFilterBuilder withResolvedIncident() {
    return ResolvedIncidentFilterBuilder.construct(this);
  }

  public NoIncidentFilterBuilder noIncidents() {
    return NoIncidentFilterBuilder.construct(this);
  }

  public RollingDateFilterBuilder rollingEndDate() {
    return RollingDateFilterBuilder.endDate(this);
  }

  public RollingDateFilterBuilder rollingStartDate() {
    return RollingDateFilterBuilder.startDate(this);
  }

  public RelativeDateFilterBuilder relativeStartDate() {
    return RelativeDateFilterBuilder.startDate(this);
  }

  public FixedDateFilterBuilder fixedEndDate() {
    return FixedDateFilterBuilder.endDate(this);
  }

  public FixedDateFilterBuilder fixedStartDate() {
    return FixedDateFilterBuilder.startDate(this);
  }

  public DurationFilterBuilder duration() {
    return DurationFilterBuilder.construct(this);
  }

  public VariableFilterBuilder variable() {
    return VariableFilterBuilder.construct(this);
  }

  public FlowNodeDurationFilterBuilder flowNodeDuration() {
    return FlowNodeDurationFilterBuilder.construct(this);
  }

  public IdentityLinkFilterBuilder assignee() {
    return IdentityLinkFilterBuilder.constructAssigneeFilterBuilder(this);
  }

  public IdentityLinkFilterBuilder candidateGroups() {
    return IdentityLinkFilterBuilder.constructCandidateGroupFilterBuilder(this);
  }

  void addFilter(ProcessFilterDto<?> result) {
    filters.add(result);
  }

  @SuppressWarnings(UNCHECKED_CAST)
  public <T extends ProcessFilterDto<?>> List<T> buildList() {
    return (List<T>) filters;
  }
}
