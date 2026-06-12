package com.cuervo.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class RevisionProductoRequestDTO {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Double stockActual;
}