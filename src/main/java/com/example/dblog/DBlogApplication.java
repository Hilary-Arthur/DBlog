package com.example.dblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DBlogApplication.class, args);
    }

}
