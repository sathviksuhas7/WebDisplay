package com.epdcl.webdisplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebdisplayApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebdisplayApplication.class, args);
	}
}