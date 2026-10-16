package com.freightfox.redis_chat.dto;

import java.util.List;

public class MessageHistoryResponse {
    private final List<ChatMessage> messages;

    public MessageHistoryResponse(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
