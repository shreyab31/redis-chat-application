package com.freightfox.redis_chat.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.freightfox.redis_chat.dto.ChatMessage;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class ChatService {
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> createRoomScript;
    private final ObjectMapper objectMapper;

    public ChatService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.createRoomScript = new DefaultRedisScript<>();
        this.createRoomScript.setResultType(Long.class);
        this.createRoomScript.setScriptText(
            "if redis.call('EXISTS', KEYS[1]) == 1 or redis.call('EXISTS', KEYS[2]) == 1 then " +
            "  return 0 " +
            "end " +
            "redis.call('HSET', KEYS[1], 'name', ARGV[1], 'createdAt', ARGV[2]) " +
            "return 1"
        );
    }

    public boolean createRoom(String roomName) {
        String metaKey = "{room:" + roomName + "}:meta";
        String membersKey = "{room:" + roomName + "}:members";
        String createdAt = String.valueOf(System.currentTimeMillis());

        Long result = redisTemplate.execute(
            createRoomScript,
            Arrays.asList(metaKey, membersKey),
            roomName,
            createdAt
        );
        return result != null && result == 1L;
    }

    public boolean joinRoom(String roomName, String participantName) {
        String metaKey = "{room:" + roomName + "}:meta";
        String membersKey = "{room:" + roomName + "}:members";

        if (Boolean.FALSE.equals(redisTemplate.hasKey(metaKey))) {
            return false;
        }

        redisTemplate.opsForSet().add(membersKey, participantName);
        return true;
    }

    public boolean sendMessage(String roomName, String participant, String message) {
        String metaKey = "{room:" + roomName + "}:meta";
        String messagesKey = "{room:" + roomName + "}:messages";
        String channelKey = "room:" + roomName + ":events";

        if (Boolean.FALSE.equals(redisTemplate.hasKey(metaKey))) {
            return false;
        }

        String timestamp = java.time.Instant.now().toString();
        ChatMessage chatMsg = new ChatMessage(participant, message, timestamp);

        try {
            String payload = objectMapper.writeValueAsString(chatMsg);
            redisTemplate.opsForList().rightPush(messagesKey, payload);
            redisTemplate.convertAndSend(channelKey, payload);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }

        return true;
    }

    public List<ChatMessage> getMessages(String roomName, int limit) {
        String metaKey = "{room:" + roomName + "}:meta";
        String messagesKey = "{room:" + roomName + "}:messages";

        if (Boolean.FALSE.equals(redisTemplate.hasKey(metaKey))) {
            return null;
        }

        Long size = redisTemplate.opsForList().size(messagesKey);
        if (size == null || size == 0) return Collections.emptyList();

        long start = Math.max(0, size - limit);
        List<String> rawMessages = redisTemplate.opsForList().range(messagesKey, start, -1);

        if (rawMessages == null) return Collections.emptyList();

        return rawMessages.stream().map(msg -> {
            try {
                return objectMapper.readValue(msg, ChatMessage.class);
            } catch (JacksonException e) {
                return new ChatMessage("Unknown", "Error parsing message", "");
            }
        }).collect(Collectors.toList());
    }
}
