package com.mycompany.credimanager.dao;

/**
 * Error al acceder a la base de datos.
 * <p>
 * Envuelve la {@code SQLException} para que las clases de servicio no tengan
 * que declarar {@code throws SQLException}. El servicio o el Servlet la atrapa
 * y muestra un mensaje amigable con {@link #getMessage()}.
 *
 * @author Integrante 1
 */
public class DAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public DAOException(String mensaje) {
        super(mensaje);
    }
}
