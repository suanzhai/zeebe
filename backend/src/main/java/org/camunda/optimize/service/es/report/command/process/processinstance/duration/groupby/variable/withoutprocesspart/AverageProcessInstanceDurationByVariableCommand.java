package org.camunda.optimize.service.es.report.command.process.processinstance.duration.groupby.variable.withoutprocesspart;

import org.camunda.optimize.service.es.report.command.process.processinstance.duration.groupby.variable.AbstractProcessInstanceDurationByVariableCommand;
import org.camunda.optimize.service.es.report.command.util.ElasticsearchAggregationResultMappingUtil;
import org.camunda.optimize.service.es.schema.type.ProcessInstanceType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.avg.ParsedAvg;

import static org.elasticsearch.search.aggregations.AggregationBuilders.avg;

public class AverageProcessInstanceDurationByVariableCommand
  extends AbstractProcessInstanceDurationByVariableCommand {

  @Override
  protected long processAggregationOperation(Aggregations aggs) {
    ParsedAvg aggregation = aggs.get(DURATION_AGGREGATION);
    return ElasticsearchAggregationResultMappingUtil.mapToLong(aggregation);
  }

  @Override
  protected AggregationBuilder createAggregationOperation() {
    return avg(DURATION_AGGREGATION)
      .field(ProcessInstanceType.DURATION);
  }
}
