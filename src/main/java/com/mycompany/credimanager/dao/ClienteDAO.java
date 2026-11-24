package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla {@code cliente}.
 *
 * @author Integrante 1
 */
public class ClienteDAO implements ICrud<Cliente> {

    private static final String SELECT_BASE
            = "SELECT id_cliente, dni, nombres, apellidos, telefono, direccion, correo, ocupacion, ingreso_mensual FROM cliente ";

    /** Filtro para la búsqueda: por DNI, nombres o apellidos. */
    private static final String FILTRO_TEXTO
            = "WHERE (? IS NULL OR dni LIKE ? OR nombres LIKE ? OR apellidos LIKE ? OR (nombres + ' ' + apellidos) LIKE ?) ";

    @Override
    public boolean insertar(Cliente c) {
        String sql = "INSERT INTO cliente (dni, nombres, apellidos, telefono, direccion, correo, ocupacion, ingreso_mensual) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, c);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                c.setIdCliente(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar el cliente", e);
        }
    }

    @Override
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE cliente SET dni = ?, nombres = ?, apellidos = ?, telefono = ?, direccion = ?, correo = ?, "
                + "ocupacion = ?, ingreso_mensual = ? WHERE id_cliente = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, c);
            ps.setInt(9, c.getIdCliente());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el cliente", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM cliente WHERE id_cliente = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar el cliente", e);
        }
    }

    @Override
    public Cliente buscarPorId(int id) {
        return buscarUno(SELECT_BASE + "WHERE id_cliente = ?", id);
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY apellidos, nombres");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("listar los clientes", e);
        }
        return lista;
    }

    /** @return el cliente con ese DNI (13 dígitos sin guiones), o null */
    public Cliente buscarPorDni(String dni) {
        return buscarUno(SELECT_BASE + "WHERE dni = ?", dni);
    }

    /**
     * Indica si un DNI ya está registrado.
     *
     * @param excluirId ID a ignorar (el propio cliente al editar); 0 al crear
     */
    public boolean existeDni(String dni, int excluirId) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM cliente WHERE dni = ? AND id_cliente <> ?")) {
            ps.setString(1, dni);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("verificar el DNI", e);
        }
    }

    /**
     * Búsqueda con paginación (para la lista de clientes).
     *
     * @param texto  parte del DNI, nombre o apellido; null o vacío = todos
     * @param pagina número de página, empezando en 1
     * @param tamano registros por página (ver Constantes.REGISTROS_POR_PAGINA)
     */
    public List<Cliente> buscar(String texto, int pagina, int tamano) {
        String sql = SELECT_BASE + FILTRO_TEXTO + "ORDER BY apellidos, nombres OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = asignarFiltro(ps, texto);
            ps.setInt(i++, Math.max(pagina - 1, 0) * tamano);
            ps.setInt(i, tamano);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("buscar clientes", e);
        }
        return lista;
    }

    /** Cuenta los clientes que coinciden con la búsqueda (para calcular el número de páginas). */
    public int contar(String texto) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM cliente " + FILTRO_TEXTO)) {
            asignarFiltro(ps, texto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("contar clientes", e);
        }
    }

    /** Asigna los 5 parámetros del filtro y devuelve el índice del siguiente parámetro. */
    private int asignarFiltro(PreparedStatement ps, String texto) throws SQLException {
        String patron = (texto == null || texto.isBlank()) ? null : "%" + texto.trim() + "%";
        for (int i = 1; i <= 5; i++) {
            ps.setString(i, patron);
        }
        return 6;
    }

    private void asignar(PreparedStatement ps, Cliente c) throws SQLException {
        ps.setString(1, c.getDni());
        ps.setString(2, c.getNombres());
        ps.setString(3, c.getApellidos());
        JdbcUtil.setTexto(ps, 4, c.getTelefono());
        JdbcUtil.setTexto(ps, 5, c.getDireccion());
        JdbcUtil.setTexto(ps, 6, c.getCorreo());
        JdbcUtil.setTexto(ps, 7, c.getOcupacion());
        JdbcUtil.setDecimal(ps, 8, c.getIngresoMensual());
    }

    private Cliente buscarUno(String sql, Object parametro) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("buscar el cliente", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("dni"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("correo"),
                rs.getString("ocupacion"),
                rs.getBigDecimal("ingreso_mensual"));
    }
}
