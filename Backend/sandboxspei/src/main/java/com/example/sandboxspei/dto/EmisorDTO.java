package com.example.sandboxspei.dto;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(
                description = "Nombre del emisor. Debe contener únicamente letras y espacios, con una longitud de 1 a 40 caracteres.",
                pattern = "^[\\p{L} ]{1,40}$",
                example = "Juan Pérez"
        )
        String nombre,
        String sucursal,
        DocumentoIdentidadDTO documentoIdentidad,
        @Schema(
                description = "Identificación fiscal (RFC) del emisor. Debe cumplir el formato de RFC válido.",
                pattern = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$",
                example = "GARE800101ABC"
        )
        String identificacionFiscal
) {
}
