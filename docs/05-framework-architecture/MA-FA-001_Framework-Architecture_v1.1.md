---
document_id: MA-FA-001
title: Framework Architecture
version: v1.1
status: Draft
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-07-29
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001]
classification: Internal
---

# MA-FA-001 — Framework Architecture

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-FA-001 |
| Document Name | Framework Architecture |
| Version | v1.1 |
| Status | Draft |
| Project | Mobile Automation Framework |
| Project Code | MA |
| AUT | Sauce Labs Android Demo App |
| AUT Version | 2.2.0 |
| Platform | Android |
| Classification | Internal |

---

## Version History

| Version | Date | Author | Change Description |
|---|---|---|---|
| v0.1 | 2026-07-29 | Project Owner | Initial draft derived from MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001 |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |
| v1.1 | 2026-07-29 | Project Owner | Recorded concrete tool selection for the Logging Layer (SLF4J + Log4j2, §16), Reporting Layer (ExtentReports, §15), and Test Data Layer serialization (Jackson Databind, §11), ahead of Phase 01 implementation. No other content changed. |

---

## 1. Purpose

This document translates the requirements baselined in [MA-RS-001] and the approach defined in [MA-TS-001] into a structural blueprint: what components the framework will consist of, what each is responsible for, and how they depend on one another. It defines WHAT the architecture looks like and WHY each decision was made — not how the code implementing it will be written. Implementation begins only after this document is approved.

## 2. Scope

In scope: layering, folder and package architecture, component responsibilities, and the strategy for driver management, configuration, test data, page objects, base test lifecycle, utilities, reporting, logging, screenshots, synchronization, locator management, exception handling, retries, and device targeting. Out of scope: actual code, class signatures, file contents, build scripts, CI pipeline implementation (deferred to a future MA-CI document), and test case content (deferred to MA-TC).

## 3. Architectural Principles

The eight principles frozen in MA-PV-001 Section 17 govern every decision in this document. Their architectural interpretation for this project:

| Principle | Architectural Interpretation |
|---|---|
| Maintainability | Each concern lives in exactly one layer; a change to one concern should not require touching unrelated layers |
| Scalability | Layers must absorb new screens, new flows, and — eventually — a new AUT without structural rework |
| Readability | Layer and component boundaries must be self-evident from their responsibility, not from inline explanation |
| Reusability | AUT-agnostic logic (utilities, driver management, reporting) is structurally separated from AUT-specific logic (page objects) |
| Reliability | Synchronization and retry concerns are centralized so execution stability (NFR-005) is not reimplemented per test |
| Simplicity | No layer is introduced unless it resolves a concern already identified in MA-RS-001 or MA-TS-001 |
| Clean Architecture | Dependencies flow in one direction — Test Layer → Page Objects → Core/Support layers — never the reverse |
| Industry Best Practices | Layering mirrors patterns used in production Appium/TestNG frameworks: Page Object Model, centralized driver lifecycle, cross-cutting reporting/logging |

Additional architectural qualities required for this project, each justified against a concrete future need rather than adopted by default:

| Quality | Justification |
|---|---|
| Configurability | NFR-008 requires environment values external to test logic |
| Cross-device execution | NFR-009 and MA-TS-001 Section 12 require both emulator and physical device targets without code change |
| Future iOS readiness (architecture only) | MA-PV-001 Section 27 lists cross-platform expansion as a roadmap item; the architecture must not structurally block it |
| CI/CD readiness | MA-PV-001 Section 23.2 commits to a CI pipeline; reporting output must be consumable by a future pipeline without redesign |
| Reporting | NFR-006 requires a report per run |
| Logging | NFR-007 requires step-level diagnosability |
| Parallel execution readiness | MA-PV-001 Section 27 lists parallel execution as a roadmap item; driver lifecycle must not assume single-threaded execution |
| Data-driven testing readiness | NFR-011 requires test data external to test logic, structurally ready for multiple data sets per requirement |

## 4. High-Level Framework Architecture

