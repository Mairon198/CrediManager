package dao;

import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import modelo.Pago;
import modelo.enums.MetodoPago;

/** Contrato de persistencia de pagos (recibos). */
public interface PagoDAO {

    /**
     * Cobra la siguiente cuota pendiente con sp_RegistrarPago (calcula la mora,
     * marca la cuota y actualiza el estado del préstamo) y devuelve el recibo.
     */
    Pago registrarPago(int prestamoId, int usuarioId, MetodoPago metodo, String referencia) throws PersistenciaException;

    /** Mora que se cobraría hoy (o en la fecha indicada) por la siguiente cuota. */
    BigDecimal calcularMora(int prestamoId, LocalDate fecha) throws PersistenciaException;

    Optional<Pago> buscarPorId(int pagoId) throws PersistenciaException;

    Optional<Pago> buscarPorRecibo(String numeroRecibo) throws PersistenciaException;

    List<Pago> listarPorPrestamo(int prestamoId) throws PersistenciaException;

    /** Pagos entre dos fechas, ambas incluidas. */
    List<Pago> listarEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException;

    /** Total cobrado (cuotas + mora) entre dos fechas, ambas incluidas. */
    BigDecimal totalCobradoEntre(LocalDate desde, LocalDate hasta) throws PersistenciaException;
}
