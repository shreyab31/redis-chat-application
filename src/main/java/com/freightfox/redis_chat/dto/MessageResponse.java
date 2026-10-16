package com.freightfox.redis_chat.dto;

public class MessageResponse {
    private final String message;
    private final String status;

    public MessageResponse(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}
