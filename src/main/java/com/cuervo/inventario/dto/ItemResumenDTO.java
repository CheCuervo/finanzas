package com.cuervo.inventario.dto;
import com.cuervo.inventario.entity.enums.UnidadMedida;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemResumenDTO {
    private Long itemId;
    private String productoNombre;
    private Double cantidadSugerida;
    private UnidadMedida unidadMedida;
    private Boolean compradoEnSuper;
}