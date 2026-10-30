# SmartCity360 Jakarta EE Multi-Module Design

## Intent

Convert the existing empty Java 17 Maven project into a deployable Jakarta EE 10 enterprise application foundation for Payara Server 6. The foundation must prove that a Jakarta REST endpoint in the Web module can invoke a stateless EJB through a shared Core contract while leaving business features, persistence, authentication, and the full frontend out of scope.

## Goals

- Preserve `com.wmpj.smartcity360`, `1.0-SNAPSHOT`, Java 17, and the existing Git repository.
- Create exactly four child module directories: `core`, `ear`, `ejb`, and `web`.
- Package Core as a JAR, EJB as an EJB JAR, Web as a WAR, and the assembled application as an EAR.
- Expose `/smartcity360/api/health` and prove that it calls the EJB implementation through `core.service.HealthService`.
- Place the Core JAR in `EAR/lib` and avoid bundling the EJB implementation or Jakarta EE APIs in the WAR.
- Build with IntelliJ IDEA's bundled Maven, add a valid Maven Wrapper, and deploy to the existing Payara 6.2025.11 `domain1` when safe.

## Non-Goals

- PostgreSQL or Jakarta Persistence configuration.
- Authentication, authorization, or application security behavior.
- The five SmartCity360 design-pattern business features.
- A production frontend or final assessment documentation.
- Docker, Kubernetes, Spring Boot, or additional child modules.

## Constraints

- Java release 17 and Jakarta EE 10 (`jakarta.platform:jakarta.jakartaee-api:10.0.0`).
- Maven reactor module declarations remain in the requested order: Core, EAR, EJB, Web. Maven dependency edges, not declaration order, determine the effective build order.
- Annotation-based Jakarta REST and EJB configuration; no unnecessary `web.xml`, `beans.xml`, or `ejb-jar.xml`.
- Payara's server-provided Jakarta EE APIs use `provided` scope.
- Existing Payara `domain1` may be started only after confirming it is not already running.

## Approaches Considered

1. **Shared Core interface with local EJB injection (selected).** `HealthService` is a plain Java interface in Core. `HealthServiceBean` implements it and declares `@Local(HealthService.class)`. The REST resource injects the interface with `@EJB`. This keeps Web independent of the EJB implementation and keeps Core framework-independent.
2. **EJB no-interface view.** Web would compile against the bean class, coupling the presentation layer to the implementation and requiring an EJB dependency in the WAR build.
3. **Portable JNDI lookup.** This avoids field injection but adds deployment-name-sensitive lookup strings and failure modes that provide no value for an in-application local call.

## Module Design

### Parent

The root POM is a `pom` aggregator and parent. It centralizes Java, encoding, Jakarta EE, JUnit, dependency management, and compiler/resources/JAR/WAR/EJB/EAR/Surefire plugin versions.

### Core

`core` is a framework-independent JAR. It contains the requested package roots and the shared `com.wmpj.smartcity360.core.service.HealthService` contract with `String getStatus()`.

### EJB

`ejb` is an EJB 4.x-compatible JAR. `HealthServiceBean` is `@Stateless`, exposes the Core contract as a local business interface, and returns `UP`. It depends on Core and uses Jakarta EE only at provided scope.

### Web

`web` is a WAR. `RestApplication` maps Jakarta REST to `/api`; `HealthResource` maps `GET /health`, injects `HealthService` using `@EJB`, and returns a JSON object containing application name `SmartCity360` and the EJB-provided status. Core is a provided dependency so it is resolved from `EAR/lib`. The static root page contains the requested running message.

### EAR

`ear` depends on Core (`jar`), EJB (`ejb`), and Web (`war`). Maven EAR Plugin places Core in `lib/`, adds the EJB and WAR as modules, and uses the requested `src/main/application/META-INF` descriptor directory. `application.xml` defines the Jakarta EE 10 application and `/smartcity360` context root. A Payara runtime descriptor is included only in a form accepted by the installed server and must not contradict the portable descriptor.

## Validation

- Unit tests verify the EJB contract implementation and REST resource response with a stubbed Core service.
- Maven build verifies compilation and packaging across all four modules.
- Archive inspection verifies module placement, descriptors, and absence of bundled Jakarta EE API JARs or the EJB implementation in the WAR.
- Payara deployment and HTTP requests verify the root page and `/api/health` end to end when the local domain is available.

