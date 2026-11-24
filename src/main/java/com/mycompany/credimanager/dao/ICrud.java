package com.mycompany.credimanager.dao;

import java.util.List;

/**
 * Operaciones básicas que implementan todos los DAO.
 * <p>
 * Todos los métodos lanzan {@link DAOException} si falla la base de datos.
 *
 * @param <T> clase del paquete model que maneja el DAO
 * @author Integrante 1
 */
public interface ICrud<T> {

    /**
     * Inserta un registro nuevo. Al terminar, el objeto queda con el ID generado.
     *
     * @return true si se insertó
     */
    boolean insertar(T obj);

    /**
     * Actualiza un registro existente (se busca por su ID).
     *
     * @return true si se actualizó alguna fila
     */
    boolean actualizar(T obj);

    /**
     * Elimina un registro por su ID.
     *
     * @return true si se eliminó alguna fila
     * @throws DAOException si el registro está relacionado con otros (llave foránea)
     */
    boolean eliminar(int id);

    /**
     * Busca un registro por su ID.
     *
     * @return el objeto, o null si no existe
     */
    T buscarPorId(int id);

    /**
     * Lista todos los registros.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    List<T> listar();
}
