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
 * Máquina de estados explícita que controla estrictamente las transiciones
 * permitidas entre los estados de una {@link Operacion}. Cualquier intento
 * de transición fuera de la tabla definida provoca HTTP 409 (PRX-014).
 */
@Component
public class MaquinaEstados {

    private static final Map<EstadoOperacion, Set<EstadoOperacion>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoOperacion.class);

    static {
        TRANSICIONES_PERMITIDAS.put(EstadoOperacion.RECIBIDO,
                EnumSet.of(EstadoOperacion.EN_PROCESO, EstadoOperacion.RECHAZADO));
        TRANSICIONES_PERMITIDAS.put(EstadoOperacion.EN_PROCESO,
                EnumSet.of(EstadoOperacion.LIQUIDADO, EstadoOperacion.DEVUELTO, EstadoOperacion.EN_INVESTIGACION));
        TRANSICIONES_PERMITIDAS.put(EstadoOperacion.LIQUIDADO, EnumSet.noneOf(EstadoOperacion.class));
        TRANSICIONES_PERMITIDAS.put(EstadoOperacion.DEVUELTO, EnumSet.noneOf(EstadoOperacion.class));
        TRANSICIONES_PERMITIDAS.put(EstadoOperacion.RECHAZADO, EnumSet.noneOf(EstadoOperacion.class));
        TRANSICIONES_PERMITIDAS.put(EstadoOperacion.EN_INVESTIGACION, EnumSet.noneOf(EstadoOperacion.class));
    }

    /**
     * Indica si la transición del estado {@code origen} al {@code destino}
     * está permitida por la tabla de estados.
     */
    public boolean esTransicionPermitida(EstadoOperacion origen, EstadoOperacion destino) {
        return TRANSICIONES_PERMITIDAS.getOrDefault(origen, Set.of()).contains(destino);
    }

    /**
     * Aplica una transición de estado sobre la operación, validando
     * previamente que sea una transición permitida. Registra la transición
     * en el historial cronológico de la operación.
     *
     * @throws TransicionInvalidaException si la transición no está permitida (PRX-014, HTTP 409)
     */
    public void transicionar(Operacion operacion, EstadoOperacion destino, String motivo) {
        EstadoOperacion origen = operacion.getEstado();
        if (!esTransicionPermitida(origen, destino)) {
            throw new TransicionInvalidaException(origen, destino);
        }
        operacion.agregarTransicion(destino, motivo);
    }
}
