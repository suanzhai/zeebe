/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.service.search.sort;

import io.camunda.util.ObjectBuilder;
import java.util.List;
import java.util.function.Function;

public final record ProcessInstanceSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static ProcessInstanceSort of(
      final Function<Builder, ObjectBuilder<ProcessInstanceSort>> fn) {
    return SortOptionBuilders.processInstance(fn);
  }

  public static final class Builder extends SortOption.AbstractBuilder<Builder>
      implements ObjectBuilder<ProcessInstanceSort> {

    public Builder processInstanceKey() {
      currentOrdering = new FieldSorting("key", null);
      return this;
    }

    public Builder startDate() {
      currentOrdering = new FieldSorting("startDate", null);
      return this;
    }

    public Builder endDate() {
      currentOrdering = new FieldSorting("endDate", null);
      return this;
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public ProcessInstanceSort build() {
      return new ProcessInstanceSort(orderings);
    }
  }
}
