# Claude Code Learnings

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
