# Kafka Interview Guide - Banking Application

## 1. Overview: Kafka's Role in This Project

### What is Kafka Used For?
Kafka is a **distributed event streaming platform** used in this banking application for **asynchronous, event-driven communication** between microservices. It enables:

- **Decoupling of services**: Services don't need to communicate synchronously
- **Real-time event processing**: Events flow through the system in real-time
- **Event durability**: Events are persisted and can be replayed if needed
- **Scalability**: Multiple consumers can independently process the same events

### Current Architecture
```
Transaction Service (Producer)
        |
        | Publishes transaction events
        v
    Kafka Topics
        |
        +--- "transaction-events" ---> Fraud Service (Consumer)
        |
        +--- "fraud-events" ---> (Future consumers)
```

---

## 2. Why Use Message Queue? (Core Concept Interview Question)

### Problem This Solves

**Without Message Queue (Synchronous):**
```
User initiates transfer
    |
    v
Transaction Service writes to DB
    |
    v
Makes DIRECT REST call to Fraud Service
    |
    +-- If Fraud Service is slow/down → Transaction fails
    +-- If Fraud Service is busy → User waits
    +-- Tight coupling between services
```

**With Kafka (Asynchronous):**
```
User initiates transfer
    |
    v
Transaction Service:
    1. Writes to DB (transaction saved)
    2. Creates OutboxMessage record
    3. Returns success to user immediately
    |
    v
OutboxPublisherScheduler:
    1. Reads pending messages every 5 seconds
    2. Publishes to Kafka
    3. Marks as PROCESSED
    |
    v
Fraud Service (independent):
    1. Continuously listens to Kafka topic
    2. Processes events when ready
    3. No impact on transaction if slow
```

### Key Benefits

| Aspect | Without Queue | With Kafka |
|--------|---------------|-----------|
| **User Experience** | Waits for fraud check | Immediate response |
| **Service Coupling** | Tight (direct calls) | Loose (event-based) |
| **Fault Tolerance** | Transaction fails if service down | Transaction saved, can retry |
| **Scalability** | 1 fraud service handles all | Can scale fraud service independently |
| **Performance** | Bottleneck: slowest service | Non-blocking |

### Real-World Banking Example

**Scenario: Customer transfers $5000**

1. **Transaction Service receives request**
   - Saves transfer record to DB
   - Creates OutboxMessage with transfer details
   - Returns "Transfer initiated" to user
   
2. **Without Kafka** ❌
   - Must wait for fraud check before confirming
   - If fraud service is busy: user waits 30 seconds
   - User experience: slow
   
3. **With Kafka** ✅
   - Returns immediately (user sees "Processing")
   - Publishes to Kafka asynchronously
   - Fraud check happens in background
   - User experience: fast and responsive

---

## 3. Why Kafka and NOT RabbitMQ? (Comparative Interview Question)

### Quick Comparison

| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| **Type** | Log-based distributed streaming | Message broker (queue-based) |
| **Throughput** | Very High (millions msgs/sec) | Medium (thousands msgs/sec) |
| **Message Durability** | Persistent on disk by default | Optional durability |
| **Replayability** | ✅ Messages retained, can replay | ❌ Messages deleted after consumption |
| **Scaling** | Horizontal (add brokers) | Vertical (add memory) |
| **Latency** | Higher (100s of ms) | Lower (10s of ms) |
| **Use Case** | Real-time event streaming | Task queues, RPC |
| **Partition Model** | Partitioned (parallel processing) | Exchanges/Queues |
| **Complexity** | Complex (requires ZK/Kraft) | Simple to setup |

### For This Banking Application - Why Kafka?

1. **Fraud Detection at Scale**
   - Fraud checking must handle PEAK load (thousands of transactions/minute)
   - Kafka easily handles this volume
   - RabbitMQ would be a bottleneck

