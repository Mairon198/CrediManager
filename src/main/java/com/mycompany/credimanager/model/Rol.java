package com.mycompany.credimanager.model;

import java.io.Serializable;

/**
 * Rol de un usuario del sistema (Administrador, Asesor o Cajero).
 * <p>Tabla: {@code rol}.
 *
 * @author Integrante 1
 */
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador del rol. */
    private int idRol;
    /** Nombre del rol; ver Constantes.ROL_*. */
    private String nombre;

    public Rol() {
    }

    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Rol{" + "idRol=" + idRol + ", " + "nombre=" + nombre + "}";
    }
}
