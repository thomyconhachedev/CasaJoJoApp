# AGENTS.md — CasaJoJo Backend (Spring Boot 4 / Java 21)

Backend del menú de CasaJoJo. Lee **`API.md`** para el contrato completo de endpoints y DTOs (es la fuente de verdad para el móvil).

## Comandos

```powershell
.\mvnw.cmd test                # correr tests (usan H2, NO requieren MySQL)
.\mvnw.cmd spring-boot:run     # arrancar app en :8080 (requiere MySQL de XAMPP corriendo)
```

## Reglas duras

1. **Siempre parar la app después de probarla.** Nunca devolver resultados con el proceso corriendo:
   ```powershell
   Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
   # además matar los java restantes del proyecto:
   Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -like '*CasaJoJoBackEnd*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
   ```
   El usuario despliega la app manualmente (Docker) y le molesta encontrarla prendida.
2. Confirmar con puerto 8080 libre y sin `java.exe` del proyecto antes de terminar.

## Entorno

- **BD:** MariaDB 10.4 de XAMPP (`c:\xampp\mysql\bin\mysql.exe`), base `casajojo`, usuario `root`, sin password, puerto 3306. Arrancar XAMPP si está apagado.
- **Tests:** H2 en memoria (`src/test/resources/application.properties`) → pasan sin XAMPP.
- **Datos:** el menú vive en un Google Sheet publicado como CSV (`menu.sheet.csv-url` en `application.properties`).

## Estructura

```
controller/MenuController.java      4 endpoints bajo /api/v1/menu
service/MenuService.java            upsert desde Sheet, consultas por categoría
parser/MenuCsvParser.java           parseo por encabezados (tolera columnas faltantes/reordenadas)
source/GoogleSheetsMenuCsvSource    descarga del CSV (interfaz MenuCsvSource → stub en tests)
model/Producto.java                 entidad; codigo_externo = ID del Sheet (no se expone en la API)
dto/ProductoDTO                     único DTO de salida
```

## Comportamiento clave

- **Sync (`POST /sincronizar`) = upsert**: no borra todo. Coincide por `codigo_externo` (ID del Sheet) o, si no hay columna ID, por `categoria+nombre`. Los `id` de la BD se mantienen entre syncs.
- **`id` es autoincremental** y no se reinicia al borrar; no es el ID del Sheet (ese es `codigo_externo`).
- Parser por encabezados: puede cambiar el orden de columnas o quitarse la columna `ID`; filas sin `nombre` se ignoran; duplicados se descartan.
- `ddl-auto=update`: al agregar columnas a la entidad se solas crean en la BD.

## Verificación típica

1. `.\mvnw.cmd test` (17 tests).
2. Arrancar, `POST http://localhost:8080/api/v1/menu/sincronizar`, verificar por SQL (`mysql.exe -uroot casajojo -e "SELECT id, codigo_externo, nombre FROM productos"`) que se reflejan altas/cambios/bajas y los `id` no cambian.
3. **Detener la app (regla 1).**

## Módulo de pedidos (PENDIENTE)

El backend hoy **solo** tiene el módulo de menú (`/api/v1/menu`). Falta construir el módulo de **pedidos**, cuyo contrato está definido en **`API.md` sección 4**:

- `POST /api/v1/pedidos` — crear pedido (estado inicial `PENDIENTE`).
- `GET /api/v1/pedidos` — cola de pedidos para el admin.
- `PATCH /api/v1/pedidos/{id}/estado` — cambiar estado (`PENDIENTE → EN_COCINA → LISTO → ENTREGADO`; `CANCELADO` es un estado terminal alcanzable desde cualquier estado no terminal).

Entidades sugeridas: `Pedido` + `DetallePedido`, con `estado_pedido` (enum) y `estado_pago`. El `item_menu_id` del request referencia `Producto.id`. Respetar el **snake_case** de `PedidoResponseDto` (`cliente_nombre`, `estado_pedido`, `creado_en`).

## Móvil (modo local / API)

La app Android tiene un **interruptor de modo de datos** (en runtime, default LOCAL):

- **LOCAL**: el menú viene de datos hardcodeados y la cola de pedidos vive en memoria (no usa backend).
- **API**: consume este backend (menú + pedidos). Requiere el backend corriendo y la `BASE_URL` correcta según el dispositivo (emulador → `10.0.2.2`, celular físico → la IP de la PC en la misma WiFi).

Mientras el módulo de pedidos no exista, el **modo API de pedidos** devolverá error; la app debe degradar a local o mostrar el mensaje, **no romperse**.
