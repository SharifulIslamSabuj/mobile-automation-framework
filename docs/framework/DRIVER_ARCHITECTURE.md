# Driver Architecture

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-DRV-001 |
| Title | Driver Architecture |
| Version | v1.0 |
| Status | Draft |
| Phase | Driver Management (Phase 4) |
| Governed By | [MA-FR-001 — Framework Architecture Rules](FRAMEWORK_ARCHITECTURE_RULES.md), [MA-CS-001 — Coding Standards](../standards/CODING_STANDARDS.md), [MA-CFG-001 — Configuration Architecture](CONFIGURATION_ARCHITECTURE.md) |

## 1. Driver Architecture

```
com.mobileautomation.framework.driver
├── DriverProvider            (public)          — the ONLY class other layers may reference
├── DriverManager              (package-private)  — ThreadLocal lifecycle owner; compiler-enforced encapsulation
├── DriverFactory               (public interface) — extension point: "how is a session created"
├── AndroidDriverFactory          (public)          — the only DriverFactory implementation today
├── CapabilityBuilder            (public interface) — extension point: "how are capabilities built"
└── UiAutomator2CapabilityBuilder  (public)          — the only CapabilityBuilder implementation today
```

```
                         ┌────────────────────┐
  (future) Base Test  →  │   DriverProvider    │   public, static, stateless facade
  (future) Page Object → │  (the ONLY door in)  │
                         └──────────┬───────────┘
                                    │  package-private call
                                    ▼
                         ┌────────────────────┐
                         │    DriverManager     │   ThreadLocal<AppiumDriver> owner
                         │  (package-private)    │
                         └──────────┬───────────┘
                                    │  driverFactory.createDriver()
                                    ▼
                         ┌────────────────────┐
                         │   DriverFactory       │   interface
                         │  ┌──────────────────┐ │
                         │  │AndroidDriverFactory│ │  ← only implementation today
                         │  └─────────┬────────┘ │
                         └────────────┼──────────┘
                                      │ 1. ConfigReader.getInstance()
                                      │ 2. CapabilityConfiguration.fromConfigReader(...)
                                      │ 3. capabilityBuilder.build(...)
                                      ▼
                         ┌────────────────────┐
                         │  CapabilityBuilder    │   interface
                         │ ┌───────────────────┐ │
                         │ │UiAutomator2CapabilityBuilder│ ← only implementation today
                         │ └───────────────────┘ │
                         └────────────────────┘
                                      │
                                      ▼
                     new AndroidDriver(serverUrl, capabilities)
```

**Single responsibility per class:** `DriverProvider` only exposes the four lifecycle operations; `DriverManager` only owns the `ThreadLocal` and the create/reuse/quit decision; `AndroidDriverFactory` only assembles the pieces needed to construct one `AndroidDriver`; `UiAutomator2CapabilityBuilder` only maps configuration values onto Appium capability objects. No class does more than one of these things.

## 2. Driver Lifecycle

