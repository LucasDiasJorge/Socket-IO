package com.service.socketio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringSocketProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringSocketProjectApplication.class, args);
	}
}
