package com.cuervo.inventario.dto;

import lombok.Data;

@Data
public class SupermercadoResponseDTO {
    private Long id;
    private String nombre;
    private String ubicacion;
    private Boolean activo;
}