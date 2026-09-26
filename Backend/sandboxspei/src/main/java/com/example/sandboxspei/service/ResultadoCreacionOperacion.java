package com.example.sandboxspei.service;

import com.example.sandboxspei.dto.OperacionResponseDTO;

/**
 * Resultado de procesar {@code POST /operaciones}: indica si la operación
 * es nueva (→ 201 Created) o si corresponde a un reintento idempotente con
 * cuerpo idéntico (→ 200 OK, se devuelve el recurso original).
 */
public record ResultadoCreacionOperacion(
        OperacionResponseDTO operacion,
        boolean esNueva
) {
}
