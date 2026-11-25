package excepciones;

/** Error al leer o guardar en la base de datos. */
public class PersistenciaException extends CrediManagerException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
