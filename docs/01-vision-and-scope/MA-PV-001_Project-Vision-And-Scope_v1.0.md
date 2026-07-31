---
document_id: MA-PV-001
title: Project Vision and Scope
version: v1.0
status: Draft
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-07-29
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: []
classification: Internal
---

# MA-PV-001 — Project Vision & Scope

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-PV-001 |
| Document Name | Project Vision & Scope |
| Version | v1.0 |
| Status | Draft |
| Project | Mobile Automation Framework |
| Project Code | MA |
| Repository | mobile-automation-framework |
| AUT | Sauce Labs Android Demo App |
| AUT Version | 2.2.0 |
| Platform | Android |
| Classification | Internal |

---

## Version History

| Version | Date | Author | Change Description |
|---|---|---|---|
| v0.1 | 2026-07-29 | Project Owner | Initial draft created |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Document Author | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewer | Pending | Pending | — |
| Approver | Pending | Pending | — |

*This document becomes the governing baseline for the Mobile Automation Framework project once approved. All subsequent documents (MA-AA, MA-RS, MA-TS, MA-FD, and beyond) must remain consistent with the vision, scope, and boundaries defined here. Any deviation must be raised as a formal change request against this document, not silently absorbed into later phases.*

---

## Table of Contents

1. Executive Summary
2. Document Purpose
3. Business Background
4. Project Vision
5. Project Mission
6. Problem Statement
7. Business Objectives
8. Project Goals
9. Success Metrics
10. Target Audience
11. Project Scope
12. Supported Platform
13. Supported Android Versions
14. Supported Device Types
15. Supported Execution Modes
16. Technology Stack Overview
17. Project Principles
18. Project Assumptions
19. Project Constraints
20. Known Risks
21. Risk Mitigation Strategy
22. Dependencies
23. Project Deliverables
24. Acceptance Criteria
25. Definition of Success
26. Expected Benefits
27. Future Roadmap
28. Glossary
29. References
30. Conclusion

---

## 1. Executive Summary

The Mobile Automation Framework is a from-scratch, documentation-first mobile test automation initiative built against the Sauce Labs Android Demo App (v2.2.0). The project exists to demonstrate — through a real, working artifact rather than a slide deck — how a senior Software Development Engineer in Test (SDET) approaches automation the way an enterprise team would: by freezing scope and intent before writing a single line of code, by producing traceable governing documents before framework design, and by treating the automation codebase as a maintained software product rather than a disposable script collection.

This document, MA-PV-001, is the first substantive artifact in that lifecycle. It does not describe how the framework will be built. It describes why it is being built, who it is being built for, what boundaries define "done," and what would make the project a success or a failure. Every later document — AUT analysis, requirements, test strategy, framework design, CI/CD design — inherits its authority from this one. If a later decision cannot be traced back to something stated or reasonably implied here, that decision is out of alignment and must be reconciled.

The project targets Android automation using Appium 3.x with the UiAutomator2 driver, written in Java 17, built with Gradle, and executed through TestNG. These tool choices are stated here as frozen facts inherited from Phase 0; this document does not justify or elaborate on them technically — that discussion belongs to MA-FD (Framework Design). What this document does establish is the human and business reasoning behind the project: a portfolio-grade demonstration of enterprise QA automation discipline, built to be read, reviewed, and judged by technical interviewers and hiring managers as much as it is meant to be executed by a CI pipeline.

## 2. Document Purpose

This document formally defines the vision, mission, scope boundaries, target audience, and success criteria for the Mobile Automation Framework project. It is written to be the single source of truth for "what this project is" before any requirements gathering, AUT analysis, or framework design begins.

Specifically, this document exists to:

- Freeze the project's direction so that scope does not silently drift as implementation work begins.
- Give any future reader — including the author returning to this project months later — a fast, unambiguous answer to "why does this exist and what is it supposed to do."
- Establish the boundary between what this project will deliver and what it explicitly will not, so that effort is not wasted building things nobody asked for and nobody will evaluate.
- Provide a citable reference point for every later document in the MA document series, so scope disagreements can be resolved by re-reading this document rather than by memory or assumption.
- Serve as the first artifact a technical reviewer, recruiter, or hiring manager encounters when assessing the seriousness and maturity of the project.

