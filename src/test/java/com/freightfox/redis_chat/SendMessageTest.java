package com.freightfox.redis_chat;

import com.freightfox.redis_chat.dto.MessageRequest;
import com.freightfox.redis_chat.dto.MessageResponse;
import com.freightfox.redis_chat.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.freightfox.redis_chat.controller.ChatController.class)
class SendMessageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void testSendMessageSuccess() throws Exception {
        String roomId = "testRoom";
        String participant = "user1";
        String message = "Hello world";
        when(chatService.sendMessage(roomId, participant, message)).thenReturn(true);

        String jsonRequest = String.format("{\"participant\":\"%s\",\"message\":\"%s\"}", participant, message);

        mockMvc.perform(post("/api/chatapp/chatrooms/" + roomId + "/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Message sent successfully."));
    }

    @Test
    void testSendMessageRoomNotFound() throws Exception {
        String roomId = "nonExistentRoom";
        String participant = "user1";
        String message = "Hello";
        when(chatService.sendMessage(roomId, participant, message)).thenReturn(false);

        String jsonRequest = String.format("{\"participant\":\"%s\",\"message\":\"%s\"}", participant, message);

        mockMvc.perform(post("/api/chatapp/chatrooms/" + roomId + "/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Room not found"));
    }

    @Test
    void testSendMessageInvalidInput() throws Exception {
        String roomId = "testRoom";
        String jsonRequest = "{\"participant\":\"\",\"message\":\"hello\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms/" + roomId + "/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }
}
