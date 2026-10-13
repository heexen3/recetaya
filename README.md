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

## Estado actual

- [x] `ms-inventario`: CRUD de medicamentos y sucursales, stock por sucursal, reserva de stock
- [x] `ms-recetas`: emisión de recetas con detalles (OneToMany), estados (EMITIDA, DISPENSADA, ANULADA), PublicadorEventos
- [x] `ms-dispensacion`: registra entregas, descuenta stock en Inventario (llamada síncrona), marca receta como DISPENSADA
- [x] `ms-auth`: registro de usuarios, login con JWT, CRUD de usuarios
- [x] `api-gateway`: Spring Cloud Gateway con filtro reactivo para validación de JWT y ruteo centralizado (puerto 8080)
- [x] `docker-compose.yml` con 4 BDs, 4 microservicios y el API Gateway
- [x] Colección de Postman completa (`postman_collection.json`) con flujo punta a punta y pruebas de seguridad

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

### Prueba rápida del flujo completo a través del Gateway (puerto 8080)

```bash
# 1. Registrar médico (Ruta pública)
curl -X POST http://localhost:8080/api/auth/registro -H "Content-Type: application/json" \
  -d '{"username":"dr.perez","password":"pass123","nombreCompleto":"Dr. Juan Pérez","rol":"MEDICO"}'

# 2. Iniciar sesión y obtener JWT
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"dr.perez","password":"pass123"}' | grep -o '"token":"[^"]*' | grep -o '[^"]*$')

echo "Token obtenido: $TOKEN"

# 3. Crear sucursal y medicamento protegidos por JWT
curl -X POST http://localhost:8080/api/sucursales \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Sucursal Centro","direccion":"Av. Principal 123"}'

curl -X POST http://localhost:8080/api/medicamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"codigo":"PARA500","nombre":"Paracetamol 500mg","principioActivo":"Paracetamol","requiereReceta":false}'

# 4. Fijar stock
curl -X PUT http://localhost:8080/api/stock \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"medicamentoId":1,"sucursalId":1,"cantidad":50}'

# 5. Emitir receta médica
curl -X POST http://localhost:8080/api/recetas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pacienteNombre":"María López","pacienteRut":"12345678-9","medicoNombre":"Dr. Juan Pérez","diagnostico":"Dolor de cabeza","detalles":[{"medicamentoId":1,"medicamentoNombre":"Paracetamol 500mg","cantidad":5,"posologia":"1 cada 8 horas"}]}'

# 6. Dispensar receta
curl -X POST http://localhost:8080/api/dispensaciones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"recetaId":1,"sucursalId":1,"farmaceuticoNombre":"Farm. Ana García","detalles":[{"medicamentoId":1,"medicamentoNombre":"Paracetamol 500mg","cantidadDispensada":5}]}'

# 7. Consultar resultado
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/recetas/1
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/stock/consulta?medicamentoId=1&sucursalId=1"
```

## Flujo Git

- `main`: versión estable.
- `develop`: integración.
- `feature/<nombre>`: una rama por funcionalidad (por ejemplo `feature/api-gateway`), que se une a `develop` mediante merge.
- Mensajes de commit descriptivos, por ejemplo `feat(gateway): agrega api-gateway con validacion jwt`.
