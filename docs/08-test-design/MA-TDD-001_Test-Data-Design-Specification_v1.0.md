---
document_id: MA-TDD-001
title: Test Data Design Specification
version: v1.6
status: Approved
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-08-04
reviewed_by: Project Owner / Repository Maintainer
approved_by: Project Owner / Repository Maintainer
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001, MA-TD-001, MA-TC-001, MA-LOC-001]
classification: Internal
---

# MA-TDD-001 — Test Data Design Specification

**Mobile Automation Framework — Automation-Ready Enterprise Edition**

| Field | Value |
|---|---|
| Document ID | MA-TDD-001 |
| Document Name | Test Data Design Specification |
| Version | v1.6 |
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
| v1.1 | 2026-08-01 | Project Owner | Phase 9.2 (Pre-Implementation Documentation Reconciliation): TD-001 (Authentication Dataset) reconciled from "Pending Manual Verification" to "Verified" using evidence from the completed Manual Verification Phase (Username `bod@example.com`, Password `10203040`, the latter corroborated by MA-LOC-001 §5). No other dataset (TD-002, TD-003), no requirement, and no application behavior claim was changed. See docs/framework/PRE_IMPLEMENTATION_DOCUMENTATION_RECONCILIATION.md for the full change record. |
| v1.2 | 2026-08-02 | Project Owner | Phase 9.5A (Pilot Product Reconciliation): new TD-004 — Pilot Product Dataset added (§8.4), promoted from Reserved (§15). The approved Pilot Product changed from Sauce Labs Onesie ($7.99) to **Sauce Labs Backpack (violet)**, $29.99 — the newly verified product, confirmed via live Appium Inspector execution this session (Appium-driven tap, not a manual finger tap) to navigate correctly Catalog → Product Details → Add to Cart → Cart. `pilot.json` updated to match; `ProductDataFactory.pilotProduct()` unchanged (already data-driven). §6, §9, §10, §13, §14, §15, §16, §17, §18 updated for consistency. No other dataset modified. |
| v1.3 | 2026-08-02 | Project Owner | Phase 10.1A (Authentication Module Documentation Reconciliation): §18's Combined Execution Readiness table corrected — TC-004 row changed from Blocked to Execution-ready (MA-TC-001 v1.4 resolved this via Phase 9.5G runtime evidence; TD-001 was already Ready, so test data was never TC-004's actual blocker). §19 item 3 (Login validation/error rule) Blocks column corrected — it never actually blocked TC-004 and continues to block nothing, since no negative-path Authentication test case exists. TD-001 itself unchanged — it was already Verified/Ready. No other dataset modified. |
| v1.4 | 2026-08-02 | Project Owner | Phase 10.4A (Product Details Module Documentation Reconciliation): §6's Quantity Data classification row corrected — its justification cited FR-011 as "Out of Current Observation," which Phase 10.4A (using Phase 10.3 runtime evidence) partially resolved; reworded to state the value-change mechanics are now confirmed and to explain why no dedicated dataset is required regardless (the confirmed behavior is relative, not tied to a specific target value). §18/§19 reviewed — neither table tracks TC-011 (its blocker was never Test-Data-related; TC-011's Test Data field is Not Applicable), so neither required a change. No dataset (TD-001–TD-004) modified. |
| v1.5 | 2026-08-04 | Project Owner | Phase 12.7A (Payment Module Documentation Reconciliation): TD-003 (Payment Card Dataset) fully resolved from "Content Blocked pending verification" to Ready, using Phase 12.3/12.4 real-device field-structure evidence and Phase 12.5/12.6 real-device value/formatting/automated-implementation evidence — §8.3 rewritten from a "What Is Known / Unknown / Candidate Checklist" structure to a confirmed four-field Reusable Dummy Dataset (Cardholder Name, Card Number, Expiration Date, Security Code), mirroring §8.2 (TD-002)'s structure. The prior candidate checklist's unconfirmed "Postal Code" field is retired — real-device evidence confirms no such field exists on the Payment screen. §9 Complete Test Data Catalog, §10 Automation Consumption Guide, §13 Data Source Matrix, §14 Automation File Mapping (now `Defined`, citing `testdata/common/payment/card.json`), §17 Coverage Summary, §18 Automation Readiness (TD-003 and TC-023 rows), §19 Pending Manual Verification Items (items 4–6 resolved), §20 Assumptions, and §21 Risks all updated to match. No other dataset (TD-001, TD-002, TD-004) modified. |
| v1.6 | 2026-08-05 | Project Owner | Phase 15.6A (Enterprise Documentation Final Reconciliation): resolves the §14 finding from Phase 15.6's Enterprise Documentation Baseline Freeze. §14 Automation File Mapping's TD-001 and TD-002 rows corrected from `Placeholder`/`Not Yet Defined` (a stale carryover — these were never updated to reflect their actual implementation status) to `Defined`, citing the real, in-use JSON paths (`testdata/common/login/credentials.json` via `LoginDataFactory.CREDENTIALS_RESOURCE`; `testdata/common/shipping/address.json` via `ShippingDataFactory.ADDRESS_RESOURCE`), independently verified against source this session, including `TestDataEnvironmentResolver`'s resolution logic. §13 Data Source Matrix (which already correctly showed both as `Ready`) and §14 are now internally consistent. Footer corrected from "v1.1" to match the current version (now v1.6) — never previously updated since document creation. No other section, dataset, or field modified. No Java code, Page Object, locator, Test Data, or framework file modified in this phase; no tests executed. |

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
| Product Selection Data | Included, as of v1.2 (TD-004) | TC-009 itself still accepts any product card and needs no specific identity. However, TC-012's pilot requires a deterministic, verified target — see TD-004 (§8.4). This is a narrower, TC-012-specific inclusion, not a reversal of TC-009's general scope |
| Quantity Data | Excluded | Value-change mechanics (step size, zero-floor behavior) confirmed via Phase 10.3 runtime evidence and no longer Out of Current Observation (MA-RS-001 FR-011, Phase 10.4A) — the confirmed behavior is relative (+1/-1 per tap from whatever value is already displayed), so no specific target value or dedicated dataset is required. Maximum bound remains Out of Current Observation |
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