2. **Event Replaying**
   - If fraud detection algorithm improves, replay historical events
   - Kafka: All events retained → Can reprocess
   - RabbitMQ: Events gone → Can't replay

3. **Multiple Consumers (Future Growth)**
   - Today: Fraud Service listens
   - Tomorrow: Audit Service, Analytics, Notification Service
   - Kafka: All consumers see all events (broadcast)
   - RabbitMQ: Would need separate queues for each consumer

4. **Real-time Analytics**
   - Banking requires real-time dashboards
   - "How many frauds detected in last hour?"
   - Kafka's log allows efficient time-based queries
   - RabbitMQ not designed for this

### When RabbitMQ Would Be Better

- Simple task queue (send emails)
- Task scheduling
- Traditional request-response patterns
- Lower operational complexity needed

---

## 4. Zookeeper's Role (Architecture Interview Question)

### What is Zookeeper?

Zookeeper is a **distributed coordination service** that helps Kafka manage itself.

### Kafka Depends on Zookeeper For

1. **Broker Registration & Discovery**
   ```
   Zookeeper tracks:
   - Which brokers are alive
   - Which broker is the leader
   - What port each broker runs on
   ```

2. **Topic & Partition Management**
   ```
   Zookeeper maintains:
   - Which topics exist
   - How many partitions per topic
   - Which broker owns which partition
   ```

3. **Leader Election**
   ```
   If broker dies:
   - Zookeeper detects it
   - Elects new leader from replicas
   - Notifies all brokers of change
   - Ensures data consistency
   ```

4. **Consumer Group Coordination**
   ```
   Zookeeper tracks:
   - Which consumer group exists
   - Which partition each consumer owns
   - Offsets (messages already consumed)
   ```

### Architecture in This Project

```
Docker Compose Setup:
┌──────────────────┐
│   Zookeeper      │
│  (port 2181)     │
│  Runs first      │
└────────┬─────────┘
         │ Manages
         v
┌──────────────────┐
│     Kafka        │
│  (port 9092)     │
│  Depends on ZK   │
└────────┬─────────┘
         │ Produces/Consumes
         v
┌─────────────────────────────────┐
│  Transaction Service            │
│  Fraud Service                  │
│  (other consumers)              │
└─────────────────────────────────┘
```

### Current Configuration (from docker-compose.yml)

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.4.0
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181      # Port services connect to
    ZOOKEEPER_TICK_TIME: 2000        # Heartbeat interval (ms)

kafka:
  image: confluentinc/cp-kafka:7.4.0
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181  # Connects to ZK
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
    KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

### Why Zookeeper Matters for Banking

- **High Availability**: If one broker fails, ZK ensures another takes over
- **No Data Loss**: Ensures data consistency during failures
- **Automatic Recovery**: When broker comes back, ZK helps rejoin cluster
- **Fraud Detection Continuity**: If primary fraud broker dies, ZK ensures backup takes over

---

## 5. Kafka Implementation in Services

### 5.1 Transaction Service (PRODUCER)

**File**: `backend/transaction-service/`

#### How It Works

```java
// 1. Database writes transaction
transaction = new Transaction(...);
transactionRepository.save(transaction);

// 2. Create OutboxMessage (same DB transaction)
outboxMessage = OutboxMessage.builder()
    .aggregateType("TRANSACTION")
    .aggregateId(transaction.getId())
    .eventType("TRANSFER_INITIATED")
    .payload(jsonPayload)
    .status("PENDING")
    .build();
outboxRepository.save(outboxMessage);

// 3. Scheduler publishes to Kafka (every 5 seconds)
@Scheduled(fixedDelay = 5000)
public void publishPendingEvents() {
    // Reads PENDING messages
    // Sends to Kafka
    // Updates status to PROCESSED or FAILED
}
```

#### Key Features

**Outbox Pattern** (Transactional Outbox)
- Business logic and event publishing in SAME transaction
- Guarantees: "If transaction saved, event will be published"
- Prevents loss of events

