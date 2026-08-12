---
document_id: PHASE-19.4G
title: Continuous AUT Visibility Failure-Window Forensic Analysis
version: v1.0
status: Final — Forensic Report (Failure Not Reproduced, No Fix Implemented)
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D, PHASE-19.4E, PHASE-19.4F]
classification: Internal
---

# Phase 19.4G — Continuous AUT Visibility Failure-Window Forensic Analysis

---

## 1. Objective

Capture continuous, time-correlated evidence from the moment the Phase 19.4B readiness check succeeds (T0) through the first test interaction's success or failure, to determine what happens during the ~15-second interval Phase 19.4F identified as uncaptured, and to find the first observable divergence between a healthy and a failing execution. This phase does not implement a fix and does not attempt to make CI green.

---

## 2. Context

Phase 19.4F reproduced the intermittent failure directly: in a 5-run designated sequential sample, Run 5 failed with the readiness check reporting `RUNNING_IN_FOREGROUND` (with `mFocusedApp`/`topResumedActivity`/process state all agreeing) while the first test assertion then polled for the full ~15-second explicit-wait budget and never observed the AUT — both failure screenshots showed the Android launcher. Critically, that readiness-boundary signature was indistinguishable from three passing runs in the same sample, meaning no signal captured only at T0 can predict the outcome. Phase 19.4F's own recommendation was to instrument continuously through the window that follows T0, not just at the boundary itself.

---

## 3. Evidence Gap from Phase 19.4F

Phase 19.4F's instrumentation logged state only at the single moment the readiness `WebDriverWait` resolved. Nothing was captured between that moment (~729ms after session creation in the failing run) and the assertion failure roughly 15 seconds later. This phase's entire purpose is to fill that specific gap.

---

## 4. Investigation Scope

In scope: temporary, read-only, continuous instrumentation inside `AndroidDriverFactory.java` only, and a temporary CI workflow to execute up to 5 designated sequential runs, stopping immediately if a failure with complete evidence is captured or after 5 runs complete.

Out of scope (and not touched): Docker architecture, `Dockerfile`, `.dockerignore`, production `mobile-automation.yml`, Gradle configuration, test logic, Page Objects, Appium capabilities beyond read-only observability, sleeps/retries/timeout changes as fixes, `AndroidDriverFactory` redesign, Phase 19.5.

---

## 5. Instrumentation Design

A background daemon thread (`phase19-4g-window-diag`) was started immediately after the existing `WebDriverWait.until(...)` in `verifyAutForegroundReadiness()` resolved successfully — that moment is T0. The thread:

- Cleared the device logcat buffer (`logcat -c`) at T0.
- Polled every ~700ms, on each iteration capturing: `driver.queryAppState(appPackage)`, a single `dumpsys window` dump (from which `mCurrentFocus` and `mFocusedApp` were both extracted, avoiding duplicate round trips), a single `dumpsys activity activities` dump (from which `mResumedActivity` and `topResumedActivity` were both extracted), `pidof <appPackage>` (process presence), `driver.getCurrentPackage()` (an independent Appium-level signal, reusing the file's existing `safeGetCurrentPackage` helper), and the root `package="..."` attribute of `driver.getPageSource()` (a generic, non-test-coupled proxy for what UiAutomator2 currently considers the foreground app).
- Logged a line (`[AUT-WINDOW-DIAG]`) only on the first sample, on any signal change, or on a periodic heartbeat — not every poll, to avoid high-volume output.
- Dumped logcat (`[AUT-LOGCAT-DIAG]`) at cumulative checkpoints (every ~5 seconds of elapsed window time, since the T0 clear) rather than only once at the very end — a deliberate defensive design, adopted because a daemon thread can be killed by the JVM at any point without warning once no non-daemon thread remains (a real risk for passing runs, whose test body and suite teardown can complete in a few seconds), so a single deferred final dump risked being lost entirely.
- Ran for a bounded ~15.2-second window, sized to stay safely inside the active test method's execution (verified against Phase 19.4F's own timing evidence) so that Gradle/TestNG's per-test `System.out` capture would reliably attribute the output to the correct `<system-out>` section.

