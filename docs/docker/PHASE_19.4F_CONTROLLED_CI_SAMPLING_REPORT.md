---
document_id: PHASE-19.4F
title: Controlled CI Sampling & Intermittent Failure Capture
version: v1.0
status: Final — Forensic Report (Failure Reproduced, No Fix Implemented)
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D, PHASE-19.4E]
classification: Internal
---

# Phase 19.4F — Controlled CI Sampling & Intermittent Failure Capture

---

## 1. Objective

Collect a controlled sample of independent, sequential CI executions using the same Docker Model 3 architecture and the same Phase 19.4B readiness baseline, to determine whether the intermittent "AUT reported ready but launcher visible" failure (first observed in Phase 19.4C) can be reproduced, and — if reproduced — capture authoritative evidence at the readiness boundary. This phase does not attempt to make CI green and does not implement a fix.

---

## 2. Context and Previous Evidence

- Phase 19.4A **VERIFIED** the underlying race: Appium `createSession` can report success before the AUT is actually foregrounded.
- Phase 19.4B implemented a readiness check (`queryAppState(appPackage) == RUNNING_IN_FOREGROUND`), bounded by the existing 15-second explicit-wait timeout.
- Phase 19.4C **VERIFIED** this check is not sufficient in every case: Run 2 showed the readiness check passing (no `TimeoutException`) while a screenshot captured later showed the launcher, not the AUT.
- Phase 19.4E added in-process instrumentation and ran 3 designated sequential CI runs — all passed, all showed `mCurrentFocus=null` at the resolving poll while `mFocusedApp`/`topResumedActivity`/process state correctly indicated the AUT. No failure was reproduced.
- This phase (19.4F) reuses the same class of instrumentation and extends the sample to 5 designated sequential runs.

---

## 3. Baseline Under Test

`AndroidDriverFactory.java` byte-identical to Phase 19.4B (`e4d8175`) at the start of this phase — confirmed via `git diff e4d8175 -- src/main/java/.../AndroidDriverFactory.java` (empty) before instrumentation was added. Production `Dockerfile`, `.dockerignore`, and `.github/workflows/mobile-automation.yml` unchanged throughout.

---

## 4. Scope

In scope: instrumenting the exact readiness decision point in `AndroidDriverFactory.verifyAutForegroundReadiness()`, running 5 separate sequential CI executions of `LoginTest`, and — per the phase's own stop rule — halting immediately and preserving evidence if any run reproduces the failure (which occurred on Run 5; see Section 12).

Out of scope: any fix, any change to test logic/locators/Page Objects, retries, timeout changes, Dockerfile/`.dockerignore`/production CI/Appium capabilities/emulator config changes, Docker architecture changes, and Phase 19.5.

---

## 5. What Was Intentionally Not Changed

- `AndroidDriverFactory`'s actual readiness condition: still exactly `driver.queryAppState(appPackage) == ApplicationState.RUNNING_IN_FOREGROUND`, unmodified from Phase 19.4B.
- `LoginTest.java`, `ProductsPage.java`, `CommonAssertions.java`, `ElementActions.java`, `WaitUtility` — all read for evidence interpretation, none modified.
- `Dockerfile`, `.dockerignore`, `.github/workflows/mobile-automation.yml` — untouched, confirmed via `git diff e4d8175` returning empty for all three both before and after this phase.
- The 15-second `DEFAULT_EXPLICIT_WAIT_TIMEOUT_SECONDS` (`ConfigurationDefaults.java:40`) — unchanged, used only to interpret timing evidence.

---

## 6. Diagnostic Instrumentation

Reused the same class of in-process instrumentation proven in Phase 19.4E, added temporarily to `AndroidDriverFactory.java`:

- Wraps the existing `WebDriverWait.until(...)` condition (unchanged in substance) with a call to `logAutReadinessDiagnostic(...)`, synchronous with the actual decision.
- Captures via Appium's official `mobile: shell` extension (`--relaxed-security` already enabled in the workflow's Appium server invocation): `dumpsys window` → `mCurrentFocus`/`mFocusedApp`; `dumpsys activity activities` → `topResumedActivity`; `pidof <appPackage>` → process presence/pid.
- Logs only on poll 1, on any signal change, or on resolution (`[AUT-READINESS-DIAG]` prefix) — avoiding high-volume output.
- A `FINAL_TIMEOUT` variant is wired into the existing `catch (TimeoutException e)` block (not exercised in this phase — no run timed out at the readiness-check level itself).
- Unavailable signals report `"NOT AVAILABLE"` rather than being fabricated (not exercised — all shell commands succeeded in all 5 runs).

