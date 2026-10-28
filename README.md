# CasaJoJoApp

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material%203-BOM-00897B)
![Min API 28](https://img.shields.io/badge/minSdk-28-3DDC84?logo=android&logoColor=white)
![Arquitectura](https://img.shields.io/badge/arquitectura-MVVM-orange)

Aplicación Android para **La Casa JoJo SpA**, un negocio de comida casera en Chile. La app reemplaza el flujo de pedidos por WhatsApp y en papel con un **armado guiado de platos** en 5 pasos, carrito multi-plato, selección de hora de retiro y panel de cocina, listo para conectarlo a un backend propio (Spring Boot).

> **Idioma de la interfaz:** español (es-CL) · **Moneda:** pesos chilenos (CLP) · **Plato base:** $4.800

---

## Tecnologías y Herramientas

| Categoría | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.2.10 |
| UI declarativa | Jetpack Compose (BOM) | 2026.02.01 |
| Design system | Material Design 3 (BOM) | — |
| Navegación | Navigation-Compose | 2.10.2 |
| Arquitectura | MVVM + State Hoisting + Service Locator | — |
| Async / estado | Lifecycle ViewModel Compose | 2.11.0 |
| HTTP | Retrofit + Converter Gson | 2.11.0 |
| Serialización | Gson | 2.11.0 |
| Persistencia local | Room | 2.8.5 |
| Seguridad local | PBKDF2-HMAC-SHA256 (`PasswordHasher`) | — |
| Build | Gradle 9.5.0 + AGP | 9.3.3 |
| Lenguaje de scripts | Java (source/target) | 11 |

- **SDK:** `minSdk` 28 (Android 9) · `targetSdk`/`compileSdk` 37
- **Módulo único:** `:app` (single-module)
- **Release:** R8 activo (podado y ofuscado)
- **Testing:** JUnit 4, Espresso y Compose UI Test (solo plantillas por ahora; suite pendiente)

---

## Sistema de usuarios y roles (híbrido)

La app maneja **tres roles**:

| Rol | Cómo entra | Qué puede | Pedido viaja con |
|---|---|---|---|
| **INVITADO** | "Continuar como invitado" (sin cuenta) | Flujo completo de pedido; perfil local en Mi Cuenta | `usuarioId = null` |
| **USUARIO** | Registro/login (tabla local `usuarios`, hash PBKDF2) | Lo anterior + sesión persistente y datos autocompletados | `usuarioId` |
| **ADMIN** | Cuenta sembrada (`admin` / `casajojo2026` — cambiar antes de entregar) | Todo lo anterior + panel de cocina (oculto para los demás) | `usuarioId` |

- **Invitado es un rol de primera clase**: no todo el mundo quiere crear cuenta para comprar una vez.
- El registro público **nunca otorga rol ADMIN**; esa cuenta solo nace sembrada por la app (y más adelante por el backend).
- Las contraseñas se guardan **solo como hash** (PBKDF2-HMAC-SHA256 + salt) — nunca en claro.
- El backend aún no tiene autenticación: el gate actual es de UI + datos locales. La migración a login real (JWT) está planificada en **`docs/backend_login_paso_a_paso.md`** y no requiere reescribir la UI (la app ya tiene la interfaz `AuthRepository` lista para su implementación remota).

---

## Arquitectura

### Patrón

**MVVM (Model–View–ViewModel)** con *state hoisting* sobre Jetpack Compose:

- **View (Compose):** pantallas que renderizan `UiState` inmutables y emiten eventos al ViewModel.
- **ViewModels:** `OrderViewModel` (armado + carrito + cola admin + menú), `CuentaViewModel` (perfil según sesión), `LoginViewModel` (login/registro/invitado).
- **`ServicioLocator`:** contenedor de dependencias sin Hilt; instancias únicas de BD, sesión (`AuthRepository`), modo de datos y cola local, compartidas por todos los ViewModels (inicializado en `CasaJoJoApplication`).
- **Model (`domain/model`):** entidades puras sin Android (`PlatoArmado`, `Producto`, `Pedido`, `Usuario`/`Sesion`/`Rol`).
- **Data (`data/`):** Room local (clientes + usuarios), repositorios con doble implementación LOCAL/API, DTOs Retrofit y mappers aislados.
- **Navegación:** `NavHost` centralizado en `MainActivity.kt`; la ruta `admin` exige sesión ADMIN (muestra un gate hacia el login si no).

### Estructura de carpetas

```
CasaJoJoApp/
├── app/src/main/java/com/example/casajojoapp/
│   ├── MainActivity.kt              # Activity + NavHost (rutas + gate admin)
│   ├── CasaJoJoApplication.kt       # Inicializa ServicioLocator
│   ├── data/
│   │   ├── ServicioLocator.kt       # Contenedor de dependencias (singletons)
│   │   ├── local/                   # Room: clientes + usuarios (migración v1→v2)
│   │   ├── modo/                    # Interruptor LOCAL/API + BASE_URL (prefs)
│   │   ├── remote/                  # ApiService, DTOs Gson, NetworkModule, ApiError
│   │   ├── repository/              # Auth, Cliente, Menú, Pedidos (LOCAL/API)
│   │   └── mappers/                 # DTOs remotos ↔ dominio
│   ├── domain/model/                # PlatoArmado, Producto, Pedido, Usuario...
│   ├── ui/
│   │   ├── admin/                   # Cola de cocina + gate de acceso
│   │   ├── auth/                    # Login/registro/invitado
│   │   ├── cliente/armado/          # VM + estado + pasos 1–5 del flujo
│   │   ├── cliente/carrito/         # Carrito + hora de retiro
│   │   ├── cliente/cuenta/          # Perfil según sesión
│   │   ├── cliente/home/            # Home
│   │   ├── componentes/             # Header, cards, barra de progreso
│   │   └── theme/                   # Tokens de marca (contraste WCAG AA)
│   └── util/                        # HorarioRetiro, PasswordHasher
├── docs/
│   ├── backend_login_paso_a_paso.md # Cómo llevar el login al backend
│   └── memoria_tecnica.pdf
├── API.md                           # Contrato que habla la app con el backend
├── AGENTS.md
└── gradle/libs.versions.toml        # Version catalog
```

---

## Cómo ejecutar

```bash
./gradlew compileDebugKotlin      # chequeo rápido
./gradlew assembleDebug           # APK debug
./gradlew assembleRelease         # APK release (R8; sin firmar)
./gradlew testDebugUnitTest       # tests unitarios
./gradlew lintDebug               # análisis estático
```

Requisitos: Android Studio Koala+, JDK 17+ (toolchain resuelta por Gradle), Android SDK API 37.

---

## Características principales

### Flujo de armado de platos (cliente)

| # | Pantalla | Descripción |
|---|---|---|
| 1/5 | **Proteína** | Obligatoria; muestra estado "AGOTADO" si el backend la marcó |
| 2/5 | **Acompañamientos** | Mínimo 1, máximo 2 (validado en el ViewModel) |
| 3/5 | **Ensaladas** | Opcional; opción "Sin Ensalada" |
| 4/5 | **Postres y bebidas** | Opcionales; **únicos ítems con precio individual** |
| 5/5 | **Resumen** | Restricciones **por plato**; pedir otro plato o ir al carrito |

### Carrito, retiro y envío

- Múltiples platos; cada uno con su resumen y restricciones.
- Hora de retiro en slots de 10 minutos con anticipación mínima de 20 min
  (**rango de pruebas 12:00–02:00, cruzando medianoche**; el día real se
  ajusta a 12:00–16:00 cambiando `HORAS_VENTANA` en
  `util/HorarioRetiro.kt` — una sola constante, todo el texto deriva de ella).
- Envío delegado al repositorio vigente; en API el backend asigna id/código.

### Panel de cocina (admin)

- **Vista Cocina** (primer botón de la barra inferior del admin): pantalla de
  trabajo con dos pestañas — **Recibidos** y **En preparación** — ordenadas por
  hora de entrega. Cada tarjeta muestra el contenido del pedido (proteína,
  acompañamientos, ensalada, extras y notas por plato) con botones directos de
  estado: "Iniciar preparación" y "Marcar listo para retiro" (el pedido pasa a
  la cola completa al quedar listo).
- **Cola** (botón del medio): cola completa ordenada por hora de retiro con
  máquina de estados
  (`RECIBIDO → EN_PREPARACION → LISTO_PARA_RETIRO → ENTREGADO`, cancelar desde estados iniciales).
- Interruptor **LOCAL/API** + URL del backend (validada `http/https`) desde
  "Panel de datos" en Cocina o el panel de administración.
- **"Refrescar menú" = `POST /sincronizar` + `GET /menu`**: el cliente
  actualiza su menú (Google Sheet → BD) desde la app, sin Postman.
- **Degración elegante:** si el backend está caído, menú y cola se sirven
  desde la fuente local con advertencia ámbar y botón de reintento.

### Reglas de negocio

- Plato completo = 1 proteína + al menos 1 acompañamiento.
- Precio del plato fijo `$4.800` (`PlatoArmado.PRECIO_BASE`); solo extras suman.
- Pedidos de cuentas se asocian (`usuarioId`); los de invitados viajan sin cuenta.

### Recursos nativos del dispositivo

La app integra tres recursos nativos, con su permiso y configuración correspondientes:

- **Notificaciones locales** (`POST_NOTIFICATIONS` + canal): aviso de cambio de estado de la comanda ("Tu pedido {código} está listo para retiro") y **recordatorio 5 minutos antes** de la hora de retiro programado con `AlarmManager` (sobrevive a cerrar la app y se reprograma tras un reinicio).
- **Selector de hora nativo**: `TimePickerDialog` de Android en la pantalla de retiro, además de la grilla rápida de 10 minutos.
- **Almacenamiento local con Room**: perfil de invitado, usuarios con roles e **historial de pedidos** que persiste entre sesiones (migraciones v1→v2→v3).

---

## Seguridad y roadmap

**Hecho en esta MVP:** contraseñas solo como hash (PBKDF2+salt), roles con gate de admin en UI, contraseñas jamás en logs ni respuestas, R8 activo en release, sin secretos en el repo, URL del backend validada.

**Decidido conscientemente para la MVP (roadmap):**

1. **HTTPS:** la app hoy habla HTTP sobre una IP pública sin cifrar. Antes de exponerla se termina HTTPS en el backend y se elimina `usesCleartextTraffic`.
2. **Autenticación real de API:** login JWT en el backend + `AuthApi` en el móvil (paso a paso en `docs/backend_login_paso_a_paso.md`). Mientras tanto, el gate admin es de UI, no de datos: los endpoints siguen abiertos en la red interna.
3. **Idempotencia en `POST /pedidos`** para evitar duplicados ante timeouts.
4. **Notificaciones push multi-dispositivo (FCM):** hoy los avisos ("5 min antes del retiro" y cambio de estado) son **locales del dispositivo**; el aviso al teléfono del cliente cuando *otro* equipo cambia el estado requiere FCM + integración en el backend.
5. **Declaración de datos personales y política de privacidad** antes de publicar en Google Play.

---

## Repositorio

```
https://github.com/thomyconhachedev/CasaJoJoApp.git
```

## Licencia

Proyecto privado. Todos los derechos reservados.
