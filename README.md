# AI-Powered Web API Security Gateway

> **A Lightweight Adaptive Hybrid Framework for Web API Attack Detection Using Rule-Based and Machine Learning Techniques**

An enterprise-grade, real-time API security gateway and cyber defense monitoring dashboard that combines deterministic signature-based inspection with unsupervised machine learning (Isolation Forest) anomaly detection to protect modern REST and microservice APIs against known and zero-day threats.

---

## Architecture Overview

```
                      ┌──────────────────────────────────────────────┐
                      │              Client / Browser                │
                      └──────────────────────┬───────────────────────┘
                                             │ HTTP
                                             ▼
                      ┌──────────────────────────────────────────────┐
                      │   Frontend Dashboard (React + Vite + TS)     │
                      │       Reverse Proxy / Static SPA (Port 5173) │
                      └──────────────────────┬───────────────────────┘
                                             │ /api/v1/*
                                             ▼
┌───────────────────────────────────────────────────────────────────────────────────┐
│                    Spring Boot Security Gateway (Port 8080)                       │
│                                                                                   │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌──────────────────────┐  │
│  │ Feature Extractor     │  │ Deterministic Rules   │  │ Hybrid Risk Engine   │  │
│  │ (8-Feature Schema v1) │  │ (SQLi, XSS, Traversal)│  │ (Score + Override)   │  │
│  └───────────┬───────────┘  └───────────┬───────────┘  └───────────┬──────────┘  │
│              │                          │                          │             │
└──────────────┼──────────────────────────┼──────────────────────────┼─────────────┘
               │                          │                          │
               ▼                          │                          ▼
┌──────────────────────────────┐          │          ┌──────────────────────────────┐
│  FastAPI ML Microservice     │          │          │   MongoDB Database (:27017)  │
│  (Port 8000)                 │          │          │   (With In-Memory Ring Buffer│
│  - Isolation Forest v1       │          │          │    Resilience Fallback)      │
│  - CSIC 2010 Calibrated Score│          │          └──────────────────────────────┘
└──────────────────────────────┘          │
                                          ▼
                      ┌──────────────────────────────────────────────┐
                      │ Target Upstream API (Protected Destination)  │
                      └──────────────────────────────────────────────┘
```

---

## Key Features

1. **Deterministic Rule Engine (OWASP Top 10):**
   - High-throughput regex pattern matching for SQL Injection (`' OR '1'='1`, `UNION SELECT`), Cross-Site Scripting (`<script>`, `onerror=`), Path Traversal (`../../etc/passwd`), payload size limits, and rate limiting.
   - Dynamic enable/disable rule toggling at runtime via REST API.

2. **Machine Learning Anomaly Detection:**
   - Unsupervised **Isolation Forest** detector trained on **20,000 normal requests** from the **CSIC 2010 HTTP Dataset**.
   - Extracts an 8-dimensional feature vector: `requestLength`, `urlLength`, `parameterCount`, `specialCharCount`, `httpMethodCode`, `headerCount`, `bodyLength`, and `requestRate1m`.
   - Returns calibrated anomaly scores normalized from `0.0` (normal) to `1.0` (highly anomalous).

3. **Hybrid Decision Engine:**
   - Combines deterministic rule severities with ML anomaly scores:
     $$\text{Risk Score} = \min(100, \text{Rule Penalty} + (\text{Normalized ML Score} \times 70))$$
   - **ALLOW (0–29):** Clean traffic routed to upstream.
   - **FLAG (30–69):** Suspicious traffic logged and routed with security headers for auditing.
   - **BLOCK (70–100):** High-risk traffic rejected immediately (`HTTP 403 Forbidden`).
   - **Critical Rule Override:** Any Critical rule match automatically triggers an immediate `BLOCK` regardless of ML output.

4. **Fail-Safe & Outage Resilience:**
   - **ML Outage:** If the Python ML service fails or exceeds the 1,500 ms SLA timeout, the gateway enters fail-safe mode, marks prediction as `UNAVAILABLE`, sets risk to `FLAG` (40), and prevents uninspected pass-through.
   - **Database Outage:** If MongoDB is offline, the gateway automatically diverts events to an internal thread-safe `ConcurrentLinkedDeque` (retaining 1,000 recent events), allowing continuous operation without dropping user requests.

5. **Modern Cybersecurity Dashboard:**
   - Built with React 19, Vite, and TypeScript.
   - Features: Real-time KPI cards, interactive Request Tester with attack presets, live Event Logs with search and CSV export, Model Evaluation comparison, and Active Rules management.

---

## Authentic Research Evaluation Benchmark

Evaluated on **3,000 held-out requests** from the genuine CSIC 2010 HTTP Dataset (1,500 normal, 1,500 attack vectors):

| Detector Model | Precision | Recall | F1-Score | False Positives | False Positive Rate | Avg Latency |
|---|---|---|---|---|---|---|
| **Rule-Based Only** | **1.0000** | 0.1187 | 0.2122 | **0** | **0.00%** | **0.03 ms** |
| **ML-Based (Isolation Forest)** | 0.6988 | **0.6280** | **0.6615** | 406 | 27.07% | 3.74 ms |
| **Hybrid Framework (Proposed)** | **0.8255** | 0.2460 | **0.3790** | **78** | **5.20%** | 3.77 ms |

