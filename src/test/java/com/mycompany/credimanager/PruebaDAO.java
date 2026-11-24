package com.mycompany.credimanager;

import com.mycompany.credimanager.config.Conexion;
import com.mycompany.credimanager.dao.ClienteDAO;
import com.mycompany.credimanager.dao.CuotaDAO;
import com.mycompany.credimanager.dao.FiadorDAO;
import com.mycompany.credimanager.dao.GarantiaDAO;
import com.mycompany.credimanager.dao.PagoDAO;
import com.mycompany.credimanager.dao.PrestamoDAO;
import com.mycompany.credimanager.dao.TipoPrestamoDAO;
import com.mycompany.credimanager.dao.UsuarioDAO;
import com.mycompany.credimanager.model.Cliente;
import java.math.BigDecimal;

/**
 * Prueba manual de la conexión y de los DAO contra la base de datos real.
 * <p>
 * Cómo usarla en NetBeans: clic derecho sobre este archivo → Run File.
 * Necesita que la base de datos exista y que db.properties esté configurado.
 * <p>
 * Solo lee datos, excepto la prueba de clientes, que inserta un cliente de
 * prueba, lo modifica y lo borra al final.
 *
 * @author Integrante 1
 */
public class PruebaDAO {

    private static int correctas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== Prueba de conexión ===");
        if (!Conexion.probar()) {
            System.out.println("No hay conexión. Revisa db.properties y que SQL Server esté encendido.");
            return;
        }
        System.out.println("Conexión correcta\n");

        System.out.println("=== Consultas de cada DAO ===");
        probar("UsuarioDAO.listar", () -> new UsuarioDAO().listar().size());
        probar("UsuarioDAO.listarRoles", () -> new UsuarioDAO().listarRoles().size());
        probar("ClienteDAO.listar", () -> new ClienteDAO().listar().size());
        probar("ClienteDAO.buscar (página 1)", () -> new ClienteDAO().buscar("", 1, 10).size());
        probar("TipoPrestamoDAO.listar", () -> new TipoPrestamoDAO().listar().size());
        probar("PrestamoDAO.listar", () -> new PrestamoDAO().listar().size());
        probar("PrestamoDAO.contarPorEstado", () -> new PrestamoDAO().contarPorEstado());
        probar("CuotaDAO.listar", () -> new CuotaDAO().listar().size());
        probar("PagoDAO.listar", () -> new PagoDAO().listar().size());
        probar("GarantiaDAO.listar", () -> new GarantiaDAO().listar().size());
        probar("FiadorDAO.listar", () -> new FiadorDAO().listar().size());

        System.out.println("\n=== CRUD de clientes (insertar, buscar, actualizar, eliminar) ===");
        probarCrudCliente();

        System.out.printf("%nResultado: %d correctas, %d con error%n", correctas, fallidas);
    }

    private static void probarCrudCliente() {
        ClienteDAO dao = new ClienteDAO();
        Cliente c = new Cliente(0, "0000000000001", "Prueba", "Automática", "9999-9999",
                "Tegucigalpa", "prueba@correo.com", "Pruebas", new BigDecimal("15000.00"));
        try {
            dao.insertar(c);
            System.out.println("Insertado con id " + c.getIdCliente());
            c.setOcupacion("Prueba modificada");
            dao.actualizar(c);
            Cliente leido = dao.buscarPorDni("0000000000001");
            boolean ok = leido != null && "Prueba modificada".equals(leido.getOcupacion());
            System.out.println("Buscar por DNI y actualizar: " + (ok ? "OK" : "NO coincide"));
            if (ok) {
                correctas++;
            } else {
                fallidas++;
            }
        } catch (RuntimeException e) {
            fallidas++;
            System.out.println("ERROR en CRUD de clientes: " + e.getMessage());
        } finally {
            if (c.getIdCliente() > 0) {
                dao.eliminar(c.getIdCliente());
                System.out.println("Cliente de prueba eliminado");
            }
        }
    }

    private static void probar(String nombre, java.util.function.Supplier<Object> prueba) {
        try {
            System.out.printf("OK     %-32s -> %s%n", nombre, prueba.get());
            correctas++;
        } catch (RuntimeException e) {
            System.out.printf("ERROR  %-32s -> %s%n", nombre, e.getMessage());
            fallidas++;
        }
    }
}
