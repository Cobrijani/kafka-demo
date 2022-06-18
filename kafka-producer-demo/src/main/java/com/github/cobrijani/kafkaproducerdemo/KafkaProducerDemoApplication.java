package com.github.cobrijani.kafkaproducerdemo;

import lombok.extern.java.Log;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.Stream;

@Log
@SpringBootApplication
public class KafkaProducerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerDemoApplication.class, args);
    }

//    @Bean
//    public ApplicationRunner createIngredients(IngredientService service) {
//        return args -> Stream.of(
//                        Ingredient.builder().name("Milk").build(),
//                        Ingredient.builder().name("Flour").build(),
//                        Ingredient.builder().name("Egg").build(),
//                        Ingredient.builder().name("Butter").build(),
//                        Ingredient.builder().name("Oil").build(),
//                        Ingredient.builder().name("Water").build(),
//                        Ingredient.builder().name("Cauliflower").build(),
//                        Ingredient.builder().name("Lemon").build(),
//                        Ingredient.builder().name("Orange").build(),
//                        Ingredient.builder().name("Mushrooms").build()
//                )
//                .map(service::save)
//                .forEach(x -> log.info("Inserted: " + x));
//    }

}
