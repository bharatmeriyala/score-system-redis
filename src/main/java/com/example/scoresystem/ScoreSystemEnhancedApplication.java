package com.example.scoresystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ScoreSystemEnhancedApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScoreSystemEnhancedApplication.class, args);
	}

}
