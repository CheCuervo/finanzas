package com.cuervo.finanzas.service.auth;

import com.cuervo.finanzas.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    /**
     * Obtiene el usuario actualmente autenticado desde el contexto de seguridad.
     * @return El objeto User del usuario logueado.
     * @throws IllegalStateException si no hay un usuario autenticado.
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("No hay un usuario autenticado o la sesión es inválida.");
        }
        return (User) authentication.getPrincipal();
    }
}
