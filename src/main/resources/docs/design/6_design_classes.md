# 3.4 Design Classes (클래스 설계)

---

## PK-C07: 공통 모듈 (config/)

### DC-C0701: SecurityConfig

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0701 |
| **클래스 명** | SecurityConfig |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| jwtAuthFilter | private | JwtAuthFilter | JWT 인증 필터 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| securityFilterChain | public | HttpSecurity | SecurityFilterChain | Spring Security 필터 체인 구성. CSRF 비활성화, Stateless 세션, 인가 규칙 설정, JwtAuthFilter 등록 |

---

### DC-C0702: WebClientConfig

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0702 |
| **클래스 명** | WebClientConfig |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| fastapiBaseUrl | private | String | FastAPI 서버 기본 URL |
| internalToken | private | String | 내부 통신 인증 토큰 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| webClient | public | - | WebClient | FastAPI 통신용 WebClient 빈 생성. base-url 및 X-Internal-Token 기본 헤더 설정 |

---

### DC-C0703: CorsConfig

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0703 |
| **클래스 명** | CorsConfig |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| corsFilter | public | - | CorsFilter | CORS 필터 설정. 개발: localhost:3000, 운영: 환경변수 기반 도메인 허용 |

---

### DC-C0704: MongoConfig

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0704 |
| **클래스 명** | MongoConfig |

**설명:** `@EnableMongoAuditing` 활성화. @CreatedDate, @LastModifiedDate 자동 설정 지원

---

## PK-C01: 회원 및 인증 관리 (auth/)

### DC-C0101: AuthController

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C01 |
| **클래스 식별자** | DC-C0101 |
| **클래스 명** | AuthController |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| authService | private | AuthService | 인증 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| googleLogin | public | - | ResponseEntity | GET /api/auth/google — Google OAuth 인증 화면으로 리다이렉트 |
| googleCallback | public | String code | ApiResponse\<LoginResponse\> | GET /api/auth/google/callback — Google 콜백 처리, Authorization Code로 토큰 교환 후 JWT 발급 |
| refreshToken | public | TokenRefreshRequest | ApiResponse\<LoginResponse\> | POST /api/auth/refresh — Refresh Token으로 새 Access Token 발급 |
| logout | public | - | ApiResponse\<Void\> | POST /api/auth/logout — Refresh Token 무효화 (DB에서 삭제) |

---

### DC-C0102: AuthService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C01 |
| **클래스 식별자** | DC-C0102 |
| **클래스 명** | AuthService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| userRepository | private | UserRepository | 사용자 레포지토리 |
| jwtProvider | private | JwtProvider | JWT 제공자 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| processGoogleLogin | public | String authorizationCode | LoginResponse | Google Authorization Code를 토큰으로 교환, ID Token에서 사용자 정보 추출, 신규 사용자 자동 회원가입, JWT 발급 |
| refreshAccessToken | public | String refreshToken | LoginResponse | Refresh Token 검증 후 새 Access Token 발급. DB에 저장된 Refresh Token과 대조 |
| logout | public | String userId | void | 사용자의 Refresh Token을 DB에서 삭제하여 무효화 |

---

### DC-C0103: JwtProvider

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C01 |
| **클래스 식별자** | DC-C0103 |
| **클래스 명** | JwtProvider |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| secretKey | private | String | HS256 서명용 비밀키 |
| accessExpirationMs | private | long | Access Token 유효기간 (30분, 1800000ms) |
| refreshExpirationMs | private | long | Refresh Token 유효기간 (7일, 604800000ms) |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| generateAccessToken | public | User user | String | Access Token 생성. Payload: sub(userId), email, name, iat, exp |
| generateRefreshToken | public | User user | String | Refresh Token 생성. Payload: sub(userId), iat, exp |
| validateToken | public | String token | boolean | JWT 유효성 검증 (서명, 만료 확인) |
| getUserIdFromToken | public | String token | String | 토큰의 sub 클레임에서 userId 추출 |
| getEmailFromToken | public | String token | String | 토큰의 email 클레임 추출 |

---

### DC-C0104: JwtAuthFilter

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C01 |
| **클래스 식별자** | DC-C0104 |
| **클래스 명** | JwtAuthFilter |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| jwtProvider | private | JwtProvider | JWT 제공자 |
| userRepository | private | UserRepository | 사용자 레포지토리 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| doFilterInternal | protected | HttpServletRequest, HttpServletResponse, FilterChain | void | Authorization 헤더에서 Bearer 토큰 추출, JwtProvider로 검증, 유효 시 SecurityContext에 인증 정보 설정 |

