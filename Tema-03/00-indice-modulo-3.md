# Módulo 3 — Desarrollo de Microservicios con Spring Boot

> **Duración:** ~5 horas  
> **Versión base:** Spring Boot 3.x (Java 21 LTS)  
> **Lenguaje:** Java  
> **Arquitectura:** Vertical Slice + PipelinR + FluentValidation  
> **Proyecto hilo conductor:** ShopMicro → catalog-api

---

## 📋 Índice de lecciones

| Lección | Tema | Duración | Conceptos clave |
|---------|------|----------|-----------------|
| **3.1** | Diseño de API REST | 45 min | Recursos, verbos HTTP, códigos de estado, versionado |
| **3.2** | Construcción de catalog-api | 90 min | Vertical Slice, Nested Classes, PipelinR, ISP |
| **3.3** | Capas, DTOs y Mapeo | 60 min | Arquitectura por capas, DTOs, MapStruct, excepciones |
| **3.4** | Validación con FluentValidation | 60 min | Validadores declarativos, cross-field, async, testing |
| **3.5** | Manejo de errores (ProblemDetail) | 45 min | RFC 7807, excepciones de dominio, handler global |
| **3.6** | Documentación (OpenAPI/Swagger) | 45 min | Swagger UI, anotaciones, versionado API |
| **3.7** | Health Checks | 30 min | Liveness/Readiness, Actuator, custom checks |

**Total:** ~5 horas (376 minutos)

---

## 🎯 Objetivos del módulo

Al completar este módulo serás capaz de:

✅ Diseñar APIs REST siguiendo principios HATEOAS  
✅ Implementar servicios con arquitectura **Vertical Slice** (feature-driven)  
✅ Usar **PipelinR** como patrón de mediador para desacoplamiento  
✅ Validar entrada robustamente con **FluentValidation**  
✅ Manejar errores consistentemente con **ProblemDetail** (RFC 7807)  
✅ Documentar APIs automáticamente con **springdoc-openapi (Swagger)**  
✅ Implementar health checks para monitoreo en Kubernetes  
✅ Aplicar **SOLID** (especialmente ISP en repositorios)  
✅ Separar responsabilidades con DTOs y mapeo  
✅ Construir catalog-api funcional y lista para producción  

---

## 🏗️ Arquitectura implementada

### Estructura de paquetes (Vertical Slice)

```
catalog-api/
└── src/main/java/com/shopmicro/catalog/
    ├── features/
    │   └── products/
    │       ├── create/
    │       │   ├── CreateProduct.java        (Endpoint + Handler + Command + Response)
    │       │   ├── CreateProductValidator.java
    │       │   └── CreateProductRequest.java
    │       ├── getbyid/
    │       │   └── GetProductById.java       (Query versión del patrón)
    │       ├── list/
    │       │   └── ListProducts.java
    │       ├── update/
    │       ├── delete/
    │       ├── domain/
    │       │   ├── Product.java              (Entidad de dominio)
    │       │   ├── ProductRepository.java
    │       │   └── exceptions/
    │       │       ├── ProductNotFoundException.java
    │       │       ├── InsufficientStockException.java
    │       │       └── InvalidProductException.java
    │       ├── infrastructure/
    │       │   └── JpaProductRepository.java  (Implementación Spring Data JPA)
    │       └── dtos/
    │           ├── ProductResponse.java
    │           ├── ProductListItemResponse.java
    │           └── CreateProductRequest.java
    ├── core/
    │   ├── behaviors/
    │   │   └── ValidationMiddleware.java     (Middleware de PipelinR)
    │   ├── localization/
    │   ├── validators/
    │   │   └── CustomValidators.java         (Reglas reutilizables)
    │   └── errors/
    │       └── ErrorTypes.java
    ├── infrastructure/
    │   ├── web/
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── ValidationExceptionHandler.java
    │   └── health/
    │       └── CustomHealthIndicator.java
    └── CatalogApiApplication.java
```

> En Spring Boot la configuración (equivalente a `Program.cs`) vive en la clase
> `@SpringBootApplication` (`CatalogApiApplication`), en `application.yml` y en
> clases `@Configuration`. No existe un único archivo de arranque imperativo.

### Flujo de una solicitud

```
HTTP Request
    ↓
Endpoint (@RestController)
    ↓
pipeline.send(Command/Query)   // PipelinR
    ↓
ValidationMiddleware (Pipeline)
  ├─ FluentValidation ejecuta validadores
  └─ Si fallos: lanza ValidationException
    ↓
Handler (Command.Handler<C, R>)
    ├─ Accede a ProductRepository (ISP)
    └─ Devuelve Response
    ↓
GlobalExceptionHandler (@RestControllerAdvice)
    ├─ Transforma en ProblemDetail (RFC 7807)
    └─ Retorna JSON con status HTTP apropiado
    ↓
HTTP Response (200, 201, 400, 404, 409, 422, 500, etc.)
```

