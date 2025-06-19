// src/main/java/com/example/scoresystem/service/ScoreService.java
package com.example.scoresystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Service
@Slf4j
public class ScoreService {

    @Value("${app.redis.leaderboard-key}")
    private String leaderboardKey;

    @Value("${app.redis.pubsub-channel}")
    private String pubSubChannelName;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // To serialize messages for Pub/Sub
    private ZSetOperations<String, Object> zSetOperations;

    public ScoreService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        zSetOperations = redisTemplate.opsForZSet();
    }

    public void updateUserScore(String userId, double scoreChange) {
        Double newScore = zSetOperations.incrementScore(leaderboardKey, userId, scoreChange);
        log.info("User '{}' new score: {}", userId, newScore);

        // Publish update to Redis Pub/Sub
        try {
            String message = String.format("User %s score updated to %.2f (change: %.2f)", userId, newScore, scoreChange);
            redisTemplate.convertAndSend(pubSubChannelName, message);
            log.info("Published to Redis Pub/Sub channel '{}': {}", pubSubChannelName, message);
        } catch (Exception e) {
            log.error("Error publishing to Redis Pub/Sub: {}", e.getMessage(), e);
        }
    }

    public Set<ZSetOperations.TypedTuple<Object>> getTopUsers(long count) {
        return zSetOperations.reverseRangeWithScores(leaderboardKey, 0, count - 1);
    }

    public Double getUserScore(String userId) {
        return zSetOperations.score(leaderboardKey, userId);
    }

    public Long getUserRank(String userId) {
        return zSetOperations.reverseRank(leaderboardKey, userId);
    }
}