package com.huertohogar.huertohogarbackend.service;

import com.huertohogar.huertohogarbackend.model.Usuario;
import com.huertohogar.huertohogarbackend.repository.UsuarioRepository;
import com.huertohogar.huertohogarbackend.security.JwtService; // Necesitas este servicio para el login
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy; // Recomendado para PasswordEncoder
import org.springframework.security.core.userdetails.UserDetails; // Importación clave
import org.springframework.security.core.userdetails.UserDetailsService; // Importación clave
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Importación clave
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importación clave para gestión de transacciones

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService { // <<< IMPLEMENTA UserDetailsService

    private final UsuarioRepository usuarioRepository;

    @Lazy // Previene posibles dependencias circulares con SecurityConfig
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService; // <<< INYECCIÓN: Necesario para generar el token en el login

    // ----------------------------------------------------------------------
    // 1. SPRING SECURITY: Carga de Usuario por Nombre de Usuario (Email)
    // ----------------------------------------------------------------------

    /**
     * Método obligatorio de la interfaz UserDetailsService.
     * Utilizado por Spring Security para cargar los detalles del usuario durante la autenticación.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email) // Asumiendo que tu repositorio tiene findByEmail
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    // ----------------------------------------------------------------------
    // 2. LÓGICA DE NEGOCIO Y CRUD
    // ----------------------------------------------------------------------

    /**
     * Crea un nuevo usuario. Encripta la contraseña antes de guardar.
     */
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        // 1. Asegurar un rol por defecto si no se especifica
        if (usuario.getRole() == null || usuario.getRole().isEmpty()) {
            usuario.setRole("USER");
        }
        // 2. Encriptar la contraseña
        String contrasenaHash = passwordEncoder.encode(usuario.getContrasena());
        usuario.setContrasena(contrasenaHash);

        // 3. Guardar la entidad
        return usuarioRepository.save(usuario);
    }

    /**
     * Lógica de login: verifica credenciales y genera JWT.
     * NOTA: Este método se llamaría desde tu AuthController/AuthService.
     */
    public String login(String email, String contrasena) {
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 1. Verificar que la contraseña ingresada coincida con la encriptada
        if (!passwordEncoder.matches(contrasena, user.getContrasena())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // 2. Generar y devolver el Token JWT
        return jwtService.generateToken(user);
    }

    /**
     * Obtiene la lista de todos los usuarios.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene un usuario por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Actualiza los detalles de un usuario existente.
     */
    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario usuarioDetalles) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {

            // 1. Actualizar campos de texto
            usuarioExistente.setNombre(usuarioDetalles.getNombre());
            usuarioExistente.setApellido(usuarioDetalles.getApellido());
            usuarioExistente.setFechaNacimiento(usuarioDetalles.getFechaNacimiento());
            usuarioExistente.setEmail(usuarioDetalles.getEmail());
            usuarioExistente.setRole(usuarioDetalles.getRole()); // Actualizar rol (solo para ADMINs)

            // 2. Actualizar contraseña SOLO si se proporciona una nueva
            if (usuarioDetalles.getContrasena() != null && !usuarioDetalles.getContrasena().isEmpty()) {
                String nuevaContrasenaHash = passwordEncoder.encode(usuarioDetalles.getContrasena());
                usuarioExistente.setContrasena(nuevaContrasenaHash);
            }

            // 3. Actualizar campos opcionales
            usuarioExistente.setTelefono(usuarioDetalles.getTelefono());
            usuarioExistente.setComuna(usuarioDetalles.getComuna());

            return usuarioRepository.save(usuarioExistente);

        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualización con ID: " + id));
    }

    /**
     * Elimina un usuario por su ID.
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        // Verificar si existe antes de eliminar para que el controlador devuelva 404
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado para eliminación con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }


}