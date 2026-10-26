# Technical Requirements Document (TRD)
## AI-Powered Web API Security Gateway

## 1. Architecture

Use a small, modular, locally runnable system:

1. **React dashboard** — monitoring, request tester, logs, evaluation.
2. **Spring Boot gateway** — receives demo requests, extracts basic features, invokes rule engine and ML service, calculates final decision, logs the outcome, and only forwards allowed requests to a configured local demo upstream.
3. **Python FastAPI ML service** — loads a trained scikit-learn Isolation Forest pipeline and returns an anomaly result/score.
4. **MongoDB** — persists request logs, rule configuration, and evaluation run metadata.
5. **Training/evaluation scripts** — clean dataset, extract features, train model, evaluate each approach, save metrics and model artifacts.

The browser must call Spring Boot, not the ML service or database directly.

## 2. Technology Requirements

| Layer | Required technology | Notes |
|---|---|---|
| Frontend | React + Vite + TypeScript | Responsive admin dashboard |
| UI styling | Tailwind CSS or existing repository styling system | Choose one; avoid duplicate styling systems |
| Charts | Recharts or an existing chart library | Accessible labels and empty states |
| Gateway | Java + Spring Boot | Main API gateway and policy enforcement |
| ML API | Python + FastAPI + Uvicorn | Inference service only |
| ML | scikit-learn, pandas, NumPy, joblib | Isolation Forest baseline |
| Storage | MongoDB | Request/security logs and configuration |
| Testing | JUnit + Spring Boot Test; pytest; frontend test tooling | Unit and integration tests |
| API documentation | OpenAPI/Swagger for services | Document request/response contracts |
| Deployment | Local Docker Compose optional | AWS is a later milestone, not required for local MVP |

Pin compatible versions in project files and document them. Do not invent versions if the repository already specifies them.

## 3. Service Responsibilities

### Spring Boot gateway
- Validate incoming requests and enforce maximum body size.
- Extract safe, bounded request features.
- Run rule-based checks.
- Call ML service with a short timeout.
- Combine rule and ML signals into a risk score and action.
- Persist an event, including detector version and latency.
- Return structured decision information.
- Reject requests to arbitrary upstream URLs; only a fixed configured local demo upstream is allowed.
- For the demo tester endpoint, analyze requests without executing them against an external target.

### FastAPI ML service
- Load model and preprocessing artifacts once at startup.
- Expose `/health` and `/predict`.
- Validate feature schema and numeric bounds.
- Return anomaly prediction, normalized anomaly score if available, model version, and inference latency.
- Never accept arbitrary code, model paths, or remote file URLs from the caller.
- Return a controlled error if the artifact is missing; explain setup in README.

### React frontend
- Call documented gateway endpoints.
- Display loading, empty, error, and success states.
- Encode/escape untrusted request strings.
- Keep secrets out of browser code.
- Use reusable components for cards, filters, tables, badges, and charts.

## 4. Request Pipeline

1. Receive a request in the local gateway.
2. Normalize method/path/query/header metadata and enforce size limits.
3. Extract features.
4. Run rule checks.
5. Send the numeric feature vector to the ML service.
6. Convert rule severity and anomaly score to a documented risk score.
7. Apply the decision policy: `ALLOW`, `FLAG`, or `BLOCK`.
8. Persist the decision and safe evidence.
9. Return the decision and measured processing time.
10. Forward only an allowed request to the fixed local demo upstream, if proxy mode is enabled.

If ML inference fails, use a configurable fail-safe policy. For this academic prototype, default to `FLAG` for uncertain requests, record `ml_status=UNAVAILABLE`, and do not claim a full hybrid decision was made. Critical rule matches can still block. Make this behaviour explicit in tests and UI.

## 5. Feature Contract

Use a stable versioned feature schema. Initial features:

- `request_length`: bounded total request length in bytes.
- `url_length`: path plus query length.
- `parameter_count`: number of query/body parameters, without storing their secrets.
- `special_char_count`: count of selected suspicious punctuation characters.
- `http_method_code`: stable mapping for GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD, OTHER.
- `header_count`: number of headers, excluding header values from ML features unless explicitly justified.
- `body_length`: request body byte length.
- `request_rate_1m`: requests per minute for the configured demo client identifier, if implemented.
- `feature_schema_version`: version metadata, not necessarily an ML input.

Training and inference must use the same extraction code or shared test fixtures. Fit preprocessing only on training data to avoid leakage. Do not feed raw secrets or arbitrary user identifiers into the model.

## 6. Initial Rule Categories

Implement safe, explainable pattern rules for:
- SQL injection indicators.
- XSS indicators.
- Path traversal indicators.
- Excessive request rate.
- Invalid or oversized requests.

Rules are heuristic indicators, not a complete security guarantee. Store rule ID, title, severity, and short explanation. Avoid unsafe regex patterns that can cause catastrophic backtracking. Add tests for both positive and negative examples.

## 7. ML and Evaluation Requirements

