# 1. 파이썬 3.9 슬림 버전 사용 (가볍고 빠름)
FROM python:3.9-slim

# 2. 시스템 패키지 업데이트 및 필수 빌드 도구 설치
RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 전체 복사
COPY . .

# 6. 포트 설정 (Azure Container App의 기본 포트인 80번 사용)
EXPOSE 80

# 7. FastAPI 서버 실행 (uvicorn)
# main.py의 app 객체를 실행합니다. 파일명이 다르면 수정하세요.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "80"]
