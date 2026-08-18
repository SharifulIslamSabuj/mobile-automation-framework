# Phase D — ExtentReports Forensic Audit

**Date:** 2026-08-17
**Type:** Audit only. No source file was modified. No test was executed. This report is the only file created.

---

## 1. Executive Summary

ExtentReports shows every test as "Passed" even when TestNG and Allure both correctly recorded a real failure. The root cause is proven, from four independent, mutually corroborating evidence sources (framework source code, the raw execution log, the actual generated ExtentReports HTML content, and the raw Allure result JSON for the same run): **`TestListener.onTestFailure()` never calls `ExtentTest.fail(...)` (or any status-setting method) on the active Extent test node.** It only handles screenshot capture and Allure attachment, then discards the thread-local reference. Since `CommonAssertions.evaluate()` is the *only* other place in the framework that ever calls `.fail()`/`.pass()` on an `ExtentTest`, any failure that occurs **outside** a `CommonAssertions.verifyXxx()` call — such as a raw `ElementActionException` thrown directly from a Page Object method — is never reported to Extent at all. The Extent node is left holding only whatever `.pass()` calls happened to succeed before the crash, and Extent's own rollup logic correctly (from its own, incomplete, perspective) computes "Passed," because that is the only status information it was ever given.

This is not a TestNG defect, not a listener-registration defect, not a thread-safety defect, and not an Allure defect — Allure captures the failure correctly because `allure-testng` observes TestNG's own `ITestResult` directly and independently, a mechanism `TestListener`/ExtentReports does not have.

## 2. Current Symptom

Reported by the user, reproducible across at least two separate full-suite executions:

| Run | TestNG/Gradle | Allure | ExtentReports |
|---|---|---|---|
| Latest | 19 total, 18 passed, 1 failed (`ProductDetailsTest.addProductToCart`) | 19 total, 18 passed, 1 broken | 19 total, **19 passed, 0 failed** |
| Previous | 19 total, 17 passed, 2 failed | 19 total, 17 passed, 2 broken | 19 total, **19 passed, 0 failed** |

## 3. Reproduction Evidence

Per the strict "do not run the full suite" rule, this audit used **existing artifacts already on disk** — no test was executed. `logs/automation.log` (3,668 lines) contains 5 concatenated suite-run blocks (Gradle/TestNG never clears this file between invocations, an already-established framework fact). The 5th and most recent block, `12:49:11.840`–`13:00:28.680`, contains exactly 19 `Test started`/`Test passed`/`Test failed` entries, 18 `passed`, 1 `failed` (`addProductToCart`) — this is the exact "latest execution" the user described, confirmed by test count, pass/fail count, and the specific failing test name.

## 4. TestNG Result Evidence

Directly from `logs/automation.log`, lines within the 12:49–13:00 block:
```
12:59:04.649  Test started: addProductToCart
12:59:28.339  [ERROR] Test failed: addProductToCart
```
`TestListener.onTestFailure(ITestResult result)` was invoked by TestNG — proven directly by this log line, since that exact log statement (`LOGGER.error("Test failed: {}", ...)`, `TestListener.java:45`) only executes inside that method. **TestNG's own `ITestResult` status was correctly `FAILURE` for this test, and TestNG correctly routed it to `onTestFailure`.** This rules out any defect in TestNG's own exception handling, listener dispatch, or `TestListener`'s method-selection logic.

## 5. Allure Result Evidence

