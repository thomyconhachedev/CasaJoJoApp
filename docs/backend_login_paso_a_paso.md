# Backend CasaJoJo — Paso a paso: login con roles (Spring Boot 4.1 / Java 21)

> Guía para agregar el **sistema de usuarios con roles** al backend, alineada
> al sistema híbrido que ya existe en la app Android
> (`data/repository/AuthRepository.kt`). Al terminar, el móvil solo necesita
> su implementación `AuthApi` (sección 9) para dejar el login local.

---

## 0. Primero: cómo funcionan los roles (léelo antes de código)

La app maneja **3 roles**:

| Rol | Cómo lo obtiene | Pide | Pedido viaja con | Ve el panel de cocina |
|---|---|---|---|---|
| **INVITADO** | "Continuar como invitado" en la app (sin cuenta) | Sí, sin fricción | `usuarioId: null` | No |
| **USUARIO** | Registro/login (`POST /auth/registro` o `/auth/login`) | Sí, con nombre/teléfono autocompletados | `usuarioId: <id>` | No |
| **ADMIN** | Solo la cuenta sembrada por el backend (o creada por otro admin) | Sí (también es usuario) | `usuarioId: <id>` | **Sí** |

Decisiones de diseño importantes:

- **El invitado es un rol de primera clase, no un error.** No todo el mundo
  quiere crear cuenta para comprar una vez. Su pedido se crea con
  `usuarioId = null` y con `nombreCliente`/`telefonoContacto` en el body.
  **El backend NO debe rechazar pedidos sin usuario.**
- **El móvil hoy loguea contra Room (local)** con PBKDF2-SHA256. La tabla
  `usuarios` local y esta tabla del backend son mundos separados: el móvil
  migrará su `AuthRepository` a un `AuthApi` que haga estos mismos endpoints.
- **Nunca guardes contraseñas en claro.** Aquí usamos **BCrypt** (estándar
  de Spring Security). En el móvil se usa PBKDF2 porque no depende de
  Spring; ambos son correctos, son implementaciones distintas del mismo
  principio: guardar hash + salt, jamás la contraseña.
- **Los roles se sirven en el JWT como claim `rol`** (`USUARIO`/`ADMIN`).
  `INVITADO` no viaja en tokens: el invitado simplemente no manda
  `Authorization`.

---

## 1. Dependencias (`pom.xml` o `build.gradle`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

`spring-boot-starter-security` ya trae **BCrypt** (`BCryptPasswordEncoder`).

## 2. Entidad `Usuario` + enum `Rol`

`model/Rol.java`:

```java
public enum Rol { USUARIO, ADMIN }
```

> `INVITADO` **no es un valor de esta tabla**: es la ausencia de sesión.

`model/Usuario.java`:

```java
@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)          // hash BCrypt, NUNCA texto plano
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(length = 100)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    private LocalDateTime creadoEn;
    // getters/setters
}
```

DDL equivalente (MySQL/MariaDB, si prefieres migrarlo a mano):

```sql
CREATE TABLE usuarios (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,        -- hash BCrypt (~60 chars)
    rol       VARCHAR(20)  NOT NULL,        -- USUARIO | ADMIN
    nombre    VARCHAR(100),
    telefono  VARCHAR(20),
    creado_en DATETIME
);
```

## 3. Hash de contraseñas (BCrypt)

```java
@Configuration
public class SecurityBeans {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- **Guardar:** `usuario.setPassword(passwordEncoder.encode(request.password()))`
- **Validar:** `passwordEncoder.matches(request.password(), usuario.getPassword())`
- BCrypt **genera salt interno** en cada hash: no hace falta columna de salt.
- Nunca loguees la contraseña ni la devuelvas en respuestas.

## 4. Cuenta admin sembrada

Al arrancar (o en un endpoint de seeding), crea el admin si no existe:

```java
@Component
public class AdminSeeder implements ApplicationRunner {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    // constructor...
    @Override
    public void run(ApplicationArguments args) {
        if (repo.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("casajojo2026")); // CAMBIAR
            admin.setRol(Rol.ADMIN);
            admin.setNombre("Administrador");
            admin.setCreadoEn(LocalDateTime.now());
            repo.save(admin);
        }
    }
}
```

> El registro público (`/auth/registro`) **siempre crea `Rol.USUARIO`**:
> nadie se puede auto-promover a admin desde la app.

## 5. DTOs

```java
public record RegistroRequest(String username, String password, String nombre, String telefono) {}
public record LoginRequest(String username, String password) {}
public record AuthResponse(String token, Long id, String username, String rol, String nombre, String telefono) {}
```

Valida con Jakarta (`@NotBlank`, `@Size(min = 3)` en username, `@Size(min = 6)`
en password) y devuelve `400 {"error": "..."}` como el resto del backend.

## 6. Servicio + controlador de autenticación

`AuthService`: `registrar()` (valida username único → `409` si existe, hashea,
guarda, devuelve token) y `login()` (`findByUsername` → `matches` → token;
si no coincide: `401 {"error": "Usuario o contraseña incorrectos."}` —
**mensaje genérico para no revelar si el usuario existe**).

`AuthController`:

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @PostMapping("/registro") public AuthResponse registrar(@Valid @RequestBody RegistroRequest r) { ... }
    @PostMapping("/login")    public AuthResponse login(@Valid @RequestBody LoginRequest r) { ... }
    @GetMapping("/me")        public AuthResponse yo(@RequestHeader("Authorization") String h) { ... }
}
```

