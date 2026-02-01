# Claude Code Learnings

## 2026-02-01: 권한요청 알림 이벤트 생성 (#89)

### 원인
- RoleRequestService에서 권한요청 생성/승인/반려 시 알림 생성 로직 없음
- NotificationService 주입 안 됨
- NotificationType에 ROLE_REQUEST_CREATED, ROLE_REQUEST_REJECTED 부재

### 해결
- NotificationType enum에 `ROLE_REQUEST_CREATED`, `ROLE_REQUEST_REJECTED` 추가
- UserRepository에 `findAllByRoleCode()` 메서드 추가 (REVIEWER 목록 조회용)
- RoleRequestService에 NotificationService 주입
- `createRoleRequest()`: 권한요청 생성 후 모든 REVIEWER에게 알림 발송
- `processRoleRequest()`: 승인 시 ROLE_APPROVED, 반려 시 ROLE_REQUEST_REJECTED 알림 생성

### 재발 방지
- 이벤트 기반 알림 생성이 필요한 서비스에서 NotificationService 주입 확인
- 새 알림 유형 추가 시 NotificationType enum과 테스트 동시 업데이트
- 트랜잭션 내 알림 생성의 원자성 보장 확인

### 검증 방법
```bash
./gradlew test --tests "RoleRequestServiceTest"
```

### 관련 커밋
- c39a4ac

### 생성/수정 파일
```
src/main/java/.../global/enums/NotificationType.java (ROLE_REQUEST_CREATED, ROLE_REQUEST_REJECTED 추가)
src/main/java/.../domain/user/repository/UserRepository.java (findAllByRoleCode 추가)
src/main/java/.../domain/role/service/RoleRequestService.java (알림 생성 로직 추가)
src/test/java/.../domain/role/service/RoleRequestServiceTest.java (알림 테스트 3건 추가)
```

---

## 2026-02-01: 기안 생성 화면 뒤로가기 시 403 발생 (#91)

### 원인
- submitDiagnostic()에서 권한 검증이 소유자/상태 검증보다 먼저 실행
- 이미 제출된 기안 재요청 시 409 에러 → FE 뒤로가기 시 불필요한 에러
- createDiagnostic()에서 회사 미지정 시 403(PERMISSION_DENIED_RESOURCE) → 400이 적절

### 해결
- submitDiagnostic() 검증 순서 변경: 소유자 → 상태(멱등) → 상태전이 → 권한
- 이미 SUBMITTED인 기안 재요청 시 멱등 성공 응답 반환 (에러 대신)
- COMPANY_NOT_ASSIGNED(U011, 400) ErrorCode 추가, createDiagnostic()에 적용

### 재발 방지
- API 검증 순서: 입력값 → 존재확인 → 소유자 → 상태 → 권한 순서 준수
- 멱등성 필요한 API는 이미 완료된 상태에 대해 성공 응답 반환

### 검증 방법
```bash
./gradlew test --tests "DiagnosticServiceTest"
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../global/error/ErrorCode.java (COMPANY_NOT_ASSIGNED 추가)
src/main/java/.../domain/diagnostic/service/DiagnosticService.java (검증 순서 변경, 멱등 처리)
src/test/java/.../domain/diagnostic/service/DiagnosticServiceTest.java (멱등 테스트 수정)
```

---

## 2026-02-01: APPROVER 역할이 모든 도메인에서 요청 가능 (#82)

### 원인
- RoleRequestService.createRoleRequest()에 도메인-역할 정책 검증 없음
- APPROVER는 ESG 도메인에서만 유효하지만 SAFETY/COMPLIANCE에서도 요청 가능

### 해결
- 도메인 조회 후 `APPROVER + !ESG` 조합 시 `APPROVER_ONLY_ESG` (R007) 에러 반환
- ErrorCode에 `APPROVER_ONLY_ESG` 추가 (HttpStatus.BAD_REQUEST)

### 재발 방지
- 새 도메인-역할 정책 추가 시 createRoleRequest() 검증 로직 확인
- 정책 변경 시 테스트 케이스 동시 업데이트

### 검증 방법
```bash
./gradlew test --tests "RoleRequestServiceTest"
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../global/error/ErrorCode.java (APPROVER_ONLY_ESG 추가)
src/main/java/.../domain/role/service/RoleRequestService.java (도메인-역할 정책 검증)
src/test/java/.../domain/role/service/RoleRequestServiceTest.java (테스트 2건 추가)
```

---

## 2026-02-01: 권한요청 승인 후에도 게스트 유지 (#83)

### 원인
- RoleRequestService 승인 시 UserDomainRole은 생성하지만 User의 전역 역할(role)을 GUEST에서 변경하지 않음
- JWT 토큰이 전역 역할만 포함하므로 재로그인해도 GUEST로 표시
- 로그인/me 응답에 도메인 역할 정보 미포함

