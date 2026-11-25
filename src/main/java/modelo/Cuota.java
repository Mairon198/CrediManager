package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.enums.EstadoCuota;

/** Cuota del plan de pagos de un préstamo (dbo.Cuotas). Las genera sp_DesembolsarPrestamo. */
public class Cuota extends EntidadBase {

    private static final long serialVersionUID = 1L;

    private int prestamoId;
    private int numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal capital = BigDecimal.ZERO;
    private BigDecimal interes = BigDecimal.ZERO;
    /** Capital + interés. */
    private BigDecimal montoCuota = BigDecimal.ZERO;
    /** Capital que queda después de pagar esta cuota. */
    private BigDecimal saldoRestante = BigDecimal.ZERO;
    private EstadoCuota estado = EstadoCuota.PENDIENTE;
    private LocalDate fechaPago;
    /** Datos de apoyo de la vista vw_CuotasVencidas (cobranza). */
    private String numeroPrestamo;
    private String cliente;
    private String identidadCliente;
    private String telefonoCliente;
    private int diasAtraso;
    private BigDecimal moraAlDia = BigDecimal.ZERO;
    private BigDecimal totalAPagar = BigDecimal.ZERO;

    public Cuota() {
    }

    public int getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(int prestamoId) {
        this.prestamoId = prestamoId;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
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

    public BigDecimal getMontoCuota() {
        return montoCuota;
    }

    public void setMontoCuota(BigDecimal montoCuota) {
        this.montoCuota = montoCuota;
    }

    public BigDecimal getSaldoRestante() {
        return saldoRestante;
    }

    public void setSaldoRestante(BigDecimal saldoRestante) {
        this.saldoRestante = saldoRestante;
    }

    public EstadoCuota getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuota estado) {
        this.estado = estado;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getNumeroPrestamo() {
        return numeroPrestamo;
    }

    public void setNumeroPrestamo(String numeroPrestamo) {
        this.numeroPrestamo = numeroPrestamo;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getIdentidadCliente() {
        return identidadCliente;
    }

    public void setIdentidadCliente(String identidadCliente) {
        this.identidadCliente = identidadCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public BigDecimal getMoraAlDia() {
        return moraAlDia;
    }

    public void setMoraAlDia(BigDecimal moraAlDia) {
        this.moraAlDia = moraAlDia;
    }

    public BigDecimal getTotalAPagar() {
        return totalAPagar;
    }

    public void setTotalAPagar(BigDecimal totalAPagar) {
        this.totalAPagar = totalAPagar;
    }

    @Override
    public String toString() {
        return "Cuota " + numeroCuota;
    }
}
