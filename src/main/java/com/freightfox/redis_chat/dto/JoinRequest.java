package com.freightfox.redis_chat.dto;

import jakarta.validation.constraints.NotBlank;

public class JoinRequest {
    @NotBlank(message = "Participant name is required")
    private String participant;

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }
}
