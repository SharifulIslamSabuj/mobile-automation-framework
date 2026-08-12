---
document_id: PHASE-19.5B
title: Controlled Post-Integration Parallel CI Validation
version: v1.1
status: Final — Evidence Review Report (No Production File Changes), Addendum Added
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.5, PHASE-19.5A, PHASE-19.4J, PHASE-19.4M, PHASE-19.4O, PHASE-19.4P]
classification: Internal
---

# Phase 19.5B — Controlled Post-Integration Parallel CI Validation

---

## 1. Objective

Execute a bounded, deliberate sample of up to 5 designated sequential production CI runs, using the exact unmodified Phase 19.5 dual-path configuration (`main` at commit `cdc6c4c`, unchanged throughout), to obtain independent evidence of native/Docker path stability and agreement — the evidence Phase 19.5A found insufficient.

---

## 2. Scope

Evidence collection and review only. No file was modified — `main` remained at commit `cdc6c4c` (the Phase 19.5 integration commit, unchanged since) for the entire sample. All 5 runs were triggered via `workflow_dispatch` against that unchanged ref — no new commit or push was required or made. No failure was retried. No implementation was changed in response to any observed result.

---

## 3. Sample Design

5 designated sequential runs, triggered one at a time via `gh workflow run mobile-automation.yml --ref main`, each confirmed `completed` before the next was triggered. Per the stop rule: no early stop occurred for any passing run; the sample ran to its full bound of 5 regardless of the Run 5 result (Section 6).

---

## 4. Run-by-Run Results

| Run | Run ID | Trigger | Overall Conclusion | Native Exit | Docker Exit | Native Tests | Docker Tests | Native Duration | Docker Duration |
|---|---|---|---|---|---|---|---|---|---|
| 1 | `31455527318` | `workflow_dispatch` / `cdc6c4c` | success | 0 | 0 | 19/19, 0F/0E | 19/19, 0F/0E | 10m20s | 10m4s |
| 2 | `31457462612` | `workflow_dispatch` / `cdc6c4c` | success | 0 | 0 | 19/19, 0F/0E | 19/19, 0F/0E | 11m24s | 10m52s |
| 3 | `31459007580` | `workflow_dispatch` / `cdc6c4c` | success | 0 | 0 | 19/19, 0F/0E | 19/19, 0F/0E | 11m53s | 11m22s |
| 4 | `31460587791` | `workflow_dispatch` / `cdc6c4c` | success | 0 | 0 | 19/19, 0F/0E | 19/19, 0F/0E | 11m57s | 11m26s |
| 5 | `31462347740` | `workflow_dispatch` / `cdc6c4c` | **failure** | **1** | **0** | **18/19, 1F/0E** | **19/19, 0F/0E** | 10m38s (FAILED) | 10m21s |

(F/E = failures/errors, per `testsuite` JUnit XML attributes.)

---

## 5. Native Quality Gate Behavior — Verified Correct in All 5 Runs

In every run, the job's overall conclusion exactly tracked `native_exit`: `success` in Runs 1–4 (native `0`), and **`failure` in Run 5** (native `1`) — the native path continued to behave as the sole, unweakened quality gate exactly as designed, including under a genuine failure.

---

## 6. Run 5 — The One Failure, In Detail

**Failing test**: `CartTest.accessCartScreen` (TC-014), native path only. Docker's own execution of the identical test, in the same workflow run, passed (`test-results` confirms 13/13 `CartTest` methods passed on Docker, including `accessCartScreen`).

