package com.huertohogar.huertohogarbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @NotNull(message = "El precio es obligatorio")
    private Double price;

    // Relación ManyToOne: Muchas Productos pueden tener una Categoría.
    // Composición: Producto tiene una Categoría obligatoria.
    @ManyToOne(optional = false) // opcional = false forzando la relación en DB
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoría es obligatoria")
    private Categoria category;

    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    private Integer stock;

    @NotBlank(message = "La imagen es obligatoria")
    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String origin;

    private String sustainability;

    @Column(columnDefinition = "TEXT")
    private String recipe;

    @Column(columnDefinition = "TEXT")
    private String recommendations;
}