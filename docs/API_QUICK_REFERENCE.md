# API 빠른 참조표

> **버전**: 2.4
> **최종 수정**: 2026-01-30
> 프론트엔드 개발용 API 요약

---

## 도메인 필터링

대부분의 목록 조회 API는 `domainCode` 쿼리 파라미터를 지원합니다:

```
GET /api/v1/diagnostics?domainCode=ESG&page=0&size=10
GET /api/v1/approvals?domainCode=SAFETY&status=WAITING
GET /api/v1/reviews?domainCode=COMPLIANCE
```

---

## 인증 (공개)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/register` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 → accessToken 발급 |
| POST | `/api/v1/auth/check-email` | 이메일 중복 확인 |
| POST | `/api/v1/auth/send-verification` | 인증 코드 발송 |
| POST | `/api/v1/auth/verify-email` | 인증 코드 확인 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| GET | `/api/v1/auth/me` | 내 정보 조회 |

---

## 기안자 (DRAFTER)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/diagnostics` | 내 기안 목록 |
| GET | `/api/v1/diagnostics/{id}` | 기안 상세 |
| POST | `/api/v1/diagnostics` | 기안 생성 |
| POST | `/api/v1/diagnostics/{id}/submit` | 기안 제출 |
| GET | `/api/v1/diagnostics/{id}/ai-analysis` | AI 분석 결과 |
| GET | `/api/v1/diagnostics/{id}/history` | 상태 이력 |
| POST | `/api/v1/diagnostics/{id}/files` | 파일 업로드 |

---

## 결재자 (APPROVER)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/approvals` | 결재 대기 목록 |
| GET | `/api/v1/approvals/{id}` | 결재 상세 |
| PATCH | `/api/v1/approvals/{id}` | 결재 처리 (승인/반려) |
| POST | `/api/v1/approvals/{id}/submit-to-reviewer` | 원청 제출 |

---

## 수신자 (REVIEWER)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/reviews/dashboard` | 대시보드 |
| GET | `/api/v1/reviews` | 심사 목록 |
| GET | `/api/v1/reviews/{id}` | 심사 상세 |
| PATCH | `/api/v1/reviews/{id}` | 심사 결과 입력 |
| POST | `/api/v1/reviews/{id}/report` | 보고서 생성 |
| POST | `/api/v1/reviews/bulk-report` | 일괄 보고서 |
| POST | `/api/v1/reviews/export` | Excel 내보내기 |

---

## 권한 요청 (전체)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/roles/request-page` | 권한 요청 페이지 정보 |
| POST | `/api/v1/roles/requests` | 권한 요청 생성 |
| GET | `/api/v1/roles/requests/my` | 내 요청 상태 |
| GET | `/api/v1/roles/requests` | 요청 목록 (관리자) |
| PATCH | `/api/v1/roles/requests/{id}` | 요청 승인/반려 |

---

## 파일

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/diagnostics/{id}/files` | 업로드 (multipart) |
| GET | `/api/v1/diagnostics/{id}/files/{fid}/parsing-result` | 파싱 결과 |
| GET | `/api/v1/files/{id}/download-url` | 다운로드 URL |
| GET | `/api/v1/files/diagnostics/{id}/package-url` | 전체 파일 패키지 URL |
| DELETE | `/api/v1/files/{id}` | 파일 삭제 |

---

## 비동기 작업

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/jobs/{jobId}` | 작업 상태 조회 |
| POST | `/api/v1/jobs/{jobId}/retry` | 재시도 |

---

## 알림

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/notifications` | 알림 목록 |
| PATCH | `/api/v1/notifications/read` | 읽음 처리 |

---

## 관리 (REVIEWER 전용)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/management/permissions/dashboard` | 권한 대시보드 |
| PATCH | `/api/v1/management/permissions/{id}` | 권한 처리 |
| GET | `/api/v1/management/users` | 사용자 목록 |
| PATCH | `/api/v1/management/users/{id}/role` | 역할 변경 |
| PATCH | `/api/v1/management/users/{id}/status` | 상태 변경 |
| GET | `/api/v1/management/companies` | 협력사 목록 |
| POST | `/api/v1/management/companies` | 협력사 등록 |
| GET | `/api/v1/management/activity-logs` | 활동 로그 |

