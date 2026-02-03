# API Contract - Single Source of Truth (SSOT)

> **버전**: 2.4
> **최종 수정**: 2026-02-02
> **목적**: 백엔드/프론트엔드 간 API 계약의 단일 진실 공급원

---

## 1. 응답 구조 (Response Structure)

### 1.1 성공 응답 (Success Response)

모든 성공 응답은 `BaseResponse<T>` 형식을 따릅니다.

```typescript
interface BaseResponse<T> {
  success: true;
  message: string;
  data: T;
  timestamp: string; // ISO 8601 형식: "2026-01-20T10:30:00"
}
```

**예시:**
```json
{
  "success": true,
  "message": "성공",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "name": "홍길동"
  },
  "timestamp": "2026-01-20T10:30:00"
}
```

### 1.2 에러 응답 (Error Response)

```typescript
interface ErrorResponse {
  status: number;
  code: string;
  message: string;
}
```

**예시:**
```json
{
  "status": 404,
  "code": "U003",
  "message": "사용자를 찾을 수 없습니다"
}
```

### 1.3 페이지네이션 응답 (Paginated Response)

```typescript
interface PagedResponse<T> {
  content: T[];
  page: PageDto;
}

interface PageDto {
  number: number;      // 현재 페이지 (0-based)
  size: number;        // 페이지 크기
  totalElements: number; // 전체 항목 수
  totalPages: number;  // 전체 페이지 수
}
```

**예시:**
```json
{
  "success": true,
  "message": "성공",
  "data": {
    "content": [
      { "accessRequestId": 1, "status": "PENDING" },
      { "accessRequestId": 2, "status": "APPROVED" }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3
    }
  },
  "timestamp": "2026-01-20T10:30:00"
}
```

---

## 2. 에러 코드 체계 (Error Code System)

### 2.1 코드 형식

```
{도메인}_{시퀀스}
```

| 도메인 접두사 | 설명 |
|-------------|------|
| `U` | 사용자 (User) |
| `A` | 인증 (Auth) |
| `R` | 역할/권한 요청 (Role) |
| `C` | 회사 (Company) |
| `D` | 진단 (Diagnostic) |
| `E` | 증빙파일 (Evidence) |
| `DOM` | 서비스 도메인 (Domain) |
| `PERM` | 권한 에러 (Permission) |
| `S` | 서버 에러 (Server) |

### 2.2 에러 코드 목록

#### 400 Bad Request
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `U001` | 입력값이 올바르지 않습니다 | 입력 필드 검증 |
| `U002` | 이미 존재하는 이메일입니다 | 다른 이메일 사용 |
| `U004` | 비밀번호가 일치하지 않습니다 | 비밀번호 재확인 |
| `U005` | 비밀번호 형식 오류 | 형식 안내 표시 |
| `U006` | 필수 약관에 동의해야 합니다 | 약관 동의 유도 |
| `U008` | 인증 코드가 만료되었습니다 | 재발송 유도 |
| `U009` | 인증 코드가 올바르지 않습니다 | 재입력 유도 |
| `R004` | 유효하지 않은 권한입니다 | 올바른 역할 선택 |
| `R005` | 유효하지 않은 처리 결과입니다 | APPROVED/REJECTED만 허용 |

#### 401 Unauthorized
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `A001` | 유효하지 않은 토큰입니다 | 로그인 페이지로 이동 |
| `A002` | 만료된 토큰입니다 | 토큰 갱신 또는 재로그인 |
| `A003` | 이메일 또는 비밀번호가 올바르지 않습니다 | 재입력 유도 |

#### 403 Forbidden
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `A004` | 접근 권한이 없습니다 | 권한 안내 |
| `DOM002` | 해당 도메인에 대한 접근 권한이 없습니다 | 도메인 권한 요청 유도 |
| `PERM_001` | 해당 리소스에 대한 접근 권한이 없습니다 | 권한 요청 유도 |
| `PERM_002` | 해당 작업을 수행할 권한이 없습니다 | 권한 안내 |

#### 404 Not Found
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `U003` | 사용자를 찾을 수 없습니다 | 목록으로 이동 |
| `R001` | 역할을 찾을 수 없습니다 | 새로고침 |
| `R002` | 권한 요청을 찾을 수 없습니다 | 목록으로 이동 |
| `C001` | 회사를 찾을 수 없습니다 | 목록으로 이동 |
| `D001` | 진단을 찾을 수 없습니다 | 목록으로 이동 |
| `DOM001` | 도메인을 찾을 수 없습니다 | 유효한 도메인 코드 사용 |

