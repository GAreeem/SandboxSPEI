package com.example.sandboxspei.exception;

/**
 * Se lanza cuando una {@code Clave-Idempotencia} ya utilizada se reenvía
 * con un cuerpo de petición distinto al original. Provoca una respuesta
 * HTTP 409 con código {@code PRX-015}.
 */
public class IdempotenciaConflictoException extends RuntimeException {

    public static final String CODIGO = "PRX-015";

    public IdempotenciaConflictoException() {
        super("La Clave-Idempotencia ya fue utilizada con un cuerpo de petición distinto");
    }
}
