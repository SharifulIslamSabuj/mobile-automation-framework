---
document_id: MA-TS-001
title: Test Strategy
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
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001]
classification: Internal
---

# MA-TS-001 — Test Strategy

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-TS-001 |
| Document Name | Test Strategy |
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
| v0.1 | 2026-07-29 | Project Owner | Initial draft derived from MA-PV-001, MA-AA-001, MA-RS-001 |
| v1.0 | 2026-07-29 | Project Owner | First baseline version submitted for approval |

---

## 1. Purpose

This document defines how testing will be approached for the requirements baselined in [MA-RS-001], within the scope and constraints frozen in [MA-PV-001], against the AUT behavior observed in [MA-AA-001]. It governs testing approach only — it does not define framework structure, code organization, or implementation. Those decisions belong to MA-FA-001 (Framework Architecture).

## 2. Scope

This strategy covers the 31 functional requirements (FR-001–FR-031) and 12 non-functional requirements (NFR-001–NFR-012) defined in MA-RS-001, across the flows confirmed in MA-AA-001: Application Launch, Authentication, Product Browsing, Product Details, Cart, Checkout (Shipping, Payment), Order Review, Order Placement, Navigation, and State Management. Items marked "Out of Current Observation" in MA-RS-001 are addressed under Risk-Based Testing Strategy (Section 8) rather than treated as confirmed scope. Exclusions follow MA-RS-001 Section 11 without modification.

## 3. Testing Objectives

- Confirm each Must- and Should-priority functional requirement in MA-RS-001 is verifiable through UI-level test execution.
- Confirm non-functional expectations (execution stability, reporting, logging, configuration, cross-device support) are addressed at the strategy level before implementation begins.
- Resolve, through targeted verification, the behaviors MA-AA-001 could not confirm from screenshots alone, before automation of the affected requirement proceeds.
- Establish a testing approach that remains valid if the framework is later pointed at a different AUT, consistent with the reusability objective in MA-PV-001 Section 7.

## 4. Test Levels

| Level | Applicability |
|---|---|
| UI-level functional testing | Primary and only test level in scope, consistent with MA-RS-001 Section 11 exclusions |
| Unit / integration testing of the AUT | Out of scope — AUT is a fixed, unmodifiable external application (MA-PV-001 Section 11.2) |
| API / backend testing | Out of scope (MA-RS-001 Section 11) |
| Performance / load / security testing | Out of scope for this phase (MA-RS-001 Section 11; NFR-010 deferred) |

## 5. Test Types

| Test Type | Role in This Project |
|---|---|
| Functional Testing | Primary type — validates FR-001–FR-031 behavior |
| Regression Testing | Repeated execution of the same suite to confirm NFR-005 (Execution Stability) |
| Smoke Testing | A minimal subset (Application Launch, Catalog display, Cart access) used to confirm build/environment health before full execution |
| Exploratory Testing (manual, non-automated) | Used only to resolve Out of Current Observation items prior to automating the affected requirement |

Non-functional test types not listed above (performance, security, visual regression) are explicitly excluded, consistent with MA-RS-001 Section 11.

## 6. Automation Scope

All Must- and Should-priority requirements in MA-RS-001 are in automation scope. FR-030 (Error Handling Coverage, priority Could) is excluded from initial automation scope — MA-RS-001 records no verifiable error state to automate against. Requirements with partial observation (FR-004, FR-006, FR-011, FR-012, FR-018, FR-023, FR-026, FR-028) are in scope, but their automation is sequenced after the manual verification step described in Section 8, so automation logic is not built against an unconfirmed assumption.

## 7. Test Prioritization Strategy

Automation execution order follows the priority field already assigned in MA-RS-001 directly — no re-derivation:

1. **Must** — Application Launch, Authentication (entry), Product Browsing core, Product Details core, full Cart flow, full Checkout flow, Order Placement, Back Navigation, State Management (12 of 12 FR categories contain at least one Must item).
2. **Should** — Product Sort, Color Selection, Login Outcome Verification, Navigation Drawer item access, Logging (NFR-007), Data Management (NFR-011).
3. **Could** — Error Handling (FR-030), Performance Expectations (NFR-010).

This order ensures the core purchase journey (launch through order confirmation) is automated and stable before secondary interactions are added.

## 8. Risk-Based Testing Strategy

Requirements inherited from MA-RS-001 as "Out of Current Observation" carry higher implementation risk because their exact UI behavior is unconfirmed. These are treated as elevated-risk items and require a manual verification pass in Appium Inspector before automation logic is written against them:

| Elevated-Risk Item | Related Requirement |
|---|---|
| Sequence of Login relative to Proceed to Checkout | FR-018 |
| Destination screen after Login | FR-004 |
| Destination/state change after Add to Cart | FR-012 |
| Screen hosting the Place Order trigger | FR-026 |
| Drawer item destinations beyond Catalog/Login | FR-028 |
| Sort control resulting behavior | FR-006 |

This reduces the general risks already logged in MA-PV-001 Section 20 (R-1 Scope Creep, R-2 Environment Instability) by preventing automation work from being built on an unverified assumption, which would otherwise need rework.

## 9. Functional Coverage Strategy

Coverage is mapped one-to-one to the 12 functional categories defined in MA-RS-001 Section 6 (Application Launch through State Management). Every FR under a category receives at least one corresponding verification point at UI level. Where multiple FRs share a screen (for example, FR-014–FR-018 on the Cart screen), coverage is planned per requirement, not per screen, so a single screen containing several requirements does not collapse into a single test.

## 10. Non-Functional Considerations

| NFR | Strategy-Level Treatment |
|---|---|
| NFR-001–NFR-004 (Maintainability, Scalability, Reusability, Readability) | Influence test design discipline (one verification point per requirement, no duplicated checks); structural enforcement is deferred to MA-FA-001 |
| NFR-005 (Execution Stability) | Addressed through the Synchronization Strategy (Section 15) and Regression Testing (Section 5) |
| NFR-006–NFR-007 (Reporting, Logging) | Addressed in Sections 16–17 |
| NFR-008 (Configuration) | Test environment values must be externally supplied, per Section 11 |
| NFR-009 (Cross-Device Support) | Addressed in Device Strategy (Section 12) |
| NFR-010 (Performance) | Deferred — Could priority, no benchmark defined at this stage |
| NFR-011 (Data Management) | Addressed in Test Data Strategy (Section 13) |
| NFR-012 (Environment Independence) | Enforced by restricting all tooling choices to those already frozen in MA-PV-001 Section 16 |

## 11. Test Environment Strategy

Testing is performed locally only, consistent with the execution modes frozen in MA-PV-001 Section 15 — no cloud device grid in this phase. Environment-specific values (device target, AUT install source) must be externally configurable rather than embedded in test logic, satisfying NFR-008; the mechanism for supplying configuration is a framework design concern and is deferred to MA-FA-001.

## 12. Device Strategy

| Device Type | Role |
|---|---|
| Android Emulator (AVD) | Primary execution target during active test development |
| Physical Android Device (USB-connected) | Used for verification runs, per MA-PV-001 Section 14 |
| Android OS Range | API 29 and above, per MA-PV-001 Section 13 |
| Cloud / remote devices | Out of scope this phase (MA-PV-001 Section 15) |

Parallel or multi-device execution is not part of this strategy; it is listed only as a future roadmap item in MA-PV-001 Section 27.

## 13. Test Data Strategy (High Level)

Test data required by the in-scope flows: Login credentials (sample credentials observed in MA-AA-001), Shipping Address values (Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, Country), and Payment card details (fields not individually confirmed — see MA-RS-001 FR-023). Per NFR-011, these values must be supplied externally to test logic rather than hardcoded. The specific storage mechanism for this data is a framework design decision and is out of scope for this document.

