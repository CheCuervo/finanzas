package com.cuervo.finanzas.repository;

import com.cuervo.finanzas.entity.LibroGeneral;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.entity.enums.TipoCuenta;
import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LibroGeneralRepository extends JpaRepository<LibroGeneral, Long> {
    @Query("SELECT COALESCE(SUM(lg.valor), 0) FROM LibroGeneral lg WHERE lg.cuenta.user = :user AND lg.cuenta.tipo = :tipoCuenta AND lg.tipoMovimiento = :tipoMovimiento")
    BigDecimal sumValorByTipoCuentaAndTipoMovimientoAndUser(@Param("user") User user, @Param("tipoCuenta") TipoCuenta tipoCuenta, @Param("tipoMovimiento") TipoMovimiento tipoMovimiento);

    @Query("SELECT COALESCE(SUM(lg.valor), 0) FROM LibroGeneral lg WHERE lg.cuenta.id = :cuentaId AND lg.tipoMovimiento = :tipoMovimiento")
    BigDecimal sumValorByCuentaIdAndTipoMovimiento(@Param("cuentaId") Long cuentaId, @Param("tipoMovimiento") TipoMovimiento tipoMovimiento);

    boolean existsByCuentaId(Long cuentaId);

    /**
     * Busca todos los movimientos de una cuenta específica, filtrando por mes y año,
     * y devuelve los resultados de forma paginada.
     */
    /**
     * Busca todos los movimientos de una cuenta específica, filtrando por mes y año,
     * y devuelve los resultados de forma paginada.
     */
    @Query("SELECT lg FROM LibroGeneral lg WHERE lg.cuenta.id = :cuentaId " +
            "AND EXTRACT(YEAR FROM lg.fecha) = :anio " +
            "AND EXTRACT(MONTH FROM lg.fecha) = :mes " +
            "ORDER BY lg.fecha DESC")
    Page<LibroGeneral> findByCuentaIdAndFecha(@Param("cuentaId") Long cuentaId,
                                              @Param("anio") int anio,
                                              @Param("mes") int mes,
                                              Pageable pageable);
}