# SmartChain API 명세서

> 버전: 2.2
> 최종 수정: 2026-01-29
> Base URL: `http://localhost:8080`
> 인증: Bearer Token (JWT)

---

## 목차
1. [인증 (Auth)](#1-인증-auth)
2. [진단/기안 (Diagnostic)](#2-진단기안-diagnostic)
3. [결재 (Approval)](#3-결재-approval)
4. [심사 (Review)](#4-심사-review)
5. [권한 (Role)](#5-권한-role)
6. [파일 (File)](#6-파일-file)
7. [비동기 작업 (Job)](#7-비동기-작업-job)
8. [알림 (Notification)](#8-알림-notification)
9. [관리 (Management)](#9-관리-management)
10. [AI 서비스](#10-ai-서비스)

---

## 공통 사항

### 인증 헤더
```
Authorization: Bearer {accessToken}
```

### 응답 형식
```json
{
  "success": true,
  "message": "성공 메시지",
  "data": { ... }
}
```

### 에러 응답
```json
{
  "success": false,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

### 권한 체계
| 역할 | 설명 |
|------|------|
| GUEST | 회원가입 직후 기본 역할 |
| DRAFTER | 기안자 - 진단 작성/제출 |
| APPROVER | 결재자 - 진단 결재 |
| REVIEWER | 수신자/원청 - 최종 심사 |

### 서비스 도메인

플랫폼은 3개의 서비스 도메인으로 구성됩니다:

| 도메인 코드 | 이름 | 설명 |
|------------|------|------|
| `ESG` | ESG 실사 | ESG 증빙 자동 파싱 및 AI 리포트 생성 |
| `SAFETY` | 안전보건 | AI 기반 현장 안전점검(TBM) 자동 검증 |
| `COMPLIANCE` | 컴플라이언스 | LLM 기반 하도급 계약서 자동 검토 |

### 도메인별 역할

사용자는 각 도메인별로 서로 다른 역할을 가질 수 있습니다:

```json
{
  "domainRoles": [
    { "domainCode": "ESG", "roleCode": "APPROVER" },
    { "domainCode": "SAFETY", "roleCode": "DRAFTER" }
  ]
}
```

### 도메인 기반 권한 검증

API 요청 시 다음 규칙에 따라 도메인 권한이 검증됩니다:

| API | 필요 역할 | 설명 |
|-----|----------|------|
| `/api/v1/diagnostics` | DRAFTER/APPROVER | 진단이 속한 도메인에서 역할 필요 |
| `/api/v1/approvals` | APPROVER | 진단이 속한 도메인에서 APPROVER 필요 |
| `/api/v1/reviews` | REVIEWER | 진단이 속한 도메인에서 REVIEWER 필요 |
| `/api/v1/ai/run/*` | DRAFTER/APPROVER/REVIEWER | 진단 도메인에서 하나 이상 역할 필요 |

**권한 없음 에러:**
```json
{
  "success": false,
  "message": "해당 도메인에 대한 접근 권한이 없습니다",
  "code": "DOM002"
}
```

---

## 1. 인증 (Auth)

### 1.1 회원가입
```
POST /api/v1/auth/register
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Test1234!",
  "name": "홍길동",
  "termsAgreed": true
}
```

**Response:** `201 Created`
```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "GUEST"
}
```

---

### 1.2 로그인
```
POST /api/v1/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Test1234!"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 1.3 이메일 중복 확인
```
POST /api/v1/auth/check-email
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

---

### 1.4 이메일 인증 코드 발송
```
POST /api/v1/auth/send-verification
```

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

---

### 1.5 이메일 인증 확인
```
POST /api/v1/auth/verify-email
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

---

### 1.6 토큰 갱신
```
POST /api/v1/auth/refresh
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

---

### 1.7 로그아웃
```
POST /api/v1/auth/logout
```

---

### 1.8 내 정보 조회
```
GET /api/v1/auth/me
```

**Response:**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": {
    "code": "DRAFTER",
    "name": "기안자"
  },
  "domainRoles": [
    { "domainCode": "ESG", "domainName": "ESG 실사", "roleCode": "APPROVER", "roleName": "결재자" },
    { "domainCode": "SAFETY", "domainName": "안전보건", "roleCode": "DRAFTER", "roleName": "기안자" }
  ],
  "company": {
    "companyId": 1,
    "name": "(주)테크파트너"
  }
}
```

---

## 2. 진단/기안 (Diagnostic)

> 권한: DRAFTER, APPROVER

### 2.1 기안 목록 조회
```
GET /api/v1/diagnostics
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| domainCode | string | N | 도메인 필터 (ESG, SAFETY, COMPLIANCE) |
| statuses | string | N | 상태 필터 (쉼표 구분, 예: WRITING,SUBMITTED) |
| deadlineFrom | date | N | 마감일 시작 (yyyy-MM-dd) |
| deadlineTo | date | N | 마감일 종료 |
| page | int | N | 페이지 번호 (기본: 0) |
| size | int | N | 페이지 크기 (기본: 10) |

**상태값:**
- `WRITING` - 작성중
- `SUBMITTED` - 제출됨
- `RETURNED` - 반려됨
- `APPROVED` - 내부승인
- `REVIEWING` - 심사중
- `COMPLETED` - 완료

---

### 2.2 기안 상세 조회
```
GET /api/v1/diagnostics/{diagnosticId}
```

---

### 2.3 기안 생성
```
POST /api/v1/diagnostics
```

**Request Body:**
```json
{
  "campaignId": 1,
  "domainCode": "ESG",
  "title": "2026년 ESG 진단",
  "periodStartDate": "2026-01-01",
  "periodEndDate": "2026-12-31"
}
```

> **Note**: `domainCode`는 필수 필드입니다. 유효한 값: `ESG`, `SAFETY`, `COMPLIANCE`

---

### 2.4 기안 제출
```
POST /api/v1/diagnostics/{diagnosticId}/submit
```

**Request Body:**
```json
{
  "approverId": 2,
  "comment": "검토 요청드립니다."
}
```

---

### 2.5 AI 분석 결과 조회
```
GET /api/v1/diagnostics/{diagnosticId}/ai-analysis
```

---

### 2.6 기안 이력 조회
```
GET /api/v1/diagnostics/{diagnosticId}/history
```

---

## 3. 결재 (Approval)

> 권한: APPROVER

### 3.1 결재 대기 목록 조회
```
GET /api/v1/approvals
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| domainCode | string | N | 도메인 필터 (ESG, SAFETY, COMPLIANCE) |
| status | string | N | WAITING, APPROVED, REJECTED |
| page | int | N | 페이지 번호 (기본: 0) |
| size | int | N | 페이지 크기 (기본: 10) |

> **Note**: 사용자는 자신이 APPROVER 역할을 가진 도메인의 결재만 조회할 수 있습니다.

---

### 3.2 결재 상세 조회
```
GET /api/v1/approvals/{approvalId}
```

---

### 3.3 결재 처리 (승인/반려)
```
PATCH /api/v1/approvals/{approvalId}
```

**Request Body:**
```json
{
  "decision": "APPROVED",
  "comment": "승인합니다."
}
```

또는

```json
{
  "decision": "REJECTED",
  "comment": "보완이 필요합니다.",
  "rejectReason": "증빙 파일 누락"
}
```

---

### 3.4 원청 제출
```
POST /api/v1/approvals/{approvalId}/submit-to-reviewer
```

---

## 4. 심사 (Review)

> 권한: REVIEWER

### 4.1 대시보드 조회
```
GET /api/v1/reviews/dashboard
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| campaignId | long | N | 캠페인 ID 필터 |
| fromDate | string | N | 기간 시작 (yyyy-MM-dd) |
| toDate | string | N | 기간 종료 |

---

### 4.2 심사 대상 목록 조회
```
GET /api/v1/reviews
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| domainCode | string | N | 도메인 필터 (ESG, SAFETY, COMPLIANCE) |
| status | string | N | REVIEWING, APPROVED, REVISION_REQUIRED |
| riskLevel | string | N | HIGH, MEDIUM, LOW |
| companyId | long | N | 협력사 ID 필터 |
| page | int | N | 페이지 번호 |
| size | int | N | 페이지 크기 |

> **Note**: 사용자는 자신이 REVIEWER 역할을 가진 도메인의 심사만 조회할 수 있습니다.

---

### 4.3 심사 상세 조회
```
GET /api/v1/reviews/{reviewId}
```

---

### 4.4 심사 결과 입력
```
PATCH /api/v1/reviews/{reviewId}
```

**Request Body:**
```json
{
  "decision": "APPROVED",
  "score": 85,
  "comment": "양호합니다.",
  "categoryCommentE": "환경 부문 우수",
  "categoryCommentS": "사회 부문 보통",
  "categoryCommentG": "지배구조 개선 필요"
}
```

---

### 4.5 보고서 생성 (비동기)
```
POST /api/v1/reviews/{reviewId}/report
```

**Response:** `202 Accepted`
```json
{
  "jobId": "job_report_abc123",
  "status": "PENDING"
}
```

---

### 4.6 일괄 보고서 생성
```
POST /api/v1/reviews/bulk-report
```

**Request Body:**
```json
{
  "reviewIds": [1, 2, 3]
}
```

---

### 4.7 CSV/Excel 내보내기
```
POST /api/v1/reviews/export
```

**Request Body:**
```json
{
  "format": "EXCEL",
  "reviewIds": [1, 2, 3]
}
```

---

## 5. 권한 (Role)

### 5.1 권한 요청 페이지 정보 조회
```
GET /api/v1/roles/request-page
```

**Response:**
```json
{
  "currentRole": { "code": "GUEST", "name": "게스트" },
  "currentDomainRoles": [],
  "availableRoles": [
    { "code": "DRAFTER", "name": "기안자" },
    { "code": "APPROVER", "name": "결재자" }
  ],
  "availableDomains": [
    { "code": "ESG", "name": "ESG 실사" },
    { "code": "SAFETY", "name": "안전보건" },
    { "code": "COMPLIANCE", "name": "컴플라이언스" }
  ],
  "availableCompanies": [...],
  "pendingRequest": null
}
```

---

### 5.2 권한 요청 생성
```
POST /api/v1/roles/requests
```

**Request Body:**
```json
{
  "requestedRole": "DRAFTER",
  "domainCode": "ESG",
  "companyId": 1,
  "reason": "ESG 진단 업무 수행을 위해 요청합니다."
}
```

> **Note**: `domainCode`는 권한을 요청할 서비스 도메인입니다. 유효한 값: `ESG`, `SAFETY`, `COMPLIANCE`

---

### 5.3 내 권한 요청 상태 조회
```
GET /api/v1/roles/requests/my
```

---

### 5.4 권한 요청 목록 조회 (관리자)
```
GET /api/v1/roles/requests
```

> 권한: APPROVER (자사만), REVIEWER (전체)

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| status | string | N | PENDING, APPROVED, REJECTED |
| companyId | long | N | 회사 ID 필터 |
| page | int | N | 페이지 번호 |
| size | int | N | 페이지 크기 |

---

### 5.5 권한 요청 상세 조회
```
GET /api/v1/roles/requests/{accessRequestId}
```

---

### 5.6 권한 요청 승인/반려
```
PATCH /api/v1/roles/requests/{accessRequestId}
```

**Request Body:**
```json
{
  "decision": "APPROVED"
}
```

---

## 6. 파일 (File)

### 6.1 파일 업로드
```
POST /api/v1/diagnostics/{diagnosticId}/files
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: 업로드할 파일

**Response:** `202 Accepted`
```json
{
  "fileId": 1,
  "fileName": "evidence.pdf",
  "jobId": "job_parse_abc123",
  "status": "PENDING"
}
```

---

### 6.2 파싱 결과 조회
```
GET /api/v1/diagnostics/{diagnosticId}/files/{fileId}/parsing-result
```

---

### 6.3 파일 다운로드 URL 발급
```
GET /api/v1/files/{fileId}/download-url
```

**Response:**
```json
{
  "downloadUrl": "https://storage.example.com/...",
  "expiresAt": "2026-01-26T12:30:00"
}
```

---

### 6.4 파일 삭제
```
DELETE /api/v1/files/{fileId}
```

---

### 6.5 데이터 패키지 다운로드 URL 발급
```
GET /api/v1/files/diagnostics/{diagnosticId}/package-url
```

---

## 7. 비동기 작업 (Job)

### 7.1 작업 상태 조회
```
GET /api/v1/jobs/{jobId}
```

**Response:**
```json
{
  "jobId": "job_parse_abc123",
  "jobType": "FILE_PARSING",
  "status": "RUNNING",
  "progress": 50,
  "message": "파일 분석 중...",
  "resultUrl": null
}
```

**작업 상태:**
- `PENDING` - 대기중
- `RUNNING` - 진행중
- `SUCCEEDED` - 완료
- `FAILED` - 실패

---

### 7.2 작업 재시도
```
POST /api/v1/jobs/{jobId}/retry
```

---

## 8. 알림 (Notification)

### 8.1 알림 목록 조회
```
GET /api/v1/notifications
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| isRead | boolean | N | 읽음 여부 필터 |
| page | int | N | 페이지 번호 |
| size | int | N | 페이지 크기 (기본: 20) |

---

### 8.2 알림 읽음 처리
```
PATCH /api/v1/notifications/read
```

**Request Body:**
```json
{
  "notificationIds": [1, 2, 3]
}
```

또는 전체 읽음:
```json
{
  "readAll": true
}
```

---

## 9. 관리 (Management)

> 권한: REVIEWER

### 9.1 권한 대시보드 조회
```
GET /api/v1/management/permissions/dashboard
```

---

### 9.2 권한 요청 처리
```
PATCH /api/v1/management/permissions/{accessRequestId}
```

---

### 9.3 사용자 관리 목록 조회
```
GET /api/v1/management/users
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| role | string | N | GUEST, DRAFTER, APPROVER, REVIEWER |
| status | string | N | ACTIVE, INACTIVE |
| companyId | long | N | 회사 ID 필터 |
| keyword | string | N | 이름/이메일 검색 |
| page | int | N | 페이지 번호 |
| size | int | N | 페이지 크기 |

---

### 9.4 사용자 역할 변경
```
PATCH /api/v1/management/users/{userId}/role
```

**Request Body:**
```json
{
  "roleCode": "DRAFTER"
}
```

---

### 9.5 사용자 상태 변경
```
PATCH /api/v1/management/users/{userId}/status
```

**Request Body:**
```json
{
  "status": "INACTIVE"
}
```

---

### 9.6 협력사 목록 조회
```
GET /api/v1/management/companies
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| companyType | string | N | TIER1, TIER2 |
| industryCode | string | N | 업종 코드 |
| keyword | string | N | 회사명 검색 |
| page | int | N | 페이지 번호 |
| size | int | N | 페이지 크기 |

---

### 9.7 협력사 등록
```
POST /api/v1/management/companies
```

**Request Body:**
```json
{
  "name": "(주)신규협력사",
  "businessNumber": "123-45-67890",
  "scale": "중소기업",
  "companyType": "TIER2",
  "industryCode": "MANUFACTURING",
  "ceoName": "김대표",
  "address": "서울시 강남구",
  "contactEmail": "contact@company.com",
  "contactPhone": "02-1234-5678"
}
```

---

### 9.8 활동 로그 조회
```
GET /api/v1/management/activity-logs
```

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | long | N | 특정 사용자 ID |
| fromDate | date | N | 기간 시작 |
| toDate | date | N | 기간 종료 |
| actionType | string | N | 액션 유형 |
| page | int | N | 페이지 번호 |
| size | int | N | 페이지 크기 (기본: 50) |

---

### 9.9 활동 로그 내보내기
```
POST /api/v1/management/activity-logs/export
```

**Request Body:**
```json
{
  "format": "CSV",
  "fromDate": "2026-01-01",
  "toDate": "2026-01-31"
}
```

---

## 10. AI 서비스

각 도메인별 AI 서비스 엔드포인트입니다.

### 10.1 ESG 도메인 - 증빙 파싱

#### 증빙 파일 AI 파싱 요청
```
POST /api/v1/ai/esg/parse
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: 파싱할 증빙 파일
- `diagnosticId`: 진단 ID

**Response:** `202 Accepted`
```json
{
  "jobId": "job_esg_parse_abc123",
  "status": "PENDING",
  "message": "ESG 증빙 파싱이 시작되었습니다."
}
```

#### AI 리포트 생성 요청
```
POST /api/v1/ai/esg/report
```

**Request Body:**
```json
{
  "diagnosticId": 1,
  "reportType": "SUMMARY"
}
```

**Response:** `202 Accepted`
```json
{
  "jobId": "job_esg_report_abc123",
  "status": "PENDING"
}
```

---

### 10.2 SAFETY 도메인 - TBM 영상 분석

#### TBM 영상 업로드 및 분석 요청
```
POST /api/v1/ai/safety/tbm/upload
Content-Type: multipart/form-data
```

**Form Data:**
- `video`: TBM 영상 파일
- `diagnosticId`: 진단 ID
- `tbmDate`: TBM 날짜 (yyyy-MM-dd)

**Response:** `202 Accepted`
```json
{
  "jobId": "job_tbm_analyze_abc123",
  "status": "PENDING",
  "message": "TBM 영상 분석이 시작되었습니다."
}
```

#### TBM 분석 결과 조회
```
GET /api/v1/ai/safety/tbm/{jobId}
```

**Response:**
```json
{
  "jobId": "job_tbm_analyze_abc123",
  "status": "SUCCEEDED",
  "result": {
    "safetyScore": 85,
    "checklist": [
      { "item": "안전모 착용", "passed": true },
      { "item": "안전대 착용", "passed": true },
      { "item": "작업 전 안전 교육", "passed": false, "reason": "교육 미실시 감지" }
    ],
    "riskLevel": "LOW"
  }
}
```

---

### 10.3 COMPLIANCE 도메인 - 계약서 검토

#### 하도급 계약서 업로드
```
POST /api/v1/ai/compliance/contract/upload
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: 계약서 파일 (PDF)
- `contractType`: 계약 유형 (SUBCONTRACT, SERVICE, etc.)

**Response:**
```json
{
  "contractId": 1,
  "fileName": "contract_2026.pdf",
  "status": "UPLOADED"
}
```

#### AI 계약서 검토 요청
```
POST /api/v1/ai/compliance/contract/{contractId}/review
```

**Response:** `202 Accepted`
```json
{
  "jobId": "job_contract_review_abc123",
  "status": "PENDING",
  "message": "계약서 검토가 시작되었습니다."
}
```

#### 검토 결과 조회
```
GET /api/v1/ai/compliance/contract/{contractId}/result
```

**Response:**
```json
{
  "contractId": 1,
  "reviewStatus": "COMPLETED",
  "overallRisk": "MEDIUM",
  "riskItems": [
    {
      "clause": "제5조 (손해배상)",
      "riskLevel": "HIGH",
      "issue": "불공정한 손해배상 조항",
      "recommendation": "손해배상 범위 제한 조항 추가 권장"
    }
  ],
  "complianceScore": 72
}
```

---

### 10.4 AI Run API (공통)

> 기획서 기반 공통 AI Run API - 모든 도메인(ESG, SAFETY, COMPLIANCE)에서 동일한 인터페이스 사용

#### AI Run Preview - 슬롯 추정
```
POST /api/v1/ai/run/diagnostics/{diagnosticId}/preview
```

파일 추가 시 필수 항목(슬롯) 현황 확인 및 파일-슬롯 매핑 추정

**Request Body:**
```json
{
  "fileIds": [1, 2, 3]
}
```

**Response:**
```json
{
  "packageId": "PKG_1_202601_1",
  "requiredSlotStatus": [
    { "slotName": "esg.energy.usage", "required": true, "submitted": true },
    { "slotName": "esg.energy.bill", "required": true, "submitted": false }
  ],
  "slotHints": [
    { "fileId": "1", "slotName": "esg.energy.usage", "confidence": 0.95 }
  ],
  "missingRequiredSlots": ["esg.energy.gas.usage", "esg.hazmat.msds", "esg.ethics.code"]
}
```

---

#### AI Run Submit - 전체 검증 요청 (비동기)
```
POST /api/v1/ai/run/diagnostics/{diagnosticId}/submit
```

**Response:** `202 Accepted`
```json
{
  "diagnosticId": 1,
  "status": "PENDING",
  "message": "AI 분석이 시작되었습니다. 결과는 조회 API로 확인하세요."
}
```

---

#### AI Run 결과 조회
```
GET /api/v1/ai/run/diagnostics/{diagnosticId}/result
```

최신 AI 분석 결과 조회 (백엔드 DB에서 조회, AI 서비스 호출 없음)

> **verdict 가능한 값**: `PASS` | `WARN` | `NEED_CLARIFY` | `NEED_FIX`
> **riskLevel 가능한 값**: `LOW` | `MEDIUM` | `HIGH`

**Response:**
```json
{
  "resultId": 1,
  "diagnosticId": 1,
  "domainCode": "ESG",
  "packageId": "PKG_1_202601_1",
  "riskLevel": "MEDIUM",
  "verdict": "NEED_FIX",
  "whySummary": "에너지 사용량 증빙 확인 완료, 화학물질 MSDS 미제출",
  "resultJson": "{...}",
  "analyzedAt": "2026-01-28T10:30:00"
}
```

---

#### AI Run 이력 조회
```
GET /api/v1/ai/run/diagnostics/{diagnosticId}/history
```

진단의 AI Run 분석 이력 전체 조회 (백엔드 DB에서 조회, AI 서비스 호출 없음)

**Response:**
```json
[
  {
    "resultId": 2,
    "diagnosticId": 1,
    "domainCode": "ESG",
    "packageId": "PKG_1_202601_1",
    "riskLevel": "LOW",
    "verdict": "PASS",
    "whySummary": "모든 필수 항목 검증 완료",
    "analyzedAt": "2026-01-28T14:00:00"
  },
  {
    "resultId": 1,
    "diagnosticId": 1,
    "domainCode": "ESG",
    "packageId": "PKG_1_202601_1",
    "riskLevel": "MEDIUM",
    "verdict": "PASS",
    "whySummary": "에너지 사용량 증빙 확인 완료",
    "analyzedAt": "2026-01-28T10:30:00"
  }
]
```

---

### 10.5 AI 작업 공통

#### AI 작업 상태 조회
```
GET /api/v1/ai/jobs/{jobId}
```

#### AI 작업 재시도
```
POST /api/v1/ai/jobs/{jobId}/retry
```

#### AI 서비스 상태 확인
```
GET /api/v1/ai/health
```

**Response:**
```json
{
  "status": "UP",
  "services": {
    "esg": "UP",
    "safety": "UP",
    "compliance": "UP"
  }
}
```

---

## 테스트 계정

| 역할 | 이메일 | 비밀번호 |
|------|--------|----------|
| REVIEWER | reviewer@smartchain.co.kr | Test1234! |
| APPROVER | approver@techpartner.co.kr | Test1234! |
| DRAFTER | drafter1@techpartner.co.kr | Test1234! |
| DRAFTER | drafter2@greenmanu.co.kr | Test1234! |
| GUEST | guest@example.com | Test1234! |

---

## Swagger UI

개발 환경에서 Swagger UI 확인:
```
http://localhost:8080/swagger-ui.html
```
