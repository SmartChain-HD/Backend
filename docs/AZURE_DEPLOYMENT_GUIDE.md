# Azure 배포 가이드

SmartChain 백엔드 시스템을 Azure Container Apps (ACA)에 배포할 때 필요한 설정 안내입니다.

## 아키텍처

```
GitHub Repository
       │
       ▼ (수동 클론)
   로컬 머신
       │
       ▼ (docker build & push)
Azure Container Registry (ACR)
       │
       ▼ (자동 배포)
Azure Container Apps (ACA)
       │
       ├── Azure Database for PostgreSQL
       ├── Azure Blob Storage
       └── Gmail SMTP
```

---

## 1. 이미지 빌드 및 배포 절차

### 1.1 레포지토리 클론

```bash
git clone https://github.com/SmartChain-HD/Backend.git
cd Backend
```

### 1.2 Docker 이미지 빌드

```bash
docker build -t smartchain-backend:latest .
```

### 1.3 ACR 로그인 및 푸시

```bash
# ACR 로그인
az acr login --name <ACR_NAME>

# 이미지 태깅
docker tag smartchain-backend:latest <ACR_NAME>.azurecr.io/smartchain-backend:latest
docker tag smartchain-backend:latest <ACR_NAME>.azurecr.io/smartchain-backend:$(date +%Y%m%d-%H%M%S)

# ACR에 푸시
docker push <ACR_NAME>.azurecr.io/smartchain-backend:latest
docker push <ACR_NAME>.azurecr.io/smartchain-backend:$(date +%Y%m%d-%H%M%S)
```

> ACR에 푸시하면 ACA에 자동 배포됩니다.

---

## 2. 환경변수 설정

Azure Portal → Container Apps → [앱 이름] → Settings → Environment variables

### 2.1 필수 환경변수

#### 애플리케이션 프로파일

| 환경변수 | 값 | 설명 |
|---------|-----|------|
| `SPRING_PROFILES_ACTIVE` | `dev` | 활성 프로파일 |

#### 데이터베이스 (PostgreSQL)

| 환경변수 | 값 | 설명 |
|---------|-----|------|
| `DB_HOST` | `smartchain-db.postgres.database.azure.com` | PostgreSQL 호스트 |
| `DB_NAME` | `smartchain` | 데이터베이스 이름 |
| `DB_USER` | `smartchain_admin` | 사용자명 |
| `DB_PASSWORD` | `********` | 비밀번호 |

#### Azure Blob Storage

| 환경변수 | 값 | 설명 |
|---------|-----|------|
| `AZURE_STORAGE_CONNECTION_STRING` | `DefaultEndpointsProtocol=https;...` | Storage 연결 문자열 |
| `AZURE_STORAGE_CONTAINER` | `smartchain-files` | 컨테이너 이름 (기본값 있음) |

> 연결 문자열: Azure Portal → Storage Account → Access keys → Connection string

#### 이메일 발송 (Gmail SMTP)

| 환경변수 | 값 | 설명 |
|---------|-----|------|
| `MAIL_HOST` | `smtp.gmail.com` | SMTP 서버 (기본값 있음) |
| `MAIL_PORT` | `587` | SMTP 포트 (기본값 있음) |
| `MAIL_USERNAME` | `noreply@example.com` | Gmail 계정 |
| `MAIL_PASSWORD` | `********` | Gmail 앱 비밀번호 |

> Gmail 앱 비밀번호: Google 계정 → 보안 → 2단계 인증 → 앱 비밀번호

### 2.2 전체 환경변수 예시

```bash
SPRING_PROFILES_ACTIVE=dev

# Database
DB_HOST=smartchain-db.postgres.database.azure.com
DB_NAME=smartchain
DB_USER=smartchain_admin
DB_PASSWORD=<비밀번호>

# Azure Storage
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net
AZURE_STORAGE_CONTAINER=smartchain-files

# Email (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<Gmail 계정>
MAIL_PASSWORD=<앱 비밀번호>
```

---

## 3. Container Apps 설정

### 3.1 컨테이너 설정

| 설정 | 값 |
|------|-----|
| Image source | Azure Container Registry |
| Image | `<ACR_NAME>.azurecr.io/smartchain-backend:latest` |
| CPU | 0.5 cores (운영: 1.0) |
| Memory | 1.0 Gi (운영: 2.0) |

