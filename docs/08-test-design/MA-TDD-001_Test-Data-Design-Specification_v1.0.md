---
document_id: MA-TDD-001
title: Test Data Design Specification
version: v1.0
status: Approved
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-07-29
reviewed_by: Project Owner / Repository Maintainer
approved_by: Project Owner / Repository Maintainer
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001, MA-TD-001, MA-TC-001]
classification: Internal
---

# MA-TDD-001 — Test Data Design Specification

**Mobile Automation Framework — Automation-Ready Enterprise Edition**

| Field | Value |
|---|---|
| Document ID | MA-TDD-001 |
| Document Name | Test Data Design Specification |
| Version | v1.0 |
| Status | Approved |
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
| v0.1 | 2026-07-29 | Project Owner | Initial draft — submitted for review; not approved; not baselined |
| v0.2 | 2026-07-29 | Project Owner | Upgraded to Automation-Ready Enterprise structure: expanded dataset/field metadata, dummy Shipping values, Payment known/unknown breakdown, Automation Consumption Guide, Data Lifecycle, Owner Matrix, Data Source Matrix, Automation File Mapping, reserved future dataset IDs |
| v1.0 | 2026-07-29 | Project Owner | Reviewed and approved; baselined as the official Test Data baseline for automation implementation |

---

## 1. Purpose

This document defines every reusable test data set required to execute the 32 approved Test Cases in MA-TC-001, structured so an Automation Engineer can implement automation directly from it without creating additional test data. It does not modify, reinterpret, or add scope to any previously approved document. The v0.2 upgrade improved structure, metadata completeness, and automation readiness relative to v0.1 without changing which application behavior is claimed to exist; v1.0 baselines that content following review and approval.

## 2. Scope

Scope remains unchanged from v0.1: Authentication Data, Shipping Address Data, and Payment Data are the only categories with evidenced need (MA-TS-001 §13; MA-FA-001 §11). The v0.2 upgrade allowed dummy/placeholder/sandbox VALUES that were not permitted in v0.1, but did not permit new claims about application BEHAVIOR, fields, or validation rules beyond what MA-AA-001, MA-RS-001, and MA-TC-001 already establish.

## 3. Referenced Documents

| Document ID | Title | Role in This Document |
|---|---|---|
| MA-PV-001 | Project Vision & Scope | Scope boundaries; public-portfolio / no-real-PII constraint |
| MA-AA-001 | AUT Analysis | Priority evidence — observed fields, screens, sample-credentials presence |
| MA-RS-001 | Requirements Specification | FR-003, FR-004, FR-019, FR-020, FR-021, FR-023, FR-025 acceptance criteria |
| MA-TS-001 | Test Strategy | §13 Test Data Strategy — confirms exactly three data categories |
| MA-FA-001 | Framework Architecture | §11 Test Data Architecture; §10 Configuration vs. Test Data boundary |
| MA-TP-001 | Master Test Plan | §9 Required Test Data |
| MA-TD-001 | Test Design Specification | Scenario preconditions that consume data |
| MA-TC-001 | Test Case Specification | The 32 Test Cases and their Test Data fields |

## 4. Test Data Design Principles

- **Reusability**: Each dataset is defined once and referenced by every Test Case that needs it.
- **Maintainability**: Each dataset has a single owning Test Data ID; a value change is made in one place.
- **Minimal Duplication**: Exactly three active datasets exist, matching MA-TS-001 §13 and MA-FA-001 §11.
- **Automation Readiness**: Every field carries type, sample value, and automation-ready flag so a script can bind to it directly.
- **Environment Independence**: No dataset encodes a device/emulator/environment value (MA-FA-001 §10 owns that).
- **Unique Identifiers Where Necessary**: No approved requirement documents a uniqueness constraint; none is introduced.
- **Data Isolation**: Each dataset is scoped to its own module and does not share fields across datasets.
- **Future Scalability**: TD-004 through TD-010 are reserved (Section 15) without renumbering existing IDs.
- **One Source of Truth**: This document is the sole source for test data values.

