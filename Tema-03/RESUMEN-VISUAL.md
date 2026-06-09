# 📊 Resumen visual: Arquitectura Vertical Slice + Flujo de solicitudes (Spring Boot)

---

## 🏗️ Vertical Slice Architecture

```
                    ┌─────────────────────────────────┐
                    │  catalog-api (ShopMicro)        │
                    └──────────────┬──────────────────┘
                                   │
                ┌──────────────────┴──────────────────┐
                │                                     │
         ┌──────▼──────┐                      ┌──────▼──────┐
         │   features  │                      │    core     │
         └──────┬──────┘                      └─────────────┘
                │                                     │
        ┌───────┴────────┐                  ┌────────┴────────┐
        │                │                  │                 │
    ┌───▼─────┐    ┌────▼────┐        ┌────▼────┐    ┌──────▼──────┐
    │ products│    │ orders  │        │behaviors│    │   errors    │
    └────┬────┘    └─────────┘        └────┬────┘    └─────────────┘
         │                                   │
    ┌────┴─────────────────────┐            │
    │                          │            │
┌───▼──┐ ┌────▼────┐ ┌────────▼───┐ ┌──────▼─────────────┐
│create│ │getbyid  │ │   list     │ │ValidationMiddleware│
│      │ │         │ │            │ │ (Pipeline PipelinR)│
└──────┘ └─────────┘ └────────────┘ └────────────────────┘
│        │create                     │  Ejecuta validadores
│        │  ├─ Endpoint              │  Lanza ValidationException
│        │  ├─ Handler               │  (→ 422 + ProblemDetail)
│        │  ├─ Validator
│        │  ├─ Command
│        │  └─ Response

CADA FEATURE (create, getbyid, etc.) ES UN PAQUETE AUTOCONTENIDO
                             ↓
            CAMBIOS LOCALIZADOS, SIN CASCADA
```

---

## 🔄 Flujo completo de una solicitud HTTP

```
╔════════════════════════════════════════════════════════════════════════════╗
║ 1️⃣ HTTP REQUEST llega a catalog-api                                       ║
║    POST /api/v1/products                                                   ║
║    Content-Type: application/json                                          ║
║    Body: {                                                                 ║
║      "name": "Laptop XPS 13",                                              ║
║      "description": "Ultrabook premium",                                   ║
║      "price": 999.99,                                                      ║
║      "stock": 5                                                            ║
║    }                                                                       ║
╚════════════════════════════════════════════════════════════════════════════╝
                                   ↓
╔════════════════════════════════════════════════════════════════════════════╗
║ 2️⃣ ENDPOINT (CreateProduct.Endpoint.create) - @RestController             ║
║    • Recibe CreateProductRequest (DTO)                                     ║
║    • Transforma a CreateProduct.Command                                    ║
║    • Envía via pipeline.send(command)                                      ║
╚════════════════════════════════════════════════════════════════════════════╝
                                   ↓
╔════════════════════════════════════════════════════════════════════════════╗
║ 3️⃣ PIPELINR PIPELINE                                                      ║
║    • ValidationMiddleware intercepta (Command.Middleware)                  ║
║    • Ejecuta CreateProductValidator                                        ║
║    • Reglas FluentValidation:                                              ║
║      ✓ name: notEmpty + length(3,100) + matches regex                     ║
║      ✓ description: notEmpty + length(10,500)                             ║
║      ✓ price: > 0, <= 999999.99                                           ║
║      ✓ stock: >= 0, <= 999999                                             ║
║    • Si validación falla → throw ValidationException                       ║
║    • Si validación OK → next.invoke()                                      ║
╚════════════════════════════════════════════════════════════════════════════╝
                                   ↓
╔════════════════════════════════════════════════════════════════════════════╗
║ 4️⃣ HANDLER (CreateProduct.Handler) - Command.Handler<Command, Response>   ║
║    • ProductRepository inyectado (ISP - métodos específicos)               ║
║    • Product product = Product.create(...)                                 ║
║    • repository.add(product)                                               ║
║      └─ Spring Data JPA + Hibernate + PostgreSQL                           ║
║         ├─ INSERT INTO products (...)                                      ║
║         └─ commit @Transactional → transacción confirmada                 ║
║    • return new Response(product.getId(), product.getPrice())             ║
╚════════════════════════════════════════════════════════════════════════════╝
                                   ↓
╔════════════════════════════════════════════════════════════════════════════╗
║ 5️⃣ ENDPOINT (continuación)                                                ║
║    • Recibe Response de Handler                                            ║
║    • return ResponseEntity.created(location).body(response)                ║
║      └─ Status: 201 Created                                                ║
║      └─ Location: /api/v1/products/{id}                                    ║
║      └─ Body: Response JSON                                                ║
╚════════════════════════════════════════════════════════════════════════════╝
                                   ↓
╔════════════════════════════════════════════════════════════════════════════╗
║ 6️⃣ RESPONSE (éxito)                                                       ║
║    HTTP/1.1 201 Created                                                    ║
║    Content-Type: application/json                                          ║
║    Location: /api/v1/products/550e8400-e29b-41d4-a716-446655440000       ║
║                                                                            ║
║    {                                                                       ║
║      "id": "550e8400-e29b-41d4-a716-446655440000",                        ║
║      "price": 999.99                                                      ║
║    }                                                                       ║
╚════════════════════════════════════════════════════════════════════════════╝
```

