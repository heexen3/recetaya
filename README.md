# RecetaYa

Sistema de gestión digital de recetas médicas para una cadena de farmacias, construido con microservicios en **Java Spring Boot**, **PostgreSQL** y **Docker**.

El diseño prioriza **disponibilidad, escalabilidad y mantenibilidad**: la emisión de recetas no depende de que Inventario esté disponible, porque la reserva de stock se hace de forma asíncrona.

## Arquitectura

| Servicio | Responsabilidad | Puerto | BD (puerto en host) |
|---|---|---|---|
| `api-gateway` | Punto único de entrada, enrutamiento inverso y validación de JWT | 8080 | N/A |
| `ms-auth` | Usuarios, roles (Médico, Farmacéutico) y emisión de JWT | 8081 | `db-auth` (5431) |
| `ms-recetas` | Emisión, consulta y estados de recetas; publica el evento "receta emitida" | 8082 | `db-recetas` (5432) |
| `ms-inventario` | Catálogo de medicamentos, sucursales y stock por sucursal | 8083 | `db-inventario` (5433) |
| `ms-dispensacion` | Registro de entregas; descuenta stock y marca la receta como dispensada | 8084 | `db-dispensacion` (5434) |

Principios:
- Una base de datos por microservicio (sin esquemas compartidos).
- **API Gateway con JWT**: Todo el tráfico externo ingresa por el puerto `8080`. El Gateway valida el token JWT emitido por `ms-auth`, rechaza peticiones no autorizadas (`401`) e inyecta las cabeceras `X-Auth-User` y `X-Auth-Role` hacia los servicios internos.
- Procesos síncronos: login, consultas y dispensación. Proceso asíncrono: reserva de stock tras emitir una receta (diseño objetivo: Amazon SQS + AWS Lambda).

## Stack

Java 21 · Spring Boot 3.3.5 · Spring Cloud Gateway (2023.0.3) · Spring Data JPA / Hibernate · Bean Validation · Lombok · Spring Security · JJWT (0.12.6) · PostgreSQL 16 · Maven · Docker / Docker Compose

## Estructura del repositorio

```
recetaya/
├── docker-compose.yml
├── README.md
├── api-gateway/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/cl/recetaya/gateway/
│       ├── filter/
│       │   ├── JwtAuthenticationFilter.java
│       │   └── JwtAuthGatewayFilterFactory.java
│       └── GatewayApplication.java
├── ms-auth/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/cl/recetaya/auth/
├── ms-recetas/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/cl/recetaya/recetas/
├── ms-inventario/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/cl/recetaya/inventario/
└── ms-dispensacion/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/cl/recetaya/dispensacion/
```

## Requisitos

- Docker y Docker Compose
- Para ejecutar sin Docker: JDK 21 y Maven 3.9+ (y un PostgreSQL accesible)

## Cómo ejecutar

```bash
git clone <url-del-repositorio>
cd recetaya
docker compose up --build
```

El punto único de entrada del sistema queda disponible en:
- **API Gateway**: `http://localhost:8080`

(Los microservicios internos continúan accesibles directamente en los puertos 8081-8084 para depuración).

Para detener y borrar datos: `docker compose down -v`.

## API (Acceso centralizado vía API Gateway en puerto 8080)

Todas las rutas requieren la cabecera `Authorization: Bearer <token>`, excepto las rutas públicas de autenticación (`/api/auth/registro` y `/api/auth/login`).

### Rutas disponibles en el Gateway:

| Microservicio | Ruta | Métodos | Auth requerida |
|---|---|---|---|
| `ms-auth` | `/api/auth/registro` | POST | No (Pública) |
| `ms-auth` | `/api/auth/login` | POST | No (Pública) |
| `ms-auth` | `/api/auth/usuarios/**` | GET, DELETE | Sí (JWT) |
| `ms-recetas` | `/api/recetas/**` | GET, POST, PATCH | Sí (JWT) |
| `ms-inventario`| `/api/medicamentos/**` | GET, POST, PUT, DELETE | Sí (JWT) |
| `ms-inventario`| `/api/sucursales/**` | GET, POST, PUT, DELETE | Sí (JWT) |
| `ms-inventario`| `/api/stock/**` | GET, PUT | Sí (JWT) |
| `ms-dispensacion` | `/api/dispensaciones/**` | GET, POST | Sí (JWT) |

Errores devueltos: `401` falta o token inválido, `404` recurso inexistente, `409` conflicto de estado o stock insuficiente, `400` validación; formato `{"error": "..."}`.

## Pruebas con Postman (Colección Incluida)