### 해결
- 승인 시 전역 역할이 GUEST이면 요청된 역할로 업그레이드 (`user.changeRole(role)`)
- DomainRoleDto 신규 생성, UserInfoDto/MyInfoResponse에 domainRoles 필드 추가
- AuthService의 buildUserInfoDto()/getMyInfo()에서 UserDomainRoleRepository로 도메인 역할 조회 후 응답에 포함

### 재발 방지
- 권한 변경 시 전역 역할과 도메인 역할 동시 업데이트 확인
- 응답 DTO에 도메인 역할 포함 여부 검증

### 검증 방법
```bash
./gradlew test --tests "RoleRequestServiceTest"
./gradlew test --tests "AuthServiceTest"
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../dto/auth/common/DomainRoleDto.java (신규)
src/main/java/.../dto/auth/common/UserInfoDto.java (domainRoles 추가)
src/main/java/.../dto/auth/myinfo/MyInfoResponse.java (domainRoles 추가)
src/main/java/.../domain/auth/service/AuthService.java (도메인 역할 조회 로직)
src/main/java/.../domain/role/service/RoleRequestService.java (전역 역할 업그레이드)
src/test/java/.../domain/role/service/RoleRequestServiceTest.java (승인+역할 업그레이드 테스트)
src/test/java/.../domain/auth/service/AuthServiceTest.java (mock 추가)
```

---

## 2026-02-01: 업로드 처리 파이프라인 단계 정보 누락 (#88)

### 원인
- AsyncJob 엔티티에 현재 처리 단계(phase) 필드가 없어 파이프라인 진행 상황 추적 불가
- JobStatusResponse에 단계 정보 미포함으로 FE에서 OCR→검증→메트릭 단계 표시 불가
- 기존 progress/message 필드만으로는 현재 단계와 전체 단계 수 파악 어려움

### 해결
- `PipelinePhase` enum 추가: QUEUED, OCR, VALIDATION, METRICS, COMPLETED
- `AsyncJob.currentPhase` 필드 추가 및 상태 전환 메서드 구현
- `JobStatusResponse.PipelineInfo` 추가: currentPhase, phaseDescription, phaseOrder, totalPhases
- `advancePhase()`, `advanceToNextPhase()` 메서드로 단계 전환 관리

### 재발 방지
- 새 파이프라인 단계 추가 시 PipelinePhase enum과 순서(order) 동시 업데이트
- AI 콜백에서 advancePhase() 호출하여 실제 진행 상태 반영
- 단계별 진행률은 totalPhases 기준으로 자동 계산

### 검증 방법
```bash
./gradlew test --tests "PipelinePhaseTest"
./gradlew test --tests "JobServiceTest"
```

### 관련 커밋
- 61e118e

### 생성/수정 파일
```
src/main/java/.../global/enums/PipelinePhase.java (신규)
src/main/java/.../domain/job/entity/AsyncJob.java (currentPhase 필드, 메서드 추가)
src/main/java/.../domain/job/service/JobService.java (PipelineInfo 빌드 로직)
src/main/java/.../dto/job/JobStatusResponse.java (PipelineInfo 내부 클래스 추가)
src/test/java/.../global/enums/PipelinePhaseTest.java (신규)
src/test/java/.../domain/job/service/JobServiceTest.java (파이프라인 테스트 2건 추가)
```

---

## 2026-02-01: 로그인 실패 사유별 에러코드 미분기 (#85)

### 원인
- 로그인 시 비밀번호 검증만 수행하고 계정 상태(비활성화, 잠김, 이메일 미인증) 검증 없음
- User 엔티티에 `emailVerified`, `locked` 필드 부재
- ErrorCode에 `ACCOUNT_NOT_VERIFIED`, `ACCOUNT_LOCKED`, `ACCOUNT_DISABLED` 부재

### 해결
- User 엔티티에 `emailVerified`(boolean), `locked`(boolean) 필드 추가
- ErrorCode에 A005(ACCOUNT_NOT_VERIFIED), A006(ACCOUNT_LOCKED), A007(ACCOUNT_DISABLED) 추가
- AuthService.login()에 비밀번호 검증 후 계정 상태 검증 분기 추가
- 보안 고려: 사용자 미존재/비밀번호 오류 모두 INVALID_CREDENTIALS 반환 (이메일 존재 여부 미노출)

### 재발 방지
- 새 계정 상태 추가 시 login() 검증 분기와 ErrorCode 동시 업데이트
- 보안: 인증 실패 시 구체적 사유 노출 최소화 원칙 유지

### 검증 방법
```bash
./gradlew test --tests "AuthServiceTest"
```

### 관련 커밋
- acab7c4