## 5. Naming Convention

Test Data ID pattern: `TD-<3-digit sequence>`, independent of FR/TS/TC numbering. Data Set Name uses Title Case. Field Name is taken verbatim from the field label observed in MA-AA-001 wherever a field is confirmed; candidate/unconfirmed field names are explicitly labeled as such and never presented as confirmed.

## 6. Data Classification

| Category | Included? | Justification |
|---|---|---|
| Authentication Data | Included | FR-003, FR-004; MA-TS-001 §13; MA-FA-001 §11 |
| Shipping Address Data | Included | FR-019, FR-020; MA-TS-001 §13; MA-FA-001 §11 |
| Payment Data | Included | FR-023; MA-TS-001 §13; MA-FA-001 §11 |
| Product Selection Data | Excluded | TC-009 accepts any product card; no specific identity required |
| Quantity Data | Excluded | Exact mechanics Out of Current Observation (FR-011); no specific value required |
| Color Selection Data | Excluded | No specific color name documented in MA-AA-001 |
| Environment Data | Excluded | Owned by Configuration Architecture (MA-FA-001 §10) |
| Navigation Data | Excluded | Drawer item list is fixed UI structure (MA-AA-001 §6), not variable data |
| Dynamic State Verification Data | Excluded | TC-007/031/032 observe generated state; no input consumed |
| Boundary Data | Excluded | No boundary condition documented; performance/limits out of scope (MA-PV-001 §11.2) |
| Negative Data | Excluded (see §8.1.2 for itemized sub-cases) | FR-030 deferred and excluded from execution scope (MA-TS-001 §6) |
| Invalid Data | Excluded | Same basis as Negative Data |
| Optional Data | Excluded | No field documented as optional in MA-AA-001 or MA-RS-001 |
| Future Reserved Data | Reserved — Section 15 | IDs reserved, not populated |

## 7. Dataset and Field Metadata Standard

Every reusable dataset in Section 8 carries the following dataset-level metadata: Dataset ID, Purpose, Owner, Status, Priority, Reusable, Scope, Data Source, Maintenance Owner, Version, Review Status, Approval Status. Every field within a dataset carries: Field Name, Description, Sample Value, Allowed Type, Required/Optional, Known Validation Rule, Source Document, Automation Ready, Manual Verification Needed, Notes. This standard is defined once here and applied identically to every dataset that follows.

## 8. Reusable Test Data Sets

### 8.1 TD-001 — Authentication Dataset

| Field | Value |
|---|---|
| Dataset ID | TD-001 |
| Purpose | Supplies Username and Password for Login Credential Entry and Login Outcome Verification |
| Owner | Project Owner / Repository Maintainer |
| Status | Approved |
| Priority | Must (inherited from FR-003) |
| Reusable | Yes — referenced by TC-003 and TC-004 |
| Scope | Authentication (MA-RS-001 §6.2) |
| Data Source | MA-AA-001 (Login screen observation) |
| Maintenance Owner | Project Owner / Repository Maintainer |
| Version | v1.0 |
| Review Status | Reviewed |
| Approval Status | Approved |

#### 8.1.1 Positive Login Dataset

| Field Name | Description | Sample Value | Allowed Type | Required / Optional | Known Validation Rule |
|---|---|---|---|---|---|
| Username | Login username value entered into the Username field | Pending Manual Verification | String | Required | Pending Manual Verification |
| Password | Login password value entered into the Password field | Pending Manual Verification | String | Required | Pending Manual Verification |

| Field Name | Source Document | Automation Ready | Manual Verification Needed | Notes |
|---|---|---|---|---|
| Username | MA-AA-001 (sample credentials observed on-screen, text not transcribed) | Pending | Yes — read from Login screen | Do not fabricate; AUT displays the real value on-screen |
| Password | MA-AA-001 (sample credentials observed on-screen, text not transcribed) | Pending | Yes — read from Login screen | Do not fabricate; AUT displays the real value on-screen |