---

### DC-C0105: LoginResponse

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C01 |
| **클래스 식별자** | DC-C0105 |
| **클래스 명** | LoginResponse |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| accessToken | private | String | Flowify Access Token |
| refreshToken | private | String | Flowify Refresh Token |
| user | private | UserResponse | 사용자 정보 |

---

### DC-C0106: TokenRefreshRequest

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C01 |
| **클래스 식별자** | DC-C0106 |
| **클래스 명** | TokenRefreshRequest |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| refreshToken | private | String | 갱신할 Refresh Token (@NotBlank) |

---

## PK-C02: 사용자 관리 (user/)

### DC-C0201: UserController

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C02 |
| **클래스 식별자** | DC-C0201 |
| **클래스 명** | UserController |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| userService | private | UserService | 사용자 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| getMe | public | Authentication | ApiResponse\<UserResponse\> | GET /api/users/me — 현재 로그인 사용자 정보 조회 |
| updateMe | public | Authentication, UserUpdateRequest | ApiResponse\<UserResponse\> | PUT /api/users/me — 사용자 정보 수정 (이름 등) |
| deleteMe | public | Authentication | ApiResponse\<Void\> | DELETE /api/users/me — 회원 탈퇴 (관련 데이터 일괄 삭제) |

---

### DC-C0202: UserService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C02 |
| **클래스 식별자** | DC-C0202 |
| **클래스 명** | UserService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| userRepository | private | UserRepository | 사용자 레포지토리 |
| workflowRepository | private | WorkflowRepository | 워크플로우 레포지토리 (탈퇴 시 삭제용) |
| oauthTokenRepository | private | OAuthTokenRepository | OAuth 토큰 레포지토리 (탈퇴 시 삭제용) |
| executionRepository | private | ExecutionRepository | 실행 이력 레포지토리 (탈퇴 시 삭제용) |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| getUserById | public | String userId | UserResponse | 사용자 ID로 사용자 정보 조회. 없으면 USER_NOT_FOUND |
| updateUser | public | String userId, UserUpdateRequest | UserResponse | 사용자 정보 수정 (이름 등) |
| deleteUser | public | String userId | void | 회원 탈퇴. users, workflows, oauth_tokens, workflow_executions 일괄 삭제 |

---

### DC-C0203: UserRepository

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C02 |
| **클래스 식별자** | DC-C0203 |
| **클래스 명** | UserRepository |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| findByEmail | public | String email | Optional\<User\> | 이메일로 사용자 조회 |
| findByGoogleId | public | String googleId | Optional\<User\> | Google ID로 사용자 조회 |

---

### DC-C0204: User

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C02 |
| **클래스 식별자** | DC-C0204 |
| **클래스 명** | User |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | @Id — MongoDB 문서 ID |
| email | private | String | @Indexed(unique=true) — Google 계정 이메일 |
| name | private | String | Google 프로필 이름 |
| picture | private | String | Google 프로필 사진 URL |
| googleId | private | String | @Indexed(unique=true) — Google 고유 ID (sub claim) |
| refreshToken | private | String | Flowify Refresh Token (로그아웃용) |
| createdAt | private | Instant | @CreatedDate — 생성 일시 |
| updatedAt | private | Instant | @LastModifiedDate — 수정 일시 |
| lastLoginAt | private | Instant | 마지막 로그인 일시 |

---

### DC-C0205: UserResponse

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C02 |
| **클래스 식별자** | DC-C0205 |
| **클래스 명** | UserResponse |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | 사용자 ID |
| email | private | String | 이메일 |
| name | private | String | 이름 |
| picture | private | String | 프로필 사진 URL |
| createdAt | private | Instant | 가입 일시 |

---

## PK-C03: 워크플로우 관리 (workflow/)

