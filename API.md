# CasaJoJo Backend — Contrato de API (móvil)

> **Fuente de verdad del contrato que habla la app Android**
> (`data/remote/ApiService.kt`). Si el backend cambia, cambia este archivo
> Y los DTOs del móvil en el mismo commit.
> Sistema de usuarios/roles: ver `docs/backend_login_paso_a_paso.md`.

---

## 1. Generalidades

| | |
|---|---|
| Base URL (producción/pruebas) | `http://100.62.186.171:8080/` (IP interna Tailscale) |
| Base URL (emulador) | `http://10.0.2.2:8080/` |
| Prefijo | `/api/v1` |
| Content-Type | `application/json` |
| Serialización | **camelCase** (records de Spring) |
| Errores | `{"error": "mensaje legible"}` — la app lo traduce a la UI (`ApiError.kt`) |

La `BASE_URL` es configurable en runtime desde el panel admin (modo API) y
se valida que empiece con `http://` o `https://`.

## 2. DTO del menú — `ProductoDto`

```json
{
  "id": 1,
  "categoria": "PROTEINA",
  "nombre": "Pollo al Jugo",
  "descripcion": "Pechuga de pollo jugosa al orégano",
  "precio": 0,
  "stock": null,
  "disponible": true
}
```

| Campo | Tipo | Notas |
|---|---|---|
| `id` | Long | ID interno estable; el móvil lo reenvía como `itemMenuId` |
| `categoria` | String | `PROTEINA`, `ACOMPANAMIENTO`, `ENSALADA`, `BEBIDA_POSTRE` (la app la traduce a BEBIDA), `GENERAL` (no se muestra) |
| `descripcion` | String? | opcional; la app muestra fallback vacío |
| `precio` | Double | CLP. `0` para ítems base del plato; los extras (postres/bebidas) tienen precio real |
| `stock` | Int? | reservado, hoy no se usa en la UI |
| `disponible` | Boolean | `false` → la app muestra "AGOTADO" y bloquea selección |

> Los emojis NO viajan del backend: la app asigna un emoji por categoría
> (`PresentacionMappers.kt`).

## 3. Endpoints

### 3.1 `GET /api/v1/menu` — menú de hoy

**200 OK** → envolvente `{fecha, estado, items[]}`:

```json
{
  "fecha": "2026-10-24",
  "estado": "ACTIVO",
  "items": [ { "...ProductoDto...": "..." } ]
}
```

### 3.2 `POST /api/v1/menu/sincronizar` — bajar el Google Sheet

Sin body. Lee el CSV publicado del Sheet y hace upsert en la BD.

- **200 OK** → misma envolvente que 3.1 con los items sincronizados.
- **502** → `{"error": "..."}` si falló la descarga/parseo (la BD no se toca).

La app lo usa en el botón **"Refrescar menú"** del panel admin:
primero `POST /sincronizar` y luego `GET /menu` para pintar lo nuevo.
**Nunca llamar en paralelo** (una sync a la vez).

### 3.3 `POST /api/v1/pedidos` — crear pedido

Body (`CrearPedidoRequestDto`):

```json
{
  "usuarioId": null,
  "nombreCliente": "Ana Pérez",
  "telefonoContacto": "+56912345678",
  "horaRetiroSolicitada": "14:00",
  "observaciones": "Sin cebolla; sin gluten",
  "items": [
    { "itemMenuId": 1, "cantidad": 1, "observacion": "Sin cebolla", "platoId": 1 },
    { "itemMenuId": 9, "cantidad": 1, "observacion": null, "platoId": 2 }
  ]
}
```

- `usuarioId`: **null = pedido de invitado** (válido). Con sesión USUARIO/ADMIN
  la app envía el id de la cuenta (ver doc de login para hacerlo robusto
  validando contra el token).
- `platoId`: **índice del plato al que pertenece la línea (1..N dentro del
  pedido)**. El backend debe persistirlo y devolverlo en `detalle.platoId`
  (sección 3.4); así la app reagrupa las líneas por plato y cocina ve cada
  combinación por separado. Si el backend aún no lo soporta, la app muestra
  las líneas planas (compatibilidad hacia atrás).