**Exception**: `com.mobileautomation.framework.exceptions.ElementActionException: Element action 'click' failed on locator: By.xpath: //*[@text='Sauce Labs Backpack (violet)']/parent::*/*[@content-desc='Product Image']` — a standard framework click-action failure (an underlying Selenium/Appium timeout wrapped by the framework's own exception type), not an application crash exception.

**Timeline** (from `automation.log`, native path):

| Time | Event |
|---|---|
| 05:42:19.812 | "Initializing driver for test method" |
| 05:42:29.415 | TEST START (readiness succeeded, ~9.6s later — unremarkable, within normal range) |
| 05:42:31.074 | **Assertion passed**: "Product Catalog screen: expected to be visible" — the AUT was directly confirmed visible and correct at this point |
| 05:42:35.920 | "Performed 'findAll' on element: Product Title" — a further successful interaction |
| 05:42:43.760 | Test failed — the click on the specific product's image locator could not be completed |
| 05:42:44.905 | Failure screenshot captured |

**Screenshots**: both `accessCartScreen_failure_...png` and `element_action_click_failure_...png` show the **Android home launcher** — not the AUT, not a partial/transitional UI state.

---

## 7. Failure Classification (Phase 19.4M)

**INFERRED — not VERIFIED, not NOT VERIFIED, not bare UNKNOWN.** Reasoning:

- **Why not `UNKNOWN`**: real, specific, matching evidence exists — a launcher screenshot (twice), directly consistent with the exact symptom Phase 19.4J's `VERIFIED` instance exhibited, plus a timeline showing the AUT was genuinely healthy and interactive only ~8–13 seconds before the failure (an assertion for catalog visibility passed, and a `findAll` succeeded) — this is materially more than "no evidence."
- **Why not `VERIFIED`**: Phase 19.4M's evidence standard requires **both** an Android logcat `FATAL EXCEPTION` attributable to the AUT process **and** confirmed process termination within the failure window. **The production `mobile-automation.yml` workflow does not capture logcat or host-side process state at all** — that capability exists only in the temporary diagnostic workflows used in Phases 19.4H–19.4J, all removed after use, and was deliberately **not** part of the Phase 19.5 integration (Phase 19.4N's diagnostics remain unimplemented, per Phase 19.4P's own explicit decision). Neither required item is available for this run. Per this phase's own explicit instruction ("Do not classify a launcher screenshot or timeout alone as `EXTERNAL_AUT_CRASH`"), this failure is **not** upgraded to `VERIFIED` despite the strong circumstantial match.
- **Why not `NOT VERIFIED`**: nothing in the available evidence contradicts the known crash mechanism — there is no indication the AUT remained running and healthy at failure time; the opposite (launcher visible) is what was observed.

**Failure category**: `AUT-related`, specifically `INFERRED EXTERNAL_AUT_CRASH` under Phase 19.4M's taxonomy — not `DOCKER_INFRASTRUCTURE_FAILURE`, not `CONTAINER_RUNTIME_FAILURE`, not `APPIUM_CONNECTIVITY_FAILURE` (the Docker path's own Appium session, against the same host Appium server, worked perfectly moments later in the same run), and not a `FRAMEWORK_FAILURE` in the sense of a defect in this project's own code (the `ElementActionException` is the framework correctly reporting that an expected UI element could not be interacted with — the framework behaved exactly as designed given what the device actually showed).

**Shared, native-only, or Docker-only?** **Native-only.** The Docker path, executing the identical test against the same emulator and same host Appium server (per the Phase 19.5 design), passed cleanly in this same run.

---

## 8. Docker Non-Blocking Mechanism — Verified Correct Under Genuine Divergence

Run 5 is the first observed instance, across the entire Phase 19.5/19.5A/19.5B history, where the two paths actually disagreed. The mechanism behaved exactly as designed (Phase 19.5, Section 5 of that report):

- `DOCKER_TEST_EXIT=0` was correctly captured and did not cause the action to fail-fast (the script continued to its final line as normal).
- `NATIVE_TEST_EXIT=1` was correctly restored as the job's real outcome (`exit "${NATIVE_EXIT}"`), and the job's conclusion was `failure` — the native failure was **not hidden**.
- The Docker path's own full evidence (19/19 passing JUnit XML, screenshots, logs) was uploaded as its own artifact, fully intact and independently inspectable, not overwritten by or combined with the native (failing) artifact.
- No retry occurred on either path.

---

## 9. Required Analysis

