# Framework Architecture Rules

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-FR-001 |
| Title | Framework Architecture Rules |
| Version | v1.0 |
| Status | Draft |
| Phase | Project Bootstrap |
| Relationship to MA-FA-001 | This document operationalizes the layered architecture already frozen in [MA-FA-001 — Framework Architecture](../05-framework-architecture/MA-FA-001_Framework-Architecture_v1.1.md) into concrete, enforceable coding rules. It does not redefine the architecture; it constrains how code within it may be written. |

These rules are **mandatory** for every class written from the next implementation phase onward. They are documented now, ahead of any implementation, so that the first line of framework code is already compliant — no rule listed here is implemented yet, per the Project Bootstrap phase boundary.

## 1. Package Architecture Freeze

The package structure established in this phase (`config`, `core`, `driver`, `locators`, `utils`, `reporting`, `listeners`, `exceptions`, `constants`, `data`, `components`, `models` under `src/main`; `pages`, `tests`, `runners`, `assertions` under `src/test`) is **permanent**. It must not be reorganized later without an explicit, separate instruction and a corresponding update to this document.

**Amendment (Phase 8.10, Foundation Readiness Resolution):** `logging` (`src/main/java/.../logging`) is added as a 13th permanent top-level package under `src/main`, closing the open item flagged in Phases 6 and 7. It predates this freeze in directory-scaffold terms (Project Bootstrap) but was empty until Phase 6 filled it with `LogManager`. It is kept independent rather than merged into `core` or `reporting` for a concrete dependency-shape reason: `LogManager` must be safely callable from *any* layer, including ones architecturally "below" `core` (e.g. `driver`, `utils`, `config`) should a future phase add logging to them — folding it into `core` (built on top of `driver`/`utils`) or `reporting` (Cross-Cutting Infrastructure, itself a `logging` consumer) would either create a downward dependency from a foundational layer into a higher one, or make `logging` depend on its own consumer. Today's actual importers of `logging.LogManager` are `reporting`, `listeners`, `core`, and `assertions` only (verified by import-graph audit, MA-EFR-001 §2) — no foundational layer needs it yet — but the package is kept structurally independent to keep that future path open without a later redesign.

## 2. Page Object Rules

- **No business logic in Page Objects.** A Page Object exposes screen interactions (e.g., `enterUsername(String value)`, `tapLoginButton()`) — it does not decide *when* or *why* those interactions happen.
- **No assertions inside Page Objects.** Verification belongs to the Test Layer (or the dedicated `assertions` package), never to a Page Object method.
- **No hardcoded locators.** Every locator must be resolved through the `locators` package, which in turn is sourced exclusively from `docs/automation/LOCATOR_REPOSITORY.md` (MA-LOC-001) — the single source of truth. A locator value must never be typed directly into a Page Object or Test Class.
- Every Page Object must extend `BasePage` (not implemented in this phase — see Out of Scope).

## 3. Test Layer Rules

- **No hardcoded test data.** Test data is sourced from the `data`/`testdata` layer (files under `src/test/resources/testdata`), never inlined as string/number literals in a test method.
- Every test class must extend `BaseTest` (not implemented in this phase — see Out of Scope).
- Assertions live in the Test Layer or in the dedicated `assertions` helper package — never in Page Objects (see §2).

## 4. Driver Management Rules

- **Driver creation must be centralized** in the `driver` package. No test class or Page Object may instantiate an `AppiumDriver` directly.
- Session lifecycle (start/quit) is owned by the driver layer, invoked through `BaseTest` hooks once implemented — never scattered across individual test methods.

## 5. Waiting and Timing Rules

- **No `Thread.sleep()`, anywhere, for any reason.** This is an absolute rule, not a style preference.
- **Explicit waits only** — every wait must be a condition-based explicit wait (e.g., an `ExpectedConditions`-equivalent against a specific element/state). Implicit waits are not to be relied upon as the primary synchronization strategy.

## 6. Cross-Cutting Concerns

Per MA-FA-001, Reporting, Logging, and Exception Handling are cross-cutting concerns, not part of the linear Test → Page Object → Driver dependency chain:

- **Reporting** (`reporting` package, ExtentReports) is invoked via listener hooks (`listeners` package), not called ad hoc from inside test methods.
- **Logging** (SLF4J + Log4j2) is available to every layer; log statements must use SLF4J's parameterized logging (`log.info("... {}", value)`), never string concatenation.
- **Exceptions** (`exceptions` package) — the framework defines its own exception types for framework-level failures (e.g., a locator not found in the repository, a driver-creation failure) rather than letting raw framework/library exceptions propagate unexplained.

## 7. Dependency Direction (restated from MA-FA-001)

```
Test Layer → Page Object Layer → Driver Management / Locator Management / Utility layers → Configuration Layer
```

Reporting, Logging, and Exception Handling attach across all layers as cross-cutting concerns. No dependency may point backwards along this chain (e.g., a Page Object must never depend on a Test class).

## 8. Locator Repository Authority

`docs/automation/LOCATOR_REPOSITORY.md` (MA-LOC-001) is frozen as the single source of truth for locators, per its own §1. Do not rediscover locators ad hoc during implementation; if a locator is found to be missing or wrong, correct MA-LOC-001 first, then the `locators` package.

## Out of Scope for This Document's Enforcement (Not Yet Implemented)

The following classes are referenced above by name but are **explicitly not created** in the Project Bootstrap phase: `DriverManager`/`DriverFactory`/`DriverProvider`, `BasePage`, `BaseTest`, wait utilities, gesture/scroll utilities, Page Objects, Test Classes, Locator classes, Configuration Reader, Reporting implementation, Logging implementation, Listeners, Retry Analyzer, Screenshot Manager, Test Data Loader. These rules exist now purely as governing constraints for when that implementation begins.
