# E2E 테스트 데이터 가이드

> 이 문서는 프론트엔드 E2E 테스트를 위한 초기 데이터를 설명합니다.
> 모든 계정의 비밀번호: `Test1234!`

## 빠른 참조

### 테스트 계정 요약

| 역할 | 이메일 | 회사 | 담당 도메인 |
|------|--------|------|-------------|
| 심사자(ESG) | `reviewer.esg@hdhhi.co.kr` | HD현대중공업 | ESG |
| 심사자(안전) | `reviewer.safety@hdhhi.co.kr` | HD현대중공업 | SAFETY |
| 심사자(컴플) | `reviewer.compliance@hdhhi.co.kr` | HD현대중공업 | COMPLIANCE |
| 결재자 | `approver@techpartner.co.kr` | 테크파트너 | ESG, COMPLIANCE |
| 결재자 | `approver@greenmanu.co.kr` | 그린매뉴팩처링 | ESG, SAFETY |
| 결재자 | `approver@safebuild.co.kr` | 안전건설 | SAFETY |
| 기안자 | `drafter1@techpartner.co.kr` | 테크파트너 | ESG, COMPLIANCE |
| 기안자 | `drafter2@techpartner.co.kr` | 테크파트너 | ESG |
| 기안자 | `drafter@greenmanu.co.kr` | 그린매뉴팩처링 | ESG, SAFETY |
| 기안자 | `drafter@safebuild.co.kr` | 안전건설 | SAFETY |
| 게스트 | `newbie1@precision.co.kr` | 정밀부품 | - (권한 없음) |
| 게스트 | `newbie2@precision.co.kr` | 정밀부품 | - (권한 없음) |

---

## 1. 회사 데이터 (5개)

| 회사명 | 타입 | 규모 | 업종 |
|--------|------|------|------|
| HD현대중공업 | TIER1 (원청) | 대기업 | 제조업 |
| (주)테크파트너 | TIER2 | 중견기업 | IT/소프트웨어 |
| (주)그린매뉴팩처링 | TIER2 | 중소기업 | 제조업 |
| (주)안전건설 | TIER2 | 중견기업 | 건설업 |
| (주)정밀부품 | TIER2 | 중소기업 | 제조업 |

---

## 2. 캠페인 데이터 (6개)

| 캠페인 코드 | 도메인 | 제목 | 기간 | 마감일 |
|-------------|--------|------|------|--------|
| CAMP-ESG-2026-001 | ESG | 2026년 상반기 ESG 공급망 진단 | 2026.01~06 | 2026-03-31 |
| CAMP-ESG-2025-001 | ESG | 2025년 ESG 공급망 진단 (완료) | 2025.01~12 | 2025-03-31 |
| CAMP-SAFETY-2026-001 | SAFETY | 2026년 1분기 안전보건 점검 | 2026.01~03 | 2026-02-28 |
| CAMP-SAFETY-2025-002 | SAFETY | 2025년 하반기 안전보건 점검 (완료) | 2025.07~12 | 2025-11-30 |
| CAMP-COMPL-2026-001 | COMPLIANCE | 2026년 하도급 컴플라이언스 점검 | 2026.02~04 | 2026-04-15 |
| CAMP-COMPL-2025-001 | COMPLIANCE | 2025년 하도급 컴플라이언스 점검 (완료) | 2025.03~05 | 2025-05-15 |

---

## 3. 진단 데이터 - 상태별 (17개)

### ESG 도메인 (12건)

| 상태 | 진단코드 | 회사 | 기안자 | 테스트 시나리오 |
|------|----------|------|--------|-----------------|
| **WRITING** | DG-ESG-2026-001 | 테크파트너 | drafter1@techpartner.co.kr | 작성중 진단 편집 |
| **WRITING** | DG-ESG-2026-002 | 그린매뉴팩처링 | drafter@greenmanu.co.kr | 작성중 진단 편집 |
| **SUBMITTED** | DG-ESG-2026-003 | 테크파트너 | drafter2@techpartner.co.kr | 결재 대기 목록 확인 |
| **SUBMITTED** | DG-ESG-2026-004 | 정밀부품 | newbie1@precision.co.kr | 게스트가 제출한 건 |
| **RETURNED** | DG-ESG-2026-005 | 그린매뉴팩처링 | drafter@greenmanu.co.kr | 반려 후 재작성 |
| **APPROVED** | DG-ESG-2026-006 | 테크파트너 | drafter1@techpartner.co.kr | 심사 대기 |
| **APPROVED** | DG-ESG-2026-007 | 안전건설 | drafter@safebuild.co.kr | 심사 대기 |
| **REVIEWING** | DG-ESG-2025-001 | 테크파트너 | drafter1@techpartner.co.kr | 심사중 |
| **REVIEWING** | DG-ESG-2025-002 | 그린매뉴팩처링 | drafter@greenmanu.co.kr | 심사중 |
| **COMPLETED** | DG-ESG-2025-003 | 안전건설 | drafter@safebuild.co.kr | 완료된 진단 조회 |
| **COMPLETED** | DG-ESG-2025-004 | 정밀부품 | newbie1@precision.co.kr | 완료된 진단 조회 |

