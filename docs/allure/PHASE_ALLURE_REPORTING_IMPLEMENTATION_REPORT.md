# Allure Reporting Implementation Report

**Date:** 2026-08-14
**Framework version at implementation time:** 1.0.0 (`build.gradle`)
**Author of this report:** Claude Code, on request of the repository owner
**Status:** Implementation complete and validated. Not committed, not pushed, not tagged, no CI triggered — awaiting review per the phase's own stop rule.

---

## 1. Objective

Add Allure Report as a **second, additive** reporting mechanism to the existing TestNG-based mobile automation framework, without:
- replacing or degrading ExtentReports (the framework's existing reporting mechanism, MA-FA-001 §15),
- redesigning the framework's architecture, execution model, or Page Object structure,
- modifying Phase 19's true-parallel Native/Docker GitHub Actions CI architecture,
- beginning any Jenkins work.

Success is defined as: real Allure result files and a real, inspected Allure HTML report generated from the existing TestNG suite, with ExtentReports still functioning correctly afterward — not merely "dependencies added, build passes."

## 2. Existing Reporting Architecture (Pre-Implementation Audit)

Audited before any code was changed:

| Component | File | Role |
|---|---|---|
| `ExtentReportManager` | `src/main/java/.../reporting/ExtentReportManager.java` | Owns the single JVM-wide `ExtentReports` instance, builds one `ExtentSparkReporter` HTML file per run under `reports/AutomationReport_<timestamp>_<random>.html` |
| `ReportProvider` | `src/main/java/.../reporting/ReportProvider.java` | Thread-local-safe accessor (`createTest`/`getTest`/`clearCurrentTest`/`hasActiveTest`) sitting between the listener/assertion layer and `ExtentReportManager` |
| `ScreenshotManager` | `src/main/java/.../reporting/ScreenshotManager.java` | Captures PNG screenshots to `reports/screenshots/`; explicitly documented as **not** attaching to any report — attachment was out of scope before this phase |
| `TestListener` | `src/main/java/.../listeners/TestListener.java` | TestNG `ITestListener`; drives `ReportProvider.createTest`/`clearCurrentTest` and captures a failure screenshot on `onTestFailure` |
| `CommonAssertions` | `src/test/java/.../assertions/CommonAssertions.java` | Single centralized `evaluate()` method every framework assertion (`verifyVisible`, `verifyHidden`, etc.) flows through; logs to SLF4J and to the active `ExtentTest` |
| `BaseTest` | `src/main/java/.../core/BaseTest.java` | `@Listeners({SuiteListener.class, TestListener.class, MethodListener.class})`; no `testng.xml` — Gradle's TestNG integration auto-discovers `@Test` methods |

No screenshot-attachment mechanism existed anywhere in the reporting/listener packages prior to this phase (confirmed by grep across both packages) — Allure's attachment capability is net-new, not a duplicate of something ExtentReports already did.

## 3. Allure Integration Design

Allure was layered on **exactly** the same lifecycle points ExtentReports already uses, rather than introducing a parallel lifecycle:

- **Result writing**: handled automatically by `allure-testng`'s `AllureTestNg` TestNG listener, which registers itself via `META-INF/services` (Java `ServiceLoader`) — **zero changes** to `BaseTest`'s existing `@Listeners` annotation were needed or made.
- **Assertion-level pass/fail**: `CommonAssertions.evaluate()` calls `Allure.step(message, Status.PASSED/FAILED)` immediately alongside its existing `ExtentTest.pass()/fail()` calls — same method, same call site, additive.
- **Screenshot attachment**: `CommonAssertions.evaluate()` and `TestListener.onTestFailure()` each gained one small private helper (`attachScreenshotToAllure`) that reads the bytes of the screenshot `ScreenshotManager` already captured and calls `Allure.addAttachment(...)`. No second screenshot-capture mechanism was created.
- **Metadata**: `@Epic`/`@Feature`/`@Story`/`@Severity` and `@Step` are pure annotations read by the `allure-testng` adapter at result-write time; they require no runtime wiring.

## 4. Dependency Changes

`build.gradle`:
```groovy
plugins {
    id 'java'
    id 'io.qameta.allure' version '4.1.0'
}
```
```groovy
dependencies {
    ...
    testImplementation platform('io.qameta.allure:allure-bom:2.35.3')
    testImplementation 'io.qameta.allure:allure-testng'
}
```

Versions were verified against the current official documentation before use (`https://allurereport.org/docs/integrations-gradle/`, `https://plugins.gradle.org/plugin/io.qameta.allure-report`) rather than copied from an older tutorial. Gradle 8.11+/Java 17+ is required by plugin v4.1.0; this project already runs Gradle 9.0.0 / Java 17, so no toolchain change was needed.

Both the plugin's own automatic TestNG-adapter wiring **and** the explicit `allure-testng`/`allure-bom` dependency were required — the plugin wires result-writing into the `test` task automatically, but compile-time access to the annotations (`@Epic`, `@Step`, etc.) needed the explicit dependency. This was discovered empirically after finding a genuine inconsistency between two official-looking Allure documentation pages.

## 5. Gradle Configuration

- `src/test/resources/allure.properties` (new file):
  ```properties
  allure.results.directory=build/allure-results
  ```
  This matches the plugin's own default; made explicit for clarity, not because the default was wrong.
- No changes to the existing `test { }` block (`useTestNG()`, `failOnNoDiscoveredTests`, `systemProperties(System.properties)`) — the plugin hooks into the existing `test` task without requiring modification of it.
- Two new tasks became available from the plugin, used only for validation, never modified: `allureReport` (static HTML to `build/reports/allure-report/allureReport/`) and `allureServe` (generates + serves via a locally-bound HTTP server, `build/reports/allure-report/allureServe/`).

## 6. TestNG Integration

Confirmed empirically, not assumed: `allure-testng`'s `AllureTestNg` listener registers itself via `ServiceLoader`/`META-INF/services` with **zero** changes to `BaseTest`'s existing `@Listeners({SuiteListener.class, TestListener.class, MethodListener.class})` line. Both validation runs (Section 13) produced correct Allure results with this registration mechanism completely untouched.

## 7. Metadata Strategy

Applied to exactly one representative class (`LoginTest`), using the project's real, pre-existing test-domain language rather than placeholders:

```java
@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseTest {

    @Test(description = "TC-004 — Login Outcome Verification")
    @Story("Valid Login")
    @Severity(SeverityLevel.CRITICAL)
    public void loginOutcomeVerification() { ... }
}
```

This models `Epic > Authentication > Login > Valid Login` as required. No other test class was annotated in this phase — deliberately kept to a small, representative set rather than a mass rollout, per the phase's own instruction. Extending this pattern to the framework's other 18 automated test cases is future work, not part of this phase.

## 8. Step Strategy

`@Step` was applied only to `LoginPage`'s meaningful, reusable user actions — not to every line:

```java
@Step("Enter username: {username}")
public void enterUsername(String username) { ... }

@Step("Enter password")
public void enterPassword(String password) { ... }

@Step("Tap Login button")
public void tapLogin() { ... }

@Step("Log in with username: {username}")
public void login(String username, String password) { ... }
```

`ElementActions` (the lowest-level click/type/getText layer) was deliberately **not** annotated — every interaction there would have produced step-level noise the phase explicitly warned against. Other `LoginPage` methods (`getUsernameFieldValue`, `isUsernameErrorDisplayed`, etc.) were left unannotated for the same reason.

**Known limitation, honestly documented:** `@Step`'s parameter capture is not limited by what the name template interpolates. Even though `@Step("Enter password")` does not interpolate `{password}` into the displayed step name, Allure still records **all** method arguments in a `parameters` array. Confirmed directly in a raw result JSON from Validation 1:
```json
{"name":"password","value":"10203040","masked":false}
```
This is safe in the current pilot only because the AUT's demo credentials are already public test data — it is not safe by design. No `@Step` argument-masking was configured in this phase (that would require deciding a masking convention across the framework, which is out of this phase's scope). **If real credentials are ever used with `@Step`-annotated methods, masking must be added first.**

