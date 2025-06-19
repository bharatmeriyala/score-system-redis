// src/main/java/com/example/scoresystem/controller/LeaderboardController.java
package com.example.scoresystem.controller;

import com.example.scoresystem.model.ScoreUpdate;
import com.example.scoresystem.producer.ScoreUpdateProducer;
import com.example.scoresystem.service.ScoreService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final ScoreService scoreService;
    private final ScoreUpdateProducer scoreUpdateProducer;

    public LeaderboardController(ScoreService scoreService, ScoreUpdateProducer scoreUpdateProducer) {
        this.scoreService = scoreService;
        this.scoreUpdateProducer = scoreUpdateProducer;
    }

    @PostMapping("/update-kafka")
    public String sendScoreUpdateToKafka(@RequestBody ScoreUpdate scoreUpdate) {
        scoreUpdateProducer.sendScoreUpdate(scoreUpdate);
        return "Score update sent to Kafka for " + scoreUpdate.getUserId();
    }

    @GetMapping("/top/{count}")
    public Map<Object, Double> getTopScores(@PathVariable int count) {
        Set<ZSetOperations.TypedTuple<Object>> topUsers = scoreService.getTopUsers(count);
        return topUsers.stream()
                .collect(Collectors.toMap(ZSetOperations.TypedTuple::getValue, ZSetOperations.TypedTuple::getScore));
    }

    @GetMapping("/user/{userId}/score")
    public Double getUserScore(@PathVariable String userId) {
        return scoreService.getUserScore(userId);
    }

    @GetMapping("/user/{userId}/rank")
    public Long getUserRank(@PathVariable String userId) {
        return scoreService.getUserRank(userId);
    }
}