# AI Run API 연동 가이드

> 최종 테스트: 2026-02-02
> 백엔드 ↔ AI 서버 연동 검증 완료

## 1. 개요

AI Run API는 협력사 자료(PDF/이미지)를 도메인(ESG/Safety/Compliance)별로 자동 검증하는 공통 엔진입니다.

### 아키텍처

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │────▶│   Backend   │────▶│  AI Server  │
│  (React)    │     │ (Spring Boot)│     │  (FastAPI)  │
└─────────────┘     └─────────────┘     └─────────────┘
                           │                    │
                           ▼                    ▼
                    ┌─────────────┐     ┌─────────────┐
                    │  PostgreSQL │     │ File Storage│
                    └─────────────┘     └─────────────┘
```

### 서버 정보

| 환경 | AI Server URL | Backend URL |
|------|---------------|-------------|
| Local | `http://localhost:8000` | `http://localhost:8080` |
| Dev | 환경변수 참조 | Azure 배포 |

---

## 2. API 엔드포인트

### 2.1 AI Server (직접 호출)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/health` | 헬스 체크 |
| POST | `/run/preview` | 슬롯 추정 (파일 업로드 시) |
| POST | `/run/submit` | 전체 검증 및 판정 |

### 2.2 Backend API (프론트엔드용)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/ai/run/diagnostics/{id}/preview` | 슬롯 추정 |
| POST | `/api/v1/ai/run/diagnostics/{id}/submit` | AI 검증 요청 (비동기) |
| GET | `/api/v1/ai/run/diagnostics/{id}/result` | 최신 결과 조회 |
| GET | `/api/v1/ai/run/diagnostics/{id}/result/detail` | 결과 상세 (슬롯별 파싱) |
| GET | `/api/v1/ai/run/diagnostics/{id}/history` | 분석 이력 조회 |

---

## 3. 요청/응답 스키마

### 3.1 Preview API

#### 요청 (AI Server)
```json
{
  "domain": "safety",           // "esg" | "safety" | "compliance"
  "period_start": "2024-01-01",
  "period_end": "2024-12-31",
  "package_id": null,           // 첫 호출 시 null, 이후 재사용
  "added_files": [
    {
      "file_id": "1",
      "storage_uri": "https://storage.example.com/file.pdf",
      "file_name": "안전교육이수현황.pdf"
    }
  ]
}
```

#### 요청 (Backend - 프론트엔드용)
```json
POST /api/v1/ai/run/diagnostics/1/preview
{
  "fileIds": [1, 2, 3]
}
```

#### 응답
```json
{
  "package_id": "PKG_9122CA32E1E8",
  "slot_hint": [
    {
      "file_id": "1",
      "slot_name": "safety.education.status",
      "confidence": 0.95,
      "match_reason": "filename_keyword"
    }
  ],
  "required_slot_status": [
    { "slot_name": "safety.education.status", "status": "SUBMITTED" },
    { "slot_name": "safety.fire.inspection", "status": "MISSING" }
  ],
  "missing_required_slots": ["safety.fire.inspection", "safety.risk.assessment"]
}
```

### 3.2 Submit API

#### 요청 (AI Server)
```json
{
  "package_id": "PKG_9122CA32E1E8",
  "domain": "safety",
  "period_start": "2024-01-01",
  "period_end": "2024-12-31",
  "files": [
    {
      "file_id": "1",
      "storage_uri": "https://storage.example.com/file.pdf",
      "file_name": "소방점검결과.pdf"
    }
  ],
  "slot_hint": [
    {
      "file_id": "1",
      "slot_name": "safety.fire.inspection",
      "confidence": 0.9
    }
  ]
}
```

#### 요청 (Backend - 프론트엔드용)
```json
POST /api/v1/ai/run/diagnostics/1/submit
{}  // Body 없음 - 백엔드에서 자동으로 파일 목록 조회
```

#### 응답
```json
{
  "package_id": "PKG_TEST_002",
  "risk_level": "HIGH",           // "LOW" | "MEDIUM" | "HIGH"
  "verdict": "NEED_FIX",          // "PASS" | "NEED_FIX" | "NEED_CLARIFY"
  "why": "필수 항목이 부족하거나 확인이 어려운 파일이 있습니다.",
  "slot_results": [
    {
      "slot_name": "safety.fire.inspection",
      "verdict": "NEED_CLARIFY",
      "reasons": ["DATE_MISMATCH", "FIRE_COPYPASTE_PATTERN"],
      "file_ids": ["1"],
      "file_names": ["fire_inspection.pdf"],
      "extras": {}
    },
    {
      "slot_name": "safety.management.system",
      "verdict": "PASS",
      "reasons": [],
      "file_ids": ["2"],
      "file_names": ["safety_management.pdf"],
      "extras": {}
    }
  ],
  "clarifications": [
    {
      "slot_name": "safety.fire.inspection",
      "message": "파일의 날짜가 일치하지 않습니다. 확인 후 재제출해 주세요.",
      "file_ids": ["1"]
    }
  ],
  "extras": {}
}
```