---

## ❌ Flujo en caso de ERRORES

### Caso A: Validación falla (422 Unprocessable Entity)

```
HTTP Request POST /api/v1/products
  ├─ name: ""                    ❌ Vacío
  ├─ price: -10                  ❌ Negativo
  └─ stock: "abc"                ❌ No es número
                                  ↓
         ValidationMiddleware
                ↓
    ValidationException lanzada
                ↓
    GlobalExceptionHandler (@RestControllerAdvice) atrapa
                ↓
      ┌─────────────────────────────┐
      │ ProblemDetail (RFC 7807)    │
      ├─────────────────────────────┤
      │ status: 422                 │
      │ type: /errors/validation    │
      │ title: Validation Error     │
      │ detail: "..."               │
      │ errors: {                   │
      │   "name": [                 │
      │     "El nombre es obligatorio",
      │     "Debe tener 3-100 car..." │
      │   ],                        │
      │   "price": [                │
      │     "Debe ser > 0"          │
      │   ],                        │
      │   "stock": [                │
      │     "Debe ser número entero"│
      │   ]                         │
      │ }                           │
      └─────────────────────────────┘
                ↓
        HTTP/1.1 422 Unprocessable Entity
```

### Caso B: Recurso no encontrado (404 Not Found)

```
HTTP Request GET /api/v1/products/00000000-0000-0000-0000-000000000000
                                  ↓
         Handler busca en BD
                ↓
    repository.getById(id)  → Optional.empty()
                ↓
    .orElseThrow(() -> new ProductNotFoundException(id))
                ↓
    GlobalExceptionHandler atrapa
                ↓
      ┌──────────────────────────────┐
      │ ProblemDetail (RFC 7807)     │
      ├──────────────────────────────┤
      │ status: 404                  │
      │ type: /errors/not-found      │
      │ title: Not Found             │
      │ detail: "El producto con ID  │
      │          00000000-0000-0000  │
      │          no fue encontrado"  │
      │ instance: /api/v1/products/..│
      └──────────────────────────────┘
                ↓
        HTTP/1.1 404 Not Found
```

### Caso C: Conflicto de negocio (409 Conflict)

```
HTTP Request POST /api/v1/orders/add-item
  Body: {
    "productId": "550e8400-...",
    "quantity": 100              ❌ Solo hay 5 en stock
  }
                ↓
         Handler intenta restar stock
                ↓
    if (product.getStock() < quantity)
        throw new InsufficientStockException(...)
                ↓
    GlobalExceptionHandler atrapa
                ↓
      ┌──────────────────────────────┐
      │ ProblemDetail (RFC 7807)     │
      ├──────────────────────────────┤
      │ status: 409                  │
      │ type: /errors/insufficient.. │
      │ title: Conflict              │
      │ detail: "Stock insuficiente: │
      │          solicitado 100,     │
      │          disponible 5"       │
      │ productId: "550e8400-..."    │
      │ requestedStock: 100          │
      │ availableStock: 5            │
      └──────────────────────────────┘
                ↓
        HTTP/1.1 409 Conflict
```

---

## 📦 Estructura de directorios implementada

