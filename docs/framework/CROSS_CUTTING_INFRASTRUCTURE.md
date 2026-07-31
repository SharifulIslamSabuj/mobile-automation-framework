# Cross-Cutting Infrastructure

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-XCI-001 |
| Title | Cross-Cutting Infrastructure |
| Version | v1.0 |
| Status | Draft |
| Phase | Cross-Cutting Infrastructure (Phase 6) |
| Governed By | [MA-FR-001 — Framework Architecture Rules](FRAMEWORK_ARCHITECTURE_RULES.md), [MA-CS-001 — Coding Standards](../standards/CODING_STANDARDS.md), [MA-CFG-001 — Configuration Architecture](CONFIGURATION_ARCHITECTURE.md), [MA-DRV-001 — Driver Architecture](DRIVER_ARCHITECTURE.md), [MA-UTIL-001 — Core Utilities Architecture](CORE_UTILITIES_ARCHITECTURE.md) |

## 1. Scope of This Phase

This phase built seven cross-cutting concerns — Logging, Exception Framework, Reporting Foundation, Screenshot Manager, Retry Analyzer, TestNG Listeners, and an Allure extension point — none of which contain or reference Page Object, Test Class, or business-assertion logic. Nothing in this phase automates a test case; TC-004 and TC-012 remain untouched.

```
com.mobileautomation.framework
├── logging
│   └── LogManager                — SLF4J entry point + Log4j2 system-property bridge
├── exceptions
│   ├── FrameworkException (abstract, new root)
│   ├── ConfigurationException     (refactored to extend FrameworkException)
│   ├── DriverInitializationException (refactored to extend FrameworkException)
│   ├── UtilityOperationException  (refactored to extend FrameworkException)
│   └── ReportingException         (new)
├── reporting
│   ├── ExtentReportManager       — package-private, owns the ExtentReports instance
│   ├── ReportProvider            — public facade
│   └── ScreenshotManager         — wraps utils.ScreenshotUtility for listener hooks
└── listeners
    ├── RetryAnalyzer             — IRetryAnalyzer
    ├── SuiteListener             — ISuiteListener
    ├── TestListener              — ITestListener
    └── MethodListener            — IInvokedMethodListener
```

## 2. Logging Architecture

**`LogManager`** (`logging` package) is the single entry point every framework class must use to obtain a logger — `LogManager.getLogger(Class<?>)` — never `org.slf4j.LoggerFactory` or any `org.apache.logging.log4j` class directly (MA-CS-001 §11: SLF4J-only logging API in code).

Beyond being a thin factory, `LogManager` bridges the Configuration Layer into Log4j2's static XML configuration:

```
LogManager.getLogger(clazz)
        │
        ▼
ensureConfigurationBridged()   (synchronized, double-checked, runs once per JVM)
        │
        ├─ ConfigReader.getInstance().getLogLevel()        → System.setProperty("log.level", ...)
        └─ ConfigReader.getInstance().getLoggingDirectory() → System.setProperty("logging.directory", ...)
        │
        ▼
org.slf4j.LoggerFactory.getLogger(clazz)   (first call triggers Log4j2's one-time XML parse)
```

`setIfAbsent` semantics: a system property already supplied externally (e.g. `-Dlog.level=DEBUG` on the command line) is never overwritten — the Configuration Layer only fills in what wasn't already set. This must run **before** the first SLF4J logger is created, because Log4j2 parses `log4j2.xml` and resolves its `${sys:...}` lookups on that first touch; any bridge that ran later would be a no-op.

`src/test/resources/log4j2.xml` (real configuration, replacing the Phase 1 placeholder) defines:
- `Console` appender — always active, `SYSTEM_OUT`, pattern `%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n`
- `RollingFile` appender — writes to `${sys:logging.directory:-logs}/automation.log`, rolls daily or at 10MB, keeps 10 archives (`.log.gz`)
- `Root` level — `${sys:log.level:-INFO}`

No business-specific information is ever logged by any Phase 6 component — only framework/lifecycle events (test name, thread name, retry attempt, screenshot path, report init/flush).

