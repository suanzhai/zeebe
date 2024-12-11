/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.camunda.security.auth.Authentication;
import io.camunda.service.JobServices;
import io.camunda.service.JobServices.UpdateJobChangeset;
import io.camunda.zeebe.gateway.protocol.rest.JobActivationResponse;
import io.camunda.zeebe.gateway.rest.RestControllerTest;
import io.camunda.zeebe.protocol.impl.record.value.job.JobRecord;
import io.camunda.zeebe.protocol.impl.record.value.job.JobResult;
import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskRecord;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(JobController.class)
public class JobControllerTest extends RestControllerTest {

  static final String JOBS_BASE_URL = "/v2/jobs";

  @MockBean JobServices<JobActivationResponse> jobServices;
  @MockBean ResponseObserverProvider responseObserverProvider;

  @BeforeEach
  void setup() {
    when(jobServices.withAuthentication(any(Authentication.class))).thenReturn(jobServices);
  }

  @Test
  void shouldFailJob() {
    // given
    when(jobServices.failJob(anyLong(), anyInt(), anyString(), anyLong(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
            {
              "retries": 1,
              "errorMessage": "error",
              "retryBackOff": 1,
              "variables": {
                "foo": "bar"
              }
            }""";
    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/failure")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).failJob(1L, 1, "error", 1L, Map.of("foo", "bar"));
  }

  @Test
  void shouldFailJobWithoutBody() {
    // given
    when(jobServices.failJob(anyLong(), anyInt(), anyString(), anyLong(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/failure")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).failJob(1L, 0, "", 0L, Map.of());
  }

  @Test
  void shouldFailJobWithEmptyBody() {
    // given
    when(jobServices.failJob(anyLong(), anyInt(), anyString(), anyLong(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
            {}
            """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/failure")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).failJob(1L, 0, "", 0L, Map.of());
  }

  @Test
  void shouldThrowErrorJob() {
    // given
    when(jobServices.errorJob(anyLong(), anyString(), anyString(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
            {
              "errorCode": "400",
              "errorMessage": "error",
              "variables": {
                "foo": "bar"
              }
            }""";
    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/error")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).errorJob(1L, "400", "error", Map.of("foo", "bar"));
  }

  @Test
  void shouldRejectThrowErrorJobWithoutBody() {
    // given
    final var expectedBody =
        """
            {
              "type": "about:blank",
              "status": 400,
              "title": "Bad Request",
              "detail": "Required request body is missing",
              "instance": "%s"
            }"""
            .formatted(JOBS_BASE_URL + "/1/error");

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/error")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .json(expectedBody);
  }

  @Test
  void shouldRejectThrowErrorJobWithoutErrorCode() {
    // given
    final var request =
        """
            {
              "errorMessage": "error",
              "variables": {
                "foo": "bar"
              }
            }""";

    final var expectedBody =
        """
            {
              "type": "about:blank",
              "status": 400,
              "title": "INVALID_ARGUMENT",
              "detail": "No errorCode provided.",
              "instance": "%s"
            }"""
            .formatted(JOBS_BASE_URL + "/1/error");

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/error")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .json(expectedBody);
  }

  @Test
  void shouldRejectThrowErrorJobWithEmptyErrorCode() {
    // given
    final var request =
        """
            {
              "errorCode": "",
              "errorMessage": "error",
              "variables": {
                "foo": "bar"
              }
            }""";

    final var expectedBody =
        """
            {
              "type": "about:blank",
              "status": 400,
              "title": "INVALID_ARGUMENT",
              "detail": "No errorCode provided.",
              "instance": "%s"
            }"""
            .formatted(JOBS_BASE_URL + "/1/error");

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/error")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .json(expectedBody);
  }

  @Test
  void shouldRejectThrowErrorJobWithOnlySpacesErrorCode() {
    // given
    final var request =
        """
            {
              "errorCode": "    ",
              "errorMessage": "error",
              "variables": {
                "foo": "bar"
              }
            }""";

    final var expectedBody =
        """
            {
              "type": "about:blank",
              "status": 400,
              "title": "INVALID_ARGUMENT",
              "detail": "No errorCode provided.",
              "instance": "%s"
            }"""
            .formatted(JOBS_BASE_URL + "/1/error");

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/error")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .json(expectedBody);
  }

  @Test
  void shouldCompleteJob() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));
    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).completeJob(eq(1L), eq(Map.of()), any(JobResult.class));
  }

  @Test
  void shouldCompleteJobWithResultDeniedTrue() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "result": {
              "denied": true,
              "corrections": {}
            }
          }
        """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    final ArgumentCaptor<JobResult> jobResultArgumentCaptor =
        ArgumentCaptor.forClass(JobResult.class);
    Mockito.verify(jobServices)
        .completeJob(eq(1L), eq(Map.of()), jobResultArgumentCaptor.capture());
    Assertions.assertTrue(jobResultArgumentCaptor.getValue().isDenied());
  }

  @Test
  void shouldCompleteJobWithResultWithCorrections() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "result": {
              "denied": false,
              "corrections": {
                "assignee": "Test",
                "dueDate": "2025-05-23T01:02:03+01:00",
                "followUpDate": "2025-05-25T01:02:03+01:00",
                "candidateUsersList": ["UserA", "UserB"],
                "candidateGroupsList": ["GroupA", "GroupB"],
                "priority": 20
              }
            }
          }
        """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    final ArgumentCaptor<JobResult> jobResultArgumentCaptor =
        ArgumentCaptor.forClass(JobResult.class);
    Mockito.verify(jobServices)
        .completeJob(eq(1L), eq(Map.of()), jobResultArgumentCaptor.capture());

    Assertions.assertEquals(
        "Test", jobResultArgumentCaptor.getValue().getCorrections().getAssignee());
    Assertions.assertEquals(
        "2025-05-23T01:02:03+01:00",
        jobResultArgumentCaptor.getValue().getCorrections().getDueDate());
    Assertions.assertEquals(
        "2025-05-25T01:02:03+01:00",
        jobResultArgumentCaptor.getValue().getCorrections().getFollowUpDate());
    Assertions.assertEquals(
        List.of("UserA", "UserB"),
        jobResultArgumentCaptor.getValue().getCorrections().getCandidateUsers());
    Assertions.assertEquals(
        List.of("GroupA", "GroupB"),
        jobResultArgumentCaptor.getValue().getCorrections().getCandidateGroups());
    Assertions.assertEquals(20, jobResultArgumentCaptor.getValue().getCorrections().getPriority());

    Assertions.assertEquals(
        List.of(
            "assignee",
            UserTaskRecord.DUE_DATE,
            UserTaskRecord.FOLLOW_UP_DATE,
            UserTaskRecord.CANDIDATE_USERS,
            UserTaskRecord.CANDIDATE_GROUPS,
            UserTaskRecord.PRIORITY),
        jobResultArgumentCaptor.getValue().getCorrectedAttributes());
  }

  @Test
  void shouldCompleteJobWithResultWithCorrectionsPartiallySet() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "result": {
              "denied": false,
              "corrections": {
                "assignee": "Test",
                "candidateUsersList": ["UserA", "UserB"],
                "candidateGroupsList": ["GroupA", "GroupB"],
                "priority": 20
              }
            }
          }
        """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    final ArgumentCaptor<JobResult> jobResultArgumentCaptor =
        ArgumentCaptor.forClass(JobResult.class);
    Mockito.verify(jobServices)
        .completeJob(eq(1L), eq(Map.of()), jobResultArgumentCaptor.capture());
    Assertions.assertEquals(
        "Test", jobResultArgumentCaptor.getValue().getCorrections().getAssignee());
    Assertions.assertEquals("", jobResultArgumentCaptor.getValue().getCorrections().getDueDate());
    Assertions.assertEquals(
        "", jobResultArgumentCaptor.getValue().getCorrections().getFollowUpDate());
    Assertions.assertEquals(
        List.of("UserA", "UserB"),
        jobResultArgumentCaptor.getValue().getCorrections().getCandidateUsers());
    Assertions.assertEquals(
        List.of("GroupA", "GroupB"),
        jobResultArgumentCaptor.getValue().getCorrections().getCandidateGroups());
    Assertions.assertEquals(20, jobResultArgumentCaptor.getValue().getCorrections().getPriority());

    Assertions.assertEquals(
        List.of(
            "assignee",
            UserTaskRecord.CANDIDATE_USERS,
            UserTaskRecord.CANDIDATE_GROUPS,
            UserTaskRecord.PRIORITY),
        jobResultArgumentCaptor.getValue().getCorrectedAttributes());
  }

  @Test
  void shouldCompleteJobWithResultDeniedFalse() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "result": {
              "denied": false,
              "corrections": {}
            }
          }
        """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    final ArgumentCaptor<JobResult> jobResultArgumentCaptor =
        ArgumentCaptor.forClass(JobResult.class);
    Mockito.verify(jobServices)
        .completeJob(eq(1L), eq(Map.of()), jobResultArgumentCaptor.capture());
    Assertions.assertFalse(jobResultArgumentCaptor.getValue().isDenied());
  }

  @Test
  void shouldCompleteJobWithResultAndIgnoreUnknownField() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "result": {
              "unknownField": true,
              "corrections": {}
            }
          }
        """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    final ArgumentCaptor<JobResult> jobResultArgumentCaptor =
        ArgumentCaptor.forClass(JobResult.class);
    Mockito.verify(jobServices)
        .completeJob(eq(1L), eq(Map.of()), jobResultArgumentCaptor.capture());
    Assertions.assertFalse(jobResultArgumentCaptor.getValue().isDenied());
  }

  @Test
  void shouldCompleteJobWithVariables() {
    // given
    when(jobServices.completeJob(anyLong(), any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "variables": {
              "foo": "bar"
            }
          }
        """;

    // when/then
    webClient
        .post()
        .uri(JOBS_BASE_URL + "/1/completion")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).completeJob(eq(1L), eq(Map.of("foo", "bar")), any(JobResult.class));
  }

  @Test
  void shouldUpdateJob() {
    // given
    when(jobServices.updateJob(anyLong(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "changeset": {
              "retries": 5,
              "timeout": 1000
            }
          }
        """;
    // when/then
    webClient
        .patch()
        .uri(JOBS_BASE_URL + "/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).updateJob(1L, new UpdateJobChangeset(5, 1000L));
  }

  @Test
  void shouldUpdateJobWithOnlyRetries() {
    // given
    when(jobServices.updateJob(anyLong(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
          {
            "changeset": {
              "retries": 5
            }
          }
        """;
    // when/then
    webClient
        .patch()
        .uri(JOBS_BASE_URL + "/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).updateJob(1L, new UpdateJobChangeset(5, null));
  }

  @Test
  void shouldUpdateJobWithOnlyTimeout() {
    // given
    when(jobServices.updateJob(anyLong(), any()))
        .thenReturn(CompletableFuture.completedFuture(new JobRecord()));

    final var request =
        """
            {
              "changeset": {
                "timeout": 1000
              }
            }""";
    // when/then
    webClient
        .patch()
        .uri(JOBS_BASE_URL + "/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isNoContent();

    Mockito.verify(jobServices).updateJob(1L, new UpdateJobChangeset(null, 1000L));
  }

  @Test
  void shouldRejectUpdateJob() {
    // given
    final var request =
        """
          {
            "changeset": {}
          }
        """;

    final var expectedBody =
        """
            {
              "type": "about:blank",
              "status": 400,
              "title": "INVALID_ARGUMENT",
              "detail": "At least one of [retries, timeout] is required.",
              "instance": "%s"
            }"""
            .formatted(JOBS_BASE_URL + "/1");

    // when/then
    webClient
        .patch()
        .uri(JOBS_BASE_URL + "/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .json(expectedBody);
  }

  @Test
  void shouldRejectUpdateJobNoBody() {
    // given
    final var expectedBody =
        """
            {
              "type": "about:blank",
              "status": 400,
              "title": "Bad Request",
              "detail": "Required request body is missing",
              "instance": "%s"
            }"""
            .formatted(JOBS_BASE_URL + "/1");

    // when/then
    webClient
        .patch()
        .uri(JOBS_BASE_URL + "/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .json(expectedBody);
  }
}