The framework is organized as a layered structure with a single allowed dependency direction: the Test Layer depends on the Page Object Layer, which depends on the Driver Management, Locator Management, and Utility/Synchronization layers, which in turn depend on the Configuration Layer. Reporting and Logging are cross-cutting — every layer may write to them, but no layer depends on test outcomes flowing back from them. No layer is permitted to depend on a layer above it; this one-directional rule is the architecture's primary safeguard against the tightly-coupled, hard-to-maintain structures identified as a risk in MA-PV-001 Section 6 (Problem Statement).

## 5. Framework Layers

| Layer | Responsibility | Depends On |
|---|---|---|
| Test Layer | TestNG test classes expressing requirement-level verification (per MA-RS-001 FR/NFR) | Page Object Layer, Base Test Strategy |
| Page Object Layer | One screen-level abstraction per AUT screen identified in MA-AA-001; exposes screen actions and state to tests | Driver Management, Locator Management, Utility/Synchronization |
| Driver Management (Core) Layer | Owns Appium/UiAutomator2 session lifecycle | Configuration Layer |
| Locator Management Layer | Holds and organizes element locators, separate from interaction logic | Configuration Layer (for locator strategy toggles, if any) |
| Utility/Support Layer | AUT-agnostic reusable helpers (e.g., generic wait, generic scroll) consumed across page objects | Driver Management |
| Configuration Layer | Supplies environment and execution values to all other layers | None (base layer) |
| Test Data Layer | Supplies external test input values, separated from test logic | Configuration Layer |
| Reporting Layer | Aggregates per-run, per-requirement pass/fail outcomes | Cross-cutting — consumed by Test Layer and Base Test Strategy |
| Logging Layer | Records step-level execution activity | Cross-cutting — consumed by all layers |
| Exception Handling | Defines failure categories consumed uniformly by Logging and Reporting | Cross-cutting |

## 6. Project Folder Architecture

| Folder (Conceptual) | Purpose | Justification |
|---|---|---|
| Framework core source | Houses Driver Management, Configuration, Locator Management, Utility, Reporting, Logging, Exception Handling layers | Separates reusable framework code from AUT-specific test code (Reusability) |
| Test source | Houses Page Object Layer and Test Layer | Keeps AUT-specific code isolated so it can be replaced if the AUT changes without touching framework core |
| Test resources | Houses configuration values and externalized test data | Satisfies NFR-008 (Configuration) and NFR-011 (Data Management) by keeping values out of source code |
| Build configuration (Gradle-managed) | Declares dependencies and build lifecycle | Already frozen by MA-PV-001 Section 16; contents are implementation, not architecture |
| Generated reports | Destination for Reporting Layer output at runtime | Kept separate from source so it can be excluded from version control |

This section defines folder *purpose*, not file names, contents, or Gradle configuration — those are implementation activities out of scope for this document.

## 7. Package Architecture

| Package Grouping (Conceptual) | Responsibility | Depends On |
|---|---|---|
| Core / Driver | Driver Management Layer | Configuration |
| Config | Configuration Layer | None |
| Locators | Locator Management Layer | Config |
| Pages | Page Object Layer | Core/Driver, Locators, Utils |
| Utils | Utility/Support Layer | Core/Driver |
| Data | Test Data Layer | Config |
| Reporting | Reporting Layer | None (cross-cutting) |
| Logging | Logging Layer | None (cross-cutting) |
| Exceptions | Exception Handling | None (cross-cutting) |
| Tests | Test Layer | Pages, Data, Base Test |

Package boundaries mirror the layer boundaries in Section 5 exactly, so that a future contributor can locate any concern by its layer name alone (Readability, Maintainability).

## 8. Component Responsibilities

| Component (Conceptual) | Single Responsibility |
|---|---|
| Driver Manager | Create, expose, and terminate the Appium/UiAutomator2 session |
| Config Reader | Resolve environment/execution values from the external configuration source |
| Base Test | Provide shared test lifecycle (setup/teardown) without containing test logic |
| Base Page | Provide shared page-object behavior (e.g., synchronization access) without containing screen-specific logic |
| Locator Repository | Hold locator definitions separately from page object interaction logic |
| Test Data Provider | Supply externalized input values (credentials, address, payment) to tests |
| Report Listener | Capture per-test outcome and assemble the run report |
| Log Handler | Provide a single logging entry point used by every layer |
| Retry Analyzer | Apply the configured retry policy to a failed test |
| Screenshot Capturer | Capture and attach evidence on failure |
| Exception Handler | Translate low-level failures into the framework's defined failure categories |

