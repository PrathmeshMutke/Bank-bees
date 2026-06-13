# Kafka Interview Notes For This Banking Application

## 1. One-Minute Interview Answer

In this project, Kafka is used for asynchronous, event-driven communication between microservices.

The main implemented flow is:

1. `transaction-service` processes a banking transaction, especially the deposit flow.
2. After saving the transaction and transaction history, it writes an event into the `OUTBOX` table.
3. `OutboxPublisherScheduler` runs every 5 seconds, reads `PENDING` outbox messages, and publishes them to Kafka.
4. The Kafka topic used is `transaction-events`.
5. `fraud-service` listens to `transaction-events` using `@KafkaListener`.
6. When a message arrives, `fraud-service` parses the JSON payload and calls the Oracle stored procedure `PKG_CORE_BANKING.PRC_DETECT_FRAUD` to calculate fraud risk.

So the main use of Kafka here is to decouple transaction processing from fraud detection. The transaction service does not directly call the fraud service through REST. It only publishes an event, and the fraud service reacts to that event independently.

## 2. Where Kafka Is Implemented

### Producer Side: transaction-service

Files:

- `backend/transaction-service/src/main/java/com/banking/transaction/service/TransactionService.java`
- `backend/transaction-service/src/main/java/com/banking/transaction/service/OutboxPublisherScheduler.java`
- `backend/transaction-service/src/main/java/com/banking/transaction/entity/OutboxMessage.java`
- `backend/transaction-service/src/main/resources/application.yml`

In `TransactionService.deposit(...)`, after the deposit is successful:

- Account balance is updated.
- Transaction row is saved.
- Transaction history row is saved.
- An `OutboxMessage` is inserted with status `PENDING`.

The outbox payload looks like JSON:

```json
{
  "transactionId": 123,
  "ref": "TXN123456789",
  "amount": 5000,
  "dest": 10
}
```

Then `OutboxPublisherScheduler` runs every 5 seconds:

```java
@Scheduled(fixedDelay = 5000)
public void publishPendingEvents()
```

It reads:

```java
outboxRepository.findByStatus("PENDING")
```

Then publishes to Kafka:

```java
kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload())
```

Current topic:

```text
transaction-events
```

After successful send, the outbox message status becomes:

```text
PROCESSED
```

If Kafka publishing fails, the status becomes:

```text
FAILED
```

### Consumer Side: fraud-service

Files:

- `backend/fraud-service/src/main/java/com/banking/fraud/service/FraudListener.java`
- `backend/fraud-service/src/main/resources/application.yml`

The fraud service consumes transaction events:

```java
@KafkaListener(topics = "transaction-events", groupId = "fraud-group")
public void consumeTransactionEvent(String payload)
```

Inside the listener:

1. It receives the transaction JSON payload.
2. It extracts the transaction reference from `ref`.
3. It calls the Oracle procedure:

```sql
PKG_CORE_BANKING.PRC_DETECT_FRAUD
```

4. It logs whether the transaction is safe or suspicious based on the returned fraud flag and risk score.

## 3. Kafka Configuration In This Project

### transaction-service producer config

File:

```text
backend/transaction-service/src/main/resources/application.yml
```

Important config:

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
```

Interview explanation:

- `bootstrap-servers` tells the service where Kafka is running.
- The producer sends both key and value as strings.
- `acks: all` means the producer waits for full broker acknowledgment before treating the send as successful. This gives stronger delivery reliability.

### fraud-service consumer config

File:

```text
backend/fraud-service/src/main/resources/application.yml
```

Important config:

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: fraud-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

Interview explanation:

- `fraud-group` is the consumer group name.
- If multiple instances of fraud-service run with the same group id, Kafka distributes partitions among them.
- Deserializers convert Kafka byte messages back into Java strings.

## 4. Docker And Zookeeper Setup

File:

```text
docker-compose.yml
```

Services:

- `zookeeper`
- `kafka`
- `transaction-service`
- `fraud-service`

Kafka depends on Zookeeper:

```yaml
kafka:
  depends_on:
    - zookeeper
