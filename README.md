# Bank Withdrawal Service

## 1. Executive Summary

This service implements a **bank account withdrawal capability** with **reliable event publication on successful withdrawal**.

It is designed using **production-grade engineering principles** commonly expected in high-scale financial systems:

- correctness under concurrency (no race conditions)
- transactional integrity at database level
- clean separation of concerns
- resilience for external integrations (SNS)
- strong observability and traceability
- testability and replaceability of infrastructure components
- lightweight deployment using in-memory H2 for local execution

### Core Business Capability

> Withdraw funds from an account if sufficient balance exists, then publish a withdrawal event.

---

## 2. Architecture Overview

### High-Level Flow


Client → Controller → Service → Repository → H2 Database
↓
EventPublisher → SNS (AWS)



### Architectural Style

- Layered Architecture (Controller / Service / Repository)
- Dependency Inversion (EventPublisher abstraction)
- Infrastructure isolation (SNS hidden behind interface)

---

## 3. Key Engineering Decisions

## 3.1 Clean Layered Architecture

System is structured into clear layers:

- Controller → API boundary (HTTP concerns only)
- Service → Business logic + orchestration
- Repository → Persistence logic only

### Why this matters

- prevents business logic leakage into controllers
- improves maintainability and onboarding speed
- enables independent testing of layers
- supports future migration to microservices

---

## 3.2 Concurrency-Safe Atomic Withdrawal

### ❌ Anti-pattern (race condition prone)


SELECT balance FROM accounts
UPDATE accounts SET balance = balance - ?


### ✔ Correct approach (atomic DB operation)


UPDATE accounts
SET balance = balance - ?
WHERE id = ?
AND balance >= ?



### Benefits

- eliminates race conditions
- prevents double spending
- enforces invariants at database level
- reduces application complexity

---

## 3.3 Transaction Boundary Strategy

The service layer is the **single transaction boundary**:

@Transactional



### Guarantees

- atomic DB updates
- rollback on failure
- consistent state transitions

---

## 3.4 Event Publishing via Abstraction Layer

Instead of directly coupling to AWS SNS:

- `EventPublisher` interface
- `SnsEventPublisher` implementation

### Benefits

- decouples business logic from cloud vendor
- enables easy swap (SNS → Kafka → RabbitMQ)
- improves unit testing (mockable dependency)
- supports multi-channel publishing in future

---

## 3.5 H2 Database for Local Execution

Uses:

H2 in-memory database

### Why H2 is used

- zero infrastructure setup
- fast startup time
- ideal for demos / interviews / local dev
- deterministic schema initialization via SQL scripts

---

## 3.6 DTO-Based API Design

External API uses explicit DTOs:

- `WithdrawalRequest`
- `WithdrawalResponse`

### Benefits

- protects internal domain model from external changes
- enables API evolution without breaking clients
- improves clarity of contract boundaries

---

## 3.7 Centralized Exception Handling

Implemented using:


@RestControllerAdvice


### Benefits

- consistent error response format
- removes boilerplate try/catch from controllers
- centralized control of HTTP error mapping

---

## 3.8 Structured Logging & Traceability

All logs include:

- business context (accountId, amount)
- correlationId (request trace identifier)

### Benefits

- production debugging support
- audit-friendly execution trace
- observability-ready structure
- easier incident root cause analysis

---

## 3.9 API Boundary Validation

Validation enforced at entry point:

- amount must be > 0
- request must not be null

### Benefits

- prevents invalid state entering business logic
- reduces defensive coding inside service layer
- enforces contract correctness early

---

## 3.10 Lightweight Event-Driven Design

On successful withdrawal:

- domain event is published to SNS

### Benefits

- enables downstream consumers:
    - fraud detection
    - notifications
    - analytics pipelines
- decouples core banking logic from side effects

---

## 4. Technology Stack

- Spring Boot (REST + DI)
- H2 Database (in-memory persistence)
- JdbcTemplate (lightweight SQL access)
- AWS SNS SDK (event publishing)
- Jackson (JSON serialization)
- Spring Retry (resilience for external calls)

---

## 5. Transaction Flow (Detailed)

1. Validate request at API boundary
2. Execute atomic withdrawal in database
3. Retrieve updated balance
4. Publish withdrawal event (with retry policy)
5. Return response to client
6. Log full execution trace with correlationId

---

## 6. Reliability & Resilience Considerations

### 6.1 SNS Publishing Failure Handling

- retries with exponential backoff
- failure does not rollback DB transaction
- errors logged with full context

> Design choice: favor financial correctness over event delivery guarantees

---

### 6.2 Trade-off: No Outbox Pattern

Current implementation:

- direct publish after DB commit

Production enhancement:

- Outbox Pattern (recommended)
- guarantees exactly-once event consistency

---

## 7. Known Trade-offs

### 7.1 SNS Inside Service Layer

Current:
- synchronous publish after DB update

Limitations:
- partial failure risk (DB success, event failure)

Future improvement:
- Outbox pattern + async dispatcher

---

### 7.2 H2 Database Limitations

- in-memory only (non-persistent)
- not suitable for production scale testing
- limited concurrency realism vs production DBs (PostgreSQL, Oracle)

---

## 8. Summary of Improvements

| Area | Improvement |
|------|------------|
| Correctness | Atomic SQL update |
| Concurrency | Race-condition elimination |
| Architecture | Clean layered separation |
| Resilience | Retry-enabled SNS publishing |
| Observability | Correlation-based logging |
| Maintainability | Interface-driven design |
| Testability | Dependency inversion |
| Portability | H2-based local runtime |
| Extensibility | Event abstraction layer |
| Fault tolerance | Controlled failure handling |

---
