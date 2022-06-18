package com.github.cobrijani.kafkaproducerdemo;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "INGREDIENT")
public class Ingredient implements Serializable {

    @Id
    @GeneratedValue
    @Type(
            type = "uuid-char"
    )
    private UUID id;

    private String name;

}