```
catalog-api/src/main/java/com/shopmicro/catalog/
│
├── features/
│   └── products/                          ← VERTICAL SLICE
│       ├── create/
│       │   ├── CreateProduct.java         ← Nested Classes
│       │   │   ├─ public static class Endpoint  ← @RestController
│       │   │   ├─ public static class Handler   ← Command.Handler<Command, Response>
│       │   │   ├─ public static class Validator ← AbstractValidator<Command>
│       │   │   ├─ public record Command         ← implements Command<Response>
│       │   │   └─ public record Response
│       │   ├── CreateProductRequest.java   ← DTO entrada
│       │   └── CreateProductValidator.java ← Reglas FluentValidation
│       │
│       ├── getbyid/
│       │   └── GetProductById.java
│       │       ├─ Query (lectura)
│       │       └─ Handler
│       │
│       ├── list/
│       │   └── ListProducts.java
│       │       ├─ Query con paginación
│       │       └─ Handler
│       │
│       ├── update/
│       │   └── UpdateProduct.java
│       │
│       ├── delete/
│       │   └── DeleteProduct.java
│       │
│       ├── domain/                        ← LÓGICA DE NEGOCIO
│       │   ├── Product.java               ← Entidad @Entity
│       │   ├── ProductRepository.java     ← ISP (métodos específicos)
│       │   └── exceptions/
│       │       ├── ProductNotFoundException.java
│       │       ├── InsufficientStockException.java
│       │       └── InvalidProductException.java
│       │
│       ├── infrastructure/                ← PERSISTENCIA
│       │   ├── JpaProductRepository.java       ← Adaptador ISP
│       │   └── SpringDataProductRepository.java← JpaRepository
│       │
│       └── dtos/                          ← DATA TRANSFER OBJECTS (record)
│           ├── ProductResponse.java
│           ├── ProductListItemResponse.java
│           └── CreateProductRequest.java
│
├── core/                                  ← COMPORTAMIENTOS TRANSVERSALES
│   ├── behaviors/
│   │   └── ValidationMiddleware.java      ← Command.Middleware (PipelinR)
│   ├── validators/
│   │   └── CustomValidators.java          ← Reglas reutilizables
│   ├── errors/
│   │   └── ErrorTypes.java                ← Constantes de tipos error
│   └── fluentvalidation/                  ← Infraestructura del validador fluido
│       ├── AbstractValidator.java
│       ├── RuleBuilder.java
│       └── ValidationException.java
│
├── infrastructure/                        ← WEB Y SERVICIOS
│   ├── web/
│   │   ├── GlobalExceptionHandler.java    ← @RestControllerAdvice
│   │   └── ValidationExceptionHandler.java← Manejo de validación → 422
│   ├── health/
│   │   └── CustomHealthIndicator.java     ← HealthIndicator (Actuator)
│   └── mapping/
│       └── ProductMapper.java             ← MapStruct (opcional)
│
├── config/                                ← CONFIGURACIÓN (@Configuration)
│   ├── PipelinrConfiguration.java         ← bean Pipeline (PipelinR)
│   └── OpenApiConfiguration.java          ← bean OpenAPI (Swagger)
│
└── CatalogApiApplication.java             ← @SpringBootApplication (main)

src/main/resources/
├── application.yml                        ← CONFIGURACIÓN
│   ├─ spring.datasource (ConnectionString)
│   ├─ spring.jpa
│   ├─ management (Actuator / health)
│   └─ springdoc (Swagger)
└── db/migration/                          ← Flyway migrations
    └─ V1__create_products.sql

pom.xml
├─ Java 21, Spring Boot 3.x
├─ Dependencias:
│   ├─ pipelinr (an.awesome)
│   ├─ fluentvalidation (port Java)
│   ├─ springdoc-openapi-starter-webmvc-ui
│   ├─ spring-boot-starter-data-jpa + postgresql
│   └─ spring-boot-starter-actuator
└─ mapstruct (opcional)
```

---

## 🔗 Relación entre componentes

