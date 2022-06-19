package com.github.cobrijani.core.kafka;

import com.github.cobrijani.core.IngredientMessage;
import org.apache.kafka.common.serialization.Serializer;

public class IngredientSerializer implements Serializer<IngredientMessage> {
    @Override
    public byte[] serialize(String s, IngredientMessage ingredient) {
        return ingredient.toByteArray();
    }
}