`build/allure-results` still contains this exact run's 19 raw result files (timestamps `2026-08-17T06:49`–`07:00` UTC = `12:49`–`13:00` local, matching precisely). The `addProductToCart` result (`c2be84fb-0bc7-4f3d-bfdd-6344fc5d4707-result.json`):
```json
"status": "broken",
"statusDetails": {
  "message": "Element action 'getText' failed on locator: By.xpath: //*[@text='Sauce Labs Backpack (violet)']/parent::*/*[@content-desc='Product Price']",
  "trace": "com.mobileautomation.framework.exceptions.ElementActionException: ... \n\tat com.mobileautomation.framework.core.ElementActions.execute(ElementActions.java:181)\n\tat com.mobileautomation.framework.core.ElementActions.supplyFromVisible(ElementActions.java:151)\n\tat com.mobileautomation.framework.core.ElementActions.getText(ElementActions.java:46)\n\tat com.mobileautomation.framework.pages.ProductsPage.getCardPrice(ProductsPage.java:153)\n\tat com.mobileautomation.framework.tests.ProductDetailsTest.addProductToCart(ProductDetailsTest.java:67)\n...\nCaused by: org.openqa.selenium.TimeoutException: Expected condition failed: waiting for visibility of element located by By.xpath: ...(tried for 15 second(s) with 500 milliseconds interval)"
},
"steps": [{"name":"verifyVisible [Product Catalog screen]: expected to be visible","status":"passed"}]
```
`status: "broken"` is Allure's own correct terminology for "an unexpected exception occurred" (as opposed to `"failed"`, which Allure reserves for a deliberate assertion failure) — this is accurate Allure semantics, not a discrepancy with TestNG's own binary pass/fail. **Allure correctly captured both the failure and its full cause chain, entirely independent of anything `CommonAssertions` or `TestListener` did** — proven by the fact that the `steps` array contains only **one** entry (`verifyVisible [Product Catalog screen]`, the only `CommonAssertions` call that ran before the crash), yet the top-level `status` is still correctly `broken` — Allure's own listener (`AllureTestNg`, self-registered via `META-INF/services/org.testng.ITestNGListener` inside `allure-testng`'s own jar, confirmed via direct jar inspection in a prior phase) observes TestNG's `ITestResult` and its throwable **directly**, independent of the steps recorded during the test body.

## 6. ExtentReports Result Evidence

`reports/AutomationReport_20260817_124911_vR5Vow.html` — timestamp matches the 12:49:11 suite start exactly. Its embedded `statusGroup` data (read directly from the file, not the rendered UI):
```js
var statusGroup = {
parentCount: 5, passParent: 19, failParent: 0, ...
eventsCount: 5, passEvents: 279, failEvents: 0, ...
};
```
**The raw data itself says `failParent: 0` — this is not a UI/rendering bug.** The literal rendered HTML for the `addProductToCart` node:
```html
<p class="name">addProductToCart</p>
<span>12:59:04 PM</span> / <span>00:00:01:603</span>
<span class="badge pass-bg log float-right">Pass</span>
...
<h5 class="test-status text-pass">addProductToCart</h5>
...
<tbody>
    <tr class="event-row">
      <td><span class="badge log pass-bg">Pass</span></td>
      <td>12:59:06 PM</td>
      <td>verifyVisible [Product Catalog screen]: expected to be visible</td>
    </tr>
</tbody>
```
**The entire event table for this test node contains exactly one row** — the single `CommonAssertions.verifyVisible` call that succeeded before the crash. Nothing else was ever logged to this node: no failure, no exception, no error, no screenshot reference. The recorded duration (`00:00:01:603`) and end-timestamp (`12:59:06 PM`) reflect only the span between test start and that one logged event — **not** the true TestNG-measured wall-clock duration (`12:59:04.649`–`12:59:28.339`, ~23.7 s). This proves Extent's internal "last known state" for this test froze at the last explicit `.pass()`/`.fail()` call it ever received, 22 seconds before the test actually finished.

## 7. ExtentReports Architecture (Traced From Source)

