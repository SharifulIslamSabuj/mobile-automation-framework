# Base Framework Architecture

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-BF-001 |
| Title | Base Framework Architecture |
| Version | v1.0 |
| Status | Draft |
| Phase | Base Framework (Phase 7) |
| Governed By | [MA-FR-001 — Framework Architecture Rules](FRAMEWORK_ARCHITECTURE_RULES.md), [MA-CS-001 — Coding Standards](../standards/CODING_STANDARDS.md), [MA-CFG-001 — Configuration Architecture](CONFIGURATION_ARCHITECTURE.md), [MA-DRV-001 — Driver Architecture](DRIVER_ARCHITECTURE.md), [MA-UTIL-001 — Core Utilities Architecture](CORE_UTILITIES_ARCHITECTURE.md), [MA-XCI-001 — Cross-Cutting Infrastructure](CROSS_CUTTING_INFRASTRUCTURE.md) |

## 1. Scope of This Phase

This phase built the reusable execution layer every future Page Object and Test Class will inherit from or compose with — BasePage, BaseTest, Element Actions, Navigation Helper, Common Assertions, Reusable Components, and Dialog Components. No application-specific logic, business workflow, or business assertion was written. No Page Object, no Test Class, no Test Data Framework, and no automation of TC-004 or TC-012 exists after this phase.

```
com.mobileautomation.framework
├── core                              (main)
│   ├── BasePage                      — base for every future Page Object
│   ├── BaseTest                      — base for every future Test Class
│   ├── ElementActions                — reusable Selenium/Appium interaction wrapper
│   └── NavigationHelper              — generic navigation operations
├── components                        (main)
│   ├── BaseComponent                 — caller-supplied-root-locator base
│   ├── HeaderComponent, FooterComponent, ToolbarComponent,
│   │   BottomNavigationComponent, SearchBarComponent, LoadingIndicatorComponent
│   └── ConfirmationDialogComponent, AlertDialogComponent,
│       GenericPopupComponent, PermissionDialogComponent
├── exceptions                        (main)
│   └── ElementActionException        — new; extends FrameworkException
└── assertions                        (test)
    └── CommonAssertions               — generic verify* wrappers
```

## 2. Class Responsibilities

| Class | Responsibility | What it deliberately does NOT do |
|---|---|---|
| `BasePage` | Driver access (`driver()`), element-interaction delegation (`elementActions` field), navigation-helper access (`navigationHelper` field), screenshot access (`captureScreenshot(...)`), logging access (`logger` field) | No business workflow, no application navigation, no assertion, no test logic — stays lightweight (5 fields/methods total) |
| `BaseTest` | Per-method driver init/cleanup, TestNG listener registration (`@Listeners`), Configuration Layer access (`config()`) | No `@Test` method, no test logic, no application logic — abstract only |
| `ElementActions` | `click`, `type`, `clear`, `getText`, `isDisplayed`, `isEnabled`, `isSelected`, `select`, `deselect` — all built on `WaitUtility`, logged via `LogManager`, screenshotted-and-wrapped into `ElementActionException` on failure | No locator is ever hardcoded — every method takes a caller-supplied `By` |
| `NavigationHelper` | `back`, `refresh`, `openNotifications`, `closeKeyboardIfShown` | No AUT-specific screen-to-screen navigation |
| `CommonAssertions` | `verifyVisible`, `verifyHidden`, `verifyEnabled`, `verifyDisabled`, `verifyText`, `verifyContains` — centralized message format, reporting integration, failure screenshot | No business assertion (e.g. no `verifyLoginSucceeded`) |
| `BaseComponent` + 6 UI components | Generic, reusable UI patterns (header, footer, toolbar, bottom nav, search bar, loading indicator), all locator-parameterized by the caller | No fixed/hardcoded locator anywhere in the package |
| 4 Dialog Components | Generic dialog shapes (confirmation, alert, generic popup) plus a Dialog-Component-shaped wrapper around the existing OS permission dialog handling | No AUT-specific dialog |

## 3. Dependency Diagram

