# E2E 테스트 시나리오

> 프론트엔드-백엔드 통합 테스트용 시나리오 문서
>
> **테스트 환경**: http://localhost:8080
> **공통 비밀번호**: `Test1234!`

---

## 1. 인증 (Authentication)

### 1.1 로그인 성공

| 항목 | 내용 |
|------|------|
| **시나리오** | 유효한 계정으로 로그인 |
| **계정** | `drafter1@techpartner.co.kr` / `Test1234!` |
| **API** | `POST /api/v1/auth/login` |
| **예상 결과** | 200 OK, accessToken 반환 |
| **확인 사항** | - 토큰이 정상 발급되는가<br>- 사용자 정보가 올바른가 |

### 1.2 로그인 실패 - 잘못된 비밀번호

| 항목 | 내용 |
|------|------|
| **시나리오** | 잘못된 비밀번호로 로그인 시도 |
| **계정** | `drafter1@techpartner.co.kr` / `WrongPassword!` |
| **예상 결과** | 401 Unauthorized, `AUTH_001` |

### 1.3 로그인 실패 - 존재하지 않는 계정

| 항목 | 내용 |
|------|------|
| **시나리오** | 존재하지 않는 이메일로 로그인 |
| **계정** | `notexist@example.com` / `Test1234!` |
| **예상 결과** | 401 Unauthorized, `AUTH_001` |

---

## 2. 진단 (Diagnostic)

### 2.1 진단 목록 조회 - 기안자

| 항목 | 내용 |
|------|------|
| **시나리오** | 기안자가 본인 진단 목록 조회 |
| **계정** | `drafter1@techpartner.co.kr` |
| **API** | `GET /api/v1/diagnostics` |
| **예상 결과** | 본인이 작성한 진단만 표시 |
| **확인 사항** | - DG-ESG-2026-001 (WRITING)<br>- DG-ESG-2026-006 (APPROVED)<br>- DG-COMPL-2026-001 (WRITING) |

### 2.2 진단 목록 조회 - 도메인 필터

| 항목 | 내용 |
|------|------|
| **시나리오** | ESG 도메인 진단만 조회 |
| **계정** | `drafter1@techpartner.co.kr` |
| **API** | `GET /api/v1/diagnostics?domainCode=ESG` |
| **예상 결과** | ESG 도메인 진단만 표시 |

### 2.3 진단 목록 조회 - 상태 필터

| 항목 | 내용 |
|------|------|
| **시나리오** | 작성중 상태 진단만 조회 |
| **계정** | `drafter1@techpartner.co.kr` |
| **API** | `GET /api/v1/diagnostics?statuses=WRITING` |
| **예상 결과** | WRITING 상태 진단만 표시 |

### 2.4 진단 상세 조회

| 항목 | 내용 |
|------|------|
| **시나리오** | 진단 상세 정보 조회 |
| **계정** | `drafter1@techpartner.co.kr` |
| **API** | `GET /api/v1/diagnostics/{diagnosticId}` |
| **예상 결과** | 진단 상세 정보 (캠페인, 회사, 기간, 상태 등) |

### 2.5 진단 제출

| 항목 | 내용 |
|------|------|
| **시나리오** | 작성중 진단을 제출 |
| **계정** | `drafter1@techpartner.co.kr` |
| **대상** | DG-ESG-2026-001 (WRITING 상태) |
| **API** | `POST /api/v1/diagnostics/{id}/submit` |
| **예상 결과** | - 상태: WRITING → SUBMITTED<br>- Approval 레코드 생성<br>- approvalId 반환 |

### 2.6 진단 제출 실패 - 이미 제출됨

| 항목 | 내용 |
|------|------|
| **시나리오** | 이미 제출된 진단 재제출 시도 |
| **계정** | `drafter2@techpartner.co.kr` |
| **대상** | DG-ESG-2026-003 (SUBMITTED 상태) |
| **예상 결과** | 400 Bad Request, `BIZ_002` |

### 2.7 진단 접근 권한 없음

