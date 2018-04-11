package org.camunda.optimize.service.engine.importing.index.handler;

import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.engine.importing.index.page.ImportPage;

import java.util.Optional;
import java.util.OptionalDouble;

public interface ImportIndexHandler<PAGE extends ImportPage, INDEX> {

  /**
   * Retrieves all information to import a new page from the engine. With
   * especially an offset where to start the import and the number of
   * instances to fetch.
   */
  PAGE getNextPage();

  /**
   * Creates a data transfer object (DTO) about an index to store that
   * to Elasticsearch. On every restart of Optimize this information
   * can be used to continue the import where it stopped the last time.
   */
  INDEX createIndexInformationForStoring();

  /**
   * Initializes the import index.
   */
  void readIndexFromElasticsearch();


  /**
   * Resets the import index such that it can start the import
   * all over again. E.g., that can be helpful to import
   * entities that were missed during the first round.
   */
  void resetImportIndex();

  void restartImportCycle();

  EngineContext getEngineContext();
}
