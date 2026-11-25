package prueba;

import conexion.ConexionDB;
import dao.impl.ClienteDAOImpl;
import dao.impl.CuotaDAOImpl;
import dao.impl.FiadorDAOImpl;
import dao.impl.GarantiaDAOImpl;
import dao.impl.PagoDAOImpl;
import dao.impl.PrestamoDAOImpl;
import dao.impl.ReporteDAOImpl;
import dao.impl.TipoPrestamoDAOImpl;
import dao.impl.UsuarioDAOImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.Usuario;
import util.Seguridad;

/**
 * Prueba la conexión y los DAO contra la base de datos real (solo lectura).
 * <p>
 * En NetBeans: clic derecho sobre este archivo → Run File.
 * Antes hay que ejecutar database/CrediManagerDB.sql y crear config.properties.
 */
public class PruebaConexion {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Prueba {
        Object ejecutar() throws Exception;
    }

    public static void main(String[] args) {
        System.out.println("=== Conexión ===");
        if (!ConexionDB.probarConexion()) {
            System.out.println("No hay conexión. Revise config.properties y que SQL Server esté encendido.");
            return;
        }

        System.out.println("\n=== Consultas de cada DAO ===");
        probar("Usuarios", () -> new UsuarioDAOImpl().listar().size() + " usuarios");
        probar("Clientes activos", () -> new ClienteDAOImpl().listarActivos().size() + " clientes");
        probar("Buscar clientes 'mar'", () -> new ClienteDAOImpl().buscarPaginado("mar", 1, 10).size() + " resultados");
        probar("Tipos de préstamo", () -> new TipoPrestamoDAOImpl().listarActivos());
        probar("Préstamos", () -> new PrestamoDAOImpl().listar().size() + " préstamos");
        probar("Préstamo PRE-000001", () -> new PrestamoDAOImpl().buscarPorNumero("PRE-000001")
                .map(p -> p.getCliente() + ", " + p.getEstado() + ", saldo " + p.getSaldoPendiente()).orElse("no existe"));
        probar("Plan de pagos préstamo 1", () -> new CuotaDAOImpl().listarPorPrestamo(1).size() + " cuotas");
        probar("Cuotas vencidas", () -> new CuotaDAOImpl().listarVencidas().size() + " cuotas");
        probar("Pagos préstamo 1", () -> new PagoDAOImpl().listarPorPrestamo(1).size() + " pagos");
        probar("Cobrado últimos 365 días", () -> new PagoDAOImpl().totalCobradoEntre(LocalDate.now().minusDays(365), LocalDate.now()));
        probar("Garantías", () -> new GarantiaDAOImpl().listar().size() + " garantías");
        probar("Fiadores", () -> new FiadorDAOImpl().listar().size() + " fiadores");
        probar("Cartera por estado", () -> new ReporteDAOImpl().carteraPorEstado().size() + " estados");
        probar("fn_CalcularCuota(60000, 18, 12)", () -> new PrestamoDAOImpl()
                .calcularCuota(new BigDecimal("60000"), new BigDecimal("18"), 12) + " (esperado 5500.80)");

        System.out.println("\n=== Contraseña del usuario admin ===");
        probar("admin / Admin2026", () -> {
            Usuario u = new UsuarioDAOImpl().buscarPorNombre("admin").orElseThrow();
            return Seguridad.verificar("Admin2026".toCharArray(), u.getSal(), u.getIteraciones(), u.getHuella())
                    ? "correcta" : "NO coincide";
        });

        System.out.printf("%nResultado: %d correctas, %d con error%n", correctas, fallidas);
    }

    private static void probar(String nombre, Prueba prueba) {
        try {
            System.out.printf("OK     %-34s -> %s%n", nombre, prueba.ejecutar());
            correctas++;
        } catch (Exception e) {
            System.out.printf("ERROR  %-34s -> %s%n", nombre, e.getMessage());
            fallidas++;
        }
    }
}
