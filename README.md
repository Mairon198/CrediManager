# CrediManager

Sistema web para una empresa de préstamos. Java 17 + Jakarta EE (Servlets/JSP), Apache Tomcat, SQL Server.

## Cómo correr el proyecto

1. Abrir la carpeta en NetBeans (es un proyecto Maven) y configurar Tomcat como servidor.
2. Crear la base de datos `CrediManager` en SQL Server con los scripts de la carpeta `database/`.
3. Copiar `src/main/resources/db.properties.example` como `src/main/resources/db.properties` y poner usuario y contraseña de SQL Server. Este archivo **no se sube a GitHub**.
4. En SQL Server Configuration Manager, verificar que TCP/IP esté habilitado y el puerto sea 1433.
5. Probar la conexión: clic derecho en `src/test/java/.../PruebaDAO.java` → **Run File**.
6. Ejecutar el proyecto (Run). Abre en `http://localhost:8080/CrediManager`.

## Arquitectura

```
JSP (vista) → Servlet (controller) → Service (reglas de negocio) → DAO → SQL Server
```

| Paquete | Qué contiene | Responsable |
| --- | --- | --- |
| `config` | `Conexion`, `Constantes` | Integrante 1 |
| `model` | Clases entidad (una por tabla) | Integrante 1 |
| `dao` | `ICrud<T>` y un DAO por tabla | Integrante 1 |
| `util` | `PasswordUtil`, `FechaUtil`, `Validador`, `FormatoMoneda` | Integrante 1 |
| `service` | Reglas de negocio | Integrantes 2 y 3 |
| `controller` | Servlets | Integrantes 2 y 3 |
| `filter` | `AuthFilter` | Integrante 2 |
| `webapp/views` | JSP | Integrante 3 |

Reglas:

- Los Servlets llaman a un **Service**, nunca a un DAO directamente.
- No hay SQL fuera de los DAO ni lógica de negocio en las JSP.
- El dinero siempre es `BigDecimal`; las fechas, `LocalDate`.
- Los DAO lanzan `DAOException` (no hay que declarar `throws SQLException`); el Servlet la atrapa y muestra `e.getMessage()`.
- Roles, estados y métodos de pago se escriben con `Constantes`, nunca con texto suelto.

## Métodos útiles de los DAO (además del CRUD)

| DAO | Métodos extra |
| --- | --- |
| `UsuarioDAO` | `buscarPorUsuario`, `existeUsuario`, `cambiarContrasena`, `listarRoles` |
| `ClienteDAO` | `buscarPorDni`, `existeDni`, `buscar(texto, pagina, tamano)`, `contar(texto)` |
| `TipoPrestamoDAO` | `existeNombre` |
| `PrestamoDAO` | `listarPorCliente`, `listarPorEstado`, `actualizarEstado`, `aprobarConPlan` (transacción), `contarPorEstado`, `sumarMontoPorEstado` |
| `CuotaDAO` | `listarPorPrestamo`, `listarVencidas`, `siguientePendiente`, `contarNoPagadas`, `actualizarEstado`, `marcarVencidas`, `insertarLote` |
| `PagoDAO` | `registrarPago` (transacción), `listarPorCuota`, `listarPorPrestamo`, `listarPorFechas`, `totalCobrado` |
| `GarantiaDAO`, `FiadorDAO` | `listarPorPrestamo` |

## Base de datos: nombres que esperan los DAO

Las tablas y columnas deben llamarse exactamente así. Los IDs son `INT IDENTITY(1,1) PRIMARY KEY`.

| Tabla | Columnas |
| --- | --- |
| `rol` | `id_rol`, `nombre` |
| `usuario` | `id_usuario`, `nombre`, `usuario` (único), `contrasena_hash` (NVARCHAR(200)), `id_rol` → rol, `activo` (BIT) |
| `cliente` | `id_cliente`, `dni` (único, 13 dígitos sin guiones), `nombres`, `apellidos`, `telefono`, `direccion`, `correo`, `ocupacion`, `ingreso_mensual` (DECIMAL(12,2)) |
| `tipo_prestamo` | `id_tipo`, `nombre` (único), `tasa_interes_anual` (DECIMAL(5,2)), `plazo_max_meses` (INT), `monto_max` (DECIMAL(12,2)), `porcentaje_mora` (DECIMAL(5,2)) |
| `prestamo` | `id_prestamo`, `id_cliente` → cliente, `id_tipo` → tipo_prestamo, `id_usuario` → usuario, `monto` (DECIMAL(12,2)), `plazo_meses` (INT), `tasa` (DECIMAL(5,2)), `cuota` (DECIMAL(12,2), NULL), `fecha_solicitud` (DATE), `fecha_aprobacion` (DATE, NULL), `estado` (VARCHAR(20)) |
| `cuota` | `id_cuota`, `id_prestamo` → prestamo, `numero` (INT), `fecha_vencimiento` (DATE), `capital`, `interes`, `monto_total`, `saldo` (DECIMAL(12,2)), `estado` (VARCHAR(20)) |
| `pago` | `id_pago`, `id_cuota` → cuota, `id_usuario` → usuario, `fecha_pago` (DATE), `monto_pagado` (DECIMAL(12,2)), `mora` (DECIMAL(12,2), default 0), `metodo_pago` (VARCHAR(20)) |
| `garantia` | `id_garantia`, `id_prestamo` → prestamo, `tipo` (VARCHAR(20)), `descripcion`, `valor_estimado` (DECIMAL(12,2)) |
| `fiador` | `id_fiador`, `id_prestamo` → prestamo, `dni`, `nombres`, `telefono`, `ingreso_mensual` (DECIMAL(12,2)) |

Valores de texto que usa el código (ver `Constantes`):

- `rol.nombre`: `Administrador`, `Asesor`, `Cajero`
- `prestamo.estado`: `SOLICITADO`, `APROBADO`, `RECHAZADO`, `ACTIVO`, `EN_MORA`, `CANCELADO`
- `cuota.estado`: `PENDIENTE`, `PAGADA`, `VENCIDA`
- `pago.metodo_pago`: `EFECTIVO`, `TRANSFERENCIA`, `TARJETA`
- `garantia.tipo`: `HIPOTECARIA`, `PRENDARIA`, `OTRA`

Para insertar usuarios de prueba, generar el hash de la contraseña ejecutando `PasswordUtil.java` (Run File): imprime el hash de `admin123`, `asesor123` y `cajero123`.

## Trabajo con Git

- `main`: versión estable. `develop`: integración diaria.
- Una rama por tarea: `feature/prestamos-servicio`, `feature/vistas-clientes`, etc.
- Todo entra a `develop` por Pull Request revisado por otro integrante.
- Antes de abrir un PR: `git pull origin develop`, resolver conflictos y verificar que compila.