This document is intentionally free of implementation detail. Readers looking for framework architecture, folder structure, design patterns, or code should consult later documents in the series (beginning with MA-AA-001 and MA-FD-series). Mixing "why" and "how" in a single document is a common failure mode in real engineering organizations — this project avoids that failure deliberately, starting here.

## 3. Business Background

In a real enterprise context, a mobile QA automation initiative is rarely started because "automation is good practice" in the abstract. It is started because a specific business pressure makes manual-only testing insufficient: release velocity increasing, regression surface growing, manual test cycles taking too long relative to release cadence, or defects escaping to production because coverage could not scale with the app.

This project simulates that same business context deliberately, using a stable, publicly available reference application — the Sauce Labs Android Demo App — as a stand-in for a real product under active development. The choice of a stable demo app instead of a live commercial product is intentional and is stated openly rather than hidden: it removes the variability of a constantly changing UI and backend, which would make the framework itself (not the AUT) unreliable to demonstrate. In exchange, the project can focus entirely on demonstrating framework quality, process discipline, and engineering maturity — the actual things a hiring manager or senior engineer is evaluating when they review a portfolio project.

The underlying business scenario this project is designed to reflect is a common one in mobile-first companies: a mobile application has manual regression testing as its only quality gate, release cycles are frequent, and the QA function needs a maintainable, scalable, CI-ready automation framework that a team — not just one engineer — can extend over time. The Mobile Automation Framework is built as if that scenario were real, with the same rigor a paid engagement would demand, even though the AUT itself is a fixed demo application rather than a live production system.

## 4. Project Vision

**To build and demonstrate an enterprise-grade, maintainable Android test automation framework — governed by documentation-first engineering discipline — that reflects how a senior SDET would design, structure, and evolve a real production mobile automation solution.**

The vision is not "automate the Sauce Labs Demo App." Automating that specific app is the mechanism, not the goal. The actual vision is to produce a framework whose engineering quality, documentation trail, and structural discipline would transfer directly to a real commercial Android application with minimal rework — proving the framework's design is sound independent of the AUT it happens to be pointed at.

This vision deliberately favors depth of engineering discipline over breadth of test coverage. A framework that automates twelve well-chosen scenarios with clean architecture, clear documentation, and reliable execution demonstrates more engineering maturity than one that automates two hundred scenarios with brittle, undocumented, copy-pasted code. Every future phase of this project will be evaluated against that priority.

## 5. Project Mission

The mission translates the vision into an operating commitment for how the project will actually be executed, phase by phase:

- Establish project direction and governance before any code exists (Phase 0 — complete).
- Freeze intent and boundaries in a formal Vision & Scope baseline before analysis begins (this document, Phase 1).
- Analyze the AUT and capture requirements as explicit, traceable documents before designing the framework.
- Design the framework architecture on paper before writing implementation code, so structural decisions are deliberate rather than incidental.
- Implement the framework and test suite in a way that is directly traceable back to the requirements and design documents that preceded it.
- Integrate continuous integration and reporting as a natural extension of the framework's design, not as an afterthought bolted on at the end.
- Maintain every document in this series as a living record of decisions, so the project's history is auditable the same way a real enterprise codebase's history would be.

This mission is the operating contract for how work proceeds from this point forward. It is what "documentation-first development," named explicitly in the project's methodology, means in practice.

## 6. Problem Statement

Manual regression testing of a mobile application does not scale with release frequency. As the number of user-facing flows in an application grows, the time required to manually verify all of them before each release grows with it — but release cadence in modern mobile organizations does not slow down to accommodate that growth. The result, in a real organization, is one of two outcomes: test coverage is quietly reduced to fit the available time, or releases are delayed to preserve coverage. Neither outcome is acceptable in a competitive mobile market.

At the same time, many mobile automation efforts fail not because automation itself is the wrong idea, but because the automation is built without engineering discipline: no clear scope boundary, no documented rationale for design decisions, tests coupled tightly to UI implementation details, and no separation between "why we automate this" and "how we automate it." Such frameworks become expensive to maintain and are frequently abandoned within a year of being built — a well-documented and common failure pattern in the industry.

