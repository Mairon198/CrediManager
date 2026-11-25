package modelo.enums;

import java.util.EnumSet;
import java.util.Set;

/** Estados de un préstamo. Mismo flujo que dbo.TransicionesEstadoPrestamo. */
public enum EstadoPrestamo {
    SOLICITADO("Solicitado"),
    APROBADO("Aprobado"),
    ACTIVO("Activo"),
    EN_MORA("En mora"),
    CANCELADO("Cancelado"),
    RECHAZADO("Rechazado");

    private final String descripcion;

    EstadoPrestamo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Set<EstadoPrestamo> siguientesPermitidos() {
        switch (this) {
            case SOLICITADO:
                return EnumSet.of(APROBADO, RECHAZADO);
            case APROBADO:
                return EnumSet.of(ACTIVO, RECHAZADO);
            case ACTIVO:
                return EnumSet.of(EN_MORA, CANCELADO);
            case EN_MORA:
                return EnumSet.of(ACTIVO, CANCELADO);
            default:
                return EnumSet.noneOf(EstadoPrestamo.class);
        }
    }

    public boolean puedeCambiarA(EstadoPrestamo nuevo) {
        return siguientesPermitidos().contains(nuevo);
    }

    public boolean esFinal() {
        return this == CANCELADO || this == RECHAZADO;
    }

    /** Préstamo con dinero entregado y cuotas por cobrar. */
    public boolean estaVigente() {
        return this == ACTIVO || this == EN_MORA;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
