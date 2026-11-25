package dao;

import excepciones.PersistenciaException;
import java.util.List;
import modelo.TipoPrestamo;

public interface TipoPrestamoDAO extends CrudDAO<TipoPrestamo, Integer> {

    List<TipoPrestamo> listarActivos() throws PersistenciaException;

    boolean existeNombre(String nombre, Integer excluirId) throws PersistenciaException;
}
