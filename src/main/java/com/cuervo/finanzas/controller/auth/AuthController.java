package com.cuervo.finanzas.controller.auth;

import com.cuervo.finanzas.dto.auth.AuthResponse;
import com.cuervo.finanzas.dto.auth.LoginRequest;
import com.cuervo.finanzas.dto.auth.RegistroRequest;
import com.cuervo.finanzas.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "registro")
    public ResponseEntity<AuthResponse> registro(@RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authService.registro(request));
    }
}