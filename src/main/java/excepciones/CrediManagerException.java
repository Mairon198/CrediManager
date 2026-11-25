package excepciones;

/** Excepción base del sistema. Todas las demás heredan de ella. */
public class CrediManagerException extends Exception {

    public CrediManagerException(String mensaje) {
        super(mensaje);
    }

    public CrediManagerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