**Automation Consumption:** TC-003 Steps 1–2 (entry), TC-004 Step 1 (resubmission). Automation Readiness for this sub-dataset: Pending — resolved via the manual verification pass already committed to in MA-TS-001 §8, expected to require only a direct on-screen read (MA-AA-001 confirms the value is displayed, not hidden).

#### 8.1.2 Negative Login Dataset

The following negative-authentication variants were evaluated against the approved documentation baseline. None is supported by any approved requirement, so all are marked Out of Scope rather than silently omitted.

| Negative Variant | Status | Justification |
|---|---|---|
| Invalid Username | Out of Scope | No FR exercises an invalid-credential path; FR-030 (the only requirement that could cover this) is deferred and excluded from execution scope (MA-TS-001 §6) |
| Invalid Password | Out of Scope | Same basis as Invalid Username |
| Blank Username | Out of Scope | No FR documents required-field validation behavior for Login; MA-AA-001 never observed a blank-field validation message |
| Blank Password | Out of Scope | Same basis as Blank Username |
| Locked Account | Out of Scope | Account lifecycle/locking is not documented anywhere in MA-AA-001 or MA-RS-001; no evidence this AUT models account states at all |
| Inactive Account | Out of Scope | Same basis as Locked Account |

### 8.2 TD-002 — Shipping Address Dataset

| Field | Value |
|---|---|
| Dataset ID | TD-002 |
| Purpose | Supplies values for the seven confirmed Shipping Address fields |
| Owner | Project Owner / Repository Maintainer |
| Status | Approved |
| Priority | Must (inherited from FR-020) |
| Reusable | Yes — referenced by TC-019 (context), TC-020 (primary), TC-021 (context), TC-025 (comparison) |
| Scope | Checkout — Shipping (MA-RS-001 §6.6) |
| Data Source | MA-AA-001 (field observation) + this document (fictitious dummy values) |
| Maintenance Owner | Project Owner / Repository Maintainer |
| Version | v1.0 |
| Review Status | Reviewed |
| Approval Status | Approved |

#### 8.2.1 Shipping Address — Reusable Dummy Dataset

Values below are fictitious QA test data constructed for this document. They do not correspond to any real person, business, or address, and must never be replaced with production or personal data.

| Field Name | Description | Sample Value | Allowed Type | Required / Optional | Known Validation Rule |
|---|---|---|---|---|---|
| Full Name | Recipient full name | Rahim Test Uddin | String | Required | Pending Manual Verification |
| Address Line 1 | Primary street address | House 45, Road 7 | String | Required | Pending Manual Verification |
| Address Line 2 | Secondary address line | Sector 10, Uttara | String | Required vs. Optional not documented | Pending Manual Verification |
| City | City name | Dhaka | String | Required | Pending Manual Verification |
| State/Region | State or region | Dhaka Division | String | Required | Pending Manual Verification |
| Zip Code | Postal code | 1230 | String / Numeric | Required | Pending Manual Verification |
| Country | Country name | Bangladesh | String | Required | Pending Manual Verification |

| Field Name | Source Document | Automation Ready | Manual Verification Needed | Notes |
|---|---|---|---|---|
| Full Name | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Optional (validation rule) | Reusable across all Shipping-consuming Test Cases |
| Address Line 1 | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Optional (validation rule) | — |
| Address Line 2 | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Yes (required/optional status) | Field exists per MA-AA-001; whether it is mandatory is unconfirmed |
| City | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Optional (validation rule) | — |
| State/Region | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Optional (validation rule) | — |
| Zip Code | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Optional (validation rule) | Type shown as String/Numeric because format constraint is unconfirmed |
| Country | MA-AA-001 (field confirmed); value is fictitious dummy data | Ready | No (value); Optional (validation rule) | Free-text assumed; may be a dropdown — unconfirmed |

