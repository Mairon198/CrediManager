package dao;

import excepciones.PersistenciaException;
import java.util.List;
import modelo.Garantia;

public interface GarantiaDAO extends CrudDAO<Garantia, Integer> {

    List<Garantia> listarPorPrestamo(int prestamoId) throws PersistenciaException;
}
