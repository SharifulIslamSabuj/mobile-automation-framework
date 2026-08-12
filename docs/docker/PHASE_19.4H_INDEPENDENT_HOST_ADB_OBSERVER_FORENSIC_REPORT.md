---
document_id: PHASE-19.4H
title: Independent Host-Side ADB Observer Forensic Proof
version: v1.0
status: Final — Forensic Report (Target Failure Not Reproduced, No Fix Implemented)
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D, PHASE-19.4E, PHASE-19.4F, PHASE-19.4G]
classification: Internal
---

# Phase 19.4H — Independent Host-Side ADB Observer Forensic Proof

---

## 1. Objective

Capture continuous, time-correlated Android/emulator evidence from the GitHub Actions host side — entirely independent of the Docker container, the Java test harness, the `AndroidDriver` instance, and the test's own Appium session — while the Dockerized `LoginTest` runs normally, to observe the ~15-second window between AUT readiness success (T0) and the first test assertion's outcome without repeating Phase 19.4G's session-contention failure.

---

## 2. Context

Phase 19.4F reproduced the intermittent failure directly (readiness check succeeds with all ActivityManager-level signals healthy; AUT never becomes visible; launcher shown in the failure screenshot). Phase 19.4G attempted continuous in-process observation of the window following readiness, but its background thread shared the test's own Appium session, causing severe command-level contention: test duration inflated from a healthy ~30s to ~108.6s average, and 100% of logcat capture attempts failed. This phase replaces that design with an observer that never touches the test's `AndroidDriver` or Appium session at all.

---

## 3. Why Phase 19.4G Instrumentation Was Rejected

Phase 19.4G's design ran a background Java thread inside the same JVM as the test, issuing `mobile: shell` commands through the same `AndroidDriver`/Appium session the test itself used. Evidence from that phase showed this: (a) inflated simple UI actions (a field `clear`, a `type`, a tap) from under a second to 15–22 seconds each, (b) caused the diagnostic thread's own loop to overrun its coded 15.2-second bound by 3.2–3.5×, and (c) caused every one of 10 attempted logcat checkpoint dumps (via `mobile: shell` executing `sh -c "logcat ..."`) to fail. This phase's explicit mandate was to avoid sharing the Appium session entirely, per the user's own architecture diagram (Section 5).

---

## 4. Investigation Scope

In scope: a temporary CI workflow (`.github/workflows/phase-19-4h-host-observer.yml`) adding a host-side observer (continuous `adb logcat` capture plus a 1-second-interval `adb shell dumpsys`/`pidof` poller) that starts before the Dockerized Gate 7-8 test step and stops immediately after it, running up to 5 designated sequential CI executions.

**No changes to `AndroidDriverFactory.java` or any other production source were made this phase** — confirmed via `git diff e4d8175` returning empty for that file both before and after. This is a structural difference from every prior sub-phase (19.4E/F/G), all of which modified the driver factory.

---

## 5. Independent Observer Architecture

Implemented exactly per the required execution model: the GitHub Ubuntu runner hosts the Android emulator, host `adb`, and host Appium (unchanged Docker Model 3 architecture). The observer runs as two host-side background processes, started and stopped by the CI workflow script itself — never through the Docker container, never through the Java test harness, never through the `AndroidDriver` instance, never through the Appium session:

1. **Continuous logcat capture**: `adb logcat -c` (clear) followed by `adb logcat -v time > observer-logcat-raw.log &`, running for the observer's entire lifetime as a single long-lived background process (not repeated checkpoint dumps).
2. **State-sampling poller**: a bash loop, backgrounded, executing `adb shell dumpsys window`, `adb shell dumpsys activity activities`, and `adb shell pidof <package>` once per second, appending timestamped `[HOST-OBSERVER]` lines to `observer-state.log`.

Both processes were started immediately before the Gate 7-8 `docker run ... gradlew test` command and explicitly killed (`kill $PID`) immediately after it returned — decoupled from the JVM/test process lifecycle entirely, which also solves Phase 19.4G's "daemon thread killed early" risk, since these are ordinary host processes under the workflow script's own control, not threads inside the test's JVM.