**Scheduler-Based Publishing**
```
Every 5 seconds:
1. Query: SELECT * FROM OUTBOX WHERE status = 'PENDING'
2. For each message:
   - Send to Kafka
   - If success: UPDATE status = 'PROCESSED'
   - If fail: UPDATE status = 'FAILED'
3. Retry failed messages on next cycle
```

#### Configuration (`application.yml`)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092  # Kafka server address
    producer:
      key-serializer: StringSerializer      # How to serialize keys
      value-serializer: StringSerializer    # How to serialize values
      acks: all                             # Wait for all replicas
```

**`acks: all`** means:
- Producer waits for acknowledgment from ALL brokers
- Guarantees maximum durability
- Slight performance trade-off, but critical for banking

#### Topics Published

| Topic | Event Type | Example Payload |
|-------|-----------|---|
| `transaction-events` | Transaction created/transferred | `{"ref":"TXN123", "amount":5000, "to":"ACC456"}` |
| `fraud-events` | Fraud-related events | `{"type":"RISK_ALERT", "score":0.95}` |

#### Implementation Details

**OutboxMessage Entity:**
```java
@Table(name = "OUTBOX")
public class OutboxMessage {
    @Id private Long id;
    private String aggregateType;      // "TRANSACTION", "FRAUD"
    private String aggregateId;        // txn ID or ref
    private String eventType;          // Event type
    private String payload;            // JSON data
    private String status;             // PENDING, PROCESSED, FAILED
    private LocalDateTime createdAt;
}
```

**Scheduler Class:**
```java
@Service
@EnableScheduling
public class OutboxPublisherScheduler {
    
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Scheduled(fixedDelay = 5000)  // Every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxMessage> pending = outboxRepository.findByStatus("PENDING");
        
        for (OutboxMessage msg : pending) {
            String topic = "transaction-events";
            if ("FRAUD".equals(msg.getAggregateType())) {
                topic = "fraud-events";
            }
            
            kafkaTemplate.send(topic, msg.getAggregateId(), msg.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        updateMessageStatus(msg.getId(), "PROCESSED");
                    } else {
                        updateMessageStatus(msg.getId(), "FAILED");
                    }
                });
        }
    }
}
```

---

### 5.2 Fraud Service (CONSUMER)

**File**: `backend/fraud-service/`

#### How It Works

```java
@Service
public class FraudListener {
    
    @KafkaListener(topics = "transaction-events", groupId = "fraud-group")
    public void consumeTransactionEvent(String payload) {
        // 1. Parse JSON payload
        JsonNode event = objectMapper.readTree(payload);
        String txnRef = event.get("ref").asText();
        
        // 2. Call Oracle Stored Procedure
        CallableStatement cs = connection.prepareCall(
            "{call PKG_CORE_BANKING.PRC_DETECT_FRAUD(?, ?, ?)}"
        );
        cs.setString(1, txnRef);           // Input: transaction ref
        cs.registerOutParameter(2, INTEGER);  // Output: is_fraud
        cs.registerOutParameter(3, DOUBLE);   // Output: risk_score
        cs.execute();
        
        int isFraud = cs.getInt(2);
        double riskScore = cs.getDouble(3);
        
        // 3. Log/Alert if fraud detected
        if (isFraud == 1) {
            logger.warn("Fraud detected: ref={}, risk={}", txnRef, riskScore);
            // Could trigger alert, block transaction, etc.
        } else {
            logger.info("Transaction verified: ref={}, risk={}", txnRef, riskScore);
        }
    }
}
```

#### Key Features

**@KafkaListener Annotation**
- Continuously listens to "transaction-events" topic
- Automatically joins consumer group "fraud-group"
- Processes messages as they arrive

**Integration with Oracle**
- Calls stored procedure: `PKG_CORE_BANKING.PRC_DETECT_FRAUD`
- Uses ML/relational rules to detect fraud
- Returns: is_fraud (1/0) and risk_score (0-1.0)

**Consumer Group: "fraud-group"**
- Multiple fraud service instances share this group
- Each message processed by ONE instance (load balancing)
- Kafka tracks offsets per group

#### Configuration (`application.yml`)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: fraud-group                    # Consumer group ID
      key-deserializer: StringDeserializer     # How to parse keys
      value-deserializer: StringDeserializer   # How to parse values
```