**VERIFIED**: compiled cleanly (`BUILD SUCCESSFUL in 20s`) with instrumentation present.

---

## 7. Controlled Execution Design

Per explicit instruction, batching multiple attempts inside one emulator boot was not used. Five separate, independently `workflow_dispatch`-triggered CI runs were executed strictly sequentially — each run was confirmed complete (via `gh run view --json status,conclusion`) before the next was triggered. Same commit, same Dockerfile, same Docker execution model, same test/emulator/Appium configuration, same Phase 19.4B readiness implementation, no changes between runs.

---

## 8. Run Configuration

- Temporary workflow: `.github/workflows/phase-19-4f-sampling.yml` (`workflow_dispatch`-only, removed after investigation), structurally identical to Phase 19.4E's proven workflow — image tag `mobile-automation-harness:phase19.4f`, same Gates 1–8 (Gates 9–10 / full 19-test suite out of scope, consistent with this investigation targeting only the driver-initialization readiness moment).
- `docker run`: `--network=host --user "$(id -u):$(id -g)" -v <workspace>:/workspace -e GRADLE_USER_HOME=/tmp/gradle-home ...` — the same form verified working since Phase 19.4.
- Appium server started with `--relaxed-security`.
- Test scope: `LoginTest` only (`loginOutcomeVerification`, TC-004).

---

## 9. Run 1 Result

CI run `31306688761` — **completed success**, `tests="1" failures="0" errors="0"`, `time="30.785"`.

```
[AUT-READINESS-DIAG] poll=1 elapsedMs=1041 queryAppState=RUNNING_IN_FOREGROUND ready=true
appPackage=com.saucelabs.mydemoapp.android
mCurrentFocus=Window{... com.saucelabs.mydemoapp.android/.view.activities.MainActivity}
mFocusedApp=ActivityRecord{... com.saucelabs.mydemoapp.android/.view.activities.MainActivity}
topResumedActivity=ActivityRecord{... com.saucelabs.mydemoapp.android/.view.activities.MainActivity}
processState=RUNNING pid=4902
```

Notably, in this run `mCurrentFocus` was **not** `null` — it directly named the AUT's `MainActivity`, unlike every run observed in Phase 19.4E and unlike Runs 2–5 of this phase (Sections 10–13). **VERIFIED**: `mCurrentFocus` is not always `null` at resolution; the earlier `null` observation (Phase 19.4E) is a common but not universal pattern.

---

## 10. Run 2 Result

CI run `31306870594` — **completed success**, `tests="1" failures="0" errors="0"`, `time="30.228"`.

```
[AUT-READINESS-DIAG] poll=1 elapsedMs=640 queryAppState=RUNNING_IN_FOREGROUND ready=true
mCurrentFocus=null
mFocusedApp=ActivityRecord{... MainActivity} topResumedActivity=ActivityRecord{... MainActivity}
processState=RUNNING pid=4857
```

Matches the Phase 19.4E pattern (`mCurrentFocus=null`, other signals correct). **VERIFIED**.

---

## 11. Run 3 Result

CI run `31313203196` — **completed success**, `tests="1" failures="0" errors="0"`, `time="27.114"`.

```
[AUT-READINESS-DIAG] poll=1 elapsedMs=580 queryAppState=RUNNING_IN_FOREGROUND ready=true
mCurrentFocus=null
mFocusedApp=ActivityRecord{... MainActivity} topResumedActivity=ActivityRecord{... MainActivity}
processState=RUNNING pid=4901
```

Same pattern as Run 2. **VERIFIED**.

---

## 12. Run 4 Result

CI run `31313546526` — **completed success**, `tests="1" failures="0" errors="0"`, `time="31.901"`.

```
[AUT-READINESS-DIAG] poll=1 elapsedMs=912 queryAppState=RUNNING_IN_FOREGROUND ready=true
mCurrentFocus=null
mFocusedApp=ActivityRecord{... MainActivity} topResumedActivity=ActivityRecord{... MainActivity}
processState=RUNNING pid=5121
```

