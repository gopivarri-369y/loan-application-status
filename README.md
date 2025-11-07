# Loan Application Status API

![License MIT](https://img.shields.io/badge/license-MIT-green.svg) ![Mule Runtime](https://img.shields.io/badge/mule%20runtime-4.4%2B-blue.svg) ![Coverage](https://img.shields.io/badge/munit_coverage-88%E2%80%9395%25-success.svg)

> A client-ready MuleSoft solution that keeps borrowers, lenders, and operations teams aligned on every loan application status update.

## Quick Summary
- **What it does:** Tracks, validates, and shares loan status changes in real time.
- **Who uses it:** Digital channels, back-office teams, and operations dashboards.
- **Why clients love it:** Fewer manual follow-ups, auditable decisions, and proactive notifications.

## Table of Contents
- [Business Value](#business-value)
- [Solution Overview](#solution-overview)
- [Architecture](#architecture)
- [Key Features](#key-features)
- [API Catalogue](#api-catalogue)
- [Sample Request & Responses](#sample-request--responses)
- [Rules & Safeguards](#rules--safeguards)
- [Data Model](#data-model)
- [Getting Started](#getting-started)
- [Running the Solution](#running-the-solution)
- [Testing & Quality](#testing--quality)
- [Deployment Options](#deployment-options)
- [Operations Playbook](#operations-playbook)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License & Contact](#license--contact)
- [Acknowledgments](#acknowledgments)

## Business Value
- **Transparent communication:** Keeps applicants and advisors informed at every milestone.
- **Compliance ready:** Enforces mandatory remarks and validates audit-critical timestamps.
- **Operational efficiency:** Reduces duplicate work and manual reconciliation.
- **Faster decisions:** Automates routing to the right downstream processes and teams.

## Solution Overview
- **Layered MuleSoft stack:** Dedicated Experience, Process, and System APIs for scalability.
- **Smart validations:** Stops invalid transitions before they reach core systems.
- **Actionable alerts:** Email notifications highlight both success and exception scenarios.
- **High reliability:** MUnit automation covers core happy paths and edge cases.

## Architecture

```mermaid
flowchart LR
    Client[Client Channels
    (Web, Mobile, Ops Portal)]
    XAPI[Experience API
    loan-application-status-x-api]
    PAPI[Process API
    loan-application-status-papi]
    SAPI[System API
    loan-application-status-db-s-api]
    DB[(Snowflake Database)]

    Client --> XAPI --> PAPI --> SAPI --> DB
    PAPI -->|Email Alerts| SMTP[SMTP Server]
```

| Layer | Description | Client Benefit |
| --- | --- | --- |
| Experience API | Public endpoint for digital channels. Handles payload validation and client-friendly responses. | Faster onboarding with a stable, well-documented contract. |
| Process API | Applies business rules, manages transitions, orchestrates persistence and notifications. | Confidence that every update respects internal policy. |
| System API | Encapsulates Snowflake operations and data access. | Protects core data while enabling rapid integration. |

## Key Features
- **End-to-end validation:** Ensures every update carries valid IDs, timestamps, and remarks.
- **Status lifecycle control:** Blocks illegal moves (for example, reverting from `Disbursed` back to `Under Review`).
- **Duplicate shielding:** Detects and ignores repeat messages so downstream analytics stay clean.
- **Automated messaging:** Notifies stakeholders about successful updates or action-required errors.
- **Rich logging:** Correlation IDs and payload snapshots make audits and RCA straightforward.

## API Catalogue

| Layer | Method & Path | Purpose |
| --- | --- | --- |
| Experience | `POST /api/loan/status/update` | Primary endpoint for submitting status changes. |
| Process | `POST /loanStatus` | Orchestrates validation and persistence. |
| Process | `POST /status` | Helper endpoint for internal workflows. |
| System | `GET /loanStatus/{loanId}` | Retrieves current status for a loan. |
| System | `POST /fetchingdata` | Supports applicant lookup and verification. |
| System | `PUT /updateloanrecords` | Saves approved status transitions. |

## Sample Request & Responses

### Example Request
```json
{
  "loanId": "LN20251005",
  "applicantId": "APP1005",
  "status": "Under Review",
  "updatedBy": "CreditOfficer01",
  "timestamp": "2025-10-28T19:00:00Z",
  "remarks": "Application received and logged in system"
}
```

### Successful Update
```json
{
  "status": "SUCCESS",
  "message": "Loan LN20251005 status updated and applicant notified successfully."
}
```

### Common Error Scenarios

| Scenario | Client-Friendly Response |
| --- | --- |
| Missing required fields | `{ "status": "failed", "reason": "required key [status] not found required key [loanId] not found" }` |
| Timestamp missing timezone | `{ "status": "failed", "reason": "/timestamp [2025-10-28T19:00:00] is not a valid date time. Expected [yyyy-MM-dd'T'HH:mm:ssZ]" }` |
| Applicant ID not found | `{ "status": "Failure", "response": "given ApplicantId APP100 is Not Found" }` |
| Loan/applicant mismatch | `{ "status": "FAILED", "reason": "Loan ID LN2025100 does not match with the APP1005" }` |
| Invalid status step | `{ "status": "FAILED", "reason": "Invalid Transition: cannot move from Received to Rejected" }` |
| Missing remarks for rejection | `{ "status": "failed", "reason": "required key [remarks] not found" }` |
| Duplicate update | `{ "status": "IGNORED", "reason": "Duplicate loan update received for LN20251001 with same status." }` |

## Rules & Safeguards
1. **Mandatory fields:** `loanId`, `applicantId`, `status`, `timestamp`, `updatedBy` always required.
2. **Timestamp format:** Enforces ISO 8601 with timezone to preserve audit integrity.
3. **Applicant validation:** Checks applicant existence before allowing updates.
4. **Loan-applicant pairing:** Rejects mismatched IDs to prevent data corruption.
5. **Remarks policy:** Enforced when status equals `Rejected` or `On Hold`.
6. **Duplicate control:** Ignores identical consecutive updates.
7. **Status guardrails:** Only allows sanctioned transitions in the lifecycle table below.

### Status Lifecycle

| Current Status | Allowed Next Status | Business Rationale |
| --- | --- | --- |
| `Received` | `Under Review` | Moves application into initial assessment. |
| `Under Review` | `Approved`, `Rejected`, `On Hold` | Decision or additional information required. |
| `Approved` | `Disbursed` | Final funding step. |
| `Disbursed` | _None_ | Terminal state; prevents accidental rollbacks. |

## Data Model

| Table | Role | Key Columns |
| --- | --- | --- |
| `LOAN_APPLICANTS` | Applicant master data. | `APPLICANT_ID`, `APPLICANT_NAME`, profile fields. |
| `LOAN_APPLICATIONS` | Status history ledger. | `LOAN_ID`, `APPLICANT_ID`, `STATUS`, `REMARKS`, `UPDATED_BY`, `TIMESTAMP`. |

## Getting Started

### Prerequisites
- Anypoint Studio 7.x or later.
- Mule Runtime 4.4.x or later.
- Java 8 or 11.
- Snowflake account (schema, warehouse, network access).
- SMTP credentials (for example, Gmail app password) for email alerts.

### Clone the Repository
```bash
git clone https://github.com/yourusername/loan-application-status-api.git
cd loan-application-status-api
```

### Configure Environment Settings
Update `src/main/resources/config.yaml` with environment-specific values:

```yaml
# Database Configuration
db:
  host: your-snowflake-host
  database: your-database-name
  schema: your-schema-name
  warehouse: your-warehouse
  username: ${secure::db.username}
  password: ${secure::db.password}

# HTTP Configuration
http:
  x-api:
    host: 0.0.0.0
    port: 8081
  p-api:
    host: localhost
    port: 8082
  s-api:
    host: localhost
    port: 8083

# Email Configuration
email:
  host: smtp.gmail.com
  port: 587
  username: ${secure::email.username}
  password: ${secure::email.password}
```

### Secure Credentials
Maintain secrets in `src/main/resources/config-secure.yaml`:

```yaml
db:
  username: "your-db-username"
  password: "your-db-password"

email:
  username: "your-email@gmail.com"
  password: "your-app-password"
```

### Import Projects into Anypoint Studio
1. Open Anypoint Studio.
2. Go to **File → Import → Anypoint Studio → Anypoint Studio project from File System**.
3. Import all three modules:
   - `loan-application-status-x-api`
   - `loan-application-status-papi`
   - `loan-application-status-db-s-api`

## Running the Solution
Start services in dependency order to avoid connection failures:
1. **System API (S-API)** — Port `8083`.
2. **Process API (P-API)** — Port `8082`.
3. **Experience API (X-API)** — Port `8081`.

> Tip: Use dedicated run configurations so each application’s logs remain easy to trace.

## Testing & Quality

### Automated Coverage (MUnit)

| API Layer | Command | Coverage |
| --- | --- | --- |
| System API | `mvn clean test -f loan-application-status-db-s-api/pom.xml` | 94.74% |
| Process API | `mvn clean test -f loan-application-status-papi/pom.xml` | 88.46% |
| Experience API | `mvn clean test -f loan-application-status-x-api/pom.xml` | 92.86% |

### Manual Regression (Postman)
Use the supplied Postman collection to validate:
- Successful status update.
- Missing field handling.
- Timestamp formatting error.
- Applicant not found scenario.
- Loan/applicant mismatch.
- Invalid transition error.
- Remarks requirement enforcement.
- Duplicate update rejection.

## Deployment Options

### CloudHub (Recommended for managed environments)
```bash
mvn clean deploy -DmuleDeploy \
  -Danypoint.username=your-username \
  -Danypoint.password=your-password \
  -Denvironment=Sandbox \
  -Dworkers=1 \
  -DworkerType=MICRO
```

### On-Premises Mule Runtime
1. Package the application:
   ```bash
   mvn clean package
   ```
2. Deploy the generated JAR from `target/` into the target Mule runtime.

## Operations Playbook

### Error Handling
- **APIKit errors:** Return descriptive messages for BAD_REQUEST, NOT_FOUND, METHOD_NOT_ALLOWED.
- **Database faults:** Include correlation IDs and key context for quick triage.
- **Business rule violations:** Provide actionable feedback to calling channels.
- **Unhandled exceptions:** Captured by global handlers to ensure consistent client experience.

### Notifications
Emails are triggered for:
- Validation failures and business rule breaches.
- Successful status updates (optional distribution lists).
- Applicant mismatches and invalid transitions.

### Logging & Auditing
- Correlation IDs trace requests from X-API to S-API.
- Entry/exit logs for every flow support performance dashboards.
- Error logs capture stack traces and business payloads (masking sensitive data where required).
- Payload snapshots enable compliance audits.

## Troubleshooting
- **Port conflicts:** Ensure `8081`, `8082`, `8083` are free before starting.
- **SMTP errors:** Confirm TLS settings and app password validity.
- **Snowflake authentication:** Check warehouse status and network policies.
- **Unexpected duplicates:** Clear deduplication cache or adjust testing cadence.
- **Timestamp validation:** Verify client payloads include timezone offset (`Z` or `+/-HH:MM`).

## Contributing
1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`.
3. Commit your updates: `git commit -m "Add some feature"`.
4. Push to origin: `git push origin feature/your-feature`.
5. Open a pull request describing changes and validation steps.

## License & Contact
- License: MIT — see `LICENSE` for details.
- Developer: Your Name
- Email: your.email@example.com
- Organization: Your Organization

## Acknowledgments
- MuleSoft Documentation
- Anypoint Platform
- Snowflake Database Documentation