## 9. Driver Management Architecture

The Driver Management layer is the single point of control for the Appium/UiAutomator2 session: initialization, exposure to the Page Object layer, and teardown. No other layer is permitted to create or hold a driver session directly — this satisfies Single Responsibility and prevents session-handling logic from being duplicated across page objects. The session must be resolvable per execution context rather than as a single global instance, so the same architecture supports future parallel execution (MA-PV-001 Section 27) without redesign; this is an architectural allowance now, not an implementation commitment for the current phase. Session target (emulator vs. physical device) is supplied by the Configuration Layer, never hardcoded, satisfying NFR-009 (Cross-Device Support).

## 10. Configuration Architecture

The Configuration Layer is the framework's only source of environment-specific values: execution target, platform version, AUT install source, and timeout values referenced by the Synchronization Strategy (Section 18). It is the base layer — it depends on nothing else — so that every other layer can safely depend on it without creating a circular dependency. Supporting multiple named execution profiles (e.g., emulator profile, device profile) is an architectural requirement, not an implementation detail, because MA-TS-001 Section 11 commits to environment values being externally configurable. This same structural allowance is what would let a future cloud-execution profile (MA-PV-001 Section 27) be added without modifying any layer above it.

## 11. Test Data Architecture

Test data (login credentials, shipping address fields, payment fields — per MA-RS-001 FR-003, FR-020, FR-023) is structurally separated from the Test Layer and Page Object Layer through a dedicated Test Data Layer. This satisfies NFR-011 directly. The layer is designed to expose one or more data sets per requirement rather than exactly one, so that data-driven execution is a configuration change rather than a structural one — this fulfills the "data-driven testing readiness" quality named in Section 3, even though MA-TS-001 Section 13 scopes current execution to a single data set per flow.

