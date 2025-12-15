package com.huertohogar.huertohogarbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data  // Lombok para getters/setters
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private Double price;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;  // Solo ID, no objeto

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotBlank(message = "La imagen es obligatoria")
    private String image;

    private String description;
    private String origin;
    private String sustainability;
    private String recipe;
    private String recommendations;
}