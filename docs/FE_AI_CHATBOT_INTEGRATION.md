# AI Chatbot 프론트엔드 통합 가이드

> 작성일: 2026-02-04
> 관련 이슈: #140, #141

## 개요

AI 기반 RAG 챗봇 API가 백엔드에 통합되었습니다. 이 문서는 프론트엔드에서 API를 연동하기 위한 가이드입니다.

---

## API 엔드포인트

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/chat` | AI 채팅 | DRAFTER, APPROVER, REVIEWER |
| POST | `/api/v1/admin/chat/sync` | Vector DB 동기화 | REVIEWER |
| GET | `/api/v1/admin/chat/inspect` | DB 현황 조회 | REVIEWER |

---

## 1. 채팅 API

사용자의 질문에 대해 RAG 기반으로 답변을 생성합니다.

### Request

```http
POST /api/v1/chat
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "message": "하도급법 위반 시 벌점은?",
  "history": [
    {"role": "user", "content": "이전 질문"},
    {"role": "assistant", "content": "이전 답변"}
  ],
  "domain": "compliance",
  "docName": null,
  "topK": 8,
  "sessionId": null
}
```

### Request 필드

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `message` | string | **Yes** | - | 사용자의 질문 내용 |
| `history` | array | No | `[]` | 이전 대화 기록 (멀티턴 문맥 파악용) |
| `domain` | string | No | `"all"` | 검색 영역 필터: `safety`, `compliance`, `esg`, `all` |
| `docName` | string | No | `null` | 특정 문서 내 검색 시 파일명 |
| `topK` | integer | No | `8` | 검색할 문서 개수 (1~30) |
| `sessionId` | string | No | `null` | 세션 식별자 (null이면 서버에서 자동 생성) |

### Response

```json
{
  "success": true,
  "message": "채팅 완료",
  "data": {
    "answer": "하도급법 위반 시 벌점은 위반 사유에 따라 다르며, 최대 5점까지 부과될 수 있습니다.",
    "confidence": "high",
    "notes": null,
    "sources": [
      {
        "title": "하도급가이드.pdf",
        "type": "manual",
        "snippet": "벌점 부과 기준은 다음과 같다...",
        "score": 0.89,
        "loc": {
          "page": 15,
          "lineStart": null
        }
      }
    ]
  },
  "timestamp": "2026-02-04T13:40:00"
}
```

### Response 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `answer` | string | AI가 생성한 최종 답변 |
| `confidence` | string | 답변 신뢰도: `high`, `medium`, `low` |
| `notes` | string | 비고 (예: "근거 자료 없음") |
| `sources` | array | 답변에 사용된 근거 자료 목록 |

### Source 객체

| 필드 | 타입 | 설명 |
|------|------|------|
| `title` | string | 문서 제목 또는 파일명 |
| `type` | string | 자료 유형: `manual`, `code`, `law` |
| `snippet` | string | 실제 참고한 본문 내용 (일부) |
| `score` | number | 검색 유사도 점수 (0.0 ~ 1.0) |
| `loc.page` | integer | 페이지 번호 |
| `loc.lineStart` | integer | 시작 줄 번호 |

---

## 2. Admin 동기화 API (REVIEWER 전용)

PDF 파일과 소스 코드를 파싱하여 Vector DB에 적재합니다. 비동기로 실행됩니다.

### Request

```http
POST /api/v1/admin/chat/sync
Authorization: Bearer {token}
```

### Response

```json
{
  "success": true,
  "message": "동기화 요청이 접수되었습니다",
  "data": {
    "status": "accepted",
    "message": "동기화 작업이 백그라운드에서 시작되었습니다."
  },
  "timestamp": "2026-02-04T13:40:00"
}
```

---

## 3. Admin DB 현황 조회 API (REVIEWER 전용)

현재 Vector DB에 저장된 문서 개수와 샘플 데이터를 확인합니다.

### Request

```http
GET /api/v1/admin/chat/inspect
Authorization: Bearer {token}
```

### Response

```json
{
  "success": true,
  "message": "DB 현황 조회 완료",
  "data": {
    "totalDocuments": 1542,
    "samples": [
      "[manual] 안전작업표준.pdf (ID: manual:안전작업표준.pdf:p1:c0...)",
      "[code] validators.py (ID: code:validators.py:L10-L50...)"
    ]
  },
  "timestamp": "2026-02-04T13:40:00"
}
```

---

## 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `CHAT001` | 500 | AI 채팅 서비스 오류가 발생했습니다 |
| `CHAT002` | 504 | AI 채팅 서비스 응답 시간이 초과되었습니다 |
| `CHAT003` | 400 | 유효하지 않은 도메인입니다 |
| `PERM_002` | 403 | 해당 작업을 수행할 권한이 없습니다 |
| `A001` | 401 | 유효하지 않은 토큰입니다 |

### 에러 응답 예시

```json
{
  "success": false,
  "code": "CHAT003",
  "message": "유효하지 않은 도메인입니다",
  "timestamp": "2026-02-04T13:40:00"
}
```

---

## UI 구현 가이드

### 1. 채팅 UI

- **메시지 입력**: 텍스트 입력 필드 + 전송 버튼
- **도메인 선택**: 드롭다운 (safety, compliance, esg, all)
- **히스토리 관리**: 클라이언트에서 대화 기록 유지 후 `history` 파라미터로 전달
- **세션 관리**: 첫 응답에서 받은 sessionId를 이후 요청에 재사용 (또는 서버 자동 생성)

### 2. 답변 표시

- **답변 텍스트**: `data.answer` 표시
- **신뢰도 뱃지**: `data.confidence`에 따라 색상 구분
  - `high`: 녹색
  - `medium`: 노란색
  - `low`: 빨간색
- **출처 표시**: `data.sources` 배열을 접이식 패널로 표시
  - 문서명, 유형, 페이지 번호, 유사도 점수

### 3. 로딩 상태

- AI 응답은 최대 30초까지 소요될 수 있음
- 로딩 인디케이터 또는 타이핑 애니메이션 권장

### 4. Admin 페이지 (REVIEWER 전용)

- **동기화 버튼**: "Vector DB 동기화" 버튼 클릭 시 `/admin/chat/sync` 호출
- **현황 조회**: 총 문서 수, 샘플 목록 표시

---

## TypeScript 타입 정의 (참고용)

```typescript
// Request
interface ChatRequest {
  message: string;
  history?: ChatMessage[];
  domain?: 'safety' | 'compliance' | 'esg' | 'all';
  docName?: string;
  topK?: number;
  sessionId?: string;
}

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

// Response
interface ChatResponse {
  answer: string;
  confidence: 'high' | 'medium' | 'low';
  notes: string | null;
  sources: SourceItem[];
}

interface SourceItem {
  title: string;
  type: 'manual' | 'code' | 'law';
  snippet: string;
  score: number;
  loc: {
    page: number | null;
    lineStart: number | null;
  };
}

interface AdminSyncResponse {
  status: string;
  message: string;
}

interface AdminInspectResponse {
  totalDocuments: number;
  samples: string[];
}
```

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-02-04 | 1.0 | 최초 작성 |
