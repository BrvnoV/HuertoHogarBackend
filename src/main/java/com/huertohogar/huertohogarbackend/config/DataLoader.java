// Archivo: config/DataLoader.java (Corregido)

package com.huertohogar.huertohogarbackend.config;

import com.huertohogar.huertohogarbackend.model.Usuario;
import com.huertohogar.huertohogarbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Necesaria

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    // Se inyectan las dependencias de seguridad
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Verificar si el usuario ADMIN ya existe
            if (usuarioRepository.findByEmail("admin@huertohogar.com").isEmpty()) {

                Usuario admin = new Usuario();

                // Estos métodos ahora funcionan gracias a @Data
                admin.setNombre("Admin");
                admin.setApellido("Root");
                admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
                admin.setEmail("admin@huertohogar.com");

                // Resuelve el error de 'PasswordEncoder' y 'encode(String)'
                admin.setContrasena(passwordEncoder.encode("admin123"));

                // Resuelve el error de símbolo 'Role'
                admin.setRole("ADMIN");

                usuarioRepository.save(admin);
                System.out.println(">>> Usuario ADMIN inicial creado.");
            }
        };
    }
}