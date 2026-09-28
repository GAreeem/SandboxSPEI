package com.example.sandboxspei.exception;

/**
 * Se lanza cuando una {@code Clave-Idempotencia} ya utilizada se reenvía con
 * un cuerpo distinto al original. El {@code @RestControllerAdvice} la
 * traduce a HTTP 409 con código {@code PRX-015}. Conserva la
 * {@code referenciaSeguimiento} del cuerpo recibido para poder ecoarla en
 * la respuesta.
 */
public class IdempotenciaConflictoException extends RuntimeException {

    public static final String CODIGO = "PRX-015";
    public static final String CAMPO = "Clave-Idempotencia";
    public static final String MENSAJE =
            "La clave de idempotencia ya fue utilizada con un cuerpo de peticion diferente";

    private final String referenciaSeguimiento;

    public IdempotenciaConflictoException(String referenciaSeguimiento) {
        super(MENSAJE);
        this.referenciaSeguimiento = referenciaSeguimiento;
    }

    public String getReferenciaSeguimiento() {
        return referenciaSeguimiento;
    }
}
