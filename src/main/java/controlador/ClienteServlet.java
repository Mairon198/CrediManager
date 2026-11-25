package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * CRUD de clientes con el parámetro accion (listar, nuevo, guardar, editar, desactivar).
 * <p>
 * Ruta: {@code /clientes}. Esqueleto creado por Integrante 1. Responsable: Integrante 2.
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/clientes"})
public class ClienteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // TODO Integrante 2: implementar doGet y doPost
}
