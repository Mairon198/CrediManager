package modelo.enums;

/** Tipos de garantía (columna dbo.Garantias.tipo). */
public enum TipoGarantia {
    HIPOTECARIA("Hipotecaria (inmueble)"),
    PRENDARIA("Prendaria (vehículo o bien mueble)"),
    DOCUMENTARIA("Documentaria (pagaré, letra)"),
    OTRA("Otra");

    private final String descripcion;

    TipoGarantia(String descripcion) {
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
