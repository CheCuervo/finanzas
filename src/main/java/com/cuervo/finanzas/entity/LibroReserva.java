package com.cuervo.finanzas.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tfin_libro_reservas")
@Data
public class LibroReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nm_id")
    private Long id;

    @CreationTimestamp
    @Column(name = "fe_fecha", updatable = false)
    private LocalDateTime fecha;

    @Column(name = "cd_tipo_movimiento", nullable = false)
    private String tipoMovimiento; // "Reserva" o "Pago"

    @Column(name = "nm_valor", nullable = false)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", referencedColumnName = "nm_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    // Se permite que la cuenta sea nula (nullable = true)
    @JoinColumn(name = "id_cuenta", referencedColumnName = "nm_id", nullable = true)
    private Cuenta cuenta;

    @Column(name = "ds_concepto")
    private String concepto;
}