**Tool selection (recorded v1.1):** Jackson Databind is adopted as the serialization library for externalized test data files (MA-TDD-001's TD-001–TD-003 datasets). This names the tool only; the Test Data Provider's use of it remains an implementation-phase concern.

## 12. Page Object Strategy

The framework adopts a Page Object strategy: one page-level abstraction per AUT screen confirmed in MA-AA-001 (Product Catalog, Product Details, Cart, Login, Shipping Address, Payment Method, Review Order, Order Completion, Navigation Drawer). Each abstraction is responsible only for the elements and actions belonging to its own screen — it does not perform assertions and does not orchestrate multi-screen flows, which remain the Test Layer's responsibility. This boundary is what keeps a UI change on one screen from requiring changes anywhere outside its corresponding abstraction (Maintainability, Scalability). Page objects depend on Driver Management for session access, Locator Management for element identification, and the Utility/Synchronization layer for waits — they do not implement any of those concerns themselves.

## 13. Base Test Strategy

A shared base test component owns test lifecycle plumbing — driver acquisition at start, driver teardown at end, and hooks into Logging and Reporting — so that individual test classes contain only requirement verification logic, not session management. This directly supports Clean Architecture (MA-PV-001 Section 17): lifecycle concerns and verification concerns are never mixed in the same place. The base test component itself contains no AUT-specific or requirement-specific logic, keeping it reusable if the framework is later pointed at a different AUT (MA-PV-001 Section 4, Vision).

## 14. Utility Layer Strategy

The Utility/Support layer holds AUT-agnostic reusable logic — generic interaction helpers (e.g., a generic scroll operation, a generic text-entry helper) — consumed by multiple page objects. It is architecturally distinct from the Page Object layer because utilities carry no knowledge of any specific AUT screen; this is what allows the utility layer, unlike the page object layer, to be reused as-is if the framework is ever pointed at a different AUT. Any interaction logic used by more than one page object belongs here rather than being duplicated (Reusability, Simplicity).

## 15. Reporting Architecture

Reporting is a cross-cutting concern implemented as a listener attached to the test execution lifecycle rather than logic embedded inside individual tests. It aggregates outcomes per test, organized by the functional categories defined in MA-RS-001 Section 6, satisfying NFR-006 and the reporting commitment in MA-TS-001 Section 17. Because it is decoupled from test logic, the report format or destination can change without touching the Test Layer — this is the architectural hook that keeps the framework CI/CD-ready (MA-PV-001 Section 23.2) ahead of the future MA-CI document.

**Tool selection (recorded v1.1):** ExtentReports is adopted as the Reporting Layer's report-generation library. This names the tool only; the Report Listener's use of it remains an implementation-phase concern.

## 16. Logging Architecture

A single, centralized logging component is used by every layer, rather than each layer implementing its own logging. This satisfies NFR-007 and prevents inconsistent or duplicated logging logic across the codebase (Maintainability, Single Responsibility). Logging scope is limited to framework and test execution activity — step, action, and outcome — consistent with the boundary already set in MA-TS-001 Section 16; AUT-internal diagnostics remain out of reach and out of scope.

**Tool selection (recorded v1.1):** SLF4J is adopted as the logging facade, with Log4j2 as the bound implementation. This names the tools only; the Log Handler's use of them remains an implementation-phase concern.

## 17. Screenshot Strategy

Screenshot capture is triggered from the cross-cutting Reporting/Logging layer on test failure, not from within individual page objects or tests — this keeps evidence capture consistent regardless of which screen or requirement failed, and keeps page objects free of reporting concerns (Single Responsibility). Whether capture also occurs at intermediate checkpoints (not only on failure) is left as a Configuration Layer toggle rather than a hardcoded behavior, so evidence volume can be tuned without code change.

## 18. Synchronization Strategy

Per MA-TS-001 Section 15, all waiting logic must be tied to observable state changes rather than fixed-duration delays. Architecturally, this is enforced by placing synchronization capability inside the Utility/Support layer (or as a Driver Management–adjacent concern) so that no individual page object implements its own bespoke wait. Centralizing this concern is what makes NFR-005 (Execution Stability) achievable framework-wide rather than test-by-test.

## 19. Locator Management Strategy

Locators are held in a dedicated Locator Management layer, structurally separate from page object interaction logic — a locator change does not require touching the code that acts on it, and vice versa. This directly extends the priority order already set in MA-TS-001 Section 14 (Resource ID/Accessibility ID first, semantic attributes second, XPath as fallback). Because MA-AA-001 recorded no confirmed resource IDs or accessibility IDs (Known Limitations, Section 14), this document defines only where locators will live and how they will be prioritized — actual locator values remain unresolved until the manual verification pass committed to in MA-TS-001 Section 8.

## 20. Exception Handling Strategy

The framework defines its own failure categories (e.g., a distinct category for "element not found" versus "session initialization failed") rather than allowing raw underlying exceptions to surface uniformly. Centralizing this translation feeds the Logging and Reporting layers with consistent, diagnosable failure information, directly supporting NFR-007. This is an architectural boundary — the specific exception types and their handling logic are implementation detail deferred to the build phase.

## 21. Retry Strategy

A configurable retry mechanism is included in the architecture to absorb environment-level flakiness (MA-PV-001 Section 20, R-2) rather than to mask genuine defects. Retry count is sourced from the Configuration Layer, not hardcoded, and every retried attempt is logged distinctly from a first-attempt failure so a test that only passes after a retry remains visible for review rather than silently succeeding — this preserves the integrity of the Defect Management Strategy defined in MA-TS-001 Section 18.

## 22. Device Management Strategy

Execution target (Android Emulator or physical Android device) is resolved through the Configuration Layer and consumed by Driver Management; no other layer is aware of which target is active. This satisfies MA-TS-001 Section 12 and NFR-009 without introducing device-specific branching into the Page Object or Test layers. The same extension point is what would allow a future cloud device target (MA-PV-001 Section 27) to be added as a new configuration profile rather than a structural change.

## 23. Execution Flow

The following describes the conceptual sequence of a single test execution, not code:

1. Configuration Layer resolves execution target and environment values.
2. Driver Management initializes the Appium/UiAutomator2 session against the resolved target.
3. Base Test lifecycle hooks execute, registering Logging and Reporting for the run.
4. Test Layer invokes the relevant Page Object action(s) in sequence.
5. Page Objects resolve elements via Locator Management and wait via the Synchronization capability in the Utility layer.
6. Each step is recorded by the Logging layer as it occurs.
7. On assertion or execution failure, Exception Handling categorizes the failure, the Screenshot Strategy captures evidence, and the Retry Strategy determines whether the test is re-attempted.
8. The Reporting layer records the final outcome for the test.
9. Driver Management tears down the session at the end of the test lifecycle.

## 24. Dependency Overview

| Layer | Depends On (Direct) |
|---|---|
| Test Layer | Page Object Layer, Base Test Strategy, Test Data Layer |
| Page Object Layer | Driver Management, Locator Management, Utility/Support |
| Base Test Strategy | Driver Management, Reporting, Logging |
| Driver Management | Configuration Layer |
| Locator Management | Configuration Layer |
| Utility/Support | Driver Management |
| Test Data Layer | Configuration Layer |
| Reporting Layer | None (cross-cutting) |
| Logging Layer | None (cross-cutting) |
| Exception Handling | None (cross-cutting; consumed by Logging and Reporting) |
| Configuration Layer | None (base layer) |

No layer listed above depends on a layer that is positioned above it in Section 5 — this table is the enforceable check for the one-directional dependency rule stated in Section 4.

## 25. Extension Strategy

| Future Direction (MA-PV-001 Section 27) | Architectural Hook That Enables It |
|---|---|
| Cloud Execution | New Configuration profile consumed by Driver Management; no other layer changes |
| Parallel Execution | Driver Management already designed for per-execution-context session resolution (Section 9) |
| Cross-Platform / iOS | Page Object Layer is already the only AUT/platform-specific layer; an iOS page object set could be added alongside the Android one without altering Test, Driver, Config, Reporting, or Logging layers |
| CI/CD Enhancement | Reporting Layer output is already decoupled from Test Layer execution, ready for pipeline consumption |
| Reporting Enhancement | Reporting Layer is isolated as a listener; format/destination changes do not touch other layers |
| Performance Improvement | Centralized Driver Management and Synchronization layers are the only points where execution-time tuning would be applied |

## 26. Risks

| Risk | Mitigation |
|---|---|
| Locator values remain unresolved until manual inspection (MA-TS-001 Section 8) | Locator Management layer is structurally ready; population is sequenced after inspection, not blocking architecture approval |
| Over-layering relative to a single-contributor project (MA-PV-001 Constraint C-6) | Layer count is bounded to concerns already identified in MA-RS-001/MA-TS-001; no speculative layer was added |
| Retry mechanism masking genuine defects if misconfigured | Retried attempts remain logged and visible per Section 21, preserving defect visibility |
| Parallel/cloud/iOS extension points remain untested until actually used | Extension hooks are structural allowances only; they carry no implementation risk until exercised |

## 27. Assumptions

- The AUT's screen structure remains consistent with MA-AA-001 observations throughout implementation.
- Elevated-risk items identified in MA-TS-001 Section 8 will be resolved via manual verification before the corresponding Page Object is implemented.
- A single contributor implements the framework; layering is designed for future handoff readiness (MA-PV-001 Section 26) but does not assume concurrent multi-developer workflows in this phase.
- No architectural component introduced here requires a tool or license beyond what is already frozen in MA-PV-001 Section 16.

## 28. Future Enhancements

Consistent with MA-PV-001 Section 27, and enabled by the extension hooks in Section 25: cloud device execution, parallel test execution, cross-platform (iOS) support, CI/CD pipeline enhancement, richer reporting, and performance tuning. None of these are commitments of the current architecture — they are documented here only to confirm the architecture does not structurally block them.

## 29. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

**End of Document — MA-FA-001, v1.1**
