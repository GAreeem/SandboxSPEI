package com.example.sandboxspei.dto;

/**
 * Un error de validación individual dentro de la lista acumulada de una
 * respuesta 422.
 */
public record ErrorValidacionDTO(
        String codigo,
        String campo,
        String mensaje
) {
}
