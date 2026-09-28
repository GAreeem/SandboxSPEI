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
 * Aplica, cada una en su propia transacción, las transiciones de estado
 * que ocurren en segundo plano después de que la operación fue registrada
 * en {@code RECIBIDO}.
 *
 * <p>Vive en un bean aparte de {@link ProcesadorAsincronoService} a
 * propósito: así las esperas ({@code Thread.sleep}) ocurren <b>fuera</b> de
 * cualquier transacción (no se retiene una conexión de BD mientras se
 * espera) y {@code @Transactional} funciona al invocarse a través del
 * proxy de Spring.</p>
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
     * Paso 1: RECIBIDO → EN_PROCESO. Si el escenario resuelto es S05
     * ("permanece en EN_PROCESO"), esta transición ya lleva el motivo
     * PRX-023, porque la máquina de estados no permite EN_PROCESO →
     * EN_PROCESO y el estado final de S05 es precisamente EN_PROCESO.
     */
    @Transactional
    public void pasarAEnProceso(String operacionId, String escenarioForzado) {
        Operacion operacion = cargar(operacionId);
        ResultadoEscenario escenario = resolverEscenario(operacion, escenarioForzado);
        operacion.setEscenarioResuelto(escenario.codigoEscenario());

        String motivo = escenario.estadoDestino() == EstadoOperacion.EN_PROCESO ? escenario.motivo() : null;
        maquinaEstados.transicionar(operacion, EstadoOperacion.EN_PROCESO, motivo);
        operacionRepository.save(operacion);
    }

    /**
     * Paso 2: EN_PROCESO → estado final del escenario (LIQUIDADO,
     * DEVUELTO o EN_INVESTIGACION). Para S05 no hay transición adicional.
     */
    @Transactional
    public void aplicarEstadoFinal(String operacionId, String escenarioForzado) {
        Operacion operacion = cargar(operacionId);
        ResultadoEscenario escenario = resolverEscenario(operacion, escenarioForzado);

        if (escenario.estadoDestino() == EstadoOperacion.EN_PROCESO) {
            log.debug("Operación {} permanece en EN_PROCESO (escenario {})", operacionId, escenario.codigoEscenario());
            return;
        }
        maquinaEstados.transicionar(operacion, escenario.estadoDestino(), escenario.motivo());
        operacionRepository.save(operacion);
    }

    private Operacion cargar(String operacionId) {
        return operacionRepository.findById(operacionId)
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
