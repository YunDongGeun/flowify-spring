# Flowify 백엔드 보완 요청서

> 작성일: 2026-03-30
> 기반: BE docs 전체 분석, 다이어그램 9종 검토, 팀 기술 결정사항 반영

---

## 요약

BE 설계 문서와 다이어그램의 전체 완성도는 높다. 구조, 추적성, 일관성 모두 양호하며 4/1 설계 발표 기준으로 충분한 수준이다. 다만 최근 팀에서 확정한 기술 결정사항 3건이 아직 반영되지 않았고, 직접 설정 흐름의 API 설계에서 구체화가 필요한 부분이 있다. 아래 항목들을 우선순위별로 정리한다.

---

## 1. 필수 수정 — 확정된 기술 결정사항 반영

### 1-1. LLM 모델: EXAONE → OpenAI GPT-4o

팀 회의에서 OpenAI API (GPT-4o)로 확정되었다. 문서 전체에서 EXAONE 언급을 GPT-4o 또는 OpenAI API로 수정해야 한다.

수정 대상 파일:

- PROJECT_ANALYSIS.md — 아키텍처 다이어그램의 "LLM (EXAONE)", 기술 스택 테이블
- 3_deployment_diagram.md — External Services의 "LLM (EXAONE)"
- NON_FUNCTIONAL_REQUIREMENTS.md — 외부 서비스 통신 테이블, EXR-03 발생 조건
- ACCEPTANCE_CRITERIA.md — 용어 해설의 LLM/EXAONE 항목, 참고 자료
- 시스템 배포 구성도 SVG — "LLM - EXAONE" 라벨

### 1-2. 벡터스토어: FAISS/Chroma 병기 → Chroma 단일

Chroma로 확정되었다. "FAISS / Chroma", "FAISS, Chroma"로 병기된 부분을 모두 Chroma로 통일한다.

수정 대상 파일:

- PROJECT_ANALYSIS.md — 아키텍처 다이어그램, 기술 스택, 벡터 검색 서비스 설명, Phase 5
- 3_deployment_diagram.md — Vector Store 라벨
- NON_FUNCTIONAL_REQUIREMENTS.md — 기술 스택 테이블
- ACCEPTANCE_CRITERIA.md — 용어 해설, 참고 자료
- 시스템 배포 구성도 SVG — "FAISS/Chroma" 라벨

### 1-3. 개발 카테고리(GitHub) 추가

카테고리 체계에 "개발 (GitHub)"이 확정되었다. 현재 문서의 카테고리 목록에 누락되어 있다.

수정 대상:

- FUNCTIONAL_REQUIREMENTS.md — 1.3절 카테고리 테이블에 "개발 | GitHub" 행 추가
- 2_database_design.md — workflows 컬렉션의 nodes[].category enum에 "development" 추가

참고: mapping_rules.json의 service_fields에는 GitHub 필드 목록이 이미 포함되어 있다. mapping_rules.json은 프론트 측에서 관리하므로 백엔드 수정 대상이 아니다.

---

## 2. 필수 보완 — 직접 설정 시퀀스(SD-W06) 구체화

직접 설정 흐름은 Flowify의 핵심 기능이다. 현재 시퀀스 다이어그램에서 3가지가 불명확하다.

### 2-1. 1차 선택과 2차 선택의 노드 생성 방식 명확화

현재 다이어그램에서 사용자가 1차에서 "한 건씩 처리"를 선택하면 Loop 노드가 결정되고, 2차에서 "AI로 요약하기"를 선택하면 AI 노드가 결정된다. 이것은 한 번의 "+" 클릭으로 2개의 노드가 생성되는 것이다.

명확화 필요 사항:

- `POST /nodes`가 1개 노드만 저장하는 API인지, 1차+2차 결과를 합쳐서 2개 노드를 한 번에 저장하는 API인지
- 1차(Loop) 선택 후 2차 선택지가 나오는 사이에 Loop 노드가 먼저 저장되는 것인지, 2차까지 모두 완료된 후 일괄 저장되는 것인지

