package com.example.sandboxspei.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de {@code POST /operaciones/{id}/transiciones}: estado destino
 * solicitado y motivo opcional (queda en la auditoría de la transición).
 */
public record TransicionRequestDTO(
        @Schema(
                description = "Estado destino permitido para una transición manual de la operación.",
                allowableValues = {
                        "RECIBIDO",
                        "EN_PROCESO",
                        "LIQUIDADO",
                        "DEVUELTO",
                        "RECHAZADO",
                        "EN_INVESTIGACION"
                },
                example = "EN_PROCESO"
        )
        String estado,
        String motivo
) {
}
