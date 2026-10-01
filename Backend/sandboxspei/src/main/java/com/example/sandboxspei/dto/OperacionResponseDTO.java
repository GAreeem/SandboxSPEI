package com.example.sandboxspei.dto;

import com.example.sandboxspei.entity.Operacion;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representación completa de una operación, usada como respuesta de
 * {@code POST /operaciones} (201/200) y {@code GET /operaciones/{id}}.
 *
 * <p>Coincide exactamente con el contrato de la API: {@code importe} es un
 * objeto anidado (no campos aplanados), y no expone atributos internos de
 * la entidad como {@code escenarioResuelto} o {@code fechaActualizacion}
 * (estos siguen existiendo en {@link Operacion} para trazabilidad interna,
 * simplemente no se serializan aquí).</p>
 */
public record OperacionResponseDTO(
        String id,
        String referenciaSeguimiento,
        String estado,
        String tipoOperacion,
        ImporteDTO importe,
        EmisorDTO emisor,
        ReceptorDTO receptor,
        String concepto,
        Integer folioNumerico,
        OffsetDateTime fechaRegistro,
        List<TransicionDTO> transiciones
) {

    public static OperacionResponseDTO desdeEntidad(Operacion op) {
        DocumentoIdentidadDTO doc = op.getEmisorDocumentoIdentidad() != null
                && op.getEmisorDocumentoIdentidad().getTipo() != null
                ? new DocumentoIdentidadDTO(
                        op.getEmisorDocumentoIdentidad().getTipo(),
                        op.getEmisorDocumentoIdentidad().getNumero())
                : null;

        EmisorDTO emisorDTO = new EmisorDTO(
                op.getEmisor().getInstitucion(),
                op.getEmisor().getCuenta(),
                op.getEmisor().getNombre(),
                op.getEmisor().getSucursal(),
                doc,
                op.getEmisorIdentificacionFiscal()
        );

        ReceptorDTO receptorDTO = new ReceptorDTO(
                op.getReceptor().getInstitucion(),
                op.getReceptor().getCuenta(),
                op.getReceptor().getNombre()
        );

        ImporteDTO importeDTO = new ImporteDTO(op.getImporteValor(), op.getImporteDivisa());

        List<TransicionDTO> transiciones = op.getTransiciones().stream()
                .map(TransicionDTO::desdeEntidad)
                .toList();

        return new OperacionResponseDTO(
                op.getId(),
                op.getReferenciaSeguimiento(),
                op.getEstado().name(),
                op.getTipoOperacion().name(),
                importeDTO,
                emisorDTO,
                receptorDTO,
                op.getConcepto(),
                op.getFolioNumerico(),
                op.getFechaRegistro(),
                transiciones
        );
    }
}