**Design worked as intended in one respect**: it did not require modifying any test-side file, and the diagnostic output was successfully captured in every run's JUnit `<system-out>`, confirming the daemon-thread approach and Gradle's output-attribution behavior did work together correctly.

**Design did not work as intended in two significant respects, discovered only once run live** — see Sections 11 and 12.

---

## 6. Timing Model

T0 = the moment `verifyAutForegroundReadiness()`'s `WebDriverWait.until(...)` returns without a `TimeoutException`. All `[AUT-WINDOW-DIAG]`/`[AUT-LOGCAT-DIAG]` timestamps are `t=+Nms` relative to T0, measured via `System.nanoTime()` on the diagnostic thread. Poll interval: ~700ms (nominal — actual inter-sample spacing was, in practice, far larger; see Section 12). Window bound: 15,200ms (nominal — actual thread lifetime before the final logged event ranged ~49,000–53,450ms; see Section 12, which explains this apparent contradiction). Logcat checkpoint interval: every 5,000ms of elapsed window time.

---

## 7. Signals Observed

`queryAppState`, `mCurrentFocus`, `mFocusedApp`, `mResumedActivity`, `topResumedActivity`, AUT process presence/pid, `getCurrentPackage()` (Appium), page-source root `package` attribute (UiAutomator2 hierarchy proxy), and checkpointed logcat (tag-filtered: `ActivityTaskManager`/`ActivityManager`/`WindowManager`/`InputDispatcher`/`AndroidRuntime`, plus a broader AUT-package grep).

---

## 8. Passing Run Evidence

All 5 designated sequential CI runs passed. Representative first-sample (T0) evidence, consistent across all 5:

```
[AUT-WINDOW-DIAG] t=+91–139ms sample=1 queryAppState=RUNNING_IN_FOREGROUND
mCurrentFocus=Window{... com.saucelabs.mydemoapp.android/.../MainActivity}
mFocusedApp=ActivityRecord{... MainActivity} mResumedActivity=NOT AVAILABLE
topResumedActivity=ActivityRecord{... MainActivity} processState=RUNNING pid=<pid>
currentPackageViaAppium=com.saucelabs.mydemoapp.android
pageSourceRootPackage=com.saucelabs.mydemoapp.android CHANGED
```

Notably, **`mCurrentFocus` was non-null and directly named the AUT's `MainActivity` in every sample of every run in this phase** — unlike Phase 19.4E/F, where `mCurrentFocus=null` was the more common (though not universal) observation. With only 2–3 samples captured per run (Section 12), this cannot be generalized as a stable difference between phases; it is reported as observed, not as a trend.

---

## 9. Failing Run Evidence

**None.** No run in this phase's bounded 5-run sample failed. There is no failing-run evidence to report.

---

## 10. Continuous Timeline

| Run | CI Run ID | Result | Test time | Samples logged | Sample timestamps (t=+ms) | Final logged event |
|---|---|---|---|---|---|---|
| 1 | `31315407015` | Pass | 110.364s | 3 | 56, 8943 | `WINDOW_END` at ~53,450ms |
| 2 | `31320352541` | Pass | 107.666s | 3 | 114, 6401 | `WINDOW_END` (final checkpoint ~48,971ms) |
| 3 | `31320845057` | Pass | 107.626s | 3 | 124, 4375 | `WINDOW_END` (final checkpoint ~50,861ms) |
| 4 | `31321192671` | Pass | 109.054s | 2 | 139 | `WINDOW_END` (final checkpoint ~49,244ms) |
| 5 | `31321461007` | Pass | 108.077s | 3 | 91, 4220, 7126 | `WINDOW_END` (final checkpoint ~49,705ms) |