---

## 6. Observer Interference Controls

- No input events, no AUT launch/stop, no app-data clearing, no emulator restart — only read-only `dumpsys`/`pidof`/`logcat` commands.
- All observer commands go over `adb`'s own device-shell channel, a transport entirely separate from Appium's HTTP-to-UiAutomator2-server command path — the two do not share a command queue.
- A single combined `dumpsys window` / `dumpsys activity activities` call per poll extracts multiple fields (avoiding duplicate round trips), mirroring the efficiency lesson from Phase 19.4E/F/G.
- Interference was measured directly (Section 18), not assumed.

---

## 7. Sampling Strategy

1-second polling interval, justified as follows: each `adb shell` invocation over this environment's connection typically completes in well under 500ms (consistent with every prior phase's individual `dumpsys`/`pidof` calls when not contended), so three sequential calls per iteration (window, activity, pidof) comfortably fit inside a 1-second cycle with headroom, avoiding the iteration-stacking failure Phase 19.4G exhibited at a tighter 700ms interval combined with 5–6 calls per iteration. Actual observed sample counts (59–67 per ~29-second run, Section 10) confirm the interval was achieved in practice, not merely intended.

---

## 8. Logcat Capture Strategy

A single continuous `adb logcat -v time` process, started after a buffer clear, capturing everything from before the Docker test step begins until immediately after it ends — not repeated `logcat -d` checkpoint dumps (the approach that failed 100% of the time in Phase 19.4G). The raw capture is preserved in full (25,938–30,941 lines per run) and a filtered copy (tag/package-matched, 683–776 lines per run) is produced by simple post-hoc `grep`, not live filtering, so no event within the captured window can be missed by a live-filter misconfiguration.

---

## 9. Timing Correlation Model

Both the observer (`date -u +"%Y-%m-%d %H:%M:%S.%3N"`) and the JVM's own `automation.log` (SLF4J default timestamp format) write **identical-format, identical-timezone (UTC) wall-clock timestamps**, and both processes run on the same physical CI runner sharing the same system clock. This permits direct, exact timestamp correlation between the observer's independent evidence and the test framework's own event log — a materially stronger correlation method than any prior sub-phase achieved (19.4E/F/G could only use elapsed-time-since-T0 approximations).

---

## 10. Run Results

| Run | CI Run ID | Script | Result | Test time | Observer samples | Logcat raw/filtered lines |
|---|---|---|---|---|---|---|
| 1 | `31324166317` | Buggy (`dumpsys window windows`) | **Fail** (off-target — Section 11/19) | 26.686s | 61 (window-focus fields unavailable) | 29,456 / 731 |
| 2 | `31324892146` | Corrected (`dumpsys window`) | Pass | 28.553s | 59 | 30,941 / 683 |
| 3 | `31325094216` | Corrected | Pass | 30.885s | 67 | 29,643 / 776 |
| 4 | `31325308237` | Corrected | Pass | 28.92s | 62 | 27,371 / 689 |
| 5 | `31325523577` | Corrected | Pass | 29.14s | 66 | 25,938 / 773 |

Run 1 used `adb shell dumpsys window windows` (two arguments), which returned a dump format that did not match this environment's Android API level, leaving `mCurrentFocus`/`mFocusedApp` unavailable for that run only. This was corrected to the single-argument `adb shell dumpsys window` (matching the command proven working in Phase 19.4E/F/G's own Appium-based instrumentation) before Run 2, and verified fixed immediately (Section 11). `mResumedActivity` was never available in any run, in any dump — consistent with the same finding in Phase 19.4G Section 13, now confirmed a third time via completely independent tooling.

---

## 11. Passing Run Evidence

Runs 2–5 all passed. A representative healthy-run transition (Run 2), reconstructed by directly correlating `observer-state.log` against `automation.log` on shared wall-clock time:

```
16:55:00.179  automation.log: "Initializing driver for test method"
16:55:06.656  observer: launcher focused, AUT processState=NOT_RUNNING (last pre-launch sample)
16:55:07.788  observer: AUT processState=RUNNING pid=5101; topResumedActivity=AUT SplashActivity;
              mCurrentFocus / mFocusedApp STILL show the launcher
16:55:09.481  automation.log: "========== TEST START =========="  (readiness check succeeded)
16:55:09.500  observer: mCurrentFocus=null; mFocusedApp / topResumedActivity = AUT MainActivity;
              processState=RUNNING (a transient WindowManager/ActivityManager mismatch, ~19ms after TEST START)
16:55:10.694  observer: mCurrentFocus = AUT MainActivity (fully converged); all signals healthy
```

The same `mCurrentFocus=null`-during-transition pattern was independently confirmed in **every one of the 4 corrected runs** (2, 3, 4, 5) at the equivalent point in each run's timeline — see Section 14.

---

## 12. Failing Run Evidence

Run 1 failed, but at a different point in the test and with a different symptom than the target failure — see Section 19 for full analysis. No run in this phase reproduced the specific target failure signature (readiness succeeds, `mFocusedApp`/`topResumedActivity`/process all healthy, then the AUT never becomes visible and the launcher appears).

---

## 13. Continuous Activity Timeline

For Run 1 (the only failing run), correlating `automation.log`, `observer-state.log`, and `observer-logcat-raw.log` on shared wall-clock time:

| Time | Event | Source |
|---|---|---|
| 16:38:29.571 | "Tap to login with given credentials" clicked | automation.log |
| 16:38:27.621–16:38:45.079 | `topResumedActivity` = AUT `MainActivity`, **same `ActivityRecord{27ee48e}` identity, unchanged for the entire window** | observer-state.log (14 consecutive samples) |
| 16:38:44.835 | Assertion failed: "Product Catalog screen (post-login destination)" not visible | automation.log |
| 16:38:45.433 | Failure screenshot captured | automation.log |
| 16:38:46.145 | `driver.quit()` called | automation.log |
| 16:38:47.011 | "Force stopping com.saucelabs.mydemoapp.android ... from pid 6034" | observer-logcat-raw.log |
| 16:38:47.013 | "Force removing ActivityRecord{27ee48e} ... app died, no saved state" | observer-logcat-raw.log |

The AUT process was never killed, crashed, or force-stopped **during** the failure window — the only force-stop/kill events in the entire logcat capture occur at 16:38:47.011 onward, i.e., **after** `driver.quit()` was already called as part of normal test-session teardown, not as a cause of the failure.

---

## 14. Window State Timeline

Across Runs 2, 3, 4, and 5 (all passing), the same transient divergence was observed at the AUT's launch transition, each independently timestamped:

| Run | Timestamp | `mCurrentFocus` | `mFocusedApp` | `topResumedActivity` |
|---|---|---|---|---|
| 2 | 16:55:09.500 | `null` | AUT MainActivity | AUT MainActivity |
| 3 | 16:59:34.268 | `null` | launcher | AUT SplashActivity |
| 4 | 17:05:03.640 | `null` | AUT SplashActivity | AUT SplashActivity |
| 5 | 17:09:26.209 | `null` | AUT SplashActivity | AUT SplashActivity |

Run 3 shows a further nuance beyond Phase 19.4E/F/G's own observations: at that sample, `mFocusedApp` itself still showed the **launcher** while `topResumedActivity` had already moved to the AUT — meaning `mFocusedApp` and `topResumedActivity`, both nominally "ActivityManager-level" signals, are not always in lockstep with each other either, not just with `mCurrentFocus`. **VERIFIED** across 4 independent, non-Appium-session observations.

---

## 15. Process State Timeline