### DC-C0301: WorkflowController

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C03 |
| **클래스 식별자** | DC-C0301 |
| **클래스 명** | WorkflowController |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| workflowService | private | WorkflowService | 워크플로우 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| createWorkflow | public | Authentication, WorkflowCreateRequest | ApiResponse\<WorkflowResponse\> | POST /api/workflows — 워크플로우 생성 |
| getWorkflows | public | Authentication, int page, int size | ApiResponse\<PageResponse\<WorkflowResponse\>\> | GET /api/workflows — 내 워크플로우 목록 (페이지네이션) |
| getWorkflow | public | Authentication, String id | ApiResponse\<WorkflowResponse\> | GET /api/workflows/{id} — 워크플로우 상세 조회 |
| updateWorkflow | public | Authentication, String id, WorkflowUpdateRequest | ApiResponse\<WorkflowResponse\> | PUT /api/workflows/{id} — 워크플로우 수정 |
| deleteWorkflow | public | Authentication, String id | ApiResponse\<Void\> | DELETE /api/workflows/{id} — 워크플로우 삭제 |
| shareWorkflow | public | Authentication, String id, ShareRequest | ApiResponse\<Void\> | POST /api/workflows/{id}/share — 공유 설정 (소유자만) |

---

### DC-C0302: WorkflowService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C03 |
| **클래스 식별자** | DC-C0302 |
| **클래스 명** | WorkflowService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| workflowRepository | private | WorkflowRepository | 워크플로우 레포지토리 |
| workflowValidator | private | WorkflowValidator | 워크플로우 유효성 검증기 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| createWorkflow | public | String userId, WorkflowCreateRequest | WorkflowResponse | 워크플로우 생성. userId를 소유자로 설정 |
| getWorkflowsByUserId | public | String userId, Pageable | PageResponse\<WorkflowResponse\> | 소유 또는 공유된 워크플로우 목록 조회 |
| getWorkflowById | public | String userId, String workflowId | WorkflowResponse | 워크플로우 상세 조회. 접근 권한 검증 |
| updateWorkflow | public | String userId, String workflowId, WorkflowUpdateRequest | WorkflowResponse | 워크플로우 수정. 접근 권한 검증 |
| deleteWorkflow | public | String userId, String workflowId | void | 워크플로우 삭제. 접근 권한 검증 |
| shareWorkflow | public | String userId, String workflowId, List\<String\> userIds | void | 공유 설정. 소유자만 가능 |
| verifyOwnership | private | Workflow, String userId | void | 소유자 검증. 실패 시 WORKFLOW_ACCESS_DENIED |
| verifyAccess | private | Workflow, String userId | void | 접근 권한 검증 (소유자 또는 공유 대상) |

---

### DC-C0303: WorkflowValidator

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C03 |
| **클래스 식별자** | DC-C0303 |
| **클래스 명** | WorkflowValidator |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| validate | public | Workflow | void | 워크플로우 구조 전체 검증. 실패 시 WORKFLOW_VALIDATION_FAILED (EXR-05) |
| checkCyclicReference | private | List\<NodeDefinition\>, List\<EdgeDefinition\> | void | DFS 기반 순환 참조 검출 |
| checkIsolatedNodes | private | List\<NodeDefinition\>, List\<EdgeDefinition\> | void | 연결되지 않은 고립 노드 검출 |
| checkRequiredConfig | private | List\<NodeDefinition\> | void | 노드별 필수 설정값 누락 검출 |

---

### DC-C0304: Workflow

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C03 |
| **클래스 식별자** | DC-C0304 |
| **클래스 명** | Workflow |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | @Id — MongoDB 문서 ID |
| name | private | String | 워크플로우 이름 |
| description | private | String | 워크플로우 설명 |
| userId | private | String | @Indexed — 소유자 사용자 ID |
| sharedWith | private | List\<String\> | 공유된 사용자 ID 목록 |
| isTemplate | private | boolean | 템플릿 여부 |
| templateId | private | String | 원본 템플릿 ID |
| nodes | private | List\<NodeDefinition\> | 노드 목록 |
| edges | private | List\<EdgeDefinition\> | 엣지 목록 |
| trigger | private | TriggerConfig | 트리거 설정 (null이면 수동) |
| isActive | private | boolean | 트리거 활성화 여부 |
| createdAt | private | Instant | @CreatedDate |
| updatedAt | private | Instant | @LastModifiedDate |

---