### 생성/수정 파일
```
src/main/java/.../global/error/ErrorCode.java (A005, A006, A007 추가)
src/main/java/.../domain/user/entity/User.java (emailVerified, locked 필드 추가)
src/main/java/.../domain/auth/service/AuthService.java (계정 상태 검증 분기 추가)
src/test/java/.../domain/auth/service/AuthServiceTest.java (로그인 실패 테스트 3건 추가)
```

---

## 2026-02-01: 기안 목록 조회 필터링(status, keyword) 미구현

### 원인
- Controller에 `keyword` 파라미터 자체가 없어 검색 기능 불가
- 프론트엔드가 `status`(단수)로 보내지만 백엔드는 `statuses`(복수)로 받아 매칭 실패
- Repository 복합 쿼리에서 빈 리스트 IN 절 문제 잔존 (boolean 플래그 미적용)
- 기존 if/else if 구조로 status + keyword + deadline 동시 필터링 불가

### 해결
- Controller에 `keyword`, `status`(alias) 파라미터 추가
- Repository에 `findByFilters()` 통합 쿼리 추가 (boolean 플래그로 빈 IN 절 회피)
- Repository에 `findByCompanyAndFilters()` 레거시 통합 쿼리 추가
- Service의 if/else if 분기를 단일 통합 쿼리 호출로 교체
- keyword는 title, diagnosticCode, company.name에 LIKE 검색

### 재발 방지
- 새 필터 파라미터 추가 시 프론트엔드 파라미터명과 1:1 대조
- JPQL IN 절에는 항상 boolean 플래그 패턴 사용
- 필터 조건은 if/else 분기 대신 단일 쿼리에 optional 조건으로 통합

### 검증 방법
```bash
./gradlew test --tests "DiagnosticServiceTest"
./gradlew build
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../diagnostic/controller/DiagnosticController.java (keyword, status 파라미터 추가)
src/main/java/.../diagnostic/service/DiagnosticService.java (통합 필터링 로직)
src/main/java/.../diagnostic/repository/DiagnosticRepository.java (findByFilters, findByCompanyAndFilters 추가)
src/test/java/.../diagnostic/service/DiagnosticServiceTest.java (keyword, status 필터 테스트 추가)
```

---

## 2026-02-01: 파일 목록 조회 500 에러 및 삭제 409 에러 (#60)

### 원인
- `GET /api/v1/diagnostics/{id}/files` 파일 목록 조회 엔드포인트가 미구현되어 500 (S001) 발생
- `uploadFile()`에서 진단 상태(SUBMITTED+) 검증이 누락되어 제출된 진단에도 업로드 허용
- 업로드는 되지만 삭제 시 `isSubmittedOrLater()` 검증에 걸려 409 (BIZ_001) 발생

### 해결
- `FileController`에 `GET /api/v1/diagnostics/{diagnosticId}/files` 엔드포인트 추가
- `FileService.getFileList()` 메서드 추가 (EvidenceFileDto 리스트 반환)
- `FileService.uploadFile()`에 `isSubmittedOrLater()` 검증 추가 (WRITING/RETURNED만 허용)
- 테스트 3건 추가: 파일 목록 조회 성공, 빈 목록 반환, 제출된 진단 업로드 차단

### 재발 방지
- 프론트엔드가 호출하는 API 엔드포인트와 백엔드 구현 1:1 대조 필수
- 파일 업로드/삭제 등 상태 의존 API는 동일한 상태 검증 로직 적용
- `FileServiceTest`에서 상태별 업로드/삭제 시나리오 검증

### 검증 방법
```bash
./gradlew test --tests "FileServiceTest"
./gradlew build
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../file/controller/FileController.java (파일 목록 조회 엔드포인트 추가)
src/main/java/.../file/service/FileService.java (getFileList, 업로드 상태 검증 추가)
src/test/java/.../file/service/FileServiceTest.java (테스트 3건 추가)
```

---

## 2026-01-30: SlotResult DTO에 extras 필드 누락 (#72)

### 원인
- AI Python 서비스의 `SlotResult` 스키마에 `extras: dict[str, str]` 필드 존재
- 백엔드 Java `SlotResult` record에 해당 필드 누락
- AI 응답 역직렬화 시 extras 데이터가 무시되어 손실

### 해결
- `SlotResult.java`에 `Map<String, String> extras` 필드 추가
- `AiAnalysisResultDetailResponse.java`에 `getMapValue()` 헬퍼 메서드 추가
- JSON 파싱 시 extras 필드를 `Map<String, String>`으로 안전하게 변환
- verdict 주석에서 AI가 실제로 사용하지 않는 `WARN` 값 제거

### 재발 방지
- AI 레포와 백엔드 레포 간 DTO 스키마 비교 체크리스트 운용
- 새 AI API 연동 시 Python Pydantic 스키마와 Java record 1:1 대조 필수
- AI 레포 `apps/ai_run_api/app/schemas/run.py`가 SSOT

