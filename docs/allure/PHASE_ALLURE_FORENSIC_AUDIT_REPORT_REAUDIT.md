# Allure Reporting — Forensic Audit Report (Re-Audit)

**Date:** 2026-08-15
**Type:** Read-only audit. No files were modified, no results were deleted, no tests were run, nothing was committed/pushed/tagged, no CI was triggered, during THIS audit pass.

**Important context this re-audit must state up front:** this is a **verbatim repeat** of the original Phase A forensic-audit request (see `docs/allure/PHASE_ALLURE_FORENSIC_AUDIT_REPORT.md`). Between that original audit and this one, an authorized **Phase B** ("Clean Allure Result Lifecycle + Local Validation") began and reached Part B before stopping: `build/allure-results` was **deliberately cleared** under Phase B's own explicit instruction, then Phase B **halted at Part C** because the required physical device (`10BDAT2Y9U000DF`) was not connected — no test was run, no report was regenerated. **The live system state has therefore materially changed since the original audit, specifically for Sections D, F, and H below.** Every finding here was re-verified against the system as it exists right now, not copied from the earlier report.

Legend: **VERIFIED** = proven directly from a file/command in this pass. **INFERENCE** = a reasoned conclusion from verified evidence, not independently confirmed against tool source. **NOT VERIFIED** = could not be established from available evidence.

---

## A. Repository State

- **HEAD:** `35173f7ee5902500764805f3def58d5511007e7e` — unchanged from the original audit. **VERIFIED**
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
  Identical file list to the original audit; `git diff --stat` on the 5 modified files shows the exact same line counts (18/20/22/5/9 insertions) — content has not drifted further. **VERIFIED**
- **Allure-related uncommitted changes:** Same as before — the full Allure integration (5 modified files + `allure.properties`) remains uncommitted. `docs/allure/` now additionally contains the original audit report and this re-audit; `docs/jenkins/` remains unrelated pre-existing content. **VERIFIED**

## B. Current Allure Configuration

No change since the original audit — `build.gradle`, `settings.gradle`, `gradle.properties` are byte-identical to before (confirmed via the diff-stat check above and a fresh re-read).

- **Plugin:** `io.qameta.allure` version `4.1.0`. **VERIFIED**
- **Adapter:** `allure-testng`, version pinned to `2.35.3` via `platform('io.qameta.allure:allure-bom:2.35.3')`. **VERIFIED**
- **`gradle/libs.versions.toml`:** does not exist. **VERIFIED**
- **Available Allure Gradle tasks** (re-run `./gradlew tasks --all` this pass): `allureReport`, `allureServe`, `copyCategories`, `downloadAllure`, `installAllure3` — identical set to the original audit. **VERIFIED**
- **Allure 3 CLI:** still installed locally under the project's own `build/allure/` (344.15 MB, unchanged — see Section H). **VERIFIED**

## C. Test Integration

Unchanged since the original audit (no test/source files were touched by Phase B before it stopped). Re-stating the verified facts rather than re-deriving them, since the underlying files are confirmed identical:

- `AllureTestNg` self-registers via `ServiceLoader`; `BaseTest`'s `@Listeners` list was not and does not need to be modified. **VERIFIED**
- `@Epic`/`@Feature` on `LoginTest` class, `@Story`/`@Severity` on `loginOutcomeVerification` — the only annotated test in the suite. **VERIFIED**
- `@Step` present on 4 `LoginPage` methods only. **VERIFIED**
- Screenshot attachment wired in `CommonAssertions.evaluate()` and `TestListener.onTestFailure()`. **VERIFIED**
- `RetryAnalyzer.java` exists, still referenced nowhere (`retryAnalyzer\s*=` still returns zero matches across `src/`). **VERIFIED**
- No TestNG parallel/thread configuration in `test { }`. **VERIFIED**

## D. Results Lifecycle — **materially different from the original audit**

- **Where raw results are written:** unchanged, `build/allure-results` per `allure.properties`. **VERIFIED**
- **Cleaned automatically before a run?** Still no — nothing in `build.gradle` or the Allure tasks does this automatically. **VERIFIED** (unchanged finding)
- **Current contents of `build/allure-results` right now: EMPTY.** `find build/allure-results -type f` returns **0**. This is not the framework's own behavior — it is the direct, deliberate result of Phase B Part B's authorized manual cleanup (`rm -rf build/allure-results/*`), executed and confirmed earlier in this same session. **VERIFIED**
- **Do previous executions still accumulate in this directory?** The accumulation *mechanism* is unchanged (still no auto-clean), but the *evidence* of accumulation that the original audit found (110 files spanning 2026-08-14–08-15) no longer exists on disk — it was cleared. If a new test run happens now, it will write into a genuinely empty directory for the first time in this project's observed history. **VERIFIED**
- **Is clean test execution / clean report generation now possible?** For the *results* side: yes, trivially — the directory is already empty, so the very next `./gradlew test` invocation would, for the first time, produce a dataset with nothing else mixed in. This has not been exercised yet (Phase B stopped before Part D — no test has run since the cleanup). **VERIFIED** (directory is empty) / **NOT VERIFIED** (no run has actually been done against the clean directory yet, so "clean execution is possible" is proven only up to the state of the input, not demonstrated end-to-end).

## E. Report Generation

