package com.github.cobrijani.kafkaconsumerdemo;

import com.github.cobrijani.core.IngredientMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Log
@Component
@RequiredArgsConstructor
public class IngredientDtoKafkaConsumer {

    public static final String TOPIC_NAME = "ingredients";
    private final IngredientJpaRepository repository;

    @KafkaListener(
            topics = TOPIC_NAME,
            groupId = "kafka-demo-consumer")
    public void ingredientDtoConsume(IngredientMessage message,
            @Header(KafkaHeaders.OFFSET) int offset) {
        switch (message.getAction()) {
            case CHANGED -> this.repository.findByReferenceId(message.getReferenceId())
                    .ifPresent(x -> {
                        x.setName(message.getName());
                        repository.save(x);
                    });
            case ADDED -> this.repository.save(Ingredient.builder()
                    .referenceId(message.getReferenceId())
                    .name(message.getName())
                    .build());
            case DELETED -> this.repository.findByReferenceId(message.getReferenceId())
                    .ifPresent(repository::delete);
        }
        log.info("Received Message in group kafka-demo: " + message + "; offset:" + offset);
    }
}
