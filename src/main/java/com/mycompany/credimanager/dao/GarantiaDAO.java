package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.model.Garantia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla {@code garantia}.
 *
 * @author Integrante 1
 */
public class GarantiaDAO implements ICrud<Garantia> {

    private static final String SELECT_BASE
            = "SELECT id_garantia, id_prestamo, tipo, descripcion, valor_estimado FROM garantia ";

    @Override
    public boolean insertar(Garantia g) {
        String sql = "INSERT INTO garantia (id_prestamo, tipo, descripcion, valor_estimado) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, g);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                g.setIdGarantia(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar la garantía", e);
        }
    }

    @Override
    public boolean actualizar(Garantia g) {
        String sql = "UPDATE garantia SET id_prestamo = ?, tipo = ?, descripcion = ?, valor_estimado = ? WHERE id_garantia = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, g);
            ps.setInt(5, g.getIdGarantia());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar la garantía", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM garantia WHERE id_garantia = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar la garantía", e);
        }
    }

    @Override
    public Garantia buscarPorId(int id) {
        List<Garantia> lista = consultar(SELECT_BASE + "WHERE id_garantia = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Garantia> listar() {
        return consultar(SELECT_BASE + "ORDER BY id_prestamo, id_garantia");
    }

    /** Garantías de un préstamo. */
    public List<Garantia> listarPorPrestamo(int idPrestamo) {
        return consultar(SELECT_BASE + "WHERE id_prestamo = ? ORDER BY id_garantia", idPrestamo);
    }

    private List<Garantia> consultar(String sql, Object... parametros) {
        List<Garantia> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Garantia(
                            rs.getInt("id_garantia"),
                            rs.getInt("id_prestamo"),
                            rs.getString("tipo"),
                            rs.getString("descripcion"),
                            rs.getBigDecimal("valor_estimado")));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("consultar las garantías", e);
        }
        return lista;
    }

    private void asignar(PreparedStatement ps, Garantia g) throws SQLException {
        ps.setInt(1, g.getIdPrestamo());
        ps.setString(2, g.getTipo());
        ps.setString(3, g.getDescripcion());
        JdbcUtil.setDecimal(ps, 4, g.getValorEstimado());
    }
}
