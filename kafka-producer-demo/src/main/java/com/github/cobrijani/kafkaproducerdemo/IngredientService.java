package com.github.cobrijani.kafkaproducerdemo;

import com.github.cobrijani.core.Event;
import com.github.cobrijani.core.EventAction;
import com.github.cobrijani.core.IngredientDto;
import lombok.extern.java.Log;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Log
@Service
public record IngredientService(IngredientJpaRepository ingredientJpaRepository,
                                KafkaTemplate<String, Event<IngredientDto>> kafkaTemplate) {

    public List<Ingredient> getAll() {
        return this.ingredientJpaRepository.findAll();
    }

    public Ingredient save(Ingredient ingredient) {
        boolean isNew = ingredient.getId() == null;
        var saved = ingredientJpaRepository.save(ingredient);

        ListenableFuture<SendResult<String, Event<IngredientDto>>> future =
                kafkaTemplate.send("ingredients", new Event<>(new IngredientDto(
                        saved.getId().toString(),
                        saved.getName()
                ), isNew ? EventAction.ADDED : EventAction.CHANGED));

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, Event<IngredientDto>> result) {
                log.info("Sent message=[" + ingredient +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.info("Unable to send message=["
                        + ingredient + "] due to : " + ex.getMessage());
            }
        });
        return saved;
    }
}
