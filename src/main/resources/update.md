## 8. 백엔드 정합 체크리스트

### 8.1 타입 정합

| # | 항목 | 현행 | 필요 조치 | 우선순위 |
| --- | --- | --- | --- | --- |
| T-1 | `ApiResponse` 타입 | `{ data, status, serverDateTime }` | 백엔드 스펙 `{ success, data, message, errorCode }`로 교체 | 🔴 높음 |
| T-2 | `Workflow` 엔티티 | 6개 필드 | `description`, `userId`, `isTemplate`, `templateId`, `trigger`, `isActive` 추가 | 🔴 높음 |
| T-3 | `DataType` 열거형 | 6개, kebab-case | 8개, UPPER_SNAKE_CASE로 통일 | 🔴 높음 |
| T-4 | `FlowNodeData` | `inputTypes[]`, `outputTypes[]` | `role`, `dataType`, `outputDataType` (단일), `authWarning` 추가 | 🟡 중간 |
| T-5 | `NodeDefinition` 직렬화 | React Flow `Node<FlowNodeData>` 직접 전송 | 백엔드 `NodeDefinition` 형식으로 변환 유틸 필요 | 🟡 중간 |
| T-6 | `ExecutionStatus` | 프론트 4종 | 백엔드 실행 상태와 매핑 확인 | 🟢 낮음 |

### 8.2 API 연동

| # | 항목 | 현행 | 필요 조치 | 우선순위 |
| --- | --- | --- | --- | --- |
| A-1 | 인증 API | 없음 | `auth.api.ts` 신규 생성 | 🔴 높음 |
| A-2 | 리프레시 토큰 인터셉터 | 없음 | `client.ts`에 자동 갱신 로직 추가 | 🔴 높음 |
| A-3 | 선택지 매핑 API | 없음 | `choice.api.ts` 신규 생성 | 🔴 높음 |
| A-4 | 실행 API | execute만 존재 | `execution.api.ts`로 분리, 상태/로그/스냅샷 추가 | 🟡 중간 |
| A-5 | 템플릿 API | 없음 | `template.api.ts` 신규 생성 | 🟡 중간 |
| A-6 | OAuth 토큰 API | 없음 | `oauth-token.api.ts` 신규 생성 | 🟡 중간 |
| A-7 | 사용자 API | 없음 | `user.api.ts` 신규 생성 | 🟢 낮음 |
| A-8 | LLM 생성 API | 없음 | `workflowApi.generate` 추가 | 🟢 낮음 |
| A-9 | 유효성 검증 API | 없음 | `workflowApi.validate` 추가 | 🟢 낮음 |

### 8.3 기능 구현

| # | UC 식별자 | 기능 | 현행 | 필요 조치 | 우선순위 |
| --- | --- | --- | --- | --- | --- |
| F-1 | UC-U01 | Google SSO 로그인 | 스텁 | LoginPage + GoogleLoginButton 구현 | 🔴 높음 |
| F-2 | UC-W01-A~D | 가이드형 노드 설정 | ⚠️ 부분 | Canvas useMemo 분기 완성, ChoicePanel 연동 | 🔴 높음 |
| F-3 | UC-CM01 | 동적 선택지 제공 | 없음 | choice-mapping feature 전체 구현 | 🔴 높음 |
| F-4 | UC-W01 (목록) | 워크플로우 목록 | 스텁 | WorkflowsPage + WorkflowListWidget 구현 | 🟡 중간 |
| F-5 | UC-W03 | 템플릿 기반 생성 | 스텁 | TemplatesPage, TemplateDetailPage 구현 | 🟡 중간 |
| F-6 | UC-U02 | 서비스별 OAuth 인증 | 없음 | oauth-connect feature 구현 | 🟡 중간 |
| F-7 | UC-E01 | 워크플로우 실행 | 상태 타입만 | execute-workflow feature + DebugPanel | 🟡 중간 |
| F-8 | UC-E02 | 노드별 데이터 흐름 미리보기 | 없음 | debug-panel widget 구현 | 🟡 중간 |
| F-9 | UC-S01~S05 | 서비스 노드 설정 패널 | PanelRenderer 스텁 | 노드별 설정 패널 UI 구현 | 🟡 중간 |
| F-10 | UC-P01~P09 | 프로세싱 노드 설정 패널 | 없음 | 노드별 설정 패널 UI 구현 | 🟡 중간 |
| F-11 | UC-A01 | AI 처리 설정 | 없음 | LLM 노드 설정 패널 구현 | 🟡 중간 |
| F-12 | UC-W02 | LLM 기반 자동 생성 | 없음 | chat-interface widget + API 연동 | 🟢 낮음 |

### 8.4 권장 구현 순서

```
Phase 1 — 기반 정합 (타입/API 계층)
├── T-1: ApiResponse 타입 교체
├── T-2: Workflow 엔티티 확장
├── T-3: DataType 통일
├── A-1: auth.api.ts
├── A-2: 리프레시 토큰 인터셉터
└── TanStack Query 훅 기본 패턴 수립

Phase 2 — 핵심 흐름
├── F-1: Google SSO 로그인
├── F-4: 워크플로우 목록
├── F-2: 가이드형 노드 설정 완성
├── A-3 + F-3: 선택지 매핑 API + ChoicePanel
└── T-4: FlowNodeData 확장

Phase 3 — 서비스 연동
├── F-6: OAuth 연결
├── F-9~F-11: 노드 설정 패널들
└── T-5: NodeDefinition 직렬화

Phase 4 — 실행 및 고급 기능
├── F-7: 워크플로우 실행
├── F-8: 디버그 뷰
├── F-5: 템플릿
└── F-12: LLM 기반 자동 생성
```

---

## 부록

### A. 코드 컨벤션 요약

| 규칙 | 설명 |
| --- | --- |
| Import 순서 | 외부 → `@/` 절대 → `../` `./` 상대 (Prettier 플러그인 자동 정렬) |
| `import type` | value import와 반드시 분리 |
| FSD 계층 간 import | 상위→하위만 허용, 같은 계층은 상대 경로 |
| 배럴 인덱스 | 상위 `index.ts`는 `export *`, 하위는 named export |
| 스타일 | Chakra UI props만 사용, inline style 금지 |
| 컴포넌트 | 함수 선언문 또는 const + 화살표 함수 |
| 상태 관리 | Zustand slice 패턴, immer middleware |

### B. 용어 대응표

| 프론트엔드 용어 | 백엔드 용어 | 설명 |
| --- | --- | --- |
| `Node` (React Flow) | `NodeDefinition` | 캔버스의 노드 |
| `Edge` (React Flow) | `EdgeDefinition` | 노드 간 연결선 |
| `NodeType` (15종) | — | 서비스 카테고리 (domain/processing/ai) |
| — | `node_type` (6종) | 내부 처리 타입 (LOOP, AI 등) |
| `FlowNodeData` | `NodeDefinition.config` 등 | 노드 데이터 |
| `DataType` | `data_types` (mapping_rules.json) | 노드 간 데이터 흐름 타입 |
| `workflowStore` | — | 프론트 전용 에디터 상태 |
| `activePanelNodeId` | — | 설정 패널 대상 노드 |
| `activePlaceholder` | — | 서비스 선택 패널 대상 플레이스홀더 |