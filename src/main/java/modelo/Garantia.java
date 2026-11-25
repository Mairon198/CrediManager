package modelo;

import java.math.BigDecimal;
import modelo.enums.TipoGarantia;

/** Bien que respalda un préstamo (dbo.Garantias). */
public class Garantia extends EntidadBase {

    private static final long serialVersionUID = 1L;

    private int prestamoId;
    private TipoGarantia tipo;
    private String descripcion;
    private BigDecimal valorEstimado = BigDecimal.ZERO;

    public Garantia() {
    }

    public Garantia(int prestamoId, TipoGarantia tipo, String descripcion, BigDecimal valorEstimado) {
        this.prestamoId = prestamoId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.valorEstimado = valorEstimado;
    }

    public int getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(int prestamoId) {
        this.prestamoId = prestamoId;
    }

    public TipoGarantia getTipo() {
        return tipo;
    }

    public void setTipo(TipoGarantia tipo) {
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
        return tipo + ": " + descripcion;
    }
}
