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

import com.freightfox.redis_chat.service.ChatService;

@WebMvcTest(com.freightfox.redis_chat.controller.ChatController.class)
class JoinRoomTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void testJoinRoomSuccess() throws Exception {
        String roomId = "testRoom";
        String userName = "user1";
        when(chatService.joinRoom(roomId, userName)).thenReturn(true);

        String jsonRequest = "{\"participant\":\"" + userName + "\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms/" + roomId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Participant '" + userName + "' joined room '" + roomId + "' successfully."));
    }

    @Test
    void testJoinRoomNotFound() throws Exception {
        String roomId = "nonExistentRoom";
        String userName = "user1";
        when(chatService.joinRoom(roomId, userName)).thenReturn(false);

        String jsonRequest = "{\"participant\":\"" + userName + "\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms/" + roomId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Room not found"));
    }

    @Test
    void testJoinRoomInvalidInput() throws Exception {
        String roomId = "testRoom";
        String jsonRequest = "{\"participant\":\"\"}";

        mockMvc.perform(post("/api/chatapp/chatrooms/" + roomId + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Participant name is required"));
    }
}
