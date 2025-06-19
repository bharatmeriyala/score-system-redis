// src/main/java/com/example/scoresystem/redis/LeaderboardKeyspaceEventListener.java
package com.example.scoresystem.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LeaderboardKeyspaceEventListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel()); // e.g., "__keyspace@0__:leaderboard"
        String event = new String(message.getBody());       // e.g., "zincrby", "zadd"

        log.info("Redis Keyspace Notification: Channel='{}', Event='{}'", channel, event);

        // You can parse the channel and event to react specifically
        if (channel.contains("leaderboard")) {
            log.info("Leaderboard key was modified. Event type: {}, message: {}", event, message);
            // Example: If you need to re-cache top scores or trigger other actions
        }
    }
}