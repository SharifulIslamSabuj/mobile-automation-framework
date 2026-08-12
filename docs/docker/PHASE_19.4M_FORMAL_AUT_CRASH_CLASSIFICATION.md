---
document_id: PHASE-19.4M
title: Formal AUT Crash Classification
version: v1.0
status: Final — Classification Report (Documentation Only, No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4J, PHASE-19.4K, PHASE-19.4L]
classification: Internal
---

# Phase 19.4M — Formal AUT Crash Classification

---

## 1. Objective

Formally define the project-level failure category `EXTERNAL_AUT_CRASH` — its exact meaning, the minimum evidence required before any CI failure may be classified under it, a decision matrix separating it from every other failure category, and a reporting specification for future occurrences — so that this specific, already-verified defect (Phase 19.4J/19.4K) can be correctly attributed going forward without the category being used to excuse failures that have not actually met its evidence bar.

---

## 2. Background

Phase 19.4L formally qualified the Docker technical layer against eleven independent criteria, all satisfied, and separately classified the intermittent CI failure investigated across Phases 19.4A–19.4J as caused by a third-party AUT defect. Phase 19.4J captured that defect's mechanism directly: `com.saucelabs.mydemoapp.android` 2.2.0's `ProductCatalogFragment` lacks the public no-argument constructor Android's `FragmentManager` requires to reflectively restore it during an Activity relaunch, producing an uncaught `NoSuchMethodException`, a `FATAL EXCEPTION`, and termination of the AUT process — after which the Android launcher becomes the foreground app and the test's next assertion fails because the AUT is genuinely no longer running. Phase 19.4K confirmed against the AUT's own official source that `2.2.0` is the latest release, the defect predates every release back to `1.0.0`, remains in the unreleased `main` branch, and has no official acknowledgment or fix. This phase formalizes that finding into a reusable classification, distinct from ad hoc labeling of any future failure.

---

## 3. Scope

Documentation, classification, and evidence-boundary definition only. No Docker, CI, framework, test, or AUT file was modified to produce this report. No new CI run was executed. No historical evidence was re-collected — this phase organizes and formalizes what Phases 19.4J/19.4K/19.4L already established.

---

## 4. Evidence Baseline

The entirety of this classification rests on evidence already collected and reported:

- **Phase 19.4J** — the only run in this engagement with a complete, three-source-corroborated capture of the crash mechanism: JUnit failure (`LoginTest.java:82`), failure screenshots showing the Android launcher (not the AUT), an independent host-side ADB observer recording `processState` transitioning from `RUNNING pid=5229` to `NOT_RUNNING` at the same instant `mCurrentFocus`/`mFocusedApp`/`topResumedActivity` all transitioned together to the launcher, and a full logcat Java stack trace (`FATAL EXCEPTION`, `NoSuchMethodException: ...ProductCatalogFragment.<init> []`, `ActivityRelaunchItem.execute`, `Process 5229 exited due to signal 9`).
- **Phase 19.4K** — source-level confirmation against the official `saucelabs/my-demo-app-android` repository at the exact `2.2.0` tag in use: the crash trace's line numbers match precisely to `super.onCreate(savedInstanceState)` in `MainActivity.java:105` and `BaseActivity.java:46`; no public no-argument constructor exists in `ProductCatalogFragment.java`; the defect is present in every tagged release since `1.0.0` and in the unreleased `main` branch; no official issue or PR acknowledges it.
- **Phase 19.4L** — formal separation of Docker technical qualification (independently verified across 11 criteria) from this AUT-level failure.

No other run in this engagement (including Phase 19.4F Run 5, Phase 19.4C Run 2, or the original Phase 19.4 Confirmatory Run failure) has this same level of direct evidence — those runs showed the same *symptom* (launcher visible, AUT not running) but were not independently confirmed to share this *exact* crash mechanism, since none of them captured comparable logcat evidence. This distinction is preserved throughout this report (Section 7, Section 10) and must not be collapsed.

---

## 5. Formal Classification

```
Classification Name:  EXTERNAL_AUT_CRASH
Status:                Formally defined, this phase
Verified Instances:    1 (Phase 19.4J, Run 2, CI run 31361039098)
Symptom-Only Instances (mechanism not independently confirmed):
                        Phase 19.4 Confirmatory Run, Phase 19.4C Run 2, Phase 19.4F Run 5
Component:             com.saucelabs.mydemoapp.android, release 2.2.0
Ownership:             Third-party (Sauce Labs) — outside this project's source control
```

---

