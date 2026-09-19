# Sentinel AML

Sentinel AML is a transaction monitoring prototype built using **Spring Boot, Angular, and PostgreSQL**.

The application ingests customer, account, and transaction data, evaluates transactions against multiple AML detection rules, calculates a risk score, creates and de-duplicates alerts, and allows analysts to manage alerts and cases with a complete audit trail.

---

## 1. Project Overview

The system is designed around the following flow:

```text
                    ┌──────────────────────┐
                    │      Angular UI      │
                    │  Dashboard / Alerts  │
                    │    Cases / Details   │
                    └──────────┬───────────┘
                               │ REST APIs
                               ▼
                    ┌──────────────────────┐
                    │    Spring Boot API   │
                    │                      │
                    │ Controllers          │
                    │ Services             │
                    │ Validation           │
                    └──────────┬───────────┘
                               │
                 ┌─────────────┼─────────────┐
                 │             │             │
                 ▼             ▼             ▼
          ┌────────────┐ ┌────────────┐ ┌───────────────┐
          │ Ingestion  │ │ Detection  │ │ Alert / Case  │
          │ Service    │ │ Engine     │ │ Management    │
          └─────┬──────┘ └─────┬──────┘ └───────┬───────┘
                │              │                 │
                │              ▼                 ▼
                │       ┌──────────────┐   ┌──────────────┐
                │       │ Risk Scoring │   │ Audit Events │
                │       └──────────────┘   └──────────────┘
                │
                ▼
          ┌──────────────────┐
          │    PostgreSQL    │
          │                  │
          │ Customer         │
          │ Account          │
          │ Transaction      │
          │ Alert            │
          │ Case             │
          │ AuditEvent       │
          └──────────────────┘
```

The backend is responsible for transaction processing and AML detection, while Angular provides the analyst-facing dashboard.

---

# 2. Technology Stack

### Backend

* Java
* Spring Boot
* Spring Data JPA / Hibernate
* PostgreSQL
* REST APIs
* Swagger / OpenAPI
* Basic API authentication

### Frontend

* Angular
* TypeScript
* Angular services
* Angular routing
* Alert dashboard
* Case management UI

### Database

* PostgreSQL
* Relational entity model
* Customer → Account → Transaction relationships
* Alert and Case lifecycle
* Audit history

---

# 3. Application Architecture

The application follows a layered architecture.

```text
Controller Layer
       │
       ▼
Service Layer
       │
       ├── Ingestion
       ├── Detection
       ├── Risk Scoring
       ├── Alert Management
       ├── Case Management
       └── Audit Management
       │
       ▼
Repository Layer
       │
       ▼
PostgreSQL
```

### Controller Layer

Responsible for:

* Receiving HTTP requests
* Request validation
* Returning HTTP responses
* Exposing REST APIs
* Handling API errors

### Service Layer

Contains the main business logic:

* Transaction ingestion
* INR normalization
* Detection rules
* Risk calculation
* Alert de-duplication
* Case management
* Audit event creation

### Repository Layer

Responsible for persistence using Spring Data JPA.

---

# 4. Core Data Model

The application contains the following main entities:

```text
Customer
   │
   └── 1:N
        │
        ▼
      Account
        │
        └── 1:N
             │
             ▼
        Transaction
             │
             ▼
       Detection Rules
             │
             ▼
           Alert
             │
             ▼
            Case
             │
             ▼
        AuditEvent
```

## Customer

Stores customer information such as:

* Customer ID
* Name
* KYC identifier
* Country
* Risk rating
* Created date

## Account

Stores:

* Account ID
* Customer ID
* Account number
* Account type
* Currency
* Opening date
* Status

## Transaction

Stores:

* Transaction ID
* Account ID
* Amount
* Currency
* INR normalized amount
* Transaction type
* Counterparty
* Counterparty country
* Channel
* Transaction timestamp
* Created date

## Alert

Stores suspicious activity identified by the detection engine.

Important fields include:

* Alert ID
* Customer / Account
* Alert type
* Risk score
* Status
* Explanation
* Evidence transaction IDs
* Created date
* Updated date

## Case

A case is created when an analyst needs to investigate an alert.

Contains:

