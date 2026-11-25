package dao;

import excepciones.PersistenciaException;
import java.util.List;
import modelo.Fiador;

public interface FiadorDAO extends CrudDAO<Fiador, Integer> {

    List<Fiador> listarPorPrestamo(int prestamoId) throws PersistenciaException;

    /** Indica si esa persona ya es fiadora del préstamo. */
    boolean existeEnPrestamo(int prestamoId, String identidad, Integer excluirId) throws PersistenciaException;
}
