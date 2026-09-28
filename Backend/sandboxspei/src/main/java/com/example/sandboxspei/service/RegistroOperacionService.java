package com.example.sandboxspei.service;

import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.OperacionResponseDTO;
import com.example.sandboxspei.entity.DocumentoIdentidad;
import com.example.sandboxspei.entity.EstadoOperacion;
import com.example.sandboxspei.entity.Operacion;
import com.example.sandboxspei.entity.ParteOperacion;
import com.example.sandboxspei.entity.TipoOperacion;
import com.example.sandboxspei.repository.OperacionRepository;
import com.example.sandboxspei.validation.ValidadorOperacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Registro transaccional de una operación (y, si aplica, de su clave de
 * idempotencia) en UNA sola transacción.
 *
 * <p>Vive en un bean aparte de {@link OperacionService} para que
 * {@code OperacionService} pueda capturar fuera de la transacción un
 * conflicto de duplicados concurrentes (ver
 * {@link OperacionService#crearOperacion}).</p>
 */
@Service
public class RegistroOperacionService {

    private final OperacionRepository operacionRepository;
    private final IdempotenciaService idempotenciaService;
    private final IdempotenciaHasher idempotenciaHasher;
    private final ValidadorOperacionService validadorOperacionService;
    private final ProcesadorAsincronoService procesadorAsincronoService;

    public RegistroOperacionService(OperacionRepository operacionRepository,
                                    IdempotenciaService idempotenciaService,
                                    IdempotenciaHasher idempotenciaHasher,
                                    ValidadorOperacionService validadorOperacionService,
                                    ProcesadorAsincronoService procesadorAsincronoService) {
        this.operacionRepository = operacionRepository;
        this.idempotenciaService = idempotenciaService;
        this.idempotenciaHasher = idempotenciaHasher;
        this.validadorOperacionService = validadorOperacionService;
        this.procesadorAsincronoService = procesadorAsincronoService;
    }

    /**
     * Algoritmo:
     * <ol>
     *   <li>Sin header {@code Clave-Idempotencia}: valida, registra en RECIBIDO → 201.</li>
     *   <li>Con header: calcula el SHA-256 del cuerpo y consulta la clave.
     *     <ul>
     *       <li>No existe → valida V01-V19, registra la operación (RECIBIDO), guarda la
     *           clave (clave + hash + operacionId) → 201.</li>
     *       <li>Existe con el mismo hash → devuelve la operación original → 200.</li>
     *       <li>Existe con hash distinto → excepción → 409 (PRX-015).</li>
     *     </ul></li>
     * </ol>
     * La consulta de idempotencia va ANTES de la validación: un reintento no
     * se vuelve a validar y un conflicto se reporta como 409 aunque el cuerpo
     * nuevo tuviera errores de formato.
     */
    @Transactional
    public ResultadoCreacionOperacion registrar(OperacionRequestDTO request,
                                                String claveIdempotencia,
                                                String escenarioForzado) {
        boolean conClave = claveIdempotencia != null && !claveIdempotencia.isBlank();
        String hashCuerpo = null;

        if (conClave) {
            hashCuerpo = idempotenciaHasher.calcularHash(request);
            Optional<ResultadoCreacionOperacion> reintento =
                    idempotenciaService.resolverReintento(claveIdempotencia, hashCuerpo, request);
            if (reintento.isPresent()) {
                return reintento.get(); // 200 OK (o 409 si lanzó la excepción)
            }
        }

        // V01-V19: acumula errores y lanza ValidacionException (422) sin persistir nada.
        validadorOperacionService.validar(request);

        // Se usa la instancia devuelta por save(): es la administrada por JPA.
        Operacion operacion = operacionRepository.save(construirOperacion(request));

        if (conClave) {
            idempotenciaService.registrar(claveIdempotencia, hashCuerpo, operacion);
        }

        programarProcesamientoAsincrono(operacion.getId(), escenarioForzado);
        return new ResultadoCreacionOperacion(OperacionResponseDTO.desdeEntidad(operacion), true);
    }

    private Operacion construirOperacion(OperacionRequestDTO request) {
        Operacion operacion = new Operacion();
        operacion.setId("op_" + UUID.randomUUID().toString().replace("-", ""));
        operacion.setTipoOperacion(TipoOperacion.valueOf(request.tipoOperacion()));

        operacion.setEmisor(new ParteOperacion(
                request.emisor().institucion(),
                request.emisor().cuenta(),
                request.emisor().nombre(),
                request.emisor().sucursal()));

        if (request.emisor().documentoIdentidad() != null) {
            operacion.setEmisorDocumentoIdentidad(new DocumentoIdentidad(
                    request.emisor().documentoIdentidad().tipo(),
                    request.emisor().documentoIdentidad().numero()));
        } else {
            operacion.setEmisorDocumentoIdentidad(new DocumentoIdentidad());
        }

        operacion.setReceptor(new ParteOperacion(
                request.receptor().institucion(),
                request.receptor().cuenta(),
                request.receptor().nombre(),
                null));

        operacion.setImporteValor(request.importe().valor());
        operacion.setImporteDivisa(request.importe().divisa());
        operacion.setConcepto(request.concepto());
        operacion.setFolioNumerico(request.folioNumerico());
        operacion.setReferenciaSeguimiento(request.referenciaSeguimiento());

        OffsetDateTime ahora = OffsetDateTime.now();
        operacion.setFechaRegistro(ahora);
        operacion.setFechaActualizacion(ahora);

        // Toda instrucción sintácticamente válida se acepta en RECIBIDO.
        operacion.agregarTransicion(EstadoOperacion.RECIBIDO, null);
        return operacion;
    }

    /**
     * Dispara el avance asíncrono (RECIBIDO → EN_PROCESO → final) solo
     * DESPUÉS del commit; si la transacción se revierte, no se dispara.
     */
    private void programarProcesamientoAsincrono(String operacionId, String escenarioForzado) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    procesadorAsincronoService.procesar(operacionId, escenarioForzado);
                }
            });
        } else {
            procesadorAsincronoService.procesar(operacionId, escenarioForzado);
        }
    }
}
