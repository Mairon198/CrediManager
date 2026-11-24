package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Tipo de préstamo que ofrece la empresa, con sus condiciones.
 * <p>Tabla: {@code tipo_prestamo}.
 *
 * @author Integrante 1
 */
public class TipoPrestamo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idTipo;
    /** Nombre (ej. Personal, Vehicular). */
    private String nombre;
    /** Tasa de interés anual en porcentaje (ej. 18.00). */
    private BigDecimal tasaInteresAnual;
    /** Plazo máximo en meses. */
    private int plazoMaxMeses;
    /** Monto máximo permitido. */
    private BigDecimal montoMax;
    /** Porcentaje de mora por cuota atrasada. */
    private BigDecimal porcentajeMora;

    public TipoPrestamo() {
    }

    public TipoPrestamo(int idTipo, String nombre, BigDecimal tasaInteresAnual, int plazoMaxMeses, BigDecimal montoMax, BigDecimal porcentajeMora) {
        this.idTipo = idTipo;
        this.nombre = nombre;
        this.tasaInteresAnual = tasaInteresAnual;
        this.plazoMaxMeses = plazoMaxMeses;
        this.montoMax = montoMax;
        this.porcentajeMora = porcentajeMora;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(BigDecimal tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public int getPlazoMaxMeses() {
        return plazoMaxMeses;
    }

    public void setPlazoMaxMeses(int plazoMaxMeses) {
        this.plazoMaxMeses = plazoMaxMeses;
    }

    public BigDecimal getMontoMax() {
        return montoMax;
    }

    public void setMontoMax(BigDecimal montoMax) {
        this.montoMax = montoMax;
    }

    public BigDecimal getPorcentajeMora() {
        return porcentajeMora;
    }

    public void setPorcentajeMora(BigDecimal porcentajeMora) {
        this.porcentajeMora = porcentajeMora;
    }

    @Override
    public String toString() {
        return "TipoPrestamo{" + "idTipo=" + idTipo + ", " + "nombre=" + nombre + ", " + "tasaInteresAnual=" + tasaInteresAnual + ", " + "plazoMaxMeses=" + plazoMaxMeses + ", " + "montoMax=" + montoMax + ", " + "porcentajeMora=" + porcentajeMora + "}";
    }
}
