# Banking Application - Detailed Project Explanation

## 1. What This Project Is

This repository is an AI-powered core banking platform. It is designed as a microservices-based banking system, not as one single backend application.

The project contains:

- A Next.js frontend for the user interface.
- Multiple Spring Boot backend services.
- A shared backend library used by all services.
- Oracle database schema and stored procedures.
- Kafka and Zookeeper for event-driven communication.
- Redis for cache/session-related support.
- Docker Compose files for local container-based running.
- Kubernetes YAML files for cluster deployment.

In simple words, this project is trying to model a real banking system where different responsibilities are separated into different services.

Example:

```text
Login is handled by auth-service.
Customer profile and KYC are handled by customer-service.
Accounts and debit cards are handled by account-service.
Deposits and transfers are handled by transaction-service.
Fraud checking is handled by fraud-service.
The frontend talks to the backend through api-gateway.
```

## 2. Main Folder Structure

The top-level folders are:

```text
Banking-Application/
  backend/
  frontend/
  database/
  k8s/
  docker-compose.yml
  .gitignore
```

### backend

This contains all Spring Boot backend services.

Important modules:

```text
backend/
  core-shared-lib/
  api-gateway/
  auth-service/
  customer-service/
  account-service/
  transaction-service/
  payment-service/
  loan-service/
  notification-service/
  fraud-service/
  audit-service/
  ai-assistant-service/
  reporting-service/
```

### frontend

This contains the Next.js web application. This is what the user sees in the browser.

### database

This contains Oracle SQL files:

```text
database/schema/core_schema.sql
database/procedures/procedures.sql
```

The schema file creates database tables. The procedures file contains Oracle stored procedures used by services such as transaction and fraud detection.

### k8s

This contains Kubernetes deployment files:

```text
k8s/database-deploy.yml
k8s/gateway-deploy.yml
k8s/ingress.yml
k8s/kafka-deploy.yml
```

These files are used if you want to deploy the app into a Kubernetes cluster.

## 3. Overall Architecture

The project follows this request flow:

```text
Browser / Frontend
        |
        v
API Gateway
        |
        v
Backend Microservice
        |
        v
Oracle Database / Redis / Kafka
```

The frontend should not directly call every backend service. Instead, it should call the API Gateway.

The API Gateway receives the request and forwards it to the correct backend service.

Example:

```text
Request: /api/v1/accounts
Gateway sends it to: account-service

Request: /api/v1/auth/login
Gateway sends it to: auth-service

Request: /api/v1/transactions/deposit
Gateway sends it to: transaction-service
```

## 4. Backend Parent Project

The file `backend/pom.xml` is the parent Maven file.

It defines:

- Project group: `com.banking`
- Parent artifact: `banking-platform-parent`
- Java version: `21`
- Spring Boot version: `3.3.0`
- Spring Cloud version: `2023.0.1`
- Common dependency versions
- List of all backend modules

This means all backend services inherit common configuration from the parent project.

The parent POM has this module list:

```xml
<modules>
    <module>core-shared-lib</module>
    <module>api-gateway</module>
    <module>auth-service</module>
    <module>customer-service</module>
    <module>account-service</module>
    <module>transaction-service</module>
    <module>payment-service</module>
    <module>loan-service</module>
    <module>notification-service</module>
    <module>fraud-service</module>
    <module>audit-service</module>
    <module>ai-assistant-service</module>
    <module>reporting-service</module>
</modules>
```

So when you run Maven from the backend folder, Maven knows that this is a multi-module project.

## 5. Core Shared Library

Folder:

```text
backend/core-shared-lib
```

This module contains classes shared by multiple backend services.

Important examples:

```text
ApiResponse.java
CustomExceptions.java
BankingEvents.java
UserSessionDTO.java
```

### ApiResponse

`ApiResponse<T>` is a common response wrapper.

