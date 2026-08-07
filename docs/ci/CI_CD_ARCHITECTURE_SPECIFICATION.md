---
document_id: MA-CICD-001
title: CI/CD Architecture Specification
version: v1.0
status: Frozen — Implemented and Verified
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs My Demo App (Android)
aut_version: 2.2.0
related_documents: [MA-PV-001, MA-FA-001, MA-RS-001, MA-TC-001, MA-DEP-001]
classification: Internal
---

# MA-CICD-001 — CI/CD Architecture Specification

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-CICD-001 |
| Document Name | CI/CD Architecture Specification |
| Version | v1.0 |
| Status | Frozen — Implemented and Verified |
| Project | Mobile Automation Framework |
| Project Code | MA |
| AUT | Sauce Labs My Demo App (Android) |
| AUT Version | 2.2.0 |
| Platform | Android |
| Classification | Internal |

---

## Version History

| Version | Date | Author | Change Description |
|---|---|---|---|
| v0.1 | 2026-08-07 | Project Owner | Initial architecture-only draft. No implementation. Defines the CI/CD design that v1.1.0 (GitHub Actions, Docker, parallel execution) will implement against. |
| v1.0 | 2026-08-08 | Project Owner | Architecture Freeze confirmed — implemented and independently verified: `.github/workflows/mobile-automation.yml` (Phase 17.3), stabilized through Phases 17.4–17.6 and 18 to a reproducibly green, 19/19-passing baseline (see [Phase 17 Final Report](PHASE_17_FINAL_CI_BASELINE_QUALIFICATION_REPORT.md) and [Phase 18 Investigation Report](PHASE_18_CI_PARITY_INVESTIGATION_REPORT.md) for the as-built, evidence-based record). No architectural decision in this document required revision during implementation — every fix made stayed within the frozen design. |

---

## Scope Note

This document is an **architecture specification, not an implementation**. It contains no YAML, no GitHub Actions syntax, and no shell commands. Its purpose is to be reviewed and frozen *before* any workflow file is written, so that the pipeline built in Phase 17.1 (implementation) has a single, agreed design to follow rather than being designed ad hoc inside a `.yml` file.

---

## 1. Executive Summary

### Purpose

Framework Version 1.0.0 has produced a working, documented, locally-executable automation framework: 19 automated test cases, a frozen five-document baseline, and a layered architecture verified against a real Android device. All execution to date, however, has been manual — a developer runs `./gradlew test` on their own machine, on their own schedule, with no independent record of whether the suite still passes on a clean checkout.

The purpose of introducing CI/CD is to remove that dependency on a human remembering to run the suite, and to replace it with an automatic, repeatable, evidence-producing execution that runs the same way every time, on infrastructure the framework does not control the state of in advance.

### Business Goals

- Give a reviewer (recruiter, hiring manager, technical interviewer, or collaborator) objective, third-party evidence that the suite runs and passes, without asking the author to demonstrate it live.
- Reduce the risk of a regression reaching `main` unnoticed.
- Demonstrate that the framework was built with the same operational discipline as its architecture and documentation.

### Engineering Goals

- Make every test run reproducible: the same commit, built the same way, produces the same result.
- Make failures fast to detect and fast to diagnose, using the reporting and logging the framework already produces.
- Make the pipeline itself a reviewable artifact — versioned, documented, and free of hidden manual steps.

### Benefits

- Continuous verification that the framework still builds and the suite still executes as documented.
- A durable, timestamped record of execution outcomes, independent of any one developer's machine.
- A foundation that later releases (Docker, parallel execution, Grid, cloud device providers) extend rather than replace.

### Scope

This specification covers the CI/CD architecture intended for **v1.1.0** (GitHub Actions on the existing local execution model) and the extension points that let v1.2.0–v2.0.0 (Docker, parallel execution, Jenkins, Azure DevOps, Grid, BrowserStack, Allure, Sauce Labs, iOS) build on it without redesigning it.

### Out of Scope

