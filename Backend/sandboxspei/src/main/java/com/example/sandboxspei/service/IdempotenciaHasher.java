package com.example.sandboxspei.service;

import com.example.sandboxspei.dto.OperacionRequestDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calcula el hash SHA-256 (hexadecimal, 64 caracteres) del cuerpo de una
 * petición de forma <b>determinista</b>.
 *
 * <p>Estrategia: el cuerpo se "aplana" a pares {@code ruta → valor}
 * ({@code emisor.cuenta}, {@code importe.valor}, ...), se ordenan
 * alfabéticamente por ruta y se concatenan en un texto canónico. Así:</p>
 * <ul>
 *   <li>el orden de las propiedades en el JSON recibido no afecta el hash;</li>
 *   <li>los espacios/saltos de línea del JSON original no afectan el hash;</li>
 *   <li>{@code 1500.5} y {@code 1500.50} producen el mismo hash (el importe
 *       se normaliza sin ceros sobrantes);</li>
 *   <li>un campo ausente y un campo {@code null} son equivalentes.</li>
 * </ul>
 * No depende de Jackson, por lo que el resultado no cambia con la
 * configuración de serialización.
 */
@Component
public class IdempotenciaHasher {

    public String calcularHash(OperacionRequestDTO request) {
        return sha256Hex(construirTextoCanonico(request));
    }

    /** Visible para pruebas: texto canónico sobre el que se calcula el hash. */
    String construirTextoCanonico(OperacionRequestDTO r) {
        Map<String, String> campos = new TreeMap<>();

        agregar(campos, "tipoOperacion", r.tipoOperacion());
        agregar(campos, "concepto", r.concepto());
        agregar(campos, "folioNumerico", r.folioNumerico() != null ? r.folioNumerico().toString() : null);
        agregar(campos, "referenciaSeguimiento", r.referenciaSeguimiento());

        if (r.emisor() != null) {
            agregar(campos, "emisor.institucion", r.emisor().institucion());
            agregar(campos, "emisor.cuenta", r.emisor().cuenta());
            agregar(campos, "emisor.nombre", r.emisor().nombre());
            agregar(campos, "emisor.sucursal", r.emisor().sucursal());
            if (r.emisor().documentoIdentidad() != null) {
                agregar(campos, "emisor.documentoIdentidad.tipo", r.emisor().documentoIdentidad().tipo());
                agregar(campos, "emisor.documentoIdentidad.numero", r.emisor().documentoIdentidad().numero());
            }
        }
        if (r.receptor() != null) {
            agregar(campos, "receptor.institucion", r.receptor().institucion());
            agregar(campos, "receptor.cuenta", r.receptor().cuenta());
            agregar(campos, "receptor.nombre", r.receptor().nombre());
        }
        if (r.importe() != null) {
            agregar(campos, "importe.valor", normalizar(r.importe().valor()));
            agregar(campos, "importe.divisa", r.importe().divisa());
        }

        // ruta=longitud:valor\n  (la longitud evita ambigüedades entre valores con separadores)
        StringBuilder texto = new StringBuilder();
        campos.forEach((ruta, valor) ->
                texto.append(ruta).append('=').append(valor.length()).append(':').append(valor).append('\n'));
        return texto.toString();
    }

    private void agregar(Map<String, String> campos, String ruta, String valor) {
        if (valor != null) {
            campos.put(ruta, valor);
        }
    }

    private String normalizar(BigDecimal valor) {
        if (valor == null) {
            return null;
        }
        return valor.compareTo(BigDecimal.ZERO) == 0 ? "0" : valor.stripTrailingZeros().toPlainString();
    }

    private String sha256Hex(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(contenido.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