### DC-C0305: NodeDefinition

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C03 |
| **클래스 식별자** | DC-C0305 |
| **클래스 명** | NodeDefinition |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | 노드 고유 ID (e.g., "node_1") |
| category | private | String | 노드 카테고리 (service \| processing \| ai) |
| type | private | String | 노드 타입 (communication, storage, spreadsheet, web_crawl, calendar, trigger, filter, loop, condition, multi_output, data_process, output_format, early_stop, notification, llm) |
| config | private | Map\<String, Object\> | 노드별 설정값 |
| position | private | Position | 캔버스 좌표 {x, y} |

---

### DC-C0306: EdgeDefinition

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C03 |
| **클래스 식별자** | DC-C0306 |
| **클래스 명** | EdgeDefinition |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| source | private | String | 출발 노드 ID |
| target | private | String | 도착 노드 ID |

---

### DC-C0307 ~ DC-C0309: 워크플로우 DTO

**DC-C0307: WorkflowCreateRequest**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| name | private | String | @NotBlank — 워크플로우 이름 |
| description | private | String | 설명 |
| nodes | private | List\<NodeDefinition\> | 노드 목록 |
| edges | private | List\<EdgeDefinition\> | 엣지 목록 |
| trigger | private | TriggerConfig | 트리거 설정 |

**DC-C0308: WorkflowUpdateRequest**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| name | private | String | 워크플로우 이름 |
| description | private | String | 설명 |
| nodes | private | List\<NodeDefinition\> | 노드 목록 |
| edges | private | List\<EdgeDefinition\> | 엣지 목록 |
| trigger | private | TriggerConfig | 트리거 설정 |
| isActive | private | Boolean | 트리거 활성화 여부 |

**DC-C0309: WorkflowResponse**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | 워크플로우 ID |
| name | private | String | 이름 |
| description | private | String | 설명 |
| userId | private | String | 소유자 ID |
| nodes | private | List\<NodeDefinition\> | 노드 목록 |
| edges | private | List\<EdgeDefinition\> | 엣지 목록 |
| trigger | private | TriggerConfig | 트리거 설정 |
| isActive | private | boolean | 활성화 여부 |
| createdAt | private | Instant | 생성 일시 |
| updatedAt | private | Instant | 수정 일시 |

---

## PK-C04: 워크플로우 실행 (execution/)

### DC-C0401: ExecutionController

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C04 |
| **클래스 식별자** | DC-C0401 |
| **클래스 명** | ExecutionController |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| executionService | private | ExecutionService | 실행 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| executeWorkflow | public | Authentication, String workflowId | ApiResponse\<String\> | POST /api/workflows/{id}/execute — 워크플로우 실행 요청. execution_id 반환 |
| getExecutions | public | Authentication, String workflowId | ApiResponse\<List\> | GET /api/workflows/{id}/executions — 실행 이력 목록 |
| getExecutionDetail | public | Authentication, String workflowId, String execId | ApiResponse\<WorkflowExecution\> | GET /api/workflows/{id}/executions/{execId} — 실행 상세 (노드 로그 포함) |
| rollbackExecution | public | Authentication, String workflowId, String execId | ApiResponse\<Void\> | POST /api/workflows/{id}/executions/{execId}/rollback — 실행 롤백 |

---

### DC-C0402: ExecutionService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C04 |
| **클래스 식별자** | DC-C0402 |
| **클래스 명** | ExecutionService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| executionRepository | private | ExecutionRepository | 실행 이력 레포지토리 |
| workflowService | private | WorkflowService | 워크플로우 서비스 |
| fastApiClient | private | FastApiClient | FastAPI 통신 클라이언트 |
| oauthTokenService | private | OAuthTokenService | OAuth 토큰 서비스 |
| snapshotService | private | SnapshotService | 스냅샷 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| executeWorkflow | public | String userId, String workflowId | String | 워크플로우 실행. 소유권 검증 → 유효성 검증 → 토큰 복호화 → FastAPI 위임. execution_id 반환 |
| getExecutionsByWorkflowId | public | String userId, String workflowId | List | 워크플로우별 실행 이력 목록 조회. 접근 권한 검증 |
| getExecutionDetail | public | String userId, String execId | WorkflowExecution | 실행 상세 조회. nodeLogs, inputData, outputData, snapshot 포함 |
| rollbackExecution | public | String userId, String execId | void | 스냅샷 기반 롤백 실행 (EXR-06) |

---

