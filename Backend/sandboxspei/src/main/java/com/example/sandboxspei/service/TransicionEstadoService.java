package com.example.sandboxspei.service;

import com.example.sandboxspei.engine.EscenarioResolver;
import com.example.sandboxspei.engine.MaquinaEstados;
import com.example.sandboxspei.engine.ResultadoEscenario;
import com.example.sandboxspei.entity.EstadoOperacion;
import com.example.sandboxspei.entity.Operacion;
import com.example.sandboxspei.exception.OperacionNoEncontradaException;
import com.example.sandboxspei.repository.OperacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aplica, cada una en su propia transacción, las transiciones de estado que
 * ocurren en segundo plano después de que la operación fue registrada en
 * {@code RECIBIDO}.
 *
 * <p>Vive en un bean aparte de {@link ProcesadorAsincronoService} a propósito:
 * las esperas ({@code Thread.sleep}) ocurren FUERA de cualquier transacción y
 * {@code @Transactional} funciona al invocarse a través del proxy.</p>
 *
 * <p>Cada paso lee la operación con bloqueo y solo actúa si sigue en el
 * estado esperado: si mientras tanto alguien la movió (p. ej. una
 * transición manual a RECHAZADO), el paso se omite en silencio en vez de
 * fallar con PRX-014 en un hilo de fondo.</p>
 */
@Service
public class TransicionEstadoService {

    private static final Logger log = LoggerFactory.getLogger(TransicionEstadoService.class);

    private final OperacionRepository operacionRepository;
    private final EscenarioResolver escenarioResolver;
    private final MaquinaEstados maquinaEstados;

    public TransicionEstadoService(OperacionRepository operacionRepository,
                                   EscenarioResolver escenarioResolver,
                                   MaquinaEstados maquinaEstados) {
        this.operacionRepository = operacionRepository;
        this.escenarioResolver = escenarioResolver;
        this.maquinaEstados = maquinaEstados;
    }

    /**
     * Paso 1: RECIBIDO → EN_PROCESO. Si el escenario es S05 ("permanece en
     * EN_PROCESO"), esta transición ya lleva el motivo PRX-023 porque la
     * máquina de estados no permite EN_PROCESO → EN_PROCESO.
     *
     * @return {@code true} si avanzó; {@code false} si la operación ya no estaba en RECIBIDO
     */
    @Transactional
    public boolean pasarAEnProceso(String operacionId, String escenarioForzado) {
        Operacion operacion = cargarParaActualizar(operacionId);
        if (operacion.getEstado() != EstadoOperacion.RECIBIDO) {
            log.info("Operación {} ya no está en RECIBIDO (estado actual: {}); se omite el avance automático",
                    operacionId, operacion.getEstado());
            return false;
        }
        ResultadoEscenario escenario = resolverEscenario(operacion, escenarioForzado);
        operacion.setEscenarioResuelto(escenario.codigoEscenario());

        String motivo = escenario.estadoDestino() == EstadoOperacion.EN_PROCESO ? escenario.motivo() : null;
        maquinaEstados.transicionar(operacion, EstadoOperacion.EN_PROCESO, motivo);
        operacionRepository.save(operacion);
        return true;
    }

    /**
     * Paso 2: EN_PROCESO → estado final del escenario. Para S05 no hay
     * transición adicional. Se omite si la operación ya no está en EN_PROCESO.
     */
    @Transactional
    public void aplicarEstadoFinal(String operacionId, String escenarioForzado) {
        Operacion operacion = cargarParaActualizar(operacionId);
        if (operacion.getEstado() != EstadoOperacion.EN_PROCESO) {
            log.info("Operación {} ya no está en EN_PROCESO (estado actual: {}); se omite el estado final automático",
                    operacionId, operacion.getEstado());
            return;
        }
        ResultadoEscenario escenario = resolverEscenario(operacion, escenarioForzado);
        if (escenario.estadoDestino() == EstadoOperacion.EN_PROCESO) {
            log.debug("Operación {} permanece en EN_PROCESO (escenario {})", operacionId, escenario.codigoEscenario());
            return;
        }
        maquinaEstados.transicionar(operacion, escenario.estadoDestino(), escenario.motivo());
        operacionRepository.save(operacion);
    }

    private Operacion cargarParaActualizar(String operacionId) {
        return operacionRepository.findByIdForUpdate(operacionId)
                .orElseThrow(() -> new OperacionNoEncontradaException(operacionId));
    }

    private ResultadoEscenario resolverEscenario(Operacion operacion, String escenarioForzado) {
        return escenarioResolver.resolver(
                operacion.getReceptor().getCuenta(),
                operacion.getReceptor().getInstitucion(),
                escenarioForzado
        );
    }
}
