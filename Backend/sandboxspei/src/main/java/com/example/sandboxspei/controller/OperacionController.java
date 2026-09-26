package com.example.sandboxspei.controller;

import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.OperacionResponseDTO;
import com.example.sandboxspei.dto.PaginaResponseDTO;
import com.example.sandboxspei.service.OperacionService;
import com.example.sandboxspei.service.ResultadoCreacionOperacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del recurso Operación (instrucciones de pago SPEI
 * simuladas). Toda la lógica de negocio reside en {@link OperacionService};
 * este controlador solo traduce HTTP a llamadas de servicio.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Operaciones", description = "Registro y consulta de instrucciones de pago SPEI simuladas")
public class OperacionController {

    private final OperacionService operacionService;

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    @Operation(summary = "Registra una nueva instrucción de pago",
            description = "Toda instrucción sintácticamente válida se acepta y registra en estado RECIBIDO (201). "
                    + "Los fallos de negocio se reflejan como transiciones a DEVUELTO, no como error HTTP.")
    @PostMapping("/operaciones")
    public ResponseEntity<OperacionResponseDTO> crearOperacion(
            @RequestBody OperacionRequestDTO request,
            @Parameter(description = "UUID para garantizar idempotencia del registro")
            @RequestHeader(value = "Clave-Idempotencia", required = false) String claveIdempotencia,
            @Parameter(description = "Fuerza un escenario determinista S01..S06, sobrescribiendo la resolución por cuenta")
            @RequestHeader(value = "X-Escenario-Forzado", required = false) String escenarioForzado) {

        ResultadoCreacionOperacion resultado = operacionService.crearOperacion(request, claveIdempotencia, escenarioForzado);

        HttpStatus status = resultado.esNueva() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.operacion());
    }

    @Operation(summary = "Consulta una operación por identificador",
            description = "Devuelve el recurso completo junto con su historial cronológico de transiciones.")
    @GetMapping("/operaciones/{id}")
    public ResponseEntity<OperacionResponseDTO> obtenerOperacion(@PathVariable String id) {
        return ResponseEntity.ok(operacionService.obtenerPorId(id));
    }

    @Operation(summary = "Lista operaciones de forma paginada",
            description = "Ordenado por fechaRegistro descendente.")
    @GetMapping("/operaciones")
    public ResponseEntity<PaginaResponseDTO<OperacionResponseDTO>> listarOperaciones(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamano", defaultValue = "20") int tamano) {

        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.max(tamano, 1));
        PaginaResponseDTO<OperacionResponseDTO> pagina2 = PaginaResponseDTO.desdePage(operacionService.listar(pageable));
        return ResponseEntity.ok(pagina2);
    }
}
