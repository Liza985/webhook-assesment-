package com.example.webhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebhookApplication {

	public static void main(String[] args) {
		System.out.println("🚀 Starting Application...");
		SpringApplication.run(WebhookApplication.class, args);

	}

}
