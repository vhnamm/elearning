package com.hnv.elearning;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElearningApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(dotenvEntry -> System.setProperty(dotenvEntry.getKey(), dotenvEntry.getValue()));
		SpringApplication.run(ElearningApplication.class, args);
	}

}
