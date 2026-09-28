package com.example.sandboxspei.service;

import com.example.sandboxspei.dto.ErrorValidacionDTO;
import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.OperacionResponseDTO;
import com.example.sandboxspei.entity.Operacion;
import com.example.sandboxspei.exception.OperacionNoEncontradaException;
import com.example.sandboxspei.exception.ValidacionException;
import com.example.sandboxspei.repository.OperacionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Fachada de operaciones: creación (con idempotencia), consulta por id y
 * listado paginado.
 *
 * <p>{@link #crearOperacion} NO es transaccional a propósito: delega la
 * transacción en {@link RegistroOperacionService} y así puede capturar, ya
 * fuera de ella, el error de llave duplicada que ocurre cuando dos
 * peticiones idénticas con la misma clave llegan al mismo tiempo.</p>
 */
@Service
public class OperacionService {

    private final OperacionRepository operacionRepository;
    private final RegistroOperacionService registroOperacionService;
    private final IdempotenciaService idempotenciaService;
    private final IdempotenciaHasher idempotenciaHasher;

    public OperacionService(OperacionRepository operacionRepository,
                            RegistroOperacionService registroOperacionService,
                            IdempotenciaService idempotenciaService,
                            IdempotenciaHasher idempotenciaHasher) {
        this.operacionRepository = operacionRepository;
        this.registroOperacionService = registroOperacionService;
        this.idempotenciaService = idempotenciaService;
        this.idempotenciaHasher = idempotenciaHasher;
    }

    public ResultadoCreacionOperacion crearOperacion(OperacionRequestDTO request,
                                                     String claveIdempotencia,
                                                     String escenarioForzado) {
        try {
            return registroOperacionService.registrar(request, claveIdempotencia, escenarioForzado);
        } catch (DataIntegrityViolationException e) {
            return resolverDuplicadoConcurrente(request, claveIdempotencia, e);
        }
    }

    /**
     * Carrera entre peticiones simultáneas: la otra petición ganó y ya hizo
     * commit, así que esta transacción se revirtió por llave duplicada
     * (clave de idempotencia o referenciaSeguimiento). Se vuelve a consultar
     * en una transacción nueva y se responde como si hubiera llegado después:
     * 200 (mismo cuerpo) o 409 (cuerpo distinto).
     */
    private ResultadoCreacionOperacion resolverDuplicadoConcurrente(OperacionRequestDTO request,
                                                                     String claveIdempotencia,
                                                                     DataIntegrityViolationException causa) {
        if (claveIdempotencia != null && !claveIdempotencia.isBlank()) {
            String hash = idempotenciaHasher.calcularHash(request);
            Optional<ResultadoCreacionOperacion> reintento =
                    idempotenciaService.resolverReintento(claveIdempotencia, hash, request);
            if (reintento.isPresent()) {
                return reintento.get();
            }
        }
        // Sin clave (o clave distinta): la única otra restricción única es la referencia.
        if (request.referenciaSeguimiento() != null
                && operacionRepository.existsByReferenciaSeguimiento(request.referenciaSeguimiento())) {
            throw new ValidacionException(List.of(new ErrorValidacionDTO("PRX-010", "referenciaSeguimiento",
                    "La referencia de seguimiento ya fue registrada previamente")), request.referenciaSeguimiento());
        }
        throw causa;
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
}
