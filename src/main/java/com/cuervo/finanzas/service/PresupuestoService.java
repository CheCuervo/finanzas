package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.*;
import com.cuervo.finanzas.entity.ConfigPresupuesto;
import com.cuervo.finanzas.entity.Reserva;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.entity.enums.TipoReserva;
import com.cuervo.finanzas.repository.ConfigPresupuestoRepository;
import com.cuervo.finanzas.repository.ReservaRepository;
import com.cuervo.finanzas.service.auth.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cuervo.finanzas.exception.NegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class PresupuestoService {

    private final ConfigPresupuestoRepository configPresupuestoRepository;
    private final ReservaRepository reservaRepository;
    private final AuthHelper authHelper;
    private static final BigDecimal CUATRO = new BigDecimal("4");
    private static final BigDecimal CIEN = new BigDecimal("100");

    public ResumenPresupuestoDTO getResumenPresupuesto() {
        User user = authHelper.getAuthenticatedUser();
        ConfigPresupuesto config = configPresupuestoRepository.findByUser(user)
                .orElseGet(() -> createDefaultConfig(user));

        ResumenPresupuestoDTO resumen = new ResumenPresupuestoDTO();
        BigDecimal ingresoSemanal = config.getIngresoSemanal();
        resumen.setConfig(mapToConfigDTO(config, ingresoSemanal));

        if (ingresoSemanal == null || ingresoSemanal.compareTo(BigDecimal.ZERO) == 0) {
            resumen.setPptoSemanal(new PresupuestoDTO());
            resumen.setPptoMensual(new PresupuestoDTO());
            return resumen;
        }

        // Obtener todas las reservas una sola vez
        List<Reserva> todasLasReservas = reservaRepository.findAllByUser(user);

        // Cálculos Semanales (Reales)
        BigDecimal gastosSemanales = calcularTotalSemanal(todasLasReservas, List.of(TipoReserva.GASTO_FIJO, TipoReserva.GASTO_FIJO_MES));
        BigDecimal ahorrosSemanales = calcularTotalSemanal(todasLasReservas, List.of(TipoReserva.AHORRO));
        BigDecimal inversionesSemanales = calcularTotalSemanal(todasLasReservas, List.of(TipoReserva.INVERSION));
        BigDecimal totalReservasSemanales = gastosSemanales.add(ahorrosSemanales).add(inversionesSemanales);
        BigDecimal disponibleSemanal = ingresoSemanal.subtract(totalReservasSemanales);

        PresupuestoDTO pptoSemanal = new PresupuestoDTO();
        pptoSemanal.setGastos(new PresupuestoDetalleDTO(gastosSemanales, calcularPorcentaje(gastosSemanales, ingresoSemanal)));
        pptoSemanal.setAhorros(new PresupuestoDetalleDTO(ahorrosSemanales, calcularPorcentaje(ahorrosSemanales, ingresoSemanal)));
        pptoSemanal.setInversiones(new PresupuestoDetalleDTO(inversionesSemanales, calcularPorcentaje(inversionesSemanales, ingresoSemanal)));
        pptoSemanal.setDisponible(new PresupuestoDetalleDTO(disponibleSemanal, calcularPorcentaje(disponibleSemanal, ingresoSemanal)));

        // Mapear detalles
        pptoSemanal.setDetalleGastos(mapReservasToDTO(todasLasReservas, List.of(TipoReserva.GASTO_FIJO, TipoReserva.GASTO_FIJO_MES)));
        pptoSemanal.setDetalleAhorros(mapReservasToDTO(todasLasReservas, List.of(TipoReserva.AHORRO)));
        pptoSemanal.setDetalleInversiones(mapReservasToDTO(todasLasReservas, List.of(TipoReserva.INVERSION)));
        resumen.setPptoSemanal(pptoSemanal);

        // Cálculos Mensuales (Reales)
        PresupuestoDTO pptoMensual = new PresupuestoDTO();
        pptoMensual.setGastos(new PresupuestoDetalleDTO(gastosSemanales.multiply(CUATRO), pptoSemanal.getGastos().getPorcentaje()));
        pptoMensual.setAhorros(new PresupuestoDetalleDTO(ahorrosSemanales.multiply(CUATRO), pptoSemanal.getAhorros().getPorcentaje()));
        pptoMensual.setInversiones(new PresupuestoDetalleDTO(inversionesSemanales.multiply(CUATRO), pptoSemanal.getInversiones().getPorcentaje()));
        pptoMensual.setDisponible(new PresupuestoDetalleDTO(disponibleSemanal.multiply(CUATRO), pptoSemanal.getDisponible().getPorcentaje()));
        pptoMensual.setDetalleGastos(pptoSemanal.getDetalleGastos());
        pptoMensual.setDetalleAhorros(pptoSemanal.getDetalleAhorros());
        pptoMensual.setDetalleInversiones(pptoSemanal.getDetalleInversiones());
        resumen.setPptoMensual(pptoMensual);

        return resumen;
    }


    public ConfigPresupuesto guardarConfigPresupuesto(ConfigPresupuestoRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();

        // --- Nuevas Validaciones ---
        Integer gastos = request.getGastos();
        Integer ahorros = request.getAhorros();
        Integer inversiones = request.getInversiones();
        Integer libre = request.getLibre();

        if (gastos < 0 || ahorros < 0 || inversiones < 0 || libre < 0) {
            throw new NegocioException("Los porcentajes no pueden ser valores negativos.");
        }

        if (gastos + ahorros + inversiones + libre != 100) {
            throw new NegocioException("La suma de los porcentajes (gastos, ahorros, inversiones, libre) debe ser exactamente 100.");
        }

        BigDecimal totalCuotasSemanales = reservaRepository.sumTotalValorReservaSemanalByUser(user);
        if (request.getIngresoSemanal().compareTo(totalCuotasSemanales) < 0) {
            throw new NegocioException("El ingreso semanal no puede ser menor a la suma de las cuotas semanales de tus reservas (" + totalCuotasSemanales + ").");
        }

        // --- Lógica de Guardado ---
        ConfigPresupuesto config = configPresupuestoRepository.findByUser(user)
                .orElse(new ConfigPresupuesto());

        config.setUser(user);
        config.setIngresoSemanal(request.getIngresoSemanal());
        config.setGastos(gastos);
        config.setAhorros(ahorros);
        config.setInversiones(inversiones);
        config.setLibre(libre);

        return configPresupuestoRepository.save(config);
    }
    private ConfigPresupuesto createDefaultConfig(User user) {
        ConfigPresupuesto defaultConfig = new ConfigPresupuesto();
        defaultConfig.setUser(user);
        return configPresupuestoRepository.save(defaultConfig);
    }

    private ConfigPresupuestoDTO mapToConfigDTO(ConfigPresupuesto config, BigDecimal ingresoSemanal) {
        ConfigPresupuestoDTO dto = new ConfigPresupuestoDTO();
        dto.setIngresoSemanal(ingresoSemanal);
        dto.setIngresosMensuales(ingresoSemanal.multiply(CUATRO));

        dto.setGastos(new ConfigDetalleDTO(calcularValor(ingresoSemanal, config.getGastos()), config.getGastos()));
        dto.setAhorros(new ConfigDetalleDTO(calcularValor(ingresoSemanal, config.getAhorros()), config.getAhorros()));
        dto.setInversiones(new ConfigDetalleDTO(calcularValor(ingresoSemanal, config.getInversiones()), config.getInversiones()));
        dto.setLibre(new ConfigDetalleDTO(calcularValor(ingresoSemanal, config.getLibre()), config.getLibre()));

        return dto;
    }

    private BigDecimal calcularValor(BigDecimal total, Integer porcentaje) {
        if (total == null || porcentaje == null) {
            return BigDecimal.ZERO;
        }
        return total.multiply(new BigDecimal(porcentaje)).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPorcentaje(BigDecimal valor, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTotalSemanal(List<Reserva> reservas, List<TipoReserva> tipos) {
        return reservas.stream()
                .filter(r -> tipos.contains(r.getTipo()))
                .map(Reserva::getValorReservaSemanal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ReservaPresupuestoDTO> mapReservasToDTO(List<Reserva> reservas, List<TipoReserva> tipos) {
        return reservas.stream()
                .filter(r -> tipos.contains(r.getTipo()))
                .map(r -> new ReservaPresupuestoDTO(r.getId(), r.getConcepto(), r.getValorReservaSemanal()))
                .collect(Collectors.toList());
    }
}
