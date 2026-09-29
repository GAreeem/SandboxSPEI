package com.example.sandboxspei.engine;

import com.example.sandboxspei.entity.EstadoOperacion;
import com.example.sandboxspei.entity.Operacion;
import com.example.sandboxspei.exception.TransicionInvalidaException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados explícita: ÚNICO punto por el que una {@link Operacion}
 * puede cambiar de estado después de su registro. Cada transición aceptada
 * queda auditada en la lista {@code transiciones} de la operación (estado,
 * momento y motivo).
 *
 * <h3>Matriz de transiciones permitidas</h3>
 * <pre>
 *   RECIBIDO   → EN_PROCESO, RECHAZADO
 *   EN_PROCESO → LIQUIDADO, DEVUELTO, EN_INVESTIGACION
 * </pre>
 * <ul>
 *   <li><b>Estados terminales</b> ({@code LIQUIDADO}, {@code DEVUELTO},
 *       {@code RECHAZADO}): inmutables, no pueden pasar a NINGÚN estado.</li>
 *   <li>{@code EN_INVESTIGACION} no tiene salidas definidas en la matriz, por
 *       lo que tampoco admite transiciones.</li>
 *   <li>Pasar al mismo estado (p. ej. EN_PROCESO → EN_PROCESO) tampoco está
 *       permitido.</li>
 * </ul>
 * Cualquier otro intento lanza {@link TransicionInvalidaException}
 * (HTTP 409, PRX-014) SIN modificar la operación.
 */
@Component
public class MaquinaEstados {

    /** Estados finales: inmutables bajo cualquier circunstancia. */
    private static final Set<EstadoOperacion> ESTADOS_TERMINALES =
            EnumSet.of(EstadoOperacion.LIQUIDADO, EstadoOperacion.DEVUELTO, EstadoOperacion.RECHAZADO);

    private static final Map<EstadoOperacion, Set<EstadoOperacion>> TRANSICIONES_PERMITIDAS =
            new EnumMap<>(EstadoOperacion.class);

    static {
        for (EstadoOperacion estado : EstadoOperacion.values()) {
            TRANSICIONES_PERMITIDAS.put(estado, EnumSet.noneOf(EstadoOperacion.class));
        }
        TRANSICIONES_PERMITIDAS.get(EstadoOperacion.RECIBIDO)
                .addAll(EnumSet.of(EstadoOperacion.EN_PROCESO, EstadoOperacion.RECHAZADO));
        TRANSICIONES_PERMITIDAS.get(EstadoOperacion.EN_PROCESO)
                .addAll(EnumSet.of(EstadoOperacion.LIQUIDADO, EstadoOperacion.DEVUELTO, EstadoOperacion.EN_INVESTIGACION));
    }

    public boolean esEstadoTerminal(EstadoOperacion estado) {
        return ESTADOS_TERMINALES.contains(estado);
    }

    /**
     * Indica si {@code origen → destino} está permitida. Un estado terminal
     * nunca permite salida, aunque la tabla llegara a tener una entrada.
     */
    public boolean esTransicionPermitida(EstadoOperacion origen, EstadoOperacion destino) {
        if (origen == null || destino == null || esEstadoTerminal(origen)) {
            return false;
        }
        return TRANSICIONES_PERMITIDAS.getOrDefault(origen, Set.of()).contains(destino);
    }

    /**
     * Aplica la transición y registra la auditoría (estado, momento, motivo).
     * Si no está permitida, lanza la excepción ANTES de tocar la operación.
     *
     * @throws TransicionInvalidaException PRX-014, HTTP 409
     */
    public void transicionar(Operacion operacion, EstadoOperacion destino, String motivo) {
        EstadoOperacion origen = operacion.getEstado();
        if (!esTransicionPermitida(origen, destino)) {
            throw new TransicionInvalidaException(
                    operacion.getReferenciaSeguimiento(), origen, destino != null ? destino.name() : null);
        }
        operacion.agregarTransicion(destino, motivo);
    }
}
