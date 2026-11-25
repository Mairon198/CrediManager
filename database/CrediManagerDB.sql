/* =====================================================================
   CrediManager - Base de datos CrediManager (SQL Server)
   Sistema de gestión de préstamos
   ---------------------------------------------------------------------
   Convención de nombres (la misma que usan los DAO del proyecto):
     - tablas en PascalCase:   dbo.Clientes, dbo.Prestamos, dbo.Cuotas ...
     - columnas en snake_case:  cliente_id, monto_cuota, fecha_registro ...
   ---------------------------------------------------------------------
   Cómo ejecutarlo:
   1. Conectarse al servidor con SSMS y abrir este archivo.
   2. Ejecutar el archivo completo (F5). Trabaja sobre la base CrediManager.
   ATENCIÓN: la sección 0 BORRA todas las tablas y las vuelve a crear,
   así que el script se puede ejecutar varias veces durante el desarrollo.
   ---------------------------------------------------------------------
   Contenido:
     0. Limpieza
     1. Catálogos (estados del préstamo y transiciones permitidas)
     2. Usuarios del sistema (inicio de sesión)
     3. Clientes
     4. Tipos de préstamo
     5. Préstamos, garantías y fiadores
     6. Plan de pagos (cuotas) y pagos
     7. Auditoría
     8. Funciones
     9. Triggers
    10. Procedimientos almacenados
    11. Vistas administrativas
    12. Datos de prueba
    13. Consultas de ejemplo (JOIN, GROUP BY, subconsultas)
   ===================================================================== */

USE CrediManager;
GO


/* =====================================================================
   0. LIMPIEZA (orden inverso a las dependencias)
   ===================================================================== */
DROP VIEW IF EXISTS dbo.vw_ResumenTiposPrestamo;
DROP VIEW IF EXISTS dbo.vw_PagosDetalle;
DROP VIEW IF EXISTS dbo.vw_CarteraPorEstado;
DROP VIEW IF EXISTS dbo.vw_CuotasVencidas;
DROP VIEW IF EXISTS dbo.vw_PrestamosResumen;
DROP PROCEDURE IF EXISTS dbo.sp_HistorialCliente;
DROP PROCEDURE IF EXISTS dbo.sp_ActualizarMora;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarPago;
DROP PROCEDURE IF EXISTS dbo.sp_DesembolsarPrestamo;
DROP PROCEDURE IF EXISTS dbo.sp_AprobarPrestamo;
DROP PROCEDURE IF EXISTS dbo.sp_CambiarEstadoPrestamo;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarSolicitud;
DROP FUNCTION IF EXISTS dbo.fn_ResumenPrestamo;
DROP FUNCTION IF EXISTS dbo.fn_CalcularMora;
DROP FUNCTION IF EXISTS dbo.fn_CalcularCuota;

DROP TABLE IF EXISTS dbo.Auditoria;
DROP TABLE IF EXISTS dbo.Pagos;
DROP TABLE IF EXISTS dbo.Cuotas;
DROP TABLE IF EXISTS dbo.Fiadores;
DROP TABLE IF EXISTS dbo.Garantias;
DROP TABLE IF EXISTS dbo.Prestamos;
DROP TABLE IF EXISTS dbo.TiposPrestamo;
DROP TABLE IF EXISTS dbo.Clientes;
DROP TABLE IF EXISTS dbo.Usuarios;
DROP TABLE IF EXISTS dbo.TransicionesEstadoPrestamo;
DROP TABLE IF EXISTS dbo.EstadosPrestamo;
GO


/* =====================================================================
   1. CATÁLOGOS
   ===================================================================== */

-- Estados posibles de un préstamo (mismo flujo que el enum EstadoPrestamo de Java)
CREATE TABLE dbo.EstadosPrestamo (
    codigo       VARCHAR(20)  NOT NULL CONSTRAINT PK_EstadosPrestamo PRIMARY KEY,
    descripcion  VARCHAR(50)  NOT NULL,
    secuencia    TINYINT      NOT NULL,
    es_final     BIT          NOT NULL CONSTRAINT DF_EstadosPrestamo_EsFinal DEFAULT 0
);
GO

-- Cambios de estado permitidos (tabla puente estado -> estado)
CREATE TABLE dbo.TransicionesEstadoPrestamo (
    estado_actual     VARCHAR(20) NOT NULL CONSTRAINT FK_Trans_Actual    REFERENCES dbo.EstadosPrestamo(codigo),
    estado_siguiente  VARCHAR(20) NOT NULL CONSTRAINT FK_Trans_Siguiente REFERENCES dbo.EstadosPrestamo(codigo),
    CONSTRAINT PK_TransicionesEstadoPrestamo PRIMARY KEY (estado_actual, estado_siguiente),
    CONSTRAINT CK_Trans_Distinto CHECK (estado_actual <> estado_siguiente)
);
GO


/* =====================================================================
   2. USUARIOS DEL SISTEMA (inicio de sesión)
   ---------------------------------------------------------------------
   Las contraseñas NO se guardan: se guarda su huella PBKDF2-HMAC-SHA256
   (sal aleatoria de 16 bytes, 120000 iteraciones). La aplicación calcula la
   huella de lo que escribe el usuario y la compara con la guardada.
   Tras 5 intentos fallidos seguidos el usuario queda bloqueado.
   Roles: ADMIN (todo), ASESOR (clientes y préstamos), CAJERO (cobros).
   ===================================================================== */
CREATE TABLE dbo.Usuarios (
    usuario_id         INT IDENTITY(1,1) CONSTRAINT PK_Usuarios PRIMARY KEY,
    nombre_usuario     VARCHAR(30)   NOT NULL CONSTRAINT UQ_Usuarios_Nombre UNIQUE,
    nombre_completo    VARCHAR(100)  NOT NULL,
    rol                VARCHAR(20)   NOT NULL CONSTRAINT DF_Usuarios_Rol DEFAULT 'ASESOR',
    sal                CHAR(32)      NOT NULL,
    hash_contrasena    CHAR(64)      NOT NULL,
    iteraciones        INT           NOT NULL,
    intentos_fallidos  INT           NOT NULL CONSTRAINT DF_Usuarios_Intentos DEFAULT 0,
    ultimo_acceso      DATETIME2(0)  NULL,
    activo             BIT           NOT NULL CONSTRAINT DF_Usuarios_Activo DEFAULT 1,
    fecha_registro     DATETIME2(0)  NOT NULL CONSTRAINT DF_Usuarios_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Usuarios_Rol      CHECK (rol IN ('ADMIN','ASESOR','CAJERO')),
    CONSTRAINT CK_Usuarios_Intentos CHECK (intentos_fallidos >= 0),
    CONSTRAINT CK_Usuarios_Iter     CHECK (iteraciones >= 10000)
);
GO


/* =====================================================================
   3. CLIENTES
   ===================================================================== */
