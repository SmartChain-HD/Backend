# Claude Code Learnings

## 2026-01-28: AI Run API 공통 클라이언트 구현

### 원인
- 기획서(AI_RunAPI_기획서)는 공통 `/run/preview`, `/run/submit` API 구조를 정의
- GitHub 이슈(#22-31)는 도메인별 분리된 AI 클라이언트 구조로 설계
- 두 접근 방식 간 설계 불일치 존재

### 해결
- 기획서 기반 공통 AI Run API 클라이언트 구현 선택
- 구현 내용:
  - `AiRunApiClient`: WebClient 기반 `/run/preview`, `/run/submit` 호출
  - `AiAnalysisService`: Diagnostic과 AI API 연동, 비동기 처리 지원
  - `AiAnalysisResult` 엔티티: 분석 결과 저장
  - `AiAnalysisController`: REST API 엔드포인트 제공

### 재발 방지
- 기획서와 이슈 간 설계 불일치 발견 시 먼저 방향 확인 질문
- 엔드포인트 충돌 방지를 위해 기존 컨트롤러 엔드포인트 확인 후 경로 설계
- JPA Repository 메서드명은 엔티티의 실제 ID 필드명 확인 필수 (예: `diagnosticId` vs `id`)

### 검증 방법
```bash
./gradlew build     # 빌드 성공
./gradlew test      # 123 tests passed
```

### 관련 커밋
- (커밋 예정)

### 생성/수정 파일
```
src/main/java/com/smartchain/platform/
├── domain/ai/
│   ├── client/AiRunApiClient.java
│   ├── config/AiRunApiConfig.java
│   ├── controller/AiAnalysisController.java
│   ├── entity/AiAnalysisResult.java
│   ├── repository/AiAnalysisResultRepository.java
│   └── service/AiAnalysisService.java
├── dto/ai/
│   ├── AiAnalysisRequest.java
│   ├── AiAnalysisResultResponse.java
│   ├── AiPreviewRequest.java
│   └── run/
│       ├── Clarification.java
│       ├── FileInfo.java
│       ├── RunPreviewRequest.java
│       ├── RunPreviewResponse.java
│       ├── RunSubmitRequest.java
│       ├── RunSubmitResponse.java
│       ├── SlotHint.java
│       ├── SlotResult.java
│       └── SlotStatus.java
├── global/
│   ├── config/AsyncConfig.java
│   └── error/ErrorCode.java (AI 에러코드 추가)
build.gradle (webflux 의존성 추가)
application.yaml (AI 설정 추가)
```

### API 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/ai/run/diagnostics/{id}/preview` | 슬롯 추정 |
| POST | `/api/v1/ai/run/diagnostics/{id}/submit` | 전체 검증 요청 |
| GET | `/api/v1/ai/run/diagnostics/{id}/result` | 최신 결과 조회 |
| GET | `/api/v1/ai/run/diagnostics/{id}/history` | 분석 이력 조회 |
