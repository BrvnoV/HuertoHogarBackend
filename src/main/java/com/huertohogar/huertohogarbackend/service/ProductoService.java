package com.huertohogar.huertohogarbackend.service;

import com.huertohogar.huertohogarbackend.model.Producto;
import com.huertohogar.huertohogarbackend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Inyecta el repositorio vía constructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    // --- MÉTODOS CRUD ---

    /**
     * Crea un nuevo producto.
     */
    public Producto crearProducto(Producto producto) {
        // Lógica de negocio antes de guardar, si fuera necesaria (ej. validaciones de stock inicial)
        return productoRepository.save(producto);
    }

    /**
     * Obtiene la lista de todos los productos.
     */
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    /**
     * Obtiene un producto por su ID.
     */
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Actualiza los detalles de un producto existente.
     * Lanza una RuntimeException si el producto no se encuentra.
     */
    public Producto actualizarProducto(Long id, Producto productoDetalles) {
        return productoRepository.findById(id).map(productoExistente -> {

            // 1. Actualizar todos los campos requeridos y opcionales
            productoExistente.setName(productoDetalles.getName());
            productoExistente.setPrice(productoDetalles.getPrice());
            productoExistente.setCategory(productoDetalles.getCategory());
            productoExistente.setStock(productoDetalles.getStock());
            productoExistente.setImage(productoDetalles.getImage());
            productoExistente.setDescription(productoDetalles.getDescription());
            productoExistente.setOrigin(productoDetalles.getOrigin());
            productoExistente.setSustainability(productoDetalles.getSustainability());
            productoExistente.setRecipe(productoDetalles.getRecipe());
            productoExistente.setRecommendations(productoDetalles.getRecommendations());

            return productoRepository.save(productoExistente);

        }).orElseThrow(() -> new RuntimeException("Producto no encontrado para actualización con ID: " + id));
        // La RuntimeException será capturada por el ProductoController para devolver 404
    }

    /**
     * Elimina un producto por su ID.
     * Lanza una RuntimeException si el producto no se encuentra.
     */
    public void eliminarProducto(Long id) {
        // Verificar si existe antes de eliminar para devolver 404 si no existe
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado para eliminación con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
}