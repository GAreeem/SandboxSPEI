package com.example.sandboxspei.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad raíz que representa una instrucción de pago SPEI simulada dentro
 * del sandbox. El identificador primario es un ULID con prefijo {@code op_}.
 */
@Entity
@Table(name = "operaciones", indexes = {
        @Index(name = "idx_operaciones_referencia", columnList = "referenciaSeguimiento", unique = true),
        @Index(name = "idx_operaciones_fecha_registro", columnList = "fechaRegistro")
})
public class Operacion {

    @Id
    @Column(length = 40)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private TipoOperacion tipoOperacion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "institucion", column = @Column(name = "emisor_institucion", length = 3)),
            @AttributeOverride(name = "cuenta", column = @Column(name = "emisor_cuenta", length = 18)),
            @AttributeOverride(name = "nombre", column = @Column(name = "emisor_nombre", length = 40)),
            @AttributeOverride(name = "sucursal", column = @Column(name = "emisor_sucursal", length = 40))
    })
    private ParteOperacion emisor;

    @Embedded
    private DocumentoIdentidad emisorDocumentoIdentidad;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "institucion", column = @Column(name = "receptor_institucion", length = 3)),
            @AttributeOverride(name = "cuenta", column = @Column(name = "receptor_cuenta", length = 18)),
            @AttributeOverride(name = "nombre", column = @Column(name = "receptor_nombre", length = 40)),
            @AttributeOverride(name = "sucursal", column = @Column(name = "receptor_sucursal", length = 40))
    })
    private ParteOperacion receptor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importeValor;

    @Column(nullable = false, length = 3)
    private String importeDivisa;

    @Column(nullable = false, length = 40)
    private String concepto;

    @Column(nullable = false)
    private Integer folioNumerico;

    @Column(nullable = false, unique = true, length = 30)
    private String referenciaSeguimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado;

    @Column(length = 3)
    private String escenarioResuelto;

    @Column(nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(nullable = false)
    private OffsetDateTime fechaActualizacion;

    @OneToMany(mappedBy = "operacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("momento ASC")
    private List<TransicionEstado> transiciones = new ArrayList<>();

    public Operacion() {
    }

    // ---- Getters y setters explícitos (sin Lombok) ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public ParteOperacion getEmisor() {
        return emisor;
    }

    public void setEmisor(ParteOperacion emisor) {
        this.emisor = emisor;
    }

    public DocumentoIdentidad getEmisorDocumentoIdentidad() {
        return emisorDocumentoIdentidad;
    }

    public void setEmisorDocumentoIdentidad(DocumentoIdentidad emisorDocumentoIdentidad) {
        this.emisorDocumentoIdentidad = emisorDocumentoIdentidad;
    }

    public ParteOperacion getReceptor() {
        return receptor;
    }

    public void setReceptor(ParteOperacion receptor) {
        this.receptor = receptor;
    }

    public BigDecimal getImporteValor() {
        return importeValor;
    }

    public void setImporteValor(BigDecimal importeValor) {
        this.importeValor = importeValor;
    }

    public String getImporteDivisa() {
        return importeDivisa;
    }

    public void setImporteDivisa(String importeDivisa) {
        this.importeDivisa = importeDivisa;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Integer getFolioNumerico() {
        return folioNumerico;
    }

    public void setFolioNumerico(Integer folioNumerico) {
        this.folioNumerico = folioNumerico;
    }

    public String getReferenciaSeguimiento() {
        return referenciaSeguimiento;
    }

    public void setReferenciaSeguimiento(String referenciaSeguimiento) {
        this.referenciaSeguimiento = referenciaSeguimiento;
    }

    public EstadoOperacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoOperacion estado) {
        this.estado = estado;
    }

    public String getEscenarioResuelto() {
        return escenarioResuelto;
    }

    public void setEscenarioResuelto(String escenarioResuelto) {
        this.escenarioResuelto = escenarioResuelto;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<TransicionEstado> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(List<TransicionEstado> transiciones) {
        this.transiciones = transiciones;
    }

    /**
     * Agrega una transición de estado a la operación, manteniendo la
     * relación bidireccional y actualizando el estado vigente.
     */
    public void agregarTransicion(EstadoOperacion nuevoEstado, String motivo) {
        TransicionEstado transicion = new TransicionEstado();
        transicion.setOperacion(this);
        transicion.setEstado(nuevoEstado);
        transicion.setMomento(OffsetDateTime.now());
        transicion.setMotivo(motivo);
        this.transiciones.add(transicion);
        this.estado = nuevoEstado;
        this.fechaActualizacion = transicion.getMomento();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operacion operacion)) return false;
        return java.util.Objects.equals(id, operacion.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Operacion{id='" + id + "', tipoOperacion=" + tipoOperacion + ", estado=" + estado
                + ", referenciaSeguimiento='" + referenciaSeguimiento + "'}";
    }
}
