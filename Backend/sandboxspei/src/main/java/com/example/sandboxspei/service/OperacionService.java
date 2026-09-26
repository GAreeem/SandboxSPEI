package com.example.sandboxspei.service;

import tools.jackson.databind.json.JsonMapper;
import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.OperacionResponseDTO;
import com.example.sandboxspei.engine.EscenarioResolver;
import com.example.sandboxspei.engine.MaquinaEstados;
import com.example.sandboxspei.engine.ResultadoEscenario;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio que orquesta el ciclo de vida completo de una
 * operación: validación (V01-V19), verificación de idempotencia, registro
 * en estado {@code RECIBIDO} y ejecución del motor de escenarios
 * deterministas.
 */
@Service
public class OperacionService {

    private final OperacionRepository operacionRepository;
    private final ClaveIdempotenciaRepository claveIdempotenciaRepository;
    private final ValidadorOperacionService validadorOperacionService;
    private final EscenarioResolver escenarioResolver;
    private final MaquinaEstados maquinaEstados;
    private final JsonMapper objectMapper;

    public OperacionService(OperacionRepository operacionRepository,
                             ClaveIdempotenciaRepository claveIdempotenciaRepository,
                             ValidadorOperacionService validadorOperacionService,
                             EscenarioResolver escenarioResolver,
                             MaquinaEstados maquinaEstados,
                            JsonMapper objectMapper) {
        this.operacionRepository = operacionRepository;
        this.claveIdempotenciaRepository = claveIdempotenciaRepository;
        this.validadorOperacionService = validadorOperacionService;
        this.escenarioResolver = escenarioResolver;
        this.maquinaEstados = maquinaEstados;
        this.objectMapper = objectMapper;
    }

    /**
     * Procesa una solicitud de registro de operación de pago.
     *
     * <p>Orden de evaluación: (1) idempotencia (si aplica un reintento
     * idéntico, se corta aquí sin volver a validar), (2) validación
     * sintáctica/condicional V01-V19 (422, nada se persiste si falla),
     * (3) registro en RECIBIDO, (4) ejecución del motor de escenarios.</p>
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

        Operacion operacion = construirOperacion(request);
        operacionRepository.save(operacion);
        ejecutarMotorEscenarios(operacion, request, escenarioForzado);
        operacionRepository.save(operacion);

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
     * Ejecuta el motor de simulación: mueve la operación de RECIBIDO a
     * EN_PROCESO y, según el escenario resuelto, a su estado final
     * (LIQUIDADO, DEVUELTO o EN_INVESTIGACION), o la deja en EN_PROCESO
     * (S05).
     */
    private void ejecutarMotorEscenarios(Operacion operacion, OperacionRequestDTO request, String escenarioForzado) {
        ResultadoEscenario resultado = escenarioResolver.resolver(
                request.receptor().cuenta(),
                request.receptor().institucion(),
                escenarioForzado
        );
        operacion.setEscenarioResuelto(resultado.codigoEscenario());

        if (resultado.estadoDestino() == EstadoOperacion.EN_PROCESO) {
            // S05: una sola transición RECIBIDO -> EN_PROCESO con motivo.
            maquinaEstados.transicionar(operacion, EstadoOperacion.EN_PROCESO, resultado.motivo());
            return;
        }

        // Resto de escenarios: primero pasa por EN_PROCESO sin motivo...
        maquinaEstados.transicionar(operacion, EstadoOperacion.EN_PROCESO, null);
        // ...y luego a su estado final con el motivo correspondiente.
        maquinaEstados.transicionar(operacion, resultado.estadoDestino(), resultado.motivo());
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