**Automation Consumption:** TC-020 Steps 1–7 (direct entry, in field order above). TC-019 and TC-021 use this dataset as execution context only (no field-level consumption). TC-025 uses this dataset to verify Review Order summary consistency. Automation Readiness for this dataset: Ready — every field has a usable sample value; only the (non-blocking) validation-rule detail remains open.

### 8.3 TD-003 — Payment Card Dataset

| Field | Value |
|---|---|
| Dataset ID | TD-003 |
| Purpose | Supplies card entry value(s) for Payment Card Data Entry |
| Owner | Project Owner / Repository Maintainer |
| Status | Approved (dataset record); Content Blocked pending verification |
| Priority | Must (inherited from FR-023) |
| Reusable | Yes — single dataset for all Payment-stage consumers, once resolved |
| Scope | Checkout — Payment (MA-RS-001 §6.7) |
| Data Source | MA-AA-001 (generic "card entry screen" observation only) |
| Maintenance Owner | Project Owner / Repository Maintainer |
| Version | v1.0 |
| Review Status | Reviewed |
| Approval Status | Approved (as a Pending-structure record) |

#### 8.3.1 What Is Known

- A "Payment Method" screen exists and follows the Shipping Address screen (MA-AA-001, MA-RS-001 FR-022).
- The screen displays "Credit/Debit card information" and a generic "card entry screen" (MA-AA-001).
- Card entry field(s) accept input in principle (MA-RS-001 FR-023 acceptance criteria).
- A "Continue" action navigates from this screen to Review Order (MA-RS-001 FR-024).

#### 8.3.2 What Is Unknown

- The exact number of card entry fields on the screen.
- The identity/labels of each field (e.g., whether card number, expiry, CVV, and cardholder name are separate fields or combined).
- Any format or validation rule applied to card input (length, numeric-only, Luhn check, expiry format).
- Whether the AUT performs any real validation at all, given it is a demo application.

#### 8.3.3 What Must Be Manually Verified

The actual field structure of the Payment Method screen must be inspected via Appium Inspector page source during the manual verification pass already committed to in MA-TS-001 §8. Until that inspection occurs, this dataset cannot be finalized into named, confirmed fields.

#### 8.3.4 Candidate Field Structure (NOT CONFIRMED — Verification Checklist Only)

> The fields below are industry-typical for payment forms in general. They are **NOT confirmed** to exist in this AUT. They are provided only as a starting checklist for the manual verification pass — do not treat any row as documented application behavior.

| Candidate Field (Unconfirmed) | Illustrative Sandbox Value (Unconfirmed Applicability) | Basis |
|---|---|---|
| Card Holder Name | Rahim Test Uddin (reuse of TD-002 Full Name persona, if a name field exists) | Not observed — candidate only |
| Card Number | 4111 1111 1111 1111 (globally standard industry test-card number; not confirmed accepted by this AUT) | Not observed — candidate only |
| Expiry Date | 12/2030 (illustrative future date) | Not observed — candidate only |
| CVV | 123 (illustrative) | Not observed — candidate only |
| Postal Code | 1230 (reuse of TD-002 Zip Code, if a field exists) | Not observed — candidate only |

| Field | Value |
|---|---|
| Automation Ready | Blocked |
| Manual Verification Needed | Yes — full field structure, then values and validation rules |
| Notes | No field name above is asserted as confirmed; this table exists solely to accelerate the manual verification pass, per the explicit "dummy/sandbox values are allowed" permission for this upgrade, while still complying with "do not invent confirmed field names" |

**Automation Consumption:** TC-023 Step 1 cannot be automated against a named field yet; TC-022, TC-024, TC-025 surround this stage contextually. Automation Readiness for this dataset: Blocked.

## 9. Complete Test Data Catalog

