package com.example.sandboxspei.dto;

/**
 * Cuerpo de {@code POST /operaciones/{id}/transiciones}: estado destino
 * solicitado y motivo opcional (queda en la auditoría de la transición).
 */
public record TransicionRequestDTO(
        String estado,
        String motivo
) {
}