---

## 4. 도메인별 슬롯 정의

### 4.1 ESG 도메인 (15개 슬롯, 필수 4개)

| 슬롯 | 필수 | 키워드 |
|------|------|--------|
| `esg.energy.electricity.usage` | ✅ | 전기, electricity, 전력사용, kwh |
| `esg.energy.electricity.bill` | | 전기고지서, 전기요금 |
| `esg.energy.gas.usage` | ✅ | 가스, gas, 도시가스 |
| `esg.energy.gas.bill` | | 가스고지서, 가스요금 |
| `esg.energy.water.usage` | | 수도, water, 용수 |
| `esg.hazmat.msds` | ✅ | msds, sds, 물질안전, 유해물질 |
| `esg.hazmat.inventory` | | 유해물질목록, 화학물질목록 |
| `esg.ethics.code` | ✅ | 윤리강령, ethics, code of conduct |
| `esg.ethics.distribution.log` | | 배포확인, 수신확인 |
| `esg.ethics.pledge` | | 서약서, pledge |

### 4.2 Safety 도메인 (8개 슬롯, 필수 4개)

| 슬롯 | 필수 | 키워드 |
|------|------|--------|
| `safety.education.status` | ✅ | 교육이수, 안전교육, 수료 |
| `safety.fire.inspection` | ✅ | 소방점검, 소화기, 방화 |
| `safety.risk.assessment` | ✅ | 위험성평가, risk assessment |
| `safety.management.system` | ✅ | 안전보건관리, 안전매뉴얼 |
| `safety.site.photos` | | 현장사진, site photo |
| `safety.education.attendance` | | 출석부, attendance |
| `safety.education.photo` | | 교육사진, education photo |
| `safety.tbm` | | tbm, toolbox, 작업전회의 |

### 4.3 Compliance 도메인 (7개 슬롯, 필수 3개)

| 슬롯 | 필수 | 키워드 |
|------|------|--------|
| `compliance.contract.sample` | ✅ | 표준계약, 하도급계약, 근로계약 |
| `compliance.education.privacy` | ✅ | 개인정보교육, privacy education |
| `compliance.fair.trade` | ✅ | 공정거래, fair trade, 자율점검 |
| `compliance.ethics.report` | | 내부신고, ethics report |
| `compliance.education.plan` | | 교육계획, education plan |
| `compliance.education.attendance` | | 출석부, attendance |

---

## 5. 프론트엔드 연동 가이드

### 5.1 파일 업로드 플로우

```typescript
// 1. 파일 업로드
const uploadResponse = await fetch(`/api/v1/diagnostics/${diagnosticId}/files`, {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData  // multipart/form-data
});

// 2. Preview 호출 (슬롯 추정)
const previewResponse = await fetch(`/api/v1/ai/run/diagnostics/${diagnosticId}/preview`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ fileIds: [1, 2, 3] })
});

// 3. 필수 슬롯 현황 표시
const { required_slot_status, missing_required_slots } = previewResponse.data;
```

### 5.2 AI 분석 요청 플로우

```typescript
// 1. Submit 요청 (비동기)
const submitResponse = await fetch(`/api/v1/ai/run/diagnostics/${diagnosticId}/submit`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: '{}'
});
// Response: { status: "PENDING", message: "AI 분석이 시작되었습니다" }

// 2. 결과 폴링 (5초 간격 권장)
const pollResult = async () => {
  const result = await fetch(`/api/v1/ai/run/diagnostics/${diagnosticId}/result`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (result.status === 404) {
    // 아직 분석 중 - 재시도
    setTimeout(pollResult, 5000);
  } else {
    // 결과 수신 완료
    displayResult(result.data);
  }
};
```

### 5.3 결과 화면 구성

```typescript
interface AiAnalysisResult {
  packageId: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  verdict: 'PASS' | 'NEED_FIX' | 'NEED_CLARIFY';
  whySummary: string;
  slotResults: SlotResult[];
  clarifications: Clarification[];
}

// 위험도별 색상
const riskColors = {
  LOW: 'green',
  MEDIUM: 'yellow',
  HIGH: 'red'
};

// 판정별 아이콘
const verdictIcons = {
  PASS: '✅',
  NEED_FIX: '❌',
  NEED_CLARIFY: '⚠️'
};
```

---

## 6. 테스트 방법

### 6.1 AI Server 직접 테스트

```bash
# Health 체크
curl http://localhost:8000/health

# Preview 테스트
curl -X POST http://localhost:8000/run/preview \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "safety",
    "period_start": "2024-01-01",
    "period_end": "2024-12-31",
    "package_id": null,
    "added_files": [{
      "file_id": "1",
      "storage_uri": "https://example.com/test.pdf",
      "file_name": "safety_education.pdf"
    }]
  }'
```