| 항목 | 내용 |
|------|------|
| **시나리오** | 다른 사람의 진단 접근 시도 |
| **계정** | `drafter1@techpartner.co.kr` |
| **대상** | 그린매뉴 소속 진단 |
| **예상 결과** | 403 Forbidden |

---

## 3. 결재 (Approval)

### 3.1 결재 대기 목록 조회 - 결재자

| 항목 | 내용 |
|------|------|
| **시나리오** | 결재자가 대기중인 결재 목록 조회 |
| **계정** | `approver@techpartner.co.kr` |
| **API** | `GET /api/v1/approvals?status=WAITING` |
| **예상 결과** | 테크파트너 소속 WAITING 상태 결재 목록 |
| **확인 사항** | - DG-ESG-2026-003 결재 건 |

### 3.2 결재 승인

| 항목 | 내용 |
|------|------|
| **시나리오** | 결재 승인 처리 |
| **계정** | `approver@techpartner.co.kr` |
| **API** | `POST /api/v1/approvals/{id}/approve` |
| **Body** | `{ "comment": "승인합니다" }` |
| **예상 결과** | - Approval 상태: APPROVED<br>- Diagnostic 상태: APPROVED<br>- Review 레코드 생성 |

### 3.3 결재 반려

| 항목 | 내용 |
|------|------|
| **시나리오** | 결재 반려 처리 |
| **계정** | `approver@techpartner.co.kr` |
| **API** | `POST /api/v1/approvals/{id}/reject` |
| **Body** | `{ "comment": "보완 필요합니다" }` |
| **예상 결과** | - Approval 상태: REJECTED<br>- Diagnostic 상태: RETURNED |

### 3.4 결재 권한 없음 - 다른 회사

| 항목 | 내용 |
|------|------|
| **시나리오** | 다른 회사 결재 시도 |
| **계정** | `approver@techpartner.co.kr` |
| **대상** | 그린매뉴 결재 건 |
| **예상 결과** | 403 Forbidden |

---

## 4. 심사 (Review)

### 4.1 심사 대기 목록 조회 - 심사자

| 항목 | 내용 |
|------|------|
| **시나리오** | 심사자가 심사 목록 조회 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `GET /api/v1/reviews?status=REVIEWING` |
| **예상 결과** | ESG 도메인 심사 목록 |
| **확인 사항** | - DG-ESG-2025-001 (테크파트너, 점수 68)<br>- DG-ESG-2025-002 (그린매뉴, 점수 75) |

### 4.2 심사 상세 조회

| 항목 | 내용 |
|------|------|
| **시나리오** | 심사 상세 정보 조회 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `GET /api/v1/reviews/{reviewId}` |
| **예상 결과** | 심사 상세 (진단 정보, 점수, 위험도 등) |

### 4.3 심사 승인

| 항목 | 내용 |
|------|------|
| **시나리오** | 심사 승인 처리 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `POST /api/v1/reviews/{id}/approve` |
| **Body** | `{ "comment": "우수합니다", "categoryCommentE": "환경 우수" }` |
| **예상 결과** | - Review 상태: APPROVED<br>- Diagnostic 상태: COMPLETED |

### 4.4 심사 보완 요청

| 항목 | 내용 |
|------|------|
| **시나리오** | 보완 요청 처리 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `POST /api/v1/reviews/{id}/request-revision` |
| **Body** | `{ "comment": "증빙자료 보완 필요" }` |
| **예상 결과** | Review 상태: REVISION_REQUIRED |

---

## 5. 역할 요청 (Role Request)

### 5.1 역할 요청 생성 - 게스트

| 항목 | 내용 |
|------|------|
| **시나리오** | 게스트가 기안자 권한 요청 |
| **계정** | `newbie1@precision.co.kr` |
| **API** | `POST /api/v1/role-requests` |
| **Body** | `{ "domainCode": "ESG", "requestedRole": "DRAFTER", "reason": "업무 담당" }` |
| **예상 결과** | RoleRequest 생성, 상태: PENDING |

### 5.2 역할 요청 목록 조회 - 심사자