| Method | Owner | Behavior |
|---|---|---|
| `initializeDriver()` | `DriverProvider` → `DriverManager` | Creates a driver on the current thread via the default `AndroidDriverFactory`. **Idempotent per thread**: if a driver is already active on this thread, this is a no-op — it never creates a second session. |
| `initializeDriver(DriverFactory)` | `DriverProvider` → `DriverManager` | Same as above, but with a caller-supplied `DriverFactory` — the extension point for a future non-Android or cloud-vendor factory. |
| `getDriver()` | `DriverProvider` → `DriverManager` | Returns the current thread's active driver. Throws `DriverInitializationException` (naming the current thread) if none has been initialized yet — callers are never handed a `null`. |
| `quitDriver()` | `DriverProvider` → `DriverManager` | Quits the current thread's driver, if any, and **always** clears the `ThreadLocal` slot afterward, even if `quit()` itself throws (`finally` block). A safe no-op if nothing is active — calling it twice in a row, or calling it when nothing was ever initialized, never throws. |
| `hasActiveDriver()` | `DriverProvider` → `DriverManager` | Returns whether a driver is active on the current thread, with no side effects — used by callers (and by this phase's own validation) to check state without risking an exception. |

All four were exercised directly (see §9 — Validation) and behaved exactly as described above.

## 3. ThreadLocal Design

```java
private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();
```

- **Deliberately a plain `ThreadLocal`, not `InheritableThreadLocal`.** If a future parallel-execution setup spawns worker threads per test (TestNG `parallel="methods"`/`"classes"`), an `InheritableThreadLocal` would let a child thread silently inherit its parent's driver reference — a well-known source of two threads racing to use the same `AppiumDriver` instance. A plain `ThreadLocal` forces every thread to call `initializeDriver()` for itself, which is the correct behavior for "one driver per execution thread."
- **The field is `private static final` on a package-private class.** Nothing outside `DriverManager` can read or write it directly, by construction — not merely by convention.
- **Cleanup is unconditional.** `quitDriver()`'s `try { driver.quit(); } finally { DRIVER.remove(); }` guarantees the slot is cleared even if the underlying `quit()` call throws (e.g., the session already died server-side). Without this, a thread reused by a thread pool (as TestNG does) would retain a dead driver reference indefinitely — a classic `ThreadLocal` leak.
- **No implicit creation.** `getDriver()` never silently calls `initializeDriver()` on the caller's behalf — a missing driver is always a thrown, named exception, never a silent null or a surprise session creation.

## 4. Driver Ownership

- **`DriverManager` owns the `ThreadLocal` and the create/reuse/quit decision.** It is package-private specifically so this ownership cannot be bypassed — the compiler rejects any attempt from outside the `driver` package to touch it.
- **`DriverProvider` owns the public contract.** It is a thin, stateless, static facade with no logic of its own beyond delegating to `DriverManager` — its only job is to be the one door every other layer walks through.
- **No other layer may hold a driver reference of its own.** A future `BasePage`/`BaseTest` must call `DriverProvider.getDriver()` on every use, not cache the returned reference in a field — caching would defeat the ThreadLocal design the moment that object crosses a thread boundary. (This will be restated as an explicit rule when `BasePage`/`BaseTest` are implemented in a later phase.)

## 5. Capability Flow

```
CapabilityConfiguration (Configuration Layer, Phase 3)
        │  CapabilityConfiguration.fromConfigReader(configReader)
        ▼
AndroidDriverFactory.createDriver()
        │  capabilityBuilder.build(capabilityConfiguration)
        ▼
UiAutomator2CapabilityBuilder
        │  maps each present field to the matching UiAutomator2Options setter;
        │  blank/absent optional values (udid, platformVersion, etc.) are
        │  omitted rather than sent as empty strings, so Appium's own
        │  defaults apply
        ▼
org.openqa.selenium.Capabilities  (returned to AndroidDriverFactory)
        │
        ▼
new AndroidDriver(serverUrl, capabilities)
```

No capability value is written as a literal anywhere in the `driver` package — every value traces back to `CapabilityConfiguration`, which itself traces back to `ConfigReader`. This was directly confirmed during validation (§9): the exact capability values requested (`appPackage`, `appActivity`, `deviceName=emulator-5554`, `automationName=UIAutomator2`, `noReset=true`, `fullReset=false`, `autoGrantPermissions=true`, `newCommandTimeout=120`) appeared verbatim in the Selenium `SessionNotCreatedException`'s command payload, matching `config.properties`/`config-emulator.properties` exactly.

## 6. Configuration Flow

Only `AndroidDriverFactory` touches the Configuration Layer — `DriverManager` and `DriverProvider` have zero dependency on `config`/`constants`, deliberately. This is a narrower integration surface than "the whole driver package talks to configuration," and keeps the ThreadLocal lifecycle logic (the part that most needs to stay simple and correct) completely decoupled from how configuration is loaded.

```
AndroidDriverFactory.createDriver()
  1. ConfigReader.getInstance()                              — Configuration Layer, Phase 3
  2. CapabilityConfiguration.fromConfigReader(configReader)    — Configuration Layer, Phase 3
  3. capabilityBuilder.build(capabilityConfiguration)          — this phase
  4. configReader.getAppiumServerUrl()                         — Configuration Layer, extended this phase (see §8)
  5. new AndroidDriver(serverUrl, capabilities)
```

## 7. Driver Cleanup Strategy

| Scenario | Behavior |
|---|---|
| Normal end of test | `DriverProvider.quitDriver()` → `driver.quit()` succeeds → `ThreadLocal` cleared |
| `quit()` throws (session already dead, network error, etc.) | `ThreadLocal` is **still** cleared (`finally` block) — the next `initializeDriver()` call on this thread starts clean rather than silently reusing/erroring on a dead reference |
| `quitDriver()` called with nothing active | No-op, no exception — safe to call defensively/unconditionally in future cleanup hooks |
| `initializeDriver()` fails (server unreachable, bad capabilities) | The `ThreadLocal` is **never set** in the first place (the exception is thrown before `DRIVER.set(...)` is reached) — confirmed empirically (§9): `hasActiveDriver()` remained `false` after a failed initialization attempt, and a subsequent `initializeDriver()` call independently retried rather than being blocked by stale state |
| Thread reused by a thread pool across multiple tests | Cleanup after each test's `quitDriver()` prevents any leaked reference from a prior test being visible to the next test on the same thread |

## 8. Necessary Configuration Layer Extension

Phase 3's property list (Application/Device/Driver/Execution/Reporting/Logging/Test Execution) did not include an Appium **server endpoint** — but `DriverFactory` cannot construct a session without one, and "never hardcode" / "configuration driven" apply to this value exactly as much as to any capability. Rather than hardcode it in `AndroidDriverFactory` (which would violate this phase's own explicit requirements), one property was added to the already-existing Configuration Layer:

- `appium.serverUrl` (key), default `http://127.0.0.1:4723` (Appium 2.x/3.x's default base URL — no `/wd/hub` suffix, which was the Appium 1.x convention), exposed via `ConfigReader.getAppiumServerUrl()`.

This is flagged explicitly in §10 as a decision worth your confirmation, since it technically extends a phase already marked complete — though it follows that phase's own established pattern (key → default → typed getter → properties file) exactly.

## 9. Validation Performed This Phase

Per the phase's Validation checklist, each item was exercised via a throwaway program (compiled and run against the real `io.appium:java-client:9.4.0` dependency, then discarded — not committed, and deliberately not written as a TestNG `@Test`, which is out of scope this phase):

| Requirement | Result |
|---|---|
| Configuration loads successfully | `ConfigReader.getInstance().getAppiumServerUrl()` resolved `http://127.0.0.1:4723` correctly |
| `getDriver()` before initialization throws meaningfully | `DriverInitializationException`: *"No driver has been initialized on thread 'main'. Call DriverProvider.initializeDriver() first."* |
| `quitDriver()` with nothing active is safe | Completed without throwing |
| Driver initializes successfully | **Not fully exercised** — see limitation note below |
| Driver quits successfully | **Not fully exercised** — see limitation note below |
| Invalid configuration produces meaningful exceptions | Confirmed via the real failure path: connecting to an unreachable Appium server produced `DriverInitializationException` wrapping Selenium's `SessionNotCreatedException`, with the exact capability payload visible in the message |
| ThreadLocal cleanup succeeds | `hasActiveDriver()` correctly stayed `false` after a failed `initializeDriver()` — no orphaned reference was ever set |
| Multiple initialization attempts are handled safely | A second `initializeDriver()` call after a failure independently retried (not silently skipped due to stale state); repeated `quitDriver()` calls remained safe no-ops throughout |

**Explicit limitation:** no Appium server, Android emulator, or physical device is available in this coding environment (Phase 2's evidence collection used a separate physical device and laptop running Appium Inspector, outside this environment's scope). This means the two checked-off items above marked "Not fully exercised" — an actual **successful** session creation and quit against a live device — could not be empirically proven in this phase. Everything that *can* be verified without live device infrastructure (configuration flow, capability mapping accuracy, exception messaging, and ThreadLocal safety under the failure path) was verified directly, not merely asserted. The first real, live-session validation will necessarily happen when the pilot test cases (TC-004, TC-012) are implemented against actual Appium/device infrastructure, per this project's governance rule (§11).

## 10. Architectural Decisions Requiring Approval

- **`appium.serverUrl` was added to the Configuration Layer** (§8) — a necessary but out-of-original-plan extension. Confirm this is acceptable, or redirect to a different mechanism (e.g., a dedicated `driver.properties` file instead of extending `config.properties`).
- **`DriverManager` is package-private**, stronger than the instructions strictly required ("no component should access DriverManager internals directly" was written as a rule, not necessarily as a visibility requirement). I chose compiler-enforced encapsulation over a documentation-only convention. Confirm this is the desired strictness, or relax it if a future need (e.g., direct unit testing of `DriverManager` from a different package) requires `public`/package visibility changes.
- **`DriverManager`'s lifecycle methods are `static`**, mirroring `ConfigReader`'s singleton-style access pattern from Phase 3, rather than an instance-based design. This keeps a single, unambiguous `ThreadLocal` regardless of how many `DriverManager` "instances" might otherwise exist. Confirm this consistency with `ConfigReader`'s style is desired, versus a fully instance-based/dependency-injected alternative.
- **Live-session validation could not be performed** in this environment (§9). Flagging this as an open item rather than silently claiming full validation — recommend it be explicitly re-run once Appium/device infrastructure is available, ideally as part of the pilot phase's own validation rather than assumed already covered here.

## 11. Future Extension Strategy

- **A new platform** (e.g., iOS/XCUITest): implement `DriverFactory` (e.g., `IosDriverFactory`) and `CapabilityBuilder` (e.g., `XcuiTest CapabilityBuilder`). Neither `DriverManager` nor `DriverProvider` change — they depend only on the `DriverFactory` interface.
- **A cloud vendor** (BrowserStack, Sauce Labs, Android Cloud Devices): implement `DriverFactory` to point at the vendor's remote endpoint and inject vendor-specific capabilities (likely via a vendor-specific `CapabilityBuilder`, or by composing/wrapping `UiAutomator2CapabilityBuilder`'s output with additional vendor options). The Configuration Layer would gain a new `Environment` value (e.g., `CLOUD`) and corresponding `config-cloud.properties` file, following the exact extension pattern already documented in MA-CFG-001 §10 — no change to `ConfigReader`'s loading logic itself.
- **Parallel execution**: already architecturally ready — the `ThreadLocal` design requires no change; only a future `BaseTest`/TestNG suite XML needs to enable `parallel="methods"` (or similar) and ensure every test thread calls `DriverProvider.initializeDriver()`/`quitDriver()` itself.
- **Do not** add a second way to obtain a driver (e.g., a static field on a future `BaseTest`). `DriverProvider` remains the only door, per §4.
