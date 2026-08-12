---
document_id: PHASE-19.4N
title: Framework-Level AUT Crash Diagnostics Design
version: v1.0
status: Final — Design Report (No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4B, PHASE-19.4E, PHASE-19.4G, PHASE-19.4H, PHASE-19.4I, PHASE-19.4J, PHASE-19.4M]
classification: Internal
---

# Phase 19.4N — Framework-Level AUT Crash Diagnostics Design

---

## 1. Objective

Design — without implementing — the smallest practical diagnostic mechanism capable of closing the evidence gap Phase 19.4M identified: production CI currently cannot reliably capture the two pieces of evidence (AUT logcat `FATAL EXCEPTION` + confirmed process termination) that classification standard requires for a `VERIFIED EXTERNAL_AUT_CRASH` finding.

---

## 2. Scope

Design and evidence-review only. No source file, `Dockerfile`, `.dockerignore`, production CI workflow, or AUT file was modified to produce this report. No new CI run was executed. This phase reuses evidence already collected in Phases 19.4E–19.4M; it does not re-run any experiment.

---

## 3. Evidence Baseline

- **Phase 19.4G**: a continuous, background, in-JVM diagnostic thread sharing the test's own Appium session caused severe interference — test duration inflated from a healthy ~30s to ~108.6s average (~3.6×), and every attempted logcat capture (10/10 checkpoints) failed.
- **Phase 19.4H**: an independent, host-side, out-of-process ADB observer (continuous `adb logcat` + 1s-interval `dumpsys`/`pidof` polling, started/stopped by the CI workflow script itself, never touching the Appium session) achieved negligible interference (test duration ~29.4s average, matching the healthy baseline) and 100% logcat capture success (5/5 runs).
- **Phase 19.4I**: discrete, single, main-thread Appium calls (`getText()`/`findElements()`) at defined checkpoints, issued synchronously by the test's own thread (not a background thread), also showed negligible interference (~29.6s average) — establishing that it is *continuous background contention*, not "using the Appium session" per se, that Phase 19.4G's finding actually indicts.
- **Phase 19.4J**: the only run with a complete evidence capture, obtained via the Phase 19.4H architecture; the AUT process crash (06:14:56.852) occurred **~27 seconds before** the test's own assertion failure was reported (06:15:24.046) — the crash predates failure detection by a wide margin.
- **Phase 19.2/19.3 (Docker Model 3 architecture, re-affirmed, not re-investigated)**: the Docker container contains only Java 17 and the Gradle wrapper — no Android SDK, no `adb`, no Appium, no emulator. These remain host-managed. This is a hard architectural constraint on any diagnostic design that runs inside the container/JVM.

---

## 4. Current Diagnostic Gap

Production `mobile-automation.yml` does not run any host-side observer and the framework itself (`TestListener`, `ScreenshotManager`) captures only a screenshot on test failure — no AUT process state, no logcat, no crash-attribution evidence. This means a future recurrence of the Phase 19.4J mechanism would, under production CI as it exists today, produce only a generic assertion-failure screenshot — insufficient, per Phase 19.4M's own evidence standard, to classify the failure as `VERIFIED EXTERNAL_AUT_CRASH`. It would default to `UNKNOWN` or, at best, `INFERRED`.

---

## 5. Existing Architecture Review

| Extension point | What it does today | Diagnostic collection feasibility |
|---|---|---|
| **A. `AndroidDriverFactory`** | Creates the driver, runs the Phase 19.4B readiness check, then returns control | Feasible for a one-time, T0-adjacent snapshot (as Phase 19.4E proved), but this location cannot see a crash that occurs later in the test body — it has already returned by then |
| **B. TestNG `ITestListener`** (`TestListener.java`) | `onTestStart`/`onTestSuccess`/`onTestFailure`/`onTestSkipped` — already logs and, on failure, calls `ScreenshotManager.captureScreenshot()` | **The correct, already-proven, already-in-production hook for failure-triggered, in-JVM diagnostic collection** — it already does exactly this pattern for screenshots |
| **C. `@AfterMethod` failure hook** (`BaseTest.quitDriver()`) | Unconditionally quits the driver after every test method, `alwaysRun = true` | Runs *after* `TestListener.onTestFailure` in TestNG's own listener/hook ordering; by this point the driver is about to be torn down — not the ideal place to add new diagnostic logic, since `TestListener` already fires first and is purpose-built for this |
| **D. `ScreenshotManager`/reporting utility** | Failure-triggered, already defensively coded (never throws, converts failures to logged warnings) | The correct **pattern** to imitate for any new diagnostic call, not necessarily the class to extend directly (single-responsibility: screenshots vs. AUT-state snapshots) |
| **E. Gradle execution wrapper** | Invoked once per `docker run`, no per-test granularity | Not suitable — operates above the level of individual test failures |
| **F. GitHub Actions host-side observer** | Does not exist in production; existed only as temporary diagnostic workflows in Phases 19.4H/19.4I/19.4J, always removed after use | **Proven architecture (Section 3), not yet made permanent** |
| **G. Other extension points** | None identified beyond A–F in the existing codebase | — |

