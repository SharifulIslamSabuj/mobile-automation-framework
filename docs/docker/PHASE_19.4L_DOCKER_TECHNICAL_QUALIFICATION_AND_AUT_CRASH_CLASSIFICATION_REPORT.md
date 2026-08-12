---
document_id: PHASE-19.4L
title: Docker Technical Qualification & AUT Crash Classification
version: v1.0
status: Final — Consolidation Report (Documentation Only, No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-10
last_updated: 2026-08-10
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.1A, PHASE-19.1B, PHASE-19.1C, PHASE-19.2, PHASE-19.3, PHASE-19.4, PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D, PHASE-19.4E, PHASE-19.4F, PHASE-19.4G, PHASE-19.4H, PHASE-19.4I, PHASE-19.4J, PHASE-19.4K]
classification: Internal
---

# Phase 19.4L — Docker Technical Qualification & AUT Crash Classification

---

## 1. Executive Summary

Across Phases 19.1A through 19.4K, the Docker Model 3 architecture has been technically proven end-to-end — image build, container startup, non-root Gradle execution, container-to-host Appium connectivity, real Appium session creation, real emulator interaction, AUT installation/launch, and a full 19/19 test-suite pass have all been directly demonstrated with real execution evidence, more than once. Separately, an intermittent CI failure was investigated across ten forensic phases (19.4A–19.4J) and, in Phase 19.4J, was conclusively traced to a genuine, third-party defect inside the Sauce Labs demo AUT itself (a missing Fragment constructor causing an app crash during an Android-initiated Activity relaunch) — confirmed against the AUT's own official source in Phase 19.4K, which also established no official fix currently exists. This phase formally separates these two findings so that the Docker implementation is not mischaracterized as unreliable because of a defect it does not own, while preserving full visibility that the intermittent failure is real, unresolved, and will continue to occur.

---

## 2. Scope

Documentation and evidence-classification only. This phase reviewed existing repository documentation and CI history; it did not re-run any historical experiment, did not modify any source, build, Docker, or CI file, and did not start Phase 19.5. All content below is a synthesis of already-collected evidence from the referenced phase reports.

---

## 3. Evidence Reviewed

