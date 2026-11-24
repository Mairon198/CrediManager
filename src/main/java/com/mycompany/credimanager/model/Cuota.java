package com.mycompany.credimanager.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una cuota del plan de pagos de un préstamo.
 * <p>Tabla: {@code cuota}.
 *
 * @author Integrante 1
 */
public class Cuota implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idCuota;
    /** Préstamo al que pertenece. */
    private int idPrestamo;
    /** Número de cuota (1, 2, 3...). */
    private int numero;
    /** Fecha límite de pago. */
    private LocalDate fechaVencimiento;
    /** Parte de capital de la cuota. */
    private BigDecimal capital;
    /** Parte de interés de la cuota. */
    private BigDecimal interes;
    /** Capital + interés. */
    private BigDecimal montoTotal;
    /** Saldo del préstamo después de pagar esta cuota. */
    private BigDecimal saldo;
    /** Estado; ver Constantes.CUOTA_*. */
    private String estado;

    public Cuota() {
    }

    public Cuota(int idCuota, int idPrestamo, int numero, LocalDate fechaVencimiento, BigDecimal capital, BigDecimal interes, BigDecimal montoTotal, BigDecimal saldo, String estado) {
        this.idCuota = idCuota;
        this.idPrestamo = idPrestamo;
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.capital = capital;
        this.interes = interes;
        this.montoTotal = montoTotal;
        this.saldo = saldo;
        this.estado = estado;
    }

    public int getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(int idCuota) {
        this.idCuota = idCuota;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getInteres() {
        return interes;
    }

    public void setInteres(BigDecimal interes) {
        this.interes = interes;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Cuota{" + "idCuota=" + idCuota + ", " + "idPrestamo=" + idPrestamo + ", " + "numero=" + numero + ", " + "fechaVencimiento=" + fechaVencimiento + ", " + "capital=" + capital + ", " + "interes=" + interes + ", " + "montoTotal=" + montoTotal + ", " + "saldo=" + saldo + ", " + "estado=" + estado + "}";
    }
}
