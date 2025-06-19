// src/main/java/com/example/scoresystem/redis/ScoreUpdatePubSubSubscriber.java
package com.example.scoresystem.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScoreUpdatePubSubSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String receivedMessage = new String(message.getBody());
        log.info("Redis Pub/Sub Received: Channel='{}', Message='{}'", channel, receivedMessage);
    }
}