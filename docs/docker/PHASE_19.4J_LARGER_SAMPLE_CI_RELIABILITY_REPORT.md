---
document_id: PHASE-19.4J
title: Larger-Sample Independent CI Reliability Investigation
version: v1.0
status: Final — Forensic Report (Target Failure Reproduced With Complete Evidence, No Fix Implemented)
author: Project Owner / Repository Maintainer
created_date: 2026-08-10
last_updated: 2026-08-10
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D, PHASE-19.4E, PHASE-19.4F, PHASE-19.4G, PHASE-19.4H, PHASE-19.4I]
classification: Internal
---

# Phase 19.4J — Larger-Sample Independent CI Reliability Investigation

---

## 1. Objective

Perform a larger bounded CI sample (up to 10 designated sequential runs) using Phase 19.4H's already-proven, low-interference, independent host-side ADB observer architecture, to determine whether the original AUT-visibility failure (Appium readiness succeeds, but the AUT is not visibly available and the first relevant UI assertion fails) can be captured with sufficient evidence to identify the actual mechanism — evidence collection only, no fix.

---

## 2. Scope

In scope: a temporary CI workflow reusing Phase 19.4H's observer verbatim, up to 10 sequential executions of the representative `LoginTest`. **No framework source file was modified this phase at all** — confirmed via `git diff` against the established baselines (`AndroidDriverFactory.java` against `e4d8175`, `LoginPage.java` against `f0728ed`), both empty before and after. Out of scope: any fix, Docker architecture changes, Phase 19.5.

---

## 3. Prior Evidence Baseline

Phase 19.4F reproduced the target failure once, with the readiness-boundary signals (`queryAppState`, `mFocusedApp`, `topResumedActivity`, process presence) all appearing healthy at T0, yet the AUT never became visible in the ~15s window that followed. Phase 19.4G's shared-Appium-session instrumentation caused severe interference and captured no useful mid-window evidence. Phase 19.4H validated a host-side, session-independent observer with negligible interference and 100% logcat capture success, but did not reproduce the target failure in its own sample. Phase 19.4I ruled out the "Username is required" text-entry issue as unrelated, low-frequency, and not reproduced.

---

## 4. Target Failure Definition

Per the phase's own explicit definition: Appium session/driver initialization reports success → normal test execution begins → the AUT is expected to be visible → the first relevant UI assertion cannot find the expected AUT UI → evidence indicates the emulator is showing the launcher/home screen or otherwise not visibly presenting the AUT. Explicitly excluded from this classification: the "Username is required" issue, the "Log Out" drawer-item issue (`task_9dd1344d`), and any other unrelated or infrastructure failure — each classified separately (Section 17).

---

## 5. Independent Observer Architecture

Reused Phase 19.4H's architecture unchanged: continuous `adb logcat -v time` (cleared before start) plus a 1-second-interval `adb shell dumpsys window` / `dumpsys activity activities` / `pidof` poller, both running as host-side background processes started before and stopped immediately after the Docker `gradlew test` invocation — never sharing the test's Appium session, never touching `AndroidDriverFactory.java`.

---

## 6. Run Configuration

Identical Docker Model 3 architecture, unmodified `Dockerfile`/`.dockerignore`, identical `docker run` invocation, identical Appium/emulator setup (Appium 3.6.0, UiAutomator2 8.2.2, API 34, `google_apis` x86_64, Pixel profile), identical single-test scope (`LoginTest`), across every run — the only workflow file added was the temporary `.github/workflows/phase-19-4j-larger-sample.yml`.

---

## 7. Sampling Method

Runs triggered strictly sequentially via `gh workflow run`, each confirmed `completed` before the next was triggered (no concurrent execution). Bounded maximum of 10; the phase's stop rule required halting immediately after any run that reproduced the target failure with sufficient evidence.

---

## 8. Interference Validation

Run 1 (healthy): 30.878s — closely matching the established ~30s healthy baseline (Phase 19.4F, Phase 19.4H). Run 2 (target failure): 45.021s — longer, but this is explained entirely by the failure's own mechanics (an unplanned ~15s explicit-wait timeout on the final assertion, on top of normal execution, Section 15), not by observer interference; the crash itself occurred mid-run, and every action logged before the crash (06:14:42–06:14:53) shows normal, uninflated timing identical to Phase 19.4H's validated baseline. **VERIFIED**: no evidence of observer-induced interference in either run.

---

## 9. Run-by-Run Results

