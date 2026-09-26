package com.example.sandboxspei.dto;

import java.util.List;

/**
 * Cuerpo de respuesta para HTTP 422: instrucción mal formada. Devuelve la
 * lista completa de errores acumulados, sin registrar nada en base de
 * datos.
 */
public record ErrorValidacionResponseDTO(
        String referenciaSeguimiento,
        List<ErrorValidacionDTO> errores
) {
}