**Status: Verified (Phase 9.2, 2026-08-01).** Resolved via the Manual Verification Phase, confirmed through actual AUT execution — not sourced from GitHub source code or invented/sample documentation. The Password value independently corroborates MA-LOC-001 §5's own source-level finding ("Credential Row 1 — Password... Text `10203040`... only row with a visible password value in default data"), giving this resolution two independent, mutually-consistent evidence points.

| Field Name | Description | Sample Value | Allowed Type | Required / Optional | Known Validation Rule |
|---|---|---|---|---|---|
| Username | Login username value entered into the Username field | `bod@example.com` | String | Required | Pending Manual Verification (value now known; format/validation-rule confirmation remains open — no format-validation logic was found beyond the required-field check, MA-LOC-001 §18) |
| Password | Login password value entered into the Password field | `10203040` | String | Required | Pending Manual Verification (value now known; format/validation-rule confirmation remains open, same basis as Username) |

| Field Name | Source Document | Automation Ready | Manual Verification Needed | Notes |
|---|---|---|---|---|
| Username | Manual Verification Phase (verified through actual AUT execution); MA-AA-001 (sample credentials' on-screen presence originally observed) | Ready | No — verified | Value confirmed by direct on-screen read during the Manual Verification Phase, not fabricated |
| Password | Manual Verification Phase (verified through actual AUT execution); corroborated by MA-LOC-001 §5 (`password1TV` = "10203040", source-level finding) | Ready | No — verified | Two independent evidence sources agree on this value |

**Automation Consumption:** TC-003 Steps 1–2 (entry), TC-004 Step 1 (resubmission). Automation Readiness for this sub-dataset: **Ready** — both values verified via the Manual Verification Phase (previously: Pending, resolved via the manual verification pass committed to in MA-TS-001 §8, as anticipated).

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
| Purpose | Supplies values for the four confirmed Payment Card fields |
| Owner | Project Owner / Repository Maintainer |
| Status | Approved |
| Priority | Must (inherited from FR-023) |
| Reusable | Yes — single dataset for all Payment-stage consumers |
| Scope | Checkout — Payment (MA-RS-001 §6.7) |
| Data Source | Phase 12.3/12.4 (field structure, real-device runtime investigation) + Phase 12.5 (values and formatting behavior, real-device runtime investigation) + this document (fictitious dummy values) |
| Maintenance Owner | Project Owner / Repository Maintainer |
| Version | v1.1 |
| Review Status | Reviewed |
| Approval Status | Approved |

#### 8.3.1 What Is Known

- The Payment Method screen displays exactly **four** card entry fields — Cardholder Name, Card Number, Expiration Date, Security Code — confirmed via Phase 12.3's real-device UI hierarchy dump and independently re-confirmed via Phase 12.5's direct field-by-field interaction. No fifth field (e.g. a separate Postal Code) exists on this screen — see §8.3.3's correction note.
- Card Number and Expiration Date are custom widgets (`CreditCardNumberEditText`/`CreditCardDateEditText` per MA-LOC-001 §11, source-decompiled) that **auto-format** entered digits as you type: Card Number groups digits in fours separated by spaces (e.g. `4111 1111 1111 1111`); Expiration Date inserts a slash after the second digit (e.g. `12/25`). `getText()` returns the formatted value, not the raw input (Phase 12.5, confirmed).
- Cardholder Name and Security Code are plain `EditText` fields with no reformatting; Security Code displays unmasked (no dots/asterisks) — confirmed Phase 12.5.
- All four fields are required: each shows a "Value looks invalid." validation message (border + icon, for all four; the text label itself was observed for Cardholder Name, Expiration Date, and Security Code but did not appear for Card Number in two attempts — an unexplained, non-blocking asymmetry, not further investigated) when empty and blurred — confirmed Phase 12.5.
- The Billing-Same-As-Shipping checkbox is checked by default — confirmed Phase 12.3/12.5.
- A "Review Order" action exists on this screen (`paymentBtn`, third reuse of that resource-id per MA-LOC-001 §10/§11) — its enabled state does not depend on form validity (observed enabled even with all fields empty, Phase 12.5); its navigation behavior when tapped remains untested (out of scope through Phase 12.6).

#### 8.3.2 What Remains Unknown

- Whether the AUT performs any server-side/business validation beyond the client-side "Value looks invalid." format check (e.g. a Luhn check on Card Number) — no such rule was triggered or observed.
- The exact cause of Card Number's error-text-label asymmetry (§8.3.1).
- Review Order's actual navigation/validation behavior when tapped (deferred to a future Test Case; not TC-023's scope).

