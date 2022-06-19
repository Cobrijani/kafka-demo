package com.github.cobrijani.kafkaproducerdemo;

import com.github.cobrijani.core.EventAction;
import com.github.cobrijani.core.IngredientMessage;
import com.github.cobrijani.core.Version;
import lombok.extern.java.Log;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log
@Service
public record IngredientService(IngredientJpaRepository ingredientJpaRepository,
                                KafkaTemplate<String, IngredientMessage> kafkaTemplate) {

    public static final String TOPIC_NAME = "ingredients";

    /**
     * Retrieve list of all ingredients
     *
     * @return collection of ingredients
     */
    public List<Ingredient> getAll() {
        return this.ingredientJpaRepository.findAll();
    }

    /**
     * Save the ingredient data
     *
     * @param ingredient ingredient data
     */
    public void save(Ingredient ingredient) {
        boolean isNew = ingredient.getId() == null;
        var saved = ingredientJpaRepository.save(ingredient);
        sendMessage(saved, isNew ? EventAction.ADDED : EventAction.CHANGED);
    }

    /**
     * Delete the ingredient with given id
     *
     * @param id identifier
     */
    public void deleteIngredient(UUID id) {
        this.ingredientJpaRepository.findById(id)
                .ifPresent(ingredient -> {
                    this.ingredientJpaRepository.delete(ingredient);
                    sendMessage(ingredient, EventAction.DELETED);
                });
    }

    private void sendMessage(Ingredient ingredient, EventAction action) {
        final IngredientMessage ingredientMessage = IngredientMessage.newBuilder()
                .setAction(action)
                .setName(ingredient.getName())
                .setReferenceId(ingredient.getId().toString())
                .setVersion(Version.V1)
                .build();

        final Message<IngredientMessage> msg = MessageBuilder.withPayload(ingredientMessage)
                .setHeader(KafkaHeaders.TOPIC, TOPIC_NAME)
                .build();

        final ListenableFuture<SendResult<String, IngredientMessage>> future = kafkaTemplate.send(msg);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(@Nullable SendResult<String, IngredientMessage> result) {
                log.info("Sent message=[" + ingredient +
                        "] with offset=[" + Optional.ofNullable(result).map(SendResult::getRecordMetadata).map(RecordMetadata::offset)
                        .orElse(-1L) + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.info("Unable to send message=["
                        + ingredient + "] due to : " + ex.getMessage());
            }
        });
    }

}
