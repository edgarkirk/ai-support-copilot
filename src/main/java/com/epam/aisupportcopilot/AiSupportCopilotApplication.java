package com.epam.aisupportcopilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class AiSupportCopilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiSupportCopilotApplication.class, args);
	}

}