* Case ID
* Alert ID
* Assigned analyst
* Status
* Disposition reason
* Analyst notes
* Created date
* Closed date

## AuditEvent

Tracks important state changes.

Contains:

* Alert / Case ID
* Actor
* Previous state
* New state
* Action
* Timestamp
* Reason

The intended relationship is that alert/case state changes generate audit records.

---

# 5. Transaction Ingestion

The system supports multiple ingestion mechanisms.

## Customer / Account / Transaction CSV Import

CSV files can be used to import:

* Customers
* Accounts
* Transactions

The import process validates the incoming records before persisting them.

---

## JSON Bulk Transaction Ingestion

Multiple transactions can be submitted through a bulk JSON request.

```text
JSON Request
     │
     ▼
Validate records
     │
     ▼
Validate account/currency/type
     │
     ▼
Normalize INR
     │
     ▼
Persist transactions
     │
     ▼
Run detection rules
     │
     ▼
Create/update alerts
```

---

## CSV Bulk Transaction Ingestion

The system also supports transaction ingestion through CSV.

The bulk processing flow is:

```text
CSV File
   │
   ▼
Parse rows
   │
   ▼
Validate each row
   │
   ├── Valid ───────► Process transaction
   │
   └── Invalid ─────► Record row + reason
```

The bulk response can report accepted, rejected, and alerted records.

This follows the intended bulk-ingestion design where invalid records are reported with their row number and reason.

---

# 6. Single Transaction Ingestion

A single transaction follows this processing pipeline:

```text
POST Transaction
       │
       ▼
Validate request
       │
       ▼
Validate Account
       │
       ▼
Validate Currency
       │
       ▼
Validate Transaction Type
       │
       ▼
Convert Amount → INR
       │
       ▼
Save Transaction
       │
       ▼
Run Detection Rules
       │
       ▼
Calculate Risk Score
       │
       ▼
De-duplicate Alert
       │
       ▼
Return Transaction + Alert
```

The intended single-transaction flow validates the account and transaction fields, normalizes currency, persists the transaction, runs detection, scores the result, and then de-duplicates matching alerts.

---

# 7. INR Normalization

Transactions may be received in different currencies.

The application normalizes the transaction amount into INR before applying AML rules.

```text
Original Amount
      │
      ▼
Original Currency
      │
      ▼
Exchange Rate
      │
      ▼
INR Normalized Amount
      │
      ▼
Detection Engine
```

This allows detection thresholds to be evaluated consistently regardless of the original transaction currency.

---

# 8. AML Detection Engine

The detection engine evaluates each transaction against multiple rules.

```text
                    Transaction
                         │
                         ▼
              TransactionDetectionService
                         │
        ┌────────────────┼─────────────────┐
        │        │       │       │         │
        ▼        ▼       ▼       ▼         ▼
      Large   Struct.  Rapid   High-Risk  Behavior
      Txn     Pattern  Movement Jurisdiction Deviation
        │        │       │       │         │
        └────────┴───────┴───────┴─────────┘
                         │
                         ▼
                 Rule Results
                         │
                         ▼
                  Risk Scoring
```

The detection service executes the enabled rules for each transaction.

---

# 9. Implemented Detection Rules

## 9.1 Large Transaction

Detects transactions exceeding the configured threshold.

Example:

```text
Normalized Amount >= INR 10,000
                │
                ▼
             Alert
```

Configured threshold:

```text
INR 10,000
```

---

## 9.2 Structuring

Detects multiple transactions designed to remain individually below the configured threshold.

The rule evaluates transactions from the same account within a time window.

Example:

```text
₹9,500
₹9,800
₹9,200
   │
   └── Same account + 24 hours
             │
             ▼
       Structuring Alert
```

The configured prototype criteria are at least three transactions between INR 9,000 and INR 9,999 within 24 hours.

---

# 10. Rapid Movement

Detects funds that move out of an account shortly after being deposited.

Example:

```text
Deposit
₹100,000
    │
    │ within configured window
    ▼
Transfer / Withdrawal
₹85,000
    │
    ▼
Rapid Movement Alert
```

The implemented rule uses the configured percentage and time window to identify rapid movement. The prototype specification uses 80% and 48 hours.

---

# 11. High-Risk Jurisdiction

