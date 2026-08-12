---
document_id: PHASE-19.4O
title: Docker Qualification Boundary Definition
version: v1.0
status: Final — Boundary Definition Report (Documentation Only, No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4L, PHASE-19.4M, PHASE-19.4N, PHASE-19.4B-R, PHASE-19.4J, PHASE-19.4K]
classification: Internal
---

# Phase 19.4O — Docker Qualification Boundary Definition

---

## 1. Objective

Formally define when this project's Docker-based execution is considered successfully qualified, and precisely which failures fall outside that qualification boundary — consolidating, not re-investigating, the evidence already established in Phases 19.4L, 19.4M, 19.4N, and 19.4B-R.

---

## 2. Scope

Documentation and decision-definition only. No Java source, test code, Page Object, `AndroidDriverFactory`, `Dockerfile`, `.dockerignore`, Gradle configuration, Appium configuration, capability, production workflow, or AUT file was modified. No new CI run was executed. No forensic investigation was reopened.

---

## 3. Completed Evidence Baseline

- **Phase 19.4L**: `DOCKER TECHNICALLY QUALIFIED` — all 11 defined criteria (build, startup, runtime, non-root execution, connectivity, session creation, emulator interaction, AUT install/launch, harness execution, 19/19 baseline, baseline preservation) independently VERIFIED.
- **Phase 19.4M**: `EXTERNAL_AUT_CRASH` formally defined with an explicit evidence standard (logcat `FATAL EXCEPTION` + confirmed process termination, both required for `VERIFIED`); exactly one run (Phase 19.4J, Run 2) meets that bar; three earlier symptom-matching runs capped at `INFERRED`.
- **Phase 19.4N**: a hybrid diagnostics architecture (permanent host-side observer + minimal `TestListener` extension) designed, not implemented; one material gap (post-hoc-only logcat sufficiency) left explicitly `UNKNOWN`.
- **Phase 19.4B-R**: `READINESS FIX RETAINED — LIMITED T0 SCOPE` — the Phase 19.4B check is confirmed correct and sufficient for its own narrow problem (session-creation-time readiness), and confirmed structurally incapable of addressing a later mid-test crash.

This phase synthesizes these four verdicts into one boundary definition; it does not add new evidence.

---

## 4. Qualification Boundary

Docker technical qualification and end-to-end run reliability are **two different questions with two different, independently-evidenced answers**. The boundary this phase defines exists specifically to prevent either question's answer from being used to silently overwrite the other: a technically sound Docker layer must not be called "unreliable" because of a defect it does not own, and a genuine AUT defect must not be hidden behind a "Docker is qualified" claim.

---

## 5. Qualification Dimensions

