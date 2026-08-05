---
document_id: MA-TC-001
title: Test Case Specification
version: v1.16
status: Draft
author: Project Owner / Repository Maintainer
created_date: 2026-07-29
last_updated: 2026-08-04
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
aut_name: Sauce Labs Android Demo App
aut_version: 2.2.0
related_documents: [MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001, MA-TD-001, MA-TDD-001, MA-LOC-001]
classification: Internal
---

# MA-TC-001 — Test Case Specification

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document ID | MA-TC-001 |
| Document Name | Test Case Specification |
| Version | v1.16 |
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
| v0.1 | 2026-07-29 | Project Owner | Draft scaffold with TC-001 |
| v1.0 | 2026-07-29 | Project Owner | Complete set of 32 Test Cases (TC-001–TC-032) submitted for approval |
| v1.1 | 2026-08-01 | Project Owner | Phase 9.2 (Pre-Implementation Documentation Reconciliation): TC-003 and TC-004's Test Data fields updated from "Pending Test Data Design" to reference MA-TDD-001 §8.1.1 (TD-001), now Verified. TC-004's Automation Status remains Blocked — its blocker is the unrelated TS-004 destination-screen ambiguity, not test data, and was not touched. No other Test Case was modified. See docs/framework/PRE_IMPLEMENTATION_DOCUMENTATION_RECONCILIATION.md. |
| v1.2 | 2026-08-01 | Project Owner | TC-012 Isolated Resolution: TC-012's Automation Status changed from Blocked to Ready, Verification Status to Confirmed, Step 2 Expected Result, Post Condition, Automation Mapping, Execution Tags, and Reviewer Notes updated using Task #16 reconciliation evidence (this session) and MA-LOC-001 §6, §8. Automation Readiness Summary (§9) updated accordingly. No other Test Case modified; TC-012's underlying automation code remains blocked on a separate, unrelated TC-009 defect under investigation in Phase 9.5.1 — see TC-012's Reviewer Notes. |
| v1.3 | 2026-08-02 | Project Owner | Phase 9.5A (Pilot Product Reconciliation): TC-012 fully rewritten around the newly approved Pilot Product, Sauce Labs Backpack (violet) — supersedes Sauce Labs Onesie. Test Data field populated (MA-TDD-001 §8.4, TD-004); Preconditions and Dependencies changed from "Product Details displayed (TC-009)" to "Product Catalog displayed (TC-001)", since TC-012's own steps now include Product Card location and identity verification; Test Steps expanded from 2 to 14; Objective, Expected Results, Post Condition, and Reviewer Notes rewritten accordingly. Step 11's Color Selector expected result was corrected from a generic "Violet" assumption to the actually-verified "Unknown color" fallback (MA-LOC-001 §8) — Violet is not one of the AUT's four recognized color constants. Documentation only; TC-012's automation code (`ProductDetailsTest.addProductToCart()`) has not yet been updated to this new flow — see TC-012's Automation Mapping note. No other Test Case modified. |
| v1.4 | 2026-08-02 | Project Owner | Phase 10.1A (Authentication Module Documentation Reconciliation): TC-004's Automation Status changed from Blocked to Ready, Verification Status to Confirmed, Steps expanded from 2 to 5, Post Condition/Automation Mapping/Execution Tags/Reviewer Notes updated, using Phase 9.5G runtime evidence (this session, Enterprise Framework v1.0.0). TC-002 and TC-003's Automation Mapping/Execution Tags populated (both were already automation-ready; only the mapping was unpopulated) and TC-003's stale Post Condition corrected. Automation Readiness Summary (§9) and Verification Status counts updated accordingly. No other Test Case modified. |
| v1.5 | 2026-08-02 | Project Owner | Phase 10.2 (Product Details Module Documentation Reconciliation): TC-009's Automation Mapping populated for Steps 1-4 (incidentally exercised inline within TC-012's `ProductDetailsTest.addProductToCart()`, mirroring TC-002/TC-003's Phase 10.1A pattern) and its Execution Tags corrected to `@regression`; Step 5 (product rating) explicitly documented as unautomated — no rating locator exists anywhere in the framework. TC-010 and TC-011's Reviewer Notes updated to document incidental partial evidence (control displayed/enabled/default-state checks only) without changing their Automation Status, Automation Mapping, or Execution Type — their actual defined interactions (color selection, quantity value change) remain unautomated and unevidenced; FR-011/TS-011's Out of Current Observation status is explicitly unaffected and remains unresolved. TC-012's Automation Mapping corrected — its Phase 9.5A note claiming the code had not yet been updated to the 14-step flow was found stale via this phase's consistency sweep (fresh code review confirms `ProductDetailsTest.addProductToCart()` already matches the 14-step flow, implemented in Phase 9.5B). TC-013 reviewed; no stale or inconsistent content found, no change made. Automation Readiness Summary (§9) counts unchanged — no Test Case's Automation Status, Execution Type, or Verification Status was altered, only Automation Mapping/Execution Tags/Reviewer Notes fields. |
| v1.6 | 2026-08-02 | Project Owner | Phase 10.4A (Product Details Module Documentation Reconciliation, post Phase 10.3 implementation): TC-010's Automation Mapping/Execution Tags/Execution Type populated (`ProductDetailsTest.selectProductColor()`), Verification Status reworded to distinguish what Phase 10.3 evidence confirms (control displayed/interactable) from what remains genuinely open (a selected-state *change*, unobservable for the Pilot Product's single color swatch). TC-011's Automation Status changed from **Blocked to Ready** and Execution Type to Automated (`ProductDetailsTest.adjustProductQuantity()`) — resolved using Phase 10.3 real-device evidence (Increase/Decrease change quantity by exactly 1; zero floor disables Add To Cart), scoped precisely: the selector's maximum bound and the Decrease control's own state at the zero floor remain unexercised and are not claimed as resolved. TC-013's Automation Mapping/Execution Tags/Execution Type populated (`ProductDetailsTest.scrollToProductHighlights()`); no prior ambiguity existed to resolve. TC-009 and TC-012 reviewed; both already accurate from Phase 10.2, no change made. Automation Readiness Summary (§9) updated: Ready 25→26, Blocked 6→5 (TC-011 moved); §10 Assumptions and §11 Risks updated to match (5 remaining Blocked, TC-011 added to the resolved list, module list corrected to Product Browsing/Cart/Payment/Order Placement/Navigation). Companion updates: MA-RS-001 v1.3 (FR-011 partially resolved), MA-TD-001 v1.4 (TS-011 partially resolved), MA-TDD-001 v1.4 (§6 Quantity Data justification corrected). No Java code, Page Object, locator, or framework file modified in this phase. |
| v1.7 | 2026-08-03 | Project Owner | Phase 11.5A (Cart Module Documentation Reconciliation): TC-014/015/016/017's Automation Mapping/Execution Type populated (`CartTest.accessCartScreen()`/`updateCartItemQuantity()`/`removeCartItem()`/`verifyCartTotal()`, Phases 11.2–11.4). TC-015/016/017's Verification Status changed from Partially Confirmed to Confirmed — each Test Case's previous "inferred, not literally observed" caveat is now resolved by direct real-device evidence (quantity-to-total causal recalculation, item-removal cart-summary update, price×quantity summation formula). Automation Status unchanged for all four (already Ready before automation existed). Execution Tags assigned: `@regression` for read-only verification (TC-014, TC-017), `@regression`+`@critical` for state-mutating actions (TC-015, TC-016), consistent with the Product Details module's TC-011/TC-012 precedent. TC-016's Reviewer Notes additionally flag a genuine, disclosed automation gap: the Locator Repository's source-confirmed decrease-to-zero removal path (distinct from the explicit Remove button) has never been exercised by any test. TC-018 reviewed; no change — FR-018's Login-sequence ambiguity remains genuinely unresolved, not over-resolved. Automation Readiness Summary (§9) Verification Status counts updated (Confirmed 17→20, Partially Confirmed 14→11) to reflect the three TC-level changes; Ready/Blocked counts unchanged (no TC moved between those categories). §11's pre-existing "16 of 32" Partially-Confirmed-but-Ready risk figure (already flagged inconsistent in Phase 10.4A) is now further affected by this phase's changes but not recalculated — correcting it requires auditing non-Cart Test Cases, out of this phase's Cart-only scope; flagged for a future whole-document audit. MA-RS-001, MA-TD-001, MA-TDD-001 reviewed in full for FR-014–018/TS-014–018/Cart test data — no change required in any of the three: FR-014/015/016/017 and TS-014/015/016/017 were already fully specified with no Out of Current Observation language (the "inferred" caveats lived only in MA-TC-001's own Verification Status fields, not at the requirement/scenario level); FR-018/TS-018 correctly remain Out of Current Observation, not touched. No Java code, Page Object, locator, or framework file modified in this phase. |
| v1.8 | 2026-08-04 | Project Owner | Phase 11.9A (Cart Module — TC-018 Documentation Reconciliation): TC-018's Automation Status changed from **Blocked to Ready**, Verification Status from Partially Confirmed to Confirmed, Execution Type to Automated (`CartTest.proceedToCheckoutAnonymousUser()` / `proceedToCheckoutAuthenticatedUser()`) — resolved using Phase 11.7's real-device runtime investigation (direct device interaction, both authentication-state branches independently confirmed) and Phase 11.8's subsequent automated implementation (16 real-device executions, zero flakiness). Steps expanded from 2 to 3 to document the conditional Login/Shipping branch and the auto-resume behavior; Post Condition, Automation Mapping (citing `CheckoutPage`, a new minimally-scoped Page Object added Phase 11.8), Execution Tags (`@regression`, `@critical`), and Reviewer Notes all updated to match. Automation Readiness Summary (§9) updated: Ready 26→27, Blocked 5→4 (TC-018 moved); Verification Status Confirmed 20→21, Partially Confirmed 11→10. §10 Assumptions and §11 Risks updated to match (4 remaining Blocked; TC-018 added to the resolved-items list; the "16% of 32" figure recalculated to "13% of 32" with Cart removed from its module list; the "Checkout dependency chain concentration" risk marked resolved, with a note that TC-019–TC-027 themselves remain separately unimplemented). Companion updates: MA-RS-001 v1.4 (FR-018 fully resolved; §12 Traceability Summary Cart row Partial→Yes), MA-TD-001 v1.5 (TS-018 fully resolved; §10 dependency note rewritten from unresolved to the confirmed conditional relationship; §11/§12 elevated-risk lists updated). MA-TDD-001 reviewed; TC-018's Test Data is Not Applicable, no drift found, no change made. No Java code, Page Object, locator, or framework file modified in this phase. |
| v1.9 | 2026-08-04 | Project Owner | Phase 12.7A (Payment Module Documentation Reconciliation): TC-022's Verification Status, Execution Type (to Automated — `CartTest.accessPaymentScreen()`), Steps (expanded from 1 to 16, one per individually-verified static element), Automation Mapping, Execution Tags (`@regression`), and Reviewer Notes updated using Phase 12.3 real-device runtime investigation and Phase 12.4 automated implementation evidence; Automation Status was already Ready and required no change. TC-023's Automation Status changed from **Blocked to Ready**, Verification Status from Partially Confirmed to Confirmed, Execution Type to Automated (`CartTest.enterPaymentCardData()`), Test Data updated to reference MA-TDD-001 §8.3 (TD-003, now Ready), Steps expanded from 2 to 5 (one per field plus the Billing checkbox default-state check), Post Condition, Automation Mapping, Execution Tags (`@regression`, `@critical` — state-mutating data entry, per the Phase 11.5A precedent), and Reviewer Notes updated using Phase 12.5 real-device runtime investigation and Phase 12.6 automated implementation evidence — resolved using Phase 12.5's confirmed four-field structure and auto-formatting behavior (Card Number space-grouped, Expiration Date slash-inserted) and Phase 12.6's real-device execution (1 individual + 2 five-test suite runs, zero flakiness after one found-and-fixed defect, see Phase 12.6/12.7). TC-023's Dependencies/Preconditions corrected from "TC-021" to "TC-022" — the Payment screen itself, not merely the Shipping screen, is TC-023's actual precondition; this was a pre-existing inconsistency, not introduced by this phase. Automation Readiness Summary (§9) updated: Ready 27→28, Blocked 4→3 (TC-023 moved); Verification Status Confirmed 21→22, Partially Confirmed 10→9. §10 Assumptions and §11 Risks updated to match (3 remaining Blocked: TC-006, 026, 028; TC-023 added to the resolved-items list; the "13% of 32" figure recalculated to "~9% of 32 (3 of 32)" with Checkout — Payment removed from its module list; the "Several Ready Test Cases carry Partially Confirmed verification (16 of 32)" figure noted as further affected but still not corrected, per the same out-of-scope rationale Phase 11.5A/11.9A already established; the "Test data remains undocumented" risk marked fully resolved — TD-003 was its only remaining open item). Companion updates: MA-RS-001 v1.5 (FR-023 fully resolved; FR-022 reviewed, no change needed; §12 Traceability Summary Checkout — Payment row Partial→Yes), MA-TD-001 v1.6 (TS-023 fully resolved; TS-022 reviewed, no change needed), MA-TDD-001 (TD-003 fully resolved — see its own Version History). No Java code, Page Object, locator, or framework file modified in this phase. |
| v1.10 | 2026-08-04 | Project Owner | Phase 13.3A (TC-026 Documentation Reconciliation): TC-026's Automation Status changed from **Blocked to Ready**, Verification Status from Partially Confirmed to Confirmed, Execution Type to Automated (`CartTest.placeOrder()`), Test Data field confirmed Not Applicable (unchanged — matches MA-TDD-001's own FR/TS/TC/TD matrix, no drift found there), Steps expanded from 2 to 17 (one per individually-verified Review Order element, plus Place Order tap, plus one per Checkout Complete element, plus Continue Shopping and the returned-Products-screen check), Post Condition, Automation Mapping, Execution Tags (`@regression`, `@critical` — state-mutating terminal action), and Reviewer Notes updated using Phase 13.2 real-device automated-execution evidence and Phase 13.3 Enterprise Acceptance Review. Reviewer Notes document, as historical implementation findings (not current defects): (1) the Review Order screen is a single scrollable page — Payment Method/Billing Address/Shipping Method/Total/Place Order render below the fold, resolved via existing `ScrollUtility`; (2) a Google Play Services "Save card to Google?" system dialog appears after Place Order and is dismissed via a targeted, minimal Page Object check; (3) the cart badge does **not** persist on the Products screen after Continue Shopping — the initial implementation attempt's assumption that it would (over-extending MA-LOC-001 §13's narrower claim about the Checkout Complete screen itself) was corrected against real-device evidence during the same phase. TC-025 and TC-027's Automation Mapping/Reviewer Notes updated to document incidental coverage from `CartTest.placeOrder()`, per this phase's Incidental Automation Review (§8 below) — their Automation Status/Verification Status/Execution Type deliberately left unchanged, mirroring the TC-002/TC-003/TC-009 precedent (Phase 10.1A/10.2) for Test Cases without their own dedicated `@Test` method. Automation Readiness Summary (§9) updated: Ready 28→29, Blocked 3→2 (TC-026 moved); Verification Status Confirmed 22→23, Partially Confirmed 9→8. §10 Assumptions and §11 Risks updated to match (2 remaining Blocked: TC-006, 028; TC-026 added to the resolved-items list; the "~9% of 32 (3 of 32)" figure recalculated to "~6% of 32 (2 of 32)" with Order Placement removed from its module list; the "Several Ready Test Cases carry Partially Confirmed verification" figure noted as further affected but still not corrected, per the same out-of-scope rationale every prior reconciliation phase has established). Companion updates: MA-RS-001 v1.6 (FR-026 fully resolved; §12 Traceability Summary Order Placement row Partial→Yes), MA-TD-001 v1.7 (TS-026 fully resolved; §11/§12 elevated-risk lists updated). MA-TDD-001 reviewed; TC-026's Test Data is correctly Not Applicable in the FR/TS/TC/TD matrix, no drift found, no change made. No Java code, Page Object, locator, or framework file modified in this phase — confirmed via `git diff`. |
| v1.11 | 2026-08-04 | Project Owner | Phase 13.4A (Shipping Sub-Module Documentation Reconciliation): TC-019's Automation Mapping (`CartTest.accessShippingScreen()`), Execution Type (to Automated), Steps (expanded from 7 to 9, adding the navigation/arrival step and matching the code's actual per-field assertion order), Execution Tags (`@regression`), and Reviewer Notes updated using Phase 12.2 real-device automated-implementation evidence and Phase 12.4/12.6/12.7/13.2/13.4's repeated suite-run confirmation; Automation Status was already Ready and Verification Status already Confirmed, neither required a change. TC-020's Automation Mapping (`CartTest.enterShippingAddressData()`), Execution Type (to Automated), Test Data reference corrected from "Shipping Address — Reference: Pending Test Data Design (Future MA-TDD-001)" to MA-TDD-001 §8.2 (TD-002, Ready) — this reference had literally pre-dated MA-TDD-001's own existence and was never updated once TD-002 was built (Phase 12.2); Steps expanded from 7 to 9; Execution Tags (`@regression`, `@critical` — state-mutating data entry, per the Phase 11.5A precedent); Reviewer Notes updated the same way. TC-021's Automation Mapping (`CartTest.proceedToPayment()`), Execution Type (to Automated), Steps expanded from 1 to 5 (populate precondition, verify button, tap, verify transition); Execution Tags (`@regression`, `@critical` — gates the Payment screen); Reviewer Notes updated, including the confirmed real-device finding (Phase 12.2) that Proceed to Payment against an empty/unpopulated form is blocked by the AUT's own client-side validation, which is why TC-020/021 both populate the form first. This closes the documentation gap identified in Phase 13.1's cross-module audit and re-confirmed in Phase 13.4's Checkout Module freeze verification. Automation Readiness Summary (§9) **unchanged** — TC-019/020/021 were already counted as Ready/Confirmed (never Blocked/Partially-Confirmed), so no count required recalculation; only the supporting Automation Mapping/Execution Type/Test Data/Steps/Reviewer Notes/Execution Tags fields were stale, not the summary-level classification. §10/§11 reviewed — neither references TC-019/020/021 in any unresolved-items list, so neither required a change. Companion review: MA-RS-001 (FR-019/020/021) and MA-TD-001 (TS-019/020/021) both reviewed in full and found to already accurately describe confirmed behavior, with no "Out of Current Observation" or stale language present — neither document required any change; §12/§10-11's respective Shipping rows already read "Yes/Fully observed" and contain no unresolved Shipping items. MA-TDD-001 reviewed; TD-002 already correctly documents the Shipping Address dataset (§8.2, Ready) that TC-020's corrected Test Data reference now points to — no change required there either. No Java code, Page Object, locator, Test Data, or framework file modified in this phase — confirmed via `git status`/`git diff`. |
| v1.12 | 2026-08-04 | Project Owner | Phase 14.2A (Product Sort — TC-006 Documentation Reconciliation): TC-006's Automation Status changed from **Blocked to Ready**, Verification Status from Partially Confirmed to Confirmed, Execution Type to Automated (`CartTest.sortProductCatalog()`), Test Data field confirmed Not Applicable (unchanged — matches MA-TDD-001's own FR/TS/TC/TD matrix, no drift found there), Steps expanded from 2 to 11 (default-state observation, dialog open/option-visibility check, then one select-and-verify pair per option — Name Descending, Price Ascending, Price Descending — plus the explicit Name Ascending re-selection-to-baseline step and the already-active-option idempotency step), Post Condition, Automation Mapping, Execution Tags (`@regression` — Should priority, not a state-mutating critical action), and Reviewer Notes updated using Phase 14.1 real-device automated-execution evidence and Phase 14.2 Enterprise Acceptance Review. Reviewer Notes document the notable finding from Phase 14.1 (corroborated Phase 14.2): Name - Ascending is the Product Catalog's default sort state on app launch — consistent with, but not previously stated by, MA-LOC-001 §7 (which documented the other three checkmarks, but not this one, as `invisible` by default). Automation Readiness Summary (§9) updated: Ready 29→30, Blocked 2→1 (TC-006 moved, only TC-028 remains); Verification Status Confirmed 23→24, Partially Confirmed 8→7. §10 Assumptions and §11 Risks updated to match (1 remaining Blocked: TC-028; TC-006 added to the resolved-items list; the "~6% of 32 (2 of 32)" figure recalculated to "~3% of 32 (1 of 32)" with Product Browsing removed from its module list; the "Several Ready Test Cases carry Partially Confirmed verification" figure noted as further affected but still not corrected, per the same out-of-scope rationale every prior reconciliation phase has established). Companion updates: MA-RS-001 v1.7 (FR-006 fully resolved; §12 Traceability Summary Product Browsing row Partial→Yes), MA-TD-001 v1.8 (TS-006 fully resolved; §11/§12 elevated-risk lists updated, one remaining item: TS-028). MA-TDD-001 reviewed; TC-006's Test Data is correctly Not Applicable in the FR/TS/TC/TD matrix, no drift found, no change made. No Java code, Page Object, locator, Test Data, or framework file modified in this phase — confirmed independently in Phase 14.2's Framework Review via file-modification-timestamp analysis. |
| v1.13 | 2026-08-04 | Project Owner | Phase 14.3A (Product Module Documentation Reconciliation): resolves the single documentation exception disclosed in Phase 14.3's Product Module Final Baseline Freeze. TC-012's Execution Type corrected from "Manual — baseline for future automation" to "Automated — `ProductDetailsTest.addProductToCart()`, real-device validated" — this field had never been updated since the automation was implemented (Phase 9.5B/9.5C, predating even TC-010/011/013's Phase 10.3 automation), despite Automation Status already being Ready and Automation Mapping already being fully populated (Phase 10.2). One further genuine inconsistency independently discovered during this same reconciliation pass: TC-012's Automation Mapping was missing an Evidence/screenshot citation that every other resolved Test Case in this document carries — added (`tc012_01_catalog_before_selection`, `tc012_01b_product_card_verified`, `tc012_02_product_details_after_navigation`, `tc012_03_cart_after_add_to_cart`, framework log). Reviewer Notes updated with a dated correction entry documenting both fixes. TC-006, TC-010, TC-011, and TC-013 were independently re-reviewed against Automation Status, Verification Status, Execution Type, Automation Mapping, Execution Tags, Reviewer Notes, Traceability, Test Data, Steps, Post Condition, and Evidence references — all found already accurate; **no change required** for any of the four. Automation Readiness Summary (§9) **unchanged** — TC-012 was already counted as Ready/Verified before this correction (never Blocked/Partially Confirmed), so no count required recalculation; only the supporting Execution Type and Automation Mapping fields were stale, not the summary-level classification. §10/§11 reviewed — neither references TC-012 in any unresolved-items list, so neither required a change. Companion review: MA-RS-001 (FR-010–013) and MA-TD-001 (TS-010–013) both reviewed in full — **no update required** for either document; both already accurately describe confirmed/honestly-scoped behavior, with TC-011's own disclosed maximum-bound/zero-floor limitation correctly still present (intentional, not stale). MA-TDD-001 reviewed — **no update required**; the FR/TS/TC/TD matrix already correctly maps TC-010/011/013 to Not Applicable and TC-012 to TD-004. No Java code, Page Object, locator, Test Data, or framework file modified in this phase — confirmed via file-modification-timestamp analysis (all remain at their Phase 14.1/prior timestamps). |
| v1.14 | 2026-08-05 | Project Owner | Phase 15.2A (Navigation — TC-028 Documentation Reconciliation): TC-028's Automation Status changed from **Blocked to Ready**, Verification Status from Partially Confirmed to Confirmed, Execution Type to Automated (`NavigationTest.accessDrawerItems()`), Test Data field confirmed Not Applicable (unchanged — matches MA-TDD-001's own FR/TS/TC/TD matrix, no drift found there), Steps expanded from 2 to 9 (one per verified drawer destination — WebView, QR Code Scanner, Geo Location, Drawing, About, FingerPrint, Virtual USB — plus the Reset App State dialog step), Post Condition, Automation Mapping, Execution Tags (`@regression` — Should priority, not a state-mutating critical action), and Reviewer Notes updated using Phase 15.1 real-device automated-execution evidence and Phase 15.2 Enterprise Acceptance Review. Reviewer Notes document the genuine defect found and fixed during Phase 15.1 runtime validation: MA-LOC-001 §17's documented Reset App State dialog title id (`android:id/alertTitle`) did not match this device's actual rendering (`com.saucelabs.mydemoapp.android:id/alertTitle`), confirmed via a fresh `uiautomator dump`, reproducible (3/3) before the fix — an AUT/documentation discrepancy, not a framework or test defect; MA-LOC-001 §17 itself corrected in this same reconciliation (see MA-LOC-001 Review below). Crash App (Debug) remains permanently excluded from automation — explicitly documented, not a gap. Automation Readiness Summary (§9) updated: Ready 30→31, **Blocked 1→0 — zero Test Cases remain Blocked**; Verification Status Confirmed 24→25, Partially Confirmed 7→6. §10 Assumptions and §11 Risks updated to match (the "elevated-risk items" list and the "Test Cases cannot proceed to automation" risk are both now **fully resolved**, marked struck-through per this document's established convention for fully-resolved risks; the "Several Ready Test Cases carry Partially Confirmed verification" figure noted as further affected but still not corrected, per the same out-of-scope rationale every prior reconciliation phase has established). Companion updates: MA-RS-001 v1.8 (FR-028 fully resolved; §12 Traceability Summary Navigation row Partial→Yes), MA-TD-001 v1.9 (TS-028 fully resolved; §11/§12 elevated-risk lists now show zero remaining items). MA-LOC-001 §17 corrected (Reset App State dialog title id only — see MA-LOC-001 Review). MA-TDD-001 reviewed; TC-028's Test Data is correctly Not Applicable in the FR/TS/TC/TD matrix, no drift found, no change made. No Java code, Page Object, locator, Test Data, or framework file modified in this phase — confirmed via file-modification-timestamp analysis. |
| v1.15 | 2026-08-05 | Project Owner | Phase 15.5A (Enterprise Incidental Automation Documentation Reconciliation): using ONLY the verified findings of Phase 15.5's Enterprise Incidental Automation Review, updated 8 Test Cases. **TC-024 (fully incidentally covered)**: Execution Type changed from Manual to Automated (Incidental Coverage) — `CartTest.placeOrder()` (TC-026) exercises this exact action and asserts the destination screen's own title/subtitle, not navigation alone; Automation Status/Verification Status were already Ready/Confirmed and required no value change; Steps, Post Condition, Automation Mapping, Execution Tags (`@regression`), and Reviewer Notes all populated/updated to cite the exact assertion chain. **6 Test Cases (partially incidentally covered) — TC-001, TC-005, TC-007, TC-008, TC-031, TC-032**: Automation Mapping and Reviewer Notes populated with precise, evidence-cited descriptions of exactly which steps/elements are and are not incidentally covered (TC-001: launch/Catalog-display covered, grid/cart-icon/menu-icon not; TC-005: Name/Price covered, Image/Rating not; TC-007: badge value/match covered, cart icon not; TC-008: 9 of 11 drawer items covered, Catalog row not, Crash App intentionally excluded; TC-031: Cart Total dynamic update covered, cart Badge dynamic update not; TC-032: Quantity covered, Color-selection not). Execution Tags populated (`@regression`) for all 6, matching the TC-009 precedent (Phase 10.2) of populating this field even when Execution Type remains Manual. Automation Status/Verification Status/Execution Type deliberately **left unchanged** for all 6 — coverage is genuinely partial, and changing these fields would overstate automation per this phase's explicit instruction. **TC-029 (not covered)**: Reviewer Notes updated to document that Phase 15.5 found zero incidental coverage — `ProductDetailsPage.navigateBack()` is never called by any test; the only `.navigateBack()` call in the suite (`NavigationDrawerPage.navigateBack()`, via TC-028) exercises a different flow (Drawer destination → Catalog, not Product Details → Catalog). Automation Status, Verification Status, Execution Type, and Automation Mapping intentionally **left completely unchanged** (Ready/Confirmed/Manual/Placeholder), per this phase's explicit instruction. §11 Risks: the "Checkout dependency chain concentration" row updated to move TC-024 from "without incidental coverage" to "has documented incidental coverage," alongside TC-025/TC-027. Automation Readiness Summary (§9) **unchanged** — no Automation Status or Verification Status value actually changed for any of the 8 Test Cases (TC-024's Execution Type change is not a §9-tracked field), so no recalculation was required or performed. MA-RS-001 and MA-TD-001 reviewed in full for all 8 Test Cases' underlying FR/TS — **no update required for either document**; none of the 7 relevant FRs/TSs (FR-001/005/007/008/024/029/031, TS-001/005/007/008/024/029/031/032) contain "Out of Current Observation" or resolution-pending wording — automation-incidental-coverage is a Test-Case-level (MA-TC-001) concept, not a Requirement- or Scenario-level one. MA-TDD-001 reviewed; all 8 Test Cases' Test Data rows already correctly read "Not Applicable," no drift found, no change made. No Java code, Page Object, locator, Test Data, or framework file modified in this phase; no tests executed — confirmed via file-modification-timestamp analysis. |
| v1.16 | 2026-08-05 | Project Owner | Phase 15.6A (Enterprise Documentation Final Reconciliation): resolves the findings of Phase 15.6's Enterprise Documentation Baseline Freeze. §11's "Several Ready Test Cases carry Partially Confirmed verification" figure corrected from the stale "16 of 32" (never re-derived since first stated at Phase 10.4A, only patched via individual transition citations across six subsequent phases) to a directly, independently recounted **"6 of 32"** — TC-007, TC-010, TC-011, TC-025, TC-031, TC-032, each already carrying its own documented inference caveat. The arithmetic gap between the historical citation chain's implied 8 (16 minus eight tracked transitions) and the directly-verified 6 could not be reconstructed from available Version History evidence and is disclosed rather than silently resolved — the direct recount is used as authoritative since it was independently re-derived from the document's own live field values, not from historical narrative. Footer corrected from "v1.1" to match the current version (now v1.16) — this document's footer had never been updated since the document's creation, across all 15 prior version bumps. Companion updates: MA-RS-001 and MA-TD-001 footers corrected (v1.0→current) — no content change in either document. MA-TDD-001 §14 corrected — see MA-TDD-001's own Version History (v1.6). MA-TDD-001 approval-status question (MA-TDD-001 shows itself Approved while MA-RS-001/MA-TD-001/MA-TC-001 show Pending) reviewed — **no update required**; no objective evidence was found either confirming or refuting either state, so neither was changed. No Java code, Page Object, locator, Test Data, or framework file modified in this phase; no tests executed. |

---

## 1. Purpose

This document converts every Test Scenario defined in [MA-TD-001] into a professional, evidence-based Test Case, forming the bridge between test design and future automation implementation. It was not part of the original documentation roadmap; it is introduced because every automated test requires a documented Test Case before implementation begins. It does not modify, reinterpret, or add scope to any previously approved document.

## 2. Scope

Every one of the 32 Test Scenarios in MA-TD-001 (TS-001–TS-032) produces exactly one Test Case (TC-001–TC-032). No new requirement, scenario, or application behavior is introduced. Only information already present in MA-PV-001, MA-AA-001, MA-RS-001, MA-TS-001, MA-FA-001, MA-TP-001, and MA-TD-001 is used, in that evidence priority order (MA-AA-001 observed behavior first, then MA-RS-001, MA-TD-001, MA-TP-001, and MA-FA-001/MA-TS-001 only where directly relevant). Where those documents do not confirm a detail, the corresponding field states "Pending Manual Verification" or "Not Confirmed by Current Evidence" rather than inventing one.

## 3. Referenced Documents

| Document ID | Title | Role in This Document |
|---|---|---|
| MA-PV-001 | Project Vision & Scope | Principles, constraints, execution modes |
| MA-AA-001 | AUT Analysis | Priority 1 evidence — observed screen elements and behavior |
| MA-RS-001 | Requirements Specification | Priority 2 evidence — parent Functional Requirement per Test Case |
| MA-TS-001 | Test Strategy | Priority 6 evidence — elevated-risk items, environment/device strategy |
| MA-FA-001 | Framework Architecture | Priority 5 evidence — architectural references only where needed |
| MA-TP-001 | Master Test Plan | Priority 4 evidence — execution stage/environment context |
| MA-TD-001 | Test Design Specification | Priority 3 evidence — parent Test Scenario per Test Case |

## 4. Test Case Design Principles

- Each Test Case maps to exactly one Test Scenario in MA-TD-001, which maps to exactly one Functional Requirement in MA-RS-001.
- No field is populated from outside knowledge, prior experience, or generic mobile/e-commerce assumptions — only from the seven documents in Section 3, applied in the stated evidence priority order.
- Where a Test Scenario's Expected Result carries an "Out of Current Observation" qualifier, the corresponding Test Case preserves that qualifier, sets Automation Status to Blocked, and does not resolve the ambiguity.
- Every Expected Result — overall and per step — is observable, objective, and independently answerable as Pass or Fail. Vague terms ("works correctly," "successfully," "properly") are never used.
- Each step performs exactly one action and yields exactly one observable result.
- Test data is never invented; fields requiring data reference "Pending Test Data Design (Future MA-TDD-001)" unless a value is already documented in an approved source (none currently is).

## 5. Test Case Numbering

Pattern: `TC-<3-digit sequence>`, aligned 1:1 with the corresponding `TS-<3-digit sequence>` in MA-TD-001. Traceability chain: Requirement → Scenario → Test Case → Future Automation Script.

## 6. Test Cases

### TC-001 — Application Launch to Product Catalog

| Field | Value |
|---|---|
| Test Case ID | TC-001 |
| Requirement ID | FR-001 |
| Scenario ID | TS-001 |
| Module | Application Launch (MA-RS-001 §6.1) |
| Feature | Application Launch to Product Catalog |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Product Catalog elements (grid, cart icon, menu icon) and the Launch → Product Catalog sequence are both directly observed in MA-AA-001 (§6, §7) |
| Objective | Verify that launching the AUT displays the Product Catalog screen with its confirmed elements |
| Preconditions | AUT is installed and not currently running |
| Dependencies | None — entry-point scenario (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Launch the AUT. | AUT launches without error. |
| 2 | Observe the screen displayed immediately after launch. | Product Catalog screen is displayed. |
| 3 | Observe the Product Catalog screen for the product grid. | Product grid is displayed. |
| 4 | Observe the Product Catalog screen for the cart icon. | Cart icon is displayed. |
| 5 | Observe the Product Catalog screen for the menu icon. | Menu icon is displayed. |

| Field | Value |
|---|---|
| Post Condition | AUT remains running with Product Catalog displayed, ready for scenarios that depend on TS-001 (e.g., TS-009) |
| Requirement Traceability | FR-001 |
| Scenario Traceability | TS-001 |
| Automation Mapping | **Incidental coverage only (Phase 15.5A)** — no dedicated automated method exists for this Test Case. Steps 1-2 (AUT launches without error; Product Catalog screen displayed) are incidentally, repeatedly confirmed by every `@Test` method across `LoginTest`/`ProductDetailsTest`/`CartTest`/`NavigationTest`, each of which begins with `new ProductsPage(); CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen")` immediately after `BaseTest`'s `initializeDriver()` launches the app fresh (`ExecutionStrategy.ISOLATED`) — confirmed via Phase 15.5 Enterprise Incidental Automation Review. Steps 3-5 (product grid, cart icon, menu icon as individually displayed elements) are **not** incidentally covered: `ProductsLocators.PRODUCT_LIST` is never referenced by any Page Object method or test; `ProductsPage.tapCart()`/`tapMenu()` are click-only, with no preceding `isDisplayed()` assertion anywhere in the codebase. |
| Execution Group | Smoke (MA-TP-001 §10 names Application Launch as part of the Smoke stage) |
| Execution Tags | `@regression` |
| Reviewer Notes | Underlying resource identifiers for the grid/cart/menu icons remain unconfirmed (MA-AA-001 Known Limitations); this is an implementation-phase concern, not a defect in this Test Case. **Reviewed Phase 15.5A** (Enterprise Incidental Automation Review, Phase 15.5): Steps 1-2 are incidentally covered by every test's own opening assertion — see Automation Mapping. Steps 3-5 remain genuinely uncovered. Automation Status/Verification Status/Execution Type intentionally left unchanged (Ready/Confirmed/Manual) — no dedicated automated method exists and coverage is only partial. |

---

### TC-002 — Login Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-002 |
| Requirement ID | FR-002 |
| Scenario ID | TS-002 |
| Module | Authentication (MA-RS-001 §6.2) |
| Feature | Login Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Login screen and its fields (Username, Password, Login button) are directly observed in MA-AA-001, reachable from the Navigation Drawer |
| Objective | Verify the Login screen is reachable from the Navigation Drawer and displays its confirmed fields |
| Preconditions | Navigation Drawer is open (TC-008 executed) |
| Dependencies | TC-008 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap "Login" in the Navigation Drawer. | Login screen is displayed. |
| 2 | Observe the Login screen for the Username field. | Username field is displayed. |
| 3 | Observe the Login screen for the Password field. | Password field is displayed. |
| 4 | Observe the Login screen for the Login button. | Login button is displayed. |

| Field | Value |
|---|---|
| Post Condition | Login screen remains displayed, ready for TC-003 |
| Requirement Traceability | FR-002 |
| Scenario Traceability | TS-002 |
| Automation Mapping | Script/Class/Method: `LoginTest.loginOutcomeVerification()` (`src/test/java/com/mobileautomation/framework/tests/LoginTest.java`) — this step is executed inline within the same method as TC-003/TC-004; see TC-004's Reviewer Notes |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | Added Phase 10.1A: Automation Mapping populated — this Test Case's action was already automated and passing, only the documentation was unsynchronized. |

---

### TC-003 — Login Credential Entry

| Field | Value |
|---|---|
| Test Case ID | TC-003 |
| Requirement ID | FR-003 |
| Scenario ID | TS-003 |
| Module | Authentication (MA-RS-001 §6.2) |
| Feature | Login Credential Entry |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Username and Password are classified as input fields in MA-AA-001 §9, and the Login button is a confirmed control |
| Objective | Verify the Username and Password fields accept input and the Login button is triggerable |
| Preconditions | Login screen is displayed (TC-002 executed) |
| Dependencies | TC-008 (MA-TD-001 §10); sequential execution after TC-002 is a natural manual order not separately formalized as a dependency in MA-TD-001 |
| Test Data | Login Credentials — Reference: MA-TDD-001 §8.1.1 (TD-001) — **Verified** (Username `bod@example.com`, Password `10203040`; Manual Verification Phase, 2026-08-01) |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Enter a value into the Username field. | Username field contains the entered value. |
| 2 | Enter a value into the Password field. | Password field contains the entered value. |
| 3 | Tap the Login button. | Login button responds to activation. |

| Field | Value |
|---|---|
| Post Condition | Login form has been submitted; resulting screen is confirmed as the Product Catalog screen — see TC-004 |
| Requirement Traceability | FR-003 |
| Scenario Traceability | TS-003 |
| Automation Mapping | Script/Class/Method: `LoginTest.loginOutcomeVerification()` (`src/test/java/com/mobileautomation/framework/tests/LoginTest.java`) — this step is executed inline within the same method as TC-002/TC-004; see TC-004's Reviewer Notes |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | ~~Credential values are not invented; execution requires MA-TDD-001 or officially documented sample credentials~~ — **Resolved 2026-08-01**: values verified via the Manual Verification Phase, see Test Data field above. Added Phase 10.1A: Automation Mapping populated — this Test Case's action was already automated and passing, only the documentation was unsynchronized. |

---

### TC-004 — Login Outcome Verification

| Field | Value |
|---|---|
| Test Case ID | TC-004 |
| Requirement ID | FR-004 |
| Scenario ID | TS-004 |
| Module | Authentication (MA-RS-001 §6.2) |
| Feature | Login Outcome Verification |
| Priority | Should |
| Automation Status | Ready |
| Verification Status | Confirmed — Phase 9.5G runtime evidence (this session, real-device execution on Enterprise Framework v1.0.0): Login submission is confirmed, and the resulting destination screen (Product Catalog) is confirmed, independently corroborated by the Navigation Drawer's authenticated-state marker ("Log Out" displayed, "Log In" absent) |
| Objective | Verify the screen displayed after Login submission is capturable, identifiable, and reflects a genuinely authenticated business state — not merely navigation back to a screen that is also reachable while logged out |
| Preconditions | Valid credentials entered on Login screen (TC-003 executed) |
| Dependencies | TC-003 |
| Test Data | Login Credentials — Reference: MA-TDD-001 §8.1.1 (TD-001) — **Verified** (Username `bod@example.com`, Password `10203040`; Manual Verification Phase, 2026-08-01). |
| Execution Type | Automated — `LoginTest.loginOutcomeVerification()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Submit the Login form with previously entered credentials. | Login button responds to activation; the Product Catalog screen is displayed. |
| 2 | Reopen the Navigation Drawer. | Navigation Drawer is displayed. |
| 3 | Observe the Navigation Drawer for the "Log Out" item. | "Log Out" item is displayed — the primary business evidence of successful authentication. |
| 4 | Observe the Navigation Drawer for the "Log In" item. | "Log In" item is not displayed. |
| 5 | Observe the Product Catalog screen. | Product Catalog screen remains displayed. |

| Field | Value |
|---|---|
| Post Condition | User is authenticated; Product Catalog screen is displayed; Navigation Drawer reflects the authenticated state ("Log Out" displayed, "Log In" absent). |
| Requirement Traceability | FR-004 |
| Scenario Traceability | TS-004 |
| Automation Mapping | Script/Class/Method: `LoginTest.loginOutcomeVerification()` (`src/test/java/com/mobileautomation/framework/tests/LoginTest.java`). Page Objects: `ProductsPage` (Drawer access/read), `LoginPage` (credential entry/submit). Test Data: `TestDataManager.loginData().standardCredentials()` (MA-TDD-001 §8.1.1, TD-001). Execution Strategy: `ISOLATED` (guarantees a clean, logged-out starting state — MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 3 screenshots (`tc004_01_drawer_before_login_log_in`, `tc004_02_drawer_after_login_log_out`, `tc004_03_catalog_after_verification`) plus framework log. |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | **Resolved 2026-08-02 (Phase 10.1A)**: previous Blocked status is superseded by Phase 9.5G runtime evidence — the destination-screen ambiguity this Test Case was blocked on is answered, with the Navigation Drawer's authenticated-state marker as corroborating evidence stronger than mere screen re-display. Scoped to the standalone Login path (via the Navigation Drawer) only — the Checkout-interrupted Login path remains FR-018's unresolved concern, not this Test Case's. TC-002 and TC-003 are implemented as steps within this same `loginOutcomeVerification()` method, not as independent test methods — a deliberate, documented implementation shape (Phase 10.1 §12), not a gap. |

---

### TC-005 — Product Catalog Display Verification

| Field | Value |
|---|---|
| Test Case ID | TC-005 |
| Requirement ID | FR-005 |
| Scenario ID | TS-005 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Product Catalog Display Verification |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — product image, name, price, and rating are all directly listed as observed in MA-AA-001 |
| Objective | Verify each product card on the Catalog displays image, name, price, and rating |
| Preconditions | Product Catalog screen is displayed (TC-001 executed) |
| Dependencies | Not formally listed in MA-TD-001 §10; precondition requires Product Catalog to be displayed |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe a product card on the Product Catalog screen for a product image. | Product image is displayed. |
| 2 | Observe the same product card for a product name. | Product name is displayed. |
| 3 | Observe the same product card for a product price. | Product price is displayed. |
| 4 | Observe the same product card for a product rating. | Product rating is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Catalog remains displayed |
| Requirement Traceability | FR-005 |
| Scenario Traceability | TS-005 |
| Automation Mapping | **Incidental coverage only (Phase 15.5A)** — no dedicated automated method exists for this Test Case. Steps 2-3 (Product Name, Product Price) are incidentally confirmed via `ProductDetailsTest.addProductToCart()` (TC-012): `CommonAssertions.verifyText(cardName, pilotProduct.name(), "Product Card — Product Name")` and `CommonAssertions.verifyText(cardPrice, formattedPrice(pilotProduct), "Product Card — Product Price")`, with the Name assertion also mirrored in `CartTest.placeOrder()` (TC-026) — confirmed via Phase 15.5 Enterprise Incidental Automation Review. Step 1 (Product Image on the catalog card) is **not** covered — `ProductsLocators.productImageForCard()` is only ever clicked (`ProductsPage.openProductCard()`), never asserted displayed. Step 4 (Product Rating) remains unautomatable — no locator exists anywhere in the framework (MA-LOC-001 §20.2). |
| Execution Group | Smoke (MA-TP-001 §10 names Catalog display as part of the Smoke stage) |
| Execution Tags | `@regression` |
| Reviewer Notes | **Reviewed Phase 15.5A** (Enterprise Incidental Automation Review, Phase 15.5): Name and Price (Steps 2-3) incidentally confirmed — see Automation Mapping. Image (Step 1) and Rating (Step 4) remain genuinely uncovered; Rating has no locator anywhere in the framework and is not automatable at this time. Automation Status/Verification Status/Execution Type intentionally left unchanged. |

---

### TC-006 — Product Sort Interaction

| Field | Value |
|---|---|
| Test Case ID | TC-006 |
| Requirement ID | FR-006 |
| Scenario ID | TS-006 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Product Sort Interaction |
| Priority | Should |
| Automation Status | Ready — resolved Phase 14.2A (was Blocked; see Reviewer Notes) |
| Verification Status | Confirmed — all four Sort dialog options (Name - Ascending, Name - Descending, Price - Ascending, Price - Descending) and the default sort state are directly confirmed via Phase 14.1 real-device automated execution and Phase 14.2 Enterprise Acceptance Review (this session): each option correctly re-orders the Product Catalog in its named direction, and Name - Ascending is confirmed as the catalog's default sort state on app launch |
| Objective | Verify the Sort control on the Product Catalog is interactable and each of its four options correctly re-orders the catalog |
| Preconditions | Product Catalog screen is displayed |
| Dependencies | Not formally listed in MA-TD-001 §10; precondition requires Product Catalog to be displayed |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.sortProductCatalog()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Product Catalog screen's default state. | Name - Ascending is the active sort (its checkmark is visible with no interaction); catalog displays products in ascending alphabetical order — captured as the baseline order. |
| 2 | Tap the Sort control. | Sort dialog is displayed, showing the title "Sort by:" and all four options (Name - Ascending, Name - Descending, Price - Ascending, Price - Descending). |
| 3 | Select Name - Descending. | Dialog dismisses automatically; catalog re-orders to descending alphabetical order. |
| 4 | Re-open the Sort control. | Name - Descending checkmark is now the active sort. |
| 5 | Select Price - Ascending. | Dialog dismisses automatically; catalog re-orders to ascending numeric price order. |
| 6 | Re-open the Sort control. | Price - Ascending checkmark is now the active sort. |
| 7 | Select Price - Descending. | Dialog dismisses automatically; catalog re-orders to descending numeric price order. |
| 8 | Re-open the Sort control. | Price - Descending checkmark is now the active sort. |
| 9 | Select Name - Ascending (explicit re-selection). | Dialog dismisses automatically; catalog returns to the exact default baseline order captured in Step 1. |
| 10 | Re-open the Sort control. | Name - Ascending checkmark is now the active sort. |
| 11 | Select Name - Ascending again (already-active option). | Dialog dismisses automatically without changing the catalog order — confirms the interaction is idempotent. |

| Field | Value |
|---|---|
| Post Condition | Product Catalog displayed with Name - Ascending as the active sort (matching the confirmed default state); all four sort options individually verified for option visibility, selection success, checkmark state, catalog refresh, and resulting order correctness |
| Requirement Traceability | FR-006 |
| Scenario Traceability | TS-006 |
| Automation Mapping | Script/Class/Method: `CartTest.sortProductCatalog()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `ProductsPage` (extended Phase 14.1 — `isSortDialogTitleDisplayed()`, and per-option `is*OptionDisplayed()`/`is*Selected()`/`tap*()` for all four options). Locators: `ProductsLocators` (extended Phase 14.1 — `SORT_DIALOG_TITLE` and 4 option/4 checkmark constants, sourced from MA-LOC-001 §7, source-decompiled). Test Data: Not Applicable — Sort option labels are fixed AUT UI text (locator constants, not test data); the Product Catalog's contents are AUT data owned by Sauce Labs, not framework test data (mirrors `ProductDataFactory`'s Pilot-Product-only scope). Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc006_01`–`tc006_07` (captured on each of 5 executions), framework log — Phase 14.1 (implementation) and Phase 14.2 (Enterprise Acceptance Review), this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Previously required a manual verification pass per MA-TS-001 §8 — **resolved Phase 14.2A** using Phase 14.1 real-device automated-execution evidence and Phase 14.2 Enterprise Acceptance Review: a dedicated automated method (`sortProductCatalog()`) now exists and passes repeatedly on the real device (5/5 executions this session across individual, repeat, Product Module suite, and full `CartTest` regression runs — zero flakiness; see Phase 14.2 Runtime Review). Automation Status changed from Blocked to Ready because the specific ambiguity that blocked it — the resulting catalog re-ordering behavior — is now fully resolved for all four options, not merely inferred. Notable finding (Phase 14.1, corroborated Phase 14.2): Name - Ascending is the Product Catalog's default sort state on app launch, consistent with, but not previously stated by, MA-LOC-001 §7 (which documented the other three checkmarks, but not this one, as `invisible` by default). Ordering correctness is verified dynamically (pairwise comparison across the currently-rendered catalog after each selection), never against a hardcoded expected product list, since the catalog's exact contents are AUT data, not test data this framework defines — see Automation Mapping. Zero framework modification occurred resolving this Test Case (confirmed independently, Phase 14.2 Framework Review, via file-modification-timestamp analysis). |

---

### TC-007 — Cart Icon and Badge Verification

| Field | Value |
|---|---|
| Test Case ID | TC-007 |
| Requirement ID | FR-007 |
| Scenario ID | TS-007 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Cart Icon and Badge Verification |
| Priority | Must |
| Automation Status | Ready — TS-007 Expected Result contains no "Out of Current Observation" qualifier |
| Verification Status | Partially Confirmed — cart icon and cart badge existence are confirmed (MA-AA-001 §5, §10); that the badge value specifically represents item count is inferred, not literally stated in MA-AA-001 |
| Objective | Verify the cart icon is displayed and its badge value reflects the current cart item count |
| Preconditions | An item has been added to the Cart (TC-012 executed) |
| Dependencies | TC-012 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Product Catalog screen for the cart icon. | Cart icon is displayed. |
| 2 | Observe the cart badge value. | Cart badge displays a value. |
| 3 | Compare the cart badge value to the number of items added to the Cart. | Cart badge value matches the number of items added. |

| Field | Value |
|---|---|
| Post Condition | Cart badge reflects current state |
| Requirement Traceability | FR-007 |
| Scenario Traceability | TS-007 |
| Automation Mapping | **Incidental coverage only (Phase 15.5A)** — no dedicated automated method exists for this Test Case. Steps 2-3 (cart badge value readable; badge value matches actual item count) are strongly incidentally confirmed via `ProductDetailsTest.addProductToCart()` (TC-012): `CommonAssertions.verifyVisible(productsPage.isCartBadgeDisplayed(), "Cart Badge")` and `CommonAssertions.verifyText(productsPage.getCartBadgeCount().orElse(null), "1", "Cart Badge value")`, asserted immediately after adding exactly one item — the badge value is compared against the true, known item count, not merely checked for presence. The same pattern repeats in `CartTest.verifyShippingScreen()` (used by TC-019/020/021/022/023/026). Confirmed via Phase 15.5 Enterprise Incidental Automation Review. Step 1 (cart icon itself, distinct from the badge overlay) is **not** covered — `ProductsLocators.CART_BUTTON` is only ever clicked (`ProductsPage.tapCart()`), never asserted displayed. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Step 3's exact matching semantics rely on a reasonable but not literally documented reading of "badge" as an item-count indicator; flagged for reviewer awareness even though Automation Status is Ready per the literal-text rule. **Reviewed Phase 15.5A**: Steps 2-3 are now incidentally confirmed with real runtime evidence (TC-012's badge-value-equals-actual-count assertion) — see Automation Mapping. This corroborates, but does not by itself resolve, the inference noted above. Step 1 remains uncovered. Automation Status/Verification Status/Execution Type intentionally left unchanged. |

---

### TC-008 — Navigation Drawer Access

| Field | Value |
|---|---|
| Test Case ID | TC-008 |
| Requirement ID | FR-008 |
| Scenario ID | TS-008 |
| Module | Product Browsing (MA-RS-001 §6.3) |
| Feature | Navigation Drawer Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the Navigation Drawer and its full item list are directly observed in MA-AA-001 §6 |
| Objective | Verify the Navigation Drawer opens from the Product Catalog and displays its confirmed items |
| Preconditions | Product Catalog screen is displayed |
| Dependencies | Not formally listed in MA-TD-001 §10; precondition requires Product Catalog to be displayed |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the menu icon on the Product Catalog screen. | Navigation Drawer is displayed. |
| 2 | Observe the Navigation Drawer contents. | Navigation Drawer displays Catalog, WebView, QR Code Scanner, Geo Location, Drawing, About, Reset App State, FingerPrint, Virtual USB, Crash App (Debug), and Login. |

| Field | Value |
|---|---|
| Post Condition | Navigation Drawer remains open, ready for TC-002, TC-028 |
| Requirement Traceability | FR-008 |
| Scenario Traceability | TS-008 |
| Automation Mapping | **Incidental coverage only (Phase 15.5A)** — no dedicated automated method exists for this Test Case. 9 of the 11 documented drawer items are individually, explicitly confirmed displayed: Login/Log In via `LoginTest.loginOutcomeVerification()` (TC-004) and `CartTest.proceedToCheckoutAnonymousUser()` (TC-018); WebView, QR Code Scanner, Geo Location, Drawing, About, FingerPrint, Virtual USB, and Reset App State via `NavigationTest.accessDrawerItems()` (TC-028) — each via `CommonAssertions.verifyVisible(productsPage.isDrawerItemDisplayed(itemText), "Navigation Drawer — " + itemText + " item")`. Confirmed via Phase 15.5 Enterprise Incidental Automation Review. The Catalog row is never independently asserted as a drawer item (it is the origin screen itself). Crash App (Debug) is permanently excluded from all automation by explicit design (MA-LOC-001 §14 row 9) — not a gap. No dedicated generic "drawer container displayed" assertion exists, but its presence is strongly implied by the 9 successful item-level assertions. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | **Reviewed Phase 15.5A** (Enterprise Incidental Automation Review, Phase 15.5): 9 of 11 drawer items incidentally confirmed via TC-004/TC-018/TC-028 — see Automation Mapping. The Catalog row remains genuinely unasserted; Crash App (Debug) is intentionally, permanently excluded, not an open item. Automation Status/Verification Status/Execution Type intentionally left unchanged. |

---

### TC-009 — Product Detail Navigation

| Field | Value |
|---|---|
| Test Case ID | TC-009 |
| Requirement ID | FR-009 |
| Scenario ID | TS-009 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Detail Navigation |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Product Details screen and its elements (image, title, price, rating) are directly observed in MA-AA-001, and the Catalog → Details transition is confirmed in the Navigation Summary |
| Objective | Verify tapping a product card opens Product Details with its confirmed elements |
| Preconditions | Product Catalog screen is displayed (TC-001 executed) |
| Dependencies | TC-001 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap a product card on the Product Catalog screen. | Product Details screen is displayed. |
| 2 | Observe the Product Details screen for the product image. | Product image is displayed. |
| 3 | Observe the Product Details screen for the product title. | Product title is displayed. |
| 4 | Observe the Product Details screen for the product price. | Product price is displayed. |
| 5 | Observe the Product Details screen for the product rating. | Product rating is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Details screen remains displayed, ready for TC-010, TC-011, TC-012, TC-013, TC-029, TC-032 |
| Requirement Traceability | FR-009 |
| Scenario Traceability | TS-009 |
| Automation Mapping | Script/Class/Method: `ProductDetailsTest.addProductToCart()` (`src/test/java/com/mobileautomation/framework/tests/ProductDetailsTest.java`) — Step 1 (tap Product Card → Product Details displayed) and Steps 2-4 (image, title, price displayed) are executed inline within the same method as TC-012, mirroring TC-002/TC-003's Phase 10.1A pattern; see TC-012's Reviewer Notes. Step 5 (product rating displayed) is **not** exercised — no rating-related locator or Page Object method exists anywhere in the framework (`ProductDetailsLocators`/`ProductDetailsPage` reviewed, Phase 10.2 evidence) — this step remains unautomated. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Added Phase 10.2 (Product Details Module Documentation Reconciliation): Automation Mapping populated for Steps 1-4 — this Test Case's core navigation and display-verification actions are already automated (incidentally, via TC-012's `ProductDetailsTest.addProductToCart()`), only the documentation was unsynchronized. Step 5 (product rating) is confirmed **not** automated — no rating locator exists anywhere in the framework. Automation Status and Execution Type intentionally left unchanged (Ready / Manual), consistent with TC-002/TC-003's precedent, since no dedicated automated method implements this Test Case's own 5-step flow. |

---

### TC-010 — Product Color Selection

| Field | Value |
|---|---|
| Test Case ID | TC-010 |
| Requirement ID | FR-010 |
| Scenario ID | TS-010 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Color Selection |
| Priority | Should |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — color selection control existence and interactability are now runtime-confirmed (Phase 10.3, real-device execution, this session); the precise visual mechanism for a selected-state *change* remains not confirmed — the Pilot Product renders only one color swatch, so no actual state transition was observable this session |
| Objective | Verify a color option can be selected and visibly reflects a selected state |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `ProductDetailsTest.selectProductColor()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap a color selection option on the Product Details screen. | Color selection option responds to activation — confirmed Phase 10.3 (tap succeeds without error). |
| 2 | Observe the color selection option after activation. | Selected color option visibly reflects a selected state — confirmed only in the sense that the swatch's content-description remains a real, non-empty, correctly-read value before and after the tap; no actual visual *change* was observable for the Pilot Product's single rendered swatch (Phase 10.3 evidence). |

| Field | Value |
|---|---|
| Post Condition | Selected color state persists on screen — verified for the Pilot Product's single rendered swatch (Phase 10.3 evidence, this session) |
| Requirement Traceability | FR-010 |
| Scenario Traceability | TS-010 |
| Automation Mapping | Script/Class/Method: `ProductDetailsTest.selectProductColor()` (`src/test/java/com/mobileautomation/framework/tests/ProductDetailsTest.java`). Page Objects: `ProductsPage` (Catalog navigation/Product Card location), `ProductDetailsPage` (`isColorSelectorDisplayed()`, `getSelectedColorDescription()`, `selectColor(String)`). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 2 screenshots (`tc010_01_product_details_after_navigation`, `tc010_02_color_selector_after_interaction`) plus framework log — Phase 10.3, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Reviewer should confirm exact visual indicator of "selected state" during implementation-phase inspection — **still open**, see below. Noted Phase 10.2: `ProductDetailsTest.addProductToCart()` (TC-012) incidentally verifies the Color Selector's displayed state. **Updated Phase 10.4A**: this Test Case now has its own dedicated automated method (`selectProductColor()`, Phase 10.3), populated above. The automation confirms the control is displayed and interactable (tap succeeds) but — per real, evidence-based limitation, not a testing oversight — cannot demonstrate an actual selected-state *change*, because the Pilot Product renders exactly one color swatch with no alternative to switch to. This Test Case's original ambiguity (exact visual indicator of a selected-state change) therefore remains genuinely open for any product with multiple color options; it is not resolved by this automation and should not be treated as such. |

---

### TC-011 — Product Quantity Selection

| Field | Value |
|---|---|
| Test Case ID | TC-011 |
| Requirement ID | FR-011 |
| Scenario ID | TS-011 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Quantity Selection |
| Priority | Must |
| Automation Status | Ready — resolved Phase 10.4A (was Blocked; see Reviewer Notes) |
| Verification Status | Partially Confirmed — Phase 10.3 runtime evidence (real-device execution, this session) confirms Increase/Decrease each change the quantity by exactly 1, and that reducing quantity to zero disables the Add To Cart button. The selector's maximum bound and the Decrease control's own enabled/disabled state at the zero floor were not exercised and remain unconfirmed |
| Objective | Verify the quantity selector on Product Details accepts a value change |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `ProductDetailsTest.adjustProductQuantity()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Interact with the quantity selector on the Product Details screen. | Quantity selector responds to interaction — confirmed Phase 10.3 (Increase/Decrease taps succeed without error). |
| 2 | Observe the quantity value after interaction. | Quantity value increases/decreases by exactly 1 per tap; quantity can reach zero, at which point the Add To Cart button becomes disabled — confirmed via Phase 10.3 real-device evidence (this session). Maximum bound was not exercised and remains Pending Manual Verification / Out of Current Observation. |

| Field | Value |
|---|---|
| Post Condition | Quantity Selector confirmed to accept Increase/Decrease interactions, changing the value by exactly 1 per tap; decrementing to zero disables the Add To Cart button. Maximum bound not exercised — Phase 10.3 evidence, this session |
| Requirement Traceability | FR-011 |
| Scenario Traceability | TS-011 |
| Automation Mapping | Script/Class/Method: `ProductDetailsTest.adjustProductQuantity()` (`src/test/java/com/mobileautomation/framework/tests/ProductDetailsTest.java`). Page Objects: `ProductsPage` (Catalog navigation/Product Card location), `ProductDetailsPage` (`isQuantitySelectorDisplayed()`, `isQuantitySelectorInteractable()`, `getQuantityValue()`, `increaseQuantity()`, `decreaseQuantity()`, `isAddToCartButtonEnabled()`). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 3 screenshots (`tc011_02_quantity_after_increase`, `tc011_03_quantity_after_decrease`, `tc011_04_quantity_after_floor_decrease`) plus framework log — Phase 10.3, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | Previously required a manual verification pass per MA-TS-001 §8 — **resolved Phase 10.4A** using Phase 10.3 runtime evidence: a dedicated automated method (`adjustProductQuantity()`) now exists and passes repeatedly on the real device (8/8 executions this session, zero flakiness — see Phase 10.4 Enterprise Review). Automation Status changed from Blocked to Ready because the specific ambiguity that blocked it — "exact control mechanics" — is now substantially resolved for the tested scope (step size, zero-floor behavior and its Add-To-Cart consequence). Scoped precisely, per this phase's "do not infer unsupported business rules" rule: the selector's **maximum bound** and the **Decrease control's own enabled/disabled state** at the zero floor were never exercised (only the downstream Add-To-Cart consequence was) and remain genuinely open — not resolved by this reconciliation, not to be assumed. |

---

### TC-012 — Add to Cart Action

| Field | Value |
|---|---|
| Test Case ID | TC-012 |
| Requirement ID | FR-012 |
| Scenario ID | TS-012 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Add to Cart Action |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Verified — Verified Manual Execution Evidence, 2026-08-02 (this session, live Appium Inspector capture); corroborated by MA-LOC-001 §6, §8 |
| Objective | Verify that the verified Pilot Product can be successfully opened from the Product Catalog, its Product Details are displayed correctly, and it can be successfully added to the Cart while the Cart Badge and Cart contents correctly reflect the completed business transaction. |
| Preconditions | Product Catalog is displayed. The Pilot Product has been loaded from `ProductDataFactory.pilotProduct()`. The Product Card matching the expected Product Name has been identified. The Product Card identity has been verified before navigation. |
| Dependencies | TC-001 (MA-TD-001 §10) — supersedes the previous TC-009 dependency; Product Selection is now exercised within this Test Case's own steps (see Reviewer Notes) |
| Test Data | Source: `ProductDataFactory.pilotProduct()` (`testdata/common/product/pilot.json`). Current verified Pilot Dataset — Reference: MA-TDD-001 §8.4 (TD-004) — **Verified**: Product Name `Sauce Labs Backpack (violet)`; Price as supplied by the data source, not duplicated or hardcoded here. |
| Execution Type | Automated — `ProductDetailsTest.addProductToCart()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Load the Pilot Product from `ProductDataFactory.pilotProduct()`. | Pilot Product (Product Name, Price) is loaded successfully. |
| 2 | Locate the Product Card matching the expected Product Name in the Product Catalog. | A Product Card whose name matches the loaded Product Name is found in the currently-rendered catalog. |
| 3 | Verify the Product Card's displayed Product Name. | Displayed name matches the loaded Product Name. |
| 4 | Verify the Product Card's displayed Product Price. | Displayed price matches the loaded Price. |
| 5 | Tap the Product Image belonging to the verified Product Card. | Product Image responds to activation. |
| 6 | Verify the Product Details screen is displayed. | Product Details screen is displayed. |
| 7 | Verify the displayed Product Name on Product Details. | Displayed Product Name matches the loaded Product Name. |
| 8 | Verify the displayed Product Price on Product Details. | Displayed Price matches the loaded Price. |
| 9 | Verify the Add To Cart button. | Add To Cart button is displayed and interactable. |
| 10 | Verify the Quantity Selector. | Quantity Selector (decrease control, value, increase control) is displayed and interactable. |
| 11 | Verify the Color Selector. | Color Selector is displayed. Its content-description reads "Unknown color" — **not** "Violet" — because "Violet" is not one of the AUT's four recognized color constants (Black/Green/Gray/Blue); it correctly falls through to the documented default (MA-LOC-001 §8). This is expected, verified behavior, not a defect. |
| 12 | Tap the Add To Cart button. | Add To Cart button responds to activation. |
| 13 | Verify the header Cart Badge. | Cart Badge appears (previously absent) showing item count 1. |
| 14 | Open the Cart and verify the selected product. | Cart screen displays the loaded Product Name and Price as a line item; no unexpected application error occurs. |

| Field | Value |
|---|---|
| Post Condition | The selected Pilot Product has been successfully added to the Cart. The Cart Badge reflects the completed business action (count 1). The Cart contains the selected product with its correct Product Name and Price. |
| Requirement Traceability | FR-012 |
| Scenario Traceability | TS-012 |
| Automation Mapping | Script/Class/Method: `ProductDetailsTest.addProductToCart()` (`src/test/java/com/mobileautomation/framework/tests/ProductDetailsTest.java`). **Updated Phase 10.2**: previously this field stated the implementation had not yet been updated to the 14-step flow below (a Phase 9.5A note that predated the Phase 9.5B implementation). Fresh code review this session confirms that claim is now stale — the method's inline comments and structure (`// Step 1`, `// Steps 2-4`, ... `// Step 14`) match this Test Case's 14 steps directly, including the Assertion Order sequence described in the class Javadoc. No further code update is needed for this mapping to be accurate. Evidence: screenshots `tc012_01_catalog_before_selection`, `tc012_01b_product_card_verified`, `tc012_02_product_details_after_navigation`, `tc012_03_cart_after_add_to_cart`, plus framework log — cumulative real-device execution evidence dating back to Phase 9.5C (added Phase 14.3A; this citation was previously missing despite the evidence already existing on disk). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | (1) Previous Blocked status is resolved using verified manual execution evidence collected during Pilot Automation (Task #16, prior session) and reconfirmed with fresh evidence this session (2026-08-02) against the newly approved Pilot Product. (2) Product Details navigation is achieved by tapping the Product Image belonging to the verified Product Card — the Product Card's Name and Price are verified before navigation, not after. (3) Product Card identity must always be verified before navigation — this supersedes the previous Reviewer Note that assigned Product Selection exclusively to TC-009; TC-012's pilot flow now performs its own Product Card verification as an explicit precondition step, while TC-009 continues to independently cover the generic (any-card) case. (4) Step 11's Color Selector expected result ("Unknown color") deviates from a generic "Violet" assumption — verified from this session's actual evidence, not inferred; see MA-LOC-001 §8 for the source-confirmed fallback rule. (5) Added Phase 10.2: the Automation Mapping's stale "not yet updated to the 14-step flow" claim (carried over from Phase 9.5A, before the Phase 9.5B implementation landed) is corrected — see Automation Mapping field above. (6) **Corrected Phase 14.3A**: the Execution Type field had read "Manual — baseline for future automation" despite this Test Case's own Automation Mapping (above) having been fully populated and code-verified since Phase 10.2, and `ProductDetailsTest.addProductToCart()` having executed successfully with cumulative runtime evidence dating back to Phase 9.5C — this was a stale field left over from before the automation existed, independently discovered during Phase 14.3's Product Module Final Baseline Freeze verification. Corrected to "Automated" to match every other field on this Test Case and the pattern used by every other automated Test Case in this document; no code, behavior, or other field changed. |

---

### TC-013 — Product Details Scroll Support

| Field | Value |
|---|---|
| Test Case ID | TC-013 |
| Requirement ID | FR-013 |
| Scenario ID | TS-013 |
| Module | Product Details (MA-RS-001 §6.4) |
| Feature | Product Details Scroll Support |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — scrollable content and Product Highlights are both directly observed together on the Product Details screen in MA-AA-001; now also runtime-corroborated (Phase 10.3, real-device execution, this session) |
| Objective | Verify scrolling the Product Details screen reveals Product Highlights |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `ProductDetailsTest.scrollToProductHighlights()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Scroll down on the Product Details screen. | Screen content scrolls — confirmed Phase 10.3. |
| 2 | Observe the screen after scrolling. | Product Highlights content is displayed — confirmed Phase 10.3; the screen was additionally verified to scroll back and remain fully available (Product Name/Price still correct) afterward, confirming no unexpected UI behavior occurred. |

| Field | Value |
|---|---|
| Post Condition | Product Highlights remain visible; Product Details screen confirmed to remain intact and available after scrolling back (Phase 10.3 evidence, this session) |
| Requirement Traceability | FR-013 |
| Scenario Traceability | TS-013 |
| Automation Mapping | Script/Class/Method: `ProductDetailsTest.scrollToProductHighlights()` (`src/test/java/com/mobileautomation/framework/tests/ProductDetailsTest.java`). Page Objects: `ProductsPage` (Catalog navigation/Product Card location), `ProductDetailsPage` (`scrollToHighlights()`, `isProductHighlightsDisplayed()`, `scrollToTop()`). Supporting Components: `utils.ScrollUtility#scrollToResourceId(String)`/`#scrollUp()` (Phase 5, reused unmodified). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 3 screenshots (`tc013_01_product_details_after_navigation`, `tc013_02_highlights_after_scroll`, `tc013_03_product_details_after_scroll_back`) plus framework log — Phase 10.3, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Added Phase 10.4A: this Test Case now has a dedicated automated method (`scrollToProductHighlights()`, Phase 10.3), populated above. This Test Case had no prior ambiguity (Verification Status was already Confirmed pre-automation) — automation only adds runtime corroboration, it does not resolve any open question. |

---

### TC-014 — Cart Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-014 |
| Requirement ID | FR-014 |
| Scenario ID | TS-014 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Cart screen and the cart-icon navigation path are directly confirmed in MA-AA-001 Navigation Summary; now also runtime-corroborated (Phase 11.2, real-device execution, this session) |
| Objective | Verify tapping the cart icon opens the Cart screen with items and total displayed |
| Preconditions | An item has been added to the Cart (TC-012 executed) |
| Dependencies | TC-012 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.accessCartScreen()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the cart icon on the Product Catalog screen. | Cart screen is displayed — confirmed Phase 11.2. |
| 2 | Observe the Cart screen for cart items. | Cart items are displayed — confirmed Phase 11.2 (correct Product Name and Price). |
| 3 | Observe the Cart screen for the total. | Total is displayed — confirmed Phase 11.2 (both Total Items Count and Total Price sections). |

| Field | Value |
|---|---|
| Post Condition | Cart screen remains displayed, ready for TC-015, TC-016, TC-017, TC-018 |
| Requirement Traceability | FR-014 |
| Scenario Traceability | TS-014 |
| Automation Mapping | Script/Class/Method: `CartTest.accessCartScreen()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `ProductsPage`, `ProductDetailsPage` (navigation/Add to Cart), `CartPage` (`isDisplayed()`, `getFirstItemName()`, `getFirstItemPrice()`, `isTotalItemsDisplayed()`, `isTotalPriceDisplayed()`). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 2 screenshots (`tc014_01_cart_after_navigation`, `tc014_02_cart_screen_verified`) plus framework log — Phase 11.2, this session, real-device execution (vivo I2301). |
| Execution Group | Smoke (MA-TP-001 §10 names Cart access as part of the Smoke stage) |
| Execution Tags | `@regression` |
| Reviewer Notes | Added Phase 11.5A: reaches the Cart via `CartTest.addPilotProductToCartAndOpenCart()`, a private helper that composes the same `ProductsPage`/`ProductDetailsPage` navigation `ProductDetailsTest.addProductToCart()` (TC-012) uses — TC-012 itself is not called or reimplemented. |

---

### TC-015 — Cart Item Quantity Update

| Field | Value |
|---|---|
| Test Case ID | TC-015 |
| Requirement ID | FR-015 |
| Scenario ID | TS-015 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Item Quantity Update |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the causal relationship between quantity change and total recalculation, previously a reasonable but unobserved inference from MA-AA-001 §10, is now literally confirmed via Phase 11.3 real-device evidence (this session): increasing quantity 1→2 changed Total Items "1 Items"→"2 Items" and Total Price "$ 29.99"→"$ 59.98"; decreasing returned both to their initial values |
| Objective | Verify updating item quantity on the Cart screen recalculates the displayed total |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.updateCartItemQuantity()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Update the quantity of a Cart item using the quantity control. | Quantity control responds to the update — confirmed Phase 11.3 (quantity changes by exactly 1 per tap, both Increase and Decrease). |
| 2 | Observe the Cart total after the update. | Cart total displays an updated value — confirmed Phase 11.3: Total Items and Total Price both recalculate correctly (unit price × quantity), for an increased quantity (2) and back to the initial quantity (1). |

| Field | Value |
|---|---|
| Post Condition | Cart total reflects updated quantity — confirmed Phase 11.3, this session |
| Requirement Traceability | FR-015 |
| Scenario Traceability | TS-015 |
| Automation Mapping | Script/Class/Method: `CartTest.updateCartItemQuantity()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `ProductsPage`, `ProductDetailsPage` (navigation/Add to Cart), `CartPage` (`isIncreaseButtonDisplayed()`, `isDecreaseButtonDisplayed()`, `getQuantity()`, `increaseQuantity()`, `decreaseQuantity()`, `getTotalItems()`, `getTotalPrice()`). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 3 screenshots (`tc015_02_cart_initial_state`, `tc015_03_cart_after_increase`, `tc015_04_cart_after_decrease`) plus framework log — Phase 11.3, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | Previous note ("causal relationship... is a reasonable inference... not a literal observation") is **resolved Phase 11.5A** using Phase 11.3 runtime evidence — see Verification Status. Tagged `@critical` because this action directly mutates the Cart's business-critical total, alongside Add to Cart (TC-012). |

---

### TC-016 — Cart Item Removal

| Field | Value |
|---|---|
| Test Case ID | TC-016 |
| Requirement ID | FR-016 |
| Scenario ID | TS-016 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Item Removal |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Remove Item control is confirmed in MA-AA-001, and the resulting update to the cart summary upon removal, previously inferred, is now literally confirmed via Phase 11.4 real-device evidence (this session): the item's row disappears, the empty-state indicator ("No Items") is displayed, and the Total Items/Total Price sections become hidden entirely |
| Objective | Verify the Remove Item action removes the selected item and updates the cart summary |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.removeCartItem()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Remove Item action for a Cart item. | Item is removed from the Cart — confirmed Phase 11.4. A source-confirmed 5-second simulated UI-thread block occurs on this tap (LOCATOR_REPOSITORY.md line 185); automation waits on the real UI condition (row invisibility), not a fixed sleep. |
| 2 | Observe the Cart summary after removal. | Cart summary displays the updated item list — confirmed Phase 11.4: the empty-state indicator is displayed, and Total Items/Total Price become hidden (not zeroed-but-visible). |

| Field | Value |
|---|---|
| Post Condition | Cart summary reflects removal — confirmed Phase 11.4, this session: empty-state indicator displayed, Total Items/Total Price hidden |
| Requirement Traceability | FR-016 |
| Scenario Traceability | TS-016 |
| Automation Mapping | Script/Class/Method: `CartTest.removeCartItem()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `ProductsPage`, `ProductDetailsPage` (navigation/Add to Cart), `CartPage` (`isItemPresent()`, `getQuantity()`, `getTotalPrice()`, `removeItem()`, `isCartEmpty()`, `getEmptyCartMessage()`, `isTotalItemsDisplayed()`, `isTotalPriceDisplayed()`). Supporting Components: `utils.WaitUtility#waitForInvisibility(By)` (called directly from `CartPage.removeItem()`, since `ElementActions` does not currently expose an invisibility-wait wrapper — see `CartPage` class Javadoc). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 4 screenshots (`tc016_01_cart_after_navigation` through `tc016_04_cart_empty_state`) plus framework log — Phase 11.4, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | Previous note ("resulting update to the cart summary... is inferred, not literally observed") is **resolved Phase 11.5A** using Phase 11.4 runtime evidence — see Verification Status. Tagged `@critical` because this action directly mutates the Cart's business-critical contents, alongside Add to Cart (TC-012). Added Phase 11.5A: the Locator Repository (LOCATOR_REPOSITORY.md line 183, source-code-derived, not this phase's runtime evidence) documents a second, distinct removal path — decreasing an item's quantity to 0 also removes it (`CartItemAdapter.onClick`, `minusIV`). This alternate path has never been exercised by any automated test (TC-015 only decreases from 2 back to 1) and remains a genuine, disclosed automation gap for a future phase, not a defect. |

---

### TC-017 — Cart Total Verification

| Field | Value |
|---|---|
| Test Case ID | TC-017 |
| Requirement ID | FR-017 |
| Scenario ID | TS-017 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Cart Total Verification |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Cart total display is confirmed in MA-AA-001, and the price × quantity summation logic, previously inferred, is now literally confirmed via real-device evidence: unit price × 1 = "$ 29.99" (Phase 11.2/11.5) and unit price × 2 = "$ 59.98" (Phase 11.3, this session) |
| Objective | Verify the Cart total reflects the sum of item price and quantity as displayed |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.verifyCartTotal()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the price and quantity of each item on the Cart screen. | Price and quantity are displayed for each item — confirmed Phase 11.2/11.5. |
| 2 | Observe the displayed Cart total. | Cart total is displayed — confirmed Phase 11.2/11.5 (both Total Items Count and Total Price sections). |
| 3 | Compare the displayed total to the sum of displayed item price and quantity values. | Displayed total matches the calculated sum of displayed item values — confirmed Phase 11.2/11.5 for quantity 1 ("$ 29.99"); further corroborated by Phase 11.3's quantity-2 evidence ("$ 59.98") under TC-015. |

| Field | Value |
|---|---|
| Post Condition | Cart total verified against displayed line items — confirmed Phase 11.2/11.5, this session |
| Requirement Traceability | FR-017 |
| Scenario Traceability | TS-017 |
| Automation Mapping | Script/Class/Method: `CartTest.verifyCartTotal()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `ProductsPage`, `ProductDetailsPage` (navigation/Add to Cart), `CartPage` (`isDisplayed()`, `isTotalItemsDisplayed()`, `isTotalPriceDisplayed()`, `getTotalItems()`, `getTotalPrice()`). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, per-test node. Evidence: 2 screenshots (`tc017_01_cart_after_navigation`, `tc017_02_total_verified`) plus framework log — Phase 11.2, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Previous note ("exact price × quantity summation logic is inferred") is **resolved Phase 11.5A** using Phase 11.2 and Phase 11.3 runtime evidence — see Verification Status. Not tagged `@critical`: this Test Case is a read-only verification, not a state-mutating action (unlike TC-015/TC-016/TC-012). |

---

### TC-018 — Proceed to Checkout Action

| Field | Value |
|---|---|
| Test Case ID | TC-018 |
| Requirement ID | FR-018 |
| Scenario ID | TS-018 |
| Module | Cart (MA-RS-001 §6.5) |
| Feature | Proceed to Checkout Action |
| Priority | Must |
| Automation Status | Ready — resolved Phase 11.9A (was Blocked; see Reviewer Notes) |
| Verification Status | Confirmed — Proceed to Checkout button is confirmed, and the next screen and its sequence relative to Login, previously unconfirmed, is now fully confirmed via Phase 11.7 real-device evidence (this session): conditional on authentication state, with Cart contents preserved throughout |
| Objective | Verify the Proceed to Checkout button is interactable and correctly navigates according to authentication state |
| Preconditions | Cart screen is displayed with at least one item (TC-014 executed) |
| Dependencies | TC-014 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.proceedToCheckoutAnonymousUser()` / `proceedToCheckoutAuthenticatedUser()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Proceed to Checkout button on the Cart screen. | Proceed to Checkout button responds to activation — confirmed Phase 11.7/11.8. |
| 2 | Observe the screen displayed after activation. | Conditional on authentication state — confirmed Phase 11.7/11.8: if anonymous, the Login screen is displayed; if already authenticated, the Shipping Address screen is displayed directly. |
| 3 | If redirected to Login (anonymous case only), authenticate with valid credentials. | Login succeeds; the interrupted checkout flow automatically resumes, landing directly on the Shipping Address screen — confirmed Phase 11.7/11.8. |

| Field | Value |
|---|---|
| Post Condition | Anonymous user: Login screen reached, then Shipping Address screen reached after successful login. Authenticated user: Shipping Address screen reached directly. Cart contents (the Pilot Product) preserved throughout both flows — confirmed Phase 11.7/11.8, this session |
| Requirement Traceability | FR-018 |
| Scenario Traceability | TS-018 |
| Automation Mapping | Script/Class/Method: `CartTest.proceedToCheckoutAnonymousUser()` and `CartTest.proceedToCheckoutAuthenticatedUser()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`) — two independent methods, one per verified authentication-state scenario. Page Objects: `ProductsPage`, `ProductDetailsPage` (navigation/Add to Cart), `CartPage` (`isProceedToCheckoutButtonDisplayed()`, `isProceedToCheckoutButtonEnabled()`, `tapProceedToCheckout()`), `LoginPage` (reused unmodified from TC-004), `CheckoutPage` (`isDisplayed()`, `isSubtitleDisplayed()`, `areAllFieldsDisplayed()` — new, minimally-scoped Page Object added Phase 11.8 for the Shipping Address screen). Supporting Components: `utils.WaitUtility#waitUntil(Function)` (the existing custom-wait escape hatch, used to branch on which of the two screens appears — no new WaitUtility capability). Test Data: `TestDataManager.productData().pilotProduct()` (MA-TDD-001 §8.4, TD-004) and `TestDataManager.loginData().standardCredentials()` (MA-TDD-001 §8.1.1, TD-001). Execution Strategy: `ISOLATED` (MA-FA-001/Phase 9.5I). Reporting: Extent Report, two per-test nodes. Evidence: 3 screenshots per method (`tc018a_02`–`tc018a_04` anonymous; `tc018b_02`–`tc018b_03` authenticated) plus framework log — Phase 11.7 (runtime investigation) and Phase 11.8 (automated implementation), this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | **Resolved Phase 11.9A** using Phase 11.7's real-device runtime investigation (direct device interaction, not automated code) and Phase 11.8's subsequent automated implementation — see Automation Mapping. Downstream Checkout Test Cases (TC-019–TC-027) assumed this transition succeeds; that assumption is now confirmed correct by direct evidence, not merely presumed. Tagged `@critical`: this action gates the entire downstream Checkout chain. Implemented as two independent `@Test` methods (not one) because the two authentication-state branches are genuinely different, separately-verified flows — TestNG reports each with its own pass/fail node, matching how Phase 11.7's investigation itself treated them as Scenario A and Scenario B. |

---

### TC-019 — Shipping Address Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-019 |
| Requirement ID | FR-019 |
| Scenario ID | TS-019 |
| Module | Checkout — Shipping (MA-RS-001 §6.6) |
| Feature | Shipping Address Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — all seven Shipping Address fields are directly listed as observed in MA-AA-001, and individually re-confirmed via repeated real-device automated execution (Phase 12.2, 12.4, 12.6, 12.7, 13.2, 13.4) |
| Objective | Verify the Shipping Address screen displays all required fields |
| Preconditions | Checkout flow initiated (TC-018 executed) |
| Dependencies | TC-018 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.accessShippingScreen()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate to the Shipping Address screen (conditional Login branch handled per TC-018's verified flow). | Shipping Address screen is reached. |
| 2 | Observe the Checkout title and Shipping subtitle. | Both are displayed. |
| 3 | Observe the Shipping Address screen for the Full Name field. | Full Name field is displayed. |
| 4 | Observe the Shipping Address screen for the Address Line 1 field. | Address Line 1 field is displayed. |
| 5 | Observe the Shipping Address screen for the Address Line 2 field. | Address Line 2 field is displayed. |
| 6 | Observe the Shipping Address screen for the City field. | City field is displayed. |
| 7 | Observe the Shipping Address screen for the State/Region field. | State/Region field is displayed. |
| 8 | Observe the Shipping Address screen for the Zip Code field. | Zip Code field is displayed. |
| 9 | Observe the Shipping Address screen for the Country field. | Country field is displayed. |

| Field | Value |
|---|---|
| Post Condition | Shipping Address screen remains displayed, ready for TC-020 |
| Requirement Traceability | FR-019 |
| Scenario Traceability | TS-019 |
| Automation Mapping | Script/Class/Method: `CartTest.accessShippingScreen()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`), via the shared private helper `CartTest.navigateToShippingScreen(ProductItem, String)`. Page Objects: `ProductsPage`, `ProductDetailsPage`, `CartPage`, `LoginPage` (navigation, reused unmodified from TC-018), `CheckoutPage` (`isDisplayed()`, `isSubtitleDisplayed()`, `isFullNameFieldDisplayed()`, `isAddressLine1FieldDisplayed()`, `isAddressLine2FieldDisplayed()`, `isCityFieldDisplayed()`, `isStateFieldDisplayed()`, `isZipCodeFieldDisplayed()`, `isCountryFieldDisplayed()`). Locators: `CheckoutLocators` (`SHIPPING_TITLE`, `SHIPPING_SUBTITLE`, and the seven field locators — all MA-LOC-001 §10, source-confirmed). Test Data: `TestDataManager.productData().pilotProduct()` (TD-004), `.loginData().standardCredentials()` (TD-001, anonymous-flow branch). Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc019_01`–`tc019_03`, framework log — Phase 12.2 (implementation), corroborated by repeated passing suite runs in Phase 12.4/12.6/12.7/13.2/13.4, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Read-only verification Test Case (no field mutation), tagged `@regression` only, consistent with the established tagging convention (read-only → `@regression`; state-mutating → `@regression`+`@critical`). Reaches the screen via the shared `navigateToShippingScreen()` helper, which itself reuses TC-018's proven, conditional-Login-aware navigation — no duplicated navigation logic. **Documentation reconciled Phase 13.4A**: this Test Case's automation has existed and passed repeatedly since Phase 12.2; only the documentation was unsynchronized (identified in Phase 13.1's cross-module audit, re-confirmed in Phase 13.4's Checkout Module freeze verification). |

---

### TC-020 — Shipping Address Data Entry

| Field | Value |
|---|---|
| Test Case ID | TC-020 |
| Requirement ID | FR-020 |
| Scenario ID | TS-020 |
| Module | Checkout — Shipping (MA-RS-001 §6.6) |
| Feature | Shipping Address Data Entry |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — all seven fields are classified as input fields in MA-AA-001 §9, and individually re-confirmed (entry and read-back) via repeated real-device automated execution (Phase 12.2, 12.4, 12.6, 12.7, 13.2, 13.4) |
| Objective | Verify each Shipping Address field accepts text input |
| Preconditions | Shipping Address screen is displayed (TC-019 executed) |
| Dependencies | TC-018 (MA-TD-001 §10) |
| Test Data | Shipping Address — MA-TDD-001 §8.2, TD-002 (Ready) |
| Execution Type | Automated — `CartTest.enterShippingAddressData()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate to the Shipping Address screen. | Shipping Address screen is reached. |
| 2 | Load the Shipping Address dataset (TD-002). | Dataset loaded via `TestDataManager.shippingData().standardAddress()`, never hardcoded. |
| 3 | Enter a value into the Full Name field. | Full Name field contains the entered value. |
| 4 | Enter a value into the Address Line 1 field. | Address Line 1 field contains the entered value. |
| 5 | Enter a value into the Address Line 2 field. | Address Line 2 field contains the entered value. |
| 6 | Enter a value into the City field. | City field contains the entered value. |
| 7 | Enter a value into the State/Region field. | State/Region field contains the entered value. |
| 8 | Enter a value into the Zip Code field. | Zip Code field contains the entered value. |
| 9 | Enter a value into the Country field. | Country field contains the entered value. |

| Field | Value |
|---|---|
| Post Condition | All Shipping Address fields populated with TD-002 values, each individually read back and verified |
| Requirement Traceability | FR-020 |
| Scenario Traceability | TS-020 |
| Automation Mapping | Script/Class/Method: `CartTest.enterShippingAddressData()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`), via `CartTest.navigateToShippingScreen(ProductItem, String)`. Page Objects: `CheckoutPage` (`enterShippingAddress(ShippingAddress)` — composes `enterFullName()`/`enterAddressLine1()`/`enterAddressLine2()`/`enterCity()`/`enterState()`/`enterZipCode()`/`enterCountry()` — and the seven `get*()` read-back methods). Locators: `CheckoutLocators` (the seven field locators, MA-LOC-001 §10). Test Data: `TestDataManager.shippingData().standardAddress()` (MA-TDD-001 §8.2, TD-002, new pipeline built Phase 12.2 — `ShippingAddress` record, `ShippingDataFactory`, `testdata/common/shipping/address.json`), plus `productData().pilotProduct()`/`loginData().standardCredentials()` for shared navigation. Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc020_01`–`tc020_03`, framework log — Phase 12.2 (implementation), corroborated by repeated passing suite runs in Phase 12.4/12.6/12.7/13.2/13.4, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | Field values are not invented — sourced exclusively from TD-002 (MA-TDD-001 §8.2). Tagged `@critical`: this Test Case mutates the Shipping form's field state (data entry), consistent with the Phase 11.5A tagging convention. **Documentation reconciled Phase 13.4A**: this Test Case's automation and its TD-002 test-data pipeline have existed and passed repeatedly since Phase 12.2; the Test Data field previously read "Pending Test Data Design (Future MA-TDD-001)" — a reference that had literally pre-dated MA-TDD-001's own existence and was never updated once TD-002 was built — now corrected to cite MA-TDD-001 §8.2 directly. Identified in Phase 13.1's cross-module audit, re-confirmed in Phase 13.4's Checkout Module freeze verification. |

---

### TC-021 — Proceed to Payment Action

| Field | Value |
|---|---|
| Test Case ID | TC-021 |
| Requirement ID | FR-021 |
| Scenario ID | TS-021 |
| Module | Checkout — Shipping (MA-RS-001 §6.6) |
| Feature | Proceed to Payment Action |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the "To Payment" → Payment Method transition is directly confirmed in MA-AA-001 Navigation Summary, and re-confirmed via repeated real-device automated execution (Phase 12.2, 12.4, 12.6, 12.7, 13.2, 13.4). Real-device evidence additionally confirmed that this action requires the Shipping Address fields to be populated first — tapping "To Payment" against an empty form is blocked by the AUT's own client-side validation (surfaces "Please provide your..." field errors), which is why TS-021's own Precondition ("Shipping Address fields are populated") is not optional decoration |
| Objective | Verify the "To Payment" button navigates to the Payment Method screen |
| Preconditions | Shipping Address fields are populated (TC-020 executed) |
| Dependencies | TC-018 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.proceedToPayment()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate to the Shipping Address screen and populate all fields (TS-021 Precondition). | Fields populated with TD-002 data. |
| 2 | Observe the "To Payment" button. | Displayed and enabled. |
| 3 | Tap the "To Payment" button on the Shipping Address screen. | Button responds to activation. |
| 4 | Observe the screen after activation. | Payment Method screen is displayed — confirmed via the Shipping Address title becoming invisible, without referencing the destination screen. |

| Field | Value |
|---|---|
| Post Condition | Payment Method screen displayed, ready for TC-022, TC-023 |
| Requirement Traceability | FR-021 |
| Scenario Traceability | TS-021 |
| Automation Mapping | Script/Class/Method: `CartTest.proceedToPayment()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`), via `CartTest.navigateToShippingScreen(ProductItem, String)`. Page Objects: `CheckoutPage` (`enterShippingAddress(ShippingAddress)`, `isToPaymentButtonDisplayed()`, `isToPaymentButtonEnabled()`, `tapToPayment()` — waits for the Shipping title's own invisibility via `WaitUtility.waitForInvisibility(By)`, deliberately without referencing any Payment-screen locator). Locators: `CheckoutLocators.TO_PAYMENT_BUTTON` (accessibility id, MA-LOC-001 §10 — never the reused bare resource-id `paymentBtn`). Test Data: `TestDataManager.shippingData().standardAddress()` (TD-002), plus `productData().pilotProduct()`/`loginData().standardCredentials()` for shared navigation. Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc021_01`–`tc021_04`, framework log — Phase 12.2 (implementation, including the real-device-confirmed empty-form-validation finding), corroborated by repeated passing suite runs in Phase 12.4/12.6/12.7/13.2/13.4, this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | Tagged `@critical`: this action gates entry to the entire downstream Payment/Review-Order/Place-Order chain (TC-022–026), the same rationale TC-018 established for gating the Checkout chain. Deliberately does not reference, validate, or assert anything about the Payment screen itself — that is TC-022/TC-023's scope. **Documentation reconciled Phase 13.4A**: this Test Case's automation has existed and passed repeatedly since Phase 12.2; only the documentation was unsynchronized. Identified in Phase 13.1's cross-module audit, re-confirmed in Phase 13.4's Checkout Module freeze verification. |

---

### TC-022 — Payment Method Screen Access

| Field | Value |
|---|---|
| Test Case ID | TC-022 |
| Requirement ID | FR-022 |
| Scenario ID | TS-022 |
| Module | Checkout — Payment (MA-RS-001 §6.7) |
| Feature | Payment Method Screen Access |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — Payment Method screen and all sixteen static elements (title, subtitle, details note, Card label, Visa icon, Mastercard icon, four field label/field pairs, Billing-Same-As-Shipping checkbox, Review Order button) individually confirmed via Phase 12.3 real-device runtime investigation and Phase 12.4 automated execution (this session) |
| Objective | Verify the Payment Method screen displays card entry fields and every other static screen element |
| Preconditions | Navigated from Shipping Address screen (TC-021 executed) |
| Dependencies | TC-021 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.accessPaymentScreen()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Payment Method screen title. | Title is displayed. |
| 2 | Observe the "Enter a payment method" subtitle. | Subtitle is displayed. |
| 3 | Observe the payment details note. | Note is displayed. |
| 4 | Observe the Card label. | Label is displayed. |
| 5 | Observe the Visa icon. | Icon is displayed. |
| 6 | Observe the Mastercard icon. | Icon is displayed. |
| 7 | Observe the Cardholder Name field's label. | Label is displayed. |
| 8 | Observe the Cardholder Name field. | Field is displayed. |
| 9 | Observe the Card Number field's label. | Label is displayed. |
| 10 | Observe the Card Number field (custom widget). | Field is displayed. |
| 11 | Observe the Expiration Date field's label. | Label is displayed. |
| 12 | Observe the Expiration Date field (custom widget). | Field is displayed. |
| 13 | Observe the Security Code field's label. | Label is displayed. |
| 14 | Observe the Security Code field. | Field is displayed. |
| 15 | Observe the Billing-Same-As-Shipping checkbox. | Checkbox is displayed. |
| 16 | Observe the Review Order button. | Button is displayed. |

| Field | Value |
|---|---|
| Post Condition | Payment Method screen remains displayed, ready for TC-023 |
| Requirement Traceability | FR-022 |
| Scenario Traceability | TS-022 |
| Automation Mapping | Script/Class/Method: `CartTest.accessPaymentScreen()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `ProductsPage`, `ProductDetailsPage`, `CartPage`, `LoginPage`, `CheckoutPage` (navigation reuse, unmodified), `PaymentPage` (new, Phase 12.4 — sixteen `is*Displayed()` methods). Locators: `PaymentLocators` (new, Phase 12.4, sourced from Phase 12.3 runtime evidence). Test Data: `TestDataManager.productData().pilotProduct()` (TD-004), `TestDataManager.shippingData().standardAddress()` (TD-002), `TestDataManager.loginData().standardCredentials()` (TD-001, anonymous-flow branch only). Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc022_01`–`tc022_04`, framework log — Phase 12.3 (runtime investigation) and Phase 12.4 (automated implementation), this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Read-only verification Test Case (no field mutation), tagged `@regression` only, consistent with the Phase 11.5A tagging convention (read-only → `@regression`; state-mutating → `@regression`+`@critical`). Deliberately does not type into any field or interact with the Billing checkbox or Review Order button — that is TC-023's scope. Phase 12.7 Enterprise Acceptance Review confirmed PASS, 3/3 real-device executions, zero framework impact. |

---

### TC-023 — Payment Card Data Entry

| Field | Value |
|---|---|
| Test Case ID | TC-023 |
| Requirement ID | FR-023 |
| Scenario ID | TS-023 |
| Module | Checkout — Payment (MA-RS-001 §6.7) |
| Feature | Payment Card Data Entry |
| Priority | Must |
| Automation Status | Ready — resolved Phase 12.7A (was Blocked; see Reviewer Notes) |
| Verification Status | Confirmed — all four card entry fields (Cardholder Name, Card Number, Expiration Date, Security Code) individually confirmed via Phase 12.5 real-device runtime investigation and Phase 12.6 automated execution (this session); Card Number/Expiration Date auto-formatting behavior confirmed; Billing-Same-As-Shipping checkbox default-checked state confirmed |
| Objective | Verify card entry fields on the Payment Method screen accept and retain input, including the AUT's auto-formatting behavior for Card Number and Expiration Date |
| Preconditions | Payment Method screen is displayed (TC-022 executed) |
| Dependencies | TC-022 (MA-TD-001 §10) |
| Test Data | Payment Card — MA-TDD-001 §8.3, TD-003 (Ready) |
| Execution Type | Automated — `CartTest.enterPaymentCardData()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Enter a value into the Cardholder Name field. | Field retains the entered value exactly, unformatted. |
| 2 | Enter a value into the Card Number field. | Field auto-formats the entered digits into space-grouped groups of four (e.g. `4111 1111 1111 1111`) and retains the formatted value. |
| 3 | Enter a value into the Expiration Date field. | Field auto-inserts a slash after the second digit (e.g. `12/25`) and retains the formatted value. |
| 4 | Enter a value into the Security Code field. | Field retains the entered value exactly, unmasked. |
| 5 | Observe the Billing-Same-As-Shipping checkbox. | Checkbox remains checked (default state). |

| Field | Value |
|---|---|
| Post Condition | Payment Card fields populated with TD-003 values; Billing-Same-As-Shipping checkbox remains checked (default state). Review Order not tapped — out of this Test Case's scope |
| Requirement Traceability | FR-023 |
| Scenario Traceability | TS-023 |
| Automation Mapping | Script/Class/Method: `CartTest.enterPaymentCardData()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `CheckoutPage` (navigation reuse, unmodified), `PaymentPage` (extended Phase 12.6 — `enterCardholderName()`/`enterCardNumber()`/`enterExpirationDate()`/`enterSecurityCode()`/`enterPaymentCard()`, `getCardholderName()`/`getCardNumber()`/`getExpirationDate()`/`getSecurityCode()`, `isBillingCheckboxChecked()`). Locators: `PaymentLocators` (unmodified, all four field locators already existed from Phase 12.4). Test Data: `TestDataManager.paymentData().standardCard()` (MA-TDD-001 §8.3, TD-003, new Phase 12.6 — `PaymentCard` record, `PaymentDataFactory`, `testdata/common/payment/card.json`), plus `productData().pilotProduct()`/`shippingData().standardAddress()`/`loginData().standardCredentials()` for shared navigation. Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc023_01`–`tc023_03`, framework log — Phase 12.5 (runtime investigation) and Phase 12.6 (automated implementation), this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | **Resolved Phase 12.7A** using Phase 12.5's real-device runtime investigation (direct device interaction confirming `sendKeys` works for both custom widgets — Card Number and Expiration Date — their auto-formatting behavior, and Security Code's unmasked plain-text display) and Phase 12.6's automated implementation (1 individual + 2 five-test-suite real-device runs, zero flakiness after one found-and-fixed defect: `ElementActions.isSelected()` does not reflect Android `Checkable.isChecked()` state under UiAutomator2 — fixed at the `PaymentPage` level via `getAttribute("checked")`, no framework class modified; see Phase 12.6/12.7). Tagged `@critical`: this Test Case mutates the Payment form's field state (data entry), consistent with the Phase 11.5A tagging convention. Dependencies/Preconditions corrected from TC-021 to TC-022 during this reconciliation — TC-023's actual precondition is the Payment screen itself (TC-022's outcome), not merely the Shipping screen. Card Number's `cardNumberErrorTV` validation-label text was observed not to appear under identical empty/blurred conditions where the other three fields' labels did (Phase 12.5) — an open, unexplained, non-blocking observation; not asserted or automated by this Test Case, which covers only the confirmed positive-entry path. Does not toggle the Billing checkbox (Phase 12.5 already investigated the toggled-off, duplicate-locator state directly) and does not tap Review Order — both remain out of this Test Case's scope. Phase 12.7 Enterprise Acceptance Review confirmed PASS, zero framework impact. |

---

### TC-024 — Continue to Review Action

| Field | Value |
|---|---|
| Test Case ID | TC-024 |
| Requirement ID | FR-024 |
| Scenario ID | TS-024 |
| Module | Checkout — Payment (MA-RS-001 §6.7) |
| Feature | Continue to Review Action |
| Priority | Must |
| Automation Status | Ready — reviewed Phase 15.5A (was already Ready; now incidentally confirmed by automated evidence, see Reviewer Notes) |
| Verification Status | Confirmed — Continue → Review Order transition is directly confirmed in MA-AA-001 Navigation Summary, and independently corroborated by real-device automated execution (Phase 15.5/15.5A) via `CartTest.placeOrder()` (TC-026) |
| Objective | Verify the Continue (Review Order) action navigates from Payment Method to Review Order |
| Preconditions | Payment details are entered (TC-023 executed) |
| Dependencies | TC-021 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated (Incidental Coverage) — `CartTest.placeOrder()` (TC-026), real-device validated; no dedicated method exists for TC-024 itself |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Tap the Continue (Review Order) button on the Payment Method screen. | Review Order screen is displayed — confirmed via the destination screen's own title and subtitle, not navigation alone. |

| Field | Value |
|---|---|
| Post Condition | Review Order screen displayed, ready for TC-025 — incidentally confirmed via `CartTest.placeOrder()` (TC-026), Phase 15.5A |
| Requirement Traceability | FR-024 |
| Scenario Traceability | TS-024 |
| Automation Mapping | Script/Class/Method: `CartTest.placeOrder()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`) — incidental coverage, no dedicated TC-024 method exists. `paymentPage.tapReviewOrder()` is called, then `CommonAssertions.verifyVisible(navigatedToReviewOrder, "Successful transition to Review Order screen")`, followed by `CommonAssertions.verifyVisible(reviewOrderPage.isCheckoutTitleDisplayed(), "Review Order screen — Checkout title")` and `CommonAssertions.verifyVisible(reviewOrderPage.isReviewOrderSubtitleDisplayed(), "Review Order screen — \"Review your order\" title")` — the destination screen's own identity is explicitly asserted, not merely that a transition occurred. Confirmed via Phase 15.5 Enterprise Incidental Automation Review. Test Data: Not Applicable. Execution Strategy: `ISOLATED`. Evidence: screenshot `tc026_03_review_order_screen_arrival` (73 files, cumulative), framework log. Note: this Test Case's own wording ("Continue button") differs from the codebase's naming ("Review Order button" / `tapReviewOrder()`) — the same physical element per MA-LOC-001 §11's own dual-naming ("Review Order / Continue Button"), not a functional discrepancy. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | **Reviewed Phase 15.5A** (Enterprise Incidental Automation Review, Phase 15.5): this Test Case is **fully incidentally covered** — `CartTest.placeOrder()` (TC-026) exercises this exact action and asserts the destination screen's own title/subtitle, not navigation alone, satisfying TC-024's Expected Result completely. Automation Status/Verification Status were already Ready/Confirmed prior to this review (based on MA-AA-001 evidence alone); Execution Type is updated from Manual to Automated (Incidental Coverage) to reflect that real automated evidence now exists, though no method dedicated to TC-024 itself was written. See Automation Mapping for the exact assertion chain. |

---

### TC-025 — Review Order Screen Verification

| Field | Value |
|---|---|
| Test Case ID | TC-025 |
| Requirement ID | FR-025 |
| Scenario ID | TS-025 |
| Module | Order Review (MA-RS-001 §6.8) |
| Feature | Review Order Screen Verification |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — Review Order screen and order summary existence are now directly, incidentally confirmed via `CartTest.placeOrder()` (Phase 13.2/13.3); the summary's Product Name/Price, Delivery Address Full Name, and Payment Method Cardholder Name are individually value-compared against the data entered earlier in the flow, but Address Line 1/City/Country, Card Number/Expiration Date, and the Total's computed value are not — Step 2's data-consistency comparison remains only partially, not fully, evidenced |
| Objective | Verify the Review Order screen displays an order summary consistent with data entered earlier in the flow |
| Preconditions | Navigated from Payment Method screen (TC-024 executed) |
| Dependencies | TC-024 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Review Order screen for an order summary. | Order summary is displayed. |
| 2 | Compare the order summary to the Cart, Shipping Address, and Payment data entered earlier in the flow. | Order summary content reflects the data entered earlier in the flow. |

| Field | Value |
|---|---|
| Post Condition | Review Order screen remains displayed, ready for TC-026 |
| Requirement Traceability | FR-025 |
| Scenario Traceability | TS-025 |
| Automation Mapping | No dedicated automated method exists for this Test Case's own 2-step flow — Automation Status/Execution Type intentionally left unchanged, per the TC-002/TC-003/TC-009 precedent (Phase 10.1A/10.2). **Incidental coverage (Phase 13.3A)**: `CartTest.placeOrder()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`), via `ReviewOrderPage`, individually verifies the Review Order screen's title/subtitle, Product Name/Price (compared to `pilotProduct`), Delivery Address (Full Name compared to `address.fullName()`), Payment Method (Cardholder Name compared to `card.cardholderName()`), Billing Address, Shipping Method, and Total sections. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Step 2's data-consistency comparison is a reasonable inference, not a literal MA-AA-001 observation; flagged for reviewer awareness. **Updated Phase 13.3A**: incidental automated evidence now exists (see Automation Mapping) but does not fully resolve this note — only the anchor field per section (product name/price, full name, cardholder name) is value-compared, not the complete field set (e.g. Address Line 1/City/Country, Card Number/Expiration Date) or the Total's computed value. Per Phase 13.3's Incidental Automation Review: this Test Case's Acceptance Criteria are only partially satisfied by incidental coverage, so Automation Status/Verification Status are deliberately **not** changed to Confirmed/Automated in this reconciliation — a dedicated TC-025 implementation with full field-level comparison would be required to resolve this properly. Execution Tags populated (read-only verification, `@regression`, per the TC-009 precedent) despite no dedicated method existing. |

---

### TC-026 — Place Order Action

| Field | Value |
|---|---|
| Test Case ID | TC-026 |
| Requirement ID | FR-026 |
| Scenario ID | TS-026 |
| Module | Order Placement (MA-RS-001 §6.9) |
| Feature | Place Order Action |
| Priority | Must |
| Automation Status | Ready — resolved Phase 13.3A (was Blocked; see Reviewer Notes) |
| Verification Status | Confirmed — the Place Order action and its hosting screen (Review Order) are both directly confirmed via Phase 13.2 real-device automated execution and Phase 13.3 Enterprise Acceptance Review (this session): tapping the Place Order button navigates to the Checkout Complete screen |
| Objective | Verify the Place Order action is triggerable and correctly navigates from the Review Order screen to the Checkout Complete screen |
| Preconditions | Review Order screen is displayed (TC-025 executed) |
| Dependencies | TC-025 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `CartTest.placeOrder()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate to the Shipping Address screen and populate all fields. | Shipping Address screen fields are populated — reuses `CheckoutPage.enterShippingAddress()`. |
| 2 | Navigate to the Payment screen and populate all card fields. | Payment screen card fields are populated — reuses `PaymentPage.enterPaymentCard()`. |
| 3 | Tap Review Order on the Payment screen. | Review Order screen is displayed. |
| 4 | Observe the Review Order screen's Checkout title and "Review your order" subtitle. | Both are displayed. |
| 5 | Observe the Product information (name and price). | Both match the Pilot Product's loaded data. |
| 6 | Observe the Delivery Address section. | Displayed; Full Name matches the Shipping Address data entered. |
| 7 | Observe the Payment Method section. | Displayed; Cardholder Name matches the Payment Card data entered. |
| 8 | Observe the Billing Address section. | Displayed (the "same as shipping" note — the Billing checkbox is checked by default and never unchecked in this flow). |
| 9 | Observe the Shipping Method section. | Displayed ("DHL Standard Delivery" — the only shipping-method row confirmed in source). |
| 10 | Observe the Total (item count and amount). | Both displayed. |
| 11 | Observe the Place Order button. | Displayed and enabled. |
| 12 | Tap the Place Order button. | Navigates to the Checkout Complete screen. |
| 13 | Observe the Checkout Complete screen's title, Thank You message, Success message, and Dispatch message. | All four displayed. |
| 14 | Observe the Continue Shopping button. | Displayed and enabled. |
| 15 | Tap Continue Shopping. | Navigates to the Products screen. |
| 16 | Observe the Products screen. | Displayed. |
| 17 | Observe the header cart badge. | Not displayed — the badge does not persist on the Products screen after Continue Shopping (real-device evidence, Phase 13.2; see Reviewer Notes). |

| Field | Value |
|---|---|
| Post Condition | Order placed; Checkout Complete screen was displayed and confirmed; app returned to the Products screen via Continue Shopping with the cart badge no longer displayed |
| Requirement Traceability | FR-026 |
| Scenario Traceability | TS-026 |
| Automation Mapping | Script/Class/Method: `CartTest.placeOrder()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`). Page Objects: `CheckoutPage`, `PaymentPage` (navigation reuse, `PaymentPage` extended Phase 13.2 with `isReviewOrderButtonEnabled()`/`tapReviewOrder()`), `ReviewOrderPage` (new, Phase 13.2 — display checks for every business section plus `tapPlaceOrder()`), `CheckoutCompletePage` (new, Phase 13.2 — display checks for title/messages/button plus `tapContinueShopping()`), `ProductsPage` (destination confirmation). Locators: `ReviewOrderLocators`, `CheckoutCompleteLocators` (new, Phase 13.2, sourced from MA-LOC-001 §12/§13, source-decompiled). Test Data: `TestDataManager.productData().pilotProduct()` (TD-004), `.shippingData().standardAddress()` (TD-002), `.paymentData().standardCard()` (TD-003), `.loginData().standardCredentials()` (TD-001, anonymous-flow branch). Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc026_01`–`tc026_08`, framework log — Phase 13.2 (implementation) and Phase 13.3 (review), this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression`, `@critical` |
| Reviewer Notes | **Resolved Phase 13.3A** using Phase 13.2's real-device automated implementation and Phase 13.3's Enterprise Acceptance Review. Three runtime findings from Phase 13.2 are documented here as historical implementation findings, not current defects: (1) the Review Order screen is a single scrollable page taller than the viewport — Payment Method/Billing Address/Shipping Method/Total/Place Order render below the fold; resolved by `ReviewOrderPage` scrolling to each target via the existing, unmodified `ScrollUtility`. (2) A Google Play Services "Save card to Google?" system dialog appears after Place Order, external to the AUT; `CheckoutCompletePage.tapContinueShopping()` dismisses it via "Not now" if present, located with the same `AppiumBy.androidUIAutomator(...)` strategy `ScrollUtility` already uses elsewhere in this framework. (3) The cart badge does **not** persist on the Products screen after Continue Shopping — the first implementation attempt assumed it would, over-extending MA-LOC-001 §13's narrower claim (badge doesn't reset while still on the Checkout Complete screen); real-device evidence contradicted that extension, and the assertion (Step 17 above) now reflects what was actually observed. Zero framework modification occurred resolving any of the three. |