#### Process Flow

```
1. Transaction Service:
   - Customer transfers $5000
   - Saves TRANSFER_INITIATED event to OUTBOX table
   - Returns success (user sees "Processing")

2. OutboxPublisherScheduler (every 5 sec):
   - Reads OUTBOX WHERE status='PENDING'
   - Sends to Kafka topic "transaction-events"
   - Updates status='PROCESSED'

3. Kafka:
   - Stores event in topic partition
   - Replicates across brokers
   - Maintains log for 7 days (default)

4. Fraud Service:
   - Continuously listens to "transaction-events"
   - Receives: {"ref":"TXN123", "amount":5000, ...}
   - Calls Oracle stored procedure
   - Detects fraud (or clears transaction)
   - Logs result
```

---

## 6. Topics in This Application

### Current Topics

#### Topic 1: "transaction-events"
```yaml
Name: transaction-events
Producer: Transaction Service
Consumer: Fraud Service
Partition: 1 (single partition, no parallelization yet)
Retention: Default (7 days)

Sample Message:
{
  "ref": "TXN123",
  "customerId": "CUST456",
  "amount": 5000.00,
  "fromAccount": "ACC001",
  "toAccount": "ACC002",
  "type": "TRANSFER",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Topic 2: "fraud-events"
```yaml
Name: fraud-events
Producer: Transaction Service (if FRAUD type)
Consumer: (Currently none, but ready for:)
          - Notification Service (alert user)
          - Audit Service (log fraud attempts)
          - Analytics (dashboard)

Sample Message:
{
  "transactionRef": "TXN123",
  "isFraud": 1,
  "riskScore": 0.95,
  "reason": "High amount + Multiple geographic locations in 1 hour",
  "timestamp": "2024-01-15T10:30:05Z"
}
```

---

## 7. Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        BANKING APPLICATION                      │
└─────────────────────────────────────────────────────────────────┘

[1] User initiates transfer via Frontend
         |
         v
[2] API Gateway routes to Transaction Service
         |
         v
[3] Transaction Service:
    - Saves TRANSFER in DB (ACCOUNTS table)
    - Creates OutboxMessage in OUTBOX table
    - Both in SAME database transaction
    - Returns 200 OK to user
         |
         v
[4] Simultaneously - OutboxPublisherScheduler (runs every 5 sec):
    - SELECT FROM OUTBOX WHERE status='PENDING'
    - For each message:
      * kafkaTemplate.send("transaction-events", payload)
      * UPDATE OUTBOX SET status='PROCESSED'
         |
         v
[5] Kafka Broker:
    - Receives message from Producer
    - Writes to "transaction-events" partition
    - Replicates to other brokers
    - Keeps in log for 7 days
    - Stores offset: "This message is position 12345"
         |
         v
[6] Fraud Service (Consumer):
    - @KafkaListener constantly reads "transaction-events"
    - When message arrives:
      * Parse JSON payload
      * Extract transaction reference
      * Call Oracle stored procedure: PRC_DETECT_FRAUD
      * Get fraud decision (1=fraud, 0=safe)
      * Log decision
      * (Optional) Save fraud record to DB
      * Commit offset (tell Kafka "we processed position 12345")
         |
         v
[7] End Result:
    - User sees transfer complete
    - Fraud check happened in background
    - If fraud detected → can block/alert separately
    - No impact on transfer speed
```

---

## 8. Interview Q&A Preparation

### Q1: "Why use message queue instead of direct REST calls?"

