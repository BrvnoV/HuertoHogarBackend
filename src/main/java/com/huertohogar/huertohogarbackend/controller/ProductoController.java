package com.huertohogar.huertohogarbackend.controller;

import com.huertohogar.huertohogarbackend.model.Producto;
import com.huertohogar.huertohogarbackend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.huertohogar.huertohogarbackend.dto.ProductoRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Gestión del catálogo de productos")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // En la clase:
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Crear Nuevo Producto", description = "Acceso: ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody ProductoRequest request) {  // Cambia a DTO
        try {
            Producto nuevoProducto = productoService.crearProductoFromRequest(request);  // Nuevo método en service
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {  // Catch para SQL/otros errores
            // Opcional: log.error("Error interno: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al guardar producto");
        }
    }

    @GetMapping
    @Operation(summary = "Obtener Todos los Productos", description = "Acceso: Público")
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener Producto por ID", description = "Acceso: Público")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Actualizar Producto", description = "Acceso: ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {  // Cambia a DTO
        try {
            Producto actualizado = productoService.actualizarProductoFromRequest(id, request);  // Nuevo método
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al actualizar");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Eliminar Producto", description = "Acceso: ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}