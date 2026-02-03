# SmartChain API 테스트 HTTP 파일

IntelliJ IDEA 또는 VS Code REST Client로 API를 테스트할 수 있는 HTTP 파일 모음입니다.

## 파일 구성

| 파일 | 설명 |
|------|------|
| `00_seed_test_data.sql` | 테스트용 시드 데이터 (Domain, Industry, Company, Campaign) |
| `01_auth_and_setup.http` | 인증 API (회원가입, 로그인, 내 정보) |
| `02_esg_workflow.http` | ESG 워크플로우 (기안자 → 결재자 → 수신자) |
| `03_safety_compliance_workflow.http` | SAFETY/COMPLIANCE 워크플로우 (기안자 → 수신자) |
| `04_ai_run_workflow.http` | AI Run API 테스트 (Preview → Submit → Result → History) |
| `http-client.env.json` | 환경 변수 설정 |

## 사전 준비

### 1. 시드 데이터 입력

`data.sql`에는 Role만 있으므로 추가 데이터 필요:

```bash
# Docker PostgreSQL (smartchain-db 컨테이너)
docker exec -i smartchain-db psql -U esg -d smartchain < http/00_seed_test_data.sql

# 또는 컨테이너 내부 접속 후 실행
docker exec -it smartchain-db psql -U esg -d smartchain
# psql> \i /path/to/00_seed_test_data.sql
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. 역할 부여

회원가입 후 사용자는 GUEST 역할이 부여됩니다.
테스트를 위해 DB에서 직접 역할을 변경하거나 권한 요청 API를 사용하세요:

```bash
# Docker 컨테이너에서 psql 접속
docker exec -it smartchain-db psql -U esg -d smartchain
```

```sql
-- 역할 변경 (예시)
UPDATE "user" SET role_id = (SELECT role_id FROM role WHERE code = 'DRAFTER')
WHERE email = 'drafter@test.com';

-- 도메인 역할 추가
INSERT INTO user_domain_role (user_id, domain_id, role_id, created_at, updated_at)
SELECT u.user_id, d.domain_id, r.role_id, NOW(), NOW()
FROM "user" u, domain d, role r
WHERE u.email = 'drafter@test.com' AND d.code = 'ESG' AND r.code = 'DRAFTER';
```

## 워크플로우 차이

### ESG 도메인 (3단계)
```
기안자(DRAFTER) → 결재자(APPROVER) → 수신자(REVIEWER)
    작성/제출       승인/원청제출         심사/승인
```

### SAFETY, COMPLIANCE 도메인 (2단계)
```
기안자(DRAFTER) → 수신자(REVIEWER)
    작성/제출        심사/승인
(결재 단계 없음)
```

## 테스트 순서

### ESG 전체 플로우

1. `01_auth_and_setup.http` - 회원가입, 로그인
2. DB에서 역할 부여
3. `02_esg_workflow.http` - STEP 1~12 순서대로 실행

### SAFETY/COMPLIANCE 플로우

1. `01_auth_and_setup.http` - 회원가입, 로그인
2. DB에서 역할 부여
3. `03_safety_compliance_workflow.http` - STEP 1~7 순서대로 실행

## 변수 사용

HTTP 파일 내에서 자동으로 변수가 저장됩니다:

- `{{drafterToken}}` - 기안자 JWT 토큰
- `{{approverToken}}` - 결재자 JWT 토큰
- `{{reviewerToken}}` - 수신자 JWT 토큰
- `{{diagnosticId}}` - 생성된 기안 ID
- `{{approvalId}}` - 결재 ID
- `{{reviewId}}` - 심사 ID

### AI Run API 테스트

1. AI 서비스(Python FastAPI) 실행: `http://localhost:8000`
2. 진단 생성 + 증빙 파일 업로드 완료
3. `04_ai_run_workflow.http` - STEP 1~6 순서대로 실행

```
파일 업로드 → Preview(슬롯 추정) → Submit(AI 검증, 비동기)
→ Result(결과 조회, DB) → Result Detail(상세) → History(이력, DB)
```

> **참고**: Submit 후 Result 조회 시 404가 반환되면 아직 분석 중입니다. 잠시 후 재시도하세요.

## 주의사항

1. **캠페인 ID**: 기안 생성 시 `campaignId`는 실제 DB의 캠페인 ID로 교체 필요
2. **회사 연결**: 사용자와 회사 연결이 필요한 경우 DB에서 수동 설정
3. **토큰 만료**: JWT 토큰 만료 시 다시 로그인 필요
