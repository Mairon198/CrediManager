package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Persona que solicita préstamos.
 * <p>Tabla: {@code cliente}.
 *
 * @author Integrante 1
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idCliente;
    /** Número de identidad (13 dígitos). */
    private String dni;
    /** Nombres. */
    private String nombres;
    /** Apellidos. */
    private String apellidos;
    /** Teléfono. */
    private String telefono;
    /** Dirección. */
    private String direccion;
    /** Correo electrónico. */
    private String correo;
    /** Ocupación o lugar de trabajo. */
    private String ocupacion;
    /** Ingreso mensual en lempiras. */
    private BigDecimal ingresoMensual;

    public Cliente() {
    }

    public Cliente(int idCliente, String dni, String nombres, String apellidos, String telefono, String direccion, String correo, String ocupacion, BigDecimal ingresoMensual) {
        this.idCliente = idCliente;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.direccion = direccion;
        this.correo = correo;
        this.ocupacion = ocupacion;
        this.ingresoMensual = ingresoMensual;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public BigDecimal getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(BigDecimal ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    /** @return nombres y apellidos juntos. */
    public String getNombreCompleto() {
        return (nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos);
    }

    @Override
    public String toString() {
        return "Cliente{" + "idCliente=" + idCliente + ", " + "dni=" + dni + ", " + "nombres=" + nombres + ", " + "apellidos=" + apellidos + ", " + "telefono=" + telefono + ", " + "direccion=" + direccion + ", " + "correo=" + correo + ", " + "ocupacion=" + ocupacion + ", " + "ingresoMensual=" + ingresoMensual + "}";
    }
}
