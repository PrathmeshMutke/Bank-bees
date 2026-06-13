package com.banking.transaction.service;

import com.banking.transaction.entity.OutboxMessage;
import com.banking.transaction.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@EnableScheduling
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TRANSACTION_TOPIC = "transaction-events";

    public OutboxPublisherScheduler(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByStatus("PENDING");
        
        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("[OutboxPublisherScheduler] Found {} pending outbox messages to publish to Kafka", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                String topic = TRANSACTION_TOPIC;
                if ("FRAUD".equals(message.getAggregateType())) {
                    topic = "fraud-events";
                }
                
                kafkaTemplate.send(topic, message.getAggregateId(), message.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                updateMessageStatus(message.getId(), "PROCESSED");
                            } else {
                                log.error("[OutboxPublisherScheduler] Failed to send outbox message to Kafka: {}", ex.getMessage());
                                updateMessageStatus(message.getId(), "FAILED");
                            }
                        });
            } catch (Exception e) {
                log.error("[OutboxPublisherScheduler] Error processing outbox message {}: {}", message.getId(), e.getMessage());
                message.setStatus("FAILED");
                outboxRepository.save(message);
            }
        }
    }

    @Transactional
    public void updateMessageStatus(Long id, String status) {
        outboxRepository.findById(id).ifPresent(msg -> {
            msg.setStatus(status);
            outboxRepository.save(msg);
        });
    }
}
