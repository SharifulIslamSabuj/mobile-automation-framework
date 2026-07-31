---
document_id: MA-TP-001
title: Master Test Plan
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
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001]
classification: Internal
---

# MA-TP-001 — Master Test Plan

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-TP-001 |
| Document Name | Master Test Plan |
| Version | v1.0 |
| Status | Draft |
| Project | Mobile Automation Framework |
| Project Code | MA |
| AUT | Sauce Labs Android Demo App |
| AUT Version | 2.2.0 |
| Platform | Android |
| Classification | Internal |

---

## Version History

| Version | Date | Author | Change Description |
|---|---|---|---|
| v0.1 | 2026-07-29 | Project Owner | Initial draft derived from MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001 |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |

---

## 1. Purpose

This document defines the execution plan for testing the Mobile Automation Framework: what will be executed, in what order, under what entry/exit conditions, and with what evidence produced. It governs execution only. It does not define requirements ([MA-RS-001]), testing approach ([MA-TS-001]), or framework structure ([MA-FA-001]) — it assumes those documents as approved input and schedules their execution.

## 2. Scope

Execution scope covers the 31 functional requirements and 12 non-functional requirements baselined in MA-RS-001, executed per the automation scope defined in MA-TS-001 Section 6. Out-of-scope items are inherited unchanged from MA-PV-001 Section 11.2 and MA-RS-001 Section 11 — this document introduces no new scope.

## 3. Referenced Documents

| Document ID | Title | Role in This Plan |
|---|---|---|
| MA-PV-001 | Project Vision & Scope | Source of business objectives, scope boundaries, assumptions, constraints |
| MA-AA-001 | AUT Analysis | Source of confirmed AUT screens and behavior |
| MA-RS-001 | Requirements Specification | Source of functional/non-functional requirements being executed |
| MA-TS-001 | Test Strategy | Source of test types, prioritization, and entry/exit criteria this plan schedules |
| MA-FA-001 | Framework Architecture | Source of the structural design the executed automation is built on |

## 4. Objectives

- Execute and pass all Must-priority requirements defined in MA-RS-001.
- Confirm the elevated-risk items identified in MA-TS-001 Section 8 are resolved prior to or during execution of their corresponding requirement.
- Validate non-functional expectations (execution stability, reporting, logging) under real execution conditions.
- Produce a traceable, evidence-backed execution record suitable as the project's completion baseline.

## 5. Test Items

| Module | Description | Requirement Source |
|---|---|---|
| Application Launch | App startup and Catalog display | MA-RS-001 §6.1 |
| Authentication | Login access, credential entry, outcome | MA-RS-001 §6.2 |
| Product Browsing | Catalog display, sort, cart badge, drawer access | MA-RS-001 §6.3 |
| Product Details | Detail view, color/quantity selection, add to cart, scroll | MA-RS-001 §6.4 |
| Cart | Item display, quantity update, removal, total, checkout trigger | MA-RS-001 §6.5 |
| Checkout — Shipping | Shipping Address form and navigation | MA-RS-001 §6.6 |
| Checkout — Payment | Payment Method entry and navigation | MA-RS-001 §6.7 |
| Order Review | Order summary verification | MA-RS-001 §6.8 |
| Order Placement | Place Order trigger and completion confirmation | MA-RS-001 §6.9 |
| Navigation | Drawer item access, back navigation | MA-RS-001 §6.10 |
| State Management | Dynamic element state verification (badge, quantity, total, color) | MA-RS-001 §6.12 |

Error Handling (MA-RS-001 §6.11, FR-030) is a test item of record but is excluded from execution per the automation scope decision in MA-TS-001 Section 6.

## 6. Items Not Tested

Per MA-PV-001 Section 11.2 and MA-RS-001 Section 11, unchanged: iOS/cross-platform automation, cloud device farm execution, performance/load/security testing, API/backend testing, exhaustive regression coverage, visual regression testing, any modification of the AUT, and support for applications other than the named AUT.

## 7. Test Deliverables

| Deliverable | Reference |
|---|---|
| Automated Test Suite execution | MA-PV-001 §23.2 |
| Execution Report (per run) | MA-TS-001 §17, MA-FA-001 §15 |
| Test Summary (aggregated across requirement categories) | MA-RS-001 §6 categories |
| Failure Screenshots | MA-FA-001 §17 |
| Execution Logs | MA-TS-001 §16, MA-FA-001 §16 |

## 8. Test Environment

Execution occurs locally only — Android Emulator and physical Android device, per MA-PV-001 Section 15 and MA-TS-001 Section 12. No cloud device grid is used in this phase. Environment target resolution follows the Configuration Architecture defined in MA-FA-001 Section 10; this document does not restate or alter that mechanism.

## 9. Required Test Data