## 9. Screenshot / Attachment Strategy

No new screenshot-capture code was written. Both integration points read the *already-captured* file from `ScreenshotManager` and attach its bytes:

- `CommonAssertions.evaluate()` — on assertion failure, after the existing `ExtentTest.fail(message, screenshot)` call.
- `TestListener.onTestFailure()` — covers failures raised outside an assertion (e.g., a raw `ElementActionException`) that `CommonAssertions` never sees.

Both call `Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), ".png")` after `Files.readAllBytes(screenshotPath)`. Verified in Section 13 that the resulting attachment file on disk is byte-identical in size to the source screenshot (`contentLength` in the processed report JSON exactly matched the raw PNG's file size).

## 10. ExtentReports Compatibility

No line of `ExtentReportManager.java`, `ReportProvider.java`, or `ScreenshotManager.java` was changed. `CommonAssertions.evaluate()` and `TestListener` keep their original ExtentReports calls; the Allure calls were added alongside them, never in place of them. Regression-verified in Section 14 against the same Validation 2 run.

## 11. Parallel Execution Compatibility

Two distinct levels were examined, per the phase's explicit instruction to do a **lightweight** check rather than a full Phase 19 revalidation:

- **Within a single test process (local or one CI job):** `build.gradle`'s `test { }` block has no TestNG `parallel=`/thread-count configuration. Every Validation 2 result JSON independently confirms this — all 7 carry the identical thread label `"22808@DESKTOP-OIQ0753.Test worker(1)"`. Tests run sequentially in one thread; there is no concurrent-write risk to `build/allure-results` locally.
- **Across Phase 19's actual parallelism:** `.github/workflows/mobile-automation.yml` defines `native-tests` and `docker-tests` as two separate jobs, each `runs-on: ubuntu-24.04`, with no `needs:` between them (confirmed by direct inspection — not re-run). Separate GitHub-hosted runners mean separate, non-shared filesystems: even though both jobs would each produce their own `build/allure-results` directory, those directories exist on physically different machines. Cross-job Allure result-file collision is structurally impossible under the current architecture, not merely unobserved.

**Documented limitation:** this confirms the two parallel CI jobs cannot corrupt or collide with each other's Allure results. It does **not** mean a unified, single Allure report currently exists for a CI run — no artifact-upload/merge step was added (that would be a CI workflow change, explicitly out of scope unless proven necessary, and none was proven necessary for this phase's goal). If a single combined report across both jobs is wanted later, that is a distinct, separate piece of work.

