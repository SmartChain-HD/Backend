# Claude Code Learnings

## 2026-02-11: REVISION_REQUIRED 처리 시 간헐적 500 에러 (DiagnosticHistory comment 길이 불일치)

### 원인
- `DiagnosticHistory.comment` 엔티티는 `@Column(length = 2000)`이나, DB DDL 스키마(`ERD_DDL.sql`)는 `VARCHAR(500)`으로 불일치
- AI 분석 결과(clarifications)를 보완 사유 초안으로 자동 채우는 기능으로 인해 500자 초과 comment 발생 가능
- 500자 초과 comment가 `diagnosticHistoryRepository.save()` 시 `DataIntegrityViolationException` 발생 → `GlobalExceptionHandler`의 범용 핸들러가 HTTP 500 / S001 반환

### 해결
- `ERD_DDL.sql`의 `diagnostic_history.comment`를 `VARCHAR(500)` → `VARCHAR(2000)`으로 변경하여 엔티티와 일치시킴
- `ReviewDecisionRequest.comment`에 `@Size(max = 2000)` 유효성 검증 추가 → 초과 시 400 에러 반환
- 긴 코멘트(2000자) 보완 요청 테스트 케이스 추가

### 재발방지
- 엔티티 `@Column(length)` 값과 DDL 스키마의 `VARCHAR(N)` 값을 항상 동기화할 것
- DTO에 `@Size` 검증을 추가하여 DB 제약 위반 전에 사용자 친화적 에러 반환

### 검증방법
- `./gradlew test --tests "ReviewServiceTest"` 전체 통과 (긴 코멘트 테스트 포함)

---

## 2026-02-09: 결재 상세 API에서 기안자 maskedName 누락

### 원인
- 목록 조회(`RequesterDto`)에는 `maskedName` 필드가 있으나 상세 조회(`RequesterDetailDto`)에는 누락
- 목록/상세가 서로 다른 DTO를 사용하면서 필드 불일치 발생
- 서비스에서 `RequesterDetailDto` 빌드 시 `maskedName` 설정 코드 누락

### 해결
- `RequesterDetailDto`에 `maskedName` 필드 추가
- `ApprovalService.getApprovalDetail()`에서 `NameMaskingUtil.mask()` 호출하여 값 설정

### 재발방지
- 목록/상세 DTO 간 공통 필드(userId, name, maskedName) 불일치 여부 점검
- 새 DTO 필드 추가 시 프론트엔드가 사용하는 필드 목록과 대조

### 검증방법
- `./gradlew test --tests "ApprovalServiceTest"` 전체 통과
- 상세 조회 테스트에서 `maskedName`, `email` 필드 검증 추가

---

## 2026-02-08: AI Preview 다량 파일 시 무반응(타임아웃) 문제

### 원인
- `AiRunApiClient.previewSync()`가 `.block()`으로 서블릿 스레드를 블로킹
- preview와 submit이 동일한 타임아웃(180초) × 재시도(3회) 설정을 공유
- 최악의 경우 ~727초(12분) 동안 서블릿 스레드가 점유되어 프론트에서 "아무 반응 없음"

### 해결
- `AiRunApiConfig`에 preview 전용 설정 분리: `previewTimeoutSeconds(30)`, `previewMaxRetry(1)`
- `AiRunApiClient.preview()`에 `Mono.timeout(Duration.ofSeconds(30))` 추가
- 최악의 경우 ~62초로 단축, 타임아웃 시 AI001 에러 코드로 명확한 응답 반환

### 재발방지
- 동기 블로킹(`.block()`) 사용 시 반드시 용도별 타임아웃 분리
- preview(경량 조회)와 submit(중량 처리)은 성격이 다르므로 설정을 분리할 것

### 검증방법
- `./gradlew test --tests "AiAnalysisServiceTest"` 전체 통과
- preview 요청 시 30초 내 응답 또는 타임아웃 에러 반환 확인

### 관련커밋
- (커밋 전)

---

## 2026-02-08: AI Preview 간헐적 500 에러 및 파싱 미완료 파일 처리

### 원인
- `AiAnalysisService.preview()`에서 `diagnostic.getPeriodStartDate().format()`를 호출하는데, `periodStartDate`/`periodEndDate`가 nullable 컬럼이라 NPE 발생 → catch-all handler가 500 + `S001` 반환
- `toFileInfo()`에서 `EvidenceFile.parsingStatus`를 검증하지 않아 파싱 미완료(WAITING/PROCESSING) 파일도 AI 서비스로 전달
- 프론트에서 AI001~AI006만 처리하므로 `S001`, `S003` 등 코드가 "알 수 없는 오류"로 표시

### 해결
- `ErrorCode`에 `AI_FILE_NOT_READY(AI009)`, `AI_MISSING_PERIOD_DATES(AI010)` 추가
- `preview()`/`submit()`에 `periodStartDate`/`periodEndDate` null 체크 추가
- `toFileInfo()`에 `parsingStatus != SUCCESS` 검증 추가
- `submit()`에도 동일한 파싱 상태 일괄 검증 추가

### 재발방지
- nullable 필드를 참조할 때는 반드시 null 체크 후 접근
- 비동기 처리(파일 파싱) 결과에 의존하는 API는 상태 검증 필수

### 검증방법
- `./gradlew test --tests "AiAnalysisServiceTest"` — 파싱 미완료 테스트 케이스 포함 전체 통과
- 프론트에서 파일 업로드 직후 Add 클릭 시 AI009 에러 코드 반환 확인

### 관련커밋
- (커밋 전)

---

## 2026-02-06: 반려 후 재제출 시 Review 중복 생성 버그 수정

### 원인
- 심사자가 반려(REVISION_REQUIRED) 후 기안자가 재제출하면 심사 목록에 2개의 Review가 나타남
- `ApprovalService.submitToReviewer()`, `DiagnosticService.submitDiagnostic()`에서 항상 새 Review를 생성하는 로직
- 기존 REVISION_REQUIRED 상태의 Review를 무시하고 새로 생성

### 해결
- `ReviewRepository.findByDiagnostic()` 메서드 추가
- `Review.resubmit()` 메서드 추가 (상태를 REVIEWING으로 되돌리고 제출 시간 업데이트)
- `ApprovalService.submitToReviewer()`: 기존 Review가 있으면 `resubmit()` 호출하여 재사용
- `DiagnosticService.submitDiagnostic()` (SAFETY/COMPLIANCE): 동일하게 기존 Review 재사용

