package com.banking.fraud.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Types;

@Service
public class FraudListener {

    private static final Logger log = LoggerFactory.getLogger(FraudListener.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FraudListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics = "transaction-events", groupId = "fraud-group")
    public void consumeTransactionEvent(String payload) {
        log.info("[FraudListener] Received transaction event: {}", payload);
        try {
            JsonNode event = objectMapper.readTree(payload);
            String txnRef = event.get("ref").asText();

            // Spring Framework 6 removed SimpleJdbcCall.withPackageName(); use CallableStatement directly
            int[] isFraudHolder = new int[1];
            double[] riskScoreHolder = new double[1];
            jdbcTemplate.execute((java.sql.Connection con) -> {
                CallableStatement cs = con.prepareCall(
                    "{call PKG_CORE_BANKING.PRC_DETECT_FRAUD(?,?,?)}" );
                cs.setString(1, txnRef);
                cs.registerOutParameter(2, Types.INTEGER);
                cs.registerOutParameter(3, Types.DOUBLE);
                cs.execute();
                isFraudHolder[0] = cs.getInt(2);
                riskScoreHolder[0] = cs.getDouble(3);
                return cs;
            });

            if (isFraudHolder[0] == 1) {
                log.warn("[FraudListener] Fraud detected on transaction ref={}, riskScore={}", txnRef, riskScoreHolder[0]);
            } else {
                log.info("[FraudListener] Transaction ref={} verified as safe, riskScore={}", txnRef, riskScoreHolder[0]);
            }
        } catch (Exception e) {
            log.error("[FraudListener] Failed to evaluate fraud for payload={}, error={}", payload, e.getMessage());
        }
    }
}
