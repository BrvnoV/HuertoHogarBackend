package com.huertohogar.huertohogarbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegistroRequest {

    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Apellido es obligatorio")
    private String apellido;

    @NotNull(message = "Fecha de nacimiento es obligatoria")
    @Past(message = "Fecha de nacimiento debe ser en el pasado")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  // Asegura formato YYYY-MM-DD
    private LocalDate fechaNacimiento;

    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Contraseña es obligatoria")
    private String contraseña;  // Coincide con frontend; mapea a 'contrasena' en service

    // Campos opcionales
    private String telefono;
    private String comuna;
}