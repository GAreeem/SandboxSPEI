package com.example.sandboxspei.dto;

/**
 * Institución participante del catálogo del sandbox.
 */
public record InstitucionDTO(
        String clave,
        String nombre,
        boolean puedeEmitir,
        boolean enMantenimiento
) {
}
