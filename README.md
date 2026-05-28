# Bank Withdrawal Service – Technical Submission

### Business Capability

- Withdraw money from a bank account
- Return updated balance and status
- Publish a withdrawal event to AWS SNS

---

## 🧠 Approach Summary

### Architecture Style

- Layered architecture:
  - Controller → Service → Repository → Messaging
- Clear separation of concerns
- Single responsibility per layer

---

### Core Business Flow (Unchanged)

1. Receive withdrawal request (accountId, amount)
2. Validate input
3. Atomically deduct balance from database
4. If successful:
  - Retrieve updated balance
  - Publish event to AWS SNS
  - Return success response
5. If unsuccessful:
  - If account does not exist → `AccountNotFoundException`
  - Otherwise → `InsufficientFundsException`

---

## ⚙️ Implementation Choices

### 1. Atomic Balance Update (Concurrency Safe)

```sql
UPDATE accounts
SET balance = balance - ?
WHERE id = ?
AND balance >= ?

```

# ⚙️ Implementation Details & Design Decisions

---

## 1. Atomic Balance Update (Concurrency Safety)

### Why

- Prevents race conditions
- Avoids double spending
- Ensures data consistency
- Reduces database round trips

---

## 2. Service Layer Design

- All business logic resides in `WithdrawalService`
- Controller remains thin and HTTP-focused

### Why

- Improves testability
- Improves maintainability
- Follows clean architecture principles

---

## 3. SNS Event Publishing Separation

- SNS logic is isolated in `SnsEventPublisher`

### Why

- Decouples business logic from infrastructure
- Allows future replacement (Kafka / RabbitMQ)
- Improves modularity

---

## 4. Exception Handling Strategy

### Custom Exceptions

- `AccountNotFoundException`
- `InsufficientFundsException`

### Global Handling

- `GlobalExceptionHandler`

### Why

- Consistent API error responses
- Centralized error handling
- Cleaner service logic

---

## 5. Immutable DTOs (Java Records)

### Used for

- Request
- Response
- Event

### Why

- Thread-safe
- Less boilerplate
- Clear intent

---

## 6. Dependency Injection

- Constructor injection via `@RequiredArgsConstructor`

### Why

- Improves testability
- Avoids hidden dependencies
- Aligns with Spring best practices

---

## 7. AWS SNS Configuration

- SNS client configured via Spring `@Configuration`

### Why

- Centralized configuration
- Reusable across components
- Easier environment management

---

## 8. Logging Strategy

### Includes

- accountId
- amount
- balance

### Why

- Improves observability
- Supports debugging in distributed systems
- Avoids sensitive data leakage

---

## 📦 External Library Usage

---

### AWS SNS SDK

- `software.amazon.awssdk.services.sns.SnsClient`
- Used to publish withdrawal events to AWS SNS
- Requires external AWS credentials configuration

---

### Spring JdbcTemplate

- Used for executing SQL queries
- Provides direct database interaction without ORM overhead

---

### Jackson ObjectMapper

- Converts Java objects to JSON
- Used for SNS message serialization

---

### Jakarta Bean Validation

- Ensures request validation at API boundary
- Prevents invalid withdrawal requests  