제안: 1차 선택 시 Loop 노드를 먼저 저장하고, 2차부터는 새로운 "+" 클릭과 동일한 흐름으로 진행하는 것이 깔끔하다. 이렇게 하면 모든 노드 추가가 동일한 API 패턴을 따른다.

### 2-2. 단일 데이터 타입 진입 시 분기 표현

이전 노드의 출력이 단일 파일, 텍스트, 단일 이메일 등 단일 데이터일 경우, 1차 처리 방식 질문("한 건씩/전체")을 건너뛰고 바로 2차 선택지로 가야 한다. 현재 다이어그램에는 이 분기가 표현되어 있지 않다.

제안: `getOptionsForNode(prevOutputType)` 호출 결과에서 `requires_processing_method`가 false이면 바로 actions 목록을 반환하는 흐름을 다이어그램에 alt 프래그먼트로 추가한다.

### 2-3. 조건 분기 선택 시 후속 흐름

사용자가 "파일 종류별로 다르게 처리" 같은 조건 분기 선택지를 고르면, `branch_config`에 정의된 분기 기준 선택 화면(어떤 파일 종류를 구분할까요? → PDF, 이미지 등 multi_select)이 표시되어야 한다. 이 후속 흐름이 다이어그램에 없다.

제안: `onUserSelect(optionId)` 응답에 `branch_config`가 포함된 경우, 추가 선택 화면을 표시하는 흐름을 다이어그램에 추가한다.

---

## 3. 필수 보완 — 노드 단위 API 설계

현재 WorkflowController에는 워크플로우 전체 CRUD만 있다. 직접 설정 흐름에서 핵심인 노드 단위 조작 API가 구체적으로 설계되어 있지 않다.

