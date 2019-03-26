package org.camunda.optimize.service.engine.importing.service;

import org.camunda.optimize.dto.engine.HistoricProcessInstanceDto;
import org.camunda.optimize.dto.optimize.importing.ProcessInstanceDto;
import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.es.ElasticsearchImportJobExecutor;
import org.camunda.optimize.service.es.job.ElasticsearchImportJob;
import org.camunda.optimize.service.es.job.importing.RunningProcessInstanceElasticsearchImportJob;
import org.camunda.optimize.service.es.writer.RunningProcessInstanceWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class RunningProcessInstanceImportService implements ImportService<HistoricProcessInstanceDto> {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected ElasticsearchImportJobExecutor elasticsearchImportJobExecutor;
  protected EngineContext engineContext;
  private RunningProcessInstanceWriter runningProcessInstanceWriter;

  public RunningProcessInstanceImportService(
    RunningProcessInstanceWriter runningProcessInstanceWriter,
    ElasticsearchImportJobExecutor elasticsearchImportJobExecutor,
    EngineContext engineContext
  ) {
    this.elasticsearchImportJobExecutor = elasticsearchImportJobExecutor;
    this.engineContext = engineContext;
    this.runningProcessInstanceWriter = runningProcessInstanceWriter;
  }

  @Override
  public void executeImport(List<HistoricProcessInstanceDto> pageOfEngineEntities, Runnable callback) {
    logger.trace("Importing entities from engine...");

    boolean newDataIsAvailable = !pageOfEngineEntities.isEmpty();
    if (newDataIsAvailable) {
      List<ProcessInstanceDto> newOptimizeEntities = mapEngineEntitiesToOptimizeEntities(pageOfEngineEntities);
      ElasticsearchImportJob<ProcessInstanceDto> elasticsearchImportJob =
        createElasticsearchImportJob(newOptimizeEntities, callback);
      addElasticsearchImportJobToQueue(elasticsearchImportJob);
    }
  }

  private void addElasticsearchImportJobToQueue(ElasticsearchImportJob elasticsearchImportJob) {
    elasticsearchImportJobExecutor.executeImportJob(elasticsearchImportJob);
  }

  private List<ProcessInstanceDto> mapEngineEntitiesToOptimizeEntities(List<HistoricProcessInstanceDto>
                                                                         engineEntities) {
    return engineEntities
      .stream().map(this::mapEngineEntityToOptimizeEntity)
      .collect(Collectors.toList());
  }

  private ElasticsearchImportJob<ProcessInstanceDto> createElasticsearchImportJob(List<ProcessInstanceDto>
                                                                                    processInstances, Runnable
                                                                                    callback) {
    RunningProcessInstanceElasticsearchImportJob importJob =
      new RunningProcessInstanceElasticsearchImportJob(runningProcessInstanceWriter, callback);
    importJob.setEntitiesToImport(processInstances);
    return importJob;
  }

  private ProcessInstanceDto mapEngineEntityToOptimizeEntity(HistoricProcessInstanceDto engineEntity) {
    ProcessInstanceDto processInstanceDto = new ProcessInstanceDto();
    processInstanceDto.setProcessDefinitionKey(engineEntity.getProcessDefinitionKey());
    processInstanceDto.setProcessDefinitionVersion(engineEntity.getProcessDefinitionVersionAsString());
    processInstanceDto.setProcessDefinitionId(engineEntity.getProcessDefinitionId());
    processInstanceDto.setProcessInstanceId(engineEntity.getId());
    processInstanceDto.setBusinessKey(engineEntity.getBusinessKey());
    processInstanceDto.setStartDate(engineEntity.getStartTime());
    processInstanceDto.setState(engineEntity.getState());
    processInstanceDto.setEndDate(null);
    processInstanceDto.setEngine(engineContext.getEngineAlias());
    return processInstanceDto;
  }

}
