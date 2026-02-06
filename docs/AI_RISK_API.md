# 외부 리스크 감지 API - 프론트엔드 연동 가이드

> 최종 업데이트: 2026-02-05
> PR: #183 / Commit: fbc2aa8
> 담당: REVIEWER(수신자) 전용 기능

## 1. 개요

협력사에 대한 외부 리스크(뉴스, 제재이력, 규제 위반 등)를 AI로 분석하는 API입니다.

### 아키텍처

```
┌─────────────┐     ┌─────────────┐     ┌──────────────────┐
│   Frontend  │────▶│   Backend   │────▶│  AI Risk Server  │
│  (React)    │     │ (Spring Boot)│     │  (FastAPI)       │
└─────────────┘     └─────────────┘     └──────────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  PostgreSQL │
                    │  (결과 저장) │
                    └─────────────┘
```

### 권한

| 역할 | 접근 가능 여부 |
|------|---------------|
| GUEST | X |
| DRAFTER (기안자) | X |
| APPROVER (결재자) | X |
| **REVIEWER (수신자)** | **O** |

> REVIEWER가 아닌 사용자에게는 해당 메뉴/버튼을 숨기거나 비활성화 처리해주세요.

---

## 2. API 엔드포인트

**Base URL**: `/api/v1/risk/external`
**인증**: 모든 요청에 `Authorization: Bearer <JWT토큰>` 헤더 필수

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/risk/external/detect` | 리스크 분석 요청 |
| GET | `/api/v1/risk/external/results/{companyId}` | 특정 회사 최신 결과 조회 |
| GET | `/api/v1/risk/external/results?page=0&size=10` | 전체 결과 이력 조회 |

---

## 3. 상세 스펙

### 3.1 리스크 분석 요청

```
POST /api/v1/risk/external/detect
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "companyIds": [1, 2, 3]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `companyIds` | `Long[]` | O | 분석 대상 회사 ID 배열 (1개 이상) |

**Response (200 OK):**

```json
{
  "success": true,
  "message": "외부 위험 감지 완료",
  "data": [
    {
      "id": 1,
      "companyId": 1,
      "companyName": "협력사A",
      "riskLevel": "HIGH",
      "summary": "최근 환경 규제 위반 이력 발견",
      "evidenceJson": "[{\"source\":\"뉴스\",\"title\":\"협력사A 환경규제 위반\",\"snippet\":\"...\",\"url\":\"https://...\",\"date\":\"2026-01-15\"}]",
      "detectedAt": "2026-02-05T15:30:00"
    }
  ],
  "timestamp": "2026-02-05T15:30:00"
}
```

> **주의**: AI 서버 호출이 포함되어 응답까지 **최대 60초** 소요될 수 있습니다. 로딩 UI를 반드시 적용해주세요.

---

### 3.2 특정 회사 최신 결과 조회

```
GET /api/v1/risk/external/results/{companyId}
Authorization: Bearer <token>
```

| 파라미터 | 위치 | 타입 | 설명 |
|---------|------|------|------|
| `companyId` | Path | `Long` | 회사 ID |

**Response (200 OK):**

```json
{
  "success": true,
  "message": "리스크 결과 조회 완료",
  "data": {
    "id": 1,
    "companyId": 1,
    "companyName": "협력사A",
    "riskLevel": "MEDIUM",
    "summary": "경미한 리스크 요소 발견",
    "evidenceJson": "[...]",
    "detectedAt": "2026-02-05T15:30:00"
  },
  "timestamp": "2026-02-05T15:30:00"
}
```

---

### 3.3 전체 결과 이력 조회 (페이지네이션)

```
GET /api/v1/risk/external/results?page=0&size=10
Authorization: Bearer <token>
```

| 파라미터 | 위치 | 타입 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `page` | Query | `int` | `0` | 페이지 번호 (0-based) |
| `size` | Query | `int` | `10` | 페이지당 항목 수 |

**Response (200 OK):**

```json
{
  "success": true,
  "message": "리스크 결과 이력 조회 완료",
  "data": {
    "content": [
      {
        "id": 1,
        "companyId": 1,
        "companyName": "협력사A",
        "riskLevel": "HIGH",
        "summary": "환경 규제 위반 이력 발견",
        "evidenceJson": "[...]",
        "detectedAt": "2026-02-05T15:30:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 25,
      "totalPages": 3
    }
  },
  "timestamp": "2026-02-05T15:30:00"
}
```

> 기존 `diagnostics`, `approvals` API와 동일한 `PagedResponse` 형식입니다.

---

## 4. 응답 필드 상세

### 4.1 결과 객체 (ExternalRiskResultResponse)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `Long` | 결과 고유 ID |
| `companyId` | `Long` | 회사 ID |
| `companyName` | `String` | 회사명 |
| `riskLevel` | `String` | 리스크 등급: `LOW`, `MEDIUM`, `HIGH` |
| `summary` | `String` | AI가 생성한 리스크 요약 |
| `evidenceJson` | `String` | 근거 자료 JSON 문자열 (**파싱 필요**) |
| `detectedAt` | `String` | 분석 시각 (ISO 8601) |

### 4.2 riskLevel 값 정의

