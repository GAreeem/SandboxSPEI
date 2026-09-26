package com.example.sandboxspei.exception;

import com.example.sandboxspei.dto.ErrorValidacionDTO;

import java.util.List;

/**
 * Se lanza cuando la instrucción de pago viola una o más reglas de
 * validación sintáctica/condicional (V01-V19). Provoca una respuesta
 * HTTP 422 con la lista completa de errores acumulados; nada se persiste.
 */
public class ValidacionException extends RuntimeException {

    private final List<ErrorValidacionDTO> errores;
    private final String referenciaSeguimiento;

    public ValidacionException(List<ErrorValidacionDTO> errores, String referenciaSeguimiento) {
        super("La instrucción de pago contiene " + errores.size() + " error(es) de validación");
        this.errores = errores;
        this.referenciaSeguimiento = referenciaSeguimiento;
    }

    public List<ErrorValidacionDTO> getErrores() {
        return errores;
    }

    public String getReferenciaSeguimiento() {
        return referenciaSeguimiento;
    }
}
