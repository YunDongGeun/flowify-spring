package org.github.flowify.execution.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeLog {

    private String nodeId;
    private String status;
    private Map<String, Object> inputData;
    private Map<String, Object> outputData;
    private NodeSnapshot snapshot;
    private ErrorDetail error;
    private Instant startedAt;
    private Instant finishedAt;
}