package com.github.cobrijani.kafkaproducerdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientRestController {

    @Autowired
    private IngredientService service;

    @GetMapping
    public List<Ingredient> getIngredients() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<Void> createIngredient(@RequestBody Ingredient ingredient) {
        service.save(ingredient);
        return ResponseEntity.ok().build();
    }
}