In every run, `processState` transitioned cleanly from `NOT_RUNNING` to `RUNNING pid=<pid>` exactly once, with a stable pid thereafter for the remainder of that run — no run showed the process disappearing, restarting, or changing pid mid-run (Run 1's process, pid 5306, remained stable for the entire 16:38:27–45 failure window before being force-stopped during post-failure teardown at 16:38:47).

---

## 16. Logcat Analysis

Logcat capture **succeeded in 100% of runs (5/5)** — a complete reversal of Phase 19.4G's 100% failure rate (0/10 checkpoints). For Run 1's failure window specifically (16:38:27–46), the filtered logcat shows routine `AppsFilter`/`ActivityManager` package-visibility bookkeeping and IME/input-method service activity, but **no crash indicator, no ANR indicator, no `AndroidRuntime` exception, and no `WindowManager`/`ActivityTaskManager` transition event involving the AUT** during the actual failure window — the AUT simply continued running, unremarked-upon by the system, the entire time. **VERIFIED**: the absence of crash/ANR/transition events during this specific window is a real finding, not an assumption, since the full window was continuously captured (Section 8).

---

## 17. Appium Correlation

The Appium server's own log (`appium-server.log`) is written to `${RUNNER_TEMP}`, outside the paths this workflow uploads as artifacts — the same gap noted in Phase 19.4F/G. **NOT AVAILABLE** for direct Appium-side correlation in this phase either; not fixed, since fixing it was not required by this phase's brief (which centers on host-adb observation, not Appium log capture) and doing so was out of scope.

---

## 18. Observer Interference Analysis

**Interference was negligible — the opposite of Phase 19.4G's finding.** Average test duration across the 4 valid corrected runs (2–5): `(28.553 + 30.885 + 28.92 + 29.14) / 4 ≈ 29.37s`, essentially identical to Phase 19.4F's own healthy baseline (`~30.0s` average across its Runs 1–4). Individual action timings in Run 1's `automation.log` (clicks, clears, types, all under ~1.4s each outside the genuine 15.264s explicit-wait timeout) show no contention signature at all, unlike Phase 19.4G's uniform 15–22s inflation of every simple action. **VERIFIED**: this observer design achieves the phase's own stated target of "observation with negligible practical impact." The only genuine cost discovered was the command-syntax bug in Run 1 (Section 10), which affected data completeness, not test timing.

---

## 19. Passing vs Failing Comparison

Run 1's failure does **not** match the target failure signature from Phase 19.4F/A-G: no launcher appeared in either failure screenshot; instead, both screenshots show the AUT's own **Login screen**, displaying a **"Username is required" client-side validation error**, with both the username and password fields empty. `automation.log` shows the framework's own `clear`/`type` actions on both fields were reported as successfully "Performed" at 16:38:24.249–16:38:26.352, yet at submission time (16:38:29.571) the fields were evidently empty, and the app's own validation correctly rejected the submission. The subsequent assertion failure ("Product Catalog screen not visible," `LoginTest.java:61`) is a **downstream symptom** of this earlier, unnoticed text-entry failure, not a recurrence of the AUT-visibility/launcher race this entire investigation (Phases 19.4A–H) has targeted.

**This is a genuinely different, previously unrecognized intermittent failure mode** — text entry silently not registering (or being silently cleared) before form submission — discovered incidentally by this phase but outside its scope to root-cause further.

---

## 20. First Observable Divergence

**For the target failure (AUT-not-visible/launcher race): no divergence toward that failure was observed, because it did not occur in this sample.** The `mCurrentFocus`-lag pattern documented in Section 14 is the earliest and most consistent divergence observed in this phase, but it appeared in **every passing run**, resolving within roughly 1.2 seconds in all cases (Run 2's example: `null` at 16:55:09.500, converged by 16:55:10.694) — it is evidently a normal, benign, self-resolving part of the AUT's launch sequence, not by itself the failure mechanism, consistent with Phase 19.4F's own finding that this signature was indistinguishable between its passing and failing run.

**For Run 1's actual (off-target) failure**: the first observable divergence from a normal login flow is the absence of any device-side confirmation that the username field retained the typed text — but this phase's observer does not capture field-level UI content (only Activity/Window/process state), so this is **INFERRED** from the failure screenshot's validation message, not directly observed at the moment of divergence.

---

## 21. Root Cause Assessment

**Not reached for the target failure** — it was not reproduced in this sample, so none of the candidate mechanisms (A–H) can be confirmed or excluded against direct evidence this phase.

For Run 1's off-target failure: **NOT VERIFIED** as to mechanism. The observed symptom (validation error implying empty text fields at submission) is consistent with several distinct possible causes — a UiAutomator2 `sendKeys`/`clear` reliability issue, a race between the password field's `type` action and the subsequent login-button tap, or an IME (keyboard) interaction quirk (logcat shows `GoogleInputMethodService` activity in this same window) — but this phase's evidence does not distinguish between them, and doing so is outside this phase's scope (host-side Activity/Window/process/logcat observation, not field-content verification).

---

## 22. What Is Ruled Out

- **For the target failure**: nothing new is ruled out — no failure occurred to test any hypothesis against.
- **For Run 1's failure**: an AUT crash or process restart during the failure window is ruled out (**VERIFIED** — stable pid, no crash/kill logcat events until after `driver.quit()`, Section 13/16). A launcher takeover is ruled out (**VERIFIED** — both failure screenshots show the AUT's own Login screen, Section 19).
- **General**: the hypothesis that Phase 19.4G's contention was an unavoidable cost of any background observation is ruled out — this phase's host-side design achieved negligible interference (Section 18), proving low-impact continuous observation is achievable when the observer does not share the Appium session.

---

## 23. Remaining Unknowns

- The target failure's mechanism remains exactly as unknown as it was at the end of Phase 19.4G — this phase adds corroborating evidence about the healthy-path `mCurrentFocus` lag pattern but no failure-path evidence.
- The mechanism behind Run 1's text-entry failure (Section 21) — a distinct, newly-discovered open question.
- Why `mResumedActivity` is never present in any `dumpsys activity activities` output captured across this entire engagement (19.4G and 19.4H both) — still unconfirmed whether it requires a different command/API-level.
- Whether the target failure would eventually reproduce with the same low-interference design given a larger sample — Section 20 of Phase 19.4F's binomial reasoning still applies: a 5-run sample has a real chance of missing a ~20%-rate intermittent event by chance alone.

---

## 24. Recommended Next Step

Not a fix (out of scope). Two independent, concrete next steps follow directly from this phase's findings:

1. **Continue using this phase's host-side observer architecture** (now proven low-interference and reliable — 100% logcat success, dense continuous sampling, no measurable timing distortion) for any further attempt at capturing the target failure. A larger bounded sample (e.g., 10–15 runs, following Phase 19.4F's own binomial reasoning) is now a low-cost way to improve the odds of an actual capture, since this design does not waste evidence quality the way Phase 19.4G's did.
2. **Investigate Run 1's text-entry failure as its own, separate thread of work** — it is a real, reproducible-albeit-once-observed distinct intermittent condition in the login flow's `clear`/`type` sequence, unrelated to driver readiness, and deserves its own evidence-first investigation rather than being folded into the AUT-visibility investigation this phase was scoped to.

---

## 25. Final Verdict

# FAILURE NOT CAPTURED WITHIN BOUNDED SAMPLE

The target failure (readiness succeeds with healthy ActivityManager signals, then the AUT never becomes visible and the launcher is shown) was not reproduced in any of the 5 designated sequential runs. This phase's host-side observer architecture is validated as a low-interference, reliable design (negligible timing impact vs. Phase 19.4G's 3.6× inflation; 100% logcat capture success vs. 0%; dense continuous sampling) and should be reused for future sampling attempts. This phase's sample did, however, produce two pieces of independent value: (1) corroboration, via completely non-Appium-session tooling, of the transient `mCurrentFocus=null`/ActivityManager-lag pattern first observed in Phase 19.4E/F, now confirmed benign and self-resolving in every healthy run observed; and (2) the incidental discovery of a separate, previously unrecognized intermittent failure mode (login-form text entry not registering before submission) that is unrelated to this investigation's target and is recommended as its own follow-up. No fix was implemented; the Phase 19.4B readiness implementation and all production files are unchanged and remain under investigation, not validated.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Forensic Report (Target Failure Not Reproduced, No Fix Implemented) | — | — |

---

**End of Document — Phase 19.4H Independent Host-Side ADB Observer Forensic Report, v1.0**
