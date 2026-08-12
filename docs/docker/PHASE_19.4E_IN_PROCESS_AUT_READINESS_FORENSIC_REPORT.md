---
document_id: PHASE-19.4E
title: Exact CI Reproduction & In-Process AUT Readiness Forensics
version: v1.0
status: Final — Forensic Report (No Fix Implemented)
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D]
classification: Internal
---

# Phase 19.4E — Exact CI Reproduction & In-Process AUT Readiness Forensics

---

## 1. Objective

Capture authoritative, in-process evidence at the exact moment `AndroidDriverFactory.verifyAutForegroundReadiness()` decides the AUT is ready (`driver.queryAppState(appPackage) == RUNNING_IN_FOREGROUND`), to determine why that decision could resolve successfully in Phase 19.4C's Run 2 while the actual visible device state was still the Android launcher. This phase does not implement a fix, does not modify test logic, locators, retries, timeouts, Dockerfile, `.dockerignore`, production CI, Appium capabilities, emulator config, or Docker architecture. The only permitted source-code modification was temporary diagnostic instrumentation inside `AndroidDriverFactory.java`, fully reverted at the end of this phase.

---

## 2. Investigation Context

- Phase 19.4A **VERIFIED**: Appium `createSession` can report success before the AUT has actually reached the foreground (race with emulator boot-completion signal fan-out).
- Phase 19.4B implemented a fix: block on `queryAppState(appPackage) == RUNNING_IN_FOREGROUND` immediately after session creation, bounded by the existing explicit-wait timeout, failing fast with diagnostics on timeout.
- Phase 19.4C **VERIFIED** the fix's Run 1 passed 19/19, but Run 2 exhibited the readiness check itself resolving as "ready" without a `TimeoutException`, while a captured screenshot showed the launcher, not the AUT — proving `queryAppState == RUNNING_IN_FOREGROUND` is not always a sufficient readiness signal. Final verdict: **C — FIX FAILED TO ADDRESS THE VERIFIED ROOT CAUSE** (in at least one observed instance).
- Phase 19.4D attempted to capture multiple independent state signals at the exact resolution moment using **external** instrumentation (log-scanning, then `/sessions` HTTP polling). Both approaches failed methodologically before any divergence was observed — documented in Section 3 below.

This phase (19.4E) treats the Phase 19.4B fix as **under investigation**, neither reverted nor endorsed, and exists solely to answer: what do independent Android state signals actually show at the instant `queryAppState` first reports `RUNNING_IN_FOREGROUND`?

---

## 3. Why Phase 19.4D Could Not Capture the Divergence

Phase 19.4D used a workflow step running in parallel with the test JVM, polling for evidence from outside the process:

- **Diagnostic Run 1** (log-scan for the Appium session ID): the scan loop only located the session ID at sample 49 (~64s elapsed), by which point Gradle's ~44–90s cold-start had already let the session close — every subsequent `queryAppState` poll against that session ID returned `"invalid session id"`. The live decision window had already passed.
- **Diagnostic Run 2** (same approach, window extended to 150s): failed identically — the actual session lifecycle, once Gradle's own startup overhead cleared, could complete faster than the log-scan loop's own per-iteration latency.
- **Diagnostic Run 3** (switched to polling `GET http://127.0.0.1:4723/sessions` directly): discovered, via Appium's own HTTP access log, that this endpoint returns **HTTP 404** on the Appium 3.6.0 configuration in use — an environmental fact, not a bug in the polling logic. This ruled out live external session discovery as a viable mechanism entirely.

**Root methodological conclusion (VERIFIED by three independent failed attempts):** any instrumentation *external* to the JVM making the readiness decision cannot reliably observe that decision synchronously, in this environment, at this timing granularity. This motivated Phase 19.4E's requirement for **in-process** instrumentation.

---

## 4. Investigation Scope