#### 8.3.3 Reusable Dummy Dataset

Values below are fictitious QA test data (the Card Number is the globally standard industry test number, `4111 1111 1111 1111`). They do not correspond to any real person, business, or payment instrument, and must never be replaced with production or personal data.

| Field Name | Description | Sample Value (raw entry) | Expected Value (AUT auto-formatted) | Allowed Type | Required / Optional | Known Validation Rule |
|---|---|---|---|---|---|---|
| Cardholder Name | Name on card | Rahim Test Uddin (reuse of TD-002 Full Name persona) | Not applicable — no reformatting | String | Required | "Value looks invalid." on empty + blur (Phase 12.5) |
| Card Number | Card number | 4111111111111111 | 4111 1111 1111 1111 | String / Numeric | Required | "Value looks invalid." border/icon on empty + blur; text label not observed (Phase 12.5) |
| Expiration Date | Card expiration (MM/YY) | 1225 | 12/25 | String / Numeric | Required | "Value looks invalid." on empty + blur (Phase 12.5) |
| Security Code | Card security code (CVV) | 456 | Not applicable — no reformatting | String / Numeric | Required | "Value looks invalid." on empty + blur (Phase 12.5) |

| Field Name | Source Document | Automation Ready | Manual Verification Needed | Notes |
|---|---|---|---|---|
| Cardholder Name | Phase 12.3/12.5 (field confirmed); value is fictitious dummy data | Ready | No | Plain `EditText`; no reformatting |
| Card Number | Phase 12.3/12.5 (field and auto-formatting confirmed) | Ready | No | Custom widget; assert against the formatted "Expected Value", never the raw entry |
| Expiration Date | Phase 12.3/12.5 (field and auto-formatting confirmed) | Ready | No | Custom widget; assert against the formatted "Expected Value", never the raw entry |
| Security Code | Phase 12.3/12.5 (field confirmed) | Ready | No | Plain `EditText`; displays unmasked |