`ExtentReportManager.java` (package-private, single owner of all Extent state):
1. **`ExtentReports` instantiated**: `buildExtentReports()`, lazily, on first `initializeReport()` call — `static volatile ExtentReports extentReports` field, double-checked-locking singleton (`synchronized (ExtentReportManager.class)`), one instance per JVM.
2. **`ExtentSparkReporter` instantiated**: inside `buildExtentReports()`, one new timestamped HTML file path per JVM run (`AutomationReport_<timestamp>_<random>.html`), attached via `reports.attachReporter(sparkReporter)`.
3. **`ExtentTest` created**: `createTest(String testName)` → `extentReports.createTest(testName)`, called only from `TestListener.onTestStart()`.
4. **Association with the running test**: via a `ThreadLocal<ExtentTest> CURRENT_TEST`, set in `createTest()`.
5. **`ThreadLocal` used**: yes, confirmed (`private static final ThreadLocal<ExtentTest> CURRENT_TEST`).
6. **Static variables used**: yes — `extentReports` itself is `static volatile`, shared across the whole JVM (by design — one report, many tests).
7. **`ExtentReports` shared between tests**: yes, intentionally — one report instance, one HTML file, many `ExtentTest` child nodes (one per test method).
8. **`ExtentTest` removed/cleared**: `clearCurrentTest()` → `CURRENT_TEST.remove()`, called from **all three** of `TestListener.onTestSuccess()`, `onTestFailure()`, `onTestSkipped()` — confirmed each path clears the thread-local reference (but, per Section 9, only `evaluate()`-driven failures ever set a `.fail()` status on it *before* that clearing happens).
9. **`ExtentReports.flush()` called**: `SuiteListener.onFinish(ISuite)` → `ReportProvider.flushReport()` — once per suite.
10. **Flush frequency**: once — confirmed by `SuiteListener`'s single `onFinish` callback per suite boundary, matching the log's one "Suite finished" line per suite block.
11. **Where `ExtentTest` is created**: exclusively in `TestListener.onTestStart()` — no other call site anywhere in `src/`.
12. **More than one reporting listener**: yes, two independent ones — `TestListener` (`ITestListener`, drives ExtentReports + Allure screenshot attachment on failure) and `allure-testng`'s self-registered `AllureTestNg` (drives Allure result writing). They do not interact with or depend on each other.

## 8. TestNG Listener Lifecycle (Traced From Source)

