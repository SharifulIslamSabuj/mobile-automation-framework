---
document_id: PHASE-19.4P
title: Formal Decision — Accept and Document Third-Party AUT Limitation (Option A)
version: v1.0
status: Final — Decision Record (Documentation Only, No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Project Owner / Repository Maintainer
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4J, PHASE-19.4K, PHASE-19.4L, PHASE-19.4M, PHASE-19.4N, PHASE-19.4O, PHASE-19.4B-R]
classification: Internal
---

# Phase 19.4P — Formal Decision: Accept and Document Third-Party AUT Limitation

---

## 1. Objective

Formally record that **Option A — accept and document the third-party AUT limitation** has been explicitly selected and approved, closing the decision point Phase 19.4O left open, and establishing a clear, documented boundary so Phase 19.5 can proceed without reopening the completed Docker/AUT-crash forensic investigation (Phases 19.4A–19.4O).

---

## 2. Decision Record

**Option A is APPROVED**, as explicitly directed by the Project Owner / Repository Maintainer. This decision was made available as a recommendation in Phase 19.4O (Section 19 of that report) and in Phase 19.4L (Section 15/16); this phase records its formal acceptance, distinct from — and not itself constituting — any implementation.

---

## 3. Scope

Documentation and decision-record only. No Java source, test code, Page Object, `AndroidDriverFactory.java`, `Dockerfile`, `.dockerignore`, production CI workflow, or AUT file was modified to produce this report. No AUT patch, fork, or replacement was created. No Phase 19.4N diagnostics were implemented. No Phase 19.5 work was started. Nothing was committed or pushed.

---

## 4. What Is Being Accepted

- The verified `EXTERNAL_AUT_CRASH` mechanism (Phase 19.4J): `com.saucelabs.mydemoapp.android` 2.2.0's `ProductCatalogFragment` lacks the public no-argument constructor Android's `FragmentManager` requires during an Activity relaunch, causing `NoSuchMethodException` → `FATAL EXCEPTION` → AUT process termination — is formally accepted as a **known, external limitation of the current test target**, not a defect in this project's own Docker implementation, framework, or test code.
- That no official upstream remediation exists (Phase 19.4K): `2.2.0` is the latest release, the defect predates every release back to `1.0.0`, remains in the unreleased `main` branch, and is not acknowledged in any official issue or pull request.
- That this limitation will continue to cause intermittent CI failures at an unquantified rate, for as long as this AUT version remains in use.
- That Docker technical qualification (Phase 19.4L, all eleven criteria VERIFIED) and this AUT limitation are two separate, independently-evidenced facts, and both are accepted as currently true and stable conclusions of this engagement.

---

## 5. What Is NOT Being Accepted

