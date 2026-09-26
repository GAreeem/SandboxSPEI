package com.example.sandboxspei.exception;

import com.example.sandboxspei.entity.EstadoOperacion;

/**
 * Se lanza cuando la máquina de estados detecta un intento de transición
 * no permitida. Provoca una respuesta HTTP 409 con código {@code PRX-014}.
 */
public class TransicionInvalidaException extends RuntimeException {

    public static final String CODIGO = "PRX-014";

    public TransicionInvalidaException(EstadoOperacion origen, EstadoOperacion destino) {
        super("Transición no permitida de '" + origen + "' a '" + destino + "'");
    }
}
