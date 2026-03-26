package org.github.flowify.workflow.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ShareRequest {

    @NotEmpty(message = "공유할 사용자 ID 목록은 필수입니다.")
    private List<String> userIds;
}
