package com.huertohogar.huertohogarbackend.controller;

import com.huertohogar.huertohogarbackend.dto.LoginRequest;
import com.huertohogar.huertohogarbackend.dto.RegistroRequest;
import com.huertohogar.huertohogarbackend.model.Usuario;
import com.huertohogar.huertohogarbackend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.validation.Valid;
// import org.springframework.security.core.userdetails.UserDetails; // Ya no es necesario importar solo UserDetails

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios y autenticación")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // -------------------------------------------------------------
    // 1. LOGIN (ACCESO PÚBLICO)
    // -------------------------------------------------------------
    @PostMapping("/login")
    @Operation(summary = "Iniciar Sesión",
            description = "Verifica credenciales, devuelve el Token JWT y el objeto Usuario.",
            security = @SecurityRequirement(name = ""))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operación exitosa: Token y usuario devueltos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @Tag(name = "Autenticación")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail();
            String contrasena = loginRequest.getContrasena();

            // 1. Autenticación (se lanza excepción si falla)
            String token = usuarioService.login(email, contrasena);

            // 2. Obtener el objeto Usuario completo para devolver al frontend
            // CRÍTICO: Ya no usamos getId() de UserDetails. Usamos el email para obtener el objeto Usuario.
            Usuario user = usuarioService.obtenerUsuarioPorEmail(email)
                    .orElseThrow(() -> new RuntimeException("Error interno: Usuario autenticado no encontrado en DB"));

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuario", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // El servicio lanza RuntimeException("Credenciales inválidas") que se mapea a 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        }
    }

    // ... (El resto de métodos están correctos y no necesitan cambios)

    // -------------------------------------------------------------
    // 2. REGISTRO (ACCESO PÚBLICO)
    // -------------------------------------------------------------
    @PostMapping("/register")
    @Operation(summary = "Registrar Nuevo Usuario",
            description = "Permite el registro público de nuevos usuarios con rol 'USUARIO'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody RegistroRequest request) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuarioFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al registrar usuario");
        }
    }

    // -------------------------------------------------------------
    // 3. GESTIÓN Y CONSULTA (RESTRINGIDO)
    // -------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Obtener Todos los Usuarios",
            description = "Devuelve una lista de todos los usuarios registrados. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or authentication.name == #id")
    @Operation(summary = "Obtener Usuario por ID",
            description = "Devuelve el perfil de un usuario específico. **Acceso: ADMIN o Propietario**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or authentication.name == #id")
    @Operation(summary = "Actualizar Usuario por ID",
            description = "Actualiza los datos del usuario. **Acceso: ADMIN o Propietario**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetalles) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDetalles);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Eliminar Usuario por ID",
            description = "Elimina permanentemente un usuario. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + e.getMessage());
        }
    }
}