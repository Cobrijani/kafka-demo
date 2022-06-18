package com.github.cobrijani.kafkaconsumerdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientRestController {

    @Autowired
    private IngredientJpaRepository repository;

    @GetMapping
    public List<Ingredient> getIngredients() {
        return repository.findAll();
    }
}
