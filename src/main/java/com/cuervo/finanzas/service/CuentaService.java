package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.CuentaBalanceDTO;
import com.cuervo.finanzas.dto.CuentaRequestDTO;
import com.cuervo.finanzas.dto.MovimientoDTO;
import com.cuervo.finanzas.dto.ReajusteCuentaRequestDTO;
import com.cuervo.finanzas.entity.Cuenta;
import com.cuervo.finanzas.entity.LibroGeneral;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.finanzas.repository.CuentaRepository;
import com.cuervo.finanzas.repository.LibroGeneralRepository;
import com.cuervo.finanzas.service.auth.AuthHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final LibroGeneralRepository libroGeneralRepository;
    private final AuthHelper authHelper;

    // --- CREATE ---
    public Cuenta crearCuenta(CuentaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        if (request.getDescripcion() == null || request.getDescripcion().isEmpty()) {
            throw new NegocioException("La descripción de la cuenta no puede ser nula o vacía.");
        }
        if (request.getTipo() == null) {
            throw new NegocioException("El tipo de cuenta no puede ser nulo.");
        }

        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setDescripcion(request.getDescripcion());
        nuevaCuenta.setTipo(request.getTipo());
        nuevaCuenta.setUser(user);
        return cuentaRepository.save(nuevaCuenta);
    }

    // --- READ ---
    public List<CuentaBalanceDTO> consultarCuentas() {
        User user = authHelper.getAuthenticatedUser();
        List<Cuenta> cuentas = cuentaRepository.findAllByUser(user);
        return cuentas.stream()
                .map(this::mapToCuentaBalanceDTO)
                .collect(Collectors.toList());
    }

    public CuentaBalanceDTO consultarCuentaPorId(Long id) {
        User user = authHelper.getAuthenticatedUser();
        Cuenta cuenta = cuentaRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + id + " no existe o no pertenece al usuario."));
        return mapToCuentaBalanceDTO(cuenta);
    }

    // --- UPDATE ---
    public Cuenta editarCuenta(Long id, CuentaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Cuenta cuenta = cuentaRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + id + " no existe o no pertenece al usuario."));
        cuenta.setDescripcion(request.getDescripcion());
        cuenta.setTipo(request.getTipo());
        return cuentaRepository.save(cuenta);
    }

    // --- DELETE ---
    public void eliminarCuenta(Long id) {
        User user = authHelper.getAuthenticatedUser();
        Cuenta cuenta = cuentaRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + id + " no existe o no pertenece al usuario."));

        if (libroGeneralRepository.existsByCuentaId(cuenta.getId())) {
            throw new NegocioException("No se puede eliminar la cuenta con id " + id + " porque tiene movimientos asociados.");
        }
        cuentaRepository.deleteById(id);
    }

    // --- Lógica de Movimientos ---
    public Page<MovimientoDTO> consultarMovimientosPorCuenta(Long cuentaId, int anio, int mes, int page, int size) {
        User user = authHelper.getAuthenticatedUser();
        cuentaRepository.findByIdAndUser(cuentaId, user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + cuentaId + " no existe o no pertenece al usuario."));

        Pageable pageable = PageRequest.of(page, size);
        Page<LibroGeneral> movimientos = libroGeneralRepository.findByCuentaIdAndFecha(cuentaId, anio, mes, pageable);

        return movimientos.map(this::mapToMovimientoDTO);
    }

    // --- Lógica Adicional ---
    @Transactional
    public void reajustarCuenta(ReajusteCuentaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Cuenta cuenta = cuentaRepository.findByIdAndUser(request.getIdCuenta(), user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + request.getIdCuenta() + " no existe o no pertenece al usuario."));

        BigDecimal ingresos = libroGeneralRepository.sumValorByCuentaIdAndTipoMovimiento(cuenta.getId(), TipoMovimiento.INGRESO);
        BigDecimal egresos = libroGeneralRepository.sumValorByCuentaIdAndTipoMovimiento(cuenta.getId(), TipoMovimiento.EGRESO);
        BigDecimal balanceActual = ingresos.subtract(egresos);
        BigDecimal valorObjetivo = request.getValor();
        BigDecimal diferencia = valorObjetivo.subtract(balanceActual);

        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        LibroGeneral movimientoDeAjuste = new LibroGeneral();
        movimientoDeAjuste.setCuenta(cuenta);
        movimientoDeAjuste.setConcepto("Reajuste de cuenta");

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            movimientoDeAjuste.setTipoMovimiento(TipoMovimiento.INGRESO);
            movimientoDeAjuste.setValor(diferencia);
        } else {
            movimientoDeAjuste.setTipoMovimiento(TipoMovimiento.EGRESO);
            movimientoDeAjuste.setValor(diferencia.abs());
        }
        libroGeneralRepository.save(movimientoDeAjuste);
    }

    // --- Helper Methods ---
    private CuentaBalanceDTO mapToCuentaBalanceDTO(Cuenta cuenta) {
        BigDecimal ingresos = libroGeneralRepository.sumValorByCuentaIdAndTipoMovimiento(cuenta.getId(), TipoMovimiento.INGRESO);
        BigDecimal egresos = libroGeneralRepository.sumValorByCuentaIdAndTipoMovimiento(cuenta.getId(), TipoMovimiento.EGRESO);
        BigDecimal balance = ingresos.subtract(egresos);

        return new CuentaBalanceDTO(
                cuenta.getId(),
                cuenta.getDescripcion(),
                cuenta.getTipo(),
                balance
        );
    }

    private MovimientoDTO mapToMovimientoDTO(LibroGeneral libroGeneral) {
        return MovimientoDTO.builder()
                .id(libroGeneral.getId())
                .fecha(libroGeneral.getFecha())
                .concepto(libroGeneral.getConcepto())
                .tipoMovimiento(libroGeneral.getTipoMovimiento())
                .valor(libroGeneral.getValor())
                .cuentaDescripcion(libroGeneral.getCuenta().getDescripcion())
                .cuentaTipo(libroGeneral.getCuenta().getTipo())
                .build();
    }
}
