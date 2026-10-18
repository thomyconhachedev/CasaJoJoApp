# CasaJoJoApp

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material%203-1.4.0-00897B)
![Min API 28](https://img.shields.io/badge/minSdk-28-3DDC84?logo=android&logoColor=white)
![Arquitectura](https://img.shields.io/badge/arquitectura-MVVM-orange)

Aplicación Android para **La Casa JoJo SpA**, un negocio de comida casera y sushi en Chile. La app reemplaza el flujo de pedidos por WhatsApp y en papel con un **armado guiado de platos** en 5 pasos, carrito multi-plato y selección de hora de retiro, listo para conectarlo a un backend propio.

> **Idioma de la interfaz:** español (es-CL) · **Moneda:** pesos chilenos (CLP) · **Plato base:** $4.800

---

## Tecnologías y Herramientas

| Categoría | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.2.10 |
| UI declarativa | Jetpack Compose (BOM) | 2026.02.01 |
| Design system | Material Design 3 | 1.4.0 |
| Navegación | Navigation-Compose | 2.10.2 |
| Arquitectura | MVVM + State Hoisting | — |
| Async / estado | Lifecycle ViewModel Compose | 2.11.0 |
| HTTP (preparado) | Retrofit + Converter Gson | 2.11.0 |
| Serialización | Gson | 2.11.0 |
| Imágenes | Coil Compose | 2.6.0 |
| Persistencia (preparada) | Room KTX | 2.8.5 |
| Build | Gradle 9.5.0 + AGP | 9.3.3 |
| Lenguaje de scripts | Java (source/target) | 11 |

- **SDK:** `minSdk` 28 (Android 9) · `targetSdk`/`compileSdk` 37
- **Módulo único:** `:app` (single-module)
- **Testing:** JUnit 4, Espresso y Compose UI Test (solo plantillas por ahora)

---

## Arquitectura y Estructura del Proyecto

### Patrón arquitectónico

La app usa **MVVM (Model–View–ViewModel)** con *state hoisting* sobre Jetpack Compose:

- **View (Compose):** pantallas stateless que renderizan el `OrderUiState` y emiten eventos al ViewModel vía callbacks/lambdas. No contienen lógica de negocio.
- **ViewModel (`OrderViewModel`):** una **única instancia compartida** por todo el flujo de armado (creada con `viewModel()` en `MainActivity` e inyectada en cada ruta). Expone un `OrderUiState` inmutable y acciones como `alternarAcompanamiento()`, `agregarExtra()`, `actualizarNotasPlato()`, `actualizarHoraRetiro()` y `agregarPlatoAlCarrito()`. Las UI se actualizan reactivamente al mutar el estado con `.copy()`.
- **Model (`domain/model`):** entidades de negocio puras sin dependencias de Android (`PlatoArmado`, `Producto`, `Pedido`). `PlatoArmado` encapsula el cálculo de precio (`PRECIO_BASE` + extras).
- **Data (`data/remote`):** DTOs Gson listos para el API REST futuro (`CrearPedidoRequestDto`, `ItemMenuDto`, `MenuDiarioDto`, `PedidoResponseDto`). Mapeo dominio→DTO aislado en `Mapeadores.kt`. El envío real (`enviarPedido()`) es hoy un stub preparado para Retrofit.
- **Navegación:** `NavHost` centralizado en `MainActivity.kt`. El estado sobrevive entre pantallas gracias a que el ViewModel se comparte, no a `SavedStateHandle`.

### Estructura de carpetas

```
CasaJoJoApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/casajojoapp/
│       │   ├── MainActivity.kt              # Activity + NavHost (rutas del flujo)
│       │   ├── PantallaDos.kt               # Home (promociones, fecha)
│       │   ├── data/
│       │   │   └── remote/                  # DTOs para el backend REST (Gson)
│       │   │       ├── MenuDiarioDto.kt
│       │   │       └── dto/
│       │   │           ├── ItemMenuDto.kt
│       │   │           ├── request/CrearPedidoRequestDto.kt
│       │   │           └── response/PedidoResponseDto.kt
│       │   ├── domain/
│       │   │   └── model/                   # Entidades de negocio (sin Android)
│       │   │       ├── Pedido.kt
│       │   │       ├── PlatoArmado.kt       # PRECIO_BASE + extras → precioTotal
│       │   │       └── Proteina.kt          # Producto, CategoriaItem
│       │   ├── ui/
│       │   │   ├── cliente/
│       │   │   │   ├── armado/              # VM + estado + pantallas 1–5 del flujo
│       │   │   │   │   ├── OrderViewModel.kt
│       │   │   │   │   ├── OrderUiState.kt
│       │   │   │   │   ├── Mapeadores.kt
│       │   │   │   │   ├── SeleccionProteinaSC.kt
│       │   │   │   │   ├── SeleccionAcompanamientosSC.kt
│       │   │   │   │   ├── SeleccionEnsaladasSC.kt
│       │   │   │   │   ├── SeleccionPostresBebidasSC.kt
│       │   │   │   │   └── ResumenPlatoScreen.kt
│       │   │   │   └── carrito/             # Carrito y hora de retiro
│       │   │   │       ├── CarritoSC.kt
│       │   │   │       └── SeleccionarHoraRetiroSC.kt
│       │   │   ├── componentes/             # Composables reutilizables
│       │   │   │   ├── ContadorSeleccion.kt
│       │   │   │   ├── Header.kt
│       │   │   │   ├── ProgressBar.kt
│       │   │   │   └── ProteinaCard.kt
│       │   │   └── theme/                   # Color (tokens de marca), Theme, Type
│       │   └── viewmodel/state/
│       │       └── AdminQueueUiState.kt     # Estado reservado para módulo admin
│       └── res/                             # Drawables, themes, values/-night
├── docs/
│   └── memoria_tecnica.pdf                  # Documentación técnica completa
├── AGENTS.md
├── gradle/libs.versions.toml                # Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Requisitos Previos e Instalación

### Requisitos

| Herramienta | Versión mínima |
|---|---|
| Android Studio | Koala o superior (estable) |
| JDK | 11+ (resuelta por el toolchain de Gradle) |
| Android SDK | API 37 instalada |
| Dispositivo/emulador | Android 9 (API 28) o superior |
| Git | Cualquier versión reciente |

No se requieren credenciales ni variables de entorno: la app funciona con datos en memoria; la integración con el backend está preparada pero no conectada.

### Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/thomyconhachedev/CasaJoJoApp.git
cd CasaJoJoApp

# 2. Abrir en Android Studio (File > Open) y dejar sincronizar Gradle
#    — o desde terminal:
./gradlew assembleDebug   # descarga dependencias y genera el APK

# 3. Ejecutar en dispositivo/emulador conectado
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Cómo Ejecutar el Proyecto

```bash
# Compilar (chequeo rápido, sin generar APK)
./gradlew compileDebugKotlin

# Build de desarrollo (APK en app/build/outputs/apk/debug/)
./gradlew assembleDebug

# Build de producción (release)
./gradlew assembleRelease

# Tests unitarios
./gradlew testDebugUnitTest

# Tests instrumentados (requiere dispositivo/emulador conectado)
./gradlew connectedDebugAndroidTest

# Análisis estático
./gradlew lintDebug
```

Para desarrollo iterativo, usar **Run ▶** de Android Studio (modo debug con hot-reload de Compose). La app sigue el tema del sistema (claro/oscuro) automáticamente; no incluye interruptor propio.

---

## Características Principales

### Flujo de armado de platos (cliente)

| # | Pantalla | Descripción |
|---|---|---|
| 1/5 | **Proteína** | Selección obligatoria entre el menú del día; indica disponibilidad ("agotado"). |
| 2/5 | **Acompañamientos** | Mínimo 1, máximo 2 (validado en el ViewModel). |
| 3/5 | **Ensaladas** | Opcional; opción "Sin Ensalada". |
| 4/5 | **Postres y bebidas** | Opcionales; **únicos ítems con precio individual**. |
| 5/5 | **Resumen** | Restricciones/alergias **por plato** (ej. "sin cebolla"); permite pedir otro plato o ir al carrito. |

### Carrito y retiro

- Múltiples platos armados, cada uno con su resumen y restricciones.
- **Hora de retiro** elegible en 25 tramos de 12:00 a 16:00 (cada 10 minutos), modificable desde el carrito.
- Total acumulado calculado reactivamente (`totalCarrito`).

### Reglas de negocio

- Un plato es completo con **proteína + al menos 1 acompañamiento**.
- Precio del plato fijo: `$4.800` (`PlatoArmado.PRECIO_BASE`); solo extras suman aparte.
- Un único `OrderViewModel` compartido garantiza consistencia del pedido en todo el flujo.

### Transversal

- **Material Design 3** con paleta de marca propia (naranjo/verde JoJo) y contraste WCAG AA verificado.
- **Modo oscuro** automático siguiendo el sistema (`values-night`, `SystemBarStyle.auto`, edge-to-edge).
- **DTOs y mapeadores** listos para el endpoint de pedidos (`POST /pedidos`) con Retrofit/Gson; el envío se activará al definir la base URL del backend Spring Boot + PostgreSQL.
- **Estado reservado** para módulo de administración (`AdminQueueUiState`) y seguimiento de pedidos (colores de estado RF13 en `Color.kt`).

---

## Repositorio

```
https://github.com/thomyconhachedev/CasaJoJoApp.git
```

## Licencia

Proyecto privado. Todos los derechos reservados.
