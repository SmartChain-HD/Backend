# 로컬 테스트 가이드

SmartChain 플랫폼 로컬 환경 테스트 가이드입니다.

---

## 목차

1. [사전 준비](#1-사전-준비)
2. [백엔드 실행](#2-백엔드-실행)
3. [프론트엔드 실행](#3-프론트엔드-실행)
4. [AI 서버 실행](#4-ai-서버-실행)
5. [테스트 계정](#5-테스트-계정)
6. [API 테스트](#6-api-테스트)
7. [테스트 데이터](#7-테스트-데이터)
8. [트러블슈팅](#8-트러블슈팅)

---

## 1. 사전 준비

### 필수 소프트웨어

| 소프트웨어 | 버전 | 용도 | 설치 확인 명령어 |
|-----------|------|------|-----------------|
| Java | 17+ | 백엔드 | `java -version` |
| Docker | 최신 | PostgreSQL DB | `docker --version` |
| Docker Compose | 최신 | PostgreSQL DB | `docker-compose --version` |
| Node.js | 18+ | 프론트엔드 | `node -v` |
| Python | 3.10+ | AI 서버 | `python --version` |
| Git | 최신 | 소스 관리 | `git --version` |

### 저장소 클론

```bash
# 백엔드
git clone https://github.com/SmartChain-HD/smartchain.git
cd smartchain

# 프론트엔드 (별도 디렉토리)
git clone https://github.com/SmartChain-HD/Frontend.git

# AI 서버 (별도 디렉토리)
git clone https://github.com/SmartChain-HD/AI.git
```

---

## 2. 백엔드 실행

### Step 1: PostgreSQL 데이터베이스 실행

```bash
# 프로젝트 루트에서 실행
docker-compose up -d
```

**DB 연결 정보:**
| 항목 | 값 |
|------|-----|
| Host | `localhost` |
| Port | `5432` |
| Database | `smartchain` |
| Username | `esg` |
| Password | `esg1234` |

DB 상태 확인:
```bash
docker-compose ps
# 또는
docker logs smartchain-db
```

### Step 2: 백엔드 서버 실행

```bash
# Windows
gradlew.bat bootRun

# Mac/Linux
./gradlew bootRun
```

**서버 정보:**
- URL: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Profile: `local` (기본값)

### 백엔드 실행 확인

```bash
# Health Check
curl http://localhost:8080/actuator/health

# 또는 브라우저에서 Swagger UI 접속
http://localhost:8080/swagger-ui.html
```

---

## 3. 프론트엔드 실행

**저장소:** https://github.com/SmartChain-HD/Frontend

**기술 스택:** React 18 + Vite + TypeScript + Tailwind CSS v4

### Step 1: 저장소 클론 및 의존성 설치

```bash
git clone https://github.com/SmartChain-HD/Frontend.git
cd Frontend
npm install
```

### Step 2: 개발 서버 실행

```bash
npm run dev
```

**서버 정보:**
- URL: http://localhost:5173 (Vite 기본 포트)
- 백엔드 API: http://localhost:8080 으로 연결

### Step 3: 실행 확인

브라우저에서 http://localhost:5173 접속 → 로그인 페이지 표시

### 주요 라우팅

| 경로 | 설명 |
|------|------|
| `/login` | 로그인 페이지 |
| `/signup/step1` | 회원가입 1단계 (약관 동의) |
| `/signup/step2` | 회원가입 2단계 (정보 입력) |
| `/dashboard` | 역할별 대시보드 |

---

## 4. AI 서버 실행

**저장소:** https://github.com/SmartChain-HD/AI

**기술 스택:** Python 3.10 + FastAPI + OpenAI GPT + YOLO + ChromaDB

### Step 1: 저장소 클론 및 의존성 설치

```bash
git clone https://github.com/SmartChain-HD/AI.git
cd AI
pip install -r requirements.txt
```

### Step 2: 환경변수 설정

```bash
cp .env.example .env
```

`.env` 파일 편집:
```env
# 필수
OPENAI_API_KEY="your-openai-api-key"

# ai_run_api용
CLOVA_INVOKE_URL="your-clova-url"
CLOVA_OCR_SECRET="your-clova-secret"
OPENAI_MODEL_LIGHT="gpt-4o-mini"
OPENAI_MODEL_HEAVY="gpt-5.1"
```

### Step 3: AI 서버 실행

```bash
# 메인 검증 엔진 (포트 8000) - 백엔드 연동용
uvicorn app.main:app --reload --port 8000 --app-dir apps/ai_run_api
```

**서버 정보:**
- URL: http://localhost:8000
- Health Check: http://localhost:8000/health

### 추가 서버 (선택)

```bash
# 컴플라이언스 Q&A 챗봇 (포트 8001)
uvicorn app.main:app --reload --port 8001 --app-dir apps/chatbot_api

# 외부 리스크 분석 (포트 8002)
uvicorn app.main:app --reload --port 8002 --app-dir apps/out_risk_api
```

### AI API 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| GET | `/health` | 서버 상태 확인 |
| POST | `/run/preview` | 파일 분류 + 슬롯 추정 |
| POST | `/run/submit` | 전체 검증 파이프라인 실행 |

### Streamlit 테스트 UI (선택)

```bash
# AI 검증 테스트 UI
streamlit run apps/ai_run_api/app/ui/streamlit_app.py
```

---

## 5. 테스트 계정

### 공통 비밀번호: `Test1234!`

### 원청 심사자 (REVIEWER) - HD현대중공업

| 담당 도메인 | 이메일 | 역할 |
|------------|--------|------|
| ESG | `reviewer.esg@hdhhi.co.kr` | ESG 심사 담당 |
| 안전보건 | `reviewer.safety@hdhhi.co.kr` | Safety 심사 담당 |
| 컴플라이언스 | `reviewer.compliance@hdhhi.co.kr` | Compliance 심사 담당 |

### 협력사 결재자 (APPROVER)

| 회사 | 이메일 | 담당 도메인 |
|------|--------|------------|
| (주)테크파트너 | `approver@techpartner.co.kr` | ESG |
| (주)그린매뉴팩처링 | `approver@greenmanu.co.kr` | ESG |
| (주)안전건설 | `approver@safebuild.co.kr` | ESG |

### 협력사 기안자 (DRAFTER)

| 회사 | 이메일 | 담당 도메인 |
|------|--------|------------|
| (주)테크파트너 | `drafter1@techpartner.co.kr` | ESG, COMPLIANCE |
| (주)테크파트너 | `drafter2@techpartner.co.kr` | ESG |
| (주)그린매뉴팩처링 | `drafter@greenmanu.co.kr` | ESG, SAFETY |
| (주)안전건설 | `drafter@safebuild.co.kr` | SAFETY |

### 게스트 (GUEST) - 권한 요청 테스트용

| 회사 | 이메일 |
|------|--------|
| (주)정밀부품 | `newbie1@precision.co.kr` |
| (주)정밀부품 | `newbie2@precision.co.kr` |

---

## 6. API 테스트

### Swagger UI 사용

1. http://localhost:8080/swagger-ui.html 접속
2. `POST /api/v1/auth/login` 에서 로그인
3. 응답에서 `accessToken` 복사
4. 우측 상단 "Authorize" 버튼 클릭
5. `Bearer {accessToken}` 형식으로 입력
6. 이후 다른 API 테스트 가능

### 로그인 API 예시

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "drafter1@techpartner.co.kr",
    "password": "Test1234!"
  }'
```

### 주요 API 엔드포인트

| 기능 | Method | Endpoint |
|------|--------|----------|
| 로그인 | POST | `/api/v1/auth/login` |
| 진단 목록 | GET | `/api/v1/diagnostics` |
| 결재 목록 | GET | `/api/v1/approvals` |
| 심사 목록 | GET | `/api/v1/reviews` |
| AI 분석 요청 | POST | `/api/v1/ai/run/diagnostics/{id}/submit` |

---

## 7. 테스트 데이터

서버 시작 시 자동으로 테스트 데이터가 생성됩니다.

### 회사 (5개)

| 회사명 | 타입 | 업종 |
|--------|------|------|
| HD현대중공업 | TIER1 (원청) | 제조업 |
| (주)테크파트너 | TIER2 (협력사) | IT/소프트웨어 |
| (주)그린매뉴팩처링 | TIER2 (협력사) | 제조업 |
| (주)안전건설 | TIER2 (협력사) | 건설업 |
| (주)정밀부품 | TIER2 (협력사) | 제조업 |

### 캠페인 (6개)

| 도메인 | 캠페인 | 상태 |
|--------|--------|------|
| ESG | 2026년 1분기 ESG 공급망 진단 | 활성 |
| ESG | 2025년 3분기 ESG 공급망 진단 | 완료 |
| SAFETY | 2026년 1분기 안전보건 점검 | 활성 |
| SAFETY | 2025년 3분기 안전보건 점검 | 완료 |
| COMPLIANCE | 2026년 1분기 하도급 컴플라이언스 점검 | 활성 |
| COMPLIANCE | 2025년 1분기 하도급 컴플라이언스 점검 | 완료 |

### 진단 상태별 테스트 데이터 (17건)

| 상태 | 설명 | 건수 |
|------|------|------|
| WRITING | 작성 중 | 4건 |
| SUBMITTED | 제출됨 (결재 대기) | 3건 |
| RETURNED | 반려됨 | 1건 |
| APPROVED | 승인됨 (심사 대기) | 2건 |
| REVIEWING | 심사 중 | 2건 |
| COMPLETED | 완료 | 5건 |

### 역할별 테스트 시나리오

**기안자 (DRAFTER) 테스트:**
- `drafter1@techpartner.co.kr` 로그인
- 진단 작성 → 제출 → 결재 요청 플로우 테스트

**결재자 (APPROVER) 테스트:**
- `approver@techpartner.co.kr` 로그인
- 결재 목록 조회 → 승인/반려 플로우 테스트

**심사자 (REVIEWER) 테스트:**
- `reviewer.esg@hdhhi.co.kr` 로그인
- 심사 목록 조회 → 심사 완료/보완요청 플로우 테스트

---

## 8. 트러블슈팅

### DB 연결 실패

```bash
# Docker 컨테이너 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose restart db

# 로그 확인
docker-compose logs db
```

### 포트 충돌

```bash
# 8080 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :8080

# 5432 포트 사용 중인 프로세스 확인 (Windows)
netstat -ano | findstr :5432
```

### 데이터 초기화

DB를 완전히 초기화하려면:

```bash
# 컨테이너 및 볼륨 삭제
docker-compose down -v

# 다시 시작
docker-compose up -d

# 백엔드 재시작 (데이터 재생성)
./gradlew bootRun
```

### Gradle 빌드 실패

```bash
# 캐시 정리 후 재빌드
./gradlew clean build --refresh-dependencies
```

---

## 전체 실행 순서 (Quick Start)

모든 서비스를 한 번에 실행하는 순서입니다.

### 터미널 1: PostgreSQL DB
```bash
cd smartchain
docker-compose up -d
```

### 터미널 2: 백엔드 (Spring Boot)
```bash
cd smartchain
./gradlew bootRun
# Windows: gradlew.bat bootRun
```
→ http://localhost:8080

### 터미널 3: AI 서버 (FastAPI)
```bash
cd AI
uvicorn app.main:app --reload --port 8000 --app-dir apps/ai_run_api
```
→ http://localhost:8000

### 터미널 4: 프론트엔드 (Vite)
```bash
cd Frontend
npm run dev
```
→ http://localhost:5173

### 실행 확인 체크리스트

| 서비스 | URL | 확인 방법 |
|--------|-----|----------|
| PostgreSQL | localhost:5432 | `docker-compose ps` |
| Backend | http://localhost:8080 | Swagger UI 접속 |
| AI Server | http://localhost:8000 | `/health` 엔드포인트 |
| Frontend | http://localhost:5173 | 로그인 페이지 표시 |

---

## 서비스 포트 요약

| 서비스 | 포트 | 설명 |
|--------|------|------|
| PostgreSQL | 5432 | 데이터베이스 |
| Backend (Spring) | 8080 | REST API + Swagger |
| AI Run API | 8000 | 문서 검증 엔진 |
| AI Chatbot | 8001 | Q&A 챗봇 (선택) |
| AI Risk | 8002 | 외부 리스크 분석 (선택) |
| Frontend (Vite) | 5173 | 웹 UI |

---

## 문의

테스트 중 문제가 발생하면 개발팀에 문의해주세요.

- Backend: https://github.com/SmartChain-HD/smartchain
- Frontend: https://github.com/SmartChain-HD/Frontend
- AI: https://github.com/SmartChain-HD/AI
