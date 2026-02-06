# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project Guardrails (Persistent)

## Workflow (Must)
- 작업 시작 시 반드시 /rename 으로 세션 이름 지정 (이슈/기능 단위)
- 변경 전: 계획 5줄 요약 → 영향 범위 → 테스트 방법을 먼저 제시
- 변경 후: 반드시 tests/build 실행 결과를 보고

## Learning Capture (Must)
- 문제를 해결했으면 docs/claude/LEARNINGS.md에 아래 템플릿으로 1개 엔트리 추가
- 엔트리는 "원인/해결/재발방지/검증방법/관련커밋"을 포함
- 비밀키/토큰/내부URL 등 민감정보는 절대 기록하지 말 것


## Project Overview

AI 기반 협력사 리스크 관리 플랫폼 - HD현대중공업 ESG 관리팀, 안전보건팀, 구매팀을 위한 AI 기반 공급망 관리 시스템.

### 3개 서비스 도메인

| 도메인 | 코드 | 핵심 기능 |
|--------|------|----------|
| ESG 실사 | `ESG` | ESG 증빙 자동 파싱 및 AI 리포트 생성 |
| 안전보건 | `SAFETY` | TBM 영상 AI 분석 및 안전점검 검증 |
| 컴플라이언스 | `COMPLIANCE` | 하도급 계약서 LLM 자동 검토 |

### 4가지 역할

| 역할 | 설명 |
|------|------|
| GUEST | 회원가입 직후, 권한 요청만 가능 |
| DRAFTER (기안자) | 협력사 작성 직원 - 진단 데이터 작성/업로드 |
| APPROVER (결재자) | 협력사 팀장 - 회사 정보 관리, 결재 처리 |
| REVIEWER (수신자) | 원청 담당자 - 심사, 보고서 발행, 권한 관리 |

- **Stack**: Java 17, Spring Boot 3.5, Spring Security 6, Spring Data JPA, PostgreSQL 16
- **Build**: Gradle 8
- **Package**: `com.smartchain.platform`

## Build & Run Commands

```bash
# Build
./gradlew build

# Run locally (uses local profile by default)
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'

# Run tests
./gradlew test

# Run single test class
./gradlew test --tests "DiagnosticServiceTest"

# Clean build
./gradlew clean build
```

## Architecture

### Package Structure

```
src/main/java/com/smartchain/platform/
├── global/           # Cross-cutting concerns
│   ├── config/       # SecurityConfig, SwaggerConfig, JpaConfig
│   ├── entity/       # BaseTimeEntity (auditing base class)
│   ├── enums/        # DiagnosticStatus, RequestStatus, ApprovalStatus, etc.
│   ├── error/        # ErrorCode enum, CustomException, GlobalExceptionHandler
│   └── response/     # ErrorResponse
├── domain/           # Business domain modules
│   ├── diagnostic/   # ESG diagnostic assessments (core domain)
│   │   └── entity/   # Diagnostic, Campaign, Question, ResultQual, ResultQuant, Report
│   ├── approval/     # Approval workflow
│   │   └── entity/   # Approval
│   ├── review/       # Review workflow
│   │   └── entity/   # Review
│   ├── user/         # Users and companies
│   │   └── entity/   # User, Company, Role, Domain, UserDomainRole, Industry, RoleRequest
│   ├── evidence/     # Evidence files for diagnostics
│   ├── log/          # Activity logging
│   └── ...
├── dto/              # DTOs organized by domain
│   ├── diagnostic/   # DiagnosticListRequest, QualAssessmentSaveRequest, etc.
│   ├── approval/     # ApprovalListRequest, ApprovalDecisionRequest, etc.
│   ├── common/       # PagedResponse, PageInfo, shared DTOs
│   └── campaign/
└── HealthController.java
```

### Domain-based Authorization System

사용자는 각 서비스 도메인(ESG, SAFETY, COMPLIANCE)별로 서로 다른 역할을 가질 수 있습니다.

