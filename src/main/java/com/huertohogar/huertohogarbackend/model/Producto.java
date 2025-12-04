package com.huertohogar.huertohogarbackend.model; // Ajusta el paquete

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    // --- ID (Clave Primaria) ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Cambiado de string a Long para usar autoincremental estándar de MySQL

    // --- Propiedades del Producto ---
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price; // Usamos Double o BigDecimal para precios

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer stock;

    private String image; // URL o ruta de la imagen

    @Column(columnDefinition = "TEXT") // TEXT permite mayor longitud para descripciones
    private String description;

    private String origin;

    private String sustainability;

    @Column(columnDefinition = "TEXT")
    private String recipe;

    @Column(columnDefinition = "TEXT")
    private String recommendations; // Almacenado como String (e.g., JSON o delimitado por comas)

    // Si realmente necesitas que 'recommendations' sea una lista en Java,
    // tendrías que usar un conversor JPA (@Convert) o una tabla separada.
    // Para simplificar, lo manejaremos como un String largo (TEXT).
}