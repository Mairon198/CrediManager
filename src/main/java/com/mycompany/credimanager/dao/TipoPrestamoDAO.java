package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.model.TipoPrestamo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla {@code tipo_prestamo}.
 *
 * @author Integrante 1
 */
public class TipoPrestamoDAO implements ICrud<TipoPrestamo> {

    private static final String SELECT_BASE
            = "SELECT id_tipo, nombre, tasa_interes_anual, plazo_max_meses, monto_max, porcentaje_mora FROM tipo_prestamo ";

    @Override
    public boolean insertar(TipoPrestamo t) {
        String sql = "INSERT INTO tipo_prestamo (nombre, tasa_interes_anual, plazo_max_meses, monto_max, porcentaje_mora) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, t);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                t.setIdTipo(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar el tipo de préstamo", e);
        }
    }

    @Override
    public boolean actualizar(TipoPrestamo t) {
        String sql = "UPDATE tipo_prestamo SET nombre = ?, tasa_interes_anual = ?, plazo_max_meses = ?, monto_max = ?, "
                + "porcentaje_mora = ? WHERE id_tipo = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, t);
            ps.setInt(6, t.getIdTipo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el tipo de préstamo", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM tipo_prestamo WHERE id_tipo = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar el tipo de préstamo", e);
        }
    }

    @Override
    public TipoPrestamo buscarPorId(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE id_tipo = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("buscar el tipo de préstamo", e);
        }
    }

    @Override
    public List<TipoPrestamo> listar() {
        List<TipoPrestamo> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("listar los tipos de préstamo", e);
        }
        return lista;
    }

    /**
     * Indica si ya existe un tipo con ese nombre.
     *
     * @param excluirId ID a ignorar al editar; 0 al crear
     */
    public boolean existeNombre(String nombre, int excluirId) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM tipo_prestamo WHERE nombre = ? AND id_tipo <> ?")) {
            ps.setString(1, nombre);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("verificar el nombre del tipo de préstamo", e);
        }
    }

    private void asignar(PreparedStatement ps, TipoPrestamo t) throws SQLException {
        ps.setString(1, t.getNombre());
        JdbcUtil.setDecimal(ps, 2, t.getTasaInteresAnual());
        ps.setInt(3, t.getPlazoMaxMeses());
        JdbcUtil.setDecimal(ps, 4, t.getMontoMax());
        JdbcUtil.setDecimal(ps, 5, t.getPorcentajeMora());
    }

    private TipoPrestamo mapear(ResultSet rs) throws SQLException {
        return new TipoPrestamo(
                rs.getInt("id_tipo"),
                rs.getString("nombre"),
                rs.getBigDecimal("tasa_interes_anual"),
                rs.getInt("plazo_max_meses"),
                rs.getBigDecimal("monto_max"),
                rs.getBigDecimal("porcentaje_mora"));
    }
}
