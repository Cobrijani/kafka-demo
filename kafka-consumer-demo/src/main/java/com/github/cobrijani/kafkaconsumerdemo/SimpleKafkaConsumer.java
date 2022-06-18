package com.github.cobrijani.kafkaconsumerdemo;

import lombok.extern.java.Log;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log
@Component
public class SimpleKafkaConsumer {

    public static final String TOPIC_NAME = "simple-topic";
    private final List<String> messages = new ArrayList<>();

    @KafkaListener(
            topics = TOPIC_NAME,
            groupId = "kafka-demo-consumer")
    public void listenGroupFoo(String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) int offset) {
        log.info("Received Message in group kafka-demo-consume: " + message + "; offset:" + offset);
        this.messages.add(message);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