---

### TC-027 — Order Completion Confirmation

| Field | Value |
|---|---|
| Test Case ID | TC-027 |
| Requirement ID | FR-027 |
| Scenario ID | TS-027 |
| Module | Order Placement (MA-RS-001 §6.9) |
| Feature | Order Completion Confirmation |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the "Checkout Complete" title and all three confirmation messages are directly, incidentally confirmed via `CartTest.placeOrder()` (Phase 13.2/13.3), fully satisfying both of this Test Case's steps |
| Objective | Verify the Order Completion screen displays a success confirmation after Place Order |
| Preconditions | Place Order action has been triggered (TC-026 executed) |
| Dependencies | TC-025 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the screen displayed after the Place Order action. | Order Completion screen is displayed. |
| 2 | Observe the Order Completion screen for a success message. | "Checkout Complete" / "Order Completed" message is displayed. |

| Field | Value |
|---|---|
| Post Condition | Order Completion screen displayed as the terminal screen of the flow |
| Requirement Traceability | FR-027 |
| Scenario Traceability | TS-027 |
| Automation Mapping | No dedicated automated method exists for this Test Case's own 2-step flow — Automation Status/Execution Type intentionally left unchanged, per the TC-002/TC-003/TC-009 precedent (Phase 10.1A/10.2). **Incidental coverage (Phase 13.3A)**: `CartTest.placeOrder()` (`src/test/java/com/mobileautomation/framework/tests/CartTest.java`), via `CheckoutCompletePage`, individually verifies the Checkout Complete screen is reached after Place Order (Step 1) and its title/Thank You/Success/Dispatch messages are all displayed (Step 2, and beyond — three distinct confirmation messages are checked, exceeding this Test Case's single "success message" requirement). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Further navigation beyond this screen is Not Observed per MA-AA-001; not in scope for this Test Case. **Updated Phase 13.3A, per Phase 13.3's Incidental Automation Review**: unlike TC-025, every Acceptance Criterion this Test Case actually requires (screen reached, success message displayed) is now fully covered by incidental evidence — both steps are satisfied without gap. Automation Status/Verification Status/Execution Type are nonetheless deliberately left unchanged (Ready/Confirmed/Manual), consistent with the TC-002/TC-003/TC-009 precedent that a Test Case without its own dedicated `@Test` method keeps its Execution Type as Manual regardless of how completely another Test Case's method happens to exercise it — only Automation Mapping and Execution Tags are populated to disclose the incidental relationship. |