The transaction's country or counterparty country is checked against the configured high-risk/sanctioned list.

```text
Transaction
     │
     ▼
Country / Counterparty
     │
     ▼
High-Risk List
     │
 ┌───┴────┐
 │        │
Match    No Match
 │        │
 ▼        ▼
Alert    Continue
```

---

# 12. Behavioral Deviation

The system compares current transaction behavior against the customer's historical activity.

Conceptually:

```text
Today's Transaction Value
          │
          ▼
Compare with
90-Day Rolling Average
          │
          ▼
Significant Deviation?
       │       │
      Yes      No
       │       │
       ▼       ▼
     Alert   Continue
```

The prototype configuration uses a multiplier of 3 and a 90-day history.

---

# 13. Round-Number Pattern

Detects suspicious repeated round-number transactions or repeated transactions close to configured thresholds.

Examples:

```text
₹10,000
₹10,000
₹10,000
```

or:

```text
₹9,900
₹9,800
₹9,700
```

This rule can act as a supporting signal for other suspicious activity.

---

# 14. Risk Scoring

Each detection rule contributes a risk weight.

Example weights:

| Rule                   | Weight |
| ---------------------- | -----: |
| Large Transaction      |     40 |
| Structuring            |     80 |
| Rapid Movement         |     75 |
| High-Risk Jurisdiction |     90 |
| Behavioral Deviation   |     60 |
| Round-Number Pattern   |     25 |

The final risk score is capped at **100**.

```text
Triggered Rules
      │
      ▼
Calculate Score
      │
      ▼
Score > 100 ?
   │        │
  Yes       No
   │        │
   ▼        ▼
  100      Score
```

The prototype specification defines these example rule weights and explicitly caps the final score at 100.

---

# 15. Alert De-duplication

The system prevents multiple alerts from being created for the same suspicious pattern.

Before creating a new alert, the system checks for an existing matching open alert based on:

* Customer / Account
* Rule type or pattern
* Relevant time window

If a matching alert already exists:

```text
Existing Alert Found
        │
        ▼
Update Alert
 ├── Risk Score
 ├── Explanation
 └── Evidence
```

Instead of:

```text
Create Alert 1
Create Alert 2
Create Alert 3
Create Alert 4
...
```

This prevents duplicate alerts for the same suspicious activity.

---

# 16. Alert Sorting

The alert dashboard sorts alerts by risk score.

```text
Highest Risk
     ↓
100
 90
 80
 65
 40
     ↓
Lowest Risk
```

This allows analysts to see higher-risk alerts first.

The intended alert queue ordering is risk score descending, followed by creation time.

---

# 17. Alert Status Validation

Alerts follow a controlled lifecycle.

```text
             ┌──────────────┐
             │     OPEN     │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │  IN_REVIEW   │
             └──────┬───────┘
                    │
             ┌──────┴───────┐
             ▼              ▼
        ┌──────────┐   ┌───────────┐
        │ CLEARED  │   │ ESCALATED │
        └──────────┘   └───────────┘
```

Invalid status transitions are rejected.

Alerts are not physically deleted.

The intended lifecycle is `OPEN → IN_REVIEW → CLEARED/ESCALATED`.

---

# 18. Case Management

An alert can be converted into a case for analyst investigation.

```text
Alert
  │
  ▼
Create Case
  │
  ├── Assign Analyst
  │
  ├── Add Notes
  │
  ├── Review Evidence
  │
  ├── Clear
  │
  └── Escalate
```

Case information includes:

* Assigned analyst
* Analyst notes
* Disposition
* Case status
* Audit history

---

# 19. Audit Trail

Every important alert/case status transition creates an audit event.

```text
OPEN
 │
 │ Analyst changes status
 ▼
IN_REVIEW
 │
 └──────► AuditEvent
             │
             ├── Actor
             ├── Previous State
             ├── New State
             ├── Action
             ├── Reason
             └── Timestamp
```

This provides traceability for analyst actions.

The intended design requires every status transition to create an immutable `AuditEvent`.

---

# 20. REST API Structure

The application uses versioned REST APIs.

### Transaction APIs

```http
POST /api/v1/customers
POST /api/v1/accounts
POST /api/v1/transactions
POST /api/v1/transactions/bulk
GET  /api/v1/transactions/{id}
```

