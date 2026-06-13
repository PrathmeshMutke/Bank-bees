package com.banking.ai.controller;

import com.banking.core.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AIChatController {

    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> queryChat(@RequestBody ChatRequest request) {
        log.info("POST /api/v1/ai/chat - userId={}, messageLength={}",
                request.getUserId(), request.getMessage() != null ? request.getMessage().length() : 0);
        String msg = request.getMessage().toLowerCase();
        String reply;

        if (msg.contains("balance") || msg.contains("money")) {
            reply = "Your current account balances are: Savings: Rs. 45,200.50, Current: Rs. 1,20,000.00. Use the quick actions panel to transfer funds.";
        } else if (msg.contains("limit") || msg.contains("card")) {
            reply = "You can customize daily transaction and ATM withdrawal limits directly on the Cards Management panel on your dashboard.";
        } else if (msg.contains("fraud") || msg.contains("suspend")) {
            reply = "If you notice unauthorized transfers, you can freeze your debit card instantly under the Cards panel or submit a dispute case.";
        } else if (msg.contains("loan") || msg.contains("emi")) {
            reply = "We offer Personal, Home, and Vehicle loans. You can calculate your monthly EMI schedules and submit digital applications directly.";
        } else if (msg.contains("save") || msg.contains("budget") || msg.contains("insight")) {
            reply = "Financial Insight: You spent 32% on dining and 15% on shopping this month. We recommend allocating Rs. 5,000 to your Recurring Deposit to earn 7.1% interest.";
        } else {
            reply = "I am your AI Banking Assistant. I can help with balance inquiries, card limits, budget insights, and digital loan applications. What can I do for you today?";
        }

        ChatResponse response = new ChatResponse();
        response.setResponse(reply);
        log.info("AI chat response generated for userId={}, category={}",
                request.getUserId(), resolveCategory(msg));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String resolveCategory(String msg) {
        if (msg.contains("balance") || msg.contains("money")) return "balance";
        if (msg.contains("limit") || msg.contains("card")) return "cards";
        if (msg.contains("fraud") || msg.contains("suspend")) return "fraud";
        if (msg.contains("loan") || msg.contains("emi")) return "loans";
        if (msg.contains("save") || msg.contains("budget") || msg.contains("insight")) return "insights";
        return "general";
    }

    public static class ChatRequest {
        private String message;
        private Long userId;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static class ChatResponse {
        private String response;

        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
    }
}