**관련 엔티티:**
- `Domain`: 서비스 도메인 정의 (code: ESG, SAFETY, COMPLIANCE)
- `UserDomainRole`: User-Domain-Role 매핑 (다대다 관계)
- `User.domainRoles`: 사용자의 도메인별 역할 목록

**권한 검증 패턴:**
```java
// 도메인 기반 권한 검증
user.hasRoleInDomain("ESG", "APPROVER");  // ESG 도메인에서 APPROVER 역할 확인
user.getDomainsWithRole("DRAFTER");       // DRAFTER 역할을 가진 도메인 목록 조회
```

### Key Patterns

1. **Entity Inheritance**: All entities extend `BaseTimeEntity` for `createdAt`/`updatedAt` auditing
2. **Error Handling**: Throw `CustomException` with `ErrorCode` enum - handled by `GlobalExceptionHandler`
3. **Pagination**: Use `PagedResponse<T>` wrapper with `PageInfo` for paginated endpoints
4. **DTO Naming**: `[Domain][Action][Request/Response]` (e.g., `DiagnosticCreateRequest`, `ApprovalDetailResponse`)

### Core Domain: Diagnostic Workflow

The diagnostic flow follows: Campaign → Diagnostic → Approval → Review
- `DiagnosticStatus`: WRITING → SUBMITTED → RETURNED/APPROVED → REVIEWING → COMPLETED
- `ApprovalStatus`: WAITING → APPROVED/REJECTED
- Key roles: Drafter (기안자), Approver (결재자), Reviewer (수신자)
- Each Diagnostic belongs to a Campaign, Company, and Domain

### Workflow by Domain

| 도메인 | 워크플로우 |
|--------|----------|
| ESG | 증빙 업로드 → AI 파싱 → 진단 작성 → 결재 → 심사 → 리포트 |
| SAFETY | TBM 영상 업로드 → AI 분석 → 검증 → 결재 → 심사 |
| COMPLIANCE | 계약서 업로드 → LLM 검토 → 위험 조항 확인 → 결재 → 심사 |

## Conventions

### Commit Messages
```
feat(diagnostic): 정성적 평가 저장 API 구현
fix(auth): JWT 토큰 만료 시간 오류 수정
refactor: User 엔티티 구조 변경
```

### Branch Strategy
- `main`: production
- `develop`: development integration
- `feature/*`: new features
- `hotfix/*`: urgent fixes

### API Endpoints
Base paths follow `/api/v1/[domain]` pattern:
- `/api/v1/auth` - Authentication
- `/api/v1/diagnostics` - ESG diagnostics (도메인 필터링 지원: `?domainCode=ESG`)
- `/api/v1/approvals` - Approval workflow (도메인 기반 권한 검증)
- `/api/v1/reviews` - Review workflow (도메인 기반 권한 검증)
- `/api/v1/files` - File upload/download
- `/api/v1/ai/run/diagnostics/{id}/*` - AI Run API (공통 - 모든 도메인 지원)
- `/api/v1/ai/esg/*` - ESG AI services (파싱, 리포트)
- `/api/v1/ai/safety/*` - Safety AI services (TBM 분석)
- `/api/v1/ai/compliance/*` - Compliance AI services (계약서 검토)

## Configuration

Profiles defined in `application.yaml`:
- `local`: Local development with local PostgreSQL
- `dev`: Azure development environment (uses env vars: DB_HOST, DB_NAME, DB_USER, DB_PASSWORD)
- `test`: H2 in-memory database for testing

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

## Testing Notes

### Test Profile Configuration
테스트 시 `application-test.yaml` 사용:
- H2 인메모리 데이터베이스 (PostgreSQL 호환 모드)
- `spring.sql.init.mode: never` - data.sql 비활성화 (H2 SQL 호환성 문제 방지)
- `spring.jpa.hibernate.ddl-auto: create-drop`

### Mock 설정 주의사항
- 도메인 기반 권한 검증 시 `user.getDomainsWithRole()` mock 필수
- 레거시 데이터 (domain이 null인 경우) 테스트 케이스 포함 권장
- `lenient().when()` 사용하여 UnnecessaryStubbingException 방지

