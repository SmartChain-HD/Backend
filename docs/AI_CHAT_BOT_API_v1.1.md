# 챗봇(v1.1) - 0205

# **📘 HD HHI AI Advisor - API 명세서 (v1.1)**

## 1. 기본 정보

- **Base URL**: `http://127.0.0.1:8001` (로컬 기준)
- **Content-Type**: `application/json`
- **Auth**: Admin API는 `X-API-KEY` 헤더 필요

---

## 2. 데이터 스키마 (Data Schemas)

### 🟢 요청 객체: `ChatRequest`

채팅 API 호출 시 전송하는 JSON 데이터 구조입니다.

| 필드명 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `message` | `string` | **Yes** | - | 사용자의 질문 내용 |
| `file_url` | `string` | No | `null` | **[New]** 분석할 파일의 S3 URL (Presigned URL).입력 시 해당 문서를 다운로드하여 답변 생성에 참고합니다. |
| `history` | `list[dict]` | No | `[]` | 이전 대화 기록 (멀티턴 문맥 파악용)예: `[{"role": "user", "content": "..."}]` |
| `domain` | `string` | No | `"all"` | 검색 영역 필터 (`safety`, `compliance`, `esg`, `all`) |
| `doc_name` | `string` | No | `null` | Vector DB에 저장된 특정 문서 내 검색 시 파일명(예: `"하도급법_가이드.pdf"`) |
| `top_k` | `integer` | No | `8` | 검색할 문서 개수 (1~30) |
| `session_id` | `string` | No | `null` | 세션 식별자 (로그 분석용) |

### 🔵 응답 객체: `ChatResponse`

채팅 API의 응답 JSON 데이터 구조입니다.

| 필드명 | 타입 | 설명 |
| --- | --- | --- |
| `answer` | `string` | AI가 생성한 최종 답변 |
| `confidence` | `string` | 답변 신뢰도 (`high`, `medium`, `low`) |
| `notes` | `string` | 비고 (예: "근거 자료 없음") |
| `sources` | `list[SourceItem]` | 답변에 사용된 근거 자료 목록 |

### 🟠 근거 자료 객체: `SourceItem`

`ChatResponse.sources` 리스트 안에 들어가는 객체입니다.

| 필드명 | 타입 | 설명 |
| --- | --- | --- |
| `title` | `string` | 문서 제목 또는 파일명 |
| `type` | `string` | 자료 유형 (`manual`, `code`, `law`) |
| `snippet` | `string` | 실제 참고한 본문 내용 (일부) |
| `score` | `float` | 검색 유사도 점수 (0.0 ~ 1.0) |
| `loc` | `object` | 위치 정보 (`page`: 페이지 번호, `line_start`: 시작 줄 등) |

---

## 3. API 엔드포인트 (Endpoints)

### 💬 1. 채팅 (RAG + File Analysis)

사용자의 질문을 받아 답변을 생성합니다. `file_url`이 제공되면 해당 문서를 우선적으로 분석합니다.

- **URL**: `/api/chat`
- **Method**: `POST`
- **Auth**: 없음

**Request Example (기본 채팅):**

```json
{
  "message": "하도급법 위반 시 벌점은?",
  "domain": "compliance",
  "history": [
    {"role": "user", "content": "하도급법이 뭐야?"},
    {"role": "assistant", "content": "하도급 거래 공정화에 관한 법률입니다..."}
  ]
}
```

**Request Example (파일 분석 채팅):**

```json
{
  "message": "이 문서의 핵심 내용을 요약해줘",
  "file_url": "https://s3.ap-northeast-2.amazonaws.com/my-bucket/sample_contract.pdf?X-Amz-Algorithm=...",
  "domain": "all"
}
```

**Response Example:**

```json
{
  "answer": "문서 내용에 따르면, 하도급법 위반 시 벌점은 최대 5점까지 부과될 수 있습니다. [manual:하도급가이드.pdf p.15]",
  "confidence": "high",
  "sources": [
    {
      "title": "하도급가이드.pdf",
      "type": "manual",
      "score": 0.89,
      "loc": {"page": 15},
      "snippet": "벌점 부과 기준은 다음과 같다..."
    }
  ]
}
```

---

### ⚙️ 2. 데이터 동기화 (Admin)

PDF 파일과 소스 코드를 파싱하여 Vector DB에 적재합니다. (비동기 실행)

- **URL**: `/api/admin/sync`
- **Method**: `POST`
- **Headers**: `X-API-KEY: {ADMIN_KEY}`

**Response Example:**

```json
{
  "status": "accepted",
  "message": "동기화 작업이 백그라운드에서 시작되었습니다. 완료될 때까지 터미널 로그를 확인해주세요."
}
```

---

### 🔍 3. DB 현황 조회 (Admin)

현재 Vector DB에 저장된 문서 개수와 샘플 데이터를 확인합니다.

- **URL**: `/api/admin/inspect`
- **Method**: `GET`
- **Headers**: `X-API-KEY: {ADMIN_KEY}`

**Response Example:**

```json
{
  "total_documents": 1542,
  "samples": [
    "[manual] 안전작업표준.pdf (ID: manual:안전작업표준.pdf:p1:c0...)",
    "[code] validators.py (ID: code:validators.py:L10-L50...)"
  ]
}
```
