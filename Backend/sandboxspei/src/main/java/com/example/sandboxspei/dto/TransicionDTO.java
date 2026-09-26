package com.example.sandboxspei.dto;

import com.example.sandboxspei.entity.TransicionEstado;

import java.time.OffsetDateTime;

/**
 * Representación de una transición de estado en las respuestas de la API.
 */
public record TransicionDTO(
        String estado,
        OffsetDateTime momento,
        String motivo
) {
    public static TransicionDTO desdeEntidad(TransicionEstado transicion) {
        return new TransicionDTO(
                transicion.getEstado().name(),
                transicion.getMomento(),
                transicion.getMotivo()
        );
    }
}