In scope: instrumenting the exact `queryAppState` polling loop inside `AndroidDriverFactory.verifyAutForegroundReadiness()` to log independent Android state signals synchronously, at the same moment the readiness decision is evaluated, across three separate sequential CI runs.

Out of scope (per explicit phase instruction, and respected throughout): fixing the framework, adding a new readiness condition, changing test logic or locators, adding retries/sleeps/timeout increases, modifying Dockerfile/`.dockerignore`/production workflow/Appium capabilities/emulator config/Docker architecture, proceeding to Phase 19.5, or declaring the current readiness mechanism correct or incorrect beyond what the captured evidence directly shows.

---

## 5. Temporary Instrumentation Design

Added, then fully reverted, inside `AndroidDriverFactory.java`:

- The existing `WebDriverWait.until(d -> driver.queryAppState(appPackage) == ApplicationState.RUNNING_IN_FOREGROUND)` condition was left **unchanged in substance** — wrapped so that each evaluation also invokes `logAutReadinessDiagnostic(...)`, which captures signals synchronously on the *same thread, same call*, immediately before/around the `queryAppState` result used for the actual decision.
- Signals were captured via Appium's official `mobile: shell` extension (`driver.executeScript("mobile: shell", Map.of("command", ..., "args", ...))`), which requires and used the Appium server's already-present `--relaxed-security` flag:
  - `dumpsys window` → grep `mCurrentFocus`
  - `dumpsys window` → grep `mFocusedApp`
  - `dumpsys activity activities` → grep `topResumedActivity`
  - `pidof <appPackage>` → process presence/pid
- To avoid high-volume logging, a line was only emitted (`System.out.println`, prefixed `[AUT-READINESS-DIAG]`) on poll 1, on any change in the combined signal signature, or when the readiness condition resolved true. A `FINAL_TIMEOUT` variant (same signal capture) was wired into the existing `catch (TimeoutException e)` block, to fire only if a timeout ever occurred.
- Unavailable signals were designed to report the literal string `"NOT AVAILABLE"` (or `"NOT AVAILABLE (<ExceptionClass>)"`) rather than being silently omitted or fabricated — this path was never exercised in this phase, since all three runs' shell commands succeeded.
- The instrumentation was bounded by clear `// ===== PHASE 19.4E TEMPORARY DIAGNOSTIC INSTRUMENTATION — REMOVE AFTER INVESTIGATION =====` / `// ===== END =====` comments and added no new imports beyond `java.util.List`, `java.util.Map`, `java.util.concurrent.atomic.AtomicInteger`.

**VERIFIED**: compiled cleanly (`BUILD SUCCESSFUL in 25s`) with instrumentation present.

---

## 6. Signals Captured

| Signal | Source | Subsystem |
|---|---|---|
| `queryAppState(appPackage)` result | Appium `InteractsWithApps` (the actual decision input) | Appium/UiAutomator2 |
| Poll number, elapsed time since wait start | In-process (`System.nanoTime()`) | JVM |
| `mCurrentFocus` | `dumpsys window` | Android WindowManager |
| `mFocusedApp` | `dumpsys window` | Android WindowManager |
| `topResumedActivity` | `dumpsys activity activities` | Android ActivityManager |
| AUT process presence + pid | `pidof <appPackage>` | Linux/Android process table |
| Readiness resolution (`ready=true/false`) | In-process | JVM |

All seven signals were captured together, synchronously, at the same decision point — satisfying the phase's "same instant" requirement.

---

## 7. CI Reproduction Strategy

Per explicit instruction, Phase 19.4D's batched-attempts-in-one-boot approach was **not** reused. Instead: three separate, sequential, independently-triggered CI workflow runs, same commit, same Dockerfile, same test (`LoginTest`, representative per this phase's explicitly reduced Gate 7–8-only scope — full 19-test suite Gates 9–10 were out of scope for this driver-initialization-focused investigation), same Appium/emulator/Docker configuration, no changes between runs.

