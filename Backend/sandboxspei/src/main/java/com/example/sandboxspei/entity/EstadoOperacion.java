package com.example.sandboxspei.entity;

/**
 * Estados posibles del ciclo de vida de una operación SPEI simulada.
 */
public enum EstadoOperacion {
    RECIBIDO,
    EN_PROCESO,
    LIQUIDADO,
    DEVUELTO,
    RECHAZADO,
    EN_INVESTIGACION
}
