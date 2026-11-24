package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Bien que respalda un préstamo.
 * <p>Tabla: {@code garantia}.
 *
 * @author Integrante 1
 */
public class Garantia implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idGarantia;
    /** Préstamo que respalda. */
    private int idPrestamo;
    /** Tipo; ver Constantes.GARANTIA_*. */
    private String tipo;
    /** Descripción del bien. */
    private String descripcion;
    /** Valor estimado en lempiras. */
    private BigDecimal valorEstimado;

    public Garantia() {
    }

    public Garantia(int idGarantia, int idPrestamo, String tipo, String descripcion, BigDecimal valorEstimado) {
        this.idGarantia = idGarantia;
        this.idPrestamo = idPrestamo;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.valorEstimado = valorEstimado;
    }

    public int getIdGarantia() {
        return idGarantia;
    }

    public void setIdGarantia(int idGarantia) {
        this.idGarantia = idGarantia;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    @Override
    public String toString() {
        return "Garantia{" + "idGarantia=" + idGarantia + ", " + "idPrestamo=" + idPrestamo + ", " + "tipo=" + tipo + ", " + "descripcion=" + descripcion + ", " + "valorEstimado=" + valorEstimado + "}";
    }
}
