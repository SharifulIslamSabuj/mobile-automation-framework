# Mobile Automation Framework

**Enterprise Android UI test automation framework — built documentation-first, verified on a real device, and frozen at Release v1.0.0.**

Target application under test: [Sauce Labs My Demo App (Android)](https://github.com/saucelabs/my-demo-app-android) — `com.saucelabs.mydemoapp.android`.

[![Release](https://img.shields.io/badge/release-v1.0.0-blue)](#)
[![Java](https://img.shields.io/badge/Java-17-orange)](#)
[![Appium](https://img.shields.io/badge/Appium-Java%20Client%209.4.0-purple)](#)
[![Gradle](https://img.shields.io/badge/Gradle-9.0.0-02303A)](#)
[![License](https://img.shields.io/badge/license-unspecified-lightgrey)](#license)

---

## At a Glance

| | |
|---|---|
| **Framework Version** | 1.0.0 |
| **Language** | Java 17 |
| **Platform** | Android (real device, UiAutomator2) |
| **Architecture** | Layered — Page Object Model |
| **Automation Scope** | 19 of 32 test cases automated |
| **Documentation Status** | Frozen baseline, 5 enterprise documents |
| **Current Release** | v1.0.0 |

---

## What This Project Is

This is a Java/Appium/TestNG UI automation framework built against the Sauce Labs My Demo App for Android, developed using a documentation-first methodology: requirements, test design, test cases, test data design, and a locator repository were authored and traceable *before* and *alongside* implementation, rather than reverse-documented after the fact.

The framework is not a demo script — it is a layered, configuration-driven automation codebase with its own driver management, reusable Page Object layer, centralized locator repository, external test data, structured logging, and HTML reporting, backed by five enterprise-grade governing documents that were independently audited and frozen at this release.

This repository represents **Release v1.0.0** exactly as it exists today — no aspirational features, no roadmap items presented as current capability.

## What Makes This Framework Different

- **Documentation-first development** — governing specifications (requirements, test design, test cases, test data design, locators) exist as versioned documents, not after-the-fact writeups.
- **Enterprise traceability** — every automated test case traces back to a functional requirement (FR) and a test scenario (TS) in the requirements and test design documents.
- **Evidence-based implementation** — locators and expected behavior were derived from direct analysis of the AUT's own source and runtime evidence (screenshots, logs), not guesswork.
- **Layered architecture** — a strict, single-direction dependency chain (Test → Page Object → Driver/Locator/Utility → Configuration), with reporting, logging, and exception handling as cross-cutting concerns.
- **Configuration-driven execution** — environment, device, and execution-strategy values resolve through a tiered configuration layer (system property → environment file → common file → compiled default); no hardcoded environment values in test code.
- **Reusable Page Object Model** — one Page Object per AUT screen, with locators isolated into a dedicated locator layer.
- **Enterprise documentation baseline** — five governing documents (Requirements, Test Design, Test Cases, Test Data Design, Locator Repository) kept version-controlled and cross-referenced.
- **Frozen documentation, independently verified** — the full documentation set was subjected to a whole-project consistency audit and reconciliation cycle, then independently re-verified and approved without exception before this release.
- **Release verification** — this release was preceded by an explicit, evidence-based release-readiness assessment rather than being cut ad hoc.

## Repository Snapshot

| Metric | Value |
|---|---|
| Framework Version | 1.0.0 |
| Test Classes | 4 |
| Page Objects | 9 |
| Locator Classes | 10 |
| Automated Test Cases | 19 (18 dedicated + 1 incidental) |
| Manual / Not-Yet-Automated Test Cases | 12 |
| Deferred Test Cases | 1 |
| Total Documented Test Cases | 32 |
| Enterprise Documents | 5 |
| Framework Architecture Layers | 10 |

## Framework Architecture

The framework enforces a single, strict dependency direction: the Test Layer depends on the Page Object Layer, which depends on Driver Management, Locator Management, and the Utility layer, all of which ultimately depend on the Configuration Layer. Reporting, Logging, and Exception Handling are cross-cutting concerns consumed by every layer above them. Full detail: [MA-FA-001 — Framework Architecture](docs/05-framework-architecture/MA-FA-001_Framework-Architecture_v1.1.md).

```
src/main/java/com/mobileautomation/framework/
├── config          — Configuration Layer (ConfigReader, Environment, CapabilityConfiguration)
├── constants        — Configuration keys and defaults
├── driver           — Driver Management (DriverManager, DriverFactory, AndroidDriverFactory, CapabilityBuilder)
├── locators         — Locator Management Layer (10 locator classes, one per screen/concern)
├── components       — Reusable generic UI/dialog components (alerts, toolbars, dialogs, popups)
├── core             — BasePage, BaseTest, ElementActions, NavigationHelper
├── utils            — Reusable driver/config-only utilities (waits, gestures, scrolling, keyboard, device, files, etc.)
├── data             — Test Data Framework (loaders, readers, resolvers, factories)
├── models           — Immutable POJO test data models
├── reporting        — ExtentReports lifecycle and screenshot manager
├── logging          — SLF4J/Log4j2 entry point
├── listeners        — TestNG suite/test/method listeners, retry analyzer
└── exceptions        — Framework exception hierarchy

src/test/java/com/mobileautomation/framework/
├── pages            — Page Object Layer (9 page objects, one per AUT screen)
├── tests            — Test classes (CartTest, LoginTest, NavigationTest, ProductDetailsTest)
├── assertions       — CommonAssertions (reusable, reporting-integrated assertion wrappers)
└── runners          — Reserved for TestNG suite runners
```

Per-layer architecture rationale is documented under [`docs/framework/`](docs/framework/) and [`docs/architecture/`](docs/architecture/), including [Configuration Architecture](docs/framework/CONFIGURATION_ARCHITECTURE.md), [Driver Architecture](docs/framework/DRIVER_ARCHITECTURE.md), [Core Utilities Architecture](docs/framework/CORE_UTILITIES_ARCHITECTURE.md), [Cross-Cutting Infrastructure](docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md), [Base Framework Architecture](docs/framework/BASE_FRAMEWORK_ARCHITECTURE.md), and [Test Data Framework](docs/framework/TEST_DATA_FRAMEWORK.md).

## Engineering Capabilities

- **Configuration Layer** — four-tier precedence (system property → environment file → common file → compiled default), with dedicated profiles for `emulator` and `real-device` execution.
- **Driver Management** — centralized Appium/UiAutomator2 session lifecycle behind a manager/provider pattern, with `ThreadLocal`-based driver instances.
- **Page Object Model** — one Page Object per AUT screen, with locators isolated into a dedicated locator layer sourced from a centralized [Locator Repository](docs/automation/LOCATOR_REPOSITORY.md) built from direct AUT source analysis.
- **External test data** — JSON, YAML, and Properties test data read behind one common interface, resolved through an environment-aware resolver, backed by strongly-typed immutable model classes.
- **Explicit synchronization** — explicit-wait-only strategy (no implicit waits) via a dedicated wait utility.
- **Test isolation** — an `ISOLATED` execution strategy that resets AUT state between test methods, plus a `FAST` mode for local debugging only.
- **Structured logging** — SLF4J routed through Log4j2, with per-run log files under `logs/`.
- **HTML reporting** — ExtentReports-based reporting with automatic screenshot capture on failure, written under `reports/`.
- **Retry and listener infrastructure** — a TestNG `RetryAnalyzer` plus suite/test/method listeners for consistent setup, teardown, and reporting hooks.
- **Reusable assertions** — a `CommonAssertions` wrapper that ties assertion outcomes into the reporting layer.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build Tool | Gradle 9.0.0 (wrapper-pinned) |
| Automation Engine | Appium Java Client 9.4.0 |
| Driver | UiAutomator2 |
| WebDriver Protocol Layer | Selenium Java 4.25.0 |
| Test Framework | TestNG 7.10.2 |
| Reporting | ExtentReports 5.1.1 |
| Logging | SLF4J 2.0.16 + Log4j2 2.24.1 |
| Configuration | Java Properties, tiered resolution |
| Test Data | JSON / YAML / Properties (Jackson 2.18.0), DataFaker 2.4.3 |
| Architecture | Layered, Page Object Model |
| Boilerplate Reduction | Lombok 1.18.34 (compile-time only) |

Full dependency version rationale: [MA-DEP-001 — Dependency Version Freeze](docs/architecture/DEPENDENCY_VERSION_FREEZE.md).

## Automation Coverage

Of the 32 documented test cases (see [MA-TC-001 — Test Case Specification](docs/08-test-design/MA-TC-001_Test-Case-Specification_v1.0.md)):

| Category | Count | Notes |
|---|---|---|
| Automated — dedicated test method | 18 | Directly implemented as TestNG `@Test` methods across `LoginTest`, `NavigationTest`, `ProductDetailsTest`, `CartTest` |
| Automated — incidental coverage | 1 | Verified as covered by assertions inside another test's flow, without a dedicated test method (documented and reviewed as its own category, not counted as dedicated automation) |
| Manual / Not Yet Automated | 12 | In scope for future automation |
| Deferred | 1 | Explicitly deferred, not scheduled for this release |

Every automated test case traces to a Functional Requirement (MA-RS-001), a Test Scenario (MA-TD-001), and a Test Case (MA-TC-001), with an Automation Mapping entry recording the exact test method and page object involved. Coverage claims for incidental automation were derived by comparing documented acceptance criteria against actual assertions in code — navigation alone, a successful click, or a method executing without exception was never counted as coverage on its own.

## Documentation

The framework is governed by five enterprise documents, each independently version-controlled and cross-referenced:

| Document | ID | Purpose |
|---|---|---|
| [Requirements Specification](docs/03-requirements/MA-RS-001_Requirements-Specification_v1.0.md) | MA-RS-001 | Functional and non-functional requirements |
| [Test Design Specification](docs/08-test-design/MA-TD-001_Test-Design-Specification_v1.0.md) | MA-TD-001 | Test scenarios derived from requirements |
| [Test Case Specification](docs/08-test-design/MA-TC-001_Test-Case-Specification_v1.0.md) | MA-TC-001 | Detailed test cases, automation status, and traceability |
| [Test Data Design Specification](docs/08-test-design/MA-TDD-001_Test-Data-Design-Specification_v1.0.md) | MA-TDD-001 | Test data sources, formats, and automation file mapping |
| [Locator Repository](docs/automation/LOCATOR_REPOSITORY.md) | MA-LOC-001 | Centralized, AUT-source-verified element locators |

The full documentation set (governance, vision and scope, AUT analysis, test strategy, framework architecture, environment and tooling, test planning, execution and reporting, risk management, glossary) lives under [`docs/`](docs/).

As of this release, the five documents above were subjected to a whole-project consistency audit, a reconciliation pass, and an independent final re-verification — the baseline was approved without exception. This does not mean the framework or its test coverage is complete; it means the documentation describing what exists is internally consistent and traceable as written.

## Project Structure

```
mobile-automation-framework/
├── docs/                     — Enterprise documentation set (governance through test design)
├── src/
│   ├── main/java/.../framework/   — Framework infrastructure (config, driver, locators, core, utils, data, models, reporting, logging, listeners, exceptions, components)
│   └── test/
│       ├── java/.../framework/    — Page Objects, test classes, assertions
│       └── resources/
│           ├── config/             — Environment configuration profiles
│           ├── testdata/           — External JSON/YAML/Properties test data
│           └── log4j2.xml          — Logging configuration
├── logs/                     — Per-run execution logs
├── reports/                  — ExtentReports HTML reports and failure screenshots
├── build.gradle
├── settings.gradle
└── gradlew / gradlew.bat
```

## Getting Started

### Prerequisites

- Java 17 (JDK)
- Android SDK with `adb` on `PATH`
- A running Appium server (default expected at `http://127.0.0.1:4723`)
- An Android device or emulator with the AUT (`com.saucelabs.mydemoapp.android`) installed
- The AUT's `.apk` if a fresh install is required (path supplied via `-Dapp.path=<absolute-path>`; not committed to source control)

### Clone

```bash
git clone https://github.com/SharifulIslamSabuj/mobile-automation-framework.git
cd mobile-automation-framework
```

### Build

```bash
./gradlew clean build
```

### Run the full test suite

```bash
./gradlew test
```

### Run a single test class

```bash
./gradlew test --tests "com.mobileautomation.framework.tests.LoginTest"
```

### Run against a real device

```bash
./gradlew test -Denv=real-device -Ddevice.name="<device-name>" -Dplatform.version="<android-version>" -Ddevice.udid="<adb-serial>"
```

Reports are written to `reports/`, with screenshots on failure under `reports/screenshots/`. Execution logs are written to `logs/`.

## Current Limitations

This release is intentionally scoped. The following are honestly out of scope for v1.0.0, not partially implemented:

- No CI pipeline is configured yet — all execution to date has been local.
- No containerized (Docker) execution environment.
- Android only — no iOS support.
- No Appium/Selenium Grid — single-device execution only.
- No cloud device execution (BrowserStack, Sauce Labs cloud) — validated against a physical Android device only.
- No parallel execution — the driver layer is `ThreadLocal`-based and parallel-ready, but no parallel run has been configured or exercised.
- 12 of 32 documented test cases remain manual; 1 is explicitly deferred.

## Future Roadmap

The following are planned for future releases and are **not** part of v1.0.0:

| Version | Planned Scope |
|---|---|
| v1.1 | GitHub Actions CI, Docker-based execution environment, parallel test execution |
| v1.2 | Jenkins pipeline integration, Appium/Selenium Grid |
| v1.3 | BrowserStack and Sauce Labs cloud device execution |
| v2.0 | iOS support, cross-platform framework, Azure DevOps pipeline integration |

## Author

**Shariful Islam Sabuj**
GitHub: [@SharifulIslamSabuj](https://github.com/SharifulIslamSabuj)
Email: ss.cse.ru@gmail.com

## License

No license has been declared for this repository yet. All rights are reserved by the author unless and until a license file is added.

---

*Mobile Automation Framework — Release v1.0.0. This README describes v1.0.0 scope only; see [Future Roadmap](#future-roadmap) for planned work.*