### DC-C0403: FastApiClient

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C04 |
| **클래스 식별자** | DC-C0403 |
| **클래스 명** | FastApiClient |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| webClient | private | WebClient | FastAPI 통신용 WebClient |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| execute | public | String workflowId, String userId, Object workflowDefinition, Map\<String, String\> serviceTokens | String | POST fastapi/api/v1/workflows/{id}/execute. X-User-ID 헤더 포함. execution_id 반환. 접속 불가 시 FASTAPI_UNAVAILABLE |
| getStatus | public | String executionId | Object | 실행 상태 조회 (필요 시 FastAPI에서 직접 조회) |

---

### DC-C0404: SnapshotService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C04 |
| **클래스 식별자** | DC-C0404 |
| **클래스 명** | SnapshotService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| executionRepository | private | ExecutionRepository | 실행 이력 레포지토리 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| captureSnapshot | public | String execId, String nodeId, Map stateData | void | 노드 실행 전 상태 스냅샷 캡처 (EXR-06) |
| rollbackToSnapshot | public | String execId, String nodeId | void | 마지막 성공 스냅샷으로 복원 |

---

### DC-C0405: WorkflowExecution

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C04 |
| **클래스 식별자** | DC-C0405 |
| **클래스 명** | WorkflowExecution |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | @Id — 실행 고유 ID |
| workflowId | private | String | 워크플로우 ID |
| userId | private | String | 실행 요청 사용자 ID |
| state | private | String | 실행 상태 (pending \| running \| success \| failed \| rollback_available) |
| nodeLogs | private | List\<NodeLog\> | 노드별 실행 로그 |
| startedAt | private | Instant | 실행 시작 시각 |
| finishedAt | private | Instant | 실행 종료 시각 |

---

### DC-C0406: ExecutionRepository

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C04 |
| **클래스 식별자** | DC-C0406 |
| **클래스 명** | ExecutionRepository |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| findByWorkflowId | public | String workflowId | List\<WorkflowExecution\> | 워크플로우별 실행 이력 조회 |
| findByUserId | public | String userId | List\<WorkflowExecution\> | 사용자별 실행 이력 조회 |
| deleteByUserId | public | String userId | void | 사용자 탈퇴 시 실행 이력 일괄 삭제 |

---

## PK-C05: 템플릿 관리 (template/)

### DC-C0501: TemplateController

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C05 |
| **클래스 식별자** | DC-C0501 |
| **클래스 명** | TemplateController |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| templateService | private | TemplateService | 템플릿 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| getTemplates | public | String category | ApiResponse\<List\> | GET /api/templates — 템플릿 목록 (카테고리 필터) |
| getTemplateById | public | String id | ApiResponse\<Template\> | GET /api/templates/{id} — 템플릿 상세 |
| instantiateTemplate | public | Authentication, String id | ApiResponse\<WorkflowResponse\> | POST /api/templates/{id}/instantiate — 템플릿으로 워크플로우 생성 |
| createTemplate | public | Authentication, CreateTemplateRequest | ApiResponse\<Template\> | POST /api/templates — 사용자 템플릿 생성 |

---

### DC-C0502: TemplateService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C05 |
| **클래스 식별자** | DC-C0502 |
| **클래스 명** | TemplateService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| templateRepository | private | TemplateRepository | 템플릿 레포지토리 |
| workflowService | private | WorkflowService | 워크플로우 서비스 (인스턴스화용) |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| getTemplates | public | String category | List\<Template\> | 카테고리별 템플릿 목록 조회. null이면 전체 |
| getTemplateById | public | String id | Template | 템플릿 상세 조회. 없으면 TEMPLATE_NOT_FOUND |
| instantiateTemplate | public | String userId, String templateId | WorkflowResponse | 템플릿 기반 워크플로우 생성. 노드/엣지 복사, templateId 설정, useCount 증가 |
| createUserTemplate | public | String userId, CreateTemplateRequest | Template | 사용자 워크플로우를 템플릿으로 저장. isSystem=false, authorId 설정 |

---

### DC-C0503: TemplateRepository

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C05 |
| **클래스 식별자** | DC-C0503 |
| **클래스 명** | TemplateRepository |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| findByCategory | public | String category | List\<Template\> | 카테고리별 템플릿 조회 |
| findByIsSystem | public | boolean isSystem | List\<Template\> | 시스템/사용자 템플릿 구분 조회 |

---