This project treats that failure pattern as the problem to be solved, not just at the code level but at the process level. The Mobile Automation Framework exists to demonstrate a corrective approach: automation built the way a mature engineering organization would build it, with scope frozen, decisions documented, and structure planned before implementation — specifically so that the resulting framework does not fall into the same maintainability trap that causes most mobile automation efforts to be discarded.

## 7. Business Objectives

| # | Business Objective | Rationale |
|---|---|---|
| BO-1 | Demonstrate enterprise-grade QA automation engineering capability | The project's primary audience evaluates hiring signal, not app coverage |
| BO-2 | Establish a documentation trail that mirrors real enterprise SDLC governance | Distinguishes this project from typical "tutorial-style" automation repos |
| BO-3 | Produce a framework structurally reusable against a different AUT with minimal rework | Proves the design is sound engineering, not app-specific scripting |
| BO-4 | Reduce the perceived risk a hiring manager associates with an unstructured portfolio project | A governed, traceable project is easier to evaluate and trust in an interview setting |
| BO-5 | Create a durable reference project the author can extend, reuse, and cite across future roles | Long-term personal and professional value beyond a single job search cycle |

These objectives are business-level, not technical. They describe outcomes for the project's stakeholders (see Section 10), not outcomes for the AUT itself.

## 8. Project Goals

Project goals operationalize the business objectives into concrete, phase-spanning targets:

- **G-1:** Complete a governed document series (MA-PV, MA-AA, MA-RS, MA-TS, MA-FD, and subsequent phases) before framework implementation begins, with each document formally baselined.
- **G-2:** Design and implement a Java-based Appium 3.x / UiAutomator2 automation framework that is readable, maintainable, and extensible by someone other than the original author.
- **G-3:** Achieve automated coverage of a deliberately scoped, representative set of user flows within the Sauce Labs Android Demo App — prioritizing flow diversity and framework robustness over raw test count.
- **G-4:** Integrate the framework with a CI pipeline so tests execute automatically and produce reviewable reports, without requiring manual local setup to observe results.
- **G-5:** Publish the completed project to a public GitHub repository in a state that is self-explanatory to a technical reviewer within minutes of opening it.

These goals are stated at a level appropriate for a vision document. Specific numeric test counts, coverage matrices, and pass/fail thresholds are defined later, in MA-TS (Test Strategy) and MA-TP (Test Plan), once the AUT has been formally analyzed.

## 9. Success Metrics

| Metric | Target | Measured By |
|---|---|---|
| Governing documents completed before implementation | 100% of planned Phase 0–4 documents baselined | Document status field = Approved |
| Framework build reliability | Framework compiles and executes cleanly with zero manual environment patching | Fresh clone + documented setup steps succeed |
| Test suite stability | Automated suite produces consistent pass/fail results across repeated runs against an unchanged AUT | Repeated CI execution history |
| Traceability | Every implemented test traceable to a documented requirement or scope item | Cross-reference between MA-TC and MA-RS documents |
| Reviewer comprehension | A technical reviewer can understand the project's purpose and structure without contacting the author | Structured README, documentation series, clear repository layout |
| CI integration | Test execution and reporting occur automatically on defined triggers, not only on-demand locally | CI pipeline execution history |

These metrics intentionally avoid vague language such as "high quality" or "robust framework" without a measurable anchor. Where a metric cannot yet be made numeric (for example, exact test counts), it is deferred to the document where that number is properly owned (MA-TS, MA-TP) rather than guessed here.

## 10. Target Audience

This project has two audience categories that must both be satisfied simultaneously: **technical practitioners** who will read, extend, or evaluate the framework as engineering work, and **evaluators** who will judge it as a hiring signal. Designing for one at the expense of the other would undermine the project's purpose, so both are treated as first-class stakeholders.

### 10.1 QA Engineers

QA Engineers reviewing this project will primarily assess whether the automation reduces real manual testing burden and whether the framework's scope decisions are sensible. For this audience, the project must clearly show what is and is not automated (Section 11) and why, so the framework reads as a deliberate coverage strategy rather than an arbitrary subset of flows.

### 10.2 SDET Engineers

SDET Engineers are the audience most likely to scrutinize engineering rigor: documentation traceability, separation of concerns, and whether the project reflects genuine SDLC discipline versus surface-level structure. This document series, and the documentation-first methodology it enforces, is aimed squarely at satisfying this audience's expectations.

