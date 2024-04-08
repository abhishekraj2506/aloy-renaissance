package com.aloy.sellerbppservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EnableMongoRepositories("com.aloy.sellerbppservice.repos")
@EntityScan("com.aloy.sellerbppservice.model")
public class SellerBppServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SellerBppServiceApplication.class, args);
	}

}
