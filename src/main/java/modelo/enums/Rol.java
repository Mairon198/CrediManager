package modelo.enums;

/** Rol del usuario (columna dbo.Usuarios.rol). */
public enum Rol {
    ADMIN("Administrador"),
    ASESOR("Asesor de créditos"),
    CAJERO("Cajero");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
