package com.example.sandboxspei.controller;

import com.example.sandboxspei.dto.ErrorCatalogoDTO;
import com.example.sandboxspei.dto.InstitucionDTO;
import com.example.sandboxspei.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de catálogos de referencia: instituciones participantes
 * y códigos de error {@code PRX-xxx}.
 */
@RestController
@RequestMapping("/api/v1/catalogos")
@Tag(name = "Catálogos", description = "Catálogos de referencia del sandbox")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(summary = "Lista las instituciones participantes del sandbox")
    @GetMapping("/instituciones")
    public ResponseEntity<List<InstitucionDTO>> listarInstituciones() {
        return ResponseEntity.ok(catalogoService.listarInstituciones());
    }

    @Operation(summary = "Lista el catálogo completo de códigos de error PRX-xxx")
    @GetMapping("/errores")
    public ResponseEntity<List<ErrorCatalogoDTO>> listarErrores() {
        return ResponseEntity.ok(catalogoService.listarErrores());
    }
}
