package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.*;
import com.cuervo.finanzas.entity.*;
import com.cuervo.finanzas.entity.enums.TipoCuenta;
import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import com.cuervo.finanzas.entity.enums.TipoReserva;
import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.finanzas.repository.*;
import com.cuervo.finanzas.service.auth.AuthHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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
    private final ConfigPresupuestoRepository configPresupuestoRepository;

    // --- CREATE ---
    @Transactional
    public Reserva crearReserva(ReservaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();

        // --- Nueva Validación ---
        ConfigPresupuesto config = configPresupuestoRepository.findByUser(user)
                .orElseThrow(() -> new NegocioException("No se ha configurado un presupuesto. Por favor, configure su ingreso semanal."));

        BigDecimal ingresoSemanal = config.getIngresoSemanal();
        if (ingresoSemanal == null || ingresoSemanal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El ingreso semanal debe ser mayor a cero para crear reservas.");
        }

        BigDecimal totalReservasSemanalesActual = reservaRepository.sumTotalValorReservaSemanalByUser(user);
        BigDecimal nuevoTotal = totalReservasSemanalesActual.add(request.getValorReservaSemanal());

        if (nuevoTotal.compareTo(ingresoSemanal) > 0) {
            throw new NegocioException("La suma de las cuotas semanales (" + nuevoTotal + ") no puede superar tu ingreso semanal (" + ingresoSemanal + ").");
        }

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setConcepto(request.getConcepto());
        nuevaReserva.setValorMeta(request.getValorMeta());
        nuevaReserva.setTipo(request.getTipo());
        nuevaReserva.setValorReservaSemanal(request.getValorReservaSemanal());

        if (request.getTipo() == TipoReserva.GASTO_FIJO_MES) {
            LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            nuevaReserva.setFechaMeta(lastDayOfMonth);
        } else {
            nuevaReserva.setFechaMeta(request.getFechaMeta());
        }

        nuevaReserva.setUser(user);
        return reservaRepository.save(nuevaReserva);
    }

    // --- UPDATE ---
    @Transactional
    public Reserva editarReserva(Long id, ReservaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Reserva reserva = consultarReservaPorId(id);

        // --- Nueva Validación ---
        ConfigPresupuesto config = configPresupuestoRepository.findByUser(user)
                .orElseThrow(() -> new NegocioException("No se ha configurado un presupuesto."));

        BigDecimal ingresoSemanal = config.getIngresoSemanal();
        if (ingresoSemanal == null || ingresoSemanal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El ingreso semanal debe ser mayor a cero para editar reservas.");
        }

        BigDecimal totalReservasSemanalesActual = reservaRepository.sumTotalValorReservaSemanalByUser(user);
        BigDecimal nuevoTotal = totalReservasSemanalesActual
                .subtract(reserva.getValorReservaSemanal()) // Resta el valor antiguo
                .add(request.getValorReservaSemanal());   // Suma el valor nuevo

        if (nuevoTotal.compareTo(ingresoSemanal) > 0) {
            throw new NegocioException("La suma de las cuotas semanales (" + nuevoTotal + ") no puede superar tu ingreso semanal (" + ingresoSemanal + ").");
        }

        reserva.setConcepto(request.getConcepto());
        reserva.setValorMeta(request.getValorMeta());
        reserva.setTipo(request.getTipo());
        reserva.setValorReservaSemanal(request.getValorReservaSemanal());

        if (request.getTipo() == TipoReserva.GASTO_FIJO_MES) {
            LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            reserva.setFechaMeta(lastDayOfMonth);
        } else {
            reserva.setFechaMeta(request.getFechaMeta());
        }

        return reservaRepository.save(reserva);
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
        } else if ("Retiro".equalsIgnoreCase(tipoMovimientoReserva)) {
            if (request.getIdCuenta() == null) {
                throw new NegocioException("Para un movimiento de 'Retiro', el campo 'idCuenta' es obligatorio.");
            }
            Cuenta cuenta = cuentaRepository.findByIdAndUser(request.getIdCuenta(), user)
                    .orElseThrow(() -> new NegocioException("La Cuenta con id " + request.getIdCuenta() + " no existe o no pertenece al usuario."));
            procesarRetiro(request.getValor(), reserva, cuenta, request.getConcepto());
        } else {
            throw new NegocioException("El tipo de movimiento de la reserva debe ser 'Reserva' o 'Retiro'.");
        }
    }

    private void procesarRetiro(BigDecimal valorRetiro, Reserva reserva, Cuenta cuenta, String concepto) {
        BigDecimal totalReservado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Reserva");
        BigDecimal totalPagado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Retiro");
        BigDecimal saldoReservado = totalReservado.subtract(totalPagado);

        if (valorRetiro.compareTo(saldoReservado) > 0) {
            throw new NegocioException("El valor a retirar supera el saldo reservado disponible.");
        }
        registrarMovimientoLibroReserva("Retiro", valorRetiro, reserva, cuenta, concepto);
        registrarMovimientoPagoEnLibroGeneral(cuenta, valorRetiro, "Retiro Reserva: " + concepto);
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
        BigDecimal CUATRO = new BigDecimal("4");

        // --- Cálculos de Reservas ---
        BigDecimal totalAhorradoGeneral = libroReservaRepository.sumTotalValorByTipoMovimientoAndUser(user, "Reserva");
        BigDecimal totalPagadoGeneral = libroReservaRepository.sumTotalValorByTipoMovimientoAndUser(user, "Retiro");
        BigDecimal totalReservadoGeneral = totalAhorradoGeneral.subtract(totalPagadoGeneral);

        BigDecimal totalReservasAhorro = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.AHORRO, "Reserva");
        BigDecimal totalPagosAhorro = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.AHORRO, "Retiro");
        BigDecimal totalReservadoAhorros = totalReservasAhorro.subtract(totalPagosAhorro);

        BigDecimal totalReservasGastoFijo = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.GASTO_FIJO, "Reserva");
        BigDecimal totalPagosGastoFijo = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.GASTO_FIJO, "Retiro");
        BigDecimal totalReservadoGastoFijos = totalReservasGastoFijo.subtract(totalPagosGastoFijo);

        BigDecimal totalReservasInversion = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.INVERSION, "Reserva");
        BigDecimal totalPagosInversion = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.INVERSION, "Retiro");
        BigDecimal totalReservadoInversiones = totalReservasInversion.subtract(totalPagosInversion);

        BigDecimal totalReservasGFMes = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.GASTO_FIJO_MES, "Reserva");
        BigDecimal totalPagosGFMes = libroReservaRepository.sumTotalByTipoReservaAndTipoMovimientoAndUser(user, TipoReserva.GASTO_FIJO_MES, "Retiro");
        BigDecimal totalReservadoGFMes = totalReservasGFMes.subtract(totalPagosGFMes);

        // --- Cálculos de Presupuesto ---
        BigDecimal pptoSemanalAhorros = reservaRepository.sumValorReservaSemanalByUserAndTipo(user, TipoReserva.AHORRO);
        BigDecimal pptoSemanalGastosFijos = reservaRepository.sumValorReservaSemanalByUserAndTipo(user, TipoReserva.GASTO_FIJO);
        BigDecimal pptoSemanalInversiones = reservaRepository.sumValorReservaSemanalByUserAndTipo(user, TipoReserva.INVERSION);
        BigDecimal pptoSemanalGFMes = reservaRepository.sumValorReservaSemanalByUserAndTipo(user, TipoReserva.GASTO_FIJO_MES);
        BigDecimal pptoSemanalTotal = reservaRepository.sumTotalValorReservaSemanalByUser(user);

        BigDecimal pptoMensualAhorros = pptoSemanalAhorros.multiply(CUATRO);
        BigDecimal pptoMensualGastosFijos = pptoSemanalGastosFijos.multiply(CUATRO);
        BigDecimal pptoMensualTotal = pptoSemanalTotal.multiply(CUATRO);

        // --- Lógica de Filtrado y Detalle ---
        List<Reserva> reservasMaestras = reservaRepository.findAllByUser(user);
        List<ReservaDetalleDTO> detalles = new ArrayList<>();
        for (Reserva reserva : reservasMaestras) {
            BigDecimal valorAhorrado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Reserva");
            BigDecimal valorGastado = libroReservaRepository.sumValorByReservaAndTipo(reserva.getId(), "Retiro");
            BigDecimal valorReservado = valorAhorrado.subtract(valorGastado);
            BigDecimal valorFaltante = reserva.getValorMeta().subtract(valorAhorrado);

            LocalDate fechaMetaReal = null;
            if (reserva.getValorReservaSemanal() != null && reserva.getValorReservaSemanal().compareTo(BigDecimal.ZERO) > 0) {
                if (valorFaltante.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal semanasParaMeta = valorFaltante.divide(reserva.getValorReservaSemanal(), 0, RoundingMode.CEILING);
                    fechaMetaReal = LocalDate.now().plusWeeks(semanasParaMeta.longValue());
                } else {
                    fechaMetaReal = LocalDate.now(); // Si ya se cumplió la meta
                }
            }

            BigDecimal cuotaSugerida = null;
            if (reserva.getFechaMeta() != null && reserva.getFechaMeta().isAfter(LocalDate.now())) {
                long semanasRestantes = ChronoUnit.WEEKS.between(LocalDate.now(), reserva.getFechaMeta());
                if (semanasRestantes > 0 && valorFaltante.compareTo(BigDecimal.ZERO) > 0) {
                    cuotaSugerida = valorFaltante.divide(new BigDecimal(semanasRestantes), 2, RoundingMode.HALF_UP);
                }
            }

            detalles.add(ReservaDetalleDTO.builder()
                    .id(reserva.getId())
                    .concepto(reserva.getConcepto())
                    .tipo(reserva.getTipo())
                    .valorMeta(reserva.getValorMeta())
                    .valorReservaSemanal(reserva.getValorReservaSemanal())
                    .fechaMeta(reserva.getFechaMeta())
                    .valorAhorrado(valorAhorrado)
                    .valorGastado(valorGastado)
                    .valorReservado(valorReservado)
                    .valorFaltante(valorFaltante)
                            .cuotaSugerida(cuotaSugerida)
                            .fechaMetaReal(fechaMetaReal)
                    .build());
        }

        // --- Construcción del DTO de Respuesta Final ---
        return ResumenReservasDTO.builder()
                .totalReservado(totalReservadoGeneral)
                .totalReservadoAhorros(totalReservadoAhorros)
                .totalReservadoGastoFijos(totalReservadoGastoFijos)
                .totalReservadoInversiones(totalReservadoInversiones)
                .totalReservadoGFMes(totalReservadoGFMes)
                .pptoSemanalAhorros(pptoSemanalAhorros)
                .pptoSemanalGastosFijos(pptoSemanalGastosFijos)
                .pptoSemanalInversiones(pptoSemanalInversiones)
                .pptoSemanalGFMes(pptoSemanalGFMes)
                .pptoSemanalTotal(pptoSemanalTotal)
                .pptoMensualAhorros(pptoMensualAhorros)
                .pptoMensualGastosFijos(pptoMensualGastosFijos)
                .pptoMensualTotal(pptoMensualTotal)
                .reservas(detalles)
                .build();
    }

    // --- Lógica de Movimientos de Reserva ---
    public Page<MovimientoReservaDTO> consultarMovimientosPorReserva(Long reservaId, int anio, int mes, int page, int size) {
        User user = authHelper.getAuthenticatedUser();
        reservaRepository.findByIdAndUser(reservaId, user)
                .orElseThrow(() -> new NegocioException("La Reserva con id " + reservaId + " no existe o no pertenece al usuario."));

        Pageable pageable = PageRequest.of(page, size);
        Page<LibroReserva> movimientos = libroReservaRepository.findByReservaIdAndFecha(reservaId, anio, mes, pageable);

        return movimientos.map(this::mapToMovimientoReservaDTO);
    }

    @Transactional
    public void eliminarMovimientoReserva(Long movimientoId) {
        User user = authHelper.getAuthenticatedUser();
        LibroReserva movimiento = libroReservaRepository.findById(movimientoId)
                .orElseThrow(() -> new NegocioException("El movimiento de reserva con id " + movimientoId + " no existe."));

        if (!movimiento.getReserva().getUser().getId().equals(user.getId())) {
            throw new NegocioException("No tiene permiso para eliminar este movimiento.");
        }

        libroReservaRepository.deleteById(movimientoId);
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

    private MovimientoReservaDTO mapToMovimientoReservaDTO(LibroReserva libroReserva) {
        return MovimientoReservaDTO.builder()
                .id(libroReserva.getId())
                .fecha(libroReserva.getFecha())
                .concepto(libroReserva.getConcepto())
                .tipoMovimiento(libroReserva.getTipoMovimiento())
                .valor(libroReserva.getValor())
                .cuentaDescripcion(libroReserva.getCuenta() != null ? libroReserva.getCuenta().getDescripcion() : null)
                .build();
    }
}
