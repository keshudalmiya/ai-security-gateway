# Product Requirements Document (PRD)
## AI-Powered Web API Security Gateway
**Project title:** A Lightweight Adaptive Hybrid Framework for Web API Attack Detection Using Rule-Based and Machine Learning Techniques  
**Project type:** Academic research and application-based mini project  
**Primary users:** Security administrator, API/backend developer, project evaluator  
**Document status:** Implementation baseline for Antigravity

---

## 1. Product Summary

Build a working prototype that inspects HTTP/API requests before they reach a protected demo application. It combines predefined security rules with a lightweight machine-learning anomaly detector, assigns a risk level, and produces one of three decisions:

- **Allow:** low-risk request may continue to the demo upstream application.
- **Flag:** medium-risk request is logged for administrator review. For the demo, do not silently treat this as safe; show the policy result clearly.
- **Block:** high-risk request is rejected by the gateway.

The product must also provide a React monitoring dashboard and an evaluation area comparing rule-only, ML-only, and hybrid detection.

## 2. Problem Statement

Static rules detect known patterns but may miss modified or unfamiliar attacks. An ML-only detector can find unusual behaviour but may flag legitimate requests. This project investigates whether a lightweight hybrid approach balances detection quality, false positives, and latency better than either approach alone.

## 3. Goals

1. Inspect HTTP requests and extract measurable request features.
2. Detect known suspicious patterns with transparent rules.
3. Identify anomalous requests using a lightweight Isolation Forest model.
4. Combine signals into Low, Medium, and High risk decisions.
5. Record decisions and evidence for later review.
6. Compare rule-based, ML-based, and hybrid approaches using Precision, Recall, F1-score, false positives, and latency.
7. Demonstrate the workflow through a browser-based dashboard and a safe demo API.

## 4. Non-Goals

- This is not a production-grade WAF or a replacement for a commercial security service.
- Do not deploy in front of a real public production service.
- Do not perform active attacks against third-party systems.
- No LLM is required for detection in the MVP.
- AWS deployment is optional; local development and demonstration must work without paid services.
- Do not claim zero-day detection is guaranteed. Anomaly detection only identifies deviations from learned patterns.

## 5. Target Users and Main Jobs

### Security administrator
- View current system status and request volume.
- Find flagged and blocked requests.
- Open a request to understand its risk score, matched rules, and decision.
- Review aggregate model performance.

### API developer / evaluator
- Submit safe sample requests to the demo gateway.
- Understand why a request was allowed, flagged, or blocked.
- Compare the three detection approaches using the same evaluation dataset.

## 6. MVP Features and Acceptance Criteria

### F1. Dashboard overview
- Show total requests, allowed, flagged, blocked, and recent high-risk events.
- Show risk/action distribution and request trend over time.
- Show API and ML service health.
- Empty/loading/error states must be designed.

**Acceptance:** Counts match the records returned by the backend and update after a test request.

### F2. Request inspection
- Provide a safe request-testing form for method, path, query parameters, selected headers, and body.
- Submit only to the local demo gateway.
- Display decision, risk level/score, matched rules, ML anomaly result, and processing time.

**Acceptance:** A normal sample and a clearly suspicious test sample produce visible, logged decisions. The UI must never send a test payload to an external target.

### F3. Hybrid detection
- Extract features such as request length, URL length, parameter count, special-character count, method, and basic request-rate features where available.
- Run rule checks for common patterns such as SQL injection indicators, XSS indicators, path traversal indicators, and excessive request rate.
- Run the trained Isolation Forest model on a consistent numerical feature vector.
- Combine results with documented, configurable thresholds.
- Return `ALLOW`, `FLAG`, or `BLOCK` plus reason codes.

**Acceptance:** Every analyzed request receives a deterministic response schema and an auditable decision explanation.

### F4. Request logs
- Search/filter by date, action, risk, HTTP method, and detected rule.
- Paginate results.
- View request details with sensitive fields masked.
- Store only the minimum payload required for the educational demo.

