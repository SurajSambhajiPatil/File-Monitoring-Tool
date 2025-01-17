package com.metron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.metron")
public class FileMonitoringToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileMonitoringToolApplication.class, args);
	}

}
