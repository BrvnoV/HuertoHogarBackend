package com.huertohogar.huertohogarbackend.service;

import com.huertohogar.huertohogarbackend.dto.RegistroRequest;
import com.huertohogar.huertohogarbackend.model.Usuario;
import com.huertohogar.huertohogarbackend.repository.UsuarioRepository;
import com.huertohogar.huertohogarbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Lazy
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // NOTA: Esta implementación causaba el error 403 y el error de compilación posterior (getId())
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    @Transactional
    public Usuario crearUsuarioFromRequest(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email ya registrado: " + request.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        usuario.setComuna(request.getComuna());
        usuario.setRole("USUARIO");

        String contrasenaHash = passwordEncoder.encode(request.getContraseña());
        usuario.setContrasena(contrasenaHash);

        log.info("Registrando usuario: {} con email: {}", request.getNombre(), request.getEmail());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email ya registrado: " + usuario.getEmail());
        }

        if (usuario.getRole() == null || usuario.getRole().isEmpty()) {
            usuario.setRole("USUARIO");
        }

        String contrasenaHash = passwordEncoder.encode(usuario.getContrasena());
        usuario.setContrasena(contrasenaHash);

        return usuarioRepository.save(usuario);
    }

    public String login(String email, String contrasena) {
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // NOTA: Esta es la lógica manual de matching que fallaba con 401
        if (!passwordEncoder.matches(contrasena, user.getContrasena())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return jwtService.generateToken(user);
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        // Asume que tienes un método findByEmail en tu UsuarioRepository
        return usuarioRepository.findByEmail(email);
    }

    // Método que hacía falta en la versión original, pero que no estaba
    // public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
    //     return usuarioRepository.findByEmail(email);
    // }

    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario usuarioDetalles) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            if (!usuarioExistente.getEmail().equals(usuarioDetalles.getEmail()) &&
                    usuarioRepository.existsByEmail(usuarioDetalles.getEmail())) {
                throw new IllegalArgumentException("Email ya registrado: " + usuarioDetalles.getEmail());
            }

            usuarioExistente.setNombre(usuarioDetalles.getNombre());
            usuarioExistente.setApellido(usuarioDetalles.getApellido());
            usuarioExistente.setFechaNacimiento(usuarioDetalles.getFechaNacimiento());
            usuarioExistente.setEmail(usuarioDetalles.getEmail());
            usuarioExistente.setRole(usuarioDetalles.getRole());

            if (usuarioDetalles.getContrasena() != null && !usuarioDetalles.getContrasena().isEmpty()) {
                String nuevaContrasenaHash = passwordEncoder.encode(usuarioDetalles.getContrasena());
                usuarioExistente.setContrasena(nuevaContrasenaHash);
            }

            usuarioExistente.setTelefono(usuarioDetalles.getTelefono());
            usuarioExistente.setComuna(usuarioDetalles.getComuna());

            return usuarioRepository.save(usuarioExistente);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualización con ID: " + id));
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado para eliminación con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}