Unchanged from the original audit at the *configuration* level (same tasks, same dependency chain: `copyCategories → downloadNode → installAllure3 → downloadAllure → allureReport`/`allureServe`, confirmed via `tasks --all`; `allureServe` does not depend on `test`). **VERIFIED**

**What is different now:** a generated report **still physically exists** on disk at `build/reports/allure-report/allureReport/` (`index.html`, `data/`, `widgets/` all present, 3.15 MB, 120 files) — but it was **not regenerated** during this session. It is the exact same report the original audit inspected, now **stale relative to the (now-empty) raw results directory that produced it.** **VERIFIED**

## F. Current 20-Result Report Forensics — re-verified, with a new caveat

The on-disk report's `summary.json` was re-read in this pass, fresh, not assumed carried-over:
```json
{"total":20,"skipped":18,"retries":2,"broken":1,"passed":1}
```
**VERIFIED** — identical to the original audit's finding, because it is literally the same unregenerated file; nothing about the report itself has changed.

The full root-cause analysis from the original audit (single `DriverInitializationException: Failed to create Android driver session against Appium server http://127.0.0.1:4723...` repeated across every one of the 45 raw results that produced this report; accumulation across ≥2 separate manual execution sessions spanning 2026-08-14 12:16 UTC–2026-08-15 05:32 UTC; `RetryAnalyzer` confirmed unwired so these were manual re-invocations, not automatic retries) **could not be re-verified against the raw files in this pass, because those raw files no longer exist** — they were the exact 45 files Phase B Part B deleted. This audit's confidence in that root-cause finding rests on the original audit's direct file inspection (performed while those files still existed) and is now **carried forward as established historical record**, not re-provable from currently-available evidence. **VERIFIED at the time it was originally established; the underlying raw evidence is no longer inspectable in this environment.**

**New finding this pass:** the report on disk and the results directory that would normally back it are now **out of sync** — `build/allure-results` is empty, but `build/reports/allure-report/allureReport/` still displays the old 20/1/1/18/2 aggregate as if it were current. Anyone opening this report right now (via `allureServe` or otherwise) would see stale data with no on-screen indication that the underlying raw results have since been cleared. **VERIFIED.**

## G. GitHub Actions Integration

Unchanged — re-checked `grep -i allure .github/workflows/mobile-automation.yml` this pass: **zero matches**, same as the original audit. No Allure result generation, report generation, or artifact upload exists in CI. **VERIFIED**

## H. Storage Impact — re-measured

| Location | Original audit | This re-audit | Change |
|---|---|---|---|
| `build/allure-results` | 0.59 MB / 110 files | **0.00 MB / 0 files** | **−0.59 MB** (Phase B Part B cleanup) |
| `build/reports/allure-report` | 3.15 MB / 120 files | 3.15 MB / 120 files | No change (not regenerated) |
| `build/allure` (Allure3 CLI + Node, project-local) | 344.15 MB | 344.15 MB | No change |
| Gradle module cache (`io.qameta.allure*`) | 9.27 MB | not re-measured this pass (no dependency resolution occurred; config unchanged, so this figure stands) | — |
| Gradle module cache (`org.nodejs`) | 38.99 MB | not re-measured this pass, same reasoning | — |

**VERIFIED** (the two re-measured rows) / carried forward unchanged for the two not re-measured (nothing occurred in this session that could plausibly have altered them — no dependency resolution or plugin-tooling download was triggered).

## I. Problems / Risks

All risks from the original audit still apply to the underlying *configuration* (no clean-before-run convention, `RetryAnalyzer` dead code, no CI wiring, minimal metadata coverage — none of these have changed). **One new, immediate risk specific to right now:**

- **Stale report / empty results mismatch (Section F).** If someone runs `allureServe` or opens the existing `build/reports/allure-report/allureReport/index.html` right now, they will see the old 20/1/1/18/2 data and could mistake it for current, when in fact the raw results backing it were deliberately cleared and no new run has happened since. This is a direct, immediate consequence of Phase B being paused mid-flight (stopped at Part C for the missing device) rather than a defect in the Allure integration itself. **VERIFIED**

## J. Recommended Minimal Changes

Unchanged from the original audit (clean-before-run convention; decide on `RetryAnalyzer`'s fate; confirm Appium/device connectivity before trusting Allure data as a signal of test-logic health; CI wiring is separate, additive scope). One addition specific to the current moment: **regenerate or clearly label the existing report as stale** before anyone relies on it, since it no longer reflects the current (empty) results directory.

## K. Proposed Final Workflow

Unchanged from the original audit's proposal. Phase B is already attempting to execute exactly this proposed workflow (clean → controlled run → verify results → generate report → serve) and is currently paused at the "controlled run" step, waiting on the physical device `10BDAT2Y9U000DF` to become available.

---

## VERDICT

**B. REQUIRES FURTHER INVESTIGATION** — unchanged conclusion from the original audit, for the same underlying reasons (Section I), plus one new, narrow, easily-resolved item: the existing on-disk report is now stale and should not be treated as current until Phase B's controlled run completes and the report is regenerated from the fresh (currently empty) results directory.

**No changes were made during this audit pass.** `build/allure-results` was already empty going into this pass (from Phase B Part B, a prior authorized step, not something this audit did) and remains empty. Awaiting explicit direction: either resume Phase B (once the physical device is connected) or provide further instructions.
