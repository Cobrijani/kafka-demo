package com.github.cobrijani.kafkaconsumerdemo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientJpaRepository extends JpaRepository<Ingredient, UUID> {

    Optional<Ingredient> findByReferenceId(String referenceId);
}