### Alert APIs

```http
GET   /api/v1/alerts
GET   /api/v1/alerts/{id}
PATCH /api/v1/alerts/{id}/status
POST  /api/v1/alerts/{id}/case
```

### Case APIs

```http
GET   /api/v1/cases
GET   /api/v1/cases/{id}
PATCH /api/v1/cases/{id}/assign
PATCH /api/v1/cases/{id}/disposition
GET   /api/v1/cases/{id}/audit
```

## These endpoint groups correspond to the planned ingestion, alert, and case-management API structure.

# 21. API Authentication

Basic API authentication has been implemented to protect backend APIs.

The frontend sends authenticated requests to the Spring Boot backend.

```text
Angular
   │
   │ Authentication
   ▼
Spring Boot
   │
   ▼
Protected REST API
```

---

# 22. CORS

CORS configuration has been added to allow the Angular frontend to communicate with the Spring Boot backend during local development.

```text
Angular Application
       │
       │ HTTP Request
       ▼
Spring Boot API
       │
       ▼
CORS Validation
       │
       ▼
Controller
```

---

# 23. Swagger / OpenAPI

Swagger/OpenAPI is enabled for API documentation and testing.

It provides:

* Available endpoints
* Request models
* Response models
* HTTP methods
* API testing

This makes it easier to verify backend APIs without requiring the Angular application.

---

# 24. Consistent Error Handling

The backend provides consistent JSON error responses.

Example structure:

```json
{
  "timestamp": "2026-09-19T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid transaction amount",
  "path": "/api/v1/transactions"
}
```

This gives the frontend a predictable structure for displaying API errors.

---

# 25. Angular Dashboard

The starter Angular Users feature was removed and replaced with an AML-focused interface.

The frontend is organized around:

```text
Dashboard
   │
   ├── Alerts
   │     └── Alert Details
   │
   ├── Customers
   │     └── Customer Details
   │
   └── Cases
         └── Case Details
```

The planned AML routes include dashboard, alert, customer, and case views.

---

# 26. Alert Dashboard

The dashboard provides visibility into suspicious activity.

It can display:

* Total open alerts
* High-risk alerts
* Alert rule types
* Recent alerts
* Risk scores
* Alert status

The alert queue supports risk-score sorting and status/rule/date filtering.

---

# 27. Alert Details

The alert details screen provides information required for investigation:

```text
Alert
 │
 ├── Risk Score
 │
 ├── Alert Status
 │
 ├── Triggered Rules
 │
 ├── Explanation
 │
 ├── Evidence Transactions
 │
 ├── Transaction Timeline
 │
 └── Create Case
```

This allows an analyst to understand why the transaction was flagged and inspect the supporting evidence.

---

# 28. Security / Privacy Considerations

The application is designed around AML investigation data.

Important considerations include:

* Customer information should be masked where appropriate.
* Real customer information should not be used for synthetic demonstrations.
* Database credentials should be supplied through environment variables.
* Alerts should not be physically deleted.
* Audit history should remain available.

The original implementation specification explicitly recommends synthetic data and avoiding real customer information.

---

# 29. End-to-End Transaction Flow

The complete implemented flow can be summarized as:

```text
             Transaction Input
                    │
          ┌─────────┴─────────┐
          │                   │
        JSON                 CSV
          │                   │
          └─────────┬─────────┘
                    ▼
             Validate Input
                    │
                    ▼
             Validate Account
                    │
                    ▼
              INR Conversion
                    │
                    ▼
             Save Transaction
                    │
                    ▼
          Detection Rule Engine
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
     Large      Structuring   Rapid Movement
       │            │            │
       ├────────────┼────────────┤
       ▼            ▼            ▼
  High-Risk    Behavioral    Round Number
 Jurisdiction   Deviation      Pattern
       │            │            │
       └────────────┼────────────┘
                    ▼
              Rule Results
                    │
                    ▼
              Risk Scoring
                    │
                    ▼
             Cap Score at 100
                    │
                    ▼
             Alert De-duplication
                    │
                    ▼
             Create / Update Alert
                    │
                    ▼
             Sort by Risk Score
                    │
                    ▼
              Analyst Review
                    │
                    ▼
               Create Case
                    │
                    ▼
             Assign / Investigate
                    │
              ┌─────┴─────┐
              ▼           ▼
           Cleared     Escalated
              │           │
              └─────┬─────┘
                    ▼
              Audit Event
```