## 14. Locator Strategy (High Level)

MA-AA-001 explicitly did not capture resource IDs, accessibility IDs, or XML hierarchy (Known Limitations, Section 14). This strategy therefore states locator priority in principle only, pending element inspection during framework design:

1. Resource ID / Accessibility ID (preferred, when present)
2. Stable, semantically meaningful attributes (e.g., content-desc)
3. XPath (fallback only, minimized due to fragility)

Concrete locator values and any wrapper mechanism are deferred to MA-FA-001.

## 15. Synchronization Strategy (High Level)

Waiting logic must be tied to observable state changes already documented in MA-RS-001 (e.g., cart badge update per FR-007, cart total recalculation per FR-017, screen transitions per FR-009/FR-014/FR-019/FR-022/FR-025/FR-027) rather than fixed-duration delays. This is a strategy commitment only; the specific wait implementation belongs to MA-FA-001.

## 16. Logging Strategy

Per NFR-007, every test execution must produce a log sufficient to identify the failing step without requiring re-execution to diagnose it. Logging scope is limited to test execution activity (step, action, outcome); it does not extend to AUT-internal diagnostics, which are outside this project's access. Logging implementation (library, format) is deferred to MA-FA-001.

## 17. Reporting Strategy

Per NFR-006, each execution must produce a human-readable report reflecting pass/fail status per requirement-mapped test, organized by the functional categories in Section 9. Reports must be accessible without inspecting raw logs. Report tooling and CI integration are deferred to MA-FA-001 and the future MA-CI document.

## 18. Defect Management Strategy

Consistent with the single-contributor constraint recorded in MA-PV-001 (Section 19, C-6), defect management is lightweight: failures identified during execution are logged against the project's risk and issue register (future MA-RA series) rather than a dedicated external defect-tracking tool. Each logged defect must reference the failing requirement ID (FR-xxx/NFR-xxx) for traceability.

## 19. Entry Criteria

- MA-RS-001 is approved and baselined.
- MA-AA-001 remains the unmodified evidence baseline for this strategy.
- Environment dependencies listed in MA-RS-001 Section 10 (Appium Server, UiAutomator2, Android SDK, ADB, emulator/device) are available and functional.
- The AUT (v2.2.0) is installable and launchable in the target environment.

## 20. Exit Criteria

- All Must-priority requirements from MA-RS-001 have an executed, passing verification point.
- Elevated-risk items in Section 8 have been resolved through manual verification and their MA-RS-001 status updated accordingly.
- NFR-005 (Execution Stability) is demonstrated through consistent results across repeated runs.
- Reporting (Section 17) and Logging (Section 16) are operational for every executed run.
- No open Must-priority defect remains unresolved in the issue register.

## 21. Risks

| Risk | Source |
|---|---|
| Scope creep during automation implementation | MA-PV-001 Section 20, R-1 |
| Emulator/device environment instability | MA-PV-001 Section 20, R-2 |
| Automation built against an unconfirmed UI assumption | New — mitigated by Section 8 |
| Manual verification pass delays automation start for elevated-risk items | New — accepted trade-off in exchange for reliability |

## 22. Assumptions

- The AUT remains available and stable at v2.2.0 for the duration of test execution (MA-PV-001, A-1).
- A suitable local device/emulator environment is available (MA-PV-001, A-2).
- The AUT does not require authentication against a live backend beyond what is bundled in the demo app (MA-PV-001, A-6).
- Manual exploratory verification will successfully resolve each Out of Current Observation item listed in Section 8 before or during automation of the affected requirement.

## 23. Deliverables

This strategy produces no new deliverable list — it governs execution of the deliverables already committed in MA-PV-001 Section 23.1 (Test Plan, Test Case Specifications, Execution & Reporting Records) and Section 23.2 (Automated Test Suite, Execution Reports).

## 24. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

**End of Document — MA-TS-001, v1.0**
