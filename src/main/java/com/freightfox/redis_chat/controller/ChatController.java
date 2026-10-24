package com.freightfox.redis_chat.controller;

import com.freightfox.redis_chat.dto.JoinRequest;
import com.freightfox.redis_chat.dto.JoinResponse;
import com.freightfox.redis_chat.dto.MessageRequest;
import com.freightfox.redis_chat.dto.MessageResponse;
import com.freightfox.redis_chat.dto.MessageHistoryResponse;
import com.freightfox.redis_chat.dto.RoomRequest;
import com.freightfox.redis_chat.dto.RoomResponse;


import com.freightfox.redis_chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatapp")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chatrooms")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        String roomName = request.getRoomName();
        boolean created = chatService.createRoom(roomName);

        if (created) {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RoomResponse(
                    "Chat room '" + roomName + "' created successfully.",
                    roomName,
                    "success"
                ));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new RoomResponse(
                    "Room already exists",
                    roomName,
                    "error"
                ));
        }
    }

    @PostMapping("/chatrooms/{roomId}/join")
    public ResponseEntity<JoinResponse> joinRoom(@PathVariable String roomId, @Valid @RequestBody JoinRequest request) {
        boolean joined = chatService.joinRoom(roomId, request.getParticipant());
        if (joined) {
            return ResponseEntity.ok(new JoinResponse(
                "Participant '" + request.getParticipant() + "' joined room '" + roomId + "' successfully.",
                roomId,
                "success"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JoinResponse("Room not found", roomId, "error"));
        }
    }

    @PostMapping("/chatrooms/{roomId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable String roomId, @Valid @RequestBody MessageRequest request) {
        boolean sent = chatService.sendMessage(roomId, request.getParticipant(), request.getMessage());
        if (sent) {
            return ResponseEntity.ok(new MessageResponse("Message sent successfully.", "success"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Room not found", "error"));
        }
    }

    @GetMapping("/chatrooms/{roomId}/messages")
    public ResponseEntity<MessageHistoryResponse> getMessages(@PathVariable String roomId,
                                                              @RequestParam(defaultValue = "10") int limit) {
        if (limit <= 0 || limit > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        var messages = chatService.getMessages(roomId, limit);
        if (messages == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(new MessageHistoryResponse(messages));
    }
}