Instead of returning raw data from APIs, services return a structure like:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-06-13T10:00:00"
}
```

This helps all services return responses in the same format.

### BankingEvents

`BankingEvents.java` defines event classes such as:

- `CustomerCreatedEvent`
- `TransactionInitiatedEvent`
- `TransactionSuccessEvent`
- `TransactionFailedEvent`
- `PaymentProcessedEvent`
- `FraudDetectedEvent`

These are useful in an event-driven system where services communicate through Kafka.

## 6. API Gateway

Folder:

```text
backend/api-gateway
```

The API Gateway runs on port:

```text
8080
```

Its routing configuration is in:

```text
backend/api-gateway/src/main/resources/application.yml
```

It routes requests like this:

```text
/api/v1/auth/**          -> auth-service
/api/v1/customers/**     -> customer-service
/api/v1/accounts/**      -> account-service
/api/v1/transactions/**  -> transaction-service
/api/v1/payments/**      -> payment-service
/api/v1/loans/**         -> loan-service
/api/v1/ai/**            -> ai-assistant-service
```

The purpose of the gateway is to give the frontend one common backend address.

Without the gateway, the frontend would need to know many service URLs:

```text
auth-service:8081
customer-service:8082
account-service:8083
transaction-service:8084
...
```

With the gateway, the frontend can use:

```text
http://localhost:8080
```

## 7. Auth Service

Folder:

```text
backend/auth-service
```

This service handles user authentication.

Important files:

```text
AuthController.java
AuthService.java
JwtService.java
SecurityConfig.java
User.java
Role.java
Permission.java
UserRepository.java
RoleRepository.java
```

The controller exposes APIs like:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/verify-otp
```

Responsibilities:

- Register new users.
- Log users in.
- Verify OTP.
- Generate JWT tokens.
- Manage roles and permissions.

This is the entry point for authentication.

## 8. Customer Service

Folder:

```text
backend/customer-service
```

This service handles customer profile and KYC.

Important files:

```text
CustomerController.java
CustomerService.java
Customer.java
KycDocument.java
CustomerRepository.java
KycDocumentRepository.java
```

Main APIs:

```text
POST /api/v1/customers/onboard
POST /api/v1/customers/{customerId}/kyc
PUT  /api/v1/customers/{customerId}/kyc/{documentId}/verify
GET  /api/v1/customers/user/{userId}
GET  /api/v1/customers/{customerId}/documents
GET  /api/v1/customers
```

Responsibilities:

- Create customer profile.
- Store KYC document information.
- Verify KYC status.
- Fetch customers.
- Fetch customer documents.

Typical flow:

```text
User registers in auth-service.
Then customer profile is created in customer-service.
Then KYC documents are uploaded and verified.
```

## 9. Account Service

Folder:

```text
backend/account-service
```

This service manages bank accounts, balances, and debit cards.

Important files:

```text
AccountController.java
AccountService.java
Account.java
AccountBalance.java
DebitCard.java
AccountRepository.java
AccountBalanceRepository.java
DebitCardRepository.java
```

Main APIs:

```text
POST /api/v1/accounts
GET  /api/v1/accounts/customer/{customerId}
GET  /api/v1/accounts/{accountId}/balance
POST /api/v1/accounts/{accountId}/cards
GET  /api/v1/accounts/{accountId}/cards
PUT  /api/v1/accounts/cards/{cardId}/status
PUT  /api/v1/accounts/cards/{cardId}/limit
PUT  /api/v1/accounts/cards/{cardId}/pin
```

Responsibilities:

- Create a bank account.
- Create initial balance record.
- Fetch all accounts for a customer.
- Fetch account balance.
- Issue debit cards.
- Freeze, block, or activate a card.
- Change card daily limit.
- Change debit card PIN.

Example account creation flow:

```text
Request comes to AccountController.
AccountController calls AccountService.
AccountService generates an account number.
AccountService saves Account entity.
AccountService creates AccountBalance entity.
Response is returned as ApiResponse.
```

Important note:

The current account number and card number generation logic uses random numbers. In a production banking system, this should be stronger and should guarantee uniqueness.

## 10. Transaction Service

Folder:

```text
backend/transaction-service
```

This service handles deposits, transfers, transaction history, and transaction events.

Important files:

```text
TransactionController.java
TransactionService.java
OutboxPublisherScheduler.java
Transaction.java
TransactionHistory.java
OutboxMessage.java
TransactionRepository.java
TransactionHistoryRepository.java
OutboxRepository.java
```

Main APIs:

```text
POST /api/v1/transactions/transfer
POST /api/v1/transactions/deposit
GET  /api/v1/transactions/account/{accountId}
```

### Transfer Flow

For transfer, the service calls an Oracle stored procedure:

```text
PKG_CORE_BANKING.PRC_TRANSFER_FUNDS
```

The Java service uses `JdbcTemplate` and `CallableStatement` to call the procedure.

Flow:

```text
TransactionController receives transfer request.
TransactionService calls Oracle procedure.
Oracle procedure performs the money movement.
Procedure returns transaction reference.
Service returns transaction reference to user.
```

### Deposit Flow

For deposit, the service:

1. Generates transaction reference.
2. Updates account balance in `ACCOUNT_BALANCES`.
3. Saves a `Transaction`.
4. Saves a `TransactionHistory`.
5. Saves an `OutboxMessage`.

That outbox message is later published to Kafka.

### Outbox Pattern

The file `OutboxPublisherScheduler.java` runs every 5 seconds.

It checks for messages with status:

```text
PENDING
```

Then it sends them to Kafka topic:

```text
transaction-events
```

After successful publishing, the message status becomes:

```text
PROCESSED
```

This pattern is useful because database changes and event publishing become more reliable.

## 11. Fraud Service

Folder:

```text
backend/fraud-service
```

Important file:

```text
FraudListener.java
```

This service listens to Kafka:

```java
@KafkaListener(topics = "transaction-events", groupId = "fraud-group")
```

When it receives a transaction event, it reads the transaction reference and calls this Oracle procedure:

```text
PKG_CORE_BANKING.PRC_DETECT_FRAUD
```

The procedure returns:

- `isFraud`
- `riskScore`

If fraud is detected, the service logs a warning.

Flow:

```text
transaction-service creates transaction event.
OutboxPublisherScheduler sends event to Kafka.
fraud-service receives event from Kafka.
fraud-service calls fraud detection stored procedure.
fraud-service logs fraud or safe result.
```

## 12. AI Assistant Service

Folder:

```text
backend/ai-assistant-service
```

This service is intended to provide banking chatbot and financial insight features.

Its `pom.xml` currently includes:

- `spring-boot-starter-web`
- `core-shared-lib`

That means it can expose REST APIs and reuse shared classes.

Important note:

Even though the description says "OpenAI-powered", the current Maven file does not include an OpenAI SDK dependency. The service currently looks prepared for AI functionality, but the actual AI integration would need to be implemented in code.

## 13. Other Backend Services

The repo also contains:

```text
payment-service
loan-service
notification-service
audit-service
reporting-service
```

These are separate microservices. Some of them appear to be more basic or scaffolded compared with account, transaction, auth, customer, and fraud services.

Their intended responsibilities are:

- `payment-service`: external or internal payment processing.
- `loan-service`: loan products and loan applications.
- `notification-service`: SMS, email, push notifications.
- `audit-service`: audit logs and compliance tracking.
- `reporting-service`: reports and analytics.

## 14. Frontend

Folder:

```text
frontend
```

The frontend is built using:

- Next.js
- React
- TypeScript
- Tailwind CSS
- lucide-react icons

Important pages:

```text
frontend/src/app/page.tsx
frontend/src/app/login/page.tsx
frontend/src/app/dashboard/page.tsx
frontend/src/app/admin/page.tsx
```

### Home Page

The home page presents the banking product as "AURA".

It has:

- Header
- Sign in button
- Get started button
- Hero section
- Feature cards

### Dashboard Page

The dashboard contains:

- Account balance cards
- Send money form
- Debit card controls
- Transaction history
- Floating AI chat drawer

Important note:

The dashboard currently uses local React state and sample data for many features. For example, balances and transactions are managed in `useState`. That means the frontend is currently simulating some banking actions instead of fully calling the backend APIs.

To make it fully connected, the frontend would need API calls to:

```text
GET  /api/v1/accounts/customer/{customerId}
GET  /api/v1/accounts/{accountId}/balance
POST /api/v1/transactions/transfer
GET  /api/v1/transactions/account/{accountId}
PUT  /api/v1/accounts/cards/{cardId}/status
PUT  /api/v1/accounts/cards/{cardId}/limit
PUT  /api/v1/accounts/cards/{cardId}/pin
```

## 15. Database

Folder:

```text
database
```

The project uses Oracle Database.

Database files:

```text
database/schema/core_schema.sql
database/procedures/procedures.sql
```

The backend services use database tables for:

- users
- roles
- permissions
- customers
- KYC documents
- accounts
- account balances
- debit cards
- transactions
- transaction history
- outbox messages

The transaction and fraud services also call Oracle stored procedures.

Important procedures:

```text
PKG_CORE_BANKING.PRC_TRANSFER_FUNDS
PKG_CORE_BANKING.PRC_DETECT_FRAUD
```

This means part of the business logic is inside the database, not only inside Java code.

## 16. Kafka and Event Flow

Kafka is used for asynchronous communication between services.

The main topic used in the current code is:

```text
transaction-events
```

Flow:

```text
transaction-service saves OutboxMessage.
OutboxPublisherScheduler publishes the message to Kafka.
fraud-service consumes the Kafka message.
fraud-service checks transaction risk.
```

This is different from normal REST calls.

REST is synchronous:

```text
Service A calls Service B and waits for response.
```

Kafka is asynchronous:

```text
Service A publishes event.
Service B receives it later.
Service A does not need to wait.
```

## 17. Redis

Redis is included in Docker Compose and Kubernetes.

It is commonly used for:

- caching
- session storage
- OTP temporary storage
- rate limiting

In this project, Redis is mainly connected around authentication-related infrastructure.

## 18. Docker Compose

File:

```text
docker-compose.yml
```

Docker Compose is used to run many services together locally.

It defines containers for:

- Redis
- Zookeeper
- Kafka
- auth-service
- customer-service
- account-service
- transaction-service
- payment-service
- loan-service
- notification-service
- fraud-service
- audit-service
- ai-assistant-service
- reporting-service
- api-gateway

Docker is not mandatory for the Java code itself. It is a convenient way to start many services and infrastructure components together.

Without Docker, you would need to manually install and run:

- Oracle
- Redis
- Kafka
- Zookeeper
- each Spring Boot service
- frontend

## 19. Kubernetes Files

Folder:

```text
k8s
```

### database-deploy.yml

This deploys:

- Oracle XE database
- Redis

It creates Kubernetes Deployments and Services for both.

### kafka-deploy.yml

This deploys:

- Zookeeper
- Kafka

Kafka depends on Zookeeper in this setup.

Note:

There appears to be a YAML formatting issue in this line:

```yaml
- name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
```

It should likely be:

```yaml
- name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
  value: "1"
```

### gateway-deploy.yml

This deploys:

- api-gateway
- auth-service
- transaction-service

It also creates Kubernetes Services for them.

Important note:

The Docker Compose file includes many services, but the Kubernetes gateway deployment file currently includes only some backend services. If you want the full system on Kubernetes, more service deployments would need to be added.

### ingress.yml

This exposes the API Gateway through Kubernetes Ingress.

The ingress sends incoming traffic to:

```text
api-gateway:8080
```

## 20. Example End-to-End Flows

### User Registration Flow

```text
Frontend registration page
        |
        v
POST /api/v1/auth/register
        |
        v
API Gateway
        |
        v
auth-service
        |
        v
Oracle database
```

### Customer Onboarding Flow

```text
Frontend customer form
        |
        v
POST /api/v1/customers/onboard
        |
        v
API Gateway
        |
        v
customer-service
        |
        v
Oracle database
```

### Account Creation Flow

```text
Frontend account creation form
        |
        v
POST /api/v1/accounts
        |
        v
API Gateway
        |
        v
account-service
        |
        v
Oracle database
```

Inside account-service:

```text
Create Account
Create AccountBalance
Return ApiResponse
```

### Deposit Flow

```text
Frontend deposit action
        |
        v
POST /api/v1/transactions/deposit
        |
        v
API Gateway
        |
        v
transaction-service
        |
        v
Oracle database
        |
        v
Outbox message
        |
        v
Kafka transaction-events
        |
        v
fraud-service
```

### Transfer Flow

```text
Frontend transfer form
        |
        v
POST /api/v1/transactions/transfer
        |
        v
API Gateway
        |
        v
transaction-service
        |
        v
Oracle stored procedure PRC_TRANSFER_FUNDS
        |
        v
Transaction reference returned
```

### Fraud Detection Flow

```text
transaction-service publishes transaction event
        |
        v
Kafka topic: transaction-events
        |
        v
fraud-service Kafka listener
        |
        v
Oracle stored procedure PRC_DETECT_FRAUD
        |
        v
Log fraud result
```

## 21. How To Run Without Docker

You can run this project without Docker, but you must manually run the dependencies.

Required tools:

- Java 21
- Maven
- Node.js and npm
- Oracle Database
- Kafka
- Zookeeper
- Redis

Build backend:

```powershell
cd backend
mvn clean install
```

Run a backend service:

```powershell
cd backend/account-service
mvn spring-boot:run
```

Run the gateway:

```powershell
cd backend/api-gateway
mvn spring-boot:run
```

Run frontend:

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:3000
```

Gateway URL:

```text
http://localhost:8080
```

## 22. How To Study This Project

A good learning order is:

1. Start with `backend/pom.xml`.
2. Read `core-shared-lib`, especially `ApiResponse`.
3. Read `api-gateway/application.yml`.
4. Read `auth-service`.
5. Read `customer-service`.
6. Read `account-service`.
7. Read `transaction-service`.
8. Read `fraud-service`.
9. Read `frontend/src/app/dashboard/page.tsx`.
10. Read `docker-compose.yml`.
11. Read the `k8s` YAML files.

This order helps because each step builds on the previous one.

## 23. Important Observations

This project has a good microservices structure, but some parts look incomplete or demo-oriented.

Examples:

- The frontend dashboard mostly uses local state instead of real backend API calls.
- Some services are scaffolded and may not have full business logic yet.
- AI Assistant service is named as OpenAI-powered, but the current POM does not include an OpenAI SDK.
- Card PIN and CVV values are stored in a simplified way in code examples. In real banking software these must be securely hashed and never stored as plain values.
- Kubernetes files do not yet include every backend service.
- Some Kubernetes YAML may need correction before applying.
- Oracle credentials are hardcoded in config files. In production, these should be Kubernetes Secrets or environment secrets.

## 24. One-Sentence Summary

This project is a Spring Boot and Next.js banking microservices platform where the frontend talks to an API Gateway, the gateway routes to banking services, services store data in Oracle, and transaction events flow through Kafka for fraud detection and future event-driven features.
