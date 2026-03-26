package org.github.flowify.template.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.template.dto.CreateTemplateRequest;
import org.github.flowify.template.entity.Template;
import org.github.flowify.template.service.TemplateService;
import org.github.flowify.user.entity.User;
import org.github.flowify.workflow.dto.WorkflowResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ApiResponse<List<Template>> getTemplates(@RequestParam(required = false) String category) {
        return ApiResponse.ok(templateService.getTemplates(category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Template> getTemplateById(@PathVariable String id) {
        return ApiResponse.ok(templateService.getTemplateById(id));
    }

    @PostMapping("/{id}/instantiate")
    public ApiResponse<WorkflowResponse> instantiateTemplate(Authentication authentication,
                                                              @PathVariable String id) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(templateService.instantiateTemplate(user.getId(), id));
    }

    @PostMapping
    public ApiResponse<Template> createTemplate(Authentication authentication,
                                                @Valid @RequestBody CreateTemplateRequest request) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(templateService.createUserTemplate(user.getId(), request));
    }
}
