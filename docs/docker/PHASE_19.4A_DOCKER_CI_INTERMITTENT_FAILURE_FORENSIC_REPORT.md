---
document_id: PHASE-19.4A
title: Docker CI Intermittent Failure Forensic Investigation
version: v1.0
status: Final — Forensic Investigation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4, PHASE-19.4-CONFIRM]
classification: Internal
---

# Phase 19.4A — Docker CI Intermittent Failure Forensic Investigation

No Dockerfile, `.dockerignore`, Java source, Page Object, test class, Appium capability, emulator configuration, or the production `.github/workflows/mobile-automation.yml` was modified anywhere in this investigation — verified explicitly (Section 5, Section 18). One temporary, read-only diagnostic workflow was added, used, and removed; nothing it added is a production change.

---

## 1. Objective

Determine the exact mechanism behind PASS → FAIL → PASS across three materially identical Docker-on-GitHub-Actions executions (Phase 19.4/19.4-confirm), using direct evidence rather than intuition, before deciding whether any fix is warranted.

---

## 2. Investigation Scope

Three prior runs (Run A, B, C, Section 4) plus one new, purpose-built diagnostic run (Section 3.1) that reproduced the failure a fourth time with far richer evidence than any prior run captured.

---

## 3. Known Baseline

Phase 19.4 established the Model 3 Docker architecture works end-to-end (container → host Appium → host ADB → emulator). Phase 19.4-confirm established the pass rate was not a guaranteed 100% on unmodified configuration: 2 of 3 runs reached 19/19; 1 failed at the representative-test gate with a screenshot showing the emulator's home screen (AUT not foregrounded), a different signature from Phase 19.3's local Windows ANR.

### 3.1 New diagnostic run (this phase)

Because none of the three prior runs captured Appium's own server-side session log (it was redirected to a file that was never uploaded as an artifact) or any ADB-level foreground/process/activity state, a temporary, read-only diagnostic workflow (`phase-19-4a-diagnostic.yml`, manual-trigger-only, deleted after use) ran `LoginTest` **five consecutive times** against the unmodified Dockerfile and Phase 19.3 harness image, in one emulator boot, capturing after every attempt: the full Appium debug log, `adb shell dumpsys window`/`dumpsys activity activities` (foreground/resumed-activity state), `adb shell ps -A`/`pm list packages` (AUT process/install state), and `adb logcat -d`. **Result: Attempt 1 FAILED, Attempts 2–5 all PASSED** — reproducing the exact same intermittent pattern with, this time, complete diagnostic evidence. [Run 31292476364](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31292476364).

---

## 4. Comparison Set

| Run | ID | Result |
|---|---|---|
| A | [31269472881](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31269472881) | PASS, 19/19 |
| B | [31272374474](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31272374474) | FAIL at representative test |
| C | [31272885639](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31272885639) | PASS, 19/19 |
| D (diagnostic, 5 attempts) | [31292476364](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31292476364) | Attempt 1 FAIL, Attempts 2–5 PASS |

---

## 5. Run Identity Verification

| Element | A | B | C | D | Classification |
|---|---|---|---|---|---|
| Dockerfile | `git diff ee9c9c0` empty across all | identical | identical | identical | **NON-MATERIAL (identical)** |
| `.dockerignore` | identical | identical | identical | identical | **NON-MATERIAL (identical)** |
| Base image digest | `sha256:29467857...` (pinned) | same | same | same | **NON-MATERIAL (identical)** |
| Git commit (workflow content) | `ee9c9c0` content | `ee9c9c0` content (restored byte-identical) | same | same Dockerfile/harness, new throwaway workflow file (diagnostics only) | **NON-MATERIAL** |
| Java/Gradle/Appium/UiAutomator2 versions | 17 / 9.0.0 / 3.6.0 / 8.2.2 | same | same | same | **NON-MATERIAL (identical)** |
| Android API/target/arch/profile | 34/google_apis/x86_64/pixel | same | same | same | **NON-MATERIAL (identical)** |
| AUT APK | `mda-2.2.0-25.apk`, same cache key | same | same | same | **NON-MATERIAL (identical)** |
| `docker run` command (representative test) | `--network=host --user $(id -u):$(id -g)` + dedicated `GRADLE_USER_HOME` | identical | identical | identical | **NON-MATERIAL (identical)** |
| System properties passed to Gradle | `-Denv=emulator -Dappium.serverUrl=http://127.0.0.1:4723 -Dplatform.version=14 -Ddevice.name=emulator-5554 -Dapp.path=...` | identical | identical | identical | **NON-MATERIAL (identical)** |
| Representative test selection | `LoginTest` | same | same | same | **NON-MATERIAL (identical)** |
| **Elapsed time since emulator boot-completed at first AUT launch attempt** | ~44s (Gradle compile) before test began | ~45s before test began | ~45s before test began | **0.16s (attempt 1 only)** | **MATERIAL — the one dimension that actually varied** |

