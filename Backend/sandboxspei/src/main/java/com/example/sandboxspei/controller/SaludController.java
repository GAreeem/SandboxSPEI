package com.example.sandboxspei.controller;

import com.example.sandboxspei.dto.SaludDTO;
import com.example.sandboxspei.repository.OperacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público de estado y salud del sandbox. Disponible en
 * {@code /salud} y también en {@code /api/v1/salud}.
 */
@RestController
@Tag(name = "Salud", description = "Estado del servicio")
public class SaludController {

    private final OperacionRepository operacionRepository;
    private final long retardoMs;

    public SaludController(OperacionRepository operacionRepository,
                           @Value("${sandbox.retardo-ms:1000}") long retardoMs) {
        this.operacionRepository = operacionRepository;
        this.retardoMs = retardoMs;
    }

    @Operation(summary = "Estado y salud del servicio",
            description = "Devuelve el estado, el total de operaciones registradas y el retardo (ms) entre transiciones.")
    @GetMapping({"/salud", "/api/v1/salud"})
    public ResponseEntity<SaludDTO> salud() {
        return ResponseEntity.ok(new SaludDTO("ok", operacionRepository.count(), retardoMs));
    }
}