| Test Data ID | Field Name | Sample Value | Automation Ready | Manual Verification Needed |
|---|---|---|---|---|
| TD-001 | Username | Pending Manual Verification | Pending | Yes |
| TD-001 | Password | Pending Manual Verification | Pending | Yes |
| TD-002 | Full Name | Rahim Test Uddin | Ready | No |
| TD-002 | Address Line 1 | House 45, Road 7 | Ready | No |
| TD-002 | Address Line 2 | Sector 10, Uttara | Ready | No |
| TD-002 | City | Dhaka | Ready | No |
| TD-002 | State/Region | Dhaka Division | Ready | No |
| TD-002 | Zip Code | 1230 | Ready | No |
| TD-002 | Country | Bangladesh | Ready | No |
| TD-003 | Card Entry Field(s) — structure not confirmed | Pending Manual Verification | Blocked | Yes |

## 10. Automation Consumption Guide

This section maps every consuming Test Case to its dataset and fields, so an Automation Engineer can wire test data without re-deriving it from MA-TC-001.

| Test Case | Dataset | Fields Consumed | Consumption Type |
|---|---|---|---|
| TC-003 | TD-001 | Username, Password | Direct Entry |
| TC-004 | TD-001 | Username, Password | Direct Entry (resubmission) |
| TC-019 | TD-002 | Shipping Address (all 7 fields, display only) | Context (verification of field presence, no entry) |
| TC-020 | TD-002 | Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, Country | Direct Entry |
| TC-021 | TD-002 | Shipping Address (populated state) | Context (precondition only) |
| TC-023 | TD-003 | Card Entry Field(s) — Pending | Direct Entry (Blocked until structure confirmed) |
| TC-025 | TD-002 | Shipping Address (all 7 fields) | Comparison (consistency check against Review Order summary) |

## 11. Data Lifecycle

| Stage | Description |
|---|---|
| Create | Dataset and field records are authored in this document, sourced only from approved evidence, with dummy/sandbox values where permitted |
| Review | A Reviewer checks the dataset against MA-AA-001/MA-RS-001/MA-TC-001 for evidence accuracy and traceability |
| Approve | An Approver marks the dataset Approved; only then may downstream documents or automation treat it as baselined |
| Consume by Test Case | MA-TC-001 Test Cases reference the dataset by Test Data ID during manual execution |
| Consume by Automation | A future automation script binds to the dataset's fields (see Section 14, Automation File Mapping) once implementation begins |
| Maintain | Any confirmed change (e.g., a Pending value resolved via manual verification) is updated in this document only, preserving One Source of Truth |

## 12. Dataset Owner Matrix

| Dataset | Owner | Review Owner | Approval Owner |
|---|---|---|---|
| TD-001 | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer |
| TD-002 | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer |
| TD-003 | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer |

Consistent with the single-contributor constraint in MA-PV-001 §19 (C-6), these roles are held by the same individual across gates; they remain conceptually distinct for governance traceability, matching the pattern already used in MA-TP-001 §17.

## 13. Data Source Matrix

| Dataset | Data Source | Manual Verification Needed | Automation Ready |
|---|---|---|---|
| TD-001 | MA-AA-001 (Login screen observation) | Yes — Username/Password values | Pending |
| TD-002 | MA-AA-001 (field observation) + this document (fictitious dummy values) | No (values); Optional (validation rules) | Ready |
| TD-003 | MA-AA-001 (generic card entry screen observation) | Yes — full field structure, then values and validation rules | Blocked |

## 14. Automation File Mapping

The fields below are reserved mapping placeholders for a future automation implementation phase. No implementation is defined or implied here.