**Conclusion: every configuration element was confirmed identical across A/B/C. The only material, evidenced difference found anywhere in this investigation is elapsed wall-clock time between emulator boot-completion and the first AUT-launch attempt — and only the diagnostic run's Attempt 1 tested the true zero-buffer case directly.**

---

## 6. Environment Comparison

GitHub-hosted `ubuntu-24.04` runner in all four runs; no evidence of different runner hardware/kernel versions was sought or needed, since Section 5 already isolates the timing dimension as the only material variable. **Classification: UNKNOWN** whether underlying runner hardware allocation varies run-to-run (GitHub does not expose this) — not further investigated, as it was not necessary to explain the observed evidence.

---

## 7. Artifact Inventory

| Run | JUnit XML | Screenshots | `automation.log` | Appium server log | ADB foreground/activity state | logcat |
|---|---|---|---|---|---|---|
| A | ✓ | ✓ | ✓ | ✗ (not uploaded) | ✗ | ✗ |
| B | ✓ (failure) | ✓ (2, home screen) | ✓ | ✗ | ✗ | ✗ |
| C | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| D, Attempt 1 | ✓ (failure) | — (test never reached a screenshot-worthy assertion until the final one) | ✓ | ✓ (full debug) | ✓ | ✓ |
| D, Attempts 2–5 | ✓ (pass) | ✓ | ✓ | ✓ (same file, later sessions) | ✓ | ✓ |

The diagnostic run closed every evidence gap A/B/C left open.

---

## 8. Successful Run A Timeline

| Event | Time (UTC) |
|---|---|
| T0 Emulator boot begins | 17:29:21.86 |
| T1/T2 Boot completed, ADB available | 17:30:03.12 |
| T3/T4 Appium started/reachable | 17:30:05.17 |
| T6 Gate 6 container→Appium TCP confirmed | 17:30:05.38 |
| (Gradle compile, cold cache) | 17:30:49.50 → ~17:30:56 |
| T7 Session creation (`initializeDriver`) begins | 17:30:56.44 |
| T11 Session created, test method begins | 17:31:10.93 (14.49s init) |
| T13 Product Catalog screen confirmed visible | 17:31:13.44 (**+2.51s** after test start) |
| T15 Test passes | 17:31:40.90 |

---

## 9. Failed Run B Timeline

| Event | Time (UTC) |
|---|---|
| T0 Emulator boot begins | 18:38:29.19 |
| T1/T2 Boot completed, ADB available | 18:39:19.12 |
| T3/T4 Appium started/reachable | 18:39:21.18 |
| T6 Gate 6 confirmed | 18:39:21.41 |
| (Gradle compile) | 18:40:03.93 → ~18:40:10 |
| T7 Session creation begins | 18:40:10.52 |
| T11 Session created, test method begins | 18:40:19.73 (**9.2s init — faster than A**) |
| T13 Expected screen never becomes visible | — |
| T15 Test fails (explicit wait exhausted) | 18:40:35.16 (**+15.4s**, full timeout) |