**Answer Structure:**
1. **Problem**: Direct calls cause tight coupling and performance issues
2. **Solution**: Kafka enables asynchronous, event-driven architecture
3. **Benefits**: 
   - Decoupling of services
   - Better performance (no waiting)
   - Fault tolerance (service down ≠ transaction fails)
   - Scalability (services scale independently)
4. **Example**: Transfer happens immediately, fraud check in background

### Q2: "Why Kafka and not RabbitMQ?"

**Answer Structure:**
1. **Throughput**: Kafka designed for millions msgs/sec vs RabbitMQ thousands/sec
2. **Replayability**: Kafka retains messages → can replay on algorithm changes
3. **Multiple Consumers**: Kafka's broadcast model better for future growth
4. **Real-time Analytics**: Banking needs real-time dashboards (Kafka better)
5. **Complexity Trade-off**: Worth the extra operational complexity

### Q3: "What's Zookeeper's role?"

**Answer Structure:**
1. **Coordination Service**: Manages Kafka cluster coordination
2. **Broker Management**: Tracks which brokers alive, elects leaders
3. **Partition Management**: Ensures data consistency across replicas
4. **Consumer Groups**: Tracks consumer offsets and assignments
5. **Banking Perspective**: Ensures fraud detection continuity even on failures

### Q4: "How is Kafka implemented here specifically?"

**Answer Structure:**
1. **Pattern**: Transactional Outbox Pattern
2. **Producer** (Transaction Service):
   - Saves transaction + OutboxMessage in same DB transaction
   - Scheduler publishes to Kafka every 5 seconds
   - Retries on failure
3. **Consumer** (Fraud Service):
   - Listens to "transaction-events" topic
   - Calls Oracle stored procedure for fraud detection
   - Logs results
4. **Configuration**: 
   - Spring Kafka with StringSerializer/Deserializer
   - Bootstrap servers: kafka:29092 (Docker) or localhost:9092 (Local)

### Q5: "What happens if Fraud Service is down?"

**Answer:**
- Transaction still completes (already saved in DB)
- Message stays in Kafka topic (retained)
- When Fraud Service restarts:
  - Consumer rejoins group
  - Kafka sends missed messages (using offset)
  - Fraud check happens for all transactions
- No data loss, just delayed fraud detection

### Q6: "How do you ensure no message is lost?"

**Answer:**
1. **Producer Side** (Transaction Service):
   - `acks: all` ensures all broker replicas acknowledge
   - OutboxMessage in DB means message persisted before Kafka send
   
2. **Message Side** (Kafka):
   - Default retention: 7 days
   - Replicated across brokers
   
3. **Consumer Side** (Fraud Service):
   - Manually commits offset after processing
   - If crash before commit: message reprocessed on restart

### Q7: "What's the Outbox Pattern?"

**Answer:**
```
Goal: Guarantee "if transaction saved, event published"

Without Outbox (Problem):
1. Save transaction to DB
2. Send to Kafka
3. If send fails but transaction already saved:
   → Event lost, fraud check never happens ❌

With Outbox (Solution):
1. Save BOTH transaction AND OutboxMessage in ONE DB transaction:
   BEGIN TRANSACTION
   - INSERT INTO ACCOUNTS ...
   - INSERT INTO OUTBOX ...
   COMMIT
2. Scheduler separately sends to Kafka
3. If Kafka send fails, message stays in OUTBOX
4. Scheduler retries on next cycle
→ Guaranteed delivery ✅
```

### Q8: "How does consumer group work?"

**Answer:**
```
Consumer Group: "fraud-group"

Scenario 1: 1 Fraud Service instance
- Consumes all messages from "transaction-events"
- Maintains offset: "processed up to message 12345"

Scenario 2: 2 Fraud Service instances
- Kafka assigns partitions
  * Instance 1: handles partition 0
  * Instance 2: handles partition 1
- Each maintains separate offset
- Load balanced automatically

Scenario 3: Instance 1 crashes
- Instance 2 rebalances
- Takes over partition 0
- Reads from Instance 1's offset
- Continues from where Instance 1 stopped
```