| Dataset | Future JSON File | Future YAML File | Future Excel Sheet | Future CSV | Future Secrets Store | Future Env Variable | Status |
|---|---|---|---|---|---|---|---|
| TD-001 | Placeholder | Placeholder | Placeholder | Placeholder | Placeholder (recommended for credential values) | Placeholder | Not Yet Defined |
| TD-002 | Placeholder | Placeholder | Placeholder | Placeholder | Not Applicable | Placeholder | Not Yet Defined |
| TD-003 | Placeholder | Placeholder | Placeholder | Placeholder | Placeholder (recommended for card values, if confirmed) | Placeholder | Not Yet Defined |

## 15. Future Scalability — Reserved Dataset IDs

The following IDs are reserved for future datasets and are intentionally left unpopulated. No category or content is implied by reserving a number.

| Reserved ID | Status |
|---|---|
| TD-004 | Reserved — Not Yet Defined |
| TD-005 | Reserved — Not Yet Defined |
| TD-006 | Reserved — Not Yet Defined |
| TD-007 | Reserved — Not Yet Defined |
| TD-008 | Reserved — Not Yet Defined |
| TD-009 | Reserved — Not Yet Defined |
| TD-010 | Reserved — Not Yet Defined |

## 16. Requirement → Scenario → Test Case → Test Data Matrix

| FR | TS | TC | TD |
|---|---|---|---|
| FR-001 | TS-001 | TC-001 | Not Applicable |
| FR-002 | TS-002 | TC-002 | Not Applicable |
| FR-003 | TS-003 | TC-003 | TD-001 |
| FR-004 | TS-004 | TC-004 | TD-001 |
| FR-005 | TS-005 | TC-005 | Not Applicable |
| FR-006 | TS-006 | TC-006 | Not Applicable |
| FR-007 | TS-007 | TC-007 | Not Applicable |
| FR-008 | TS-008 | TC-008 | Not Applicable |
| FR-009 | TS-009 | TC-009 | Not Applicable |
| FR-010 | TS-010 | TC-010 | Not Applicable |
| FR-011 | TS-011 | TC-011 | Not Applicable |
| FR-012 | TS-012 | TC-012 | Not Applicable |
| FR-013 | TS-013 | TC-013 | Not Applicable |
| FR-014 | TS-014 | TC-014 | Not Applicable |
| FR-015 | TS-015 | TC-015 | Not Applicable |
| FR-016 | TS-016 | TC-016 | Not Applicable |
| FR-017 | TS-017 | TC-017 | Not Applicable |
| FR-018 | TS-018 | TC-018 | Not Applicable |
| FR-019 | TS-019 | TC-019 | TD-002 (context) |
| FR-020 | TS-020 | TC-020 | TD-002 |
| FR-021 | TS-021 | TC-021 | TD-002 (context) |
| FR-022 | TS-022 | TC-022 | Not Applicable |
| FR-023 | TS-023 | TC-023 | TD-003 |
| FR-024 | TS-024 | TC-024 | Not Applicable |
| FR-025 | TS-025 | TC-025 | TD-002 (comparison) |
| FR-026 | TS-026 | TC-026 | Not Applicable |
| FR-027 | TS-027 | TC-027 | Not Applicable |
| FR-028 | TS-028 | TC-028 | Not Applicable |
| FR-029 | TS-029 | TC-029 | Not Applicable |
| FR-030 | TS-030 | TC-030 | Not Applicable |
| FR-031 | TS-031 | TC-031 | Not Applicable |
| FR-031 | TS-032 | TC-032 | Not Applicable |

## 17. Coverage Summary

| Coverage Dimension | Result |
|---|---|
| Positive Data | 3 datasets (TD-001, TD-002, TD-003) |
| Negative Data | 6 variants evaluated, all Out of Scope (§8.1.2) |
| Boundary Data | None — excluded (Section 6) |
| Optional Data | None confirmed — Address Line 2 optionality flagged Pending (§8.2) |
| Dynamic Data | None — dynamic elements are observed, not supplied as input |
| Reusable Data | 3 of 3 active datasets are reusable across multiple Test Cases |
| Unique Data | None required — no uniqueness constraint documented |
| Pending Verification Data | 2 of 3 datasets (TD-001, TD-003) still contain Pending items |

