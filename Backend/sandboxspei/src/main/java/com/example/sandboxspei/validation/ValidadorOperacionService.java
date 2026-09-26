package com.example.sandboxspei.validation;

import com.example.sandboxspei.dto.EmisorDTO;
import com.example.sandboxspei.dto.ErrorValidacionDTO;
import com.example.sandboxspei.dto.OperacionRequestDTO;
import com.example.sandboxspei.dto.ReceptorDTO;
import com.example.sandboxspei.entity.Institucion;
import com.example.sandboxspei.entity.TipoOperacion;
import com.example.sandboxspei.exception.ValidacionException;
import com.example.sandboxspei.repository.InstitucionRepository;
import com.example.sandboxspei.repository.OperacionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Motor de validación sintáctica y condicional (reglas V01 a V19). Acumula
 * <b>todos</b> los errores encontrados antes de fallar, para poder
 * devolverlos en una sola respuesta HTTP 422. Ninguna de estas reglas
 * provoca escritura en base de datos.
 */
@Service
public class ValidadorOperacionService {

    private final ClabeValidator clabeValidator;
    private final InstitucionRepository institucionRepository;
    private final OperacionRepository operacionRepository;

    public ValidadorOperacionService(ClabeValidator clabeValidator,
                                      InstitucionRepository institucionRepository,
                                      OperacionRepository operacionRepository) {
        this.clabeValidator = clabeValidator;
        this.institucionRepository = institucionRepository;
        this.operacionRepository = operacionRepository;
    }

