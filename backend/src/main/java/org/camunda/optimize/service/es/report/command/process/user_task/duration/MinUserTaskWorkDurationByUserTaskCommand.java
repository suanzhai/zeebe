package org.camunda.optimize.service.es.report.command.process.user_task.duration;

import org.camunda.optimize.service.es.schema.type.UserTaskInstanceType;

public class MinUserTaskWorkDurationByUserTaskCommand extends MinUserTaskDurationByUserTaskCommand {
  @Override
  protected String getDurationFieldName() {
    return UserTaskInstanceType.WORK_DURATION;
  }
}