### SAFETY 도메인 (3건)

| 상태 | 진단코드 | 회사 | 기안자 |
|------|----------|------|--------|
| **WRITING** | DG-SAFETY-2026-001 | 그린매뉴팩처링 | drafter@greenmanu.co.kr |
| **SUBMITTED** | DG-SAFETY-2026-002 | 안전건설 | drafter@safebuild.co.kr |
| **COMPLETED** | DG-SAFETY-2025-001 | 안전건설 | drafter@safebuild.co.kr |

### COMPLIANCE 도메인 (2건)

| 상태 | 진단코드 | 회사 | 기안자 |
|------|----------|------|--------|
| **WRITING** | DG-COMPL-2026-001 | 테크파트너 | drafter1@techpartner.co.kr |
| **COMPLETED** | DG-COMPL-2025-001 | 테크파트너 | drafter1@techpartner.co.kr |

---

## 4. 결재 데이터 - 상태별 (5건)

| 상태 | 진단 | 요청자 | 결재자 | 코멘트 |
|------|------|--------|--------|--------|
| **WAITING** | DG-ESG-2026-003 | drafter2@techpartner.co.kr | - | 결재 대기 테스트 |
| **WAITING** | DG-SAFETY-2026-002 | drafter@safebuild.co.kr | - | 결재 대기 테스트 |
| **APPROVED** | DG-ESG-2026-006 | drafter1@techpartner.co.kr | approver@techpartner.co.kr | "검토 완료, 승인합니다." |
| **APPROVED** | DG-ESG-2025-001 | drafter1@techpartner.co.kr | approver@techpartner.co.kr | "승인합니다." |
| **REJECTED** | DG-ESG-2026-005 | drafter@greenmanu.co.kr | approver@greenmanu.co.kr | "온실가스 배출량 데이터 누락" |

---

## 5. 심사 데이터 - 상태별 (6건)

| 상태 | 진단 | 회사 | 점수 | 위험도 | 심사자 |
|------|------|------|------|--------|--------|
| **REVIEWING** | DG-ESG-2025-001 | 테크파트너 | 68 | MEDIUM | reviewer.esg@hdhhi.co.kr |
| **REVIEWING** | DG-ESG-2025-002 | 그린매뉴팩처링 | 75 | MEDIUM | reviewer.esg@hdhhi.co.kr |
| **APPROVED** | DG-ESG-2025-003 | 안전건설 | 82 | LOW | reviewer.esg@hdhhi.co.kr |
| **APPROVED** | DG-SAFETY-2025-001 | 안전건설 | 91 | LOW | reviewer.safety@hdhhi.co.kr |
| **REVISION_REQUIRED** | DG-ESG-2025-004 | 정밀부품 | 45 | HIGH | reviewer.esg@hdhhi.co.kr |
| **REVISION_REQUIRED** | DG-COMPL-2025-001 | 테크파트너 | 58 | MEDIUM | reviewer.compliance@hdhhi.co.kr |

### 위험도 기준
- **LOW**: 70점 이상
- **MEDIUM**: 40~69점
- **HIGH**: 40점 미만

---

## 6. 역할 요청 데이터 - 상태별 (4건)

| 상태 | 요청자 | 도메인 | 요청 역할 | 사유 |
|------|--------|--------|----------|------|
| **PENDING** | newbie1@precision.co.kr | ESG | DRAFTER | ESG 진단 업무 담당자 지정 |
| **PENDING** | newbie2@precision.co.kr | SAFETY | DRAFTER | 안전보건 점검 업무 담당 |
| **APPROVED** | drafter1@techpartner.co.kr | COMPLIANCE | DRAFTER | 컴플라이언스 업무 추가 담당 |
| **REJECTED** | newbie2@precision.co.kr | ESG | APPROVER | 결재자 권한 요청 (거절됨) |