---

## 9. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         INFRASTRUCTURE                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Docker Compose (Local) / Kubernetes (Production)              │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    ZOOKEEPER                            │   │
│  │  - Broker coordination                                  │   │
│  │  - Leader election                                      │   │
│  │  - Consumer group management                            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              ↑                                    │
│                    (manages via port 2181)                        │
│                              ↓                                    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                     KAFKA BROKER                         │   │
│  │  - Partition: transaction-events (1 partition)         │   │
│  │  - Partition: fraud-events (1 partition)               │   │
│  │  - Replication factor: 1                                │   │
│  │  - Retention: 7 days                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│          ↑                                      ↓                │
│      (send)                              (consume)              │
│          │                                      │                │
│  ┌──────────────────────────┐    ┌──────────────────────────┐  │
│  │ Transaction Service      │    │   Fraud Service         │  │
│  │ (Port 8084)              │    │   (Port 8088)           │  │
│  │                          │    │                         │  │
│  │ Producer:                │    │ Consumer:               │  │
│  │ - Publishes to Kafka     │    │ - Listens to Kafka      │  │
│  │ - Uses Outbox Pattern    │    │ - Calls Oracle procedure│  │
│  │ - Scheduler every 5 sec  │    │ - Fraud detection logic │  │
│  │                          │    │                         │  │
│  │ Topics Published:        │    │ Topics Consumed:        │  │
│  │ - transaction-events     │    │ - transaction-events    │  │
│  │ - fraud-events           │    │                         │  │
│  └──────────────────────────┘    └──────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        DATABASE (Oracle)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Transaction Service Tables:                                   │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ ACCOUNTS (Business Data)                                │  │
│  │ - account_id, balance, customer_id, status             │  │
│  └─────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ OUTBOX (Event Storage)                                  │  │
│  │ - id, aggregate_type, aggregate_id, event_type         │  │
│  │ - payload (JSON), status (PENDING|PROCESSED|FAILED)    │  │
│  │ - created_at                                            │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Fraud Service Tables:                                         │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ FRAUD_DETECTIONS (Audit Trail)                          │  │
│  │ - transaction_ref, is_fraud, risk_score, detected_at   │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Stored Procedures:                                            │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ PKG_CORE_BANKING.PRC_DETECT_FRAUD                       │  │
│  │ Input: transaction_ref                                  │  │
│  │ Output: is_fraud (1/0), risk_score (0.0-1.0)          │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 10. Key Code Examples for Interview

### Transaction Service - Publishing Events

```java
// Save transaction and outbox message in SAME transaction
@Transactional
public TransferResponse initiateTransfer(TransferRequest request) {
    // 1. Create and save transfer
    Transfer transfer = new Transfer();
    transfer.setFromAccountId(request.getFromAccountId());
    transfer.setToAccountId(request.getToAccountId());
    transfer.setAmount(request.getAmount());
    transfer.setStatus("INITIATED");
    transferRepository.save(transfer);
    
    // 2. Create outbox message (same transaction)
    OutboxMessage outbox = OutboxMessage.builder()
        .aggregateType("TRANSACTION")
        .aggregateId(transfer.getId().toString())
        .eventType("TRANSFER_INITIATED")
        .payload(objectMapper.writeValueAsString(new TransactionEvent(
            transfer.getId(),
            transfer.getFromAccountId(),
            transfer.getToAccountId(),
            transfer.getAmount()
        )))
        .status("PENDING")
        .build();
    outboxRepository.save(outbox);
    
    // 3. Return success (Kafka send happens separately in scheduler)
    return new TransferResponse("success", transfer.getId());
}

// Scheduler sends to Kafka every 5 seconds
@Scheduled(fixedDelay = 5000)
@Transactional
public void publishPendingEvents() {
    List<OutboxMessage> pending = outboxRepository.findByStatus("PENDING");
    for (OutboxMessage msg : pending) {
        kafkaTemplate.send(
            "transaction-events",
            msg.getAggregateId(),
            msg.getPayload()
        ).whenComplete((result, ex) -> {
            if (ex == null) {
                msg.setStatus("PROCESSED");
                outboxRepository.save(msg);
            }
        });
    }
}
```