Every run's actual instrumented window ran roughly 3.2×–3.5× longer than the intended 15,200ms bound before the diagnostic thread finished (Section 12 explains why). Average total test time across this phase's 5 runs was **~108.6 seconds**, versus Phase 19.4F's healthy-run baseline of **~30.0 seconds** (Runs 1–4 average) — a ~3.6× inflation. **VERIFIED** from the JUnit XML `time` attribute in every run.

---

## 11. Logcat Analysis

**Logcat capture failed in every checkpoint, in every run — 5 of 5 runs, all checkpoints, both the tag-filtered dump and the package-grep dump.** Each attempt printed `NOT AVAILABLE (shell error)`, meaning the underlying `mobile: shell` call (`command="sh", args=["-c", "logcat ..."]`) threw a `RuntimeException` that the instrumentation's `safeShellRaw` helper caught and converted to `null`, and `truncate(null, ...)` renders as `"NOT AVAILABLE (shell error)"`.

**BLOCKED**: the specific exception type/message was not captured or logged — a design gap in this phase's own instrumentation (unlike `safeQueryAppState`, which does record `e.getClass().getSimpleName()`, `safeShellRaw` discards the exception entirely). Plausible causes include the `sh` binary not being permitted through `mobile: shell` even under `--relaxed-security`, or the command timing out due to the same contention documented in Section 12 — **neither is verified**.

Per the phase's own instruction, no claim is made about the absence of ActivityTaskManager/WindowManager/crash events during the window — that evidence was simply never obtained. **This is a complete, systematic gap in this phase's evidence, not a negative finding.**

---

## 12. Appium Analysis

**A significant, unanticipated finding**: the background diagnostic thread's own Appium/`mobile: shell` calls appear to contend heavily with the main test thread's own commands on the same Appium session. Cross-referencing `logs/automation.log` against the diagnostic timestamps (Run 1) shows simple UI actions that normally complete in under a second — typing a username, clearing a field, tapping a drawer item — instead took 15–22 seconds each while the diagnostic thread was active:

| Action (Run 1) | Normal expectation | Observed duration |
|---|---|---|
| Type username field → clear password field | <1s | ~21.7s |
| Clear password field → type password field | <1s | ~20.6s |
| Tap login → Product Catalog visible | a few seconds | ~20.3s |
| Open drawer → "Log Out" confirmed displayed | <1s | ~20.1s |
| "Log In" confirmed absent | <1s | ~15.4s |

