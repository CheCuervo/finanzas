package com.cuervo.inventario.dto;

import com.cuervo.inventario.entity.enums.UnidadMedida;
import lombok.Data;

@Data
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private Double stockIdeal;
    private Double stockActual;
    
    // 👇 NUEVO CAMPO AGREGADO
    private Double stockMinimoSugerido;

    private UnidadMedida unidadMedida;
    private Integer ordenUbicacion;
    private Integer ordenSupermercado;
    private Boolean activo;
    private Boolean obligatorio;
    
    private Long categoriaId;
    private String categoriaNombre;
    private Long supermercadoId;
    private String supermercadoNombre;
}