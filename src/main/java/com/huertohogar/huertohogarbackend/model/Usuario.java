package com.huertohogar.huertohogarbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Data // <<< CORRECCIÓN: Genera los SETTERS (soluciona los errores 'setNombre', 'setApellido', etc.)
@NoArgsConstructor
@AllArgsConstructor
// IMPLEMENTA UserDetails (Soluciona el error de conversión en SecurityConfig)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos del modelo
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contrasena;

    private String telefono;
    private String comuna;

    // Asumimos que es String ya que no tienes el enum Role
    @Column(nullable = false)
    private String role; // "ADMIN" o "USER"


    // ----------------------------------------------------
    // IMPLEMENTACIÓN DE USERDETAILS (REQUERIDO POR SPRING SECURITY)
    // ----------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapea el campo 'role' (String) a la autoridad que Spring Security espera
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return email;
    }

    // Las siguientes siempre se dejan como 'true' si no se implementa lógica de bloqueo/expiración
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}