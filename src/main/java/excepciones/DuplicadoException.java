package excepciones;

/** Ya existe un registro con ese dato único (identidad, nombre de usuario...). */
public class DuplicadoException extends CrediManagerException {

    public DuplicadoException(String mensaje) {
        super(mensaje);
    }

    public DuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