```
┌─────────────────────────────────────────────────────────────────────┐
│                 Endpoint (@RestController)                          │
│                  Recibe HTTP Request, devuelve JSON                  │
└───────────┬─────────────────────────────────────────────────┬───────┘
            │                                                 │
            ▼                                                 ▼
    ┌──────────────┐                                 ┌───────────────┐
    │ DTO Request  │                                 │ DTO Response  │
    │ (record)     │                                 │ (record)      │
    └──────┬───────┘                                 └───┬───────────┘
           │                                             │
           │ toCommand()                                 │
           │                                             │
           ▼                                             │
    ┌──────────────┐                                     │
    │ pipeline     │                                     │
    │ .send(Cmd)   │  ← PipelinR                         │
    └──────┬───────┘                                     │
           │                                             │
           ▼                                             │
    ┌──────────────────┐                                │
    │ Validation       │  ◄─── Validadores (FluentVal) │
    │ Middleware       │       Reglas complejas         │
    │ (Command.Middle) │                                │
    └──────┬───────────┘                                │
           │                                             │
           ▼                                             │
    ┌──────────────┐                                     │
    │ Handler      │                                     │
    │ (Servicio)   │                                     │
    └──────┬───────┘                                     │
           │                                             │
           ▼                                             │
    ┌──────────────────────┐                            │
    │ ProductRepository    │ ◄─── ISP (Interface        │
    │ (Abstracción)        │       Segregation Principle)
    │ add()                │       Métodos específicos  │
    │ getById()            │       (no genéricos)       │
    │ update()             │                            │
    │ delete()             │                            │
    └──────┬───────────────┘                            │
           │                                             │
           ▼                                             │
    ┌──────────────────────┐                            │
    │ JpaProductRepository │                            │
    │ (Implementación)     │                            │
    └──────┬───────────────┘                            │
           │                                             │
           ▼                                             │
    ┌──────────────────────┐                            │
    │ Spring Data JPA +    │  ← INSERT/SELECT/UPDATE   │
    │ Hibernate + Postgres │                            │
    └──────────────────────┘                            │
           │                                             │
           │ Resultado                                  │
           │ (Product entity)                           │
           │                                             │
           ▼                                             │
    ┌──────────────────────┐                            │
    │ Handler transforma   │ ◄─── Mapping (opcional)    │
    │ Response             │       entity → DTO         │
    └──────────┬───────────┘                            │
               │                                         │
               └────────────────┬──────────────────────►┼──────────┐
                                │                        │          │
                                ▼                        │          │
                        ┌────────────────┐               │          │
                        │ Endpoint rec.  │               │          │
                        │ response       │               │          │
                        └────────┬───────┘               │          │
                                 │                       │          │
                  return ResponseEntity.created()        │          │
                     HTTP 201 + Location header          │          │
                                 │                       │          │
                                 └───────────────────────┴──────────┘
                                           │
                                           ▼
                                   HTTP Response JSON
```

---

## 🎯 Matriz de responsabilidades (SOLID)

```
┌────────────────────┬───────────────┬─────────────────────────┐
│ Componente         │ Responsabilidad   │ Principio SOLID     │
├────────────────────┼───────────────┼─────────────────────────┤
│ Endpoint           │ HTTP parsing  │ SRP (Single Resp.)      │
│ (@RestController)  │ Status codes  │                         │
├────────────────────┼───────────────┼─────────────────────────┤
│ DTO (record)       │ Mapeo datos   │ LSP (Liskov Subst.)     │
│                    │ Contract API  │                         │
├────────────────────┼───────────────┼─────────────────────────┤
│ Validator          │ Reglas entrada│ SRP                     │
│ (FluentValidation) │ Declarativas  │ OCP (Open/Closed)       │
├────────────────────┼───────────────┼─────────────────────────┤
│ Handler            │ Lógica proceso│ SRP                     │
│ (Command.Handler)  │ Orquestar     │ DIP (Dependency Invert) │
├────────────────────┼───────────────┼─────────────────────────┤
│ Entity (@Entity)   │ Lógica negocio│ SRP                     │
│ (Product)          │ Invariantes   │ OCP                     │
├────────────────────┼───────────────┼─────────────────────────┤
│ Repository         │ Persistencia  │ ISP (Interface Segr.)   │
│ Interface          │ Métodos claros│ DIP                     │
├────────────────────┼───────────────┼─────────────────────────┤
│ Exceptions         │ Errores       │ SRP                     │
│ (RuntimeException) │ Negocio       │ OCP                     │
├────────────────────┼───────────────┼─────────────────────────┤
│ ProblemDetail      │ Respuestas    │ LSP                     │
│                    │ Estándar      │                         │
├────────────────────┼───────────────┼─────────────────────────┤
│ @RestControllerAdv │ Atrapar errs  │ SRP                     │
│                    │ Transformar   │                         │
└────────────────────┴───────────────┴─────────────────────────┘

S = Single Responsibility    (Una responsabilidad por clase)
O = Open/Closed             (Abierto a extensión, cerrado a modificación)
L = Liskov Substitution      (Subclases reemplazan bases sin romper)
I = Interface Segregation    (Interfaces pequeñas y específicas)
D = Dependency Inversion     (Depender de abstracciones, no implementaciones)
```