### Key Research Findings
- **Zero False-Positive Signature Baseline:** Rule engine achieves 100% precision on known signatures, but misses novel structural variations.
- **Novel Anomaly Coverage:** Isolation Forest catches abnormal payloads without signatures, providing 5.3x higher recall.
- **Optimal Trade-Off in Hybrid Mode:** The hybrid pipeline cuts the ML false positive rate from **27.07% down to 5.20%** while achieving superior precision (82.55%) and sub-4ms decision latency.

---

## Technology Stack

- **Gateway Backend:** Java 21, Spring Boot 3.3.5, Spring Actuator, Springdoc OpenAPI 2.6.0, Gradle 9.1
- **Machine Learning Service:** Python 3.11+, FastAPI, Uvicorn, Scikit-Learn 1.6, Joblib
- **Frontend Dashboard:** React 19, TypeScript, Vite, Lucide Icons, Pure CSS Design System
- **Database:** MongoDB 7.0 (with in-memory circular buffer fallback)
- **Containerization:** Docker, Docker Compose, Multi-stage Dockerfiles, Nginx Alpine

---

## Quick Start with Docker Compose

Ensure Docker Desktop is running, then execute:

```bash
docker compose up --build -d
```

### Access Services:
- **Frontend Dashboard:** [http://localhost:5173](http://localhost:5173)
- **Spring Boot Gateway API:** [http://localhost:8080](http://localhost:8080)
- **Interactive Swagger Documentation:** [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
- **FastAPI ML Health:** [http://localhost:8000/health](http://localhost:8000/health)

To tear down containers:
```bash
docker compose down
```

---

## Local Development (Without Docker)

### 1. Python ML Microservice
```bash
cd ml-service
python -m venv venv
# On Windows:
.\venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
uvicorn main:app --host 127.0.0.1 --port 8000 --reload
```

### 2. Spring Boot Gateway
```bash
cd gateway
# Ensure Java 21+ is configured
gradle bootRun
```
*Note: MongoDB is optional. If not running, the gateway activates its internal circular ring-buffer fallback automatically.*

### 3. React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## API Endpoints Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/health` | Gateway, ML microservice, and database health check |
| `POST` | `/api/v1/analyze` | Primary endpoint: inspects request and returns Allow/Flag/Block decision |
| `GET` | `/api/v1/requests` | Query recent security events with filtering and pagination |
| `GET` | `/api/v1/requests/{requestId}` | Detailed security event inspection |
| `GET` | `/api/v1/metrics/summary` | Aggregated telemetry (total requests, decisions, latencies, attack breakdown) |
| `GET` | `/api/v1/rules` | List all active and disabled deterministic rules |
| `PATCH` | `/api/v1/rules/{ruleId}` | Toggle rule status (`enabled: true/false`) |
| `GET` | `/api/v1/evaluation/latest` | Retrieve authentic CSIC 2010 benchmark evaluation metrics |
| `ANY` | `/api/v1/demo/upstream` | Demo protected upstream service returning mock catalogue items |

---

## Testing the Detection Flow

### 1. Benign Request (ALLOW)
```bash
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "method": "GET",
    "path": "/api/products?category=electronics",
    "headers": {"User-Agent": "Mozilla/5.0"},
    "clientIp": "192.168.1.50"
  }'
```
*Expected Decision:* `ALLOW` (Risk score: 0–29).

### 2. Suspicious Request (FLAG)
```bash
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "method": "POST",
    "path": "/api/upload?debug=true&bypass=0",
    "headers": {"User-Agent": "curl/7.68.0"},
    "clientIp": "192.168.1.75",
    "body": "{\"meta\":\"%25%25%25%25%25\"}"
  }'
```
*Expected Decision:* `FLAG` (Risk score: 30–69).

### 3. Attack Request (BLOCK)
```bash
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "method": "POST",
    "path": "/api/login",
    "headers": {"User-Agent": "Mozilla/5.0"},
    "clientIp": "10.0.0.99",
    "body": "username=admin&password=\x27 OR \x271\x27=\x271"
  }'
```
*Expected Decision:* `BLOCK` (Risk score: 70–100, Matched Rule: `RULE-SQLI-001`).

---

## Running Automated Test Suites

### Python ML Service Tests
```bash
pytest ml-service/test_ml_service.py -v
```
*(4 unit tests covering `/health`, normal prediction, anomalous prediction, and validation errors).*

### Spring Boot Gateway Tests
```bash
cd gateway
gradle test
```
*(11 unit and integration tests covering rule engine, hybrid risk scoring, feature extraction, and fail-safe fallback).*

### Frontend Production Build
```bash
cd frontend
npm run build
```
*(TypeScript type-checking and Vite bundling into `dist/`).*

---

## License

Academic and Educational Use — Developed as a collegiate engineering demonstration of hybrid AI cybersecurity gateway architectures.
