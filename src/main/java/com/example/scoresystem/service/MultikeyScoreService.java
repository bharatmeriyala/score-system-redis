//// src/main/java/com/example/scoresystem/service/ScoreService.java
//package com.example.scoresystem.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.core.RedisOperations;   // <--- Ensure this import is present
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SessionCallback;   // <--- Ensure this import is present
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.stereotype.Service;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@Service
//@Slf4j
//public class MultikeyScoreService {
//
//    @Value("${app.redis.leaderboard-key}")
//    private String leaderboardKey;
//
//    @Value("${app.redis.user-details-key-prefix}")
//    private String userDetailsKeyPrefix; // Will be "user" now
//
//    @Value("${app.redis.pubsub-channel}")
//    private String pubSubChannelName;
//
//    private RedisTemplate<String, Object> redisTemplate = null;
//    private ObjectMapper objectMapper = null;
//    private ZSetOperations<String, Object> zSetOperations;
//
//    public MultikeyScoreService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
//        this.redisTemplate = redisTemplate;
//        this.objectMapper = objectMapper;
//    }
//
//    @PostConstruct
//    private void init() {
//        zSetOperations = redisTemplate.opsForZSet();
//    }
//
//    public void updateUserScore(String userId, double scoreChange) {
//        // Corrected key construction: user:userId:details
//        String userDetailsKey = userDetailsKeyPrefix + ":" + userId + ":details";
//
//        // Perform multi-key operations within a Redis transaction using SessionCallback
//        List<Object> transactionResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
//
//            @Override
//            // Fix: Specify generics for RedisOperations to match RedisTemplate's type
//            @SuppressWarnings("unchecked") // Still needed because the raw 'operations.exec()' returns List<Object>
//            public List<Object> execute(RedisOperations<String, Object> operations) {
//                operations.multi(); // Start the Redis transaction (MULTI command)
//
//                // --- Multi-Key Operations ---
//
//                // 1. Update score in Sorted Set (on the 'leaderboard' key)
//                operations.opsForZSet().incrementScore(leaderboardKey, userId, scoreChange);
//                log.debug("Queued ZINCRBY on '{}' for user '{}'", leaderboardKey, userId);
//
//                // 2. Update user details in Hash (on the 'user:{userId}:details' key)
//                operations.opsForHash().increment(userDetailsKey, "total_updates", 1L); // Increment a counter field
//                operations.opsForHash().put(userDetailsKey, "last_update_timestamp", String.valueOf(System.currentTimeMillis())); // Set timestamp
//                operations.opsForHash().put(userDetailsKey, "last_score_change", String.valueOf(scoreChange)); // Store the last change value
//                log.debug("Queued HINCRBY and HSET on '{}' for user '{}'", userDetailsKey, userId);
//
//                // 3. Get the new score after the increment (this command's result will be part of the transaction's response list)
//                operations.opsForZSet().score(leaderboardKey, userId);
//                log.debug("Queued ZSCORE on '{}' for user '{}'", leaderboardKey, userId);
//
//                return operations.exec(); // Execute the transaction (EXEC command)
//            }
//        });
//
//        // Process the transaction results
//        if (transactionResults != null && !transactionResults.isEmpty()) {
//            // The last item in the list should be the result of the ZSCORE command, returning the new score as a Double.
//            Double newScore = (Double) transactionResults.get(transactionResults.size() - 1);
//
//            log.info("Multi-key transaction completed for user '{}'. New score: {}", userId, newScore);
//            log.debug("Full transaction results: {}", transactionResults);
//
//            // Publish update to Redis Pub/Sub (after successful transaction)
//            try {
//                String message = String.format("User %s score updated to %.2f (change: %.2f)", userId, newScore, scoreChange);
//                redisTemplate.convertAndSend(pubSubChannelName, message);
//                log.info("Published to Redis Pub/Sub channel '{}': {}", pubSubChannelName, message);
//            } catch (Exception e) {
//                log.error("Error publishing to Redis Pub/Sub: {}", e.getMessage(), e);
//            }
//        } else {
//            log.error("Multi-key transaction failed or returned unexpected/null results for user '{}'. Results: {}", userId, transactionResults);
//            // Handle transaction failure or empty results appropriately
//        }
//    }
//
//    // --- Existing methods (getTopUsers, getBottomUsers, getUserScore, getUserRank) remain the same ---
//
//    public Set<ZSetOperations.TypedTuple<Object>> getTopUsers(long count) {
//        return zSetOperations.reverseRangeWithScores(leaderboardKey, 0, count - 1);
//    }
//
//    public Set<ZSetOperations.TypedTuple<Object>> getBottomUsers(long count) {
//        return zSetOperations.rangeWithScores(leaderboardKey, 0, count - 1);
//    }
//
//    public Double getUserScore(String userId) {
//        return zSetOperations.score(leaderboardKey, userId);
//    }
//
//    public Long getUserRank(String userId) {
//        return zSetOperations.reverseRank(leaderboardKey, userId);
//    }
//
//    /**
//     * Retrieves additional details for a specific user from their Hash key.
//     * This demonstrates fetching data from the new user-specific Hash key.
//     */
//    public Map<Object, Object> getUserDetails(String userId) {
//        // Corrected key construction: user:userId:details
//        String userDetailsKey = userDetailsKeyPrefix + ":" + userId + ":details";
//        return redisTemplate.opsForHash().entries(userDetailsKey);
//    }
//}