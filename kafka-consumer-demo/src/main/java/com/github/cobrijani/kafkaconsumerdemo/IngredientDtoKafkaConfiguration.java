package com.github.cobrijani.kafkaconsumerdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class IngredientDtoKafkaConfiguration {

//    @Bean
//    public RecordMessageConverter messageConverter() {
//        return new StringJsonMessageConverter();
//    }

}