```
                    ┌─────────────┐   ┌──────────────┐   ┌───────────┐   ┌─────────────────┐
                    │ config      │   │ driver       │   │ utils     │   │ logging/reporting│
                    │ (Phase 3)   │   │ (Phase 4)    │   │ (Phase 5) │   │ (Phase 6)        │
                    └──────┬──────┘   └──────┬───────┘   └─────┬─────┘   └────────┬─────────┘
                           │                 │                 │                  │
                           └────────┬────────┴────────┬────────┴──────────┬───────┘
                                    │                 │                   │
                                    ▼                 ▼                   ▼
                              ┌───────────────────────────────────────────────┐
                              │                    core                        │
                              │  ElementActions ◄── WaitUtility, ScreenshotManager, LogManager
                              │  NavigationHelper ◄── DriverProvider, KeyboardUtility, LogManager
                              │  BasePage ◄── ElementActions, NavigationHelper, ScreenshotManager, LogManager, DriverProvider
                              │  BaseTest ◄── DriverProvider, ConfigReader, listeners.*, LogManager
                              └───────────────────────┬─────────────────────────┘
                                                        │
                                                        ▼
                              ┌───────────────────────────────────────────────┐
                              │                 components                     │
                              │  BaseComponent ◄── core.ElementActions          │
                              │  (all 6 UI components + 3 generic dialogs) ◄── BaseComponent
                              │  PermissionDialogComponent ◄── utils.PermissionUtility (no BaseComponent — see §6)
                              └───────────────────────────────────────────────┘

                              ┌───────────────────────────────────────────────┐
                              │            assertions (test source set)        │
                              │  CommonAssertions ◄── reporting.ReportProvider, │
                              │                       reporting.ScreenshotManager, logging.LogManager │
                              │  (zero dependency on core/components — see §5) │
                              └───────────────────────────────────────────────┘
```

`exceptions.ElementActionException` is referenced by `core.ElementActions` and `components.BottomNavigationComponent` — a shared vocabulary edge, consistent with how `ReportingException` is referenced across Phase 6.

No class built this phase imports anything from `pages`, `tests`, `runners`, `data`, or `models` — confirmed by inspection of every import statement written, matching this phase's Integration Rules (Configuration Layer, Driver Management, Core Utilities, Cross-Cutting Infrastructure only).

## 4. Element Action Philosophy

Every `ElementActions` method funnels through one private `execute(By, String, Supplier<T>)` helper (DRY): it waits using the correct `WaitUtility` condition for that action (`waitForClickable` for `click`/`select`/`deselect`, `waitForVisibility` for `type`/`clear`/`getText`, `waitForPresence` for `isEnabled`/`isSelected`), performs the action, logs success at INFO, and on any `RuntimeException` logs at ERROR, captures a screenshot through `ScreenshotManager`, and re-throws as `ElementActionException` with the original cause preserved — never swallowed.

`isDisplayed(By)` is the one deliberate exception to that pattern: it catches only `org.openqa.selenium.TimeoutException` (a genuine "never became visible" signal) and returns `false`, rather than treating "not currently visible" as a failure. An infrastructure-level failure — no driver active, for instance — is a different category of problem and is **not** caught here; it propagates as `DriverInitializationException` uncaught, so a caller can't mistake "the framework is broken" for "the element just isn't on screen." This was empirically confirmed: with no active driver, `isDisplayed()` on a component correctly propagated `DriverInitializationException` rather than silently returning `false`.

## 5. Assertion Strategy

`CommonAssertions` takes an already-computed actual value (a `boolean` or `String`), never a locator or a driver — this keeps it fully decoupled from `core`/`components` (zero import from either package) and testable in isolation. Obtaining the actual value (via `ElementActions`) is a future Page Object's job; asserting on it is `CommonAssertions`'s job (single responsibility).

Every `verify*` method funnels through one private `evaluate(boolean, String, String, String)` method that centralizes the message format, logs pass/fail, and — if a report test node is active (`ReportProvider.hasActiveTest()`) — records a `pass`/`fail` entry on it. On failure it additionally captures a screenshot via `ScreenshotManager` and, when successful, attaches it to the failed report entry via `MediaEntityBuilder.createScreenCaptureFromPath(...)`.

Failures throw a plain `java.lang.AssertionError`, not an `ElementActionException`/`FrameworkException` subtype — deliberately, so TestNG's native pass/fail detection (the same mechanism `org.testng.Assert` relies on) continues to work unmodified. This was empirically verified: a failing `verifyVisible`/`verifyText` call logged the failure, attempted a screenshot, recorded a `fail` entry on the report, and then threw `AssertionError` with the expected centralized message.

## 6. Component Strategy