## 6. Definition of EXTERNAL_AUT_CRASH

**What it means**: a CI test failure caused by the AUT process itself terminating abnormally — via an uncaught exception, a system-initiated kill following such an exception, or an equivalent directly-observed crash — during test execution, where the termination is independently confirmed (not inferred from the test's own symptom alone) and traced to code inside the AUT, not this project's framework, Docker configuration, or Appium/emulator integration.

**What component ownership means**: the defect exists in source this project does not write, does not control, and cannot fix without forking or patching a third-party repository (Phase 19.4K). Classifying a failure as `EXTERNAL_AUT_CRASH` is a statement about *where the defect lives*, not a statement that the failure is acceptable, expected, or should be ignored.

**What it does not mean**: it does not mean the CI run passed. It does not mean Docker, Appium, or the framework are exonerated by default — that exoneration must be separately evidenced (Section 7), not assumed. It does not mean every future "launcher visible" failure is automatically this same issue. It does not mean the crash's frequency, trigger, or precise conditions are fully understood (Phase 19.4J itself left the upstream trigger of the Activity relaunch as **NOT VERIFIED**).

**When it may be used**: only when the evidence standard in Section 7 is met for that specific CI run.

**When it must NOT be used**: it must **not** be applied merely because — a test failed; the AUT was not visible; the emulator showed the launcher; an explicit-wait assertion timed out; the failure "looks similar" to a past occurrence; or classifying it this way would be convenient. Each of these, alone, is consistent with several other categories (Section 8) and is not, by itself, evidence of a crash.

---

## 7. Evidence Requirements

**A. Required evidence** (all of the following must be present for a `VERIFIED` classification):

1. Android logcat containing a `FATAL EXCEPTION` (or equivalent unambiguous crash signal, e.g., an ANR trace) for the AUT's own process, with a timestamp falling within the failing test's execution window.
2. Confirmation that the AUT process disappeared — either a process-state transition captured independently (e.g., a host-side observer showing `RUNNING` → `NOT_RUNNING`/process death) or an explicit `ActivityManager`/`Zygote` "process ... has died" / "exited due to signal" log line for the correct pid.

**B. Strong supporting evidence** (not sufficient alone, but corroborates A and raises confidence):

- A failure screenshot showing the Android launcher rather than the AUT.
- A simultaneous Activity/window-focus transition to the launcher (`mCurrentFocus`/`mFocusedApp`/`topResumedActivity` all in agreement, not merely one signal).
- JUnit failure timing consistent with the crash timestamp (the assertion failing shortly after, not long before, the observed crash).
- Appium server logs showing no anomaly at the driver/session layer during the same window (supports that the failure is not session-related).

**C. Insufficient evidence** (must never, alone or in combination with each other, justify `EXTERNAL_AUT_CRASH`):

- A launcher screenshot with no logcat crash evidence.
- An assertion timeout with no process-state or logcat evidence.
- A single ambiguous or ambiguous-window `mCurrentFocus=null` reading without `mFocusedApp`/`topResumedActivity` also confirming a state change (Phase 19.4E/19.4F/19.4H established this specific signal is often a transient, benign artifact of normal launch sequencing, not a crash indicator).
- Similarity to a previously-classified `EXTERNAL_AUT_CRASH` run without independently re-confirming Required evidence for the run in question.
- Absence of contrary evidence ("nothing else explains it" is not itself evidence).

---

## 8. Failure Classification Matrix

| Observed Condition | Classification | Rationale |
|---|---|---|
| Docker image fails to build | `DOCKER_INFRASTRUCTURE_FAILURE` | Failure occurs before any AUT/Appium involvement |
| Container fails to start | `CONTAINER_RUNTIME_FAILURE` | Infrastructure layer, pre-dates test execution |
| Java/Gradle execution fails inside the container (e.g., compile error, permission error) | `CONTAINER_RUNTIME_FAILURE` or `FRAMEWORK_FAILURE` (if the cause is project source) | Distinguish by whether the cause is the container environment or this project's own code |
| Appium unreachable from the container | `APPIUM_CONNECTIVITY_FAILURE` | Infrastructure/networking layer |
| ADB/emulator unavailable | `EMULATOR_FAILURE` | Infrastructure layer, outside AUT scope |
| Appium session creation fails/errors | `APPIUM_CONNECTIVITY_FAILURE` or `FRAMEWORK_FAILURE` (if caused by malformed capabilities) | Distinguish by the actual error source |
| AUT fails to install | `EMULATOR_FAILURE` or `FRAMEWORK_FAILURE` (if the APK reference/path is wrong) — **not** `EXTERNAL_AUT_CRASH` unless install failure is itself traced to a defect in the APK's own manifest/build | Installation failure is a different failure mode than a runtime crash after a successful install |
| AUT fails to launch (never reaches a running process at all) | `FRAMEWORK_FAILURE` or `EMULATOR_FAILURE`, pending investigation — **not automatically** `EXTERNAL_AUT_CRASH` | A launch failure with no crash evidence is unexplained, not classified |
| AUT process crash with verified logcat `FATAL EXCEPTION` + confirmed process termination | **`EXTERNAL_AUT_CRASH`** | Meets the Section 7 evidence standard |
| Launcher screenshot only, no logcat evidence, no process-state evidence | **`UNKNOWN` / requires further investigation — must NOT be classified `EXTERNAL_AUT_CRASH`** | Insufficient evidence (Section 7C) |
| Assertion failure while the AUT process is confirmed still running | `TEST_ASSERTION_FAILURE` or `FRAMEWORK_FAILURE` | The AUT did not crash; the failure is a genuine behavioral/locator/timing mismatch this project may own |
| Unknown infrastructure failure with no clear signal | `UNKNOWN` — pending investigation | Do not force a category when evidence does not support one |

---

## 9. Known AUT Limitation

- **AUT**: Sauce Labs My Demo App, Android, release `2.2.0` (`com.saucelabs.mydemoapp.android`).
- **Ownership**: third-party — official public repository `saucelabs/my-demo-app-android`, no license file present (Phase 19.4K).
- **Known crash mechanism**: `ProductCatalogFragment` lacks a public no-argument constructor; an Android-initiated Activity relaunch attempts to reflectively restore it via `FragmentManager`, raising `NoSuchMethodException`, crashing the app (Phase 19.4J).
- **Official remediation status**: none. `2.2.0` is the latest release; the defect is present in every release since `1.0.0` and in the unreleased `main` branch; no official issue or PR acknowledges it (Phase 19.4K).
- **Reproducibility status**: confirmed reproducible at least once with full evidence (Phase 19.4J, 1 occurrence in a 10-run bounded sample before the sample was halted per its own stop rule). The precise frequency is **NOT VERIFIED** — the single Phase 19.4F 1-in-5 figure predates the mechanism-level confirmation and is not treated as a reliable rate estimate.
- **Impact on CI stability**: this defect can cause an otherwise-correct CI run to fail intermittently, at an unquantified rate, independent of anything this project controls.
- **Impact on Docker qualification**: **none.** Per Phase 19.4L, all eleven Docker qualification criteria were independently satisfied, including in the very run that captured this crash (Docker, Appium, and the AUT launch had already all succeeded before the crash occurred mid-test).

**The AUT limitation does NOT invalidate Docker Technical Qualification. It DOES limit run-to-run end-to-end qualification stability** — these are two separate, independently-evidenced facts, and this report preserves that distinction exactly as stated in Phase 19.4L.

---

## 10. Classification Confidence Model

Four labels only, applied per-run, never assumed:

- **VERIFIED** — direct crash evidence exists for that specific run: a logcat `FATAL EXCEPTION`/crash trace for the AUT process, plus confirmed process termination (Section 7A), within the failing test's execution window. Only one run in this engagement currently meets this bar: Phase 19.4J, Run 2.
- **INFERRED** — strong supporting evidence (Section 7B) is present and consistent with the known mechanism, but Required evidence (Section 7A) is incomplete for that run — e.g., a launcher screenshot and a matching process-state transition exist, but logcat was not captured or was inconclusive. An `INFERRED` classification must always be reported as `INFERRED`, never silently upgraded to `VERIFIED` in any dashboard, summary, or downstream report.
- **NOT VERIFIED** — evidence available for that run actively contradicts the known crash mechanism (e.g., the AUT process is confirmed still running at the time of failure) — the failure is real but is not this issue.
- **UNKNOWN** — insufficient evidence was captured to evaluate the run against this mechanism at all (e.g., no logcat, no screenshot, no process-state data available). `UNKNOWN` is not a synonym for `EXTERNAL_AUT_CRASH` and must not be treated as one.

Historical instances under this model: Phase 19.4J Run 2 = **VERIFIED**. Phase 19.4 Confirmatory Run, Phase 19.4C Run 2, Phase 19.4F Run 5 = **INFERRED at best** (symptom matches — launcher visible, readiness/timing signals otherwise healthy — but none of these runs captured logcat or independent process-state evidence at the time, so Required evidence per Section 7A is not available for them retroactively; this report does not retroactively upgrade them to `VERIFIED`).

---

## 11. Reporting Requirements

For any future CI failure suspected of matching this issue, the following must be recorded **before** a classification decision is made:

1. Workflow name and CI run identifier (e.g., GitHub Actions run ID).
2. The specific failing test (class and method).
3. Failure timestamp (from the JUnit result or test log).
4. The relevant JUnit failure message and stack trace.
5. The failure screenshot(s), if captured.
6. AUT process state at/around the failure time, if available (running/not-running, pid).
7. Android crash evidence, if available (logcat `FATAL EXCEPTION` or equivalent).
8. Full or filtered logcat covering the failure window, if available.
9. The resulting classification decision (one of the categories in Section 8).
10. The confidence level applied (Section 10) and the specific evidence items that justified it.

This is a reporting specification only — it does not require any change to the production workflow, framework, or test code in this phase. Implementing automated capture of items 6–8 is explicitly out of scope here (a candidate for a future framework-diagnostics phase, not decided by this report).

---

## 12. Impact on Docker Qualification

- **A. Docker technical qualification** — independently established (Phase 19.4L), unaffected by AUT reliability.
- **B. Full-suite execution** — has been independently demonstrated (19/19, Phase 19.4 GitHub Docker Execution Proof) under conditions where the AUT crash did not occur.
- **C. AUT reliability** — separately assessed as unstable/external-limitation (Section 9); not a property of Docker.
- **D. Run-to-run CI stability** — the dimension the AUT crash actually affects: an individual CI run can fail because of C even when A and B are both sound.

**Docker may be technically qualified even if the third-party AUT is not perfectly reliable.** A third-party AUT crash must not be automatically counted as a Docker architecture failure. However, a crash may still prevent a given CI run from being fully green — that run remains a genuine failure requiring the reporting discipline in Section 11, not a result to be hidden or reclassified as a pass.

---

## 13. Limitations

- The evidence standard in Section 7 is calibrated to what this engagement has actually demonstrated it can capture (Phase 19.4H/19.4J's host-side observer plus logcat). It does not claim this is the only possible standard, only that it is the one this project can currently meet and verify against.
- The classification model in Section 10 depends on evidence actually being captured at failure time; the production `mobile-automation.yml` workflow does not currently capture host-side observer data or full logcat (that capability exists only in the temporary diagnostic workflows used in Phases 19.4H–19.4J, all of which were removed after use). Until/unless that capability is added to production CI (a decision this phase does not make), most future failures will default to `UNKNOWN` or `INFERRED` under this model, not `VERIFIED` — this is a known, honest consequence of the current evidence-capture gap, not a flaw in the classification itself.
- The exact upstream trigger of the Activity relaunch remains unidentified (Phase 19.4J); this classification does not depend on knowing it, but a future fix attempt would.

---

## 14. Explicit Non-Decisions

This phase does **not**:

- Patch the AUT.
- Fork the AUT.
- Replace the AUT.
- Modify Docker (`Dockerfile`, `.dockerignore`, or the container invocation).
- Modify production CI (`mobile-automation.yml`).
- Modify the framework (`AndroidDriverFactory.java`, Page Objects, or any other source file).
- Add crash diagnostics or any other instrumentation, temporary or permanent.
- Proceed to Phase 19.5.

Those decisions belong to later, separately-scoped and separately-approved phases.

---

## 15. Final Verdict

# FORMAL AUT CRASH CLASSIFICATION COMPLETE — READY FOR PHASE 19.4N FRAMEWORK-LEVEL AUT CRASH DIAGNOSTICS DESIGN

`EXTERNAL_AUT_CRASH` is now formally defined, with an explicit evidence standard (Section 7), a failure-classification decision matrix (Section 8), a four-level confidence model that forbids silently upgrading `INFERRED` to `VERIFIED` (Section 10), and a reporting specification for future occurrences (Section 11). Exactly one run in this engagement's history currently meets the `VERIFIED` bar (Phase 19.4J, Run 2); three earlier symptom-matching runs are retroactively classified no higher than `INFERRED`, preserving rather than inflating the evidentiary record. Docker technical qualification (Phase 19.4L) remains independently sound and unaffected by this classification. No code, Docker, CI, or AUT change was made this phase.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Document Status | Final — Classification Report (Documentation Only, No Implementation) | — | — |

---

**End of Document — Phase 19.4M Formal AUT Crash Classification Report, v1.0**