#### 409 Conflict
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `R003` | 이미 대기중인 권한 요청이 있습니다 | 기존 요청 상태 확인 유도 |
| `PERM_003` | 이미 처리된 권한 요청입니다 | 목록 새로고침 |

#### 429 Too Many Requests
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `U010` | 잠시 후 다시 시도해주세요 | 카운트다운 표시 |

#### 500 Internal Server Error
| 코드 | 메시지 | 대응 방법 |
|------|--------|----------|
| `S001` | 서버 오류가 발생했습니다 | 재시도 버튼 |
| `S002` | 파일 업로드에 실패했습니다 | 재시도 유도 |
| `S003` | AI 서비스 연동에 실패했습니다 | 재시도 유도 |

---

## 3. 상태 코드 (Status Enums)

### 3.1 권한 요청 상태 (RequestStatus)

```typescript
type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
```

| 상태 | 한글 레이블 | 설명 |
|------|-----------|------|
| `PENDING` | 승인 대기중 | 관리자 승인 대기 |
| `APPROVED` | 승인 완료 | 권한 부여 완료 |
| `REJECTED` | 승인 반려 | 권한 거부됨 |

### 3.2 역할 코드 (RoleCode)

```typescript
type RoleCode = 'GUEST' | 'DRAFTER' | 'APPROVER' | 'REVIEWER';
```

| 코드 | 한글명 | 권한 설명 |
|------|-------|----------|
| `GUEST` | 게스트 | 회원가입 직후, 권한 요청만 가능 |
| `DRAFTER` | 기안자 | ESG 자가 진단 데이터 업로드 |
| `APPROVER` | 결재자 | 회사 정보 관리, ESG 파일 관리, 협력사 권한 관리 |
| `REVIEWER` | 수신자 | 심사, 보고서 발행, 권한 관리 |

### 3.3 진단 상태 (DiagnosticStatus)

```typescript
type DiagnosticStatus = 'WRITING' | 'SUBMITTED' | 'RETURNED' | 'APPROVED' | 'REVIEWING' | 'COMPLETED';
```

### 3.4 도메인 코드 (DomainCode)

```typescript
type DomainCode = 'ESG' | 'SAFETY' | 'COMPLIANCE';
```

| 코드 | 한글명 | 서비스 설명 |
|------|-------|------------|
| `ESG` | ESG 실사 | ESG 증빙 자동 파싱 및 AI 리포트 생성 |
| `SAFETY` | 안전보건 | AI 기반 현장 안전점검(TBM) 자동 검증 |
| `COMPLIANCE` | 컴플라이언스 | LLM 기반 하도급 계약서 자동 검토 |

### 3.5 사용자 도메인 역할 (UserDomainRole)

사용자는 각 도메인별로 서로 다른 역할을 가질 수 있습니다:

```typescript
interface UserDomainRoleDto {
  domainCode: DomainCode;
  domainName: string;
  roleCode: RoleCode;
  roleName: string;
}
```

**예시:**
```json
{
  "domainRoles": [
    { "domainCode": "ESG", "domainName": "ESG 실사", "roleCode": "APPROVER", "roleName": "결재자" },
    { "domainCode": "SAFETY", "domainName": "안전보건", "roleCode": "DRAFTER", "roleName": "기안자" }
  ]
}
```

---

## 4. 공통 DTO 구조 (Common DTOs)

### 4.1 사용자 정보

```typescript
interface UserSimpleDto {
  userId: number;
  name: string;
  email: string;
}

interface UserInfoDto {
  userId: number;
  email: string;
  name: string;
  role?: RoleInfoDto;          // 레거시 전역 역할
  domainRoles?: UserDomainRoleDto[];  // 도메인별 역할
  company?: CompanyInfoDto;
}
```

### 4.2 역할 정보

```typescript
interface RoleSimpleDto {
  code: string;   // "DRAFTER"
  name: string;   // "기안자"
}

interface RoleInfoDto {
  code: string;
  name: string;
}
```

### 4.3 회사 정보

```typescript
interface CompanySimpleDto {
  companyId: number;
  companyName: string;
}

interface CompanyInfoDto {
  companyId: number;
  companyName: string;
}
```

### 4.4 처리자 정보

```typescript
interface ProcessedByDto {
  userId: number;
  name: string;
}
```

### 4.5 도메인 정보

```typescript
interface DomainSimpleDto {
  code: string;   // "ESG", "SAFETY", "COMPLIANCE"
  name: string;   // "ESG 실사", "안전보건", "컴플라이언스"
}

interface DomainInfoDto {
  domainId: number;
  code: string;
  name: string;
  description?: string;
}

interface DomainResponse {
  id: number;
  code: string;
  name: string;
  description?: string;
  active: boolean;
}
```

### 4.6 진단 정보 (도메인 포함)