**Correction (Phase 12.7A):** the prior candidate checklist (v1.0 of this section) included an unconfirmed "Postal Code" field, reusing TD-002's Zip Code "if a field exists." Phase 12.3/12.5's real-device evidence confirms no such field exists on the Payment screen — the field count is exactly four, not five. This candidate is retired, not carried forward.

**Automation Consumption:** TC-023 Steps 1–4 (direct entry, in field order above) plus Step 5 (Billing checkbox default-state context check). TC-022 uses this screen as display-verification context only (no field-level data consumption). Automation Readiness for this dataset: Ready — every field has a usable, confirmed sample value and a confirmed expected (post-formatting, where applicable) assertion value.

### 8.4 TD-004 — Pilot Product Dataset

| Field | Value |
|---|---|
| Dataset ID | TD-004 |
| Purpose | Supplies a single, deterministic, verified product identity for TC-012's Pilot Automation — Product Selection, Product Details verification, and Add to Cart |
| Owner | Project Owner / Repository Maintainer |
| Status | Approved |
| Priority | Must (inherited from FR-012) |
| Reusable | Yes, architecturally — any future Test Case needing a deterministic catalog item may reference this dataset. TC-012 is its only current consumer |
| Scope | Product Details / Add to Cart (MA-RS-001 §6.4) — Pilot Automation only |
| Data Source | Verified Manual Execution Evidence, 2026-08-02 (live Appium Inspector capture, this session — Appium-driven tap, not a manual finger tap) |
| Maintenance Owner | Project Owner / Repository Maintainer |
| Version | v1.0 |
| Review Status | Reviewed |
| Approval Status | Approved |

#### 8.4.1 Approved Pilot Product

**Status: Verified (Phase 9.5A, 2026-08-02).** Supersedes the previous Pilot Product (Sauce Labs Onesie, $7.99, approved Phase 9.3) per explicit direction — the Onesie is no longer the approved Pilot dataset.

| Field Name | Description | Sample Value | Allowed Type | Required / Optional | Known Validation Rule |
|---|---|---|---|---|---|
| Product Name | Exact on-screen catalog product name (`titleTV`), used for exact-text product-card matching by the existing, unmodified `ProductsPage.selectProductByName()` | `Sauce Labs Backpack (violet)` | String | Required | Not Applicable — read-only display field, no input validation |
| Price | Exact on-screen catalog/details price (`priceTV`), used for Product Card and Product Details verification | `29.99` | BigDecimal | Required | Not Applicable — read-only display field |

| Field Name | Source Document | Automation Ready | Manual Verification Needed | Notes |
|---|---|---|---|---|
| Product Name | Verified Manual Execution Evidence, 2026-08-02 (this session) — confirmed via live Appium Inspector capture across Catalog (`titleTV`), Product Details (`productTV`), and Cart (`titleTV`) screens, all reading "Sauce Labs Backpack (violet)" | Ready | No — verified | The app displays name and color as a single combined string, not separate fields; `ProductItem` has no dedicated variant field, so "Violet" is encoded within `name`, matching the AUT's own display convention |
| Price | Same evidence — `priceTV` read as "$ 29.99" on both Catalog and Product Details, `totalPriceTV` read as "$ 29.99" on Cart (single item, no shipping added yet) | Ready | No — verified | — |

**Automation Consumption:** TC-012 Steps 1–2 (load + locate), Steps 3–4 and 7–8 (Product Card / Product Details name+price verification), Step 14 (Cart content verification). Consumed via `ProductDataFactory.pilotProduct()` → `product/pilot.json`; automation must not hardcode these values. Automation Readiness for this dataset: **Ready**.

## 9. Complete Test Data Catalog

