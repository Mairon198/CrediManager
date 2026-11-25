package modelo;

import java.math.BigDecimal;

/** Persona que respalda el préstamo de un cliente (dbo.Fiadores). */
public class Fiador extends Persona {

    private static final long serialVersionUID = 1L;

    private int prestamoId;
    /** Relación con el cliente (hermano, amigo, compañero de trabajo...). */
    private String parentesco;
    private BigDecimal ingresoMensual = BigDecimal.ZERO;

    public Fiador() {
    }

    @Override
    public String getTipoPersona() {
        return "Fiador";
    }

    public int getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(int prestamoId) {
        this.prestamoId = prestamoId;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public BigDecimal getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(BigDecimal ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

}