```typescript
interface DiagnosticSimpleDto {
  diagnosticId: number;
  diagnosticCode: string;
  title: string;
  domain?: DomainSimpleDto;
}

interface DiagnosticCreateRequest {
  title: string;
  campaignId: number;
  domainCode: string;         // 필수: ESG, SAFETY, COMPLIANCE
  periodStartDate: string;    // "2026-01-01"
  periodEndDate: string;
  deadline?: string;
}
```

### 4.7 캠페인 정보

```typescript
interface CampaignSimpleDto {
  campaignId: number;
  campaignName: string;
  year: number;
}

interface CampaignDetailResponse {
  campaignId: number;
  campaignName: string;
  year: number;
  startDate: string;
  endDate: string;
  description?: string;
}
```

---

## 5. 날짜/시간 형식 (DateTime Formats)

### 5.1 API 응답 형식

| 필드명 패턴 | 형식 | 예시 |
|------------|------|------|
| `*At`, `*Date` | ISO 8601 (초 단위) | `2026-01-20T10:30:00` |
| `*AtLabel` | 한국식 날짜 | `26.01.20` |

### 5.2 프론트엔드 변환

```typescript
// ISO 8601 → 표시용
const formatDate = (isoString: string): string => {
  const date = new Date(isoString);
  return date.toLocaleDateString('ko-KR', {
    year: '2-digit',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\./g, '.').replace(/ /g, '');
};

// "2026-01-20T10:30:00" → "26.01.20"
```

---

## 6. ID 필드 네이밍 규칙 (ID Naming Convention)

| 도메인 | 필드명 | 타입 |
|--------|--------|------|
| 사용자 | `userId` | number |
| 회사 | `companyId` | number |
| 서비스 도메인 | `domainId` | number |
| 도메인 코드 | `domainCode` | string |
| 권한 요청 | `accessRequestId` | number |
| 역할 | `roleId` | number |
| 진단 | `diagnosticId` | number |
| 증빙파일 | `evidenceId` / `fileId` | number |
| 캠페인 | `campaignId` | number |
| 결재 | `approvalId` | number |
| 심사 | `reviewId` | number |
| 알림 | `notificationId` | number |

---

## 7. API 버전 관리

### 7.1 URL 구조

```
/api/v1/{domain}/{resource}
```

### 7.2 현재 버전

- **Current**: `v1`
- **Base URL**: `/api/v1`

---

## 8. AI Run API DTO 구조

### 8.1 AI Run Preview Request/Response

```typescript
// Preview 요청 - 파일 추가 시 슬롯 추정
interface AiPreviewRequest {
  fileIds: number[];  // 추정할 파일 ID 목록
}

interface RunPreviewResponse {
  packageId: string;           // 패키지 식별자
  requiredSlotStatus: SlotStatus[];  // 필수 슬롯 상태
  slotHints: SlotHint[];       // 파일-슬롯 매핑 추정
  missingRequiredSlots: string[];    // 미제출 필수 슬롯 목록
}
```

### 8.2 AI Run Submit Response (고정 스키마)

```typescript
interface RunSubmitResponse {
  packageId: string;           // 패키지 식별자
  verdict: 'PASS' | 'WARN' | 'NEED_CLARIFY' | 'NEED_FIX';
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  why: string;                 // 판정 사유
  slotResults: SlotResult[];   // 슬롯별 검증 결과
  clarifications: Clarification[];  // 보완 요청
  extras: Record<string, any>; // 도메인별 추가 데이터
}
```

### 8.3 공통 DTO

```typescript
interface FileInfo {
  fileId: string;
  url: string;       // SAS URL
  fileName: string;
}

interface SlotHint {
  fileId: string;
  slotName: string;  // 예: "esg.energy.usage", "safety.tbm"
  confidence?: number;
}

interface SlotStatus {
  slotName: string;
  required: boolean;
  submitted: boolean;
}

interface SlotResult {
  slotName: string;
  status: 'VALID' | 'INVALID' | 'MISSING';
  message?: string;
  extractedData?: Record<string, any>;
}

interface Clarification {
  targetSlot: string;
  code: string;
  message: string;  // 보완 요청 메시지
}
```

### 8.4 AI 분석 결과 응답

```typescript
interface AiAnalysisResultResponse {
  resultId: number;
  diagnosticId: number;
  domainCode: string;
  packageId: string;
  riskLevel: string;
  verdict: string;
  whySummary: string;
  resultJson: string;  // 전체 AI 응답 JSON
  analyzedAt: string;  // ISO 8601
}
```

### 8.5 AI 분석 결과 상세 응답

> `/result/detail` 엔드포인트 - resultJson을 파싱하여 구조화된 데이터 제공