---

## 🔧 Tecnologías utilizadas

| Componente | Tecnología | Función |
|------------|-----------|---------|
| **API Framework** | Spring Web MVC (`@RestController`) | Endpoints HTTP |
| **ORM** | Spring Data JPA / Hibernate | Acceso a datos |
| **Base de datos** | PostgreSQL (recomendado) | Persistencia |
| **Patrón de mediador** | PipelinR | Desacoplamiento |
| **Validación** | FluentValidation (port Java) | Validación declarativa |
| **Mapeo** | MapStruct (opcional) | DTO ↔ Entity |
| **Documentación** | springdoc-openapi (Swagger UI) | API autodescriptiva |
| **Health Checks** | Spring Boot Actuator | Monitoreo K8s |
| **Logging** | SLF4J (built-in) | Trazabilidad |

---

## 📝 Patrones implementados

### 1. Vertical Slice Architecture
**Beneficio:** Cambios localizados, menos conflictos merge  
**Ejemplo:** Feature CreateProduct = 1 paquete con Endpoint, Handler, Validator, Request, Response

### 2. Nested Classes
**Beneficio:** Cohesión alta, encapsulación  
**Ejemplo:**
```java
public final class CreateProduct {
    @RestController public static class Endpoint { ... }
    @Component     public static class Handler implements Command.Handler<Command, Response> { ... }
    @Component     public static class Validator extends AbstractValidator<Command> { ... }
    public record Command(...) implements an.awesome.pipelinr.Command<Response> { ... }
    public record Response(...) { ... }
}
```

### 3. CQRS Lite (Command Query Responsibility Segregation)
**Beneficio:** Separación clara entre operaciones lectura/escritura  
**Ejemplo:**
- Commands: CreateProduct, UpdateProduct, DeleteProduct
- Queries: GetProductById, ListProducts

### 4. Interface Segregation Principle (ISP)
**Beneficio:** Interfaces específicas, no genéricas  
**Ejemplo:**
```java
public interface ProductRepository {
    void add(Product product);
    Optional<Product> getById(UUID id);
    void update(Product product);
    void delete(UUID id);
}
```

### 5. Problem Details (RFC 7807)
**Beneficio:** Respuestas de error consistentes y machine-readable  
**Ejemplo:**
```json
{
  "type": "https://api.shopmicro.local/errors/validation-error",
  "title": "Validation Error",
  "status": 422,
  "detail": "...",
  "errors": { "name": [...], "price": [...] }
}
```

---

## 🚀 Flujo de desarrollo práctico

### Paso 1: Definir el recurso (Dominio)
```java
@Entity
public class Product { ... }

public interface ProductRepository { ... }
```

### Paso 2: Crear la feature (Command/Query + Handler)
```java
public final class CreateProduct {
    @Component
    public static class Handler implements Command.Handler<Command, Response> { ... }
    public record Command(...) implements an.awesome.pipelinr.Command<Response> { }
    public record Response(...) { }
}
```

### Paso 3: Validar entrada
```java
@Component
public class CreateProductValidator extends AbstractValidator<CreateProduct.Command> {
    public CreateProductValidator() {
        ruleFor(CreateProduct.Command::name).notEmpty().maximumLength(100);
        // ... más reglas
    }
}
```

### Paso 4: Exponer el endpoint
```java
@PostMapping
public ResponseEntity<CreateProduct.Response> create(@RequestBody CreateProductRequest request) {
    var command = request.toCommand();
    var response = pipeline.send(command);
    var location = URI.create("/api/v1/products/" + response.id());
    return ResponseEntity.created(location).body(response);
}
```

### Paso 5: Documentar
```java
@Operation(summary = "Crea un nuevo producto")
@ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
@ApiResponse(responseCode = "422", description = "Error de validación")
```

---

## 🎓 Concepto central: Vertical Slice vs. Capas tradicionales

### ❌ Capas tradicionales (difícil de mantener)

```
controller/
  ProductController.java
service/
  ProductService.java
repository/
  ProductRepository.java
model/
  Product.java
  CreateProductDto.java
  CreateProductResponse.java

Para agregar una feature: tocar 5 archivos en 5 paquetes
```

### ✅ Vertical Slice (fácil de mantener)

```
features/products/create/
  CreateProductEndpoint.java
  CreateProductHandler.java
  CreateProductValidator.java
  CreateProductRequest.java

Para agregar una feature: nuevo paquete = cambios localizados
```

---

## 📚 Referencia de comandos útiles

### Crear proyecto (Spring Initializr)
```bash
# Vía https://start.spring.io o CLI:
spring init \
  --boot-version=3.3.0 \
  --java-version=21 \
  --dependencies=web,data-jpa,postgresql,validation,actuator \
  --build=maven \
  --group-id=com.shopmicro \
  --artifact-id=catalog-api \
  catalog-api
cd catalog-api
```