Every generic UI component and dialog (except `PermissionDialogComponent`) extends `BaseComponent`, whose constructor takes a caller-supplied root `By` locator and composes an `ElementActions` instance (composition over inheritance for the interaction logic itself — `BaseComponent` does not re-implement waiting/interaction, it delegates). Every child locator (title, back button, tab locators, message, confirm/cancel buttons, etc.) is also caller-supplied, via the component's constructor — never a fixed string anywhere in the `components` package. This is what makes `HeaderComponent`, `BottomNavigationComponent`, etc. genuinely reusable across any screen of any AUT, not just the Sauce Labs demo app.

`PermissionDialogComponent` is the one exception: it wraps `utils.PermissionUtility` (built and evidence-verified in Phase 5), whose Android-OS-level resource-ids are fixed and not caller-suppliable (there is no "root locator" a caller could sensibly provide for an OS dialog). It therefore does not extend `BaseComponent` — a deliberate composition-over-inheritance choice: it *has-a* `PermissionUtility` dependency rather than forcing an inheritance relationship that doesn't fit.

`BottomNavigationComponent` is the one component with an open-ended shape (an app can have any number of tabs, named anything) — it accepts a `Map<String, By>` rather than fixed constructor parameters, throwing `ElementActionException` if a caller queries a tab name that was never registered.

## 7. Navigation Model

