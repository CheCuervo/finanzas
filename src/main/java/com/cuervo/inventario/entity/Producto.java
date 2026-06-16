package com.cuervo.inventario.entity;

import com.cuervo.inventario.entity.enums.UnidadMedida;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tmer_producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, name = "stock_ideal")
    private Double stockIdeal; 

    @Column(nullable = false, name = "stock_actual")
    private Double stockActual = 0.0; 

    @Column(nullable = false, name = "stock_minimo_sugerido", columnDefinition = "float default 0.0")
    private Double stockMinimoSugerido = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "unidad_medida", length = 10)
    private UnidadMedida unidadMedida; 

    @Column(nullable = false, name = "orden_ubicacion")
    private Integer ordenUbicacion;

    // 🔥 NUEVA COLUMNA
    @Column(name = "orden_supermercado")
    private Integer ordenSupermercado;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean obligatorio = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supermercado_id", nullable = false)
    private Supermercado supermercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}