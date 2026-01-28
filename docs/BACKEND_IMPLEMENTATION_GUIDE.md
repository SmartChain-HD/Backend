# 백엔드 구현 가이드

> **AI 기반 협력사 리스크 관리 플랫폼**
> 버전: v2.1 | 작성일: 2026-01-28
> 팀원: 진지현(풀스택 리더), 김건우, 박세용

---

## 서비스 도메인 구조

플랫폼은 3개의 핵심 서비스 도메인으로 구성됩니다:

| 도메인 | 코드 | 핵심 기능 | 대상 사용자 |
|--------|------|----------|------------|
| ESG 실사 | `ESG` | ESG 증빙 자동 파싱 및 AI 리포트 생성 | ESG 관리팀 |
| 안전보건 | `SAFETY` | TBM 영상 AI 분석 및 안전점검 검증 | 안전보건팀 |
| 컴플라이언스 | `COMPLIANCE` | 하도급 계약서 LLM 자동 검토 | 구매팀 |

### 도메인 기반 권한 시스템

사용자는 각 도메인별로 서로 다른 역할을 가질 수 있습니다:

```java
// 예: 사용자 A는 ESG 도메인에서 결재자, SAFETY 도메인에서 기안자
User user = userRepository.findById(userId);
user.hasRoleInDomain("ESG", "APPROVER");      // true
user.hasRoleInDomain("SAFETY", "DRAFTER");    // true
user.hasRoleInDomain("COMPLIANCE", "APPROVER"); // false
```

---

## 📋 프로젝트 환경

| 항목 | 버전/설정 |
|------|----------|
| Java | 17 |
| Spring Boot | 3.2.x (3.5.9로 명시했으나 확인 필요) |
| PostgreSQL | 15 |
| Build Tool | Gradle |
| DB 환경 | Docker |
| 프론트엔드 | React + Vite + TailwindCSS |

---

## 📁 프로젝트 구조 (권장)

```
src/main/java/com/aivle/esg/
├── EsgPlatformApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── SwaggerConfig.java
│   ├── AsyncConfig.java           # 비동기 처리 설정
│   └── CorsConfig.java
├── common/
│   ├── dto/
│   │   ├── ApiResponse.java       # 공통 응답 래퍼
│   │   ├── PageResponse.java      # 페이지네이션 응답
│   │   └── ErrorResponse.java     # 에러 응답
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java         # 에러 코드 Enum
│   └── util/
│       ├── CodeGenerator.java     # DG-2026-00001 등 코드 생성
│       └── DateTimeUtil.java
├── domain/
│   ├── auth/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   ├── role/
│   ├── diagnostic/
│   ├── approval/
│   ├── review/
│   ├── admin/
│   ├── file/
│   ├── job/
│   └── notification/
└── entity/                        # 또는 domain 내 entity
    ├── User.java
    ├── Company.java
    ├── Domain.java              # 서비스 도메인 (ESG, SAFETY, COMPLIANCE)
    ├── UserDomainRole.java      # 사용자-도메인-역할 매핑
    ├── Diagnostic.java
    └── ... (ERD 기반 엔티티)
```

### 도메인 권한 엔티티 구조

```java
// Domain.java - 서비스 도메인 정의
@Entity
public class Domain extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long domainId;

    @Column(unique = true, nullable = false)
    private String code;          // "ESG", "SAFETY", "COMPLIANCE"

    private String name;          // "ESG 실사", "안전보건", "컴플라이언스"
    private String description;
}

// UserDomainRole.java - 사용자-도메인-역할 매핑
@Entity
@Table(name = "user_domain_role")
public class UserDomainRole extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
}

// User.java - 도메인 권한 조회 메서드
@Entity
public class User extends BaseTimeEntity {
    // ...

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserDomainRole> domainRoles = new ArrayList<>();

    public boolean hasRoleInDomain(String domainCode, String roleCode) {
        return domainRoles.stream()
            .anyMatch(dr -> dr.getDomain().getCode().equals(domainCode)
                         && dr.getRole().getCode().equals(roleCode));
    }

    public List<Domain> getDomainsWithRole(String roleCode) {
        return domainRoles.stream()
            .filter(dr -> dr.getRole().getCode().equals(roleCode))
            .map(UserDomainRole::getDomain)
            .toList();
    }
}
```

