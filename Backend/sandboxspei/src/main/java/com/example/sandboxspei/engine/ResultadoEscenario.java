package com.example.sandboxspei.engine;

import com.example.sandboxspei.entity.EstadoOperacion;

/**
 * Resultado de la resolución determinista de un escenario: el código de
 * escenario (S01-S06), el estado destino y el motivo asociado (si aplica).
 */
public record ResultadoEscenario(
        String codigoEscenario,
        EstadoOperacion estadoDestino,
        String motivo
) {
}
