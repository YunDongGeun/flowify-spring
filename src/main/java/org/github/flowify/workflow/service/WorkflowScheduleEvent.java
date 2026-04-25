package org.github.flowify.workflow.service;

/**
 * WorkflowService가 schedule 트리거 변경 시 발행하는 이벤트.
 * ScheduleTriggerService가 수신하여 스케줄을 등록/해제한다.
 */
public record WorkflowScheduleEvent(
        String workflowId,
        boolean register,
        String cron,
        String timezone
) {
}
