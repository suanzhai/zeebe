/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.job;

import io.camunda.zeebe.engine.processing.job.behaviour.JobUpdateBehaviour;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor;
import io.camunda.zeebe.protocol.impl.record.value.job.JobRecord;
import io.camunda.zeebe.stream.api.records.TypedRecord;

public final class JobUpdateRetriesProcessor implements TypedRecordProcessor<JobRecord> {

  private final JobUpdateBehaviour jobUpdateBehaviour;

  public JobUpdateRetriesProcessor(final JobUpdateBehaviour jobUpdateBehaviour) {
    this.jobUpdateBehaviour = jobUpdateBehaviour;
  }

  @Override
  public void processRecord(final TypedRecord<JobRecord> command) {
    final long jobKey = command.getKey();

    final var jobRecord = jobUpdateBehaviour.getJobOrAppendRejection(jobKey, command);
    if (jobRecord == null) {
      return;
    }
    jobUpdateBehaviour.updateJobRetries(jobKey, jobRecord, command);
    jobUpdateBehaviour.writeUpdateJobRetriesResponse(jobKey, jobRecord, command);
  }
}