1. **Docker path stability**: 5/5 passed. No Docker-only failure occurred in this sample.
2. **Native path stability**: 4/5 passed, 1/5 failed (Run 5) — matches the known, pre-existing intermittent pattern (Phase 19.4F/19.4J), not a new instability.
3. **Agreement/disagreement between paths**: 4/5 runs in full agreement (both pass); 1/5 runs in disagreement (native fails, Docker passes).
4. **Docker non-blocking mechanism**: behaved correctly in all 5 runs, including the one disagreement case (Section 8) — this is the first time this specific behavior (mechanism under genuine divergence) has been directly observed, and it worked as designed.
5. **Docker-only failures**: none.
6. **Native-only failures**: one (Run 5), classified `INFERRED` per Section 7.
7. **`VERIFIED EXTERNAL_AUT_CRASH`**: none — Run 5 does not meet the evidence bar (Section 7).
8. **Phase 19.5 production integration validity**: remains technically valid — every mechanism it introduced (parallel execution, exit-code capture/restoration, independent artifact collection, honest non-hiding of failures) performed correctly across all 5 runs, under both agreement and disagreement conditions.
9. **Sufficient evidence for Docker-primary migration**: **no** — explicitly out of scope for this phase regardless of outcome, and substantively: a 5-run sample with one observed divergence is not a basis for a migration decision in either direction; if anything, Run 5 (Docker passing while native failed) is a single data point and must not be read as "Docker is more reliable" — that would be an unsound generalization from n=1.

---

## 10. What This Review Distinguishes