---

## 📊 Comparativa: Sin Vertical Slice vs. Con Vertical Slice

```
┌─────────────────────────────────────────────────────────────┐
│ SIN VERTICAL SLICE (Capas tradicionales)                    │
├─────────────────────────────────────────────────────────────┤
│ Nueva feature "CreateProduct" requiere:                     │
│                                                             │
│ 1. controller/ProductController.java        ← Crear/Editar │
│ 2. service/ProductService.java              ← Crear/Editar │
│ 3. repository/ProductRepository.java        ← Crear/Editar │
│ 4. model/Product.java                       ← Crear/Editar │
│ 5. dto/CreateProductRequest.java            ← Crear/Editar │
│ 6. dto/CreateProductResponse.java           ← Crear/Editar │
│ 7. validator/ProductValidator.java          ← Crear/Editar │
│ 8. exception/ProductExceptions.java         ← Crear/Editar │
│                                                             │
│ TOTAL: 8 archivos en 8 paquetes diferentes                │
│        = Cambios dispersos                                 │
│        = Conflictos merge frecuentes                       │
│        = Difícil navegación                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ CON VERTICAL SLICE                                          │
├─────────────────────────────────────────────────────────────┤
│ Nueva feature "CreateProduct" requiere:                     │
│                                                             │
│ features/products/create/CreateProduct.java ← TODO EN UNO  │
│  ├─ public static class Endpoint                            │
│  ├─ public static class Handler                             │
│  ├─ public static class Validator                           │
│  ├─ public record Command                                   │
│  ├─ public record Response                                  │
│  └─ public record CreateProductRequest                      │
│                                                             │
│ TOTAL: 1 archivo en 1 paquete                              │
│        = Cambios localizados                                │
│        = CERO conflictos merge                              │
│        = Fácil navegación                                   │
│        = Máxima cohesión                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔁 Equivalencias .NET → Java/Spring Boot

```
┌──────────────────────────────┬──────────────────────────────────────┐
│ .NET / ASP.NET Core          │ Java / Spring Boot                   │
├──────────────────────────────┼──────────────────────────────────────┤
│ ASP.NET Core Controller      │ @RestController                      │
│ Program.cs                   │ @SpringBootApplication + @Configuration│
│ MediatR (IRequest/Handler)   │ PipelinR (Command/Command.Handler)   │
│ IPipelineBehavior<,>         │ Command.Middleware                   │
│ _mediator.Send(cmd)          │ pipeline.send(cmd)                   │
│ FluentValidation             │ FluentValidation (port Java)         │
│ AbstractValidator<T>         │ AbstractValidator<T> (ruleFor)       │
│ Entity Framework Core        │ Spring Data JPA + Hibernate          │
│ DbContext                    │ JpaRepository + @Entity              │
│ AutoMapper                   │ MapStruct                            │
│ ProblemDetails (RFC 7807)    │ ProblemDetail (Spring 6+)            │
│ Middleware de excepciones    │ @RestControllerAdvice + @ExceptionHandler│
│ Swashbuckle / Swagger        │ springdoc-openapi                    │
│ [ProduceResponseType]        │ @ApiResponse / @Schema              │
│ AspNetCore.HealthChecks      │ Spring Boot Actuator                 │
│ IHealthCheck                 │ HealthIndicator                     │
│ ILogger<T>                   │ SLF4J (Logger)                      │
│ Guid / decimal / DateTime    │ UUID / BigDecimal / Instant         │
│ record / async Task<T>       │ record / (síncrono, JPA bloqueante) │
└──────────────────────────────┴──────────────────────────────────────┘
```

---

**Fin del resumen visual - Módulo 3 completo ✅**