추가가 필요한 API:

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/api/workflows/{id}/choices/{prevNodeId}` | 이전 노드의 outputDataType 기반 선택지 조회. 목록형이면 processing_method 반환, 단일형이면 actions 반환 |
| POST | `/api/workflows/{id}/choices/{prevNodeId}/select` | 사용자 선택 전송. follow_up 또는 branch_config가 있으면 후속 설정 반환, 없으면 노드 타입 확정 결과 반환 |
| POST | `/api/workflows/{id}/nodes` | 확정된 노드를 워크플로우에 추가. edge도 함께 생성 |
| PUT | `/api/workflows/{id}/nodes/{nodeId}` | 기존 노드 설정 수정 |
| DELETE | `/api/workflows/{id}/nodes/{nodeId}` | 노드 삭제 + 후속 노드 캐스케이드 삭제 |

이 API들은 5_sequence_diagram.md에 SD-W06으로, 0_summary.md의 API 엔드포인트 요약에도 추가되어야 한다.

---

## 4. 권장 보완 — 설계 구체화

### 4-1. ChoiceMappingService JSON 로딩 전략

현재 설계에는 `loadMappingRules()` 메서드만 있고, 로딩 시점이 명시되어 있지 않다.

제안: 서버 시작 시 `@PostConstruct`로 mapping_rules.json을 메모리에 캐싱한다. 매 요청마다 파일을 읽지 않는다. 개발 환경에서는 파일 변경 시 자동 리로드를 지원하면 편리하다.

### 4-2. 워크플로우 생성 상태 다이어그램 — 도착 노드 OAuth 확인

현재 상태 다이어그램에서 ServiceConnection(OAuth 인증 확인)이 시작 노드 설정 전에만 있다. 도착 노드에서 시작 노드와 다른 서비스를 선택할 경우에도 OAuth 확인이 필요하다.

제안: EndNodeConfig 진입 전에 ServiceConnection 상태를 추가하거나, EndNodeConfig 내부에서 CheckAuth를 수행하는 것으로 명시한다.

### 4-3. Phase ↔ 마일스톤 매핑

DEVELOPMENT_PLAN.md에 Phase 1~6이 정의되어 있지만, 4/29 중간 발표와 6/17 최종 제출에 어떤 Phase가 대응하는지 명시적으로 매핑되어 있지 않다.

제안:

- Phase 1~3 (기반 + 인증 + 워크플로우): 4/29 중간 발표까지
- Phase 4~6 (템플릿/OAuth/실행 + 고급 기능 + 안정화): 6/17 최종 제출까지

### 4-4. FastAPI 비동기 통신 패턴 명시

SD-E01(실행 시퀀스)에서 폴링 방식이 표현되어 있어 구현 방향은 확인되었다. 다만 설계 문서(0_summary.md, PROJECT_ANALYSIS.md)에 "REST + 비동기 패턴"이라고만 되어 있고, 구체적으로 "폴링 방식"이라는 명시가 없다.

제안: 0_summary.md의 서비스 간 통신 흐름 섹션에 "워크플로우 실행은 비동기 위임 후 프론트엔드가 Status Polling으로 실행 상태를 조회한다"를 추가한다.

---

## 5. 참고 — 수정 불필요 확인 항목

아래 항목들은 검토 결과 문제 없음을 확인했다.

| 항목 | 상태 |
| --- | --- |
| 서비스 철학 반영 (시스템 결정 원칙, 기술 용어 비노출, 동적 선택지) | 정확 |
| 워크플로우 생성 흐름 (시작 → 도착 → 중간 과정) | UC-W01-A~D로 정확 반영 |
| 노드 타입 6가지 (LOOP, CONDITION_BRANCH, AI, DATA_FILTER, AI_FILTER, PASSTHROUGH) | 정확 |
| 데이터 타입 8가지 | 정확 |
| mapping_rules.json 내용 | 우리가 만든 파일과 동일 |
| DB 스키마 5개 컬렉션 | 정확 |
| ERD | DB 스키마와 일치 |
| Google SSO 로그인 시퀀스 (SD-U01) | 정확 |
| 워크플로우 실행 시퀀스 (SD-E01) | 정확, 폴링 패턴 명시 |
| 에러 처리 및 롤백 시퀀스 | EXR-01, EXR-06 정확 반영 |
| 인증 클래스 다이어그램 (PK-C01) | 정확 |
| 워크플로우 클래스 다이어그램 (PK-C03, PK-C08) | 정확 |
| 예외 처리 체계 (18종 에러코드) | 요구사항 EXR-01~08 모두 매핑 |
| OAuth 토큰 암호화 (AES-256-GCM) | 정확 |
| JWT 인증 체계 (HS256, Access 30분/Refresh 7일) | 정확 |
| 요구사항 추적표 | UC → 클래스 → 시퀀스 추적 가능 |

---

## 6. 작업 우선순위 요약

| 우선순위 | 항목 | 작업량 | 기한 |
| --- | --- | --- | --- |
| P0 | EXAONE → GPT-4o 전체 수정 | 텍스트 치환 + SVG 1건 | 4/1 전 |
| P0 | FAISS/Chroma → Chroma 전체 수정 | 텍스트 치환 + SVG 1건 | 4/1 전 |
| P0 | 개발 카테고리(GitHub) 추가 | 카테고리 목록 + DB enum | 4/1 전 |
| P1 | 노드 단위 API 설계 추가 | API 5개 정의 | 4/1 전 권장 |
| P1 | SD-W06 시퀀스 다이어그램 보완 | 3가지 흐름 추가 | 4/1 전 권장 |
| P2 | ChoiceMappingService 로딩 전략 명시 | 1줄 추가 | 구현 시 |
| P2 | 상태 다이어그램 도착 노드 OAuth 추가 | 상태 1개 추가 | 구현 시 |
| P2 | Phase ↔ 마일스톤 매핑 추가 | 텍스트 추가 | 4/1 전 권장 |
| P2 | 폴링 방식 명시 | 텍스트 1줄 추가 | 4/1 전 권장 |
