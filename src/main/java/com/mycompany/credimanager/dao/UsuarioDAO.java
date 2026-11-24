package com.mycompany.credimanager.dao;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.model.Rol;
import com.mycompany.credimanager.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a las tablas {@code usuario} y {@code rol}.
 *
 * @author Integrante 1
 */
public class UsuarioDAO implements ICrud<Usuario> {

    private static final String SELECT_BASE
            = "SELECT u.id_usuario, u.nombre, u.usuario, u.contrasena_hash, u.id_rol, u.activo, r.nombre AS nombre_rol "
            + "FROM usuario u INNER JOIN rol r ON r.id_rol = u.id_rol ";

    @Override
    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO usuario (nombre, usuario, contrasena_hash, id_rol, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getContrasenaHash());
            ps.setInt(4, u.getIdRol());
            ps.setBoolean(5, u.isActivo());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                u.setIdUsuario(JdbcUtil.idGenerado(ps));
            }
            return ok;
        } catch (SQLException e) {
            throw JdbcUtil.error("registrar el usuario", e);
        }
    }

    /** Actualiza nombre, usuario, rol y estado. La contraseña se cambia con {@link #cambiarContrasena}. */
    @Override
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE usuario SET nombre = ?, usuario = ?, id_rol = ?, activo = ? WHERE id_usuario = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsuario());
            ps.setInt(3, u.getIdRol());
            ps.setBoolean(4, u.isActivo());
            ps.setInt(5, u.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("actualizar el usuario", e);
        }
    }

    /**
     * Elimina el usuario. Si ya registró préstamos o pagos no se podrá eliminar;
     * en ese caso conviene desactivarlo (activo = false) con {@link #actualizar}.
     */
    @Override
    public boolean eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM usuario WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("eliminar el usuario", e);
        }
    }

    @Override
    public Usuario buscarPorId(int id) {
        return buscarUno(SELECT_BASE + "WHERE u.id_usuario = ?", id);
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY u.nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("listar los usuarios", e);
        }
        return lista;
    }

    /**
     * Busca por nombre de usuario (para el inicio de sesión).
     *
     * @return el usuario con su hash de contraseña, o null si no existe
     */
    public Usuario buscarPorUsuario(String usuario) {
        return buscarUno(SELECT_BASE + "WHERE u.usuario = ?", usuario);
    }

    /**
     * Indica si un nombre de usuario ya está ocupado.
     *
     * @param excluirId ID a ignorar (el propio usuario al editar); 0 al crear
     */
    public boolean existeUsuario(String usuario, int excluirId) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE usuario = ? AND id_usuario <> ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("verificar el nombre de usuario", e);
        }
    }

    /**
     * Cambia la contraseña.
     *
     * @param nuevoHash hash generado con PasswordUtil.hashear
     */
    public boolean cambiarContrasena(int idUsuario, String nuevoHash) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("UPDATE usuario SET contrasena_hash = ? WHERE id_usuario = ?")) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcUtil.error("cambiar la contraseña", e);
        }
    }

    /** Lista los roles (para el combo del formulario de usuarios). */
    public List<Rol> listarRoles() {
        List<Rol> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT id_rol, nombre FROM rol ORDER BY id_rol");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("listar los roles", e);
        }
        return lista;
    }

    private Usuario buscarUno(String sql, Object parametro) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw JdbcUtil.error("buscar el usuario", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("usuario"),
                rs.getString("contrasena_hash"),
                rs.getInt("id_rol"),
                rs.getBoolean("activo"));
        u.setNombreRol(rs.getString("nombre_rol"));
        return u;
    }
}
