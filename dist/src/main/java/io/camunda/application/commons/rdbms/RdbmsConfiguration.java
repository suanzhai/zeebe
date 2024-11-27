/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.application.commons.rdbms;

import io.camunda.db.rdbms.RdbmsService;
import io.camunda.db.rdbms.read.service.DecisionDefinitionReader;
import io.camunda.db.rdbms.read.service.DecisionInstanceReader;
import io.camunda.db.rdbms.read.service.DecisionRequirementsReader;
import io.camunda.db.rdbms.read.service.FlowNodeInstanceReader;
import io.camunda.db.rdbms.read.service.FormReader;
import io.camunda.db.rdbms.read.service.MappingReader;
import io.camunda.db.rdbms.read.service.ProcessDefinitionReader;
import io.camunda.db.rdbms.read.service.ProcessInstanceReader;
import io.camunda.db.rdbms.read.service.TenantReader;
import io.camunda.db.rdbms.read.service.UserReader;
import io.camunda.db.rdbms.read.service.UserTaskReader;
import io.camunda.db.rdbms.read.service.VariableReader;
import io.camunda.db.rdbms.sql.DecisionDefinitionMapper;
import io.camunda.db.rdbms.sql.DecisionInstanceMapper;
import io.camunda.db.rdbms.sql.DecisionRequirementsMapper;
import io.camunda.db.rdbms.sql.ExporterPositionMapper;
import io.camunda.db.rdbms.sql.FlowNodeInstanceMapper;
import io.camunda.db.rdbms.sql.FormMapper;
import io.camunda.db.rdbms.sql.MappingMapper;
import io.camunda.db.rdbms.sql.ProcessDefinitionMapper;
import io.camunda.db.rdbms.sql.ProcessInstanceMapper;
import io.camunda.db.rdbms.sql.TenantMapper;
import io.camunda.db.rdbms.sql.UserMapper;
import io.camunda.db.rdbms.sql.UserTaskMapper;
import io.camunda.db.rdbms.sql.VariableMapper;
import io.camunda.db.rdbms.write.RdbmsWriterFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "camunda.database", name = "type", havingValue = "rdbms")
@Import(MyBatisConfiguration.class)
public class RdbmsConfiguration {

  @Bean
  public VariableReader variableRdbmsReader(final VariableMapper variableMapper) {
    return new VariableReader(variableMapper);
  }

  @Bean
  public DecisionDefinitionReader decisionDefinitionReader(
      final DecisionDefinitionMapper decisionDefinitionMapper) {
    return new DecisionDefinitionReader(decisionDefinitionMapper);
  }

  @Bean
  public DecisionInstanceReader decisionInstanceReader(
      final DecisionInstanceMapper decisionInstanceMapper) {
    return new DecisionInstanceReader(decisionInstanceMapper);
  }

  @Bean
  public DecisionRequirementsReader decisionRequirementsReader(
      final DecisionRequirementsMapper decisionRequirementsMapper) {
    return new DecisionRequirementsReader(decisionRequirementsMapper);
  }

  @Bean
  public FlowNodeInstanceReader flowNodeInstanceReader(
      final FlowNodeInstanceMapper flowNodeInstanceMapper) {
    return new FlowNodeInstanceReader(flowNodeInstanceMapper);
  }

  @Bean
  public ProcessDefinitionReader processDeploymentRdbmsReader(
      final ProcessDefinitionMapper processDefinitionMapper) {
    return new ProcessDefinitionReader(processDefinitionMapper);
  }

  @Bean
  public ProcessInstanceReader processRdbmsReader(
      final ProcessInstanceMapper processInstanceMapper) {
    return new ProcessInstanceReader(processInstanceMapper);
  }

  @Bean
  public TenantReader tenantReader(final TenantMapper tenantMapper) {
    return new TenantReader(tenantMapper);
  }

  @Bean
  public UserReader userRdbmsReader(final UserMapper userTaskMapper) {
    return new UserReader(userTaskMapper);
  }

  @Bean
  public UserTaskReader userTaskRdbmsReader(final UserTaskMapper userTaskMapper) {
    return new UserTaskReader(userTaskMapper);
  }

  @Bean
  public FormReader formRdbmsReader(final FormMapper formMapper) {
    return new FormReader(formMapper);
  }

  @Bean
  public MappingReader mappingRdbmsReader(final MappingMapper mappingMapper) {
    return new MappingReader(mappingMapper);
  }

  @Bean
  public RdbmsWriterFactory rdbmsWriterFactory(
      final SqlSessionFactory sqlSessionFactory,
      final ExporterPositionMapper exporterPositionMapper) {
    return new RdbmsWriterFactory(sqlSessionFactory, exporterPositionMapper);
  }

  @Bean
  public RdbmsService rdbmsService(
      final RdbmsWriterFactory rdbmsWriterFactory,
      final VariableReader variableReader,
      final DecisionDefinitionReader decisionDefinitionReader,
      final DecisionInstanceReader decisionInstanceReader,
      final DecisionRequirementsReader decisionRequirementsReader,
      final FlowNodeInstanceReader flowNodeInstanceReader,
      final ProcessDefinitionReader processDefinitionReader,
      final ProcessInstanceReader processInstanceReader,
      final TenantReader tenantReader,
      final UserReader userReader,
      final UserTaskReader userTaskReader,
      final FormReader formReader,
      final MappingReader mappingReader) {
    return new RdbmsService(
        rdbmsWriterFactory,
        decisionDefinitionReader,
        decisionInstanceReader,
        decisionRequirementsReader,
        flowNodeInstanceReader,
        processDefinitionReader,
        processInstanceReader,
        tenantReader,
        variableReader,
        userReader,
        userTaskReader,
        formReader,
        mappingReader);
  }
}
