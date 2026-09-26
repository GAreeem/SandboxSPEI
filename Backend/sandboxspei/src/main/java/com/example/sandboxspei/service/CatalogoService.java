package com.example.sandboxspei.service;

import com.example.sandboxspei.dto.ErrorCatalogoDTO;
import com.example.sandboxspei.dto.InstitucionDTO;
import com.example.sandboxspei.entity.Institucion;
import com.example.sandboxspei.repository.InstitucionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Provee los catálogos del sandbox. El catálogo de instituciones ahora se
 * consulta desde la base de datos ({@link InstitucionRepository}) en vez de
 * un mapa en memoria, para que sea la única fuente de verdad usada tanto
 * por este servicio como por el motor de validaciones. El catálogo de
 * códigos de error {@code PRX-xxx} permanece estático porque no forma
 * parte de una entidad persistida.
 */
@Service
public class CatalogoService {

    private final InstitucionRepository institucionRepository;
    private final List<ErrorCatalogoDTO> catalogoErrores;

    public CatalogoService(InstitucionRepository institucionRepository) {
        this.institucionRepository = institucionRepository;

        catalogoErrores = List.of(
                new ErrorCatalogoDTO("PRX-001", "CLABE inválida: debe contener exactamente 18 dígitos numéricos"),
                new ErrorCatalogoDTO("PRX-002", "CLABE inválida: el dígito verificador no corresponde"),
                new ErrorCatalogoDTO("PRX-003", "La institución declarada no existe en el catálogo"),
                new ErrorCatalogoDTO("PRX-004", "El importe debe ser mayor a 0"),
                new ErrorCatalogoDTO("PRX-005", "El importe excede el máximo permitido o tiene más de 2 decimales"),
                new ErrorCatalogoDTO("PRX-006", "La divisa debe ser estrictamente 'MXN'"),
                new ErrorCatalogoDTO("PRX-007", "El concepto debe tener entre 1 y 40 caracteres"),
                new ErrorCatalogoDTO("PRX-008", "El folio numérico debe estar entre 1 y 9,999,999"),
                new ErrorCatalogoDTO("PRX-009", "La referencia de seguimiento debe ser alfanumérica de 1 a 30 caracteres"),
                new ErrorCatalogoDTO("PRX-010", "La referencia de seguimiento ya fue registrada previamente"),
                new ErrorCatalogoDTO("PRX-011", "Campo obligatorio faltante según el tipo de operación"),
                new ErrorCatalogoDTO("PRX-012", "Campo prohibido presente según el tipo de operación"),
                new ErrorCatalogoDTO("PRX-013", "La cuenta del emisor debe ser distinta a la del receptor en T2T"),
                new ErrorCatalogoDTO("PRX-014", "Transición de estado no permitida"),
                new ErrorCatalogoDTO("PRX-015", "Clave-Idempotencia reutilizada con un cuerpo distinto"),
                new ErrorCatalogoDTO("PRX-020", "Fondos insuficientes"),
                new ErrorCatalogoDTO("PRX-021", "Cuenta inexistente"),
                new ErrorCatalogoDTO("PRX-022", "Institución no disponible"),
                new ErrorCatalogoDTO("PRX-023", "Operación en proceso, revisión adicional requerida"),
                new ErrorCatalogoDTO("PRX-024", "Operación enviada a investigación"),
                new ErrorCatalogoDTO("PRX-030", "Los primeros 3 dígitos de la CLABE no coinciden con la institución declarada"),
                new ErrorCatalogoDTO("PRX-031", "El tipo de operación debe ser 'T2T' o 'VNT'")
        );
    }

    /**
     * Lista las instituciones del catálogo para {@code GET /catalogos/instituciones}.
     */
    public List<InstitucionDTO> listarInstituciones() {
        return institucionRepository.findAll().stream()
                .map(this::aDTO)
                .toList();
    }

    public List<ErrorCatalogoDTO> listarErrores() {
        return catalogoErrores;
    }

    /**
     * V04 (PRX-003): la institución debe existir en el catálogo persistido.
     */
    public boolean existeInstitucion(String codigo) {
        return codigo != null && institucionRepository.existsById(codigo);
    }

    public Optional<Institucion> buscarInstitucion(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return institucionRepository.findById(codigo);
    }

    /**
     * Restricción de emisión: la institución 804 no puede actuar como
     * emisora (solo receptora).
     */
    public boolean puedeEmitir(String codigo) {
        return buscarInstitucion(codigo).map(Institucion::isPuedeEmitir).orElse(false);
    }

    /**
     * Usado por el motor de escenarios (S04 - PRX-022): la institución 805
     * está en mantenimiento.
     */
    public boolean estaEnMantenimiento(String codigo) {
        return buscarInstitucion(codigo).map(Institucion::isEnMantenimiento).orElse(false);
    }

    private InstitucionDTO aDTO(Institucion institucion) {
        return new InstitucionDTO(
                institucion.getCodigo(),
                institucion.getNombre(),
                institucion.isPuedeEmitir(),
                institucion.isEnMantenimiento()
        );
    }
}