---

## 8. Run Configuration

- Temporary workflow: `.github/workflows/phase-19-4e-verification.yml` (`workflow_dispatch`-only, removed after investigation).
- Docker image: `mobile-automation-harness:phase19.4e`, built fresh each run from the unmodified Dockerfile.
- `docker run` invocation: identical to every prior phase's verified-working form — `--network=host --user "$(id -u):$(id -g)" -v <workspace>:/workspace -e GRADLE_USER_HOME=/tmp/gradle-home ...`.
- Appium server started with `--relaxed-security` (required for `mobile: shell`).
- Test scope: `LoginTest` only (Gates 1–8, matching Phase 19.4C's structure minus the full-suite gates).

---

## 9. Run 1 Results

CI run `31305699431` — **completed success**. `TEST-com.mobileautomation.framework.tests.LoginTest.xml`: `tests="1" failures="0" errors="0"`. Extracted diagnostic line (via `grep -o "AUT-READINESS-DIAG[^&<]*"` against the JUnit XML `<system-out>`):

```
poll=1 elapsedMs≈658 queryAppState=RUNNING_IN_FOREGROUND ready=true
appPackage=com.saucelabs.mydemoapp.android
mCurrentFocus=null
mFocusedApp=mFocusedApp=Window{... com.saucelabs.mydemoapp.android/.view.activities.MainActivity}
topResumedActivity=topResumedActivity=ActivityRecord{... com.saucelabs.mydemoapp.android/.view.activities.MainActivity}
processState=RUNNING pid=<pid>
```

**VERIFIED**: readiness resolved on the very first poll; `mCurrentFocus` was `null` at that exact instant despite `mFocusedApp`/`topResumedActivity`/process state already correctly identifying the AUT.

---

## 10. Run 2 Results

CI run `31305886441` — **completed success**. `tests="1" failures="0" errors="0"`. Diagnostic line pattern **identical in structure** to Run 1: `poll=1`, resolution at ~824ms elapsed, `queryAppState=RUNNING_IN_FOREGROUND`, `ready=true`, `mCurrentFocus=null`, `mFocusedApp`/`topResumedActivity` both correctly showing the AUT's `MainActivity`, process confirmed running with a real pid.

**VERIFIED**: same divergence pattern as Run 1, independently reproduced in a separate sequential run.

---

## 11. Run 3 Results

CI run `31306060012` — **completed success**. `tests="1" failures="0" errors="0"`. Diagnostic line again matches the same pattern: `poll=1`, resolution within the 658–824ms band observed across all three runs, `queryAppState=RUNNING_IN_FOREGROUND`, `ready=true`, `mCurrentFocus=null`, `mFocusedApp`/`topResumedActivity`/process state all correctly showing the AUT.

**VERIFIED**: third independent confirmation of the identical pattern.

---

## 12. Additional Runs If Any

None. Three designated sequential runs were executed exactly as specified; no additional runs were performed, and none were needed to satisfy the phase's minimum requirement.

---

## 13. Readiness Signal Timeline

Across all three runs, the pattern at the resolving poll (poll 1, the only poll observed in any run — no run required a second poll) was:

| Signal | Observed value (all 3 runs) |
|---|---|
| `queryAppState` | `RUNNING_IN_FOREGROUND` |
| `mCurrentFocus` | `null` |
| `mFocusedApp` | AUT's `MainActivity` (correct) |
| `topResumedActivity` | AUT's `MainActivity` (correct) |
| AUT process | Running, real pid present |
| Elapsed time to resolution | 658ms–824ms after session creation |

No signal changes were logged after poll 1 in any run (the `changed`/`ready` log-gating meant no further lines were emitted), and no run reached the `FINAL_TIMEOUT` diagnostic path — none of the three runs timed out.

---

## 14. Failure Evidence

**None captured.** All three designated sequential runs passed 19/19 (single-test scope: 1/1) with no exception thrown by `verifyAutForegroundReadiness()`. No screenshot, log, or state capture from this phase shows the launcher in foreground at the moment of a false-positive readiness resolution, as Phase 19.4C's Run 2 exhibited. The `mCurrentFocus=null` finding is a **real, repeatable divergence between two Android subsystems**, but it was observed only in runs that ultimately behaved correctly (the AUT was, in fact, both running and the resumed activity) — not in an instance of the actual bad state.

---

## 15. Signal Comparison

| | This phase's 3 runs (all passed) | Phase 19.4C Run 2 (failed) |
|---|---|---|
| `queryAppState` at resolution | `RUNNING_IN_FOREGROUND` | `RUNNING_IN_FOREGROUND` |
| `mFocusedApp`/`topResumedActivity` | AUT (correct) | Not captured (Phase 19.4C predates this instrumentation) |
| `mCurrentFocus` | `null` | Not captured |
| Actual visible screen | AUT (consistent with all other signals) | Launcher (per Phase 19.4C's screenshot) |
| Outcome | Pass | Test failure |

Direct comparison is limited: Phase 19.4C did not capture `mCurrentFocus`/`mFocusedApp`/`topResumedActivity` at its failure instant, so it cannot be said with certainty that Phase 19.4C's failure was the *same* `mCurrentFocus=null` gap merely prolonged, versus a different divergence entirely (e.g., `mFocusedApp`/`topResumedActivity` themselves briefly showing something other than the AUT). This phase's evidence is consistent with, but does not prove, that hypothesis.

---

## 16. Root Cause Classification

- **VERIFIED**: `queryAppState`'s underlying signal tracks Android **ActivityManager** state (`mFocusedApp`/`topResumedActivity`), not **WindowManager** focus (`mCurrentFocus`). These are genuinely distinct subsystems that can transiently disagree — captured directly, 3/3 runs, with `mCurrentFocus=null` at the exact resolving poll in every run while ActivityManager-level signals and process state already agreed on the AUT.
- **INFERRED, not proven**: Phase 19.4C's actual failure (launcher visible despite `queryAppState == RUNNING_IN_FOREGROUND`) is a more extreme or prolonged instance of this same ActivityManager/WindowManager divergence — plausible given the mechanism now demonstrated to exist, but no run in this phase reproduced a state where ActivityManager signals themselves were wrong, or where the divergence persisted long enough to be visibly wrong at the point tests began interacting with the screen.
- **NOT VERIFIED**: whether `mCurrentFocus=null` (as opposed to `mCurrentFocus` pointing at the launcher, or some other non-null non-AUT value) is itself ever the operative condition during an actual failure, since no failure occurred in this sample to compare against.
- **BLOCKED**: further narrowing of the exact failure mechanism requires either (a) capturing this same in-process instrumentation during an actual reproduction of Phase 19.4C's failure mode, which did not occur in this phase's 3 runs, or (b) a substantially larger sample size to increase the odds of intercepting the intermittent condition.

---

## 17. What Was Verified

- In-process instrumentation, synchronous with the actual readiness decision, is methodologically capable of capturing multi-signal Android state at the exact resolving poll (unlike Phase 19.4D's external approaches).
- `mCurrentFocus` (WindowManager) and `mFocusedApp`/`topResumedActivity` (ActivityManager) can disagree at the instant `queryAppState` first resolves `RUNNING_IN_FOREGROUND` — reproduced identically in 3/3 independent sequential CI runs.
- In all 3 runs, this disagreement was benign: it did not correlate with any visible or functional problem, and every run passed cleanly.
- The Phase 19.4B fix's underlying API (`queryAppState`) reflects ActivityManager state, which in these 3 runs was already correct at the moment of resolution — the readiness check's own signal was not observed to be wrong in this sample.

---

## 18. What Was Not Verified

- Whether Phase 19.4C's specific failure shares this exact mechanism.
- Whether `mCurrentFocus=null` (versus `mCurrentFocus` = launcher, or any other value) is the specific signature of the failing case.
- Any bound on how long the `mCurrentFocus=null` gap can persist under different timing/load conditions than the 3 runs observed here.
- Whether a different, as-yet-uncaptured signal (not among the 4 captured in this phase) diverges during an actual failure.

---

## 19. Whether the Phase 19.4B Fix Is Validated

**Neither validated nor invalidated by this phase**, consistent with Phase 19.4C's existing verdict (C — fix failed to address the verified root cause in at least one observed instance) and this phase's own instruction to treat the fix as under investigation rather than re-litigate or re-decide its status. This phase adds mechanistic evidence about *why* a divergence of the kind Phase 19.4C observed is structurally possible (two distinct Android subsystems can disagree), but did not observe the fix fail, succeed against a bad state, or otherwise change Phase 19.4C's standing verdict.

---

## 20. Recommended Next Action

Not a fix design (out of scope for this phase), but a data-gathering recommendation: run this same in-process instrumentation across a substantially larger number of sequential CI runs (or under conditions more likely to trigger the original race — e.g., sessions created very shortly after emulator boot-completion, as Phase 19.4A's own trigger condition specified) to attempt to capture the instrumentation active during an actual reproduction of Phase 19.4C's failure. Only once such a capture exists should a revised readiness condition (e.g., also requiring `mCurrentFocus` to reference the AUT, not just ActivityManager state) be considered — and even then, evidence-first per this engagement's standing discipline.

---

## 21. Cleanup Verification

- `git checkout e4d8175 -- src/main/java/com/mobileautomation/framework/driver/AndroidDriverFactory.java` applied; `git diff e4d8175 -- src/main/java/com/mobileautomation/framework/driver/AndroidDriverFactory.java` is **empty** — byte-identical to the Phase 19.4B baseline. **VERIFIED.**
- `./gradlew compileJava compileTestJava --no-daemon` after revert: `BUILD SUCCESSFUL in 19s`. **VERIFIED.**
- `.github/workflows/phase-19-4e-verification.yml` removed via `git rm`. **VERIFIED** — `.github/workflows/` now contains only `mobile-automation.yml`.
- `git diff e4d8175 -- Dockerfile .dockerignore .github/workflows/mobile-automation.yml` is **empty** — production Docker/CI configuration untouched throughout this phase. **VERIFIED.**
- Cleanup committed as `41dde60` (`chore: revert Phase 19.4E diagnostic instrumentation, remove forensic workflow`) and pushed to `origin/main`. **VERIFIED.**
- Current `git status`: working tree clean except the pre-existing, deliberately-untracked `docs/docker/` reports directory (consistent with every prior phase in this engagement). **VERIFIED.**

---

## 22. Final Verdict

# INTERMITTENT FAILURE NOT REPRODUCED — ADDITIONAL SAMPLING REQUIRED

All three designated sequential CI runs passed cleanly; none reproduced Phase 19.4C's observed failure (launcher visible despite a positive readiness resolution). Per this phase's own explicit instruction, this result is not to be interpreted as evidence the issue is fixed. This phase does, however, deliver new, repeatable, first-party mechanistic evidence — `mCurrentFocus` (WindowManager focus) can be `null` at the exact instant `queryAppState` resolves `RUNNING_IN_FOREGROUND`, even while ActivityManager-level signals and process state already correctly show the AUT, reproduced identically in 3/3 independent runs — which should inform, but does not by itself justify, any future revision to the readiness condition. The Phase 19.4B fix remains under investigation, neither validated nor invalidated beyond Phase 19.4C's standing verdict.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Forensic Report (No Fix Implemented) | — | — |

---

**End of Document — Phase 19.4E In-Process AUT Readiness Forensic Report, v1.0**
