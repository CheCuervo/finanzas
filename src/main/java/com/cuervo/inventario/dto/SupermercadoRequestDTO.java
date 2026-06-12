package com.cuervo.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupermercadoRequestDTO {

    @NotBlank(message = "El nombre del supermercado es obligatorio")
    private String nombre;

    // La ubicación es opcional, por eso no lleva validación @NotBlank
    private String ubicacion; 
}