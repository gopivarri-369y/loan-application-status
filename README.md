# Loan Application Status API

A MuleSoft-based three-tier API architecture for managing and tracking loan application status updates with comprehensive validation, error handling, and email notifications.

## Architecture Overview

This project implements a layered API architecture following MuleSoft best practices:

- **Experience API (X-API)**: `loan-application-status-x-api` - Entry point for external clients
- **Process API (P-API)**: `loan-application-status-papi` - Business logic and orchestration layer
- **System API (S-API)**: `loan-application-status-db-s-api` - Database integration layer

## Features

- **Comprehensive Validation**: Validates loan ID, applicant ID, status transitions, timestamps, and required fields
- **Status Transition Management**: Enforces valid state transitions (e.g., prevents moving from 'Disbursed' to 'Under Review')
- **Duplicate Detection**: Prevents duplicate status updates for the same loan
- **Email Notifications**: Sends automated email alerts for failures and successful updates
- **Error Handling**: Centralized error handling with detailed error messages
- **Database Integration**: Snowflake database connectivity for data persistence
- **High Test Coverage**: ~85-95% MUnit test coverage across all layers

## API Endpoints

### Experience API (X-API)
```
POST /api/loan/status/update
```

### Process API (P-API)
```
POST /loanStatus
POST /status
```

### System API (S-API)
```
GET /loanStatus/{loanId}
POST /fetchingdata
PUT /updateloanrecords
```

## Request Payload

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

### Required Fields
- `loanId`: Unique loan identifier
- `applicantId`: Unique applicant identifier
- `status`: Current status of the loan
- `timestamp`: ISO 8601 format (yyyy-MM-dd'T'HH:mm:ssZ)
- `remarks`: Required for 'Rejected' or 'On Hold' status

## Response Examples

### Success Response
```json
{
  "status": "SUCCESS",
  "message": "Loan LN20251005 status updated and applicant notified successfully."
}
```

### Error Responses

**Missing Required Fields**
```json
{
  "status": "failed",
  "reason": "required key [status] not found required key [loanId] not found"
}
```

**Invalid Timestamp**
```json
{
  "status": "failed",
  "reason": "/timestamp [2025-10-28T19:00:00] is not a valid date time. Expected [yyyy-MM-dd'T'HH:mm:ssZ]"
}
```

**Applicant ID Not Found**
```json
{
  "status": "Failure",
  "response": "given ApplicantId APP100 is Not Found"
}
```

**Loan ID & Applicant ID Mismatch**
```json
{
  "status": "FAILED",
  "reason": "Loan ID LN2025100 does not match with the APP1005"
}
```

**Invalid Status Transition**
```json
{
  "status": "FAILED",
  "reason": "Invalid Transition: cannot move from Received to Rejected"
}
```

**Missing Remarks**
```json
{
  "status": "failed",
  "reason": "required key [remarks] not found"
}
```

**Duplicate Status**
```json
{
  "status": "IGNORED",
  "reason": "Duplicate loan update received for LN20251001 with same status."
}
```

## Validation Rules

1. **Mandatory Field Validation**: `loanId` and `status` are required
2. **Timestamp Format**: Must follow ISO 8601 format
3. **Applicant ID Verification**: Validates applicant exists in database
4. **Loan-Applicant Mapping**: Ensures applicant ID matches with loan ID
5. **Remarks Requirement**: Mandatory for 'Rejected' or 'On Hold' status
6. **Duplicate Detection**: Prevents same status updates
7. **Transition Validation**: Enforces valid status transitions

## Valid Status Transitions

The API enforces the following status transition rules:
- `Received` → `Under Review`
- `Under Review` → `Approved` / `Rejected` / `On Hold`
- `Approved` → `Disbursed`
- Invalid transitions (e.g., `Disbursed` → `Under Review`) are rejected

## Database Schema

### Tables
- **LOAN_APPLICANTS**: Stores applicant information
  - `APPLICANT_ID`
  - `APPLICANT_NAME`
  - Other applicant details

- **LOAN_APPLICATIONS**: Stores loan application records
  - `LOAN_ID`
  - `APPLICANT_ID`
  - `STATUS`
  - `REMARKS`
  - `UPDATED_BY`
  - `TIMESTAMP`

## Prerequisites

