package modelo;

import java.math.BigDecimal;

/** Persona que solicita préstamos (dbo.Clientes). */
public class Cliente extends Persona {

    private static final long serialVersionUID = 1L;

    private String ocupacion;
    /** Ingreso mensual en lempiras. La cuota no puede pasar del 40 % de este valor. */
    private BigDecimal ingresoMensual = BigDecimal.ZERO;

    public Cliente() {
    }

    public Cliente(String identidad, String nombre, String apellido, String telefono, String correo, String direccion) {
        this.identidad = identidad;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public Cliente(String identidad, String nombre, String apellido, String telefono, String correo, String direccion,
                   String ocupacion, BigDecimal ingresoMensual) {
        this(identidad, nombre, apellido, telefono, correo, direccion);
        this.ocupacion = ocupacion;
        this.ingresoMensual = ingresoMensual;
    }

    @Override
    public String getTipoPersona() {
        return "Cliente";
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

}
