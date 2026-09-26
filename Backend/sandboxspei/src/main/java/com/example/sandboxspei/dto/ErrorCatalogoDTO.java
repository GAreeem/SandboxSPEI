package com.example.sandboxspei.dto;

/**
 * Entrada del catálogo de códigos de error {@code PRX-xxx}.
 */
public record ErrorCatalogoDTO(
        String codigo,
        String mensaje
) {
}
