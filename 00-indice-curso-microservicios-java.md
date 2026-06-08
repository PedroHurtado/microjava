# Microservicios con Java y Spring Boot — Índice del Curso

> **Duración:** 25 horas (≈26h de contenido para dar margen)
> **Versión base:** Java 21 (LTS) + Spring Boot 3.x
> **Lenguaje:** Java
> **Modalidad de ejemplos:** snippets concretos por concepto + proyecto hilo conductor que evoluciona durante todo el curso

---

## Objetivos del curso

- Comprender los conceptos fundamentales de la arquitectura de microservicios y sus ventajas.
- Aprender a desarrollar microservicios utilizando Spring Boot.
- Aprender la contenerización de microservicios con Docker y su orquestación mediante Docker Compose y Kubernetes.
- Implementar comunicación resiliente entre servicios (HTTP, OpenFeign, gRPC y mensajería asíncrona).
- Asegurar microservicios con JWT y OAuth2 / OpenID Connect.
- Gestionar datos con el patrón *database per service* y resolver la consistencia distribuida.
- Instrumentar el sistema con observabilidad (logs, métricas y trazas distribuidas).

## Requisitos previos

- Conocimientos de Java.
- Fundamentos de desarrollo web.
- Conceptos básicos de bases de datos.
- Familiaridad con la línea de comandos.

---

## Nota sobre la versión de Java y Spring Boot

El curso usa **Java 21 (LTS)** con **Spring Boot 3.x** como base. Java 21 es el *baseline* mínimo de
Spring Boot 3 y la opción más extendida en empresa, con el ecosistema de microservicios más maduro y
documentado. Aunque Java 25 (LTS) ya está disponible, mantenemos Java 21 por estabilidad y compatibilidad;
el módulo final incluye la **ruta de migración**. El código del curso es prácticamente idéntico en ambas versiones.

---

## Proyecto hilo conductor: "ShopMicro"

Un e-commerce mínimo descompuesto en servicios que se construye módulo a módulo:

| Servicio | Responsabilidad | Tecnología destacada |
|---|---|---|
| **catalog-api** | Catálogo de productos | Spring Data JPA + PostgreSQL |
| **basket-api** | Carrito de la compra | Spring Data Redis |
| **ordering-api** | Gestión de pedidos | Spring Data JPA + mensajería |
| **identity** | Autenticación y emisión de tokens | Spring Security (JWT / OIDC) |
| **gateway** | Punto de entrada único | Spring Cloud Gateway |

---

## Módulo 1 — Introducción a los Microservicios *(~3h)*

- 1.1 ¿Qué es un microservicio? Definición y características
- 1.2 Monolito vs. microservicios: cuándo SÍ y cuándo NO
- 1.3 Ventajas, costes y falacias de los sistemas distribuidos
- 1.4 Conceptos clave: *bounded context*, acoplamiento, cohesión
- 1.5 Anatomía de ShopMicro (visión global del proyecto del curso)

## Módulo 2 — Fundamentos de Spring Boot para Microservicios *(~3h)*

- 2.1 El JDK 21 y las herramientas de build (Maven, Spring Initializr)
- 2.2 Anatomía de un proyecto Spring Boot REST
- 2.3 Inyección de dependencias y el contenedor de beans
- 2.4 Configuración (`application.yml`, perfiles, variables de entorno, `@ConfigurationProperties`)
- 2.5 Logging estructurado y la cadena de filtros

## Módulo 3 — Desarrollo de Microservicios con Spring Boot *(~5h)*

- 3.1 Diseño de una API REST: recursos, verbos, códigos de estado
- 3.2 Construcción de **catalog-api** desde cero (`@RestController`)
- 3.3 Capas, DTOs y mapeo (MapStruct)
- 3.4 Validación de entrada (Bean Validation / Hibernate Validator)
- 3.5 Manejo global de errores (`@ControllerAdvice` + `ProblemDetail`)
- 3.6 Documentación con OpenAPI / Swagger (springdoc-openapi)
- 3.7 *Health checks* (Spring Boot Actuator)

## Módulo 4 — Comunicación entre Microservicios *(~5h)*

- 4.1 Síncrona vs. asíncrona: el gran dilema
- 4.2 Comunicación HTTP con `RestClient` / `WebClient`: la forma base
- 4.3 OpenFeign: clientes HTTP declarativos por interface (*typed clients* sin *boilerplate*)
- 4.4 Resiliencia con Resilience4j: *retry*, *timeout*, *circuit breaker*
- 4.5 gRPC: contratos con Protobuf y comunicación de alto rendimiento (REST vs. gRPC, cuándo cada uno)
- 4.6 Mensajería asíncrona: conceptos (*broker*, cola, *topic*, eventos)
- 4.7 Implementación con RabbitMQ + Spring AMQP
- 4.8 Patrón: publicar un evento `OrderCreated` y consumirlo
- 4.9 API Gateway con Spring Cloud Gateway

## Módulo 5 — Seguridad en Microservicios *(~3h)*

- 5.1 Autenticación vs. autorización en sistemas distribuidos
- 5.2 JWT: estructura y validación
- 5.3 OAuth2 / OpenID Connect (panorama)
- 5.4 Proteger catalog-api y ordering-api con JWT (Spring Security Resource Server)
- 5.5 Autorización por roles y políticas (`@PreAuthorize`)
- 5.6 Secretos y configuración sensible (variables de entorno, *secrets*)

## Módulo 6 — Gestión de Datos en Microservicios *(~4h)*

- 6.1 *Database per service*: el principio y sus consecuencias
- 6.2 Spring Data JPA con PostgreSQL en catalog-api (migraciones con Flyway)
- 6.3 Redis como almacén del basket-api (Spring Data Redis)
- 6.4 El problema de la consistencia: transacciones distribuidas
- 6.5 Patrón Saga (coreografía) — visión práctica
- 6.6 Patrón Outbox para no perder eventos

## Módulo 7 — Implementación, Despliegue y Observabilidad *(~4h)*

- 7.1 Contenerización: Dockerfile *multi-stage* (o Buildpacks) para un servicio Spring Boot
- 7.2 Orquestación local con Docker Compose (toda la solución en marcha)
- 7.3 Los tres pilares de la observabilidad: logs, métricas y trazas
- 7.4 Trazas distribuidas con OpenTelemetry / Micrometer Tracing (instrumentación, propagación de contexto, correlación entre servicios)
- 7.5 Visualización con Jaeger (seguir una petición a través de ShopMicro)
- 7.6 Introducción a Kubernetes: *pods*, *deployments*, *services*
- 7.7 Desplegar ShopMicro en Kubernetes (manifiestos YAML)
- 7.8 Configuración, *secrets* y escalado en K8s
- 7.9 Cierre: *checklist* de producción y ruta de migración de versión

---

## Distribución orientativa de horas

| Módulo | Tema | Horas |
|---|---|---|
| 1 | Introducción a los Microservicios | 3 |
| 2 | Fundamentos de Spring Boot | 3 |
| 3 | Desarrollo con Spring Boot | 5 |
| 4 | Comunicación entre Microservicios | 5 |
| 5 | Seguridad | 3 |
| 6 | Gestión de Datos | 4 |
| 7 | Implementación, Despliegue y Observabilidad | 4 |
| | **Total** | **~26h** |
