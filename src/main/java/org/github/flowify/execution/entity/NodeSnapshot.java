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
public class NodeSnapshot {

    private Instant capturedAt;
    private Map<String, Object> stateData;
}