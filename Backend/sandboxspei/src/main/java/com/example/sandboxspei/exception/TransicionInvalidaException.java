package com.example.sandboxspei.exception;

import com.example.sandboxspei.entity.EstadoOperacion;

/**
 * Se lanza cuando la máquina de estados rechaza una transición no permitida
 * (por ejemplo LIQUIDADO → DEVUELTO, o cualquier salida de un estado
 * terminal). El {@code @RestControllerAdvice} la traduce a HTTP 409 con
 * código {@code PRX-014} y campo {@code estado}.
 */
public class TransicionInvalidaException extends RuntimeException {

    public static final String CODIGO = "PRX-014";
    public static final String CAMPO = "estado";

    private final String referenciaSeguimiento;

    /**
     * @param referenciaSeguimiento referencia de la operación afectada (se ecoa en la respuesta)
     * @param origen                estado actual de la operación
     * @param destino               estado solicitado (texto, porque el cliente puede enviar un valor desconocido)
     */
    public TransicionInvalidaException(String referenciaSeguimiento, EstadoOperacion origen, String destino) {
        super("Transicion de estado no permitida desde " + origen + " a " + destino);
        this.referenciaSeguimiento = referenciaSeguimiento;
    }

    public String getReferenciaSeguimiento() {
        return referenciaSeguimiento;
    }
}