- **Phase 19.1A** — Docker/ADB connectivity spike (exploratory).
- **Phase 19.1B** — "ADB CONNECTIVITY PARTIALLY VERIFIED": the Windows + Docker Desktop path (DNS → TCP → ADB server → device discovery → device communication) fully verified with real execution evidence; the GitHub-hosted Linux path left for Phase 19.1C.
- **Phase 19.1C** — "GITHUB LINUX CONNECTIVITY VERIFIED — READY FOR DOCKER ARCHITECTURE DESIGN": both Model 1 (ADB-protocol layer) and Model 3 (full Appium-session layer) proven with real execution evidence on a GitHub-hosted Ubuntu runner.
- **Phase 19.2** — Docker Architecture Specification: Model 3 selected (container = Java 17 + Gradle wrapper + unmodified harness only; emulator/ADB/Appium remain host-managed). Design only, no code produced.
- **Phase 19.3** — "DOCKER PHASE A PARTIALLY VERIFIED — ADDITIONAL WORK REQUIRED": the Model 3 container build/run, host-Appium reachability, and a real UiAutomator2 session from inside the container were all proven, repeatedly, with direct Appium-server-log evidence.
- **Phase 19.4 (GitHub Docker Execution Proof)** — "DOCKER GITHUB EXECUTION VERIFIED — 19/19 BASELINE PRESERVED": full 19/19 test-suite pass achieved on a real GitHub-hosted Ubuntu runner via Docker, confirmed via JUnit XML.
- **Phase 19.4 (Confirmatory Run)** — "DOCKER GITHUB EXECUTION NOT YET REPRODUCIBLY VERIFIED": a subsequent designated confirmatory run failed at the representative-test gate — the first observed instance of the intermittent failure this phase's chain (19.4A–19.4J) went on to investigate.
- **Phase 19.4A** — "ROOT CAUSE VERIFIED — READY FOR MINIMAL FIX": identified and directly verified a real race condition (Appium `createSession` reporting success before the AUT was confirmed foregrounded, around emulator post-boot settling).
- **Phase 19.4B** — Implemented a readiness-check fix (`queryAppState(...) == RUNNING_IN_FOREGROUND`) targeting the 19.4A race.
- **Phase 19.4C** — CI verification of the 19.4B fix: Run 1 passed, but Run 2 failed with the readiness check reporting success while the launcher was actually visible — proving the fix, while valid for the 19.4A mechanism, did not cover every occurrence of the broader "AUT not visible" symptom.
- **Phase 19.4D** — Attempted multi-signal divergence capture; established methodology limitations (external instrumentation could not observe the live decision point in time) without resolving the mechanism.
- **Phase 19.4E** — In-process readiness diagnostics; found a transient, benign `mCurrentFocus=null` gap at the readiness boundary in 3/3 sampled runs, all of which passed — not itself the failure mechanism.
- **Phase 19.4F** — Controlled 5-run CI sample; reproduced the intermittent failure directly (1/5) with a full evidence chain (readiness signals healthy, then a ~15s explicit-wait timeout, launcher visible in the failure screenshot).
- **Phase 19.4G** — Attempted continuous in-process observation of the post-readiness window; found the instrumentation itself caused severe interference (a documented methodology finding, not a root-cause finding) and did not reproduce the failure.
- **Phase 19.4H** — Replaced the interfering design with an independent, host-side ADB observer (negligible interference, 100% logcat capture success) — validated as the correct observation architecture, though it also did not reproduce the target failure in its own sample; incidentally captured a distinct, unrelated login-input anomaly (not reproduced again in Phase 19.4I).
- **Phase 19.4I** — Bounded investigation of the incidental login-input anomaly: not reproduced in 5 runs; incidentally surfaced a second, separately-tracked distinct issue (a "Log Out" drawer-item timing anomaly, `task_9dd1344d`).
- **Phase 19.4J** — Larger 10-run bounded sample using the Phase 19.4H architecture: reproduced the target failure in Run 2 with a complete, three-source-corroborated evidence chain (JUnit failure, launcher screenshot, host-observer process-state transition, and a full logcat Java stack trace) identifying the AUT-internal crash mechanism.
- **Phase 19.4K** — Investigated official remediation for the Phase 19.4J-identified defect against the AUT's own upstream GitHub repository: no newer official version exists, the defect is present in every release since 1.0.0 and in the unreleased `main` branch, and no official issue/PR acknowledges it.

---

## 4. Docker Technical Qualification Criteria

| # | Criterion |
|---|---|
| 1 | Docker image build |
| 2 | Container startup |
| 3 | Correct Java/Gradle runtime |
| 4 | Non-root execution |
| 5 | Container-to-host Appium connectivity |
| 6 | Real Appium session creation |
| 7 | Real emulator interaction |
| 8 | AUT installation and launch |
| 9 | Execution of the existing test harness |
| 10 | Successful complete-suite baseline execution |
| 11 | Preservation of the existing 19-test baseline |

---

## 5. Qualification Results Table