- Any workflow YAML or GitHub Actions configuration (implementation, not architecture).
- Containerization design (Docker arrives in v1.2.0; this document defines where it will plug in, not how it will be built).
- Cloud device provider integration design (BrowserStack, Sauce Labs — v1.7.0/v1.9.0).
- Grid/parallel execution design detail (v1.3.0/v1.6.0) beyond confirming the framework does not structurally block it.
- iOS pipeline design (v2.0.0) — this framework is Android-only today; no iOS-specific CI behavior is defined here.
- Cost, billing, or GitHub Actions minutes/quota analysis.

---

## 2. Existing Framework Baseline

The pipeline is built *around* the framework as it exists today, not the other way around. A CI/CD design that requires framework changes to accommodate it has the dependency backwards.

| Capability | Current State | Relevance to CI/CD |
|---|---|---|
| Language / JDK | Java 17 | Pipeline's build environment must provision JDK 17, matching `build.gradle`'s toolchain declaration |
| Build Tool | Gradle 9.0.0, wrapper-pinned (`gradlew`) | Pipeline must invoke the committed wrapper, never a runner-provided Gradle install, to preserve the version pin |
| Test Framework | TestNG 7.10.2, invoked via Gradle's `useTestNG()` | No standalone TestNG suite XML exists yet — test selection today is class-based (`--tests`) or whole-suite (`test`) |
| Automation Engine | Appium Java Client 9.4.0 + UiAutomator2 | Requires a running Appium server and an Android target (device or emulator) reachable from the runner — this is the primary CI/CD design constraint (Section 5) |
| Reporting | ExtentReports 5.1.1, written to `reports/` | `reports/` is gitignored and runtime-only; CI must capture it as a pipeline artifact, not assume it persists |
| Logging | SLF4J + Log4j2, written to `logs/` | Same as above — `logs/` is gitignored and must be captured as an artifact, not committed |
| Test Data | JSON / YAML / Properties under `src/test/resources/testdata/`, resolved via an environment-aware resolver | Already externalized and version-controlled; requires no CI-specific handling |
| Configuration | Tiered resolution (system property → environment file → common file → default), with `emulator` and `real-device` profiles | CI target selection is a configuration concern, not a code change — this is what makes the pipeline environment-agnostic (Section 5) |
| Retry | A TestNG `RetryAnalyzer` already retries individual failing test methods (`execution.retryCount`) | This is a test-level concern the pipeline must not duplicate — see Section 11 |
| Documentation | 5 governing documents (MA-RS-001, MA-TD-001, MA-TC-001, MA-TDD-001, MA-LOC-001), frozen and audited | This document extends that same governance discipline to the pipeline itself |
| Automation Coverage | 19 automated (18 dedicated + 1 incidental), 12 manual, 1 deferred, across 4 test classes | Defines what "the suite" means for CI purposes today — see Section 9 |
| Source Control | Single `main` branch, GitHub repository already published, no existing `.github/workflows/` | This is a greenfield CI/CD introduction — no existing pipeline to migrate or reconcile |
| Concurrency Readiness | Driver layer is `ThreadLocal`-based (documented, unproven under parallel load) | Confirms the architecture does not block v1.3.0 parallel execution, but no parallel run has ever been exercised — see Section 14 |

---

## 3. CI/CD Objectives

Each objective is written to be checkable, not aspirational.

| # | Objective | How it will be judged |
|---|---|---|
| O1 | Automatic build on every push and pull request | A commit to `main` or a PR triggers a build without manual action |
| O2 | Automatic test execution against the real Appium/UiAutomator2 stack | The pipeline exercises the same `./gradlew test` path a developer runs locally — no shortcut or mock substitution |
| O3 | Fast feedback | A developer knows build/test outcome without polling — result is visible on the commit/PR within the pipeline's normal run time |
| O4 | Artifact preservation | Every run's ExtentReports HTML, logs, and screenshots are retrievable after the run ends, even though they are gitignored |
| O5 | Reproducible execution | Given the same commit, the pipeline produces the same pass/fail outcome — no dependency on an unpinned tool version or an undocumented local machine state |
| O6 | Release-quality gating | A tagged release cannot be cut from a commit that has not passed the pipeline |
| O7 | No pipeline-specific code paths | The framework code executed in CI is identical to the framework code a developer runs locally — CI supplies configuration, not conditional logic |

