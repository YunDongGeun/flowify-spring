package org.github.flowify.workflow.service;

import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.workflow.entity.EdgeDefinition;
import org.github.flowify.workflow.entity.NodeDefinition;
import org.github.flowify.workflow.entity.Workflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WorkflowValidator {

    public void validate(Workflow workflow) {
        List<NodeDefinition> nodes = workflow.getNodes();
        List<EdgeDefinition> edges = workflow.getEdges();

        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        checkCyclicReference(nodes, edges);
        checkIsolatedNodes(nodes, edges);
        checkRequiredConfig(nodes);
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
}
