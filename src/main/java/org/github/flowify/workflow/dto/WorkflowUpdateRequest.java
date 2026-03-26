package org.github.flowify.workflow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.github.flowify.workflow.entity.EdgeDefinition;
import org.github.flowify.workflow.entity.NodeDefinition;
import org.github.flowify.workflow.entity.TriggerConfig;

import java.util.List;

@Getter
@NoArgsConstructor
public class WorkflowUpdateRequest {

    private String name;
    private String description;
    private List<NodeDefinition> nodes;
    private List<EdgeDefinition> edges;
    private TriggerConfig trigger;
    private Boolean isActive;
}
