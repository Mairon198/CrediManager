package com.mycompany.credimanager.model;

import java.io.Serializable;

/**
 * Persona que usa el sistema. La contraseña se guarda como hash (ver PasswordUtil).
 * <p>Tabla: {@code usuario}.
 *
 * @author Integrante 1
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador. */
    private int idUsuario;
    /** Nombre completo. */
    private String nombre;
    /** Nombre de usuario para iniciar sesión. */
    private String usuario;
    /** Hash de la contraseña, nunca el texto plano. */
    private String contrasenaHash;
    /** Rol asignado. */
    private int idRol;
    /** Si puede iniciar sesión. */
    private boolean activo;

    // ----- Datos de apoyo para mostrar en listas (no son columnas de esta tabla) -----
    /** Nombre del rol (se llena en las consultas con JOIN). */
    private String nombreRol;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombre, String usuario, String contrasenaHash, int idRol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
        this.idRol = idRol;
        this.activo = activo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    @Override
    public String toString() {
        return "Usuario{" + "idUsuario=" + idUsuario + ", " + "nombre=" + nombre + ", " + "usuario=" + usuario + ", " + "idRol=" + idRol + ", " + "activo=" + activo + "}";
    }
}
