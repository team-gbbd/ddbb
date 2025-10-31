package com.ddbb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ddbb.entity.management")
@EnableJpaRepositories(basePackages = "com.ddbb.repository.management")
public class DdbbApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdbbApplication.class, args);
	}

}