---

## 👥 팀 분업 가이드

### 역할 분담 (3인 풀스택)

| 담당자 | 백엔드 도메인 | 프론트엔드 화면 | 비고 |
|--------|-------------|----------------|------|
| **진지현** | Auth, Role, Admin | 로그인, 회원가입, 권한요청, 관리자 | 풀스택 리더, 공통 설정 |
| **김건우** | Diagnostic, File, Job | 기안 작성, 파일 업로드, 진단표 | 파일 파싱 연동 |
| **박세용** | Approval, Review, Notification | 결재, 심사, 알림 | AI 보고서 연동 |

### 공통 작업 (진지현 리드)
- Security 설정
- JWT 토큰 처리
- 공통 응답/에러 처리
- Swagger 설정
- Docker 환경

---

## ✅ 구현 체크리스트

### Phase 1: 공통 인프라 (Day 1-2)

#### 1.1 공통 응답/에러 처리 [진지현]
- [ ] `ApiResponse<T>` 공통 응답 래퍼
- [ ] `ErrorCode` Enum 정의 (AUTH_001, VAL_001 등)
- [ ] `GlobalExceptionHandler` 구현
- [ ] `fieldErrors` 포함 Validation 에러 처리

```java
// ApiResponse.java 예시
@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message("요청이 성공적으로 처리되었습니다")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

#### 1.2 Security 설정 [진지현]
- [ ] JWT 토큰 생성/검증
- [ ] 권한별 접근 제어 (`@PreAuthorize`)
- [ ] 비밀번호 암호화 (BCrypt)

#### 1.3 코드 생성기 [김건우]
- [ ] `CodeGenerator.java` 구현
  - `generateDiagnosticCode()` → `DG-2026-00001`
  - `generateCampaignCode()` → `CMP-2026-001`
  - `generateAuditCode()` → `AUD-2026-00001`

---

### Phase 2: 인증/권한 도메인 (Day 2-3)

#### 2.1 Auth API [진지현]
- [ ] `POST /auth/register` - 회원가입
- [ ] `POST /auth/login` - 로그인
- [ ] `POST /auth/refresh` - 토큰 갱신
- [ ] `POST /auth/logout` - 로그아웃
- [ ] `POST /auth/check-email` - 이메일 중복 확인
- [ ] `GET /auth/me` - 내 정보 조회

**구현 포인트**
```java
// LoginResponse 예시
@Getter @Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private long expiresIn;   // 초 단위
    private UserInfo user;
}
```

#### 2.2 Role API [진지현]
- [ ] `GET /roles/request-page` - 권한 요청 페이지 정보
- [ ] `POST /roles/requests` - 권한 요청 생성
- [ ] `GET /roles/requests/my` - 내 권한 요청 상태
- [ ] `GET /roles/requests` - 권한 요청 목록 (결재자용)
- [ ] `GET /roles/requests/{id}` - 권한 요청 상세
- [ ] `PATCH /roles/requests/{id}` - 권한 승인/반려 ⚠️

**주의: POST → PATCH 변경**
```java
// 기존 (v1.0)
@PostMapping("/roles/requests/{id}/decision")

