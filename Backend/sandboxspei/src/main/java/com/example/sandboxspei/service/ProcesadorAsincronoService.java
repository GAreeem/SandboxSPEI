package com.example.sandboxspei.service;

import com.example.sandboxspei.config.AsyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Orquesta en segundo plano el avance de una operación registrada en
 * {@code RECIBIDO}:
 * <ol>
 *   <li>espera {@code retardoMs} → transiciona a {@code EN_PROCESO};</li>
 *   <li>espera {@code retardoMs} → evalúa el escenario determinista y
 *       transiciona a su estado final.</li>
 * </ol>
 * Este servicio NO es transaccional: cada paso se persiste en su propia
 * transacción a través de {@link }.
 */
@Service
public class ProcesadorAsincronoService {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorAsincronoService.class);

    private final com.example.sandboxspei.service.TransicionEstadoService transicionEstadoService;
    private final long retardoMs;

    public ProcesadorAsincronoService(com.example.sandboxspei.service.TransicionEstadoService transicionEstadoService,
                                      @Value("${sandbox.retardo-ms:1000}") long retardoMs) {
        this.transicionEstadoService = transicionEstadoService;
        this.retardoMs = retardoMs;
    }

    @Async(AsyncConfig.EJECUTOR_OPERACIONES)
    public void procesar(String operacionId, String escenarioForzado) {
        try {
            Thread.sleep(retardoMs);
            transicionEstadoService.pasarAEnProceso(operacionId, escenarioForzado);

            Thread.sleep(retardoMs);
            transicionEstadoService.aplicarEstadoFinal(operacionId, escenarioForzado);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Procesamiento asíncrono interrumpido para la operación {}", operacionId);
        } catch (Exception e) {
            log.error("Error en el procesamiento asíncrono de la operación {}", operacionId, e);
        }
    }
}