    /**
     * Valida la petición completa y lanza {@link ValidacionException} con
     * la lista completa de errores si alguna regla V01-V19 se incumple.
     */
    public void validar(OperacionRequestDTO request) {
        List<ErrorValidacionDTO> errores = new ArrayList<>();

        // V13: tipoOperacion válido. Si es inválido, no podemos evaluar las
        // reglas condicionadas por tipo, pero sí el resto.
        TipoOperacion tipoOperacion = null;
        if (request.tipoOperacion() == null
                || !(request.tipoOperacion().equals("T2T") || request.tipoOperacion().equals("VNT"))) {
            errores.add(new ErrorValidacionDTO("PRX-031", "tipoOperacion",
                    "El tipo de operación debe ser 'T2T' o 'VNT'"));
        } else {
            tipoOperacion = TipoOperacion.valueOf(request.tipoOperacion());
        }

        EmisorDTO emisor = request.emisor();
        ReceptorDTO receptor = request.receptor();

        validarReceptor(receptor, errores);
        validarEmisorComun(emisor, errores);
        validarImporte(request, errores);
        validarConcepto(request, errores);
        validarFolio(request, errores);
        validarReferencia(request, errores);

        if (tipoOperacion != null) {
            validarReglasCondicionadas(tipoOperacion, emisor, receptor, errores);
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores, request.referenciaSeguimiento());
        }
    }

    private void validarReceptor(ReceptorDTO receptor, List<ErrorValidacionDTO> errores) {
        if (receptor == null) {
            errores.add(new ErrorValidacionDTO("PRX-011", "receptor", "El receptor es obligatorio"));
            return;
        }
        // V01/V02/V03: CLABE de receptor
        validarClabe(receptor.cuenta(), "receptor.cuenta", errores);
        // V04/V05: institución de receptor
        validarInstitucionYCorrespondencia(receptor.institucion(), receptor.cuenta(),
                "receptor.institucion", "receptor.cuenta", errores);
        // Nombre común
        validarNombre(receptor.nombre(), "receptor.nombre", errores);
    }

    private void validarEmisorComun(EmisorDTO emisor, List<ErrorValidacionDTO> errores) {
        if (emisor == null) {
            errores.add(new ErrorValidacionDTO("PRX-011", "emisor", "El emisor es obligatorio"));
            return;
        }
        validarNombre(emisor.nombre(), "emisor.nombre", errores);
    }

    private void validarNombre(String nombre, String campo, List<ErrorValidacionDTO> errores) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 40) {
            errores.add(new ErrorValidacionDTO("PRX-011", campo,
                    "El campo '" + campo + "' es obligatorio y debe tener entre 1 y 40 caracteres"));
        }
    }

    private void validarClabe(String cuenta, String campo, List<ErrorValidacionDTO> errores) {
        if (cuenta == null) {
            // La obligatoriedad de la cuenta se valida en las reglas condicionadas
            // por tipo de operación; aquí solo se valida formato si viene presente.
            return;
        }
        if (!clabeValidator.esFormatoValido(cuenta)) {
            errores.add(new ErrorValidacionDTO("PRX-001", campo,
                    "La CLABE debe contener exactamente 18 dígitos numéricos"));
            return;
        }
        if (!clabeValidator.esClabeValida(cuenta)) {
            errores.add(new ErrorValidacionDTO("PRX-002", campo,
                    "El dígito verificador de la CLABE no corresponde"));
        }
    }

    private void validarInstitucionYCorrespondencia(String institucion, String cuenta,
                                                      String campoInstitucion, String campoCuenta,
                                                      List<ErrorValidacionDTO> errores) {
        // V04 (PRX-003): la institución debe existir en el catálogo persistido (tabla `instituciones`).
        Optional<Institucion> institucionEncontrada = institucion != null
                ? institucionRepository.findById(institucion)
                : Optional.empty();

        if (institucionEncontrada.isEmpty()) {
            errores.add(new ErrorValidacionDTO("PRX-003", campoInstitucion,
                    "La institución declarada no existe en el catálogo (801-805)"));
            return;
        }
        // V05 (PRX-030): solo se puede comparar si la CLABE tiene formato válido.
        if (cuenta != null && clabeValidator.esFormatoValido(cuenta)) {
            String institucionEnClabe = clabeValidator.extraerInstitucion(cuenta);
            if (!institucionEnClabe.equals(institucion)) {
                errores.add(new ErrorValidacionDTO("PRX-030", campoCuenta,
                        "Los primeros 3 dígitos de la CLABE no coinciden con la institución declarada"));
            }
        }
    }

    private void validarImporte(OperacionRequestDTO request, List<ErrorValidacionDTO> errores) {
        if (request.importe() == null || request.importe().valor() == null) {
            errores.add(new ErrorValidacionDTO("PRX-004", "importe.valor", "El importe es obligatorio y debe ser mayor a 0"));
            return;
        }
        BigDecimal valor = request.importe().valor();
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add(new ErrorValidacionDTO("PRX-004", "importe.valor", "El importe debe ser mayor a 0"));
        }
        boolean excedeMaximo = valor.compareTo(new BigDecimal("1000000.00")) > 0;
        boolean masDeDosDecimales = valor.scale() > 2;
        if (excedeMaximo || masDeDosDecimales) {
            errores.add(new ErrorValidacionDTO("PRX-005", "importe.valor",
                    "El importe debe ser menor o igual a 1,000,000.00 y tener máximo 2 decimales"));
        }
        String divisa = request.importe().divisa();
        if (divisa == null || !divisa.equals("MXN")) {
            errores.add(new ErrorValidacionDTO("PRX-006", "importe.divisa", "La divisa debe ser estrictamente 'MXN'"));
        }
    }

    private void validarConcepto(OperacionRequestDTO request, List<ErrorValidacionDTO> errores) {
        String concepto = request.concepto();
        if (concepto == null || concepto.isEmpty() || concepto.length() > 40) {
            errores.add(new ErrorValidacionDTO("PRX-007", "concepto", "El concepto debe tener entre 1 y 40 caracteres"));
        }
    }

    private void validarFolio(OperacionRequestDTO request, List<ErrorValidacionDTO> errores) {
        Integer folio = request.folioNumerico();
        if (folio == null || folio < 1 || folio > 9_999_999) {
            errores.add(new ErrorValidacionDTO("PRX-008", "folioNumerico",
                    "El folio numérico debe ser un entero entre 1 y 9,999,999"));
        }
    }

    private void validarReferencia(OperacionRequestDTO request, List<ErrorValidacionDTO> errores) {
        String referencia = request.referenciaSeguimiento();
        if (referencia == null || referencia.isEmpty() || referencia.length() > 30
                || !referencia.matches("[A-Za-z0-9]+")) {
            errores.add(new ErrorValidacionDTO("PRX-009", "referenciaSeguimiento",
                    "La referencia de seguimiento debe ser alfanumérica de 1 a 30 caracteres"));
            return;
        }
        if (operacionRepository.existsByReferenciaSeguimiento(referencia)) {
            errores.add(new ErrorValidacionDTO("PRX-010", "referenciaSeguimiento",
                    "La referencia de seguimiento ya fue registrada previamente"));
        }
    }

    private void validarReglasCondicionadas(TipoOperacion tipoOperacion, EmisorDTO emisor, ReceptorDTO receptor,
                                             List<ErrorValidacionDTO> errores) {
        if (emisor == null) {
            return; // ya reportado en validarEmisorComun
        }
        if (tipoOperacion == TipoOperacion.T2T) {
            if (emisor.cuenta() == null || emisor.cuenta().isBlank()) {
                errores.add(new ErrorValidacionDTO("PRX-011", "emisor.cuenta",
                        "emisor.cuenta es obligatoria para operaciones T2T"));
            } else {
                validarClabe(emisor.cuenta(), "emisor.cuenta", errores);
                validarInstitucionYCorrespondencia(emisor.institucion(), emisor.cuenta(),
                        "emisor.institucion", "emisor.cuenta", errores);
                boolean noPuedeEmitir = institucionRepository.findById(emisor.institucion())
                        .map(inst -> !inst.isPuedeEmitir())
                        .orElse(false);
                if (noPuedeEmitir) {
                    errores.add(new ErrorValidacionDTO("PRX-012", "emisor.institucion",
                            "La institución emisora declarada no está autorizada para emitir operaciones"));
                }
                if (receptor != null && receptor.cuenta() != null && emisor.cuenta().equals(receptor.cuenta())) {
                    errores.add(new ErrorValidacionDTO("PRX-013", "emisor.cuenta",
                            "emisor.cuenta debe ser distinta de receptor.cuenta en operaciones T2T"));
                }
            }
            if (emisor.sucursal() != null && !emisor.sucursal().isBlank()) {
                errores.add(new ErrorValidacionDTO("PRX-012", "emisor.sucursal",
                        "emisor.sucursal está prohibido para operaciones T2T"));
            }
            if (emisor.documentoIdentidad() != null) {
                errores.add(new ErrorValidacionDTO("PRX-012", "emisor.documentoIdentidad",
                        "emisor.documentoIdentidad está prohibido para operaciones T2T"));
            }
        } else if (tipoOperacion == TipoOperacion.VNT) {
            if (emisor.cuenta() != null && !emisor.cuenta().isBlank()) {
                errores.add(new ErrorValidacionDTO("PRX-012", "emisor.cuenta",
                        "emisor.cuenta está prohibida para operaciones VNT"));
            }
            if (emisor.sucursal() == null || emisor.sucursal().isBlank()) {
                errores.add(new ErrorValidacionDTO("PRX-011", "emisor.sucursal",
                        "emisor.sucursal es obligatoria para operaciones VNT"));
            }
            if (emisor.documentoIdentidad() == null
                    || emisor.documentoIdentidad().tipo() == null || emisor.documentoIdentidad().tipo().isBlank()
                    || emisor.documentoIdentidad().numero() == null || emisor.documentoIdentidad().numero().isBlank()) {
                errores.add(new ErrorValidacionDTO("PRX-011", "emisor.documentoIdentidad",
                        "emisor.documentoIdentidad (tipo y numero) es obligatorio para operaciones VNT"));
            }
        }
    }
}