| Dimension | What is being qualified | Required evidence | PASS | FAIL | Outside its ownership |
|---|---|---|---|---|---|
| **A. Docker Architecture** | That Model 3 (container = Java 17 + Gradle wrapper only; emulator/ADB/Appium host-managed) is a sound design for this project's needs | Phase 19.1B/19.1C/19.2 direct connectivity proofs on both Windows and GitHub-hosted Linux | Both platforms independently proven reachable at the required protocol layers | Either platform cannot establish the required connectivity | Anything about a specific AUT's behavior once a session exists |
| **B. Docker Implementation** | That the actual `Dockerfile`/`.dockerignore` correctly realize the architecture | Phase 19.3/19.4: repeated successful builds, unmodified since | Image builds successfully, contains only the specified minimal toolchain | Build failure, or drift from the specified minimal-container design | Runtime behavior once the container is running |
| **C. Docker Runtime Environment** | That the container executes correctly once started (Java/Gradle runtime, non-root execution) | Phase 19.3/19.4 and every subsequent phase's successful `BUILD SUCCESSFUL` Gradle output, `--user "$(id -u):$(id -g)"` fix | Container starts, Gradle compiles/executes without permission or runtime errors | Container fails to start, or a permission/runtime error recurs | Anything about network reachability or session outcomes |
| **D. Docker Networking** | That the container can reach host-managed Appium (`--network=host`, `127.0.0.1`) | Phase 19.3 Gate 6 and every later phase's equivalent gate, passed consistently | TCP reachability confirmed | Connection refused/unreachable | Whether a session, once reachable, succeeds |
| **E. Framework Execution** | That this project's own driver/Page Object/test code runs as intended inside the container | Every sampled CI run's `automation.log`, showing the framework's own logic executing to completion (pass or fail) | The harness runs to completion, produces a JUnit result | The harness itself fails to execute (compile error, uncaught framework exception unrelated to the AUT) | AUT-internal behavior |
| **F. Appium Session Lifecycle** | That a real Appium/UiAutomator2 session can be created and torn down from the container | Phase 19.3's direct Appium-server-log confirmation, corroborated in every later phase's `automation.log` | Session created, driver returned, session quit cleanly | Session creation errors or hangs | AUT process behavior after the session exists |
| **G. Emulator Availability** | That the host-managed emulator boots and is reachable | Every phase's Gate 1-2 `adb devices -l` check, passed consistently | Emulator listed as `device`, reachable | Emulator fails to boot or is not listed | AUT-specific install/launch outcomes |
| **H. AUT Installation and Launch** | That the AUT APK installs and reaches a running process at least once per test | Host-side observer process-state evidence (Phase 19.4H onward): AUT confirmed `RUNNING` in 100% of sampled runs, including the one that later crashed | Process reaches `RUNNING` state | APK fails to install, or process never starts | Whether the process *remains* running for the rest of the test |
| **I. Test Harness Execution** | That the unmodified TestNG/Gradle harness runs the intended test(s) | Every sampled run's JUnit XML existing with a real result (pass or fail), never a harness crash | JUnit result produced | Harness itself crashes before producing a result | The actual pass/fail content of that result |
| **J. Test Result Reliability** | Whether a given CI run's pass/fail outcome is trustworthy and reproducible run-to-run | Phase 19.4F/19.4J/19.4M: intermittent failures observed, root-caused, formally classified | N/A — this dimension is explicitly **not** a pass/fail gate; it is the dimension the AUT crash actually affects (Section 9) | — | Docker/framework correctness, which is separately evidenced above |
| **K. Third-Party AUT Reliability** | Whether the AUT itself behaves correctly across its own execution lifetime | Phase 19.4J (crash mechanism, VERIFIED for one instance) + Phase 19.4K (no official fix exists) | Not currently assessable as "PASS" — the defect is confirmed present and unfixed | Confirmed: `ProductCatalogFragment` missing constructor causes a crash under Activity relaunch | Everything above — this dimension's outcome does not retroactively fail A–I |

---

## 6. Failure Ownership Model

| Category | Minimum evidence required | Notes |
|---|---|---|
| `DOCKER_FAILURE` | Image build failure, or container fails to start | Dimension B/C |
| `CONTAINER_RUNTIME_FAILURE` | Gradle/Java execution error inside a started container (e.g., permission error, compile error attributable to the container environment) | Dimension C |
| `NETWORK_CONNECTIVITY_FAILURE` | Container cannot reach host Appium at the TCP level | Dimension D |
| `APPIUM_FAILURE` | Appium server fails to start, or session creation returns an Appium-level error | Dimension F |
| `ADB_OR_EMULATOR_FAILURE` | Emulator fails to boot, or `adb devices` does not list it as `device` | Dimension G |
| `FRAMEWORK_FAILURE` | The project's own code (driver factory, Page Object, utility) throws an unexpected exception unrelated to AUT or Appium behavior, or produces an incorrect result due to a defect in this project's logic | Dimension E; requires the exception/defect to be traceable to this project's own source |
| `TEST_FAILURE` | A genuine, reproducible assertion mismatch while the AUT is confirmed still running and healthy | Requires positive confirmation the AUT was running at failure time (per Phase 19.4M) |
| `AUT_FAILURE` | The AUT behaves incorrectly (wrong content, broken navigation) without a confirmed crash | Broader than `EXTERNAL_AUT_CRASH`; does not require logcat crash evidence, but does require the AUT process to be confirmed still running |
| `EXTERNAL_AUT_CRASH` | Per Phase 19.4M exactly: logcat `FATAL EXCEPTION` attributable to the AUT process **and** confirmed process termination within the failure window — both required | The narrowest, highest-evidence-bar category; must not be assigned without both required items |
| `CI_INFRASTRUCTURE_FAILURE` | GitHub Actions runner-level failure (e.g., runner provisioning failure, action timeout unrelated to any of the above) | Distinct from Docker/Appium/emulator failures — this is the hosting platform itself |
| `UNKNOWN` | Insufficient evidence to place the failure in any category above | The mandatory default when evidence is incomplete — never silently defaulted to `EXTERNAL_AUT_CRASH` or any other specific category |

