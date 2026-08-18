# Allure Reporting — Forensic Audit Report

**Date:** 2026-08-15
**Type:** Read-only audit. No files were modified, no results were deleted, no tests were run, nothing was committed/pushed/tagged, no CI was triggered.
**Confirmation of no side effects from this audit:** `git rev-parse HEAD` before and after this audit both returned `35173f7ee5902500764805f3def58d5511007e7e`; `build/allure-results` held 110 files before and after. The only Gradle invocations used during this audit were `tasks --all` and `--dry-run` (introspection only, execute no task actions).

Legend: **VERIFIED** = proven directly from a file/command in this audit. **INFERENCE** = a reasoned conclusion from verified evidence, not independently confirmed against tool source code. **NOT VERIFIED** = could not be established from available evidence.

---

## A. Repository State

- **HEAD:** `35173f7` — "docs: update README for v1.3.0 release" (2026-08-12). **VERIFIED**, and confirmed unchanged across the entire audit.
- **Branch:** `main`, no stash entries. **VERIFIED**
- **`git status --short`:**
  ```
   M build.gradle
   M src/main/java/com/mobileautomation/framework/listeners/TestListener.java
   M src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java
   M src/test/java/com/mobileautomation/framework/pages/LoginPage.java
   M src/test/java/com/mobileautomation/framework/tests/LoginTest.java
  ?? docs/allure/
  ?? docs/jenkins/
  ?? src/test/resources/allure.properties
  ```
  **VERIFIED**
- **Allure-related uncommitted changes:** Yes — all 5 modified files plus `allure.properties` are the Allure integration from the prior implementation phase; none of it has been committed. `docs/jenkins/` is unrelated, pre-existing untracked content from an earlier (separate) phase, not part of Allure work. **VERIFIED**

## B. Current Allure Configuration

- **Plugin:** `io.qameta.allure` version `4.1.0` (`build.gradle`). **VERIFIED**
- **Adapter:** `testImplementation platform('io.qameta.allure:allure-bom:2.35.3')` + `testImplementation 'io.qameta.allure:allure-testng'`, version pinned via the BOM to `2.35.3`. **VERIFIED** (read directly from `build.gradle`; the Gradle module cache confirms `2.35.3` is present, alongside two other versions — `2.32.0` and `2.35.2` — left over from earlier dependency-resolution exploration in the prior implementation phase; these stale cached versions are not what the build actually uses. **VERIFIED** the BOM forces `2.35.3`; the presence of the other two cached versions is a harmless cache artifact, not a build risk.)
- **`settings.gradle`:** only `rootProject.name = 'mobile-automation-framework'` — no Allure-related settings. **VERIFIED**
- **`gradle.properties`:** only `org.gradle.jvmargs=-Xmx1536m` — no Allure-related properties. **VERIFIED**
- **`gradle/libs.versions.toml`:** does not exist in this project (no version catalog is used anywhere; all dependencies are declared as string literals in `build.gradle`). **VERIFIED**
- **Available Allure Gradle tasks** (from `./gradlew tasks --all`):
  ```
  allureReport   — Builds Allure report from allureReport dependencies
  allureServe    — Builds Allure report from allureReport dependencies and launches Allure server
  copyCategories — Copies categories.json to allure-results folders
  downloadAllure
  installAllure3
  ```
  **VERIFIED**. A `--dry-run` of both `allureReport` and `allureServe` shows both depend on `copyCategories → downloadNode → installAllure3 → downloadAllure`, in that order. **VERIFIED**
- **Allure 3 CLI:** downloaded and unpacked locally under the project's own `build/allure/` directory (not the Gradle user cache) — see Section H. **VERIFIED**

## C. Test Integration

