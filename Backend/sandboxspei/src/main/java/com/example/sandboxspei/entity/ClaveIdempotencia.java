package com.example.sandboxspei.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de una clave de idempotencia recibida en el encabezado
 * {@code Clave-Idempotencia} de {@code POST /operaciones}.
 *
 * <p>Tabla {@code claves_idempotencia}: la clave (UUID enviado por el
 * cliente) es la llave primaria, se guarda el hash SHA-256 del cuerpo con el
 * que se usó por primera vez y una llave foránea a la operación creada.</p>
 *
 * <p>Implementa {@link Persistable} a propósito: como la llave primaria es
 * asignada por el cliente (no generada), sin esto Spring Data trataría
 * {@code save()} como un {@code merge} (SELECT + INSERT/UPDATE) y un
 * duplicado concurrente sobrescribiría el registro existente en lugar de
 * fallar. Con {@code isNew() == true} se hace un {@code persist} (INSERT) y
 * la llave primaria de MySQL impide duplicados.</p>
 */
@Entity
@Table(name = "claves_idempotencia")
public class ClaveIdempotencia implements Persistable<String> {

    @Id
    @Column(name = "clave", length = 100)
    private String clave;

    @Column(name = "hash_cuerpo", nullable = false, length = 64)
    private String hashCuerpo;

    /**
     * FK a {@code operaciones.id}. Se declara VARCHAR(50) explícitamente
     * (la columna referenciada es VARCHAR(40); MySQL permite longitudes
     * distintas en llaves foráneas de tipo cadena).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operacion_id", nullable = false, columnDefinition = "VARCHAR(50)",
            foreignKey = @ForeignKey(name = "fk_claves_idempotencia_operacion"))
    private Operacion operacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /** Bandera no persistida que le indica a Spring Data si el registro es nuevo. */
    @Transient
    private boolean nueva = true;

    protected ClaveIdempotencia() {
    }

    public ClaveIdempotencia(String clave, String hashCuerpo, Operacion operacion, LocalDateTime fechaRegistro) {
        this.clave = clave;
        this.hashCuerpo = hashCuerpo;
        this.operacion = operacion;
        this.fechaRegistro = fechaRegistro;
    }

    @PostLoad
    @PostPersist
    void marcarComoExistente() {
        this.nueva = false;
    }

    // ---- Persistable ----

    @Override
    public String getId() {
        return clave;
    }

    @Override
    public boolean isNew() {
        return nueva;
    }

    // ---- Getters y setters explícitos (sin Lombok) ----

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

    public Operacion getOperacion() {
        return operacion;
    }

    public void setOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    /** Id de la operación asociada (no inicializa el proxy lazy). */
    public String getOperacionId() {
        return operacion != null ? operacion.getId() : null;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaveIdempotencia that)) return false;
        return Objects.equals(clave, that.clave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clave);
    }

    @Override
    public String toString() {
        return "ClaveIdempotencia{clave='" + clave + "', operacionId='" + getOperacionId() + "'}";
    }
}
