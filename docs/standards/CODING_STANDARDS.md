# Coding Standards

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-CS-001 |
| Title | Coding Standards |
| Version | v1.0 |
| Status | Draft |
| Phase | Project Bootstrap |

These standards are **mandatory** for all code written in this repository from the next implementation phase onward. They apply regardless of author. Related governing documents: [MA-FR-001 — Framework Architecture Rules](../framework/FRAMEWORK_ARCHITECTURE_RULES.md), [MA-LOC-001 — Locator Repository](../automation/LOCATOR_REPOSITORY.md).

## 1. Package Naming

- All lowercase, no underscores, no camelCase: `com.mobileautomation.framework.driver`, not `com.mobileautomation.framework.Driver` or `driver_utils`.
- Package names are singular category nouns matching the frozen package list (`driver`, not `drivers`; `listeners`, not `listener`) — follow the exact name already established in the frozen structure rather than a personal preference.
- No sub-packaging beyond the frozen top-level packages without an explicit architecture-change decision (see MA-FR-001 §1).

## 2. Class Naming

- `PascalCase`, noun-based, descriptive of role: `LoginPage`, `DriverFactory`, `BaseTest`, `LocatorRepository`.
- Page Objects are suffixed `Page` (`CartPage`, `CheckoutPaymentPage`) — matching the screen names used throughout MA-LOC-001, so a Page Object's name is traceable back to its Locator Repository section.
- Test classes are suffixed `Test` or `Tests` consistently across the whole project — pick one at first implementation and do not mix.
- Custom exceptions are suffixed `Exception` (`LocatorNotFoundException`, `DriverInitializationException`).
- Listeners are suffixed `Listener` (`RetryListener`, `ScreenshotListener`, `ReportingListener`).

## 3. Method Naming

- `camelCase`, verb-first, describing the action, not the implementation: `enterUsername(String value)`, not `setNameETText(String value)`.
- Page Object interaction methods describe user-facing behavior (`tapAddToCart()`), not raw widget operations (`clickCartBt()`) — this keeps Page Objects readable even as underlying locators change.
- Boolean-returning methods are prefixed `is`/`has`/`can`: `isCartBadgeVisible()`, `hasValidationError()`.
- Test methods describe the scenario under test in plain language: `loginWithValidCredentials_navigatesToProductCatalog()` or an equivalent readable pattern — pick one convention at first implementation and apply it consistently.

## 4. Variable Naming

- `camelCase`, descriptive, no single-letter names except conventional loop indices (`i`, `j`) in tight, obviously-scoped loops.
- Locator fields are named after the enterprise Locator Repository name, not the raw app id: `loginUsernameField`, not `nameET` (see MA-LOC-001 §3/§21 and §5 below).
- Avoid abbreviations that aren't already standard in the codebase (`btn`, `desc` are acceptable if used consistently; invented abbreviations are not).

## 5. Constant Naming

- `UPPER_SNAKE_CASE`, declared `static final`, grouped in the `constants` package by concern (`TimeoutConstants`, `EnvironmentConstants`) rather than one monolithic `Constants` class.
- Timeout values are named for what they wait for, with the unit implied by a suffix or Javadoc, e.g. `DEFAULT_EXPLICIT_WAIT_SECONDS`.

## 6. Locator Naming

Governed primarily by MA-LOC-001 §3 and §21 — restated here as a coding-standard obligation:

- Every locator field/variable follows `<screen><Element><Type>` camelCase (e.g., `loginUsernameField`, `catalogProductCard`, `cartCheckoutButton`, `productQuantityIncreaseButton`).
- The `<Type>` suffix reflects the element's role, not its Android widget class: `Field` (text input), `Button`, `Label`, `List`, `Badge`, `Checkbox`, `Icon` — not `EditText`, `TextView`, `Button` (Android class names leak implementation detail into the name).
- A locator name must never encode the raw app resource-id (`cartBt`, `paymentBtn`) as its Java identifier, because — per MA-LOC-001 §20 — those raw ids are reused across screens with different meanings; the enterprise name must disambiguate what the raw id cannot.

## 7. File Naming

- One public class per file; file name matches the class name exactly (standard Java requirement, restated for emphasis).
- Test data files (`src/test/resources/testdata`) use `snake_case.json`/`.yaml` matching the dataset name in MA-TDD-001 where applicable (e.g., `td_001_authentication.json`).
- Capability files (`src/test/resources/capabilities`) are named per target platform/device (`android_uiautomator2.json`).

