package com.example.sandboxspei.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de la petición {@code POST /api/v1/operaciones}. No se anotan
 * restricciones de Bean Validation aquí porque el motor de validación
 * V01-V19 ({@code ValidadorOperacionService}) acumula y reporta todos los
 * errores en una sola respuesta 422, en vez de fallar en el primer campo.
 */
public record OperacionRequestDTO(
        @Schema(
                description = "Tipo de operación. T2T corresponde a transferencia de cuenta a cuenta y VNT a ventanilla.",
                allowableValues = {"T2T", "VNT"},
                example = "T2T"
        )
        String tipoOperacion,
        EmisorDTO emisor,
        ReceptorDTO receptor,
        ImporteDTO importe,
        @Schema(
                description = "Concepto de la operación. Debe cumplir el formato establecido por las reglas de validación V01-V19.",
                pattern = "^[\\p{L}\\p{N} ]{1,40}$",
                example = "Pago de servicio"
        )
        String concepto,
        Integer folioNumerico,
        @Schema(
                description = "Referencia de seguimiento de la operación. Debe cumplir el formato establecido por las reglas de validación V01-V19.",
                pattern = "[A-Za-z0-9]+",
                example = "REF123456"
        )
        String referenciaSeguimiento
) {
}