---

## 4. Guiding Principles

| Principle | Meaning in this project |
|---|---|
| Documentation First | This specification exists and is reviewed before any workflow file is written — the same discipline already applied to MA-RS-001 through MA-LOC-001 |
| Build Once, Promote Forward | A single build's compiled output is what gets tested; the pipeline does not rebuild between steps in ways that could produce a different artifact than what was tested |
| Fail Fast | A build failure stops the pipeline before test execution begins — there is no value in running tests against code that does not compile |
| Single Source of Truth | `build.gradle`'s dependency versions and the wrapper's Gradle version are the only source of truth for the pipeline's toolchain — no version is redeclared inside the pipeline |
| Immutable Artifacts | A report or log produced by a given run is never overwritten by a later run — each run's evidence is retrievable on its own |
| Reproducible Builds | The pipeline pins the same JDK 17 / Gradle 9.0.0 / Appium Java Client 9.4.0 versions the repository already pins — it introduces no separate version decision |
| Pipeline as Code | The pipeline definition itself is version-controlled and reviewed like any other change — not configured through a UI setting that leaves no diff |
| No Secrets in Repository | No credential, token, or device identifier is ever committed — this is already true of `config-real-device.properties`, which intentionally ships blank (Section 12) |
| Evidence-Driven Reporting | The pipeline's output is the same ExtentReports/Log4j2 evidence the framework already produces — CI does not introduce a second, parallel reporting format |
| Configuration Over Code | Differences between local and CI execution (device target, environment) are expressed through the existing configuration layer, not through pipeline-only code branches |

---

## 5. High-Level Pipeline Architecture

Conceptual flow only — no implementation syntax.

```
Developer
   |
   v
GitHub (push / pull request)
   |
   v
GitHub Actions (workflow triggered)
   |
   v
Checkout (repository at the triggering commit)
   |
   v
Environment Provisioning (JDK 17, Android target reachable, Appium server available)
   |
   v
Build (Gradle wrapper — compile, dependency resolution)
   |
   v
Execute Tests (TestNG via Gradle, against the provisioned Android target)
   |
   v
Generate Reports (ExtentReports HTML, Log4j2 logs, failure screenshots)
   |
   v
Upload Artifacts (reports, logs, screenshots preserved as pipeline output)
   |
   v
Result (pass/fail status surfaced on the commit / pull request)
```

The one step this framework does not yet have a settled answer for is **Environment Provisioning** — specifically, what Android target the pipeline runs against. GitHub Actions runners do not include a physical Android device, and this framework's only verified execution evidence to date is against a real device (Section 2). Section 9 and Section 14 address this directly: v1.1.0 is expected to target an Android emulator (already a supported configuration profile — `config-emulator.properties` — even though it has not been the framework's primary verification path), with real-device and cloud-device execution remaining future-release concerns (v1.7.0, v1.9.0), not something v1.1.0 needs to solve.

---

## 6. Workflow Trigger Strategy

| Trigger | Recommended for v1.1.0 | Rationale |
|---|:---:|---|
| `push` (to `main`) | Yes | Every change that lands on `main` should be verified — this is the minimum viable continuous-integration guarantee, and matches the single-branch reality described in Section 2 |
| `pull_request` | Yes | Verifies a change *before* it merges, not after — catches a regression while it is still cheap to fix and still attached to review context |
| `workflow_dispatch` | Yes | A manual trigger costs nothing to support and is valuable while the pipeline is new — it lets the pipeline be re-run against a specific commit without needing a new push, useful for diagnosing a flaky first few runs |
| `schedule` | Not yet | A scheduled (e.g. nightly) run only earns its cost once there is a reason to detect drift with no code change involved — with a single branch and no external dependency likely to silently change, there is nothing today a scheduled run would catch that `push`/`pull_request` would not. Reconsider once cloud device providers (v1.7.0+) introduce infrastructure the framework does not control |
| `release` | Not yet | Meaningful once Section 13's versioning strategy is implemented (tag → release), so a `release` trigger has a defined job to do (e.g. gating publication). Introducing it before that relationship exists would trigger a workflow with nothing release-specific to run |
| `tag` (tag-push trigger) | Not yet | Same reasoning as `release` — tags are not yet part of this repository's workflow (no tags exist today); introduce alongside Section 13, not before it |