### 재발방지
- 워크플로우에서 상태 전이 시 새 엔티티 생성 전에 기존 엔티티 존재 여부 확인
- 반려 → 재제출 시나리오는 기존 레코드 재사용이 일반적

### 검증방법
- `./gradlew build` 전체 빌드 및 테스트 통과
- 프론트엔드에서 반려 → 재제출 → 심사 목록 확인

### 관련커밋
- (커밋 전)

---

## 2026-02-06: 기안 삭제 API 500 에러 수정 (FK constraint 위반)

### 원인
- `DELETE /api/v1/diagnostics/{id}` 호출 시 500 Internal Server Error 발생
- `Diagnostic` 엔티티를 참조하는 연관 엔티티(DiagnosticHistory, EvidenceFile, ResultQual, ResultQuant, AiAnalysisResult)가 FK constraint로 연결되어 있어 삭제 불가
- Diagnostic 엔티티에 cascade 설정이 없었고, 서비스 코드에서 연관 데이터 삭제 로직도 없었음

### 해결
- 각 Repository에 `deleteAllByDiagnostic` 메서드 추가:
  - `DiagnosticHistoryRepository.deleteAllByDiagnostic(Diagnostic)`
  - `EvidenceFileRepository.deleteAllByDiagnostic_DiagnosticId(Long)`
  - `AiAnalysisResultRepository.deleteAllByDiagnostic_DiagnosticId(Long)`
- 신규 Repository 생성:
  - `ResultQualRepository.deleteAllByDiagnostic_DiagnosticId(Long)`
  - `ResultQuantRepository.deleteAllByDiagnostic_DiagnosticId(Long)`
- `DiagnosticService.deleteDiagnostic()` 수정: 연관 엔티티 먼저 삭제 후 Diagnostic 삭제

### 재발방지
- 엔티티 삭제 기능 구현 시 FK 참조 관계 확인 필수
- `@JoinColumn` 검색으로 참조 엔티티 파악: `grep -r "@JoinColumn.*diagnostic_id"`
- 삭제 순서: 참조하는 자식 엔티티 → 부모 엔티티

### 검증방법
- `./gradlew test --tests "DiagnosticServiceTest"` - 기안 삭제 테스트 통과
- `./gradlew test` - 전체 테스트 통과 (431 tests)

### 관련커밋
- (커밋 전)

---

## 2026-02-05: 서버 사이드 이름 마스킹 (개인정보 보호법 제29조 준수)

### 원인
- API 응답에서 사용자 이름이 평문으로 노출되어 네트워크 탭에서 원본 이름 확인 가능
- 프론트엔드 마스킹만으로는 개인정보 보호법 제29조(안전조치의무) 미충족

### 해결
- `NameMaskingUtil` 유틸리티 클래스 신설 (`global/util/NameMaskingUtil.java`)
- 마스킹 규칙: 1글자 → 그대로, 2글자 → 첫+*, 3글자 → 첫+*+끝, 4글자+ → 첫+**...+끝
- 10개 API 대상 DTO에 `maskedName` 필드 추가 (기존 `name` 필드 유지)
- 6개 서비스 (Auth, Approval, Diagnostic, Review, RoleRequest, Management)에서 빌더 호출 시 마스킹 적용
- 동일 구조의 ProcessedByDto가 3개 패키지(approval/detail, review/common, role/common)에 분산되어 있어 모두 통일 수정

### 재발방지
- 신규 사용자 이름 포함 DTO 추가 시 반드시 `maskedName` 필드 포함
- `NameMaskingUtil.mask()` 사용하여 서비스 레이어에서 마스킹 적용

### 검증방법
- `NameMaskingUtilTest` 단위 테스트 (null/빈값, 1~4+글자, 영문 포함)
- `./gradlew build` 전체 빌드 및 테스트 통과

### 관련커밋
- (커밋 전)

---

## 2026-02-05: 외부 위험 감지 API 연동 - REVIEWER 전용 (#183)

### 원인
- REVIEWER 전용 외부 리스크 감지 기능 필요 (협력사 리스크 분석)
- 기존 AI Run API 패턴 재사용하여 새로운 `/risk/external/detect` API 연동

### 해결
- **ErrorCode**: RISK001~004 에러코드 추가
- **ExternalRiskApiConfig**: `ai.risk-api` 설정 기반 WebClient 빈 생성 (AiRunApiConfig 패턴 복사)
- **DTO 4개**: Backend↔AI 통신용 + Frontend↔Backend 통신용 분리
- **ExternalRiskApiClient**: WebClient 기반 AI 서버 통신 (retry/에러핸들링)
- **ExternalRiskResult**: JPA 엔티티 (리스크 결과 저장, BaseTimeEntity 상속)
- **ExternalRiskService**: userId 기반 User 조회 + REVIEWER 권한 검증 + AI 호출 + 결과 저장
- **ExternalRiskController**: REST 엔드포인트 3개 (detect, results/{companyId}, results)
- **SecurityConfig**: `/api/v1/risk/**` REVIEWER 전용 제한 추가

### 재발방지
- 새 도메인 API 추가 시 SecurityConfig에 경로 권한 설정 필수
- 서비스 메서드는 Controller에서 userId(Long)를 받아 내부에서 User 조회하는 패턴 준수
- WebClient 빈 이름 충돌 주의 (@Qualifier로 구분)

### 검증방법
- `./gradlew test --tests "ExternalRiskServiceTest"` (단위 테스트 8개)
- `./gradlew test && ./gradlew build` (전체 빌드 통과)

### 관련커밋
- feature/refactor-data-initializer 브랜치에서 작업

## 2026-02-04: 기안 상태 불일치 - 심사중/완료 동시 존재 (#156)

### 원인
- 기안 목록에서 기안(Diagnostic) 상태는 실제 진행 상태(REVIEWING 등) 표시
- 캠페인(Campaign) 상태는 날짜 기반으로 동적 계산(DRAFT/ACTIVE/CLOSED)
- CampaignSimpleDto에 status 필드가 없어 프론트엔드에서 별도 계산
- 캠페인 종료일 지났으나 기안이 아직 심사 중이면 상태 불일치 발생