### Fraud Service - Consuming Events

```java
@Service
public class FraudListener {
    
    private final JdbcTemplate jdbcTemplate;
    
    @KafkaListener(topics = "transaction-events", groupId = "fraud-group")
    public void consumeTransactionEvent(String payload) {
        try {
            // Parse incoming message
            JsonNode event = objectMapper.readTree(payload);
            String txnRef = event.get("ref").asText();
            
            // Call Oracle stored procedure
            int[] isFraudHolder = new int[1];
            double[] riskScoreHolder = new double[1];
            
            jdbcTemplate.execute((Connection con) -> {
                CallableStatement cs = con.prepareCall(
                    "{call PKG_CORE_BANKING.PRC_DETECT_FRAUD(?, ?, ?)}"
                );
                cs.setString(1, txnRef);
                cs.registerOutParameter(2, Types.INTEGER);
                cs.registerOutParameter(3, Types.DOUBLE);
                cs.execute();
                
                isFraudHolder[0] = cs.getInt(2);
                riskScoreHolder[0] = cs.getDouble(3);
                return cs;
            });
            
            // Log result
            if (isFraudHolder[0] == 1) {
                logger.warn("Fraud detected: ref={}, risk={}", 
                    txnRef, riskScoreHolder[0]);
                // Could send alert, block account, etc.
            } else {
                logger.info("Transaction safe: ref={}, risk={}", 
                    txnRef, riskScoreHolder[0]);
            }
            
        } catch (Exception e) {
            logger.error("Fraud check failed: {}", e.getMessage());
        }
    }
}
```

---

## 11. Configuration Files Reference

### docker-compose.yml (Kafka Setup)

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

### Transaction Service - application.yml

```yaml
server:
  port: 8084

spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all  # Wait for all replicas before confirming
```

### Fraud Service - application.yml

```yaml
server:
  port: 8088

spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: fraud-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

---

## 12. Quick Reference: Interview Talking Points

### ✅ What to Emphasize

1. **Decoupling**: Services don't depend on each other being fast
2. **Scalability**: Each service scales independently
3. **Resilience**: One service down doesn't break others
4. **Event-Driven**: Modern architecture paradigm
5. **Transactional Outbox**: Ensures no message loss
6. **Real-time Processing**: Critical for banking
7. **Auditability**: All events stored for compliance

### ❌ What to Avoid

1. "Kafka is always better than RabbitMQ" (context matters)
2. "No configuration needed" (it has defaults but requires tuning)
3. "No message loss with defaults" (explain `acks: all` is needed)
4. "Simple to set up" (it's complex, needs ZK, monitoring)
5. "Latency doesn't matter" (it does, but acceptable for fraud detection)

### 🎯 Demo Points (if asked for live demo)

1. Run `docker-compose up` and show Kafka/ZK starting
2. Show transaction service saving to OUTBOX table
3. Show OutboxPublisherScheduler publishing every 5 seconds
4. Show Kafka message in topic (using `kafka-console-consumer`)
5. Show Fraud Service consuming and logging detection

---

## 13. Related Files to Review Before Interview

```
Key files to understand deeply:

1. Docker Setup:
   - docker-compose.yml (Kafka/Zookeeper config)
   
2. Transaction Service:
   - backend/transaction-service/src/main/java/com/banking/transaction/entity/OutboxMessage.java
   - backend/transaction-service/src/main/java/com/banking/transaction/service/OutboxPublisherScheduler.java
   - backend/transaction-service/src/main/resources/application.yml
   