El proyecto incluye la colección oficial en el archivo [`postman_collection.json`](./postman_collection.json) con todas las peticiones listas para ejecutar a través del API Gateway (`http://localhost:8080`).

### 1. Cómo importar la colección
1. Abre **Postman**.
2. Haz clic en el botón **Import** (esquina superior izquierda, o atajo `Ctrl + O`).
3. Arrastra el archivo `postman_collection.json` a la ventana de Postman (o haz clic en *Upload Files* y selecciónalo).
4. Confirma la importación. Verás la colección **`RecetaYa - API Endpoints (Gateway)`** en el panel izquierdo.

### 2. Gestión automática del Token JWT
- Al ejecutar la petición **`1.3 Login`**, Postman ejecuta un script de pruebas integrado que **captura automáticamente el token JWT** de la respuesta y lo asigna a la variable de colección `{{jwtToken}}`.
- Todas las peticiones posteriores de las carpetas 2, 3 y 4 ya tienen configurado `Authorization: Bearer {{jwtToken}}`, por lo que **no necesitas copiar y pegar el token manualmente**.

### 3. Flujo guiado de pruebas (orden sugerido)

1. **1. Autenticación (`ms-auth`)**:
   - `1.1 Registro Médico`: Crea al usuario `dr.perez` con rol `MEDICO` (Retorna `201 Created`).
   - `1.2 Registro Farmacéutico`: Crea a `farm.garcia` con rol `FARMACEUTICO` (Retorna `201 Created`).
   - `1.3 Login`: Inicia sesión con `dr.perez` y almacena el token JWT (Retorna `200 OK`).
   - `1.4 Listar Usuarios`: Comprueba el acceso a rutas protegidas con token (Retorna `200 OK`).

2. **2. Inventario (`ms-inventario`)**:
   - `2.1 Crear Sucursal`: Registra "Sucursal Centro" (Retorna `201 Created`).
   - `2.3 Crear Medicamento`: Registra "Paracetamol 500mg" con `id: 1` (Retorna `201 Created`).
   - `2.5 Fijar Stock en Sucursal`: Inicializa `50` unidades para el medicamento 1 en la sucursal 1 (Retorna `200 OK`).
   - `2.6 Consultar Stock Medicamento en Sucursal`: Verifica las `50` unidades disponibles (Retorna `200 OK`).

3. **3. Recetas (`ms-recetas`)**:
   - `3.1 Emitir Receta Médica`: Emite una receta para el paciente con `5` unidades de Paracetamol. Queda en estado `EMITIDA` (Retorna `201 Created`).
   - `3.3 Obtener Receta por ID`: Comprueba que la receta fue registrada con sus detalles (Retorna `200 OK`).
   - `3.4 Buscar Recetas por RUT Paciente`: Filtra por el RUT del paciente (Retorna `200 OK`).

4. **4. Dispensación (`ms-dispensacion`)**:
   - `4.1 Dispensar Receta (Flujo Integrado)`: Registra la entrega en farmacia. El microservicio orquesta automáticamente:
     - Descuenta `5` unidades de stock en `ms-inventario`.
     - Actualiza el estado de la receta a `DISPENSADA` en `ms-recetas`.
     - Guarda el registro histórico de la dispensación (Retorna `201 Created`).
   - *Verificación*: Vuelve a ejecutar `2.6` (el stock habrá bajado a `45`) y `3.3` (el estado de la receta ahora es `DISPENSADA`).

5. **5. Pruebas de Seguridad y Validaciones (Casos de Borde / Rúbrica)**:
   - `5.1 Petición Sin Token`: Verifica que el Gateway rechaza accesos sin JWT con `401 Unauthorized`.
   - `5.2 Dispensar Receta Ya Dispensada`: Intenta dispensar nuevamente la misma receta y verifica la respuesta de conflicto `409 Conflict`.
   - `5.3 Consultar Receta Inexistente`: Consulta un ID inválido y verifica la respuesta `404 Not Found`.

### 4. Ejecución en lote (Collection Runner)
Para demostraciones o grabación del video de entrega:
1. Haz clic derecho sobre la colección **`RecetaYa - API Endpoints (Gateway)`** en Postman.
2. Selecciona **Run collection**.
3. Haz clic en **Run RecetaYa - API Endpoints (Gateway)** para ver todo el flujo ejecutarse en secuencia con resultados satisfactorios.

## Flujo Git

- `main`: versión estable.
- `develop`: integración.
- `feature/<nombre>`: una rama por funcionalidad (por ejemplo `feature/api-gateway`), que se une a `develop` mediante merge.
- Mensajes de commit descriptivos, por ejemplo `feat(gateway): agrega api-gateway con validacion jwt`.
