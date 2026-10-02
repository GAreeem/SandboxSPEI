package com.example.sandboxspei.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Documento de identidad del emisor, obligatorio solo para operaciones VNT.
 */
public record DocumentoIdentidadDTO(
        @Schema(
                description = "Tipo de documento de identidad.",
                pattern = "^(?=.{1,30}$)\\p{L}+(?: \\p{L}+)*$",
                example = "INE"
        )
        String tipo,

        @Schema(
                description = "Número del documento de identidad.",
                pattern = "^[A-Za-z0-9]{1,20}$",
                example = "1234567890"
        )
        String numero
) {
}
