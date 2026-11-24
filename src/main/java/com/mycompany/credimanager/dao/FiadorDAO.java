package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.model.Fiador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla {@code fiador}.
 *
 * @author Integrante 1
 */
public class FiadorDAO implements ICrud<Fiador> {

    private static final String SELECT_BASE
            = "SELECT id_fiador, id_prestamo, dni, nombres, telefono, ingreso_mensual FROM fiador ";

    @Override
    public boolean insertar(Fiador f) {
        String sql = "INSERT INTO fiador (id_prestamo, dni, nombres, telefono, ingreso_mensual) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, f);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                f.setIdFiador(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar el fiador", e);
        }
    }

    @Override
    public boolean actualizar(Fiador f) {
        String sql = "UPDATE fiador SET id_prestamo = ?, dni = ?, nombres = ?, telefono = ?, ingreso_mensual = ? WHERE id_fiador = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, f);
            ps.setInt(6, f.getIdFiador());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el fiador", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM fiador WHERE id_fiador = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar el fiador", e);
        }
    }

    @Override
    public Fiador buscarPorId(int id) {
        List<Fiador> lista = consultar(SELECT_BASE + "WHERE id_fiador = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Fiador> listar() {
        return consultar(SELECT_BASE + "ORDER BY id_prestamo, id_fiador");
    }

    /** Fiadores de un préstamo. */
    public List<Fiador> listarPorPrestamo(int idPrestamo) {
        return consultar(SELECT_BASE + "WHERE id_prestamo = ? ORDER BY id_fiador", idPrestamo);
    }

    private List<Fiador> consultar(String sql, Object... parametros) {
        List<Fiador> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Fiador(
                            rs.getInt("id_fiador"),
                            rs.getInt("id_prestamo"),
                            rs.getString("dni"),
                            rs.getString("nombres"),
                            rs.getString("telefono"),
                            rs.getBigDecimal("ingreso_mensual")));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("consultar los fiadores", e);
        }
        return lista;
    }

    private void asignar(PreparedStatement ps, Fiador f) throws SQLException {
        ps.setInt(1, f.getIdPrestamo());
        ps.setString(2, f.getDni());
        ps.setString(3, f.getNombres());
        JdbcUtil.setTexto(ps, 4, f.getTelefono());
        JdbcUtil.setDecimal(ps, 5, f.getIngresoMensual());
    }
}
