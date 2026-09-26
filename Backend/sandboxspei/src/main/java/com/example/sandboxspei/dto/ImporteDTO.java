package com.example.sandboxspei.dto;

import java.math.BigDecimal;

/**
 * Representa el importe de una operación: valor numérico y divisa.
 */
public record ImporteDTO(
        BigDecimal valor,
        String divisa
) {
}
