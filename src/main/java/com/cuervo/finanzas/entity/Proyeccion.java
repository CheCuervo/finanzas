package com.cuervo.finanzas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tfin_proyecciones")
@Data
public class Proyeccion {
    // ... campos existentes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nm_id")
    private Long id;

    @Column(name = "ds_concepto", nullable = false)
    private String concepto;

    @Column(name = "nm_valor", nullable = false)
    private BigDecimal valor;

    @CreationTimestamp
    @Column(name = "fe_fecha", updatable = false)
    private LocalDateTime fecha;

    // --- Nueva Relación ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}