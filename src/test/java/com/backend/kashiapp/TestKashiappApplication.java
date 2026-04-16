package com.backend.kashiapp;

import org.springframework.boot.SpringApplication;

public class TestKashiappApplication {

	public static void main(String[] args) {
		SpringApplication.from(KashiappApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