## 7. JWT

- Claim: `sub` = username, `rol` = `USUARIO`/`ADMIN`, expiración ~7 días
  (suficiente para un MVP; el móvil la re-minta al iniciar sesión).
- `JwtService`: `generarToken(Usuario)` y `extraerUsername(token)`.
- Secreto en variable de entorno `JWT_SECRET` (**nunca** en el repo, igual
  que la IP no es secreto pero la clave sí lo es).
- Filtro `JwtAuthFilter`: si el header `Authorization: Bearer <token>` viene
  válido, carga el `Usuario` y lo pone en el `SecurityContext`.

## 8. Seguridad (`SecurityFilterChain`)

| Ruta | Acceso |
|---|---|
| `GET /api/v1/menu/**`, `POST /api/v1/menu/sincronizar` | público (la app los usa también en modo cliente) |
| `POST /api/v1/auth/registro`, `POST /api/v1/auth/login` | público |
| `POST /api/v1/pedidos` | público (invitado) o autenticado (asocia usuarioId) |
| `GET /api/v1/pedidos` | `hasRole("ADMIN")` |
| `PATCH /api/v1/pedidos/{id}/estado` | `hasRole("ADMIN")` |
| `GET /api/v1/auth/me` | autenticado |

```java
http.csrf(csrf -> csrf.disable())          // API stateless, no hay cookies
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/menu/**", "/api/v1/auth/login",
                         "/api/v1/auth/registro", "/api/v1/pedidos").permitAll()
        .requestMatchers("/api/v1/pedidos/**").hasRole("ADMIN")
        .requestMatchers("/api/v1/auth/me").authenticated()
        .anyRequest().authenticated())
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
```

> **Pendiente de decisión:** `GET /api/v1/pedidos` hoy es "la cola completa".
> Si algún día los clientes deben ver SOLO su pedido, se agrega
> `GET /api/v1/pedidos/mios` autenticado en vez de abrir la cola.

### Asociación de pedidos a usuario

Cuando el móvil envía `POST /api/v1/pedidos`:

- Sin header `Authorization` → pedido de **invitado**: se guarda con
  `usuario_id = NULL`. Es válido.
- Con header válido → el backend ignora el `usuarioId` del body (si viniera)
  y usa el `id` del token. Así nadie puede firmar pedidos por otro usuario.

Agrega a la tabla `pedidos` una columna `usuario_id BIGINT NULL REFERENCES usuarios(id)`.

## 9. Cómo conectar el móvil después (sin tocar la UI)

La app ya tiene la puerta preparada:

1. Crear `AuthApi` (Retrofit) con los 3 endpoints de la sección 6.
2. Crear `AuthRepositoryApi` que implemente `AuthRepository`
   (`data/repository/AuthRepository.kt`): su `iniciarSesion()` llama a
   `/auth/login`, guarda el token y publica la `Sesion` en el `StateFlow`.
3. Cambiar UNA línea en `ServicioLocator.authRepository` para que devuelva
   la implementación remota.
4. Agregar un interceptor OkHttp que adjunte `Authorization: Bearer <token>`
   a `crearPedido` cuando haya sesión (los invitados siguen sin header).
5. **Nota:** `NetworkModule.crearApiService()` hoy no configura OkHttp
   explícitamente; al implementar esto se agrega `OkHttpClient` con el
   interceptor y timeouts explícitos.

## 10. Prueba con curl (sin Postman)

```powershell
# Registro de un cliente
curl -X POST http://100.62.186.171:8080/api/v1/auth/registro `
  -H "Content-Type: application/json" `
  -d '{"username":"ana","password":"secreto123","nombre":"Ana Pérez","telefono":"+56912345678"}'

# Login (devuelve el token)
curl -X POST http://100.62.186.171:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"casajojo2026"}'

# Cola (solo ADMIN) — debe dar 401 sin token y 200 con él
curl http://100.62.186.171:8080/api/v1/pedidos
curl http://100.62.186.171:8080/api/v1/pedidos -H "Authorization: Bearer <TOKEN>"

# Pedido de invitado (sin token): debe crear OK con usuario_id NULL
curl -X POST http://100.62.186.171:8080/api/v1/pedidos `
  -H "Content-Type: application/json" `
  -d '{"nombreCliente":"Invitado Test","horaRetiroSolicitada":"14:00","items":[{"itemMenuId":1,"cantidad":1}]}'
```

## 11. Checklist de entrega

- [ ] `JWT_SECRET` en variable de entorno, no en `application.properties` del repo.
- [ ] Password del admin sembrado cambiado respecto a `casajojo2026`.
- [ ] `GET /pedidos` y `PATCH /pedidos/{id}/estado` responden 401 sin token.
- [ ] Registro con username repetido → 409; login malo → 401 genérico.
- [ ] Pedido sin token crea el pedido con `usuario_id NULL`.
- [ ] Ninguna respuesta incluye el campo `password`.
