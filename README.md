# 🌱 ESG Supply Chain Due Diligence Platform - Backend

> **AI 기반 공급망 ESG 실사 자동화 플랫폼**  
> KT AIVLE School 8기 빅프로젝트 | AI 수도권 05반 10조

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Azure](https://img.shields.io/badge/Azure-Cloud-0078D4?style=flat&logo=microsoft-azure&logoColor=white)](https://azure.microsoft.com/)

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
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
EU CSDDD(공급망 실사 지침) 발효에 따라 원청기업의 공급망 내 인권·환경 리스크 관리 의무가 법적 강제 사항으로 전환됩니다(2027년부터 단계 적용). 약 3,354개에 달하는 협력사를 대상으로 인력 중심의 실사를 진행하기에는 물리적/시간적 한계가 존재합니다.

### 솔루션
AI 기반 자동화 시스템을 통해 증빙 파일 업로드만으로 ESG 지표 산출과 진단을 자동화하여, 데이터 무결성을 확보하고 협력사의 이행 부담을 완화합니다.

### 주요 기능
| 기능 | 설명 |
|------|------|
| 📄 **다중형식 자료입력** | PDF/XLSX/DOCX/이미지 등 다양한 증빙파일 OCR 추출 |
| 🤖 **AI 리포트 생성** | LLM 기반 ESG 위험군 분류 및 성과 요약 |
| 📦 **증빙 패키지** | 감사 대응용 data.zip 패키징 및 1:1 역추적 |
| 👥 **역할 기반 접근 제어** | 게스트/기안자/결재자/수신자/관리자 RBAC |

### 기대 효과
- 📈 공급망 관리 커버리지 **6.5배 확대** (516개 → 3,354개사)
- 💰 잠재 운영 비용 **약 141억원 절감**
- ⏱️ 데이터 검증 리드타임 **90% 단축** (120분 → 10분)

---

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 설명 |
|------|------|------|
| Java | 17 | 메인 언어 |
| Spring Boot | 3.2.x | 웹 프레임워크 |
| Spring Security | 6.x | 인증/인가 |
| Spring Data JPA | 3.2.x | ORM |
| Gradle | 8.x | 빌드 도구 |

### Database & Storage
| 기술 | 버전 | 설명 |
|------|------|------|
| PostgreSQL | 16 | 메인 데이터베이스 (Azure) |
| Redis | 7.x | 세션/캐시 (선택) |
| Azure Blob Storage | - | 파일 스토리지 |

### AI & External API
| 기술 | 설명 |
|------|------|
| OpenAI GPT-4o | 진단표 생성, 보고서 생성 |
| Naver Clova OCR | 문서 텍스트 추출 |
| LangChain | AI 에이전트 오케스트레이션 |

### DevOps
| 기술 | 설명 |
|------|------|
| Azure App Service | 애플리케이션 배포 |
| Azure Key Vault | 시크릿 관리 |
| GitHub Actions | CI/CD |

---

## 🏗 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                     Presentation Tier (Frontend)                 │
│                 React + TypeScript + Tailwind CSS                │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Application Tier (Backend)                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Main API (Spring Boot)                │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │    │
│  │  │   Auth   │ │ Diagnos- │ │ Approval │ │  Admin   │   │    │
│  │  │ Service  │ │tic Svc   │ │ Service  │ │ Service  │   │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                  AI Service (FastAPI)                    │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐                 │    │
│  │  │   OCR    │ │ LLM Gen  │ │   RAG    │                 │    │
│  │  │ Pipeline │ │  Report  │ │  Search  │                 │    │
│  │  └──────────┘ └──────────┘ └──────────┘                 │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Data Tier (Azure)                          │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │    PostgreSQL    │  │   Blob Storage   │                     │
│  │   (Azure DB)     │  │   (Files/Docs)   │                     │
│  └──────────────────┘  └──────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/aivle/esg/
│   │   ├── EsgApplication.java            # 애플리케이션 진입점
│   │   │
│   │   ├── common/                        # 공통 모듈
│   │   │   ├── config/                    # 설정
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── JpaConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── exception/                 # 예외 처리
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── ErrorCode.java
│   │   │   ├── response/                  # 공통 응답
│   │   │   │   └── ApiResponse.java
│   │   │   └── util/                      # 유틸리티
│   │   │       └── DateUtil.java
│   │   │
│   │   ├── domain/                        # 도메인 모듈
│   │   │   ├── auth/                      # 인증
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── user/                      # 사용자
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── role/                      # 권한
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── company/                   # 협력사
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── diagnostic/                # 진단/기안
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── approval/                  # 결재
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── review/                    # 수신자(리뷰)
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── file/                      # 파일
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   ├── admin/                     # 관리자
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   └── dto/
│   │   │   │
│   │   │   └── notification/              # 알림
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       └── dto/
│   │   │
│   │   └── infra/                         # 인프라 연동
│   │       ├── ai/                        # AI 서비스 클라이언트
│   │       ├── storage/                   # Azure Blob Storage
│   │       └── external/                  # 외부 API
│   │
│   └── resources/
│       ├── application.yml                # 기본 설정
│       ├── application-local.yml          # 로컬 환경
│       ├── application-dev.yml            # 개발 환경
│       └── application-prod.yml           # 운영 환경
│
└── test/
    └── java/com/aivle/esg/
        ├── domain/
        │   ├── auth/
        │   ├── diagnostic/
        │   └── ...
        └── integration/
```

---

## 🚀 시작하기

### 사전 요구사항
- Java 17+
- Gradle 8+
- PostgreSQL 16+
- IDE (IntelliJ IDEA 권장)

### 환경 설정

1. **레포지토리 클론**
```bash
git clone https://github.com/your-org/esg-platform-backend.git
cd esg-platform-backend
```

2. **환경 변수 설정**
```bash
# application-local.yml 또는 환경변수 설정
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

3. **필수 환경변수**
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/esg_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600

azure:
  storage:
    connection-string: ${AZURE_STORAGE_CONNECTION}
    container-name: esg-files

openai:
  api-key: ${OPENAI_API_KEY}
```

4. **데이터베이스 초기화**
```bash
# DDL 자동 생성 (개발 환경만)
# spring.jpa.hibernate.ddl-auto: update

# 또는 Flyway 마이그레이션 사용
./gradlew flywayMigrate
```

5. **빌드 및 실행**
```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는
java -jar build/libs/esg-platform-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

6. **접속 확인**
```bash
# Health Check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 📚 API 문서

### Swagger UI
개발 환경에서 아래 URL로 API 문서를 확인할 수 있습니다:
```
http://localhost:8080/swagger-ui.html
```

### API 명세서
상세 API 명세서는 [API_SPECIFICATION.md](./docs/API_SPECIFICATION.md)를 참조하세요.

### 주요 엔드포인트

| 도메인 | Base Path | 설명 |
|--------|-----------|------|
| 인증 | `/api/v1/auth` | 로그인, 회원가입, 토큰 관리 |
| 권한 | `/api/v1/roles` | 권한 요청/승인 |
| 진단 | `/api/v1/diagnostics` | ESG 진단 CRUD |
| 결재 | `/api/v1/approvals` | 결재 처리 |
| 리뷰 | `/api/v1/reviews` | 수신자 진단 현황 |
| 관리 | `/api/v1/admin` | 관리자 기능 |
| 파일 | `/api/v1/files` | 파일 업로드/다운로드 |

---

## 📖 개발 가이드

### 브랜치 전략
```
main              ← 운영 배포
  └── develop     ← 개발 통합
        ├── feature/auth-login      ← 기능 개발
        ├── feature/diagnostic-crud
        └── hotfix/critical-bug     ← 긴급 수정
```

### 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅 (기능 변경 X)
refactor: 코드 리팩토링
test: 테스트 코드 추가
chore: 빌드, 설정 파일 수정
```

**예시:**
```bash
git commit -m "feat(diagnostic): 정성적 평가 저장 API 구현"
git commit -m "fix(auth): JWT 토큰 만료 시간 오류 수정"
```

### 코드 컨벤션

**네이밍 규칙**
| 유형 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `DiagnosticService` |
| 메서드/변수 | camelCase | `findByUserId()` |
| 상수 | UPPER_SNAKE | `MAX_FILE_SIZE` |
| 패키지 | lowercase | `com.aivle.esg.domain` |
| DTO | 용도 명시 | `DiagnosticCreateRequest`, `DiagnosticDetailResponse` |

**DTO 네이밍 규칙**
```
[도메인][동작][Request/Response/Dto]

예시:
- DiagnosticCreateRequest    → 진단 생성 요청
- DiagnosticDetailResponse   → 진단 상세 응답
- UserInfoDto               → 사용자 정보 (내부 전달용)
```

**API 응답 형식**
```java
@GetMapping("/{id}")
public ApiResponse<DiagnosticDetailResponse> getDiagnostic(@PathVariable Long id) {
    DiagnosticDetailResponse response = diagnosticService.findById(id);
    return ApiResponse.success(response);
}
```

### 테스트
```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "DiagnosticServiceTest"

# 커버리지 리포트
./gradlew jacocoTestReport
```

---

## 🔐 보안 설정

### JWT 인증 흐름
```
1. POST /auth/login → Access Token + Refresh Token 발급
2. Request Header: Authorization: Bearer {accessToken}
3. Access Token 만료 시 → POST /auth/refresh → 새 토큰 발급
```

### 권한 체크
```java
@PreAuthorize("hasRole('DRAFTER')")
@PostMapping
public ApiResponse<DiagnosticCreateResponse> create(...) { }

@PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
@GetMapping("/approvals")
public ApiResponse<ApprovalListResponse> getApprovals(...) { }
```

---

## 📊 모니터링

### Actuator 엔드포인트
```
GET /actuator/health     # 헬스 체크
GET /actuator/info       # 애플리케이션 정보
GET /actuator/metrics    # 메트릭
```

### 로깅
```yaml
logging:
  level:
    root: INFO
    com.aivle.esg: DEBUG
    org.springframework.security: DEBUG  # 보안 디버깅 시
```

---

## 🚢 배포

### Azure App Service 배포
```bash
# Azure CLI 로그인
az login

# 배포
./gradlew bootJar
az webapp deploy --resource-group esg-rg --name esg-api --src-path build/libs/*.jar
```

### GitHub Actions CI/CD
`.github/workflows/deploy.yml` 참조

---

## 👥 팀 정보

### AI 수도권 05반 10조

| 이름 | 역할 | 담당 |
|------|------|------|
| 이종헌 | PM | 전체 총괄, FE/BE/AI/Infra |
| 이수오 | 인프라 리더 | FE, Infra |
| 김건우 | 팀원 | FE, BE, Infra |
| 진지현 | 풀스택 리더 | FE, BE |
| 박세용 | 팀원 | FE, BE |
| 이수빈 | AI 리더 | FE, AI |
| 배수한 | 팀원 | FE, AI, Infra |

### 프로젝트 정보
- **과제명**: AI 기반 공급망 ESG 실사 자동화 플랫폼
- **기간**: 2025.12.29 ~ 2026.02.20
- **목표 고객**: HD현대중공업 ESG 경영팀 (B2B)

---

## 📄 라이선스

This project is licensed under the MIT License.

---

## 📞 문의

프로젝트 관련 문의는 이슈 등록 또는 아래 연락처로 연락 바랍니다.

- **GitHub Issues**: [이슈 등록](https://github.com/your-org/esg-platform-backend/issues)
- **Email**: team10@aivle.kt.com

---

<p align="center">
  <img src="https://img.shields.io/badge/KT_AIVLE_School-8기-00A9E0?style=for-the-badge" alt="AIVLE">
  <img src="https://img.shields.io/badge/Big_Project-AI_10조-FF6B35?style=for-the-badge" alt="Team">
</p>
