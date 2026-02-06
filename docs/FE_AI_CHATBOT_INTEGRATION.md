# AI Chatbot 프론트엔드 통합 가이드

> 작성일: 2026-02-04 | 수정일: 2026-02-05
> 관련 이슈: #140, #141, #186, #187, #188

## 개요

AI 기반 RAG 챗봇 API가 백엔드에 통합되었습니다. 이 문서는 프론트엔드에서 API를 연동하기 위한 가이드입니다.

---

## v1.1 변경사항 (2026-02-05)

| 항목 | 변경 내용 |
|------|----------|
| `file_id` 필드 추가 | 증빙파일 ID를 보내면 AI가 해당 문서를 분석하여 답변 생성 |
| `file_url` 필드 | **FE는 사용하지 않음** (BE가 내부적으로 Python에 전달하는 용도) |
| 권한 검증 강화 | 다른 회사의 파일 접근 시 403 에러 |

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

#### 기본 채팅 (텍스트만)

```json
{
  "message": "하도급법 위반 시 벌점은?",
  "domain": "compliance"
}
```

#### 파일 분석 채팅 (v1.1 신규)

```json
{
  "message": "이 문서의 핵심 내용을 요약해줘",
  "file_id": 42,
  "domain": "esg"
}
```

#### 멀티턴 대화

```json
{
  "message": "좀 더 자세히 설명해줘",
  "history": [
    {"role": "user", "content": "하도급법이 뭐야?"},
    {"role": "assistant", "content": "하도급 거래 공정화에 관한 법률입니다..."}
  ],
  "domain": "compliance",
  "session_id": "이전-응답에서-받은-세션ID"
}
```

### Request 필드

| 필드 | JSON key | 타입 | 필수 | 기본값 | 설명 |
|------|----------|------|------|--------|------|
| message | `message` | string | **Yes** | - | 사용자의 질문 내용 |
| fileId | `file_id` | number | No | `null` | **[v1.1]** 분석할 증빙파일 ID (`EvidenceFile.resultFileId`) |
| history | `history` | array | No | `[]` | 이전 대화 기록 (멀티턴 문맥 파악용) |
| domain | `domain` | string | No | `"all"` | 검색 영역 필터: `safety`, `compliance`, `esg`, `all` |
| docName | `doc_name` | string | No | `null` | 특정 문서 내 검색 시 파일명 |
| topK | `top_k` | integer | No | `8` | 검색할 문서 개수 (1~30) |
| sessionId | `session_id` | string | No | `null` | 세션 식별자 (null이면 서버에서 자동 생성) |

> **참고**: `file_url` 필드는 BE 내부 전용입니다. FE에서 보내지 마세요. `file_id`만 보내면 BE가 presigned URL을 생성하여 AI 서비스에 전달합니다.

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
  "timestamp": "2026-02-05T13:40:00"
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
  "timestamp": "2026-02-05T13:40:00"
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
  "timestamp": "2026-02-05T13:40:00"
}
```

---

## 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `CHAT001` | 500 | AI 채팅 서비스 오류가 발생했습니다 |
| `CHAT002` | 504 | AI 채팅 서비스 응답 시간이 초과되었습니다 |
| `CHAT003` | 400 | 유효하지 않은 도메인입니다 |
| `FILE_001` | 404 | **[v1.1]** 파일을 찾을 수 없습니다 (`file_id`가 잘못된 경우) |
| `PERM_002` | 403 | 해당 작업을 수행할 권한이 없습니다 (다른 회사 파일 접근 포함) |
| `A001` | 401 | 유효하지 않은 토큰입니다 |

### 에러 응답 예시

```json
{
  "success": false,
  "code": "FILE_001",
  "message": "파일을 찾을 수 없습니다",
  "timestamp": "2026-02-05T13:40:00"
}
```

---

## 파일 분석 연동 가이드 (v1.1)

### 사용 시나리오

1. 사용자가 진단 상세 화면에서 증빙파일을 보고 있음
2. 챗봇을 열고 "이 문서 요약해줘" + 파일 선택
3. FE는 해당 파일의 `resultFileId`를 `file_id`로 전달
4. AI가 문서를 분석하여 답변 생성

### file_id 얻는 방법

증빙파일 목록 API 응답에서 `resultFileId` 사용:

```
GET /api/v1/diagnostics/{id}/files
→ response.data[].resultFileId  ← 이 값을 file_id로 사용
```

### 권한 규칙

- 자기 회사의 파일만 분석 가능
- 다른 회사 파일 → `403 PERM_002`
- 존재하지 않는 file_id → `404 FILE_001`

### UI 구현 포인트

- 챗봇 입력창 옆에 **파일 첨부 버튼** (클립 아이콘 등)
- 클릭 시 현재 진단의 증빙파일 목록에서 선택
- 선택된 파일명을 입력창 위에 칩/태그로 표시
- 파일 없이도 일반 채팅은 정상 동작

---

## UI 구현 가이드

### 1. 채팅 UI

- **메시지 입력**: 텍스트 입력 필드 + 전송 버튼
- **파일 첨부**: 파일 선택 버튼 (optional)
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
- 파일 분석 시 더 오래 걸릴 수 있음
- 로딩 인디케이터 또는 타이핑 애니메이션 권장

### 4. Admin 페이지 (REVIEWER 전용)

- **동기화 버튼**: "Vector DB 동기화" 버튼 클릭 시 `/admin/chat/sync` 호출
- **현황 조회**: 총 문서 수, 샘플 목록 표시

---

## TypeScript 타입 정의

```typescript
// Request
interface ChatRequest {
  message: string;
  file_id?: number;         // [v1.1] 분석할 증빙파일 ID
  history?: ChatMessage[];
  domain?: 'safety' | 'compliance' | 'esg' | 'all';
  doc_name?: string;
  top_k?: number;
  session_id?: string;
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
| 2026-02-05 | 1.1 | `file_id` 파일 분석 기능 추가, 에러코드 추가, TS 타입 업데이트 |
