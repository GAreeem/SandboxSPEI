package com.example.sandboxspei.dto;

/**
 * Datos del receptor de la instrucción de pago.
 */
public record ReceptorDTO(
        String institucion,
        String cuenta,
        String nombre
) {
}