| Test Data ID | Field Name | Sample Value | Automation Ready | Manual Verification Needed |
|---|---|---|---|---|
| TD-001 | Username | `bod@example.com` | Ready | No — Verified (Manual Verification Phase) |
| TD-001 | Password | `10203040` | Ready | No — Verified (Manual Verification Phase) |
| TD-002 | Full Name | Rahim Test Uddin | Ready | No |
| TD-002 | Address Line 1 | House 45, Road 7 | Ready | No |
| TD-002 | Address Line 2 | Sector 10, Uttara | Ready | No |
| TD-002 | City | Dhaka | Ready | No |
| TD-002 | State/Region | Dhaka Division | Ready | No |
| TD-002 | Zip Code | 1230 | Ready | No |
| TD-002 | Country | Bangladesh | Ready | No |
| TD-003 | Cardholder Name | Rahim Test Uddin | Ready | No |
| TD-003 | Card Number | 4111111111111111 (raw entry) / 4111 1111 1111 1111 (AUT-formatted) | Ready | No |
| TD-003 | Expiration Date | 1225 (raw entry) / 12/25 (AUT-formatted) | Ready | No |
| TD-003 | Security Code | 456 | Ready | No |
| TD-004 | Product Name | `Sauce Labs Backpack (violet)` | Ready | No — Verified (2026-08-02 session evidence) |
| TD-004 | Price | `29.99` | Ready | No — Verified (2026-08-02 session evidence) |

## 10. Automation Consumption Guide

This section maps every consuming Test Case to its dataset and fields, so an Automation Engineer can wire test data without re-deriving it from MA-TC-001.

| Test Case | Dataset | Fields Consumed | Consumption Type |
|---|---|---|---|
| TC-003 | TD-001 | Username, Password | Direct Entry |
| TC-004 | TD-001 | Username, Password | Direct Entry (resubmission) |
| TC-019 | TD-002 | Shipping Address (all 7 fields, display only) | Context (verification of field presence, no entry) |
| TC-020 | TD-002 | Full Name, Address Line 1, Address Line 2, City, State/Region, Zip Code, Country | Direct Entry |
| TC-021 | TD-002 | Shipping Address (populated state) | Context (precondition only) |
| TC-022 | TD-003 | Payment Card fields (display only, no entry) | Context (verification of field presence, no entry) |
| TC-023 | TD-003 | Cardholder Name, Card Number, Expiration Date, Security Code | Direct Entry |
| TC-025 | TD-002 | Shipping Address (all 7 fields) | Comparison (consistency check against Review Order summary) |
| TC-012 | TD-004 | Product Name, Price | Direct Load (`ProductDataFactory.pilotProduct()`) + Verification (Product Card, Product Details, Cart) |

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
| TD-004 | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer | Project Owner / Repository Maintainer |

Consistent with the single-contributor constraint in MA-PV-001 §19 (C-6), these roles are held by the same individual across gates; they remain conceptually distinct for governance traceability, matching the pattern already used in MA-TP-001 §17.

## 13. Data Source Matrix

| Dataset | Data Source | Manual Verification Needed | Automation Ready |
|---|---|---|---|
| TD-001 | Manual Verification Phase (verified through actual AUT execution); MA-AA-001 (on-screen presence originally observed); corroborated by MA-LOC-001 §5 for the Password value | No — Verified | Ready |
| TD-002 | MA-AA-001 (field observation) + this document (fictitious dummy values) | No (values); Optional (validation rules) | Ready |
| TD-003 | Phase 12.3/12.4 (field structure, real-device runtime investigation) + Phase 12.5 (values/formatting, real-device runtime investigation) + this document (fictitious dummy values) | No — Verified (Phase 12.3–12.6) | Ready |
| TD-004 | Verified Manual Execution Evidence, 2026-08-02 (live Appium Inspector capture, this session) | No — Verified | Ready |

## 14. Automation File Mapping

**Corrected Phase 15.6A**: TD-001 and TD-002 were still shown below as reserved *future* placeholders despite being fully implemented and in active use since early in this project — a stale carryover this table was never updated to reflect, independently discovered and confirmed during Phase 15.6's Enterprise Documentation Baseline Freeze, then verified directly against source (`LoginDataFactory.CREDENTIALS_RESOURCE`, `ShippingDataFactory.ADDRESS_RESOURCE`, and `TestDataEnvironmentResolver`'s resolution logic) during this reconciliation. The fields for any dataset still genuinely unimplemented remain reserved placeholders for a future automation implementation phase — no implementation is defined or implied for those.

