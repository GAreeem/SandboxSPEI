package com.example.sandboxspei.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Documento de identidad del emisor, requerido únicamente para operaciones
 * de tipo VNT (ventanilla).
 */
@Embeddable
public class DocumentoIdentidad {

    @Column(name = "emisor_doc_tipo")
    private String tipo;

    @Column(name = "emisor_doc_numero")
    private String numero;

    public DocumentoIdentidad() {
    }

    public DocumentoIdentidad(String tipo, String numero) {
        this.tipo = tipo;
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentoIdentidad that)) return false;
        return java.util.Objects.equals(tipo, that.tipo) && java.util.Objects.equals(numero, that.numero);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(tipo, numero);
    }

    @Override
    public String toString() {
        return "DocumentoIdentidad{tipo='" + tipo + "', numero='" + numero + "'}";
    }
}