---

# 30. Implementation Summary

The following features have been implemented:

* [x] Customer entity
* [x] Account entity
* [x] Transaction entity
* [x] Alert entity
* [x] Case entity
* [x] AuditEvent entity
* [x] Customer CSV import
* [x] Account CSV import
* [x] Transaction CSV import
* [x] JSON bulk transaction ingestion
* [x] CSV bulk transaction ingestion
* [x] Single transaction ingestion
* [x] INR normalization
* [x] Large transaction detection
* [x] Structuring detection
* [x] Rapid movement detection
* [x] High-risk jurisdiction detection
* [x] Behavioral deviation detection
* [x] Round-number pattern detection
* [x] Risk scoring
* [x] Risk score capped at 100
* [x] Alert de-duplication
* [x] Alert sorting by risk score
* [x] Alert status validation
* [x] Case status management
* [x] Audit event creation
* [x] Basic API authentication
* [x] CORS configuration
* [x] Swagger/OpenAPI
* [x] Consistent JSON error responses
* [x] Angular AML alert dashboard
* [x] Starter Users feature removed
* [x] Backend compilation successful
* [x] Angular compilation successful

---

# 31. Architecture at a Glance

```text
                         SENTINEL AML
                              │
             ┌────────────────┴────────────────┐
             │                                 │
             ▼                                 ▼
       Angular Frontend                  Spring Boot
             │                                 │
       ┌─────┼─────┐                  ┌───────┼────────┐
       │     │     │                  │       │        │
       ▼     ▼     ▼                  ▼       ▼        ▼
   Dashboard Alerts Cases        Ingestion Detection  Case
                                       │       │       Management
                                       │       │
                                       ▼       ▼
                                    Transaction Rules
                                             │
                                             ▼
                                        Risk Scoring
                                             │
                                             ▼
                                           Alerts
                                             │
                                             ▼
                                           Cases
                                             │
                                             ▼
                                        AuditEvents
                                             │
                                             ▼
                                         PostgreSQL
```

---

# 32. Build Verification

The current implementation has been verified to compile successfully on both sides:

```text
Backend
Spring Boot
    │
    └── Compilation: SUCCESS


Frontend
Angular
    │
    └── Compilation: SUCCESS
```

This confirms that the implemented backend and Angular application are buildable.

---

# 33. Future Enhancements

The following items can be considered for future versions:

* Kafka-based transaction streaming
* Advanced authentication / OAuth2
* Role-based access control
* Machine-learning-based anomaly detection
* Graph-based transaction analysis
* Rule versioning
* Advanced rule configuration UI
* SAR document generation
* Production-grade monitoring and observability

Kafka and machine learning were also identified as items that could be postponed from the initial prototype.

---

# 34. Demo Flow

A typical demonstration can be performed as follows:

1. Start PostgreSQL.
2. Start the Spring Boot backend.
3. Start the Angular application.
4. Import customers and accounts.
5. Import or submit transactions.
6. Submit a normal transaction.
7. Submit a large transaction.
8. Submit structuring transactions.
9. Submit a rapid-movement transaction sequence.
10. Submit a high-risk jurisdiction transaction.
11. Open the alert dashboard.
12. Verify alerts are sorted by risk score.
13. Open an alert.
14. Review triggered rules and evidence.
15. Create a case.
16. Assign the case to an analyst.
17. Change the case/alert status.
18. Add a disposition reason.
19. Open the audit history.
20. Verify the state transition was recorded.

The expected end result is that a reviewer can submit a transaction, immediately see whether it was flagged, understand the reason and evidence, create a case, change its status, and verify the resulting audit history.

---

## Conclusion

Sentinel AML provides an end-to-end transaction monitoring workflow:

**Ingestion → Validation → INR Normalization → Detection → Risk Scoring → Alert De-duplication → Analyst Review → Case Management → Audit Trail**

The architecture separates the Angular presentation layer from the Spring Boot business layer and PostgreSQL persistence layer, while the detection engine provides the core AML transaction-monitoring capability.