**Acceptance:** A request can be found in the logs immediately after analysis.

### F5. Rule management (MVP-lite)
- Display enabled detection rules and descriptions.
- Admin can enable/disable a rule in the local demo, if authentication is implemented.
- Never allow arbitrary executable code to be entered as a rule.

**Acceptance:** Changing a rule affects subsequent test requests and is recorded in an audit log.

### F6. Model and evaluation
- Show model status, dataset/model version, and feature list.
- Show evaluation metrics for Rule-Based, ML-Based, and Hybrid approaches.
- Display metrics only when computed from a documented test set; never invent demo metrics.
- Include a CSV export of evaluation results.

**Acceptance:** Metric values can be traced to a reproducible evaluation script and saved report.

### F7. Health and configuration
- Show gateway, ML service, and database status.
- Show current risk thresholds and safe demo mode.
- Provide a clear error state if the ML service is unavailable.

**Acceptance:** A failed ML service does not crash the UI; the backend follows an explicitly documented fallback policy and logs that fallback.

## 7. Core User Stories

- As an administrator, I want to see request and decision totals so I can understand the gateway's activity.
- As an administrator, I want to filter suspicious requests so I can inspect likely threats.
- As a tester, I want to submit a sample request so I can demonstrate the detection pipeline.
- As a reviewer, I want to see why a request was flagged or blocked so the system is explainable.
- As a researcher, I want to compare three detection approaches on the same held-out dataset.
- As a developer, I want health information so I can identify a stopped backend or ML service.

## 8. Decision Policy

Use a documented risk score in the range 0–100. Initial thresholds are configurable defaults, not scientifically validated constants:

- **Low: 0–29 → ALLOW**
- **Medium: 30–69 → FLAG**
- **High: 70–100 → BLOCK**

A high-confidence critical rule may directly cause `BLOCK`; rule severity and the exact override policy must be visible in code and documentation. Calibrate thresholds against validation data before reporting research results. Never describe the default thresholds as optimal without evidence.

## 9. Security and Privacy Requirements

- Local demo mode is the default.
- Require authentication for administrative functions if they are exposed beyond localhost.
- Validate request size, field lengths, HTTP methods, and input types.
- Do not log authorization headers, cookies, passwords, tokens, or full sensitive bodies.
- Sanitize and escape all request content displayed in the dashboard.
- Use parameterized database operations.
- Add rate limits to the test endpoint.
- Restrict proxying to a configured local demo upstream; prevent arbitrary URL forwarding and SSRF.
- Keep secrets in environment variables; commit only `.env.example`.
- Do not expose stack traces or secrets to the frontend.

## 10. Success Metrics

The product is successful when:
- The complete request-to-decision-to-log workflow works locally.
- Rule-only, ML-only, and hybrid modes can be run against the same test data.
- Precision, recall, F1-score, false-positive rate/count, and latency are measured reproducibly.
- Held-out attack categories are separated from training/tuning data where the dataset supports it.
- The dashboard clearly explains decisions and limitations.
- The team can run the application using the README.

## 11. MVP Priority

**P0:** Request analysis, rules, Isolation Forest training/inference, hybrid decision, persisted logs, dashboard, evaluation script, tests, README.  
**P1:** Rule toggles, CSV exports, richer charts, admin login.  
**Future:** AWS deployment, explainability layer, model comparisons, multi-site learning.

## 12. Assumptions and Decisions

- Use the CSIC HTTP dataset or another documented public HTTP request dataset; record the exact source, license, preprocessing, and label mapping.
- The synopsis proposes Java/Spring Boot for the gateway, Python/FastAPI for ML, MongoDB for logs, and React for the dashboard. Preserve this architecture unless repository constraints make it impractical; if changing it, document the reason before implementation.
- `FLAG` is a recorded/visible decision, not a promise that the request is safe.
- A flagged request is not forwarded in the default demo policy. Any alternative must be explicit and tested.
- This project is for academic demonstration and controlled testing only.
