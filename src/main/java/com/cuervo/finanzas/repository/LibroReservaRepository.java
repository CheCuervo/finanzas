package com.cuervo.finanzas.repository;

import com.cuervo.finanzas.entity.LibroReserva;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.entity.enums.TipoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface LibroReservaRepository extends JpaRepository<LibroReserva, Long> {

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LibroReserva l WHERE l.reserva.id = :reservaId AND l.tipoMovimiento = :tipoMovimiento")
    BigDecimal sumValorByReservaAndTipo(@Param("reservaId") Long reservaId, @Param("tipoMovimiento") String tipoMovimiento);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LibroReserva l WHERE l.reserva.user = :user AND l.tipoMovimiento = :tipoMovimiento")
    BigDecimal sumTotalValorByTipoMovimientoAndUser(@Param("user") User user, @Param("tipoMovimiento") String tipoMovimiento);

    boolean existsByReservaId(Long reservaId);

    /**
     * Suma el valor de todos los movimientos (Reserva o Pago) para un usuario,
     * filtrando por el tipo de reserva maestra (GASTO_FIJO o AHORRO).
     */
    @Query("SELECT COALESCE(SUM(lr.valor), 0) FROM LibroReserva lr WHERE lr.reserva.user = :user AND lr.reserva.tipo = :tipoReserva AND lr.tipoMovimiento = :tipoMovimiento")
    BigDecimal sumTotalByTipoReservaAndTipoMovimientoAndUser(@Param("user") User user, @Param("tipoReserva") TipoReserva tipoReserva, @Param("tipoMovimiento") String tipoMovimiento);
}
