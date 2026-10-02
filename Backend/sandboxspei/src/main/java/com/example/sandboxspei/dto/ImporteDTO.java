package com.example.sandboxspei.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Representa el importe de una operación: valor numérico y divisa.
 */
public record ImporteDTO(
        @Schema(
                description = "Valor monetario del importe. Debe ser un valor positivo.",
                example = "1500.50",
                minimum = "0",
                exclusiveMinimum = true
        )
        BigDecimal valor,
        @Schema(
                description = "Divisa del importe. Actualmente solo se acepta MXN (peso mexicano).",
                allowableValues = {"MXN"},
                example = "MXN"
        )
        String divisa
) {
}
