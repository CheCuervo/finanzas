package com.cuervo.finanzas.entity;

import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tfin_libro_general")
@Data
public class LibroGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nm_id")
    private Long id;

    @CreationTimestamp
    @Column(name = "fe_fecha", updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "cd_tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento; // INGRESO o EGRESO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta", referencedColumnName = "nm_id", nullable = false)
    private Cuenta cuenta;

    @Column(name = "ds_concepto", nullable = false)
    private String concepto;

    @Column(name = "nm_valor", nullable = false)
    private BigDecimal valor;
}
