package com.example.sandboxspei.dto;

/**
 * Documento de identidad del emisor, obligatorio solo para operaciones VNT.
 */
public record DocumentoIdentidadDTO(
        String tipo,
        String numero
) {
}
