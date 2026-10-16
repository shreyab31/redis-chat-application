package com.freightfox.redis_chat.dto;

public class ChatMessage {
    private final String participant;
    private final String message;
    private final String timestamp;

    public ChatMessage() {
    this.participant = null;
    this.message = null;
    this.timestamp = null;
}

    public ChatMessage(String participant, String message, String timestamp) {
        this.participant = participant;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getParticipant() {
        return participant;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