### Dependencias necesarias (`pom.xml`)
```xml
<dependencies>
    <!-- API REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Persistencia (JPA + Hibernate) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Mediador (equivalente a MediatR) -->
    <dependency>
        <groupId>an.awesome</groupId>
        <artifactId>pipelinr</artifactId>
        <version>0.9</version>
    </dependency>

    <!-- FluentValidation (port Java) -->
    <dependency>
        <groupId>com.github.fluentvalidation</groupId>
        <artifactId>fluentvalidation</artifactId>
        <version>1.x</version>
    </dependency>

    <!-- Documentación OpenAPI / Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.6.0</version>
    </dependency>

    <!-- Health Checks -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Mapeo (opcional) -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.6.0</version>
    </dependency>
</dependencies>
```

> **Nota sobre FluentValidation:** en estos documentos usamos el estilo
> `AbstractValidator<T>` / `ruleFor(...)` idéntico al de .NET. Existen ports de la
> comunidad para Java; como alternativa nativa de Spring está **Jakarta Bean
> Validation** (Hibernate Validator). Mantenemos la API fluida por su valor
> didáctico y por equivalencia directa con el material original.

### Ejecutar
```bash
./mvnw spring-boot:run
# Swagger UI: http://localhost:8080/swagger-ui.html
# Health:     http://localhost:8080/actuator/health/readiness
```

### Migraciones de esquema (Flyway / Hibernate)
```bash
# Con Flyway (recomendado en producción):
# coloca V1__create_products.sql en src/main/resources/db/migration
./mvnw flyway:migrate

# En desarrollo puedes usar:
# spring.jpa.hibernate.ddl-auto=update  (en application.yml)
```

---

## ✅ Checklist de finalización del módulo

Antes de pasar al Módulo 4, verifica:

- [ ] Comprendido Vertical Slice Architecture
- [ ] Implementado al menos 3 features CRUD (Create, Read, Update)
- [ ] Todos los endpoints validados con FluentValidation
- [ ] Errores manejados con ProblemDetail (RFC 7807)
- [ ] Documentación Swagger generada automáticamente
- [ ] Health checks implementados (liveness + readiness)
- [ ] Base de datos PostgreSQL integrada
- [ ] Repositorio sigue ISP (métodos específicos, no genéricos)
- [ ] PipelinR configurado con ValidationMiddleware
- [ ] DTOs separados de entidades de dominio
- [ ] Tests unitarios para validadores

---

## 🔗 Conexión con módulos siguientes

### Módulo 4 — Comunicación entre Microservicios
Con catalog-api completa, ahora necesitará **comunicarse**:
- HTTP con `RestClient` / `WebClient`
- Feign (typed clients declarativos)
- gRPC (alto rendimiento)
- Mensajería asíncrona (RabbitMQ + Spring AMQP / Kafka)

### Módulo 5 — Seguridad
catalog-api necesitará **autenticación y autorización**:
- JWT Bearer tokens (Spring Security)
- OAuth2 / OpenID Connect (Spring Authorization Server)
- Autorización por roles y políticas (`@PreAuthorize`)

### Módulo 6 — Gestión de Datos
Más adelante, escalaremos **persistencia**:
- Database per service
- Saga pattern para transacciones distribuidas
- Outbox pattern para consistencia

### Módulo 7 — Observabilidad y Despliegue
Finalmente, operacionalizaremos:
- Micrometer + OpenTelemetry + Jaeger (trazas distribuidas)
- Docker + Docker Compose
- Kubernetes (pods, deployments, services)

---

## 📖 Recursos adicionales

**Documentación oficial:**
- [Spring Boot Reference](https://docs.spring.io/spring-boot/index.html)
- [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [PipelinR](https://github.com/sizovs/PipelinR)
- [FluentValidation (concepto original)](https://fluentvalidation.net/)
- [springdoc-openapi](https://springdoc.org/)

**Comunidad:**
- [Spring Community](https://spring.io/community)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot)

---

## 🎯 Conclusión

Has aprendido a construir **microservicios empresariales en Spring Boot** siguiendo:

1. **REST puro:** Recursos, verbos HTTP, códigos correctos
2. **Arquitectura limpia:** Vertical Slice, separación de responsabilidades
3. **Validación robusta:** FluentValidation con reglas complejas
4. **Manejo de errores:** ProblemDetail estándar (RFC 7807)
5. **Documentación automática:** springdoc-openapi / Swagger
6. **Monitoreo:** Health checks (Actuator) para Kubernetes
7. **Principios SOLID:** Especialmente ISP y SRP

**catalog-api está lista para:**
- Ser consumida por otros servicios (Módulo 4)
- Ser asegurada (Módulo 5)
- Escalar (Módulo 6)
- Ser desplegada en producción (Módulo 7)

---

**Próximo módulo:** [4 — Comunicación entre Microservicios]