---

## 6. Diagnostic Architecture Options

| | A: Through Appium session | B: Direct ADB from framework | C: Host-side CI observer | D: Failure-triggered GH Actions step | E: Hybrid |
|---|---|---|---|---|---|
| Capture AUT process termination | Possible via `queryAppState`/`getCurrentPackage`, but only if the session itself survives the crash | **Blocked under Docker Model 3** — no `adb` binary exists inside the container (Section 3) | **Proven** (Phase 19.4H/19.4J) | Same mechanism as C, gated by exit code | Combines A (context) + C (evidence) |
| Capture logcat `FATAL EXCEPTION` | **NOT VERIFIED as reliable** — Phase 19.4G's `mobile: shell "sh -c logcat..."` attempts failed 10/10, exact cause (command syntax vs. contention) not isolated | Blocked, same reason as above | **Proven, 100% success** (Phase 19.4H/19.4J) | Depends on buffer retention at trigger time — **UNKNOWN**, untested (Section 8) | Uses C for this |
| Compatible with Docker Model 3 | Yes (routes through Appium, not a container-local `adb`) | **No** | Yes (host-side, outside the container) | Yes | Yes |
| Windows compatibility | Yes | Only if `adb` is on the local host running the JVM (true for non-Docker local dev; not the Docker-container case) | Yes, `host.docker.internal` topology already established | Yes | Yes |
| GitHub-hosted Linux compatibility | Yes | No (container has no `adb`) | **Proven** (Phase 19.4H/19.4J) | Yes | Yes |
| Effect on future BrowserStack/Sauce Labs integration | Portable in principle, but depends on `--relaxed-security` being available on the provider's Appium server — **NOT VERIFIED** for any specific provider | Not portable (no direct device/host access assumed) | Not portable as-is — depends on host-level device access this project has only verified for local/GitHub-hosted emulators (Section 9) | Same as C | Same limitation as C for the host-side component; the Appium-session component (A) remains available regardless of provider |
| Risk of interfering with test execution | Low if discrete/failure-triggered (Phase 19.4I precedent); high if continuous (Phase 19.4G) | N/A (blocked) | **Proven negligible** (Phase 19.4H/19.4J), including when continuous | Same as C | Low, if each component follows its own proven-safe pattern |
| Implementation complexity | Low (one new method, reusing existing driver access) | N/A (blocked) | Low-medium (a workflow script addition, already exists as a template from 19.4H/19.4J) | Low-medium, same script, gated | Medium — two small, independent pieces |
| Diagnostic reliability | Medium (session health at crash time is itself uncertain) | N/A | **High, directly proven** | Uncertain for logcat specifically (Section 8) | High (relies on the proven parts) |
| Evidence exists at moment of failure? | Only what the session can still report post-crash | N/A | Yes, if capture is continuous from before the test starts | Uncertain — depends on buffer retention (Section 8) | Yes |
| Maintenance cost | Low | N/A | Low-medium (one workflow section, already precedented 3 times in this engagement) | Same as C | Low-medium |

---

## 7. Evidence Preservation Requirements

Minimum evidence to collect on a relevant failure, mapped to Phase 19.4M's evidence standard:

| Item | Source | Required by Phase 19.4M? |
|---|---|---|
| Failing test identifier | TestNG `ITestResult` (already available in `TestListener`) | Correlation metadata |
| Failure timestamp | Existing SLF4J log timestamp (already produced on every log line) | Correlation metadata |
| AUT package name | Already known (`ConfigReader`/`CapabilityConfiguration`) | Context |
| AUT process state at/near failure | Host-side `pidof`/`dumpsys` (Option C) | **Required (Section 7A item 2, Phase 19.4M)** |
| Foreground/focused package | Host-side `dumpsys window`/`dumpsys activity activities` (Option C) | Strong supporting evidence |
| Relevant logcat window incl. `FATAL EXCEPTION` | Host-side continuous `adb logcat` (Option C) | **Required (Section 7A item 1, Phase 19.4M)** |
| Exception/stack-trace attribution | Same logcat capture, parsed/filtered | Required, part of item 1 |
| Screenshot | Already captured today (`ScreenshotManager`, unchanged) | Strong supporting evidence |
| Appium/session state at failure | A single framework-side snapshot call at the existing `TestListener.onTestFailure` hook | Strong supporting evidence / correlation |

