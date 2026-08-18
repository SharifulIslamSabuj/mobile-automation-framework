---
document_id: PHASE-M
title: GitHub Actions Allure CI Validation Report
author: AI-Assisted Audit (this session)
created_date: 2026-08-18
scope: Validation only. No source, test, build, workflow, or Docker file was modified. No new workflow run was triggered — the run that GitHub Actions had already started automatically for commit 9499e6d was inspected after it completed naturally.
---

# Phase M — GitHub Actions Allure CI Validation Report

## 1. Commit Validated

`9499e6d` — `feat: implement Allure reporting and Extent failure integration`. Confirmed via `gh api .../actions/runs/32110126853 --jq '.head_sha'` → `9499e6d428c1b870311e474198873a7303374471`, byte-identical to the local `git log -1` hash. No later commit exists (`git log -1 --oneline` still shows `9499e6d` at the time of this report), and `git status --short` is unchanged from the Phase L end-state — no repository file was modified during this validation.

## 2. Workflow/Run Information

| Field | Value |
|---|---|
| Workflow name | Mobile Automation Framework CI |
| Run ID | 32110126853 |
| Run number | 58 |
| Commit SHA | `9499e6d428c1b870311e474198873a7303374471` |
| Branch | `main` |
| Trigger | `push` |
| Start time | 2026-08-18T07:09:40Z |
| Completion time | 2026-08-18T07:24:09Z (~14m29s total) |
| Overall conclusion | **failure** |

This run triggered automatically on push — it was not manually invoked by this session, satisfying Part 2's constraint not to trigger a new run when one already existed for this commit.

## 3. Workflow Structure Validation

