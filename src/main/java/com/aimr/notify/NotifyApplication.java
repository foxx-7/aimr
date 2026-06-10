package com.aimr.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotifyApplication {

	public static void main(String[] args) {
        Runtime runtime=Runtime.getRuntime();
        System.out.println("this runtime has "+runtime.availableProcessors()+" available processors");
		SpringApplication.run(NotifyApplication.class, args);
	}

}
