package com.example.sandboxspei.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envoltorio genérico de paginación para {@code GET /operaciones}.
 */
public record PaginaResponseDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
    public static <T> PaginaResponseDTO<T> desdePage(Page<T> page) {
        return new PaginaResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