## Documentation

상세 문서는 `./docs/` 디렉토리 참조:
- [API Contract SSOT](./docs/API_CONTRACT_SSOT.md) - API 계약 정의
- [API Quick Reference](./docs/API_QUICK_REFERENCE.md) - API 빠른 참조
- [Frontend Integration Rules](./docs/FE_INTEGRATION_RULES.md) - 프론트엔드 연동 가이드
- [Backend Implementation Guide](./docs/BACKEND_IMPLEMENTATION_GUIDE.md) - 백엔드 구현 가이드
- [Status and Error Codes](./docs/STATUS_AND_ERROR_CODES.md) - 상태/에러 코드 참조
- [AI Integration Guide](./docs/AI_INTEGRATION_GUIDE.md) - AI Run API 연동 가이드
- [AI Risk API](./docs/AI_RISK_API.md) - 외부 리스크 감지 API 프론트엔드 연동 가이드

## Implementation Notes

### 도메인 기반 권한 검증 구현 패턴
```java
private void validateDomainAccess(User user, Diagnostic diagnostic, String requiredRole) {
    Domain domain = diagnostic.getDomain();

    if (domain != null) {
        // 도메인 기반 권한 검증
        if (!user.hasRoleInDomain(domain.getCode(), requiredRole)) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    } else {
        // 레거시: 전역 역할로 검증
        if (!requiredRole.equals(user.getRole().getCode())) {
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }
    }
}
```

### Repository 도메인 기반 쿼리 패턴
```java
@Query("SELECT d FROM Diagnostic d WHERE d.domain IN :domains AND d.company = :company")
Page<Diagnostic> findByDomainsAndCompanyOrderByCreatedAtDesc(
    @Param("domains") List<Domain> domains,
    @Param("company") Company company,
    Pageable pageable);
```

### AI Run API 구현 (공통)

기획서 기반 공통 AI Run API - 모든 도메인(ESG, SAFETY, COMPLIANCE)에서 동일한 인터페이스 사용

**패키지 구조:**
```
src/main/java/com/smartchain/platform/
├── domain/ai/
│   ├── client/AiRunApiClient.java       # WebClient 기반 AI API 호출
│   ├── config/AiRunApiConfig.java       # AI API 설정
│   ├── controller/AiAnalysisController.java
│   ├── entity/AiAnalysisResult.java     # 분석 결과 엔티티
│   ├── repository/AiAnalysisResultRepository.java
│   └── service/AiAnalysisService.java   # 비동기 분석 처리
├── dto/ai/
│   ├── AiAnalysisRequest.java
│   ├── AiPreviewRequest.java
│   ├── AiAnalysisResultResponse.java
│   └── run/                             # AI Run API DTO
│       ├── FileInfo.java
│       ├── SlotHint.java
│       ├── SlotStatus.java
│       ├── SlotResult.java
│       ├── Clarification.java
│       ├── RunPreviewRequest.java
│       ├── RunPreviewResponse.java
│       ├── RunSubmitRequest.java
│       └── RunSubmitResponse.java
```

**API 엔드포인트:**
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/ai/run/diagnostics/{id}/preview` | 슬롯 추정 |
| POST | `/api/v1/ai/run/diagnostics/{id}/submit` | 전체 검증 요청 (비동기) |
| GET | `/api/v1/ai/run/diagnostics/{id}/result` | 최신 결과 조회 |
| GET | `/api/v1/ai/run/diagnostics/{id}/history` | 분석 이력 조회 |

**응답 스키마 (고정):**
```java
// RunSubmitResponse - 고정 스키마
record RunSubmitResponse(
    String packageId,
    String verdict,      // PASS | FAIL | PENDING
    String riskLevel,    // LOW | MEDIUM | HIGH | CRITICAL
    String why,
    List<SlotResult> slotResults,
    List<Clarification> clarifications,
    Map<String, Object> extras
) {}
```
