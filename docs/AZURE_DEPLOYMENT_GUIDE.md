# Azure 배포 가이드

SmartChain 백엔드 시스템을 Azure에 배포할 때 필요한 인프라 설정 안내입니다.

## 1. 환경변수 설정

Azure App Service → Settings → Environment variables에서 아래 환경변수를 설정합니다.

### 1.1 필수 환경변수

#### 데이터베이스 (PostgreSQL)

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `DB_HOST` | PostgreSQL 서버 호스트 | `smartchain-db.postgres.database.azure.com` |
| `DB_NAME` | 데이터베이스 이름 | `smartchain` |
| `DB_USER` | 데이터베이스 사용자 | `smartchain_admin` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | `********` |

#### Azure Blob Storage

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `AZURE_STORAGE_CONNECTION_STRING` | Storage Account 연결 문자열 | `DefaultEndpointsProtocol=https;AccountName=...` |
| `AZURE_STORAGE_CONTAINER` | 컨테이너 이름 (기본값: `smartchain-files`) | `smartchain-files` |

> 연결 문자열 확인: Azure Portal → Storage Account → Access keys → Connection string

#### 이메일 발송 (SMTP)

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `MAIL_HOST` | SMTP 서버 (기본값: `smtp.gmail.com`) | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP 포트 (기본값: `587`) | `587` |
| `MAIL_USERNAME` | 발신 이메일 계정 | `noreply@smartchain.com` |
| `MAIL_PASSWORD` | 이메일 비밀번호 (Gmail: 앱 비밀번호) | `********` |

> Gmail 앱 비밀번호 발급: Google 계정 → 보안 → 2단계 인증 → 앱 비밀번호

### 1.2 선택 환경변수

#### 애플리케이션 설정

| 환경변수 | 설명 | 기본값 |
|---------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `dev` |
| `SERVER_PORT` | 서버 포트 | `8080` |

#### AI 서비스 연동 (필요 시)

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `AI_RUN_API_URL` | AI Run API 엔드포인트 | `https://ai-api.smartchain.com` |
| `AI_RUN_API_TIMEOUT` | API 타임아웃 (초) | `180` |

---

## 2. Azure 리소스 구성

### 2.1 필요한 Azure 리소스

| 리소스 | 용도 | 권장 SKU |
|--------|------|----------|
| **App Service** | 백엔드 API 서버 | B1 이상 (운영: P1v2) |
| **Azure Database for PostgreSQL** | 데이터베이스 | Flexible Server, Burstable B1ms |
| **Storage Account** | 파일 저장소 (증빙파일) | Standard LRS |

### 2.2 네트워크 구성

```
[App Service]
    ├── VNet Integration (권장)
    │       └── [PostgreSQL] (Private Endpoint)
    └── [Storage Account] (Service Endpoint 또는 Public)
```

### 2.3 PostgreSQL 설정

1. **방화벽 규칙**: App Service 아웃바운드 IP 허용
2. **SSL 연결**: 필수 (Azure 기본 설정)
3. **데이터베이스 생성**:
   ```sql
   CREATE DATABASE smartchain;
   ```

### 2.4 Storage Account 설정

1. **컨테이너 생성**: `smartchain-files`
2. **액세스 수준**: Private (Blob 단위 SAS 토큰 사용)
3. **CORS 설정** (프론트엔드 직접 업로드 시):
   - Allowed origins: `https://smartchain.com`
   - Allowed methods: `GET, PUT`

---

## 3. App Service 설정

### 3.1 일반 설정

| 설정 | 값 |
|------|-----|
| Stack | Java 17 |
| Java web server stack | Java SE (Embedded Web Server) |
| Startup Command | (비워둠 - Spring Boot 기본 사용) |

### 3.2 배포 설정

**GitHub Actions 또는 Azure DevOps 권장**

배포 시 JAR 파일 경로:
```
build/libs/platform-0.0.1-SNAPSHOT.jar
```

### 3.3 Health Check

| 설정 | 값 |
|------|-----|
| Path | `/health` |
| Interval | 30초 |

---

## 4. 환경변수 설정 예시 (전체)

```bash
# 데이터베이스
DB_HOST=smartchain-db.postgres.database.azure.com
DB_NAME=smartchain
DB_USER=smartchain_admin
DB_PASSWORD=<비밀번호>

# Azure Storage
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=smartchainstorage;AccountKey=...;EndpointSuffix=core.windows.net
AZURE_STORAGE_CONTAINER=smartchain-files

# 이메일 (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@smartchain.com
MAIL_PASSWORD=<앱 비밀번호>

# 프로파일
SPRING_PROFILES_ACTIVE=dev
```

---

## 5. 배포 후 확인사항

### 5.1 헬스체크
```bash
curl https://<app-name>.azurewebsites.net/health
# 응답: {"status":"UP"}
```

### 5.2 Swagger UI 접근
```
https://<app-name>.azurewebsites.net/swagger-ui.html
```

### 5.3 로그 확인
Azure Portal → App Service → Monitoring → Log stream

---

## 6. 트러블슈팅

### 데이터베이스 연결 실패
- PostgreSQL 방화벽에 App Service IP 추가 확인
- SSL 연결 문자열 확인: `?sslmode=require`

### 파일 업로드 실패
- Storage Account 연결 문자열 확인
- 컨테이너 존재 여부 확인

### 이메일 발송 실패
- Gmail 2단계 인증 활성화 확인
- 앱 비밀번호 사용 여부 확인 (일반 비밀번호 X)
- "보안 수준이 낮은 앱 액세스" 설정 불필요 (앱 비밀번호 사용 시)

---

## 7. 보안 권장사항

1. **환경변수 관리**: Azure Key Vault 연동 권장
2. **네트워크 격리**: VNet Integration + Private Endpoint 사용
3. **HTTPS 강제**: App Service에서 HTTPS Only 활성화
4. **접근 제한**: IP 화이트리스트 또는 Azure AD 인증

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-02-02 | 초안 작성 (이메일 설정 추가) |
