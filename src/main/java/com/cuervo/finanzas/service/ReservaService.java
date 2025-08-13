package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.*;
import com.cuervo.finanzas.entity.*;
import com.cuervo.finanzas.entity.enums.TipoCuenta;
import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import com.cuervo.finanzas.entity.enums.TipoReserva;
import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.finanzas.repository.CuentaRepository;
import com.cuervo.finanzas.repository.LibroGeneralRepository;
import com.cuervo.finanzas.repository.LibroReservaRepository;
import com.cuervo.finanzas.repository.ReservaRepository;
import com.cuervo.finanzas.service.auth.AuthHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LibroReservaRepository libroReservaRepository;
    private final CuentaRepository cuentaRepository;
    private final LibroGeneralRepository libroGeneralRepository;
    private final AuthHelper authHelper;

    // --- CREATE ---
    public Reserva crearReserva(ReservaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setConcepto(request.getConcepto());
        nuevaReserva.setValorMeta(request.getValorMeta());
        nuevaReserva.setTipo(request.getTipo());
        nuevaReserva.setValorReservaSemanal(request.getValorReservaSemanal());
        nuevaReserva.setUser(user);
        return reservaRepository.save(nuevaReserva);
    }

    // --- READ ---
    public List<Reserva> consultarReservas() {
        User user = authHelper.getAuthenticatedUser();
        return reservaRepository.findAllByUser(user);
    }

    public Reserva consultarReservaPorId(Long id) {
        User user = authHelper.getAuthenticatedUser();
        return reservaRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NegocioException("La Reserva con id " + id + " no existe o no pertenece al usuario."));
    }

    // --- UPDATE ---
    public Reserva editarReserva(Long id, ReservaRequestDTO request) {
        Reserva reserva = consultarReservaPorId(id);
        reserva.setConcepto(request.getConcepto());
        reserva.setValorMeta(request.getValorMeta());
        reserva.setTipo(request.getTipo());
        reserva.setValorReservaSemanal(request.getValorReservaSemanal());
        return reservaRepository.save(reserva);
    }

    // --- DELETE ---
    public void eliminarReserva(Long id) {
        consultarReservaPorId(id); // Valida pertenencia
        if (libroReservaRepository.existsByReservaId(id)) {
            throw new NegocioException("No se puede eliminar la reserva con id " + id + " porque tiene movimientos asociados.");
        }
        reservaRepository.deleteById(id);
    }

    // --- Lógica de Movimientos Individuales ---
    @Transactional
    public void ingresarMovimientoReserva(MovimientoReservaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Reserva reserva = consultarReservaPorId(request.getIdReserva());
        String tipoMovimientoReserva = request.getTipoMovimiento();

        if ("Reserva".equalsIgnoreCase(tipoMovimientoReserva)) {
            Cuenta cuenta = null;
            if (request.getIdCuenta() != null) {
                cuenta = cuentaRepository.findByIdAndUser(request.getIdCuenta(), user)
                        .orElseThrow(() -> new NegocioException("La Cuenta con id " + request.getIdCuenta() + " no existe o no pertenece al usuario."));
            }
            registrarMovimientoLibroReserva("Reserva", request.getValor(), reserva, cuenta, request.getConcepto());
        } else if ("Pago".equalsIgnoreCase(tipoMovimientoReserva)) {
            if (request.getIdCuenta() == null) {
                throw new NegocioException("Para un movimiento de 'Pago', el campo 'idCuenta' es obligatorio.");
            }
            Cuenta cuenta = cuentaRepository.findByIdAndUser(request.getIdCuenta(), user)
                    .orElseThrow(() -> new NegocioException("La Cuenta con id " + request.getIdCuenta() + " no existe o no pertenece al usuario."));
            procesarPago(request.getValor(), reserva, cuenta, request.getConcepto());
        } else {
            throw new NegocioException("El tipo de movimiento de la reserva debe ser 'Reserva' o 'Pago'.");
        }
    }

    private void procesarPago(BigDecimal valorPago, Reserva reserva, Cuenta cuenta, String concepto) {
        BigDecimal totalReservado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Reserva");
        BigDecimal totalPagado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Pago");
        BigDecimal saldoReservado = totalReservado.subtract(totalPagado);

        if (valorPago.compareTo(saldoReservado) > 0) {
            throw new NegocioException("El valor pagado (" + valorPago + ") supera el saldo reservado disponible (" + saldoReservado + ") para esta Reserva.");
        }
        registrarMovimientoLibroReserva("Pago", valorPago, reserva, cuenta, concepto);
        registrarMovimientoPagoEnLibroGeneral(cuenta, valorPago, "Pago Reserva: " + concepto);
    }

    // --- Lógica de Reservas Masivas ---
    @Transactional
    public void realizarReservasMasivas(ReservaMasivaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Cuenta cuenta = null;
        if (request.getIdCuenta() != null) {
             cuenta = cuentaRepository.findByIdAndUser(request.getIdCuenta(), user)
                .orElseThrow(() -> new NegocioException("La Cuenta de origen con id " + request.getIdCuenta() + " no existe o no pertenece al usuario."));
        }

        List<Reserva> todasLasReservas = reservaRepository.findAllByUser(user);
        List<Reserva> reservasFiltradas;
        String tipoFiltro = request.getTipoReserva().toUpperCase();

        if ("ALL".equals(tipoFiltro)) {
            reservasFiltradas = todasLasReservas;
        } else {
            try {
                TipoReserva tipo = TipoReserva.valueOf(tipoFiltro);
                reservasFiltradas = todasLasReservas.stream()
                        .filter(r -> r.getTipo() == tipo)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new NegocioException("El tipo de reserva '" + request.getTipoReserva() + "' no es válido. Use AHORRO, GASTO_FIJO o ALL.");
            }
        }

        for (Reserva reserva : reservasFiltradas) {
            BigDecimal valorSemanal = reserva.getValorReservaSemanal();
            if (valorSemanal != null && valorSemanal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalAReservar = valorSemanal.multiply(new BigDecimal(request.getNmSemanas()));
                String conceptoMovimiento = request.getConcepto() + " - " + reserva.getConcepto();
                registrarMovimientoLibroReserva("Reserva", totalAReservar, reserva, cuenta, conceptoMovimiento);
            }
        }
    }

    // --- Lógica de Resumen de Reservas ---
    public ResumenReservasDTO getResumenReservas(String tipoFiltroStr) {
        User user = authHelper.getAuthenticatedUser();

        // --- Lógica de Totales Corregida ---
        BigDecimal totalAhorradoGeneral = libroReservaRepository.sumTotalValorByTipoMovimientoAndUser(user, "Reserva");
        BigDecimal totalPagadoGeneral = libroReservaRepository.sumTotalValorByTipoMovimientoAndUser(user, "Pago");
        BigDecimal totalReservadoGeneral = totalAhorradoGeneral.subtract(totalPagadoGeneral);

        BigDecimal totalReservasAhorro = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.AHORRO, "Reserva");
        BigDecimal totalPagosAhorro = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.AHORRO, "Pago");
        BigDecimal totalReservadoAhorros = totalReservasAhorro.subtract(totalPagosAhorro);

        BigDecimal totalReservasGastoFijo = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.GASTO_FIJO, "Reserva");
        BigDecimal totalPagosGastoFijo = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.GASTO_FIJO, "Pago");
        BigDecimal totalReservadoGastoFijos = totalReservasGastoFijo.subtract(totalPagosGastoFijo);

        // --- Lógica de Filtrado y Detalle (Sin Cambios) ---
        List<Reserva> reservasMaestras = reservaRepository.findAllByUser(user);
        List<Reserva> reservasFiltradas;
        if ("ALL".equalsIgnoreCase(tipoFiltroStr)) {
            reservasFiltradas = reservasMaestras;
        } else {
            try {
                TipoReserva tipoFiltro = TipoReserva.valueOf(tipoFiltroStr.toUpperCase());
                reservasFiltradas = reservasMaestras.stream()
                        .filter(r -> r.getTipo() == tipoFiltro)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new NegocioException("Tipo de filtro no válido. Use AHORRO, GASTO_FIJO o ALL.");
            }
        }

        List<ReservaDetalleDTO> detalles = new ArrayList<>();
        for (Reserva reserva : reservasFiltradas) {
            BigDecimal valorAhorrado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Reserva");
            BigDecimal valorGastado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Pago");
            BigDecimal valorReservado = valorAhorrado.subtract(valorGastado);
            BigDecimal valorFaltante = reserva.getValorMeta().subtract(valorAhorrado);

            detalles.add(ReservaDetalleDTO.builder()
                    .id(reserva.getId())
                    .concepto(reserva.getConcepto())
                    .tipo(reserva.getTipo())
                    .valorMeta(reserva.getValorMeta())
                    .valorReservaSemanal(reserva.getValorReservaSemanal())
                    .valorAhorrado(valorAhorrado)
                    .valorGastado(valorGastado)
                    .valorReservado(valorReservado)
                    .valorFaltante(valorFaltante)
                    .build());
        }

        // --- Construcción del DTO de Respuesta Final ---
        return ResumenReservasDTO.builder()
                .totalReservado(totalReservadoGeneral)
                .totalReservadoAhorros(totalReservadoAhorros)
                .totalReservadoGastoFijos(totalReservadoGastoFijos)
                .reservas(detalles)
                .build();
    }
    
    // --- Métodos de Registro Internos ---
    private void registrarMovimientoLibroReserva(String tipo, BigDecimal valor, Reserva reserva, Cuenta cuenta, String concepto) {
        LibroReserva movimiento = new LibroReserva();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setValor(valor);
        movimiento.setReserva(reserva);
        movimiento.setCuenta(cuenta);
        movimiento.setConcepto(concepto);
        libroReservaRepository.save(movimiento);
    }
    
    private void registrarMovimientoPagoEnLibroGeneral(Cuenta cuenta, BigDecimal valor, String concepto) {
        LibroGeneral movimientoGeneral = new LibroGeneral();
        if (cuenta.getTipo() == TipoCuenta.CREDITO) {
            movimientoGeneral.setTipoMovimiento(TipoMovimiento.INGRESO);
        } else {
            movimientoGeneral.setTipoMovimiento(TipoMovimiento.EGRESO);
        }
        movimientoGeneral.setCuenta(cuenta);
        movimientoGeneral.setValor(valor);
        movimientoGeneral.setConcepto(concepto);
        libroGeneralRepository.save(movimientoGeneral);
    }
}
