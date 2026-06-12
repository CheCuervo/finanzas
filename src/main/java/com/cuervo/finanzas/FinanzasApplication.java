package com.cuervo.finanzas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.cuervo")
@EntityScan(basePackages = "com.cuervo")
@EnableJpaRepositories(basePackages = "com.cuervo")
public class FinanzasApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanzasApplication.class, args);
	}

}
