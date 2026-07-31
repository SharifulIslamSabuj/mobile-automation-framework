# Core Utilities Architecture

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-UTIL-001 |
| Title | Core Utilities Architecture |
| Version | v1.0 |
| Status | Draft |
| Phase | Core Utilities (Phase 5) |
| Governed By | [MA-FR-001 — Framework Architecture Rules](FRAMEWORK_ARCHITECTURE_RULES.md), [MA-CS-001 — Coding Standards](../standards/CODING_STANDARDS.md), [MA-CFG-001 — Configuration Architecture](CONFIGURATION_ARCHITECTURE.md), [MA-DRV-001 — Driver Architecture](DRIVER_ARCHITECTURE.md) |

## 1. Utility Architecture

All twelve utilities live flatly in the `utils` package — no sub-packaging was introduced, per the package-architecture freeze from Project Bootstrap. Every class is a stateless, static-method utility class (private constructor, no instance fields), the same pattern already established by `ConfigReader`/`DriverProvider`.

```
com.mobileautomation.framework.utils
├── WaitUtility           — explicit waits (the foundation several others compose with)
├── ScrollUtility           — UiScrollable-based scrolling
├── GestureUtility           — W3C Actions (tap/double-tap/long-press/swipe/drag/fling)
├── KeyboardUtility           — soft-keyboard show/hide/detect
├── ToastUtility                — Android Toast detection (composes WaitUtility)
├── ScreenshotUtility            — capture + timestamped save (composes File/Date/Random)
├── DeviceUtility                  — orientation, screen size, activity/package, app state
├── AppUtility                       — activate/terminate/background app
├── PermissionUtility                  — OS permission-dialog handling (composes WaitUtility)
├── FileUtility           — driver-independent — pure java.nio.file
├── RandomUtility            — driver-independent — pure java.util/Commons Lang3 (composes DateUtility)
└── DateUtility                 — driver-independent — pure java.time
```

**Two-tier dependency shape**, deliberately:

```
Driver-dependent tier (talk to DriverProvider):
  WaitUtility ◄── ToastUtility, PermissionUtility (compose it for "wait for X" behavior)
  ScrollUtility, GestureUtility, KeyboardUtility, DeviceUtility, AppUtility  (standalone)
  ScreenshotUtility ── captures via DriverProvider, then hands off to the driver-independent tier

Driver-independent tier (zero DriverProvider dependency):
  FileUtility, RandomUtility, DateUtility
  RandomUtility ◄── composes DateUtility for its timestamp suffix
  ScreenshotUtility ◄── composes FileUtility + DateUtility + RandomUtility for naming/persistence
```

This matches the phase's own architecture principle — "driver-independent design where applicable" — literally: three of the twelve utilities (File, Random, Date) have no driver dependency whatsoever, and a fourth (Screenshot) isolates its one driver-touching line (`getScreenshotAs`) from all of its file-naming/persistence logic, which is itself fully driver-independent (see §7).

## 2. Utility Responsibilities

