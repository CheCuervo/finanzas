package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.CuentaBalanceDTO;
import com.cuervo.finanzas.dto.MovimientoGeneralRequestDTO;
import com.cuervo.finanzas.dto.ResumenFinancieroDTO;
import com.cuervo.finanzas.entity.Cuenta;
import com.cuervo.finanzas.entity.LibroGeneral;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.entity.enums.TipoCuenta;
import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.finanzas.repository.CuentaRepository;
import com.cuervo.finanzas.repository.LibroGeneralRepository;
import com.cuervo.finanzas.repository.LibroReservaRepository;
import com.cuervo.finanzas.repository.ProyeccionRepository;
import com.cuervo.finanzas.service.auth.AuthHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanzasService {

    private final LibroGeneralRepository libroGeneralRepository;
    private final CuentaRepository cuentaRepository;
    private final LibroReservaRepository libroReservaRepository;
    private final ProyeccionRepository proyeccionRepository;
    private final AuthHelper authHelper;

    public ResumenFinancieroDTO getResumenFinanciero() {
        User user = authHelper.getAuthenticatedUser();
        ResumenFinancieroDTO resumen = new ResumenFinancieroDTO();

        // --- 1. Calcular Balances y Totales por Usuario ---
        List<Cuenta> todasLasCuentas = cuentaRepository.findAllByUser(user);
        List<CuentaBalanceDTO> cuentasConBalance = new ArrayList<>();
        for (Cuenta cuenta : todasLasCuentas) {
            BigDecimal ingresosCuenta = libroGeneralRepository.sumValorByCuentaIdAndTipoMovimiento(cuenta.getId(), TipoMovimiento.INGRESO);
            BigDecimal egresosCuenta = libroGeneralRepository.sumValorByCuentaIdAndTipoMovimiento(cuenta.getId(), TipoMovimiento.EGRESO);
            BigDecimal balanceCuenta = ingresosCuenta.subtract(egresosCuenta);
            cuentasConBalance.add(new CuentaBalanceDTO(cuenta.getId(), cuenta.getDescripcion(), cuenta.getTipo(), balanceCuenta));
        }
        resumen.setCuentas(cuentasConBalance);

        BigDecimal ingresosAhorro = libroGeneralRepository.sumValorByTipoCuentaAndTipoMovimientoAndUser(user, TipoCuenta.AHORRO, TipoMovimiento.INGRESO);
        BigDecimal egresosAhorro = libroGeneralRepository.sumValorByTipoCuentaAndTipoMovimientoAndUser(user, TipoCuenta.AHORRO, TipoMovimiento.EGRESO);
        BigDecimal ingresosInversion = libroGeneralRepository.sumValorByTipoCuentaAndTipoMovimientoAndUser(user, TipoCuenta.INVERSION, TipoMovimiento.INGRESO);
        BigDecimal egresosInversion = libroGeneralRepository.sumValorByTipoCuentaAndTipoMovimientoAndUser(user, TipoCuenta.INVERSION, TipoMovimiento.EGRESO);
        BigDecimal ingresosCredito = libroGeneralRepository.sumValorByTipoCuentaAndTipoMovimientoAndUser(user, TipoCuenta.CREDITO, TipoMovimiento.INGRESO);
        BigDecimal egresosCredito = libroGeneralRepository.sumValorByTipoCuentaAndTipoMovimientoAndUser(user, TipoCuenta.CREDITO, TipoMovimiento.EGRESO);
        BigDecimal totalReservas = libroReservaRepository.sumTotalValorByTipoMovimientoAndUser(user, "Reserva");
        BigDecimal totalRetiros = libroReservaRepository.sumTotalValorByTipoMovimientoAndUser(user, "Retiro");

        // --- 2. Calcular Nuevos Campos ---
        BigDecimal dineroTotal = (ingresosAhorro.add(ingresosInversion)).subtract(egresosAhorro.add(egresosInversion));
        BigDecimal balanceNetoReservas = totalReservas.subtract(totalRetiros);
        BigDecimal balanceCredito = ingresosCredito.subtract(egresosCredito);
        BigDecimal dineroDisponible = dineroTotal.subtract(balanceCredito).subtract(balanceNetoReservas);
        BigDecimal dineroReservado = balanceNetoReservas.add(balanceCredito);
        BigDecimal totalProyecciones = proyeccionRepository.sumTotalProyeccionesByUser(user);
        BigDecimal dineroTotalConProyecciones = dineroTotal.add(totalProyecciones);
        BigDecimal dineroDisponibleConProyecciones = dineroDisponible.add(totalProyecciones);

        // --- 3. Asignar todos los valores al DTO ---
        resumen.setDineroTotal(dineroTotal);
        resumen.setDineroDisponible(dineroDisponible);
        resumen.setDineroReservado(dineroReservado);
        resumen.setTotalProyecciones(totalProyecciones);
        resumen.setDineroTotalConProyecciones(dineroTotalConProyecciones);
        resumen.setDineroDisponibleConProyecciones(dineroDisponibleConProyecciones);

        return resumen;
    }

    @Transactional
    public void registrarMovimientoGeneral(MovimientoGeneralRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Cuenta cuenta = cuentaRepository.findByIdAndUser(request.getIdCuenta(), user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + request.getIdCuenta() + " no existe o no pertenece al usuario."));
        TipoMovimiento tipo;
        try {
            tipo = TipoMovimiento.valueOf(request.getTipoMovimiento().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NegocioException("El tipo de movimiento debe ser 'INGRESO' o 'EGRESO'.");
        }
        LibroGeneral movimiento = new LibroGeneral();
        movimiento.setCuenta(cuenta);
        movimiento.setValor(request.getValor());
        movimiento.setConcepto(request.getConcepto());
        movimiento.setTipoMovimiento(tipo);
        libroGeneralRepository.save(movimiento);
    }

    @Transactional
    public void eliminarMovimiento(Long movimientoId) {
        User user = authHelper.getAuthenticatedUser();
        LibroGeneral movimiento = libroGeneralRepository.findById(movimientoId)
                .orElseThrow(() -> new NegocioException("El movimiento con id " + movimientoId + " no existe."));

        // Verificación de seguridad: Asegura que el movimiento pertenezca al usuario logueado.
        if (!movimiento.getCuenta().getUser().getId().equals(user.getId())) {
            throw new NegocioException("No tiene permiso para eliminar este movimiento.");
        }

        libroGeneralRepository.deleteById(movimientoId);
    }
}
