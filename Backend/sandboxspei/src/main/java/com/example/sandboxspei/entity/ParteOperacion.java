package com.example.sandboxspei.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Representa los datos de una parte de la operación (emisor o receptor).
 * Se reutiliza vía {@code @AttributeOverrides} en la entidad {@link Operacion}.
 */
@Embeddable
public class ParteOperacion {

    @Column(length = 3)
    private String institucion;

    @Column(length = 18)
    private String cuenta;

    @Column(length = 40)
    private String nombre;

    @Column(length = 40)
    private String sucursal;

    public ParteOperacion() {
    }

    public ParteOperacion(String institucion, String cuenta, String nombre, String sucursal) {
        this.institucion = institucion;
        this.cuenta = cuenta;
        this.nombre = nombre;
        this.sucursal = sucursal;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParteOperacion that)) return false;
        return java.util.Objects.equals(institucion, that.institucion)
                && java.util.Objects.equals(cuenta, that.cuenta)
                && java.util.Objects.equals(nombre, that.nombre)
                && java.util.Objects.equals(sucursal, that.sucursal);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(institucion, cuenta, nombre, sucursal);
    }

    @Override
    public String toString() {
        return "ParteOperacion{institucion='" + institucion + "', cuenta='" + cuenta
                + "', nombre='" + nombre + "', sucursal='" + sucursal + "'}";
    }
}
