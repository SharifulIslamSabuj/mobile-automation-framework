---
document_id: PHASE-J
title: Final Commit Scope & Release Readiness Audit
author: AI-Assisted Audit (this session, read-only)
created_date: 2026-08-18
scope: Read-only audit only. No file was modified, staged, committed, pushed, reverted, reset, stashed, or deleted during this phase. No test was executed.
---

# Phase J — Final Commit Scope & Release Readiness Audit

## 1. Executive Summary

The Allure + ExtentReports reporting implementation this session built and validated (Phases A–G) is functionally sound and confirmed by a genuine 19/19 physical-device run (`PHASE_FULL_SUITE_EXECUTION_REPORT.md`). Phase H proved that run's validated state already included a separate, concurrent "Phase 5 Lab" process's changes — a failure-evidence subsystem (`FailureEvidenceCollector`, screenshot + page-source capture, dual-report attachment) and an unrelated `ProductsPage.getCardPrice()` flakiness fix. Phase I attempted to execution-verify that failure-evidence subsystem using a real, unmanufactured test failure; the target test passed naturally instead, so the subsystem remains **implemented but not execution-verified** — not proven defective, simply unexercised.

This phase's finding: **the working tree cannot be committed as a single, cleanly-attributable unit.** Two files (`TestListener.java`, `CommonAssertions.java`) interleave this session's own validated Allure/Extent work with Phase 5 Lab's unreviewed, execution-unverified work at the hunk level. A clean **Scope A (minimal core)** commit is achievable for every file except those two, which require either accepting Phase 5 Lab's content alongside the core fix or a manual hunk-level split. Verdict: **C — USER DECISION REQUIRED**, narrowed specifically to the Phase 5 Lab scope question, not to any doubt about the core Allure/ExtentReports/CI work itself.

## 2. Current Git State

```
git status --short
```
11 modified tracked files, 8 untracked paths (4 directories) — byte-identical to the state recorded at the end of Phase H and Phase I; no drift across any of the three most recent phases.

```
git diff --stat
```
```
 .github/workflows/mobile-automation.yml            | 131 ++++++++++++++++++++-
 build.gradle                                       |  68 +++++++++++
 .../framework/config/ConfigReader.java             |   5 +
 .../framework/constants/ConfigurationDefaults.java |   1 +
 .../framework/constants/ConfigurationKeys.java     |   1 +
 .../framework/listeners/TestListener.java          |  65 ++++++++--
 .../framework/locators/ProductsLocators.java       |  22 ++++
 .../framework/assertions/CommonAssertions.java     |  28 +++--
 .../framework/pages/LoginPage.java                 |   5 +
 .../framework/pages/ProductsPage.java              |  55 +++++----
 .../framework/tests/LoginTest.java                 |   9 ++
 src/test/resources/config/config.properties        |   1 +
 12 files changed, 349 insertions(+), 42 deletions(-)
```

`git diff --check` → exit 0 (only pre-existing CRLF-conversion warnings on every file — a repository-wide line-ending convention, not a defect introduced by any of this work; no literal conflict markers).

## 3. Complete Change Inventory

**Modified (tracked, 11):** `.github/workflows/mobile-automation.yml`, `build.gradle`, `ConfigReader.java`, `ConfigurationDefaults.java`, `ConfigurationKeys.java`, `TestListener.java`, `ProductsLocators.java`, `CommonAssertions.java`, `LoginPage.java`, `ProductsPage.java`, `LoginTest.java`, `config.properties`.

**Untracked (8):** `allure-report/` (dir, project root — generated artifact, not gitignored since it sits outside `build/`), `docs/allure/` (dir — 13 markdown reports, this phase adds a 14th), `docs/jenkins/` (dir — pre-existing, unrelated per the original Phase A audit), `FailureEvidenceCollector.java`, `PageSourceManager.java`, `PageSourceUtility.java`, `allure.properties`.

**Deleted / renamed:** none (`git status --porcelain=v2` shows no `D`/`R` code for any tracked file, per Phase H Part 2).

No ownership is assumed here from filenames alone — see Parts 4–7 for the evidence-based classification.

## 4. Core Allure Changes (Validated)

