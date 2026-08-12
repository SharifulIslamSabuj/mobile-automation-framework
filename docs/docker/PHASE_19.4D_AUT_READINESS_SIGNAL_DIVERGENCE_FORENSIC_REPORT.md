---
document_id: PHASE-19.4D
title: AUT Readiness Signal Divergence Forensic Investigation
version: v1.0
status: Final — Forensic Investigation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C]
classification: Internal
---

# Phase 19.4D — AUT Readiness Signal Divergence Forensic Investigation

No `AndroidDriverFactory.java`, `Dockerfile`, `.dockerignore`, or production `mobile-automation.yml` was modified anywhere in this investigation — verified explicitly (Section 4, and the final integrity check below). Three temporary, read-only-intent diagnostic workflows were added, iterated on, and removed; nothing they added is a production change.

```
$ git diff e4d8175 -- src/main/java/com/mobileautomation/framework/driver/AndroidDriverFactory.java Dockerfile .dockerignore .github/workflows/mobile-automation.yml
(empty)
```

---

## 1. Objective

Determine, with direct evidence, why `queryAppState(appPackage)` could report `RUNNING_IN_FOREGROUND` (Phase 19.4C Run 2) while the Android launcher was, by screenshot, still visibly on screen — and identify the strongest available readiness signal for this framework, without implementing any fix.

---

## 2. Established Baseline

Not re-investigated. Phase 19.4A proved Appium can report session success without the AUT genuinely launching. Phase 19.4B added a `queryAppState`-based readiness check. Phase 19.4C then proved, directly, that this specific check can pass (no exception, driver init completed in 8.59s) in a run whose screenshot showed the launcher — disproving "RUNNING_IN_FOREGROUND guarantees visible readiness."

---

## 3. Why Phase 19.4B Was Insufficient

Established by Phase 19.4C, not re-derived here: the check's signal (`ApplicationState.RUNNING_IN_FOREGROUND` via Appium's `mobile: queryAppState`) resolved successfully in a run where the visible UI state was, by direct screenshot evidence, the launcher — meaning this specific signal is not a sufficient proxy for "the AUT is visibly ready for interaction" in every case.

---

## 4. Diagnostic Scope