CREATE TABLE dbo.Clientes (
    cliente_id       INT IDENTITY(1,1) CONSTRAINT PK_Clientes PRIMARY KEY,
    identificacion   VARCHAR(15)   NOT NULL CONSTRAINT UQ_Clientes_Identificacion UNIQUE,   -- 0801-1990-12345
    nombre           VARCHAR(60)   NOT NULL,
    apellido         VARCHAR(60)   NOT NULL,
    telefono         VARCHAR(20)   NULL,
    correo           VARCHAR(120)  NULL,
    direccion        VARCHAR(200)  NULL,
    ocupacion        VARCHAR(100)  NULL,
    ingreso_mensual  DECIMAL(12,2) NOT NULL CONSTRAINT DF_Clientes_Ingreso DEFAULT 0,
    activo           BIT           NOT NULL CONSTRAINT DF_Clientes_Activo  DEFAULT 1,
    fecha_registro   DATETIME2(0)  NOT NULL CONSTRAINT DF_Clientes_Fecha   DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Clientes_Identificacion CHECK (identificacion LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_Clientes_Nombre   CHECK (LEN(LTRIM(nombre)) > 0 AND LEN(LTRIM(apellido)) > 0),
    CONSTRAINT CK_Clientes_Correo   CHECK (correo IS NULL OR correo = '' OR correo LIKE '%_@_%._%'),
    CONSTRAINT CK_Clientes_Ingreso  CHECK (ingreso_mensual >= 0)
);
GO


/* =====================================================================
   4. TIPOS DE PRÉSTAMO
   ---------------------------------------------------------------------
   porcentaje_mora: porcentaje mensual sobre la cuota atrasada,
   prorrateado por día (mora = cuota * % / 100 * días / 30).
   ===================================================================== */
CREATE TABLE dbo.TiposPrestamo (
    tipo_prestamo_id    INT IDENTITY(1,1) CONSTRAINT PK_TiposPrestamo PRIMARY KEY,
    nombre              VARCHAR(60)   NOT NULL CONSTRAINT UQ_TiposPrestamo_Nombre UNIQUE,
    descripcion         VARCHAR(200)  NULL,
    tasa_interes_anual  DECIMAL(5,2)  NOT NULL,
    monto_minimo        DECIMAL(12,2) NOT NULL,
    monto_maximo        DECIMAL(12,2) NOT NULL,
    plazo_minimo_meses  INT           NOT NULL,
    plazo_maximo_meses  INT           NOT NULL,
    porcentaje_mora     DECIMAL(5,2)  NOT NULL CONSTRAINT DF_TiposPrestamo_Mora     DEFAULT 5,
    requiere_garantia   BIT           NOT NULL CONSTRAINT DF_TiposPrestamo_Garantia DEFAULT 0,
    activo              BIT           NOT NULL CONSTRAINT DF_TiposPrestamo_Activo   DEFAULT 1,
    fecha_registro      DATETIME2(0)  NOT NULL CONSTRAINT DF_TiposPrestamo_Fecha    DEFAULT SYSDATETIME(),
    CONSTRAINT CK_TiposPrestamo_Tasa   CHECK (tasa_interes_anual BETWEEN 0 AND 100),
    CONSTRAINT CK_TiposPrestamo_Montos CHECK (monto_minimo > 0 AND monto_maximo >= monto_minimo),
    CONSTRAINT CK_TiposPrestamo_Plazos CHECK (plazo_minimo_meses > 0 AND plazo_maximo_meses >= plazo_minimo_meses),
    CONSTRAINT CK_TiposPrestamo_Mora   CHECK (porcentaje_mora BETWEEN 0 AND 100)
);
GO


/* =====================================================================
   5. PRÉSTAMOS, GARANTÍAS Y FIADORES
   ---------------------------------------------------------------------
   tasa_interes_anual y porcentaje_mora se copian del tipo de préstamo al
   registrar la solicitud: si después cambia el tipo, el préstamo conserva
   las condiciones con que se pactó (dato histórico).
   ===================================================================== */
CREATE TABLE dbo.Prestamos (
    prestamo_id         INT IDENTITY(1,1) CONSTRAINT PK_Prestamos PRIMARY KEY,
    numero_prestamo     AS ('PRE-' + RIGHT('000000' + CAST(prestamo_id AS VARCHAR(10)), 6)) PERSISTED,
    cliente_id          INT           NOT NULL CONSTRAINT FK_Prestamos_Clientes REFERENCES dbo.Clientes(cliente_id),
    tipo_prestamo_id    INT           NOT NULL CONSTRAINT FK_Prestamos_Tipos    REFERENCES dbo.TiposPrestamo(tipo_prestamo_id),
    usuario_id          INT           NOT NULL CONSTRAINT FK_Prestamos_Usuarios REFERENCES dbo.Usuarios(usuario_id),  -- quien registró la solicitud
    monto               DECIMAL(12,2) NOT NULL,
    plazo_meses         INT           NOT NULL,
    tasa_interes_anual  DECIMAL(5,2)  NOT NULL,
    porcentaje_mora     DECIMAL(5,2)  NOT NULL,
    cuota_mensual       DECIMAL(12,2) NULL,                 -- se calcula al aprobar
    destino             VARCHAR(200)  NULL,                 -- para qué se usará el dinero
    estado              VARCHAR(20)   NOT NULL CONSTRAINT DF_Prestamos_Estado DEFAULT 'SOLICITADO'
                                      CONSTRAINT FK_Prestamos_Estado REFERENCES dbo.EstadosPrestamo(codigo),
    fecha_solicitud     DATE          NOT NULL CONSTRAINT DF_Prestamos_FechaSol DEFAULT CAST(SYSDATETIME() AS DATE),
    fecha_aprobacion    DATE          NULL,
    fecha_desembolso    DATE          NULL,
    observaciones       VARCHAR(300)  NULL,
    activo              BIT           NOT NULL CONSTRAINT DF_Prestamos_Activo DEFAULT 1,
    fecha_registro      DATETIME2(0)  NOT NULL CONSTRAINT DF_Prestamos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Prestamos_Numero      UNIQUE (numero_prestamo),
    CONSTRAINT CK_Prestamos_Monto       CHECK (monto > 0),
    CONSTRAINT CK_Prestamos_Plazo       CHECK (plazo_meses > 0),
    CONSTRAINT CK_Prestamos_Tasa        CHECK (tasa_interes_anual BETWEEN 0 AND 100),
    CONSTRAINT CK_Prestamos_Cuota       CHECK (cuota_mensual IS NULL OR cuota_mensual > 0),
    CONSTRAINT CK_Prestamos_Aprobacion  CHECK (fecha_aprobacion IS NULL OR fecha_aprobacion >= fecha_solicitud),
    CONSTRAINT CK_Prestamos_Desembolso  CHECK (fecha_desembolso IS NULL OR (fecha_aprobacion IS NOT NULL AND fecha_desembolso >= fecha_aprobacion))
);
GO
CREATE INDEX IX_Prestamos_Cliente ON dbo.Prestamos(cliente_id);
CREATE INDEX IX_Prestamos_Estado  ON dbo.Prestamos(estado);
GO

CREATE TABLE dbo.Garantias (
    garantia_id     INT IDENTITY(1,1) CONSTRAINT PK_Garantias PRIMARY KEY,
    prestamo_id     INT           NOT NULL CONSTRAINT FK_Garantias_Prestamo REFERENCES dbo.Prestamos(prestamo_id),
    tipo            VARCHAR(15)   NOT NULL,
    descripcion     VARCHAR(300)  NOT NULL,
    valor_estimado  DECIMAL(12,2) NOT NULL,
    activo          BIT           NOT NULL CONSTRAINT DF_Garantias_Activo DEFAULT 1,
    fecha_registro  DATETIME2(0)  NOT NULL CONSTRAINT DF_Garantias_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Garantias_Tipo  CHECK (tipo IN ('HIPOTECARIA','PRENDARIA','DOCUMENTARIA','OTRA')),
    CONSTRAINT CK_Garantias_Valor CHECK (valor_estimado > 0)
);
GO
CREATE INDEX IX_Garantias_Prestamo ON dbo.Garantias(prestamo_id);
GO

CREATE TABLE dbo.Fiadores (
    fiador_id        INT IDENTITY(1,1) CONSTRAINT PK_Fiadores PRIMARY KEY,
    prestamo_id      INT           NOT NULL CONSTRAINT FK_Fiadores_Prestamo REFERENCES dbo.Prestamos(prestamo_id),
    identificacion   VARCHAR(15)   NOT NULL,
    nombre           VARCHAR(60)   NOT NULL,
    apellido         VARCHAR(60)   NOT NULL,
    telefono         VARCHAR(20)   NULL,
    direccion        VARCHAR(200)  NULL,
    parentesco       VARCHAR(40)   NULL,
    ingreso_mensual  DECIMAL(12,2) NOT NULL CONSTRAINT DF_Fiadores_Ingreso DEFAULT 0,
    activo           BIT           NOT NULL CONSTRAINT DF_Fiadores_Activo  DEFAULT 1,
    fecha_registro   DATETIME2(0)  NOT NULL CONSTRAINT DF_Fiadores_Fecha   DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Fiadores_Prestamo_Identificacion UNIQUE (prestamo_id, identificacion),   -- el mismo fiador no se repite en un préstamo
    CONSTRAINT CK_Fiadores_Identificacion CHECK (identificacion LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_Fiadores_Ingreso CHECK (ingreso_mensual >= 0)
);
GO


/* =====================================================================
   6. PLAN DE PAGOS (CUOTAS) Y PAGOS
   ---------------------------------------------------------------------
   Las cuotas las genera sp_DesembolsarPrestamo (sistema francés, cuota fija).
   Cada pago liquida una cuota completa (cuota + mora); por eso un pago
   tiene una sola cuota y una cuota un solo pago.
   ===================================================================== */
CREATE TABLE dbo.Cuotas (
    cuota_id           INT IDENTITY(1,1) CONSTRAINT PK_Cuotas PRIMARY KEY,
    prestamo_id        INT           NOT NULL CONSTRAINT FK_Cuotas_Prestamo REFERENCES dbo.Prestamos(prestamo_id),
    numero_cuota       SMALLINT      NOT NULL,
    fecha_vencimiento  DATE          NOT NULL,
    capital            DECIMAL(12,2) NOT NULL,
    interes            DECIMAL(12,2) NOT NULL,
    monto_cuota        DECIMAL(12,2) NOT NULL,
    saldo_restante     DECIMAL(12,2) NOT NULL,       -- capital que queda después de pagar esta cuota
    estado             VARCHAR(15)   NOT NULL CONSTRAINT DF_Cuotas_Estado DEFAULT 'PENDIENTE',
    fecha_pago         DATE          NULL,
    CONSTRAINT UQ_Cuotas_Numero   UNIQUE (prestamo_id, numero_cuota),
    -- Permite que Pagos verifique que la cuota pertenece al préstamo
    CONSTRAINT UQ_Cuotas_Prestamo UNIQUE (cuota_id, prestamo_id),
    CONSTRAINT CK_Cuotas_Numero   CHECK (numero_cuota > 0),
    CONSTRAINT CK_Cuotas_Montos   CHECK (capital >= 0 AND interes >= 0 AND saldo_restante >= 0 AND monto_cuota = capital + interes),
    CONSTRAINT CK_Cuotas_Estado   CHECK (estado IN ('PENDIENTE','PAGADA','VENCIDA')),
    CONSTRAINT CK_Cuotas_Pago     CHECK ((estado = 'PAGADA' AND fecha_pago IS NOT NULL) OR (estado <> 'PAGADA' AND fecha_pago IS NULL))
);
GO
CREATE INDEX IX_Cuotas_Vencimiento ON dbo.Cuotas(estado, fecha_vencimiento);
GO

CREATE TABLE dbo.Pagos (
    pago_id       INT IDENTITY(1,1) CONSTRAINT PK_Pagos PRIMARY KEY,
    numero_recibo AS ('REC-' + RIGHT('000000' + CAST(pago_id AS VARCHAR(10)), 6)) PERSISTED,
    prestamo_id   INT           NOT NULL,
    cuota_id      INT           NOT NULL,
    usuario_id    INT           NOT NULL CONSTRAINT FK_Pagos_Usuarios REFERENCES dbo.Usuarios(usuario_id),   -- cajero
    fecha_pago    DATETIME2(0)  NOT NULL CONSTRAINT DF_Pagos_Fecha DEFAULT SYSDATETIME(),
    monto_cuota   DECIMAL(12,2) NOT NULL,
    mora          DECIMAL(12,2) NOT NULL CONSTRAINT DF_Pagos_Mora  DEFAULT 0,
    total         AS (monto_cuota + mora) PERSISTED,
    dias_atraso   INT           NOT NULL CONSTRAINT DF_Pagos_Dias  DEFAULT 0,
    metodo        VARCHAR(15)   NOT NULL,
    referencia    VARCHAR(60)   NULL,
    CONSTRAINT FK_Pagos_Cuota      FOREIGN KEY (cuota_id, prestamo_id) REFERENCES dbo.Cuotas(cuota_id, prestamo_id),
    CONSTRAINT UQ_Pagos_Cuota      UNIQUE (cuota_id),            -- una cuota no se cobra dos veces
    CONSTRAINT UQ_Pagos_Recibo     UNIQUE (numero_recibo),
    CONSTRAINT CK_Pagos_Monto      CHECK (monto_cuota > 0),
    CONSTRAINT CK_Pagos_Mora       CHECK (mora >= 0),
    CONSTRAINT CK_Pagos_Dias       CHECK (dias_atraso >= 0),
    CONSTRAINT CK_Pagos_Metodo     CHECK (metodo IN ('EFECTIVO','TARJETA','TRANSFERENCIA'))
);
GO
CREATE INDEX IX_Pagos_Prestamo ON dbo.Pagos(prestamo_id);
CREATE INDEX IX_Pagos_Fecha    ON dbo.Pagos(fecha_pago);
GO


/* =====================================================================
   7. AUDITORÍA
   ===================================================================== */
CREATE TABLE dbo.Auditoria (
    auditoria_id    BIGINT IDENTITY(1,1) CONSTRAINT PK_Auditoria PRIMARY KEY,
    tabla           VARCHAR(50)   NOT NULL,
    operacion       VARCHAR(10)   NOT NULL,
    registro_id     INT           NOT NULL,
    campo           VARCHAR(50)   NOT NULL,
    valor_anterior  VARCHAR(200)  NULL,
    valor_nuevo     VARCHAR(200)  NULL,
    usuario         VARCHAR(60)   NOT NULL CONSTRAINT DF_Auditoria_Usuario DEFAULT SUSER_SNAME(),
    fecha           DATETIME2(0)  NOT NULL CONSTRAINT DF_Auditoria_Fecha   DEFAULT SYSDATETIME()
);
GO


/* =====================================================================
   8. FUNCIONES
   ===================================================================== */

-- Cuota mensual fija (sistema francés): C = P * i(1+i)^n / ((1+i)^n - 1), con i = tasa anual / 12 / 100
-- AmortizacionService (Java) debe usar la misma fórmula y el mismo redondeo para que el simulador coincida.
CREATE OR ALTER FUNCTION dbo.fn_CalcularCuota (
    @monto        DECIMAL(12,2),
    @tasa_anual   DECIMAL(5,2),
    @plazo_meses  INT
)
RETURNS DECIMAL(12,2)
AS
BEGIN
    IF @monto IS NULL OR @tasa_anual IS NULL OR @plazo_meses IS NULL OR @plazo_meses <= 0
        RETURN NULL;

    DECLARE @i FLOAT = CAST(@tasa_anual AS FLOAT) / 1200.0;
    IF @i = 0
        RETURN CAST(ROUND(@monto / @plazo_meses, 2) AS DECIMAL(12,2));

    DECLARE @factor FLOAT = POWER(1.0E0 + @i, @plazo_meses);
    RETURN CAST(ROUND(CAST(@monto AS FLOAT) * @i * @factor / (@factor - 1.0E0), 2) AS DECIMAL(12,2));
END;
GO

-- Mora de una cuota atrasada: porcentaje mensual prorrateado por día
CREATE OR ALTER FUNCTION dbo.fn_CalcularMora (
    @monto_cuota      DECIMAL(12,2),
    @porcentaje_mora  DECIMAL(5,2),
    @dias_atraso      INT
)
RETURNS DECIMAL(12,2)
AS
BEGIN
    IF @monto_cuota IS NULL OR @porcentaje_mora IS NULL OR @dias_atraso IS NULL OR @dias_atraso <= 0
        RETURN 0;
    RETURN CAST(ROUND(@monto_cuota * @porcentaje_mora / 100.0 * @dias_atraso / 30.0, 2) AS DECIMAL(12,2));
END;
GO

-- Resumen de un préstamo: lo que se debe pagar, lo pagado y lo pendiente.
-- El saldo no se guarda en la tabla (es un dato derivado); se calcula aquí.
CREATE OR ALTER FUNCTION dbo.fn_ResumenPrestamo (@prestamo_id INT)
RETURNS TABLE
AS
RETURN
    SELECT
        cu.total_a_pagar,
        cu.total_pagado,
        pg.mora_pagada,
        cu.capital_pendiente,
        cu.saldo_pendiente,
        cu.cuotas_pagadas,
        cu.cuotas_pendientes,
        cu.cuotas_vencidas,
        cu.proximo_vencimiento
    FROM (
        SELECT
            ISNULL(SUM(c.monto_cuota), 0)                                         AS total_a_pagar,
            ISNULL(SUM(CASE WHEN c.estado = 'PAGADA'  THEN c.monto_cuota END), 0) AS total_pagado,
            ISNULL(SUM(CASE WHEN c.estado <> 'PAGADA' THEN c.capital END), 0)     AS capital_pendiente,
            ISNULL(SUM(CASE WHEN c.estado <> 'PAGADA' THEN c.monto_cuota END), 0) AS saldo_pendiente,
            COUNT(CASE WHEN c.estado = 'PAGADA'  THEN 1 END)                      AS cuotas_pagadas,
            COUNT(CASE WHEN c.estado <> 'PAGADA' THEN 1 END)                      AS cuotas_pendientes,
            COUNT(CASE WHEN c.estado = 'VENCIDA' THEN 1 END)                      AS cuotas_vencidas,
            MIN(CASE WHEN c.estado <> 'PAGADA' THEN c.fecha_vencimiento END)      AS proximo_vencimiento
        FROM dbo.Cuotas c
        WHERE c.prestamo_id = @prestamo_id
    ) cu
    CROSS JOIN (
        SELECT ISNULL(SUM(p.mora), 0) AS mora_pagada
        FROM dbo.Pagos p
        WHERE p.prestamo_id = @prestamo_id
    ) pg;
GO


/* =====================================================================
   9. TRIGGERS
   ===================================================================== */

-- Auditoría: cada cambio de estado de un préstamo
CREATE OR ALTER TRIGGER dbo.trg_Prestamos_AuditoriaEstado
ON dbo.Prestamos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(estado) RETURN;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'Prestamos', 'UPDATE', i.prestamo_id, 'estado', d.estado, i.estado
    FROM inserted i INNER JOIN deleted d ON d.prestamo_id = i.prestamo_id
    WHERE i.estado <> d.estado;
END;
GO

-- Las condiciones (cliente, tipo, monto, plazo, tasa) solo se cambian mientras la solicitud está en SOLICITADO
CREATE OR ALTER TRIGGER dbo.trg_Prestamos_ProtegerCondiciones
ON dbo.Prestamos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT (UPDATE(cliente_id) OR UPDATE(tipo_prestamo_id) OR UPDATE(monto) OR UPDATE(plazo_meses)
            OR UPDATE(tasa_interes_anual) OR UPDATE(porcentaje_mora)) RETURN;

    IF EXISTS (SELECT 1
               FROM inserted i INNER JOIN deleted d ON d.prestamo_id = i.prestamo_id
               WHERE d.estado <> 'SOLICITADO'
                 AND (i.cliente_id <> d.cliente_id OR i.tipo_prestamo_id <> d.tipo_prestamo_id
                      OR i.monto <> d.monto OR i.plazo_meses <> d.plazo_meses
                      OR i.tasa_interes_anual <> d.tasa_interes_anual OR i.porcentaje_mora <> d.porcentaje_mora))
    BEGIN
        THROW 50010, 'Las condiciones de un préstamo solo se pueden modificar mientras está en estado SOLICITADO.', 1;
    END
END;
GO

-- Auditoría: cambios de tasa o mora de un tipo de préstamo (los préstamos ya registrados no cambian)
CREATE OR ALTER TRIGGER dbo.trg_TiposPrestamo_Auditoria
ON dbo.TiposPrestamo
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'TiposPrestamo', 'UPDATE', i.tipo_prestamo_id, 'tasa_interes_anual',
           CAST(d.tasa_interes_anual AS VARCHAR(20)), CAST(i.tasa_interes_anual AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.tipo_prestamo_id = i.tipo_prestamo_id
    WHERE i.tasa_interes_anual <> d.tasa_interes_anual;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'TiposPrestamo', 'UPDATE', i.tipo_prestamo_id, 'porcentaje_mora',
           CAST(d.porcentaje_mora AS VARCHAR(20)), CAST(i.porcentaje_mora AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.tipo_prestamo_id = i.tipo_prestamo_id
    WHERE i.porcentaje_mora <> d.porcentaje_mora;
END;
GO


/* =====================================================================
   10. PROCEDIMIENTOS ALMACENADOS
   ---------------------------------------------------------------------
   Los errores de negocio se lanzan con THROW 5xxxx: los DAO los muestran
   tal cual al usuario (ver CrediDAOBase.error en Java).
   ===================================================================== */

-- 10.1 Registrar una solicitud de préstamo
--      Valida cliente y tipo activos, monto y plazo dentro de los límites del tipo,
--      y copia la tasa y la mora del tipo (condiciones pactadas).
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarSolicitud
    @cliente_id        INT,
    @tipo_prestamo_id  INT,
    @usuario_id        INT,
    @monto             DECIMAL(12,2),
    @plazo_meses       INT,
    @destino           VARCHAR(200) = NULL,
    @fecha_solicitud   DATE = NULL,
    @prestamo_id       INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @msg NVARCHAR(300);
    DECLARE @tasa DECIMAL(5,2), @mora DECIMAL(5,2), @min DECIMAL(12,2), @max DECIMAL(12,2),
            @pmin INT, @pmax INT, @tipo_activo BIT;

    SET @fecha_solicitud = ISNULL(@fecha_solicitud, CAST(SYSDATETIME() AS DATE));

    IF NOT EXISTS (SELECT 1 FROM dbo.Clientes WHERE cliente_id = @cliente_id AND activo = 1)
        THROW 50001, 'El cliente no existe o está inactivo.', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.Usuarios WHERE usuario_id = @usuario_id AND activo = 1)
        THROW 50002, 'El usuario que registra la solicitud no existe o está inactivo.', 1;

    SELECT @tasa = tasa_interes_anual, @mora = porcentaje_mora, @min = monto_minimo, @max = monto_maximo,
           @pmin = plazo_minimo_meses, @pmax = plazo_maximo_meses, @tipo_activo = activo
    FROM dbo.TiposPrestamo WHERE tipo_prestamo_id = @tipo_prestamo_id;

    IF @tipo_activo IS NULL OR @tipo_activo = 0
        THROW 50003, 'El tipo de préstamo no existe o está inactivo.', 1;
    IF @monto IS NULL OR @monto < @min OR @monto > @max
    BEGIN
        SET @msg = CONCAT('El monto debe estar entre ', @min, ' y ', @max, ' para este tipo de préstamo.');
        THROW 50004, @msg, 1;
    END
    IF @plazo_meses IS NULL OR @plazo_meses < @pmin OR @plazo_meses > @pmax
    BEGIN
        SET @msg = CONCAT('El plazo debe estar entre ', @pmin, ' y ', @pmax, ' meses para este tipo de préstamo.');
        THROW 50005, @msg, 1;
    END
    IF @fecha_solicitud > CAST(SYSDATETIME() AS DATE)
        THROW 50006, 'La fecha de solicitud no puede ser futura.', 1;

    INSERT INTO dbo.Prestamos (cliente_id, tipo_prestamo_id, usuario_id, monto, plazo_meses,
                               tasa_interes_anual, porcentaje_mora, destino, fecha_solicitud)
    VALUES (@cliente_id, @tipo_prestamo_id, @usuario_id, @monto, @plazo_meses,
            @tasa, @mora, @destino, @fecha_solicitud);

    SET @prestamo_id = SCOPE_IDENTITY();
END;
GO

-- 10.2 Cambiar el estado de un préstamo validando el flujo (rechazar, pasar a mora manualmente).
--      APROBADO, ACTIVO y CANCELADO tienen su propio procedimiento porque hacen más cosas.
CREATE OR ALTER PROCEDURE dbo.sp_CambiarEstadoPrestamo
    @prestamo_id    INT,
    @estado_nuevo   VARCHAR(20),
    @observaciones  VARCHAR(300) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @msg NVARCHAR(300), @estado VARCHAR(20);

    IF @estado_nuevo IN ('APROBADO','ACTIVO','CANCELADO')
        THROW 50030, 'Use sp_AprobarPrestamo, sp_DesembolsarPrestamo o sp_RegistrarPago para ese cambio de estado.', 1;
    IF @estado_nuevo = 'RECHAZADO' AND (@observaciones IS NULL OR LEN(LTRIM(@observaciones)) = 0)
        THROW 50031, 'Indique el motivo del rechazo.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @estado = estado FROM dbo.Prestamos WITH (UPDLOCK, HOLDLOCK)
        WHERE prestamo_id = @prestamo_id AND activo = 1;

        IF @estado IS NULL
            THROW 50020, 'El préstamo no existe o está inactivo.', 1;
        IF NOT EXISTS (SELECT 1 FROM dbo.TransicionesEstadoPrestamo
                       WHERE estado_actual = @estado AND estado_siguiente = @estado_nuevo)
        BEGIN
            SET @msg = CONCAT('No se puede pasar un préstamo de ', @estado, ' a ', @estado_nuevo, '.');
            THROW 50032, @msg, 1;
        END

        UPDATE dbo.Prestamos
        SET estado = @estado_nuevo,
            observaciones = COALESCE(@observaciones, observaciones)
        WHERE prestamo_id = @prestamo_id;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 10.3 Aprobar una solicitud
--      Revisa de nuevo los límites del tipo, que tenga garantía o fiador si el tipo lo exige
--      y que la cuota no pase del 40 % del ingreso mensual del cliente. Calcula la cuota.
CREATE OR ALTER PROCEDURE dbo.sp_AprobarPrestamo
    @prestamo_id       INT,
    @fecha_aprobacion  DATE = NULL,
    @observaciones     VARCHAR(300) = NULL,
    @cuota_mensual     DECIMAL(12,2) = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @msg NVARCHAR(300);
    DECLARE @estado VARCHAR(20), @monto DECIMAL(12,2), @plazo INT, @tasa DECIMAL(5,2),
            @tipo INT, @cliente INT, @fecha_sol DATE;
    DECLARE @min DECIMAL(12,2), @max DECIMAL(12,2), @pmin INT, @pmax INT, @requiere BIT;
    DECLARE @ingreso DECIMAL(12,2), @cliente_activo BIT;

    SET @fecha_aprobacion = ISNULL(@fecha_aprobacion, CAST(SYSDATETIME() AS DATE));

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @estado = estado, @monto = monto, @plazo = plazo_meses, @tasa = tasa_interes_anual,
               @tipo = tipo_prestamo_id, @cliente = cliente_id, @fecha_sol = fecha_solicitud
        FROM dbo.Prestamos WITH (UPDLOCK, HOLDLOCK)
        WHERE prestamo_id = @prestamo_id AND activo = 1;

        IF @estado IS NULL
            THROW 50020, 'El préstamo no existe o está inactivo.', 1;
        IF @estado <> 'SOLICITADO'
            THROW 50021, 'Solo se puede aprobar un préstamo en estado SOLICITADO.', 1;
        IF @fecha_aprobacion < @fecha_sol OR @fecha_aprobacion > CAST(SYSDATETIME() AS DATE)
            THROW 50022, 'La fecha de aprobación no puede ser anterior a la solicitud ni futura.', 1;

        SELECT @min = monto_minimo, @max = monto_maximo, @pmin = plazo_minimo_meses,
               @pmax = plazo_maximo_meses, @requiere = requiere_garantia
        FROM dbo.TiposPrestamo WHERE tipo_prestamo_id = @tipo;

        IF @monto < @min OR @monto > @max OR @plazo < @pmin OR @plazo > @pmax
            THROW 50023, 'El monto o el plazo ya no cumplen los límites del tipo de préstamo.', 1;

        IF @requiere = 1
           AND NOT EXISTS (SELECT 1 FROM dbo.Garantias WHERE prestamo_id = @prestamo_id AND activo = 1)
           AND NOT EXISTS (SELECT 1 FROM dbo.Fiadores  WHERE prestamo_id = @prestamo_id AND activo = 1)
            THROW 50024, 'Este tipo de préstamo exige registrar una garantía o un fiador antes de aprobarlo.', 1;

        SELECT @ingreso = ingreso_mensual, @cliente_activo = activo FROM dbo.Clientes WHERE cliente_id = @cliente;
        IF @cliente_activo = 0
            THROW 50025, 'El cliente está inactivo.', 1;

        SET @cuota_mensual = dbo.fn_CalcularCuota(@monto, @tasa, @plazo);

        IF @cuota_mensual > @ingreso * 0.40
        BEGIN
            SET @msg = CONCAT('La cuota (', @cuota_mensual, ') supera el 40 % del ingreso mensual del cliente (',
                              @ingreso, '). Reduzca el monto o aumente el plazo.');
            THROW 50026, @msg, 1;
        END

        UPDATE dbo.Prestamos
        SET estado = 'APROBADO',
            fecha_aprobacion = @fecha_aprobacion,
            cuota_mensual = @cuota_mensual,
            observaciones = COALESCE(@observaciones, observaciones)
        WHERE prestamo_id = @prestamo_id;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 10.4 Desembolsar un préstamo aprobado y generar su plan de pagos
--      En una sola transacción: pasa a ACTIVO y crea una cuota por mes.
--      Cada cuota: interés = saldo * i; capital = cuota - interés.
--      La última cuota liquida el saldo exacto (ajusta los centavos del redondeo).
CREATE OR ALTER PROCEDURE dbo.sp_DesembolsarPrestamo
    @prestamo_id       INT,
    @fecha_desembolso  DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @estado VARCHAR(20), @monto DECIMAL(12,2), @plazo INT, @tasa DECIMAL(5,2),
            @cuota DECIMAL(12,2), @fecha_apr DATE;
    DECLARE @i DECIMAL(18,10), @saldo DECIMAL(12,2), @interes DECIMAL(12,2), @capital DECIMAL(12,2), @n INT;

    SET @fecha_desembolso = ISNULL(@fecha_desembolso, CAST(SYSDATETIME() AS DATE));

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @estado = estado, @monto = monto, @plazo = plazo_meses, @tasa = tasa_interes_anual,
               @cuota = cuota_mensual, @fecha_apr = fecha_aprobacion
        FROM dbo.Prestamos WITH (UPDLOCK, HOLDLOCK)
        WHERE prestamo_id = @prestamo_id AND activo = 1;

        IF @estado IS NULL
            THROW 50020, 'El préstamo no existe o está inactivo.', 1;
        IF @estado <> 'APROBADO'
            THROW 50033, 'Solo se puede desembolsar un préstamo en estado APROBADO.', 1;
        IF @fecha_desembolso < @fecha_apr OR @fecha_desembolso > CAST(SYSDATETIME() AS DATE)
            THROW 50034, 'La fecha de desembolso no puede ser anterior a la aprobación ni futura.', 1;
        IF EXISTS (SELECT 1 FROM dbo.Cuotas WHERE prestamo_id = @prestamo_id)
            THROW 50035, 'El préstamo ya tiene un plan de pagos.', 1;

        SET @i = CAST(@tasa AS DECIMAL(18,10)) / 1200;
        SET @saldo = @monto;
        SET @n = 1;

        WHILE @n <= @plazo
        BEGIN
            SET @interes = ROUND(@saldo * @i, 2);
            SET @capital = CASE WHEN @n = @plazo THEN @saldo ELSE @cuota - @interes END;
            IF @capital > @saldo SET @capital = @saldo;
            IF @capital < 0      SET @capital = 0;
            SET @saldo = @saldo - @capital;

            INSERT INTO dbo.Cuotas (prestamo_id, numero_cuota, fecha_vencimiento, capital, interes, monto_cuota, saldo_restante)
            VALUES (@prestamo_id, @n, DATEADD(MONTH, @n, @fecha_desembolso), @capital, @interes, @capital + @interes, @saldo);

            SET @n = @n + 1;
        END

        UPDATE dbo.Prestamos
        SET estado = 'ACTIVO', fecha_desembolso = @fecha_desembolso
        WHERE prestamo_id = @prestamo_id;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 10.5 Registrar el pago de la siguiente cuota pendiente (la más antigua)
--      Calcula los días de atraso y la mora; marca la cuota como PAGADA.
--      Si era la última, el préstamo pasa a CANCELADO; si estaba EN_MORA y ya no
--      debe cuotas atrasadas, vuelve a ACTIVO.
--      Control de concurrencia: bloquea el préstamo y la cuota (UPDLOCK), así dos
--      cajeros no pueden cobrar la misma cuota; además UQ_Pagos_Cuota lo impide.
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarPago
    @prestamo_id   INT,
    @usuario_id    INT,
    @metodo        VARCHAR(15),
    @referencia    VARCHAR(60) = NULL,
    @fecha_pago    DATE = NULL,
    @pago_id       INT = NULL OUTPUT,
    @total_pagado  DECIMAL(12,2) = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @hoy DATE = CAST(SYSDATETIME() AS DATE);
    DECLARE @estado VARCHAR(20), @porcentaje_mora DECIMAL(5,2), @fecha_des DATE;
    DECLARE @cuota_id INT, @vencimiento DATE, @monto DECIMAL(12,2), @dias INT, @mora DECIMAL(12,2);

    SET @fecha_pago = ISNULL(@fecha_pago, @hoy);

    IF @metodo IS NULL OR @metodo NOT IN ('EFECTIVO','TARJETA','TRANSFERENCIA')
        THROW 50040, 'Método de pago no válido.', 1;
    IF @fecha_pago > @hoy
        THROW 50041, 'La fecha de pago no puede ser futura.', 1;
    IF NOT EXISTS (SELECT 1 FROM dbo.Usuarios WHERE usuario_id = @usuario_id AND activo = 1)
        THROW 50002, 'El usuario que registra el pago no existe o está inactivo.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @estado = estado, @porcentaje_mora = porcentaje_mora, @fecha_des = fecha_desembolso
        FROM dbo.Prestamos WITH (UPDLOCK, HOLDLOCK)
        WHERE prestamo_id = @prestamo_id AND activo = 1;

        IF @estado IS NULL
            THROW 50020, 'El préstamo no existe o está inactivo.', 1;
        IF @estado NOT IN ('ACTIVO','EN_MORA')
            THROW 50042, 'Solo se pueden registrar pagos de préstamos ACTIVOS o EN MORA.', 1;
        IF @fecha_pago < @fecha_des
            THROW 50043, 'La fecha de pago no puede ser anterior al desembolso.', 1;

        SELECT TOP (1) @cuota_id = cuota_id, @vencimiento = fecha_vencimiento, @monto = monto_cuota
        FROM dbo.Cuotas WITH (UPDLOCK, HOLDLOCK)
        WHERE prestamo_id = @prestamo_id AND estado <> 'PAGADA'
        ORDER BY numero_cuota;

        IF @cuota_id IS NULL
            THROW 50044, 'El préstamo no tiene cuotas pendientes.', 1;

        SET @dias = CASE WHEN @fecha_pago > @vencimiento THEN DATEDIFF(DAY, @vencimiento, @fecha_pago) ELSE 0 END;
        SET @mora = dbo.fn_CalcularMora(@monto, @porcentaje_mora, @dias);

        INSERT INTO dbo.Pagos (prestamo_id, cuota_id, usuario_id, fecha_pago, monto_cuota, mora, dias_atraso, metodo, referencia)
        VALUES (@prestamo_id, @cuota_id, @usuario_id,
                CASE WHEN @fecha_pago = @hoy THEN SYSDATETIME() ELSE CAST(@fecha_pago AS DATETIME2(0)) END,
                @monto, @mora, @dias, @metodo, NULLIF(LTRIM(RTRIM(@referencia)), ''));

        SET @pago_id = SCOPE_IDENTITY();
        SET @total_pagado = @monto + @mora;

        UPDATE dbo.Cuotas SET estado = 'PAGADA', fecha_pago = @fecha_pago WHERE cuota_id = @cuota_id;

        IF NOT EXISTS (SELECT 1 FROM dbo.Cuotas WHERE prestamo_id = @prestamo_id AND estado <> 'PAGADA')
        BEGIN
            UPDATE dbo.Prestamos SET estado = 'CANCELADO' WHERE prestamo_id = @prestamo_id;
        END
        ELSE IF @estado = 'EN_MORA'
             AND NOT EXISTS (SELECT 1 FROM dbo.Cuotas
                             WHERE prestamo_id = @prestamo_id AND estado <> 'PAGADA' AND fecha_vencimiento < @hoy)
        BEGIN
            UPDATE dbo.Prestamos SET estado = 'ACTIVO' WHERE prestamo_id = @prestamo_id;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 10.6 Actualizar la mora: marca como VENCIDA cada cuota sin pagar cuyo vencimiento ya pasó
--      y pasa a EN_MORA los préstamos ACTIVOS que tengan alguna cuota vencida.
--      La aplicación lo ejecuta al iniciar sesión o al abrir el tablero.
CREATE OR ALTER PROCEDURE dbo.sp_ActualizarMora
    @fecha              DATE = NULL,
    @cuotas_vencidas    INT = NULL OUTPUT,
    @prestamos_en_mora  INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @fecha = ISNULL(@fecha, CAST(SYSDATETIME() AS DATE));

    BEGIN TRY
        BEGIN TRANSACTION;

        UPDATE dbo.Cuotas
        SET estado = 'VENCIDA'
        WHERE estado = 'PENDIENTE' AND fecha_vencimiento < @fecha;
        SET @cuotas_vencidas = @@ROWCOUNT;

        UPDATE p
        SET p.estado = 'EN_MORA'
        FROM dbo.Prestamos p
        WHERE p.estado = 'ACTIVO'
          AND EXISTS (SELECT 1 FROM dbo.Cuotas c WHERE c.prestamo_id = p.prestamo_id AND c.estado = 'VENCIDA');
        SET @prestamos_en_mora = @@ROWCOUNT;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO


/* =====================================================================
   11. VISTAS ADMINISTRATIVAS
   ===================================================================== */

-- Resumen de préstamos con cliente, tipo, estado y lo pagado / pendiente
CREATE OR ALTER VIEW dbo.vw_PrestamosResumen
AS
SELECT
    p.prestamo_id,
    p.numero_prestamo,
    p.cliente_id,
    c.identificacion,
    c.nombre                               AS cliente_nombre,
    c.apellido                             AS cliente_apellido,
    CONCAT(c.nombre, ' ', c.apellido)      AS cliente,
    c.telefono,
    p.tipo_prestamo_id,
    t.nombre                               AS tipo_prestamo,
    p.usuario_id,
    u.nombre_completo                      AS registrado_por,
    p.monto,
    p.plazo_meses,
    p.tasa_interes_anual,
    p.porcentaje_mora,
    p.cuota_mensual,
    p.destino,
    p.estado,
    e.descripcion                          AS estado_descripcion,
    p.fecha_solicitud,
    p.fecha_aprobacion,
    p.fecha_desembolso,
    p.observaciones,
    p.activo,
    p.fecha_registro,
    r.total_a_pagar,
    r.total_pagado,
    r.mora_pagada,
    r.capital_pendiente,
    r.saldo_pendiente,
    r.cuotas_pagadas,
    r.cuotas_pendientes,
    r.cuotas_vencidas,
    r.proximo_vencimiento
FROM dbo.Prestamos p
INNER JOIN dbo.Clientes c         ON c.cliente_id       = p.cliente_id
INNER JOIN dbo.TiposPrestamo t    ON t.tipo_prestamo_id = p.tipo_prestamo_id
INNER JOIN dbo.EstadosPrestamo e  ON e.codigo           = p.estado
INNER JOIN dbo.Usuarios u         ON u.usuario_id       = p.usuario_id
CROSS APPLY dbo.fn_ResumenPrestamo(p.prestamo_id) r
WHERE p.activo = 1;
GO

-- Cuotas atrasadas con los días de atraso y la mora al día de hoy (para cobranza)
CREATE OR ALTER VIEW dbo.vw_CuotasVencidas
AS
SELECT
    cu.cuota_id,
    cu.prestamo_id,
    p.numero_prestamo,
    cl.identificacion,
    CONCAT(cl.nombre, ' ', cl.apellido)                                   AS cliente,
    cl.telefono,
    cu.numero_cuota,
    cu.fecha_vencimiento,
    a.dias_atraso,
    cu.monto_cuota,
    dbo.fn_CalcularMora(cu.monto_cuota, p.porcentaje_mora, a.dias_atraso)                   AS mora_al_dia,
    cu.monto_cuota + dbo.fn_CalcularMora(cu.monto_cuota, p.porcentaje_mora, a.dias_atraso)  AS total_a_pagar
FROM dbo.Cuotas cu
INNER JOIN dbo.Prestamos p ON p.prestamo_id = cu.prestamo_id
INNER JOIN dbo.Clientes cl ON cl.cliente_id = p.cliente_id
CROSS APPLY (SELECT DATEDIFF(DAY, cu.fecha_vencimiento, CAST(SYSDATETIME() AS DATE)) AS dias_atraso) a
WHERE cu.estado <> 'PAGADA'
  AND cu.fecha_vencimiento < CAST(SYSDATETIME() AS DATE)
  AND p.activo = 1;
GO

-- Cartera por estado: cantidad de préstamos, monto prestado y saldo pendiente (GROUP BY)
CREATE OR ALTER VIEW dbo.vw_CarteraPorEstado
AS
SELECT
    e.codigo                              AS estado,
    e.descripcion,
    e.secuencia,
    COUNT(p.prestamo_id)                  AS cantidad,
    ISNULL(SUM(p.monto), 0)               AS monto_prestado,
    ISNULL(SUM(r.saldo_pendiente), 0)     AS saldo_pendiente
FROM dbo.EstadosPrestamo e
LEFT JOIN dbo.Prestamos p ON p.estado = e.codigo AND p.activo = 1
OUTER APPLY dbo.fn_ResumenPrestamo(p.prestamo_id) r
GROUP BY e.codigo, e.descripcion, e.secuencia;
GO

-- Detalle de pagos (recibos) con cliente, cuota y cajero
CREATE OR ALTER VIEW dbo.vw_PagosDetalle
AS
SELECT
    pg.pago_id,
    pg.numero_recibo,
    pg.fecha_pago,
    pg.prestamo_id,
    p.numero_prestamo,
    cl.identificacion,
    CONCAT(cl.nombre, ' ', cl.apellido)   AS cliente,
    pg.cuota_id,
    cu.numero_cuota,
    cu.fecha_vencimiento,
    pg.dias_atraso,
    pg.monto_cuota,
    pg.mora,
    pg.total,
    pg.metodo,
    pg.referencia,
    pg.usuario_id,
    u.nombre_completo                     AS cajero
FROM dbo.Pagos pg
INNER JOIN dbo.Cuotas cu    ON cu.cuota_id    = pg.cuota_id
INNER JOIN dbo.Prestamos p  ON p.prestamo_id  = pg.prestamo_id
INNER JOIN dbo.Clientes cl  ON cl.cliente_id  = p.cliente_id
INNER JOIN dbo.Usuarios u   ON u.usuario_id   = pg.usuario_id;
GO

-- Préstamos por tipo: cantidad, monto total y promedio (GROUP BY + agregaciones)
CREATE OR ALTER VIEW dbo.vw_ResumenTiposPrestamo
AS
SELECT
    t.tipo_prestamo_id,
    t.nombre                               AS tipo_prestamo,
    COUNT(p.prestamo_id)                   AS cantidad,
    ISNULL(SUM(p.monto), 0)                AS monto_total,
    ISNULL(AVG(p.monto), 0)                AS monto_promedio
FROM dbo.TiposPrestamo t
LEFT JOIN dbo.Prestamos p ON p.tipo_prestamo_id = t.tipo_prestamo_id
                         AND p.activo = 1 AND p.estado NOT IN ('SOLICITADO','RECHAZADO')
GROUP BY t.tipo_prestamo_id, t.nombre;
GO

-- Historial completo de un cliente (por número de identidad)
CREATE OR ALTER PROCEDURE dbo.sp_HistorialCliente
    @identificacion VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT numero_prestamo, tipo_prestamo, monto, plazo_meses, cuota_mensual, estado_descripcion AS estado,
           fecha_solicitud, fecha_desembolso, total_pagado, saldo_pendiente, cuotas_vencidas
    FROM dbo.vw_PrestamosResumen
    WHERE identificacion = @identificacion
    ORDER BY fecha_solicitud DESC, prestamo_id DESC;
END;
GO


/* =====================================================================
   12. DATOS DE PRUEBA
   ===================================================================== */

INSERT INTO dbo.EstadosPrestamo (codigo, descripcion, secuencia, es_final) VALUES
('SOLICITADO', 'Solicitado', 1, 0),
('APROBADO',   'Aprobado',   2, 0),
('ACTIVO',     'Activo',     3, 0),
('EN_MORA',    'En mora',    4, 0),
('CANCELADO',  'Cancelado',  5, 1),
('RECHAZADO',  'Rechazado',  6, 1);

INSERT INTO dbo.TransicionesEstadoPrestamo (estado_actual, estado_siguiente) VALUES
('SOLICITADO', 'APROBADO'),
('SOLICITADO', 'RECHAZADO'),
('APROBADO',   'ACTIVO'),
('APROBADO',   'RECHAZADO'),    -- se revoca la aprobación antes de entregar el dinero
('ACTIVO',     'EN_MORA'),
('ACTIVO',     'CANCELADO'),
('EN_MORA',    'ACTIVO'),       -- se pone al día
('EN_MORA',    'CANCELADO');

-- Usuarios de prueba. Contraseñas: admin = Admin2026, asesor = Asesor2026, cajero = Cajero2026
-- (cámbienlas desde la aplicación; para crear otra huella ejecute util/Seguridad.java)
INSERT INTO dbo.Usuarios (nombre_usuario, nombre_completo, rol, sal, hash_contrasena, iteraciones) VALUES
    ('admin',  'Administrador del sistema', 'ADMIN',  '3B024AA75ECF472DCF55AB76D36A7B17', '4AF23F518C26F5310F67EA408A7F4E42C3E8534BCF1B9A91BE131CCFCA73CB9E', 120000),
    ('asesor', 'Asesor de créditos',        'ASESOR', '9988E093E3D91786093A909BD657CFCE', '14F32F8A25E387E291CBEE747789898C3B9C8B0EFB00D3D2379D2698BDD83A14', 120000),
    ('cajero', 'Cajero principal',          'CAJERO', 'DA3F387ECA26715D94EE75A1DF2CB42C', '39963FEEF49D080B7B5C25A8F1F47D001C41C355E5C31525829B6D25EA112DBA', 120000);

INSERT INTO dbo.TiposPrestamo (nombre, descripcion, tasa_interes_anual, monto_minimo, monto_maximo,
                               plazo_minimo_meses, plazo_maximo_meses, porcentaje_mora, requiere_garantia) VALUES
('Personal',     'Consumo, gastos personales o del hogar',       18.00,   5000.00,  150000.00,  6,  36, 5.00, 0),
('Vehicular',    'Compra de vehículo nuevo o usado',             14.00,  50000.00,  800000.00, 12,  60, 5.00, 1),
('Hipotecario',  'Compra, construcción o mejora de vivienda',    11.00, 300000.00, 3000000.00, 60, 240, 3.00, 1),
('Microempresa', 'Capital de trabajo para pequeños negocios',    22.00,  10000.00,  200000.00,  6,  24, 6.00, 0);

INSERT INTO dbo.Clientes (identificacion, nombre, apellido, telefono, correo, direccion, ocupacion, ingreso_mensual) VALUES
('0801-1988-04512', 'Carlos',  'Mejía Flores',     '9988-1122', 'carlos.mejia@correo.hn',   'Col. Kennedy, Tegucigalpa',             'Contador',             25000.00),
('0501-1990-11873', 'Ana',     'Rodríguez Paz',    '9876-5432', 'ana.rodriguez@correo.hn',  'Barrio Río de Piedras, San Pedro Sula', 'Ingeniera industrial', 45000.00),
('0801-1979-23301', 'José',    'Martínez Cruz',    '3344-5566', 'jose.martinez@correo.hn',  'Mercado Zonal Belén, Comayagüela',      'Comerciante',          30000.00),
('1501-1995-00981', 'María',   'López Castillo',   '9455-7788', NULL,                       'Col. Alameda, Tegucigalpa',             'Docente',              15000.00),
('0801-1983-17654', 'Roberto', 'Hernández Rivera', '9911-2233', 'roberto.hdz@correo.hn',    'Res. Lomas del Guijarro, Tegucigalpa',  'Médico',               80000.00),
('0301-1999-05521', 'Daniela', 'Zelaya Ortiz',     '8877-6655', 'daniela.zelaya@correo.hn', 'Col. Palmira, Tegucigalpa',             'Asistente',            12000.00);
GO

-- Préstamos de prueba en distintos estados.
-- Las fechas se calculan desde HOY para que el ejemplo funcione cualquier día que se ejecute.
DECLARE @hoy DATE = CAST(SYSDATETIME() AS DATE);
DECLARE @id INT, @des DATE, @fecha DATE, @k INT, @cuota DECIMAL(12,2), @pago INT, @total DECIMAL(12,2);
DECLARE @asesor INT = (SELECT usuario_id FROM dbo.Usuarios WHERE nombre_usuario = 'asesor');
DECLARE @cajero INT = (SELECT usuario_id FROM dbo.Usuarios WHERE nombre_usuario = 'cajero');
DECLARE @personal INT     = (SELECT tipo_prestamo_id FROM dbo.TiposPrestamo WHERE nombre = 'Personal');
DECLARE @vehicular INT    = (SELECT tipo_prestamo_id FROM dbo.TiposPrestamo WHERE nombre = 'Vehicular');
DECLARE @hipotecario INT  = (SELECT tipo_prestamo_id FROM dbo.TiposPrestamo WHERE nombre = 'Hipotecario');
DECLARE @microempresa INT = (SELECT tipo_prestamo_id FROM dbo.TiposPrestamo WHERE nombre = 'Microempresa');

-- Préstamo 1: Carlos, Personal L 60,000 a 12 meses. ACTIVO, al día (5 cuotas pagadas a tiempo).
SET @des = DATEADD(DAY, -170, @hoy);
SET @fecha = DATEADD(DAY, -5, @des);
EXEC dbo.sp_RegistrarSolicitud @cliente_id = 1, @tipo_prestamo_id = @personal, @usuario_id = @asesor,
     @monto = 60000, @plazo_meses = 12, @destino = 'Remodelación de cocina', @fecha_solicitud = @fecha, @prestamo_id = @id OUTPUT;
SET @fecha = DATEADD(DAY, -2, @des);
EXEC dbo.sp_AprobarPrestamo @prestamo_id = @id, @fecha_aprobacion = @fecha, @cuota_mensual = @cuota OUTPUT;
EXEC dbo.sp_DesembolsarPrestamo @prestamo_id = @id, @fecha_desembolso = @des;
SET @k = 1;
WHILE @k <= 5
BEGIN
    SET @fecha = DATEADD(MONTH, @k, @des);
    EXEC dbo.sp_RegistrarPago @prestamo_id = @id, @usuario_id = @cajero, @metodo = 'EFECTIVO',
         @fecha_pago = @fecha, @pago_id = @pago OUTPUT, @total_pagado = @total OUTPUT;
    SET @k = @k + 1;
END

-- Préstamo 2: Ana, Vehicular L 250,000 a 36 meses con garantía prendaria. Pagó 2 cuotas y debe la 3.ª: EN MORA.
SET @des = DATEADD(DAY, -110, @hoy);
SET @fecha = DATEADD(DAY, -7, @des);
EXEC dbo.sp_RegistrarSolicitud @cliente_id = 2, @tipo_prestamo_id = @vehicular, @usuario_id = @asesor,
     @monto = 250000, @plazo_meses = 36, @destino = 'Compra de Toyota Corolla 2022', @fecha_solicitud = @fecha, @prestamo_id = @id OUTPUT;
INSERT INTO dbo.Garantias (prestamo_id, tipo, descripcion, valor_estimado)
VALUES (@id, 'PRENDARIA', 'Toyota Corolla 2022, placa HDA 4521, VIN 2T1BURHE0JC012345', 320000);
SET @fecha = DATEADD(DAY, -3, @des);
EXEC dbo.sp_AprobarPrestamo @prestamo_id = @id, @fecha_aprobacion = @fecha, @cuota_mensual = @cuota OUTPUT;
EXEC dbo.sp_DesembolsarPrestamo @prestamo_id = @id, @fecha_desembolso = @des;
SET @k = 1;
WHILE @k <= 2
BEGIN
    SET @fecha = DATEADD(MONTH, @k, @des);
    EXEC dbo.sp_RegistrarPago @prestamo_id = @id, @usuario_id = @cajero, @metodo = 'TRANSFERENCIA',
         @referencia = 'BAC-TRF', @fecha_pago = @fecha, @pago_id = @pago OUTPUT, @total_pagado = @total OUTPUT;
    SET @k = @k + 1;
END

-- Préstamo 3: José, Microempresa L 40,000 a 6 meses con fiador. Pagó todo (la última con 10 días de atraso): CANCELADO.
SET @des = DATEADD(DAY, -200, @hoy);
SET @fecha = DATEADD(DAY, -4, @des);
EXEC dbo.sp_RegistrarSolicitud @cliente_id = 3, @tipo_prestamo_id = @microempresa, @usuario_id = @asesor,
     @monto = 40000, @plazo_meses = 6, @destino = 'Mercadería para el puesto del mercado', @fecha_solicitud = @fecha, @prestamo_id = @id OUTPUT;
INSERT INTO dbo.Fiadores (prestamo_id, identificacion, nombre, apellido, telefono, direccion, parentesco, ingreso_mensual)
VALUES (@id, '0801-1981-33219', 'Luis', 'Martínez Cruz', '9654-3210', 'Col. Kennedy, Tegucigalpa', 'Hermano', 28000);
SET @fecha = DATEADD(DAY, -1, @des);
EXEC dbo.sp_AprobarPrestamo @prestamo_id = @id, @fecha_aprobacion = @fecha, @cuota_mensual = @cuota OUTPUT;
EXEC dbo.sp_DesembolsarPrestamo @prestamo_id = @id, @fecha_desembolso = @des;
SET @k = 1;
WHILE @k <= 6
BEGIN
    SET @fecha = CASE WHEN @k = 6 THEN DATEADD(DAY, 10, DATEADD(MONTH, @k, @des)) ELSE DATEADD(MONTH, @k, @des) END;
    EXEC dbo.sp_RegistrarPago @prestamo_id = @id, @usuario_id = @cajero, @metodo = 'EFECTIVO',
         @fecha_pago = @fecha, @pago_id = @pago OUTPUT, @total_pagado = @total OUTPUT;
    SET @k = @k + 1;
END

-- Préstamo 4: María, Personal L 25,000 a 12 meses. APROBADO, pendiente de desembolso.
SET @fecha = DATEADD(DAY, -3, @hoy);
EXEC dbo.sp_RegistrarSolicitud @cliente_id = 4, @tipo_prestamo_id = @personal, @usuario_id = @asesor,
     @monto = 25000, @plazo_meses = 12, @destino = 'Gastos médicos', @fecha_solicitud = @fecha, @prestamo_id = @id OUTPUT;
EXEC dbo.sp_AprobarPrestamo @prestamo_id = @id, @cuota_mensual = @cuota OUTPUT;

-- Préstamo 5: Roberto, Hipotecario L 900,000 a 120 meses con garantía hipotecaria. SOLICITADO (en evaluación).
EXEC dbo.sp_RegistrarSolicitud @cliente_id = 5, @tipo_prestamo_id = @hipotecario, @usuario_id = @asesor,
     @monto = 900000, @plazo_meses = 120, @destino = 'Compra de casa en Res. Las Hadas', @prestamo_id = @id OUTPUT;
INSERT INTO dbo.Garantias (prestamo_id, tipo, descripcion, valor_estimado)
VALUES (@id, 'HIPOTECARIA', 'Casa de 2 plantas, Res. Las Hadas, matrícula 0012345 del Instituto de la Propiedad', 1350000);

-- Préstamo 6: Daniela, Personal L 150,000 a 36 meses. RECHAZADO (la cuota supera su capacidad de pago).
SET @fecha = DATEADD(DAY, -10, @hoy);
EXEC dbo.sp_RegistrarSolicitud @cliente_id = 6, @tipo_prestamo_id = @personal, @usuario_id = @asesor,
     @monto = 150000, @plazo_meses = 36, @destino = 'Consolidación de deudas', @fecha_solicitud = @fecha, @prestamo_id = @id OUTPUT;
EXEC dbo.sp_CambiarEstadoPrestamo @prestamo_id = @id, @estado_nuevo = 'RECHAZADO',
     @observaciones = 'La cuota estimada supera el 40 % de su ingreso mensual.';

-- Marca las cuotas atrasadas y pasa a EN_MORA los préstamos que las tengan (préstamo 2)
EXEC dbo.sp_ActualizarMora;
GO


/* =====================================================================
   13. CONSULTAS DE EJEMPLO
   ===================================================================== */

-- INNER JOIN: préstamos con su cliente y tipo
-- SELECT p.numero_prestamo, c.nombre + ' ' + c.apellido AS cliente, t.nombre AS tipo, p.monto, p.estado
-- FROM dbo.Prestamos p
-- INNER JOIN dbo.Clientes c      ON c.cliente_id = p.cliente_id
-- INNER JOIN dbo.TiposPrestamo t ON t.tipo_prestamo_id = p.tipo_prestamo_id;

-- LEFT JOIN: todos los clientes y cuántos préstamos tienen (incluye los que no tienen)
-- SELECT c.identificacion, c.nombre, c.apellido, COUNT(p.prestamo_id) AS prestamos
-- FROM dbo.Clientes c LEFT JOIN dbo.Prestamos p ON p.cliente_id = c.cliente_id
-- GROUP BY c.identificacion, c.nombre, c.apellido;

-- GROUP BY + agregaciones: total cobrado por mes (cuotas y mora)
-- SELECT FORMAT(fecha_pago, 'yyyy-MM') AS mes, COUNT(*) AS pagos, SUM(monto_cuota) AS cuotas, SUM(mora) AS mora, SUM(total) AS total
-- FROM dbo.Pagos GROUP BY FORMAT(fecha_pago, 'yyyy-MM') ORDER BY mes;

-- Subconsulta: clientes con ingreso mayor al promedio
-- SELECT identificacion, nombre, apellido, ingreso_mensual FROM dbo.Clientes
-- WHERE ingreso_mensual > (SELECT AVG(ingreso_mensual) FROM dbo.Clientes);

-- Subconsulta: clientes que nunca han solicitado un préstamo
-- SELECT identificacion, nombre, apellido FROM dbo.Clientes
-- WHERE cliente_id NOT IN (SELECT cliente_id FROM dbo.Prestamos);

-- Plan de pagos de un préstamo
-- SELECT numero_cuota, fecha_vencimiento, capital, interes, monto_cuota, saldo_restante, estado, fecha_pago
-- FROM dbo.Cuotas WHERE prestamo_id = 1 ORDER BY numero_cuota;

-- Cuota de un préstamo antes de solicitarlo (simulador)
-- SELECT dbo.fn_CalcularCuota(60000, 18, 12) AS cuota_mensual;

-- Vistas
-- SELECT * FROM dbo.vw_PrestamosResumen;
-- SELECT * FROM dbo.vw_CuotasVencidas ORDER BY dias_atraso DESC;
-- SELECT * FROM dbo.vw_CarteraPorEstado ORDER BY secuencia;
-- SELECT * FROM dbo.vw_PagosDetalle ORDER BY fecha_pago DESC;
-- SELECT * FROM dbo.vw_ResumenTiposPrestamo;
-- EXEC dbo.sp_HistorialCliente @identificacion = '0801-1988-04512';

-- Procedimientos
-- DECLARE @pago INT, @total DECIMAL(12,2);
-- EXEC dbo.sp_RegistrarPago @prestamo_id = 1, @usuario_id = 3, @metodo = 'EFECTIVO', @pago_id = @pago OUTPUT, @total_pagado = @total OUTPUT;
-- SELECT @pago AS pago_id, @total AS total;
-- EXEC dbo.sp_ActualizarMora;
-- SELECT * FROM dbo.Auditoria ORDER BY fecha DESC;

-- Usuarios y su último acceso / desbloquear un usuario
-- SELECT nombre_usuario, nombre_completo, rol, intentos_fallidos, ultimo_acceso, activo FROM dbo.Usuarios;
-- UPDATE dbo.Usuarios SET intentos_fallidos = 0 WHERE nombre_usuario = 'admin';
