package excepciones;

/** Un dato escrito por el usuario no es válido (campo vacío, formato incorrecto...). */
public class ValidacionException extends CrediManagerException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
