/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.immutable;

import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskRecord;
import java.util.Map;

public interface UserTaskState {

  LifecycleState getLifecycleState(final long userTaskKey);

  UserTaskRecord getUserTask(final long userTaskKey);

  UserTaskRecord getUserTask(final long userTaskKey, final Map<String, Object> authorizations);

  UserTaskRecord getUserTaskIntermediateState(final long userTaskKey);

  enum LifecycleState {
    NOT_FOUND((byte) 0),
    CREATING((byte) 1),
    CREATED((byte) 2),

    COMPLETING((byte) 3),
    CANCELING((byte) 4),

    ASSIGNING((byte) 5),

    UPDATING((byte) 6);

    final byte value;

    LifecycleState(final byte value) {
      this.value = value;
    }
  }
}
