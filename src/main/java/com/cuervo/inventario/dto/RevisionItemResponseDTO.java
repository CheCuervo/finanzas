package com.cuervo.inventario.dto;

import lombok.Data;

@Data
public class RevisionItemResponseDTO {
    private Long id; // ID del RevisionItem para procesarlo
    private boolean revisado;
    private Integer existenciaActual;
    
    // Datos aplanados del producto para React
    private Long productoId;
    private String productoNombre;
    private Double stockIdeal;
    private Double stockActual;

    // 👇 NUEVO CAMPO AGREGADO
    private Double stockMinimoSugerido;

    private String unidadMedida;
    private String categoriaNombre;
    private String supermercadoNombre;
    private Boolean obligatorio; 
}