package com.example.sandboxspei.dto;

import com.example.sandboxspei.entity.Operacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representación completa de una operación, usada como respuesta de
 * {@code POST /operaciones} (201/200) y {@code GET /operaciones/{id}}.
 */
public record OperacionResponseDTO(
        String id,
        String tipoOperacion,
        EmisorDTO emisor,
        ReceptorDTO receptor,
        BigDecimal importeValor,
        String importeDivisa,
        String concepto,
        Integer folioNumerico,
        String referenciaSeguimiento,
        String estado,
        String escenarioResuelto,
        OffsetDateTime fechaRegistro,
        OffsetDateTime fechaActualizacion,
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
                doc
        );

        ReceptorDTO receptorDTO = new ReceptorDTO(
                op.getReceptor().getInstitucion(),
                op.getReceptor().getCuenta(),
                op.getReceptor().getNombre()
        );

        List<TransicionDTO> transiciones = op.getTransiciones().stream()
                .map(TransicionDTO::desdeEntidad)
                .toList();

        return new OperacionResponseDTO(
                op.getId(),
                op.getTipoOperacion().name(),
                emisorDTO,
                receptorDTO,
                op.getImporteValor(),
                op.getImporteDivisa(),
                op.getConcepto(),
                op.getFolioNumerico(),
                op.getReferenciaSeguimiento(),
                op.getEstado().name(),
                op.getEscenarioResuelto(),
                op.getFechaRegistro(),
                op.getFechaActualizacion(),
                transiciones
        );
    }
}
