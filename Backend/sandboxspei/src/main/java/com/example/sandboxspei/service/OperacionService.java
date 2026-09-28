package com.example.sandboxspei.service;

import tools.jackson.databind.json.JsonMapper;
import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.OperacionResponseDTO;
import com.example.sandboxspei.entity.*;
import com.example.sandboxspei.exception.IdempotenciaConflictoException;
import com.example.sandboxspei.exception.OperacionNoEncontradaException;
import com.example.sandboxspei.repository.ClaveIdempotenciaRepository;
import com.example.sandboxspei.repository.OperacionRepository;
import com.example.sandboxspei.validation.ValidadorOperacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio que orquesta el ciclo de vida completo de una
 * operación: validación (V01-V19), verificación de idempotencia, registro
 * en estado {@code RECIBIDO} y disparo del procesamiento asíncrono que
 * lo lleva a su estado final.
 */
@Service
public class OperacionService {

    private final OperacionRepository operacionRepository;
    private final ClaveIdempotenciaRepository claveIdempotenciaRepository;
    private final ValidadorOperacionService validadorOperacionService;
    private final ProcesadorAsincronoService procesadorAsincronoService;
    private final JsonMapper objectMapper;

    public OperacionService(OperacionRepository operacionRepository,
                            ClaveIdempotenciaRepository claveIdempotenciaRepository,
                            ValidadorOperacionService validadorOperacionService,
                            ProcesadorAsincronoService procesadorAsincronoService,
                            JsonMapper objectMapper) {
        this.operacionRepository = operacionRepository;
        this.claveIdempotenciaRepository = claveIdempotenciaRepository;
        this.validadorOperacionService = validadorOperacionService;
        this.procesadorAsincronoService = procesadorAsincronoService;
        this.objectMapper = objectMapper;
    }

    /**
     * Procesa una solicitud de registro de operación de pago.
     *
     * <p>Orden de evaluación: (1) idempotencia (si aplica un reintento
     * idéntico, se corta aquí sin volver a validar), (2) validación
     * sintáctica/condicional V01-V19 (422, nada se persiste si falla),
     * (3) registro en RECIBIDO, (4) disparo del procesamiento asíncrono
     * tras el commit.</p>
     */
    @Transactional
    public ResultadoCreacionOperacion crearOperacion(OperacionRequestDTO request,
                                                     String claveIdempotencia,
                                                     String escenarioForzado) {
        String cuerpoCanonico = serializarCanonico(request);
        String hashCuerpo = calcularHash(cuerpoCanonico);

        if (claveIdempotencia != null && !claveIdempotencia.isBlank()) {
            Optional<ClaveIdempotencia> existente = claveIdempotenciaRepository.findByClave(claveIdempotencia);
            if (existente.isPresent()) {
                ClaveIdempotencia registro = existente.get();
                if (!registro.getHashCuerpo().equals(hashCuerpo)) {
                    throw new IdempotenciaConflictoException();
                }
                Operacion operacionOriginal = operacionRepository.findById(registro.getOperacionId())
                        .orElseThrow(() -> new OperacionNoEncontradaException(registro.getOperacionId()));
                return new ResultadoCreacionOperacion(OperacionResponseDTO.desdeEntidad(operacionOriginal), false);
            }
        }

        // V01-V19: valida y acumula errores; lanza ValidacionException (422) sin persistir nada.
        validadorOperacionService.validar(request);

        // Solo se registra en RECIBIDO; el avance a EN_PROCESO y al estado final ocurre en segundo plano.
        Operacion operacion = construirOperacion(request);
        operacionRepository.save(operacion);
        programarProcesamientoAsincrono(operacion.getId(), escenarioForzado);

        if (claveIdempotencia != null && !claveIdempotencia.isBlank()) {
            ClaveIdempotencia registro = new ClaveIdempotencia();
            registro.setClave(claveIdempotencia);
            registro.setHashCuerpo(hashCuerpo);
            registro.setOperacionId(operacion.getId());
            registro.setCuerpoOriginal(cuerpoCanonico);
            registro.setFechaCreacion(OffsetDateTime.now());
            claveIdempotenciaRepository.save(registro);
        }

        return new ResultadoCreacionOperacion(OperacionResponseDTO.desdeEntidad(operacion), true);
    }

    @Transactional(readOnly = true)
    public OperacionResponseDTO obtenerPorId(String id) {
        Operacion operacion = operacionRepository.findById(id)
                .orElseThrow(() -> new OperacionNoEncontradaException(id));
        return OperacionResponseDTO.desdeEntidad(operacion);
    }

    @Transactional(readOnly = true)
    public Page<OperacionResponseDTO> listar(Pageable pageable) {
        return operacionRepository.findAllByOrderByFechaRegistroDesc(pageable)
                .map(OperacionResponseDTO::desdeEntidad);
    }

    // ---- Métodos auxiliares privados ----

    private Operacion construirOperacion(OperacionRequestDTO request) {
        Operacion operacion = new Operacion();
        operacion.setId(generarId());
        operacion.setTipoOperacion(TipoOperacion.valueOf(request.tipoOperacion()));

        ParteOperacion emisor = new ParteOperacion(
                request.emisor().institucion(),
                request.emisor().cuenta(),
                request.emisor().nombre(),
                request.emisor().sucursal()
        );
        operacion.setEmisor(emisor);

        if (request.emisor().documentoIdentidad() != null) {
            operacion.setEmisorDocumentoIdentidad(new DocumentoIdentidad(
                    request.emisor().documentoIdentidad().tipo(),
                    request.emisor().documentoIdentidad().numero()
            ));
        } else {
            operacion.setEmisorDocumentoIdentidad(new DocumentoIdentidad());
        }

        ParteOperacion receptor = new ParteOperacion(
                request.receptor().institucion(),
                request.receptor().cuenta(),
                request.receptor().nombre(),
                null
        );
        operacion.setReceptor(receptor);

        operacion.setImporteValor(request.importe().valor());
        operacion.setImporteDivisa(request.importe().divisa());
        operacion.setConcepto(request.concepto());
        operacion.setFolioNumerico(request.folioNumerico());
        operacion.setReferenciaSeguimiento(request.referenciaSeguimiento());

        OffsetDateTime ahora = OffsetDateTime.now();
        operacion.setFechaRegistro(ahora);
        operacion.setFechaActualizacion(ahora);

        // Estado inicial: toda instrucción sintácticamente válida se acepta.
        operacion.agregarTransicion(EstadoOperacion.RECIBIDO, null);
        return operacion;
    }

    /**
     * Dispara el procesamiento asíncrono (RECIBIDO → EN_PROCESO → estado
     * final) <b>solo después de que la transacción de registro haga
     * commit</b>. Si se lanzara antes, el hilo de fondo podría no encontrar
     * aún la operación en la base de datos.
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

    private String generarId() {
        return "op_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String serializarCanonico(OperacionRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible serializar el cuerpo de la petición", e);
        }
    }

    private String calcularHash(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash no disponible", e);
        }
    }
}
