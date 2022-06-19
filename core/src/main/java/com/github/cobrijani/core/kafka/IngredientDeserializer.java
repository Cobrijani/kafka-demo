package com.github.cobrijani.core.kafka;

import com.github.cobrijani.core.IngredientMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class IngredientDeserializer implements Deserializer<IngredientMessage> {
    @Override
    public IngredientMessage deserialize(String s, byte[] data) {
        try {
            return IngredientMessage.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            log.error("Received un-parse message exception and skip.");
            return null;
        }
    }
}