```

Kafka connects to Zookeeper using:

```yaml
KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
```

Inside Docker, services connect to Kafka using:

```text
kafka:29092
```

That is set for both:

- `transaction-service`
- `fraud-service`

Local machine access uses:

```text
localhost:9092
```

## 5. Why We Use A Message Queue Here

Use this answer in interviews:

We use Kafka/message queue because fraud detection should not block the main transaction API response. Transaction processing is the core banking operation, while fraud detection is a follow-up action. If transaction-service directly called fraud-service through REST, then transaction-service would become dependent on fraud-service availability and latency.

With Kafka:

- Services are loosely coupled.
- Transaction-service can publish an event and continue.
- Fraud-service can process independently.
- If fraud-service is temporarily down, events can remain in Kafka and be consumed later.
- More consumers can be added later, like notification-service, audit-service, reporting-service, or analytics-service, without changing transaction-service logic.

## 6. Why Kafka Instead Of Direct REST Calls

Direct REST is synchronous.

If transaction-service directly calls fraud-service:

- Transaction response becomes slower.
- Fraud-service failure can break transaction flow.
- Retry logic becomes harder.
- Adding more downstream services means adding more REST calls from transaction-service.

Kafka is asynchronous.

With Kafka:

- Transaction-service only publishes an event.
- Fraud-service reacts independently.
- Other services can subscribe to the same event later.
- It supports event replay and scalable consumers.

Good interview line:

I would use REST for immediate request-response operations, like fetching account details. I would use Kafka for events that other services need to react to asynchronously, like transaction completed, payment processed, fraud detected, or notification required.

## 7. Why Kafka Instead Of RabbitMQ

Kafka and RabbitMQ are both messaging systems, but they are optimized for different patterns.

Kafka is a distributed event streaming platform. It stores events in topics, supports high throughput, consumer groups, replay, and multiple independent consumers reading the same event stream.

RabbitMQ is a traditional message broker. It is excellent for task queues, routing, request/reply, and complex exchange patterns. Usually, once a message is consumed and acknowledged, it is removed from the queue.

Why Kafka fits this project:

- Banking transaction events are important business events.
- Multiple services may need the same event: fraud, audit, notification, reporting, analytics.
- Kafka can retain events for replay.
- Kafka handles high-volume transaction streams well.
- Consumer groups allow horizontal scaling.

Interview answer:

RabbitMQ would also work for simple queue-based fraud jobs. But Kafka is a better choice if we treat transactions as an event stream and want replay, high throughput, audit-style event history, and multiple independent consumers.

## 8. What Is Zookeeper's Role

In this project, Kafka uses the Confluent `cp-kafka:7.4.0` image with Zookeeper configuration.

Zookeeper helps Kafka manage cluster metadata, such as:

- Broker registration.
- Topic and partition metadata.
- Controller election.
- Cluster coordination.

In this local setup there is only one Kafka broker, but Zookeeper is still configured because this Kafka image/setup expects it.

Interview note:

Newer Kafka versions can run without Zookeeper using KRaft mode. But this project uses the classic Zookeeper-based Kafka setup, so Zookeeper is present as Kafka's coordination service.

## 9. What Is The Outbox Pattern And Why It Matters

The outbox pattern is used to avoid losing events when database work succeeds but Kafka publishing fails.

Instead of publishing directly to Kafka inside the transaction business logic, the service first saves an outbox row in the database.

Current flow:

1. Save business data.
2. Save `OUTBOX` event with `PENDING` status.
3. Scheduler reads pending events.
4. Scheduler publishes them to Kafka.
5. Scheduler marks them as `PROCESSED` or `FAILED`.

Why this is good:

- Event data is persisted.
- Failed publishing can be tracked.
- The system can retry or inspect failed events later.
- It avoids coupling business transaction success directly to Kafka availability.

Strong interview line:

In banking, reliability is important. The outbox pattern gives us a safer event publishing mechanism because the event is stored durably before being sent to Kafka.

## 10. Exact Event Flow In This Project

### Deposit flow

1. Client calls deposit API in `transaction-service`.
2. `transaction-service` updates `ACCOUNT_BALANCES`.
3. It saves the `Transaction`.
4. It saves `TransactionHistory`.
5. It inserts an `OUTBOX` row with `TransactionSuccessEvent`.
6. `OutboxPublisherScheduler` picks the pending outbox row.
7. Scheduler publishes the payload to Kafka topic `transaction-events`.
8. `fraud-service` consumes the event.
9. `fraud-service` calls Oracle fraud detection procedure.
10. Fraud result is logged.

### Transfer flow caveat

In the current code, `transferFunds(...)` calls the Oracle transfer procedure and returns the transaction reference, but it does not currently create an outbox event.

So in an interview, say:

The Kafka flow is implemented for transaction events through the outbox publisher, and the deposit path currently creates an outbox event. A natural improvement would be to also create outbox events for fund transfers and failed transactions, so fraud, audit, and notification services can react to all transaction types consistently.

## 11. Common Interview Questions And Answers

### Q1. Where exactly is Kafka used in your project?

Kafka is used between `transaction-service` and `fraud-service`. `transaction-service` publishes transaction events to the `transaction-events` topic using `KafkaTemplate`. `fraud-service` consumes those events using `@KafkaListener` with consumer group `fraud-group`.

### Q2. Why did you use Kafka?

We used Kafka to make transaction processing asynchronous and decoupled. Fraud detection should not slow down or break the main transaction API. Kafka lets the transaction service publish an event, while fraud-service processes it independently.

### Q3. What topic did you use?

The main topic is:

```text
transaction-events
```

There is also scheduler logic that can route `FRAUD` aggregate type messages to:

```text
fraud-events
```

But the main implemented consumer currently listens to `transaction-events`.

### Q4. Who is the producer?

`transaction-service` is the producer. More specifically, `OutboxPublisherScheduler` uses `KafkaTemplate<String, String>` to send messages.

### Q5. Who is the consumer?

`fraud-service` is the consumer. `FraudListener` has:

```java
@KafkaListener(topics = "transaction-events", groupId = "fraud-group")
```

### Q6. What is a consumer group?

A consumer group is a set of consumers that share the work of reading from a topic. In this project, the group id is `fraud-group`. If we run multiple fraud-service instances, Kafka can distribute topic partitions among them so fraud processing can scale.

### Q7. What happens if fraud-service is down?

The transaction-service can still publish events to Kafka. Fraud-service can consume them after it comes back, depending on Kafka retention and committed offsets. This is one reason Kafka is better than direct REST for this use case.

### Q8. What happens if Kafka is down?

In this code, the transaction service first writes to the `OUTBOX` table. If Kafka publishing fails, the outbox message is marked `FAILED`. A future improvement would be to add retry logic for failed outbox rows, dead-letter topics, and alerting.

### Q9. Why is `acks: all` used?

`acks: all` improves reliability because the producer waits for broker acknowledgment before considering the send successful. In production with replicated Kafka partitions, this helps reduce the chance of message loss.

### Q10. Is Kafka synchronous or asynchronous?

Kafka is used asynchronously here. The transaction-service does not wait for fraud-service to process the message. It only publishes the event, and fraud-service processes it separately.

### Q11. Why not RabbitMQ?

RabbitMQ is good for traditional work queues and routing. Kafka is better for durable event streams, replay, high throughput, and multiple independent consumers. Since banking transaction events may later be consumed by fraud, audit, notifications, reporting, and analytics, Kafka is a strong fit.

### Q12. What is Zookeeper doing?

In this setup, Zookeeper coordinates Kafka cluster metadata and broker management. Kafka connects to it using `zookeeper:2181`. Newer Kafka can use KRaft instead, but this project uses a Zookeeper-based Kafka deployment.

### Q13. What improvement would you make?

I would:

- Publish events for `transferFunds(...)` also, not only deposit.
- Add retry handling for `FAILED` outbox messages.
- Add a dead-letter topic for messages that repeatedly fail.
- Use structured event DTOs/ObjectMapper instead of manually building JSON strings.
- Add idempotency in consumers so duplicate messages do not cause duplicate side effects.
- Add monitoring for consumer lag and failed outbox count.

## 12. Short Version To Say In Interview

In my banking microservices project, Kafka is used for event-driven communication. The transaction service acts as a producer. After a successful deposit, it stores an outbox event in the database. A scheduled publisher reads pending outbox rows every 5 seconds and sends them to the Kafka topic `transaction-events` using Spring Kafka's `KafkaTemplate`. The fraud service acts as a consumer. It listens to `transaction-events` with `@KafkaListener` and group id `fraud-group`, parses the transaction event, and calls an Oracle fraud detection procedure.

We used Kafka to decouple transaction processing from fraud detection. This avoids synchronous dependency between services, improves resilience, and allows future services like audit, notification, reporting, and analytics to consume the same events. Kafka was preferred over RabbitMQ because this use case is closer to event streaming, where retention, replay, high throughput, and multiple independent consumers are useful. Zookeeper is used in this setup to coordinate the Kafka broker and manage cluster metadata.

