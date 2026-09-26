package com.example.sandboxspei.exception;

import com.example.sandboxspei.dto.ErrorResponseDTO;
import com.example.sandboxspei.dto.ErrorValidacionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo global y centralizado de excepciones de la API. Traduce cada
 * excepción de dominio al código de estado HTTP y cuerpo de error
 * correspondientes según la especificación del reto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de validación sintáctica/condicional (V01-V19) → 422.
     */
    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorValidacionResponseDTO> manejarValidacion(ValidacionException ex) {
        ErrorValidacionResponseDTO cuerpo = new ErrorValidacionResponseDTO(ex.getReferenciaSeguimiento(), ex.getErrores());
        return ResponseEntity.unprocessableEntity().body(cuerpo);
    }

    /**
     * Operación inexistente → 404.
     */
    @ExceptionHandler(OperacionNoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoEncontrada(OperacionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.de("PRX-404", ex.getMessage()));
    }

    /**
     * Reutilización de Clave-Idempotencia con cuerpo distinto → 409.
     */
    @ExceptionHandler(IdempotenciaConflictoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarConflictoIdempotencia(IdempotenciaConflictoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDTO.de(IdempotenciaConflictoException.CODIGO, ex.getMessage()));
    }

    /**
     * Transición de estado no permitida → 409.
     */
    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> manejarTransicionInvalida(TransicionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDTO.de(TransicionInvalidaException.CODIGO, ex.getMessage()));
    }

    /**
     * Cualquier otro error no controlado → 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.de("PRX-500", "Error interno: " + ex.getMessage()));
    }
}
