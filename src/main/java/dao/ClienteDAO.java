package dao;

import excepciones.PersistenciaException;
import modelo.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO extends CrudDAO<Cliente, Integer> {

    Optional<Cliente> buscarPorIdentidad(String identidad) throws PersistenciaException;

    List<Cliente> listarActivos() throws PersistenciaException;

    /** Busca por identidad, nombre, apellido o teléfono. */
    List<Cliente> buscarPorCriterio(String filtro) throws PersistenciaException;

    /**
     * Igual que buscarPorCriterio pero por páginas (para la lista de clientes).
     *
     * @param pagina número de página, empezando en 1
     */
    List<Cliente> buscarPaginado(String filtro, int pagina, int tamano) throws PersistenciaException;

    /** Cantidad de clientes que coinciden con el filtro (para calcular las páginas). */
    int contar(String filtro) throws PersistenciaException;

    boolean existeIdentidad(String identidad, Integer excluirId) throws PersistenciaException;
}
