package com.study.shortlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShortLinkApplication  {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApplication.class, args);
        Character c = 'a';
    }
}