## 3. Exception Hierarchy

```
java.lang.RuntimeException
        │
        ▼
FrameworkException (abstract — every throw site must use a concrete subtype)
        │
        ├── ConfigurationException        (config package — malformed/missing configuration)
        ├── DriverInitializationException (driver package — driver session lifecycle failures)
        ├── UtilityOperationException     (utils package — wrapped checked I/O failures)
        └── ReportingException            (reporting package — new this phase; e.g. getTest() with no active node)
```

`FrameworkException` is deliberately abstract so nothing is ever thrown as the bare base type — every catch site can distinguish exception categories precisely. Both constructors (`message`, `message + cause`) are preserved down the hierarchy so root-cause information is never dropped (no exception is swallowed by this phase's own code — the one deliberate exception to that rule, `ScreenshotManager`, is called out explicitly in §7).

## 4. Reporting Architecture

Mirrors the `DriverManager`/`DriverProvider` package-private-manager + public-facade pattern from Phase 4:

- **`ExtentReportManager`** (package-private) — owns a single `volatile ExtentReports` instance (double-checked-locking init, one per JVM) and a `ThreadLocal<ExtentTest>` for the in-progress test node per thread. Report file: `<report.directory>/AutomationReport_<yyyyMMdd_HHmmss>_<6-char-random>.html`, an `ExtentSparkReporter` themed `Theme.STANDARD`, with system info (Environment, Platform, Automation Engine, Execution Mode) sourced from `ConfigReader` — never business data.
- **`ReportProvider`** (public facade) — `initializeReport()`, `createTest(String)`, `getTest()`, `hasActiveTest()`, `clearCurrentTest()`, `flushReport()`, `isInitialized()`. Every method delegates 1:1 to `ExtentReportManager`; no other class may reference `ExtentReportManager` directly (compiler-enforced by package-private visibility).

This phase prepares reporting infrastructure only — no test result content is written to the report by any Phase 6 component itself; that begins once `TestListener` (built this phase, but not yet exercised against a real test) is wired into an actual TestNG run in a later phase.

## 5. Screenshot Flow

**`ScreenshotManager`** (`reporting` package) wraps `utils.ScreenshotUtility` (built Phase 5) for use by listener failure hooks:

```
TestListener.onTestFailure(result)
        │
        ▼
ScreenshotManager.captureScreenshot(namePrefix)
        │
        ▼
try { ScreenshotUtility.captureScreenshot(namePrefix) }   ← may throw (e.g. no active driver)
        │
   success ──► log INFO, return Optional.of(path)
   failure ──► log WARN with full cause, return Optional.empty()   (NEVER throws)
```

Screenshots are not attached to the report by any component built this phase — `TestListener` only calls `captureScreenshot`; attaching the returned path to an `ExtentTest` node is out of scope here.

## 6. Listener Lifecycle

```
SuiteListener.onStart(suite)      → LogManager (INFO) + ReportProvider.initializeReport()
        │
        ▼
   [for each test method, TestNG invokes:]
        │
   MethodListener.beforeInvocation → LogManager (DEBUG, start timestamp)
        │
   TestListener.onTestStart        → LogManager (INFO) + ReportProvider.createTest(name)
        │
   [test method executes — no framework code from this phase runs here]
        │
   TestListener.onTestSuccess/onTestFailure/onTestSkipped
        → LogManager, ReportProvider.clearCurrentTest()
        → onTestFailure additionally calls ScreenshotManager.captureScreenshot(...)
        │
   MethodListener.afterInvocation  → LogManager (DEBUG, duration)
        │
        ▼
SuiteListener.onFinish(suite)     → ReportProvider.flushReport() + LogManager (INFO)
```

`RetryAnalyzer` is orthogonal to this chain — TestNG consults `IRetryAnalyzer.retry(ITestResult)` immediately after a test method fails, before `TestListener.onTestFailure` fires, to decide whether to re-invoke the method at all.

## 7. Retry Lifecycle

`RetryAnalyzer implements org.testng.IRetryAnalyzer`. TestNG instantiates one instance per `@Test` method, so `retry()` can safely hold a per-method `attempt` counter as an instance field.

```
retry(ITestResult result):
    maxRetries = ConfigReader.getInstance().getRetryCount()   (read fresh every call — not cached)
    if maxRetries <= 0 or attempt >= maxRetries:
        return false
    attempt++
    log WARN "Retrying test '<name>' — attempt <n> of <maxRetries>."
    return true
```

Contains no business-specific retry logic (e.g. no special-casing of a particular test name or failure type) — purely a configuration-driven attempt counter, reusable across the entire eventual suite.

## 8. Allure — Extension Point Only

Allure is **not integrated** this phase, per the explicit instruction to prepare an extension point only. Investigation confirmed real architectural value would be minimal right now: Allure's TestNG integration (`io.qameta.allure.testng.AllureTestNg`) is itself a self-registering `ITestListener`/`ISuiteListener` — it does not need to be woven into `ReportProvider` or any Phase 6 class to function. If Allure is adopted later, the extension point is: add the `io.qameta.allure:allure-testng` dependency and register `AllureTestNg` alongside `SuiteListener`/`TestListener` in the TestNG XML `<listeners>` block — no change to any class built this phase would be required. No Allure dependency, interface, or package was added.

## 9. Dependency Diagram

```
                 ┌────────────┐        ┌──────────────┐
                 │ config     │        │ driver       │
                 │ (Phase 3)  │        │ (Phase 4)    │
                 └─────┬──────┘        └──────┬───────┘
                       │                       │
        ┌──────────────┼───────────────────────┼──────────────┐
        │              │                       │              │
        ▼              ▼                       │              ▼
   logging       reporting ◄─────────────────────┐        utils (Phase 5)
 (LogManager)  (ExtentReportManager,               │      (ScreenshotUtility)
       ▲        ReportProvider,                    │             ▲
       │        ScreenshotManager) ─────────────────────────────┘
       │              ▲                            │
       │              │                             (ScreenshotManager wraps
       │         listeners                           ScreenshotUtility)
       │      (RetryAnalyzer, SuiteListener,
       │       TestListener, MethodListener)
       │              │
       └──────────────┘
   (every Phase 6 class obtains its Logger via LogManager)

   exceptions: referenced by config, driver, utils, reporting (ReportingException) —
   a shared vocabulary, not a dependency edge in the diagram above.
```

No component built this phase imports anything from `pages`, `tests`, `runners`, `assertions`, `data`, `components`, or `models` — confirmed by inspection of every import statement written. Integration is exclusively with `config`, `driver` (indirectly, via `utils.ScreenshotUtility`), `utils`, and `exceptions`, exactly matching this phase's Integration Rules.

## 10. Validation Performed This Phase

| Requirement | Result |
|---|---|
| Framework builds successfully | `gradlew clean build` → BUILD SUCCESSFUL against the real `com.aventstack:extentreports:5.1.1` / `org.testng:testng:7.10.2` / `log4j-core:2.24.1` dependencies — no method-name guesses needed correction this time (`ExtentSparkReporter`, `Theme.STANDARD`, `.config()`, `setSystemInfo`, `createTest` all compiled as designed on first attempt) |
| Logging initializes successfully | **Empirically verified** via a throwaway program: `LogManager.getLogger(...)` bridged `log.level`→`INFO` and `logging.directory`→`logs` as system properties; an INFO message appeared on console AND in `logs/automation.log`; a DEBUG message was correctly suppressed by the `INFO` root level |
| Reports initialize successfully | **Empirically verified**: `ReportProvider.isInitialized()` false→true across `initializeReport()`; `createTest`/`getTest`/`hasActiveTest`/`clearCurrentTest` round-tripped correctly, including `getTest()` throwing `ReportingException` once no node was bound; `flushReport()` produced a real `.html` file in `reports/` |
| Retry analyzer reads configuration | **Empirically verified**: `ConfigReader.getInstance().getRetryCount()` returned the configured value (`1`); `RetryAnalyzer` instantiated cleanly |
| Screenshot Manager integrates correctly with ScreenshotUtility | **Empirically verified**: with no active driver on the thread, `captureScreenshot(...)` logged a WARN with the full `DriverInitializationException` cause and returned `Optional.empty()` — confirmed it never propagates the exception |
| Listeners register successfully | **Empirically verified** at the instantiation level: `SuiteListener`, `TestListener`, `MethodListener` all constructed without error. **Not** verified against a live TestNG suite run (would require actually executing tests, out of scope per "Do not validate with automation tests") |
| Framework builds successfully | See row 1 |

**Correction made during validation:** the first `log4j2.xml` replacement was written before the file had been read in this session, which silently failed and left the Phase 1 placeholder (Console-only) in place; `build/resources/test` still held the stale copy even after a Gradle rebuild until `processTestResources` was explicitly re-run. Caught by the empirical check itself (`logs/automation.log exists: false` on the first run) rather than assumed — the file was then correctly replaced and re-verified, confirmed by a second run producing a real `logs/automation.log` with rolling-file output.

## 11. Architectural Decisions Requiring Approval

- **TestNG dependency scope changed from `testImplementation` to `implementation`** in `build.gradle`. Required because `listeners` (main source set, per the frozen Project Bootstrap package list) hosts TestNG-coupled classes (`RetryAnalyzer implements IRetryAnalyzer`, etc.) that need TestNG at main-source compile time, not just test compile time. Also argued as the architecturally correct scope for a *framework* project, whose consumers need TestNG regardless. Confirm, or redirect toward moving these classes to a test-scoped location instead.
- **`ScreenshotManager` deliberately swallows exceptions** (logs and returns `Optional.empty()` rather than propagating) — in tension with the Exception Framework's "do not swallow exceptions" principle. Justified because a screenshot failure during failure-handling must never mask the real test failure that triggered it. Confirm this is an acceptable, narrowly-scoped exception to that rule.
- ~~**The `logging` package's status remains unreconciled**~~ **RESOLVED (Phase 8.10):** `logging` is now the 13th permanent top-level package — see MA-FR-001 §1's amendment and docs/framework/FOUNDATION_READINESS_RESOLUTION.md.
- **Allure was left as a documentation-only extension point** (§8), not a code-level interface or abstraction. Confirm this is sufficient, or redirect toward an actual (even if unused) extension seam if stronger future-proofing is wanted.

## 12. Future Extension Strategy

- **A new logger consumer** always calls `LogManager.getLogger(YourClass.class)` — never `LoggerFactory`/Log4j2 directly.
- **A new exception type** extends `FrameworkException` directly (not `RuntimeException`), placed in the package it's most relevant to (mirroring `ReportingException` living in `reporting`, not `exceptions`, if a package already owns the concern — note the current four concrete types all live in `exceptions` itself; a future type could follow either precedent, but should stay consistent with whichever is chosen).
- **A new listener** implements the relevant TestNG SPI interface in `listeners`, obtains its logger via `LogManager`, and touches only `ReportProvider`/`ScreenshotManager`/`ConfigReader` — never a Page Object or Test Class type, even transitively.
- **Attaching screenshots/logs to the Extent report** (deferred from this phase) would extend `TestListener`'s failure hook to call `ReportProvider.getTest().fail(...)`/`.addScreenCaptureFromPath(...)` — additive to `TestListener`, no change needed to `ExtentReportManager`/`ReportProvider`'s public surface.
- **Allure adoption** — see §8; additive via TestNG XML listener registration, no code change to this phase's classes.
- **Do not** let any component in `logging`, `reporting`, or `listeners` grow a dependency on `pages`, `tests`, `assertions`, or `data` when those layers are built — if a future Base Framework class needs richer reporting/logging behavior, that behavior is added to `BasePage`/`BaseTest` calling these public facades, not by these facades reaching upward into business layers.
