package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pago registrado por un cajero sobre una cuota.
 * <p>Tabla: {@code pago}.
 *
 * @author Integrante 1
 */
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idPago;
    /** Cuota que se paga. */
    private int idCuota;
    /** Cajero que registró el pago. */
    private int idUsuario;
    /** Fecha del pago. */
    private LocalDate fechaPago;
    /** Monto pagado de la cuota (sin mora). */
    private BigDecimal montoPagado;
    /** Mora cobrada. */
    private BigDecimal mora;
    /** Método; ver Constantes.PAGO_*. */
    private String metodoPago;

    // ----- Datos de apoyo para mostrar en listas (no son columnas de esta tabla) -----
    /** Préstamo de la cuota (JOIN). */
    private int idPrestamo;
    /** Número de la cuota (JOIN). */
    private int numeroCuota;
    /** Cliente que pagó (JOIN). */
    private String nombreCliente;
    /** Cajero que registró (JOIN). */
    private String nombreUsuario;

    public Pago() {
    }

    public Pago(int idPago, int idCuota, int idUsuario, LocalDate fechaPago, BigDecimal montoPagado, BigDecimal mora, String metodoPago) {
        this.idPago = idPago;
        this.idCuota = idCuota;
        this.idUsuario = idUsuario;
        this.fechaPago = fechaPago;
        this.montoPagado = montoPagado;
        this.mora = mora;
        this.metodoPago = metodoPago;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(int idCuota) {
        this.idCuota = idCuota;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public BigDecimal getMora() {
        return mora;
    }

    public void setMora(BigDecimal mora) {
        this.mora = mora;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public String toString() {
        return "Pago{" + "idPago=" + idPago + ", " + "idCuota=" + idCuota + ", " + "idUsuario=" + idUsuario + ", " + "fechaPago=" + fechaPago + ", " + "montoPagado=" + montoPagado + ", " + "mora=" + mora + ", " + "metodoPago=" + metodoPago + "}";
    }
}
