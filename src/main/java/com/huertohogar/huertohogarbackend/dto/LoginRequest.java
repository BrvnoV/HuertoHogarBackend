// Archivo: dto/LoginRequest.java (CORREGIDO)
package com.huertohogar.huertohogarbackend.dto;

import lombok.Data; // <<< Asegura que esta anotación esté presente
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    // Los nombres de los campos DEBEN coincidir con los getters (getEmail, getContrasena)
    private String email;
    private String contrasena;
}