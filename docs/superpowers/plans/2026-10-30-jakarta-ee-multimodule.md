# SmartCity360 Jakarta EE Multi-Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and deploy a four-child-module Jakarta EE 10 EAR foundation whose REST health endpoint invokes a stateless EJB through a shared Core interface.

**Architecture:** The Core JAR owns a framework-independent `HealthService` contract. The EJB module supplies a local stateless implementation, the Web WAR injects that contract and exposes it through Jakarta REST, and the EAR assembles Core under `lib` plus the EJB and Web modules.

**Tech Stack:** Java 17, Maven 3.9.16, Jakarta EE 10, Jakarta Enterprise Beans 4.x, Jakarta REST 3.1, JUnit Jupiter, Payara Server 6.2025.11

**Spec:** `docs/superpowers/specs/2026-10-30-jakarta-ee-multimodule-design.md`

## Global Constraints

- Keep Group ID `com.wmpj.smartcity360` and version `1.0-SNAPSHOT`.
- Keep Java release `17`; do not introduce Java 21 or Spring Boot.
- Declare exactly `core`, `ear`, `ejb`, and `web` as child modules in the requested order.
- Use Jakarta EE 10 APIs with `provided` scope and `jakarta.*` packages only.
- Do not add PostgreSQL, authentication, business-pattern modules, a full frontend, Docker, Kubernetes, or extra Maven modules.
- Keep Core independent from EJB, Web, and EAR; do not package the EJB implementation in the WAR.

## Review Focus

- Reactor order with EAR declared before EJB/Web: `mvn validate` must resolve dependency-driven ordering and build EAR last.
- Shared class visibility: archive tests must show Core once under `EAR/lib` and absent from `WEB-INF/lib` and the EJB archive.
- Server APIs: archive tests must show no `jakarta.jakartaee-api` JAR anywhere in the EAR.
- Local EJB resolution: deployed `/api/health` must return HTTP 200 and status `UP`, proving injection rather than only unit-level behavior.
- Context mapping: both `/smartcity360/` and `/smartcity360/api/health` must resolve after deployment.

---

### Task 1: Parent and Module Maven Structure

**Files:**
- Modify: `pom.xml`
- Create: `core/pom.xml`
- Create: `ejb/pom.xml`
- Create: `web/pom.xml`
- Create: `ear/pom.xml`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: Existing Maven coordinates and Java 17 configuration.
- Produces: A four-module reactor with centralized dependency and plugin versions.

- [ ] Run IntelliJ bundled Maven `validate` before restructuring and record the missing-module failure expectation.
- [ ] Replace the root POM with the parent aggregator and add the four module POMs with exact packaging and dependency edges.
- [ ] Create only the requested source roots and package directories.
- [ ] Run `validate` and confirm reactor order is Parent, Core, EJB, Web, EAR despite declaration order.

### Task 2: Shared Contract and EJB Health Service

**Files:**
- Create: `core/src/main/java/com/wmpj/smartcity360/core/service/HealthService.java`
- Create: requested empty Core package roots using package markers where necessary
- Create: `ejb/src/main/java/com/wmpj/smartcity360/ejb/service/HealthServiceBean.java`
- Create: `ejb/src/test/java/com/wmpj/smartcity360/ejb/service/HealthServiceBeanTest.java`
- Create: requested empty EJB package roots using package markers where necessary

**Interfaces:**
- Consumes: Jakarta EE API at provided scope and JUnit at test scope.
- Produces: `HealthService#getStatus(): String` and a local `HealthServiceBean` returning `UP`.

- [ ] Write `HealthServiceBeanTest#getStatusReturnsUp` and run it to observe compilation failure because the contract/bean do not exist.
- [ ] Add the minimal Core interface and stateless bean using `@Local(HealthService.class)`.
- [ ] Run the EJB unit test and confirm it passes.

### Task 3: Jakarta REST Web Module

**Files:**
- Create: `web/src/main/java/com/wmpj/smartcity360/web/config/RestApplication.java`
- Create: `web/src/main/java/com/wmpj/smartcity360/web/resource/HealthResource.java`
- Create: `web/src/test/java/com/wmpj/smartcity360/web/resource/HealthResourceTest.java`
- Create: requested empty Web package roots using package markers where necessary
- Create: `web/src/main/webapp/index.html`

**Interfaces:**
- Consumes: `HealthService#getStatus(): String`.
- Produces: Jakarta REST application path `/api`, resource path `/health`, and JSON keys `application` and `status`.

- [ ] Write tests for REST path annotations and the response map produced from a stub `HealthService`; run them to observe failure before resource classes exist.
- [ ] Add the REST application, injectable resource, and minimal static page.
- [ ] Run Web tests and confirm the requested mappings and response values pass.

### Task 4: EAR Assembly and Deployment Descriptors

**Files:**
- Create: `ear/src/main/application/META-INF/application.xml`
- Create: `ear/src/main/application/META-INF/payara-application.xml` only if validated as a supported packaged descriptor by Payara 6; otherwise create the supported Payara equivalent and document the decision.

**Interfaces:**
- Consumes: Core JAR, EJB JAR, and Web WAR artifacts.
- Produces: `smartcity360-ear-1.0-SNAPSHOT.ear` with `/smartcity360` context root.

- [ ] Run EAR packaging before descriptors and archive configuration are complete to establish the expected failing validation/assembly state.
- [ ] Add Jakarta EE 10 `application.xml`, validated Payara runtime metadata, and explicit Maven EAR modules.
- [ ] Package the reactor and inspect the EAR/WAR/JAR entries for correct placement and absence of duplicate/server libraries.

### Task 5: Maven Wrapper and Full Build

**Files:**
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Create: `.mvn/wrapper/maven-wrapper.jar` when generated by the official wrapper tooling
- Create: `mvnw`
- Create: `mvnw.cmd`

**Interfaces:**
- Consumes: IntelliJ bundled Maven 3.9.16 and the completed reactor.
- Produces: A valid Maven Wrapper pinned to a compatible Maven release.

- [ ] Generate the wrapper with official Maven Wrapper tooling; do not handcraft placeholder binaries.
- [ ] Run `mvnw.cmd clean package` with Java 17.
- [ ] Confirm Core JAR, EJB JAR, Web WAR, and EAR artifacts exist and all tests pass.

### Task 6: Payara Deployment and HTTP Verification

**Files:**
- Inspect: `D:/payara-6.2025.11/payara6/glassfish/domains/domain1/logs/server.log`

**Interfaces:**
- Consumes: Packaged EAR and local Payara `domain1`.
- Produces: Verified HTTP responses from the root page and EJB-backed health endpoint.

- [ ] Reconfirm `domain1` is not running and check for deployed applications before starting it.
- [ ] Start `domain1` with Java 17, deploy the EAR under a stable application name, and inspect deployment output/logs.
- [ ] Request `/smartcity360/` and `/smartcity360/api/health`; confirm HTTP 200, the requested page message, and JSON status `UP`.
- [ ] Run a final clean wrapper build and archive inspection before reporting completion.