No failure may be classified based only on a screenshot or a timeout alone (Phase 19.4M, reaffirmed here).

---

## 7. Failure Classification Evidence Requirements

Restated from Phase 19.4M, applied to this boundary: a launcher screenshot alone is `UNKNOWN` (or, if readiness/process signals are otherwise healthy and only the visual symptom is present, at most `INFERRED`). An assertion timeout alone, with the AUT's process state unknown, is `UNKNOWN`. `EXTERNAL_AUT_CRASH` at `VERIFIED` confidence requires both a logcat crash trace and confirmed process termination for that specific run — retroactive or analogical classification based on similarity to a past `VERIFIED` instance is not permitted without independently meeting the same bar for the new instance.

---

## 8. Docker Technical Success Criteria

`DOCKER IMPLEMENTATION SUCCESSFULLY QUALIFIED` is established, based on evidence already collected (Phase 19.4L, Section 5 of that report, reaffirmed unchanged here):

- Docker image build: **VERIFIED**
- Container startup: **VERIFIED**
- Java/Gradle execution: **VERIFIED**
- Non-root execution: **VERIFIED**
- Container-to-Appium connectivity: **VERIFIED**
- Real Appium session creation: **VERIFIED**
- Emulator interaction: **VERIFIED**
- AUT installation: **VERIFIED**
- AUT launch: **VERIFIED**
- Framework execution: **VERIFIED**
- Full-suite 19/19 successful execution: **VERIFIED** (Phase 19.4, GitHub Docker Execution Proof)
- Baseline preservation: **VERIFIED**

**Intermittent AUT crashes do not invalidate any of the above.** Every one of these twelve items was independently satisfied in the very run (Phase 19.4J, Run 2) where the AUT later crashed — the crash occurred only after all twelve had already succeeded. A criterion that was satisfied before the crash occurred is not retroactively unsatisfied by an event outside its own scope.

---

## 9. Run-to-Run Stability Boundary

Three distinct metrics, not one:

- **A. Docker technical qualification** — a property of the infrastructure and framework layers, assessed once per architecture/implementation change, not per CI run. **VERIFIED** (Section 8).
- **B. Test execution reproducibility** — whether a given CI run, on a given day, produces the same pass/fail outcome as the last. **NOT VERIFIED as fully stable** — Phase 19.4F/19.4J directly observed intermittent divergence.
- **C. AUT reliability** — whether the third-party application itself behaves correctly across its own execution lifetime. **Confirmed unstable for at least one specific, evidenced mechanism** (Phase 19.4J/19.4K); overall frequency remains unquantified.

**Docker can be, and is, technically qualified (A) while B and C remain imperfect** — this is directly supported by the evidence in Section 8 and Phase 19.4L, not an assumption. When Docker infrastructure is healthy but the AUT crashes, the honest report is: *the CI run failed, Docker/Appium/framework are not implicated, the AUT is* — never "the run passed" and never "Docker failed."

---

## 10. AUT Reliability Boundary

The AUT (`com.saucelabs.mydemoapp.android` 2.2.0) has a confirmed, source-verified defect (Phase 19.4J/19.4K) with no official fix available. This project does not own the defect's source and cannot resolve it without a fork (Phase 19.4K, Option C — not decided or pursued). This boundary exists specifically so that this known, external limitation is never conflated with a defect in Docker, Appium, or this project's own framework.

---

## 11. External AUT Crash Policy

For a run formally classified `VERIFIED EXTERNAL_AUT_CRASH`:

- **Does the run count as a passing run?** **No.** It is, and remains, a failing CI run.
- **Does the test failure remain visible?** **Yes**, in the CI system exactly as any other failure would (red status, JUnit failure recorded).
- **Is the failure hidden or ignored?** **No** — this policy exists explicitly to prevent that. The classification changes *attribution*, not *visibility* or *outcome*.
- **Does it invalidate Docker technical qualification?** **No** (Section 8/9).
- **Does it invalidate production test reliability?** **It reduces run-to-run reproducibility (Section 9, dimension B), which is a real, honestly-reported limitation** — it does not invalidate the *Docker* layer's own qualification, but it does mean "production test reliability" as a whole cannot currently be claimed as perfect.
- **How should CI reporting distinguish infrastructure failure from AUT failure?** By recording, alongside the standard pass/fail result, the failure classification (Section 6) and its confidence level (Phase 19.4M: VERIFIED/INFERRED/NOT VERIFIED/UNKNOWN) — this requires the evidence-capture capability Phase 19.4N designed but did not implement (Section 13).