| Run | CI Run ID | Result | Test time | Classification |
|---|---|---|---|---|
| 1 | `31359935592` | Pass | 30.878s | **A. HEALTHY PASS** |
| 2 | `31361039098` | Fail | 45.021s | **B. TARGET FAILURE REPRODUCED** |

Sampling stopped after Run 2 per the phase's explicit stop rule (2 of the 10-run budget used).

---

## 10. Healthy Run Evidence

Run 1 completed all test steps normally with no anomaly in any observer sample or logcat line reviewed for consistency with prior phases' healthy-run patterns (not separately re-analyzed in full detail here, since this phase's primary yield is Run 2).

---

## 11. Target Failure Evidence

Run 2 is a complete, directly-observed, multi-layer-corroborated capture of the target failure:

- **JUnit XML**: `failures="1"`, message `verifyVisible [Product Catalog screen (after Navigation Drawer verification)]: expected to be visible`, at `LoginTest.java:82` — the test's final assertion.
- **Failure screenshots** (`assertion_verifyVisible_failure_20260810_061525_mNyVmY.png` and `loginOutcomeVerification_failure_20260810_061527_3ZFQAD.png`): both show the Android home launcher — wallpaper, dock icons, Google search bar — identical in kind to Phase 19.4F's original failure screenshots.
- **Host observer** (`observer-state.log`): `processState` transitions from `RUNNING pid=5229` (last healthy sample, 06:14:55.685) to `NOT_RUNNING` with `topResumedActivity`/`mFocusedApp`/`mCurrentFocus` all showing `com.google.android.apps.nexuslauncher` (first post-crash sample, 06:14:56.929), and remains in that state continuously through the end of the test (06:15:26.216, the last sample captured).
- **Logcat** (`observer-logcat-raw.log`): a complete, unambiguous Java stack trace pinpointing the exact cause (Section 15).

---

## 12. Distinct Failure Evidence

None occurred in this bounded sample. (Both prior distinct failures — "Username is required" and the "Log Out" drawer item — are tracked separately, Phase 19.4I and `task_9dd1344d` respectively, and did not recur here.)

---

## 13. ADB State Analysis

The host observer's `dumpsys`-derived signals are unambiguous and fully self-consistent in Run 2: `mCurrentFocus`, `mFocusedApp`, and `topResumedActivity` **all transition together, simultaneously**, from the AUT's `MainActivity` to the launcher's `NexusLauncherActivity` at the same observed sample (06:14:56.929), and all three remain in agreement (all showing launcher) for the rest of the run. This is a materially different signature from the transient, benign `mCurrentFocus=null`-only divergence Phase 19.4E/F/H observed during healthy launch transitions (where `mFocusedApp`/`topResumedActivity` continued correctly showing the AUT while only `mCurrentFocus` briefly lagged) — in Run 2, all three signals agree the AUT is genuinely gone, with no partial/transient state. **VERIFIED.**

---

## 14. Logcat Analysis

The critical sequence, verbatim from `observer-logcat-raw.log` (host-side, captured independently of the Appium session):

```
06:14:56.852 E/AndroidRuntime( 5229): FATAL EXCEPTION: main
06:14:56.852 E/AndroidRuntime( 5229): Process: com.saucelabs.mydemoapp.android, PID: 5229
06:14:56.852 E/AndroidRuntime( 5229): java.lang.RuntimeException: Unable to start activity
    ComponentInfo{com.saucelabs.mydemoapp.android/....view.activities.MainActivity}:
    androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment
    com.saucelabs.mydemoapp.android.view.fragments.ProductCatalogFragment: could not find Fragment constructor
06:14:56.852 E/AndroidRuntime( 5229): Caused by: androidx.fragment.app.Fragment$InstantiationException: ...
06:14:56.852 E/AndroidRuntime( 5229): 	at com.saucelabs.mydemoapp.android.utils.base.BaseActivity.onCreate(BaseActivity.java:46)
06:14:56.852 E/AndroidRuntime( 5229): 	at com.saucelabs.mydemoapp.android.view.activities.MainActivity.onCreate(MainActivity.java:105)
06:14:56.852 E/AndroidRuntime( 5229): Caused by: java.lang.NoSuchMethodException: com.saucelabs.mydemoapp.android.view.fragments.ProductCatalogFragment.<init> []
06:14:56.852 E/AndroidRuntime( 5229): 	at android.app.ActivityThread.handleRelaunchActivityInner(ActivityThread.java:5946)
06:14:56.852 E/AndroidRuntime( 5229): 	at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:5842)
06:14:56.852 E/AndroidRuntime( 5229): 	at android.app.servertransaction.ActivityRelaunchItem.execute(ActivityRelaunchItem.java:76)
06:14:56.860 W/ActivityTaskManager(  519):   Force finishing activity com.saucelabs.mydemoapp.android/.view.activities.MainActivity
06:14:56.885 I/Zygote  (  336): Process 5229 exited due to signal 9 (Killed)
06:14:56.888 I/ActivityManager(  519): Process com.saucelabs.mydemoapp.android (pid 5229) has died: fg  TOP
```

