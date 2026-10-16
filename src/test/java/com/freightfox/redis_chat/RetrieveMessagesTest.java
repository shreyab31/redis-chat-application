package com.freightfox.redis_chat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.freightfox.redis_chat.dto.ChatMessage;
import com.freightfox.redis_chat.service.ChatService;

@WebMvcTest(com.freightfox.redis_chat.controller.ChatController.class)
class RetrieveMessagesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void testGetMessagesSuccess() throws Exception {
        String roomId = "testRoom";
        var msg1 = new ChatMessage("user1", "Hello", "2024-01-01T10:00:00Z");
var msg2 = new ChatMessage("user2", "Hi", "2024-01-01T10:01:00Z");
        when(chatService.getMessages(eq(roomId), anyInt())).thenReturn(Arrays.asList(msg1, msg2));

        mockMvc.perform(get("/api/chatapp/chatrooms/" + roomId + "/messages?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages[0].participant").value("user1"))
                .andExpect(jsonPath("$.messages[0].message").value("Hello"))
                .andExpect(jsonPath("$.messages[0].timestamp").value("2024-01-01T10:00:00Z"));
    }

    @Test
    void testGetMessagesRoomNotFound() throws Exception {
        String roomId = "missingRoom";
        when(chatService.getMessages(eq(roomId), anyInt())).thenReturn(null);

        mockMvc.perform(get("/api/chatapp/chatrooms/" + roomId + "/messages?limit=10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMessagesEmpty() throws Exception {
        String roomId = "emptyRoom";
        when(chatService.getMessages(eq(roomId), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/chatapp/chatrooms/" + roomId + "/messages?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isEmpty());
    }

    @Test
    void testGetMessagesInvalidLimit() throws Exception {
        String roomId = "testRoom";
        mockMvc.perform(get("/api/chatapp/chatrooms/" + roomId + "/messages?limit=-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/chatapp/chatrooms/" + roomId + "/messages?limit=101"))
                .andExpect(status().isBadRequest());
    }
}