This is consistent with Appium (or the emulator's ADB/UiAutomator2 bridge) processing commands for a given session serially: a steady stream of diagnostic `dumpsys`/`pidof`/`getPageSource` calls from the background thread appears to queue ahead of or interleave with the main thread's own commands, inflating their latency by an order of magnitude. This same contention also explains Section 10's timing anomaly: individual diagnostic-thread iterations, not the loop's own bound-checking logic, took tens of seconds to complete once the main thread's login flow was actively issuing its own commands, causing the diagnostic window to run far longer than its coded 15,200ms bound before the thread's own body finally returned control to the loop condition.

**VERIFIED**: this instrumentation design measurably altered test execution timing (Section 10), contrary to the phase's own design goal that instrumentation "does not materially delay or alter the test flow." **INFERRED, not proven**: the specific mechanism is Appium/ADB-bridge command serialization under concurrent access from two threads sharing one session — plausible and consistent with all observed timing, but not confirmed via, e.g., Appium server-side logs (which this workflow does not capture in its uploaded artifacts, the same gap noted in Phase 19.4F).

---

## 13. Android Activity State Analysis

`mFocusedApp` and `topResumedActivity` referenced the AUT's `MainActivity` in every logged sample across all 5 runs — consistently healthy. `mResumedActivity` was **`NOT AVAILABLE` in every single sample, in every run, with no exceptions** — the literal string `mResumedActivity` was never found in any `dumpsys activity activities` dump captured in this phase. **VERIFIED** as a consistent, real grep-miss (not a fabricated absence): this signal is either not present under this label in this Android version/dump format, or appears in a section this simple substring search did not reach. **NOT VERIFIED**: the exact reason.

One minor internal churn was observed within Run 5's own healthy timeline: between t=+4220ms and t=+7126ms, `mFocusedApp` and `topResumedActivity` briefly referenced different `ActivityRecord` object identities (different hex IDs) before reconverging — both, at all times, still named the AUT's `MainActivity`. This is evidence that these low-level records can churn even inside a run that ultimately passes cleanly, consistent with Phase 19.4E/F's broader finding that individual raw signals are not perfectly stable snapshots. It is not evidence of the launcher, or any other app, taking over.

---

## 14. Window Visibility Analysis

`mCurrentFocus` was non-null and named the AUT's `MainActivity` window in every sample of every run in this phase (Section 8) — a different pattern from Phase 19.4E/F, where `mCurrentFocus=null` was common. Given only 2–3 samples per run were captured (Section 12), this is reported as an observation, not as evidence that `mCurrentFocus` is more stable under this phase's conditions; the sample density is too low, and too confounded by the contention finding, to draw that conclusion.

---

## 15. Process State Analysis

The AUT process was reported running, with a stable pid, in every sample of every run — no run showed the process disappearing or restarting (no pid change within a run). **VERIFIED** for the samples actually captured; **NOT VERIFIED** for the large uncaptured gaps between samples (Section 12).

---

## 16. UiAutomator State Analysis

Both `getCurrentPackage()` (Appium) and the page-source root `package` attribute (UiAutomator2 hierarchy) matched the AUT package in every sample of every run — fully consistent with the other signals and with every run's passing outcome. No sample in this phase ever showed UiAutomator2's own hierarchy view diverging from ActivityManager's view.

---

## 17. Passing vs Failing Comparison

Not possible in this phase — no failing run was captured to compare against. For context only (not new data): Phase 19.4F's single failing run showed the same T0 signal pattern (`queryAppState`/`mFocusedApp`/`topResumedActivity`/process all healthy) as every passing run in that phase and in this one; this phase adds no further comparison point.

---

## 18. First Observable Divergence

**Not applicable — no divergence toward a failure state was observed**, because no run in this bounded sample failed. The only state change observed within any run (Run 5's brief `ActivityRecord` identity churn, Section 13) never left the AUT's package and does not constitute a divergence toward the launcher or any other failure indicator.

---

## 19. Root Cause Assessment

**Not reached.** No failure was captured in this phase, so there is no failure-window evidence to assess a root cause against. This phase neither confirms nor rules out any of the candidate mechanisms (A–H) listed in its own objective.

---

## 20. Alternative Explanations

- **The instrumentation's own interference suppressed the race.** Plausible given Section 12's contention finding — heavier, slower command traffic on the shared session could plausibly have changed timing enough to avoid whatever narrow window triggers the original race. **INFERRED at most; not provable from this data** — there is no control run (same instrumentation overhead, different mechanism) to isolate this effect.
- **Pure sampling variance, unrelated to interference.** Phase 19.4F's own sample put the empirical failure rate at roughly 1 in 5 (20%). If that rate is representative, the probability of seeing zero failures in a fresh 5-run sample by chance alone is `0.8^5 ≈ 33%` — a materially likely outcome even with **no** interference at all. **VERIFIED** as a straightforward binomial calculation; this alone is sufficient to explain this phase's all-pass result without invoking the interference hypothesis.
- Both explanations are consistent with the observed data; this phase's evidence cannot distinguish between them, and per its own instruction, neither is upgraded to a conclusion.

---

## 21. What Is Ruled Out

Very little. This phase does not rule out any of categories A–H from its own objective, since no failure occurred to test them against. It does add one narrow, real finding unrelated to the original failure mechanism: this specific instrumentation design (concurrent driver access from a background thread) is not a safe way to observe a live Appium session without materially altering its timing — that approach, as implemented, is ruled out for future use without redesign.

---

## 22. What Remains Unknown

- Everything Phase 19.4F left unknown remains unknown: the exact mechanism producing the launcher-visible failure, and which of the hypotheses in Phase 19.4F's Section 20 (brief genuine resume then lost, vs. false positive from the start) is correct.
- Why the logcat shell command failed in 100% of attempts (Section 11) — the specific exception was not captured.
- Whether the severe session contention (Section 12) has any causal relationship to the original failure's frequency or character, or is entirely orthogonal to it.
- Whether `mResumedActivity` is obtainable at all via `dumpsys activity activities` in this environment, or requires a different dump command/section.

---

## 23. Risks

- **Continuing to use this phase's instrumentation design unmodified would keep producing low-value, heavily-perturbed samples** — each run costs ~2–3 minutes of CI time (plus the underlying ~110s test) while yielding only 2–3 real samples and zero logcat evidence, a poor return given the goals in Section 1.
- **The underlying production risk is unchanged and unresolved**: Phase 19.4F's ~20% single-sample failure rate stands; this phase neither confirms nor revises it.
- **A 5-run bounded sample is, on its own, a weak instrument** for an intermittent condition at this rate (Section 20) — treating a clean 5-run pass as reassuring would be a statistical error this phase explicitly avoids.
- **Repeating this exact design at a larger scale would multiply, not fix, the contention and logcat gaps** — more runs alone would not close either evidence gap identified here.

---

## 24. Recommended Next Step

Not a fix (out of scope). Two concrete design corrections should precede any further sampling attempt:

1. **Eliminate concurrent access to the shared Appium session.** Move continuous state sampling out of the JVM process entirely and into a host-side, ADB-direct observer running in parallel in the CI workflow (the GitHub Actions runner has direct `adb` access to the emulator in this Docker Model 3 architecture, independent of the container's Appium HTTP session) — this avoids the command-serialization contention documented in Section 12 without needing any change to `AndroidDriverFactory` at all.
2. **Fix logcat error visibility** before relying on it again — capture and log the actual exception class/message (not just a generic "shell error") so a future failed attempt is diagnosable rather than only detectable.

Only after these two corrections are in place should a further bounded sampling attempt (sized with Section 20's binomial reasoning in mind — likely more than 5 runs, to meaningfully improve the odds of intercepting a ~20% intermittent event) be run.

**Cleanup verification**: `git checkout e4d8175 -- src/main/java/.../AndroidDriverFactory.java` applied; `git diff e4d8175 -- src/main/java/.../AndroidDriverFactory.java Dockerfile .dockerignore .github/workflows/mobile-automation.yml` is empty (**VERIFIED**). `./gradlew compileJava compileTestJava --no-daemon` after revert: `BUILD SUCCESSFUL in 14s` (**VERIFIED**). `.github/workflows/phase-19-4g-window-diag.yml` removed via `git rm`; `.github/workflows/` now contains only `mobile-automation.yml` (**VERIFIED**). Cleanup committed as `08aa091` and pushed to `origin/main` (**VERIFIED**). `git status` shows a clean working tree except the pre-existing, deliberately-untracked `docs/docker/` directory (**VERIFIED**).

---

## 25. Final Verdict

# FAILURE NOT CAPTURED WITHIN BOUNDED SAMPLE

All 5 designated sequential CI runs passed; the intermittent failure Phase 19.4F reproduced was not observed in this sample. This result should be read as weak and inconclusive, not reassuring: Section 20 shows a clean 5-run pass is the expected outcome roughly one-third of the time even if the underlying ~20% failure rate is unchanged and no interference occurred, and this phase separately discovered that its own instrumentation measurably altered test timing (a ~3.6× slowdown) and failed to capture any logcat evidence in any run. The intermittent condition, its mechanism, and the first-observable-divergence question this phase set out to answer all remain open. No fix was implemented; the Phase 19.4B readiness implementation is unchanged and remains under investigation, not validated.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Forensic Report (Failure Not Reproduced, No Fix Implemented) | — | — |

---

**End of Document — Phase 19.4G Continuous AUT Visibility Failure-Window Forensic Analysis Report, v1.0**