The `at android.app.servertransaction.ActivityRelaunchItem.execute` / `handleRelaunchActivity` / `handleRelaunchActivityInner` frames prove this was an **Android-system-initiated Activity relaunch** — the framework requesting `MainActivity` destroy and recreate itself within the **same process** (pid 5229 both before and during the crash, not a new pid) — not a fresh cold launch and not an unrelated process kill. During that relaunch's `onCreate()`, the `FragmentManager` attempted to reinstate its previously-saved Fragment state, which included `ProductCatalogFragment`; reflection-based reinstantiation requires a public no-argument constructor, which this class does not have, producing `NoSuchMethodException` → uncaught `RuntimeException` → `FATAL EXCEPTION: main` → the process killed by the system (`signal 9`). **VERIFIED**, directly from the stack trace — not inferred.

**NOT VERIFIED / BLOCKED**: the specific system event that *requested* the relaunch. No explicit `Configuration changed` or `onConfigurationChanged` line was found in the captured logcat window immediately preceding the relaunch; scattered `TimedProcessReaper "Scheduling killing of process to refresh configuration"` lines appear in this window but reference other, unrelated process IDs (4243, 2954, 1807, 4507 — never 5229), so they are not evidenced as the trigger. The upstream cause of the relaunch request itself remains unidentified.

---

## 15. Timeline Analysis

| Time (UTC) | Event | Source |
|---|---|---|
| 06:14:40.340 | TEST START (readiness already succeeded) | automation.log |
| 06:14:42.973 | Assertion passed: Product Catalog (post-launch) | automation.log |
| 06:14:46.869 | Assertion passed: Login screen visible | automation.log |
| 06:14:51.654 | Assertion passed: Product Catalog (post-login) | automation.log |
| 06:14:51.197–55.685 | Observer: AUT `MainActivity` resumed, `processState=RUNNING pid=5229` — healthy, 5 consecutive samples | observer-state.log |
| 06:14:52.976 | "Log Out" displayed: true | automation.log |
| 06:14:53.328 | Assertion passed: "Log Out" drawer item visible | automation.log |
| 06:14:53.329 | Begin checking "Log In" absent (explicit-wait poll, expected to run the full ~15s since the element is genuinely absent) | automation.log |
| **06:14:56.852** | **FATAL EXCEPTION — AUT crashes during an Activity relaunch (`NoSuchMethodException` on `ProductCatalogFragment`)** | observer-logcat-raw.log |
| 06:14:56.885 | Process 5229 killed (signal 9) | observer-logcat-raw.log |
| 06:14:56.929 | Observer: first post-crash sample — `topResumedActivity`/`mFocusedApp`/`mCurrentFocus` = launcher, `processState=NOT_RUNNING` | observer-state.log |
| 06:15:08.997 | "Log In absent: true" — assertion passes (this check cannot detect the crash, since it is itself checking for absence) | automation.log |
| 06:15:09.xxx | Final assertion begins polling for Product Catalog visibility | automation.log (inferred from next line) |
| 06:15:24.046 | Assertion FAILS: Product Catalog not visible — full ~15s poll never found it, because the AUT was not running | automation.log |
| 06:15:25.357 / 06:15:27.069 | Failure screenshots — both show the launcher | automation.log |

The crash occurred **16.5 seconds after readiness succeeded**, after three separate UI assertions had already passed, during a window when the test itself was not interacting with the AUT (only polling for the absence of an element) — the failure is entirely decoupled from the readiness boundary that Phases 19.4A–H focused on.

---

## 16. Signal Comparison

| | Healthy runs (this phase's Run 1, and Phase 19.4H's runs) | Run 2 (target failure) |
|---|---|---|
| `mCurrentFocus` at readiness/launch transitions | Can transiently show `null` for ~1s, self-resolving | N/A — no transient state; jumps directly and permanently to launcher |
| `mFocusedApp`/`topResumedActivity` agreement | Can briefly disagree with each other during launch (Phase 19.4H, Run 3) | Fully agree with each other and with `mCurrentFocus`, always |
| Process state | Stable, running throughout | Running, then abruptly `NOT_RUNNING` at one specific sample, permanently |
| Logcat | Routine IME/AppsFilter/system noise | A complete `FATAL EXCEPTION` Java stack trace |

