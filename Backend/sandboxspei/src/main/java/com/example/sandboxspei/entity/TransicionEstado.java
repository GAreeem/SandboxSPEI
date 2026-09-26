package com.example.sandboxspei.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Registro histórico e inmutable de cada cambio de estado sufrido por una
 * {@link Operacion}.
 */
@Entity
@Table(name = "transiciones_estado")
public class TransicionEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operacion_id", nullable = false)
    private Operacion operacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado;

    @Column(nullable = false)
    private OffsetDateTime momento;

    @Column(length = 200)
    private String motivo;

    public TransicionEstado() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public void setOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    public EstadoOperacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoOperacion estado) {
        this.estado = estado;
    }

    public OffsetDateTime getMomento() {
        return momento;
    }

    public void setMomento(OffsetDateTime momento) {
        this.momento = momento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransicionEstado that)) return false;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TransicionEstado{estado=" + estado + ", momento=" + momento + ", motivo='" + motivo + "'}";
    }
}
