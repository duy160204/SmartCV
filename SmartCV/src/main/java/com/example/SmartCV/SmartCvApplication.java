package com.example.SmartCV;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cache.annotation.EnableCaching
public class SmartCvApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartCvApplication.class, args);
	}

}