// 변경 (v2.0) - PATCH 사용
@PatchMapping("/roles/requests/{id}")
public ApiResponse<AccessRequestResponse> processRequest(
    @PathVariable Long id,
    @RequestBody @Valid DecisionRequest request) {
    // decision: APPROVED / REJECTED
}
```

---

### Phase 3: 기안 관리 도메인 (Day 3-5)

#### 3.1 Diagnostic API [김건우]
- [ ] `GET /diagnostics` - 기안 목록 조회
- [ ] `GET /diagnostics/{id}` - 기안 상세 조회
- [ ] `POST /diagnostics` - 기안 신규 생성
- [ ] `GET /diagnostics/{id}/qualitative` - 정성적 평가 문항 조회
- [ ] `PATCH /diagnostics/{id}/qualitative` - 정성적 평가 저장
- [ ] `GET /diagnostics/{id}/quantitative/guide` - 정량 데이터 가이드
- [ ] `POST /diagnostics/{id}/files` - 파일 업로드 (비동기)
- [ ] `GET /diagnostics/{id}/files/{fileId}/parsing-result` - 파싱 결과
- [ ] `GET /diagnostics/{id}/indicators` - 지표 결과 조회
- [ ] `PATCH /diagnostics/{id}/indicators/{indicatorId}` - 지표 수정
- [ ] `GET /diagnostics/{id}/preview` - PDF 미리보기
- [ ] `POST /diagnostics/{id}/submit` - 검수 요청

#### 3.2 File API [김건우]
- [ ] `POST /files/upload` - 공통 파일 업로드
- [ ] `GET /files/{id}/download` - 파일 다운로드
- [ ] `GET /files/{id}/preview-url` - 미리보기 URL 발급
- [ ] `DELETE /files/{id}` - 파일 삭제
- [ ] `GET /files/diagnostics/{id}/package-url` - 패키지 다운로드 URL

#### 3.3 Job API (비동기 처리) [김건우]
- [ ] `GET /jobs/{jobId}` - 작업 상태 조회
- [ ] `POST /jobs/{jobId}/retry` - 작업 재시도

**비동기 처리 구현**
```java
@Service
@RequiredArgsConstructor
public class FileParsingService {
    
    private final JobRepository jobRepository;
    
    @Async("fileParsingExecutor")
    public void parseFileAsync(Long fileId, String jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow();
        job.updateStatus(JobStatus.RUNNING);
        jobRepository.save(job);
        
        try {
            // 파싱 로직...
            job.complete(result);
        } catch (Exception e) {
            job.fail(e.getMessage(), true); // retryable
        }
        jobRepository.save(job);
    }
}
```

---

### Phase 4: 결재/심사 도메인 (Day 4-6)

#### 4.1 Approval API [박세용]
- [ ] `GET /approvals` - 결재 대기 목록
- [ ] `GET /approvals/{id}` - 결재 상세
- [ ] `GET /approvals/{id}/diagnostic-pdf` - 진단표 PDF 탭
- [ ] `GET /approvals/{id}/data-package` - 데이터 패키지 탭
- [ ] `GET /approvals/{id}/revision-log` - 수정 로그 탭
- [ ] `GET /approvals/{id}/ai-report` - AI 보고서 탭
- [ ] `PATCH /approvals/{id}` - 결재 처리 ⚠️
- [ ] `POST /approvals/bulk-download` - 일괄 다운로드

#### 4.2 Review API [박세용]
- [ ] `GET /reviews` - 진단 현황 조회
- [ ] `GET /reviews/{id}` - 심사 상세
- [ ] `PATCH /reviews/{id}/risk-level` - 위험군 변경 ⚠️
- [ ] `PATCH /reviews/{id}` - 심사 결과 처리 ⚠️
- [ ] `POST /reviews/{id}/publish-report` - 보고서 발행 (비동기)
- [ ] `POST /reviews/bulk-report` - 일괄 보고서 생성
- [ ] `POST /reviews/export` - CSV/Excel 내보내기

#### 4.3 Notification API [박세용]
- [ ] `GET /notifications` - 알림 목록
- [ ] `PATCH /notifications/read` - 알림 읽음 처리

---

### Phase 5: 관리자 도메인 (Day 5-7)

#### 5.1 Admin API [진지현]
- [ ] `GET /admin/permissions/dashboard` - 권한 대시보드
- [ ] `PATCH /admin/permissions/{id}` - 권한 처리 (관리자)
- [ ] `GET /admin/users` - 사용자 목록
- [ ] `PATCH /admin/users/{id}/role` - 역할 변경
- [ ] `PATCH /admin/users/{id}/status` - 상태 변경
- [ ] `GET /admin/activity-logs` - 활동 로그
- [ ] `POST /admin/activity-logs/export` - 로그 내보내기
- [ ] `GET /admin/companies` - 협력사 목록
- [ ] `POST /admin/companies` - 협력사 등록

---

## 핵심 구현 가이드

### 0. 도메인 기반 권한 검증 패턴

모든 서비스에서 도메인 기반 권한 검증을 수행합니다:

```java
@Service
@RequiredArgsConstructor
public class DiagnosticService {

