package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.CuentaRequestDTO;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final LibroGeneralRepository libroGeneralRepository;
    private final AuthHelper authHelper;

    public Cuenta crearCuenta(CuentaRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        if (request.getDescripcion() == null || request.getDescripcion().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la cuenta no puede ser nula o vacía.");
        }
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de cuenta no puede ser nulo.");
        }

        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setDescripcion(request.getDescripcion());
        nuevaCuenta.setTipo(request.getTipo());
        nuevaCuenta.setUser(user);
        return cuentaRepository.save(nuevaCuenta);
    }

    public List<Cuenta> consultarCuentas() {
        User user = authHelper.getAuthenticatedUser();
        return cuentaRepository.findAllByUser(user);
    }

    public Cuenta consultarCuentaPorId(Long id) {
        User user = authHelper.getAuthenticatedUser();
        return cuentaRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NegocioException("La Cuenta con id " + id + " no existe o no pertenece al usuario."));
    }

    public Cuenta editarCuenta(Long id, CuentaRequestDTO request) {
        Cuenta cuenta = consultarCuentaPorId(id); // Ya valida la pertenencia al usuario
        cuenta.setDescripcion(request.getDescripcion());
        cuenta.setTipo(request.getTipo());
        return cuentaRepository.save(cuenta);
    }

    public void eliminarCuenta(Long id) {
        // Valida que la cuenta exista y pertenezca al usuario antes de cualquier operación
        consultarCuentaPorId(id);

        if (libroGeneralRepository.existsByCuentaId(id)) {
            throw new NegocioException("No se puede eliminar la cuenta con id " + id + " porque tiene movimientos asociados.");
        }
        cuentaRepository.deleteById(id);
    }

    @Transactional
    public void reajustarCuenta(ReajusteCuentaRequestDTO request) {
        Cuenta cuenta = consultarCuentaPorId(request.getIdCuenta()); // Ya valida la pertenencia
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
}
