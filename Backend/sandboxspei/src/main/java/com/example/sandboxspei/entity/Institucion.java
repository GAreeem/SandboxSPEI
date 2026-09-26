package com.example.sandboxspei.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Institución participante del sandbox SPEI. Es la fuente de verdad del
 * catálogo de instituciones (códigos de 3 dígitos) usada tanto por el
 * endpoint {@code GET /catalogos/instituciones} como por el motor de
 * validaciones (V04 - PRX-003, V05 - PRX-030, y la restricción de emisión
 * de la institución 804).
 */
@Entity
@Table(name = "instituciones")
public class Institucion {

    /**
     * Código de 3 dígitos de la institución (ej. "801"). Es la llave
     * primaria natural: no requiere un identificador generado aparte.
     */
    @Id
    @Column(length = 3)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private boolean puedeEmitir;

    @Column(length = 255)
    private String descripcionComportamiento;

    public Institucion() {
    }

    public Institucion(String codigo, String nombre, boolean puedeEmitir, String descripcionComportamiento) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.puedeEmitir = puedeEmitir;
        this.descripcionComportamiento = descripcionComportamiento;
    }

    // ---- Getters y setters explícitos (sin Lombok) ----

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isPuedeEmitir() {
        return puedeEmitir;
    }

    public void setPuedeEmitir(boolean puedeEmitir) {
        this.puedeEmitir = puedeEmitir;
    }

    public String getDescripcionComportamiento() {
        return descripcionComportamiento;
    }

    public void setDescripcionComportamiento(String descripcionComportamiento) {
        this.descripcionComportamiento = descripcionComportamiento;
    }

    /**
     * Deriva si la institución está en mantenimiento (escenario S04,
     * PRX-022) a partir de su descripción de comportamiento. Se usa una
     * bandera textual en vez de una columna booleana adicional porque el
     * "mantenimiento" es, en este sandbox, una propiedad narrativa de la
     * institución 805, no un estado operativo que cambie en tiempo real.
     */
    public boolean isEnMantenimiento() {
        return descripcionComportamiento != null
                && descripcionComportamiento.toLowerCase().contains("mantenimiento");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Institucion that)) return false;
        return java.util.Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Institucion{codigo='" + codigo + "', nombre='" + nombre + "', puedeEmitir=" + puedeEmitir + "}";
    }
}