## 8. Java Formatting

- 4-space indentation, no tabs.
- Line length target: 120 characters; hard wrap long fluent chains (e.g., ExtentReports builder chains, Selenium/Appium `Wait` builders) one call per line.
- Braces: opening brace on the same line as the declaration (K&R style), matching the style already present in this repository's `build.gradle` and configuration files.
- One blank line between methods; no more than one consecutive blank line anywhere.

## 9. Import Ordering

**Revised in Phase 8.10 (Foundation Readiness Resolution)** to match the convention actually applied consistently across every file since Phase 3 (confirmed by audit, MA-EFR-001 §16: 100% of files use this ordering, not the originally-specified 3-tier grouping): every import — `java.*`, third-party, and project-internal (`com.mobileautomation.framework.*`) alike — is sorted in one flat alphabetical block, matching standard IDE "organize imports" behavior. There is no separate `java.*`-first tier and no separate third-party-vs-internal grouping.

No wildcard imports (`import java.util.*;`) — every import is explicit. No unused imports committed.

## 10. Exception Handling

- Never catch `Exception` or `Throwable` broadly and swallow it silently. Catch the specific exception type expected.
- Framework-level failures (locator missing from the repository, driver failed to initialize, configuration file missing) are wrapped and re-thrown as the appropriate custom type from the `exceptions` package, with the original exception preserved as the cause (`throw new DriverInitializationException("...", originalException);`).
- A caught exception is always either handled meaningfully or logged with full context before propagating — never caught and discarded with an empty block.

## 11. Logging

- SLF4J is the only logging API referenced in code; never call Log4j2 classes directly from framework/test code (that coupling belongs solely to the Log4j2 configuration file).
- Always use parameterized logging: `log.info("Navigated to {} after login", destinationScreen);` — never string concatenation (`log.info("Navigated to " + destinationScreen)`), for both performance and consistency.
- Log levels: `ERROR` for failures that abort a test or framework operation; `WARN` for recoverable anomalies; `INFO` for high-level test/step milestones; `DEBUG` for locator resolution, wait polling, and other fine-grained detail not needed in a normal run.
- Never log credentials, full card numbers, or other sensitive test data values at `INFO` level or above.

## 12. Reporting

- Every test step meaningful to a report reader (not every internal method call) is logged to ExtentReports via the reporting layer/listener — not called directly and repeatedly from inside Page Object methods.
- Screenshots are attached to the report on failure at minimum; attaching on every step is a later, explicit decision, not a default.
- Report entries use the same plain-language phrasing as the corresponding MA-TC-001 test case step where one exists, so the report stays traceable back to the governing test case.

## 13. Wait Strategy

- Restated from MA-FR-001 §5 as a coding standard: explicit, condition-based waits only; `Thread.sleep()` is forbidden without exception.
- Wait timeout values are sourced from `constants`, never a magic number typed inline (`new WebDriverWait(driver, Duration.ofSeconds(15))` — the `15` must come from a named constant).
- Every wait condition targets a specific, meaningful state (element visible, element clickable, text present, element count changed) — never a generic "wait and hope."

## 14. Assertions

- Assertions live only in the Test Layer or the `assertions` helper package (see MA-FR-001 §2/§3) — never in a Page Object.
- Prefer soft-assertion grouping (TestNG `SoftAssert` or an equivalent custom wrapper in `assertions`) when a single test step verifies multiple independent facts, so one failure doesn't hide the rest.
- Every assertion failure message states what was expected and what was actually observed — never a bare `assertTrue(condition)` with no message.

## 15. JavaDoc Policy

- Every `package-info.java` carries a one-paragraph purpose statement (already established for every package in this Project Bootstrap phase — see `src/main/java/.../package-info.java` and `src/test/java/.../package-info.java`).
- Public classes and public methods in framework infrastructure (`core`, `driver`, `locators`, `utils`, `reporting`, `listeners`, `exceptions`) carry a JavaDoc comment stating *why*/*what contract* the class or method fulfills — not a restatement of its name.
- Page Objects and Test classes do not require exhaustive JavaDoc on every method; a class-level JavaDoc stating which screen (Page Object) or which test case ID (Test class, referencing its MA-TC-001 ID) it corresponds to is sufficient.
- No commented-out code is ever committed, in JavaDoc or elsewhere.
