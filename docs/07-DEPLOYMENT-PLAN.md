# Deployment Plan & Operations Manual

## 1. Architecture Topology

The **AI-Powered Web API Security Gateway** is organized as a resilient multi-container distributed application:

```
[ Client / Web Browser ]
           │
           ▼
[ Frontend SPA (Nginx on :5173) ]
           │ /api/ proxy
           ▼
[ Spring Boot Gateway (:8080) ] ◄── (Fail-safe In-Memory Ring Buffer Fallback)
      │                     │
      ▼                     ▼
[ FastAPI ML (:8000) ]   [ MongoDB Database (:27017) ]
(Isolation Forest v1)     (Collections: request_events, security_rules, audit_events, evaluation_runs)
```

---

## 2. Port Mapping and Network Layout

| Service | Internal Port | Host Port | Purpose | Health Check Endpoint |
|---|---|---|---|---|
| **frontend** | 5173 | 5173 | React Dashboard UI + Reverse Proxy | `http://localhost:5173/` |
| **gateway** | 8080 | 8080 | Core Security Inspection Engine & REST API | `http://localhost:8080/api/v1/health` |
| **ml-service** | 8000 | 8000 | Python Isolation Forest Anomaly Scoring | `http://localhost:8000/health` |
| **mongodb** | 27017 | 27017 | Security Events & Rules Persistence | `db.adminCommand('ping')` via `mongosh` |

---

## 3. Deployment Modes

### Mode A: Full Containerized Stack (Recommended for Production / Evaluation)
Run the entire 4-tier stack using Docker Compose:

```bash
# From workspace root
docker compose up --build -d
```

Check service status and container health:
```bash
docker compose ps
docker compose logs -f
```

To gracefully tear down:
```bash
docker compose down -v
```

### Mode B: Local Development / Hybrid Run
For active development without Docker:

1. **Start Python ML Microservice:**
   ```bash
   cd ml-service
   pip install -r requirements.txt
   uvicorn main:app --host 127.0.0.1 --port 8000 --reload
   ```

2. **Start Spring Boot Gateway:**
   ```bash
   cd gateway
   gradle bootRun
   ```
   *(Note: If MongoDB is not running locally, the gateway automatically activates its thread-safe in-memory ring buffer fallback, allowing all dashboard and analysis endpoints to operate without errors).*

3. **Start React Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Access the dashboard at `http://localhost:5173`.

---

## 4. Environment Variables Reference

| Variable | Default Value | Description |
|---|---|---|
| `GATEWAY_PORT` | `8080` | Port for the Spring Boot Gateway server |
| `MONGODB_URI` | `mongodb://localhost:27017/security_gateway` | MongoDB connection URI |
| `ML_SERVICE_URL` | `http://127.0.0.1:8000` | URL of the Python FastAPI ML microservice |
| `ML_TIMEOUT_MS` | `1500` | Maximum wait time for ML inference before fail-safe fallback |
| `RISK_LOW_MAX` | `29` | Maximum risk score for ALLOW decision (0-29) |
| `RISK_MEDIUM_MAX` | `69` | Maximum risk score for FLAG decision (30-69); >=70 is BLOCK |
| `MAX_REQUEST_BODY_BYTES` | `65536` | Maximum permissible payload size (64 KB) |
| `RATE_LIMIT_PER_MINUTE` | `60` | Client rate limit per IP per minute |
| `VITE_API_BASE_URL` | `/api/v1` | Base URL used by the frontend for gateway communication |

---

## 5. Fail-Safe and High-Availability Policies

1. **ML Service Outage or Latency Exceeded:**
   - When the ML microservice is unreachable or fails to respond within `ML_TIMEOUT_MS` (1,500 ms), the gateway:
     - Sets ML prediction status to `UNAVAILABLE`.
     - Assigns a default fail-safe ML risk score (40).
     - Forces decision to at least `FLAG` (flagged for human analyst inspection) unless a critical rule commands `BLOCK`.
     - Never silently permits uninspected traffic if rules or anomaly detectors cannot evaluate it.

2. **MongoDB Outage or Connectivity Loss:**
   - If MongoDB fails to connect or throws an exception, the `EventPersistenceService` captures the failure, logs a concise warning, and diverts all incoming `RequestEvent` records into an in-memory `ConcurrentLinkedDeque` (retaining the most recent 1,000 events).
   - Metrics calculation, request log queries, and test evaluations continue seamlessly from memory without dropping HTTP requests or failing client traffic.

3. **Data Redaction and Privacy:**
   - Sensitive authorization headers (`Bearer`, `Basic`, `ApiKey`) and passwords in request bodies are automatically redacted (`[REDACTED]`) before persistence and logging.

---

## 6. Verification and Smoke Testing Checklist

1. **ML Service Health:**
   `curl http://localhost:8000/health` → `{"status":"UP","modelLoaded":true}`
2. **Gateway Health:**
   `curl http://localhost:8080/api/v1/health` → `{"status":"UP","services":{"gateway":"UP",...}}`
3. **Allow Flow:**
   `POST /api/v1/analyze` with benign GET request → `decision: ALLOW, riskScore: 0-29`
4. **Flag Flow:**
   `POST /api/v1/analyze` with structural anomaly → `decision: FLAG, riskScore: 30-69`
5. **Block Flow:**
   `POST /api/v1/analyze` with SQL injection `' OR '1'='1` → `decision: BLOCK, riskScore: 70-100`
6. **Dashboard UI:**
   Navigate to `http://localhost:5173` → Verify Overview KPIs, Request Tester, Security Logs, Evaluation Benchmark, and Active Rules.
