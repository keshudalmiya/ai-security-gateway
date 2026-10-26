# Application Flow
## AI-Powered Web API Security Gateway

## 1. Overall System Flow

```mermaid
flowchart TD
    A[User / Test Client] --> B[React Dashboard]
    B --> C[Spring Boot API Gateway]
    C --> D[Validate and Normalize Request]
    D --> E[Feature Extraction]
    E --> F[Rule-Based Detection]
    E --> G[FastAPI ML Service]
    F --> H[Hybrid Risk Engine]
    G --> H
    H --> I{Decision}
    I -->|Low risk| J[ALLOW]
    I -->|Medium risk| K[FLAG]
    I -->|High risk / critical rule| L[BLOCK]
    J --> M[Persist Safe Security Event]
    K --> M
    L --> M
    M --> N[MongoDB]
    M --> O[Dashboard / Logs / Metrics]
    P[Training Dataset] --> Q[Preprocessing and Feature Extraction]
    Q --> R[Train Isolation Forest]
    R --> S[Model Artifact]
    S --> G
    P --> T[Evaluation Script]
    T --> U[Rule vs ML vs Hybrid Metrics]
    U --> N
```

## 2. Request Analysis Flow

1. Tester opens **Request Tester**.
2. Tester chooses a method and enters a path, query, optional safe headers, and optional body.
3. Frontend validates basic required fields and sends the request description to `POST /api/v1/analyze`.
4. Gateway validates sizes and input types.
5. Gateway extracts the versioned numerical feature vector.
6. Rule engine evaluates known suspicious patterns and request-rate rules.
7. Gateway calls the FastAPI model service with the feature vector.
8. Hybrid engine calculates risk and selects `ALLOW`, `FLAG`, or `BLOCK`.
9. Gateway persists a sanitized event and timing data.
10. Frontend displays the result and links to its request detail/log entry.
11. If a real proxy demonstration is enabled, only `ALLOW` requests may be forwarded to the fixed local demo upstream. The request tester must not forward to arbitrary URLs.

## 3. Risk Decision Flow

```mermaid
flowchart TD
    A[Normalized Request] --> B[Rule Checks]
    A --> C[ML Anomaly Score]
    B --> D{Critical rule matched?}
    D -->|Yes| E[BLOCK]
    D -->|No| F[Combine rule severity and ML score]
    F --> G{Risk score}
    G -->|0-29| H[LOW / ALLOW]
    G -->|30-69| I[MEDIUM / FLAG]
    G -->|70-100| J[HIGH / BLOCK]
    C --> F
    H --> K[Log decision]
    I --> K
    J --> K
```

The score ranges are initial defaults only. Keep thresholds configurable and calibrate them with validation data before research claims. If the ML service is unavailable, record the degraded state and follow the documented fallback policy.

## 4. Dashboard Navigation Flow

```mermaid
flowchart LR
    A[Login / Demo Entry] --> B[Overview]
    B --> C[Request Tester]
    B --> D[Request Logs]
    D --> E[Request Details]
    B --> F[Evaluation]
    B --> G[Rules]
    B --> H[System Health]
    C --> E
    F --> I[Export Results]
```

## 5. Screen-by-Screen Flow

- **Login / Demo Entry:** authenticate if admin login is included; otherwise enter the local demo dashboard with a clear “Local demo” label.
- **Overview:** view counts and trends; shortcuts open logs, tester, evaluation, or health.
- **Request Tester:** submit a request description; show analysis progress and result.
- **Request Logs:** filter, search, sort, paginate; select a row to open details.
- **Request Details:** inspect decision, score, matched rules, ML status, timings, and sanitized metadata.
- **Evaluation:** compare three approaches and export computed results.
- **Rules:** view rule descriptions and, if protected admin control is implemented, toggle rules.
- **System Health:** view gateway, ML model/service, and database health plus current configuration.
- **Not Found / Error:** show a helpful explanation and a route back to Overview.
