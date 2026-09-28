package com.example.sandboxspei.dto;

/**
 * Respuesta de {@code GET /salud}.
 */
public record SaludDTO(
        String estado,
        long operaciones,
        long retardoMs
) {
}
