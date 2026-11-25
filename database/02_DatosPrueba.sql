/* =====================================================================
   CrediManager - Datos de prueba (OPCIONAL)
   Ejecutar sobre CrediManager después de CrediManagerDB.sql si se quieren
   tipos de préstamo, clientes y préstamos de ejemplo en distintos estados.
   ===================================================================== */

USE CrediManager;
GO

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