| 값 | 의미 | 권장 색상 |
|----|------|----------|
| `LOW` | 낮은 리스크 | 🟢 초록 (`#22C55E`) |
| `MEDIUM` | 중간 리스크 | 🟡 주황 (`#F59E0B`) |
| `HIGH` | 높은 리스크 | 🔴 빨강 (`#EF4444`) |

### 4.3 evidenceJson 파싱

`evidenceJson`은 **JSON 문자열**로 전달됩니다. 프론트에서 파싱이 필요합니다.

```typescript
// 파싱 예시
interface Evidence {
  source: string;   // 출처 유형 (예: "뉴스", "공시")
  title: string;    // 제목
  snippet: string;  // 관련 내용 발췌
  url: string;      // 원문 URL
  date: string;     // 날짜
}

const evidenceList: Evidence[] = JSON.parse(result.evidenceJson);
```

**파싱 후 구조 예시:**

```json
[
  {
    "source": "뉴스",
    "title": "협력사A, 환경규제 위반으로 과징금 부과",
    "snippet": "환경부는 협력사A에 대해 대기오염물질 배출 기준 초과로...",
    "url": "https://news.example.com/article/12345",
    "date": "2026-01-15"
  },
  {
    "source": "공시",
    "title": "행정처분 공시",
    "snippet": "사업정지 15일 처분...",
    "url": "https://dart.example.com/report/67890",
    "date": "2026-01-20"
  }
]
```

---

## 5. 에러 응답

### 공통 에러 형식

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null,
  "timestamp": "2026-02-05T15:30:00"
}
```

### 에러 코드

| 코드 | HTTP Status | 메시지 | 발생 상황 | 프론트 처리 |
|------|-------------|--------|----------|------------|
| `RISK001` | 404 | 리스크 분석 대상 회사를 찾을 수 없습니다 | 존재하지 않는 companyId 전달 | "회사 정보를 찾을 수 없습니다" 안내 |
| `RISK002` | 500 | 외부 위험 감지 API 호출에 실패했습니다 | AI 서버 장애 또는 타임아웃 | "잠시 후 다시 시도해주세요" 안내 |
| `RISK003` | 404 | 리스크 분석 결과를 찾을 수 없습니다 | 해당 회사 분석 이력 없음 | "아직 분석된 결과가 없습니다" 안내 |
| `RISK004` | 403 | REVIEWER만 리스크 분석을 요청할 수 있습니다 | 권한 없는 사용자 접근 | 접근 차단 또는 권한 안내 |
| `U001` | 400 | 입력값이 올바르지 않습니다 | companyIds 빈 배열 전달 | 입력값 검증 |

---

## 6. 프론트엔드 구현 체크리스트

### 필수 사항

- [ ] REVIEWER 역할 확인 후 메뉴/버튼 노출 제어
- [ ] `detect` 요청 시 로딩 UI 적용 (최대 60초 소요 가능)
- [ ] `evidenceJson` 필드 `JSON.parse()` 처리
- [ ] 에러 코드별 사용자 안내 메시지 분기 처리
- [ ] 페이지네이션 `page` 파라미터 0-based 처리

### 권장 사항

- [ ] riskLevel별 색상 배지(Badge) 컴포넌트
- [ ] evidence 목록에서 URL 클릭 시 새 탭으로 열기 (`target="_blank"`)
- [ ] 분석 요청 전 확인 모달 (여러 회사 동시 분석 시)
- [ ] 결과 없는 회사에 대해 "분석 요청" 버튼 노출

---

## 7. 연동 예시 (React/TypeScript)

### API 호출

```typescript
// 리스크 분석 요청
const detectRisk = async (companyIds: number[]) => {
  const response = await api.post('/api/v1/risk/external/detect', {
    companyIds,
  });
  return response.data;
};

// 특정 회사 최신 결과 조회
const getLatestResult = async (companyId: number) => {
  const response = await api.get(`/api/v1/risk/external/results/${companyId}`);
  return response.data;
};

// 전체 결과 이력 조회
const getAllResults = async (page = 0, size = 10) => {
  const response = await api.get('/api/v1/risk/external/results', {
    params: { page, size },
  });
  return response.data;
};
```

### 타입 정의

```typescript
interface ExternalRiskResult {
  id: number;
  companyId: number;
  companyName: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  summary: string;
  evidenceJson: string;
  detectedAt: string;
}

interface Evidence {
  source: string;
  title: string;
  snippet: string;
  url: string;
  date: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

interface PagedData<T> {
  content: T[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}
```

---

## 8. 테스트 방법

Swagger UI에서 직접 테스트 가능: `http://localhost:8080/swagger-ui.html`

### 테스트 시나리오

| 시나리오 | 방법 | 기대 결과 |
|---------|------|----------|
| 정상 분석 | REVIEWER 토큰으로 `POST /detect` | 200 + 결과 배열 |
| 권한 없음 | DRAFTER 토큰으로 `POST /detect` | 403 RISK004 |
| 없는 회사 | 존재하지 않는 companyId 전달 | 404 RISK001 |
| 결과 없음 | 분석 이력 없는 회사 결과 조회 | 404 RISK003 |
| 빈 배열 | `companyIds: []` 전달 | 400 U001 |
