package org.camunda.optimize.service.es.report.command.process.processinstance.duration.groupby.date.withoutprocesspart;

import org.camunda.optimize.service.es.report.command.process.processinstance.duration.groupby.date.AbstractProcessInstanceDurationGroupByStartDateCommand;
import org.camunda.optimize.service.es.report.command.util.ElasticsearchAggregationResultMappingUtil;
import org.camunda.optimize.service.es.schema.type.ProcessInstanceType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.percentiles.tdigest.ParsedTDigestPercentiles;

public class MedianProcessInstanceDurationGroupByStartDateCommand extends
  AbstractProcessInstanceDurationGroupByStartDateCommand {

  @Override
  protected AggregationBuilder createAggregationOperation() {
    return AggregationBuilders
      .percentiles(DURATION_AGGREGATION)
      .percentiles(50)
      .field(ProcessInstanceType.DURATION);
  }

  @Override
  protected long processAggregationOperation(Aggregations aggs) {
    ParsedTDigestPercentiles aggregation = aggs.get(DURATION_AGGREGATION);
    return ElasticsearchAggregationResultMappingUtil.mapToLong(aggregation);
  }

}