- **Docker technical qualification** (Phase 19.4L): unaffected, unchanged, not re-tested by this phase.
- **Phase 19.5 integration success**: reaffirmed and strengthened — the mechanism now has 6 total observed runs (1 from Phase 19.5 itself + 5 here), 6/6 correct behavior of the non-blocking design, including the one case where it actually mattered (Run 5's divergence).
- **Individual CI run outcomes**: 4 clean double-passes, 1 native-only failure — recorded exactly as they occurred, with the failure fully preserved (JUnit XML, screenshots, logs) and never retried.
- **Run-to-run stability**: native shows a 4/5 (80%) pass rate in this sample; Docker shows 5/5 (100%). Neither figure is treated as a reliable long-run rate estimate from a 5-run sample (consistent with this engagement's established statistical discipline, e.g. Phase 19.4F Section 20).
- **External AUT reliability**: unchanged from Phase 19.4J/19.4K/19.4O/19.4P — Run 5 is consistent with, but not newly proof of, the known limitation; it neither confirms a specific rate nor introduces a new mechanism.

---

## 11. Explicit Non-Claims

This report does not claim: that AUT reliability is fixed or improved because 4 of 5 runs passed cleanly; that Docker is more reliable than native because it happened to pass when native failed once; that Run 5's failure is `VERIFIED EXTERNAL_AUT_CRASH` (it is not, per Section 7); that a Docker-specific defect was found (none was — Docker passed 5/5); or that this evidence is sufficient to decide on Docker-primary migration (explicitly out of scope and not recommended here).

---

## 12. Risks and Limitations

- Production CI still cannot capture logcat or host-side process state — Run 5's failure, like the three earlier symptom-matching instances noted in Phase 19.4M, will likely never be upgradable to `VERIFIED` after the fact. This is the same, unaddressed evidence gap Phase 19.4N described and Phase 19.4P deliberately left unimplemented.
- The sample size (5 runs, 1 divergence) is too small to estimate a stable native failure rate; it is consistent with, but does not refine, Phase 19.4F's own earlier (and equally small) 1-in-5 figure.
- Two of the artifact downloads for this phase's own evidence-gathering encountered transient local network errors (unrelated to GitHub or the workflow itself) and required retries — noted for completeness, not a finding about the CI system.

---

## 13. Recommended Next Step

None beyond what Phase 19.5/19.5A already recommended: continue observing organic and/or further deliberate samples over time to build a larger evidence base, particularly toward eventually estimating a more reliable native/Docker failure-rate comparison. No action is required as a result of this specific sample — the integration remains valid, the one failure was handled exactly as designed, and it was correctly and honestly reported rather than hidden or retried.

---

## 14. Final Verdict

# A. PARALLEL CI VALIDATION VERIFIED

Across 5 designated, independent, unmodified production CI runs, the Phase 19.5 dual-path integration mechanism performed correctly in every case — including the one run (Run 5) where the native and Docker paths genuinely disagreed, which is the most rigorous test this design has faced to date. The native path correctly remained the sole quality gate (job conclusion tracked native exit code in all 5 runs). The Docker path never blocked, never hid, and never had its own result altered — it failed 0/5 times and passed 5/5 times. The one observed failure (native-only, Run 5) was preserved in full, not retried, and classified `INFERRED` (not `VERIFIED`) per Phase 19.4M's evidence standard, since production CI does not currently capture the logcat/process evidence required for a `VERIFIED` classification. This verdict certifies the **integration mechanism's** validity — it does not certify AUT reliability, which remains a known, separate, unresolved external limitation, and it does not constitute evidence for or a recommendation toward Docker-primary migration.

---

## 15. Addendum — Supplemental Run 5 Confirmation

**Added**: 2026-08-11, after the original Phase 19.5B sample and report (Sections 1–14) were already complete and finalized. **This addendum does not modify, replace, or reclassify anything in Sections 1–14.** The original Run 5 record (Section 6, Section 7's `INFERRED` classification, and the `PARALLEL CI VALIDATION VERIFIED` verdict in Section 14) stands exactly as originally written.

### 15.1 Motivation

The user monitored the original Run 5 locally, and their PC entered sleep partway through that monitoring session. This addendum executes exactly one additional, otherwise-identical CI run — with the local machine kept awake and actively monitored throughout — to check whether the native `CartTest.accessCartScreen` failure reproduces under that condition.

### 15.2 Run Configuration

Identical to the original 5-run sample: `main` at commit `cdc6c4c` (unchanged — no source, Dockerfile, `.dockerignore`, workflow, AUT, version, timeout, retry, or environment setting was modified before, during, or after this run), triggered via `workflow_dispatch`. No commit or push was made. This was the only additional run triggered; no run followed it.

### 15.3 Supplemental Run Evidence

| Field | Value |
|---|---|
| GitHub Actions run ID | `31479456331` |
| Commit SHA | `cdc6c4cc45e449a0f753e3a6ec2dbb07e1fae9c6` (`cdc6c4c`) |
| Trigger | `workflow_dispatch` |
| Overall workflow conclusion | `success` |
| Native exit code | `0` |
| Docker exit code | `0` |
| Native test result | **PASS** — 19/19, 0 failures, 0 errors (`CartTest` 13/13 incl. `accessCartScreen`; `LoginTest` 1/1; `NavigationTest` 1/1; `ProductDetailsTest` 4/4) |
| Docker test result | **PASS** — 19/19, 0 failures, 0 errors (identical breakdown) |
| Native duration | 11m 33s (`BUILD SUCCESSFUL`) |
| Docker duration | 11m 1s (`BUILD SUCCESSFUL`) |
| Total job duration | 25m 27s (09:49:45–10:15:12 UTC) |
| Failed test(s) | **None** |
| Failure screenshots/logs/artifacts | **None** — all 83 native-path and equivalent Docker-path screenshots are routine step-evidence captures (no `*fail*`-named files); no JUnit XML in either artifact reports `failures` or `errors` > 0 |
| Native/Docker agreement | **Agree** — both passed |

`accessCartScreen` specifically: native JUnit XML records `<testcase name="accessCartScreen" ... time="21.398"/>` with no failure/error child element, and `automation.log` records an explicit `Test passed: accessCartScreen` line.

### 15.4 Direct Answers

**A. Did the native `CartTest.accessCartScreen` failure reproduce?**
No. It passed cleanly (21.398s), with no anomaly in the surrounding timeline.

**B. Did Docker again pass 19/19?**
Yes — the 6th consecutive clean Docker-path result across Phase 19.5B (5 original runs + this one).

**C. Does keeping the local PC awake appear to have any relationship to the failure?**
No plausible relationship, for an architectural reason established independently of this single non-reproduction: the workflow's job runs on `runs-on: ubuntu-24.04` — a **GitHub-hosted cloud runner** (confirmed directly in `.github/workflows/mobile-automation.yml:52`), not a self-hosted runner on the user's machine. Once `workflow_dispatch` triggers the job, execution (emulator provisioning, Appium, the native and Docker test runs) proceeds entirely on GitHub's own infrastructure. The local PC's role is limited to having issued the trigger and, optionally, polling GitHub's API to observe status — it is not in the execution path. There is no mechanism by which the local machine's power state could affect a test's outcome on that remote runner. The one non-reproduction observed here is consistent with that architectural fact, but is not itself proof of "no relationship" — it is one data point, and the underlying intermittent native-only pattern (Phase 19.4F, 19.4J, and original Run 5) was already established well before this addendum, on runs where no PC-sleep event was reported at all.

**D. Is there evidence the original failure was caused by local monitoring interruption, or did the GitHub Actions run independently complete normally?**
The evidence supports the latter, not the former. The original Run 5 (`31462347740`) is recorded as `status: completed`, and produced a complete, well-formed result: a full job log, a specific Gradle failure (`BUILD FAILED in 10m 38s`), a specific failing test with stack trace and two screenshots, and fully uploaded native and Docker artifacts (the Docker artifact itself 19/19 clean, from the same run) — this is the output of a CI job that ran to completion normally, not one that was interrupted or truncated. Nothing in that evidence resembles an infrastructure-level interruption (e.g., a runner disconnect, a cancelled job, a network-severed step); it resembles a genuine test-execution failure on one test method, consistent with the same `INFERRED EXTERNAL_AUT_CRASH`-pattern symptom (launcher screenshot, prior healthy interaction) documented for other instances in this engagement (Phase 19.4M) that occurred with no PC-sleep event reported at all. Combined with the point C architecture fact (the runner does not depend on the local machine once triggered), there is no evidence supporting local-monitoring interruption as a cause.

**E. Does the new evidence change the Phase 19.5B conclusion?**
No. The original Run 5 is preserved exactly as recorded (Sections 6–7) and remains classified `INFERRED` — this addendum's clean result does not upgrade, downgrade, or remove that classification, since the evidence gap that prevented a `VERIFIED` classification (no logcat capture, no host-side process-state capture in production CI) is unchanged and unaffected by any subsequent run. The supplemental run simply adds a 6th data point: native now 5/6 (≈83%) pass, Docker 6/6 (100%) across the full Phase 19.5B evidence base — a small-sample figure, not a reliable rate estimate, and consistent with (not a refinement of) the original conclusion. The Section 14 verdict, `A. PARALLEL CI VALIDATION VERIFIED`, was a certification of the **integration mechanism's** correctness (exit-code capture/restoration, non-blocking behavior, independent artifact handling), not a claim about AUT reliability or about Docker being more reliable than native — both of which this addendum explicitly declines to claim, per Section 15.5.

### 15.5 Explicit Non-Claims (Addendum-Specific)

This addendum does not claim: that the original Run 5 failure was caused by, or related to, the local PC entering sleep (no supporting evidence, and no plausible mechanism given the cloud-hosted runner architecture); that the underlying native-path intermittent failure is resolved (one clean run does not establish this, and the same pattern has recurred across multiple unrelated runs throughout this engagement); or that Docker is more reliable than native (6/6 vs. 5/6 on a 6-run sample is not a statistically meaningful comparison).

### 15.6 Stop Confirmation

Exactly one supplemental run was executed, matching the user's explicit instruction. No further CI run was triggered after it. No source code, Dockerfile, `.dockerignore`, production workflow, AUT APK, version, timeout, retry, or environment setting was changed at any point (`git status`/`git diff` confirm `main` unchanged at `cdc6c4c`; only this report file and its supporting scratch evidence were touched). No commit or push was made. No Docker-primary migration, Phase 19.6, or native-path removal was started.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Document Status | Final — Evidence Review Report (No Production File Changes) | — | — |

---

**End of Document — Phase 19.5B Controlled Post-Integration Parallel CI Validation Report, v1.1 (Section 15 addendum added post-completion)**