---

### TC-028 — Navigation Drawer Item Access

| Field | Value |
|---|---|
| Test Case ID | TC-028 |
| Requirement ID | FR-028 |
| Scenario ID | TS-028 |
| Module | Navigation (MA-RS-001 §6.10) |
| Feature | Navigation Drawer Item Access |
| Priority | Should |
| Automation Status | Ready — resolved Phase 15.2A (was Blocked; see Reviewer Notes) |
| Verification Status | Confirmed — all seven navigable drawer destinations (WebView, QR Code Scanner, Geo Location, Drawing, About, FingerPrint, Virtual USB) and the Reset App State confirmation dialog are directly confirmed via Phase 15.1 real-device automated execution and Phase 15.2 Enterprise Acceptance Review (this session) |
| Objective | Verify each verified Navigation Drawer item responds to selection and produces its confirmed resulting state |
| Preconditions | Navigation Drawer is open (TC-008 executed) |
| Dependencies | TC-008 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Automated — `NavigationTest.accessDrawerItems()`, real-device validated |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the Product Catalog screen. | Displayed — precondition for opening the Navigation Drawer. |
| 2 | Open the Navigation Drawer and select WebView. | Item displayed before selection; WebView screen's title displayed after; system back gesture returns to the Product Catalog. |
| 3 | Open the Navigation Drawer and select QR Code Scanner. | Item displayed before selection; QR Code Scanner screen's title displayed after; system back gesture returns to the Product Catalog. |
| 4 | Open the Navigation Drawer and select Geo Location. | Item displayed before selection; Geo Location screen's title displayed after; system back gesture returns to the Product Catalog. |
| 5 | Open the Navigation Drawer and select Drawing. | Item displayed before selection; Drawing screen's title displayed after; system back gesture returns to the Product Catalog. |
| 6 | Open the Navigation Drawer and select About. | Item displayed before selection; About screen's title displayed after; system back gesture returns to the Product Catalog. |
| 7 | Open the Navigation Drawer and select FingerPrint. | Item displayed before selection; FingerPrint screen's title displayed after; system back gesture returns to the Product Catalog. |
| 8 | Open the Navigation Drawer and select Virtual USB. | Item displayed before selection; Virtual USB's dynamic status message displayed after (a separate Android Activity, not a fragment); system back gesture returns to the Product Catalog. |
| 9 | Open the Navigation Drawer and select Reset App State. | Item displayed before selection; a confirmation dialog (title, message, Cancel, Reset App) is displayed — not a navigation. Cancel is tapped; the Product Catalog is displayed again. Confirm ("RESET APP") is never tapped, since it would destructively mutate app state for any test running afterward in the same suite. |

