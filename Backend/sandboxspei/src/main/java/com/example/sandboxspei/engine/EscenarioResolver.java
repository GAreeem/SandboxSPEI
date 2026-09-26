package com.example.sandboxspei.engine;

import com.example.sandboxspei.entity.EstadoOperacion;
import com.example.sandboxspei.validation.ClabeValidator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Motor de simulación y resolución determinista de escenarios, a partir de
 * los dígitos 14-17 de {@code receptor.cuenta} y de la institución
 * receptora. El encabezado {@code X-Escenario-Forzado} sobrescribe esta
 * resolución cuando está presente.
 */
@Component
public class EscenarioResolver {

    private static final Set<String> ESCENARIOS_VALIDOS = Set.of("S01", "S02", "S03", "S04", "S05", "S06");
    private static final String INSTITUCION_EN_MANTENIMIENTO = "805";

    private final ClabeValidator clabeValidator;

    public EscenarioResolver(ClabeValidator clabeValidator) {
        this.clabeValidator = clabeValidator;
    }

    /**
     * Resuelve el escenario aplicable para la operación. Si
     * {@code escenarioForzado} viene presente y es válido (S01-S06), tiene
     * prioridad absoluta sobre la resolución basada en la cuenta.
     */
    public ResultadoEscenario resolver(String cuentaReceptor, String institucionReceptora, String escenarioForzado) {
        String escenario;
        if (escenarioForzado != null && ESCENARIOS_VALIDOS.contains(escenarioForzado.toUpperCase())) {
            escenario = escenarioForzado.toUpperCase();
        } else {
            escenario = resolverPorCuenta(cuentaReceptor, institucionReceptora);
        }
        return mapearResultado(escenario);
    }

    private String resolverPorCuenta(String cuentaReceptor, String institucionReceptora) {
        String segmento = clabeValidator.esFormatoValido(cuentaReceptor)
                ? clabeValidator.extraerSegmentoEscenario(cuentaReceptor)
                : "";

        if ("9004".equals(segmento) || INSTITUCION_EN_MANTENIMIENTO.equals(institucionReceptora)) {
            return "S04";
        }
        return switch (segmento) {
            case "9002" -> "S02";
            case "9003" -> "S03";
            case "9005" -> "S05";
            case "9006" -> "S06";
            default -> "S01";
        };
    }

    private ResultadoEscenario mapearResultado(String escenario) {
        return switch (escenario) {
            case "S02" -> new ResultadoEscenario("S02", EstadoOperacion.DEVUELTO, "PRX-020");
            case "S03" -> new ResultadoEscenario("S03", EstadoOperacion.DEVUELTO, "PRX-021");
            case "S04" -> new ResultadoEscenario("S04", EstadoOperacion.DEVUELTO, "PRX-022");
            case "S05" -> new ResultadoEscenario("S05", EstadoOperacion.EN_PROCESO, "PRX-023");
            case "S06" -> new ResultadoEscenario("S06", EstadoOperacion.EN_INVESTIGACION, "PRX-024");
            default -> new ResultadoEscenario("S01", EstadoOperacion.LIQUIDADO, null);
        };
    }
}
