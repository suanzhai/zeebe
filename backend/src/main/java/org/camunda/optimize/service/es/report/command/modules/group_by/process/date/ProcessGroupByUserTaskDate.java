/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.report.command.modules.group_by.process.date;

import lombok.extern.slf4j.Slf4j;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.service.es.report.MinMaxStatsService;
import org.camunda.optimize.service.es.report.command.exec.ExecutionContext;
import org.camunda.optimize.service.es.report.command.service.DateAggregationService;
import org.elasticsearch.index.query.QueryBuilder;

import static org.camunda.optimize.service.es.filter.util.modelelement.UserTaskFilterQueryUtil.createUserTaskAggregationFilter;
import static org.camunda.optimize.service.es.schema.index.ProcessInstanceIndex.USER_TASKS;

@Slf4j
public abstract class ProcessGroupByUserTaskDate extends AbstractProcessGroupByModelElementDate {

  ProcessGroupByUserTaskDate(final DateAggregationService dateAggregationService,
                             final MinMaxStatsService minMaxStatsService) {
    super(dateAggregationService, minMaxStatsService);
  }

  @Override
  protected QueryBuilder getFilterQuery(final ExecutionContext<ProcessReportDataDto> context) {
    return createUserTaskAggregationFilter(context.getReportData());
  }

  @Override
  protected String getPathToElementField() {
    return USER_TASKS;
  }

}