**Recommendation for v1.1.0:** `push` to `main`, `pull_request`, and `workflow_dispatch`. `schedule`, `release`, and `tag` triggers are deferred until the versioning strategy (Section 13) and cloud-device releases (v1.7.0+) give them a concrete purpose — adding a trigger with no defined job is complexity without benefit.

---

## 7. Branch Strategy

The repository currently has exactly one branch: `main`. There are no `feature/*`, `release/*`, or `hotfix/*` branches today, and no branch protection has been established.

| Branch Pattern | Recommendation | Rationale |
|---|---|---|
| `main` | Protected; always green | Represents the current, verified state of the framework — the same commit history that produced the frozen v1.0.0 documentation baseline should never regress silently |
| `feature/*` | Recommended for future work | Isolates in-progress changes (e.g. a new Page Object, a new test case) from `main` until the pipeline confirms them — natural home for a `pull_request`-triggered run |
| `release/*` | Deferred until Section 13 is implemented | Not needed until releases are cut from something other than `main` directly — introducing it now would create a branch with no defined purpose |
| `hotfix/*` | Deferred | No production incident process exists yet for a portfolio framework at this stage — recommend adopting this pattern only if/when the framework has an actual consumer depending on a stable release |

**Recommendation:** adopt `main` (protected) + `feature/*` for v1.1.0. This is the minimum branching model that gives the `pull_request` trigger (Section 6) something meaningful to run against, without inventing branch types the project has no current use for.

---

## 8. Build Strategy

