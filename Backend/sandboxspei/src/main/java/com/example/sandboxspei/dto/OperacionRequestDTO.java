package com.example.sandboxspei.dto;

/**
 * Cuerpo de la petición {@code POST /api/v1/operaciones}. No se anotan
 * restricciones de Bean Validation aquí porque el motor de validación
 * V01-V19 ({@code ValidadorOperacionService}) acumula y reporta todos los
 * errores en una sola respuesta 422, en vez de fallar en el primer campo.
 */
public record OperacionRequestDTO(
        String tipoOperacion,
        EmisorDTO emisor,
        ReceptorDTO receptor,
        ImporteDTO importe,
        String concepto,
        Integer folioNumerico,
        String referenciaSeguimiento
) {
}