Per `PHASE_ALLURE_REPORTING_IMPLEMENTATION_REPORT.md`, `PHASE_ALLURE_FORENSIC_AUDIT_REPORT.md` (+ re-audit), `PHASE_ALLURE_CLEAN_RESULT_LIFECYCLE_VALIDATION_REPORT.md`, `PHASE_C_ALLURE_SERVE_ZERO_RESULTS_ROOT_CAUSE_AND_FIX_REPORT.md`, `PHASE_C_CORRECTION_ALLURE_CLI_MIGRATION_AUDIT.md`:

- `src/test/resources/allure.properties` (new) — `allure.results.directory=build/allure-results`.
- `LoginPage.java` — `@Step("Enter username: {username}")` / `@Step("Enter password")` / `@Step("Tap Login button")` / `@Step("Log in with username: {username}")`.
- `LoginTest.java` — `@Epic("Authentication")` / `@Feature("Login")` (class-level), `@Story("Valid Login")` / `@Severity(SeverityLevel.CRITICAL)` (method-level).
- `build.gradle` — `io.qameta.allure` plugin, `allure { report { singleFile = true } }`, `allure-bom`/`allure-testng` dependencies, and the `allureLocalReport` Exec task (standalone-CLI workflow, Phase C-Correction).
- `CommonAssertions.java` — the `Allure.step(message, Status.PASSED)` / `Allure.step(message, Status.FAILED)` calls (hunks 2 and 5 of Phase H's hunk analysis) — **not** the surrounding Phase 5 Lab routing changes in the same file.

## 5. ExtentReports Changes (Validated)

Per `PHASE_D_EXTENTREPORTS_FORENSIC_AUDIT_REPORT.md` and `PHASE_E_EXTENTREPORTS_MINIMAL_FAILURE_FIX_VALIDATION_REPORT.md`:

- `TestListener.java`, `onTestFailure(...)` — exactly: `if (ReportProvider.hasActiveTest()) { ReportProvider.getTest().fail(result.getThrowable()); }` plus its explanatory comment (Phase H hunk 5). This is the single-line minimal fix for the "ExtentReports shows PASS for a failed test" defect, independently validated in Phase E using the naturally-failing test, and confirmed to have zero regression on the passing path.

No other ExtentReports-specific change is part of this session's own validated work.

## 6. Phase C CI Changes (Validated, Not Yet Pushed)

Per the Phase C CI report (folded into this session's documentation set) and Part A of this session's pre-commit validation:

- `.github/workflows/mobile-automation.yml` — +131 lines: Allure-results collection/upload in the `native-tests`/`docker-tests` jobs, and a merge + `allureReport` + upload sequence in `aggregate`, every new step tagged `if: always()` + `continue-on-error: true` (additive only, does not alter the existing native-only quality gate or Phase 19's true-parallel Native/Docker architecture). Statically validated (YAML syntax, job dependency graph) but **never exercised against actual GitHub Actions**, since this session has not pushed it — this remains true today.

## 7. Phase 5 Lab Changes (Present, Compiled, Passed 19/19 — Not Independently Reviewed or Execution-Verified for Failure Behavior)

Per Phase H's hunk-by-hunk and file-level analysis, re-confirmed here without re-reading source (already fully captured in Phase H/I):

| File | Nature |
|---|---|
| `FailureEvidenceCollector.java` (new) | Centralizes screenshot + page-source capture, attaches to both ExtentReports (`.info(...)`) and Allure (`Allure.addAttachment(...)`), thread-local de-duplication guard. |
| `PageSourceManager.java` (new) | Defensive wrapper around `PageSourceUtility`, mirrors `ScreenshotManager`'s contract. |
| `PageSourceUtility.java` (new) | Captures `driver.getPageSource()`, persists under `ConfigReader.getPageSourceDirectory()`. |
| `ConfigReader.java`, `ConfigurationDefaults.java`, `ConfigurationKeys.java`, `config.properties` | Small, additive `report.pageSourceDirectory` config plumbing supporting the above. |
| `ProductsLocators.java` | New `productCard(String)` locator supporting the `getCardPrice()` rewrite. |
| `ProductsPage.java` | `getCardPrice()` rewritten to resolve the card container once (by presence) and read the price as a descendant, replacing an unconditional-scroll + second-whole-document-query approach that a live diagnostic showed could overshoot. Addresses the exact flakiness Phase D investigated, but under a different mechanism than anything this session designed. |
| `TestListener.java` | Adds `IInvokedMethodListener`/`afterInvocation(...)`, routes failure-evidence capture through `FailureEvidenceCollector`, removes the old inline `ScreenshotManager`+`attachScreenshotToAllure` mechanism this session's Phase D/E read and referenced. |
| `CommonAssertions.java` | Removes the old inline `ScreenshotManager`+`MediaEntityBuilder` fail-path code, replaces it with `FailureEvidenceCollector.captureAndAttach(...)`. |

**Execution status**: per Phase H Part 12, all of the above was on disk and compiled without error during the validated 19/19 run (44-minute margin between Phase 5 Lab's last edit and that run's Gradle invocation). Per Phase I, the failure-triggering code path inside this subsystem (`FailureEvidenceCollector.captureAndAttach`, both `attachToExtent`/`attachToAllure` methods, and `ProductsPage.getCardPrice()`'s own exception-wrapping branch) has **not** been exercised by any run this session witnessed — the one attempt (Phase I) produced a natural pass, not a failure.

## 8. Mixed-File Analysis

**`TestListener.java`** and **`CommonAssertions.java`** each contain both category-A/B (this session's validated work) and category-D (Phase 5 Lab's work) content, interleaved at the hunk level (full breakdown: Phase H Parts 6–7). Structurally:

- In `TestListener.java`, this session's fix (hunk 5, the `onTestFailure` `.fail(result.getThrowable())` call) is a self-contained statement reading only `ReportProvider` — it does not call or depend on anything Phase 5 Lab added. It **could** be isolated with a manual hunk-level split (`git add -p`), but as the file currently stands, staging the whole file pulls in both.
- In `CommonAssertions.java`, the two `Allure.step(...)` calls (hunks 2/5) are similarly self-contained lines, but sit inside the same diff hunks as Phase 5 Lab's fail-path rewrite, making a clean split slightly more fiddly (the pass-branch `Allure.step` call is on its own line and trivially separable; the fail-branch one is immediately adjacent to the `FailureEvidenceCollector.captureAndAttach(...)` call Phase 5 Lab added).

**Can they safely remain together?** Functionally, yes — both files compiled and ran correctly in the 19/19 validated run with all of this content combined; there is no known conflict between this session's fix and Phase 5 Lab's rework (they touch different branches/methods of the same files). The question is not functional safety but **review/attribution scope**: committing these files as-is means committing Phase 5 Lab's unreviewed, execution-unverified failure-evidence code inseparably alongside this session's independently-validated fix. No file was edited or split in this phase, per instruction.

## 9. Validation Matrix

| Feature | Validated? | Evidence |
|---|---|---|
| Allure result generation | ✅ Yes | Raw `*-result.json`/`*-container.json` inspected directly, multiple runs (Phase B, Full-Suite, Phase I) |
| Clean Allure lifecycle | ✅ Yes | `PHASE_ALLURE_CLEAN_RESULT_LIFECYCLE_VALIDATION_REPORT.md`; re-confirmed Phase I (0 results after cleanup, exactly 1 after run) |
| Allure HTML report generation | ✅ Yes | `allureLocalReport` task, `build/allure-report/widgets/summary.json` matched disk data exactly, Full-Suite report |
| Native Allure serving (live curl) | ✅ Yes (standalone CLI only) | `allure open` live-curled and matched disk, both 2-test and 19-test datasets. Gradle-plugin `allureServe` remains proven broken (Section 6 fix report) — superseded by the CLI workflow, not fixed itself |
| Epic / Feature / Story / Severity | ✅ Yes | `PHASE_FULL_SUITE_EXECUTION_REPORT.md` §15, raw result JSON fields for `TC-004` |
| Display name | ✅ Yes | Same source, `name` field |
| `@Step` (top-level) | ✅ Yes | §16 of the same report, 7 steps matching `LoginTest`'s 7 assertions |
| Nested Page Object `@Step` | ✅ Yes | Same section, 3 nested `LoginPage` steps confirmed intact |
| Assertion steps (`Allure.step` pass/fail) | ✅ Yes | Populated step data in the 19/19 run |
| ExtentReports pass handling | ✅ Yes | 19/19 run, all nodes rendered `Pass` |
| ExtentReports failure handling (the Phase E fix) | ✅ Yes | `PHASE_E_EXTENTREPORTS_MINIMAL_FAILURE_FIX_VALIDATION_REPORT.md`, using the naturally-failing test at that time |
| TestNG/Extent/Allure consistency | ✅ Yes | Full-Suite report §14, exact 19/19/19 agreement, test-for-test |
| Physical-device execution | ✅ Yes | Every phase in this engagement ran against `10BDAT2Y9U000DF` (I2301, Android 15) — no emulator used at any point |
| Full-suite execution (19 tests) | ✅ Yes | `PHASE_FULL_SUITE_EXECUTION_REPORT.md`, verdict A |
| CI Allure integration (GitHub Actions) | ⚠️ Statically validated only | YAML/job-graph reviewed; never exercised against real GitHub Actions (not pushed) |
| **Failure screenshot capture/attachment** | ❌ **Not execution-verified** | Phase I: target test passed naturally; mechanism unexercised |
| **Page-source capture/attachment** | ❌ **Not execution-verified** | Same as above |
| **`FailureEvidenceCollector` execution** | ❌ **Not execution-verified** | Same as above — source-reviewed only (Phase I §9, §17) |

## 10. Core Release Scope (Scope A — Minimal)

Every change strictly necessary for the validated Allure + ExtentReports + CI implementation, and its documentation:

```
.github/workflows/mobile-automation.yml
build.gradle
src/test/resources/allure.properties
src/test/java/com/mobileautomation/framework/pages/LoginPage.java
src/test/java/com/mobileautomation/framework/tests/LoginTest.java
docs/allure/PHASE_ALLURE_REPORTING_IMPLEMENTATION_REPORT.md
docs/allure/PHASE_ALLURE_FORENSIC_AUDIT_REPORT.md
docs/allure/PHASE_ALLURE_FORENSIC_AUDIT_REPORT_REAUDIT.md
docs/allure/PHASE_ALLURE_CLEAN_RESULT_LIFECYCLE_VALIDATION_REPORT.md
docs/allure/PHASE_C_ALLURE_SERVE_ZERO_RESULTS_ROOT_CAUSE_AND_FIX_REPORT.md
docs/allure/PHASE_C_CORRECTION_ALLURE_CLI_MIGRATION_AUDIT.md
docs/allure/PHASE_D_EXTENTREPORTS_FORENSIC_AUDIT_REPORT.md
docs/allure/PHASE_E_EXTENTREPORTS_MINIMAL_FAILURE_FIX_VALIDATION_REPORT.md
docs/allure/PHASE_FULL_SUITE_EXECUTION_REPORT.md
docs/allure/PHASE_G_FINAL_GIT_SCOPE_AUDIT.md
docs/allure/PHASE_H_CHANGE_OWNERSHIP_AND_WORKTREE_RECOVERY_AUDIT.md
docs/allure/PHASE_I_FAILURE_PATH_AND_EVIDENCE_VALIDATION_REPORT.md
docs/allure/PHASE_J_FINAL_COMMIT_SCOPE_AND_RELEASE_READINESS.md
```

**Blocked by the mixed-file problem**: `TestListener.java` and `CommonAssertions.java` cannot be added to this list *as they currently stand* without also including Phase 5 Lab's content — see Part 8. Scope A therefore either (a) excludes both files entirely, deferring the Phase E ExtentReports fix and the `Allure.step` instrumentation until they can be committed cleanly (via a manual split not performed in this phase), or (b) accepts that these two files must be committed with their full current content, which functionally makes this "Scope A" indistinguishable from Scope B for those two files specifically. This ambiguity is surfaced, not resolved, per this phase's read-only mandate.

## 11. Extended Release Scope (Scope B)

Scope A, plus:

```
src/main/java/com/mobileautomation/framework/reporting/FailureEvidenceCollector.java
src/main/java/com/mobileautomation/framework/reporting/PageSourceManager.java
src/main/java/com/mobileautomation/framework/utils/PageSourceUtility.java
src/main/java/com/mobileautomation/framework/config/ConfigReader.java
src/main/java/com/mobileautomation/framework/constants/ConfigurationDefaults.java
src/main/java/com/mobileautomation/framework/constants/ConfigurationKeys.java
src/test/resources/config/config.properties
src/main/java/com/mobileautomation/framework/locators/ProductsLocators.java
src/test/java/com/mobileautomation/framework/pages/ProductsPage.java
src/main/java/com/mobileautomation/framework/listeners/TestListener.java   (full current content)
src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java   (full current content)
```

Choosing Scope B means knowingly committing the unexercised failure-evidence subsystem (Part 9's matrix) and the unreviewed `getCardPrice()` rewrite, on the strength of "it compiled and did not break a passing run" rather than a positive validation of its own failure-path behavior.

## 12. Files Excluded From Commit (Either Scope)

| File | Reason |
|---|---|
| `allure-report/` (project root) | Empty generated artifact, byproduct of an earlier malformed `allure generate` invocation, `total:0`, no real data. Not `.gitignore`d (only `build/` is), so it would be picked up by an unqualified `git add`. Must be explicitly excluded (or added to `.gitignore`, a change not made in this read-only phase). |
| `docs/jenkins/` | Pre-existing, confirmed unrelated to this work in the original Phase A audit; predates this engagement. |
| `docs/allure/PHASE_F_FINAL_CROSS_REPORT_VALIDATION.md` | Authored by Phase 5 Lab (frontmatter: `author: AI-Assisted Audit (Phase 5 Lab, read-only)`), not this session — see Part 13; committing it presumes crediting/accepting a document this session did not write. |

No secrets, credentials, personal paths, or environment-specific values were found in any modified or untracked file — `config.properties`'s `app.path` key is intentionally blank in source control (supplied via `-Dapp.path=` at invocation time), and no other key contains a token, password, or absolute local path.

## 13. Files Requiring User Decision

| File | Decision needed |
|---|---|
| `TestListener.java` | Commit as-is (accepting Phase 5 Lab's rework alongside the validated fix), or hold for a manual hunk-level split — not performed in this phase. |
| `CommonAssertions.java` | Same. |
| `ProductsPage.java`, `ProductsLocators.java` | Accept the `getCardPrice()` root-cause fix (functionally plausible, addresses exactly the flakiness Phase D documented, but not independently reviewed or stress-tested by this session) as part of this commit, or defer to a separately-reviewed change. |
| `FailureEvidenceCollector.java`, `PageSourceManager.java`, `PageSourceUtility.java`, and their four supporting config changes | Accept as an execution-unverified but source-reviewed-sound addition (Part 9's matrix), or defer until a real failure can exercise them. |
| `docs/allure/PHASE_F_FINAL_CROSS_REPORT_VALIDATION.md` | Include as documentation of Phase 5 Lab's own work (consistent if Phase 5 Lab's code is accepted), or exclude as not this session's product. |

## 14. Exact Proposed Commit Manifest

**COMMIT (Scope A — no ambiguity):**
```
.github/workflows/mobile-automation.yml
build.gradle
src/test/resources/allure.properties
src/test/java/com/mobileautomation/framework/pages/LoginPage.java
src/test/java/com/mobileautomation/framework/tests/LoginTest.java
docs/allure/PHASE_ALLURE_REPORTING_IMPLEMENTATION_REPORT.md
docs/allure/PHASE_ALLURE_FORENSIC_AUDIT_REPORT.md
docs/allure/PHASE_ALLURE_FORENSIC_AUDIT_REPORT_REAUDIT.md
docs/allure/PHASE_ALLURE_CLEAN_RESULT_LIFECYCLE_VALIDATION_REPORT.md
docs/allure/PHASE_C_ALLURE_SERVE_ZERO_RESULTS_ROOT_CAUSE_AND_FIX_REPORT.md
docs/allure/PHASE_C_CORRECTION_ALLURE_CLI_MIGRATION_AUDIT.md
docs/allure/PHASE_D_EXTENTREPORTS_FORENSIC_AUDIT_REPORT.md
docs/allure/PHASE_E_EXTENTREPORTS_MINIMAL_FAILURE_FIX_VALIDATION_REPORT.md
docs/allure/PHASE_FULL_SUITE_EXECUTION_REPORT.md
docs/allure/PHASE_G_FINAL_GIT_SCOPE_AUDIT.md
docs/allure/PHASE_H_CHANGE_OWNERSHIP_AND_WORKTREE_RECOVERY_AUDIT.md
docs/allure/PHASE_I_FAILURE_PATH_AND_EVIDENCE_VALIDATION_REPORT.md
docs/allure/PHASE_J_FINAL_COMMIT_SCOPE_AND_RELEASE_READINESS.md
```

**DO NOT COMMIT:**
```
allure-report/
docs/jenkins/
```

**USER DECISION REQUIRED:**
```
src/main/java/com/mobileautomation/framework/listeners/TestListener.java
src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java
src/test/java/com/mobileautomation/framework/pages/ProductsPage.java
src/main/java/com/mobileautomation/framework/locators/ProductsLocators.java
src/main/java/com/mobileautomation/framework/reporting/FailureEvidenceCollector.java
src/main/java/com/mobileautomation/framework/reporting/PageSourceManager.java
src/main/java/com/mobileautomation/framework/utils/PageSourceUtility.java
src/main/java/com/mobileautomation/framework/config/ConfigReader.java
src/main/java/com/mobileautomation/framework/constants/ConfigurationDefaults.java
src/main/java/com/mobileautomation/framework/constants/ConfigurationKeys.java
src/test/resources/config/config.properties
docs/allure/PHASE_F_FINAL_CROSS_REPORT_VALIDATION.md
```

No `git add` of any kind was performed. This is a proposal only.

## 15. Release-Readiness Verdict

**C — USER DECISION REQUIRED**, narrowly scoped:

- **The core Allure + ExtentReports + CI implementation (Scope A file list, Part 14) is READY TO COMMIT** — every one of those files is independently validated by this session's own physical-device execution evidence (Parts 4–5, 9), with no open question.
- **The GitHub Actions CI integration is ready to commit but not yet ready to declare "GitHub Actions validated"** — it has only been statically checked, never exercised by an actual workflow run (Part 6), which is expected since it hasn't been pushed. Pushing is the next step to close that gap, and remains gated on the user's own explicit approval, as it has been throughout this engagement.
- **The sole open uncertainty for the full working tree is the optional Phase 5 Lab enhancement** (the `FailureEvidenceCollector` failure-evidence subsystem and the `getCardPrice()` fix, plus their entanglement with `TestListener.java`/`CommonAssertions.java`). This is not a defect finding — Part 9's matrix and Phase I's source audit found the implementation internally sound — it is an **unexecuted, unreviewed** scope-inclusion question only the user can resolve.

If the user's only concern is getting the validated Allure/ExtentReports/CI work committed, **Scope A can proceed today** for every file except `TestListener.java`/`CommonAssertions.java`, which remain blocked on the Phase 5 Lab decision regardless of which way it's decided.

## 16. Remaining Known Issues

1. `TestListener.java`/`CommonAssertions.java` cannot be committed as pure Scope A without either accepting Phase 5 Lab's content or a manual `git add -p` split (not performed in this phase).
2. `FailureEvidenceCollector`'s screenshot/page-source capture and dual-report attachment remain **implemented but not execution-verified** — not proven defective, simply unexercised by any failure this session has observed (Phase I).
3. `ProductsPage.getCardPrice()`'s rewrite is a plausible, well-documented root-cause fix for previously-diagnosed flakiness, but has not been independently reviewed or stress-tested by this session beyond appearing in one passing 19/19 run.
4. The GitHub Actions Allure integration (`.github/workflows/mobile-automation.yml`) remains statically validated only — real-CI validation requires an actual push, which is outside this phase's scope and still requires explicit user approval, per this engagement's established practice.
5. `allure-report/` (project root, empty/stale generated artifact) should be deleted or added to `.gitignore` before any commit — not done in this read-only phase.
6. Native Gradle-plugin `allureServe` remains a known-broken convenience task (superseded, not fixed, by the standalone-CLI `allureLocalReport` workflow) — a pre-existing, already-disclosed limitation, not new to this phase.

---

**No file was modified, staged, committed, pushed, reverted, reset, stashed, or deleted during this phase, other than the creation of this report.** Stopping here per instruction, pending explicit user approval before any further action.