    /**
     * 도메인 기반 접근 권한 검증
     * - 도메인이 있는 경우: 해당 도메인에서 필요한 역할 보유 여부 확인
     * - 도메인이 없는 경우 (레거시): 전역 역할로 검증
     */
    private void validateDomainAccess(User user, Diagnostic diagnostic, String requiredRole) {
        Domain domain = diagnostic.getDomain();

        if (domain != null) {
            // 도메인 기반 권한 검증
            if (!user.hasRoleInDomain(domain.getCode(), requiredRole)) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }

            // 같은 회사 검증
            if (!user.getCompany().getCompanyId().equals(diagnostic.getCompany().getCompanyId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_RESOURCE);
            }
        } else {
            // 레거시: 전역 역할로 검증
            if (!requiredRole.equals(user.getRole().getCode())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
            }
        }
    }

    public DiagnosticDetailResponse getDiagnosticDetail(Long userId, Long diagnosticId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSTIC_NOT_FOUND));

        // DRAFTER 역할 검증 (도메인 기반)
        validateDomainAccess(user, diagnostic, "DRAFTER");

        // ... 상세 조회 로직
    }
}
```

### 도메인 기반 목록 조회

```java
public DiagnosticListResponse getDiagnosticList(Long userId, String domainCode, int page, int size) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // DRAFTER 권한을 가진 도메인 목록 조회
    List<Domain> drafterDomains = user.getDomainsWithRole("DRAFTER");

    // 도메인 코드로 필터링 (선택적)
    if (domainCode != null && !domainCode.isEmpty()) {
        drafterDomains = drafterDomains.stream()
            .filter(d -> d.getCode().equals(domainCode))
            .toList();
    }

    // 도메인 기반 조회
    Page<Diagnostic> diagnosticPage = diagnosticRepository
        .findByDomainsAndCompanyOrderByCreatedAtDesc(
            drafterDomains, user.getCompany(), PageRequest.of(page, size));

    // ... 응답 매핑
}
```

### 1. 상태 관리 (State Machine 패턴)

```java
// DiagnosticStatus.java
@Getter
@RequiredArgsConstructor
public enum DiagnosticStatus {
    WRITING("작성중", Set.of(SUBMITTED)),
    SUBMITTED("제출됨", Set.of(RETURNED, APPROVED)),
    RETURNED("반려됨", Set.of(WRITING)),
    APPROVED("내부승인", Set.of(REVIEWING)),
    REVIEWING("심사중", Set.of(COMPLETED)),
    COMPLETED("완료", Set.of());
    
    private final String displayName;
    private final Set<DiagnosticStatus> allowedTransitions;
    
    public boolean canTransitionTo(DiagnosticStatus next) {
        return allowedTransitions.contains(next);
    }
}
```

### 2. 페이지네이션 표준화

```java
// PageResponse.java
@Getter @Builder
public class PageResponse<T> {
    private List<T> content;
    private PageInfo page;
    
