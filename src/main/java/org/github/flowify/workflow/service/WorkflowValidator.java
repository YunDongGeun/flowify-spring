package org.github.flowify.workflow.service;

import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.workflow.dto.ValidationWarning;
import org.github.flowify.workflow.entity.EdgeDefinition;
import org.github.flowify.workflow.entity.NodeDefinition;
import org.github.flowify.workflow.entity.Workflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WorkflowValidator {

    public List<ValidationWarning> validate(Workflow workflow) {
        List<NodeDefinition> nodes = workflow.getNodes();
        List<EdgeDefinition> edges = workflow.getEdges();

        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        checkCyclicReference(nodes, edges);
        checkIsolatedNodes(nodes, edges);
        checkRequiredConfig(nodes);

        return checkDataTypeCompatibility(nodes, edges);
    }

    private void checkCyclicReference(List<NodeDefinition> nodes, List<EdgeDefinition> edges) {
        if (edges == null || edges.isEmpty()) {
            return;
        }

        Map<String, List<String>> adjacency = new HashMap<>();
        for (NodeDefinition node : nodes) {
            adjacency.put(node.getId(), new ArrayList<>());
        }
        for (EdgeDefinition edge : edges) {
            adjacency.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge.getTarget());
        }

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (NodeDefinition node : nodes) {
            if (hasCycle(node.getId(), adjacency, visited, recursionStack)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "워크플로우에 순환 참조가 존재합니다.");
            }
        }
    }

    private boolean hasCycle(String nodeId, Map<String, List<String>> adjacency,
                             Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(nodeId)) {
            return true;
        }
        if (visited.contains(nodeId)) {
            return false;
        }

        visited.add(nodeId);
        recursionStack.add(nodeId);

        List<String> neighbors = adjacency.getOrDefault(nodeId, List.of());
        for (String neighbor : neighbors) {
            if (hasCycle(neighbor, adjacency, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(nodeId);
        return false;
    }

    private void checkIsolatedNodes(List<NodeDefinition> nodes, List<EdgeDefinition> edges) {
        if (nodes.size() <= 1) {
            return;
        }

        if (edges == null || edges.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "연결되지 않은 노드가 존재합니다.");
        }

        Set<String> connectedNodes = new HashSet<>();
        for (EdgeDefinition edge : edges) {
            connectedNodes.add(edge.getSource());
            connectedNodes.add(edge.getTarget());
        }

        for (NodeDefinition node : nodes) {
            if (!connectedNodes.contains(node.getId())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "노드 '" + node.getId() + "'이(가) 연결되지 않았습니다.");
            }
        }
    }

    private void checkRequiredConfig(List<NodeDefinition> nodes) {
        for (NodeDefinition node : nodes) {
            if (node.getCategory() == null || node.getType() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                        "노드 '" + node.getId() + "'의 category 또는 type이 누락되었습니다.");
            }
        }
    }

    private List<ValidationWarning> checkDataTypeCompatibility(List<NodeDefinition> nodes,
                                                                List<EdgeDefinition> edges) {
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, NodeDefinition> nodeMap = nodes.stream()
                .collect(Collectors.toMap(NodeDefinition::getId, Function.identity()));

        List<ValidationWarning> warnings = new ArrayList<>();

        for (EdgeDefinition edge : edges) {
            NodeDefinition source = nodeMap.get(edge.getSource());
            NodeDefinition target = nodeMap.get(edge.getTarget());

            if (source == null || target == null) {
                continue;
            }

            String sourceOutput = source.getOutputDataType();
            String targetInput = target.getDataType();

            if (sourceOutput == null || sourceOutput.isBlank()
                    || targetInput == null || targetInput.isBlank()) {
                continue;
            }

            if (!sourceOutput.equals(targetInput)) {
                warnings.add(ValidationWarning.builder()
                        .nodeId(target.getId())
                        .message("노드 '" + source.getId() + "'의 출력 타입(" + sourceOutput
                                + ")이 노드 '" + target.getId() + "'의 입력 타입(" + targetInput
                                + ")과 호환되지 않습니다.")
                        .sourceType(sourceOutput)
                        .targetType(targetInput)
                        .build());
            }
        }

        return warnings;
    }
}
