# CrediManager

Sistema web para la gestión de una empresa de **préstamos**: clientes, tipos de préstamo,
solicitudes, aprobación, desembolso con plan de pagos, cobro de cuotas con mora, garantías,
fiadores y reportes.

Sigue los mismos lineamientos del proyecto **AutoTech Manager**.

## Tecnologías

- Java 17 (Maven, NetBeans) con Jakarta EE: Servlets + JSP + JSTL, Apache Tomcat
- JDBC con Microsoft SQL Server
- Arquitectura por capas: presentación (`controlador` + JSP), negocio (`servicio`) y datos (`dao`, `conexion`)

## Cómo ejecutarlo

1. En SSMS ejecutar `database/CrediManagerDB.sql` sobre la base **CrediManager** (F5).
   Crea tablas, funciones, triggers, procedimientos, vistas y datos de prueba.
2. Copiar `src/main/resources/config.properties.example` como `config.properties` (misma carpeta)
   y escribir los datos de conexión.
3. Probar la conexión: clic derecho en `src/test/java/prueba/PruebaConexion.java` → *Run File*.
4. Abrir el proyecto en NetBeans, hacer *Clean and Build* y ejecutar en Tomcat.
5. Usuarios de prueba: `admin` / `Admin2026` (ADMIN), `asesor` / `Asesor2026` (ASESOR),
   `cajero` / `Cajero2026` (CAJERO). En la base solo se guarda su huella cifrada (PBKDF2-SHA256).

## Estructura

```
database/   Script SQL (tablas, funciones, triggers, procedimientos, vistas y datos de prueba)
src/main/java
├── conexion      ConexionDB
├── dao           Contratos de persistencia (y dao/impl con JDBC)
├── servicio      Reglas de negocio
├── controlador   Servlets
├── filtro        Control de sesión y permisos por rol
├── modelo        Entidades y enums
├── excepciones   Excepciones del sistema
└── util          Configuración, validaciones, formato, sesión y cifrado de contraseñas
src/main/webapp   Vistas JSP, includes y recursos (css, js, img)
```

## Base de datos

- Tablas en PascalCase (`dbo.Prestamos`), columnas en snake_case (`prestamo_id`).
- Flujo del préstamo: `SOLICITADO → APROBADO → ACTIVO ↔ EN_MORA → CANCELADO`
  (o `RECHAZADO`). Lo validan los procedimientos y el enum `EstadoPrestamo`.
- Procedimientos: `sp_RegistrarSolicitud`, `sp_AprobarPrestamo`, `sp_DesembolsarPrestamo`
  (genera las cuotas), `sp_RegistrarPago`, `sp_CambiarEstadoPrestamo`, `sp_ActualizarMora`.
- La cuota se calcula con el sistema francés (`fn_CalcularCuota`) y no puede pasar del 40 %
  del ingreso mensual del cliente.

## Trabajo con Git

- `main`: rama principal del proyecto.
- Una rama por tarea, creada desde `main`: `feature/prestamos-servicio`, `feature/vistas-clientes`...
- Todo entra a `main` por Pull Request revisado por otro integrante.
- Antes de empezar a trabajar: `git pull origin main`.
