package com.cuervo.finanzas.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "tfin_config_presupuesto")
@Data
public class ConfigPresupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nm_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "ingreso_semanal", precision = 19, scale = 2)
    private BigDecimal ingresoSemanal = BigDecimal.ZERO;

    @Column(name = "gastos")
    private Integer gastos = 60;

    @Column(name = "ahorros")
    private Integer ahorros = 20;

    @Column(name = "inversiones")
    private Integer inversiones = 10;

    @Column(name = "libre")
    private Integer libre = 10;

    @PrePersist
    @PreUpdate
    private void validatePercentages() {
        if (gastos + ahorros + inversiones + libre != 100) {
            throw new IllegalStateException("La suma de los porcentajes (gastos, ahorros, inversiones, libre) debe ser 100.");
        }
    }
}
