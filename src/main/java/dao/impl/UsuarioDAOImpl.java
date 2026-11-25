package dao.impl;

import dao.UsuarioDAO;
import excepciones.PersistenciaException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.Usuario;
import modelo.enums.Rol;

public class UsuarioDAOImpl extends CrediDAOBase implements UsuarioDAO {

    private static final String COLUMNAS = "usuario_id, nombre_usuario, nombre_completo, rol, sal, hash_contrasena, "
            + "iteraciones, intentos_fallidos, ultimo_acceso, activo, fecha_registro";

    private static final String AUDITAR =
            "INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo) "
            + "VALUES ('Usuarios', ?, ?, 'sesion', NULL, ?)";

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("usuario_id"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setSal(rs.getString("sal"));
        u.setHuella(rs.getString("hash_contrasena"));
        u.setIteraciones(rs.getInt("iteraciones"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setUltimoAcceso(fechaHora(rs, "ultimo_acceso"));
        u.setActivo(rs.getBoolean("activo"));
        u.setFechaCreacion(fechaHora(rs, "fecha_registro"));
        return u;
    }

    /** El usuario debe traer sal, huella e iteraciones (ver util.Seguridad). */
    @Override
    public Integer insertar(Usuario u) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Usuarios (nombre_usuario, nombre_completo, rol, sal, hash_contrasena, iteraciones, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql, u.getNombreUsuario(), u.getNombreCompleto(), u.getRol(),
                u.getSal(), u.getHuella(), u.getIteraciones(), u.isActivo());
        u.setId(id);
        return id;
    }

    /** Actualiza nombre, rol y estado. La contraseña se cambia con cambiarContrasena. */
    @Override
    public boolean actualizar(Usuario u) throws PersistenciaException {
        String sql = "UPDATE dbo.Usuarios SET nombre_usuario = ?, nombre_completo = ?, rol = ?, activo = ? WHERE usuario_id = ?";
        return ejecutarUpdate(sql, u.getNombreUsuario(), u.getNombreCompleto(), u.getRol(), u.isActivo(), u.getId()) > 0;
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) throws PersistenciaException {
        return consultarUno("SELECT " + COLUMNAS + " FROM dbo.Usuarios WHERE usuario_id = ?", this::mapear, id);
    }

    @Override
    public List<Usuario> listar() throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.Usuarios ORDER BY nombre_completo", this::mapear);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        return ejecutarUpdate("UPDATE dbo.Usuarios SET activo = 0 WHERE usuario_id = ?", id) > 0;
    }

    @Override
    public Optional<Usuario> buscarPorNombre(String nombreUsuario) throws PersistenciaException {
        return consultarUno("SELECT " + COLUMNAS + " FROM dbo.Usuarios WHERE nombre_usuario = ?", this::mapear, nombreUsuario);
    }

    @Override
    public boolean existeNombreUsuario(String nombreUsuario, Integer excluirId) throws PersistenciaException {
        int excluir = excluirId == null ? 0 : excluirId;
        return consultarUno("SELECT COUNT(*) FROM dbo.Usuarios WHERE nombre_usuario = ? AND usuario_id <> ?",
                rs -> rs.getInt(1), nombreUsuario, excluir).map(c -> c > 0).orElse(false);
    }

    @Override
    public int registrarFallo(int usuarioId) throws PersistenciaException {
        return ejecutarEnTransaccion(cn -> {
            ejecutarUpdate(cn, "UPDATE dbo.Usuarios SET intentos_fallidos = intentos_fallidos + 1 WHERE usuario_id = ?", usuarioId);
            ejecutarUpdate(cn, AUDITAR, "LOGIN_FAIL", usuarioId, "Contraseña incorrecta");
            return consultarUno(cn, "SELECT intentos_fallidos FROM dbo.Usuarios WHERE usuario_id = ?",
                    rs -> rs.getInt(1), usuarioId).orElse(0);
        });
    }

    @Override
    public void registrarAcceso(int usuarioId, String nombreUsuario) throws PersistenciaException {
        ejecutarEnTransaccion(cn -> {
            ejecutarUpdate(cn, "UPDATE dbo.Usuarios SET intentos_fallidos = 0, ultimo_acceso = SYSDATETIME() "
                    + "WHERE usuario_id = ?", usuarioId);
            ejecutarUpdate(cn, AUDITAR, "LOGIN", usuarioId, "Inicio de sesión de " + nombreUsuario);
        });
    }

    @Override
    public void registrarSalida(int usuarioId, String nombreUsuario) throws PersistenciaException {
        ejecutarUpdate(AUDITAR, "LOGOUT", usuarioId, "Cierre de sesión de " + nombreUsuario);
    }

    @Override
    public boolean cambiarContrasena(int usuarioId, String sal, String huella, int iteraciones) throws PersistenciaException {
        return ejecutarEnTransaccion(cn -> {
            int filas = ejecutarUpdate(cn, "UPDATE dbo.Usuarios SET sal = ?, hash_contrasena = ?, iteraciones = ?, "
                    + "intentos_fallidos = 0 WHERE usuario_id = ?", sal, huella, iteraciones, usuarioId);
            ejecutarUpdate(cn, AUDITAR, "CLAVE", usuarioId, "Cambio de contraseña");
            return filas > 0;
        });
    }

    @Override
    public boolean desbloquear(int usuarioId) throws PersistenciaException {
        return ejecutarUpdate("UPDATE dbo.Usuarios SET intentos_fallidos = 0 WHERE usuario_id = ?", usuarioId) > 0;
    }
}
