/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.it.authorization;

import static io.camunda.zeebe.it.util.AuthorizationsUtil.createClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.application.Profile;
import io.camunda.client.ZeebeClient;
import io.camunda.client.api.command.ProblemException;
import io.camunda.client.protocol.rest.PermissionTypeEnum;
import io.camunda.client.protocol.rest.ResourceTypeEnum;
import io.camunda.zeebe.it.util.AuthorizationsUtil;
import io.camunda.zeebe.it.util.AuthorizationsUtil.Permissions;
import io.camunda.zeebe.qa.util.cluster.TestStandaloneBroker;
import io.camunda.zeebe.qa.util.junit.ZeebeIntegration;
import io.camunda.zeebe.qa.util.junit.ZeebeIntegration.TestZeebe;
import io.camunda.zeebe.test.util.junit.AutoCloseResources;
import io.camunda.zeebe.test.util.junit.AutoCloseResources.AutoCloseResource;
import io.camunda.zeebe.test.util.testcontainers.TestSearchContainers;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoCloseResources
@Testcontainers
@ZeebeIntegration
public class DecisionEvaluationEvaluateAuthorizationIT {

  @Container
  private static final ElasticsearchContainer CONTAINER =
      TestSearchContainers.createDefeaultElasticsearchContainer();

  private static final String DECISION_ID = "jedi_or_sith";
  private static AuthorizationsUtil authUtil;
  @AutoCloseResource private static ZeebeClient defaultUserClient;

  @TestZeebe(autoStart = false)
  private TestStandaloneBroker broker =
      new TestStandaloneBroker()
          .withRecordingExporter(true)
          .withSecurityConfig(c -> c.getAuthorizations().setEnabled(true))
          .withAdditionalProfile(Profile.AUTH_BASIC);

  @BeforeEach
  void beforeEach() {
    broker.withCamundaExporter("http://" + CONTAINER.getHttpHostAddress());
    broker.start();

    final var defaultUsername = "demo";
    defaultUserClient = createClient(broker, defaultUsername, "demo");
    authUtil = new AuthorizationsUtil(broker, defaultUserClient, CONTAINER.getHttpHostAddress());

    authUtil.awaitUserExistsInElasticsearch(defaultUsername);
    defaultUserClient
        .newDeployResourceCommand()
        .addResourceFromClasspath("dmn/drg-force-user.dmn")
        .send()
        .join();
  }

  @Test
  void shouldBeAuthorizedToEvaluateDecisionWithDefaultUser() {
    // when
    final var response =
        defaultUserClient
            .newEvaluateDecisionCommand()
            .decisionId(DECISION_ID)
            .variables(Map.of("lightsaberColor", "red"))
            .send()
            .join();

    // then
    assertThat(response.getDecisionOutput()).isEqualTo("\"Sith\"");
  }

  @Test
  void shouldBeAuthorizedToEvaluateDecisionWithUser() {
    // given
    final var username = UUID.randomUUID().toString();
    final var password = "password";
    authUtil.createUserWithPermissions(
        username,
        password,
        new Permissions(
            ResourceTypeEnum.DECISION_DEFINITION,
            PermissionTypeEnum.CREATE_DECISION_INSTANCE,
            List.of(DECISION_ID)));

    try (final var client = authUtil.createClient(username, password)) {
      // when
      final var response =
          client
              .newEvaluateDecisionCommand()
              .decisionId(DECISION_ID)
              .variables(Map.of("lightsaberColor", "red"))
              .send()
              .join();

      // then
      assertThat(response.getDecisionOutput()).isEqualTo("\"Sith\"");
    }
  }

  @Test
  void shouldBeUnauthorizedToEvaluateDecisionIfNoPermissions() {
    // given
    final var username = UUID.randomUUID().toString();
    final var password = "password";
    authUtil.createUser(username, password);

    try (final var client = authUtil.createClient(username, password)) {
      // when
      final var response =
          client
              .newEvaluateDecisionCommand()
              .decisionId(DECISION_ID)
              .variables(Map.of("lightsaberColor", "red"))
              .send();

      // then
      assertThatThrownBy(response::join)
          .isInstanceOf(ProblemException.class)
          .hasMessageContaining("title: FORBIDDEN")
          .hasMessageContaining("status: 403")
          .hasMessageContaining(
              "Insufficient permissions to perform operation 'CREATE_DECISION_INSTANCE' on resource 'DECISION_DEFINITION', required resource identifiers are one of '[*, %s]'",
              DECISION_ID);
    }
  }
}
