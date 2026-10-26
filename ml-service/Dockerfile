FROM python:3.11-slim

WORKDIR /app

# Install system dependencies if required
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy requirements and install
COPY ml-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy service code
COPY ml-service/ /app/ml-service/
COPY ml/artifacts/ /app/ml/artifacts/

WORKDIR /app/ml-service

EXPOSE 8000

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