---

## 7. 비동기 작업 데이터 (5건)

| 상태 | 작업 타입 | 요청자 | 대상 |
|------|----------|--------|------|
| **PENDING** | REPORT_GENERATION | reviewer.esg@hdhhi.co.kr | Review |
| **RUNNING** | AI_ESG_ANALYSIS | drafter@greenmanu.co.kr | Diagnostic |
| **SUCCEEDED** | FILE_PARSING | drafter1@techpartner.co.kr | Diagnostic |
| **SUCCEEDED** | BULK_REPORT | reviewer.esg@hdhhi.co.kr | Campaign |
| **FAILED** | FILE_PARSING | drafter@safebuild.co.kr | Diagnostic |

---

## 8. 알림 데이터 (10건)

| 타입 | 수신자 | 제목 |
|------|--------|------|
| APPROVAL_COMPLETE | approver@techpartner.co.kr | 결재 요청 |
| APPROVAL_COMPLETE | drafter2@techpartner.co.kr | 결재 완료 |
| REVISION_REQUIRED | drafter@greenmanu.co.kr | 결재 반려 |
| REVIEW_COMPLETE | reviewer.esg@hdhhi.co.kr | 심사 요청 |
| REVIEW_COMPLETE | drafter1@techpartner.co.kr | 심사 완료 |
| ROLE_APPROVED | newbie1@precision.co.kr | 권한 요청 접수 |
| ROLE_APPROVED | drafter1@techpartner.co.kr | 권한 승인 |
| REVISION_REQUIRED | newbie2@precision.co.kr | 권한 거절 |
| AI_ANALYSIS_COMPLETE | drafter1@techpartner.co.kr | AI 분석 완료 |
| AI_ANALYSIS_FAILED | drafter@safebuild.co.kr | AI 분석 실패 |

---

## 9. 테스트 시나리오별 계정 추천

### 기안자 플로우 테스트
```
계정: drafter1@techpartner.co.kr
- 작성중 진단이 있음 (DG-ESG-2026-001)
- 승인된 진단 있음 (DG-ESG-2026-006)
- 심사중/완료 진단 있음
- ESG, COMPLIANCE 도메인 권한
```

### 결재자 플로우 테스트
```
계정: approver@techpartner.co.kr
- 결재 대기 건 있음 (DG-ESG-2026-003)
- 결재 완료 건 있음
- ESG, COMPLIANCE 도메인 권한
```

### 심사자 플로우 테스트
```
계정: reviewer.esg@hdhhi.co.kr
- 심사중 건 있음 (DG-ESG-2025-001, DG-ESG-2025-002)
- 심사 완료/보완요청 건 있음
- ESG 도메인 전담
```

### 게스트(신규 가입) 플로우 테스트
```
계정: newbie1@precision.co.kr
- GUEST 역할 (권한 없음)
- 역할 요청 PENDING 상태
- 권한 요청 → 승인/거절 플로우 테스트
```

### 반려/보완 플로우 테스트
```
계정: drafter@greenmanu.co.kr
- 반려된 진단 있음 (DG-ESG-2026-005)
- 재작성 → 재제출 플로우 테스트
```

---

## 10. API 테스트 예시

### 로그인
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "drafter1@techpartner.co.kr", "password": "Test1234!"}'
```

### 진단 목록 조회 (ESG)
```bash
curl -X GET "http://localhost:8080/api/v1/diagnostics?domainCode=ESG" \
  -H "Authorization: Bearer {TOKEN}"
```

### 결재 대기 목록 조회
```bash
curl -X GET "http://localhost:8080/api/v1/approvals?status=WAITING" \
  -H "Authorization: Bearer {TOKEN}"
```

### 심사 목록 조회
```bash
curl -X GET "http://localhost:8080/api/v1/reviews?status=REVIEWING" \
  -H "Authorization: Bearer {TOKEN}"
```

---

## 11. 데이터 초기화

로컬 환경에서 데이터를 초기화하려면:

```bash
# Docker PostgreSQL 컨테이너 재시작
docker restart smartchain-db

# 앱 재시작 (ddl-auto: create이므로 자동 초기화)
./gradlew bootRun
```

> **참고**: `local` 프로필은 `ddl-auto: create` 설정이므로 앱 재시작 시 데이터가 초기화됩니다.
