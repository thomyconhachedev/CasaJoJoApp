# CasaJoJo Backend — Contrato de API

> **Este archivo es la fuente de verdad de la API para el proyecto móvil.**
> Si eres una IA leyendo esto en una sesión futura: lee también `AGENTS.md` antes de tocar nada.

---

## 1. Generalidades

| | |
|---|---|
| Base URL (local) | `http://localhost:8080` |
| Prefijo | `/api/v1/menu` |
| Content-Type | `application/json` (las respuestas) y `POST` sin body |
| Encoding | UTF-8 |
| Puerto | `8080` (configurable en `application.properties`) |

**Desde un dispositivo/emulador** no usar `localhost`:

- Emulador Android → `http://10.0.2.2:8080`
- Celular físico → `http://<IP-de-la-PC>:8080` (misma WiFi; desactivar el firewall o abrir el puerto)

---

## 2. DTO

### `ProductoDTO` (único DTO de la API)

```json
{
  "id": 1,
  "categoria": "PROTEINA",
  "nombre": "Pollo al Jugo",
  "descripcion": "Pechuga de pollo jugosa al orégano",
  "precioExtra": 0,
  "disponible": true
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` / número | ID **interno** de la BD. Estable entre sincronizaciones, pero **sube** con cada producto nuevo. No es el ID del Sheet. |
| `categoria` | `String` | Siempre en MAYÚSCULAS. Valores actuales: `PROTEINA`, `ACOMPANAMIENTO`, `ENSALADA`, `BEBIDA_POSTRE`. Si la fila del Sheet viene sin categoría → `GENERAL`. |
| `nombre` | `String` | Nombre del producto (máx. 100 chars). |
| `descripcion` | `String` | Descripción (máx. 255 chars). Nunca `null`. |
| `precioExtra` | `Integer` | Precio **extra** en pesos chilenos enteros (ej. `1200`). `0` si no tiene. |
| `disponible` | `Boolean` | `true` = disponible a la venta. |

> **No existe `codigoExterno` en el JSON**: es la columna interna `codigo_externo` que mapea el ID del Google Sheet y solo sirve para la sincronización. El móvil no debe usarla.

---

## 3. Endpoints

### 3.1 `GET /api/v1/menu/productos` — todos los productos

Trae **todo** el catálogo, disponibles y no disponibles, en el orden del Sheet.

**200 OK** → `ProductoDTO[]`

```json
[
  { "id": 1, "categoria": "PROTEINA", "nombre": "Pollo al Jugo", "descripcion": "Pechuga de pollo jugosa al orégano", "precioExtra": 0, "disponible": true },
  { "id": 11, "categoria": "PROTEINA", "nombre": "Pescado Frito", "descripcion": "Reineta frita en batido crujiente", "precioExtra": 800, "disponible": false }
]
```

---

### 3.2 `GET /api/v1/menu/productos/disponibles` — agrupados por categoría

Solo productos con `disponible: true`, devueltos **separados por categoría** (clave = categoría, valor = lista). Pensado para pintar las secciones del menú.

**200 OK** → objeto con listas por categoría

```json
{
  "ACOMPANAMIENTO": [
    { "id": 4, "categoria": "ACOMPANAMIENTO", "nombre": "Arroz Primavera", "descripcion": "Arroz con choclo, arvejas y zanahoria", "precioExtra": 0, "disponible": true },
    { "id": 5, "categoria": "ACOMPANAMIENTO", "nombre": "Papas Mayo", "descripcion": "Papas cocidas con mayonesa casera", "precioExtra": 0, "disponible": true }
  ],
  "BEBIDA_POSTRE": [
    { "id": 9, "categoria": "BEBIDA_POSTRE", "nombre": "Coca-Cola Zero 350ml", "descripcion": "Lata helada", "precioExtra": 1200, "disponible": true }
  ],
  "ENSALADA": [ ... ],
  "PROTEINA": [ ... ]
}
```

- Las claves vienen ordenadas alfabéticamente.
- Si **todas** las categorías están sin stock → `{}` (200).

---

### 3.3 `GET /api/v1/menu/productos/disponibles/{categoria}` — una categoría

Devuelve los disponibles de **una sola** categoría.

- El path es **case-insensitive** y tolera espacios: `/productos/disponibles/proteina`, `/productos/disponibles/%20PROTEINA%20` funcionan igual.
- No hace falta escribirlo en mayúsculas.