```typescript
interface AiAnalysisResultDetailResponse {
  id: number;
  diagnosticId: number;
  domainCode: string;
  packageId: string;
  riskLevel: string;
  verdict: string;
  whySummary: string;
  slotResults: SlotResultDetail[];    // 슬롯별 검증 결과
  clarifications: ClarificationDetail[];  // 보완 요청 목록
  extras: Record<string, any>;        // 도메인별 추가 데이터
  analyzedAt: string;
}

interface SlotResultDetail {
  slotName: string;
  verdict: string;
  reasons: string[];
  fileIds: string[];
  fileNames: string[];
}

interface ClarificationDetail {
  slotName: string;
  message: string;
  fileIds: string[];
}
```

### 8.6 도메인별 슬롯 정의

AI 서비스가 관리하는 전체 슬롯 목록입니다. 백엔드는 파일명 기반 1차 추정만 수행하고, AI 서비스가 전체 슬롯을 기준으로 검증합니다.

#### ESG 도메인 (15개 슬롯, 필수 4개)

| 슬롯명 | 필수 | 설명 |
|--------|:---:|------|
| `esg.energy.electricity.usage` | O | 전기 사용량 (XLSX) |
| `esg.energy.electricity.bill` | | 전기 고지서 (PDF) |
| `esg.energy.gas.usage` | O | 도시가스 사용량 (XLSX) |
| `esg.energy.gas.bill` | | 도시가스 고지서 (PDF) |
| `esg.energy.water.usage` | | 수도 사용량 (XLSX) |
| `esg.energy.water.bill` | | 수도 고지서 (PDF) |
| `esg.energy.ghg.evidence` | | GHG 산정 근거 |
| `esg.hazmat.msds` | O | MSDS/SDS 문서 |
| `esg.hazmat.inventory` | | 유해물질 목록 (XLSX) |
| `esg.hazmat.disposal.list` | | 폐기/처리 목록 (XLSX) |
| `esg.hazmat.disposal.evidence` | | 폐기/처리 증빙 (PDF) |
| `esg.ethics.code` | O | 윤리강령/행동강령 (PDF) |
| `esg.ethics.distribution.log` | | 배포/수신확인 로그 (XLSX) |
| `esg.ethics.pledge` | | 서약서 (PDF) |
| `esg.ethics.poster.image` | | 윤리 포스터/사진 (이미지) |

#### Safety 도메인 (8개 슬롯, 필수 4개)

| 슬롯명 | 필수 | 설명 |
|--------|:---:|------|
| `safety.education.status` | O | 교육 이수 현황 (XLSX) |
| `safety.fire.inspection` | O | 소방 점검표 (PDF/XLSX) |
| `safety.risk.assessment` | O | 위험성평가서 (XLSX/PDF) |
| `safety.management.system` | O | 안전보건관리체계 매뉴얼 (PDF) |
| `safety.site.photos` | | 현장 사진 (이미지) |
| `safety.education.attendance` | | 교육 출석부 (스캔 PDF) |
| `safety.education.photo` | | 교육일 사진 (이미지) |
| `safety.tbm` | | TBM 자료 |

#### Compliance 도메인 (7개 슬롯, 필수 3개)

| 슬롯명 | 필수 | 설명 |
|--------|:---:|------|
| `compliance.contract.sample` | O | 표준 근로/하도급 계약서 |
| `compliance.education.privacy` | O | 개인정보보호 교육 이수 현황 |
| `compliance.fair.trade` | O | 공정거래 자율 점검표 |
| `compliance.ethics.report` | | 윤리경영 내부신고 현황 |
| `compliance.education.plan` | | 법정의무 교육 계획서 |
| `compliance.education.attendance` | | 교육 출석부 (스캔 PDF) |
| `compliance.education.photo` | | 교육일 사진 (이미지) |

### 8.7 result/history 엔드포인트 아키텍처

> `result`와 `history` 엔드포인트는 **백엔드 DB 조회**입니다. AI 서비스로 프록시되지 않습니다.

```
submit 호출 → AI 서비스 /run/submit → 응답을 ai_analysis_result 테이블에 저장
result 조회 → DB에서 최신 결과 반환 (AI 서비스 호출 없음)
history 조회 → DB에서 전체 이력 반환 (AI 서비스 호출 없음)
```

---

## 9. NULL 처리 규칙

### 9.1 응답에서 NULL 필드

- `@JsonInclude(JsonInclude.Include.NON_NULL)` 적용
- NULL 값은 응답에서 **제외됨**

### 9.2 프론트엔드 처리

```typescript
// Optional chaining 사용
const companyName = response.data?.company?.companyName ?? '미지정';
```
