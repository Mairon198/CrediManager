package dao.impl;

import dao.FiadorDAO;
import excepciones.PersistenciaException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import modelo.Fiador;

public class FiadorDAOImpl extends CrediDAOBase implements FiadorDAO {

    private static final String COLUMNAS = "fiador_id, prestamo_id, identificacion, nombre, apellido, telefono, direccion, "
            + "parentesco, ingreso_mensual, activo, fecha_registro";

    private Fiador mapear(ResultSet rs) throws SQLException {
        Fiador f = new Fiador();
        f.setId(rs.getInt("fiador_id"));
        f.setPrestamoId(rs.getInt("prestamo_id"));
        f.setIdentidad(rs.getString("identificacion"));
        f.setNombre(rs.getString("nombre"));
        f.setApellido(rs.getString("apellido"));
        f.setTelefono(rs.getString("telefono"));
        f.setDireccion(rs.getString("direccion"));
        f.setParentesco(rs.getString("parentesco"));
        f.setIngresoMensual(rs.getBigDecimal("ingreso_mensual"));
        f.setActivo(rs.getBoolean("activo"));
        f.setFechaCreacion(fechaHora(rs, "fecha_registro"));
        return f;
    }

    @Override
    public Integer insertar(Fiador f) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Fiadores (prestamo_id, identificacion, nombre, apellido, telefono, direccion, parentesco, "
                   + "ingreso_mensual, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql, f.getPrestamoId(), f.getIdentidad(), f.getNombre().trim(), f.getApellido().trim(),
                vacioANull(f.getTelefono()), vacioANull(f.getDireccion()), vacioANull(f.getParentesco()),
                f.getIngresoMensual(), f.isActivo());
        f.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Fiador f) throws PersistenciaException {
        String sql = "UPDATE dbo.Fiadores SET identificacion = ?, nombre = ?, apellido = ?, telefono = ?, direccion = ?, "
                   + "parentesco = ?, ingreso_mensual = ?, activo = ? WHERE fiador_id = ?";
        return ejecutarUpdate(sql, f.getIdentidad(), f.getNombre().trim(), f.getApellido().trim(), vacioANull(f.getTelefono()),
                vacioANull(f.getDireccion()), vacioANull(f.getParentesco()), f.getIngresoMensual(), f.isActivo(),
                f.getId()) > 0;
    }

    @Override
    public Optional<Fiador> buscarPorId(Integer id) throws PersistenciaException {
        return consultarUno("SELECT " + COLUMNAS + " FROM dbo.Fiadores WHERE fiador_id = ?", this::mapear, id);
    }

    @Override
    public List<Fiador> listar() throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.Fiadores ORDER BY fiador_id DESC", this::mapear);
    }

    @Override
    public List<Fiador> listarPorPrestamo(int prestamoId) throws PersistenciaException {
        return consultarLista("SELECT " + COLUMNAS + " FROM dbo.Fiadores WHERE prestamo_id = ? AND activo = 1 "
                + "ORDER BY fiador_id", this::mapear, prestamoId);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        return ejecutarUpdate("UPDATE dbo.Fiadores SET activo = 0 WHERE fiador_id = ?", id) > 0;
    }

    @Override
    public boolean existeEnPrestamo(int prestamoId, String identidad, Integer excluirId) throws PersistenciaException {
        int excluir = excluirId == null ? 0 : excluirId;
        return consultarUno("SELECT COUNT(*) FROM dbo.Fiadores WHERE prestamo_id = ? AND identificacion = ? AND fiador_id <> ?",
                rs -> rs.getInt(1), prestamoId, identidad, excluir).map(c -> c > 0).orElse(false);
    }
}
