package com.example.sandboxspei.exception;

/**
 * Se lanza cuando se consulta una operación por un identificador
 * inexistente. Provoca una respuesta HTTP 404.
 */
public class OperacionNoEncontradaException extends RuntimeException {

    public OperacionNoEncontradaException(String id) {
        super("No existe una operación con id '" + id + "'");
    }
}
