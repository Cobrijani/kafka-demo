package com.github.cobrijani.kafkaconsumerdemo;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log
@SpringBootApplication
public class KafkaConsumerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerDemoApplication.class, args);
    }

}
