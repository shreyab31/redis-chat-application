package com.freightfox.redis_chat.integration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.freightfox.redis_chat.dto.ChatMessage;
import com.freightfox.redis_chat.service.ChatService;

@SpringBootTest
class PubSubIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    private final String TEST_ROOM = "pubsub_test_room_" + System.currentTimeMillis();
    private final String TEST_USER = "tester";
    private final String TEST_MSG = "Hello PubSub!";

    @BeforeEach
    void setUp() {
        chatService.createRoom(TEST_ROOM);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("{room:" + TEST_ROOM + "}:meta");
        redisTemplate.delete("{room:" + TEST_ROOM + "}:members");
        redisTemplate.delete("{room:" + TEST_ROOM + "}:messages");
    }

    @Test
    void testMessagePublicationAndConsistency() throws InterruptedException {
        String channel = "room:" + TEST_ROOM + ":events";
        BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();

        RedisMessageListenerContainer localContainer = new RedisMessageListenerContainer();
        localContainer.setConnectionFactory(connectionFactory);

        var adapter = new MessageListenerAdapter(new org.springframework.data.redis.connection.MessageListener() {
            @Override
            public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
                receivedMessages.add(new String(message.getBody(), StandardCharsets.UTF_8));
            }
        });

        try {
            localContainer.addMessageListener(adapter, new ChannelTopic(channel));
            localContainer.afterPropertiesSet();
            localContainer.start();

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while ((!localContainer.isRunning()) && System.nanoTime() < deadline) {
                Thread.sleep(50);
            }

            Thread.sleep(500); // Allow the Redis subscription to become active

            boolean sent = chatService.sendMessage(TEST_ROOM, TEST_USER, TEST_MSG);
            assertTrue(sent, "Message should be sent successfully");

            String pubSubPayload = receivedMessages.poll(5, TimeUnit.SECONDS);
            assertNotNull(pubSubPayload, "A message should have been received via Pub/Sub");

            List<ChatMessage> history = chatService.getMessages(TEST_ROOM, 1);
            assertNotNull(history, "Message history should be retrievable");
            assertEquals(1, history.size(), "There should be exactly one message in history");

            ChatMessage persistedMsg = history.get(0);

            assertEquals(TEST_USER, persistedMsg.getParticipant());
            assertEquals(TEST_MSG, persistedMsg.getMessage());
            assertTrue(pubSubPayload.contains(persistedMsg.getParticipant()));
            assertTrue(pubSubPayload.contains(persistedMsg.getMessage()));
            assertTrue(pubSubPayload.contains(persistedMsg.getTimestamp()));
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            localContainer.stop();
        }
    }
}
