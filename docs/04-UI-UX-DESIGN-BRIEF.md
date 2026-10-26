# UI/UX Design Brief
## AI-Powered Web API Security Gateway

## 1. Design Direction

Create a polished, professional cybersecurity admin console: dark navy/slate surfaces, restrained electric-blue accents, clear risk colors, compact data visualization, and excellent readability. The interface should feel like a practical security operations dashboard, not a marketing website.

### Design principles
- Prioritize status, decisions, and explanations over decoration.
- Use consistent labels: `ALLOW`, `FLAG`, `BLOCK`; risk levels `LOW`, `MEDIUM`, `HIGH`.
- Never communicate risk by color alone; include text and icons.
- Use readable tables, stable spacing, visible focus states, and responsive layouts.
- Avoid fake activity, fabricated metrics, or hard-coded “live” values.
- Include skeleton/loading, empty, error, and success states on every data-driven screen.
- Default to a local-demo banner; do not imply the tool protects a production system.

## 2. Global Layout

### Desktop
- Left sidebar, approximately 240 px, with product mark and navigation.
- Top bar with page title, environment badge (`LOCAL DEMO`), service health indicator, and user/admin menu if authentication exists.
- Main content area with consistent 24 px spacing and a readable max width.
- Sidebar links: Overview, Request Tester, Request Logs, Evaluation, Rules, System Health.

### Mobile
- Collapsible navigation drawer.
- KPI cards stack vertically.
- Tables become cards or allow deliberate horizontal scrolling.
- Keep primary actions visible and forms usable on touch screens.

### Visual tokens
- Background: deep slate/navy.
- Surfaces: slightly lighter slate; subtle borders.
- Primary action: blue.
- Low/Allow: green; Medium/Flag: amber; High/Block: red.
- Use risk colors only for meaningful status.
- Typography: Inter or system sans-serif.
- Radius: 10–14 px; subtle shadows only where useful.
- Use a consistent icon set such as Lucide.

## 3. Screen 1 — Login / Demo Entry

**Purpose:** Provide a clear entry point without pretending authentication exists if it has not been implemented.

**Layout**
- Centered compact panel with product name, short description, and security-gateway illustration/icon.
- If authentication is implemented: email/username and password fields, show/hide password, validation messages.
- If authentication is not implemented: a single “Open Local Demo” action with a notice that the prototype is for local academic use only.
- Footer: project title and version.

**States:** invalid credentials, loading, backend unavailable.

**Acceptance:** No fake login or hard-coded credential bypass. If demo-only access is used, label it honestly.

## 4. Screen 2 — Overview Dashboard

**Purpose:** Quickly answer “What is the gateway seeing, and what needs attention?”

**Top row**
- Page title: Overview.
- Last updated timestamp.
- Refresh action.

**KPI cards**
- Total analyzed requests.
- Allowed.
- Flagged.
- Blocked.
- ML service status.

**Main panels**
- Request volume trend over a selectable time range.
- Decision distribution chart.
- Recent high-risk events table with timestamp, method/path, risk, action, and reason.
- System status panel for Gateway, ML Service, and Database.

**Interactions**
- Clicking a KPI filters Request Logs.
- Clicking an event opens Request Details.
- Refresh reloads data from API.

**States:** no data yet; API unavailable; partial dependency outage.

**Acceptance:** All counts come from API data, not hard-coded sample numbers.

## 5. Screen 3 — Request Tester

**Purpose:** Safely demonstrate analysis of example HTTP/API requests.

**Form fields**
- HTTP method dropdown: GET, POST, PUT, PATCH, DELETE.
- Path input (must begin with `/`).
- Query string input.
- Optional headers editor with a small allowlist; never ask for real credentials.
- Body textarea with size limit and “sample data only” hint.
- “Analyze Request” primary action.
- “Load Safe Example” secondary action.

**Result panel**
- Decision: ALLOW / FLAG / BLOCK.
- Risk level and 0–100 score.
- Matched rules with IDs and human-readable reasons.
- ML status, anomaly result/score, model version.
- Total processing latency and ML inference latency.
- Request ID and link to saved log.

**UX requirements**
- Disable submit while processing.
- Show validation errors beside fields.
- Do not execute the entered request against an external host.
- Never render a supplied payload as HTML; display it as escaped text.
- Include examples for normal traffic and benign security-test strings.

**Acceptance:** The user can submit a sample, see a result, and open its log record.

## 6. Screen 4 — Request Logs