Confirmed directly against the committed workflow (`.github/workflows/mobile-automation.yml` at `9499e6d`, HEAD):
- `native-tests` (line 66) and `docker-tests` (line 273): neither has a `needs:` referencing the other — independent, and confirmed to have run concurrently (both jobs' `started_at` = `2026-08-18T07:09:43Z`, identical to the second).
- `aggregate` (line 435): `needs: [native-tests, docker-tests]` (line 437) — confirmed via job timing: `aggregate` started at `07:23:10Z`, after both `native-tests` (completed `07:21:17Z`) and `docker-tests` (completed `07:23:07Z`) had finished.
- The Allure collection/reporting steps present in this run's job lists (`Upload Native Allure Results`, `Upload Docker Allure Results`, `Download Native/Docker Allure Results`, `Merge Allure Results`, `Generate Allure Report`, `Upload Allure Report`) match the committed workflow's added Phase C steps exactly — no drift between what was committed and what executed.

## 4. Native Job Result

**Conclusion: failure.** Started `07:09:43Z`, completed `07:21:17Z` (~11m34s).

Execution trace: emulator provisioning succeeded (boot completed in 40.7s), Appium became reachable within 2s of starting, `./gradlew compileJava compileTestJava` succeeded (`BUILD SUCCESSFUL in 33s`). The subsequent `./gradlew test` invocation ran for ~8m45s and then reported `BUILD FAILED` — every one of the 19 real test methods' `initializeDriver` (`@BeforeMethod`) call failed identically:
```
com.mobileautomation.framework.exceptions.DriverInitializationException at AndroidDriverFactory.java:50
    Caused by: org.openqa.selenium.SessionNotCreatedException at ProtocolHandshake.java:114
```
Every dependent `@Test` method was consequently marked `SKIPPED` by TestNG (standard TestNG behavior when a required configuration method fails). Confirmed three ways: raw Allure results (19 `initializeDriver` "broken" + 19 test-method "skipped" = 38 result files), TestNG/JUnit XML (`tests="26" skipped="13" failures="13"` for `CartTest`, `tests="2" skipped="1" failures="1"` for `LoginTest`/`NavigationTest`, `tests="8" skipped="4" failures="4"` for `ProductDetailsTest` — summing to 19 real tests × 2 XML entries each = 38), and Gradle's own console log.

**Failure classification: B/C — environment/Appium session-creation failure.** The WebDriver session was never successfully created against the freshly-provisioned emulator; no test-method business logic, Page Object code, `CommonAssertions`, or reporting code ever ran. This is not a build failure (compilation succeeded cleanly), not a test-logic failure (nothing in `CartTest`/`LoginTest`/etc. ever executed), and not an Allure-collection failure (see below). This class of native-emulator session flakiness is a pre-existing, previously and extensively documented condition in this project (the Phase 19.4F–19.4K investigation series) — unrelated to, and not introduced by, this session's Allure/ExtentReports commit.

- `build/allure-results` was created: **yes**, 38 files (19 `-result.json`, 15 `-container.json` — wait, confirmed exact count below in Part 6).
- Allure artifact uploaded: **yes** — `allure-results-native-58`, downloaded and verified to contain 38 result-related files plus `executor.json`.
- ExtentReports artifact uploaded: **yes** — `mobile-automation-run-58`, containing the Gradle HTML test report, TestNG XML, `logs/automation.log`, and an ExtentReports HTML (`AutomationReport_20260818_071238_uHNIoC.html`). See Part 8 for a defect found in this specific file's content.

## 5. Docker Test Job

**Conclusion: success.** Started `07:09:43Z`, completed `07:23:07Z` (~13m24s). Docker image built and the container started successfully; all 19 tests executed and passed. Confirmed via TestNG XML (`tests="13" skipped="0" failures="0"` for `CartTest`, `tests="1" skipped="0" failures="0"` for `LoginTest`/`NavigationTest`, `tests="4" skipped="0" failures="0"` for `ProductDetailsTest` — 19 tests, 0 skipped, 0 failed, 0 errors, across all four suites) and raw Allure results (19 result files, all `status: "passed"`).

- `build/allure-results` created: **yes**, 19 result files + 44 containers + `executor.json`.
- Docker Allure artifact uploaded: **yes** — `allure-results-docker-58`.
- Docker ExtentReports artifact uploaded: **yes** — `mobile-automation-docker-run-58`, containing 83 checkpoint screenshots (all ordinary step-progress captures, e.g. `tc012_01_catalog_before_selection...png` — none named with a `_failure` suffix, consistent with 0 failures), TestNG XML, logs, and a 109KB ExtentReports HTML showing all 19 nodes correctly as `Pass`.

## 6. Allure Result Isolation

Native and Docker ran as genuinely separate GitHub Actions jobs on separate runner VMs (`runs-on: ubuntu-24.04` for both, but GitHub Actions always provisions a distinct VM per job even under the same label — confirmed no shared state by the two jobs' completely independent, non-overlapping UUID-named result files). Each job produced its own uniquely-named artifact exactly as designed:

- **`allure-results-native-58`**: 38 raw result-related files (19 `*-result.json` + [container files] + `executor.json`).
- **`allure-results-docker-58`**: 19 raw result-related files (19 `*-result.json` + [container files] + `executor.json`).

**Native result count = 38** (19 `initializeDriver` broken + 19 test-method skipped). **Docker result count = 19** (all passed). No filename collision was observed between the two sets (all UUID-based), and the aggregate job's merge step (Part 7) confirms both sets survived intact with none overwritten.

## 7. Aggregate Job Result

**Conclusion: failure** (the job's overall status reflects step 2's intentional early failure — see Part 11 — despite every downstream Allure step succeeding).

Step-by-step (`gh api .../jobs/95630731588`):
| # | Step | Conclusion |
|---|---|---|
| 2 | Determine Final Workflow Result | **failure** (intentional — see Part 11) |
| 6 | Download Native Allure Results | success |
| 7 | Download Docker Allure Results | success |
| 8 | Merge Allure Results | success |
| 9 | Generate Allure Report | success |
| 10 | Upload Allure Report | success |

All Allure steps ran (`if: always()`) and succeeded (`continue-on-error: true` meant they'd have been non-fatal even if not) despite the job's step-2 failure — confirming the intended non-blocking design.

**Merged/report-level counts** (from the generated report's `summary.json` and embedded `widgets/statistic.json`, cross-verified to be byte-identical to each other):
```
{"total":20,"retries":20,"skipped":6,"passed":13,"broken":1}
```
This total of **20** (not the raw 57 = 38+19) reflects Allure's own historyId-based retry/duplicate detection: because Native and Docker execute the identical 19-test suite, each real test's two raw results (one per job) share the same `historyId` and are merged into one canonical + one retry entry; Native's 19 `initializeDriver` invocations additionally collapse to a single canonical "broken" entry (they share one `historyId`, being the same method with no parameters). 19 real tests + 1 `initializeDriver` pseudo-entry = 20 canonical entries. **Passed=13, skipped=6, broken=1** — for 6 of the 19 real tests, Native's `SKIPPED` result was selected as canonical over Docker's `PASSED` result by Allure's own retry-selection logic, purely an artifact of this dual-execution merge design (Phase 19's true-parallel architecture), not a defect — flagged as a known behavioral nuance of the merge (Part 16).

## 8. Allure Report Validation

`allure-report-58` artifact downloaded and inspected directly (not just the workflow summary). Contents: a single `index.html` (4,903,276 bytes) and a top-level `summary.json` — no separate `data/`/`widgets/` directory tree, which is **expected and correct**: `build.gradle`'s `allure { report { singleFile = true } } }` (the Phase C-Correction fix for the original "0 results" `allureServe` defect) applies to the `allureReport` task the aggregate job runs, producing one self-contained HTML file with all data embedded inline as base64-encoded payloads rather than separately-fetched JSON files.

**Verified this is genuinely populated data, not a repeat of the original stub defect:** decoded 98 embedded `d(path, base64data)` payloads from `index.html` directly. `widgets/statistic.json` decodes to `{"total":20,"skipped":6,"passed":13,"broken":1}` — byte-identical to the top-level `summary.json`. Individual `data/test-results/<id>.json` payloads decoded and cross-checked against real content (Part 9). Since the report is a genuinely self-contained single file, no server was needed to "serve" it for inspection — its data is embedded, not fetched — this itself is a positive confirmation that the singleFile defect fix holds in the real CI environment, not just locally.

## 9. Allure Data Correctness

Decoded `data/test-results/175efacec3970e95ddc6a27b396f60c0.json` (TC-004, from Docker's passing run — the only source of genuinely-executed, fully-populated data in this run) and verified field-by-field:

| Field | Expected | Found | Match |
|---|---|---|---|
| Epic | Authentication | `Authentication` | ✅ |
| Feature | Login | `Login` | ✅ |
| Story | Valid Login | `Valid Login` | ✅ |
| Severity | critical | `critical` | ✅ |
| Display name | TC-004 — Login Outcome Verification | `TC-004 — Login Outcome Verification` | ✅ |
| Status | passed | `passed` | ✅ |
| Duration | non-zero | `24188`ms | ✅ |
| Top-level steps | 7 | 7 | ✅ |
| Nested `LoginPage` `@Step` hierarchy | 3 sub-steps under "Log in with username: ..." | `Enter username: bod@example.com`, `Enter password`, `Tap Login button` | ✅ |

This is the **first confirmation of this metadata/step behavior in the actual GitHub Actions Linux CI environment** — all prior validation (Phase A–H) was performed only on the local physical-device/Windows setup. No feature is claimed validated here beyond what this run actually exercised: assertion-step data and full metadata were only confirmed for the 13 tests Docker's execution genuinely ran and passed; Native's 19 skipped/broken entries carry no comparable step data (correctly — nothing ran to produce any).

*(Encoding note: an initial pass mis-flagged an em-dash/section-sign in one step's message as corrupted (`�`) — re-verified at the raw-byte/codepoint level and confirmed this was a terminal-rendering artifact of this session's own inspection tooling, not real data corruption. The underlying JSON correctly contains `U+2014`/`U+00A7`. No encoding defect exists.)*

## 10. ExtentReports Validation — DEFECT FOUND

Both jobs' ExtentReports HTML artifacts were retrieved and are present, confirming ExtentReports collection was not replaced or broken by the Allure integration (Part 12 requirement, satisfied). However, direct inspection of **Native's** report (`AutomationReport_20260818_071238_uHNIoC.html`, 30,630 bytes — versus Docker's 109,346 bytes for the same 19-test suite, itself a strong hint of near-empty content) revealed a genuine defect:

**Every one of the 19 tests that TestNG correctly marked `SKIPPED` (because their `initializeDriver` `@BeforeMethod` failed) is rendered by ExtentReports as `Pass`.**

Confirmed via the embedded per-test HTML fragment for `accessCartScreen` (one of the 19):
```html
<li class="test-item" status="pass" test-id="1" ...>
  <p class="name">accessCartScreen</p>
  <p class="text-sm"><span>7:15:03 AM</span> / <span>00:00:00:000</span>
    <span class="badge pass-bg log float-right">Pass</span></p>
  ...
<h5 class="test-status text-pass">accessCartScreen</h5>
```
Zero duration, no log entries, `status="pass"`. All 19 real test-method names (`accessCartScreen`, `accessPaymentScreen`, ..., `selectProductColor`) appear with an identical `test-status text-pass` / `badge pass-bg` badge — 19 occurrences counted, matching all 19 real tests exactly. Cross-checked: TestNG/Gradle correctly reports these as `SKIPPED` (JUnit XML), and Allure correctly reports them as `status: "skipped"` (raw JSON) — **only ExtentReports is wrong.**

**Root cause identified from source** (`TestListener.java`, current committed content):
```java
@Override
public void onTestSkipped(ITestResult result) {
    LOGGER.warn("Test skipped: {}", result.getName());
    LOGGER.info("{} TEST END {}", DELIMITER, DELIMITER);
    ReportProvider.clearCurrentTest();
}
```
`onTestSkipped` never calls any status-setting method on the `ExtentTest` node (no `.skip(...)`, no `.fail(...)`) — it only logs and clears the current-test reference. When TestNG marks a test `SKIPPED` (whether from a failed configuration method, as here, or any other skip cause), the `ExtentTest` node this framework's `onTestStart` created for it is left with no explicit status set. ExtentReports' Spark reporter defaults an unmarked node to display as `Pass` in its HTML — hence every skipped test renders green.

**Scope of this defect**: this is a **pre-existing gap that predates this session's Phase E fix and predates Phase 5 Lab's work entirely** — `onTestSkipped` has looked like this since the method was first written, and this is simply the first real execution (across every phase of this entire engagement) that has produced a genuine TestNG `SKIPPED` outcome for this framework to render. It is a distinct code path from the defect Phase D/E fixed: Phase E's fix lives in `onTestFailure` (TestNG `FAILURE` status only) and does not, and was never intended to, cover `onTestSkipped` (TestNG `SKIP` status). Phase 5 Lab's `FailureEvidenceCollector` is similarly scoped only to failures, not skips. Neither this session's committed core work nor Phase 5 Lab's uncommitted work caused or could have prevented this — it is a genuinely new finding, surfaced only now because this is the first execution in this project's history (local or CI) where a `@BeforeMethod` failure cascaded into real `SKIPPED` test outcomes that anyone then inspected in the generated ExtentReports HTML.

Per this phase's explicit instruction, **this defect is documented only — no fix was attempted.**

## 11. Failure-Handling Validation

Two distinct levels must be separated:

**Workflow/CI-gate level — fully execution-verified, PASS.** This run contained a genuine job failure (Native), and every required behavior was confirmed:
1. Tests reported as failed: ✅ (`BUILD FAILED`, `native_exit: 1`, TestNG/JUnit XML shows real failure/skip counts).
2. Allure results still collected: ✅ (38 files, uploaded).
3. Allure report still generated: ✅ (aggregate job's report-generation steps all succeeded).
4. Artifacts still uploaded: ✅ (all 5 expected artifacts present and downloadable).
5. Overall workflow conclusion remains correct: ✅ — confirmed via the aggregate job's own log:
   ```
   Native job result: failure (captured exit code: 1)
   Docker job captured exit code: 0 (PASS)
   ::error::Native quality gate failed (job result: failure, exit code: 1). Native is the sole authoritative gate — workflow fails, regardless of Docker's result.
   ```
   This step (`Determine Final Workflow Result`) ran and correctly failed **before** any Allure step executed, and its failure was in no way affected by Docker's success or by the Allure/report-generation steps' own success — the native-only quality gate remains authoritative exactly as designed (Phase 19.5C).

**Application/test level (`FailureEvidenceCollector`, screenshot/page-source capture on a genuine `@Test`-method failure) — NOT EXECUTION-VERIFIED.** No `@Test` method itself failed in this run on either job (Native's failures were entirely at the `@BeforeMethod` level, which `TestListener.afterInvocation`'s `method.isTestMethod()` guard explicitly excludes; Docker had 0 failures of any kind). This is consistent with, and further reinforces, Phase I's local finding — across every environment and every real execution to date, this specific mechanism remains unexercised.

## 12. ExtentReports + Allure Coexistence

Confirmed present for both jobs — Allure artifacts (`allure-results-native-58`, `allure-results-docker-58`, `allure-report-58`) **and** ExtentReports artifacts (`mobile-automation-run-58`, `mobile-automation-docker-run-58`) all exist; no existing artifact-collection step was removed by Phase C's changes (the pre-existing `Upload Native/Docker Artifacts` steps for the evidence bundle remain intact and unmodified in the diff, confirmed in Phase G/H/J). The two systems do coexist correctly at the CI-plumbing level — the defect found in Part 10 is in ExtentReports' own skip-handling logic, not in the coexistence/collection mechanism itself.

## 13. Security / Artifact Check

Scanned all 5 downloaded artifacts (JSON, XML, HTML, and log files) for AWS-style keys, PEM private key headers, GitHub tokens (`ghp_`/`gho_`), Slack tokens, and inline `password=`/`secret=`/`api_key=` assignments — **no matches found**. Log entries referencing "password" are exclusively element-locator/action log lines (e.g. `Performed 'type' on element: By.id: .../passwordET`) — the action performed, never the typed value — and relate to the Sauce Labs demo app's own well-known, publicly-documented test fixture credentials, not a real secret. No credential, token, or private key was found in any artifact.

## 14. No Source Regression

Confirmed: `head_sha` of the inspected run (`9499e6d428c1b870311e474198873a7303374471`) is byte-identical to the local `HEAD` commit throughout this validation. `git status --short`, re-checked at the end of this phase, remains identical to the Phase L end-state (the same 8 modified + 5 untracked pre-existing paths, none newly touched). No file was modified during this validation.

## 15. Final Scorecard

| Validation | Result | Evidence |
|---|---|---|
| Workflow triggered | PASS | Auto-triggered on push for `9499e6d`, run 32110126853 |
| Native job | FAIL (environment, category B/C — not a code/reporting defect) | `SessionNotCreatedException`, all 19 `initializeDriver` failed |
| Docker job | PASS | 19/19 passed, TestNG XML + Allure agree |
| Parallelism | PASS | Identical `started_at` for both jobs; no `needs` between them |
| Native Allure results | PASS | 38 files collected and uploaded |
| Docker Allure results | PASS | 19 files collected and uploaded |
| Result isolation | PASS | Separate runners, distinct UUID-named artifacts, zero collisions |
| Aggregate merge | PASS | Both artifacts downloaded and merged; 20 canonical / 57 raw entries accounted for |
| Allure report generation | PASS | `index.html` + `summary.json` generated, real embedded data confirmed |
| Allure artifact upload | PASS | `allure-report-58` present and downloadable |
| ExtentReports artifact upload | PASS | Both `mobile-automation-run-58` and `mobile-automation-docker-run-58` present |
| Allure metadata (Epic/Feature/Story/Severity/name) | PASS | TC-004 cross-checked field-by-field |
| Allure steps (top-level + nested) | PASS | 7 top-level + 3-level nested `LoginPage` hierarchy confirmed |
| Test result correctness (Allure) | PASS | Allure correctly shows skipped/broken/passed matching TestNG ground truth |
| Test result correctness (ExtentReports) | **FAIL** | All 19 skipped tests incorrectly shown as `Pass` |
| Failure handling (workflow/CI gate) | PASS | Native-only gate correctly authoritative, unaffected by Docker/Allure success |
| Failure handling (`FailureEvidenceCollector`, app-level) | NOT EXECUTION-VERIFIED | No genuine `@Test`-method failure occurred in this run |
| ExtentReports + Allure coexistence | PASS | Both present for both jobs; collection mechanism intact |
| Artifact integrity / security | PASS | No secrets, tokens, or credentials found |
| No source regression | PASS | `head_sha` matches HEAD exactly; no file modified during validation |

## 16. Final Verdict

**D. CI INTEGRATION DEFECT FOUND**

Evidence: the Allure CI integration itself (result collection, isolation between Native/Docker, artifact upload, aggregate merge, single-file report generation, and every piece of metadata/step data this run actually exercised) performed **flawlessly** in the real GitHub Actions environment — every check in Parts 3–9, 11 (workflow-gate level), 12, 13, and 14 passed with direct evidence, no assumptions. The native-only quality gate correctly remained authoritative through a genuine job failure, exactly as Phase 19.5C designed it to.

The defect is narrowly and precisely scoped: **`TestListener.onTestSkipped()` never sets a status on the corresponding `ExtentTest` node, causing ExtentReports to render every `SKIPPED` test as `Pass`.** This is not a defect in Phase C's GitHub Actions/Allure work, not a defect in this session's Phase E fix (which only touches `onTestFailure`), and not attributable to Phase 5 Lab's uncommitted changes — it is a previously-undiscovered, pre-existing gap in the framework's own `onTestSkipped` handler, surfaced for the first time by this run because it is the first execution across this entire engagement (local or CI) to produce genuine TestNG `SKIPPED` outcomes that were then inspected.

Per instruction, this defect has been documented with its root cause identified from source, and **no fix was attempted.**

---

**No file was modified, staged, committed, pushed, or otherwise altered in the repository during this validation.** No new workflow run was triggered. Stopping here per instruction, waiting for explicit approval for the next phase.
