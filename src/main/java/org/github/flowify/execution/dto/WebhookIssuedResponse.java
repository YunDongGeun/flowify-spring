package org.github.flowify.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebhookIssuedResponse {

    private String webhookId;

    /** 발급 시에만 포함. GET 조회 시에는 null. */
    private String secret;
}