**Purpose:** Find and investigate analyzed requests.

**Filters**
- Date range.
- Decision: all / ALLOW / FLAG / BLOCK.
- Risk: all / LOW / MEDIUM / HIGH.
- HTTP method.
- Rule ID.
- Search by request ID or path, with safe text handling.

**Table columns**
- Time.
- Method.
- Path (truncated safely).
- Risk score.
- Risk level.
- Decision.
- Primary reason.
- Latency.
- View details.

**Interactions**
- Server-side pagination and sorting.
- Clear filters.
- Export current filtered data to CSV if supported.
- Click a row to open Request Details.

**States:** no records, no matching filters, loading, failed request.

**Acceptance:** Filter counts and rows reflect backend results and do not leak secrets.

## 7. Screen 5 — Request Details

**Purpose:** Explain a single decision and make it auditable.

**Sections**
1. Header with request ID, timestamp, decision, and risk score.
2. Decision explanation with risk level and reason codes.
3. Rule results: rule ID, title, severity, matched/not matched.
4. ML result: service status, anomaly flag/score, model version.
5. Feature summary: request length, URL length, parameter count, special-character count, method code, body length. Do not expose secret values.
6. Timing: total latency and model inference latency.
7. Sanitized request metadata: method, path, safe headers, body preview only when appropriate.
8. Back to logs.

**Acceptance:** Every explanation corresponds to saved backend data; no fabricated reason text.

## 8. Screen 6 — Evaluation & Comparison

**Purpose:** Present the academic research results honestly.

**Header**
- Dataset name/version.
- Evaluation run timestamp.
- Test set size and split method.
- “Run evaluation” action only if safely implemented as a backend job; otherwise provide generated-report viewing.

**Comparison table**
- Approach: Rule-Based, ML-Based, Hybrid.
- Precision.
- Recall.
- F1-score.
- False-positive count/rate.
- Average latency and p95 latency when available.

**Charts**
- Precision/recall/F1 comparison.
- False-positive comparison.
- Latency comparison.

**Research notes**
- Data source and label mapping.
- Thresholds and model version.
- Limitations and held-out attack categories.
- Export report/CSV.

**Empty state:** “No evaluation results yet. Run the evaluation script to generate reproducible metrics.”

**Acceptance:** Never fill metrics with made-up sample values. Display “—” or “Not evaluated yet” when absent.

## 9. Screen 7 — Rules

**Purpose:** Make the rule engine understandable and optionally configurable.

**Table columns**
- Rule ID.
- Rule name.
- Category.
- Severity.
- Short explanation.
- Enabled/disabled status.
- Last updated.

**Initial categories:** SQL injection indicators, XSS indicators, path traversal indicators, excessive request rate, invalid/oversized request.

**Interactions**
- Search/filter by category or status.
- Rule detail drawer/modal with examples and limitations.
- Toggle enabled status only if admin authorization and audit logging are implemented.
- Confirm before changing a rule.

**Acceptance:** No arbitrary code editor; all rules are predefined and safely validated.

## 10. Screen 8 — System Health & Settings

**Purpose:** Show whether components are working and expose safe demo configuration.

**Status cards**
- Spring Boot gateway.
- FastAPI ML service and model-loaded state.
- MongoDB connectivity.
- Latest model version.
- Last successful evaluation run.

**Configuration summary**
- Risk thresholds.
- Request size limit.
- ML timeout.
- Demo upstream status (fixed local address only; do not make it editable to arbitrary URLs in the UI).

**Interactions**
- Refresh health checks.
- Copy diagnostic request ID.
- Show clear setup hints for a missing model or unavailable database.

**Acceptance:** Do not expose credentials, environment secrets, or internal stack traces.

## 11. Shared Components

- `AppShell`, `Sidebar`, `TopBar`
- `KpiCard`, `StatusIndicator`, `DecisionBadge`, `RiskBadge`
- `DataTable`, `FilterBar`, `Pagination`
- `EmptyState`, `ErrorState`, `LoadingSkeleton`
- `RequestForm`, `DecisionSummary`, `RuleMatchList`
- `MetricCard`, `ComparisonChart`
- `ConfirmDialog`, `Toast`, `CopyButton`

## 12. Accessibility and Content

- Label every input and chart.
- Ensure keyboard navigation and focus visibility.
- Use text plus color for risk/action indicators.
- Use concise explanations and avoid unexplained acronyms.
- Format timestamps consistently and clarify timezone.
- Respect reduced-motion settings.
