package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" Starting Spring Boot Application with Java Pipe ");
        System.out.println("==================================================");
        SpringApplication.run(Application.class, args);
    }
}