**200 OK** → `ProductoDTO[]`

```json
[
  { "id": 1, "categoria": "PROTEINA", "nombre": "Pollo al Jugo", "descripcion": "Pechuga de pollo jugosa al orégano", "precioExtra": 0, "disponible": true },
  { "id": 2, "categoria": "PROTEINA", "nombre": "Carne Mechada", "descripcion": "Vacuno tierno en su propio jugo", "precioExtra": 500, "disponible": true },
  { "id": 3, "categoria": "PROTEINA", "nombre": "Croqueta Vegana", "descripcion": "Croqueta de lentejas y verduras", "precioExtra": 0, "disponible": true },
  { "id": 14, "categoria": "PROTEINA", "nombre": "Pollo Apanado", "descripcion": "Pollo frito en batido crujiente", "precioExtra": 800, "disponible": true }
]
```

**404 Not Found** → categoría inexistente **o** sin ningún producto disponible:

```json
{ "error": "No hay productos disponibles en la categoría: noexiste" }
```

> Si el móvil quiere distinguir "categoría vacía" de "categoría inexistente", usar 3.2 y leer las claves.

---

### 3.4 `POST /api/v1/menu/sincronizar` — leer el Google Sheet y guardar en la BD

Descarga el CSV del Google Sheet publicado, lo parsea y lo **sincroniza** en MySQL. Sin body.

**200 OK** → `ProductoDTO[]` con todos los productos recién guardados

```json
[
  { "id": 1, "categoria": "PROTEINA", "nombre": "Pollo al Jugo", "descripcion": "Pechuga de pollo jugosa al orégano", "precioExtra": 0, "disponible": true },
  { "id": 12, "categoria": "BEBIDA_POSTRE", "nombre": "Pepsi Zero 350ml", "descripcion": "Lata helada", "precioExtra": 1200, "disponible": true }
]
```

**502 Bad Gateway** → falló la descarga/parseo (Sheets caído, CSV vacío, sin filas válidas). **La BD no se toca**:

```json
{ "error": "No se pudo conectar con Google Sheets para descargar el CSV: ..." }
```

Otros mensajes de error posibles:
- `"Google Sheets respondió HTTP 404 al descargar el CSV."`
- `"El CSV llegó vacío desde Google Sheets."`
- `"El CSV no contenía ningún producto válido (0 filas ignoradas). No se modifica la base de datos."`

**Qué hace exactamente (upsert, no borra todo):**

| Cambio en el Sheet | Resultado en la BD |
|---|---|
| Se agrega una fila nueva | Se **inserta** solo esa |
| Se edita un precio/nombre/disponibilidad | Se **actualiza** esa fila (mismo `id`) |
| Se borra una fila | Se **elimina** esa fila |
| Nada cambió | Nada cambia (mismos `id`) |

**Cuándo llamarlo desde el móvil:** pull-to-refresh, botón "Actualizar menú" o al iniciar si quieres datos frescos. Nunca en paralelo (una sync a la vez).

---

## 4. Pedidos (contrato para el backend)

> **Estado:** este módulo **aún no existe en el backend**. Documenta el contrato que el móvil asume y que el backend debe implementar (alineado a los DTOs del móvil: `CrearPedidoRequestDto` / `PedidoResponseDto`). La app tiene un **modo local** (cola de pedidos en memoria) que funciona sin este backend.

### Enum `estado_pedido`

| Valor | Descripción |
|---|---|
| `PENDIENTE` | Recién creado, espera ser tomado por cocina. |
| `EN_COCINA` | En preparación. |
| `LISTO` | Listo para retiro. |
| `ENTREGADO` | Entregado al cliente. |
| `CANCELADO` | Cancelado (anulado antes de la entrega). |

### 4.1 `POST /api/v1/pedidos` — crear un pedido

**Body** (`CrearPedidoRequestDto`):

```json
{
  "cliente_nombre": "Ana Pérez",
  "cliente_telefono": "+56912345678",
  "hora_retiro": "13:20",
  "notas_restricciones": "Sin cebolla; sin gluten",
  "detalles": [
    { "item_menu_id": 1, "cantidad": 1, "precio_unitario": 0 },
    { "item_menu_id": 9, "cantidad": 1, "precio_unitario": 1200 }
  ]
}
```

