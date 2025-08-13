package com.cuervo.finanzas.service;

import com.cuervo.finanzas.dto.ProyeccionRequestDTO;
import com.cuervo.finanzas.entity.Proyeccion;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.finanzas.repository.ProyeccionRepository;
import com.cuervo.finanzas.service.auth.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyeccionService {

    private final ProyeccionRepository proyeccionRepository;
    private final AuthHelper authHelper;

    public Proyeccion crearProyeccion(ProyeccionRequestDTO request) {
        User user = authHelper.getAuthenticatedUser();
        Proyeccion proyeccion = new Proyeccion();
        proyeccion.setConcepto(request.getConcepto());
        proyeccion.setValor(request.getValor());
        proyeccion.setUser(user);
        return proyeccionRepository.save(proyeccion);
    }

    public List<Proyeccion> consultarProyecciones() {
        User user = authHelper.getAuthenticatedUser();
        return proyeccionRepository.findAllByUser(user);
    }

    public Proyeccion consultarProyeccionPorId(Long id) {
        User user = authHelper.getAuthenticatedUser();
        return proyeccionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NegocioException("La proyección con id " + id + " no existe o no pertenece al usuario."));
    }

    public Proyeccion editarProyeccion(Long id, ProyeccionRequestDTO request) {
        Proyeccion proyeccion = consultarProyeccionPorId(id);
        proyeccion.setConcepto(request.getConcepto());
        proyeccion.setValor(request.getValor());
        return proyeccionRepository.save(proyeccion);
    }

    public void eliminarProyeccion(Long id) {
        // Valida que la proyección exista y pertenezca al usuario antes de eliminar
        consultarProyeccionPorId(id);
        proyeccionRepository.deleteById(id);
    }
}