- **`onTestStart(result)`**: logs, then `ReportProvider.createTest(result.getName())` — creates the Extent node. No pass/fail status is set here.
- **`onTestSuccess(result)`**: logs, then `ReportProvider.clearCurrentTest()`. **Never calls `.pass()`, `.log()`, or any status-setting Extent API.** Relies entirely on whatever status the node already accumulated from `CommonAssertions` calls during the test body.
- **`onTestFailure(result)`**: logs the error (with `result.getThrowable()` — TestNG's own captured exception, proving TestNG did capture it correctly), captures a screenshot, attaches it to **Allure only** (`Allure.addAttachment(...)`), then `ReportProvider.clearCurrentTest()`. **At no point does this method call `ReportProvider.getTest().fail(...)` or any other Extent status API.** This is the exact, proven gap.
- **`onTestSkipped(result)`**: logs, clears. Same gap (not this audit's focus, but the same defect class applies).
- **`onFinish(ITestContext)`**: not implemented as an override in `TestListener` at all (only `onStart`/`onFinish` for the *class-level* logging are present; no per-context Extent interaction).
- **`@BeforeMethod`/`@AfterMethod`** (`BaseTest.java`): `initializeDriver()`/`quitDriver()` — driver lifecycle only, zero Extent/Allure interaction, cannot influence pass/fail status.
- **Can a failed test accidentally reach code that marks it PASS?** No "accidental" path exists — proven by exhaustive reading of all three status-relevant methods. The defect is not a wrong-branch bug; it is an **absence** — `onTestFailure()` simply never emits the corresponding `.fail()` call that `onTestSuccess()`'s implicit design assumes will have already happened via `CommonAssertions`.

## 9. Failure Path Trace (`ProductDetailsTest.addProductToCart`)

```
ProductDetailsTest.java:67  productsPage.getCardPrice(pilotProduct.name())   [NOT wrapped in CommonAssertions]
  → ProductsPage.java:153  ElementActions.getText(...)
    → ElementActions.java:46 → :151 → :181  execute(...) — wraps the Selenium call
      → org.openqa.selenium.support.ui.FluentWait.until(...) throws TimeoutException
        (15s wait, 500ms interval, element not found)
    ← ElementActions.execute() catches this and rethrows as ElementActionException
      (confirmed: "Caused by: org.openqa.selenium.TimeoutException" in the Allure trace — the
       exception IS caught and rewrapped here, but rethrown, never swallowed)
  ← ElementActionException propagates, UNCAUGHT, out of addProductToCart()
→ TestNG's own method-invocation harness catches it, sets ITestResult status = FAILURE,
  stores the throwable (result.getThrowable()) — proven by Section 4/8's log evidence
→ TestNG invokes TestListener.onTestFailure(result) — proven (Section 4)
  → LOGGER.error(...) — logs it
  → ScreenshotManager.captureScreenshot(...) — captures a screenshot (confirmed in log:
    "Screenshot captured: reports\screenshots\addProductToCart_failure_...png")
  → .ifPresent(TestListener::attachScreenshotToAllure) — attaches to ALLURE only
  → ReportProvider.clearCurrentTest() — discards the thread-local ExtentTest reference
    WITHOUT ever calling .fail() on it
→ Allure's own independent AllureTestNg listener separately observes the same ITestResult
  and correctly records status="broken" with the full trace (Section 5)
→ ExtentReports' node for this test retains only its one prior .pass() entry
  (Product Catalog screen) and is never told about the failure (Section 6)
```
**Where the exception is caught**: exactly once, inside `ElementActions.execute()` (per the Allure trace's `Caused by:` structure), where it is rewrapped as `ElementActionException` — **and rethrown**, not swallowed. **Where it reaches TestNG**: uncaught, propagating naturally out of the `@Test` method — this is the correct, intended behavior (`CommonAssertions`' own class Javadoc confirms failures are meant to throw so "TestNG's native pass/fail detection continues to work exactly as it does for `org.testng.Assert`" — and it does; TestNG's own detection is not at fault). **TestNG's `ITestResult` status is proven `FAILURE`** by the "Test failed" log line existing at all (only reachable from `onTestFailure`) and by Allure's independently-derived `status: "broken"` being sourced from that same `ITestResult`.

## 10. CommonAssertions Analysis

`CommonAssertions.evaluate()` (the single method every `verifyVisible`/`verifyHidden`/`verifyEnabled`/`verifyDisabled`/`verifyText`/`verifyContains` call funnels through) is the **only** place in the entire framework that calls `ExtentTest.pass()` or `.fail()`. On the pass branch: `ReportProvider.getTest().pass(message)` + `Allure.step(message, Status.PASSED)`. On the fail branch: `ReportProvider.getTest().fail(message[, screenshot])` + `Allure.step(message, Status.FAILED)` + `throw new AssertionError(message)`. **`CommonAssertions` cannot mark a test passed independently of reality** — its `.pass()` call only fires when the caller already computed a true boolean condition, and it always throws on a real failure (never swallows, never catches its own thrown `AssertionError`). **The defect is not inside `CommonAssertions` — it is the absence of any equivalent mechanism for failures that never reach `CommonAssertions` in the first place**, i.e., exceptions thrown directly from Page Object/`ElementActions` calls made outside a `verifyXxx()` wrapper (exactly `ProductsPage.getCardPrice()` → `ElementActions.getText()`, as traced in Section 9).

## 11. Exception Handling Analysis

Searched the framework's `try`/`catch`/`finally` usage around Selenium actions, assertions, test methods, and listener/reporting methods:
- `ElementActions.execute()`: catches the underlying Selenium exception and **rethrows** as `ElementActionException` (Section 9) — does not swallow, does not touch reporting.
- `CommonAssertions.evaluate()`: no `try`/`catch` around its own logic; it throws `AssertionError` directly on failure (after logging/reporting), never catches its own exception.
- `TestListener.attachScreenshotToAllure()`: has a `try { ... } catch (IOException e) { LOGGER.warn(...) }` — but this only guards the *screenshot file read*, and a failure here only logs a warning; it cannot mark a test passed, and does not touch `ExtentTest` at all.
- **No `finally` block anywhere in the framework calls `extentTest.pass(...)`.** This was searched for specifically per the phase's own instruction and not found — ruling out the "finally-block silently marks pass" hypothesis entirely.

## 12. Listener Registration Analysis

- `@Listeners({SuiteListener.class, TestListener.class, MethodListener.class})` — declared **exactly once**, on `BaseTest.java:27`. Confirmed via a repository-wide grep for `@Listeners` — this is the only occurrence.
- No `testng.xml` exists in this project (Gradle auto-discovers `@Test` methods; already an established fact from earlier phases).
- No `META-INF/services` file registers `TestListener`, `SuiteListener`, or `MethodListener` a second time — these are plain TestNG `@Listeners`-registered classes, not ServiceLoader-based.
- The **only** ServiceLoader-registered listener in this project's dependency graph is `allure-testng`'s own `AllureTestNg` (inside its jar, `META-INF/services/org.testng.ITestNGListener` → `io.qameta.allure.testng.AllureTestNg`, confirmed via direct jar inspection in the prior CLI-migration phase) — a **separate, independent** listener from `TestListener`, not a duplicate of it.
- **No duplicate listener instances or multiple `ExtentTest` creation paths exist.** `createTest()` has exactly one call site in the whole codebase (`TestListener.onTestStart`).

## 13. Thread/Parallelism Analysis

- `build.gradle`'s `test { }` block declares no `parallel`, `maxParallelForks`, or `forkEvery` configuration (confirmed via grep — zero matches).
- The 12:49–13:00 log block's every single line carries the identical thread tag `[Test worker]` — no other thread name appears anywhere in that run.
- **Execution is sequential, confirmed directly from this run's own log, not assumed.** `ExtentTest` storage via `ThreadLocal` means even if parallelism were introduced later, each thread would get its own isolated reference — but that is not the operative factor here; the defect reproduces identically under confirmed-sequential execution, so thread-safety is not a contributing cause.

## 14. Raw Extent Report Analysis — Summary

Already detailed in Section 6. Restated for directness: **the underlying Extent data structure itself, not just the rendered UI, says `addProductToCart` passed.** `statusGroup.failParent: 0` (aggregate) and the per-test HTML fragment's single, all-`Pass` event row (detail) agree completely. This rules out a rendering/template bug — the defect is in what gets *written* into the report, not how it's *displayed*.

## 15. Exact Status Divergence Point

| Layer | Status | Evidence |
|---|---|---|
| Selenium execution | `TimeoutException` (15s wait exceeded) | Allure trace `Caused by:` (Section 5) |
| Exception | `ElementActionException`, rethrown uncaught | `ElementActions.execute()` trace (Section 9) |
| TestNG `ITestResult` | `FAILURE` | Log: `[ERROR] Test failed: addProductToCart` (Section 4) |
| `TestListener` | Correctly invoked `onTestFailure()`; screenshot + Allure attachment performed; **no Extent status call made** | `TestListener.java:44-50` (Section 8) |
| `ExtentTest` (in-memory node) | Never told about the failure — retains only its one prior `.pass()` entry | `CommonAssertions.evaluate()` is the sole `.fail()` call site (Section 10); never invoked for this failure |
| Extent raw report data | `total pass`, node marked `text-pass`, one `Pass` event row | `AutomationReport_20260817_124911_vR5Vow.html` (Section 6) |
| Extent HTML summary | 19/19 passed | `statusGroup.passParent: 19, failParent: 0` (Section 6) |
| Allure result | `broken`, full trace captured | `c2be84fb-...-result.json` (Section 5) |

**The divergence occurs at the `TestListener` layer** — specifically, the transition from "TestNG correctly knows this failed" to "Extent is told about it." Every layer before `TestListener` is correct; every layer after inherits the gap.

## 16. Proven Root Cause

`TestListener.onTestFailure(ITestResult result)` (`src/main/java/com/mobileautomation/framework/listeners/TestListener.java`, lines 43-50) never calls a status-setting method (`.fail(...)`, `.log(Status.FAIL, ...)`, etc.) on the `ExtentTest` node returned by `ReportProvider.getTest()`, before that reference is cleared. `CommonAssertions.evaluate()` is the only other code path capable of marking an Extent node failed, and it is only reached when a test explicitly calls a `CommonAssertions.verifyXxx()` method. Any failure that reaches TestNG through an **unwrapped** call — a raw Page Object/`ElementActions` exception, exactly as occurred at `ProductDetailsTest.java:67` — is therefore never reported to Extent, leaving the node's accumulated status as whatever its last successful `.pass()` call recorded (or "Passed" by Extent's own default if no entries exist at all, though that specific edge case did not occur in this evidenced run — this test had one prior pass).

## 17. Root Cause Classification

**C. Incorrect `onTestSuccess`/`onTestFailure` implementation** — more precisely, `onTestFailure`'s implementation is *incomplete*, not incorrect in the sense of doing the wrong thing; it correctly handles Allure and screenshots but omits the equivalent, necessary Extent status call that its sibling method's design silently depends on `CommonAssertions` to have already provided.

This is a single, specific, well-bounded defect — not a case for "D. Multiple root causes." No evidence was found supporting categories A (TestNG lifecycle itself — proven correct), B (status overwritten — proven never set, not overwritten), D (exception swallowed before TestNG — proven rethrown, not swallowed), E (`CommonAssertions` defect — proven correct within its own scope), F (thread-safety — proven sequential, ThreadLocal correctly scoped), G (duplicate listener registration — proven single registration), H (ExtentReports API/version issue — see Section below), I (HTML/report generation issue — proven the raw data itself is wrong, not the rendering), or J (test execution itself incorrect — the test's own failure is genuine and correctly detected by every other layer).

## 18. ExtentReports Version / API Check

From `build.gradle`: `com.aventstack:extentreports:5.1.1`, `org.testng:testng:7.10.2`, `org.seleniumhq.selenium:selenium-java:4.25.0`, `io.qameta.allure:allure-testng` via `allure-bom:2.35.3`. The `ExtentTest.pass(...)`/`.fail(...)` API used by `CommonAssertions` is a standard, long-stable part of the ExtentReports 5.x public API — its correct behavior when called (Section 10) is directly evidenced by every prior *passing* assertion in this same report rendering correctly as `Pass`. **No version incompatibility is evidenced.** Per instruction, explicitly labeling this: **UNPROVEN / REQUIRES FURTHER VALIDATION** would apply only to a hypothesis this audit did not need and found no evidence for — there is no basis to suspect a version issue given the API behaves correctly for every call that is actually made.

## 19. No-Code Reproduction Analysis

No test was rerun. No temporary source file was created. All evidence in this report was extracted from three pre-existing artifacts already on disk before this audit began: `logs/automation.log`, `reports/AutomationReport_20260817_124911_vR5Vow.html`, and `build/allure-results/*.json` — plus direct reading of the four already-existing source files (`TestListener.java`, `CommonAssertions.java`, `ExtentReportManager.java`, `ReportProvider.java`, `SuiteListener.java`, `MethodListener.java`, `RetryAnalyzer.java`, `BaseTest.java`, `ProductDetailsTest.java`).

## 20. Minimal Recommended Fix (NOT IMPLEMENTED)

1. **File to modify**: `src/main/java/com/mobileautomation/framework/listeners/TestListener.java`.
2. **Exact method/area**: `onTestFailure(ITestResult result)`, lines 43-50.
3. **What is wrong**: the method never calls a failure-marking method on the active `ExtentTest` before clearing it.
4. **Minimal behavior needed**: inside `onTestFailure`, before `ReportProvider.clearCurrentTest()`, if `ReportProvider.hasActiveTest()` is true, call `ReportProvider.getTest().fail(result.getThrowable())` (ExtentReports' own API accepts a `Throwable` directly, which would also capture the actual exception message/stack trace in the report — richer than a plain string). This mirrors the exact pattern `CommonAssertions.evaluate()` already uses on its own failure branch, just triggered from the *test-level* catch-all rather than only from within an assertion.
5. **Why this makes ExtentReports match TestNG**: `result.getThrowable()` is the exact same throwable TestNG itself captured (proven in Section 4/9) — recording it against the Extent node makes Extent's status directly derived from TestNG's own authoritative result, closing the gap for every failure mode, not just this one exception type.
6. **Why it will not affect Allure**: `TestListener.onTestFailure` already handles Allure separately (screenshot attachment only — Allure's own status capture is independent, via `AllureTestNg`, Section 5); adding an Extent-only call does not touch any Allure API.
7. **Why it will not affect test execution**: `TestListener` is a pure observer (`ITestListener`); it runs after TestNG has already finalized the test's outcome — nothing in the proposed change alters control flow, timing, or the AUT interaction.
8. **Why it will not affect existing Extent features**: `CommonAssertions`' own `.fail()` calls (with screenshots, for failures that *do* go through an assertion) are unchanged; this only adds coverage for the previously-uncovered case. `.pass()` calls, `@Step` mirroring (none exists in Extent currently), and system-info reporting are all untouched.

**This audit does not implement this fix** — it is described only, per instruction.

## 21. Files That Would Need Modification

- `src/main/java/com/mobileautomation/framework/listeners/TestListener.java` — the only file identified.
- No other file (not `CommonAssertions.java`, not `ExtentReportManager.java`, not `ReportProvider.java`, not any Page Object, not any test class) requires any change for this specific, proven defect.

## 22. Allure Regression Risk

**None.** The proposed fix is confined to `TestListener.onTestFailure`'s Extent-specific call, additive to the method's existing Allure-attachment logic. It does not touch: Allure annotations (`@Epic`/`@Feature`/`@Story`/`@Severity`/`@Step`), the Allure listener (`AllureTestNg`, external to this codebase entirely), Allure result generation (`allure-testng`'s own writing to `build/allure-results`), the Allure Gradle plugin/tasks, or any file under `build/allure-results`/`build/allure-report`/`build/reports/allure-report`.

## 23. ExtentReports Regression Risk

**Low, and precisely bounded.** The only behavioral change is that tests which fail via an exception outside `CommonAssertions` would, for the first time, correctly show as failed in Extent — this is the intended fix, not a regression. Existing passing-test rendering, existing `CommonAssertions`-driven failure rendering (with screenshots), system-info panel, and report file naming/location are all untouched by the described (not implemented) change.

## Remaining Uncertainties

- The "previous execution" run (17 passed, 2 failed per the user's report) was not independently re-verified in this audit with the same file-level rigor as the "latest" run, since the immediately-preceding evidence (Section 3-6) already fully and independently proves the mechanism, and the phase's own instruction was to avoid unnecessary reruns/redundant work. The mechanism proven here (any exception outside `CommonAssertions`) is generic and would apply identically regardless of which specific test/line triggers it — but this specific second data point was not re-derived line-by-line.
- `onTestSkipped`'s equivalent gap was noted (Section 8) but not separately traced end-to-end with its own log/report evidence, since no skip occurred in the evidenced run and the phase's focus was the `addProductToCart` failure specifically.

## Final Verdict

**A. ROOT CAUSE PROVEN — READY FOR MINIMAL FIX**

The root cause is established from four independent, mutually corroborating sources (source code reading, the raw execution log, the actual generated ExtentReports HTML content — both aggregate `statusGroup` data and the literal per-test event table — and the raw Allure result JSON for the identical run), not assumed or inferred from a single angle. The minimal fix is precisely scoped to one method in one file, with no identified risk to Allure, test execution, or any other Extent feature.

**No file was modified during this audit. No fix was implemented. No commit, no push, no GitHub Actions or Docker change.** Stopping here per instruction — awaiting explicit approval before Phase E implementation.
