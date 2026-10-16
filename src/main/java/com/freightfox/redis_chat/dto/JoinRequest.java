package com.freightfox.redis_chat.dto;

import jakarta.validation.constraints.NotBlank;

public class JoinRequest {
    @NotBlank(message = "Participant name is required")
    private String participantName;

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }
}