### 해결
- **CampaignSimpleDto**: `status`, `statusLabel` 필드 추가
- **DiagnosticService.mapToListItemDto()**: 기안 상태 기반으로 캠페인 상태 결정
  - 기안 COMPLETED → 캠페인 "COMPLETED" (완료)
  - 그 외 상태 → 캠페인 "ACTIVE" (진행중)
- 기안 목록 응답에서 기안/캠페인 상태가 일관되게 표시됨

### 재발방지
- DTO 간 상태 표시 로직이 다를 경우 불일치 발생 가능
- 관련 엔티티 상태를 표시할 때는 단일 소스(기안 상태)를 기준으로 결정
- 날짜 기반 상태 계산은 캠페인 목록에서만 사용, 기안 목록에서는 기안 상태 기반 사용

### 검증방법
```bash
./gradlew test --tests "DiagnosticServiceTest"
./gradlew build -x test
```

### 관련커밋
- feature/156-campaign-status-consistency 브랜치

### 생성/수정 파일
```
src/main/java/.../dto/diagnostic/common/CampaignSimpleDto.java (+status, +statusLabel)
src/main/java/.../domain/diagnostic/service/DiagnosticService.java (+determineCampaignStatusByDiagnostic, +getCampaignStatusLabel)
src/test/java/.../domain/diagnostic/service/DiagnosticServiceTest.java (+3 테스트 케이스)
```

---

## 2026-02-04: 반려→재승인 시 상태 처리 오류 수정 (#155)

### 원인
- 수신자(REVIEWER)가 반려(REVISION_REQUIRED)한 심사를 대시보드에서 다시 승인하려 할 때
- 기존 `processReview()` 메서드가 `REVIEWING` 상태에서만 처리 가능하도록 구현됨
- `REVISION_REQUIRED` 상태의 심사를 재승인하는 기능이 없어 의도치 않은 동작 발생

### 해결
- **Review 엔티티**: `isRevisionRequired()` 메서드 추가, `revertToReviewing()` 메서드 추가
- **ReviewService.processReview()**: `REVISION_REQUIRED` 상태에서도 재승인(APPROVED) 가능하도록 수정
  - `REVISION_REQUIRED` + `APPROVED` 요청 → 기안 상태를 REVIEWING으로 되돌린 후 COMPLETED 처리
  - `REVISION_REQUIRED` + `REVISION_REQUIRED` 요청 → 중복 방지 에러 반환
- **테스트 추가**: 재승인 성공 케이스, 중복 보완요청 실패 케이스

### 재발방지
- 상태 전이 로직 구현 시 모든 가능한 상태 조합 검토 필요
- 특히 "취소/롤백" 시나리오가 필요한지 기획 단계에서 확인
- 심사 워크플로우 변경 시 Review/Diagnostic 상태 동기화 주의

### 검증방법
```bash
./gradlew test --tests "ReviewServiceTest"
./gradlew build -x test
```

### 관련커밋
- feature/155-fix-compliance-reapproval-v3 브랜치

### 생성/수정 파일
```
src/main/java/.../domain/review/entity/Review.java (+isRevisionRequired, +revertToReviewing)
src/main/java/.../domain/review/service/ReviewService.java (processReview 로직 수정)
src/test/java/.../domain/review/service/ReviewServiceTest.java (+2 테스트 케이스)
```

---

## 2026-02-04: 전체 결재 페이지 제거 - domainCode 필수화 (#162)

### 원인
- 결재 목록 조회 API에서 `domainCode` 없이 호출 시 "전체 결재" 조회 가능
- 기획상 도메인별 결재 페이지만 존재해야 함
- 전체 결재 페이지는 존재하면 안 됨

### 해결
- `ApprovalService.getApprovalList()`에서 `domainCode` 필수 검증 추가
- `domainCode`가 null이거나 비어있으면 `DOMAIN_CODE_REQUIRED` 에러 반환
- ErrorCode에 `DOMAIN_CODE_REQUIRED` (AP005) 추가
- 레거시 모드에서도 `domainCode` 유효성 검증 수행

### 재발방지
- 목록 조회 API에서 필수 필터 파라미터는 서비스 레이어에서 검증
- 기획에서 "존재하면 안 되는 페이지"는 API 레벨에서 차단

### 검증방법
```bash
./gradlew test --tests "ApprovalServiceTest"
```

### 관련커밋
- fix/162-approval-domain-required 브랜치

### 생성/수정 파일
```
src/main/java/.../domain/approval/service/ApprovalService.java (domainCode 필수 검증)
src/main/java/.../global/error/ErrorCode.java (DOMAIN_CODE_REQUIRED 추가)
src/test/java/.../domain/approval/service/ApprovalServiceTest.java (테스트 수정)
docs/claude/LEARNINGS.md (엔트리 추가)
```

---

## 2026-02-04: 권한 요청 반려 API 오류 (#154)

### 원인
- `RoleRequest.isPending()` 메서드에서 `this.status == RequestStatus.PENDING` 비교 사용
- status가 null인 경우 `false`를 반환하여 "이미 처리된 요청입니다" 에러 발생
- `==` 비교는 null-safe하지 않음

### 해결
- `isPending()` 메서드를 `RequestStatus.PENDING.equals(this.status)`로 수정
- null-safe 비교로 변경하여 NullPointerException 및 잘못된 false 반환 방지

### 재발방지
- enum 비교 시 `==` 대신 `EnumType.VALUE.equals(field)` 패턴 사용 권장
- 특히 DB에서 조회한 엔티티의 필드는 null일 수 있음을 고려
- 상태 검증 메서드는 null-safe하게 구현

### 검증방법
```bash
./gradlew test --tests "RoleRequestServiceTest"
```

### 관련커밋
- fix/154-role-request-reject 브랜치

### 생성/수정 파일
```
src/main/java/.../domain/user/entity/RoleRequest.java (isPending 메서드 수정)
docs/claude/LEARNINGS.md (엔트리 추가)
```

---

## 2026-02-04: AI Chatbot 백엔드 통합 구현 (#140, #141)

### 원인
- AI 팀에서 FastAPI 기반 RAG 챗봇 서비스 개발 완료
- 백엔드에서 AI Chat API를 래핑하여 프론트엔드에 통합 API 제공 필요
- 인증/권한 관리, 에러 핸들링, 세션 관리 등 백엔드 레이어 추가 필요

