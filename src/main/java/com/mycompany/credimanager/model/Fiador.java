package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Persona que respalda un préstamo.
 * <p>Tabla: {@code fiador}.
 *
 * @author Integrante 1
 */
public class Fiador implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idFiador;
    /** Préstamo que respalda. */
    private int idPrestamo;
    /** Número de identidad. */
    private String dni;
    /** Nombre completo. */
    private String nombres;
    /** Teléfono. */
    private String telefono;
    /** Ingreso mensual en lempiras. */
    private BigDecimal ingresoMensual;

    public Fiador() {
    }

    public Fiador(int idFiador, int idPrestamo, String dni, String nombres, String telefono, BigDecimal ingresoMensual) {
        this.idFiador = idFiador;
        this.idPrestamo = idPrestamo;
        this.dni = dni;
        this.nombres = nombres;
        this.telefono = telefono;
        this.ingresoMensual = ingresoMensual;
    }

    public int getIdFiador() {
        return idFiador;
    }

    public void setIdFiador(int idFiador) {
        this.idFiador = idFiador;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public BigDecimal getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(BigDecimal ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    @Override
    public String toString() {
        return "Fiador{" + "idFiador=" + idFiador + ", " + "idPrestamo=" + idPrestamo + ", " + "dni=" + dni + ", " + "nombres=" + nombres + ", " + "telefono=" + telefono + ", " + "ingresoMensual=" + ingresoMensual + "}";
    }
}