- **Anypoint Studio** 7.x or higher
- **Mule Runtime** 4.4.x or higher
- **Java** 8 or 11
- **Snowflake Database** connection
- **SMTP Configuration** for email notifications

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/loan-application-status-api.git
cd loan-application-status-api
```

### 2. Configure Environment Properties

Update `src/main/resources/config.yaml` with your environment-specific values:

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

### 3. Configure Secure Properties

Create or update `src/main/resources/config-secure.yaml`:

```yaml
db:
  username: "your-db-username"
  password: "your-db-password"

email:
  username: "your-email@gmail.com"
  password: "your-app-password"
```

### 4. Import Projects into Anypoint Studio

1. Open Anypoint Studio
2. File → Import → Anypoint Studio → Anypoint Studio project from File System
3. Import all three projects:
   - `loan-application-status-x-api`
   - `loan-application-status-papi`
   - `loan-application-status-db-s-api`

### 5. Run the Applications

Start applications in this order:
1. System API (S-API) - Port 8083
2. Process API (P-API) - Port 8082
3. Experience API (X-API) - Port 8081

## Testing

### Run MUnit Tests

Each API layer includes comprehensive MUnit test suites:

**System API Tests**
```bash
mvn clean test -f loan-application-status-db-s-api/pom.xml
```

**Process API Tests**
```bash
mvn clean test -f loan-application-status-papi/pom.xml
```

**Experience API Tests**
```bash
mvn clean test -f loan-application-status-x-api/pom.xml
```

### Test Coverage
- **X-API**: 92.86%
- **P-API**: 88.46%
- **S-API**: 94.74%

### Manual Testing with Postman

Import the provided Postman collection and test various scenarios:

1. **Success Scenario**
2. **Missing Fields Validation**
3. **Invalid Timestamp Format**
4. **Applicant Not Found**
5. **Loan-Applicant Mismatch**
6. **Invalid Status Transition**
7. **Missing Remarks**
8. **Duplicate Status Detection**

## Deployment

### Deploy to CloudHub

```bash
mvn clean deploy -DmuleDeploy \
  -Danypoint.username=your-username \
  -Danypoint.password=your-password \
  -Denvironment=Sandbox \
  -Dworkers=1 \
  -DworkerType=MICRO
```

### Deploy to On-Premises

1. Package the application:
```bash
mvn clean package
```

2. Deploy the generated JAR from `target/` to your Mule Runtime

## Project Structure

```
loan-application-status-api/
├── loan-application-status-x-api/
│   ├── src/main/mule/
│   │   ├── loan-application-status-x-api-main.xml
│   │   ├── calling-process-api.xml
│   │   └── global-error-handlers.xml
│   └── src/test/munit/
│       └── loan-application-status-x-api-test-suite.xml
├── loan-application-status-papi/
│   ├── src/main/mule/
│   │   ├── loan-application-status-papi.xml
│   │   ├── validating-newstaus.xml
│   │   ├── common-flows.xml
│   │   └── global-error-handlers.xml
│   └── src/test/munit/
│       └── loan-application-status-papi-test-suite.xml
└── loan-application-status-db-s-api/
    ├── src/main/mule/
    │   ├── loan-application-status-db-sapi-main.xml
    │   ├── loan-application-status-db-sapi.xml
    │   └── global-error-handlers.xml
    └── src/test/munit/
        └── loan-application-status-db-sapi-test-suite.xml
```

## Error Handling

The API implements a centralized error handling strategy:

- **APIKit Errors**: BAD_REQUEST, NOT_FOUND, METHOD_NOT_ALLOWED
- **Database Errors**: Connection failures, SQL syntax errors
- **Validation Errors**: Business rule violations
- **System Errors**: Unexpected runtime exceptions

All errors are logged with correlation IDs for traceability.

## Email Notifications

Automated email notifications are sent for:
- Validation failures
- Business rule violations
- Successful status updates
- Applicant ID mismatches
- Invalid transitions

## Logging

The application uses structured logging with:
- **Correlation ID**: For request tracing
- **Flow Entry/Exit**: For performance monitoring
- **Error Details**: For debugging
- **Payload Snapshots**: For audit trails

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact:
- **Developer**: Your Name
- **Email**: your.email@example.com
- **Organization**: Your Organization

## Acknowledgments

- MuleSoft Documentation
- Anypoint Platform
- Snowflake Database Documentation