`NavigationHelper` covers only the four generic operations explicitly in scope: `back()`/`refresh()` (delegating to `WebDriver.Navigation`, confirmed via `javap` against the real Selenium 4.25.0 API), `openNotifications()` (delegating to Appium's `io.appium.java_client.android.HasNotifications`, also `javap`-confirmed), and `closeKeyboardIfShown()` (composing `utils.KeyboardUtility` from Phase 5 rather than duplicating its logic). Moving between AUT screens (e.g. "go from Product List to Cart") is explicitly out of scope — that is a Page Object's responsibility once Page Objects exist.

## 8. Validation Performed This Phase

| Requirement | Result |
|---|---|
| BasePage compiles successfully | `gradlew clean build` → BUILD SUCCESSFUL |
| BaseTest integrates correctly with DriverProvider | **Empirically verified**: instantiated a `BaseTest` subclass, confirmed `@Listeners` carries `SuiteListener`/`TestListener`/`MethodListener`, called `initializeDriver()` — with no Appium server reachable this correctly threw `DriverInitializationException` (proving the call path genuinely reaches `DriverProvider` → `AndroidDriverFactory` → a real Appium session attempt, not a stub), and `quitDriver()` afterward was a safe no-op with `hasActiveDriver()` remaining `false` throughout |
| Element Actions use WaitUtility | Confirmed by design (every method routes through `WaitUtility.waitForClickable`/`waitForVisibility`/`waitForPresence`) and by the empirical stack trace of a triggered failure, which shows the real call chain `ElementActions.click → WaitUtility.waitForClickable → DriverProvider.getDriver` |
| Assertions integrate with Reporting | **Empirically verified**: with an active `ReportProvider` test node, three passing and two failing `CommonAssertions` calls were made; the generated `.html` report file was inspected directly and contained the expected `verifyVisible`/`verifyText` pass and fail entries |
| Screenshots are captured through ScreenshotManager | **Empirically verified**: both `ElementActions` failures and `CommonAssertions` failures invoked `ScreenshotManager.captureScreenshot(...)`, confirmed via its WARN-level log line, and — as designed since Phase 6 — the capture failure (no driver) did not propagate, only the intended `ElementActionException`/`AssertionError` did |
| Logging works correctly | Confirmed throughout — every component logs via `LogManager.getLogger(...)`, never `LoggerFactory` directly |
| Framework builds successfully | `gradlew clean build` (main) and `gradlew compileTestJava` (test, including the new `assertions.CommonAssertions`) both BUILD SUCCESSFUL on the first attempt against the real TestNG 7.10.2 / ExtentReports 5.1.1 / Appium Java Client 9.4.0 / Selenium 4.25.0 APIs |

**Explicit limitation, consistent with Phases 4–6:** no component in this phase was exercised against a **live** Appium/device session — there is no Appium server or Android emulator/device available in this environment. Every failure path shown above was triggered by the *absence* of a driver, which is itself a real, meaningful validation of the exception-wrapping and screenshot-integration logic, but a genuine element interaction against a live app was not (and, per "No automation tests may be written," could not be) exercised. First live-session validation occurs during pilot implementation (TC-004, TC-012).

## 9. Architectural Decisions Requiring Approval

- **`BasePage` and `BaseTest` live in the `core` package (main source set)**, not a new package. `core`'s own Project-Bootstrap description ("Core framework abstractions shared across the driver, page, and test layers") anticipated exactly this. Confirm this placement, or redirect toward a dedicated package if `BasePage`/`BaseTest` should instead sit closer to (or inside) the test source set's `pages`/`tests` packages.
- **`BaseTest`'s driver lifecycle is scoped per test method** (`@BeforeMethod`/`@AfterMethod`), not per class. This maximizes test isolation at the cost of a fresh (and, for a mobile app, non-trivial) Appium session per test method. With only two pilot tests this cost is negligible, but it is a real tradeoff for the full 32-test suite later. Confirm this default, or redirect toward per-class (`@BeforeClass`/`@AfterClass`) lifecycle scoping.
- **`CommonAssertions` throws plain `java.lang.AssertionError`**, not a `FrameworkException` subtype, to preserve TestNG's native assertion semantics (§5). Confirm this is the intended behavior, or redirect toward a custom assertion exception type if stronger typing of assertion failures (as distinct from other RuntimeExceptions) is wanted.
- **`PermissionDialogComponent` does not extend `BaseComponent`** (§6) — an intentional asymmetry versus the other nine components. Confirm this reasoning is accepted, or redirect toward forcing a uniform `BaseComponent` inheritance (e.g. by giving it a synthetic root locator) for consistency at the cost of a less accurate model.
- **`ElementActionException` was added to the `exceptions` package**, following the precedent set by `ReportingException` in Phase 6 (all concrete exception types live in `exceptions`, not colocated with their throwing package). Confirm this precedent should continue.
- **`ElementActions.isDisplayed(By)` catching `TimeoutException` and returning `false`** (§4) is the same category of deliberate, non-propagating decision as `ScreenshotManager`'s (Cross-Cutting Infrastructure §11) — recorded here explicitly for consistency, since it was previously documented only inline (§4) and not elevated to this list (closed in Phase 8.10's Foundation Readiness Resolution, MA-EFR-001 §8/§17).
- **RESOLVED (Phase 8.10):** whether `CommonAssertions` requires soft-assertion (multi-fact, non-short-circuiting) support was reviewed against MA-CS-001 §14, which reads *"**Prefer** soft-assertion grouping... **when** a single test step verifies multiple independent facts"* — conditional, preferential language, not a mandate (contrast with §10's "**Never** catch Exception... broadly" or §13's "Thread.sleep() is **forbidden without exception**"). The standard itself names `org.testng.asserts.SoftAssert` as an acceptable implementation, and that class is already on the compile classpath (TestNG 7.10.2, frozen since Project Bootstrap) — so the capability the standard describes is available today with zero new code, for any future Test Class that needs it. Determined **not mandatory** for this phase; no `CommonAssertions`-integrated soft-assert wrapper was built. Revisit if Phase 9's two pilot tests, or the later 32-test suite, turn out to need multi-fact verification with `CommonAssertions`'s centralized reporting/screenshot integration specifically (not just raw `SoftAssert`).

## 10. Extension Guidelines

- **A new `ElementActions` method** (e.g. `swipeElement`, `getAttribute`) is added directly to that class, following the existing `execute(...)`-routed pattern — no new class, no new package.
- **A new reusable component** extends `BaseComponent`, takes every locator it needs via its constructor, and is added flatly into `components` — no sub-packaging, matching the flat-package precedent from Phase 5's `utils`.
- **A new `CommonAssertions` method** is added directly to that class and routes through the existing private `evaluate(...)` helper so message formatting, reporting, and screenshot behavior stay centralized.
- **A future Page Object** extends `BasePage`, uses its `elementActions`/`navigationHelper` fields for interaction, and adds its own AUT-specific locators and methods — `BasePage` itself must never grow an AUT-specific method to accommodate a single Page Object's needs.
- **A future Test Class** extends `BaseTest` and adds only `@Test` methods and assertions (via `CommonAssertions`) — `BaseTest` itself must never grow test-specific logic.
- **Do not** let `core`, `components`, or `assertions` grow a dependency on `pages`, `tests`, `data`, or `models` when those layers are built — the dependency direction is one-way (Page Objects/Test Classes depend on the Base Framework, never the reverse).