| 항목 | 내용 |
|------|------|
| **시나리오** | 심사자가 대기중인 역할 요청 조회 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `GET /api/v1/management/role-requests?status=PENDING` |
| **예상 결과** | PENDING 상태 역할 요청 목록 |

### 5.3 역할 요청 승인

| 항목 | 내용 |
|------|------|
| **시나리오** | 역할 요청 승인 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `POST /api/v1/management/role-requests/{id}/approve` |
| **예상 결과** | - RoleRequest 상태: APPROVED<br>- UserDomainRole 생성 |

### 5.4 역할 요청 거절

| 항목 | 내용 |
|------|------|
| **시나리오** | 역할 요청 거절 |
| **계정** | `reviewer.esg@hdhhi.co.kr` |
| **API** | `POST /api/v1/management/role-requests/{id}/reject` |
| **Body** | `{ "rejectReason": "현재 권한 부여 불가" }` |
| **예상 결과** | RoleRequest 상태: REJECTED |

---

## 6. 알림 (Notification)

### 6.1 알림 목록 조회

| 항목 | 내용 |
|------|------|
| **시나리오** | 본인 알림 목록 조회 |
| **계정** | `drafter1@techpartner.co.kr` |
| **API** | `GET /api/v1/notifications` |
| **예상 결과** | 본인에게 온 알림 목록 |

### 6.2 알림 읽음 처리

| 항목 | 내용 |
|------|------|
| **시나리오** | 알림 읽음 표시 |
| **API** | `PATCH /api/v1/notifications/{id}/read` |
| **예상 결과** | isRead: true |

---

## 7. 캠페인 (Campaign)

### 7.1 캠페인 목록 조회

| 항목 | 내용 |
|------|------|
| **시나리오** | 진행중인 캠페인 목록 조회 |
| **계정** | 아무 계정 |
| **API** | `GET /api/v1/campaigns` |
| **예상 결과** | 캠페인 목록 (6개) |

### 7.2 캠페인 상세 조회

| 항목 | 내용 |
|------|------|
| **시나리오** | 캠페인 상세 정보 조회 |
| **API** | `GET /api/v1/campaigns/{campaignId}` |
| **예상 결과** | 캠페인 상세 (기간, 마감일, 도메인 등) |

---

## 8. AI 분석 (AI Run API)

### 8.1 AI 슬롯 추정 (Preview)

| 항목 | 내용 |
|------|------|
| **시나리오** | 업로드된 파일의 슬롯 추정 |
| **계정** | `drafter1@techpartner.co.kr` |
| **API** | `POST /api/v1/ai/run/diagnostics/{id}/preview` |
| **예상 결과** | 슬롯 추정 결과 (slotHints) |

### 8.2 AI 분석 요청 (Submit)

| 항목 | 내용 |
|------|------|
| **시나리오** | AI 전체 검증 요청 |
| **API** | `POST /api/v1/ai/run/diagnostics/{id}/submit` |
| **예상 결과** | 분석 시작, jobId 반환 |

### 8.3 AI 분석 결과 조회

| 항목 | 내용 |
|------|------|
| **시나리오** | AI 분석 결과 조회 |
| **API** | `GET /api/v1/ai/run/diagnostics/{id}/result` |
| **예상 결과** | 분석 결과 (verdict, riskLevel, slotResults) |

---

## 9. 도메인별 워크플로우 테스트

