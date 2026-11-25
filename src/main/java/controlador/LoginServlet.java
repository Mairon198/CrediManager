package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * Muestra login.jsp (GET), inicia sesión con AutenticacionService (POST) y cierra sesión (?accion=salir).
 * <p>
 * Ruta: {@code /login}. Esqueleto creado por Integrante 1. Responsable: Integrante 2.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // TODO Integrante 2: implementar doGet y doPost
}
