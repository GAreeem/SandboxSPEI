package com.example.sandboxspei.exception;

import com.example.sandboxspei.dto.ErrorResponseDTO;
import com.example.sandboxspei.dto.ErrorValidacionDTO;
import com.example.sandboxspei.dto.ErrorValidacionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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
     * Reutilización de Clave-Idempotencia con cuerpo distinto → 409 con la
     * estructura oficial de error (referenciaSeguimiento + lista de errores).
     */
    @ExceptionHandler(IdempotenciaConflictoException.class)
    public ResponseEntity<ErrorValidacionResponseDTO> manejarConflictoIdempotencia(IdempotenciaConflictoException ex) {
        ErrorValidacionResponseDTO cuerpo = new ErrorValidacionResponseDTO(
                ex.getReferenciaSeguimiento(),
                List.of(new ErrorValidacionDTO(
                        IdempotenciaConflictoException.CODIGO,
                        IdempotenciaConflictoException.CAMPO,
                        IdempotenciaConflictoException.MENSAJE)));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }

    /**
     * Transición de estado no permitida (Caso A21) → 409 con la estructura
     * oficial: referenciaSeguimiento + lista de errores (PRX-014, campo "estado").
     */
    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<ErrorValidacionResponseDTO> manejarTransicionInvalida(TransicionInvalidaException ex) {
        ErrorValidacionResponseDTO cuerpo = new ErrorValidacionResponseDTO(
                ex.getReferenciaSeguimiento(),
                List.of(new ErrorValidacionDTO(
                        TransicionInvalidaException.CODIGO,
                        TransicionInvalidaException.CAMPO,
                        ex.getMessage())));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
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
