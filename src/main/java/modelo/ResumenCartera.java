package modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import modelo.enums.EstadoPrestamo;

/** Fila de la vista vw_CarteraPorEstado (tablero y reportes). */
public class ResumenCartera implements Serializable {

    private static final long serialVersionUID = 1L;

    private EstadoPrestamo estado;
    private int cantidad;
    private BigDecimal montoPrestado = BigDecimal.ZERO;
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getMontoPrestado() {
        return montoPrestado;
    }

    public void setMontoPrestado(BigDecimal montoPrestado) {
        this.montoPrestado = montoPrestado;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

}
