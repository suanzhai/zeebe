/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest.controller.usermanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.search.entities.RoleEntity;
import io.camunda.search.exception.NotFoundException;
import io.camunda.search.page.SearchQueryPage;
import io.camunda.search.query.RoleQuery;
import io.camunda.search.query.SearchQueryResult;
import io.camunda.search.sort.RoleSort;
import io.camunda.security.auth.Authentication;
import io.camunda.service.RoleServices;
import io.camunda.zeebe.gateway.rest.RestControllerTest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(value = RoleQueryController.class, properties = "camunda.rest.query.enabled=true")
public class RoleQueryControllerTest extends RestControllerTest {
  private static final String ROLE_BASE_URL = "/v2/roles";

  @MockBean private RoleServices roleServices;

  @BeforeEach
  void setup() {
    when(roleServices.withAuthentication(any(Authentication.class))).thenReturn(roleServices);
  }

  @Test
  void getRoleShouldReturnOk() {
    // given
    final var role = new RoleEntity(100L, "Role Name", Set.of());
    when(roleServices.getRole(role.key())).thenReturn(role);

    // when
    webClient
        .get()
        .uri("%s/%s".formatted(ROLE_BASE_URL, role.key()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(
            """
            {
              "name": "Role Name",
              "key": 100
            }""");

    // then
    verify(roleServices, times(1)).getRole(role.key());
  }

  @Test
  void getNonExistingRoleShouldReturnNotFound() {
    // given
    final var roleKey = 100L;
    final var path = "%s/%s".formatted(ROLE_BASE_URL, roleKey);
    when(roleServices.getRole(roleKey)).thenThrow(new NotFoundException("role not found"));

    // when
    webClient
        .get()
        .uri(path)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .json(
            """
            {
              "type": "about:blank",
              "title": "NOT_FOUND",
              "status": 404,
              "detail": "role not found",
              "instance": "%s"
            }"""
                .formatted(path));

    // then
    verify(roleServices, times(1)).getRole(roleKey);
  }

  @Test
  void shouldSearchRolesWithEmptyQuery() {
    // given
    when(roleServices.search(any(RoleQuery.class)))
        .thenReturn(
            new SearchQueryResult.Builder<RoleEntity>()
                .total(3)
                .sortValues(new Object[] {})
                .items(
                    List.of(
                        new RoleEntity(100L, "Role 1", Set.of()),
                        new RoleEntity(200L, "Role 2", Set.of(1L, 2L)),
                        new RoleEntity(300L, "Role 12", Set.of(3L))))
                .build());

    // when / then
    webClient
        .post()
        .uri("%s/search".formatted(ROLE_BASE_URL))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{}")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .json(
            """
          {
             "items": [
               {
                 "key": 100,
                 "name": "Role 1",
                 "assignedMemberKeys": []
               },
               {
                 "key": 200,
                 "name": "Role 2",
                 "assignedMemberKeys": [1, 2]
               },
               {
                 "key": 300,
                 "name": "Role 12",
                 "assignedMemberKeys": [3]
               }
             ],
             "page": {
               "totalItems": 3,
               "firstSortValues": [],
               "lastSortValues": []
             }
           }""");

    verify(roleServices).search(new RoleQuery.Builder().build());
  }

  @Test
  void shouldSortAndPaginateSearchResult() {
    // given
    when(roleServices.search(any(RoleQuery.class)))
        .thenReturn(
            new SearchQueryResult.Builder<RoleEntity>()
                .total(3)
                .items(
                    List.of(
                        new RoleEntity(100L, "Role 1", Set.of()),
                        new RoleEntity(300L, "Role 12", Set.of()),
                        new RoleEntity(200L, "Role 2", Set.of())))
                .build());

    // when / then
    webClient
        .post()
        .uri("%s/search".formatted(ROLE_BASE_URL))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            {
              "sort":  [{"field": "name", "order":  "asc"}],
              "page":  {"from":  20, "limit":  10}
            }
             """)
        .exchange()
        .expectStatus()
        .isOk();

    verify(roleServices)
        .search(
            new RoleQuery.Builder()
                .sort(RoleSort.of(builder -> builder.name().asc()))
                .page(SearchQueryPage.of(builder -> builder.from(20).size(10)))
                .build());
  }
}