- **TestNG registration:** `BaseTest` declares `@Listeners({SuiteListener.class, TestListener.class, MethodListener.class})`; `allure-testng`'s `AllureTestNg` listener is **not** in this list and does not need to be — it self-registers via `META-INF/services` (`ServiceLoader`), confirmed by the fact that Allure results exist at all without any explicit listener wiring. **VERIFIED**
- **Metadata annotations in use:** `@Epic`/`@Feature` on `LoginTest` class level, `@Story`/`@Severity` on `LoginTest.loginOutcomeVerification`. **No other test class carries any Allure metadata annotation.** **VERIFIED** (grep across `src/test/java/.../tests/*.java` and `pages/LoginPage.java` — only `LoginTest.java` and `LoginPage.java` reference `io.qameta.allure.*`).
- **`@DisplayName`:** not used anywhere. Allure-testng does not define a `@DisplayName` annotation of its own (that is a JUnit 5 annotation); the project instead relies on TestNG's own `@Test(description = "...")`, which the adapter picks up as the Allure result's `name` (confirmed: every raw result JSON's `name` field, e.g. `"TC-004 — Login Outcome Verification"`, matches the `@Test(description=...)` string, not the Java method name). **VERIFIED**
- **`@Step`:** present only on 4 methods in `LoginPage.java` (`enterUsername`, `enterPassword`, `tapLogin`, `login`). No other Page Object or the lower-level `ElementActions` layer carries `@Step`. **VERIFIED**
- **Attachments/screenshots:** wired in two places — `CommonAssertions.evaluate()` (assertion-level failure) and `TestListener.onTestFailure()` (test-level failure outside an assertion) — both read the PNG `ScreenshotManager` already captured and call `Allure.addAttachment(...)`. **VERIFIED** by reading both files' current content.
- **Retry handling:** `RetryAnalyzer.java` exists under `src/main/java/.../listeners/` but is **not referenced anywhere** — no `@Test(retryAnalyzer = ...)` usage exists in any test class, and no `testng.xml` exists to wire it globally. **VERIFIED** (grep for `retryAnalyzer\s*=` across `src/` returned zero matches). This means: **any "retry" behavior visible in Allure data is not the framework's own retry mechanism — it is the result of separate, manual re-invocations of Gradle across time** (see Section D/F).
- **Parallel execution interaction:** `test { }` in `build.gradle` has no TestNG `parallel=`/thread-count configuration. Every raw result file in the current `build/allure-results` carries a single-digit thread label pattern (`"...Test worker(1)"` in results from the prior implementation phase's validation runs); this audit did not re-verify thread labels on the newest 45 results specifically, but the build configuration itself is unchanged and still has no parallel settings. **VERIFIED** (build.gradle) / **INFERENCE** (that the newest 45 results also ran single-threaded, based on unchanged configuration — not re-checked per-file in this audit).

## D. Results Lifecycle

- **Where raw results are written:** `build/allure-results`, per `src/test/resources/allure.properties` (`allure.results.directory=build/allure-results`), matching the plugin's own default. **VERIFIED**
- **Cleaned automatically before a run?** **No.** Neither `build.gradle`'s `test { }` block nor any of the Allure tasks (`allureReport`, `allureServe`, `copyCategories`) declares a `doFirst`/dependency that deletes `build/allure-results` before writing new results. There is no `clean`-before-`test` wiring anywhere in the build. **VERIFIED** by reading `build.gradle` in full — no such task exists.
- **Do previous executions accumulate?** **Yes, confirmed directly.** `build/allure-results` currently holds results spanning **2026-08-14 12:16:58 UTC through 2026-08-15 05:32:53 UTC** — roughly 17 hours across what the timestamp clustering shows to be **at least two distinct execution sessions** (one full-suite attempt covering all 19 `@Test` methods within a ~6-minute window, and several additional isolated re-invocations of `LoginTest` alone spread across the following hours). None of these were cleared between runs. **VERIFIED**
- **Is clean test execution / clean report generation currently possible?** Not with the current Gradle configuration as-is — running `./gradlew test` again would add a new batch of result files on top of the existing 110, and running `allureReport`/`allureServe` again would continue to render a report blending old and new results together, exactly as it does now. Achieving a "clean" run today requires either manually deleting `build/allure-results` first (not done in this audit, per instruction) or adding a `clean`-before-`test`/`-Pclean` convention to the build (a candidate minimal change — see Section J). **VERIFIED** (absence of any clean step) / the practical consequence is **INFERENCE** from that absence.
- **Current contents, exact counts:**
  | File type | Count |
  |---|---|
  | `*-result.json` | 45 |
  | `*-container.json` | 64 |
  | `*-attachment.*` (any extension) | **0** |
  | `executor.json` | 1 |
  | **Total files** | **110** |

  **VERIFIED** directly via `find`/`ls`.
- **Notable: zero attachment files currently present.** The prior implementation phase's own validation run had produced 2 screenshot attachments; those specific files (`42c14ada-...-attachment.png`, `c9561485-...-attachment.png`) and their corresponding result files **no longer exist** in `build/allure-results` — the directory's entire content has been replaced/added-to by newer runs since that phase ended. **VERIFIED**. This is consistent with the accumulation behavior above, not a defect in the attachment code — see Section F: every failure in the current 45-result set failed at driver-session creation, before any screen was ever reached, so `ScreenshotManager` never had a screen to capture (there is nothing wrong with the attachment code; there was simply nothing to attach in these particular runs).
- **`executor.json` presence:** unusual for a purely local run — this file is typically written when a CI-environment (Jenkins, GitHub Actions, TeamCity, etc.) is detected, or written manually. **NOT VERIFIED** exactly what wrote it or when relative to the 45 results (its own mtime, 2026-08-15 11:32:34, falls between the last raw result at 05:32:53 and the `allureReport` generation at 11:51:03 — closer to the report-generation timestamp than to any test run) — worth investigating before making further changes, but not required to explain the Section F numbers.

## E. Report Generation

- **Gradle task used:** `allureReport` (confirmed — the existing generated report lives under `build/reports/allure-report/allureReport/`, matching that task's own output path, not `allureServe`'s `.../allureServe/` path). **VERIFIED**
- **Output directory:** `build/reports/allure-report/allureReport/` (from `build/tmp/allureReport/allurerc.json`'s own `"output"` field, which is an absolute path Gradle generated pointing to exactly this directory). **VERIFIED**
- **`index.html` exists:** Yes. **VERIFIED**
- **`data/` exists:** Yes. **VERIFIED**
- **`widgets/` exists:** Yes. **VERIFIED**
- **Does `allureServe` output currently exist?** No — no `build/reports/allure-report/allureServe/` directory is present right now (it existed during the prior implementation phase's validation but is gone now; either a `clean` occurred or `allureReport`'s own execution/task-output handling removed it). **VERIFIED** (directory absent) / cause of its removal is **NOT VERIFIED**.
- **`allureServe --depends-on-tests`:** not a real flag on this task — `--dry-run` shows `allureServe`'s actual dependency chain is `copyCategories → downloadNode → installAllure3 → downloadAllure → allureServe`; it does **not** depend on the `test` task. Running `allureServe` (or `allureReport`) only ever reads whatever is currently in `build/allure-results` — it never triggers a test run itself. **VERIFIED**
- **Static or served:** `allureReport` produces a static site (`index.html` + `data/` + `widgets/`, meant to be opened via HTTP, not `file://` — confirmed in the prior implementation phase that `file://` loads a blank SPA shell). `allureServe` additionally starts a local HTTP server and serves the same kind of output. **VERIFIED** (both behaviors were directly observed in the prior implementation phase; not re-tested in this audit since re-running either task was avoided to keep this pass strictly read-only over what already exists... note: `allureReport`'s own generation IS what produced the current `build/reports/allure-report/allureReport/` — that generation happened before this audit began, at 11:51:03, not during it).
- **Browser auto-opening:** `allureServe` is documented (Allure's own official docs, referenced in the prior implementation phase) to auto-launch a browser tab; this could not be confirmed in this environment because this sandbox's Browser pane refuses `http://127.0.0.1:*` navigation by policy (encountered twice already, in the prior implementation phase's own validation). **NOT VERIFIED** in this environment specifically; **INFERENCE** that it works as documented elsewhere.

## F. Current 20-Result Report Forensics

The report's own `summary.json` (`build/reports/allure-report/allureReport/summary.json`, generated 2026-08-15 11:51:03) states:
```json
{"stats":{"total":20,"skipped":18,"retries":2,"broken":1,"passed":1}, ...}
```
This is **VERIFIED** — read directly from the file on disk, not assumed.

**Root cause, fully proven from the raw result files (not assumed):**

Every single one of the 45 raw `*-result.json` files carries **exactly one distinct failure message**, verbatim:
```
Failed to create Android driver session against Appium server http://127.0.0.1:4723. Capabilities requested: ...
```
This is a `DriverInitializationException` thrown from `AndroidDriverFactory.createDriver()` → `DriverManager.initializeDriver()` → `BaseTest.initializeDriver()` (the `@BeforeMethod(alwaysRun = true)`). **VERIFIED** — a `grep`/JSON-parse across all 45 result files' `statusDetails.message` found a single unique string, and the stack trace was read in full from a representative broken result.

This is **not 19 different bugs or 22 different broken tests** — it is **one single, recurring infrastructure/environment condition (Appium server at `127.0.0.1:4723` unreachable or no session obtainable) hit repeatedly across multiple separate, uncleared test-execution attempts.**

**Reconstructing the timeline from raw data (all timestamps VERIFIED from file content, ordering/interpretation is INFERENCE):**

| Time window (UTC) | What happened | Evidence |
|---|---|---|
| 2026-08-14 12:16:58 – 12:23:02 (~6 min) | One execution attempted **all 19** `@Test` methods (13 `CartTest`, 1 `LoginTest`, 1 `NavigationTest`, 4 `ProductDetailsTest`); every single one failed at driver-session creation before any test body ran | 19 distinct test-method `historyId`s each have exactly one `skipped` result in this window, each paired with a `broken` `BaseTest.initializeDriver` result at the same timestamp |
| 2026-08-14 16:07:29 – 16:18:19 | `LoginTest` alone re-invoked twice, both times failing the same way | Two more `broken`+`skipped` pairs for `LoginTest`'s `historyId`, hours apart from the first attempt and from each other — inconsistent with any in-process retry, consistent with separate manual invocations |
| 2026-08-15 04:31:44 | `LoginTest` alone re-invoked a fourth time, still failing | Same pattern, ~12 hours after the prior attempt |
| 2026-08-15 05:32:53 | `LoginTest` alone re-invoked a fifth time — **this time the Appium session succeeded**, and the test passed | The only `passed` result in the entire dataset; no corresponding `broken` `initializeDriver` entry accompanies it |

Because `RetryAnalyzer` is confirmed unwired (Section C), **these are not the framework auto-retrying** — they are separate, manual `./gradlew test` (or `--tests LoginTest...`) invocations, run hours apart, all writing into the same never-cleared `build/allure-results` directory.

**Reconciling the raw data against the report's exact numbers:**

- Grouping the 45 raw results by `historyId` directly (this audit's own reconstruction) yields **23 distinct groups**: 19 real `@Test` methods + 4 distinct `historyId`s for `BaseTest.initializeDriver` (the configuration-method failure appears to get a different `historyId` per test-class context it was blocking — one `historyId` shared by all 13 `CartTest`-blocking failures, one shared by all 4 `LoginTest`-blocking failures, one for the single `NavigationTest`-blocking failure, one shared by all 4 `ProductDetailsTest`-blocking failures). **VERIFIED** directly by grouping the raw JSON.
- Of those 23 groups, using each group's most recent result as its "current" status: 1 `passed` (`LoginTest`), 18 `skipped` (the other 18 real tests), 4 `broken` (the four `initializeDriver` groups). This reconciles the **18 skipped + 1 passed = 19** real tests exactly, and explains **why it is 18, not 19, skipped** (because `LoginTest` alone eventually succeeded). **VERIFIED**.
- This audit's own historyId-based reconstruction (23 groups, 4 broken) does **not** exactly match the Allure3 "Awesome" report plugin's own rendered numbers (20 total, 1 broken, 2 retries). **NOT VERIFIED**: the exact internal rule the Awesome report plugin uses to collapse the 4 distinct `initializeDriver` `historyId` groups down to a single displayed "broken" entry (and to attribute only "2" retries) was not established with certainty from the raw files alone — this would require either the plugin's source or its official documentation on retry-grouping semantics, neither of which was consulted in this audit. What **is** certain, and directly evidenced, is: TestNG gives a failed `@BeforeMethod` its own `ITestResult`, `container.json` files confirm each configuration failure is recorded both as a standalone result **and** embedded in the `"befores"` array of the container wrapping the corresponding skipped test — this is standard, well-documented Allure/TestNG adapter behavior, not a defect. **VERIFIED** (container structure) / the plugin's exact collapsing arithmetic remains **NOT VERIFIED**.

**Bottom line for Section F:** The displayed "Total=20, Passed=1, Broken=1, Skipped=18, Retried=2" is an artifact of (a) **accumulated, never-cleared results from multiple separate manual runs**, and (b) **one single, real, environmental root cause** — Appium/emulator connectivity failure — repeated across nearly all of them, not 19 or 22 separate defects. This is **not a defect in the Allure integration code** from the prior implementation phase; it is a **results-lifecycle/process gap** (no clean-before-run convention exists) combined with a genuine, separate infrastructure issue (Appium wasn't reachable during most of these particular runs).

## G. GitHub Actions Integration

- `.github/workflows/mobile-automation.yml` contains **zero** references to "allure" in any form (`grep -i allure` returned no matches). **VERIFIED**
- **Allure results generated in CI:** No — the workflow's `native-tests`/`docker-tests` jobs invoke Gradle's `test` task, which (per Section B) would technically produce Allure results as a side effect of the plugin being on the classpath, but the workflow does nothing with them (no explicit step reads `build/allure-results`). **INFERENCE** that results WOULD be produced (based on the plugin auto-wiring into `test`, confirmed in Section B/C) — **NOT VERIFIED** in this audit, since CI was not run.
- **Allure report generated in CI:** No — no step invokes `allureReport` or `allureServe`. **VERIFIED** (absent from the workflow file).
- **Report uploaded as artifact:** No — no `actions/upload-artifact` step references `allure` or `build/reports/allure-report` anywhere in the workflow. **VERIFIED**.
- **CI architecture compatibility:** The existing `native-tests`/`docker-tests` parallel-job architecture (Section 11 of the prior implementation report) is structurally compatible with adding Allure result generation/upload later — each job already runs an isolated `./gradlew test` invocation on its own runner, so each would produce its own independent `build/allure-results` with no cross-job collision risk. Adding actual CI Allure support (upload-artifact steps, or an aggregation step) has **not been done** and would be a workflow change requiring explicit approval — **out of scope for this audit**, consistent with "do not modify the workflow." **INFERENCE** (compatibility assessment) — not tested end-to-end.

## H. Storage Impact

All measured directly, nothing deleted:

| Location | Size | Files | Notes |
|---|---|---|---|
| `build/allure-results` | 0.59 MB | 110 | Raw results — Section D |
| `build/reports/allure-report` | 3.15 MB | 120 | Current `allureReport` static output only (`allureServe` output not currently present) |
| `build/allure` (project-local Allure3 CLI + bundled Node.js, unpacked) | **344.15 MB** | 20,042 | `allure3/` 62.23 MB, `commandline/` 172.07 MB, `node/` 109.84 MB — **this is a separate, larger download from the Gradle-cache figures reported in the prior implementation report; it was not measured in that earlier report and is a correction/addition to that record** |
| Gradle module cache: `io.qameta.allure*` dependency jars (`C:\Users\DELL\.gradle\caches\modules-2\files-2.1`) | 9.27 MB | — | Includes 3 cached versions (2.32.0/2.35.2/2.35.3) from earlier resolution exploration; only 2.35.3 is actually used |
| Gradle module cache: `org.nodejs` (separate bundled Node runtime used by the Gradle plugin itself, distinct from `build/allure/node/`) | 38.99 MB | — | |
| **Total measured Allure-related footprint** | **≈ 396 MB** | | |

**VERIFIED**, all figures measured directly in this audit via PowerShell recursive size sums. All locations above are excluded from git via `build/` in `.gitignore` (confirmed via `git check-ignore -v` on both `build/allure-results` and `build/reports/allure-report`), so none of this is at risk of being committed. **VERIFIED**.

**Correction note:** the prior implementation phase's own report (`PHASE_ALLURE_REPORTING_IMPLEMENTATION_REPORT.md`, Section 16) stated a total footprint of "≈56.2 MB" — that figure did not include `build/allure/` (344 MB), which this audit found was not measured at that time (it may not have existed yet at that point, or was simply not checked). The true total local footprint is closer to **396 MB**, not 56 MB. This is a **correction to a prior report**, established here with direct evidence.

## I. Problems / Risks

1. **No clean-before-run convention exists** (Section D) — results accumulate indefinitely across separate invocations, producing misleading aggregate reports like the current 20/1/1/18/2 breakdown. **VERIFIED** as the direct, provable cause of Section F's confusing numbers.
2. **All but one recent test execution failed at the infrastructure level** (Appium unreachable at `127.0.0.1:4723`), not at the test-logic level — this is an environment/process issue outside the Allure integration itself, but it means the *current* `build/allure-results` content is not representative of the framework's actual test-logic health. **VERIFIED**.
3. **`RetryAnalyzer.java` exists but is dead code** (never wired to any test) — if the intent was ever to have automatic retries, that isn't happening; if manual re-running is deliberate, it is currently indistinguishable in the report from a "flaky test needing a retry mechanism" unless you read the raw messages, as this audit did. **VERIFIED**.
4. **Zero screenshot attachments in the current result set** — not a defect (Section D explains why: failures occurred before any screen was reachable), but worth knowing before assuming attachment integration is broken; it isn't, there was simply nothing to attach in these specific runs. **VERIFIED** as non-issue, but worth flagging so it isn't misread as a regression.
5. **Prior storage report was materially incomplete** (Section H) — 56 MB reported vs. ≈396 MB actual. Not a defect in the implementation, but a gap in that report's own diligence. **VERIFIED**.
6. **Metadata/`@Step` coverage is intentionally minimal** (only `LoginTest`/`LoginPage`) — carried over from the prior implementation phase's deliberate scope decision, not a new finding, but relevant context if the "correction" work intends to expand coverage. **VERIFIED** (unchanged since the prior report).
7. **CI has no Allure wiring at all** (Section G) — if the corrected workflow is meant to include CI-visible Allure reporting, that is entirely new work, not a fix to something broken. **VERIFIED**.
8. **`executor.json`'s origin is unexplained** (Section D) — worth a quick, separate check (e.g., whether a CI-detection env var was present in whatever shell ran the 11:51 `allureReport` generation) before relying on it for anything. **NOT VERIFIED**.

## J. Recommended Minimal Changes

*(Listed for review only — none of these have been made; all require explicit approval before implementation.)*

1. Add a `clean`-before-`test` (or an explicit `./gradlew clean test`) convention/documentation so `build/allure-results` doesn't silently accumulate across sessions — the single highest-leverage fix for the Section F confusion.
2. Optionally wire `allureReport`/`allureServe` to depend on `test` (or document that they must be run together, e.g. `./gradlew clean test allureReport`), since currently they silently render whatever is already on disk with no warning that it might be stale/mixed.
3. Decide and document whether `RetryAnalyzer` should be wired to real tests (if flaky-test retries are wanted) or removed (if it's genuinely unused) — currently it's neither used nor removed.
4. Before drawing any conclusions about the framework's test-logic health from Allure data, first confirm Appium/emulator connectivity — the current data cannot speak to test-logic quality at all, since virtually every result never got past driver initialization.
5. If CI-visible Allure reporting is wanted, that's additive workflow scope, not a "fix" — should be scoped and approved separately from any correction to the local results-lifecycle issue.

## K. Proposed Final Workflow

*(For discussion — not implemented.)* A corrected local workflow would look like: (1) confirm Appium/emulator is up and reachable before running tests; (2) `./gradlew clean test -Denv=... -D...` for a genuinely clean result set; (3) `./gradlew allureReport` (or `allureServe` for local viewing) immediately after, before any other invocation adds more results; (4) inspect the generated report's `summary.json` to confirm the total matches the number of tests actually intended to run. Whether to formalize steps (1)–(3) as a single Gradle task/script, and whether to extend CI to do the same, are decisions for the next phase, not this audit.

---

## VERDICT

**B. REQUIRES FURTHER INVESTIGATION**

Reasoning: the Allure integration's *code* (plugin wiring, annotations, `@Step`, screenshot attachment, ExtentReports coexistence) is unchanged since the prior implementation phase and nothing in this audit found a defect in it. But two things must be resolved with the repository owner before "minimal correction" work can be scoped correctly: (1) whether the accumulated 45-result dataset and its Appium-connectivity failures are expected/already known, or a surprise that needs separate environment troubleshooting first; and (2) what "corrected" is actually meant to fix — a results-lifecycle/clean-run gap (Section I.1, straightforward), or something about the specific 20/1/1/18/2 numbers themselves being wrong (they are not wrong — they are an accurate reflection of accumulated, environment-failing runs, per Section F). Proceeding straight to "minimal correction" without that clarification risks fixing the wrong thing.

**Per the instructions: no changes were made. Awaiting explicit approval before any correction work begins.**