    @Getter @Builder
    public static class PageInfo {
        private int number;      // 0-based
        private int size;
        private long totalElements;
        private int totalPages;
    }
    
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(PageInfo.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build())
            .build();
    }
}
```

### 3. 비동기 작업 표준화

```java
// Job.java (Entity)
@Entity
@Table(name = "async_job")
public class Job {
    @Id
    private String jobId;  // UUID
    
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    
    private Integer progress;
    private String message;
    private String resultJson;
    private String errorMessage;
    private Boolean retryable;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // 상태 전이 메서드
    public void start() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete(Object result) {
        this.status = JobStatus.SUCCEEDED;
        this.progress = 100;
        this.resultJson = JsonUtil.toJson(result);
        this.completedAt = LocalDateTime.now();
    }
    
    public void fail(String errorMessage, boolean retryable) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
        this.completedAt = LocalDateTime.now();
    }
}
```

### 4. AI Run API 클라이언트 구현

기획서 기반 공통 AI Run API 클라이언트 패턴:

```java
// AiRunApiClient.java - WebClient 기반 AI API 호출
@Component
public class AiRunApiClient {

    private final WebClient webClient;
    private final int maxRetry;

    public AiRunApiClient(WebClient aiRunApiWebClient, AiRunApiConfig config) {
        this.webClient = aiRunApiWebClient;
        this.maxRetry = config.getMaxRetry();
    }

    /**
     * Preview 호출 - 파일 추가 시 슬롯 추정
     */
    public RunPreviewResponse previewSync(RunPreviewRequest request) {
        return webClient.post()
            .uri("/run/preview")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(RunPreviewResponse.class)
            .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(1)))
            .onErrorMap(this::mapToCustomException)
            .block();
    }

    /**
     * Submit 호출 - 전체 검증 및 판정
     */
    public RunSubmitResponse submitSync(RunSubmitRequest request) {
        return webClient.post()
            .uri("/run/submit")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(RunSubmitResponse.class)
            .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(1)))
            .onErrorMap(this::mapToCustomException)
            .block();
    }

    private Throwable mapToCustomException(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            return new CustomException(ErrorCode.AI_SERVICE_ERROR);
        }
        return new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }
}
```

### 5. AI 분석 서비스 비동기 처리

```java
@Service
@Transactional(readOnly = true)
public class AiAnalysisService {

