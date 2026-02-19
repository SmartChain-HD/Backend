# AI 기반 협력사 리스크 관리 플랫폼 - Backend

> **AI 기반 협력사 리스크 관리 플랫폼**
> KT AIVLE School 8기 빅프로젝트 | AI 수도권 05반 10조

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Azure](https://img.shields.io/badge/Azure-Cloud-0078D4?style=flat&logo=microsoft-azure&logoColor=white)](https://azure.microsoft.com/)

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [서비스 도메인](#-서비스-도메인)
- [역할 체계](#-역할-체계)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [개발 가이드](#-개발-가이드)
- [팀 정보](#-팀-정보)

---

## 🎯 프로젝트 소개

### 배경

3가지 규제 흐름이 동시에 강화되고 있습니다.

1. **중대재해처벌법 전면 확대** - 2024년 상시근로자 5인 이상 전 사업장 적용, 2025년 위험성평가 인정기준 70점→90점 상향
2. **하도급법 개정 및 공정거래위원회 집중 단속** - 2025년 3월 하도급법 개정안 국회 통과, 방산 빅4 갑질 현장조사 착수
3. **글로벌 공급망 실사 지침(CSDDD) 법제화** - EU CSDDD 발효로 공급망 내 인권·환경 리스크 관리 의무가 법적 강제 사항으로 전환

약 3,354개 협력사를 인력 중심으로 실사하기에는 물리적·시간적 한계가 존재합니다.

### 솔루션

증빙 파일 업로드만으로 ESG·안전보건·컴플라이언스 검증을 자동화하는 AI 기반 협력사 리스크 관리 플랫폼입니다.

### 주요 기능

| 기능 | 설명 |
|------|------|
| 📄 **다중 형식 자료 입력** | PDF/XLSX/이미지 등 증빙 파일 자동 파싱 및 OCR 추출 |
| 🤖 **AI 6단계 검증 파이프라인** | Triage → Slot Apply → Extract → Validate → Cross → Clarify → Judge |
| 📦 **증빙 패키지** | 감사 대응용 데이터 패키지 및 1:1 역추적 |
| 👥 **역할 기반 접근 제어** | 게스트/기안자/결재자/수신자 도메인별 RBAC |
| 🔍 **외부 리스크 탐지** | GDELT/Google RSS 기반 뉴스 ESG 리스크 점수 산출 |
| 💬 **컴플라이언스 챗봇** | RAG 기반 내부 규정 Q&A |

### 기대 효과

| KPI | As-Is | To-Be |
|-----|-------|-------|
| 검증 리드타임 | 0.5~2일 | 10~30분 내 1차 판정 (**70~90% 단축**) |
| 재제출(보완) 왕복 횟수 | 2~4회 | 1~2회 |
| 형식 오류 자동 검출률 | 사람 의존 | 95%+ (목표) |
| 담당자 검토 대상 | 전체 파일 | 이슈 슬롯만 20~40% 집중 검토 |

---

## 🗂 서비스 도메인

| 도메인 | 코드 | 핵심 기능 | 주요 증빙 데이터 |
|--------|------|----------|----------------|
| ESG 실사 | `ESG` | 에너지·탄소 사용량 검증, 윤리강령 배포 확인, AI 리포트 생성 | 가스/수도/전기 요금 고지서, ISO45001, 이사회 사항, 유해물질 목록 |
| 안전보건 | `SAFETY` | TBM 영상 인원 검증, 안전교육 이수율 확인, 위험성 평가 검토 | 안전교육이수현황(Excel), 위험성평가서(Excel), 현장사진(JPG) |
| 컴플라이언스 | `COMPLIANCE` | 근로계약서 검토, 개인정보 교육 이수 확인, 공정거래 점검표 검증 | 근로계약서(PDF), 개인정보교육이수현황(Excel), 공정거래점검표(Excel) |

---

## 👤 역할 체계

| 역할 | 코드 | 목표 | 주요 기능 |
|------|------|------|----------|
| 게스트 | `GUEST` | 접근 권한 신청 및 처리 상태 확인 | 회사명/요청 역할 입력 → 대기/승인/반려 상태 확인 |
| 기안자 | `DRAFTER` | 증빙 자료 업로드 및 1차 검증 통과 후 제출 | AI Preview로 누락 탐지, Submit 후 형식 오류 사전 확인 |
| 결재자 | `APPROVER` | 내부 제출 품질 확인 후 책임 있는 결재 | 검증 결과 요약 확인 후 승인/반려 |
| 수신자 | `REVIEWER` | 협력사 제출 자료 심사 및 보완 요청 관리 | 도메인별 이슈 요약, LLM 기반 보완 요청 문구 자동 생성 |

---

## 🛠 기술 스택

### Backend (이 레포)

| 기술 | 버전 | 설명 |
|------|------|------|
| Java | 17 | 메인 언어 |
| Spring Boot | 3.5.x | 웹 프레임워크 |
| Spring Security | 6.x | 인증/인가 (JWT) |
| Spring Data JPA | 3.5.x | ORM |
| Gradle | 8.x | 빌드 도구 |
| PostgreSQL | 16 | 메인 데이터베이스 |
| Azure Blob Storage | - | 파일 스토리지 |

### Frontend ([SmartChain-HD/Frontend](https://github.com/SmartChain-HD/Frontend))

| 기술 | 버전 | 설명 |
|------|------|------|
| React | 18.3 | UI 라이브러리 |
| TypeScript | 5.3 | 언어 |
| Vite | 5.1 | 빌드 도구 |
| Tailwind CSS | v4 | 스타일링 |
| Zustand | 5.0 | 상태 관리 |
| TanStack React Query | v5 | 서버 상태 |
| React Hook Form + Zod | 7.x / 3.x | 폼 & 유효성 검사 |
| Recharts | 2.x | 차트 |

### AI ([SmartChain-HD/AI](https://github.com/SmartChain-HD/AI))

| 기술 | 설명 |
|------|------|
| FastAPI + Uvicorn | AI 서비스 프레임워크 (3개 앱: 포트 8000/8001/8002) |
| OpenAI GPT-5.1 | 이미지 증빙 해석, 최종 verdict/risk level/why 산출 (정확도 0.75) |
| OpenAI GPT-4o-mini | PDF/XLSX 텍스트 보강, Clarification 생성 |
| Naver Clova OCR V2 | 한국어 문서 OCR (정확도 96.4%, 장당 1.2초) |
| YOLO26n (CrowdHuman) | 현장 사진 인원 수 검증 (Count Accuracy 80.2%) |
| ChromaDB | RAG 기반 벡터 DB (챗봇/리스크 탐지) |
| LangChain | LLM 오케스트레이션 |

### DevOps / Infra

| 기술 | 설명 |
|------|------|
| Azure Container Apps | 컨테이너 기반 서비스 배포 |
| Azure PostgreSQL Flexible Server | 관리형 DB (Private VNet 통합) |
| Azure Key Vault | Zero-Secret 보안 (Managed Identity + RBAC) |
| GitHub Actions | CI/CD 자동 빌드/배포 (ACR → ACA) |
| Docker | 멀티스테이지 빌드 |

---

## 🏗 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                  Presentation Tier (Frontend)                    │
│         React 18 + TypeScript + Tailwind CSS v4 + Vite          │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Application Tier (Backend)                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │               Main API (Spring Boot 3.5)                │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │    │
│  │  │   Auth   │ │Diagnostic│ │ Approval │ │  Review  │   │    │
│  │  │ Service  │ │ Service  │ │ Service  │ │ Service  │   │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                AI Service (FastAPI)                      │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌─────────────────┐  │    │
│  │  │ ai_run_api   │ │ chatbot_api  │ │  out_risk_api   │  │    │
│  │  │  Port 8000   │ │  Port 8001   │ │   Port 8002     │  │    │
│  │  │ 문서 검증    │ │ 컴플라이언스 │ │ 외부 뉴스       │  │    │
│  │  │ 6단계 파이프 │ │ RAG 챗봇    │ │ ESG 리스크 탐지 │  │    │
│  │  └──────────────┘ └──────────────┘ └─────────────────┘  │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Tier (Azure)                           │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │  PostgreSQL Flexible │  │   Azure Blob Storage │             │
│  │   Server (VNet 통합) │  │   (증빙 파일/패키지)  │             │
│  └──────────────────────┘  └──────────────────────┘             │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │   Azure Key Vault    │  │  Google News RSS      │             │
│  │  (Zero-Secret 보안)  │  │  GDELT API            │             │
│  └──────────────────────┘  └──────────────────────┘             │
└─────────────────────────────────────────────────────────────────┘
```

### AI 검증 파이프라인 (ai_run_api)

```
파일 업로드
    │
    ▼
① TRIAGE      파일 분류 및 판독 가능 여부 확인
    │
    ▼
② SLOT APPLY  도메인 슬롯 매핑 (slot_hint 기반)
    │
    ▼
③ EXTRACT     PDF 파서 / XLSX pandas / OCR+Vision+YOLO
    │
    ▼
④ VALIDATE    규칙 기반 검증 + LLM 이상 탐지
    │
    ▼
⑤ CROSS       교차 검증 (출석부 인원수 ↔ YOLO 탐지 인원수)
    │
    ▼
⑥ CLARIFY     슬롯별 보완 요청 문구 자동 생성 (한국어)
    │
    ▼
⑦ JUDGE       최종 verdict + risk_level + why 산출
```

**Verdict**: `PASS` / `NEED_CLARIFY` / `NEED_FIX`
**Risk Level**: `LOW` / `MEDIUM` / `HIGH` / `CRITICAL`

---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/smartchain/platform/
│   │   ├── PlatformApplication.java         # 애플리케이션 진입점
│   │   ├── HealthController.java
│   │   │
│   │   ├── global/                          # 공통 모듈
│   │   │   ├── config/                      # SecurityConfig, SwaggerConfig, JpaConfig
│   │   │   ├── entity/                      # BaseTimeEntity (auditing)
│   │   │   ├── enums/                       # DiagnosticStatus, RequestStatus 등
│   │   │   ├── error/                       # ErrorCode, CustomException, GlobalExceptionHandler
│   │   │   └── response/                    # BaseResponse
│   │   │
│   │   └── domain/                          # 비즈니스 도메인
│   │       ├── user/                        # 사용자, 회사, 역할, 도메인 권한
│   │       │   └── entity/                  # User, Company, Role, Domain, UserDomainRole
│   │       ├── diagnostic/                  # 진단 (핵심 도메인)
│   │       │   └── entity/                  # Diagnostic, Campaign, Question, ResultQual/Quant
│   │       ├── approval/                    # 결재 워크플로우
│   │       ├── review/                      # 수신자 심사
│   │       ├── evidence/                    # 증빙 파일
│   │       ├── ai/                          # AI 분석 연동
│   │       │   ├── client/                  # AiRunApiClient (WebClient)
│   │       │   ├── config/                  # AiRunApiConfig
│   │       │   ├── controller/              # AiAnalysisController
│   │       │   ├── entity/                  # AiAnalysisResult
│   │       │   ├── repository/
│   │       │   └── service/                 # AiAnalysisService (비동기)
│   │       └── log/                         # 활동 로그
│   │
│   └── resources/
│       ├── application.yaml                 # 기본 설정
│       ├── application-local.yaml           # 로컬 환경
│       ├── application-dev.yaml             # Azure 개발 환경
│       └── application-test.yaml            # H2 인메모리 테스트
│
└── test/
    └── java/com/smartchain/platform/
```

### 도메인 기반 권한 체계

사용자는 각 서비스 도메인(ESG, SAFETY, COMPLIANCE)별로 서로 다른 역할을 가질 수 있습니다.

```java
// 도메인 기반 권한 검증
user.hasRoleInDomain("ESG", "APPROVER");     // ESG 도메인에서 APPROVER 역할 확인
user.getDomainsWithRole("DRAFTER");           // DRAFTER 역할을 가진 도메인 목록 조회
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Gradle 8+
- PostgreSQL 16+
- IDE (IntelliJ IDEA 권장)

### 빌드 및 실행

```bash
# 레포지토리 클론
git clone https://github.com/SmartChain-HD/smartchain.git
cd smartchain

# 빌드
./gradlew build

# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "DiagnosticServiceTest"

# 클린 빌드
./gradlew clean build
```

### 필수 환경변수 (dev 프로파일)

```yaml
# Azure 환경변수
DB_HOST: Azure PostgreSQL 호스트
DB_NAME: 데이터베이스명
DB_USER: 데이터베이스 사용자
DB_PASSWORD: 데이터베이스 비밀번호
```

### 접속 확인

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 📚 API 문서

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### 주요 엔드포인트

| 도메인 | Base Path | 설명 |
|--------|-----------|------|
| 인증 | `/api/v1/auth` | 로그인, 회원가입, 토큰 갱신 |
| 진단 | `/api/v1/diagnostics` | ESG/SAFETY/COMPLIANCE 진단 CRUD (`?domainCode=ESG`) |
| 결재 | `/api/v1/approvals` | 결재 처리 (도메인 기반 권한 검증) |
| 심사 | `/api/v1/reviews` | 수신자 심사 (도메인 기반 권한 검증) |
| 파일 | `/api/v1/files` | 파일 업로드/다운로드 |
| AI Run | `/api/v1/ai/run/diagnostics/{id}/*` | AI 검증 공통 API |
| ESG AI | `/api/v1/ai/esg/*` | ESG 파싱·리포트 |
| 안전 AI | `/api/v1/ai/safety/*` | TBM 영상 분석 |
| 컴플라이언스 AI | `/api/v1/ai/compliance/*` | 계약서 검토 |

### AI Run API 상세

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/ai/run/diagnostics/{id}/preview` | 슬롯 추정 (파일 분류) |
| POST | `/api/v1/ai/run/diagnostics/{id}/submit` | 전체 검증 요청 (비동기) |
| GET | `/api/v1/ai/run/diagnostics/{id}/result` | 최신 결과 조회 |
| GET | `/api/v1/ai/run/diagnostics/{id}/history` | 분석 이력 조회 |

상세 API 명세는 [`docs/API_CONTRACT_SSOT.md`](./docs/API_CONTRACT_SSOT.md) 참조.

---

## 📖 개발 가이드

### 브랜치 전략

```
main              ← 운영 배포
  └── develop     ← 개발 통합
        ├── feature/auth-login
        ├── feature/diagnostic-crud
        └── hotfix/critical-bug
```

### 커밋 컨벤션

```
feat(diagnostic): 정성적 평가 저장 API 구현
fix(auth): JWT 토큰 만료 시간 오류 수정
refactor: User 엔티티 구조 변경
test: DiagnosticService 단위 테스트 추가
docs: AI 연동 가이드 업데이트
```

### 코드 컨벤션

| 유형 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `DiagnosticService` |
| 메서드/변수 | camelCase | `findByUserId()` |
| 상수 | UPPER_SNAKE | `MAX_FILE_SIZE` |
| 패키지 | lowercase | `com.smartchain.platform.domain` |
| DTO | 용도 명시 | `DiagnosticCreateRequest`, `ApprovalDetailResponse` |

### 테스트 프로파일

테스트 시 H2 인메모리 DB 사용 (`application-test.yaml`):
- `spring.sql.init.mode: never` - data.sql 비활성화
- `spring.jpa.hibernate.ddl-auto: create-drop`
- 도메인 권한 검증 시 `user.getDomainsWithRole()` mock 필수

---

## 📊 문서

| 문서 | 설명 |
|------|------|
| [API Contract SSOT](./docs/API_CONTRACT_SSOT.md) | API 계약 정의 (SSOT) |
| [API Quick Reference](./docs/API_QUICK_REFERENCE.md) | API 빠른 참조 |
| [Frontend Integration Rules](./docs/FE_INTEGRATION_RULES.md) | 프론트엔드 연동 가이드 |
| [Backend Implementation Guide](./docs/BACKEND_IMPLEMENTATION_GUIDE.md) | 백엔드 구현 가이드 |
| [Status and Error Codes](./docs/STATUS_AND_ERROR_CODES.md) | 상태/에러 코드 참조 |
| [AI Integration Guide](./docs/AI_INTEGRATION_GUIDE.md) | AI Run API 연동 가이드 |
| [AI Risk API](./docs/AI_RISK_API.md) | 외부 리스크 감지 API 가이드 |

---

## 👥 팀 정보

### AI 수도권 05반 10조

| 이름 | 역할 | 담당 |
|------|------|------|
| 이종헌 | PM | FE, BE, AI, Infra 전체 총괄 |
| 이수오 | 인프라 리더 | FE, Infra |
| 김건우 | 팀원 | FE, BE, Infra |
| 진지현 | 풀스택 리더 | FE, BE |
| 박세용 | 팀원 | FE, BE |
| 이수빈 | AI 리더 | FE, AI |
| 배수한 | 팀원 | FE, AI |

### 프로젝트 정보

- **과제명**: AI 기반 협력사 리스크 관리 플랫폼
- **기간**: 2025.12.29 ~ 2026.02.20
- **목표 고객**: HD현대중공업 ESG 관리팀 / 안전보건팀 / 구매팀 (B2B)
- **관련 레포지토리**: [Frontend](https://github.com/SmartChain-HD/Frontend) | [AI](https://github.com/SmartChain-HD/AI)

---

## 📄 라이선스

This project is licensed under the MIT License.

---

<p align="center">
  <img src="https://img.shields.io/badge/KT_AIVLE_School-8기-00A9E0?style=for-the-badge" alt="AIVLE">
  <img src="https://img.shields.io/badge/Big_Project-AI_10조-FF6B35?style=for-the-badge" alt="Team">
  <img src="https://img.shields.io/badge/HD현대중공업-B2B-003087?style=for-the-badge" alt="HD">
</p>
