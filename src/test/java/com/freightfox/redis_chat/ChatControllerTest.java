package com.freightfox.redis_chat;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.freightfox.redis_chat.controller.ChatController;
import com.freightfox.redis_chat.service.ChatService;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void testCreateRoomSuccess() throws Exception {
        String roomName = "testRoom";
        when(chatService.createRoom(roomName)).thenReturn(true);

        String jsonRequest = "{\"roomName\":\"" + roomName + "\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(roomName))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Chat room '" + roomName + "' created successfully."));
    }

    @Test
    void testCreateRoomDuplicate() throws Exception {
        String roomName = "existingRoom";
        when(chatService.createRoom(roomName)).thenReturn(false);

        String jsonRequest = "{\"roomName\":\"" + roomName + "\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.roomId").value(roomName))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Room already exists"));
    }

    @Test
    void testCreateRoomValidationBlank() throws Exception {
        String jsonRequest = "{\"roomName\":\"\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRoomValidationTooShort() throws Exception {
        String jsonRequest = "{\"roomName\":\"ab\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRoomValidationTooLong() throws Exception {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 51; i++) longName.append("a");
        String jsonRequest = "{\"roomName\":\"" + longName.toString() + "\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }
}