| Utility | Responsibility | Driver-dependent? | Config-dependent? |
|---|---|---|---|
| `WaitUtility` | Explicit-wait conditions: visibility, presence, clickable, invisibility, attribute conditions, a generic `waitUntil` escape hatch | Yes (`DriverProvider`) | Yes — default timeout from `ConfigReader.getExplicitWaitTimeout()` |
| `ScrollUtility` | Vertical/horizontal paging, scroll-to-text/text-contains/resource-id/accessibility-id via `UiScrollable` | Yes | No |
| `GestureUtility` | Tap, double-tap, long-press, swipe, drag-and-drop, fling — all via W3C `PointerInput`/`Sequence`, never the deprecated `TouchAction` | Yes | No |
| `KeyboardUtility` | Hide keyboard, detect keyboard visibility, "show keyboard" (by focusing a caller-supplied element) | Yes | No |
| `ToastUtility` | Detect/read/wait-for a Toast — detection only, never asserts | Yes (composes `WaitUtility`) | Indirectly (via `WaitUtility`) |
| `ScreenshotUtility` | Capture + save with a timestamped, unique file name; returns the saved path | Yes (capture only) | Yes — save directory from `ConfigReader.getScreenshotDirectory()` |
| `DeviceUtility` | Orientation get/set, screen size, current activity, current package, app state query | Yes | No |
| `AppUtility` | Activate (launch)/terminate (close)/background an app by package id | Yes | No |
| `PermissionUtility` | Detect/accept/accept-once/deny an Android OS runtime-permission dialog | Yes (composes `WaitUtility`) | Indirectly |
| `FileUtility` | Existence checks, directory creation, safe copy, safe delete, path building, temp files — all `java.nio.file` | No | No |
| `RandomUtility` | Random int, alphabetic/alphanumeric strings, UUID, timestamp suffix (composes `DateUtility`) | No | No |
| `DateUtility` | Current date/time (incl. UTC), formatting, parsing, day arithmetic, filename-safe timestamp — `java.time` only | No | No |

## 3. Dependency Rules

Enforced by design, not just documented:

- **Every driver-dependent utility calls `DriverProvider.getDriver()` fresh on every method call.** No utility class holds a driver reference as a field — confirmed by inspection: none of the twelve classes declares a non-`static`/non-`final` field of any kind (they are all fully stateless).
- **No utility imports `com.mobileautomation.framework.driver.DriverManager`** — package-private, and even if it weren't, no utility needs anything beyond `DriverProvider`'s four public methods.
- **No utility imports anything from `pages`, `tests`, `reporting`, `logging`, `listeners`, `exceptions` (beyond the two exception types this layer and Phase 3/4 introduced), or any future `core`/`components`/`models` class.** Only `driver` (via `DriverProvider`) and `config` (via `ConfigReader`) are referenced, exactly matching this phase's Integration Rules.
- **`ConfigReader` values are read fresh, never cached at class-load time** — e.g. `WaitUtility.defaultTimeout()` calls `ConfigReader.getInstance().getExplicitWaitTimeout()` on every invocation, so a system-property override applied mid-run is honored immediately.

## 4. Driver Interaction Model

```
Utility method call
        │
        ▼
DriverProvider.getDriver()   ← the ONLY way any utility touches a driver
        │
        ▼
(cast to AndroidDriver / a Selenium mixin interface, e.g. Interactive,
 SupportsRotation, TakesScreenshot — only where that specific capability
 is needed for that one call)
        │
        ▼
Selenium/Appium API call
```

