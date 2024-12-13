/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.it.tasklist;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.application.Profile;
import io.camunda.client.ZeebeClient;
import io.camunda.client.protocol.rest.PermissionTypeEnum;
import io.camunda.client.protocol.rest.ResourceTypeEnum;
import io.camunda.qa.util.cluster.TestRestTasklistClient;
import io.camunda.qa.util.cluster.TestStandaloneCamunda;
import io.camunda.zeebe.it.util.AuthorizationsUtil;
import io.camunda.zeebe.it.util.AuthorizationsUtil.Permissions;
import io.camunda.zeebe.it.util.SearchClientsUtil;
import io.camunda.zeebe.qa.util.junit.ZeebeIntegration;
import io.camunda.zeebe.qa.util.junit.ZeebeIntegration.TestZeebe;
import io.camunda.zeebe.test.util.junit.AutoCloseResources;
import io.camunda.zeebe.test.util.junit.AutoCloseResources.AutoCloseResource;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@AutoCloseResources
@ZeebeIntegration
public class TasklistGetProcessDefinitionAuthorizationIT {

  private static final String PROCESS_ID = "foo";

  private static final String ADMIN_USER_NAME = "foo";
  private static final String ADMIN_USER_PASSWORD = "foo";

  private static final String TEST_USER_NAME = "bar";
  private static final String TEST_USER_PASSWORD = "bar";
  private static long testUserKey;

  @AutoCloseResource private static AuthorizationsUtil adminAuthClient;
  @AutoCloseResource private static ZeebeClient adminCamundaClient;
  @AutoCloseResource private static TestRestTasklistClient tasklistRestClient;

  private static long processDefinitionKey;

  @TestZeebe
  private TestStandaloneCamunda broker =
      new TestStandaloneCamunda()
          .withCamundaExporter()
          .withSecurityConfig(c -> c.getAuthorizations().setEnabled(true))
          .withAdditionalProfile(Profile.AUTH_BASIC);

  @BeforeEach
  public void beforeAll() {
    final var defaultUser = "demo";
    final var searchClients =
        SearchClientsUtil.createSearchClients(broker.getElasticSearchHostAddress());

    // intermediate state, so that a user exists that has
    // access to the storage to retrieve data
    try (final var intermediateAuthClient =
        AuthorizationsUtil.create(broker, broker.getElasticSearchHostAddress())) {
      intermediateAuthClient.awaitUserExistsInElasticsearch(defaultUser);
      intermediateAuthClient.createUserWithPermissions(
          ADMIN_USER_NAME,
          ADMIN_USER_PASSWORD,
          new Permissions(ResourceTypeEnum.DEPLOYMENT, PermissionTypeEnum.CREATE, List.of("*")),
          new Permissions(
              ResourceTypeEnum.PROCESS_DEFINITION, PermissionTypeEnum.READ, List.of("*")),
          new Permissions(ResourceTypeEnum.USER, PermissionTypeEnum.CREATE, List.of("*")),
          new Permissions(ResourceTypeEnum.AUTHORIZATION, PermissionTypeEnum.CREATE, List.of("*")),
          new Permissions(ResourceTypeEnum.AUTHORIZATION, PermissionTypeEnum.UPDATE, List.of("*")));
    }

    adminCamundaClient =
        AuthorizationsUtil.createClient(broker, ADMIN_USER_NAME, ADMIN_USER_PASSWORD);
    adminAuthClient = new AuthorizationsUtil(broker, adminCamundaClient, searchClients);
    tasklistRestClient = broker.newTasklistClient();

    // deploy a process as admin user
    processDefinitionKey =
        deployResource(adminCamundaClient, "process/process_with_assigned_user_task.bpmn");
    waitForProcessToBeDeployed(PROCESS_ID);

    // create new (non-admin) user
    testUserKey = adminAuthClient.createUser(TEST_USER_NAME, TEST_USER_PASSWORD);
  }

  @Test
  public void shouldNotReturnProcessDefinitionWithUnauthorizedUser() {
    // given (non-admin) user without any authorizations

    // when
    final var response =
        tasklistRestClient
            .withAuthentication(TEST_USER_NAME, TEST_USER_PASSWORD)
            .getProcessDefinition(processDefinitionKey);

    // then
    assertThat(response).isNotNull();
    assertThat(response.statusCode()).isEqualTo(403);
  }

  @Test
  public void shouldBeAuthorizedToRetrieveProcessDefinition() {
    // given
    adminAuthClient.createPermissions(
        testUserKey,
        new Permissions(
            ResourceTypeEnum.PROCESS_DEFINITION, PermissionTypeEnum.READ, List.of(PROCESS_ID)));

    // when
    final var response =
        tasklistRestClient
            .withAuthentication(TEST_USER_NAME, TEST_USER_PASSWORD)
            .getProcessDefinition(processDefinitionKey);

    // then
    assertThat(response).isNotNull();
    assertThat(response.statusCode()).isEqualTo(200);
  }

  private long deployResource(final ZeebeClient zeebeClient, final String resource) {
    return zeebeClient
        .newDeployResourceCommand()
        .addResourceFromClasspath(resource)
        .send()
        .join()
        .getProcesses()
        .getFirst()
        .getProcessDefinitionKey();
  }

  private void waitForProcessToBeDeployed(final String processDefinitionId) {
    Awaitility.await("should deploy process %s and export".formatted(processDefinitionId))
        .atMost(Duration.ofSeconds(15))
        .ignoreExceptions() // Ignore exceptions and continue retrying
        .untilAsserted(
            () -> {
              final var result =
                  adminCamundaClient
                      .newProcessDefinitionQuery()
                      .filter(f -> f.processDefinitionId(processDefinitionId))
                      .send()
                      .join();
              assertThat(result.items().size()).isEqualTo(1);
            });
  }

  public static long createProcessInstance(final String processDefinitionId) {
    return adminCamundaClient
        .newCreateInstanceCommand()
        .bpmnProcessId(processDefinitionId)
        .latestVersion()
        .send()
        .join()
        .getProcessInstanceKey();
  }
}
