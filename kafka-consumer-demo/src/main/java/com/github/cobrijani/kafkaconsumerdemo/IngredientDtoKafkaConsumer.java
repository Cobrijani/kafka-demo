package com.github.cobrijani.kafkaconsumerdemo;

import com.github.cobrijani.core.Event;
import com.github.cobrijani.core.IngredientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Log
@Component
@RequiredArgsConstructor
public class IngredientDtoKafkaConsumer {

    private final IngredientJpaRepository repository;

    @Bean
    public RecordMessageConverter messageConverter() {
        return new StringJsonMessageConverter();
    }

    @KafkaListener(
            topics = "ingredients",
            groupId = "kafka-demo-consumer")
    public void listenGroupFoo(@Payload Event<IngredientDto> message,
            @Header(KafkaHeaders.OFFSET) int offset) {
        switch (message.getAction()) {
            case CHANGED -> this.repository.findByReferenceId(message.getData().getReferenceId())
                    .ifPresent(x -> {
                        x.setName(message.getData().getName());
                        repository.save(x);
                    });
            case ADDED -> this.repository.save(Ingredient.builder()
                    .referenceId(message.getData().getReferenceId())
                    .name(message.getData().getName())
                    .build());
            case DELETED -> this.repository.findByReferenceId(message.getData().getReferenceId())
                    .ifPresent(repository::delete);
        }
        log.info("Received Message in group kafka-demo: " + message + "; offset:" + offset);
    }
}
