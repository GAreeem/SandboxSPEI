package com.example.sandboxspei.config;

import com.example.sandboxspei.engine.EscenarioResolver;
import com.example.sandboxspei.engine.MaquinaEstados;
import com.example.sandboxspei.engine.ResultadoEscenario;
import com.example.sandboxspei.entity.*;
import com.example.sandboxspei.repository.InstitucionRepository;
import com.example.sandboxspei.repository.OperacionRepository;
import com.example.sandboxspei.validation.ClabeValidator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Precarga 120 operaciones simuladas al arrancar la aplicación (si la
 * tabla está vacía), cubriendo ejemplos de todos los estados finales, para
 * que el historial paginado ({@code GET /operaciones}) no aparezca vacío.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final int TOTAL_SEMILLA = 120;
    private static final List<String> INSTITUCIONES_EMISORAS = List.of("801", "802", "803");
    private static final List<String> SEGMENTOS_ESCENARIO = List.of("0000", "9002", "9003", "9004", "9005", "9006");

    private final OperacionRepository operacionRepository;
    private final InstitucionRepository institucionRepository;
    private final ClabeValidator clabeValidator;
    private final EscenarioResolver escenarioResolver;
    private final MaquinaEstados maquinaEstados;
    private final Random random = new Random(42L);

    public DataInitializer(OperacionRepository operacionRepository,
                            InstitucionRepository institucionRepository,
                            ClabeValidator clabeValidator,
                            EscenarioResolver escenarioResolver,
                            MaquinaEstados maquinaEstados) {
        this.operacionRepository = operacionRepository;
        this.institucionRepository = institucionRepository;
        this.clabeValidator = clabeValidator;
        this.escenarioResolver = escenarioResolver;
        this.maquinaEstados = maquinaEstados;
    }

    @Override
    public void run(String... args) {
        sembrarInstituciones();
        sembrarOperaciones();
    }

    /**
     * Precarga el catálogo de instituciones (tabla {@code instituciones}) si
     * está vacío. Es la fuente de verdad para V04 (PRX-003), V05 (PRX-030)
     * y la restricción de emisión de la institución 804.
     */
    private void sembrarInstituciones() {
        if (institucionRepository.count() > 0) {
            return;
        }
        institucionRepository.saveAll(List.of(
                new Institucion("801", "Banco Praxis Alfa", true, "Operación normal"),
                new Institucion("802", "Banco Praxis Beta", true, "Operación normal"),
                new Institucion("803", "Banco Praxis Gamma", true, "Operación normal"),
                new Institucion("804", "Praxis Servicios de Pago", false, "Institución no bancaria; solo receptor"),
                new Institucion("805", "Banco Praxis Delta", true, "En mantenimiento (devuelve PRX-022)")
        ));
    }

    private void sembrarOperaciones() {
        if (operacionRepository.count() > 0) {
            return;
        }
        for (int i = 1; i <= TOTAL_SEMILLA; i++) {
            operacionRepository.save(construirOperacionSemilla(i));
        }
    }

    private Operacion construirOperacionSemilla(int indice) {
        boolean esT2T = indice % 2 == 0;
        TipoOperacion tipoOperacion = esT2T ? TipoOperacion.T2T : TipoOperacion.VNT;

        String institucionEmisora = INSTITUCIONES_EMISORAS.get(indice % INSTITUCIONES_EMISORAS.size());
        String institucionReceptora = INSTITUCIONES_EMISORAS.get((indice + 1) % INSTITUCIONES_EMISORAS.size());
        String segmentoEscenario = SEGMENTOS_ESCENARIO.get(indice % SEGMENTOS_ESCENARIO.size());

        String cuentaReceptor = generarClabe(institucionReceptora, segmentoEscenario);

        Operacion operacion = new Operacion();
        operacion.setId(generarId());
        operacion.setTipoOperacion(tipoOperacion);

        if (esT2T) {
            String cuentaEmisor = generarClabe(institucionEmisora, "0001");
            operacion.setEmisor(new ParteOperacion(institucionEmisora, cuentaEmisor, "Emisor Semilla " + indice, null));
            operacion.setEmisorDocumentoIdentidad(new DocumentoIdentidad());
        } else {
            operacion.setEmisor(new ParteOperacion(institucionEmisora, null, "Emisor Semilla " + indice, "Sucursal Centro"));
            operacion.setEmisorDocumentoIdentidad(new DocumentoIdentidad("INE", "DOC" + (100000 + indice)));
        }

        operacion.setReceptor(new ParteOperacion(institucionReceptora, cuentaReceptor, "Receptor Semilla " + indice, null));
        operacion.setImporteValor(BigDecimal.valueOf(100 + (indice * 37 % 5000)).setScale(2));
        operacion.setImporteDivisa("MXN");
        operacion.setConcepto("Pago de prueba semilla " + indice);
        operacion.setFolioNumerico(1000 + indice);
        operacion.setReferenciaSeguimiento("SEED" + String.format("%06d", indice));

        OffsetDateTime fechaRegistro = OffsetDateTime.now().minusDays(TOTAL_SEMILLA - indice).minusMinutes(indice);
        operacion.setFechaRegistro(fechaRegistro);
        operacion.setFechaActualizacion(fechaRegistro);

        operacion.agregarTransicion(EstadoOperacion.RECIBIDO, null);

        ResultadoEscenario resultado = escenarioResolver.resolver(cuentaReceptor, institucionReceptora, null);
        operacion.setEscenarioResuelto(resultado.codigoEscenario());

        if (resultado.estadoDestino() == EstadoOperacion.EN_PROCESO) {
            maquinaEstados.transicionar(operacion, EstadoOperacion.EN_PROCESO, resultado.motivo());
        } else {
            maquinaEstados.transicionar(operacion, EstadoOperacion.EN_PROCESO, null);
            maquinaEstados.transicionar(operacion, resultado.estadoDestino(), resultado.motivo());
        }

        return operacion;
    }

    /**
     * Genera una CLABE de 18 dígitos válida: institución (3) + relleno
     * aleatorio (10) + segmento de escenario deseado (4, posiciones 14-17)
     * + dígito verificador calculado.
     */
    private String generarClabe(String institucion, String segmentoEscenario) {
        StringBuilder relleno = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            relleno.append(random.nextInt(10));
        }
        String primeros17 = institucion + relleno + segmentoEscenario;
        int digitoVerificador = clabeValidator.calcularDigitoVerificador(primeros17 + "0");
        return primeros17 + digitoVerificador;
    }

    private String generarId() {
        return "op_" + UUID.randomUUID().toString().replace("-", "");
    }
}
