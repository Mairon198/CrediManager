package com.mycompany.credimanager.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * Simulador de préstamo: calcula la cuota y el plan sin guardar nada (usa AmortizacionService).
 * <p>
 * Ruta: {@code /simulador}. Esqueleto creado por Integrante 1. Pendiente de implementar
 * {@code doGet} y {@code doPost}.
 *
 * @author Integrante 3
 */
@WebServlet(name = "SimuladorServlet", urlPatterns = {"/simulador"})
public class SimuladorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // TODO Integrante 3: implementar doGet y doPost
}