### 3.2 Ingress 설정

| 설정 | 값 |
|------|-----|
| Ingress | Enabled |
| Ingress type | HTTP |
| Target port | `8080` |
| Insecure connections | Disabled (HTTPS only) |

### 3.3 스케일링 설정

| 설정 | 값 |
|------|-----|
| Min replicas | 1 |
| Max replicas | 3 (운영: 10) |
| Scale rule | HTTP concurrent requests > 100 |

### 3.4 Health Probe 설정

| 설정 | 값 |
|------|-----|
| Type | HTTP |
| Path | `/health` |
| Port | 8080 |
| Initial delay | 30초 |
| Period | 30초 |

---

## 4. Azure 리소스 구성

### 4.1 필요한 리소스

| 리소스 | 용도 | 권장 SKU |
|--------|------|----------|
| **Container Apps Environment** | ACA 호스팅 환경 | Consumption |
| **Container Registry** | Docker 이미지 저장소 | Basic |
| **Azure Database for PostgreSQL** | 데이터베이스 | Flexible Server, Burstable B1ms |
| **Storage Account** | 파일 저장소 | Standard LRS |

### 4.2 PostgreSQL 설정

1. **연결 허용**: Container Apps Environment의 아웃바운드 IP 허용
2. **SSL**: 필수 (Azure 기본)
3. **데이터베이스 생성**:
   ```sql
   CREATE DATABASE smartchain;
   ```

### 4.3 Storage Account 설정

1. **컨테이너 생성**: `smartchain-files`
2. **액세스 수준**: Private

---

## 5. 배포 후 확인

### 5.1 헬스체크

```bash
curl https://<app-name>.<region>.azurecontainerapps.io/health
# 응답: {"status":"UP"}
```

### 5.2 Swagger UI

```
https://<app-name>.<region>.azurecontainerapps.io/swagger-ui.html
```

### 5.3 로그 확인

Azure Portal → Container Apps → [앱 이름] → Monitoring → Log stream

또는 CLI:
```bash
az containerapp logs show --name <app-name> --resource-group <rg-name> --follow
```

---

## 6. 트러블슈팅

### 컨테이너 시작 실패

```bash
# 로그 확인
az containerapp logs show --name <app-name> --resource-group <rg-name>
```

- `SPRING_PROFILES_ACTIVE=dev` 설정 확인
- 환경변수 누락 확인

### 데이터베이스 연결 실패

- PostgreSQL 방화벽에 ACA 아웃바운드 IP 추가
- `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` 확인

### 파일 업로드 실패

- `AZURE_STORAGE_CONNECTION_STRING` 연결 문자열 확인
- 컨테이너 `smartchain-files` 존재 여부 확인

### 이메일 발송 실패

- Gmail 2단계 인증 활성화 확인
- 앱 비밀번호 사용 (일반 비밀번호 X)

---

## 7. 빠른 배포 스크립트

인프라팀용 배포 스크립트 예시:

```bash
#!/bin/bash
set -e

ACR_NAME="smartchainacr"
IMAGE_NAME="smartchain-backend"
TAG=$(date +%Y%m%d-%H%M%S)

echo "=== SmartChain Backend 배포 ==="

# 1. 레포 클론 (또는 pull)
if [ -d "Backend" ]; then
    cd Backend && git pull origin dev
else
    git clone https://github.com/SmartChain-HD/Backend.git && cd Backend
fi

# 2. 이미지 빌드
echo ">>> Docker 이미지 빌드 중..."
docker build -t $IMAGE_NAME:$TAG .

# 3. ACR 로그인
echo ">>> ACR 로그인..."
az acr login --name $ACR_NAME

# 4. 태깅 및 푸시
echo ">>> ACR에 푸시 중..."
docker tag $IMAGE_NAME:$TAG $ACR_NAME.azurecr.io/$IMAGE_NAME:$TAG
docker tag $IMAGE_NAME:$TAG $ACR_NAME.azurecr.io/$IMAGE_NAME:latest

docker push $ACR_NAME.azurecr.io/$IMAGE_NAME:$TAG
docker push $ACR_NAME.azurecr.io/$IMAGE_NAME:latest

echo "=== 배포 완료: $TAG ==="
```

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-02-02 | ACR + ACA 환경 기준으로 재작성 |
