package com.phihai91.springgraphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class SpringGraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringGraphqlApplication.class, args);
	}

}
