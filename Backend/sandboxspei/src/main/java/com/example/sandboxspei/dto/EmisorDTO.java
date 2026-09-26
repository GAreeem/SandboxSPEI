package com.example.sandboxspei.dto;

/**
 * Datos del emisor de la instrucción de pago. Los campos {@code cuenta},
 * {@code sucursal} y {@code documentoIdentidad} son condicionalmente
 * obligatorios o prohibidos según el {@code tipoOperacion} (ver reglas
 * V-condicionadas en la validación).
 */
public record EmisorDTO(
        String institucion,
        String cuenta,
        String nombre,
        String sucursal,
        DocumentoIdentidadDTO documentoIdentidad
) {
}
