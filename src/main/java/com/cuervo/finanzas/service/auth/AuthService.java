package com.cuervo.finanzas.service.auth;

import com.cuervo.finanzas.dto.auth.AuthResponse;
import com.cuervo.finanzas.dto.auth.LoginRequest;
import com.cuervo.finanzas.dto.auth.RegistroRequest;
import com.cuervo.finanzas.entity.ConfigPresupuesto;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.repository.ConfigPresupuestoRepository;
import com.cuervo.finanzas.repository.UserRepository;
import com.cuervo.finanzas.service.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ConfigPresupuestoRepository configPresupuestoRepository; // Repositorio añadido
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Transactional
    public AuthResponse registro(RegistroRequest request) {
        // 1. Crear y guardar el usuario
        User user = User.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);

        // 2. Crear y guardar la configuración de presupuesto inicial
        ConfigPresupuesto config = new ConfigPresupuesto();
        config.setUser(user);
        config.setIngresoSemanal(request.getIngresoSemanal());
        // Los porcentajes por defecto (60, 20, 10, 10) se asignan automáticamente desde la entidad.
        configPresupuestoRepository.save(config);

        // 3. Generar y devolver el token
        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }
}
