package com.github.cobrijani.core;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "referenceId")
@ToString
public class IngredientDto implements Serializable {

    private String referenceId;
    private String name;
}