### 해결
- **Issue #140 (기반 구조)**:
  - DTO 7개 생성: ChatRequest, ChatMessage, ChatResponse, SourceItem, SourceLocation, AdminSyncResponse, AdminInspectResponse
  - AiChatConfig: WebClient 설정 (@ConfigurationProperties)
  - AiChatApiClient: AI FastAPI 서버 통신 (chat, syncData, inspectDb)
  - ErrorCode 3개 추가: AI_CHAT_SERVICE_ERROR, AI_CHAT_TIMEOUT, AI_CHAT_INVALID_DOMAIN
  - SecurityConfig: /api/v1/chat/**, /api/v1/admin/chat/** 경로 권한 설정

- **Issue #141 (비즈니스 로직)**:
  - ChatService: 채팅 처리, 도메인 검증, REVIEWER 권한 검증
  - ChatController: REST API 엔드포인트 3개
  - ChatServiceTest: 단위 테스트 10개

### 재발방지
- 외부 AI 서비스 연동 시 WebClient 기반 클라이언트 + Config 분리 패턴 사용
- Admin API는 REVIEWER 권한으로 제한, SecurityConfig에서 경로별 권한 명시
- 도메인 유효성 검증 (safety, compliance, esg, all)은 서비스 레이어에서 수행
- sessionId 자동 생성으로 클라이언트 부담 감소

### 검증방법
```bash
./gradlew test --tests "ChatServiceTest"
./gradlew build -x test
```

### 관련커밋
- PR #142 (기반 구조), PR #143 (비즈니스 로직)

### 생성/수정 파일
```
src/main/java/.../dto/chat/ (7개 DTO)
src/main/java/.../domain/chat/config/AiChatConfig.java
src/main/java/.../domain/chat/client/AiChatApiClient.java
src/main/java/.../domain/chat/service/ChatService.java
src/main/java/.../domain/chat/controller/ChatController.java
src/main/java/.../global/error/ErrorCode.java (CHAT001-003 추가)
src/main/java/.../global/config/SecurityConfig.java (경로 추가)
src/main/resources/application.yaml (ai.chat 설정 추가)
src/test/java/.../domain/chat/service/ChatServiceTest.java
docs/FE_AI_CHATBOT_INTEGRATION.md (FE 통합 가이드)
```

### API 엔드포인트
| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/chat` | AI 채팅 | DRAFTER, APPROVER, REVIEWER |
| POST | `/api/v1/admin/chat/sync` | Vector DB 동기화 | REVIEWER |
| GET | `/api/v1/admin/chat/inspect` | DB 현황 조회 | REVIEWER |

---

## 2026-02-04: 계정 잠금 기능 구현 (로그인 실패 제한)

### 원인
- 로그인 시도 무제한으로 브루트포스 공격에 취약
- 프론트엔드 요청: 5회 실패 시 임시 잠금, 10회 실패 시 영구 잠금

### 해결
- **User 엔티티 수정**:
  - `failedLoginAttempts`: 로그인 실패 횟수
  - `lockedUntil`: 임시 잠금 해제 시간
  - `permanentlyLocked`: 영구 잠금 여부
- **잠금 정책**:
  - 5회 연속 실패 → 15분 임시 잠금 (A006)
  - 10회 연속 실패 → 영구 잠금 (A005)
- **ErrorCode 변경**:
  - A005: `ACCOUNT_PERMANENTLY_LOCKED` (영구 잠금)
  - A006: `ACCOUNT_TEMPORARILY_LOCKED` (임시 잠금)
  - A009: `ACCOUNT_NOT_VERIFIED` (기존 A005에서 이동)
- **응답에 잠금 정보 포함**:
  - `lockedUntil`, `remainingMinutes`, `remainingAttempts`
- **CustomException, ErrorResponse에 data 필드 추가**

### 재발방지
- 보안 관련 에러 코드는 프론트엔드와 사전 협의 필수
- 계정 상태 관련 검증은 비밀번호 검증보다 먼저 수행

### 검증방법
```bash
./gradlew test --tests "*AuthServiceTest*login*"
```

### 관련커밋
- dev 브랜치 직접 푸시

### 생성/수정 파일
```
User.java (잠금 필드 및 메서드 추가)
AuthService.java (잠금 처리 로직)
ErrorCode.java (A005, A006, A009 변경)
CustomException.java (data 필드 추가)
ErrorResponse.java (data 필드 추가)
GlobalExceptionHandler.java (data 포함 응답)
LoginResponse.java (경고 메시지 필드)
AccountLockInfo.java (신규 - 잠금 정보 DTO)
AuthServiceTest.java (테스트 수정)
```

---

## 2026-02-04: Google reCAPTCHA v3 로그인 보안 검증 구현

### 원인
- 로그인 API에 봇/자동화 공격 방지 보안 레이어 부재
- 프론트엔드에서 reCAPTCHA v3 구현 요청에 따른 백엔드 검증 필요

### 해결
- `RecaptchaConfig`: reCAPTCHA 설정 (secretKey, scoreThreshold, enabled)
- `RecaptchaService`: Google reCAPTCHA API 검증 서비스
  - POST `https://www.google.com/recaptcha/api/siteverify`로 토큰 검증
  - success 체크, action 변조 방지, score 임계값(0.5) 검증
- `LoginRequest`에 `recaptchaToken` 필드 추가
- `AuthService.login()`에 reCAPTCHA 검증 로직 추가 (비밀번호 검증 전 수행)
- `ErrorCode`에 `RECAPTCHA_FAILED` (CAPTCHA_001), `RECAPTCHA_LOW_SCORE` (CAPTCHA_002) 추가

### 재발방지
- 보안 검증 로직은 주요 인증 로직 앞에 배치
- 환경변수로 enabled 플래그 제공하여 개발/테스트 환경에서 비활성화 가능
- score 임계값은 환경변수로 조정 가능 (기본 0.5)

### 검증방법
```bash
./gradlew build -x test
./gradlew test --tests "*AuthServiceTest*login*"
```

### 관련커밋
- feature/recaptcha-login-validation-v2 브랜치

### 생성/수정 파일
```
src/main/java/.../global/config/RecaptchaConfig.java (신규)
src/main/java/.../domain/auth/service/RecaptchaService.java (신규)
src/main/java/.../dto/auth/login/LoginRequest.java (recaptchaToken 추가)
src/main/java/.../domain/auth/service/AuthService.java (검증 로직 추가)
src/main/java/.../global/error/ErrorCode.java (CAPTCHA_001, CAPTCHA_002 추가)
src/main/resources/application.yaml (recaptcha 설정 추가)
src/test/java/.../domain/auth/service/AuthServiceTest.java (mock 추가)
```

---

## 2026-02-03: 캠페인/기안/회원가입 버그 수정 (#134)

### 원인
1. **이메일 인증 버그**: 회원가입 전 이메일 인증 시 User가 없어서 `ifPresent()`가 동작 안함
2. **회사 연결 버그**: `RoleRequestService.processRoleRequest()`에서 `user.company` 설정 누락
3. **캠페인 필터링 버그**: `CampaignService.getCampaignList()`가 권한/상태 필터링 없이 전체 반환
4. **종료 캠페인 진단 생성**: `DiagnosticService.createDiagnostic()`에서 캠페인 종료 검증 없음

### 해결
1. 회원가입 시 `findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc()`로 인증 완료 여부 확인, `emailVerified=true`로 생성
2. `User.changeCompany()` 메서드 추가, 승인 시 회사 연결 로직 추가
3. `CampaignRepository`에 도메인/상태 필터링 쿼리 추가, 사용자 권한 기반 필터링 구현
4. 캠페인 `periodEndDate` 검증 추가, `CAMPAIGN_CLOSED` 에러 코드 추가

### 재발방지
- 회원가입/인증 플로우에서 관련 엔티티 상태 동기화 체크리스트 확인
- 권한 승인 시 연관 엔티티(User.company, UserDomainRole) 양방향 업데이트 확인
- 목록 조회 API는 권한/상태 필터링 기본 적용

### 검증방법
1. 회원가입 → 이메일 인증 → 로그인 성공 확인
2. 권한 요청 승인 → `/auth/me`에서 company 확인
3. 캠페인 목록 → 권한에 따른 필터링 확인

### 관련커밋
- fix/campaign-auth-bugs 브랜치, PR #134

### 생성/수정 파일
```
AuthService.java (회원가입 시 인증 확인)
EmailVerificationCodeRepository.java (verified 조회 메서드)
User.java (changeCompany 메서드)
RoleRequestService.java (승인 시 회사 연결)
CampaignRepository.java (필터링 쿼리)
CampaignService.java (권한 기반 필터링)
CampaignController.java (userId 기반 API)
DiagnosticService.java (캠페인 종료 검증)
ErrorCode.java (CAMPAIGN_CLOSED)
```

---

## 2026-02-03: DataInitializer 데이터 정합성 수정 (#132)

### 원인
- SAFETY/COMPLIANCE 도메인에서도 APPROVER 역할 부여 (비즈니스 규칙 위반)
- 캠페인 기간이 4개월 단위 규칙을 따르지 않음

### 해결
- APPROVER 역할을 ESG 도메인으로 제한
- 캠페인 기간을 4개월 단위로 통일 (1~4월, 5~8월, 9~12월)

### 재발방지
- 도메인별 역할 정책: ESG만 결재자(APPROVER) 존재
- 캠페인 기간 규칙: 4개월 단위, 첫달 1일 ~ 마지막달 말일

### 검증방법
- 로컬 실행 후 테스트 계정 권한 확인
- 캠페인 기간 데이터 확인

### 관련커밋
- refactor/data-initializer-132 브랜치, PR #135

### 생성/수정 파일
```
DataInitializer.java (UserDomainRole, Campaign 데이터 수정)
```

---

## 2026-02-02: 이메일 인증 코드 실제 발송 기능 구현 (#116)

### 원인
- AuthService.sendVerificationCode()에서 인증 코드 생성 및 DB 저장만 수행
- 실제 이메일 발송 없이 로그로만 코드 출력 (`log.info("Verification code sent...")`)
- spring-boot-starter-mail 의존성 없음, EmailService 구현체 부재

### 해결
- `spring-boot-starter-mail` 의존성 추가
- `EmailService` 인터페이스 정의 + 프로파일별 구현체 분리
  - `SmtpEmailService` (@Profile("dev")): 실제 SMTP 발송
  - `LocalEmailService` (@Profile({"local", "test"})): 로깅만
- `AuthService`에 `EmailService` 주입 및 `sendVerificationCode()` 호출
- `ErrorCode.EMAIL_SEND_FAILED` (S004) 추가
- `application.yaml` dev 프로파일에 SMTP 환경변수 설정 추가

### 재발방지
- 외부 서비스(이메일, SMS 등) 연동 시 인터페이스 + 프로파일별 구현체 분리 패턴 적용
- 로컬/테스트 환경에서는 실제 외부 호출 없이 로깅으로 대체
- 운영 환경 설정값은 환경변수로 주입 (민감정보 하드코딩 금지)

### 검증방법
```bash
./gradlew test --tests "AuthServiceTest"
./gradlew test --tests "EmailServiceTest"
```

### 관련커밋
- feature/116-email-send-impl 브랜치, PR #117

### 생성/수정 파일
```
build.gradle (spring-boot-starter-mail 의존성 추가)
src/main/java/.../domain/auth/service/EmailService.java (신규 - 인터페이스)
src/main/java/.../domain/auth/service/SmtpEmailService.java (신규 - SMTP 구현)
src/main/java/.../domain/auth/service/LocalEmailService.java (신규 - 로컬 로깅)
src/main/java/.../domain/auth/service/AuthService.java (EmailService 주입)
src/main/java/.../global/error/ErrorCode.java (EMAIL_SEND_FAILED 추가)
src/main/resources/application.yaml (SMTP 설정 추가)
src/test/java/.../domain/auth/service/AuthServiceTest.java (mock 추가)
src/test/java/.../domain/auth/service/EmailServiceTest.java (신규)
```

---

## 2026-02-02: diagnostic_code 중복 키 제약 조건 위반 (동시 요청 race condition)

### 원인
- `generateDiagnosticCode()`가 `MAX(diagnosticId)`를 조회하여 시퀀스 번호 생성
- 동시 요청 시 두 트랜잭션이 같은 max ID를 읽어 동일한 코드 생성
- `diagnostic_diagnostic_code_key` unique 제약 조건 위반으로 `ConstraintViolationException` 발생

### 해결
- PostgreSQL 시퀀스 `diagnostic_code_seq` 도입 (`nextval`은 트랜잭션 격리 수준과 무관하게 원자적)
- `DiagnosticRepository.getNextDiagnosticCodeSequence()` 네이티브 쿼리로 시퀀스 조회
- `data.sql`에서 시퀀스 생성 및 기존 데이터 기반 시작값 동기화
- 테스트용 `schema.sql` 추가 (H2 호환)

### 재발방지
- 유니크 코드 생성 시 애플리케이션 레벨 MAX+1 패턴 대신 DB 시퀀스 사용
- 네이티브 쿼리 사용 시 테스트 환경(H2)에서도 해당 DB 객체가 존재하는지 확인

### 검증방법
```bash
./gradlew test
```

### 관련커밋
- fix/109-email-verified-not-updated 브랜치 (diagnostic_code 중복 수정 포함)

### 생성/수정 파일
```
src/main/java/.../diagnostic/repository/DiagnosticRepository.java (findMaxDiagnosticId → getNextDiagnosticCodeSequence)
src/main/java/.../diagnostic/service/DiagnosticService.java (generateDiagnosticCode 시퀀스 기반으로 변경)
src/main/resources/data.sql (시퀀스 생성 SQL 추가)
src/test/resources/schema.sql (신규 - H2 테스트용 시퀀스)
src/test/resources/application-test.yaml (sql.init.mode: always, defer-datasource-initialization: true)
src/test/java/.../diagnostic/service/DiagnosticServiceTest.java (mock 메서드명 변경)
```

---

## 2026-02-02: 이메일 인증 완료 후 User.emailVerified 미업데이트 (#109)

### 원인
- `AuthService.verifyEmail()`에서 `EmailVerificationCode.markAsVerified()`만 호출
- User 엔티티의 `emailVerified` 필드를 `true`로 변경하는 로직이 누락됨
- User 엔티티에 `emailVerified`를 변경하는 메서드 자체가 없었음
- 결과: 이메일 인증을 완료해도 로그인 시 A005 에러 발생

### 해결
- `User.verifyEmail()` 메서드 추가
- `AuthService.verifyEmail()`에서 User 조회 후 `user.verifyEmail()` 호출
- 기존 계정 보정용 data.sql UPDATE 유지

### 재발방지
- 인증/상태 변경 플로우 구현 시 관련된 모든 엔티티의 상태가 업데이트되는지 확인
- `EmailVerificationCode`와 `User.emailVerified`는 별개 엔티티이므로 양쪽 모두 처리 필수

### 검증방법
```bash
./gradlew test --tests "AuthServiceTest"
```

### 관련커밋
- fix/109-email-verified-not-updated 브랜치

### 생성/수정 파일
```
src/main/java/.../domain/user/entity/User.java (verifyEmail() 메서드 추가)
src/main/java/.../domain/auth/service/AuthService.java (verifyEmail에서 User 상태 업데이트)
```

---

## 2026-02-02: 테스트 계정 email_verified 미활성화로 로그인 실패 (#106)

### 원인
- User 엔티티의 `emailVerified` 기본값이 `false`
- email_verified 컬럼 추가 이전에 생성된 기존 계정들이 모두 미인증 상태
- dev 환경에서 data.sql이 실행되지 않아 기존 데이터 보정 불가

### 해결
- data.sql에 `UPDATE "user" SET email_verified = true WHERE email_verified = false` 추가
- dev 프로파일에 `sql.init.mode: always` + `defer-datasource-initialization: true` 설정 추가
- 서버 기동 시 자동으로 기존 계정의 이메일 인증 상태 활성화

### 재발방지
- User 엔티티에 새 boolean 필드 추가 시 기존 데이터 마이그레이션 SQL 필수
- data.sql의 UPDATE문은 멱등(idempotent)하게 작성 (WHERE 조건으로 중복 실행 안전)
- `@Table(name = "\"User\"")` → Hibernate는 소문자 `"user"`로 생성함. data.sql에서도 `"user"` 사용 필수

### 검증방법
- 서버 재기동 후 테스트 계정으로 로그인 시 A005 에러 없이 정상 응답 확인

### 관련커밋
- fix/106-email-verified-activation 브랜치

### 생성/수정 파일
```
src/main/resources/data.sql (UPDATE문 추가)
src/main/resources/application.yaml (dev 프로파일 sql.init 설정 추가)
```

---

## 2026-02-02: 특수문자(!) 포함 비밀번호 로그인 500 에러 (#104)

### 원인
- 클라이언트가 `!` 등 특수문자를 `\!`로 이스케이프하여 JSON 전송
- Jackson 기본 설정에서 비표준 이스케이프 시퀀스(`\!`)를 파싱하지 못함
- `JsonMappingException: Unrecognized character escape '!'` 발생하여 500 반환

### 해결
- `JacksonConfig` 설정 클래스 추가
- `Jackson2ObjectMapperBuilderCustomizer`로 `ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER` 활성화
- Spring Boot 기본 ObjectMapper 설정 유지하면서 기능만 추가

### 재발방지
- 외부 클라이언트 입력을 받는 JSON 파서는 관대한(lenient) 설정 고려
- 특수문자 포함 비밀번호 테스트 케이스 필수

### 검증방법
```bash
./gradlew test --tests "JacksonConfigTest"
```

### 관련커밋
- fix/104-login-special-char-500 브랜치

### 생성/수정 파일
```
src/main/java/.../global/config/JacksonConfig.java (신규)
src/test/java/.../global/config/JacksonConfigTest.java (신규)
```

---

## 2026-02-01: 도메인별 워크플로우 분기 구현 (#78)

### 원인
- submitDiagnostic()에서 모든 도메인(ESG, SAFETY, COMPLIANCE)에 대해 동일하게 Approval 레코드 생성
- 기획상 SAFETY/COMPLIANCE는 결재 단계 없이 수신자(REVIEWER)에게 직행해야 함
- SAFETY/COMPLIANCE 진단 제출 시 결재 단계에 막혀 심사로 진행 불가

### 해결
- submitDiagnostic()에 도메인별 분기 추가: ESG → Approval 생성, SAFETY/COMPLIANCE → approve() + Review 생성
- SAFETY/COMPLIANCE는 자동 승인(AUTO_APPROVED) 히스토리 기록 후 Review 레코드 생성
- DiagnosticSubmitResponse에 reviewId 필드 추가 (SAFETY/COMPLIANCE 응답용)
- 도메인이 null인 레거시 데이터는 ESG로 기본 처리

### 재발 방지
- 도메인별 워크플로우 차이: ESG=3단계(기안→결재→심사), SAFETY/COMPLIANCE=2단계(기안→심사)
- 새 도메인 추가 시 submitDiagnostic()의 분기 로직 확인 필수

### 검증 방법
```bash
./gradlew test --tests "DiagnosticServiceTest"
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/main/java/.../diagnostic/service/DiagnosticService.java (도메인별 분기, ReviewRepository 주입)
src/main/java/.../dto/diagnostic/submit/DiagnosticSubmitResponse.java (reviewId 필드 추가)
src/test/java/.../diagnostic/service/DiagnosticServiceTest.java (SAFETY/COMPLIANCE/ESG 제출 테스트 3건 추가)
docs/claude/LEARNINGS.md (엔트리 추가)
```

---

## 2026-02-01: 도메인 상세 조회 API 권한 검증 테스트 보강 (#87)

### 원인
- 기안 상세 조회 API의 권한 검증 구현은 완료되어 있었으나 테스트 커버리지 부족
- APPROVER(같은/다른 회사), REVIEWER, 도메인 권한 없는 사용자의 403/200 분기 테스트 미존재
- DRAFTER 성공/실패와 404만 테스트되고 나머지 역할 시나리오 누락

### 해결
- APPROVER 같은 회사 상세 조회 성공 테스트 추가
- APPROVER 다른 회사 상세 조회 403 테스트 추가
- REVIEWER 도메인 내 상세 조회 성공 테스트 추가
- 도메인 권한 없는 사용자 403 테스트 추가

### 재발 방지
- 상세 조회 API 권한 검증 시 모든 역할(DRAFTER/APPROVER/REVIEWER) + 무권한 시나리오 테스트 필수
- 역할별 접근 범위: DRAFTER=본인, APPROVER=같은 회사, REVIEWER=도메인 전체

### 검증 방법
```bash
./gradlew test --tests "DiagnosticServiceTest"
```

### 관련 커밋
- (이 PR에 포함)

### 생성/수정 파일
```
src/test/java/.../diagnostic/service/DiagnosticServiceTest.java (테스트 4건 추가)
docs/claude/LEARNINGS.md (엔트리 추가)
```

---

## 2026-02-01: 레거시 DRAFTER 리스트 조회 범위 오류 (#86)

### 원인
- 레거시 `getDiagnosticListLegacy()`에서 DRAFTER도 `findByCompanyAndFilters`로 회사 전체 건을 조회
- 이슈 #86 AC: 기안자는 본인 작성 건만 조회해야 함

### 해결
- `findByDrafterIdAndFilters` 레포지토리 메서드 추가 (drafterId 기반 필터)
- 레거시 로직에서 DRAFTER/APPROVER 분기: DRAFTER → drafterId 기반, APPROVER → company 기반

### 재발 방지
- 역할별 조회 범위 원칙: DRAFTER=본인, APPROVER=회사, REVIEWER=전체
- 새 필터 쿼리 추가 시 역할 분기를 반드시 점검

### 검증 방법
```bash
./gradlew test --tests "DiagnosticServiceTest"
```

### 관련 커밋
- (이슈 #86)

### 생성/수정 파일
```
DiagnosticRepository.java (findByDrafterIdAndFilters 추가)
DiagnosticService.java (레거시 DRAFTER 분기)
DiagnosticServiceTest.java (도메인 역할별 테스트 5건 추가)
```

---

## 2026-02-01: GUEST RBAC 차단 (#84)

### 원인
- SecurityConfig에서 `.anyRequest().permitAll()` 설정으로 모든 요청 허용
- JWT 토큰이 있어도 Spring Security 컨텍스트에 인증 정보가 설정되지 않음
- GUEST 사용자가 보호된 리소스(diagnostics, approvals 등)에 접근 가능

### 해결
- `JwtAuthenticationFilter` 신규 생성: JWT에서 role 추출 → `ROLE_` prefix로 SimpleGrantedAuthority 설정
- SecurityConfig 전면 개편: 보호 대상 경로에 `hasAnyRole("DRAFTER", "APPROVER", "REVIEWER")` 적용
- 공개 경로(auth, roles, swagger, health)는 permitAll 유지
- 401/403 시 표준 ErrorResponse JSON 반환 (AuthenticationEntryPoint, AccessDeniedHandler)

### 재발방지
- 새 컨트롤러 추가 시 SecurityConfig의 보호 경로 목록에 반드시 추가
- DomainAuthorizationTest 통합 테스트로 역할별 접근 제어 검증

### 검증방법
- `./gradlew test --tests "DomainAuthorizationTest"` (GUEST 차단 + 역할 불일치 테스트 통과)

### 관련커밋
- feature/84-guest-rbac-blocking 브랜치

---

## 2026-02-01: 알림 조회/읽음처리 API 구현 (#90)

### 원인
- Issue #90 요구사항: 미읽음 개수 전용 API, 개별 읽음 처리 RESTful API, 전체 읽음 처리 명시적 API
- 기존 `PATCH /read` API는 Request Body로 처리하여 RESTful 패턴 미준수
- 헤더 배지용 미읽음 개수만 빠르게 조회하는 경량 API 부재

### 해결
- `GET /api/v1/notifications/unread-count` - 미읽음 개수만 반환하는 경량 API 추가
- `PATCH /api/v1/notifications/{id}/read` - 개별 알림 읽음 처리 RESTful API 추가
- `PATCH /api/v1/notifications/read-all` - 전체 읽음 처리 명시적 API 추가
- `UnreadCountResponse` DTO 추가
- `NOTIFICATION_NOT_FOUND` (N001) 에러코드 추가

### 재발 방지
- RESTful API 설계 시 개별 리소스 조작은 `/{id}/{action}` 경로 패턴 사용
- 본인 리소스 검증 로직 필수 (`user.userId.equals(requestUserId)`)
- 존재하지 않는 알림 접근 시 명확한 404 에러 반환

### 검증 방법
```bash
./gradlew test --tests "NotificationServiceTest"
```

### 관련 커밋
- 0dc1f34 (PR #99)

### 생성/수정 파일
```
src/main/java/.../notification/controller/NotificationController.java (3개 엔드포인트 추가)
src/main/java/.../notification/service/NotificationService.java (3개 서비스 메서드 추가)
src/main/java/.../dto/notification/UnreadCountResponse.java (신규)
src/main/java/.../global/error/ErrorCode.java (NOTIFICATION_NOT_FOUND 추가)
src/test/java/.../notification/service/NotificationServiceTest.java (10개 테스트 추가)
```

---

## 2026-02-01: 도메인-역할 멤버십 조회 API (#81)

### 원인
- 로그인 사용자의 도메인별 역할을 일관되게 조회하는 전용 API가 없었음
- /me 응답에 domainRoles는 포함되지만, GUEST 사용자의 권한요청 상태 정보가 없었음

### 해결
- `GET /api/v1/auth/me/domains` 엔드포인트 추가
- GUEST: 빈 domainRoles + roleRequestStatus(PENDING/NONE) 반환
- 일반 사용자: domainRoles 목록 반환, roleRequestStatus는 null

### 재발방지
- 기존 buildDomainRoleDtos() 헬퍼를 재사용하여 중복 방지
- GUEST 분기 처리 시 existsByUserAndStatus로 간결하게 확인

### 검증방법
- `./gradlew test --tests "AuthServiceTest"` (3개 테스트: 일반사용자/게스트PENDING/게스트NONE)

### 관련커밋
- feature/81-domain-membership-api 브랜치

---

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

---

## 2026-02-04: 심사 상세 조회 시 AI 분석 결과 슬롯별 상세 누락 (#153)

### 원인
- `ReviewService.getReviewDetail()`에서 `aiAnalysis(null)`로 설정하여 AI 분석 결과 미포함
- 프론트엔드 제출 결과 페이지에서 위험도만 표시되고 항목별 상세 분석(slotResults, clarifications) 누락
- 별도 `/api/v1/ai/run/diagnostics/{id}/result/detail` API를 호출하지 않는 한 상세 정보 조회 불가

### 해결
- `ReviewService`에 `AiAnalysisResultRepository` 의존성 추가
- `getReviewDetail()` 메서드에서 진단 ID로 최신 AI 분석 결과 조회
- `fetchAiAnalysisResult()` 헬퍼 메서드로 `AiAnalysisResultDetailResponse.from()` 호출
- `DiagnosticDetailInfoDto.aiAnalysis` 필드에 구조화된 슬롯별 상세 정보 설정
- 테스트 2건 추가: AI 분석 결과 있는 경우/없는 경우

### 재발 방지
- 상세 조회 API에서 연관 데이터(AI 분석 결과 등) 포함 여부 확인
- `Object` 타입 필드는 실제로 채워지는지 서비스 레이어에서 검증
- API 응답에 null 필드가 많으면 실제 데이터 조회 로직 구현 여부 점검

### 검증방법
```bash
./gradlew test --tests "ReviewServiceTest"
```

### 관련커밋
- PR #166

### 생성/수정 파일
```
src/main/java/.../domain/review/service/ReviewService.java (AiAnalysisResultRepository 추가, fetchAiAnalysisResult 메서드)
src/test/java/.../domain/review/service/ReviewServiceTest.java (AI 분석 결과 테스트 2건)
```

---

## 2026-02-04: 결재자 제출 후 수신자에게 서류 안 올라감 (#158)

### 원인
- `ApprovalService.submitToReviewer()` 메서드에서 Diagnostic 상태만 REVIEWING으로 변경
- Review 엔티티를 생성하지 않아 수신자 대시보드/심사 목록에서 조회 불가
- 주석에 "Review entity not yet created - will be created by Review API"라고 되어 있었으나 실제로 Review API에서 생성하는 로직 없음

### 해결
- `ApprovalService`에 `ReviewRepository` 의존성 추가
- `submitToReviewer()` 메서드에서 Review 엔티티 생성 로직 추가:
  - Diagnostic, Company, Domain, Score(overallScore), submittedAt 설정
  - ReviewRepository.save() 호출
- 응답의 reviewId에 생성된 Review ID 반환 (기존 null → 실제 ID)
- 테스트 2건 추가: Review 생성 검증, Review 데이터 검증

### 재발 방지
- 엔티티 생성이 필요한 워크플로우 전이 시 실제 생성 로직 구현 확인
- "will be created later" 주석은 실제 구현 여부를 별도 검증 필요
- 연관 엔티티 생성 누락 시 조회 API에서 결과 없음으로 나타남

### 검증방법
```bash
./gradlew test --tests "ApprovalServiceTest"
```

### 관련커밋
- PR #158 (feature/158-review-creation-on-submit)

### 생성/수정 파일
```
src/main/java/.../domain/approval/service/ApprovalService.java (ReviewRepository 추가, Review 생성 로직)
src/test/java/.../domain/approval/service/ApprovalServiceTest.java (+2 테스트 케이스)
```

---

## 2026-02-06: DiagnosticHistory comment 필드 길이 초과 오류

### 원인
- `DiagnosticHistory.comment` 필드에 `@Column` 어노테이션 없음
- JPA 기본값으로 varchar(255) 컬럼 생성
- 결재/심사 반려 시 파일명 목록 등 긴 코멘트 입력 시 255자 초과하여 DataException 발생
- 에러: `value too long for type character varying(255)`

### 해결
- `DiagnosticHistory.java`의 `comment` 필드에 `@Column(length = 2000)` 추가
- varchar(2000)으로 충분한 코멘트 길이 지원

### 재발 방지
- 사용자 입력 텍스트 필드는 기본 255자 제한 검토 필수
- 코멘트, 설명, 메모 등 자유 입력 필드는 @Column(length = N) 명시 권장
- 파일명, 에러 메시지 등 동적 내용이 포함될 수 있는 필드는 충분한 길이 확보

### 검증방법
```bash
./gradlew build
```

### 관련커밋
- feature/diagnostic-history-comment-length

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/domain/diagnostic/entity/DiagnosticHistory.java (@Column(length = 2000) 추가)
```
