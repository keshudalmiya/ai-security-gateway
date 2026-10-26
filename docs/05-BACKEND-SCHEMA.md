# Backend Schema
## AI-Powered Web API Security Gateway

This document defines the initial MongoDB collections and cross-service data contracts. Keep persisted records minimal and sanitize untrusted request data.

## 1. Collections

### 1.1 `request_events`

One document per analyzed request.

```json
{
  "_id": "ObjectId",
  "requestId": "uuid",
  "createdAt": "ISODate",
  "method": "GET",
  "path": "/api/products",
  "querySummary": "page=1&limit=10",
  "requestLength": 128,
  "urlLength": 24,
  "parameterCount": 2,
  "specialCharCount": 1,
  "headerCount": 3,
  "bodyLength": 0,
  "decision": "ALLOW",
  "riskLevel": "LOW",
  "riskScore": 12,
  "reasons": [],
  "matchedRules": [],
  "ml": {
    "status": "OK",
    "isAnomaly": false,
    "anomalyScore": 0.12,
    "modelVersion": "iforest-v1",
    "latencyMs": 2.4
  },
  "detectorMode": "HYBRID",
  "totalLatencyMs": 8.4,
  "featureSchemaVersion": "v1",
  "sourceLabel": null,
  "correlationId": "uuid"
}
```

Rules:
- `decision`: enum `ALLOW | FLAG | BLOCK`.
- `riskLevel`: enum `LOW | MEDIUM | HIGH`.
- `detectorMode`: enum `RULE | ML | HYBRID`.
- `ml.status`: enum `OK | UNAVAILABLE | ERROR | NOT_RUN`.
- Do not store authorization headers, cookies, passwords, tokens, or full raw bodies.
- If request snippets are retained for the educational demo, redact secrets, cap length, and document retention.
- `sourceLabel` is only for offline evaluation records, not a guessed production label.

### 1.2 `security_rules`

```json
{
  "_id": "ObjectId",
  "ruleId": "SQLI-001",
  "name": "SQL injection indicator",
  "category": "SQL_INJECTION",
  "description": "Detects selected suspicious SQL syntax patterns",
  "severity": "HIGH",
  "enabled": true,
  "version": 1,
  "updatedAt": "ISODate",
  "updatedBy": "admin-or-system"
}
```

Enums:
- `category`: `SQL_INJECTION | XSS | PATH_TRAVERSAL | RATE_LIMIT | REQUEST_VALIDATION`
- `severity`: `LOW | MEDIUM | HIGH | CRITICAL`

Do not store executable code supplied through the UI. Rule implementations remain in reviewed application code, keyed by `ruleId`.

### 1.3 `audit_events`

```json
{
  "_id": "ObjectId",
  "createdAt": "ISODate",
  "actor": "admin-or-system",
  "action": "RULE_ENABLED",
  "targetType": "RULE",
  "targetId": "SQLI-001",
  "details": {
    "previousEnabled": false,
    "newEnabled": true
  },
  "correlationId": "uuid"
}
```

Avoid putting secrets or full request bodies into audit details.

### 1.4 `evaluation_runs`

```json
{
  "_id": "ObjectId",
  "runId": "uuid",
  "createdAt": "ISODate",
  "datasetName": "CSIC HTTP dataset",
  "datasetVersion": "documented-source-version",
  "splitStrategy": "train-validation-test",
  "seed": 42,
  "testSampleCount": 0,
  "labelMapping": {
    "normal": 0,
    "malicious": 1
  },
  "modelVersion": "iforest-v1",
  "thresholdConfig": {
    "lowMax": 29,
    "mediumMax": 69
  },
  "metrics": {
    "ruleBased": {
      "precision": null,
      "recall": null,
      "f1": null,
      "falsePositiveCount": null,
      "falsePositiveRate": null,
      "avgLatencyMs": null,
      "p95LatencyMs": null
    },
    "mlBased": {},
    "hybrid": {}
  },
  "reportPath": null,
  "notes": []
}
```

Use `null` until metrics are actually computed. Do not insert placeholder values as if they were results.

### 1.5 `system_config` (optional for MVP)

```json
{
  "_id": "ObjectId",
  "key": "risk_thresholds",
  "value": {
    "lowMax": 29,
    "mediumMax": 69,
    "highMin": 70
  },
  "updatedAt": "ISODate",
  "updatedBy": "admin-or-system"
}
```

Keep secrets in environment variables, not in this collection.

## 2. Indexes

Create indexes at startup or through a migration/setup script:

- `request_events`: unique index on `requestId`.
- `request_events`: descending index on `createdAt`.
- `request_events`: compound index on `decision`, `createdAt`.
- `request_events`: compound index on `riskLevel`, `createdAt`.
- `request_events`: index on `matchedRules.ruleId` if supported by the chosen document structure.
- `security_rules`: unique index on `ruleId`.
- `evaluation_runs`: descending index on `createdAt`.
- `audit_events`: descending index on `createdAt`.

For a local academic demo, consider a documented retention policy or TTL index only after confirming it will not remove records needed for evaluation.

## 3. API Data Transfer Objects

### Analyze request

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

Validate method against an allowlist. `path` must be relative and start with `/`; reject absolute URLs, protocol-relative paths, CRLF, and overlong values. Limit headers and body size.

### Analyze response

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

These values are illustrative contract examples only, not actual measured results.

### Matched rule object

```json
{
  "ruleId": "SQLI-001",
  "name": "SQL injection indicator",
  "severity": "HIGH",
  "matched": true,
  "reason": "A configured SQL injection indicator matched"
}
```

### ML predict request

```json
{
  "featureSchemaVersion": "v1",
  "features": {
    "requestLength": 128,
    "urlLength": 24,
    "parameterCount": 2,
    "specialCharCount": 1,
    "httpMethodCode": 0,
    "headerCount": 3,
    "bodyLength": 0,
    "requestRate1m": 2
  }
}
```

### ML predict response

```json
{
  "status": "OK",
  "isAnomaly": false,
  "anomalyScore": 0.12,
  "modelVersion": "iforest-v1",
  "latencyMs": 2.4
}
```

Normalize Isolation Forest outputs into a documented score convention; do not assume its raw `decision_function` already means “probability of attack.”

## 4. Recommended Error Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The request description is invalid.",
    "correlationId": "uuid",
    "fieldErrors": {
      "path": "Path must start with /"
    }
  }
}
```

Do not return stack traces, secrets, raw internal exceptions, or full unsafe request payloads.

## 5. Data Lifecycle

1. Accept bounded request metadata.
2. Analyze in memory.
3. Save a sanitized event and decision.
4. Return only safe fields to the dashboard.
5. Use a separately versioned offline dataset for training and evaluation.
6. Keep generated model files, raw datasets, local secrets, and logs out of Git unless licensing and privacy review explicitly permits otherwise.
