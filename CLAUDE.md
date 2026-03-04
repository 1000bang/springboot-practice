# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

All commands run from the `server/` directory:

```bash
# Build the WAR file
./gradlew build

# Clean build
./gradlew clean build

# Run tests (JUnit Platform)
./gradlew test
```

This is a WAR-based project (no embedded server). It requires an external servlet container (e.g., Tomcat 10+) to deploy and run.

## Architecture

This is a learning/study project demonstrating the evolution from raw Servlet registration to Spring MVC's DispatcherServlet setup, all without Spring Boot.

**Tech stack:** Java 17, Gradle 7.5, Jakarta Servlet API 6.0, Spring WebMVC 6.0.4 (no Spring Boot)

### Package Structure (`server/src/main/java/hello/`)

- **`servlet/`** - Raw `HttpServlet` implementations (`HelloServlet`, `TestServlet`)
- **`container/`** - Servlet container initialization chain showing three progressive approaches:
  - `MyContainerInitV1` / `MyContainerInitV2` - Custom `ServletContainerInitializer` implementations registered via `META-INF/services/jakarta.servlet.ServletContainerInitializer`
  - `AppInit` interface + `AppInitV1Servlet` - Programmatic servlet registration (pure servlet)
  - `AppInitV2Spring` - Manual DispatcherServlet + Spring context setup (mapped to `/spring/*`)
  - `AppInitV3SrpingMvc` - Uses Spring's `WebApplicationInitializer` (mapped to `/`)
- **`spring/`** - Spring MVC layer (`HelloConfig` + `HelloController` at `/hello-spring`)

### Key Design Pattern

`MyContainerInitV2` uses `@HandlesTypes(AppInit.class)` to auto-discover all `AppInit` implementations and invoke them reflectively at startup. This mirrors how Spring's `SpringServletContainerInitializer` discovers `WebApplicationInitializer` classes.
