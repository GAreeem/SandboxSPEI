package com.example.sandboxspei.validation;

import org.springframework.stereotype.Component;

/**
 * Validador del formato y del dígito verificador de una CLABE
 * interbancaria (18 dígitos).
 *
 * <p>Algoritmo (regla V02): se aplican pesos cíclicos {@code [3, 7, 1]}
 * sobre los primeros 17 dígitos; para cada producto se toma
 * {@code producto % 10} y se suman los residuos. El dígito de control es
 * {@code (10 - (sumaTotal % 10)) % 10} y debe coincidir con el dígito 18.</p>
 */
@Component
public class ClabeValidator {

    private static final int[] PESOS = {3, 7, 1};
    private static final int LONGITUD_CLABE = 18;

    /**
     * Valida que el valor tenga exactamente 18 dígitos numéricos (regla
     * V01/V03).
     */
    public boolean esFormatoValido(String clabe) {
        return clabe != null && clabe.matches("\\d{18}");
    }

    /**
     * Calcula el dígito verificador esperado a partir de los primeros 17
     * dígitos de la CLABE.
     */
    public int calcularDigitoVerificador(String clabe) {
        int sumaTotal = 0;
        for (int i = 0; i < LONGITUD_CLABE - 1; i++) {
            int digito = Character.getNumericValue(clabe.charAt(i));
            int peso = PESOS[i % PESOS.length];
            int producto = digito * peso;
            sumaTotal += producto % 10;
        }
        return (10 - (sumaTotal % 10)) % 10;
    }

    /**
     * Valida formato y dígito verificador de la CLABE (reglas V01/V02/V03).
     */
    public boolean esClabeValida(String clabe) {
        if (!esFormatoValido(clabe)) {
            return false;
        }
        int digitoEsperado = calcularDigitoVerificador(clabe);
        int digitoRecibido = Character.getNumericValue(clabe.charAt(LONGITUD_CLABE - 1));
        return digitoEsperado == digitoRecibido;
    }

    /**
     * Extrae los 3 dígitos de institución (posiciones 1-3) de la CLABE.
     */
    public String extraerInstitucion(String clabe) {
        return clabe.substring(0, 3);
    }

    /**
     * Extrae los dígitos 14 a 17 (índices 13-16, 4 dígitos) usados por el
     * motor de resolución de escenarios deterministas.
     */
    public String extraerSegmentoEscenario(String clabe) {
        return clabe.substring(13, 17);
    }
}
