---
document_id: PHASE-19.4-CONFIRM
title: Docker GitHub Execution Reproducibility Verification
version: v1.0
status: Final — Confirmatory Run Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-09
last_updated: 2026-08-09
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4]
classification: Internal
---

# Phase 19.4 — Confirmatory Run 2/2
## Docker GitHub Execution Reproducibility Verification

No Dockerfile, `.dockerignore`, Java source, test class, `build.gradle`, Gradle wrapper, Appium configuration, emulator configuration, or the production `.github/workflows/mobile-automation.yml` was modified at any point during this check — verified explicitly (Section 3, Section 8). Nothing was "fixed" in response to the failure this check surfaced; it is reported exactly as observed.

---

## 1. Objective

Determine, via one independent, unmodified re-run of the exact Phase 19.4 configuration, whether the first successful 19/19 Docker-on-GitHub-Actions result is reproducible — not a one-off.

---

## 2. Previous Successful Run

[Run 31269472881](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31269472881) (Phase 19.4, post-fix): 19/19, 0 failures, 0 errors, confirmed via JUnit XML.

---

## 3. Environment Parity

Verified before triggering any run:

```
$ git diff ee9c9c0 -- Dockerfile .dockerignore
(empty)
$ git diff ee9c9c0 -- .github/workflows/mobile-automation.yml
(empty)
```

The confirmatory workflow file was restored via `git show ee9c9c0:.github/workflows/phase-19-4-docker-proof.yml`, byte-for-byte identical to the commit that produced the successful run — confirmed with a diff against the restored working copy (empty). No parameter, version, path, or networking setting was varied. **VERIFIED — exact reproduction of the prior configuration.**

---

## 4. Confirmatory Execution

Three GitHub Actions runs were executed in total during this check, all against the identical, unmodified workflow/Dockerfile:

| Run | Result | Duration |
|---|---|---|
| [31272374474](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31272374474) — designated confirmatory run | **FAILED** at Gate 7-8 (representative test) | 3m 34s |
| [31272885639](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31272885639) — tie-breaker run (per Section 6's own "determine whether another unchanged confirmation run is justified" allowance) | **PASSED**, 19/19 | ~19m |

The designated confirmatory run (31272374474) is the one this phase was scoped to evaluate. It failed. Per this phase's own strict evidence rule, that result is reported as-is, not superseded by the subsequent tie-breaker run's success — the tie-breaker is additional evidence about the underlying implementation's determinism, not a substitute confirmatory result.

---

## 5. Test Results

**Designated confirmatory run (31272374474):** Gates 1-6 passed cleanly (image build 8s/416MB, container start, Java 17.0.19, Gradle 9.0.0, `--network=host` connectivity all confirmed). Gate 7-8 (representative `LoginTest`) failed: `java.lang.AssertionError` at the same line (`LoginTest.java:48`) as Phase 19.3's local failure, but with a **materially different signature** (Section 11). Per this phase's own progressive-validation rule, the script correctly stopped before attempting the full suite.

**Tie-breaker run (31272885639):** All gates passed, full suite `BUILD SUCCESSFUL`, JUnit-confirmed 19/19.

---

## 6. JUnit Evidence

**Tie-breaker run (31272885639)** — the only one of the two runs in this check that reached the full suite:

| Test class | `tests` | `skipped` | `failures` | `errors` |
|---|---|---|---|---|
| `CartTest` | 13 | 0 | 0 | 0 |
| `LoginTest` | 1 | 0 | 0 | 0 |
| `NavigationTest` | 1 | 0 | 0 | 0 |
| `ProductDetailsTest` | 4 | 0 | 0 | 0 |
| **Total** | **19** | **0** | **0** | **0** |

**Designated confirmatory run (31272374474)** — only `LoginTest.xml` exists (the run stopped at Gate 7-8, as designed):

```
<testsuite name="com.mobileautomation.framework.tests.LoginTest" tests="1" skipped="0" failures="1" errors="0" ...>
```

**VERIFIED** for both runs, exactly as each actually executed — no XML was edited, regenerated, or reinterpreted.

---

## 7. Artifact Evidence

Both runs uploaded artifacts successfully (`if: always()` in the unmodified workflow). The failed run's artifact included two failure screenshots (`assertion_verifyVisible_failure_*.png`, `loginOutcomeVerification_failure_*.png`) and `logs/automation.log` — both screenshots show the **emulator's home screen**, not the AUT, and not any system ANR dialog (contrast with Phase 19.3's local failures, which showed the Products screen underneath a "Process/System UI isn't responding" dialog). `automation.log` timing: driver initialization completed in ~9.2s (faster than either the Windows local run or the first successful GitHub run), then the test's explicit-wait-bounded visibility check ran for ~15.4s before failing — consistent with the framework's configured `driver.explicitWaitTimeoutSeconds=15` being fully exhausted while the AUT was still not in the foreground. **VERIFIED** as the observed evidence; the underlying cause of why the AUT didn't foreground in time is not further diagnosed in this phase, per the explicit "do not fix, do not modify" rule.

---

## 8. Production Workflow Integrity

```
$ git diff 107d9a4 -- .github/workflows/mobile-automation.yml Dockerfile .dockerignore
(empty)
```

Confirmed unchanged across the entire confirmatory check, including through the failure and the subsequent tie-breaker run. **VERIFIED.**

---

## 9. Comparison With Previous Run

| | Run 31269472881 (original success) | Run 31272374474 (designated confirmatory) | Run 31272885639 (tie-breaker) |
|---|---|---|---|
| Configuration | Post-fix (`--user`, `GRADLE_USER_HOME`) | Byte-identical | Byte-identical |
| Gates 1-6 | PASS | PASS | PASS |
| Gate 7-8 (representative test) | PASS (1m 37s) | **FAIL** (AssertionError, home screen) | PASS |
| Full suite | PASS, 19/19 | Not reached | PASS, 19/19 |

Across three unmodified-configuration attempts (excluding the pre-fix run that surfaced the genuine uid bug already resolved before this check began): **2 of 3 reached a clean 19/19; 1 of 3 failed at the representative-test gate for a distinct, non-deterministic reason.**

---

## 10. Reproducibility Assessment

The underlying Docker-on-GitHub-Actions implementation is **not proven fully deterministic** by this check — a byte-identical configuration produced a different outcome on its second invocation. However, the failure was:

- **Not a Docker defect** — Gates 1-6 (image build, container start, networking, host-Appium reachability) passed identically across all three runs.
- **Not a Model 3 architecture defect** — the container correctly reached Appium and created a session in every run, including the failed one (session creation itself is not what failed; the *test's own visibility check*, after a live session, is what failed).
- **Not the same failure class as Phase 19.3's local ANR** — no ANR dialog, no resource-contention signature; a plain "AUT not yet foregrounded within the explicit-wait window" pattern instead.
- **Plausibly a genuine, if infrequent, app-launch timing flake** on GitHub's shared runner fleet — consistent with "B. transient GitHub runner/emulator issue" in this phase's own failure taxonomy, though this check did not gather enough repeated samples to fully rule out a lower-frequency, real synchronization gap in how the framework's `noReset=false` session/app-launch sequencing interacts with a cold app start.

**Classification: INFERRED transient (category B), not VERIFIED as definitively environmental** — one additional data point (the tie-breaker's clean pass) supports this, but a single flake-then-pass pair is not itself proof of root cause, only of non-determinism.

---

## 11. Failure/Anomaly Analysis

| Evidence | Local Phase 19.3 (Windows) | This check's failed run (GitHub) |
|---|---|---|
| Screenshot content | Products screen visible underneath a system ANR dialog | Emulator home screen, app not visible at all |
| Explicit system-level dialog | "Process system isn't responding" / "System UI isn't responding" | None |
| Driver init duration | Not directly comparable (local run included extensive npm/Appium repair) | ~9.2s (fast) |
| Reproducibility pattern | 4/4 identical failures across multiple attempts, including a full emulator restart | 1 failure out of 3 unmodified attempts; an immediate retry succeeded |

These are **evidence-backed distinct failure modes**, not the same bug recurring in a new environment. Phase 19.3's local failure was a sustained, host-wide resource-contention condition that a full emulator restart did not clear. This check's failure was a single-run app-launch timing gap that did not recur on the very next unmodified attempt. Conflating the two would overstate what either phase actually proved — this report keeps them explicitly separate.

**Classification: BLOCKED from a definitive single root cause with the evidence gathered in this phase** — the observed symptom (home screen, explicit-wait exhausted) is real and screenshotted, but *why* the AUT was slow or failed to foreground on that one occasion was not further instrumented (per this phase's explicit no-fix, no-modify, no-optimize rule, which precluded adding any new diagnostic logging).

---

## 12. Final Verdict

# DOCKER GITHUB EXECUTION NOT YET REPRODUCIBLY VERIFIED

The designated confirmatory run did not reproduce 19/19 — it failed at the representative-test gate. Per this phase's own literal decision rule (Section 10 of the brief: "If this run produces 19/19 → 2/2 GREEN. If it does not → NOT YET REPRODUCIBLY VERIFIED"), this is the correct, evidence-faithful verdict, notwithstanding that a subsequent unchanged tie-breaker run did pass cleanly. Reporting the designated run's actual result — rather than substituting the tie-breaker's more favorable outcome — is a deliberate choice to preserve evidence integrity over a convenient conclusion.

This is **not** the same finding as Phase 19.3's local result — the underlying Docker/Model 3/Appium-connectivity architecture remains fully verified (Gates 1-6 passed in all three runs, and 2 of 3 unmodified attempts reached a clean full-suite pass). What remains unproven is strict run-to-run determinism of the representative test's app-launch timing on GitHub's shared runner fleet.

**Recommended next step, not Phase 19.5 yet:** one or two additional unmodified confirmatory runs, specifically to build a real statistical picture of this app-launch-timing flake's frequency (is it ~1-in-3, or was this an outlier) — consistent with this project's own established qualification discipline (Phase 17 Final Report's "two consecutive green runs" bar, which by definition requires the *designated* runs in sequence to both pass, not a pass obtained after discarding an intervening failure). Do not proceed to **Phase 19.5 — Production CI Docker Integration** until that bar is met by designated, sequential, unmodified runs.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-09 |
| Document Status | Final — Confirmatory Run Report | — | — |

---

**End of Document — Phase 19.4 Confirmatory Run Report, v1.0**