| Field | Value |
|---|---|
| Post Condition | Product Catalog screen displayed; all seven navigable drawer destinations and the Reset App State dialog individually confirmed; app state unmodified (Reset App State was cancelled, not confirmed) |
| Requirement Traceability | FR-028 |
| Scenario Traceability | TS-028 |
| Automation Mapping | Script/Class/Method: `NavigationTest.accessDrawerItems()` (`src/test/java/com/mobileautomation/framework/tests/NavigationTest.java`). Page Objects: `ProductsPage` (drawer open/item-tap/item-displayed, reused unchanged), `NavigationDrawerPage` (new, Phase 15.1 — one title-check method per destination, plus `isResetDialogDisplayed()`/`cancelResetDialog()`/`navigateBack()`). Locators: `DrawerDestinationLocators` (new, Phase 15.1, sourced from MA-LOC-001 §15/§17; the Reset App State dialog's title id was corrected from MA-LOC-001 §17's documented `android:id/alertTitle` to the actually-observed `com.saucelabs.mydemoapp.android:id/alertTitle` — see MA-LOC-001 §17 and this Test Case's Reviewer Notes). Test Data: Not Applicable — drawer labels and destination titles are fixed AUT UI text (locator constants), not framework test data. Execution Strategy: `ISOLATED`. Reporting: Extent Report. Evidence: screenshots `tc028_01`–`tc028_09` (captured on each of 7 executions), framework log — Phase 15.1 (implementation) and Phase 15.2 (Enterprise Acceptance Review), this session, real-device execution (vivo I2301). |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Previously required a manual verification pass per MA-TS-001 §8 for each of the eight utility items individually — **resolved Phase 15.2A** using Phase 15.1 real-device automated-execution evidence and Phase 15.2 Enterprise Acceptance Review: a dedicated automated method (`accessDrawerItems()`) now exists and passes repeatedly on the real device (4/4 executions post-fix this session — individual ×2, navigation suite ×2 — zero flakiness; 3 pre-fix executions failed identically at a single, since-corrected locator, not evidence of flakiness). Automation Status changed from Blocked to Ready because the ambiguity that blocked it — resulting screen content for each item — is now fully resolved for all eight items (the seven navigable destinations plus Reset App State), matching this Test Case's own pre-existing "eight utility items" note exactly. **Genuine defect found and fixed during Phase 15.1 runtime validation**: MA-LOC-001 §17 documents the Reset App State dialog's title as `android:id/alertTitle` (platform-namespaced), citing "Confirmed matching Phase 2 device evidence" — a fresh `uiautomator dump` on this project's real device, taken after a reproducible (3/3) automated failure at that exact locator, showed the title actually resolves to `com.saucelabs.mydemoapp.android:id/alertTitle` (app-namespaced); Message/Cancel/Confirm were independently confirmed to remain correctly platform-namespaced in the same dump — only the title differs. This is an AUT/documentation discrepancy specific to this device, not a framework, test, or environment defect (see MA-LOC-001 §17). **Crash App (Debug) is permanently excluded** — not automated, not tapped, not referenced by any locator or method — it intentionally terminates the app process. Zero framework modification occurred resolving this Test Case (confirmed independently, Phase 15.2 Framework Review, via file-modification-timestamp analysis covering `DriverManager`, `DriverProvider`, `AndroidDriverFactory`, `ExecutionStrategy`, `ElementActions`, `BasePage`, `NavigationHelper`, `WaitUtility`, `ScrollUtility`, Reporting, and Logging). |

---

### TC-029 — Back Navigation Support

| Field | Value |
|---|---|
| Test Case ID | TC-029 |
| Requirement ID | FR-029 |
| Scenario ID | TS-029 |
| Module | Navigation (MA-RS-001 §6.10) |
| Feature | Back Navigation Support |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Confirmed — the Product Details → Product Catalog back-navigation path is directly confirmed in MA-AA-001 Screen Inventory |
| Objective | Verify back navigation from Product Details returns to Product Catalog |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Trigger back navigation from the Product Details screen. | Product Catalog screen is displayed. |

| Field | Value |
|---|---|
| Post Condition | Product Catalog screen displayed |
| Requirement Traceability | FR-029 |
| Scenario Traceability | TS-029 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Functional |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | **Reviewed Phase 15.5A** (Enterprise Incidental Automation Review, Phase 15.5): confirmed **zero incidental automation coverage exists** for this Test Case. A repository-wide search found exactly one `.navigateBack()` call in the entire test suite — `NavigationDrawerPage.navigateBack()`, called from `NavigationTest.accessDrawerItems()` (TC-028), which returns from a Navigation Drawer destination screen to the Product Catalog, not from Product Details. `ProductDetailsPage.navigateBack()` — the method whose own Javadoc specifically targets this exact flow (Product Details → Product Catalog) — is never called by any test in the codebase. Automation Status, Verification Status, Execution Type, and Automation Mapping are intentionally left unchanged (Ready / Confirmed / Manual / Placeholder) — this Test Case requires genuine, dedicated implementation; it cannot be resolved via incidental-coverage documentation. |

---

### TC-030 — Error Handling Coverage (Deferred)

| Field | Value |
|---|---|
| Test Case ID | TC-030 |
| Requirement ID | FR-030 |
| Scenario ID | TS-030 |
| Module | Error Handling (MA-RS-001 §6.11) |
| Feature | Error Handling Coverage |
| Priority | Could |
| Automation Status | Deferred — excluded from execution scope per MA-TS-001 §6 |
| Verification Status | Pending Manual Verification — MA-AA-001 Known Limitations explicitly states error handling was not analyzed; no verifiable error state exists in current evidence |
| Objective | Not Applicable — no verifiable error state exists to design a manual execution step against |
| Preconditions | Not Applicable |
| Dependencies | Not Applicable |
| Test Data | Not Applicable |
| Execution Type | Not Applicable — deferred |
| Environment | Not Applicable |

| Step | Action | Expected Result |
|---|---|---|
| — | Not Applicable — no verifiable error state exists in the evidence base (MA-AA-001 Known Limitations; MA-TD-001 TS-030). | Not Applicable. |

| Field | Value |
|---|---|
| Post Condition | Not Applicable |
| Requirement Traceability | FR-030 |
| Scenario Traceability | TS-030 |
| Automation Mapping | Script/Class/Method: Placeholder — Not Yet Defined |
| Execution Group | Not Applicable — Excluded from Execution |
| Execution Tags | Placeholder — Not Yet Defined |
| Reviewer Notes | Retained only for traceability to FR-030; requires a dedicated evidence-gathering pass before it can become an executable Test Case |

---

### TC-031 — Dynamic Element State Verification (Cart)

| Field | Value |
|---|---|
| Test Case ID | TC-031 |
| Requirement ID | FR-031 |
| Scenario ID | TS-031 |
| Module | State Management (MA-RS-001 §6.12) |
| Feature | Dynamic Element State Verification — Cart |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — cart badge and cart total existence as dynamic elements are confirmed (MA-AA-001 §10); their exact real-time matching to cart contents is inferred |
| Objective | Verify cart badge and cart total update to reflect the current Cart state |
| Preconditions | An item has been added to the Cart (TC-012 executed) |
| Dependencies | TC-012 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Observe the cart badge value. | Cart badge displays a value. |
| 2 | Observe the cart total value. | Cart total displays a value. |
| 3 | Compare both values to the current Cart contents. | Cart badge and total values match the current Cart contents. |

| Field | Value |
|---|---|
| Post Condition | Dynamic elements reflect current Cart state |
| Requirement Traceability | FR-031 |
| Scenario Traceability | TS-031 |
| Automation Mapping | **Incidental coverage only (Phase 15.5A)** — no dedicated automated method exists for this Test Case. Cart Total's dynamic-update behavior (Steps 2-3, in the Total sense) is strongly incidentally confirmed via `CartTest.updateCartItemQuantity()` (TC-015): Total Items/Total Price are read before a quantity-changing action, then re-asserted via `CommonAssertions.verifyText(actualIncreasedTotalItems, expectedIncreasedTotalItems, "Cart Total Items after Increase")` and the equivalent for Total Price, with expected values computed from the actual new quantity, not hardcoded. Confirmed via Phase 15.5 Enterprise Incidental Automation Review. This exercises the **Cart screen's own Total Items/Total Price row**, distinct from the **header cart badge** (`isCartBadgeDisplayed()`/`getCartBadgeCount()`) that this Test Case's own wording specifically names — no test changes cart quantity and then re-checks the header badge's count in the same flow; the badge is only checked statically elsewhere (TC-007/012/018), never as a before/after dynamic-update comparison. Step 1 (cart badge dynamic update) is **not** covered. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Same inference caveat as TC-007/TC-017 regarding badge/total semantics. **Reviewed Phase 15.5A**: Cart Total's dynamic-update behavior is now incidentally confirmed with real runtime evidence via TC-015 — see Automation Mapping. This is explicitly distinct from the header cart badge, which remains uncovered as a dynamically-updating element. Automation Status/Verification Status/Execution Type intentionally left unchanged. |

---

### TC-032 — Dynamic Element State Verification (Product Details)

| Field | Value |
|---|---|
| Test Case ID | TC-032 |
| Requirement ID | FR-031 |
| Scenario ID | TS-032 |
| Module | State Management (MA-RS-001 §6.12) |
| Feature | Dynamic Element State Verification — Product Details |
| Priority | Must |
| Automation Status | Ready |
| Verification Status | Partially Confirmed — quantity and color selection existence as dynamic elements are confirmed (MA-AA-001 §10); their exact persistence-of-selection behavior is inferred |
| Objective | Verify quantity and color selection controls reflect the currently selected state |
| Preconditions | Product Details screen is displayed (TC-009 executed) |
| Dependencies | TC-009 (MA-TD-001 §10) |
| Test Data | Not Applicable |
| Execution Type | Manual — baseline for future automation |
| Environment | Android Emulator or Physical Android Device (local only) — MA-TS-001 §12 |

| Step | Action | Expected Result |
|---|---|---|
| 1 | Select a quantity value on the Product Details screen. | Quantity selector displays the selected value. |
| 2 | Select a color option on the Product Details screen. | Color selection displays the selected option. |
| 3 | Observe both controls after selection. | Displayed quantity and color selection match the most recent user selection. |

| Field | Value |
|---|---|
| Post Condition | Dynamic elements reflect most recent selection |
| Requirement Traceability | FR-031 |
| Scenario Traceability | TS-032 |
| Automation Mapping | **Incidental coverage only (Phase 15.5A)** — no dedicated automated method exists for this Test Case. The Quantity Selector's state-reflects-selection behavior (part of Steps 1 and 3) is incidentally confirmed via `ProductDetailsTest.adjustProductQuantity()` (TC-011): `CommonAssertions.verifyText(String.valueOf(increasedQuantity), String.valueOf(initialQuantity + 1), "Quantity value after one Increase tap")` directly demonstrates the displayed value matches the just-performed action. Confirmed via Phase 15.5 Enterprise Incidental Automation Review. Color Selection (Step 2, and the color portion of Step 3) is **not** covered — `ProductDetailsTest.selectProductColor()` (TC-010) exercises tapping the *same already-selected* swatch (the Pilot Product renders exactly one color option, confirmed real-device, Phase 10.3), so its assertion proves the description is *unchanged*, not that it *reflects a new selection* — this cannot demonstrate the state-reflects-selection behavior TS-032 requires without an actual selection change, consistent with TC-010's own already-documented limitation. |
| Execution Group | Functional |
| Execution Tags | `@regression` |
| Reviewer Notes | Same inference caveat as TC-010/TC-011 regarding exact control mechanics. **Reviewed Phase 15.5A**: Quantity's state-reflects-selection behavior is now incidentally confirmed via TC-011 — see Automation Mapping. Color-selection remains genuinely uncovered, consistent with TC-010's own documented single-swatch limitation. Automation Status/Verification Status/Execution Type intentionally left unchanged. |

---

## 7. Coverage Summary

| Category | FR Count | TC Count |
|---|---|---|
| Application Launch | 1 | 1 |
| Authentication | 3 | 3 |
| Product Browsing | 4 | 4 |
| Product Details | 5 | 5 |
| Cart | 5 | 5 |
| Checkout — Shipping | 3 | 3 |
| Checkout — Payment | 3 | 3 |
| Order Review | 1 | 1 |
| Order Placement | 2 | 2 |
| Navigation | 2 | 2 |
| Error Handling | 1 | 1 |
| State Management | 1 | 2 |
| **Total** | **31** | **32** |

## 8. Requirement → Scenario → Test Case Traceability Matrix

| FR | TS | TC |
|---|---|---|
| FR-001 | TS-001 | TC-001 |
| FR-002 | TS-002 | TC-002 |
| FR-003 | TS-003 | TC-003 |
| FR-004 | TS-004 | TC-004 |
| FR-005 | TS-005 | TC-005 |
| FR-006 | TS-006 | TC-006 |
| FR-007 | TS-007 | TC-007 |
| FR-008 | TS-008 | TC-008 |
| FR-009 | TS-009 | TC-009 |
| FR-010 | TS-010 | TC-010 |
| FR-011 | TS-011 | TC-011 |
| FR-012 | TS-012 | TC-012 |
| FR-013 | TS-013 | TC-013 |
| FR-014 | TS-014 | TC-014 |
| FR-015 | TS-015 | TC-015 |
| FR-016 | TS-016 | TC-016 |
| FR-017 | TS-017 | TC-017 |
| FR-018 | TS-018 | TC-018 |
| FR-019 | TS-019 | TC-019 |
| FR-020 | TS-020 | TC-020 |
| FR-021 | TS-021 | TC-021 |
| FR-022 | TS-022 | TC-022 |
| FR-023 | TS-023 | TC-023 |
| FR-024 | TS-024 | TC-024 |
| FR-025 | TS-025 | TC-025 |
| FR-026 | TS-026 | TC-026 |
| FR-027 | TS-027 | TC-027 |
| FR-028 | TS-028 | TC-028 |
| FR-029 | TS-029 | TC-029 |
| FR-030 | TS-030 | TC-030 |
| FR-031 | TS-031, TS-032 | TC-031, TC-032 |

## 9. Automation Readiness Summary

| Automation Status | Count | Test Case IDs |
|---|---|---|
| Ready | 31 | TC-001, 002, 003, 004, 005, 006, 007, 008, 009, 010, 011, 012, 013, 014, 015, 016, 017, 018, 019, 020, 021, 022, 023, 024, 025, 026, 027, 028, 029, 031, 032 |
| Blocked | 0 | — |
| Deferred | 1 | TC-030 |

| Verification Status | Count |
|---|---|
| Confirmed | 25 |
| Partially Confirmed | 6 |
| Pending Manual Verification | 1 |

**Zero Test Cases remain Blocked as of v1.14 (Phase 15.2A).** TC-012 was resolved in Phase 9.5A/v1.2; TC-004 was resolved in Phase 10.1A/v1.4; TC-011 was resolved in Phase 10.4A/v1.6; TC-018 was resolved in Phase 11.9A/v1.8; TC-023 was resolved in Phase 12.7A/v1.9; TC-026 was resolved in Phase 13.3A/v1.10; TC-006 was resolved in Phase 14.2A/v1.12 (using Phase 14.1 real-device automated-execution evidence and Phase 14.2 Enterprise Acceptance Review); TC-028 was resolved in Phase 15.2A/v1.14 (this reconciliation, using Phase 15.1 real-device automated-execution evidence and Phase 15.2 Enterprise Acceptance Review) — the last remaining Blocked Test Case. TC-030 remains Deferred and out of automation scope per MA-TS-001 Section 6. TC-015/TC-016/TC-017's Verification Status changed from Partially Confirmed to Confirmed in Phase 11.5A/v1.7, using Phase 11.2/11.3/11.4 runtime evidence; TC-018's changed the same way in Phase 11.9A/v1.8; TC-023's changed the same way in Phase 12.7A/v1.9; TC-026's changed the same way in Phase 13.3A/v1.10; TC-006's changed the same way in Phase 14.2A/v1.12, using Phase 14.1 real-device automated-execution evidence; TC-028's changed the same way in Phase 15.2A/v1.14, using Phase 15.1 real-device automated-execution evidence — see each Test Case's own Reviewer Notes.

## 10. Assumptions

- The AUT's screen structure and behavior remain consistent with MA-AA-001 observations at execution time.
- The elevated-risk items identified in MA-TS-001 Section 8 have all now been resolved through real-device verification — none remain outstanding. TC-012's item was resolved in v1.2; TC-004's item was resolved in v1.4 (Phase 10.1A); TC-011's item was resolved in v1.6 (Phase 10.4A, using Phase 10.3 runtime evidence); TC-018's item was resolved in v1.8 (Phase 11.9A, using Phase 11.7 real-device runtime investigation); TC-023's item was resolved in v1.9 (Phase 12.7A, using Phase 12.5 real-device runtime investigation and Phase 12.6 automated implementation); TC-026's item was resolved in v1.10 (Phase 13.3A, using Phase 13.2 real-device automated-execution evidence and Phase 13.3 Enterprise Acceptance Review); TC-006's item was resolved in v1.12 (Phase 14.2A, using Phase 14.1 real-device automated-execution evidence and Phase 14.2 Enterprise Acceptance Review); TC-028's item was resolved in v1.14 (Phase 15.2A, using Phase 15.1 real-device automated-execution evidence and Phase 15.2 Enterprise Acceptance Review).
- Test data values will be supplied by a future MA-TDD-001 (or equivalent) document; none are invented here. **Update, 2026-08-01:** MA-TDD-001 now exists and is baselined; TD-001 (Login) is Verified, TD-002 (Shipping) is Ready with dummy values. **Update, 2026-08-04 (Phase 12.7A):** TD-003 (Payment) is now Ready, resolved using Phase 12.5/12.6 evidence — see MA-TDD-001 §18. No dataset category remains undocumented.
- A single contributor authors, reviews, and later automates these Test Cases, consistent with MA-PV-001 Section 19 (C-6).

## 11. Risks

| Risk | Note |
|---|---|
| ~~Test Cases cannot proceed to automation until manual verification~~ — **Fully resolved Phase 15.2A** | Zero Test Cases remain Blocked. TC-012 (Product Details, v1.2), TC-004 (Authentication, v1.4), TC-011 (Product Details, v1.6/Phase 10.4A), TC-018 (Cart, v1.8/Phase 11.9A), TC-023 (Checkout — Payment, v1.9/Phase 12.7A), TC-026 (Order Placement, v1.10/Phase 13.3A), TC-006 (Product Browsing, v1.12/Phase 14.2A), and TC-028 (Navigation, v1.14/Phase 15.2A) all resolved — TC-028 was the last remaining item |
| ~~Checkout dependency chain concentration~~ — **Resolved Phase 11.9A** | A failure or ambiguity at TC-018 (Proceed to Checkout) previously risked blocking confirmation of the entire downstream Checkout chain (TC-019–TC-027) while TC-018 remained Blocked. TC-018 is now Ready, real-device confirmed for both authentication-state branches (Phase 11.7/11.8) — the downstream chain's own entry point is no longer at risk. **Updated Phase 13.4A**: TC-019/020/021 (Phase 12.2, documented Phase 13.4A), TC-022/023 (Phase 12.4/12.6, documented Phase 12.7A), and TC-026 (Phase 13.2, documented Phase 13.3A) are now individually implemented and verified. **Updated Phase 15.5A**: only TC-025/TC-027 remain without a dedicated automated method; all three of TC-024/025/027 now have documented incidental coverage via `CartTest.placeOrder()` (TC-024 confirmed Phase 15.5A; TC-025/TC-027 documented Phase 13.3A — see each Test Case's own Reviewer Notes) |
| Several "Ready" Test Cases carry Partially Confirmed verification (**6 of 32**, corrected Phase 15.6A) | These do not literally contain "Out of Current Observation" text and are mechanically Ready, but rely on reasonable inference beyond literal MA-AA-001 statements (flagged individually in each Test Case's Reviewer Notes). **Note added Phase 11.5A**, **updated Phase 11.9A**, **updated Phase 12.7A**, **updated Phase 13.3A**, **updated Phase 14.2A**, **updated Phase 15.2A**, **recalculated Phase 15.6A**: the prior "16 of 32" figure was flagged as numerically inconsistent as far back as Phase 10.4A and was never fully corrected across six subsequent phases, each of which only patched the figure's trajectory (documenting individual Partially Confirmed → Confirmed transitions: Phase 11.5A's TC-015/016/017, Phase 11.9A's TC-018, Phase 12.7A's TC-023, Phase 13.3A's TC-026, Phase 14.2A's TC-006, Phase 15.2A's TC-028 — eight transitions total) without ever re-deriving the figure directly from the document's own current field values. Phase 15.6's whole-project audit independently recounted every one of the 32 Test Cases' literal `Verification Status` field values directly (not the historical delta) and found exactly **6** currently read "Partially Confirmed": **TC-007, TC-010, TC-011, TC-025, TC-031, TC-032** — all six carry their own already-documented, genuine inference caveats in their respective Reviewer Notes (not new limitations). Note: 16 minus the eight tracked transitions above would arithmetically predict 8, not 6 — the origin of this 2-Test-Case gap could not be reconstructed from available Version History evidence (the original "16" baseline itself, stated once at Phase 10.4A, was never independently re-derived at that time) and is not restated here as fact; the **direct recount of 6 is used as the authoritative current figure** because it was independently verified against the document's own live data, not reconstructed from historical narrative |
| ~~Test data remains undocumented~~ — **Fully resolved Phase 12.7A** | MA-TDD-001 now exists and is baselined. Login (TD-001), Shipping (TD-002), and Payment (TD-003, resolved Phase 12.7A using Phase 12.5/12.6 real-device evidence) are all Ready/Verified. No dataset category remains undocumented |
| Scope creep during future automation implementation | Inherited from MA-PV-001 Section 20, R-1 |

## 12. Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-07-29 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Draft | — | — |

---

End of Document — MA-TC-001, v1.16
