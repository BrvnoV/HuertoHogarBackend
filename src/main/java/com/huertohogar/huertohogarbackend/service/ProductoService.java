package com.huertohogar.huertohogarbackend.service;

import com.huertohogar.huertohogarbackend.dto.ProductoRequest;  // NUEVO: Importa el DTO
import com.huertohogar.huertohogarbackend.model.Categoria;
import com.huertohogar.huertohogarbackend.model.Producto;
import com.huertohogar.huertohogarbackend.repository.CategoriaRepository;
import com.huertohogar.huertohogarbackend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // Opcional: Para logging
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j  // Opcional: Activa logging con @Slf4j (añade dependency si no tienes)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * NUEVO: Método para crear producto desde DTO (evita deserialización parcial de entidades).
     * Recomendado para el controller POST.
     */
    public Producto crearProductoFromRequest(ProductoRequest request) {
        // Validar nombre único
        if (productoRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe un producto con el nombre: " + request.getName());
        }

        // Validar y cargar categoría desde ID
        Categoria categoria = categoriaRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("La categoría especificada no existe: " + request.getCategoryId()));

        // Mapear DTO a nueva entidad Producto
        Producto producto = new Producto();
        producto.setName(request.getName());
        producto.setPrice(request.getPrice());
        producto.setCategory(categoria);  // Entidad completa resuelta
        producto.setStock(request.getStock());
        producto.setImage(request.getImage());
        producto.setDescription(request.getDescription());
        producto.setOrigin(request.getOrigin());
        producto.setSustainability(request.getSustainability());
        producto.setRecipe(request.getRecipe());
        producto.setRecommendations(request.getRecommendations());

        // Opcional: Log para depuración
        log.info("Creando producto: {} con categoría ID: {}", request.getName(), request.getCategoryId());

        return productoRepository.save(producto);
    }

    /**
     * MÉTODO ANTIGUO: Mantén para compatibilidad si lo usas en otros lugares,
     * pero migra a FromRequest para nuevos endpoints.
     */
    public Producto crearProducto(Producto producto) {
        // Validar nombre único
        if (productoRepository.existsByName(producto.getName())) {
            throw new IllegalArgumentException("Ya existe un producto con el nombre: " + producto.getName());
        }

        // Validar y vincular categoría existente
        if (producto.getCategory() != null && producto.getCategory().getId() != null) {
            Categoria categoria = categoriaRepository.findById(producto.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría especificada no existe"));
            producto.setCategory(categoria);
        } else {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        return productoRepository.save(producto);
    }

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * NUEVO: Método para actualizar desde DTO (evita deserialización parcial).
     * Recomendado para el controller PUT.
     */
    public Producto actualizarProductoFromRequest(Long id, ProductoRequest request) {
        return productoRepository.findById(id).map(productoExistente -> {
            // Validar nombre único si ha cambiado
            if (!productoExistente.getName().equals(request.getName()) &&
                    productoRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Ya existe un producto con el nombre: " + request.getName());
            }

            // Validar y cargar categoría si se proporciona un nuevo ID
            if (request.getCategoryId() != null) {
                Categoria categoria = categoriaRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("La categoría especificada no existe: " + request.getCategoryId()));
                productoExistente.setCategory(categoria);
            }

            // Mapear campos del DTO
            productoExistente.setName(request.getName());
            productoExistente.setPrice(request.getPrice());
            productoExistente.setStock(request.getStock());
            productoExistente.setImage(request.getImage());
            productoExistente.setDescription(request.getDescription());
            productoExistente.setOrigin(request.getOrigin());
            productoExistente.setSustainability(request.getSustainability());
            productoExistente.setRecipe(request.getRecipe());
            productoExistente.setRecommendations(request.getRecommendations());

            // Opcional: Log
            log.info("Actualizando producto ID: {} con nuevo nombre: {}", id, request.getName());

            return productoRepository.save(productoExistente);
        }).orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    /**
     * MÉTODO ANTIGUO: Mantén para compatibilidad.
     */
    public Producto actualizarProducto(Long id, Producto productoDetalles) {
        return productoRepository.findById(id).map(productoExistente -> {

            // Validar nombre único si ha cambiado
            if (!productoExistente.getName().equals(productoDetalles.getName()) &&
                    productoRepository.existsByName(productoDetalles.getName())) {
                throw new IllegalArgumentException(
                        "Ya existe un producto con el nombre: " + productoDetalles.getName());
            }

            productoExistente.setName(productoDetalles.getName());
            productoExistente.setPrice(productoDetalles.getPrice());

            // Validar categoría si cambia
            if (productoDetalles.getCategory() != null && productoDetalles.getCategory().getId() != null) {
                Categoria categoria = categoriaRepository.findById(productoDetalles.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("La categoría especificada no existe"));
                productoExistente.setCategory(categoria);
            }

            productoExistente.setStock(productoDetalles.getStock());
            productoExistente.setImage(productoDetalles.getImage());
            productoExistente.setDescription(productoDetalles.getDescription());
            productoExistente.setOrigin(productoDetalles.getOrigin());
            productoExistente.setSustainability(productoDetalles.getSustainability());
            productoExistente.setRecipe(productoDetalles.getRecipe());
            productoExistente.setRecommendations(productoDetalles.getRecommendations());

            return productoRepository.save(productoExistente);

        }).orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
}