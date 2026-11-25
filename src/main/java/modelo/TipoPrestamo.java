package modelo;

import java.math.BigDecimal;

/** Tipo de préstamo que ofrece la empresa y sus condiciones (dbo.TiposPrestamo). */
public class TipoPrestamo extends EntidadBase {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String descripcion;
    /** Tasa de interés anual en porcentaje (ej. 18.00). */
    private BigDecimal tasaInteresAnual = BigDecimal.ZERO;
    private BigDecimal montoMinimo = BigDecimal.ZERO;
    private BigDecimal montoMaximo = BigDecimal.ZERO;
    private int plazoMinimoMeses;
    private int plazoMaximoMeses;
    /** Porcentaje mensual de mora sobre la cuota atrasada, prorrateado por día. */
    private BigDecimal porcentajeMora = BigDecimal.ZERO;
    /** Si es true, el préstamo necesita una garantía o un fiador para aprobarse. */
    private boolean requiereGarantia;

    public TipoPrestamo() {
    }

    public TipoPrestamo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(BigDecimal tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public BigDecimal getMontoMinimo() {
        return montoMinimo;
    }

    public void setMontoMinimo(BigDecimal montoMinimo) {
        this.montoMinimo = montoMinimo;
    }

    public BigDecimal getMontoMaximo() {
        return montoMaximo;
    }

    public void setMontoMaximo(BigDecimal montoMaximo) {
        this.montoMaximo = montoMaximo;
    }

    public int getPlazoMinimoMeses() {
        return plazoMinimoMeses;
    }

    public void setPlazoMinimoMeses(int plazoMinimoMeses) {
        this.plazoMinimoMeses = plazoMinimoMeses;
    }

    public int getPlazoMaximoMeses() {
        return plazoMaximoMeses;
    }

    public void setPlazoMaximoMeses(int plazoMaximoMeses) {
        this.plazoMaximoMeses = plazoMaximoMeses;
    }

    public BigDecimal getPorcentajeMora() {
        return porcentajeMora;
    }

    public void setPorcentajeMora(BigDecimal porcentajeMora) {
        this.porcentajeMora = porcentajeMora;
    }

    public boolean isRequiereGarantia() {
        return requiereGarantia;
    }

    public void setRequiereGarantia(boolean requiereGarantia) {
        this.requiereGarantia = requiereGarantia;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
