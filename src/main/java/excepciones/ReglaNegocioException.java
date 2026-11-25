package excepciones;

/** La operación no cumple una regla del negocio (estado del préstamo, capacidad de pago...). */
public class ReglaNegocioException extends CrediManagerException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