| Concern | Definition |
|---|---|
| Build Command | The committed Gradle wrapper (`./gradlew`), never a runner-provided Gradle installation — this is the only way to guarantee the pinned Gradle 9.0.0 is what actually runs |
| Compile Validation | A dedicated compile step (Gradle's compilation task) must succeed before any test task is invoked — this operationalizes the "Fail Fast" principle (Section 4): a compile failure is reported as a build failure, not a test failure |
| Dependency Restore | Dependencies resolve from the `dependencies` block already declared in `build.gradle` (Appium Java Client 9.4.0, TestNG 7.10.2, ExtentReports 5.1.1, etc.) — the pipeline introduces no additional or CI-only dependency |
| Gradle Cache | Runner-level caching of the Gradle dependency cache and wrapper distribution is recommended to keep O3 (fast feedback) realistic — this is a runner configuration concern, not a change to `build.gradle` itself |
| Failure Policy | A build failure (compile error, dependency resolution failure) halts the pipeline immediately; no test execution is attempted against a build that did not succeed |

The existing `test { useTestNG(); failOnNoDiscoveredTests = false }` configuration in `build.gradle` (originally added so the pre-Phase-9 foundation build would not fail on zero discovered tests) remains compatible with this strategy: since Phase 9, TestNG discovers and runs real tests, so this flag no longer changes pipeline behavior, but it requires no modification to support CI.

---

## 9. Test Execution Strategy

There is currently no TestNG suite XML and no `@Test` group tagging (`groups = {...}`) anywhere in the codebase (verified against all four test classes). Test selection today is either the whole suite (`gradlew test`) or a single class (`gradlew test --tests "<FQCN>"`). This is a real constraint on what "smoke vs. regression" can mean in v1.1.0.

| Trigger Context | Recommended Execution | Rationale |
|---|---|---|
| Push to `main` | Full suite (all 4 test classes, all 19 automated test cases) | `main` is the protected, always-green branch (Section 7) — it should only ever reflect a fully-verified state |
| Pull Request | Full suite | With only 19 automated test cases across 4 classes, execution time does not yet justify a reduced subset — introducing a smoke/regression split before there is a suite large enough to need one adds process without benefit |
| Manual (`workflow_dispatch`) | Full suite by default, with the option to target a single test class via the existing `--tests` filter | Reuses a capability the framework already has locally; no new selection mechanism needs to be designed |
| Future releases (v1.3.0+) | Suite-level parallelization across the existing 4 test classes, once parallel execution is implemented | The `ThreadLocal` driver design already anticipates this (Section 2); this document does not design the parallel strategy itself — that is v1.3.0's scope |

**Smoke vs. Regression:** not recommended for v1.1.0. A smoke/regression split requires either TestNG groups or a suite XML, neither of which exists today, and introducing test categorization is a framework change, not a pipeline change — it belongs in a future test-design phase (updating MA-TC-001's Execution Tags into enforceable TestNG groups), not in this CI/CD architecture. Until that exists, "the suite" means all 19 automated test cases, every run.

---

## 10. Artifact Strategy

| Artifact | Produced By | Preserve in v1.1.0? | Notes |
|---|---|:---:|---|
| ExtentReports HTML | `ExtentReportManager` → `reports/AutomationReport_*.html` | Yes | Primary human-readable evidence of the run; this is what a reviewer should be able to download and open |
| Log4j2 execution logs | `LogManager` → `logs/` | Yes | Step-level diagnostic evidence, needed to investigate any failure the HTML report alone doesn't explain |
| Failure screenshots | `ScreenshotManager` → `reports/screenshots/` | Yes | Already captured automatically on assertion/execution failure — no new capture logic required |
| Gradle build/test report | Gradle's own HTML test report | Yes | Standard Gradle output, complementary to ExtentReports — costs nothing extra to preserve since Gradle already produces it |
| TestNG XML result output | TestNG's native XML reporter (`test-output/`, gitignored per `.gitignore`) | Yes | Machine-readable result format, useful if any future tooling needs to parse outcomes rather than read HTML |
| Allure results | Not applicable | No | Allure is not implemented until v1.8.0 — nothing to preserve yet |
| Execution videos | Not applicable | No | Not implemented in any current or near-term release; would require a screen-recording capability the framework does not have |

**Retention:** each run's artifacts should be preserved independently (Section 4's "Immutable Artifacts" principle) and retained long enough to review a pull request's outcome after the fact — the specific retention period is an implementation decision, not an architectural one.

---

## 11. Failure Handling Strategy

| Concern | Strategy | Rationale |
|---|---|---|
| Fail Fast | Build failures halt the pipeline before test execution (Section 8); a test failure does not halt subsequent independent test classes, so one broken class does not hide the results of the other three | Maximizes the information returned from a single run |
| Artifact Upload on Failure | Reports, logs, and screenshots are uploaded whether the run passes or fails | A failing run's evidence is more valuable than a passing run's — the pipeline must not be configured to skip artifact upload on failure |
| Log Preservation | Full Log4j2 output for the run is preserved, not truncated | Truncated logs are a common cause of undiagnosable CI failures; this framework already produces structured, per-run logs (Section 2), so preservation is a capture decision, not a new capability |
| Failure Visibility | Pass/fail status is visible directly on the commit or pull request, without requiring a reviewer to open the workflow run to find out the outcome | Supports O3 (fast feedback) |
| Retry Policy (pipeline-level) | Not recommended in v1.1.0 | The framework already has a test-method-level `RetryAnalyzer` (`execution.retryCount`, Section 2). A pipeline-level retry on top of that risks masking a genuinely flaky test behind two layers of retry, which directly contradicts "Fail Fast" and "Evidence-Driven Reporting" (Section 4). If retry tuning is needed, it belongs in the existing `RetryAnalyzer` configuration, not in the pipeline |

---

## 12. Security Strategy

| Concern | Current State | v1.1.0 Design |
|---|---|---|
| GitHub Secrets | Not yet used — no CI exists today | GitHub Actions' built-in encrypted secrets store is the designated mechanism for any credential the pipeline needs; no credential is ever placed in a workflow file or a config file |
| Environment Variables | `config-real-device.properties` already ships with `device.name`, `platform.version`, and `device.udid` intentionally blank, resolved via system property at execution time (confirmed in the file's own header comment) | The same pattern extends to CI: any environment-specific value is supplied at run time, never hardcoded into a committed file |
| Future BrowserStack Credentials (v1.7.0) | Not applicable yet | When introduced, will be stored as GitHub Secrets and injected as environment variables/system properties at the point the framework's existing tiered configuration layer already expects them — no new credential-handling mechanism needs to be designed now |
| Future Sauce Labs Credentials (v1.9.0) | Not applicable yet | Same pattern as BrowserStack |
| Azure Credentials (v1.5.0) | Not applicable yet | Same pattern — GitHub Secrets at the point of need, consumed through configuration, not code |
| Signing Keys | Not applicable — this framework produces no distributable artifact (library JAR, mobile build) that requires signing | No signing strategy is defined; revisit only if a future release changes this framework from an automation suite into a distributed artifact |

**Principle carried forward:** every credential this pipeline will ever need — today or in v1.9.0 — is consumed the same way: injected at run time into the framework's existing configuration layer. No release on the roadmap requires inventing a second credential mechanism.

---

## 13. Versioning Strategy

| Concept | Current State | Relationship |
|---|---|---|
| Framework Version | `1.0.0`, declared in `build.gradle` (`version = '1.0.0'`) | The single source of truth for "what version is this code" |
| Git Tag | None exist yet | Recommended to be introduced alongside a release process: a tag (e.g. matching the `build.gradle` version) marks the exact commit a release corresponds to |
| GitHub Release | None exist yet (per the project's stated "Release v1.0.0 already published" — published as a milestone, not yet as a GitHub Release object with an associated tag) | Recommended to be created from a tag, once tags are introduced, so the GitHub Release page and `build.gradle`'s version never disagree |
| CI Version / Run Number | Not applicable yet | GitHub Actions' own run numbering is sufficient to distinguish pipeline executions — no separate CI-specific version scheme is needed |

**Recommendation:** the `build.gradle` version remains the single source of truth. A future release process should tag the exact commit matching that version and publish a GitHub Release from that tag — but this document does not mandate *when* that process is introduced; it only establishes that when it is, tag and `build.gradle` version must never drift apart. Implementing this relationship is separate from — and can follow — the v1.1.0 pipeline itself.

---

## 14. Future Extensibility

This section is the direct answer to the requirement that the architecture support v1.2.0–v2.0.0 without redesign.

| Future Release | What It Adds | Why This Architecture Already Accommodates It |
|---|---|---|
| v1.2.0 — Docker | Containerized execution environment | Section 5's pipeline is expressed as conceptual stages (checkout → build → test → report → upload), not as "runs on the GitHub-hosted runner's OS directly" — a containerized execution target replaces *where* the Build and Execute Tests stages run, not the stages themselves |
| v1.3.0 — Parallel Execution | Concurrent test execution | The framework's driver layer is already `ThreadLocal`-based (Section 2) specifically to not block this; Section 9 already separates "the suite" from "how it's scheduled," so parallelizing execution is a scheduling change to Section 9, not a pipeline redesign |
| v1.4.0 — Jenkins | An additional CI runner | Section 4's "Pipeline as Code" and "Configuration Over Code" principles mean the pipeline's logic lives in versioned definitions that describe the same conceptual stages (Section 5) regardless of which CI product executes them — Jenkins becomes a second implementation of the same architecture, not a different one |
| v1.5.0 — Azure DevOps | Another additional CI runner | Same reasoning as Jenkins |
| v1.6.0 — Selenium/Appium Grid | Distributed device execution | Section 5's Environment Provisioning stage is already the explicit point where the Android target is resolved; Grid changes what that stage resolves to, not the stages before or after it |
| v1.7.0 — BrowserStack | Cloud device provider | Same as Grid — a different Environment Provisioning target; Section 12 already defines how its credentials will be handled |
| v1.8.0 — Allure | Additional reporting format | Section 10's artifact table already treats reporting formats as a list, not a single hardcoded assumption — Allure results become an additional row, not a replacement of ExtentReports |
| v1.9.0 — Sauce Labs | Cloud device provider | Same as BrowserStack |
| v2.0.0 — iOS | A second platform | Section 2 already frames the pipeline around the framework's existing, environment-driven configuration layer rather than an Android-specific code path in the pipeline itself; adding an iOS target is a second Environment Provisioning configuration, following the same pattern the `emulator`/`real-device` profiles already establish for Android |

The common thread: every future release changes **what fills a stage** (Section 5) or **what a stage's target is** (Section 12, Section 14) — none of them require adding, removing, or reordering the stages themselves. That is what "without redesign" means in practice for this architecture.

---

## 15. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| No Android target is available to a GitHub-hosted runner by default | Pipeline cannot execute the suite at all | v1.1.0 must resolve this via an emulator-based Environment Provisioning step (Section 5) — this is the single largest open question this specification identifies and the first thing v1.1.0 implementation must settle |
| Emulator execution has never been verified for this framework (all existing evidence is real-device) | A test that passes on a real device might behave differently, or fail, on an emulator, producing pipeline results that don't match local verification history | Treat the first several emulator-based CI runs as a verification exercise in their own right, not an assumption of parity — discrepancies should be documented, not silently patched over |
| No TestNG groups/suite XML exists | Cannot introduce smoke/regression subsets without a framework change first | Deferred by design (Section 9) — flagged here so it is not mistaken for an oversight |
| Flaky UI test masked by two retry layers (framework `RetryAnalyzer` + a hypothetical pipeline retry) | A genuinely broken test could report as passing | Addressed directly in Section 11 — no pipeline-level retry is recommended for v1.1.0 |
| Secrets introduced prematurely for not-yet-implemented providers (BrowserStack, Sauce Labs, Azure) | Unused secrets sitting in the repository's secret store expand the security surface with no corresponding functionality | Section 12's principle — no credential is provisioned before the release that consumes it exists |
| Pipeline becomes the only place certain configuration values are known (e.g. emulator target specifics) | Local reproducibility of a CI failure becomes harder if the CI-only configuration isn't documented | Any CI-specific configuration value must be recorded in this document's future implementation companion, not left implicit inside a workflow file |

---

## 16. Definition of Done

Exit criteria that must be true before implementation (Phase 17.1) may begin:

1. This specification has been reviewed against the actual repository state (Section 2) and no factual claim in it has been found inaccurate.
2. The Environment Provisioning question raised in Section 5 and Section 15 (emulator target for v1.1.0) has an agreed answer.
3. The trigger strategy (Section 6) and branch strategy (Section 7) are agreed, including the decision to defer `schedule`/`release`/`tag` triggers.
4. The artifact list (Section 10) is agreed as the complete set of evidence v1.1.0 must preserve.
5. The decision not to introduce pipeline-level retry (Section 11) and not to introduce smoke/regression splitting (Section 9) in v1.1.0 is explicitly acknowledged, not just defaulted into.
6. No workflow YAML, shell script, or GitHub Actions configuration exists in the repository yet — confirming this document was produced as architecture-only, per the governing rule of this phase.
7. This document is committed under `docs/ci/` and cross-referenced from the project's documentation index, consistent with how MA-RS-001 through MA-LOC-001 are indexed.

---

## 17. Review Checklist

- [ ] Every claim in Section 2 (Existing Framework Baseline) was verified against the current repository, not assumed.
- [ ] No section contains YAML, shell commands, or GitHub Actions syntax.
- [ ] Every recommendation in Sections 6–13 includes a stated rationale, not just a conclusion.
- [ ] Deferred decisions (smoke/regression splitting, pipeline retry, `schedule`/`release`/`tag` triggers, `release/*`/`hotfix/*` branches) are explicitly justified, not silently omitted.
- [ ] Section 14 demonstrates, for every roadmap release (v1.2.0–v2.0.0), which specific pipeline stage or configuration point absorbs that release's change.
- [ ] Section 15's risks include the single largest open question (Android target availability on a GitHub-hosted runner) and it is not understated.
- [ ] Nothing in this document claims a capability (CI, Docker, Grid, cloud devices, Allure, iOS) is implemented — all remain correctly scoped as architecture-only or future-release.
- [ ] Document follows the same Document Control / Version History / Approval structure as MA-RS-001, MA-TD-001, MA-TC-001, MA-TDD-001, and MA-LOC-001.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Frozen — Implemented and Verified | — | 2026-08-08 |

---

**End of Document — MA-CICD-001, v1.0**