- `item_menu_id` referencia un `id` de `ProductoDTO` (sección 2).
- `hora_retiro` formato `HH:mm` (la app genera slots 12:00–16:00 cada 10 min).
- Al crear, el backend debe dejar el pedido en `estado_pedido = "PENDIENTE"`.

**201 Created** → `PedidoResponseDto` (sección 4.3).

**400 Bad Request** → `{"error": "..."}` si faltan datos o `detalles` está vacío.

### 4.2 `GET /api/v1/pedidos` — cola de pedidos (admin)

Devuelve todos los pedidos para la vista de admin (la cola se ordena en el móvil por hora de retiro).

**200 OK** → `PedidoResponseDto[]`

### 4.3 `PedidoResponseDto`

```json
{
  "id": 1,
  "cliente_nombre": "Ana Pérez",
  "cliente_telefono": "+56912345678",
  "hora_retiro": "13:20",
  "notas_restricciones": "Sin cebolla; sin gluten",
  "estado_pedido": "PENDIENTE",
  "estado_pago": "PENDIENTE",
  "creado_en": "2026-10-19T12:30:00",
  "detalles": [
    { "id": 10, "item_nombre": "Pollo al Jugo", "cantidad": 1, "precio_unitario": 0 }
  ]
}
```

### 4.4 `PATCH /api/v1/pedidos/{id}/estado` — cambiar estado

**Body**: `{ "estado": "EN_COCINA" }` (valor del enum de la sección 4).

**200 OK** → `PedidoResponseDto` actualizado.
**404 Not Found** → `{"error": "..."}` si el `id` no existe.

> **Alineación de DTOs (importante):** el DTO de menú del móvil se alinea a `ProductoDTO` (sección 2), con `precioExtra` (entero CLP) y `descripcion`. Los campos del `PedidoResponseDto` usan snake_case (`cliente_nombre`, `estado_pedido`, `creado_en`), igual que en el ejemplo.

---

## 5. Fuente de datos

- Google Sheet publicado como CSV: `menu.sheet.csv-url` en `src/main/resources/application.properties`.
- Columnas esperadas en la hoja (con o sin columna ID, y en cualquier orden):

| Columna | Requerida | Notas |
|---|---|---|
| `ID` | No | Se ignora como valor; se usa para no duplicar filas (`codigo_externo`). Si la borras del Sheet, todo sigue funcionando. |
| `Categoria` | No | Vacía → `GENERAL`. |
| `Nombre` | **Sí** | Filas sin nombre se ignoran. |
| `Descripcion` | No | |
| `PrecioExtra` | No | Se leen solo dígitos: `$1.500` → `1500`. Vacío → `0`. |
| `Disponible` | No | `false`/`falso`/`no`/`0` → `false`; todo lo demás y vacío → `true`. |

Filas duplicadas (mismo ID o misma categoría+nombre) → se conserva la primera y se contabiliza como ignorada (aparece un warning en el log).

---

## 6. Errores (resumen)

| Status | Cuándo | Body |
|---|---|---|
| `200` | OK | según endpoint |
| `404` | `disponibles/{categoria}` sin resultados | `{"error": "..."}` |
| `502` | Falla la sincronización (Sheets/CSV) | `{"error": "..."}` |

No hay autenticación. El cuerpo del error es siempre un objeto con clave `error` (string).

---

## 7. BD

MySQL/MariaDB `casajojo`, tabla `productos`:

```sql
id            BIGINT PK AUTO_INCREMENT
codigo_externo BIGINT UNIQUE NULL   -- ID del Sheet, no expuesto en la API
categoria     VARCHAR(50) NOT NULL
nombre        VARCHAR(100) NOT NULL
descripcion   VARCHAR(255)
precio_extra  INT NOT NULL
disponible    BIT/BOOLEAN NOT NULL
```

---

## 8. Comandos

```powershell
.\mvnw.cmd spring-boot:run     # arrancar (requiere XAMPP/MySQL corriendo)
.\mvnw.cmd test                # tests (usa H2, no necesita MySQL)
```

> **Regla:** siempre detener la app después de probar:
> `Get-NetTCPConnection -LocalPort 8080 -State Listen | % { Stop-Process -Id $_.OwningProcess -Force }`