### 6.2 Backend API 테스트

```bash
# 로그인
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test1234!"}' \
  | jq -r '.data.accessToken')

# Preview 호출
curl -X POST "http://localhost:8080/api/v1/ai/run/diagnostics/1/preview" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fileIds": [1, 2, 3]}'

# Submit 호출
curl -X POST "http://localhost:8080/api/v1/ai/run/diagnostics/1/submit" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'

# 결과 조회
curl "http://localhost:8080/api/v1/ai/run/diagnostics/1/result" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 7. 알려진 이슈 및 주의사항

### 7.1 지원 파일 형식

| 형식 | 지원 여부 |
|------|----------|
| PDF | ✅ 지원 |
| PNG/JPG | ✅ 지원 |
| XLSX | ❌ 미지원 (VAL_003 에러) |
| DOCX | ❌ 미지원 |

### 7.2 필수 슬롯 사전 검증

백엔드에서 Submit 호출 전 필수 슬롯 검증을 수행합니다.
- 필수 슬롯이 누락되면 `AI_MISSING_REQUIRED_SLOTS` 에러 반환
- AI Server 호출 없이 사전 차단됨

```java
// 에러 코드
ErrorCode.AI_MISSING_REQUIRED_SLOTS  // AI008
```

### 7.3 파일 접근 권한

- AI Server가 파일을 다운로드하려면 `storage_uri`가 공개 접근 가능해야 함
- 로컬 테스트 시 별도 파일 서버 필요 (`python -m http.server 9000`)
- Azure 환경에서는 Blob Storage SAS URL 사용

### 7.4 타임아웃 설정

| 설정 | 값 | 설명 |
|------|-----|------|
| AI API Timeout | 180초 | 대용량 파일 처리 고려 |
| Max Retry | 3회 | 5xx 에러 시 자동 재시도 |

---

## 8. 에러 코드

| 코드 | 설명 |
|------|------|
| `AI001` | AI 서비스 연결 실패 |
| `AI002` | AI 서비스 오류 (5xx) |
| `AI003` | AI 분석 결과 없음 |
| `AI004` | AI 요청 형식 오류 (4xx) |
| `AI005` | 유효하지 않은 verdict 값 |
| `AI006` | 유효하지 않은 riskLevel 값 |
| `AI007` | AI 응답 필수 필드 누락 |
| `AI008` | 필수 슬롯 미제출 |
| `VAL_003` | 지원하지 않는 파일 형식 |

---

## 9. 연동 테스트 결과 (2026-02-02)

### 테스트 환경
- Backend: Spring Boot 3.5 (localhost:8080)
- AI Server: FastAPI (localhost:8000)
- Database: PostgreSQL 15

### 테스트 결과

| 테스트 항목 | 결과 | 비고 |
|------------|------|------|
| AI Server Health | ✅ Pass | `{"status":"ok"}` |
| Preview API (직접) | ✅ Pass | 슬롯 추정 정상 |
| Preview API (백엔드) | ✅ Pass | 파일 ID 기반 조회 |
| Submit API (직접) | ✅ Pass | 판정 결과 정상 반환 |
| Submit API (백엔드) | ⚠️ 조건부 | 필수 슬롯 검증 필요 |
| 결과 저장 | ✅ Pass | DB 저장 확인 |

### Submit 응답 예시

```json
{
  "package_id": "PKG_TEST_002",
  "risk_level": "HIGH",
  "verdict": "NEED_FIX",
  "why": "필수 항목이 부족하거나 확인이 어려운 파일이 있습니다.",
  "slot_results": [
    {
      "slot_name": "safety.fire.inspection",
      "verdict": "NEED_CLARIFY",
      "reasons": ["DATE_MISMATCH", "FIRE_COPYPASTE_PATTERN"]
    },
    {
      "slot_name": "safety.management.system",
      "verdict": "PASS"
    }
  ]
}
```

---

## 10. 관련 파일

### Backend
- `AiRunApiClient.java` - AI API 클라이언트
- `AiAnalysisService.java` - AI 분석 서비스
- `AiAnalysisController.java` - API 컨트롤러
- `application.yaml` - AI 설정 (ai.run-api.*)

### DTO
- `dto/ai/run/RunPreviewRequest.java`
- `dto/ai/run/RunPreviewResponse.java`
- `dto/ai/run/RunSubmitRequest.java`
- `dto/ai/run/RunSubmitResponse.java`
- `dto/ai/run/SlotHint.java`
- `dto/ai/run/SlotResult.java`

---

## 문의

- Backend: 백엔드팀
- AI Server: AI팀
- Frontend: 프론트엔드팀