### DC-C0504: Template

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C05 |
| **클래스 식별자** | DC-C0504 |
| **클래스 명** | Template |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | @Id — 템플릿 고유 ID |
| name | private | String | 템플릿 이름 |
| description | private | String | 설명 |
| category | private | String | 카테고리 (communication \| storage \| spreadsheet \| web_crawl \| calendar) |
| icon | private | String | 아이콘 식별자 |
| nodes | private | List\<NodeDefinition\> | 사전 정의된 노드 구성 |
| edges | private | List\<EdgeDefinition\> | 엣지 구성 |
| requiredServices | private | List\<String\> | 필요한 외부 서비스 목록 |
| isSystem | private | boolean | 시스템 제공 여부 |
| authorId | private | String | 사용자 생성 시 작성자 ID |
| useCount | private | int | 사용 횟수 |
| createdAt | private | Instant | @CreatedDate |

---

## PK-C06: OAuth 토큰 관리 (oauth/)

### DC-C0601: OAuthTokenController

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C06 |
| **클래스 식별자** | DC-C0601 |
| **클래스 명** | OAuthTokenController |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| oauthTokenService | private | OAuthTokenService | OAuth 토큰 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| getConnectedServices | public | Authentication | ApiResponse\<List\> | GET /api/oauth-tokens — 연결된 서비스 목록 (토큰 노출 X) |
| connectService | public | Authentication, String service | ResponseEntity | POST /api/oauth-tokens/{service}/connect — OAuth 연결 시작 (리다이렉트) |
| oauthCallback | public | String service, String code | ApiResponse\<Void\> | GET /api/oauth-tokens/{service}/callback — OAuth 콜백, 토큰 저장 |
| disconnectService | public | Authentication, String service | ApiResponse\<Void\> | DELETE /api/oauth-tokens/{service} — 서비스 연결 해제 |

---

### DC-C0602: OAuthTokenService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C06 |
| **클래스 식별자** | DC-C0602 |
| **클래스 명** | OAuthTokenService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| oauthTokenRepository | private | OAuthTokenRepository | OAuth 토큰 레포지토리 |
| tokenEncryptionService | private | TokenEncryptionService | 토큰 암호화 서비스 |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| getConnectedServices | public | String userId | List\<ServiceInfo\> | 사용자의 연결된 서비스 목록 (토큰 자체 미포함) |
| saveToken | public | String userId, String service, String accessToken, String refreshToken, Instant expiresAt, List\<String\> scopes | void | 토큰 암호화 후 저장. 기존 토큰이 있으면 업데이트 |
| getDecryptedToken | public | String userId, String service | String | 복호화된 액세스 토큰 반환. 만료 5분 전이면 자동 갱신 (EXR-02) |
| refreshTokenIfNeeded | public | OAuthToken token | String | 만료 임박 시 Refresh Token으로 새 Access Token 발급 (Lazy Refresh) |
| deleteToken | public | String userId, String service | void | 토큰 삭제 (서비스 연결 해제) |

---

### DC-C0603: TokenEncryptionService

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C06 |
| **클래스 식별자** | DC-C0603 |
| **클래스 명** | TokenEncryptionService |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| secretKey | private | String | AES-256 암호화 키 (환경변수 ENCRYPTION_SECRET_KEY, Base64 인코딩) |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| encrypt | public | String plaintext | String | AES-256-GCM 암호화 → Base64 인코딩. plaintext → ciphertext |
| decrypt | public | String ciphertext | String | Base64 디코딩 → AES-256-GCM 복호화. ciphertext → plaintext |

---

### DC-C0604: OAuthTokenRepository

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C06 |
| **클래스 식별자** | DC-C0604 |
| **클래스 명** | OAuthTokenRepository |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| findByUserIdAndService | public | String userId, String service | Optional\<OAuthToken\> | 사용자+서비스 조합으로 토큰 조회 |
| findByUserId | public | String userId | List\<OAuthToken\> | 사용자의 전체 토큰 목록 |
| deleteByUserIdAndService | public | String userId, String service | void | 특정 서비스 토큰 삭제 |
| deleteByUserId | public | String userId | void | 사용자 탈퇴 시 전체 토큰 삭제 |

---

