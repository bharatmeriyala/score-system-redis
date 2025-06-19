// src/main/java/com/example/scoresystem/config/RedisConfig.java
package com.example.scoresystem.config;

import com.example.scoresystem.redis.LeaderboardKeyspaceEventListener;
import com.example.scoresystem.redis.ScoreUpdatePubSubSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.PatternTopic; // For keyspace notifications
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${app.redis.pubsub-channel}")
    private String pubSubChannelName;

    @Value("${app.redis.leaderboard-key}")
    private String leaderboardKey;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // --- Redis Pub/Sub Configuration ---

    @Bean
    public ChannelTopic scoreUpdateTopic() {
        return new ChannelTopic(pubSubChannelName);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ScoreUpdatePubSubSubscriber scoreUpdatePubSubSubscriber,
            LeaderboardKeyspaceEventListener leaderboardKeyspaceEventListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Add listener for generic Pub/Sub channel
        container.addMessageListener(scoreUpdatePubSubSubscriber, scoreUpdateTopic());

        // Add listener for Redis Keyspace Notifications
        // The pattern for keyspace notifications is "__keyspace@<db>__:<key>"
        // For events on our "leaderboard" key, it will be "__keyspace@0__:leaderboard" (assuming DB 0)
        container.addMessageListener(leaderboardKeyspaceEventListener, new PatternTopic("__keyspace@*:*" + leaderboardKey));

        return container;
    }
}