| # | Criterion | Status | Evidence |
|---|---|---|---|
| 1 | Docker image build | **VERIFIED** | Phase 19.3, and every subsequent phase (19.4–19.4K) rebuilt the same unmodified Dockerfile successfully, dozens of times |
| 2 | Container startup | **VERIFIED** | Phase 19.3 Gate 6/7 connectivity checks; repeated successfully in every later phase's CI runs |
| 3 | Correct Java/Gradle runtime | **VERIFIED** | Every CI run's Gradle output shows successful compilation and test execution inside the container (e.g., Phase 19.4J Run 1: `BUILD SUCCESSFUL`, 30.878s) |
| 4 | Non-root execution | **VERIFIED** | The `--user "$(id -u):$(id -g)"` + dedicated `GRADLE_USER_HOME` fix (established during Phase 19.4's own GitHub-runner uid/permission investigation) has been used unmodified in every workflow since, with no recurrence of the original permission failure |
| 5 | Container-to-host Appium connectivity | **VERIFIED** | Phase 19.3's Gate 6 TCP-reachability check, and every later phase's equivalent gate, passed consistently |
| 6 | Real Appium session creation | **VERIFIED** | Phase 19.3's direct Appium-server-log confirmation; corroborated in every subsequent phase's `automation.log` ("Initializing driver" → "TEST START" sequence, present in every run regardless of later test outcome) |
| 7 | Real emulator interaction | **VERIFIED** | Host-side ADB observer evidence (Phase 19.4H onward) directly shows real `dumpsys`/`pidof`/logcat state against the live emulator in every sampled run |
| 8 | AUT installation and launch | **VERIFIED** | The AUT was installed and reached a running process state in 100% of sampled runs across Phases 19.4E–19.4K (including the one run that later crashed — the crash occurred well after a successful launch) |
| 9 | Execution of the existing test harness | **VERIFIED** | The unmodified `LoginTest`/TestNG/Gradle harness executed to completion (pass or fail) in every sampled CI run across this entire investigation — it never failed to *run* |
| 10 | Successful complete-suite baseline execution | **VERIFIED** | Phase 19.4 (GitHub Docker Execution Proof): 19/19 confirmed via JUnit XML on a real GitHub-hosted Ubuntu runner |
| 11 | Preservation of the existing 19-test baseline | **VERIFIED** | No test was removed, skipped, or altered to achieve any passing result at any point in this investigation (confirmed by every phase's own explicit "no test logic changed" verification) |

**No criterion in this table is classified BLOCKED BY EXTERNAL AUT FAILURE** — the AUT crash (Section 9) is a mid-test application failure, not a failure of any Docker/infrastructure capability listed above; every one of these eleven criteria was independently satisfied in runs where the crash did not occur, and criteria 1–9 were satisfied even in the one run where it did (the crash happened after Docker, Appium, and the AUT launch had already all succeeded).

---

## 6. Successful Docker Execution Evidence

- Phase 19.3: repeated, direct Appium-server-log confirmation of container-initiated sessions against the real host-managed emulator.
- Phase 19.4 (GitHub Docker Execution Proof): 19/19 JUnit-confirmed full-suite pass on GitHub-hosted Ubuntu.
- Phases 19.4E–19.4K (collectively): dozens of individual CI executions, each independently confirming criteria 1–9 above via `automation.log`, JUnit XML, and (from 19.4H onward) independent host-side ADB observation — including the Phase 19.4J run that captured the AUT crash, which itself is positive evidence for criteria 1–9 (everything up to and including the AUT launch worked correctly; only the AUT's own subsequent behavior failed).

---

## 7. Historical Failure Context

| Event | Nature | Docker Qualification Relevance |
|---|---|---|
| Phase 19.1A/19.1B early connectivity attempts | Trial-and-error / setup investigation | None — pre-dates the qualified architecture; superseded by 19.1C |
| Phase 19.3's own iterative gate failures (before its final passing state) | Trial-and-error / setup investigation | None — part of normal implementation iteration, not a qualification run |
| Phase 19.4 Confirmatory Run failure | Real CI failure, later diagnosed | Triggered the 19.4A–19.4J investigation; not itself a Docker defect (Section 9) |
| Phase 19.4D/19.4G methodology failures (instrumentation didn't work as designed) | Diagnostic/experimental workflow failure | None — these were temporary diagnostic workflows that were themselves flawed in design, explicitly documented and removed; not CI qualification runs |
| Phase 19.4C Run 2, Phase 19.4F Run 5, Phase 19.4J Run 2 | Genuine intermittent CI failures | Root-caused to the AUT crash (Section 9), not Docker |
| Phase 19.4H/19.4I incidental distinct failures (login-input anomaly, drawer-item anomaly) | Genuine but separately-tracked failures | Not connected to Docker or to the AUT-crash mechanism; tracked independently (`task_d86a8820`, `task_9dd1344d`) |

---

## 8. Why Early Trial-and-Error Runs Are Not Docker Qualification Failures

Phases 19.1A through 19.3 were, by their own explicit scope, iterative engineering work — spikes and partial-verification reports whose own final verdicts ("PARTIALLY VERIFIED — ADDITIONAL WORK REQUIRED") already document that not every attempt inside those phases succeeded on the first try. That is normal, expected implementation iteration, not a qualification failure — qualification is assessed against the *final, verified state* each phase's own report explicitly reached (Sections 4–5), not against every intermediate command run while reaching it. Counting pre-qualification iteration as a "Docker failure" would misrepresent engineering process as instability.

---

## 9. AUT Crash Classification

```
Failure Category:  EXTERNAL_AUT_CRASH
Component:         Third-party Sauce Labs Demo Android App (com.saucelabs.mydemoapp.android, release 2.2.0)
Root Cause:        Fragment restoration failure during an Android-initiated Activity relaunch —
                    ProductCatalogFragment lacks the public no-argument constructor Android's
                    FragmentManager requires for reflective state restoration.
Evidence:          - JUnit failure (Phase 19.4J Run 2, LoginTest.java:82)
                    - failure screenshots showing the Android launcher, not the AUT
                    - AUT process-state transition (RUNNING pid=5229 -> NOT_RUNNING), host-observed
                    - simultaneous foreground/activity transition to the launcher
                      (mCurrentFocus/mFocusedApp/topResumedActivity all in agreement)
                    - logcat FATAL EXCEPTION with full Java stack trace
                    - java.lang.NoSuchMethodException: ...ProductCatalogFragment.<init> []
                    - source-level confirmation against the official saucelabs/my-demo-app-android
                      repository at the exact 2.2.0 tag in use (Phase 19.4K)
                    - confirmed absent from every release since 1.0.0 and from the unreleased
                      main branch — not an isolated build defect
Ownership:         Third-party AUT (Sauce Labs) — not this project's source
Docker Root Cause: NOT DEMONSTRATED
Framework Root Cause: NOT DEMONSTRATED
Qualification Impact: Can cause a CI run to fail even when Docker infrastructure, Appium
                    connectivity, and the test framework are all functioning correctly.
```

This classification does not claim the crash is rare, predictable, or fully characterized in frequency — only its mechanism and ownership are established (Phase 19.4J/19.4K, both directly evidenced). Its actual occurrence rate remains unquantified (Phase 19.4F's single 1-in-5 sample is the only rate-oriented data point in this entire engagement and is not treated as a reliable rate estimate by that phase's own explicit caveat).

---

## 10. Direct Root Cause Evidence

Reproduced verbatim from Phase 19.4J (host-observed logcat, independent of the Appium session):

```
06:14:56.852 E/AndroidRuntime( 5229): FATAL EXCEPTION: main
06:14:56.852 E/AndroidRuntime( 5229): Process: com.saucelabs.mydemoapp.android, PID: 5229
06:14:56.852 E/AndroidRuntime( 5229): java.lang.RuntimeException: Unable to start activity
    ComponentInfo{com.saucelabs.mydemoapp.android/....MainActivity}:
    androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment
    com.saucelabs.mydemoapp.android.view.fragments.ProductCatalogFragment: could not find Fragment constructor
06:14:56.852 E/AndroidRuntime( 5229): Caused by: java.lang.NoSuchMethodException:
    com.saucelabs.mydemoapp.android.view.fragments.ProductCatalogFragment.<init> []
06:14:56.852 E/AndroidRuntime( 5229): 	at android.app.servertransaction.ActivityRelaunchItem.execute(ActivityRelaunchItem.java:76)
06:14:56.885 I/Zygote  (  336): Process 5229 exited due to signal 9 (Killed)
```

Cross-verified against the official `saucelabs/my-demo-app-android` repository at the exact `2.2.0` tag (Phase 19.4K): `ProductCatalogFragment.java` has only a single-argument constructor (`public ProductCatalogFragment(boolean addVisualChanges)`), no public no-argument constructor exists anywhere in the class, and the crash's stack-trace line numbers (`MainActivity.java:105`, `BaseActivity.java:46`) match exactly the `super.onCreate(savedInstanceState)` calls in the official source where Android's `FragmentManager` performs this restoration.

---

## 11. Failure Taxonomy

| Category | Definition | Blocks Docker Qualification? | Invalidates Prior Docker Proof? | Remains Visible as a CI Failure? |
|---|---|---|---|---|
| `DOCKER_INFRASTRUCTURE_FAILURE` | Image build, container startup, or runtime failure | Yes | Yes | Yes |
| `CONTAINER_RUNTIME_FAILURE` | Non-root/permission/Gradle execution failure inside the container | Yes | Yes | Yes |
| `APPIUM_CONNECTIVITY_FAILURE` | Container cannot reach or create a session against host Appium | Yes | Yes | Yes |
| `EMULATOR_FAILURE` | The emulator itself fails to boot or become reachable | Yes | Yes | Yes |
| `FRAMEWORK_FAILURE` | A defect in this project's own driver/Page Object/test code | Yes | Depends on scope | Yes |
| `TEST_ASSERTION_FAILURE` | A genuine, reproducible application-behavior mismatch this project owns | No (unless it reveals a framework defect) | No | Yes |
| `EXTERNAL_AUT_CRASH` | The verified Phase 19.4J/19.4K mechanism — a third-party AUT defect | **No** | **No** | **Yes** |
| `DIAGNOSTIC_/_EXPERIMENTAL_WORKFLOW_FAILURE` | A temporary, self-authored diagnostic workflow's own instrumentation bug (e.g., Phase 19.4G's contention, Phase 19.4H Run 1's `dumpsys` command bug) | No | No | No — these were never production workflow runs |

No historical CI run in this repository's `mobile-automation.yml` (the production workflow) has ever been reclassified out of a genuine failing status by this taxonomy — the taxonomy exists to correctly attribute *why* a run failed, not to convert a failing run into a passing one.

---

## 12. Qualification Boundary

A CI run's failure counts against Docker technical qualification **only** if its root cause falls in `DOCKER_INFRASTRUCTURE_FAILURE`, `CONTAINER_RUNTIME_FAILURE`, `APPIUM_CONNECTIVITY_FAILURE`, `EMULATOR_FAILURE`, or a `FRAMEWORK_FAILURE` that is itself about the driver-initialization/Docker-integration layer. No run in this engagement's history has been attributed to any of these categories once past Phase 19.3's own final verified state — every investigated intermittent failure (Phase 19.4 Confirmatory Run, 19.4C Run 2, 19.4F Run 5, 19.4J Run 2) has been traced either to the 19.4A readiness race (addressed by the 19.4B fix, for that specific mechanism) or the 19.4J/19.4K `EXTERNAL_AUT_CRASH` mechanism — never to Docker itself. `EXTERNAL_AUT_CRASH` and `TEST_ASSERTION_FAILURE` runs remain **failing CI runs** requiring investigation; this boundary affects only which system is *credited or blamed*, not whether the run is reported as red.

---

## 13. Current Status Across All Seven Dimensions

1. **Docker Architecture**: **VERIFIED.** Model 3 formally specified (Phase 19.2) and proven suitable for both Windows Docker Desktop and GitHub-hosted Linux runners (Phases 19.1B/19.1C/19.3/19.4).
2. **Docker Implementation**: **VERIFIED.** `Dockerfile`/`.dockerignore` exist, are unmodified since Phase 19.3/19.4, and have been rebuilt successfully in every subsequent phase without a single build failure.
3. **Docker Functional Execution**: **VERIFIED.** All eleven qualification criteria (Section 5) independently satisfied, repeatedly, including in the one run that later captured the AUT crash.
4. **Full-Suite Baseline Achievement**: **VERIFIED.** 19/19 achieved on real GitHub-hosted Ubuntu via Docker (Phase 19.4, GitHub Docker Execution Proof).
5. **Run-to-Run Qualification Stability**: **NOT FULLY STABLE — EXTERNAL LIMITATION.** Individual CI runs remain intermittently subject to the `EXTERNAL_AUT_CRASH` mechanism (Section 9) at an unquantified rate; this affects run-to-run outcome, not the qualification of the Docker layer itself (Section 12).
6. **AUT Reliability**: **UNSTABLE — EXTERNAL, UNFIXED, THIRD-PARTY LIMITATION.** No official remediation exists (Phase 19.4K); this project does not own the defect's source.
7. **Production CI Integration Readiness**: **READY WITH DOCUMENTED EXTERNAL LIMITATION.** The Docker/framework/test layers are ready for production CI integration; the AUT's own crash risk should be documented as a known, accepted (pending a decision, Section 15) source of intermittent red runs, not an indictment of the integration itself.

---

## 14. Known External Limitation

The AUT (`com.saucelabs.mydemoapp.android` 2.2.0) can crash mid-test due to a missing Fragment constructor, triggered by an Android-system-initiated Activity relaunch whose exact upstream trigger remains unidentified (Phase 19.4J Section 14: not verified). This is a documented, evidenced, currently-unremediated third-party defect, not a defect in this project's Docker, framework, or test code.

---

## 15. Decision Options

**Option A — Accept the third-party AUT limitation.** Keep the official 2.2.0 AUT unchanged, document the known intermittent crash (this report, plus Phase 19.4J/19.4K), proceed with Docker integration on that basis, and continue to let affected CI runs surface as genuine failures requiring case-by-case triage (with this classification available to speed that triage). Framework-level crash diagnostics (Phase 19.4K's "Option E") could be added later, if separately approved, to make future occurrences faster to recognize — not to prevent them.

**Option B — Patch/fork the AUT.** Technically available (Phase 19.4K: the exact one-line fix is known) but requires explicit approval, a source/license review (the upstream repository currently has no license), and creates an ongoing maintenance responsibility for a self-hosted, forked artifact. Not to be done automatically.

**Option C — Replace the AUT.** Would require identifying and vetting an entirely different demo application, re-deriving the full locator repository (`MA-LOC-001`) and every Page Object against it, and is outside the current Docker architecture's scope (which was validated against this specific AUT). Requires explicit approval; the highest-cost option of the three.

No option is selected or implemented by this phase.

---

## 16. Recommended Next Step

Based on evidence, cost, and project goals: **Option A** is the only option available without further approval and without introducing new maintenance burden, license risk, or locator-repository rework — and it is fully consistent with what has actually been proven (the Docker/framework layers work; the AUT does not always). If reducing the *visibility cost* of future occurrences (faster triage, clearer failure messages) is valuable, a future, separately-scoped phase implementing Phase 19.4K's "Option E" (framework-level AUT-crash detection) would be a natural, low-risk follow-up — but that is a new phase requiring its own approval, not an action this report takes.

---

## 17. Explicit Non-Claims

This report does **not** claim: that the AUT is stable; that every historical CI run in this repository passed; that the intermittent crash's frequency is precisely known; that Docker was the cause of any historical failure; that the crash's upstream trigger (what specifically requests the Activity relaunch) is known; that a patched or replacement AUT would definitely resolve the issue without introducing new ones; or that any further action beyond documentation has been taken this phase.

---

## 18. Final Verdict

# DOCKER TECHNICALLY QUALIFIED — AUT CRASH FORMALLY CLASSIFIED AS EXTERNAL LIMITATION

The Docker Model 3 technical layer is qualified against all eleven defined criteria, based on evidence already collected across Phases 19.1B through 19.4K — no further Docker-layer proof is required. The intermittent CI failure this engagement spent ten forensic phases investigating is formally classified as `EXTERNAL_AUT_CRASH`: a verified, source-confirmed, currently-unremediated defect in the third-party Sauce Labs demo application, not in this project's Docker, framework, or test code. This classification does not convert any failing CI run into a passing one and does not claim the underlying instability is resolved — it exists to ensure that future red runs caused by this known mechanism are correctly attributed rather than mistaken for a regression in Docker technical capability. A decision among Options A/B/C (Section 15) remains open and outside this phase's scope.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-10 |
| Document Status | Final — Consolidation Report (Documentation Only, No Implementation) | — | — |

---

**End of Document — Phase 19.4L Docker Technical Qualification & AUT Crash Classification Report, v1.0**