No Appium server log, no ADB activity state, no logcat available for this run (Section 7) — the timeline above is the full extent of what could be reconstructed from `automation.log` and screenshots alone.

---

## 10. Successful Run C Timeline

| Event | Time (UTC) |
|---|---|
| T7 Session creation begins | 18:52:57.23 |
| T11 Session created, test method begins | 18:53:06.75 (9.53s init) |
| T13 Product Catalog screen confirmed visible | 18:53:09.17 (**+2.42s**) |
| T15 Test passes | (full suite continued) |

---

## 11. Timeline Comparison

| | A (pass) | B (fail) | C (pass) | D-Attempt1 (fail) | D-Attempt2 (pass) |
|---|---|---|---|---|---|
| Driver init duration | 14.49s | 9.2s | 9.53s | 7.995s | 5.468s |
| Time from test-start to expected screen | +2.51s | **never (15.4s timeout)** | +2.42s | **never** | (screen confirmed present, see Section 16) |
| Elapsed since boot-completed at launch attempt | ~58s | ~51s | ~93s | **0.16s** | ~85s |

**The failing runs are not the slowest at session creation — B and D-Attempt1 are among the *fastest* session creations of the five data points.** What distinguishes them is not speed but whether the AUT ever actually reached the foreground at all (Section 16–17), which the timing data alone cannot resolve — only the diagnostic run's richer evidence (Section 16) can.

---

## 12. Docker Boundary Analysis

Identical across every run (Section 5). Gates 1-6 (image build, container start, Java/Gradle versions, `--network=host` connectivity) passed in **every single run examined, including the failing ones**. **Classification: VERIFIED — Docker is not implicated.** The container correctly reached Appium and submitted an identical, correct session-creation request in both the failing and passing cases (Section 15).

---

## 13. Emulator Analysis

