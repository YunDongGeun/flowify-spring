package org.github.flowify.execution.service;

import lombok.RequiredArgsConstructor;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.execution.dto.WebhookIssuedResponse;
import org.github.flowify.workflow.entity.TriggerConfig;
import org.github.flowify.workflow.entity.Workflow;
import org.github.flowify.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WorkflowRepository workflowRepository;
    private final ExecutionService executionService;

    public WebhookIssuedResponse issueWebhook(String userId, String workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND));

        if (!workflow.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        String webhookId = UUID.randomUUID().toString().replace("-", "");
        byte[] secretBytes = new byte[32];
        new SecureRandom().nextBytes(secretBytes);
        String secretHex = HexFormat.of().formatHex(secretBytes);

        Map<String, Object> config = buildUpdatedConfig(workflow.getTrigger());
        config.put("webhookId", webhookId);
        config.put("secret", secretHex);

        workflow.setTrigger(TriggerConfig.builder()
                .type("webhook")
                .config(config)
                .build());
        workflowRepository.save(workflow);

        return new WebhookIssuedResponse(webhookId, secretHex);
    }

    public void revokeWebhook(String userId, String workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND));

        if (!workflow.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        TriggerConfig trigger = workflow.getTrigger();
        if (trigger != null && trigger.getConfig() != null) {
            Map<String, Object> config = new HashMap<>(trigger.getConfig());
            config.remove("webhookId");
            config.remove("secret");
            workflow.setTrigger(TriggerConfig.builder()
                    .type(trigger.getType())
                    .config(config)
                    .build());
            workflowRepository.save(workflow);
        }
    }

    public WebhookIssuedResponse getWebhookInfo(String userId, String workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND));

        if (!workflow.getUserId().equals(userId) && !workflow.getSharedWith().contains(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        TriggerConfig trigger = workflow.getTrigger();
        if (trigger == null || trigger.getConfig() == null) {
            throw new BusinessException(ErrorCode.WEBHOOK_NOT_FOUND);
        }

        String webhookId = (String) trigger.getConfig().get("webhookId");
        if (webhookId == null) {
            throw new BusinessException(ErrorCode.WEBHOOK_NOT_FOUND);
        }

        return new WebhookIssuedResponse(webhookId, null);
    }

    public String processWebhook(String webhookId, String signature, String rawBody,
                                  Map<String, Object> payload) {
        Workflow workflow = workflowRepository.findByWebhookId(webhookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEBHOOK_NOT_FOUND));

        TriggerConfig trigger = workflow.getTrigger();
        String secret = (String) trigger.getConfig().get("secret");

        if (secret != null) {
            if (signature == null || !verifySignature(rawBody, signature, secret)) {
                throw new BusinessException(ErrorCode.WEBHOOK_SIGNATURE_INVALID);
            }
        }

        return executionService.executeFromWebhook(workflow.getId(), payload);
    }

    private boolean verifySignature(String rawBody, String providedSignature, String secretHex) {
        try {
            byte[] secretBytes = HexFormat.of().parseHex(secretHex);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> buildUpdatedConfig(TriggerConfig trigger) {
        Map<String, Object> config = new HashMap<>();
        if (trigger != null && trigger.getConfig() != null) {
            config.putAll(trigger.getConfig());
        }
        return config;
    }
}