    @Async
    @Transactional
    public CompletableFuture<AiAnalysisResult> submitAsync(Long diagnosticId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return submit(diagnosticId);
            } catch (Exception e) {
                log.error("AI 분석 실패 - diagnosticId: {}", diagnosticId, e);
                throw new CustomException(ErrorCode.AI_SERVICE_ERROR);
            }
        });
    }

    @Transactional
    public AiAnalysisResult submit(Long diagnosticId) {
        Diagnostic diagnostic = getDiagnostic(diagnosticId);
        String domainCode = getDomainCode(diagnostic);  // ESG, SAFETY, COMPLIANCE

        // 증빙 파일 목록 조회
        List<EvidenceFile> evidenceFiles = evidenceFileRepository.findByDiagnosticId(diagnosticId);
        if (evidenceFiles.isEmpty()) {
            throw new CustomException(ErrorCode.DIAGNOSTIC_MISSING_EVIDENCE);
        }

        // FileInfo, SlotHint 변환
        List<FileInfo> files = evidenceFiles.stream()
            .map(ef -> new FileInfo(ef.getResultFileId().toString(),
                                    ef.getFilePath(), ef.getOriginalFileName()))
            .toList();

        List<SlotHint> slotHints = evidenceFiles.stream()
            .map(ef -> new SlotHint(ef.getResultFileId().toString(),
                                    guessSlotName(ef.getOriginalFileName(), domainCode)))
            .toList();

        // AI API 호출
        RunSubmitRequest request = new RunSubmitRequest(
            packageId, domainCode.toLowerCase(),
            periodStart, periodEnd, files, slotHints
        );
        RunSubmitResponse response = aiRunApiClient.submitSync(request);

        // 결과 저장
        return saveAnalysisResult(diagnostic, domainCode, response);
    }
}
```

### 6. Signed URL 생성 (Azure Blob Storage)

```java
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final BlobServiceClient blobServiceClient;
    
    public String generateSignedUrl(String blobName, Duration validity) {
        BlobClient blobClient = blobServiceClient
            .getBlobContainerClient("files")
            .getBlobClient(blobName);
        
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(validity);
        
        BlobSasPermission permission = new BlobSasPermission()
            .setReadPermission(true);
        
        BlobServiceSasSignatureValues sasValues = 
            new BlobServiceSasSignatureValues(expiryTime, permission);
        
        return blobClient.getBlobUrl() + "?" + 
            blobClient.generateSas(sasValues);
    }
}
```

---

## 📅 일정 (4주차 기준)

| 날짜 | 작업 내용 | 담당자 |
|------|----------|--------|
| Day 1 (1/20) | 공통 인프라, Security 설정 | 진지현 |
| Day 2 (1/21) | Auth API 완료, Role API 시작 | 진지현 |
| Day 2 (1/21) | Diagnostic 기본 CRUD | 김건우 |
| Day 2 (1/21) | Approval 목록/상세 | 박세용 |
| Day 3 (1/22) | Role API 완료, Admin 시작 | 진지현 |
| Day 3 (1/22) | Diagnostic 정성/정량 평가 | 김건우 |
| Day 3 (1/22) | Approval 결재 처리, Review 시작 | 박세용 |
| Day 4 (1/23) | Admin API 완료 | 진지현 |
| Day 4 (1/23) | File/Job API, 비동기 처리 | 김건우 |
| Day 4 (1/23) | Review API 완료 | 박세용 |
| Day 5 (1/24) | 통합 테스트, 버그 수정 | 전원 |
| Day 6 (1/25) | AI 서비스 연동 테스트 | 전원 |
| Day 7 (1/26) | 문서화, 배포 준비 | 전원 |

---

## 🧪 테스트 가이드

### API 테스트 환경
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/esg_platform
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### Swagger UI
- URL: `http://localhost:8080/swagger-ui.html`
- API 문서 자동 생성

### 테스트 시나리오
1. 회원가입 → 로그인 → 토큰 발급
2. 권한 요청 → 승인 → 역할 변경
3. 기안 생성 → 정성 평가 → 파일 업로드 → 제출
4. 결재 목록 → 상세 → 승인
5. 심사 목록 → 위험군 변경 → 보고서 발행

---

## 📝 Git 컨벤션

### 브랜치 전략
```
main
  └── develop
        ├── feature/auth-api
        ├── feature/diagnostic-api
        ├── feature/approval-api
        └── feature/admin-api
```

### 커밋 메시지
```
feat: 로그인 API 구현
fix: 토큰 만료 처리 버그 수정
refactor: 공통 응답 래퍼 리팩토링
docs: API 명세서 업데이트
test: Auth 서비스 단위 테스트 추가
```

---

## ⚠️ 주의사항

1. **PATCH vs POST**: 상태 변경 API는 `PATCH` 사용 (멱등성 보장)
2. **비동기 작업**: 파일 파싱, 리포트 생성은 `202 Accepted` + `jobId` 반환
3. **Signed URL**: 파일 다운로드는 만료 시간 포함된 URL 발급
4. **페이지네이션**: `page`는 0-based, 응답에 `totalElements` 포함
5. **에러 코드**: `ErrorCode` Enum으로 관리, `fieldErrors` 포함

---

## 📚 참고 자료

- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Azure Blob Storage SDK](https://docs.microsoft.com/en-us/azure/storage/blobs/)
- [Swagger/OpenAPI](https://springdoc.org/)