The signature is qualitatively different and unambiguous: the benign transient divergences documented in Phase 19.4E/F/H (single-signal, self-resolving within ~1–2s) are not what happened in Run 2 — Run 2 shows a hard, permanent, all-signals-in-agreement transition, directly explained by a process crash.

---

## 17. Failure Classification

- Run 1: **A. HEALTHY PASS.**
- Run 2: **B. TARGET FAILURE REPRODUCED** — matches the phase's own definition exactly: readiness succeeded, execution proceeded normally, a later UI assertion could not find the expected AUT UI, and direct evidence (screenshot, host observer, logcat) confirms the launcher was showing and the AUT was not running.
- No run in this phase was classified C (distinct failure) or D (infrastructure failure).

---

## 18. Statistical Limitations

This phase captured 1 target-failure instance in 2 runs before stopping per its own rule — this is not a frequency estimate (the sample is far too small and was deliberately truncated by the stop rule, which biases toward under-sampling the healthy-run population). No claim is made about the overall failure rate; Phase 19.4F's own 1-in-5 figure remains the only rate-oriented data point in this engagement, and this phase does not revise it.

---

## 19. Root Cause Assessment

**VERIFIED** for this specific, directly-observed instance: the target failure in Run 2 was caused by a genuine application-level defect in the AUT itself — `ProductCatalogFragment` lacks a public no-argument constructor required by Android's `FragmentManager` for state restoration during an Activity relaunch, causing an uncaught `NoSuchMethodException` that crashes the app process. This is category **H (another verified mechanism)** from the phase's own candidate list — not any of A–G, since the AUT did receive a visible window originally, did not merely lose focus, and was not affected by a UiAutomator2/Appium observation gap; it was killed by the Android runtime itself following an uncaught exception in the AUT's own code.

