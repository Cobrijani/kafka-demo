package com.github.cobrijani.kafkaproducerdemo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientRestController {

    private final IngredientService service;

    @GetMapping
    public List<Ingredient> getIngredients() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<Void> createIngredient(@RequestBody Ingredient ingredient) {
        service.save(ingredient);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateIngredient(@PathVariable UUID id, @RequestBody Ingredient ingredient) {
        if (!id.equals(ingredient.getId())) {
            return ResponseEntity.badRequest().build();
        }
        service.save(ingredient);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable UUID id) {
        service.deleteIngredient(id);
        return ResponseEntity.ok().build();
    }

}