- **Not accepted**: that a CI run affected by a verified `EXTERNAL_AUT_CRASH` counts as a passing run. It does not, and must not be reported, retried-until-green, or reclassified to appear as one.
- **Not accepted**: that the AUT crash is, or may in the future be casually described as, a Docker, Appium-networking, ADB-connectivity, container, emulator, or framework-readiness defect. Phase 19.4J/19.4L's evidence stands; re-attributing this mechanism to any of those layers would require new evidence, not a change of framing.
- **Not accepted**: any patch or fork of the Sauce Labs demo app (Phase 19.4K's Option C is explicitly declined by this decision, not merely deferred without comment).
- **Not accepted**: any replacement of the AUT (Phase 19.4K/19.4O's Option C is explicitly declined).
- **Not accepted**: implementation of Phase 19.4N's crash-diagnostics design as part of this decision — it remains a possible, separate, future enhancement, not bundled into or required by Option A.
- **Not accepted**: any modification to the Phase 19.4B readiness fix. Its Phase 19.4B-R verdict (`READINESS FIX RETAINED — LIMITED T0 SCOPE`) stands unchanged and unmodified by this decision.

---

## 6. Evidentiary Basis

This decision rests entirely on evidence already collected and finalized in the referenced phases; no new evidence was gathered to produce it:

- **Phase 19.4J** — direct, three-source-corroborated capture of the crash mechanism (JUnit failure, launcher screenshot, host-observer process-state transition, full logcat stack trace).
- **Phase 19.4K** — source-level confirmation against the AUT's own official repository that no newer release, unreleased fix, or official acknowledgment exists.
- **Phase 19.4L** — formal, independent qualification of the Docker technical layer across eleven criteria, unaffected by the AUT's own reliability.
- **Phase 19.4M** — the formal `EXTERNAL_AUT_CRASH` classification and its strict, two-part evidence standard (logcat `FATAL EXCEPTION` + confirmed process termination, both required for `VERIFIED`).
- **Phase 19.4O** — the formal qualification boundary separating Docker qualification (dimensions A–I) from run-to-run reliability and AUT reliability (dimensions J–K), and the recommendation of Option A over Option B/C on cost, risk, and legal grounds (the upstream repository has no license).

---

## 7. EXTERNAL_AUT_CRASH Evidence Standard — Preserved Unchanged

Per Phase 19.4M, restated here without modification: a CI failure may be classified `VERIFIED EXTERNAL_AUT_CRASH` only when **both** of the following are present for that specific run:

1. Android logcat containing a `FATAL EXCEPTION` (or equivalent unambiguous crash signal) attributable to the AUT process, timestamped within the failing test's execution window.
2. Confirmed AUT process termination within that same window (independent process-state evidence or an explicit "process ... has died"/"exited due to signal" log entry).

A launcher screenshot alone, an assertion timeout alone, or resemblance to a past `VERIFIED` instance is **not** sufficient (Phase 19.4M, Section 7C). This standard is not relaxed, narrowed, widened, or otherwise altered by this decision.

---

## 8. A Verified AUT Crash Does Not Count as a Passing Run

Stated explicitly and without qualification: a CI run affected by an `EXTERNAL_AUT_CRASH` — verified or otherwise — **is a failed run**. It must appear as red/failed in the CI system exactly as any other failure would. This decision changes only how the failure is *attributed* in supplementary classification metadata (Phase 19.4M/19.4O); it does not change, soften, or hide the run's actual pass/fail outcome.

---

## 9. Docker Technical Qualification Remains Valid Independently of AUT Reliability

Reaffirmed from Phase 19.4L/19.4O, unchanged by this decision: Docker architecture, implementation, runtime environment, networking, framework execution, Appium session lifecycle, emulator availability, AUT installation/launch, test harness execution, and the one full 19/19 baseline execution are all **VERIFIED**, independent of the AUT's own reliability. Accepting the AUT limitation (this phase) does not downgrade, qualify, or place any asterisk on that separately-evidenced qualification.

---

## 10. Four Dimensions, Kept Explicitly Separate

| Dimension | Status | Owner |
|---|---|---|
| **Docker qualification** | VERIFIED (Phase 19.4L) | This project's Docker architecture/implementation |
| **Framework behavior** | VERIFIED for its own defined scope (Phase 19.4B-R: T0 readiness check retained, limited scope) | This project's `AndroidDriverFactory`/framework code |
| **CI execution result** | A per-run outcome (pass/fail), reported honestly regardless of cause (Section 8) | The specific run in question |
| **Third-party AUT reliability** | Confirmed unstable via one verified, unfixed mechanism; overall frequency unquantified (Phase 19.4J/19.4K) | Sauce Labs (upstream, third-party) |

These four are never collapsed into a single pass/fail signal by this decision. A red CI run's cause must be attributed to the correct one of these (or another category from Phase 19.4M/19.4O's taxonomy) before any conclusion is drawn from it.

---

## 11. Operational Policy for Future CI Failures

1. **Classify with evidence** — every future failure suspected of matching this mechanism must be evaluated against the Section 7 standard before being labeled `EXTERNAL_AUT_CRASH`, at any confidence level above `UNKNOWN`.
2. **Do not assume every launcher screenshot is an AUT crash** — a launcher screenshot alone remains, per Phase 19.4M, insufficient evidence; it may indicate this mechanism, a different mechanism, or something else entirely.
3. **Use `EXTERNAL_AUT_CRASH` only when its evidence standard is met** — for that specific run, not by analogy to a previously-verified instance.
4. **Otherwise, retain the appropriate category** — `UNKNOWN`, `INFERRED`, `FRAMEWORK_FAILURE`, `DOCKER_INFRASTRUCTURE_FAILURE`, `TEST_ASSERTION_FAILURE`, or any other category from the Phase 19.4M/19.4O taxonomy, as the actual evidence supports. `UNKNOWN` is the mandatory default when evidence is incomplete, and must not be silently treated as, or default to, `EXTERNAL_AUT_CRASH`.
5. This policy applies whether or not Phase 19.4N's diagnostics are ever implemented; in their absence, most future occurrences will honestly land at `UNKNOWN` or `INFERRED` rather than `VERIFIED` — an accepted, documented consequence of Option A (Phase 19.4O, Section 20), not a defect in this policy.

---

## 12. Patching, Forking, and Replacing the AUT Are Outside the Approved Path

This decision explicitly does not approve, and forecloses for now, any of the following: building or maintaining a patched fork of the AUT (Phase 19.4K Option C / Phase 19.4O Option B); replacing the AUT with a different application (Phase 19.4O Option C). Either would require its own separate, explicit, future approval — this decision does not pre-authorize or lean toward either.

---

## 13. Framework-Level Crash Diagnostics Remain Optional and Separate

Phase 19.4N's designed-but-unimplemented hybrid diagnostics architecture (a permanent host-side observer plus a minimal `TestListener` extension) is **not** part of this decision and is **not** required by it. It remains available as a possible future enhancement, to be separately scoped and separately approved, should the project later decide the evidence-capture gap described in Phase 19.4N Section 13 / Phase 19.4O Section 13 is worth closing.

---

## 14. Sufficiency to Unblock Phase 19.5

This decision is sufficient to unblock Phase 19.5 — Production CI Docker Integration. Phase 19.4O already established `READY WITH DOCUMENTED EXTERNAL LIMITATION` as the production-CI-integration-readiness classification; this phase formally closes the one remaining open decision point (which option to pursue regarding the AUT limitation) that Phase 19.4O deliberately left for the Project Owner to make. Phase 19.5 may proceed treating the AUT limitation as a documented, accepted, non-blocking condition — it does not need to, and must not, reopen the completed Phase 19.4A–19.4O forensic investigation to do so.

---

## 15. Explicit Non-Claims

This document does not claim: that the AUT is now reliable; that the AUT's crash frequency is known; that any code, Docker, CI, or AUT file has been modified; that Phase 19.4N's diagnostics exist in any form; that Option B or C has been evaluated further than Phase 19.4K/19.4O already did; or that Phase 19.5 has been started.

---

## 16. Final Verdict

# OPTION A FORMALLY APPROVED — THIRD-PARTY AUT LIMITATION ACCEPTED AND DOCUMENTED — PHASE 19.5 UNBLOCKED

Option A is recorded as the explicitly approved path. The `EXTERNAL_AUT_CRASH` classification and its evidence standard (Phase 19.4M) are preserved unchanged. Docker technical qualification (Phase 19.4L) remains valid, independent of this decision. A verified AUT crash remains, and will continue to be reported as, a failed CI run — never hidden, suppressed, or silently passed. Patching, forking, and replacing the AUT remain unapproved. Phase 19.4N diagnostics remain optional and unimplemented. The repository is unchanged except for this documentation file.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Approved By | Project Owner / Repository Maintainer | **Approved — Option A** | 2026-08-11 |
| Document Status | Final — Decision Record (Documentation Only, No Implementation) | — | — |

---

**End of Document — Phase 19.4P Formal Decision: Accept and Document Third-Party AUT Limitation, v1.0**