Same pattern. **VERIFIED**.

---

## 13. Run 5 Result

CI run `31313752670` — **completed, but the test FAILED**: `tests="1" failures="1" errors="0"`, `time="16.829"`.

Readiness diagnostic (identical structure/timing band to Runs 2–4, and to all 3 Phase 19.4E runs):

```
[AUT-READINESS-DIAG] poll=1 elapsedMs=729 queryAppState=RUNNING_IN_FOREGROUND ready=true
appPackage=com.saucelabs.mydemoapp.android
mCurrentFocus=null
mFocusedApp=ActivityRecord{5c16d26 u0 com.saucelabs.mydemoapp.android/.view.activities.MainActivity t9}
topResumedActivity=ActivityRecord{5c16d26 u0 com.saucelabs.mydemoapp.android/.view.activities.MainActivity t9}
processState=RUNNING pid=4881
```

Test failure:

```
java.lang.AssertionError: verifyVisible [Product Catalog screen (post-launch)]: expected to be visible
	at com.mobileautomation.framework.assertions.CommonAssertions.evaluate(CommonAssertions.java:83)
	at com.mobileautomation.framework.assertions.CommonAssertions.verifyVisible(CommonAssertions.java:35)
	at com.mobileautomation.framework.tests.LoginTest.loginOutcomeVerification(LoginTest.java:48)
```

This is `LoginTest.java:48` — the test's **very first statement**, before any navigation or interaction: `CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (post-launch)")`.

**VERIFIED** by direct evidence: this is the same intermittent failure category first observed in Phase 19.4C — the readiness check resolved successfully (no `DriverInitializationException`, no `TimeoutException` at the driver-initialization level) while the AUT's expected screen never appeared.

---

## 14. Failure Reproduction, If Any

**Reproduced on Run 5 of 5.** No further runs were executed after Run 5 failed, per the phase's explicit stop rule. No code, test, or configuration change was made in response to the failure — evidence was preserved and analyzed only.

---

## 15. Readiness Signal Evidence

Reconstructed timeline for the failing run (`31313752670`), from `logs/automation.log`, the JUnit XML `<system-out>`, and the failure screenshots:

| Time (UTC) | Event | Source |
|---|---|---|
| 12:40:23.784 | Suite started | automation.log |
| 12:40:24.021 | "Initializing driver for test method" (`createDriver()` begins) | automation.log |
| (unlogged) | `new AndroidDriver(...)` session creation completes; `verifyAutForegroundReadiness` wait begins | inferred from code path |
| (+729ms from wait start) | `queryAppState` resolves `RUNNING_IN_FOREGROUND`; `mFocusedApp`/`topResumedActivity` show AUT `MainActivity`; process running (pid 4881); `mCurrentFocus=null` | `[AUT-READINESS-DIAG]` line |
| 12:40:35.405 | `createDriver()` returns; "TEST START" logged | automation.log |
| 12:40:35.407 | `loginOutcomeVerification` test method begins; `productsPage.isDisplayed()` begins polling (`WaitUtility.waitForVisibility`, bounded by the same 15s `DEFAULT_EXPLICIT_WAIT_TIMEOUT_SECONDS`) | automation.log, `ConfigurationDefaults.java:40` |
| 12:40:50.744 | Assertion fails — `verifyVisible` never observed the Product Catalog screen title element visible, across the full ~15.337s poll window | automation.log |
| 12:40:52.227 | Failure screenshot 1 captured (`assertion_verifyVisible_failure_...png`) | ScreenshotManager |
| 12:40:52.237 | Test marked failed | automation.log |
| 12:40:53.605 | Failure screenshot 2 captured (`loginOutcomeVerification_failure_...png`) | ScreenshotManager |
| 12:40:53.607–54.876 | Driver quit, test/suite context finished | automation.log |

Gap from "Initializing driver" (12:40:24.021) to "TEST START" (12:40:35.405) is 11.384s — most of which is Appium/UiAutomator2 session-creation round-trip; only the final 729ms of that gap is the readiness poll itself (poll 1, the only poll — resolved immediately).

**VERIFIED.**

---

## 16. Failure Evidence

