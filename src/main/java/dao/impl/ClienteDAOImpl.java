package dao.impl;

import dao.ClienteDAO;
import excepciones.PersistenciaException;
import modelo.Cliente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ClienteDAOImpl extends CrediDAOBase implements ClienteDAO {

    private static final String COLUMNAS = "cliente_id, identificacion, nombre, apellido, telefono, correo, direccion, "
            + "ocupacion, ingreso_mensual, activo, fecha_registro";

    private static final String FILTRO = "(nombre LIKE ? OR apellido LIKE ? OR CONCAT(nombre, ' ', apellido) LIKE ? "
            + "OR identificacion LIKE ? OR telefono LIKE ?)";

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("cliente_id"));
        c.setIdentidad(rs.getString("identificacion"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setDireccion(rs.getString("direccion"));
        c.setOcupacion(rs.getString("ocupacion"));
        c.setIngresoMensual(rs.getBigDecimal("ingreso_mensual"));
        c.setActivo(rs.getBoolean("activo"));
        c.setFechaCreacion(fechaHora(rs, "fecha_registro"));
        return c;
    }

    @Override
    public Integer insertar(Cliente entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Clientes (identificacion, nombre, apellido, telefono, correo, direccion, ocupacion, "
                   + "ingreso_mensual, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql,
                entidad.getIdentidad(),
                entidad.getNombre().trim(),
                entidad.getApellido().trim(),
                vacioANull(entidad.getTelefono()),
                vacioANull(entidad.getCorreo()),
                vacioANull(entidad.getDireccion()),
                vacioANull(entidad.getOcupacion()),
                entidad.getIngresoMensual(),
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Cliente entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Clientes SET identificacion = ?, nombre = ?, apellido = ?, telefono = ?, correo = ?, "
                   + "direccion = ?, ocupacion = ?, ingreso_mensual = ?, activo = ? WHERE cliente_id = ?";
        return ejecutarUpdate(sql,
                entidad.getIdentidad(),
                entidad.getNombre().trim(),
                entidad.getApellido().trim(),
                vacioANull(entidad.getTelefono()),
                vacioANull(entidad.getCorreo()),
                vacioANull(entidad.getDireccion()),
                vacioANull(entidad.getOcupacion()),
                entidad.getIngresoMensual(),
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) throws PersistenciaException {
        String sql = "SELECT " + COLUMNAS + " FROM dbo.Clientes WHERE cliente_id = ?";
        return consultarUno(sql, this::mapear, id);
    }

    @Override
    public Optional<Cliente> buscarPorIdentidad(String identidad) throws PersistenciaException {
        String sql = "SELECT " + COLUMNAS + " FROM dbo.Clientes WHERE identificacion = ?";
        return consultarUno(sql, this::mapear, identidad);
    }

    @Override
    public List<Cliente> listar() throws PersistenciaException {
        String sql = "SELECT " + COLUMNAS + " FROM dbo.Clientes ORDER BY cliente_id DESC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Cliente> listarActivos() throws PersistenciaException {
        String sql = "SELECT " + COLUMNAS + " FROM dbo.Clientes WHERE activo = 1 ORDER BY nombre ASC, apellido ASC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Cliente> buscarPorCriterio(String filtro) throws PersistenciaException {
        String like = like(filtro);
        String sql = "SELECT " + COLUMNAS + " FROM dbo.Clientes WHERE " + FILTRO + " ORDER BY nombre ASC, apellido ASC";
        return consultarLista(sql, this::mapear, like, like, like, like, like);
    }

    @Override
    public List<Cliente> buscarPaginado(String filtro, int pagina, int tamano) throws PersistenciaException {
        String like = like(filtro);
        int saltar = Math.max(pagina - 1, 0) * tamano;
        String sql = "SELECT " + COLUMNAS + " FROM dbo.Clientes WHERE " + FILTRO
                   + " ORDER BY nombre ASC, apellido ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return consultarLista(sql, this::mapear, like, like, like, like, like, saltar, tamano);
    }

    @Override
    public int contar(String filtro) throws PersistenciaException {
        String like = like(filtro);
        String sql = "SELECT COUNT(*) FROM dbo.Clientes WHERE " + FILTRO;
        return consultarUno(sql, rs -> rs.getInt(1), like, like, like, like, like).orElse(0);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Clientes SET activo = 0 WHERE cliente_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public boolean existeIdentidad(String identidad, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Clientes WHERE identificacion = ? AND cliente_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Clientes WHERE identificacion = ?";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), identidad, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), identidad);
        return count.map(c -> c > 0).orElse(false);
    }

    private static String like(String filtro) {
        return "%" + (filtro == null ? "" : filtro.trim()) + "%";
    }
}
