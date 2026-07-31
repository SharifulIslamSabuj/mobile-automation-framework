# Dependency Version Freeze

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-DEP-001 |
| Title | Dependency Version Freeze |
| Version | v1.0 |
| Status | Draft |
| Phase | Project Bootstrap |

This document records every dependency selected for the framework, the exact version frozen, and the reason it was chosen. It supersedes ad hoc version choice — any version bump must be a deliberate, documented decision here, not a silent `build.gradle` edit.

## Toolchain

| Item | Frozen Version | Reason |
|---|---|---|
| Java | 17 (LTS) | Long-Term-Support release, required by current Appium Java Client and Selenium major versions; matches MA-PV-001 §16 frozen stack |
| Gradle | Wrapper-pinned (see `gradle/wrapper/gradle-wrapper.properties`) | Gradle Wrapper guarantees every contributor and CI runner builds with an identical Gradle version, regardless of local install |
| Build tool | Gradle (Groovy DSL) | Already established in Phase 1 scaffolding; no reason to introduce Maven or Kotlin DSL mid-project |

## Dependencies

| Dependency | Frozen Version | Reason for This Version |
|---|---|---|
| `io.appium:java-client` | 9.4.0 | The 9.x line is the current Appium Java Client generation compatible with Appium 3.x servers and the W3C WebDriver protocol. (Note carried over from Phase 1: the originally-considered "3.x client" line predates W3C and is incompatible with modern Appium servers — rejected for that reason.) |
| `org.seleniumhq.selenium:selenium-java` | 4.25.0 | Required transitive-compatible Selenium version for Appium Java Client 9.4.0; W3C-only, actively maintained, no deprecated JSON-Wire-Protocol code paths |
| `org.slf4j:slf4j-api` | 2.0.16 | Current stable SLF4J 2.x line; decouples logging call sites from the concrete logging implementation, per MA-FA-001 §16 |
| `org.apache.logging.log4j:log4j-core` | 2.24.1 | Current stable Log4j2 line; post-Log4Shell (CVE-2021-44228) hardened releases only considered — no 2.x version below the patched baseline is acceptable |
| `org.apache.logging.log4j:log4j-slf4j2-impl` | 2.24.1 | Must exactly match `log4j-core`'s version to avoid classpath binding mismatches; bridges SLF4J 2.x calls to Log4j2 |
| `com.aventstack:extentreports` | 5.1.1 | Current stable ExtentReports 5.x line with Spark-theme HTML reporting, selected in MA-FA-001 v1.1 tool-decision addendum |
| `com.fasterxml.jackson.core:jackson-databind` | 2.18.0 | Current stable Jackson 2.x line for test-data (de)serialization, selected in MA-FA-001 v1.1 tool-decision addendum |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | 2.18.0 | Added Phase 8 (Test Data Framework) — gives the YAML reader the exact same `ObjectMapper`-based POJO-binding model as JSON, via `YAMLMapper`. Pinned to match `jackson-databind`'s version exactly. |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-properties` | 2.18.0 | Added Phase 8 — gives the Properties reader the same `ObjectMapper`-based POJO-binding model as JSON/YAML, via `JavaPropsMapper`, rather than hand-rolling `java.util.Properties`-to-POJO mapping. Pinned to match `jackson-databind`'s version exactly. |
| `net.datafaker:datafaker` | 2.4.3 | Added Phase 8 — synthetic test-data generation (names, emails, addresses, phone numbers, etc.). Chosen over `com.github.javafaker:javafaker` (the historically common choice): javafaker has had no release since 2020 and pulls in an outdated, CVE-affected SnakeYAML transitively; `net.datafaker` is its actively maintained successor with an equivalent API. |
| `org.testng:testng` | 7.10.2 | Current stable TestNG 7.x line; TestNG is the frozen test framework per MA-PV-001 §16 (data providers, parallel execution, and listener model needed for this framework's planned retry/reporting hooks — not available in JUnit 4, and JUnit 5 was not the frozen choice) |
| `org.projectlombok:lombok` | 1.18.34 | Current stable Lombok release with confirmed Java 17 annotation-processor compatibility. **Scope decision:** added now (Project Bootstrap) as `compileOnly`/`annotationProcessor` on both source sets — it is a compile-time-only dependency (never shipped at runtime), so including it early carries no runtime risk, and it will materially reduce boilerplate in upcoming Model/POJO classes (`models` package) and Page Object field declarations. Real value confirmed: yes. |
| `org.apache.commons:commons-lang3` | 3.17.0 | Current stable Commons Lang3 release. Selected as the single general-purpose utility library ("where appropriate") for the `utils` package — e.g., `StringUtils`, `RandomStringUtils`-style helpers for test-data generation — rather than hand-rolling equivalents or pulling in multiple overlapping utility libraries |

## Explicitly Not Added

| Candidate | Decision | Reason |
|---|---|---|
| `commons-io` | Not added | No confirmed present need; Apache Commons should be added "where appropriate," not speculatively — add when a concrete file-IO utility requirement appears in a later phase |
| JUnit (any version) | Not added | TestNG is the frozen test framework (MA-PV-001 §16); mixing test frameworks is out of scope and would violate the single-test-framework architecture rule |
| Any experimental/pre-release/`-RC`/`-beta` artifact | Not added | Explicit instruction: avoid experimental or deprecated versions |

## Change Control

Any future version bump or new dependency addition must:
1. Be proposed with the same "Frozen Version / Reason" format as above.
2. Be added to this document in the same commit/change as the `build.gradle` edit.
3. Not be introduced silently inside a Page Object, Test Class, or utility class commit.