Both failure screenshots — `assertion_verifyVisible_failure_20260809_124052_rzhLxO.png` and `loginOutcomeVerification_failure_20260809_124053_MqyOf2.png` — show the **Android home launcher screen** (wallpaper, dock icons, Google search bar, "Sun, Aug 9" date widget), not the AUT in any state (not the AUT's Product Catalog screen, not a splash/loading screen, not a crash dialog). This is the same class of visual evidence Phase 19.4C originally captured for its Run 2 failure.

Critically, `ElementActions.isDisplayed()` (`ElementActions.java:55-63`) calls `WaitUtility.waitForVisibility(locator)` and returns `false` only on `TimeoutException` — meaning the check that failed was **actively polling for the full ~15-second window**, not a single instantaneous check. The Product Catalog screen title element was never observed visible at any point during that continuous 15-second window, immediately following a readiness check whose `mFocusedApp`/`topResumedActivity`/process signals all indicated the AUT was already resumed and running.

**VERIFIED.**

---

## 17. Cross-Run Comparison

| Run | Result | Poll count | Elapsed to resolution | `mCurrentFocus` | `mFocusedApp`/`topResumedActivity` | Process | Test outcome |
|---|---|---|---|---|---|---|---|
| 1 (`31306688761`) | Pass | 1 | 1041ms | AUT `MainActivity` (non-null) | AUT `MainActivity` | Running | Pass |
| 2 (`31306870594`) | Pass | 1 | 640ms | `null` | AUT `MainActivity` | Running | Pass |
| 3 (`31313203196`) | Pass | 1 | 580ms | `null` | AUT `MainActivity` | Running | Pass |
| 4 (`31313546526`) | Pass | 1 | 912ms | `null` | AUT `MainActivity` | Running | Pass |
| 5 (`31313752670`) | **Fail** | 1 | 729ms | `null` | AUT `MainActivity` | Running | **Fail — launcher visible ~15s later** |

The single most striking fact: **the readiness diagnostic signature captured at the resolving poll in the failing Run 5 is not distinguishable from Runs 2–4**, all of which passed. Every one of the four signals this instrumentation captures (`queryAppState`, `mFocusedApp`, `topResumedActivity`, process state) agreed the AUT was foregrounded and running in Run 5, exactly as in three other runs that went on to pass cleanly. `mCurrentFocus=null` — the one signal this phase and Phase 19.4E flagged as a known, usually-benign divergence — was present in Run 5, but was equally present in Runs 2–4, which did not fail.

**VERIFIED**: the four captured signals, at the single snapshot this instrumentation takes (the resolving poll), cannot distinguish the failing run from the passing runs. **NOT VERIFIED**: what changed between the resolving poll (729ms after session creation) and the failure (~15.7s later) — no signal was captured during that intervening window in this phase's design (the instrumentation only logs at the readiness decision point, not during the subsequent test body).

---

## 18. Failure Frequency Observed

1 failure in 5 designated sequential runs (20%) for this narrow, single-test (`LoginTest` only), single-day sample. This is a **sample statistic**, not a population failure rate — Phase 19.4A already documented (and this phase does not re-litigate) that the true frequency of the underlying race is only partially characterized. **VERIFIED** as an observed sample count; **NOT VERIFIED** as a general failure rate — 5 runs is too small a sample to establish a stable rate, and this phase does not claim otherwise (see Section 22).

---

## 19. What Is Verified

- The intermittent failure Phase 19.4C first observed is reproducible under the exact Phase 19.4B baseline, using the same Docker Model 3 CI architecture, with no code changes — captured directly in Run 5 of this phase.
- At the moment `queryAppState` resolves `RUNNING_IN_FOREGROUND`, the ActivityManager-level signals this instrumentation captures (`mFocusedApp`, `topResumedActivity`, process presence) can **all agree** the AUT is foregrounded and running, in a run that goes on to fail — this same agreement was also present in every passing run in this sample. These four signals, captured only at the single readiness-decision snapshot, do not distinguish the failing case from the passing cases.
- `mCurrentFocus` can be either `null` (4 of 5 runs, including the failing one) or a direct reference to the AUT (1 of 5 runs) at the resolving poll — it is not a reliable discriminator on its own, and per Phase 19.4E's own instruction, no readiness requirement should be built on `mCurrentFocus == AUT package` (that instruction is doubly reinforced here: `mCurrentFocus` was `null`, not launcher, in the one run that failed).
- In the failing run, the AUT's expected screen was never observed during a full, continuously-polling ~15-second window immediately following the readiness check — this is not a brief, sub-second transient; it persisted for the entire explicit-wait budget.
- The failure screenshots show the Android home launcher, not any AUT state.

---

## 20. What Is Inferred

- That the failure's underlying mechanism is a **prolonged version of the `mCurrentFocus`/ActivityManager divergence** documented in Phase 19.4E is **not** supported by this run's own evidence — `mCurrentFocus=null` was equally present in three passing runs, and the ActivityManager-level signals (which Phase 19.4E treated as the more trustworthy pair) were themselves fully consistent with a healthy AUT state in the failing run too. This weakens, rather than strengthens, the Phase 19.4E hypothesis that ActivityManager state is reliable where WindowManager focus is not.
- A plausible (not proven) explanation: the AUT was briefly, genuinely resumed at the 729ms snapshot (matching what all captured signals reported), then something — a crash, an ANR, being killed and replaced by the launcher, or a UiAutomator2/instrumentation-level interruption — removed it from the foreground before any subsequent poll from `productsPage.isDisplayed()`'s own wait loop could observe it, for the entire following ~15 seconds. This is **INFERRED**, not verified, because no signal was captured during that intervening window.
- An equally plausible (not proven) alternative: the single-snapshot ActivityManager signals were already stale or inaccurate at 729ms (a true false-positive at the readiness moment itself, not just a WindowManager-level gap), and the AUT was in fact never visually rendered at all. This is also **INFERRED**, not verified, for the same reason.
- This phase does not have evidence to choose between these two explanations, and per its own explicit instruction ("do not force classification without evidence"), does not force one.

---

## 21. What Remains Unverified

- Which of the two explanations in Section 20 is correct (or whether a third, uncaptured mechanism is responsible).
- Continuous device/window/activity state between the 729ms readiness snapshot and the ~15.7s failure point — this phase's instrumentation only captures state at the readiness decision, not during the subsequent test body, so this window is a genuine evidence gap.
- Android logcat for the AUT package during this window — not captured by this phase's workflow (no `adb logcat` collection step was included).
- The Appium server's own log for this run — written to `${RUNNER_TEMP}/appium-server.log` by the workflow's Gate 3 step, which is **outside** the `${{ github.workspace }}` path tree the `Upload Sampling Evidence` step archives; it was not included in the downloaded artifact for any of the 5 runs. This is an operational gap in this phase's workflow design, not a fabricated absence — flagged honestly here (**NOT AVAILABLE** by workflow design, not attempted-and-failed).
- Whether the 20% (1/5) observed failure rate is representative of the true underlying frequency — it is not, on a sample this small.

---

## 22. Assessment of Phase 19.4B Readiness Fix

**Not validated.** This phase's evidence extends, rather than resolves, Phase 19.4C's standing verdict (fix failed to address the verified root cause in at least one observed instance). Run 5 is a second, independently-captured instance of the same failure category, now with direct confirmation that the specific signals this investigation has treated as trustworthy (`mFocusedApp`, `topResumedActivity`, process presence) — not just `mCurrentFocus` — can also agree with a state that turns out not to reflect what is actually rendered on screen roughly 15 seconds later. The Phase 19.4B fix is not reverted or replaced in this phase, per its own explicit "do not fix the framework" rule — it remains a partial mitigation with a demonstrated, direct-evidence gap.

---

## 23. Engineering Risk Assessment

Based only on the evidence in this phase and the prior related phases:

- **Observed failure rate in this sample**: 1/5 (20%) for a single representative test under this specific CI environment on this date. Given the small sample size, the true rate could plausibly be materially higher or lower — this number should not be quoted as a production SLA figure.
- **Severity**: High per-occurrence — when it happens, the readiness check (the framework's one explicit safeguard against this exact race) does not catch it, and the failure surfaces as a misleading element-visibility assertion rather than a clear driver-initialization diagnostic, exactly the failure mode Phase 19.4B was built to eliminate.
- **Current mitigation coverage**: partial. The Phase 19.4B check does catch the case where the AUT never starts at all (Phase 19.4A's original finding) but has now been directly observed, twice across two independent investigations (Phase 19.4C, this phase), not to catch a case where the AUT is briefly/apparently resumed and then is not visually present for the following ~15 seconds.
- **Risk if left as-is**: intermittent CI failures will continue to occur at some non-trivial, currently-unquantified rate, each requiring manual re-run/triage, and each currently misattributed by the framework's own error message to "AUT did not reach the foreground" only in the fail-fast (`TimeoutException`) case — Run 5's failure did **not** go through that path at all (readiness passed), so it surfaced as a generic `AssertionError` on `LoginTest.java:48` with no framework-level attribution to this known condition.
- **Risk of acting without further evidence**: per this engagement's standing discipline, implementing a fix now (e.g., requiring `mCurrentFocus` to match, or adding a settle delay) would be guessing — Section 20/21 show the current evidence cannot distinguish between at least two structurally different mechanisms, and a fix aimed at the wrong one could pass CI sampling by coincidence while leaving the real mechanism untouched, exactly as the original Phase 19.4B fix did.

---

## 24. Recommended Next Step

Not a fix (out of scope). The evidence gap identified in Sections 20–21 — no signal captured between the readiness snapshot and the failure — is the specific, answerable question a further investigation should target: instrument (a) continuous or higher-frequency `mFocusedApp`/`topResumedActivity`/`mCurrentFocus` sampling for the first several seconds *after* readiness resolves (not just at the resolving poll), and (b) `adb logcat` capture for the AUT package across that same window, then reproduce again. Capturing device state at 2–3 points during the gap, rather than only at the readiness boundary, would directly discriminate between the "brief genuine resume then lost" and "false positive from the start" hypotheses in Section 20.

---

## 25. Cleanup Verification

- `git checkout e4d8175 -- src/main/java/com/mobileautomation/framework/driver/AndroidDriverFactory.java` applied; `git diff e4d8175 -- src/main/java/com/mobileautomation/framework/driver/AndroidDriverFactory.java` is **empty** — byte-identical to the Phase 19.4B baseline. **VERIFIED.**
- `./gradlew compileJava compileTestJava --no-daemon` after revert: `BUILD SUCCESSFUL in 12s`. **VERIFIED.**
- `.github/workflows/phase-19-4f-sampling.yml` removed via `git rm`. **VERIFIED** — `.github/workflows/` now contains only `mobile-automation.yml`.
- `git diff e4d8175 -- Dockerfile .dockerignore .github/workflows/mobile-automation.yml` is **empty** — production Docker/CI configuration untouched throughout this phase. **VERIFIED.**
- Cleanup committed as `8196446` (`chore: revert Phase 19.4F diagnostic instrumentation, remove sampling workflow`) and pushed to `origin/main`. **VERIFIED.**
- Current `git status`: working tree clean except the pre-existing, deliberately-untracked `docs/docker/` reports directory (consistent with every prior phase in this engagement). **VERIFIED.**
- Committed history for this phase: `fb6ed9c` (instrumentation + workflow added) → 5 CI runs executed against that commit → `8196446` (instrumentation + workflow removed). No commit in between altered the Phase 19.4B readiness logic itself. **VERIFIED.**

---

## 26. Final Verdict

# INTERMITTENT FAILURE REPRODUCED — READY FOR EVIDENCE-BASED ROOT CAUSE ANALYSIS

Run 5 of 5 designated, independent, sequential CI executions reproduced the exact failure category first observed in Phase 19.4C: the Phase 19.4B readiness check resolved successfully — with `queryAppState`, `mFocusedApp`, `topResumedActivity`, and AUT process state all agreeing the AUT was foregrounded and running — yet the test's first assertion, polling continuously for the full 15-second explicit-wait budget, never once observed the AUT's expected screen, and both failure screenshots show the Android home launcher. This is authoritative, directly-observed evidence, not an inference. It also sharpens rather than confirms the Phase 19.4E hypothesis: because the same "AUT-confirmed" signal pattern occurred in three passing runs in this same sample, no single captured signal at the readiness boundary — including `mCurrentFocus`, `mFocusedApp`, or `topResumedActivity` — currently distinguishes a run that will fail from one that will pass. The specific mechanism between the readiness snapshot and the failure (~15 seconds later) remains uncaptured and is the concrete target for the next investigation (Section 24). No fix was implemented; the Phase 19.4B readiness implementation is unchanged and remains under investigation, not validated.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Forensic Report (Failure Reproduced, No Fix Implemented) | — | — |

---

**End of Document — Phase 19.4F Controlled CI Sampling & Intermittent Failure Capture Report, v1.0**
