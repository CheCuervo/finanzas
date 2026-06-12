package com.cuervo.inventario.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CategoriaAgrupadaDTO {
    private String categoriaNombre;
    private List<ItemResumenDTO> items;
}