### 검증 방법
```bash
./gradlew compileJava  # 빌드 성공
./gradlew test         # 전체 테스트 통과
```

### 관련 커밋
- aefde70

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/dto/ai/run/SlotResult.java (extras 필드 추가)
src/main/java/com/smartchain/platform/dto/ai/AiAnalysisResultDetailResponse.java (getMapValue 헬퍼 추가)
```

---

## 2026-01-30: AiRunApiClient 에러 핸들링 강화 (#59)

### 원인
- 4xx 클라이언트 에러(잘못된 요청)와 5xx 서버 에러가 구분되지 않고 일괄 `AI_SERVICE_ERROR`로 처리됨
- WebClient 타임아웃/네트워크 에러가 `AI_SERVICE_UNAVAILABLE`이 아닌 일반 에러로 처리됨
- AI 서비스가 반환하는 에러 응답 body가 로깅되지 않아 디버깅 어려움

### 해결
- `AI_BAD_REQUEST` (AI007) 에러코드 추가 - 4xx 응답 시 요청 파라미터 문제로 판단
- `mapToCustomException()` 메서드로 에러 유형별 분기 처리 (4xx→AI_BAD_REQUEST, 5xx→AI_SERVICE_ERROR, 타임아웃→AI_SERVICE_UNAVAILABLE)
- `isNetworkOrTimeoutError()` 메서드로 다양한 타임아웃 예외 감지 (ConnectTimeoutException, SocketTimeoutException, TimeoutException)
- `logErrorResponseBody()` 메서드로 에러 응답 body 로깅

### 재발 방지
- WebClient 에러 핸들링 시 `onErrorMap()`으로 에러 유형별 CustomException 매핑 패턴 사용
- 외부 API 호출 시 4xx/5xx/타임아웃 에러 분기 처리 필수
- 에러 응답 body 로깅으로 디버깅 용이성 확보

### 검증 방법
```bash
./gradlew test --tests "AiRunApiClientErrorHandlingTest"
```

### 관련 커밋
- 451b6a5

### 생성/수정 파일
```
build.gradle (MockWebServer 테스트 의존성 추가)
src/main/java/com/smartchain/platform/
├── global/error/ErrorCode.java (AI_BAD_REQUEST 추가)
├── domain/ai/client/AiRunApiClient.java (에러 핸들링 메서드 추가)
src/test/java/com/smartchain/platform/domain/ai/client/AiRunApiClientErrorHandlingTest.java (신규)
```

---

## 2026-01-30: AI Run API 응답 값 검증 로직 추가 (#58)

### 원인
- `AiAnalysisService.saveAnalysisResult()`에서 AI 응답의 `verdict`, `riskLevel` 값을 검증 없이 저장
- `AiRunApiClient`에서 응답 필수 필드(packageId, verdict, riskLevel) null 체크 없음
- 잘못된 값이 DB에 저장되면 프론트엔드 로직 오류 및 데이터 무결성 문제 발생 가능

### 해결
- `AiVerdict` enum 신규 생성: `PASS`, `WARN`, `NEED_CLARIFY`, `NEED_FIX` (docs 명세 기준)
- `RiskLevel` enum에 `isValid()`, `fromString()`, `validValuesString()` 검증 메서드 추가
- `AiRunApiClient.validateSubmitResponse()`: submit 응답 수신 직후 필수 필드 null 체크 및 값 검증
- `ErrorCode`에 AI 응답 검증 에러코드 추가: `AI004` (응답 유효성), `AI005` (verdict), `AI006` (riskLevel)

### 재발 방지
- AI 서비스와 Backend 간 DTO 값 정의는 docs/API_CONTRACT_SSOT.md를 SSOT로 유지
- 새로운 verdict/riskLevel 값 추가 시 enum과 문서 동시 업데이트 필수
- `AiResponseValidationTest`로 enum 검증 로직 자동 테스트

### 검증 방법
```bash
./gradlew test --tests "AiResponseValidationTest"
```

### 관련 커밋
- 05d00a8

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/
├── global/enums/AiVerdict.java (신규)
├── global/enums/RiskLevel.java (검증 메서드 추가)
├── global/error/ErrorCode.java (AI 검증 에러코드 추가)
├── domain/ai/client/AiRunApiClient.java (validateSubmitResponse 추가)
src/test/java/com/smartchain/platform/domain/ai/validation/AiResponseValidationTest.java (신규)
```

---

## 2026-01-29: 기안 목록 조회 500 에러 - JPQL 빈 IN 절 오류

