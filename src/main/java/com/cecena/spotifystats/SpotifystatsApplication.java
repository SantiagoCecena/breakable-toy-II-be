package com.cecena.spotifystats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpotifystatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotifystatsApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate () {
		return new RestTemplate();
	}

}