Test data required for execution — login credentials, shipping address values, payment details — is as defined in MA-RS-001 Section 10 and MA-TS-001 Section 13, supplied externally to test logic per NFR-011. No new data requirement is introduced by this plan.

## 10. Test Execution Approach

Execution proceeds in four sequential stages:

```
Smoke
  ↓
Functional
  ↓
Regression
  ↓
Final Verification
```

- **Smoke** — minimal subset (Application Launch, Catalog display, Cart access) confirming environment and build health, per MA-TS-001 Section 5.
- **Functional** — full execution of all in-scope requirements from MA-RS-001, ordered by priority per MA-TS-001 Section 7.
- **Regression** — repeated execution of the same suite to confirm NFR-005 (Execution Stability).
- **Final Verification** — confirmation that Exit Criteria (Section 12) are met before the execution phase is baselined.

## 11. Entry Criteria

Inherited from MA-TS-001 Section 19, with one addition specific to execution scheduling:

- MA-RS-001, MA-TS-001, and MA-FA-001 are approved and baselined.
- Environment dependencies (Section 20) are available and functional.
- The AUT (v2.2.0) is installable and launchable in the target environment.
- Framework implementation of the in-scope test items (Section 5) is complete, per MA-FA-001.

## 12. Exit Criteria

Inherited from MA-TS-001 Section 20 without modification: all Must-priority requirements executed and passing; elevated-risk items resolved; execution stability demonstrated across repeated runs; reporting and logging operational for every run; no open Must-priority defect.

## 13. Suspension Criteria

Execution is suspended if any of the following occur:

- The test environment (emulator or physical device) becomes unavailable or unstable to a degree that execution results cannot be trusted.
- The AUT becomes unreachable, uninstallable, or crashes in a way that blocks continued execution of in-scope test items.
- A defect is identified that blocks execution of a Must-priority requirement with no viable workaround.

## 14. Resumption Criteria

Execution resumes once the condition that caused suspension is resolved: environment restored and verified stable, AUT reachable and launchable again, or the blocking defect resolved and the affected requirement re-verified as executable.

## 15. Pass / Fail Criteria

A test item is marked **Pass** when its acceptance criteria, as documented in MA-RS-001, is met during execution. It is marked **Fail** otherwise. The execution phase as a whole is considered complete only when the Exit Criteria in Section 12 are satisfied.

## 16. Defect Management Approach

Consistent with MA-TS-001 Section 18: defects identified during execution are logged against the project's risk and issue register, each referencing the failing requirement ID (FR-xxx/NFR-xxx). No new defect-management process is introduced by this plan.

## 17. Roles and Responsibilities

| Role | Responsibility |
|---|---|
| Project Owner | Prepares the plan, executes tests, logs defects |
| Reviewer | Reviews execution plan and results for consistency with MA-RS-001/MA-TS-001 |
| Approver | Approves the plan prior to execution and the results at Exit Criteria |

Consistent with the single-contributor constraint in MA-PV-001 (Section 19, C-6), these roles may be held by the same individual at different gates; they remain conceptually distinct for governance traceability.

## 18. Assumptions

Inherited from MA-PV-001 Section 18 (A-1, A-2, A-4, A-6) and MA-TS-001 Section 22: the AUT remains stable at v2.2.0 for the duration of execution; a suitable local device/emulator environment is available; a single contributor performs execution; the AUT requires no live backend authentication beyond what is bundled; manual verification will have resolved the elevated-risk items in MA-TS-001 Section 8 prior to execution of the affected requirement.

## 19. Risks

Inherited from MA-PV-001 Section 20 (R-1 Scope Creep, R-2 Environment Instability) and MA-TS-001 Section 21, plus one execution-specific addition:

| Risk | Source |
|---|---|
| Scope creep during execution | MA-PV-001, R-1 |
| Emulator/device environment instability | MA-PV-001, R-2 |
| Automation built against an unconfirmed UI assumption | MA-TS-001 §21 |
| Execution start delayed by incomplete framework implementation against MA-FA-001 | New — execution-specific |

## 20. Dependencies

Inherited from MA-RS-001 Section 10 without modification: Sauce Labs Android Demo App v2.2.0, Appium 3.x Server, UiAutomator2 Driver, Java 17 Runtime, Gradle, TestNG, Android SDK/Emulator Tooling, ADB, Real Android Device, GitHub.

## 21. Milestones

```
Planning
  ↓
Strategy & Architecture
  ↓
Implementation
  ↓
Test Execution
  ↓
Baseline
```

- **Planning** — MA-PV-001, MA-AA-001, MA-RS-001.
- **Strategy & Architecture** — MA-TS-001, MA-FA-001.
- **Implementation** — framework build per MA-FA-001 (separate phase, not covered by this document).
- **Test Execution** — governed by this document (MA-TP-001).
- **Baseline** — Exit Criteria met and execution results formally approved.

## 22. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

**End of Document — MA-TP-001, v1.0**