No utility ever calls `DriverManager` directly (it is package-private and inaccessible outside `driver` regardless), and no utility ever calls `initializeDriver()` or `quitDriver()` itself — lifecycle ownership stays entirely with whatever layer started the session (a later phase's `BaseTest`). Utilities are pure *consumers* of an already-active driver.

Where a capability beyond the base `WebDriver` interface is needed (touch actions, rotation, screenshots, Android-specific app/keyboard/activity operations), the utility casts the object returned by `DriverProvider.getDriver()` to the specific Selenium/Appium interface or the concrete `AndroidDriver` type, scoped to that one call — it never widens its own field/parameter types to `AndroidDriver` beyond what's needed.

## 5. Configuration Flow

Only two utilities read configuration, and both do so exactly like `AndroidDriverFactory` did in Phase 4 — via `ConfigReader.getInstance()`, never a raw property key:

- `WaitUtility` → `ConfigReader.getExplicitWaitTimeout()` (existing Phase 3 property, no changes needed)
- `ScreenshotUtility` → `ConfigReader.getScreenshotDirectory()` (existing Phase 3 property, no changes needed)

Unlike Phase 4, **no new configuration property was required this phase** — every value Core Utilities needed already existed in the Configuration Layer.

## 6. Reusability Guidelines

- **Every utility method is generic over its input** — a locator, an element, a coordinate, a duration, a string. None references an AUT-specific locator (no `com.saucelabs.mydemoapp.android:id/...` string appears anywhere in this layer) or an AUT-specific screen name.
- **`PermissionUtility` is the one case worth explaining explicitly**: it does reference concrete resource-ids (`com.android.permissioncontroller:id/permission_allow_button`, etc.). These are **Android OS** dialog ids, identical across every app on a given OS/permission-controller version — not sourced from MA-LOC-001 (which documents only this project's AUT locators) and not specific to the Sauce Labs demo app. They were confirmed directly from this project's own Phase 2 device evidence (the Geo Location and Drawing permission-dialog captures), so they are evidence-backed, not guessed.
- **Cross-utility composition is used deliberately to avoid duplication**: `ScreenshotUtility` reuses `FileUtility`/`DateUtility`/`RandomUtility` rather than re-implementing directory creation or unique naming; `ToastUtility`/`PermissionUtility` reuse `WaitUtility` rather than each rolling its own `WebDriverWait`; `RandomUtility` reuses `DateUtility`'s timestamp formatting.
- **Selective exception wrapping**: `FileUtility` and `ScreenshotUtility` wrap checked `IOException`s in `UtilityOperationException` with contextual detail (the path/operation involved) — genuinely adding information a bare `IOException` wouldn't carry. `WaitUtility`, `ScrollUtility`, `GestureUtility`, `KeyboardUtility`, `ToastUtility`, `DeviceUtility`, `AppUtility`, and `PermissionUtility` deliberately do **not** re-wrap Selenium/Appium's own exceptions (`TimeoutException`, `NoSuchElementException`, `WebDriverException`) — those are already meaningful, and wrapping them would hide the specific exception type from a future Page Object that wants to catch it specifically.

## 7. Validation Performed This Phase

| Requirement | Result |
|---|---|
| Utilities compile successfully | `gradlew clean build` → BUILD SUCCESSFUL, against the real `io.appium:java-client:9.4.0` / `selenium-java:4.25.0` dependencies (several method names — `SupportsRotation`, `ApplicationState`, `queryAppState` — were corrected against real compiler errors during this phase, the same empirical approach used in Phase 4) |
| Driver-dependent utilities retrieve the driver only through `DriverProvider` | Confirmed by inspection — no other access path exists in any of the eight driver-dependent classes |
| Wait Utility uses explicit waits only | Confirmed by design — `WebDriverWait`/`ExpectedConditions` exclusively; no `Thread.sleep()`, no implicit-wait configuration anywhere in this class or any other utility (including `GestureUtility.doubleTap`, which deliberately relies on natural network round-trip timing between two `tap()` calls rather than an artificial delay) |
| Screenshot Utility saves files correctly | **Empirically verified** without a live driver, by splitting `captureScreenshot(String)` (the one line that touches a live driver) from a package-private `persistScreenshot(File, String)` (pure file-naming/persistence logic). Ran directly against a dummy source file: both saves landed on disk, two captures with the identical prefix produced distinct file names, unsafe characters in the prefix were sanitized, and a blank prefix was rejected with `UtilityOperationException` |
| File Utility handles missing paths safely | **Empirically verified**: `exists()` correctly returns `false` for a missing path (no throw); `deleteIfExists()` returns `true` once then `false` on a second call for the same path (no throw); `copyFile()` from a genuinely missing source threw `UtilityOperationException` naming both paths, as designed |
| Random Utility produces unique values | **Empirically verified**: 1000 `randomUuid()` calls were all pairwise distinct; 500 `randomAlphanumeric(8)` calls produced 500/500 distinct values in the actual run |
| Date Utility behaves consistently | **Empirically verified**: `format` → `parse` round-tripped to an identical value; `plusDays`/`minusDays` were confirmed as exact inverses; `timestampForFilename()`'s output matched the expected `yyyyMMdd_HHmmss` pattern |

**Explicit limitation, consistent with Phase 4:** the eight driver-dependent utilities (`Wait`, `Scroll`, `Gesture`, `Keyboard`, `Toast`, `Device`, `App`, `Permission`, and `ScreenshotUtility`'s capture step) could not be exercised against a **live** Appium/device session in this environment, for the same reason recorded in MA-DRV-001 §9 — no Appium server or Android emulator/device is available here. Their correctness rests on: (a) compiling successfully against the real Appium Java Client API (method names/return types were verified, not guessed, per the iterative-compile approach), and (b) direct API-contract review against Appium/Selenium's documented interfaces (`SupportsRotation`, `Interactive`, `InteractsWithApps`, `HidesKeyboard`, `TakesScreenshot`). First live-session validation of these will occur during pilot implementation (TC-004, TC-012), per this project's governance rule.

## 8. Architectural Decisions Requiring Approval

- **`AppUtility.launchApp`/`closeApp` are aliases for `activateApp`/`terminateApp`** (§ table in the class Javadoc) because Appium's actual Android API surface only exposes three lifecycle primitives, not five. Confirm this naming/aliasing choice, or redirect if "Launch" and "Activate" (or "Close" and "Terminate") were intended to be genuinely distinct behaviors.
- **`PermissionUtility` hardcodes Android OS permission-controller resource-ids.** As explained in §6, these are OS-level, not AUT-specific, and evidence-backed from this project's own Phase 2 captures — but this is still, literally, a set of fixed string constants baked into a "generic" utility. Confirm this reasoning is accepted, or redirect toward a fully caller-supplied-locator design if OS-level ids are considered out of bounds too.
- **Selective exception wrapping** (§6) — only `FileUtility`/`ScreenshotUtility` wrap their underlying exceptions; the rest let Selenium/Appium exceptions propagate natively. Confirm this asymmetry is acceptable, or redirect toward wrapping everything uniformly for consistency at the cost of hiding specific exception types.
- **Live-session validation could not be performed** for the eight driver-dependent utilities (§7), matching the same limitation already flagged and accepted in Phase 4. Recording again here rather than letting it go unstated for this phase specifically.
- **`ToastUtility.detectToast` and `PermissionUtility.waitForPermissionDialog` each catch `TimeoutException` and return `Optional.empty()`/`false` rather than propagating it** — the same category of deliberate, non-propagating decision later made explicit for `ScreenshotManager` (Cross-Cutting Infrastructure §11) and `ElementActions.isDisplayed` (Base Framework §9), but until Phase 8.10's Foundation Readiness Resolution this pair was documented only via inline Javadoc ("not treated as a failure"), not elevated to this list. Recorded here for consistency — no behavior changed.

## 9. Future Extension Strategy

- **A new utility method on an existing class** (e.g., a new `WaitUtility.waitForTextToBe(...)`) is added directly to that class — no new package, no new class, as long as it's genuinely generic.
- **A new utility class** (a 13th responsibility not on this phase's list) is added flatly into `utils`, following the same static-method, private-constructor, `DriverProvider`/`ConfigReader`-only dependency pattern established here.
- **A new platform's gesture/scroll semantics** (e.g., iOS `XCUIElementType`-based scrolling) would need a platform check or a parallel class (e.g. `IosScrollUtility`) — this phase does not attempt to abstract that, since only Android/UiAutomator2 is in scope (frozen stack, MA-PV-001 §16). If/when iOS support is ever added, this document should be revisited to decide between "one class per platform" vs. "one interface, two implementations" (mirroring the `DriverFactory`/`CapabilityBuilder` extension-point pattern from MA-DRV-001).
- **Do not** let a utility grow a dependency on `reporting`, `logging`, or `listeners` when those layers are implemented in a later phase — if a utility "wants" to log or report something, that need belongs to the caller (a future `BasePage`/`BaseTest`), not to the utility itself.
