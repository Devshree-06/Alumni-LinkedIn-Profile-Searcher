package com.Alumni_LinkedIn_Profile_Searcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class AlumniLinkedInProfileSearcher {

	public static void main(String[] args) {
		SpringApplication.run(AlumniLinkedInProfileSearcher.class, args);
	}


	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}

}
