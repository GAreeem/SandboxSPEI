package com.example.sandboxspei.dto;

import java.time.OffsetDateTime;

/**
 * Cuerpo de error genérico para respuestas 404 y 409.
 */
public record ErrorResponseDTO(
        String codigo,
        String mensaje,
        OffsetDateTime momento
) {
    public static ErrorResponseDTO de(String codigo, String mensaje) {
        return new ErrorResponseDTO(codigo, mensaje, OffsetDateTime.now());
    }
}
