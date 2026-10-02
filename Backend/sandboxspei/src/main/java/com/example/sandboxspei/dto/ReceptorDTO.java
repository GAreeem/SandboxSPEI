package com.example.sandboxspei.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos del receptor de la instrucción de pago.
 */
public record ReceptorDTO(
        String institucion,
        @Schema(
                description = "Cuenta CLABE del receptor. Debe contener exactamente 18 dígitos numéricos.",
                pattern = "^\\d{18}$",
                example = "646180157012345678"
        )
        String cuenta,
        @Schema(
                description = "Nombre del receptor. Debe contener únicamente letras y espacios, con una longitud de 1 a 40 caracteres.",
                pattern = "^[\\p{L} ]{1,40}$",
                example = "María López"
        )
        String nombre
) {
}
