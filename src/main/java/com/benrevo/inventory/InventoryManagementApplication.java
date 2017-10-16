package com.benrevo.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryManagementApplication {

	static DataAccess dataAccess;

	public static void main(String[] args) {
		dataAccess = new DataAccess();
		SpringApplication.run(InventoryManagementApplication.class, args);
	}
}