## 12. Docker Compatibility

No Dockerfile or `.dockerignore` change was made, and none was needed: the Docker image (`Dockerfile`) contains only the Java/Gradle harness (Model 3 architecture) and bind-mounts the repository at `/workspace`; the same `./gradlew test` invocation used natively is used inside the container, and Allure is wired into that same `test` task. Confirmed via `git status` that neither file appears in the changeset.

## 13. Validation Results

**Validation 1 — single representative test** (`LoginTest.loginOutcomeVerification`): passed after resolving a local Windows Appium/ADB session-creation contention issue (Section 18) unrelated to the Allure code changes. Allure result JSON inspected directly: correct name, `passed` status, full nested `@Step` tree (`Log in with username: ...` → `Enter username`/`Enter password`/`Tap Login button`), Epic/Feature/Story/Severity labels present.

**Validation 2 — 7-test representative subset**, spanning all four existing test classes:
```
LoginTest.loginOutcomeVerification
NavigationTest.accessDrawerItems
ProductDetailsTest.addProductToCart
ProductDetailsTest.adjustProductQuantity
ProductDetailsTest.scrollToProductHighlights
ProductDetailsTest.selectProductColor
CartTest.accessCartScreen
```
Result: **5 passed, 2 genuinely failed** (`BUILD FAILED in 14m 40s` — not manufactured; the background-task wrapper's own "exit code 0" summary was misleading and was not relied on, per Section 17):

```
NavigationTest.accessDrawerItems               FAILED  (AssertionError, NavigationTest.java:52)
ProductDetailsTest.scrollToProductHighlights    FAILED  (AssertionError, ProductDetailsTest.java:242)
```

Exactly 7 Allure `*-result.json` files were generated, one per test executed — no gap, no duplicate.

## 14. Failure Validation (Detailed)

Both real failures were inspected directly in the raw result JSON, not assumed correct from a green build:

| | `NavigationTest.accessDrawerItems` | `ProductDetailsTest.scrollToProductHighlights` |
|---|---|---|
| `status` | `failed` | `failed` |
| `statusDetails.message` | `verifyVisible [Product Catalog screen]: expected to be visible` | `verifyVisible [Product Catalog screen]: expected to be visible` |
| `statusDetails.trace` | Full Java stack trace present, correct file/line (`NavigationTest.java:52`) | Full Java stack trace present, correct file/line (`ProductDetailsTest.java:242`, called from `.java:211`) |
| Matching failed `step` | Present, name matches the assertion message | Present, name matches the assertion message |
| Screenshot attachment | `42c14ada-...-attachment.png` referenced | `c9561485-...-attachment.png` referenced |
| Attachment file verified on disk | Yes — real PNG, 1080×1920, 50,785 bytes | Yes — real PNG, 1080×1920, 410,040 bytes |

Both failures share the same root assertion ("Product Catalog screen" visibility after a drawer/navigation action) — consistent with the framework's already-documented, pre-existing intermittent AUT/navigation timing behavior (Phase 19.4M/P), not a defect introduced by this phase. No framework or AUT code was modified to force these to pass.

## 15. Allure Report Evidence (Generated Report, Not Just Results)

Per the phase's explicit instruction not to claim success from a Gradle exit code alone, the actual generated report was inspected:

- `./gradlew allureServe` was run; confirmed a live HTTP listener on `http://localhost:50770` (`netstat`) with an established client connection from the task's own auto-launched browser attempt.
- The Browser pane's `navigate` tool refused `http://127.0.0.1:50770/` as blocked by policy (same restriction encountered in Validation 1) — worked around, as before, by directly reading the report's own generated data files, which is exactly what the SPA would render.
- `build/reports/allure-report/allureServe/awesome/summary.json` (the processed report's own summary, matching the currently-served content) showed exactly the correct aggregate: `{"total":7,"passed":5,"failed":2}`, with all 7 tests correctly named and statused.
- Individual processed result files under `awesome/data/test-results/*.json` were inspected: `TC-028 — Navigation Drawer Item Access` shows `status: failed`, correct labels, a failed step, and a top-level attachment whose `contentLength` (50785 bytes) exactly matches the raw screenshot file's size on disk. `TC-004 — Login Outcome Verification` shows `epic: Authentication`, `feature: Login`, `story: Valid Login`, `severity: critical` — proving the metadata strategy (Section 7) reaches the final rendered report, not just the raw result file.
- **Known quirk, documented rather than chased further:** the server's root path (`/summary.json`, `/awesome/summary.json`) returned either stale data from an earlier `allureServe` invocation or a 404, while the underlying generated files on disk (which the server's own output directory contains) hold the correct, current data. This is consistent with `allureServe`'s bundled Node/Allure3 tooling behavior already observed to be non-trivial in Validation 1 (blank `file://` shell). Verification was completed via direct file inspection instead, matching the same rigor standard already established and accepted in Validation 1.

## 16. Storage Impact

Measured before/after, drive-level and component-level:

| Drive | Free before (read-only verification response, same session) | Free now | Net |
|---|---|---|---|
| C: | 5.41 GB | 7.64 GB | **+2.23 GB** (other, unrelated activity freed more than Allure consumed) |
| D: | 12.06 GB | 12.06 GB | 0 |
| E: | 15.18 GB | 14.71 GB | **−0.47 GB** |

Allure-specific footprint, measured directly (not inferred from the drive delta, since other activity occurred in the same window):

| Component | Location | Size |
|---|---|---|
| `allure-bom`/`allure-testng`/adapter/report-plugin jars | `C:\Users\DELL\.gradle\caches\modules-2\files-2.1\io.qameta.allure*` | 7.77 MB |
| Bundled Node.js runtime (used by `allureReport`/`allureServe`) | `C:\Users\DELL\.gradle\caches\modules-2\files-2.1\org.nodejs` | 38.99 MB |
| Gradle artifact-transform cache (Allure-related) | `C:\Users\DELL\.gradle\caches\9.0.0\transforms\*` | 0.78 MB |
| `build/allure-results` (raw results, this project) | `E:\...\mobile-automation-framework\build\allure-results` | 0.91 MB |
| `build/reports/allure-report` (generated static + served report) | `E:\...\mobile-automation-framework\build\reports\allure-report` | 7.71 MB |
| **Total measured** | | **≈ 56.2 MB** |

All of this lives in already-gitignored locations (`build/` is excluded; the Gradle cache is outside the repository entirely) — nothing here will ever be committed. This is a small, one-time, precisely-bounded footprint, not an open-ended growth risk.

## 17. Files Changed

- `build.gradle` — added `io.qameta.allure` plugin + `allure-bom`/`allure-testng` test dependencies.
- `src/test/resources/allure.properties` — **new file**, explicit results-directory declaration.
- `src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java` — added `Allure.step(...)` calls and a screenshot-attachment helper inside the existing `evaluate()` method.
- `src/main/java/com/mobileautomation/framework/listeners/TestListener.java` — added a screenshot-attachment helper called from the existing `onTestFailure()`.
- `src/test/java/com/mobileautomation/framework/pages/LoginPage.java` — added `@Step` to 4 methods.
- `src/test/java/com/mobileautomation/framework/tests/LoginTest.java` — added `@Epic`/`@Feature`/`@Story`/`@Severity`.

## 18. Files/Areas Deliberately NOT Changed

`ExtentReportManager.java`, `ReportProvider.java`, `ScreenshotManager.java`, `BaseTest.java`, `ElementActions.java`, all Page Objects other than `LoginPage`, all Test classes other than `LoginTest`, `Dockerfile`, `.dockerignore`, `.gitignore` (confirmed already sufficient — `build/`, `.gradle/`, `reports/`, `screenshots/` already excluded), `.github/workflows/mobile-automation.yml`, any Jenkins file (none exist in this repo yet).

## 19. Errors Encountered During Validation (Environment, Not Framework/Allure Defects)

1. **Appium `SessionNotCreatedException` during Validation 1** (recurred twice): Appium's internal ADB module repeatedly failed to restart its own `adb.exe` (exit `0xC0000142`/`3221225794`), traced to leftover `adb.exe` processes from earlier manual diagnostic `adb devices` calls made in this same session, conflicting with Appium's own ADB lifecycle management. **Root cause and fix:** killed both `node.exe` (Appium) and `adb.exe` fully, restarted `adb.exe` alone first and confirmed it was stable via `adb devices`, then started Appium fresh against the already-stable `adb`. This is a local Windows process-ordering artifact, unconnected to any code changed in this phase.
2. **Background-task notification said "completed (exit code 0)" for Validation 2 while the actual Gradle log showed `BUILD FAILED`.** The task wrapper's own summary text was not authoritative; the real log tail (`2 failed`, `BUILD FAILED in 14m 40s`) was read directly before drawing any conclusion. This is a process/tooling nuance worth remembering, not a defect in the framework or Allure.

## Final Verdict

**A. FULLY IMPLEMENTED AND VERIFIED.**

- Allure was integrated additively; ExtentReports was not touched and was regression-verified working on the identical Validation 2 run (Section 10 confirms untouched source; the same run's ExtentReports HTML — `reports/AutomationReport_20260814_165445_9zpL15.html` — contains all 7 test names and both pass/fail content).
- Real Allure results were generated for two separate validation runs and directly inspected (not assumed from a green build) — including two genuine, non-manufactured failures with correctly captured status, message, stack trace, and screenshot attachment.
- A real Allure HTML report was generated (`allureReport`/`allureServe`) and its actual materialized content was inspected and cross-checked against the raw results (matching statuses, matching attachment byte sizes, correct Epic/Feature/Story/Severity metadata) — not accepted merely because the Gradle task returned exit code 0.
- Parallel-execution safety was reasoned through and verified structurally at both the local (no thread parallelism exists) and CI (isolated runners, no shared state) levels, without running a full Phase 19 revalidation.
- Docker/CI compatibility required no changes, and none were made.
- Storage impact is small (≈56 MB) and fully accounted for, entirely in already-gitignored locations.
- Repository changes are limited to exactly the files listed in Section 17, confirmed via `git status`/`git diff --stat`/`git diff --check` (clean; only pre-existing CRLF line-ending warnings, no actual whitespace errors).

**Not committed, not pushed, no tag, no CI triggered.** Per the phase's own stop rule: no Jenkins, BrowserStack/Sauce Labs/Grid, iOS, CI redesign, release tagging, or full Phase 19 multi-run revalidation was performed or is recommended as an automatic next step — awaiting explicit direction.
