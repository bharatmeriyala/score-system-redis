// src/main/java/com/example/scoresystem/model/ScoreUpdate.java
package com.example.scoresystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreUpdate implements Serializable {
    private String userId;
    private double scoreChange;
    private long timestamp = System.currentTimeMillis(); // Added for more context in logs/messages
}