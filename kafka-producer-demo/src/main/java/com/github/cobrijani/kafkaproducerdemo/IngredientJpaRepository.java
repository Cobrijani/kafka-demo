package com.github.cobrijani.kafkaproducerdemo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngredientJpaRepository extends JpaRepository<Ingredient, UUID> {
}