### 원인
- `DiagnosticRepository`의 복합 JPQL 쿼리에서 `d.domain IN :reviewerDomains` 등에 빈 리스트가 전달됨
- DRAFTER 역할만 가진 사용자가 목록 조회 시 `reviewerDomains`, `approverDomains`가 빈 리스트
- PostgreSQL에서 `IN ()` 빈 절은 SQL 문법 오류로 500 에러 발생

### 해결
- JPQL 쿼리에 `hasReviewer`, `hasApprover`, `hasDrafter` boolean 플래그 파라미터 추가
- `:hasXxx = true AND d.domain IN :xxxDomains` 패턴으로 빈 리스트일 때 조건 자체를 비활성화
- 모든 도메인 리스트가 비어있으면 쿼리 실행 없이 `Page.empty()` 반환

### 재발방지
- JPQL `IN` 절에 빈 리스트 전달 시 DB별 동작이 다름 - 항상 비어있는 경우를 별도 처리
- 복합 OR 조건에서 각 분기별 존재 여부 플래그를 사용하는 패턴 적용

### 검증방법
- `./gradlew test` 전체 통과
- `GET /api/v1/diagnostics?domainCode=ESG` 요청으로 200 응답 확인

### 관련파일
- `DiagnosticRepository.java` - JPQL 쿼리에 boolean 플래그 추가
- `DiagnosticService.java` - 빈 리스트 가드 및 플래그 전달 로직

---

## 2026-01-29: API 명세서 최종 업데이트 (#17)

### 원인
- DiagnosticController Swagger 어노테이션에 잘못된 도메인 코드(ENV, SOC, GOV) 기재
- 역할별 도메인 권한 매트릭스 문서화 부재
- 도메인 기반 권한 검증 요구사항 명세 불충분

### 해결
- Swagger 도메인 코드 수정: `ENV/SOC/GOV` → `ESG/SAFETY/COMPLIANCE`
- API_QUICK_REFERENCE.md: 역할별 도메인 권한 매트릭스 섹션 추가
- API_SPECIFICATION.md: 도메인 기반 권한 검증 테이블 추가
- `ApiDocumentationConsistencyTest`: Swagger 어노테이션 일관성 검증 테스트

### 재발 방지
- 새 도메인 추가 시 모든 Swagger 어노테이션 업데이트 확인
- `ApiDocumentationConsistencyTest`가 잘못된 도메인 코드 사용 시 빌드 실패

### 검증 방법
```bash
./gradlew test --tests "ApiDocumentationConsistencyTest"
```

### 관련 커밋
- 518db07

### 생성/수정 파일
```
docs/API_QUICK_REFERENCE.md (역할별 도메인 권한 매트릭스 추가)
docs/API_SPECIFICATION.md (도메인 기반 권한 검증 섹션 추가)
src/main/java/.../DiagnosticController.java (Swagger 수정)
src/test/java/.../docs/ApiDocumentationConsistencyTest.java (신규)
```

---

## 2026-01-28: API 통합 테스트 프레임워크 구축 (#16)

### 원인
- @SpringBootTest 기반 API 통합 테스트 부재
- E2E API 동작 검증 불가
- JWT 인증이 포함된 API 테스트 프레임워크 미구축

### 해결
- `ApiSmokeTest`: @SpringBootTest + @AutoConfigureMockMvc 기반 통합 테스트 추가
- JWT 토큰 생성 및 Authorization 헤더 설정 패턴 구축
- 기본 API 엔드포인트 접근성 테스트 (Health, Diagnostic 목록, 404 응답)
- @Transactional로 테스트 간 데이터 격리

### 재발 방지
- 새 API 엔드포인트 추가 시 통합 테스트 필수
- 테스트 데이터 설정은 @BeforeEach에서 수행
- 인증이 필요한 API는 JWT 토큰 헤더 필수

### 검증 방법
```bash
./gradlew test --tests "ApiSmokeTest"
```

### 관련 커밋
- 4acc6aa

### 생성/수정 파일
```
src/test/java/com/smartchain/platform/integration/ApiSmokeTest.java (신규)
```

---

## 2026-01-28: 도메인 권한 체계 단위 테스트 추가 (#15)

### 원인
- 도메인 기반 권한 체계에 대한 단위 테스트 부재
- User 엔티티의 도메인 권한 헬퍼 메서드(getRoleForDomain, hasRoleInDomain 등) 테스트 없음
- UserDomainRoleRepository의 쿼리 메서드 검증 테스트 없음

### 해결
- `UserDomainPermissionTest`: User 엔티티의 도메인 권한 헬퍼 메서드 단위 테스트 22개 추가
  - getRoleForDomain(), hasRoleInDomain(), hasAnyRoleInDomain(), getDomainsWithRole()
  - addDomainRole(), removeDomainRole() 메서드
  - 복합 시나리오 (여러 도메인/역할 조합)
- `UserDomainRoleRepositoryTest`: Repository 통합 테스트 12개 추가
  - CRUD 메서드, 페치 조인 쿼리, 복합 조건 쿼리

