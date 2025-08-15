package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.exception.NegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones de negocio controladas (ej: "saldo insuficiente").
     * Devuelve un error 400 Bad Request.
     */
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, String>> handleNegocioException(NegocioException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * --- CORRECCIÓN ---
     * Maneja todas las demás excepciones no controladas (ej: errores de base de datos, NullPointerException, etc.).
     * Devuelve un error 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        // Se registra el error completo en los logs del servidor para poder depurarlo.
        logger.error("Ocurrió un error inesperado en el servidor: ", ex);

        // Se devuelve una respuesta genérica al cliente para no exponer detalles internos.
        return new ResponseEntity<>(Map.of("error", "Ocurrió un error inesperado en el servidor."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
