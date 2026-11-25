package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.enums.EstadoPrestamo;

/** Préstamo solicitado u otorgado a un cliente (dbo.Prestamos y vista vw_PrestamosResumen). */
public class Prestamo extends EntidadBase {

    private static final long serialVersionUID = 1L;

    /** Número visible, generado por la BD (PRE-000001). */
    private String numeroPrestamo;
    private Cliente cliente;
    private TipoPrestamo tipoPrestamo;
    /** Usuario que registró la solicitud. */
    private int usuarioId;
    private String registradoPor;
    private BigDecimal monto = BigDecimal.ZERO;
    private int plazoMeses;
    /** Tasa pactada (copiada del tipo al registrar la solicitud). */
    private BigDecimal tasaInteresAnual = BigDecimal.ZERO;
    private BigDecimal porcentajeMora = BigDecimal.ZERO;
    /** Se calcula al aprobar; null mientras está SOLICITADO. */
    private BigDecimal cuotaMensual;
    /** Para qué se usará el dinero. */
    private String destino;
    private EstadoPrestamo estado = EstadoPrestamo.SOLICITADO;
    private LocalDate fechaSolicitud;
    private LocalDate fechaAprobacion;
    private LocalDate fechaDesembolso;
    private String observaciones;
    /** Resumen calculado por la BD (fn_ResumenPrestamo). */
    private BigDecimal totalAPagar = BigDecimal.ZERO;
    private BigDecimal totalPagado = BigDecimal.ZERO;
    private BigDecimal moraPagada = BigDecimal.ZERO;
    private BigDecimal capitalPendiente = BigDecimal.ZERO;
    private BigDecimal saldoPendiente = BigDecimal.ZERO;
    private int cuotasPagadas;
    private int cuotasPendientes;
    private int cuotasVencidas;
    private LocalDate proximoVencimiento;

    public Prestamo() {
    }

    public String getNumeroPrestamo() {
        return numeroPrestamo;
    }

    public void setNumeroPrestamo(String numeroPrestamo) {
        this.numeroPrestamo = numeroPrestamo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoPrestamo getTipoPrestamo() {
        return tipoPrestamo;
    }

    public void setTipoPrestamo(TipoPrestamo tipoPrestamo) {
        this.tipoPrestamo = tipoPrestamo;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
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

    public BigDecimal getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(BigDecimal tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public BigDecimal getPorcentajeMora() {
        return porcentajeMora;
    }

    public void setPorcentajeMora(BigDecimal porcentajeMora) {
        this.porcentajeMora = porcentajeMora;
    }

    public BigDecimal getCuotaMensual() {
        return cuotaMensual;
    }

    public void setCuotaMensual(BigDecimal cuotaMensual) {
        this.cuotaMensual = cuotaMensual;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
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

    public LocalDate getFechaDesembolso() {
        return fechaDesembolso;
    }

    public void setFechaDesembolso(LocalDate fechaDesembolso) {
        this.fechaDesembolso = fechaDesembolso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getTotalAPagar() {
        return totalAPagar;
    }

    public void setTotalAPagar(BigDecimal totalAPagar) {
        this.totalAPagar = totalAPagar;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public BigDecimal getMoraPagada() {
        return moraPagada;
    }

    public void setMoraPagada(BigDecimal moraPagada) {
        this.moraPagada = moraPagada;
    }

    public BigDecimal getCapitalPendiente() {
        return capitalPendiente;
    }

    public void setCapitalPendiente(BigDecimal capitalPendiente) {
        this.capitalPendiente = capitalPendiente;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public int getCuotasPagadas() {
        return cuotasPagadas;
    }

    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
    }

    public int getCuotasPendientes() {
        return cuotasPendientes;
    }

    public void setCuotasPendientes(int cuotasPendientes) {
        this.cuotasPendientes = cuotasPendientes;
    }

    public int getCuotasVencidas() {
        return cuotasVencidas;
    }

    public void setCuotasVencidas(int cuotasVencidas) {
        this.cuotasVencidas = cuotasVencidas;
    }

    public LocalDate getProximoVencimiento() {
        return proximoVencimiento;
    }

    public void setProximoVencimiento(LocalDate proximoVencimiento) {
        this.proximoVencimiento = proximoVencimiento;
    }

    @Override
    public String toString() {
        return numeroPrestamo == null ? "Préstamo nuevo" : numeroPrestamo;
    }
}