### 재발 방지
- 새로운 권한 관련 기능 추가 시 User 엔티티 헬퍼 메서드 테스트 필수
- Repository 쿼리 메서드는 @DataJpaTest로 통합 테스트 작성

### 검증 방법
```bash
./gradlew test --tests "UserDomainPermissionTest"
./gradlew test --tests "UserDomainRoleRepositoryTest"
```

### 관련 커밋
- 2d29017

### 생성/수정 파일
```
src/test/java/com/smartchain/platform/
├── domain/user/entity/UserDomainPermissionTest.java (신규)
└── domain/user/repository/UserDomainRoleRepositoryTest.java (신규)
```

---

## 2026-01-28: ReviewController 도메인 필터링 추가 (#13)

### 원인
- 심사 API에서 도메인별 필터링 미지원
- REVIEWER가 여러 도메인 권한을 가질 때 특정 도메인만 조회 불가
- 대시보드/목록 응답에 도메인 정보가 포함되지 않아 FE에서 도메인 구분 어려움

### 해결
- GET `/api/v1/reviews`에 `domainCode` 쿼리 파라미터 추가
- GET `/api/v1/reviews/dashboard`에 `domainCode` 쿼리 파라미터 추가
- 응답 DTO에 `domainCode`, `domainName` 필드 추가
- `ReviewService`에서 단일 도메인 필터링 및 권한 검증 로직 구현
- `ReviewRepository`에 단일 도메인 조회용 쿼리 메서드 추가

### 재발 방지
- #12 ApprovalController 구현 패턴을 참고하여 일관된 구조 유지
- 기존 테스트 시그니처 변경 시 모든 호출부 확인 필수
- 도메인 필터링 시 권한 검증을 먼저 수행

### 검증 방법
```bash
./gradlew test --tests "ReviewServiceTest"
./gradlew build
```

### 관련 커밋
- b8fc218

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/
├── domain/review/
│   ├── controller/ReviewController.java (domainCode 파라미터 추가)
│   ├── repository/ReviewRepository.java (단일 도메인 쿼리 추가)
│   └── service/ReviewService.java (필터링 로직 구현)
├── dto/review/
│   ├── dashboard/ReviewDashboardRequest.java (domainCode 추가)
│   ├── detail/ReviewDetailResponse.java (domainCode, domainName 추가)
│   ├── list/ReviewListItemDto.java (domainCode, domainName 추가)
│   └── list/ReviewListRequest.java (domainCode 추가)
src/test/java/.../ReviewServiceTest.java (도메인 필터링 테스트 4건 추가)
```

---

## 2026-01-28: ApprovalController 도메인 필터링 추가 (#12)

### 원인
- 결재 API에서 도메인별 필터링 미지원
- 사용자가 여러 도메인에서 APPROVER 역할을 가질 때 특정 도메인만 조회 불가
- 응답 DTO에 도메인 정보가 포함되지 않아 FE에서 도메인 구분 어려움

### 해결
- GET `/api/v1/approvals`에 `domainCode` 쿼리 파라미터 추가 (선택)
- GET `/api/v1/approvals/{id}` 응답에 `domainCode`, `domainName` 필드 추가
- `ApprovalService`에서 domainCode 필터 시 권한 검증 강화
- `ApprovalRepository`에 단일 도메인 쿼리 메서드 추가

### 재발 방지
- 도메인 기반 API 구현 시 필터링과 권한 검증 함께 고려
- 응답 DTO에 도메인 정보 포함하여 FE 연동 용이하게
- 레거시 데이터(domain=null) 호환성 유지 필수

### 검증 방법
```bash
./gradlew test --tests "ApprovalServiceTest"
./gradlew build
```

### 관련 커밋
- 92aa38b

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/
├── domain/approval/
│   ├── controller/ApprovalController.java (domainCode 파라미터 추가)
│   ├── repository/ApprovalRepository.java (단일 도메인 쿼리 추가)
│   └── service/ApprovalService.java (필터링 로직 구현)
├── dto/approval/
│   ├── detail/ApprovalDetailResponse.java (domainCode, domainName 추가)
│   ├── list/ApprovalListItemDto.java (domainCode, domainName 추가)
│   └── list/ApprovalListRequest.java (domainCode 추가)
src/test/java/.../ApprovalServiceTest.java (도메인 필터링 테스트 4건 추가)
```

---

## 2026-01-28: AI Run API 공통 클라이언트 구현