### 10.3 Automation Engineers

Automation Engineers will evaluate tool selection reasoning, maintainability signals, and whether the framework would realistically survive contact with a changing application. While the technical justification for Appium 3.x, UiAutomator2, Java 17, Gradle, and TestNG is deferred to MA-FD, this document establishes that those choices were made deliberately as part of a frozen technology baseline (Section 16), not selected ad hoc during implementation.

### 10.4 Software Test Engineers

Software Test Engineers, particularly those transitioning from manual to automated testing, will look to this project as a structural reference for how a test effort is planned before it is executed. The staged document series (Vision → AUT Analysis → Requirements → Strategy → Design) is itself a teaching artifact for this audience.

### 10.5 Students

Students studying QA automation will use this project as a learning reference for professional process, not just code syntax. For this audience, the value is less in the final framework and more in the visible progression of documents — seeing how a project's scope and design are reasoned about before implementation, which is rarely shown in tutorial-style repositories.

### 10.6 Recruiters

Recruiters typically have limited time and limited technical depth to evaluate a repository in detail. For this audience, the project must communicate its seriousness and completeness quickly — through document structure, naming consistency, and a clear top-level narrative — without requiring the recruiter to read code.

### 10.7 Hiring Managers

Hiring Managers evaluate this project as evidence of how the author would operate inside their organization: whether the author plans before building, documents decisions, and produces maintainable work rather than one-off scripts. This audience is the primary reason the project enforces documentation-first development as a stated methodology rather than treating it as optional polish.

### 10.8 Technical Interviewers

Technical Interviewers are the most demanding audience and the most likely to probe inconsistencies — asking why a decision was made, whether the framework would scale to a second AUT, or whether test scope was chosen deliberately. This document, and the frozen conventions from Phase 0, exist specifically so that every such question has a documented, defensible answer rather than an improvised one.

## 11. Project Scope

Scope is the most consequential section of this document. Every future phase must operate within the boundaries defined here unless a formal change is recorded against this baseline.

### 11.1 In Scope

- Design and delivery of a documentation-first governance trail (Vision & Scope, AUT Analysis, Requirements, Test Strategy, Framework Design, and subsequent planning documents) preceding implementation.
- Test automation of the Sauce Labs Android Demo App, version 2.2.0, on the Android platform only.
- Use of Appium 3.x with the UiAutomator2 driver as the sole automation engine and driver combination.
- Implementation in Java 17, built and managed with Gradle, executed through TestNG.
- Local device and local emulator execution as the primary supported execution mode for this phase (see Section 15).
- Selection of a deliberately scoped, representative set of user-facing flows within the AUT, chosen for coverage diversity and framework demonstration value rather than exhaustive coverage.
- CI pipeline integration sufficient to execute the automated suite and produce accessible test reports on defined triggers.
- Publication of the completed project as a public GitHub portfolio repository.

### 11.2 Out of Scope

- iOS automation, cross-platform automation, or any driver other than UiAutomator2 (explicitly deferred to the Future Roadmap, Section 27, as a possible later expansion — not a current commitment).
- Cloud device farm execution (e.g., Sauce Labs cloud grid, BrowserStack, Firebase Test Lab) for this phase. Local execution is the frozen baseline; cloud execution is a roadmap item, not a current deliverable.
- Performance testing, load testing, or security testing of the AUT.
- API-level or backend testing of any service the AUT communicates with.
- Exhaustive regression coverage of every screen and flow in the AUT. Coverage is deliberately scoped, not total.
- Visual regression testing or pixel-level UI comparison.
- Any modification of the AUT itself. The AUT is treated as a fixed, unmodifiable reference application.
- Framework support for applications other than the named AUT, in this phase.
- Any implementation activity (code, architecture, CI pipeline definitions, page objects, design patterns). Those belong to later phases and later documents, not to this one.

### 11.3 Scope Change Control

Any change to the boundaries above after this document is baselined must be recorded as a new minor or major version of this document (per the versioning convention frozen in Phase 0), with the change and its rationale logged in the Version History table. Scope must never be silently expanded or reduced during implementation phases without updating this document first.

## 12. Supported Platform

| Attribute | Value |
|---|---|
| Platform | Android |
| Platform Scope for This Phase | Android only — no iOS, no cross-platform abstraction layer |

