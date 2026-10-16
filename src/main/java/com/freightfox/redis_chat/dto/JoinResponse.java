package com.freightfox.redis_chat.dto;

public class JoinResponse {
    private final String message;
    private final String roomId;
    private final String status;

    public JoinResponse(String message, String roomId, String status) {
        this.message = message;
        this.roomId = roomId;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getStatus() {
        return status;
    }
}
