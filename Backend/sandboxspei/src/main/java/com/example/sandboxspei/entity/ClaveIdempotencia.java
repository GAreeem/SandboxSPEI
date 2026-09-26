package com.example.sandboxspei.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Registro de las claves de idempotencia recibidas en el encabezado
 * {@code Clave-Idempotencia} de {@code POST /operaciones}. Guarda el hash
 * del cuerpo original para detectar reutilización con payload distinto.
 */
@Entity
@Table(name = "claves_idempotencia")
public class ClaveIdempotencia {

    @Id
    @Column(length = 36)
    private String clave;

    @Column(nullable = false, length = 64)
    private String hashCuerpo;

    @Column(nullable = false, length = 40)
    private String operacionId;

    @Lob
    @Column(nullable = false)
    private String cuerpoOriginal;

    @Column(nullable = false)
    private OffsetDateTime fechaCreacion;

    public ClaveIdempotencia() {
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getHashCuerpo() {
        return hashCuerpo;
    }

    public void setHashCuerpo(String hashCuerpo) {
        this.hashCuerpo = hashCuerpo;
    }

    public String getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(String operacionId) {
        this.operacionId = operacionId;
    }

    public String getCuerpoOriginal() {
        return cuerpoOriginal;
    }

    public void setCuerpoOriginal(String cuerpoOriginal) {
        this.cuerpoOriginal = cuerpoOriginal;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaveIdempotencia that)) return false;
        return java.util.Objects.equals(clave, that.clave);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(clave);
    }

    @Override
    public String toString() {
        return "ClaveIdempotencia{clave='" + clave + "', operacionId='" + operacionId + "'}";
    }
}
