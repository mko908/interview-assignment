package com.benrevo.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class InventoryManagementApplication {


	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementApplication.class, args);
	}
}