3. Fraud Service:
   - backend/fraud-service/src/main/java/com/banking/fraud/service/FraudListener.java
   - backend/fraud-service/src/main/resources/application.yml
   
4. Database:
   - database/procedures/procedures.sql (PRC_DETECT_FRAUD)
   - database/schema/core_schema.sql (OUTBOX table schema)
   
5. Kubernetes:
   - k8s/kafka-deploy.yml (Production deployment)
```

---

## 14. Follow-up Questions You Might Get

### Q: "What if a message fails to process in Fraud Service?"

A: With current setup, it would just be logged. For production, implement:
- Dead letter queue for failed messages
- Retry logic with exponential backoff
- Alert when messages fail N times
- Manual inspection UI for failed messages

### Q: "How do you scale this for millions of transactions?"

A: Several approaches:
1. Increase Kafka partitions (parallel fraud checking)
2. Multiple Fraud Service instances (one per partition)
3. Async Oracle calls (don't block on DB)
4. Batch processing (process 100 messages at once)
5. Caching (fraud patterns cache in Redis)

### Q: "What about ordering of messages?"

A: Current setup:
- Single partition → guarantees ordering per topic
- If you add more partitions → ordering only per partition
- For fraud, ordering important per account (not globally)
- Could implement: key-based partitioning (partition by accountId)

### Q: "How does this compare to event sourcing?"

A: Event Sourcing (advanced):
- Store all events as source of truth
- Rebuild state by replaying events
- Current setup: events for notifications only
- Could evolve into full event sourcing

### Q: "What monitoring do you need?"

A: Critical metrics:
- Kafka consumer lag (how behind is Fraud Service)
- Message throughput (msgs/sec)
- End-to-end latency (transaction → fraud check)
- Failed message count
- Zookeeper health
- Broker health (disk space, CPU)

---

## 15. Practice Scenarios

### Scenario 1: Live Interview Demo
"Walk us through what happens when a customer initiates a $10,000 transfer"

Your answer structure:
1. Frontend → API Gateway → Transaction Service
2. Transaction Service saves TRANSFER + OUTBOX record
3. Returns "success" to user immediately
4. Scheduler publishes to Kafka
5. Fraud Service consumes and checks
6. Result logged (fraud/safe)

### Scenario 2: Problem Solving
"Fraud detection is taking too long and impacting user experience"

Your answer:
1. Investigate: Check consumer lag (kafka-consumer-groups)
2. If lag high: 
   - Add more Fraud Service instances
   - Increase partitions
   - Optimize Oracle procedure
3. If lag low but throughput high:
   - Batch messages
   - Cache fraud patterns
   - Scale fraud service horizontally

### Scenario 3: System Design
"Design Kafka for future notification system"

Your answer:
1. Create new topic: "notification-events"
2. Transaction Service publishes: TRANSFER_INITIATED, TRANSFER_COMPLETED
3. Notification Service listens and sends emails/SMS
4. Implement similar to Fraud Service
5. Add dead letter queue for failed notifications
6. Add retry logic

---

## Summary Cheat Sheet

| Concept | Key Point |
|---------|-----------|
| **Why Message Queue** | Decoupling, async, resilience |
| **Why Kafka vs RabbitMQ** | Throughput, replayability, multiple consumers |
| **Zookeeper Role** | Broker coordination, leader election |
| **Outbox Pattern** | Transaction atomicity guarantee |
| **Producer** | Transaction Service, sends every 5 sec |
| **Consumer** | Fraud Service, processes in real-time |
| **Topics** | transaction-events, fraud-events |
| **Configuration** | Spring Kafka with StringSerializer |
| **Docker Setup** | Zookeeper on :2181, Kafka on :9092 |
| **Consumer Group** | fraud-group, tracks offsets |

---

Good luck with your interview! 🚀
