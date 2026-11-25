package modelo;

import java.time.LocalDateTime;
import modelo.enums.Rol;

/** Usuario del sistema (inicio de sesión). La contraseña solo existe como huella. */
public class Usuario extends EntidadBase {

    private static final long serialVersionUID = 1L;

    private String nombreUsuario;
    private String nombreCompleto;
    private Rol rol;
    private String sal;
    private String huella;
    private int iteraciones;
    private int intentosFallidos;
    private LocalDateTime ultimoAcceso;

    public boolean estaBloqueado(int maximoIntentos) {
        return intentosFallidos >= maximoIntentos;
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + nombreUsuario + ")";
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String v) { this.nombreUsuario = v; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { this.nombreCompleto = v; }
    public Rol getRol() { return rol; }
    public void setRol(Rol v) { this.rol = v; }
    public String getSal() { return sal; }
    public void setSal(String v) { this.sal = v; }
    public String getHuella() { return huella; }
    public void setHuella(String v) { this.huella = v; }
    public int getIteraciones() { return iteraciones; }
    public void setIteraciones(int v) { this.iteraciones = v; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int v) { this.intentosFallidos = v; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime v) { this.ultimoAcceso = v; }
}
