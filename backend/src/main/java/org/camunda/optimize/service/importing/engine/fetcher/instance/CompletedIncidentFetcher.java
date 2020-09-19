/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.importing.engine.fetcher.instance;

import org.camunda.optimize.dto.engine.HistoricIncidentEngineDto;
import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.importing.page.TimestampBasedImportPage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.camunda.optimize.service.util.importing.EngineConstants.COMPLETED_INCIDENT_ENDPOINT;
import static org.camunda.optimize.service.util.importing.EngineConstants.FINISHED_AFTER;
import static org.camunda.optimize.service.util.importing.EngineConstants.FINISHED_AT;
import static org.camunda.optimize.service.util.importing.EngineConstants.MAX_RESULTS_TO_RETURN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CompletedIncidentFetcher
  extends RetryBackoffEngineEntityFetcher<HistoricIncidentEngineDto> {

  private DateTimeFormatter dateTimeFormatter;

  public CompletedIncidentFetcher(final EngineContext engineContext) {
    super(engineContext);
  }

  @PostConstruct
  public void init() {
    dateTimeFormatter = DateTimeFormatter.ofPattern(configurationService.getEngineDateFormat());
  }

  public List<HistoricIncidentEngineDto> fetchCompletedIncidents(TimestampBasedImportPage page) {
    return fetchCompletedIncidents(
      page.getTimestampOfLastEntity(),
      configurationService.getEngineImportIncidentMaxPageSize()
    );
  }

  public List<HistoricIncidentEngineDto> fetchCompletedIncidentsForTimestamp(
    OffsetDateTime endTimeOfLastInstance) {
    logger.debug("Fetching completed incidents ...");
    long requestStart = System.currentTimeMillis();
    List<HistoricIncidentEngineDto> secondEntries =
      fetchWithRetry(() -> performCompletedIncidentRequest(endTimeOfLastInstance));
    long requestEnd = System.currentTimeMillis();
    logger.debug(
      "Fetched [{}] historic incidents for set end time within [{}] ms",
      secondEntries.size(),
      requestEnd - requestStart
    );
    return secondEntries;
  }

  private List<HistoricIncidentEngineDto> fetchCompletedIncidents(OffsetDateTime timeStamp,
                                                                  long pageSize) {
    logger.debug("Fetching historic incidents ...");
    long requestStart = System.currentTimeMillis();
    List<HistoricIncidentEngineDto> entries =
      fetchWithRetry(() -> performCompletedIncidentRequest(timeStamp, pageSize));
    long requestEnd = System.currentTimeMillis();
    logger.debug(
      "Fetched [{}] historic incidents which ended after set timestamp with page size [{}] within [{}] ms",
      entries.size(),
      pageSize,
      requestEnd - requestStart
    );

    return entries;
  }

  private List<HistoricIncidentEngineDto> performCompletedIncidentRequest(OffsetDateTime timeStamp,
                                                                          long pageSize) {
    return getEngineClient()
      .target(configurationService.getEngineRestApiEndpointOfCustomEngine(getEngineAlias()))
      .path(COMPLETED_INCIDENT_ENDPOINT)
      .queryParam(FINISHED_AFTER, dateTimeFormatter.format(timeStamp))
      .queryParam(MAX_RESULTS_TO_RETURN, pageSize)
      .request(MediaType.APPLICATION_JSON)
      .acceptEncoding(UTF8)
      .get(new GenericType<List<HistoricIncidentEngineDto>>() {
      });
  }

  private List<HistoricIncidentEngineDto> performCompletedIncidentRequest(OffsetDateTime endTimeOfLastInstance) {
    return getEngineClient()
      .target(configurationService.getEngineRestApiEndpointOfCustomEngine(getEngineAlias()))
      .path(COMPLETED_INCIDENT_ENDPOINT)
      .queryParam(FINISHED_AT, dateTimeFormatter.format(endTimeOfLastInstance))
      .queryParam(MAX_RESULTS_TO_RETURN, configurationService.getEngineImportIncidentMaxPageSize())
      .request(MediaType.APPLICATION_JSON)
      .acceptEncoding(UTF8)
      .get(new GenericType<List<HistoricIncidentEngineDto>>() {
      });
  }

}
