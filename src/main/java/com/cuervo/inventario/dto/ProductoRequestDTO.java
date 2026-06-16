package com.cuervo.inventario.dto;

import com.cuervo.inventario.entity.enums.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "El stock ideal es obligatorio")
    @Positive(message = "El stock ideal debe ser mayor a 0")
    private Double stockIdeal;

    // 👇 NUEVO CAMPO AGREGADO
    @NotNull(message = "El stock mínimo sugerido es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Double stockMinimoSugerido;

    @NotNull(message = "La unidad de medida es obligatoria")
    private UnidadMedida unidadMedida; 

    private Integer ordenUbicacion;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;

    @NotNull(message = "El ID del supermercado es obligatorio")
    private Long supermercadoId;

    @NotNull(message = "Debe especificar si el producto es de revisión obligatoria u opcional")
    private Boolean obligatorio;
}