- Baseline algorithm: Isolation Forest, as proposed in the synopsis.
- Document the target training population and contamination/threshold assumptions.
- For an anomaly detector, explicitly define how the model output becomes an anomaly score and then a binary prediction.
- Use a fixed train/validation/test split and set random seeds.
- Avoid train/test leakage; keep duplicate or near-duplicate requests from leaking across splits where feasible.
- Evaluate:
  - Precision
  - Recall
  - F1-score
  - False-positive count and rate
  - Average and p95 latency (where sample size permits)
- Compare Rule-Based, ML-Based, and Hybrid on the same held-out examples.
- Report dataset source, label mapping, class balance, split method, threshold selection, and limitations.
- Do not display fabricated metrics. If no evaluation run exists, show “Not evaluated yet.”

## 8. API Contract (Initial)

Base paths can be adjusted to match repository conventions, but keep them consistent and documented.

### `GET /api/v1/health`
Returns gateway, database, and ML service status.

### `POST /api/v1/analyze`
Accepts a safe request description for analysis. Does not forward to a real external service.

Example request:
```json
{
  "method": "GET",
  "path": "/api/products",
  "query": "page=1&limit=10",
  "headers": {
    "accept": "application/json"
  },
  "body": ""
}
```

Example response:
```json
{
  "requestId": "uuid",
  "decision": "ALLOW",
  "riskLevel": "LOW",
  "riskScore": 12,
  "reasons": [],
  "matchedRules": [],
  "ml": {
    "status": "OK",
    "isAnomaly": false,
    "anomalyScore": 0.12,
    "modelVersion": "iforest-v1"
  },
  "latencyMs": 8.4,
  "createdAt": "ISO-8601 timestamp"
}
```

### `GET /api/v1/requests`
Supports pagination and filters: `decision`, `riskLevel`, `method`, `ruleId`, `from`, `to`, `page`, `size`, `sort`.

### `GET /api/v1/requests/{requestId}`
Returns safe request metadata, decision explanation, matched rules, model version, and timings. Mask sensitive data.

### `GET /api/v1/metrics/summary`
Returns total, allowed, flagged, blocked, risk distribution, and time-series aggregates.

### `GET /api/v1/evaluation/latest`
Returns evaluation run status and metrics for each approach. Empty state if no run exists.

### `GET /api/v1/rules`
Returns rule metadata and enabled status.

### `PATCH /api/v1/rules/{ruleId}`
Updates enabled state. Protect with admin authentication if exposed beyond localhost; record audit event.

### FastAPI `GET /health`
Returns ML service health and model-loaded status.

### FastAPI `POST /predict`
Accepts the versioned feature vector and returns anomaly prediction, score, model version, and inference latency.

Use validation errors with stable JSON format; never return stack traces.

## 9. Error Handling

- `400`: invalid request shape or field bounds.
- `401/403`: unauthenticated/unauthorized admin action, if auth enabled.
- `404`: unknown request/rule ID.
- `413`: request exceeds configured body limit.
- `429`: rate limit exceeded.
- `502/503`: ML service or dependency unavailable.
- `500`: generic internal failure with correlation ID, no stack trace in response.

## 10. Observability

- Add a correlation/request ID to every analysis.
- Record decision latency and ML inference latency separately.
- Log startup configuration without printing secrets.
- Provide health checks for each service.
- Keep structured logs and explain how to inspect them.

## 11. Repository Structure

```text
ai-security-gateway/
├── frontend/                  # React + Vite + TypeScript
├── gateway/                   # Java + Spring Boot
├── ml-service/                # FastAPI inference API
├── ml/
│   ├── data/                  # ignored local datasets
│   ├── scripts/               # preprocessing, training, evaluation
│   ├── artifacts/             # ignored generated model files
│   └── reports/               # generated evaluation outputs
├── docs/                      # supplied requirements and architecture docs
├── tests/                     # cross-service/integration test assets
├── docker-compose.yml          # optional local services
├── .env.example
├── .gitignore
└── README.md
```

If the user has already provided a repository with another structure, inspect and adapt it rather than replacing it blindly.

## 12. Configuration

Provide `.env.example` with non-secret values such as:
- `MONGODB_URI`
- `ML_SERVICE_URL`
- `GATEWAY_PORT`
- `ML_SERVICE_PORT`
- `DEMO_UPSTREAM_URL` (fixed local service only)
- `MAX_REQUEST_BODY_BYTES`
- `RISK_LOW_MAX`
- `RISK_MEDIUM_MAX`
- `ML_TIMEOUT_MS`

Do not commit real credentials or cloud keys.

## 13. Non-Functional Requirements

- Local start-up instructions must be reproducible.
- API responses must have stable types and documented fields.
- Dashboard must work on desktop and mobile widths.
- Use bounded request sizes, timeouts, pagination, and database indexes.
- Tests must cover the decision policy, rules, feature extraction, and failure paths.
- Code must be modular, typed where practical, and commented where logic is non-obvious.
- Accessibility: keyboard navigation, visible focus, semantic labels, sufficient contrast.
