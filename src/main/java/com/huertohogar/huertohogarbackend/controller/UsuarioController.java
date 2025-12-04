package com.huertohogar.huertohogarbackend.controller;

import com.huertohogar.huertohogarbackend.dto.LoginRequest;
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

@RestController
@RequestMapping("/api/v1/usuarios") // <<< CAMBIO: URL base consistente con SecurityConfig
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
    @PostMapping("/login") // Endpoint: /api/v1/usuarios/login
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

            String token = usuarioService.login(email, contrasena);
            Usuario user = (Usuario) usuarioService.loadUserByUsername(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuario", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        }
    }

    // -------------------------------------------------------------
// 2. REGISTRO (ACCESO PÚBLICO)
// -------------------------------------------------------------
// Endpoint: /api/v1/usuarios/register (POST)
    @PostMapping("/register") // <<< CAMBIO: endpoint específico para registro
    @Operation(summary = "Registrar Nuevo Usuario",
            description = "Permite el registro público de nuevos usuarios con rol 'USER'.")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        // Opcional: Asignar rol por default si no se proporciona (para usuarios públicos)
        if (usuario.getRole() == null) {
            usuario.setRole("USER");
        }
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    // -------------------------------------------------------------
    // 3. GESTIÓN Y CONSULTA (RESTRINGIDO)
    // -------------------------------------------------------------

    // Endpoint: /api/v1/usuarios (GET) - Listar todos
    @GetMapping
    @Operation(summary = "Obtener Todos los Usuarios",
            description = "Devuelve una lista de todos los usuarios registrados. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // Endpoint: /api/v1/usuarios/{id} (GET) - Obtener por ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Usuario por ID",
            description = "Devuelve el perfil de un usuario específico. **Acceso: ADMIN o Propietario**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
    }

    // Endpoint: /api/v1/usuarios/{id} (PUT) - Actualizar por ID
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Usuario por ID",
            description = "Actualiza los datos del usuario. **Acceso: ADMIN o Propietario**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetalles) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDetalles);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint: /api/v1/usuarios/{id} (DELETE) - Eliminar por ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar Usuario por ID",
            description = "Elimina permanentemente un usuario. **Acceso: ADMIN**.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

// Formulario Registro
//{
//  "nombre": "ronald",
//  "apellido": "fuentes",
//  "fechaNacimiento": "1990-01-01",
//  "email": "ronald@gmail.com",
//  "contrasena": "ronald123",
//  "telefono": "string",
//  "comuna": "string",
//  "role": "USER"
//}