Per Task 3's own instruction to avoid unnecessary always-on telemetry: the framework-side component (Appium-session snapshot) should be **failure-triggered only**, mirroring the existing screenshot hook exactly. The host-side component (logcat + process-state) is the one exception recommended to run **continuously for the duration of each test**, not failure-triggered — justified specifically by Section 8's finding that a purely reactive, post-hoc dump cannot be shown, on this project's own evidence, to reliably still contain the crash event.

---

## 8. Failure-Time Window Analysis

Phase 19.4J's crash (06:14:56.852) occurred roughly 27 seconds before the test's assertion failure was reported (06:15:24.046) — proving that **collecting evidence only after a failure is detected is not sufficient by itself**, unless the evidence source's own retention window is known to reliably reach back that far.

This project's *proven* method (Phase 19.4H/19.4J) is a **continuous** capture that begins before the test starts and runs throughout — this is known to work because it was directly observed working (the crash was captured). A purely **post-hoc** design (only run `adb logcat -d` once a failure is detected) was never tested in this engagement. Android's `logcat` ring buffer is not cleared automatically, so a post-hoc dump would likely still contain an event from ~27–30 seconds earlier under típical buffer-fill conditions — but this project has no direct evidence of the buffer's actual retention window under CI load, and buffer contents can be overwritten faster under heavier logging activity. This is marked:

**UNKNOWN — requires validation.** What would need to be checked: whether a single `adb logcat -d` issued only at test-failure time (with no prior `-c` clear and no continuous capture) still contains a crash event from ~30 seconds earlier, under representative CI load. Until validated, the continuous-capture design (already proven) is the only one this report can recommend with evidence, not the cheaper reactive alternative.

---

## 9. Cloud Compatibility Boundary

