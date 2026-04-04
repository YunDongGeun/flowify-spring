package org.github.flowify.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ValidationWarning {

    private final String nodeId;
    private final String message;
    private final String sourceType;
    private final String targetType;
}