- `horaRetiroSolicitada`: `HH:mm`. El backend exige **≥20 minutos de
  anticipación** (la app genera slots de 10 min que ya respetan la regla).
- Los precios **NO viajan**: el backend los toma de su propio menú.
- `observacion` por ítem = restricciones del plato al que pertenece.

**201 Created** → `PedidoResponseDto` (3.4). **400** → `{"error": "..."}`.

### 3.4 `GET /api/v1/pedidos` — cola (admin)

**200 OK** → `PedidoResponseDto[]`. La app ordena por hora de retiro.

```json
{
  "id": 1,
  "codigoSeguimiento": "JOJO-0001",
  "estado": "RECIBIDO",
  "horaRetiro": "14:00",
  "nombreCliente": "Ana Pérez",
  "telefonoContacto": "+56912345678",
  "observaciones": "Sin cebolla",
  "montoTotal": 6000.0,
  "fechaCreacion": "2026-10-24T12:30:00",
  "detalles": [
    { "itemMenuId": 1, "nombreItem": "Pollo al Jugo", "cantidad": 1, "precioUnitario": 4800.0, "observacion": "Sin cebolla", "platoId": 1 },
    { "itemMenuId": 9, "nombreItem": "Flan", "cantidad": 1, "precioUnitario": 1200.0, "observacion": null, "platoId": 2 }
  ]
}
```

- `detalles[].platoId`: echo del plato enviado en el POST (1..N). Permite a
  la app mostrar "PLATO 1 DE 2" con la ficha de cada combinación. Campo
  nullable: si no viene, la vista de detalle degrada a líneas planas.

### 3.5 `PATCH /api/v1/pedidos/{id}/estado` — avanzar estado

Body: `{"estado": "EN_PREPARACION"}` (valores del enum de la sección 4).

**200 OK** → pedido actualizado. **404/409** → `{"error": "..."}`.
La app **solo ofrece avanzar de a un estado** (la cola admin no permite
saltos porque el backend los rechaza con 409).

## 4. Máquina de estados del pedido

```
RECIBIDO → EN_PREPARACION → LISTO_PARA_RETIRO → ENTREGADO
    └────────────┴─── CANCELADO (terminal)
```

`EstadoPedido.kt` del móvil replica esta cadena; un estado desconocido del
backend degrada a `RECIBIDO` (nunca crashea).

## 5. Checklist de verificación contra el backend desplegado

Antes de cada entrega, correr (15 min) y actualizar este archivo si algo
no cuadra:

```powershell
curl http://100.62.186.171:8080/api/v1/menu
curl -X POST http://100.62.186.171:8080/api/v1/menu/sincronizar
curl -X POST http://100.62.186.171:8080/api/v1/pedidos -H "Content-Type: application/json" `
  -d '{"nombreCliente":"Test","horaRetiroSolicitada":"14:00","items":[{"itemMenuId":1,"cantidad":1}]}'
curl http://100.62.186.171:8080/api/v1/pedidos
curl -X PATCH http://100.62.186.171:8080/api/v1/pedidos/1/estado -H "Content-Type: application/json" `
  -d '{"estado":"EN_PREPARACION"}'
```

Verificar: (1) el menú usa envelope y campo `precio` (no `precioExtra`);
(2) el request camelCase es aceptado; (3) los estados son los de la
sección 4; (4) `montoTotal` del servidor coincide con lo que la app
muestra ($4.800 base + extras).

## 6. Estados especiales de la app

- **Modo LOCAL** (default): menú hardcodeado + cola en memoria, sin backend.
- **Modo API con backend caído**: la app degrada al menú/cola local con
  una advertencia en el panel admin y botón de reintento.
- **Autenticación**: aún no aplicada a estos endpoints (roadmap, ver
  `docs/backend_login_paso_a_paso.md`). Mientras tanto el panel admin se
  protege con roles locales en la app.
