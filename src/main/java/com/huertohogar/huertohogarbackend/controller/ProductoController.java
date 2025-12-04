package com.huertohogar.huertohogarbackend.controller;

import com.huertohogar.huertohogarbackend.model.Producto;
import com.huertohogar.huertohogarbackend.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// --- Importaciones de Swagger/OpenAPI ---
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos") // URL base consistente
// <<< TAG A NIVEL DE CLASE >>>
@Tag(name = "Productos", description = "Gestión del catálogo de productos (CRUD)")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // 1. POST - Crear Producto
    @PostMapping
    @Operation(summary = "Crear Nuevo Producto",
            description = "Agrega un nuevo producto al catálogo. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth")) // Protegido
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        // La lógica de negocio está en el servicio
        Producto nuevoProducto = productoService.crearProducto(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED); // 201 Created
    }

    // 2. GET - Obtener todos los productos
    @GetMapping
    @Operation(summary = "Obtener Todos los Productos",
            description = "Devuelve una lista completa de productos. **Acceso: Público**.")
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos); // 200 OK
    }

    // 3. GET - Obtener producto por ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Producto por ID",
            description = "Devuelve los detalles de un producto específico. **Acceso: Público**.")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        // Uso idiomático de orElseThrow para devolver 404 si no se encuentra
        return productoService.obtenerProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + id));
    }

    // 4. PUT - Actualizar producto por ID
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Producto por ID",
            description = "Actualiza los detalles de un producto existente. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth")) // Protegido
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoDetalles) {
        try {
            // El servicio lanza RuntimeException si no encuentra el ID, capturada aquí para devolver 404
            Producto productoActualizado = productoService.actualizarProducto(id, productoDetalles);
            return ResponseEntity.ok(productoActualizado);
        } catch (Exception e) {
            // Devuelve 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 5. DELETE - Eliminar producto por ID
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Producto por ID",
            description = "Elimina un producto del catálogo permanentemente. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth")) // Protegido
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        try {
            // El servicio verifica existencia y lanza excepción si es necesario
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            // Devuelve 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}