### 원인
- 기획서(AI_RunAPI_기획서)는 공통 `/run/preview`, `/run/submit` API 구조를 정의
- GitHub 이슈(#22-31)는 도메인별 분리된 AI 클라이언트 구조로 설계
- 두 접근 방식 간 설계 불일치 존재

### 해결
- 기획서 기반 공통 AI Run API 클라이언트 구현 선택
- 구현 내용:
  - `AiRunApiClient`: WebClient 기반 `/run/preview`, `/run/submit` 호출
  - `AiAnalysisService`: Diagnostic과 AI API 연동, 비동기 처리 지원
  - `AiAnalysisResult` 엔티티: 분석 결과 저장
  - `AiAnalysisController`: REST API 엔드포인트 제공

### 재발 방지
- 기획서와 이슈 간 설계 불일치 발견 시 먼저 방향 확인 질문
- 엔드포인트 충돌 방지를 위해 기존 컨트롤러 엔드포인트 확인 후 경로 설계
- JPA Repository 메서드명은 엔티티의 실제 ID 필드명 확인 필수 (예: `diagnosticId` vs `id`)

### 검증 방법
```bash
./gradlew build     # 빌드 성공
./gradlew test      # 123 tests passed
```

### 관련 커밋
- (커밋 예정)

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/
├── domain/ai/
│   ├── client/AiRunApiClient.java
│   ├── config/AiRunApiConfig.java
│   ├── controller/AiAnalysisController.java
│   ├── entity/AiAnalysisResult.java
│   ├── repository/AiAnalysisResultRepository.java
│   └── service/AiAnalysisService.java
├── dto/ai/
│   ├── AiAnalysisRequest.java
│   ├── AiAnalysisResultResponse.java
│   ├── AiPreviewRequest.java
│   └── run/
│       ├── Clarification.java
│       ├── FileInfo.java
│       ├── RunPreviewRequest.java
│       ├── RunPreviewResponse.java
│       ├── RunSubmitRequest.java
│       ├── RunSubmitResponse.java
│       ├── SlotHint.java
│       ├── SlotResult.java
│       └── SlotStatus.java
├── global/
│   ├── config/AsyncConfig.java
│   └── error/ErrorCode.java (AI 에러코드 추가)
build.gradle (webflux 의존성 추가)
application.yaml (AI 설정 추가)
```

### API 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/ai/run/diagnostics/{id}/preview` | 슬롯 추정 |
| POST | `/api/v1/ai/run/diagnostics/{id}/submit` | 전체 검증 요청 |
| GET | `/api/v1/ai/run/diagnostics/{id}/result` | 최신 결과 조회 |
| GET | `/api/v1/ai/run/diagnostics/{id}/history` | 분석 이력 조회 |

---

## 2026-01-28: Domain 관리 API 추가 (#14)

### 원인
- 도메인(ESG, SAFETY, COMPLIANCE) 목록/상세 조회 API 부재
- 프론트엔드에서 도메인 정보를 하드코딩해야 하는 상황
- 도메인 코드 유효성 검증을 위한 공개 엔드포인트 필요

### 해결
- `DomainController`: 도메인 목록/상세 조회 REST API (인증 불필요)
- `DomainService`: 도메인 조회 비즈니스 로직 (대소문자 무관 코드 검색)
- `DomainResponse`: 도메인 응답 DTO (record 타입)
- 기존 `DomainRepository` 메서드 재사용 (`findByCode`, `findByIsActiveTrue`)

### 재발 방지
- 공개 API 설계 시 인증 요구사항 명확히 문서화
- 코드 검색은 대소문자 무관하게 처리 (toUpperCase 변환)
- 기존 Repository 메서드 확인 후 재사용 우선

### 검증 방법
```bash
./gradlew test --tests "DomainServiceTest"
./gradlew build
```

### 관련 커밋
- 6d6df4d

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/
├── domain/user/
│   ├── controller/DomainController.java (신규)
│   └── service/DomainService.java (신규)
├── dto/domain/
│   └── DomainResponse.java (신규)
src/test/java/.../DomainServiceTest.java (신규, 테스트 7건)
```

### API 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/domains` | 도메인 목록 조회 (includeInactive 옵션) |
| GET | `/api/v1/domains/{code}` | 도메인 상세 조회 |

---

## 2026-01-29: 서비스 플로우 문서 현행화

### 원인
- 기존 `docs/service_flow.png`가 초기 버전으로 현재 코드베이스와 불일치
- 도메인 기반 권한 체계(UserDomainRole) 미반영
- 도메인별 워크플로우 차이 미표현 (ESG는 결재 단계 있음, SAFETY/COMPLIANCE는 결재 없음)
- AI Run API, 수신자 심사 플로우, 알림 시스템 등 신규 기능 누락

### 해결
- `docs/service_flow.md` 신규 작성 (텍스트 기반 다이어그램)
- 도메인별 분리된 워크플로우 문서화:
  - ESG: 3단계 (기안자 → 결재자 → 수신자)
  - SAFETY/COMPLIANCE: 2단계 (기안자 → 수신자, 결재 없음)
- 상태 전이 요약 (WRITING → SUBMITTED → APPROVED → REVIEWING → COMPLETED)
- 수신자 공통 플로우 (심사 대시보드, 리포트 생성, 데이터 내보내기)

### 재발 방지
- 워크플로우 변경 시 `docs/service_flow.md` 동기화 필수
- 도메인별 기능 차이는 명시적으로 문서화
- 코드 레벨에서 SAFETY/COMPLIANCE 결재 스킵 로직 구현 시 문서 업데이트

### 검증 방법
- 문서 리뷰: `docs/service_flow.md` 내용과 실제 코드 동작 비교
- 도메인별 상태 전이 흐름이 코드와 일치하는지 확인

### 관련 커밋
- (별도 커밋 예정)

### 생성/수정 파일
```
docs/service_flow.md (신규)
```

---

## #10 AI API 문서-구현 불일치 수정 (2026-01-30)

### 원인
- AI Run API 초기 설계 문서가 실제 Python AI 서비스 및 Java DTO 구현과 동기화되지 않음
- verdict 값: 문서 `PASS/FAIL/PENDING` vs 실제 `PASS/WARN/NEED_CLARIFY/NEED_FIX`
- riskLevel 값: 문서 `LOW/MEDIUM/HIGH/CRITICAL` vs 실제 `LOW/MEDIUM/HIGH`
- Preview 응답의 `missingRequiredSlots` 필드 문서 누락
- 도메인별 전체 슬롯 목록(ESG 15개, Safety 8개, Compliance 7개) 미문서화
- result/history 엔드포인트가 백엔드 DB 조회임이 명시되지 않음

### 해결
- 6개 문서 파일에서 verdict/riskLevel 값 통일
- Preview 응답에 `missingRequiredSlots` 추가, 잘못된 `clarifications` 제거
- SSOT에 도메인별 전체 슬롯 정의 테이블 추가
- BACKEND_IMPLEMENTATION_GUIDE에 AI 파이프라인 아키텍처 섹션 추가
- STATUS_AND_ERROR_CODES에 `BIZ_004` (DIAGNOSTIC_MISSING_EVIDENCE) 에러 코드 추가
- FE polling 로직: verdict 기반 → 결과 존재 여부 기반으로 수정

### 재발 방지
- AI API 스키마 변경 시 docs/ 하위 모든 관련 문서 동시 업데이트 필수
- AI 레포와 백엔드 레포 간 DTO 값(verdict, riskLevel) 동기화 체크리스트 운용

### 검증 방법
- 각 문서에서 `FAIL`, `PENDING`(verdict 컨텍스트), `CRITICAL` 검색하여 잔존 여부 확인
- Java RunSubmitResponse DTO 주석과 문서 일치 확인

### 관련 커밋
- (별도 커밋 예정)

### 생성/수정 파일
```
docs/API_CONTRACT_SSOT.md
docs/API_QUICK_REFERENCE.md
docs/API_SPECIFICATION.md
docs/FE_INTEGRATION_RULES.md
docs/STATUS_AND_ERROR_CODES.md
docs/BACKEND_IMPLEMENTATION_GUIDE.md
docs/claude/LEARNINGS.md
```

---

## #62 Preview의 missingRequiredSlots 활용 로직 추가 (2026-01-30)

### 원인
- RunPreviewResponse에 missingRequiredSlots 필드가 있지만 활용되지 않음
- 필수 슬롯 미제출 상태에서 submit 요청 시 AI 서비스까지 갔다가 실패하는 비효율
- 백엔드에서 사전 검증 없이 모든 요청을 AI 서비스로 전달

### 해결
- `AiAnalysisService.validateRequiredSlots()` 메서드 추가
- submit() 호출 전 SlotConfigProperties의 필수 슬롯과 제출 파일 슬롯 비교
- 누락된 필수 슬롯 있으면 `AI_MISSING_REQUIRED_SLOTS` (AI007) 에러로 조기 차단
- ErrorCode에 AI007 추가, docs/STATUS_AND_ERROR_CODES.md 동기화

### 재발 방지
- AI 서비스 호출 전 비용이 드는 작업은 백엔드에서 사전 검증 패턴 적용
- 필수 슬롯 정의 변경 시 application.yaml과 테스트 동기화 필수
- `AiAnalysisServiceTest.validateRequiredSlots` 테스트가 로직 검증

### 검증 방법
```bash
./gradlew test --tests "AiAnalysisServiceTest"
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../ai/service/AiAnalysisService.java (validateRequiredSlots 추가)
src/main/java/.../global/error/ErrorCode.java (AI007 추가)
src/test/java/.../ai/service/AiAnalysisServiceTest.java (신규)
docs/STATUS_AND_ERROR_CODES.md (AI007 문서화)
```
