package com.cuervo.inventario.dto;

import lombok.Data;

@Data
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}