| Dataset | Future JSON File | Future YAML File | Future Excel Sheet | Future CSV | Future Secrets Store | Future Env Variable | Status |
|---|---|---|---|---|---|---|---|
| TD-001 | `src/test/resources/testdata/common/login/credentials.json` (real, in use — `LoginDataFactory.CREDENTIALS_RESOURCE`, resolved via `TestDataEnvironmentResolver`) | Not Applicable | Not Applicable | Not Applicable | Not implemented — credential values remain in plaintext JSON, consistent with this project's public-portfolio/no-real-PII scope (MA-PV-001 §19) | Not Applicable | Defined |
| TD-002 | `src/test/resources/testdata/common/shipping/address.json` (real, in use — `ShippingDataFactory.ADDRESS_RESOURCE`, resolved via `TestDataEnvironmentResolver`) | Not Applicable | Not Applicable | Not Applicable | Not Applicable | Not Applicable | Defined |
| TD-003 | `src/test/resources/testdata/common/payment/card.json` (real, in use) | Not Applicable | Not Applicable | Not Applicable | Not Applicable | Not Applicable | Defined |
| TD-004 | `src/test/resources/testdata/common/product/pilot.json` (real, in use) | Not Applicable | Not Applicable | Not Applicable | Not Applicable | Not Applicable | Defined |

## 15. Future Scalability — Reserved Dataset IDs

The following IDs are reserved for future datasets and are intentionally left unpopulated. No category or content is implied by reserving a number. **TD-004 was promoted out of this reserved range in v1.2** — see §8.4.

| Reserved ID | Status |
|---|---|
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
| FR-012 | TS-012 | TC-012 | TD-004 |
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
| Positive Data | 4 datasets (TD-001, TD-002, TD-003, TD-004) |
| Negative Data | 6 variants evaluated, all Out of Scope (§8.1.2) |
| Boundary Data | None — excluded (Section 6) |
| Optional Data | None confirmed — Address Line 2 optionality flagged Pending (§8.2) |
| Dynamic Data | None — dynamic elements are observed, not supplied as input |
| Reusable Data | 4 of 4 active datasets are architecturally reusable; TD-004 currently has a single consumer (TC-012) |
| Unique Data | None required — no uniqueness constraint documented |
| Pending Verification Data | 0 of 4 datasets contain Pending items — TD-001 resolved 2026-08-01, TD-004 added and Verified 2026-08-02, TD-003 resolved 2026-08-04 (Phase 12.7A, using Phase 12.3–12.6 evidence) |

## 18. Automation Readiness

| Dataset | Automation Readiness | Rationale |
|---|---|---|
| TD-001 | Ready | Field structure confirmed; both values verified via the Manual Verification Phase (§8.1.1) |
| TD-002 | Ready | All 7 fields have usable dummy values; no confirmed blocking validation rule |
| TD-003 | Ready | Field structure, values, and formatting behavior all confirmed via Phase 12.3–12.6 real-device evidence (§8.3) |
| TD-004 | Ready | Product Name and Price both verified via live Appium Inspector execution, 2026-08-02 (§8.4.1) |

| Test Case | MA-TC-001 Automation Status | Test Data Status | Combined Execution Readiness |
|---|---|---|---|
| TC-003 | Ready | TD-001 — Ready | Execution-ready |
| TC-004 | Ready | TD-001 — Ready | Execution-ready — resolved Phase 10.1A/MA-TC-001 v1.4 using Phase 9.5G runtime evidence |
| TC-012 | Ready | TD-004 — Ready | Execution-ready |
| TC-019 | Ready | TD-002 — Ready | Execution-ready |
| TC-020 | Ready | TD-002 — Ready | Execution-ready |
| TC-021 | Ready | TD-002 — Ready | Execution-ready |
| TC-022 | Ready | TD-003 — Ready (context only) | Execution-ready |
| TC-023 | Ready | TD-003 — Ready | Execution-ready — resolved Phase 12.7A/MA-TC-001 v1.9 using Phase 12.5/12.6 real-device evidence |
| TC-025 | Ready | TD-002 — Ready | Execution-ready |

## 19. Pending Manual Verification Items

