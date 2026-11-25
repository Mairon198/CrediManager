package dao;

import excepciones.PersistenciaException;
import java.util.Optional;
import modelo.Usuario;

/** Contrato de persistencia de los usuarios del sistema. */
public interface UsuarioDAO extends CrudDAO<Usuario, Integer> {

    Optional<Usuario> buscarPorNombre(String nombreUsuario) throws PersistenciaException;

    boolean existeNombreUsuario(String nombreUsuario, Integer excluirId) throws PersistenciaException;

    /** Suma un intento fallido y devuelve cuántos lleva. */
    int registrarFallo(int usuarioId) throws PersistenciaException;

    /** Reinicia los intentos, guarda la fecha de acceso y lo deja en la auditoría. */
    void registrarAcceso(int usuarioId, String nombreUsuario) throws PersistenciaException;

    void registrarSalida(int usuarioId, String nombreUsuario) throws PersistenciaException;

    /** Guarda una contraseña nueva (sal y huella generadas con util.Seguridad). */
    boolean cambiarContrasena(int usuarioId, String sal, String huella, int iteraciones) throws PersistenciaException;

    /** Pone en cero los intentos fallidos. */
    boolean desbloquear(int usuarioId) throws PersistenciaException;
}
