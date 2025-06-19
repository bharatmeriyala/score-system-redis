// src/main/java/com/example/scoresystem/consumer/ScoreUpdateConsumer.java
package com.example.scoresystem.consumer;

import com.example.scoresystem.model.ScoreUpdate;
import com.example.scoresystem.service.ScoreService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScoreUpdateConsumer {

    private final ScoreService scoreService;

    public ScoreUpdateConsumer(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @KafkaListener(topics = "${kafka.topic.name:score-updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ScoreUpdate scoreUpdate) {
        log.info("Received ScoreUpdate from Kafka: {}", scoreUpdate);
        scoreService.updateUserScore(scoreUpdate.getUserId(), scoreUpdate.getScoreChange());
    }
}