---

## 상태값 참조

### 기안 상태 (DiagnosticStatus)
```
WRITING → SUBMITTED → RETURNED (반려시)
                   ↓
              APPROVED → REVIEWING → COMPLETED
```

### 결재 상태 (ApprovalStatus)
```
WAITING → APPROVED / REJECTED
```

### 심사 상태 (ReviewStatus)
```
REVIEWING → APPROVED / REVISION_REQUIRED
```

### 작업 상태 (JobStatus)
```
PENDING → RUNNING → SUCCEEDED / FAILED
```

---

## 도메인 코드

| 코드 | 이름 | 설명 |
|------|------|------|
| ESG | ESG 실사 | ESG 공급망 실사 및 진단 |
| SAFETY | 안전보건 | TBM 영상 분석 기반 안전보건 관리 |
| COMPLIANCE | 컴플라이언스 | 하도급 계약서 AI 검토 |

### 역할별 도메인 권한 매트릭스

사용자는 각 도메인별로 서로 다른 역할을 가질 수 있습니다.

| 기능 | GUEST | DRAFTER | APPROVER | REVIEWER |
|------|-------|---------|----------|----------|
| 도메인 목록 조회 | O | O | O | O |
| 진단 작성/제출 | X | O (해당 도메인) | X | X |
| 결재 목록/처리 | X | X | O (해당 도메인) | X |
| 심사 목록/처리 | X | X | X | O (해당 도메인) |
| AI 분석 요청 | X | O (해당 도메인) | O (해당 도메인) | O (해당 도메인) |

### 도메인 권한 검증

- `domainCode` 파라미터로 필터링 시, 해당 도메인에 권한이 있어야 조회 가능
- 권한 없으면 `DOM002` 에러 반환

---

## AI Run API (공통)

> 모든 도메인(ESG, SAFETY, COMPLIANCE)에서 동일한 인터페이스를 사용하는 통합 AI API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/ai/run/diagnostics/{id}/preview` | 슬롯 추정 (파일 추가 시 필수 항목 확인) |
| POST | `/api/v1/ai/run/diagnostics/{id}/submit` | AI 검증 요청 (비동기) |
| GET | `/api/v1/ai/run/diagnostics/{id}/result` | 최신 AI 분석 결과 조회 |
| GET | `/api/v1/ai/run/diagnostics/{id}/result/detail` | AI 분석 결과 상세 (슬롯별 파싱) |
| GET | `/api/v1/ai/run/diagnostics/{id}/history` | AI 분석 이력 조회 |

### 도메인별 슬롯

| 도메인 | 전체/필수 | 주요 슬롯 |
|--------|:---------:|----------|
| ESG | 15/4 | `esg.energy.electricity.usage`, `esg.energy.gas.usage`, `esg.hazmat.msds`, `esg.ethics.code` |
| SAFETY | 8/4 | `safety.education.status`, `safety.fire.inspection`, `safety.risk.assessment`, `safety.management.system` |
| COMPLIANCE | 7/3 | `compliance.contract.sample`, `compliance.education.privacy`, `compliance.fair.trade` |

> 전체 슬롯 목록은 [API Contract SSOT §8.6](./API_CONTRACT_SSOT.md) 참조

### 아키텍처 참고

- `preview`/`submit`은 AI 서비스(Python FastAPI)로 프록시
- `result`/`history`는 백엔드 DB 조회 (AI 서비스 호출 없음)

### AI Run 응답 스키마 (고정)

```json
{
  "packageId": "PKG_1_202601_1",
  "verdict": "PASS | WARN | NEED_CLARIFY | NEED_FIX",
  "riskLevel": "LOW | MEDIUM | HIGH",
  "why": "판정 사유",
  "slotResults": [...],
  "clarifications": [...],
  "extras": {...}
}
```