Android is the sole supported platform for the entirety of the current project baseline. This is a direct consequence of the AUT itself being an Android-only demo application and of UiAutomator2 being an Android-specific Appium driver. Cross-platform ambition, if pursued later, is addressed only as a roadmap item (Section 27) and would require its own formal scope change against this document.

## 13. Supported Android Versions

The framework's supported Android version range is defined at the vision level as a compatibility commitment, not as a device-lab implementation detail:

| Android Version Range | Support Status |
|---|---|
| Android 10 (API 29) and above | Supported baseline |
| Android versions below API 29 | Out of scope for this phase |

This range is chosen to reflect currently realistic Android usage in professional mobile QA contexts, rather than attempting to support legacy OS versions with diminishing real-world relevance. Exact device-and-OS combinations used for actual execution are an environment/tooling concern and are addressed in MA-EN (Environment & Tooling), not here.

## 14. Supported Device Types

| Device Type | Support Status |
|---|---|
| Android Emulator (AVD) | Supported — primary development and execution target |
| Physical Android Device (USB-connected) | Supported — used for verification and demonstration |
| Cloud-hosted / remote device farms | Not supported in this phase (see Section 27, Future Roadmap) |

Device form factor is limited to standard phone-class Android devices/emulators for this phase. Tablet-specific layout validation is not a current commitment, as the AUT's demo nature does not warrant tablet-specific verification effort in this phase.

## 15. Supported Execution Modes