**NOT VERIFIED / INFERRED**: whether this exact mechanism (Fragment-reinstantiation crash during a relaunch) also explains every other historically observed instance of "AUT not visible" across this entire engagement (Phase 19.4A's original finding, Phase 19.4C's contradictory finding, Phase 19.4F's Run 5). None of those earlier instances have a comparably complete logcat capture to compare against — Phase 19.4F didn't capture logcat at all, Phase 19.4G's logcat capture failed entirely, and Phase 19.4H's own captured failure (Run 1) was the unrelated login-input issue, not this one. It is plausible that some or all of the earlier instances share this same underlying mechanism (an Android-initiated Activity relaunch hitting this same Fragment bug, potentially even during the initial post-launch window Phase 19.4A/F focused on), but this phase's evidence does not extend that far — it proves the mechanism exists and can occur, not that it is the exclusive or even primary explanation for every prior occurrence.

---

## 20. What Is Verified

- The target AUT-visibility failure is real, reproducible, and now directly observed end-to-end with a complete evidentiary chain (JUnit failure → screenshot → independent host-side window/process state → logcat stack trace), all three evidence sources fully agreeing.
- At least one concrete, verified root-cause mechanism exists: an uncaught `NoSuchMethodException` in `ProductCatalogFragment`'s missing no-arg constructor, triggered during an Android-initiated Activity relaunch, crashing the AUT process.
- This mechanism is entirely internal to the AUT (a third-party demo application, `com.saucelabs.mydemoapp.android`) — not a defect in this project's `AndroidDriverFactory`, Docker configuration, Appium capabilities, or test framework.
- The failure can occur well after driver readiness and after multiple prior UI interactions have already succeeded — it is not confined to the immediate post-readiness window every earlier phase (19.4A–19.4H) focused on.
- The host-side observer architecture (Phase 19.4H's design, reused unchanged) is now doubly validated: it reproduced clean, negligible-interference evidence in a run that also captured the target failure with complete fidelity.

---

## 21. What Is Inferred

- That some or all prior "AUT not visible" occurrences across this engagement share this same Fragment-relaunch-crash mechanism — plausible, given how the app is architected, but not directly evidenced for those specific instances.
- That the relaunch itself may be a routine, periodically-occurring Android/emulator system behavior (rather than something the test framework or Docker environment specifically provokes) — consistent with the relaunch occurring during a period when the test was doing nothing but a passive `isDisplayed()` poll, but the specific trigger was not captured (Section 14).

---

## 22. What Is Not Verified

- The exact system event that requested the Activity relaunch (Section 14) — genuinely unknown from the evidence captured.
- Whether this crash is deterministic under some specific condition (e.g., time elapsed since app launch, a periodic system relaunch policy, emulator-specific behavior) or occurs at random — this phase's 1-instance sample cannot distinguish these.
- Whether the AUT's `ProductCatalogFragment` bug is present in all builds of this AUT version or specific to this one, and whether a newer/older AUT release would exhibit it.

---

## 23. Docker Architecture Impact

**None.** The root cause identified is entirely internal to the AUT application, unrelated to the Docker Model 3 architecture, the container, `Dockerfile`, `.dockerignore`, networking, or the `docker run` invocation — all of which are confirmed unchanged and uninvolved in this failure mechanism. The Docker implementation itself remains validated as viable (per Phase 19.1B/19.1C/19.2/19.3/19.4's own prior findings, not re-litigated here).

---

## 24. Production CI Readiness

**Not ready to declare fully reliable, but for a newly-understood reason.** The intermittent failure this engagement has chased since Phase 19.4A is not (or not solely) a driver-readiness/Docker-timing problem — it is, at least in this directly-observed instance, an AUT-internal crash bug that can strike at any point in a test's execution, independent of driver initialization. Any fix targeting only the readiness boundary (as Phase 19.4B attempted) cannot address a crash occurring 16 seconds later, during normal UI interaction. This reframes the production-readiness question: it is no longer only "is the readiness check sufficient," but "how should the framework/CI respond to a genuine AUT crash mid-test."

---

## 25. Recommended Next Step

Not a fix (out of scope). Two evidence-based next steps: (1) determine whether `com.saucelabs.mydemoapp.android`'s `ProductCatalogFragment` constructor issue is a known, already-reported defect in this public demo app (it is maintained by Sauce Labs, external to this project) — if so, a newer/patched APK release may already resolve it, which would be a configuration change (APK version), not a framework fix; (2) if the crash cannot be avoided at the AUT level, the framework question becomes whether the test framework should detect and explicitly report an AUT process crash as its own distinct, accurately-diagnosed failure category (analogous to the Phase 19.4B readiness check's own diagnostic intent) rather than surfacing it as a generic element-visibility assertion failure — this is a design question for a future phase, not something this phase should decide or implement.

---

## Cleanup Verification

`.github/workflows/phase-19-4j-larger-sample.yml` removed via `git rm`; `.github/workflows/` now contains only `mobile-automation.yml` (**VERIFIED**). `git diff e4d8175` for `AndroidDriverFactory.java`/`Dockerfile`/`.dockerignore`/`mobile-automation.yml` is empty; `git diff f0728ed` for `LoginPage.java` is empty — no framework source file was touched at any point this phase (**VERIFIED**, and expected, since this phase added no instrumentation). Cleanup committed as `238c592` and pushed to `origin/main` (**VERIFIED**). `git status` shows a clean tree except the pre-existing, deliberately-untracked `docs/docker/` directory (**VERIFIED**).

---

## 26. Final Verdict

# TARGET FAILURE REPRODUCED WITH DIAGNOSTIC EVIDENCE — READY FOR ROOT CAUSE ANALYSIS

Run 2 of this phase's bounded sample reproduced the exact target failure — Appium readiness succeeded, normal test execution proceeded through three successful UI assertions, then a later assertion failed with the launcher visible instead of the AUT — and captured a complete, multi-layer-corroborated evidentiary chain identifying a **verified** root-cause mechanism for this instance: a genuine crash inside the AUT application itself (`ProductCatalogFragment` missing a public no-argument constructor required for Android's Activity-relaunch state restoration), independently confirmed via host-side window/process state and a full logcat stack trace, both fully consistent with the JUnit failure and screenshot evidence. This is the first time across the entire Phase 19.4A–19.4J investigation that a target-failure occurrence has been captured with a complete, unambiguous causal chain rather than only a correlational signal pattern. Per this phase's own stop rule, sampling halted immediately after this run; no fix was implemented, and whether this same mechanism explains every prior occurrence across this engagement remains inferred, not verified.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-10 |
| Document Status | Final — Forensic Report (Target Failure Reproduced With Complete Evidence, No Fix Implemented) | — | — |

---

**End of Document — Phase 19.4J Larger-Sample Independent CI Reliability Report, v1.0**