> **주의**: 현재 백엔드에 도메인별 분기 미구현 (Issue #78)

### 9.1 ESG 도메인 플로우 (현재 구현됨)

```
기안자 로그인 → 진단 작성 → 제출 → 결재자 승인 → 심사자 심사 → 완료
```

| 단계 | 계정 | 상태 변화 |
|------|------|----------|
| 1. 진단 제출 | drafter1@techpartner.co.kr | WRITING → SUBMITTED |
| 2. 결재 승인 | approver@techpartner.co.kr | SUBMITTED → APPROVED |
| 3. 심사 승인 | reviewer.esg@hdhhi.co.kr | APPROVED → COMPLETED |

### 9.2 SAFETY 도메인 플로우 (구현 예정)

```
기안자 로그인 → 진단 작성 → 제출 → (결재 스킵) → 심사자 심사 → 완료
```

| 단계 | 계정 | 상태 변화 |
|------|------|----------|
| 1. 진단 제출 | drafter@safebuild.co.kr | WRITING → APPROVED (바로) |
| 2. 심사 승인 | reviewer.safety@hdhhi.co.kr | APPROVED → COMPLETED |

### 9.3 COMPLIANCE 도메인 플로우 (구현 예정)

SAFETY와 동일

---

## 10. 에러 케이스 테스트

### 10.1 인증 에러

| 코드 | 상황 | 예상 응답 |
|------|------|----------|
| AUTH_001 | 잘못된 로그인 정보 | 401 |
| AUTH_002 | 토큰 만료 | 401 |
| AUTH_003 | 유효하지 않은 토큰 | 401 |

### 10.2 권한 에러

| 코드 | 상황 | 예상 응답 |
|------|------|----------|
| PERM_001 | 해당 액션 권한 없음 | 403 |
| PERM_002 | 해당 리소스 접근 불가 | 403 |

### 10.3 비즈니스 에러

| 코드 | 상황 | 예상 응답 |
|------|------|----------|
| BIZ_001 | 상태 전이 불가 | 400 |
| BIZ_002 | 이미 제출된 진단 | 400 |
| BIZ_003 | 이미 처리된 결재 | 400 |

### 10.4 리소스 에러

| 코드 | 상황 | 예상 응답 |
|------|------|----------|
| RES_001 | 사용자 없음 | 404 |
| RES_002 | 진단 없음 | 404 |
| RES_003 | 결재 없음 | 404 |

---

## 부록: 테스트 계정 요약

### 심사자 (REVIEWER)

| 이메일 | 담당 도메인 |
|--------|-------------|
| reviewer.esg@hdhhi.co.kr | ESG |
| reviewer.safety@hdhhi.co.kr | SAFETY |
| reviewer.compliance@hdhhi.co.kr | COMPLIANCE |

### 결재자 (APPROVER)

| 이메일 | 회사 | 담당 도메인 |
|--------|------|-------------|
| approver@techpartner.co.kr | 테크파트너 | ESG, COMPLIANCE |
| approver@greenmanu.co.kr | 그린매뉴팩처링 | ESG, SAFETY |
| approver@safebuild.co.kr | 안전건설 | SAFETY |

### 기안자 (DRAFTER)

| 이메일 | 회사 | 담당 도메인 |
|--------|------|-------------|
| drafter1@techpartner.co.kr | 테크파트너 | ESG, COMPLIANCE |
| drafter2@techpartner.co.kr | 테크파트너 | ESG |
| drafter@greenmanu.co.kr | 그린매뉴팩처링 | ESG, SAFETY |
| drafter@safebuild.co.kr | 안전건설 | SAFETY |

### 게스트 (GUEST)

| 이메일 | 회사 |
|--------|------|
| newbie1@precision.co.kr | 정밀부품 |
| newbie2@precision.co.kr | 정밀부품 |

---

## 체크리스트

### 인증
- [ ] 로그인 성공
- [ ] 로그인 실패 (잘못된 비밀번호)
- [ ] 로그인 실패 (없는 계정)
- [ ] 토큰 갱신

### 진단
- [ ] 목록 조회 (기안자)
- [ ] 목록 조회 (결재자)
- [ ] 목록 조회 (심사자)
- [ ] 도메인 필터
- [ ] 상태 필터
- [ ] 상세 조회
- [ ] 진단 제출
- [ ] 제출 실패 (이미 제출됨)

### 결재
- [ ] 대기 목록 조회
- [ ] 결재 승인
- [ ] 결재 반려
- [ ] 권한 없음 에러

### 심사
- [ ] 심사 목록 조회
- [ ] 심사 상세 조회
- [ ] 심사 승인
- [ ] 보완 요청

### 역할 요청
- [ ] 역할 요청 생성
- [ ] 역할 요청 승인
- [ ] 역할 요청 거절

### 알림
- [ ] 알림 목록 조회
- [ ] 읽음 처리