| Execution Mode | Support Status |
|---|---|
| Local execution (emulator or physical device connected to the developer's machine) | Supported — the frozen baseline execution mode for this project phase |
| CI-triggered local/headless emulator execution | Supported as part of Section 23 deliverables |
| Cloud grid execution | Not supported in this phase |
| Parallel multi-device execution | Not supported in this phase |

The project's current execution commitment is local device/emulator execution, extended into CI so that execution is not solely dependent on a developer's local machine being available. Expansion into cloud and parallel execution modes is explicitly acknowledged as a natural and expected future direction (Section 27) but is not promised as part of the current baseline, consistent with the instruction that this document must not commit to implementation timelines it cannot govern.

## 16. Technology Stack Overview

The technology stack was frozen during Phase 0 and is restated here at a vision-appropriate level — identifying each tool's role in the project, without describing how the tools will be integrated or architected. That discussion is reserved for MA-FD (Framework Design).

| Layer | Tool / Technology | Role |
|---|---|---|
| Automation Engine | Appium 3.x | Cross-technology mobile automation protocol and server |
| Automation Driver | UiAutomator2 | Android-native automation driver used by Appium |
| Programming Language | Java 17 | Primary language for framework and test implementation |
| Build Tool | Gradle | Dependency management and build lifecycle |
| Test Framework | TestNG | Test execution, grouping, and lifecycle management |
| Target AUT | Sauce Labs Android Demo App v2.2.0 | Application under test |
| Version Control / Hosting | GitHub | Source hosting and public portfolio presentation |

No further technical detail — dependency versions beyond what is stated, library selections, architectural layering, or design patterns — is defined in this document. Introducing that level of detail here would violate the boundary between vision/scope and design, which this document is explicitly required to preserve.

## 17. Project Principles

These principles are the qualitative standard every later engineering decision will be measured against. They are deliberately framework-agnostic — they describe the character of the work, not its mechanics.

- **Maintainability** — The framework must remain understandable and safely modifiable by someone who did not originally write it.
- **Scalability** — The framework's structure must be able to absorb additional flows, additional documents in later series (RS, TS, TC), and eventually additional AUTs, without requiring a rewrite.
- **Readability** — Code, tests, and documentation must communicate intent clearly enough that a competent reader does not need the original author present to understand them.
- **Reusability** — Components and conventions established for this AUT should generalize, rather than being hard-coded to this specific application in ways that prevent reuse.
- **Reliability** — Automated tests must produce consistent, trustworthy results; a flaky suite is treated as a defect in the framework, not an acceptable cost of automation.
- **Simplicity** — The simplest design that satisfies the requirement is preferred over a more elaborate one; complexity must be justified, not defaulted to.
- **Clean Architecture** — Separation of concerns is treated as a governing constraint on future design work, not an optional nicety.
- **Industry Best Practices** — Decisions in later phases should be defensible by reference to how mature engineering organizations actually operate, not by convenience or personal preference alone.

These principles are aspirational commitments at this stage of the project; their concrete enforcement (coding standards, review checklists, structural rules) is the responsibility of later documents, particularly MA-FD.

## 18. Project Assumptions

| ID | Assumption |
|---|---|
| A-1 | The Sauce Labs Android Demo App will remain publicly available and functionally stable at version 2.2.0 for the duration of this project's active development. |
| A-2 | The author has, or will obtain, a suitable local development environment capable of running Android emulators and/or connecting physical Android devices. |
| A-3 | GitHub will remain the project's hosting platform for the life of this project, consistent with the "Target Repository" declared in Phase 0. |
| A-4 | The project is developed and maintained by a single primary contributor; multi-contributor collaboration workflows are not assumed for this phase. |
| A-5 | Reviewers of this project (recruiters, hiring managers, technical interviewers) will primarily engage with the GitHub repository directly, rather than through a separate hosted presentation layer. |
| A-6 ic | The AUT does not require authentication against a live backend service beyond what is bundled in the demo application itself. |

Assumptions recorded here are treated as conditions under which this document's scope and goals remain valid. If an assumption is later found to be false, the impact on scope must be assessed and, if material, this document must be revised.

## 19. Project Constraints

| ID | Constraint | Type |
|---|---|---|
| C-1 | Platform limited to Android only | Technical |
| C-2 | Driver limited to UiAutomator2 | Technical |
| C-3 | Language fixed to Java 17 | Technical |
| C-4 | Build tool fixed to Gradle | Technical |
| C-5 | Test framework fixed to TestNG | Technical |
| C-6 | Single-contributor development capacity | Resourcing |
| C-7 | No dedicated device lab or cloud grid budget in this phase | Resourcing |
| C-8 | Project must remain suitable for public disclosure (portfolio use) — no proprietary or confidential material can be introduced | Compliance |

These constraints are not treated as limitations to be lamented; they are treated as fixed inputs that later design decisions must work within, consistent with how real engineering constraints (headcount, budget, existing technology commitments) function in an enterprise setting.

## 20. Known Risks

| ID | Risk | Category |
|---|---|---|
| R-1 | Scope creep during implementation phases, expanding beyond what is defined in Section 11 | Process |
| R-2 | Emulator/device environment instability affecting perceived framework reliability | Technical |
| R-3 | AUT updates or removal from public availability, invalidating the frozen AUT version baseline | External Dependency |
| R-4 | Documentation series falling out of sync with implementation as the project progresses | Process |
| R-5 | Over-investment in documentation relative to demonstrable working automation, reducing perceived practical value to evaluators | Perception |
| R-6 | Single-contributor bandwidth limiting the pace of phase completion | Resourcing |

Risks are stated here at the identification level only. Detailed risk scoring, ownership, and tracking belong to the risk register maintained under the `10-risk-and-issue-management` documentation folder in later phases, not to this vision document.

## 21. Risk Mitigation Strategy

At a high level, the following mitigation posture applies to the risks identified in Section 20:

- **Scope creep (R-1):** Enforced through the formal scope change control process defined in Section 11.3 — no scope change without a versioned update to this document.
- **Environment instability (R-2):** Addressed through environment standardization, to be formally defined in MA-EN (Environment & Tooling), rather than left to ad hoc local setup.
- **AUT availability (R-3):** Mitigated by treating the AUT version and, where feasible, its installable artifact as something the project retains a reference to, rather than depending solely on continued public availability at build time.
- **Documentation drift (R-4):** Mitigated by the versioning and metadata discipline frozen in Phase 0 — every document carries a status and version field, making staleness visible rather than silent.
- **Over-investment in documentation (R-5):** Mitigated by the phase-gated structure itself — each documentation phase is scoped to be proportionate, and implementation phases follow directly once governing documents are baselined, rather than documentation expanding indefinitely.
- **Single-contributor bandwidth (R-6):** Mitigated by phase sequencing that allows the project to pause at a clean, coherent state after any completed and approved document, rather than requiring an all-or-nothing completion.

Detailed, owner-assigned mitigation actions are a risk-register concern, to be maintained separately as the project matures past this baseline document.

## 22. Dependencies

| Dependency | Description | Type |
|---|---|---|
| Sauce Labs Android Demo App (v2.2.0) | The application under test; all automation depends on its continued structural stability | External |
| Appium 3.x server and UiAutomator2 driver | Core automation engine dependency | External Tooling |
| Java 17 runtime | Required for framework compilation and execution | External Tooling |
| Gradle | Required for build and dependency management | External Tooling |
| TestNG | Required for test execution and lifecycle control | External Tooling |
| Android SDK / emulator tooling | Required for local Android execution environment | External Tooling |
| GitHub | Required for source hosting and public portfolio presentation | External Platform |
| Phase 0 conventions (naming, ID, versioning, metadata) | All future documents depend on these frozen standards | Internal |

This project has no dependency on any paid or proprietary internal enterprise system, consistent with the constraint (Section 19) that the project remain suitable for public, unrestricted disclosure.

## 23. Project Deliverables

### 23.1 Documentation Deliverables

| Deliverable | Document Series | Status |
|---|---|---|
| Project Vision & Scope | MA-PV | This document (Draft, pending approval) |
| AUT Analysis | MA-AA | Planned — next phase |
| Requirements Specification | MA-RS | Planned |
| Test Strategy | MA-TS | Planned |
| Framework Design | MA-FD | Planned |
| Environment & Tooling Standard | MA-EN | Planned |
| Test Plan | MA-TP | Planned |
| Test Case Specifications | MA-TC | Planned |
| Execution & Reporting Records | MA-RPT | Planned, produced continuously once execution begins |
| CI/CD Design | MA-CI | Planned |
| Risk & Issue Register | MA-RA | Planned, maintained continuously |
| Glossary & References | MA-GL | Planned, maintained continuously |

### 23.2 Technical Deliverables

| Deliverable | Description |
|---|---|
| Automation Framework (Java / Gradle / TestNG / Appium) | The implemented, documented, version-controlled automation codebase |
| Automated Test Suite | The scoped set of automated flows covering the in-scope areas defined in Section 11.1 |
| CI Pipeline | Automated build-and-test execution triggered on defined events |
| Execution Reports | Human-readable test execution reports generated per run |
| Public GitHub Repository | The consolidated, publicly accessible presentation of all of the above |

Technical deliverables are listed here only as named outcomes expected from later phases. Their design and implementation are explicitly out of scope for this document, per Section 11.2.

## 24. Acceptance Criteria

This document, MA-PV-001, is considered accepted and ready for baseline when:

- All thirty sections required by the Phase 1 objective are present and internally consistent.
- Scope boundaries (Section 11) are unambiguous enough that a later document can be checked against them without requiring clarification from the author.
- No implementation, architecture, or code content has been introduced, in compliance with the phase's stated restrictions.
- Target audience needs (Section 10) are addressed specifically enough to guide tone and structure in later, more technical documents.
- The document has been reviewed and formally approved by the designated approver, with the Approval table (top of document) updated accordingly.

The broader project (beyond this document) is considered to have satisfied Phase 1 once MA-PV-001 reaches `status: Approved` and its version is confirmed at v1.0 in both the metadata block and the filename, per the Phase 0 naming and versioning conventions.

## 25. Definition of Success

Project-level success, evaluated at completion of the full initiative (not just this document), is defined as:

- A fully governed, phase-sequenced document trail exists from MA-PV-001 through implementation-adjacent planning documents, each internally consistent with this baseline.
- A working, reliable Android automation framework exists, built against the frozen technology stack (Section 16), covering the in-scope flows defined in this document's eventual successors (MA-RS, MA-TS).
- The framework and its documentation are published to a public GitHub repository in a state a technical reviewer can assess without external explanation from the author.
- The project functions as effective, defensible evidence of enterprise-grade QA automation engineering capability to the audiences identified in Section 10.

Success is explicitly not defined by test count, and explicitly not defined by covering every possible flow in the AUT. A smaller, well-governed, well-documented, reliably executing framework satisfies this definition; a large, undocumented, brittle one does not — even if it automates more screens.

## 26. Expected Benefits

| Benefit | Beneficiary |
|---|---|
| Demonstrable evidence of enterprise SDLC discipline applied to QA automation | Author (career/portfolio value) |
| A structured, referenceable example of documentation-first automation development | QA/SDET community, students |
| A reusable governance template (naming, ID, versioning, metadata conventions) applicable to future projects | Author, future project initiatives |
| A concrete artifact reducing evaluation risk for recruiters and hiring managers | Recruiters, hiring managers |
| A framework structurally capable of extension to new flows or, eventually, new AUTs | Future maintainers of this project |

## 27. Future Roadmap

The following directions are acknowledged as plausible and desirable future extensions of this project. They are recorded here to demonstrate forward-looking architectural awareness — not as commitments, timelines, or implementation promises. Pursuing any of them will require its own scope change against this document and, where relevant, against MA-RS and MA-TS.

- **Cloud Execution** — Extending supported execution modes (Section 15) to a cloud device grid (e.g., Sauce Labs cloud, BrowserStack, or Firebase Test Lab), beyond the local-only baseline defined in this document.
- **Parallel Execution** — Introducing concurrent test execution across multiple devices or emulators to reduce total suite runtime.
- **Cross-Platform Expansion** — Extending automation coverage to iOS, which would require a driver and scope decision beyond the current Android-only, UiAutomator2-only baseline (Section 12).
- **CI/CD Enhancement** — Expanding the CI pipeline beyond basic execution-and-report triggers into more advanced release-gating or scheduled-run capability.
- **Reporting Enhancement** — Richer, more visual test execution reporting beyond the baseline reporting deliverable defined in Section 23.2.
- **Performance Improvement** — Optimizing framework execution time and resource usage as the suite grows.

None of these roadmap items are treated as implied requirements of the current baseline. Their absence from the current scope (Section 11.2) is intentional, not an oversight.

## 28. Glossary

| Term | Definition |
|---|---|
| AUT | Application Under Test — the Sauce Labs Android Demo App, version 2.2.0, in this project. |
| Appium | An open-source automation framework used to drive native, hybrid, and mobile web applications across platforms. |
| UiAutomator2 | An Appium driver used specifically for automating Android applications via Google's UI Automator framework. |
| TestNG | A Java testing framework used to structure, group, and execute automated tests. |
| Gradle | A build automation tool used to manage dependencies and the build lifecycle for the Java-based framework. |
| SDET | Software Development Engineer in Test — an engineering role focused on building test automation and quality tooling. |
| Documentation-First Development | A methodology in which governing documents (vision, requirements, design) are produced and approved before implementation begins. |
| Baseline | A formally approved version of a document that becomes the reference point for all subsequent related work. |
| Scope Creep | The uncontrolled expansion of project scope beyond what was originally defined and approved. |
| Traceability | The ability to connect an implementation artifact (e.g., a test case) back to the documented requirement or scope item that justifies its existence. |

Additional terms will be added to this glossary, or migrated to the dedicated MA-GL glossary document, as later phases introduce new domain-specific vocabulary.

## 29. References

| Reference | Purpose |
|---|---|
| Phase 0 — Project Initialization Report | Source of frozen naming, ID, versioning, and metadata conventions used throughout this document |
| Sauce Labs Android Demo App (public repository/distribution) | The reference AUT this project automates against, version 2.2.0 |
| Appium official documentation | Authoritative reference for Appium 3.x capabilities, referenced conceptually in Section 16 |
| UiAutomator2 driver documentation | Authoritative reference for the automation driver named in Section 16 |

No proprietary, confidential, or internally-restricted references are used in this document, consistent with the project's public-portfolio constraint (Section 19, C-8).

## 30. Conclusion

This document establishes, in full, the reason the Mobile Automation Framework project exists, who it is built for, and where its boundaries lie. It commits the project to a documentation-first methodology, freezes the technology stack and platform scope inherited from Phase 0, and defines success in terms that later phases can be objectively measured against.

Nothing in this document authorizes implementation work. Its sole purpose is to remove ambiguity about direction and scope before that work begins. With this baseline approved, the project is ready to proceed to MA-AA-001 (AUT Analysis), where the Sauce Labs Android Demo App will be examined in structural detail — strictly within the boundaries this document has now defined.

---

**End of Document — MA-PV-001, v1.0**

PROJECT_VISION_BASELINED
