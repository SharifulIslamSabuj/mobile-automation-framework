# Mobile Automation Framework

## Purpose

An enterprise-grade Android test automation framework for the Sauce Labs Android Demo App (v2.2.0), built to demonstrate documentation-first QA automation engineering: governing specifications (vision, requirements, strategy, architecture, test design, test cases, test data) are authored and baselined before implementation begins. See [`docs/`](docs/) for the full documentation set, starting with [MA-PV-001 — Project Vision & Scope](docs/01-vision-and-scope/MA-PV-001_Project-Vision-And-Scope_v1.0.md).

## Project Status

**Enterprise Framework Foundation complete** (Phases 1–8, resolved for readiness in Phase 8.10) — documentation baseline, manual verification, evidence collection, AUT source-code analysis, and the centralized Locator Repository are complete; Configuration Layer, Driver Management, Core Utilities, Cross-Cutting Infrastructure, Base Framework, and Test Data Framework are all implemented and validated. See [MA-EFR-001 — Enterprise Framework Final Review](docs/framework/ENTERPRISE_FRAMEWORK_FINAL_REVIEW.md) and [FOUNDATION_READINESS_RESOLUTION](docs/framework/FOUNDATION_READINESS_RESOLUTION.md) for the readiness audit and blocker resolution.

Per this project's Pilot-First Enterprise Development Strategy, **no Page Object, Test Class, or automated test case exists yet** — those are scoped for Phase 9 (Pilot Automation: TC-004 Login, TC-012 Add Product to Cart), which has not started.

## Current Features

- **Configuration Layer** — 4-tier precedence (system property → environment file → common file → compiled default), environment-aware (`emulator`/`real-device`).
- **Driver Management** — centralized Appium/UiAutomator2 driver lifecycle behind a package-private-manager + public-facade pattern, `ThreadLocal`-based for parallel execution.
- **Core Utilities** — 12 reusable, driver/config-only utilities (waits, gestures, scrolling, keyboard, toast/permission dialogs, screenshots, device/app control, file/random/date helpers).
- **Cross-Cutting Infrastructure** — SLF4J/Log4j2 logging, ExtentReports reporting, screenshot capture, retry analyzer, and TestNG suite/test/method listeners.
- **Base Framework** — `BasePage`/`BaseTest`, a reusable Element Actions wrapper, navigation helper, generic UI/dialog components, and reusable assertion wrappers.
- **Test Data Framework** — JSON/YAML/Properties readers behind one common interface, strongly-typed immutable POJO models, environment-aware data resolution, Faker-backed synthetic data, and reusable negative/random data libraries.

## Framework Structure

```
src/main/java/com/mobileautomation/framework/
├── config        — Configuration Layer (ConfigReader, Environment, CapabilityConfiguration)
├── constants     — Configuration keys/defaults
├── driver        — Driver Management (DriverManager/DriverProvider, AndroidDriverFactory)
├── locators      — reserved for the Page Object locator layer (Phase 9)
├── utils         — 12 reusable driver/config-only utilities
├── logging       — SLF4J entry point + Log4j2 configuration bridge (LogManager)
├── reporting     — ExtentReports lifecycle + screenshot manager
├── listeners     — TestNG suite/test/method listeners, retry analyzer
├── exceptions    — the framework's exception hierarchy (FrameworkException and subtypes)
├── core          — BasePage, BaseTest, ElementActions, NavigationHelper
├── components    — reusable generic UI/dialog components
├── data          — Test Data Framework (manager/loader/reader/validator/environment/provider/generator/factory)
└── models        — immutable POJO test-data models

src/test/java/com/mobileautomation/framework/
├── pages         — reserved for Page Objects (Phase 9)
├── tests         — reserved for Test Classes (Phase 9)
├── runners       — reserved for TestNG suite runners (Phase 9)
└── assertions    — CommonAssertions (reusable, reporting-integrated assertion wrappers)
```

Full rationale for every package and class is in the corresponding architecture doc under [`docs/framework/`](docs/framework/) — see Architecture Overview below.

## Supported Technologies

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build Tool | Gradle (wrapper-pinned, 9.0.0) |
| Test Framework | TestNG 7.10.2 |
| Automation Engine | Appium Java Client 9.4.0 |
| Driver | UiAutomator2 |
| Browser/WebDriver layer | Selenium Java 4.25.0 |
| Logging | SLF4J 2.0.16 + Log4j2 2.24.1 |
| Reporting | ExtentReports 5.1.1 |
| Test Data Serialization | Jackson Databind, `jackson-dataformat-yaml`, `jackson-dataformat-properties` (all 2.18.0) |
| Synthetic Test Data | DataFaker 2.4.3 |
| Boilerplate Reduction | Lombok 1.18.34 (compile-time only) |
| General Utilities | Apache Commons Lang3 3.17.0 |

Full version rationale for every dependency: [MA-DEP-001 — Dependency Version Freeze](docs/architecture/DEPENDENCY_VERSION_FREEZE.md).

## Architecture Overview

The framework follows a layered architecture with a single allowed dependency direction: Test Layer → Page Object Layer → Driver Management / Locator Management / Utility layers → Configuration Layer, with Reporting, Logging, and Exception Handling as cross-cutting concerns. Full detail: [MA-FA-001 — Framework Architecture](docs/05-framework-architecture/MA-FA-001_Framework-Architecture_v1.1.md).

Concrete, code-level rules that operationalize this architecture are frozen in [MA-FR-001 — Framework Architecture Rules](docs/framework/FRAMEWORK_ARCHITECTURE_RULES.md) and [MA-CS-001 — Coding Standards](docs/standards/CODING_STANDARDS.md). Every UI locator used by the framework is sourced exclusively from [MA-LOC-001 — Locator Repository](docs/automation/LOCATOR_REPOSITORY.md), built from direct analysis of the AUT's public source code.

Per-layer architecture documents:

- [MA-CFG-001 — Configuration Architecture](docs/framework/CONFIGURATION_ARCHITECTURE.md)
- [MA-DRV-001 — Driver Architecture](docs/framework/DRIVER_ARCHITECTURE.md)
- [MA-UTIL-001 — Core Utilities Architecture](docs/framework/CORE_UTILITIES_ARCHITECTURE.md)
- [MA-XCI-001 — Cross-Cutting Infrastructure](docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md)
- [MA-BF-001 — Base Framework Architecture](docs/framework/BASE_FRAMEWORK_ARCHITECTURE.md)
- [MA-TDF-001 — Test Data Framework](docs/framework/TEST_DATA_FRAMEWORK.md)
- [MA-EFR-001 — Enterprise Framework Final Review](docs/framework/ENTERPRISE_FRAMEWORK_FINAL_REVIEW.md)

## Build Command

```bash
./gradlew clean build
```