Reproduce `LoginTest` against the unmodified Phase 19.3 Docker harness image, exactly as Phase 19.4C did, while an external, read-only monitor samples multiple independent signals (Appium's own `queryAppState`, ADB `dumpsys window`/`dumpsys activity`, AUT process existence, screenshots, UI hierarchy) at high frequency, to capture the state transition directly — in either a healthy or a divergent occurrence.

---

## 5. Reproduction Environment

Identical to Phase 19.4C in every respect: same unmodified Dockerfile (image tag varied only cosmetically per run: `phase19.4d`), same `.dockerignore`, same GitHub-hosted `ubuntu-24.04` runner, same API 34/`google_apis`/`x86_64`/pixel emulator spec, same Appium 3.6.0/UiAutomator2 8.2.2, same `LoginTest` representative flow, same `--network=host`/`--user $(id -u):$(id -g)`/`GRADLE_USER_HOME` `docker run` invocation as the fix-validated configuration.

---

## 6. Instrumentation Design

Three iterations were required, each a genuine methodological correction driven by direct evidence of the previous attempt's limitation — not guesses:

| Iteration | Design | Result | Why it changed |
|---|---|---|---|
| 1 | Fixed 40s window; session ID located by scanning new lines of Appium's server log | All `queryAppState` samples returned `"invalid session id"` (post-termination) | Gradle's cold-start (~44-90s, observed elsewhere in this investigation) consumed most of the fixed window before session creation even began |
| 2 | Extended window to 150s, same log-scan detection | Still all samples post-termination | The framework's own session lifecycle (creation → readiness check → test → quit) can complete in a span comparable to or shorter than the log-scan loop's own per-iteration latency once Gradle's overhead is behind it |
| 3 | Switched to polling Appium's `GET /sessions` endpoint directly (real-time, no log-parsing) | `GET /sessions` returned **HTTP 404** in every attempt — confirmed via the Appium server's own HTTP access log | **New, genuine, verified finding**: Appium 3.6.0 does not support the legacy `/sessions` list endpoint in this configuration |

All 15 total `LoginTest` attempts across the three diagnostic runs **passed** — the divergence did not reproduce during this phase's own execution.

---

## 7. Healthy-State Evidence

Captured directly (Diagnostic Run 1, Attempt 1) via the ADB-level signals, which worked correctly throughout despite the Appium-side instrumentation limitations above:

```
Sample 1-49  (launcher, pre-session):
  mFocusedApp=.../NexusLauncherActivity, topResumedActivity=.../NexusLauncherActivity, (no AUT process)

Sample 50 @ 07:13:53.201:
  mFocusedApp=com.saucelabs.mydemoapp.android/.view.activities.SplashActivity
  topResumedActivity / ResumedActivity = same SplashActivity
  process: u0_a195  ...  R  com.saucelabs.mydemoapp.android   (Running)

Sample 51 @ 07:13:54.309:
  mCurrentFocus=Window{...SplashActivity}   (window focus now confirmed, not just activity-manager state)

Sample 52 @ 07:13:55.428:
  mFocusedApp / topResumedActivity / ResumedActivity = .../MainActivity
  process: R (running)

Sample 53 @ 07:13:56.398:
  mCurrentFocus=Window{...MainActivity}
```

**VERIFIED**: in this healthy occurrence, `mFocusedApp`/`topResumedActivity`/`ResumedActivity` and `mCurrentFocus` all transitioned from the launcher to the AUT together, within about 1.1 seconds of each other (samples 50→51), and the AUT process existed with state `R` throughout. No divergence between these signals was observed in any healthy sample across all 15 attempts.

---

## 8. Bad-State Evidence

Not re-captured in this phase (Section 6) — cited from Phase 19.4C Run 2, the only direct observation available: driver initialization (including the `queryAppState` readiness poll) completed successfully in 8.59 seconds with no exception, yet the failure screenshot 15.5 seconds later showed the launcher. No ADB-level signals (`dumpsys window`/`activity`, process list) were captured at the exact moment of that divergence, since Phase 19.4C did not run the instrumentation this phase added. **This is the central evidence gap this phase was unable to close.**

---

## 9. Appium State Evidence

**NOT VERIFIED via live capture in this phase.** Every `queryAppState` sample collected across all three diagnostic runs was either pre-session (`SID=none`) or post-session (`"invalid session id"`) — none coincided with an active session, in either a healthy or divergent occurrence. What is confirmed: `GET /sessions` returns HTTP 404 on this Appium 3.6.0 configuration (Section 6), a genuine finding narrowing future instrumentation options, but not itself informative about the app-state divergence question.

---

## 10. Android Process Evidence

**VERIFIED for the healthy case** (Section 7): AUT process appears with state `R` (running) at the same sample the activity-manager signals first show the AUT, and persists through the remainder of the observed window. No process-existence evidence was captured for a divergent occurrence in this phase (Section 8's gap).

---

## 11. Android Activity Evidence

**VERIFIED for the healthy case**: `topResumedActivity` and `ResumedActivity` (from `dumpsys activity activities`) matched `mFocusedApp` (from `dumpsys window`) at every healthy sample — no divergence observed between these two specific signals in any of the 15 healthy attempts.

---

## 12. Android Window/Focus Evidence

**VERIFIED for the healthy case**: `mCurrentFocus` (window-level) followed `mFocusedApp`/`ResumedActivity` (activity-manager-level) within about 1 second in the one healthy transition directly observed (Section 7, sample 50→51) — the two levels of signal were never seen to diverge from each other in this phase's data, only sequenced (activity-manager state updates fractionally before window focus in the one transition captured).

---

## 13. UI Hierarchy Evidence

**BLOCKED / NOT VERIFIED.** `uiautomator dump` was captured at the end of each attempt (post-test), but since every attempt passed, these dumps show the final, already-correct UI state, not a divergent moment. No informative UI-hierarchy evidence for the bad state was obtained.

---

## 14. Screenshot Evidence

Final screenshots captured at the end of all 15 attempts in this phase show the expected passing UI state — consistent with, but not adding beyond, the JUnit results already confirming 15/15 passes. The one screenshot showing the bad state remains Phase 19.4C's own (Section 8), not reproduced here.

---

## 15. Timing Analysis

The one clearly evidenced timing fact this phase adds: in a healthy transition, activity-manager-level state (`mFocusedApp`/`ResumedActivity`) and window-level focus (`mCurrentFocus`) converge within about 1 second of each other, both well before the framework's own `driver.explicitWaitTimeoutSeconds` (15s) budget would be at risk. This is consistent with — but does not by itself explain — how a divergence lasting the *entire* 15s budget (Phase 19.4C Run 2) could occur elsewhere.

---

## 16. Signal Comparison Matrix

| Signal | Healthy State (this phase, directly observed) | Bad State (Phase 19.4C, directly observed) | Reliable for AUT Readiness? |
|---|---|---|---|
| `queryAppState` (Appium) | Not live-captured this phase; INFERRED to have resolved `RUNNING_IN_FOREGROUND` (readiness check passed, no exception) in both healthy attempts here and the Phase 19.4C bad-state run | Resolved `RUNNING_IN_FOREGROUND` — **VERIFIED insufficient alone** (Phase 19.4C) | **No, alone** — proven to pass in at least one bad-state occurrence |
| AUT process existence (`ps -A`) | VERIFIED present (state `R`) once transition begins | NOT VERIFIED (not captured in Phase 19.4C) | Unknown alone — untested against the actual divergence |
| `mFocusedApp`/`ResumedActivity`/`topResumedActivity` | VERIFIED to correctly show the AUT once transition begins | NOT VERIFIED (not captured in Phase 19.4C) | Unknown alone — untested against the actual divergence |
| `mCurrentFocus` (window-level) | VERIFIED to correctly show the AUT, ~1s after activity-manager state | NOT VERIFIED (not captured in Phase 19.4C) | Unknown alone — untested against the actual divergence, but conceptually the closest to "what is visually rendered" of the signals available |
| Screenshot | VERIFIED shows AUT in every healthy sample | VERIFIED shows launcher (Phase 19.4C) | The ground truth all other signals are being judged against, but not usable as a bounded, programmatic readiness condition |

**The matrix's most important entry is also its largest gap**: no signal other than `queryAppState` has been directly tested against an actual occurrence of the divergence. This phase strengthened confidence in what *healthy* looks like across every signal, but did not obtain a bad-state sample to determine which signal(s), if any, would have correctly caught Phase 19.4C's specific failure.

---

## 17. Root Cause Classification

Per the phase's own taxonomy (A–I): the evidence gathered here is **insufficient to select a single classification with direct proof**. The closest, most evidence-consistent candidates remain **E (race condition between launch and readiness check)** and **A (process state confused with window visibility)** — both consistent with Phase 19.4C's facts (fast, exception-free readiness resolution; launcher still visible) — but neither is directly proven by this phase's own new data, since no live bad-state sample of any signal (Appium or ADB) was captured. **Classification: NOT VERIFIED — insufficient direct evidence to select a single cause over these two candidates.**

---

## 18. Candidate Readiness Signals

| Candidate | Classification | Basis |
|---|---|---|
| `queryAppState` alone (current Phase 19.4B implementation) | **REJECTED as sufficient on its own** | Directly disproven, Phase 19.4C |
| AUT process existence (`ps -A` / equivalent) | **NOT VERIFIED** | Confirmed present in every healthy sample this phase captured, but never tested against a bad-state occurrence — a divergence could plausibly still show a running process (Phase 19.4A's diagnostic did show a genuine "no process at all" case, but Phase 19.4C's case is evidenced to be different — no exception was thrown, meaning `queryAppState` genuinely returned 4/foreground, which is architecturally harder to reconcile with "no process" than with "process exists but isn't the visible window") |
| `mCurrentFocus` / focused window package match | **INFERRED plausible, NOT VERIFIED** | Conceptually the closest available signal to "what is visually rendered," and confirmed reliable in every healthy sample; never tested against the actual divergence |
| Combination: `queryAppState == RUNNING_IN_FOREGROUND` AND `mCurrentFocus` package matches `appPackage` | **INFERRED as the strongest available candidate, NOT VERIFIED** | Requires two independent signals to agree, directly closing the exact gap Phase 19.4C exposed (one signal alone was insufficient) — but not proven against a real divergence occurrence, since none was captured |
| Requiring the readiness state to hold across two consecutive polls rather than one | **INFERRED as a plausible complementary hardening, NOT VERIFIED** | Would guard against a transient/flapping state; no evidence was gathered on whether the actual divergence is transient-flapping or sustained |
| Exact `currentActivity()` matching | **REJECTED**, consistent with Phase 19.4A/19.4B's own prior reasoning | Already rejected earlier in this investigation for splash/transition unreliability; nothing in this phase's evidence changes that |

---

## 19. Rejected Signals

`queryAppState` alone (Section 18) and exact activity-string matching (carried forward from Phase 19.4A/B) are the only signals this investigation can actively reject with direct evidence. No other candidate has been tested enough, in either direction, to reject it.

---

## 20. Strongest Evidence-Supported Readiness Condition

Given the evidence actually in hand — not a guess — the strongest defensible candidate is the **combination** condition (Section 18): `queryAppState == RUNNING_IN_FOREGROUND` cross-checked against `mCurrentFocus`'s package matching `appPackage`. This is the only candidate that directly targets the specific, proven failure mode (one signal reporting success while the visible window is something else) by requiring two independently-sourced signals to agree. It is marked **INFERRED, not VERIFIED**, because this phase could not test it against a live occurrence of the divergence — only against 15 healthy samples, where (per Section 16) both signals were never observed to disagree.

---

## 21. Risks

| Risk | Severity | Notes |
|---|---|---|
| Recommending a fix based on an INFERRED (not VERIFIED) mechanism | Medium | Explicitly flagged — Section 20's recommendation is the best-supported option available, not a proven-correct one |
| The divergence's true frequency remains uncharacterized | Medium | 1 confirmed occurrence in ~10 total real/CI attempts across Phase 19.4C and this phase combined (excluding the 15 diagnostic passes, which used a different reproduction pattern — back-to-back single-job attempts vs. Phase 19.4C's separate, sequential workflow runs) |
| Further diagnostic iteration has a real resource cost | Low-Medium | Three diagnostic runs (15 attempts) already consumed in this phase alone without reproducing the target condition |

---

## 22. Remaining Unknowns

1. What any Android/Appium signal shows *during* an actual occurrence of the divergence — still not captured, by any of the three instrumentation designs attempted.
2. Whether the divergence reproduces more reliably under Phase 19.4C's exact reproduction pattern (a fresh, separate workflow run immediately following another) than under this phase's pattern (5 attempts within one long-lived job/emulator boot) — a real, untested variable this phase's own data cannot rule in or out.
3. Whether `GET /sessions` is disabled by a specific Appium 3.x server flag/setting that could be re-enabled for a future diagnostic attempt, or whether a different Appium REST path (e.g., checking via the driver's own internal session-tracking rather than a list endpoint) would work better.

---

## 23. Recommended Next Step

Two options, not implemented here:

1. **Match Phase 19.4C's exact reproduction pattern** — run two genuinely separate, sequential CI workflow invocations (not 5 attempts within one job) with instrumentation active in both, since that is the pattern in which the divergence has actually been observed so far.
2. **Instrument from inside the framework temporarily** (would require a scoped, clearly-labeled diagnostic-only modification to `AndroidDriverFactory.java`, explicitly reverted afterward) to log all candidate signals — `queryAppState`, `getCurrentPackage()`, and a raw ADB `dumpsys` shell-out — at the exact moment the existing readiness poll resolves, removing all external-timing-correlation risk entirely. This would be a more invasive diagnostic than this phase attempted, and was not pursued here since it risks conflating diagnostic and production code even temporarily; recommended only if further external-instrumentation attempts continue to fail.

---

## 24. Final Verdict

# B. READINESS DIVERGENCE PARTIALLY UNDERSTOOD — ADDITIONAL FORENSIC EVIDENCE REQUIRED

This phase strengthened the evidence base in genuine, verified ways: it confirmed exactly what a healthy launcher-to-AUT transition looks like across every available ADB-level signal (all converging within ~1 second, all agreeing with each other), and it surfaced two real, previously-unknown environmental facts (the framework's session lifecycle can outrun external log-scan-based detection; Appium 3.6.0's `GET /sessions` endpoint is unavailable). It did **not** succeed in directly observing any signal during an actual occurrence of the divergence, despite three escalating, evidence-driven instrumentation redesigns across 15 total attempts — the target condition simply did not reproduce in this phase's own execution pattern.

Per this phase's own most important rule, no fix is proposed as final or implemented here. Section 20 identifies the best currently-supported *candidate* (a two-signal combination check), explicitly marked INFERRED rather than VERIFIED, for whoever authorizes the next phase to weigh against the cost of further diagnostic iteration (Section 23) versus proceeding with a reasoned, evidence-informed (if not fully evidence-proven) hardening of the Phase 19.4B fix.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Forensic Investigation Report | — | — |

---

**End of Document — Phase 19.4D AUT Readiness Signal Divergence Forensic Report, v1.0**
