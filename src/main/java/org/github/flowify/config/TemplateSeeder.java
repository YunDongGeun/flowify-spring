package org.github.flowify.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.flowify.template.entity.Template;
import org.github.flowify.template.repository.TemplateRepository;
import org.github.flowify.workflow.entity.EdgeDefinition;
import org.github.flowify.workflow.entity.NodeDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateSeeder implements CommandLineRunner {

    private final TemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        if (templateRepository.count() > 0) {
            log.info("시스템 템플릿이 이미 존재합니다. 시드 데이터를 건너뜁니다.");
            return;
        }

        List<Template> templates = List.of(
                buildStudyNoteTemplate(),
                buildMeetingMinutesTemplate(),
                buildNewsCrawlTemplate(),
                buildSheetReportTemplate()
        );

        templateRepository.saveAll(templates);
        log.info("{}개의 시스템 템플릿이 생성되었습니다.", templates.size());
    }

    private Template buildStudyNoteTemplate() {
        NodeDefinition input = NodeDefinition.builder()
                .id("node_1").category("storage").type("google_drive")
                .role("start").dataType("FILE_LIST").outputDataType("FILE_LIST")
                .build();
        NodeDefinition ai = NodeDefinition.builder()
                .id("node_2").category("ai").type("AI")
                .role("middle").dataType("SINGLE_FILE").outputDataType("TEXT")
                .build();
        NodeDefinition output = NodeDefinition.builder()
                .id("node_3").category("storage").type("notion")
                .role("end").dataType("TEXT")
                .build();

        return Template.builder()
                .name("학습 노트 자동 생성")
                .description("Google Drive 파일을 AI로 요약하여 Notion에 저장합니다.")
                .category("storage")
                .icon("book")
                .nodes(List.of(input, ai, output))
                .edges(List.of(
                        EdgeDefinition.builder().source("node_1").target("node_2").build(),
                        EdgeDefinition.builder().source("node_2").target("node_3").build()))
                .requiredServices(List.of("google", "notion"))
                .isSystem(true)
                .build();
    }

    private Template buildMeetingMinutesTemplate() {
        NodeDefinition input = NodeDefinition.builder()
                .id("node_1").category("storage").type("google_drive")
                .role("start").dataType("SINGLE_FILE").outputDataType("SINGLE_FILE")
                .build();
        NodeDefinition ai = NodeDefinition.builder()
                .id("node_2").category("ai").type("AI")
                .role("middle").dataType("SINGLE_FILE").outputDataType("TEXT")
                .build();
        NodeDefinition output = NodeDefinition.builder()
                .id("node_3").category("communication").type("slack")
                .role("end").dataType("TEXT")
                .build();

        return Template.builder()
                .name("회의록 요약 및 공유")
                .description("회의 녹취를 AI로 정리하여 Slack으로 전송합니다.")
                .category("communication")
                .icon("message")
                .nodes(List.of(input, ai, output))
                .edges(List.of(
                        EdgeDefinition.builder().source("node_1").target("node_2").build(),
                        EdgeDefinition.builder().source("node_2").target("node_3").build()))
                .requiredServices(List.of("google", "slack"))
                .isSystem(true)
                .build();
    }

    private Template buildNewsCrawlTemplate() {
        NodeDefinition input = NodeDefinition.builder()
                .id("node_1").category("web_crawl").type("naver_news")
                .role("start").dataType("TEXT").outputDataType("TEXT")
                .build();
        NodeDefinition ai = NodeDefinition.builder()
                .id("node_2").category("ai").type("AI")
                .role("middle").dataType("TEXT").outputDataType("SPREADSHEET_DATA")
                .build();
        NodeDefinition output = NodeDefinition.builder()
                .id("node_3").category("spreadsheet").type("google_sheets")
                .role("end").dataType("SPREADSHEET_DATA")
                .build();

        return Template.builder()
                .name("뉴스 수집 및 정리")
                .description("네이버 뉴스를 수집하고 AI로 요약하여 Google Sheets에 기록합니다.")
                .category("web_crawl")
                .icon("newspaper")
                .nodes(List.of(input, ai, output))
                .edges(List.of(
                        EdgeDefinition.builder().source("node_1").target("node_2").build(),
                        EdgeDefinition.builder().source("node_2").target("node_3").build()))
                .requiredServices(List.of("google"))
                .isSystem(true)
                .build();
    }

    private Template buildSheetReportTemplate() {
        NodeDefinition input = NodeDefinition.builder()
                .id("node_1").category("spreadsheet").type("google_sheets")
                .role("start").dataType("SPREADSHEET_DATA").outputDataType("SPREADSHEET_DATA")
                .build();
        NodeDefinition ai = NodeDefinition.builder()
                .id("node_2").category("ai").type("AI")
                .role("middle").dataType("SPREADSHEET_DATA").outputDataType("TEXT")
                .build();
        NodeDefinition output = NodeDefinition.builder()
                .id("node_3").category("storage").type("google_drive")
                .role("end").dataType("TEXT")
                .build();

        return Template.builder()
                .name("구글 시트 → 리포트 생성")
                .description("Google Sheets 데이터를 AI로 분석하여 리포트를 생성합니다.")
                .category("spreadsheet")
                .icon("chart")
                .nodes(List.of(input, ai, output))
                .edges(List.of(
                        EdgeDefinition.builder().source("node_1").target("node_2").build(),
                        EdgeDefinition.builder().source("node_2").target("node_3").build()))
                .requiredServices(List.of("google"))
                .isSystem(true)
                .build();
    }
}