## 18. Automation Readiness

| Dataset | Automation Readiness | Rationale |
|---|---|---|
| TD-001 | Pending | Field structure confirmed; both values pending an on-screen read |
| TD-002 | Ready | All 7 fields have usable dummy values; no confirmed blocking validation rule |
| TD-003 | Blocked | Field structure itself is unconfirmed; cannot be automated until inspected |

| Test Case | MA-TC-001 Automation Status | Test Data Status | Combined Execution Readiness |
|---|---|---|---|
| TC-003 | Ready | TD-001 — Pending | Not execution-ready until TD-001 resolved |
| TC-004 | Blocked | TD-001 — Pending | Not execution-ready (blocked on two fronts) |
| TC-019 | Ready | TD-002 — Ready | Execution-ready |
| TC-020 | Ready | TD-002 — Ready | Execution-ready |
| TC-021 | Ready | TD-002 — Ready | Execution-ready |
| TC-023 | Blocked | TD-003 — Blocked | Not execution-ready (blocked on two fronts) |
| TC-025 | Ready | TD-002 — Ready | Execution-ready |

## 19. Pending Manual Verification Items

| # | Item | Dataset | Blocks |
|---|---|---|---|
| 1 | Username value | TD-001 | TC-003, TC-004 |
| 2 | Password value | TD-001 | TC-003, TC-004 |
| 3 | Login validation/error rule (if any) | TD-001 | TC-004 (already Blocked in MA-TC-001) |
| 4 | Payment card field structure (count and identity of fields) | TD-003 | TC-023 (already Blocked in MA-TC-001) |
| 5 | Payment card field value(s) | TD-003 | TC-023 (already Blocked in MA-TC-001) |
| 6 | Payment card validation rule (format, e.g., Luhn) if any | TD-003 | TC-023 (already Blocked in MA-TC-001) |
| 7 | Address Line 2 required-vs-optional status | TD-002 | None currently — informational only |
| 8 | Zip Code and Country format/type (free text vs. dropdown/numeric) | TD-002 | None currently — informational only |

## 20. Assumptions

- The AUT's Login screen continues to display sample credentials directly on-screen at the time of manual verification (MA-AA-001).
- No Shipping Address field enforces a format constraint beyond accepting text input (MA-RS-001 FR-020), so the dummy values in Section 8.2 remain valid inputs.
- The Payment Method screen's exact field structure will be discoverable via Appium Inspector during the manual verification pass committed to in MA-TS-001 §8.
- Dummy/placeholder/sandbox values introduced in this document do not themselves alter or exercise any AUT business rule.
- A single contributor owns and maintains this dataset, consistent with MA-PV-001 §19 (C-6).

## 21. Risks

| Risk | Note |
|---|---|
| TC-003 is Automation-Status "Ready" in MA-TC-001 but not execution-ready here | Data unavailability (TD-001) was not visible at the MA-TC-001 layer; flagged in Section 18 to prevent a false-positive automation start |
| TD-003 requires structural discovery, not just value entry | Higher effort than TD-001/TD-002; may reveal additional fields requiring their own validation rules |
| TD-002 dummy values assume no format validation exists | If a manual pass finds format validation, this dataset must be revised before automation |
| Candidate Payment fields (§8.3.4) could be mistaken for confirmed fields if this document is skimmed | Mitigated by explicit "NOT CONFIRMED" labeling and a dedicated caution note |
| Scope creep into unneeded data categories | Mitigated by Section 6, which justifies every exclusion |

## 22. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Project Owner / Repository Maintainer | Approved | 2026-07-29 |
| Approved By | Project Owner / Repository Maintainer | Approved | 2026-07-29 |
| Document Status | Approved — Baselined — Ready for Automation | — | 2026-07-29 |

---

**End of Document — MA-TDD-001, v1.0**
