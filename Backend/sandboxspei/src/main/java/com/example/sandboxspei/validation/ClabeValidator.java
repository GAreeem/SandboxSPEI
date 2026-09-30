package com.example.sandboxspei.validation;

import org.springframework.stereotype.Component;

@Component
public class ClabeValidator {

    private static final int[] PESOS = {3, 7, 1};
    private static final int LONGITUD_CLABE = 18;

    /** Valida que el valor tenga exactamente 18 dígitos numéricos */
    public boolean esFormatoValido(String clabe) {
        return clabe != null && clabe.matches("\\d{18}");
    }

    /** Calcula el dígito verificador esperado a partir de los primeros 17 dígitos de la CLABE. */
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

    /** Valida formato y dígito verificador de la CLABE */
    public boolean esClabeValida(String clabe) {
        if (!esFormatoValido(clabe)) {
            return false;
        }
        int digitoEsperado = calcularDigitoVerificador(clabe);
        int digitoRecibido = Character.getNumericValue(clabe.charAt(LONGITUD_CLABE - 1));
        return digitoEsperado == digitoRecibido;
    }

    public String extraerInstitucion(String clabe) {
        return clabe.substring(0, 3);
    }

    public String extraerSegmentoEscenario(String clabe) {
        return clabe.substring(13, 17);
    }
}
