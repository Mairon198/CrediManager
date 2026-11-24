package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Préstamo otorgado (o solicitado) a un cliente.
 * <p>Tabla: {@code prestamo}.
 *
 * @author Integrante 1
 */
public class Prestamo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idPrestamo;
    /** Cliente que lo solicita. */
    private int idCliente;
    /** Tipo de préstamo. */
    private int idTipo;
    /** Usuario que lo registró. */
    private int idUsuario;
    /** Monto prestado. */
    private BigDecimal monto;
    /** Número de cuotas mensuales. */
    private int plazoMeses;
    /** Tasa de interés anual aplicada, en porcentaje. */
    private BigDecimal tasa;
    /** Cuota mensual fija (se calcula al aprobar). */
    private BigDecimal cuota;
    /** Fecha de la solicitud. */
    private LocalDate fechaSolicitud;
    /** Fecha de aprobación o desembolso (null si no se ha aprobado). */
    private LocalDate fechaAprobacion;
    /** Estado; ver Constantes.PRESTAMO_*. */
    private String estado;

    // ----- Datos de apoyo para mostrar en listas (no son columnas de esta tabla) -----
    /** Nombre completo del cliente (JOIN). */
    private String nombreCliente;
    /** DNI del cliente (JOIN). */
    private String dniCliente;
    /** Nombre del tipo de préstamo (JOIN). */
    private String nombreTipo;
    /** Nombre del usuario que lo registró (JOIN). */
    private String nombreUsuario;

    public Prestamo() {
    }

    public Prestamo(int idPrestamo, int idCliente, int idTipo, int idUsuario, BigDecimal monto, int plazoMeses, BigDecimal tasa, BigDecimal cuota, LocalDate fechaSolicitud, LocalDate fechaAprobacion, String estado) {
        this.idPrestamo = idPrestamo;
        this.idCliente = idCliente;
        this.idTipo = idTipo;
        this.idUsuario = idUsuario;
        this.monto = monto;
        this.plazoMeses = plazoMeses;
        this.tasa = tasa;
        this.cuota = cuota;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaAprobacion = fechaAprobacion;
        this.estado = estado;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(int plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public void setTasa(BigDecimal tasa) {
        this.tasa = tasa;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    public void setCuota(BigDecimal cuota) {
        this.cuota = cuota;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDate fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public String toString() {
        return "Prestamo{" + "idPrestamo=" + idPrestamo + ", " + "idCliente=" + idCliente + ", " + "idTipo=" + idTipo + ", " + "idUsuario=" + idUsuario + ", " + "monto=" + monto + ", " + "plazoMeses=" + plazoMeses + ", " + "tasa=" + tasa + ", " + "cuota=" + cuota + ", " + "fechaSolicitud=" + fechaSolicitud + ", " + "fechaAprobacion=" + fechaAprobacion + ", " + "estado=" + estado + "}";
    }
}
