package com.example.sandboxspei.service;

import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.OperacionResponseDTO;
import com.example.sandboxspei.entity.ClaveIdempotencia;
import com.example.sandboxspei.entity.Operacion;
import com.example.sandboxspei.exception.IdempotenciaConflictoException;
import com.example.sandboxspei.repository.ClaveIdempotenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lógica de idempotencia de {@code POST /operaciones}: consulta y registro
 * de claves ({@code Clave-Idempotencia}).
 */
@Service
public class IdempotenciaService {

    private final ClaveIdempotenciaRepository claveIdempotenciaRepository;

    public IdempotenciaService(ClaveIdempotenciaRepository claveIdempotenciaRepository) {
        this.claveIdempotenciaRepository = claveIdempotenciaRepository;
    }

    /**
     * Revisa si la clave ya fue usada.
     *
     * <ul>
     *   <li><b>Clave inexistente</b> → {@code Optional.empty()}: es la primera vez,
     *       el llamador debe validar y crear la operación.</li>
     *   <li><b>Clave existente y mismo hash</b> (reintento exacto) → devuelve la
     *       operación ORIGINAL (esNueva = false → HTTP 200). No crea nada.</li>
     *   <li><b>Clave existente y hash distinto</b> → lanza
     *       {@link IdempotenciaConflictoException} (HTTP 409, PRX-015). No crea
     *       ni modifica nada.</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public Optional<ResultadoCreacionOperacion> resolverReintento(String clave, String hashCuerpo,
                                                                   OperacionRequestDTO request) {
        Optional<ClaveIdempotencia> existente = claveIdempotenciaRepository.findById(clave);
        if (existente.isEmpty()) {
            return Optional.empty();
        }
        ClaveIdempotencia registro = existente.get();
        if (!registro.getHashCuerpo().equals(hashCuerpo)) {
            throw new IdempotenciaConflictoException(request.referenciaSeguimiento());
        }
        Operacion original = registro.getOperacion();
        return Optional.of(new ResultadoCreacionOperacion(OperacionResponseDTO.desdeEntidad(original), false));
    }

    /**
     * Guarda la clave vinculada a la operación recién creada. Debe ejecutarse
     * dentro de la MISMA transacción que crea la operación (si algo falla,
     * ambas se revierten). Usa {@code saveAndFlush} para que un duplicado
     * concurrente de la clave falle aquí mismo (llave primaria de MySQL).
     */
    @Transactional
    public void registrar(String clave, String hashCuerpo, Operacion operacion) {
        claveIdempotenciaRepository.saveAndFlush(
                new ClaveIdempotencia(clave, hashCuerpo, operacion, LocalDateTime.now()));
    }
}