| # | Item | Dataset | Blocks |
|---|---|---|---|
| 1 | ~~Username value~~ — **RESOLVED 2026-08-01** (Manual Verification Phase; `bod@example.com`, see §8.1.1) | TD-001 | None (was TC-003, TC-004) |
| 2 | ~~Password value~~ — **RESOLVED 2026-08-01** (Manual Verification Phase; `10203040`, corroborated by MA-LOC-001 §5, see §8.1.1) | TD-001 | None (was TC-003, TC-004) |
| 3 | Login validation/error rule (if any) | TD-001 | None currently — TC-004 resolved Phase 10.1A (MA-TC-001 v1.4) on other evidence; this item was never TC-004's actual blocker and remains open only as informational (no negative-path Authentication test case exists to be blocked by it — MA-TDD-001 §8.1.2) |
| 4 | ~~Payment card field structure (count and identity of fields)~~ — **RESOLVED 2026-08-04** (Phase 12.3/12.4 real-device runtime investigation and automated implementation; exactly four fields — Cardholder Name, Card Number, Expiration Date, Security Code — see §8.3.1) | TD-003 | None (was TC-023) |
| 5 | ~~Payment card field value(s)~~ — **RESOLVED 2026-08-04** (Phase 12.5 real-device runtime investigation; see §8.3.3) | TD-003 | None (was TC-023) |
| 6 | Payment card validation rule (format, e.g., Luhn) if any | TD-003 | None currently — a client-side "Value looks invalid." format check was observed (Phase 12.5, §8.3.1), but no server-side/business rule (e.g. Luhn) was triggered or tested; this remains open only as informational, since TC-023 (now Ready) never depended on it |
| 7 | Address Line 2 required-vs-optional status | TD-002 | None currently — informational only |
| 8 | Zip Code and Country format/type (free text vs. dropdown/numeric) | TD-002 | None currently — informational only |

## 20. Assumptions

- ~~The AUT's Login screen continues to display sample credentials directly on-screen at the time of manual verification (MA-AA-001).~~ **Confirmed true 2026-08-01** — the Manual Verification Phase read the value directly from the Login screen, as this assumption anticipated (§8.1.1).
- No Shipping Address field enforces a format constraint beyond accepting text input (MA-RS-001 FR-020), so the dummy values in Section 8.2 remain valid inputs.
- ~~The Payment Method screen's exact field structure will be discoverable via Appium Inspector during the manual verification pass committed to in MA-TS-001 §8.~~ **Confirmed true 2026-08-04** — Phase 12.3's real-device UI hierarchy dump (the functional equivalent of an Appium Inspector capture) discovered the field structure exactly as this assumption anticipated (§8.3.1).
- Dummy/placeholder/sandbox values introduced in this document do not themselves alter or exercise any AUT business rule.
- A single contributor owns and maintains this dataset, consistent with MA-PV-001 §19 (C-6).

## 21. Risks

| Risk | Note |
|---|---|
~~TC-003 is Automation-Status "Ready" in MA-TC-001 but not execution-ready here~~ — **RESOLVED 2026-08-01** | TD-001 is now Ready (§8.1.1, §18); TC-003 is execution-ready. Retained here, struck through, for historical traceability rather than deleted outright. |
| ~~TD-003 requires structural discovery, not just value entry~~ — **Resolved Phase 12.7A** | Structural discovery completed via Phase 12.3/12.4 real-device evidence; no additional fields were revealed beyond the four documented in §8.3 |
| TD-002 dummy values assume no format validation exists | If a manual pass finds format validation, this dataset must be revised before automation |
| ~~Candidate Payment fields could be mistaken for confirmed fields if this document is skimmed~~ — **Resolved Phase 12.7A** | The unconfirmed candidate checklist (formerly §8.3.4) was retired and replaced with a confirmed Reusable Dummy Dataset (§8.3.3), mirroring TD-002's structure — no candidate/unconfirmed content remains in §8.3 |
| Scope creep into unneeded data categories | Mitigated by Section 6, which justifies every exclusion |
| TD-004 is a single, specific catalog item (Sauce Labs Backpack, violet) | If this exact product is ever removed or renamed in the AUT's catalog, TC-012's pilot breaks even though the framework itself is unaffected — no fallback/alternate pilot product is currently defined |
| Pilot Product changed once already (Onesie → Backpack violet, v1.2) | Any future change should follow the same evidence-first process: update `pilot.json`, then this document, in a single reconciliation pass — not independently |

## 22. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Project Owner / Repository Maintainer | Approved | 2026-07-29 |
| Approved By | Project Owner / Repository Maintainer | Approved | 2026-07-29 |
| Document Status | Approved — Baselined — Ready for Automation | — | 2026-07-29 |
| v1.1 Reconciliation | Project Owner / Repository Maintainer | Approved | 2026-08-01 |

---

**End of Document — MA-TDD-001, v1.6**