### DC-C0605: OAuthToken

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C06 |
| **클래스 식별자** | DC-C0605 |
| **클래스 명** | OAuthToken |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| id | private | String | @Id — 토큰 고유 ID |
| userId | private | String | 소유 사용자 ID |
| service | private | String | 서비스명 (google \| slack \| notion) |
| accessToken | private | String | AES-256-GCM 암호화된 액세스 토큰 |
| refreshToken | private | String | AES-256-GCM 암호화된 리프레시 토큰 |
| tokenType | private | String | 토큰 유형 (Bearer) |
| expiresAt | private | Instant | 액세스 토큰 만료 시각 |
| scopes | private | List\<String\> | 권한 범위 |
| createdAt | private | Instant | @CreatedDate |
| updatedAt | private | Instant | @LastModifiedDate |

**인덱스:** `@CompoundIndex(name = "user_service_idx", def = "{'userId': 1, 'service': 1}", unique = true)`

---

## PK-C07: 공통 모듈 (common/)

### DC-C0705: GlobalExceptionHandler

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0705 |
| **클래스 명** | GlobalExceptionHandler |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| handleBusinessException | public | BusinessException | ResponseEntity\<ApiResponse\> | BusinessException 처리. ErrorCode에 매핑된 HTTP 상태 코드 반환 |
| handleValidationException | public | MethodArgumentNotValidException | ResponseEntity\<ApiResponse\> | Jakarta Validation 예외 처리. 400 Bad Request |
| handleException | public | Exception | ResponseEntity\<ApiResponse\> | 미처리 예외 처리. 500 Internal Server Error |

---

### DC-C0706: BusinessException

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0706 |
| **클래스 명** | BusinessException |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| errorCode | private | ErrorCode | 에러 코드 enum |

---

### DC-C0707: ErrorCode

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0707 |
| **클래스 명** | ErrorCode (enum) |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| httpStatus | private | HttpStatus | HTTP 상태 코드 |
| message | private | String | 기본 에러 메시지 |

**enum 값:** AUTH_INVALID_TOKEN(401), AUTH_EXPIRED_TOKEN(401), AUTH_FORBIDDEN(403), WORKFLOW_NOT_FOUND(404), WORKFLOW_ACCESS_DENIED(403), WORKFLOW_VALIDATION_FAILED(400), OAUTH_NOT_CONNECTED(400), OAUTH_TOKEN_EXPIRED(400), EXTERNAL_API_ERROR(502), LLM_API_ERROR(502), LLM_GENERATION_FAILED(422), EXECUTION_FAILED(500), CRAWL_FAILED(502), DATA_CONVERSION_FAILED(422), FASTAPI_UNAVAILABLE(503), USER_NOT_FOUND(404), TEMPLATE_NOT_FOUND(404), INVALID_REQUEST(400)

---

### DC-C0708: ApiResponse\<T\>

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0708 |
| **클래스 명** | ApiResponse\<T\> |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| success | private | boolean | 성공 여부 |
| data | private | T | 응답 데이터 (실패 시 null) |
| message | private | String | 에러 메시지 (성공 시 null) |
| errorCode | private | String | 에러 코드 문자열 (성공 시 null) |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| success | public static | T data | ApiResponse\<T\> | 성공 응답 생성 |
| error | public static | ErrorCode, String message | ApiResponse\<Void\> | 실패 응답 생성 |

---

### DC-C0709: PageResponse\<T\>

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0709 |
| **클래스 명** | PageResponse\<T\> |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| content | private | List\<T\> | 페이지 데이터 |
| page | private | int | 현재 페이지 번호 |
| size | private | int | 페이지 크기 |
| totalElements | private | long | 전체 요소 수 |
| totalPages | private | int | 전체 페이지 수 |

---

### DC-C0710: RetryPolicy

| 항목 | 내용 |
|------|------|
| **클래스 다이어그램 식별자** | PK-C07 |
| **클래스 식별자** | DC-C0710 |
| **클래스 명** | RetryPolicy |

**속성:**

| 속성명 | 가시성 | 타입 | 설명 |
|--------|--------|------|------|
| maxRetries | private | int | 최대 재시도 횟수 |
| initialDelay | private | long | 초기 대기 시간 (ms) |

**메소드:**

| 메소드명 | 가시성 | 파라미터 | 반환값 | 설명 |
|----------|--------|---------|--------|------|
| executeWithRetry | public | Supplier\<T\> | T | Exponential Backoff 기반 재시도 실행. EXR-01: 외부 API 3회, EXR-03: LLM 2회 |
