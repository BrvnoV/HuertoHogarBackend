package com.huertohogar.huertohogarbackend.config;

import com.huertohogar.huertohogarbackend.model.Usuario;
import com.huertohogar.huertohogarbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // BUSCAMOS SI EXISTE O CREAMOS UNO NUEVO
            Usuario admin = usuarioRepository.findByEmail("admin@huertohogar.com")
                    .orElse(new Usuario()); // Si no existe, creamos una nueva instancia

            // SI ES NUEVO O SI ES ANTIGUO, ASEGURAMOS LOS VALORES
            admin.setNombre("Admin");
            admin.setApellido("Root");
            admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            admin.setEmail("admin@huertohogar.com");

            // CRÍTICO: RE-HASHEAMOS LA CONTRASEÑA CADA VEZ QUE SE EJECUTA ESTE CÓDIGO
            admin.setContrasena(passwordEncoder.encode("admin123"));

            admin.setRole("ADMIN");

            usuarioRepository.save(admin);
            System.out.println(">>> Usuario ADMIN inicial creado/hash actualizado.");
        };
    }
}