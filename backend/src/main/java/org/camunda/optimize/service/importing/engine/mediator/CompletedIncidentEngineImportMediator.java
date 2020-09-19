/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.importing.engine.mediator;

import org.camunda.optimize.dto.engine.HistoricIncidentEngineDto;
import org.camunda.optimize.service.importing.TimestampBasedImportMediator;
import org.camunda.optimize.service.importing.engine.fetcher.instance.CompletedIncidentFetcher;
import org.camunda.optimize.service.importing.engine.handler.CompletedIncidentImportIndexHandler;
import org.camunda.optimize.service.importing.engine.service.incident.CompletedIncidentImportService;
import org.camunda.optimize.service.util.BackoffCalculator;
import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CompletedIncidentEngineImportMediator
  extends TimestampBasedImportMediator<CompletedIncidentImportIndexHandler, HistoricIncidentEngineDto> {

  private final CompletedIncidentFetcher engineEntityFetcher;

  public CompletedIncidentEngineImportMediator(final CompletedIncidentImportIndexHandler importIndexHandler,
                                               final CompletedIncidentFetcher engineEntityFetcher,
                                               final CompletedIncidentImportService importService,
                                               final ConfigurationService configurationService,
                                               final BackoffCalculator idleBackoffCalculator) {
    this.importIndexHandler = importIndexHandler;
    this.engineEntityFetcher = engineEntityFetcher;
    this.importService = importService;
    this.configurationService = configurationService;
    this.idleBackoffCalculator = idleBackoffCalculator;
  }

  @Override
  protected OffsetDateTime getTimestamp(final HistoricIncidentEngineDto historicIncidentEngineDto) {
    return historicIncidentEngineDto.getEndTime();
  }

  @Override
  protected List<HistoricIncidentEngineDto> getEntitiesNextPage() {
    return engineEntityFetcher.fetchCompletedIncidents(importIndexHandler.getNextPage());
  }

  @Override
  protected List<HistoricIncidentEngineDto> getEntitiesLastTimestamp() {
    return engineEntityFetcher.fetchCompletedIncidentsForTimestamp(importIndexHandler.getTimestampOfLastEntity());
  }

  @Override
  protected int getMaxPageSize() {
    return configurationService.getEngineImportActivityInstanceMaxPageSize();
  }
}