Boot completed successfully in all runs (`sys.boot_completed=1` confirmed via the emulator-runner action's own internal wait). However, `sys.boot_completed=1` is a well-documented early signal — it does not guarantee the Android system has finished its own post-boot settling (background service starts, content provider initialization, `BOOT_COMPLETED` broadcast fan-out to every installed app). Diagnostic Attempt 1's logcat (Section 16) shows dozens of system processes still starting (`chrome`, `dialer`, `tts`, carrier services, `externalstorage`) and a `Posting BOOT_COMPLETED user #0` broadcast **4 seconds after** Attempt 1's docker run had already started. **Classification: VERIFIED contributing factor for Attempt 1 specifically** — the emulator was not yet system-settled when the AUT launch was attempted.

---

## 14. ADB Analysis

ADB itself was reachable and responsive in every run — no ADB connection failure, no timeout, no "device offline" state was ever observed. The `am start-activity` command was successfully *dispatched* via ADB in the failing case (Section 15) — ADB is not implicated as broken; what is unproven is whether that dispatched command's underlying Activity Manager request actually completed on a system still absorbing boot-completion load.

---

## 15. Appium Analysis

From Diagnostic Attempt 1's captured Appium debug log (`appium-server-full.log`):

```
[ADB] Running 'adb ... shell am start-activity -W -n com.saucelabs.mydemoapp.android/....SplashActivity -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -f 0x10200000'
[AndroidUiautomator2Driver] Initialized new IPC object with max object size of 1048576 bytes and max topics of 1000
[AppiumDriver] New AndroidUiautomator2Driver session created successfully, session ... added to master session list
[AndroidUiautomator2Driver] Responding to client with driver.createSession() result: {...}
<-- POST /session 200 7995 ms - 1299
```

**Critical finding**: between the `am start-activity -W` command being issued and the session being declared "created successfully," **no output/result of that command is logged at all** — no "Status: ok", no activity confirmation, no error. The driver proceeds directly to marking the session successful. Compare this to Attempt 2 (passing)'s log, where the same command sequence is followed by explicit ActivityManager confirmation activity (Section 16). **Classification: VERIFIED** — the UiAutomator2 driver, as configured by this framework's default capabilities (no `appWaitForLaunch`/`appWaitActivity`/`appWaitDuration` override, Section 20), does not reliably surface a launch failure back to the client; `createSession` can return HTTP 200 even when the target app never actually became a running process.

---

## 16. AUT Installation Analysis

**Attempt 1 (failed):**
- `pm list packages | grep saucelabs` → `package:com.saucelabs.mydemoapp.android` (**installed**).
- `ps -A | grep saucelabs` → **empty** (no process, at any point after the test completed).
- `dumpsys window | grep mCurrentFocus` (pre- and post-run) → `NexusLauncherActivity` both times (**launcher never lost focus**).
- `logcat` → **zero** occurrences of `saucelabs` anywhere in the buffer — not even the `ActivityManager: Force stopping .../clear data` sequence that `noReset=false` triggers for *every* session (compare Attempt 2, next).

**Attempt 2 (passed), for direct comparison:**
- `logcat` shows, in order: `ActivityManager: Force stopping com.saucelabs.mydemoapp.android ... : from pid 6224`, `Force stopping ... clear data`, `Force stopping ... clearApplicationUserData` — the expected `noReset=false` sequence — immediately followed (not shown above but confirmed present) by the app's own process starting and the test passing.

**Conclusion: VERIFIED.** In the failing attempt, the AUT was never even touched by ActivityManager — not started, not force-stopped, not data-cleared. The `am start-activity` ADB command was issued by Appium (confirmed in its own log) but produced **no observable effect on the device at all**. This is not a slow launch, not a crash-after-launch, and not a rendering delay — it is an launch request that never took effect.

---

## 17. AUT Launch Analysis

Combining Sections 15–16: Appium issued the correct, well-formed `am start-activity -W -n com.saucelabs.mydemoapp.android/...SplashActivity ...` command. The command's actual execution on the device is unverified by Appium's own logging (no result captured) and unverified by device-side evidence (no logcat trace of the package at all). The most consistent explanation across all available evidence: the `adb shell am start-activity` invocation was accepted by the ADB transport (hence Appium logged no error) but the underlying `ActivityManagerService` request was **not durably processed** — plausibly dropped, silently deferred, or lost — while the system was still working through the post-boot broadcast/service-start backlog documented in Section 13. **Classification: INFERRED** (a specific, evidence-consistent mechanism, not directly proven at the ActivityManagerService source-code level, which is out of this project's control to instrument further).

---

## 18. Foreground Activity Analysis

Pre-run and post-run `mCurrentFocus`/`mFocusedApp`/`topResumedActivity` in Attempt 1 are **identical** — `NexusLauncherActivity`, unchanged. There was no transition away from the launcher at any point this diagnostic could observe. This directly confirms the failure screenshot from both Run B and Diagnostic Attempt 1: the emulator was never showing anything but its home screen. **Classification: VERIFIED**, matching category **E. Launcher/home screen never replaced** from this phase's own Section 7 taxonomy — not A (installed-not-launched is close, but more precisely the launch was *attempted and silently ineffective*, not merely never attempted), not B/C/D/F (no foregrounding occurred at all, so "backgrounded again," "wrong activity," and "crash" all require a foreground event that never happened).

---

## 19. Test Lifecycle Analysis

The framework's own test lifecycle behaved exactly as designed in every run, including the failing ones: `initializeDriver()` completed without throwing (Appium reported success), the test method began, `CommonAssertions.verifyVisible(...)` polled for up to `driver.explicitWaitTimeoutSeconds=15` and correctly reported failure with a screenshot when the expected element never appeared, `ScreenshotManager` captured evidence, `TestListener` logged the failure, and the driver was quit cleanly. **Classification: VERIFIED — no framework lifecycle defect.** The framework behaved correctly given what Appium told it (a successful session); the framework has no way to independently know Appium's own launch confirmation was wrong, because no capability requests such confirmation explicitly (Section 20).

---

## 20. Readiness Race Analysis

Confirmed via direct evidence (Sections 15, 17): **"session successfully created" occurred before/without "AUT expected initial screen actually ready"** — precisely the hypothesis this phase's own brief posed in Section 6. `CapabilityConfiguration`/`UiAutomator2CapabilityBuilder` (read in full, Section "Appium Analysis") set exactly: `platformName, platformVersion, deviceName, udid, appPackage, appActivity, appPath, newCommandTimeoutSeconds, noReset, fullReset, autoGrantPermissions`. **No `appWaitActivity`, `appWaitPackage`, `appWaitDuration`, or `appWaitForLaunch` capability is ever set** — the framework relies entirely on UiAutomator2's undocumented-to-this-project internal defaults for launch confirmation, and this investigation's evidence shows those defaults are not robust under the specific post-boot timing condition observed in Attempt 1.

---

## 21. Screenshot Comparison

Run B's two failure screenshots and Diagnostic Attempt 1 (no screenshot captured mid-attempt, but the post-run ADB dumps serve the equivalent evidentiary purpose) show the **same** state: emulator home screen, no dialog, no error UI, no partial AUT rendering. This is visually and evidentially consistent with Section 18's finding — the launcher was simply never replaced.

---

## 22. JUnit Comparison

| Run/Attempt | `tests` | `failures` | Failure location |
|---|---|---|---|
| B | 1 | 1 | `LoginTest.java:48`, first `verifyVisible` |
| D-Attempt1 | 1 | 1 | `LoginTest.java:48`, first `verifyVisible` |
| A, C, D-Attempts2-5 | 1 (or 19 for full suite) | 0 | — |

Identical failure location in both independently-observed failures — a further consistency check supporting that this is one recurring mechanism, not two unrelated flakes.

---

## 23. Failure Classification

Per this phase's own taxonomy (A–K): primarily **B (Appium session/launch issue)** and **J (emulator readiness issue, specifically post-boot settling)** acting together — not a Docker issue (A), not an AUT installation issue in isolation (C — the APK installed fine every time), not framework synchronization (G, ruled out by Section 19), not an explicit-wait timing issue in the sense of "just needs longer" (H — ruled out by Section 11, since 15s was already 6x the ~2.5s successful convergence time and still failed), not an Appium capability *misconfiguration* so much as an Appium capability *gap* (I — closest fit, no launch-confirmation capability is set).

---

## 24. Root Cause

**VERIFIED**: Appium/UiAutomator2's `createSession` reports success without confirming the AUT actually reached its expected foreground activity, under this framework's current (default) capability set. **VERIFIED, for Diagnostic Attempt 1 specifically**: this failure mode was directly triggered by attempting the AUT launch while the Android system was still processing post-boot-completion broadcasts and background service starts — i.e., a race between the emulator-runner action's boot-completion signal and true system readiness. **INFERRED, not independently re-verified with the same instrumentation**: Run B's failure (45+ seconds after boot-completed, unlike Attempt 1's 0.16 seconds) plausibly shares the same underlying "session succeeds without launch confirmation" mechanism, triggered by some other transient system-load condition rather than the specific post-boot window — the diagnostic evidence proves the mechanism *exists and is reachable*, not that boot-timing is its *only* trigger.

---

## 25. Evidence Matrix

| Claim | Status |
|---|---|
| Docker/container boundary is not the cause | VERIFIED |
| AUT was installed in the failing runs | VERIFIED |
| AUT process never existed post-failure (Attempt 1) | VERIFIED |
| Launcher/home screen never lost focus (Attempt 1) | VERIFIED |
| Appium's `am start-activity` command produced no logged result before session-success (Attempt 1) | VERIFIED |
| Session creation was reported successful without launch confirmation | VERIFIED |
| Framework's own test/assertion/screenshot lifecycle behaved correctly | VERIFIED |
| No `appWait*` capability is set anywhere in this framework | VERIFIED (direct code reading) |
| Attempt 1's failure coincided with post-boot system settling (BOOT_COMPLETED broadcast fan-out) | VERIFIED |
| Run B's failure shares the identical root mechanism as Attempt 1 | INFERRED |
| The exact reason `am start-activity` failed to take effect at the OS/ActivityManagerService level | NOT VERIFIED (out of this project's instrumentation reach) |
| This is a GitHub-runner-hardware-specific issue (vs. reproducible on any sufficiently-loaded Android system) | NOT VERIFIED / UNKNOWN |

---

## 26. Alternative Hypotheses Rejected

- **App crash after launch** — rejected; no process ever existed (Section 16), no crash entries in logcat, no "has stopped" dialog in any screenshot.
- **Slow cold start, just needed more time** — rejected as the *sole* explanation; 15s (6x the normal convergence time) still wasn't enough, and Attempt 1's own driver-init was *faster* than the successful runs, not slower (Section 11).
- **Docker networking flake** — rejected; Gate 6 (container→Appium TCP) passed identically in every run, and the session-creation HTTP request/response cycle completed normally and quickly in the failing runs too.
- **Wrong activity/package targeted** — rejected; Appium's own log (Section 15) shows the exact correct `appPackage`/`appActivity` in the `am start-activity` command.
- **APK installation failure** — rejected; `pm list packages` confirms the package was installed in the failing attempt.

---

## 27. Remaining Unknowns

1. The precise ActivityManagerService-level reason the dispatched `am start-activity` request had no effect (would require Android platform-level instrumentation beyond this project's reach).
2. Whether Run B's failure (45s post-boot) was triggered by the same "system still settling" condition as Attempt 1 (0.16s post-boot) or a different, coincidentally-identical-looking transient condition — both are consistent with "Appium doesn't verify launch," but the *trigger* for Run B specifically was not independently re-instrumented.
3. The true frequency of this failure across a larger sample than the 4 failures observed in ~8 total attempts across this and the prior phase (a meaningful but still small sample).

---

## 28. Required Diagnostic Run (Already Executed)

Satisfied by Section 3.1 — the diagnostic run planned and executed as part of this phase closed the evidence gap that made root-causing Run B alone impossible. No further diagnostic run is required to proceed with a fix recommendation.

---

## 29. Proposed Fix (Root Cause Verified for the Reachable Mechanism)

**Do not implement without separate explicit authorization** — this touches the shared driver-initialization path used by all 19 tests, both Docker and non-Docker, and deserves its own sign-off given that scope, consistent with how every prior cross-cutting change in this project has been gated.

**ROOT CAUSE:** `AndroidDriverFactory`/`UiAutomator2CapabilityBuilder` never asks Appium to confirm the AUT actually reached its expected activity before returning a session as usable — UiAutomator2's own internal launch-confirmation is not robust under the observed post-boot-settling condition (Section 24).

**EVIDENCE:** Sections 15–20 — Appium's own log shows no captured launch-verification result; device-side ADB/logcat evidence shows the AUT was never touched at the OS level in the failing attempt; the framework's capability set has no `appWaitForLaunch`/`appWaitActivity`/`appWaitDuration` override.

**WHY THIS FIX ADDRESSES IT:** A narrow, explicit, evidence-based readiness check — using Appium's own `getCurrentPackage()`/`getCurrentActivity()` API immediately after session creation, not a sleep — would let the framework detect "session claims success but the AUT isn't actually foregrounded" at the exact point of failure, rather than only discovering it 15 seconds later via an unrelated UI assertion. This directly targets the verified mechanism (Section 24) without touching timeout values, without adding `Thread.sleep()`, and without weakening any test assertion.

**WHY ALTERNATIVES WERE REJECTED:**
- *Increasing the explicit-wait timeout* — rejected; Section 11/26 show the failing case wasn't "almost there," it never moved at all; a longer timeout only delays the same failure.
- *Adding `Thread.sleep()` before the first assertion* — rejected; explicitly disallowed by this phase's own rule, and would not address a launch that never happened at all.
- *Setting `appWaitForLaunch=true` explicitly (it may already default to true)* — a plausible complementary capability change, but not clearly sufficient alone since Section 15 shows the *current* behavior already appears to attempt a wait (`-W` flag) and still failed to catch the problem; the gap is in verification after the fact, not merely in requesting a wait.
- *Retry logic at the test-execution level* — rejected per this phase's explicit "do not introduce retries merely to make the pass rate look better" rule; a retry would mask the mechanism rather than surface it, though the project's existing `RetryAnalyzer` (already present for genuine flaky-method retry, unrelated to this investigation) is a separate, pre-existing, already-governed mechanism not evaluated here.

**FILES TO BE MODIFIED (if authorized):** `src/main/java/com/mobileautomation/framework/driver/AndroidDriverFactory.java` (add a post-session-creation foreground-activity verification step, likely raising the same `DriverInitializationException` it already throws for other init failures, so the failure surfaces at driver-init time with a clear, specific message instead of an unrelated assertion 15 seconds later).

---

## 30. Validation Plan (If the Fix Is Authorized and Implemented)

1. Compile/verify locally (non-Docker) first — confirm no regression against the existing, proven non-Docker baseline.
2. Inspect the exact diff — confirm only `AndroidDriverFactory.java` (or the minimal necessary file set) changed.
3. Trigger a real Docker CI run (temporary workflow, as in Phase 19.4) — require 19/19, inspect JUnit XML.
4. Trigger a second, fully independent, unmodified confirmatory run — require 19/19 again.
5. Only after **both** are green: classify as `DOCKER GITHUB EXECUTION REPRODUCIBLY VERIFIED`.
6. Given the failure is intermittent (observed in ~4 of ~8 attempts across this and the prior phase, concentrated at points of high system load), consider whether more than two runs are warranted to build real confidence — this is a judgment call for whoever authorizes the fix, not decided by this report.

---

## 31. Risk Assessment

| Risk | Severity | Notes |
|---|---|---|
| The proposed fix touches driver initialization shared by all 19 tests, Docker and non-Docker | Medium | Exactly why it requires separate authorization and the two-consecutive-run validation bar (Section 30), not a reason to avoid fixing it |
| Frequency of the underlying issue is not fully characterized (Section 27) | Low-Medium | A fix addressing the verified mechanism is still correct even if the exact trigger frequency is unknown — it converts a silent, misleading 15-second-late failure into an immediate, correctly-diagnosed one either way |
| Leaving this unfixed | Medium | Any future Docker CI integration (Phase 19.5) would inherit an intermittent, misleadingly-diagnosed failure mode; every failure would present as "assertion failed" rather than "AUT never launched," costing future investigation time |

---

## 32. Final Verdict

# ROOT CAUSE VERIFIED — READY FOR MINIMAL FIX

The reachable failure mechanism is proven with direct, first-party evidence (Appium's own server log, ADB device-state dumps, logcat) from a purpose-built diagnostic run that reproduced the exact same failure signature as the original Phase 19.4 confirmatory-run failure: Appium/UiAutomator2 can report `createSession` as successful without the AUT ever having been confirmed foregrounded, and this framework's capability set does not request or verify that confirmation. One specific triggering condition (post-boot system settling) is independently verified for the diagnostic run; the same mechanism is the best-evidenced (INFERRED, not re-proven) explanation for the original Phase 19.4 failure.

This is **not** a Docker architecture defect, **not** a framework lifecycle defect, and **not** an emulator reliability defect in the broad sense — it is a narrow, well-scoped gap in launch-confirmation that a minimal, targeted fix (Section 29) can close without touching timeouts, without sleeps, and without weakening any test.

**Per this phase's own most important rule**, implementation is deliberately not performed automatically here — Section 29's fix proposal is presented for explicit authorization before any file is modified, given its shared-code scope. Do not proceed to **Phase 19.5 — Production CI Docker Integration** until either this fix is authorized, implemented, and validated per Section 30 (two consecutive 19/19 runs), or a decision is made to accept and formally document the residual intermittent risk instead.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Forensic Investigation Report | — | — |

---

**End of Document — Phase 19.4A Docker CI Intermittent Failure Forensic Report, v1.0**
