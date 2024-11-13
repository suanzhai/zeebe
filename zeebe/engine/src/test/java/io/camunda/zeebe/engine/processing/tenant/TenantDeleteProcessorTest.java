/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.tenant;

import static io.camunda.zeebe.protocol.record.Assertions.assertThat;

import io.camunda.zeebe.engine.util.EngineRule;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.TenantIntent;
import io.camunda.zeebe.test.util.record.RecordingExporter;
import io.camunda.zeebe.test.util.record.RecordingExporterTestWatcher;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class TenantDeleteProcessorTest {

  @Rule public final EngineRule engine = EngineRule.singlePartition();
  @Rule public final TestWatcher recordingExporterTestWatcher = new RecordingExporterTestWatcher();

  @Test
  public void shouldDeleteTenant() {
    // Given
    final var tenantId = UUID.randomUUID().toString();
    final var tenantName = UUID.randomUUID().toString();
    final var tenantKey =
        engine
            .tenant()
            .newTenant()
            .withTenantId(tenantId)
            .withName(tenantName)
            .create()
            .getValue()
            .getTenantKey();
    assertThat(engine.getProcessingState().getTenantState().getTenantByKey(tenantKey).get())
        .isNotNull();

    // When
    engine.tenant().deleteTenant(tenantKey).delete();

    // Then confirms the Tenant.DELETED event was written
    assertThat(
            RecordingExporter.tenantRecords(TenantIntent.DELETED)
                .withTenantKey(tenantKey)
                .getFirst()
                .getValue())
        .hasTenantKey(tenantKey)
        .hasTenantId(tenantId)
        .hasName(tenantName);
  }

  @Test
  public void shouldRejectIfTenantDoesNotExist() {
    // When
    final var notPresentTenantKey = 1L;
    final var rejectedDeleteRecord =
        engine.tenant().deleteTenant(notPresentTenantKey).expectRejection().delete();
    // Then
    assertThat(rejectedDeleteRecord)
        .hasRejectionType(RejectionType.NOT_FOUND)
        .hasRejectionReason(
            "Expected to delete tenant with key '%s', but no tenant with this key exists."
                .formatted(notPresentTenantKey));
  }
}
