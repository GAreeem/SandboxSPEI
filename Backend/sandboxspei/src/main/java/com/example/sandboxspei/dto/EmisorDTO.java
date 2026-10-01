package com.example.sandboxspei.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Datos del emisor de la instrucción de pago. Los campos {@code cuenta},
 * {@code sucursal} y {@code documentoIdentidad} son condicionalmente
 * obligatorios o prohibidos según el {@code tipoOperacion} (ver reglas
 * V-condicionadas en la validación).
 *
 * <p>{@code @JsonInclude(NON_NULL)}: en T2T, {@code sucursal} y
 * {@code documentoIdentidad} no aplican y se omiten por completo del JSON
 * en vez de serializarse como {@code null}.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmisorDTO(
        String institucion,
        String cuenta,
        String nombre,
        String sucursal,
        DocumentoIdentidadDTO documentoIdentidad,
        String identificacionFiscal
) {
}
