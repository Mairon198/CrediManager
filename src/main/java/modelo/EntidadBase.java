package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Datos comunes de todas las entidades: id, si está activa (borrado lógico) y
 * fecha de registro. Es Serializable porque el usuario se guarda en la sesión web.
 */
public abstract class EntidadBase implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int id;
    protected boolean activo = true;
    protected LocalDateTime fechaCreacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esNueva() {
        return id <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntidadBase otra = (EntidadBase) o;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