| Environment | ADB/logcat access from the execution host? | Evidence |
|---|---|---|
| **A. Local emulator** (non-Docker) | Likely yes — in this framework's non-Docker execution mode, the JVM, Appium, `adb`, and emulator have historically run on the same local machine, so a host-side (or even framework-side) `adb` call would reach the device directly | Not independently re-verified this phase; consistent with this project's pre-Docker architecture, not contradicted by any evidence |
| **B. GitHub-hosted emulator** (current Docker CI) | **Yes — proven** | Phase 19.4H, 19.4J: direct, repeated, successful `adb`/logcat access from the GitHub Actions runner host, independent of the Docker container |
| **C. BrowserStack** | **NOT VERIFIED** | This project has never connected to BrowserStack; whether/how device logs or crash logs are exposed (via BrowserStack's own dashboard/API, not necessarily direct `adb`) is unknown to this project |
| **D. Sauce Labs** | **NOT VERIFIED** | Same as BrowserStack — no direct evidence; Sauce Labs' own log/artifact retrieval mechanisms, if any, have not been investigated |
| **E. Selenium/Appium Grid or equivalent remote infra** | **Environment-specific, NOT VERIFIED** | Depends entirely on whether a given Grid node has host-level device access (self-hosted) or is a black-box remote endpoint; cannot be generalized |

**Design implication (not implemented this phase)**: the diagnostic *collection mechanism* should be architected as environment-pluggable — a local/GitHub-hosted-CI implementation using the proven host-side ADB approach today, with the explicit expectation that a cloud-provider migration would require a **different**, provider-specific evidence-retrieval implementation (e.g., their own API/dashboard log pull) rather than assuming today's mechanism travels unchanged. This report does not design that abstraction — it only notes that the requirement exists, per Task 5's own instruction not to over-commit today's mechanism to tomorrow's unverified environments.

---

## 10. Production Impact

**Recommendation: (C) enabled only on failure for the framework-side component; continuous-but-passive for the host-side component, in CI only.**

- The framework-side Appium-session snapshot (Section 7, last row) should fire **only** inside the existing `TestListener.onTestFailure` hook — identical trigger condition to the screenshot capture already there. A passing test is completely unaffected, exactly as it is today.
- The host-side observer (logcat + process-state polling) is recommended to run **continuously during the test step, but only in CI** (the same scoping the temporary diagnostic workflows already used) — not in every possible execution mode, and not as a framework/JVM-level always-on feature. Per Phase 19.4H/19.4I's own measurements, "continuous" here does not mean "harmful" — negligible interference was directly measured, not assumed.
- Neither component should be "always enabled" at the framework level (Option A) — that would reintroduce exactly the always-on overhead Task 3 warns against, and there is no evidence such a broad scope is needed (Section 7 shows an equally effective, narrower scope).
- The design must not reduce the reliability of healthy runs: the recommended scoping ensures a passing test triggers zero new framework-side code paths, and the host-side component's own negligible-interference property is directly evidenced, not assumed.

---

## 11. Recommended Architecture

**Option E — Hybrid**, composed of two independently-proven, narrowly-scoped pieces:

1. **Primary evidence source**: the Phase 19.4H/19.4J host-side ADB observer (Option C), made a **permanent, always-run** part of the CI workflow (not a temporary diagnostic workflow) — continuous `adb logcat` + 1s-interval `dumpsys`/`pidof` polling for the duration of the test step, artifacts uploaded (unconditionally, or gated to `if: failure()` to reduce storage — a decision left open, Section 16).
2. **Correlation/context evidence source**: a minimal extension of the already-existing, already-production `TestListener.onTestFailure` hook — one additional, defensively-coded, failure-triggered call capturing the current AUT package's `queryAppState`/foreground-package snapshot at the moment TestNG detects the failure, logged alongside the existing screenshot.

**Why preferred**: both halves reuse mechanisms already proven in this exact codebase and this exact CI environment — no new, unproven technique is introduced. The split respects the hard Docker Model 3 constraint (no `adb` inside the container, ruling out Option B outright) and Phase 19.4G's own hard-won lesson (no continuous, in-JVM, session-sharing background polling).

**Evidence supporting the decision**: Sections 3, 6, 7, 8 — every claim above traces to a specific, already-completed phase in this engagement, not a generic best practice.

**Files that would need modification** (if a future phase implements this — not this phase):
- `.github/workflows/mobile-automation.yml` (add the host-observer start/stop steps around Gate 7-8/9-10, matching the proven 19.4H/19.4J script pattern, made permanent instead of temporary).
- `src/main/java/com/mobileautomation/framework/listeners/TestListener.java` (extend `onTestFailure` with one additional, defensively-coded diagnostic call).

**New files**: possibly one new, small, single-responsibility class (e.g., a reporting-layer `AutStateSnapshot` or similar, mirroring `ScreenshotManager`'s own never-throws pattern) to keep `TestListener` from accumulating unrelated diagnostic logic directly — a design preference, not a requirement.

**Expected behavior on a normal passing test**: no change. Neither new code path executes (framework side is failure-triggered only); the host-side observer runs but has proven negligible measurable effect.

**Expected behavior on a normal assertion failure (AUT still running)**: existing screenshot capture unchanged; the new framework-side snapshot would show the AUT process/package still present — itself useful evidence *against* `EXTERNAL_AUT_CRASH`, correctly steering classification toward `TEST_ASSERTION_FAILURE`/`FRAMEWORK_FAILURE` per Phase 19.4M's matrix.

**Expected behavior on a verified AUT crash**: the host-side capture would show the same signature Phase 19.4J directly observed (logcat `FATAL EXCEPTION`, process-state transition to `NOT_RUNNING`); the framework-side snapshot would likely show the diagnostic call itself failing or returning an unexpected state — additional corroborating (not independently sufficient) evidence.

**Expected behavior when diagnostics themselves fail**: both components must follow `ScreenshotManager`'s existing pattern exactly — catch, log a warning, never throw, never mask or alter the real test result. The host-side script must use the same `|| true`-style guards Phase 19.4H's script already used.

**Rollback strategy**: both additions are isolated and independently revertible via `git checkout <baseline> -- <file>` followed by an empty-diff verification and recompile — the exact procedure already performed successfully more than five times across Phases 19.4E–19.4J.

---

## 12. Minimal Implementation Proposal

(Restated compactly per Task 7's own numbered list, already answered in full in Section 11: selected architecture = Hybrid Option E; rationale = Section 11 "Why preferred"; evidence = Sections 3/6; files = Section 11; new files = Section 11; behavior on pass/fail/crash/diagnostic-failure = Section 11; rollback = Section 11.)

---

## 13. Phase 19.4B Readiness Fix Review

Per Task 8's own options, the evidence supports **(D) — remain unchanged in behavior, but be reclassified/documented differently.**

- **(A) remain unchanged — in behavior, yes.** No evidence collected in Phases 19.4C–19.4M suggests the 19.4B check is *incorrect* for the specific mechanism it targets (Phase 19.4A's post-boot session-creation race). It should not be modified on that basis.
- **(B) revised — not evidenced.** No phase has identified a specific defect *in the check's own logic* that a revision would fix; the check does exactly what Phase 19.4A's evidence called for.
- **(C) removed — not evidenced, and actively contraindicated.** Removing it would reopen the original 19.4A race with no replacement safeguard; nothing in Phases 19.4C–19.4M suggests that race no longer occurs.
- **(D) remain but be reclassified/documented differently — supported.** The check's own code comment (`AndroidDriverFactory.java`) currently frames it as addressing "the verified root cause" of AUT-not-visible failures generally, citing Phase 19.4A. Phase 19.4J's evidence shows a **second, distinct** root cause (the AUT crash) exists for the same broad symptom family, occurring well after this check has already passed and returned control. The check remains correct and necessary for *its own* mechanism; its documentation should be updated (in a future, separately-scoped phase — not this one) to clarify it addresses only the T0 readiness race, not the full "AUT not visible" symptom space, so that its presence is not mistaken for a complete guarantee.

---

## 14. Risks

- **Storage/noise cost** of always uploading host-observer artifacts, even for passing runs, if not gated to failure-only (an open implementation decision, Section 16).
- **False confidence risk**: even with this design implemented, a failure lacking the Required evidence (Phase 19.4M Section 7A) must still be classified `UNKNOWN`/`INFERRED`, never assumed `VERIFIED` merely because the mechanism exists — this design increases the *chance* of capturing sufficient evidence; it does not guarantee it for every future occurrence.
- **Cloud-provider migration risk**: if the framework moves to BrowserStack/Sauce Labs/Grid before a provider-specific evidence mechanism is designed, this Hybrid architecture's host-side half would need to be replaced or disabled, not silently assumed to still work (Section 9).
- **Maintenance drift risk**: the host-side workflow script, once made permanent, needs the same care given to any production CI code — unlike a temporary diagnostic workflow, it cannot simply be deleted after one investigation.

---

## 15. Remaining Unknowns

- Whether a purely post-hoc (non-continuous) logcat dump would suffice — **UNKNOWN**, Section 8, requires a dedicated validation experiment before being relied upon.
- Whether BrowserStack/Sauce Labs/Grid expose any equivalent evidence retrieval mechanism — **NOT VERIFIED**, Section 9.
- Whether `--relaxed-security` (required for the Appium-session component's `mobile: shell` calls, if that path is ever used again) is available on any future cloud provider's hosted Appium server — **NOT VERIFIED**.
- The precise storage/artifact-retention cost of always-on host-observer capture across a full 19-test suite (this engagement only ever exercised the single representative `LoginTest`, never the full suite, under this instrumentation) — **NOT VERIFIED**.

---

## 16. Explicit Non-Decisions

This phase does **not**: implement any code; modify `TestListener.java`, `AndroidDriverFactory.java`, or any other source file; modify `Dockerfile`, `.dockerignore`, or `mobile-automation.yml`; modify the AUT; decide whether host-observer artifacts should be uploaded always or only on failure (Section 10/14, left open); decide the exact shape of a future cloud-provider-pluggable evidence abstraction (Section 9, noted as a requirement only); commit or push anything; proceed to Phase 19.4O; or proceed to Phase 19.5.

---

## 17. Final Verdict

# FRAMEWORK-LEVEL AUT CRASH DIAGNOSTICS DESIGN COMPLETE — READY FOR EXPLICIT IMPLEMENTATION DECISION

A minimal, evidence-grounded hybrid architecture is designed: a permanent, always-run (CI-only), host-side ADB observer as the primary evidence source (reusing the Phase 19.4H/19.4J design verbatim, now proposed as production rather than temporary), paired with a narrow, failure-triggered extension of the already-existing `TestListener.onTestFailure` hook for framework-side correlation context. Both halves are chosen specifically because Docker Model 3's architecture rules out a framework-side direct-ADB alternative, and Phase 19.4G's own evidence rules out continuous in-JVM session-sharing polling. One material gap remains explicitly unresolved and flagged rather than assumed: whether a cheaper, purely-reactive (non-continuous) capture would also suffice is `UNKNOWN` and unvalidated. No implementation, commit, or push occurred this phase.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Document Status | Final — Design Report (No Implementation) | — | — |

---

**End of Document — Phase 19.4N Framework-Level AUT Crash Diagnostics Design Report, v1.0**