---

## 12. Phase 19.4B Readiness Scope

Reaffirmed, unchanged from Phase 19.4B-R: the readiness check (`AndroidDriverFactory.verifyAutForegroundReadiness`) addresses only the T0 session-creation race (Phase 19.4A). It does not, and structurally cannot, detect a mid-test AUT crash occurring after it has already passed and returned control — Phase 19.4J directly demonstrated a crash occurring ~16.5 seconds and three successful interactions after this check succeeded. This boundary document does not claim the readiness fix solves the "AUT not visible" symptom family in general; it solves exactly one instance of it, at exactly one moment.

---

## 13. Phase 19.4N Diagnostics Relationship

- **Required before Phase 19.5?** **No** — Phase 19.4L already established Docker technical qualification independent of these diagnostics; they are not a prerequisite for that qualification, which rests on different evidence (Section 8).
- **Optional?** **Yes**, in the sense that Docker qualification does not depend on them — but they are the only designed path to closing the evidence gap described next.
- **Evidence limitation without them**: as stated in Phase 19.4N/19.4M, production CI today captures only a screenshot on failure — no logcat, no process-state timeline. Under the current, unimplemented state, most future AUT-crash-shaped failures will be classifiable no higher than `UNKNOWN` or `INFERRED`, never `VERIFIED`, regardless of how closely they resemble the Phase 19.4J instance.
- **How future classification will work under current observability**: exactly as Section 7 describes — evidence-driven, per-run, with `UNKNOWN`/`INFERRED` as the honest, expected outcome for most runs until/unless the Phase 19.4N design is implemented in a separate, future, explicitly-approved phase.

---

## 14. CI Reporting Policy

Every CI failure must be reported as a failure in the CI system's native pass/fail status, regardless of classification. The failure classification (Section 6) and its confidence level are **supplementary attribution metadata**, recorded alongside — never a substitute for, and never a means of suppressing, the underlying red result. This policy applies whether or not Phase 19.4N's diagnostics are ever implemented; in their absence, the classification metadata will simply be less complete (`UNKNOWN` more often), not absent from the reporting discipline itself.

---

## 15. Production CI Integration Readiness

**READY WITH DOCUMENTED EXTERNAL LIMITATION.**

Docker technical qualification (Section 8) is complete and does not depend on AUT reliability. The known limitation (Section 10, Section 11) is fully evidenced, formally classified, and does not need to be resolved before production integration — it needs to be *documented and visible*, which this report and its predecessors (19.4J/19.4K/19.4L/19.4M) already accomplish. `READY` (unqualified) would overstate certainty about run-to-run stability (Section 9); `NOT READY` would understate what has actually been proven about the Docker layer itself (Section 8) by conflating it with a problem it does not own.

---

## 16. Option A Evaluation — Accept and document the third-party AUT limitation

- **Implementation cost**: none beyond what is already produced (this report and its predecessors).
- **Maintenance cost**: low — ongoing awareness/triage of `EXTERNAL_AUT_CRASH`-shaped failures as they occur.
- **Technical risk**: none introduced.
- **Legal/licensing considerations**: none — no AUT modification.
- **Impact on Docker qualification**: none (Section 8/9).
- **Impact on test reliability**: unchanged — the known intermittent-failure rate persists, now correctly attributed rather than mysterious.
- **Suitability for this project's current goals**: high — consistent with every recommendation made in Phases 19.4K/19.4L/19.4M/19.4N.

---

## 17. Option B Evaluation — Patch and maintain a fork of the AUT

- **Implementation cost**: medium — the exact fix is known and trivial in isolation (Phase 19.4K), but requires forking, building, and hosting a modified APK.
- **Maintenance cost**: ongoing — a forked artifact must be tracked against upstream indefinitely.
- **Technical risk**: low for the specific fix itself; the broader risk is scope creep (once forking, other defects may invite further patching).
- **Legal/licensing considerations**: **material** — the upstream repository has no license (Phase 19.4K), an unresolved legal ambiguity for any redistributed derivative.
- **Impact on Docker qualification**: none directly, but changes what artifact the CI workflow downloads — a production CI change, out of this phase's scope to decide.
- **Impact on test reliability**: would likely resolve this specific crash mechanism, with unverified effect on any other latent AUT defects.
- **Suitability for this project's current goals**: low priority given Option A already satisfies the immediate objective (documented, evidence-based limitation) without the legal/maintenance overhead.

