package dao;

import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import modelo.Prestamo;
import modelo.enums.EstadoPrestamo;

/**
 * Contrato de persistencia de los préstamos.
 * Las operaciones que cambian el estado usan procedimientos almacenados, que
 * validan las reglas del negocio dentro de una transacción.
 */
public interface PrestamoDAO {

    /** Registra la solicitud con sp_RegistrarSolicitud y devuelve su id. */
    int registrarSolicitud(Prestamo prestamo) throws PersistenciaException;

    /** Cambia cliente, tipo, monto, plazo o destino. Solo funciona mientras está SOLICITADO. */
    boolean actualizarSolicitud(Prestamo prestamo) throws PersistenciaException;

    Optional<Prestamo> buscarPorId(int prestamoId) throws PersistenciaException;

    Optional<Prestamo> buscarPorNumero(String numeroPrestamo) throws PersistenciaException;

    List<Prestamo> listar() throws PersistenciaException;

    List<Prestamo> listarPorEstado(EstadoPrestamo estado) throws PersistenciaException;

    List<Prestamo> listarPorCliente(int clienteId) throws PersistenciaException;

    /** Busca por número de préstamo, identidad o nombre del cliente. */
    List<Prestamo> buscarPorCriterio(String filtro) throws PersistenciaException;

    /** Aprueba con sp_AprobarPrestamo y devuelve la cuota mensual calculada. */
    BigDecimal aprobar(int prestamoId, String observaciones) throws PersistenciaException;

    /** Desembolsa con sp_DesembolsarPrestamo (genera el plan de pagos). Fecha null = hoy. */
    void desembolsar(int prestamoId, LocalDate fecha) throws PersistenciaException;

    /** Rechazar o pasar a mora manualmente, con sp_CambiarEstadoPrestamo. */
    void cambiarEstado(int prestamoId, EstadoPrestamo nuevo, String observaciones) throws PersistenciaException;

    /** Borrado lógico (activo = 0). */
    boolean desactivar(int prestamoId) throws PersistenciaException;

    /**
     * Ejecuta sp_ActualizarMora.
     *
     * @return {cuotas marcadas como vencidas, préstamos que pasaron a mora}
     */
    int[] actualizarMora() throws PersistenciaException;

    /** Cuota mensual con la misma fórmula de la BD (fn_CalcularCuota), para el simulador. */
    BigDecimal calcularCuota(BigDecimal monto, BigDecimal tasaAnual, int plazoMeses) throws PersistenciaException;
}
