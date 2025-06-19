// src/main/java/com/example/scoresystem/producer/ScoreUpdateProducer.java
package com.example.scoresystem.producer;

import com.example.scoresystem.model.ScoreUpdate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScoreUpdateProducer {

    private final KafkaTemplate<String, ScoreUpdate> kafkaTemplate;
    private final String topicName = "score-updates"; // Matches application.yml

    public ScoreUpdateProducer(KafkaTemplate<String, ScoreUpdate> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendScoreUpdate(ScoreUpdate scoreUpdate) {
        log.info("Sending ScoreUpdate to Kafka: {}", scoreUpdate);
        kafkaTemplate.send(topicName, scoreUpdate.getUserId(), scoreUpdate)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent to Kafka successfully: offset={}", result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message to Kafka", ex);
                    }
                });
    }
}