---

## 18. Option C Evaluation — Replace the AUT

- **Implementation cost**: high — a new AUT requires re-deriving the entire locator repository (`MA-LOC-001`) and every Page Object.
- **Maintenance cost**: high, ongoing.
- **Technical risk**: high — an unvetted replacement AUT could introduce entirely new, uncharacterized defects.
- **Legal/licensing considerations**: depends entirely on the replacement chosen; not evaluated here (no candidate has been identified).
- **Impact on Docker qualification**: none directly — Docker Model 3 is AUT-agnostic by design.
- **Impact on test reliability**: unknown until a specific replacement is vetted.
- **Suitability for this project's current goals**: lowest of the three — highest cost, least evidence-supported, and not indicated by anything this engagement has found (the AUT's defect is narrow and well-understood, not evidence of pervasive unreliability requiring replacement).

---

## 19. Recommended Decision

**Option A.** It is the only option requiring no further approval, no legal exposure, no locator-repository rework, and it is fully sufficient to satisfy this phase's actual objective — a documented, evidence-based qualification boundary, not a defect-free AUT. This recommendation is consistent with, and does not revise, the same recommendation made in Phase 19.4L (Section 15/16 of that report). The decision is recorded here; it is not implemented by this phase.

---

## 20. Risks and Limitations

- Reporting policy (Section 14) is a **policy statement**, not an implemented mechanism — without Phase 19.4N's diagnostics, classification metadata will often be incomplete (`UNKNOWN`).
- The AUT crash's true frequency remains unquantified; Option A accepts this uncertainty rather than resolving it.
- If a future Phase 19.4N implementation is never approved, the evidence gap described in Section 13 persists indefinitely — an accepted, not eliminated, risk under Option A.

---

## 21. What Is Qualified

Docker architecture, implementation, runtime environment, networking, framework execution, Appium session lifecycle, emulator availability, AUT installation/launch, test harness execution, and one full 19/19 baseline execution — all **VERIFIED** (Section 5, dimensions A–I).

---

## 22. What Is Not Qualified

Run-to-run test result reliability (dimension J) and third-party AUT reliability (dimension K) — both honestly reported as imperfect/unstable, not qualified as "passing," and not expected to become qualified without action outside this project's own source (Option B/C, neither pursued).

---

## 23. Explicit Non-Claims

This report does not claim: that Docker reproducibility is perfect; that every future AUT-crash-shaped failure can be automatically classified `EXTERNAL_AUT_CRASH` without independently meeting Phase 19.4M's evidence standard for that specific run; that the AUT's crash frequency is known; that Phase 19.4N's diagnostics exist in production; that any implementation, commit, or push occurred this phase; or that Option A has been formally approved for action beyond being recorded as this phase's recommendation.

---

## 24. Next Phase Decision

This phase does not start Phase 19.5. The qualification boundary is defined; whether and when to proceed to Phase 19.5 — and whether to separately approve Phase 19.4N's diagnostics implementation, or Option B/C from this report — remain decisions for the user, outside this phase's own scope.

---

## 25. Final Verdict

# DOCKER QUALIFICATION BOUNDARY DEFINED — READY FOR DECISION

Docker technical qualification (Section 8, all twelve criteria VERIFIED) is formally and finally separated from the third-party AUT's own reliability (Section 10, confirmed unstable via a source-verified, unfixed defect). The failure ownership model (Section 6) and evidence requirements (Section 7) ensure future failures are attributed correctly rather than either hidden or wrongly blamed on Docker. Production CI integration readiness is classified `READY WITH DOCUMENTED EXTERNAL LIMITATION` (Section 15). Option A (accept and document) is recommended over Option B (patch/fork) or Option C (replace), on cost, risk, and legal grounds (Sections 16–19) — recorded as a recommendation only, not implemented. No source, Docker, CI, or AUT file was modified; nothing was committed or pushed; Phase 19.5 was not started.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Document Status | Final — Boundary Definition Report (Documentation Only, No Implementation) | — | — |

---

**End of Document — Phase 19.4O Docker Qualification Boundary Definition Report, v1.0**
