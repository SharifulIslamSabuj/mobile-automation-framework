# Pilot Automation Design Specification

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-PAD-001 |
| Title | Pilot Automation Design Specification |
| Version | v1.0 |
| Status | Draft |
| Phase | Pilot Automation Design (Phase 9.1) |
| Governed By | All prior framework docs; [MA-TC-001 — Test Case Specification](../08-test-design/MA-TC-001_Test-Case-Specification_v1.0.md), [MA-TD-001 — Test Design Specification](../08-test-design/MA-TD-001_Test-Design-Specification_v1.0.md), [MA-TDD-001 — Test Data Design Specification](../08-test-design/MA-TDD-001_Test-Data-Design-Specification_v1.0.md), [MA-RS-001 — Requirements Specification](../03-requirements/MA-RS-001_Requirements-Specification_v1.0.md), [MA-LOC-001 — Locator Repository](../automation/LOCATOR_REPOSITORY.md) |

This is a design-only document. No Java class, Page Object, Test Class, or test data file was created while producing it. Every mapping below cites the governing document/section it was sourced from — nothing here was invented.

## 1. Pilot Objective

Only two test cases are automated — **TC-004 (Login Outcome Verification)** and **TC-012 (Add to Cart Action)** — because the goal of this phase is not test coverage, it is **framework validation under real conditions**. Every layer built in Phases 3–8 (Configuration, Driver Management, Core Utilities, Cross-Cutting Infrastructure, Base Framework, Components, Assertions, Test Data) has been validated so far only by throwaway verification programs against no live Appium session (explicitly and repeatedly documented as a limitation in every phase's own "Validation Performed" section, e.g. MA-DRV-001 §9, MA-UTIL-001 §7, MA-BF-001 §8). Two test cases are the smallest number that exercises the framework end-to-end — a real driver session, a real screen, real waits, real assertions, real reporting, real screenshots — while keeping the blast radius of any framework defect discovered small and cheap to fix before the remaining 30 test cases are built on top of it.

TC-004 and TC-012 were not an arbitrary pick — they are the project's own Priority "Should"/"Must" entry points into the two shortest independent flows in MA-TC-001 (Authentication, Product Details → Cart) and, as detailed in §16 below, they are also the two test cases the project's own test-design documentation already flagged as needing hands-on resolution during implementation — making them a genuine, not synthetic, stress test of the framework.

## 2. Pilot Architecture

**Execution architecture:** TestNG (already the frozen test framework, MA-PV-001 §16) drives two `@Test` methods, one per pilot test case, each in its own Test Class extending `core.BaseTest`. `BaseTest`'s `@Listeners` annotation (already wired since Phase 7 — `SuiteListener`, `TestListener`, `MethodListener`) means no new listener registration is needed for the pilot to get reporting/logging lifecycle hooks "for free."

**Framework interaction (layer responsibilities, unchanged from their governing docs):**

```
Test Class (new, Phase 9.4)
  ↓ extends
BaseTest (core, Phase 7) — driver init/cleanup, listener registration
  ↓ uses
Page Objects (new, Phase 9.2) — extend BasePage (core, Phase 7)
  ↓ use
ElementActions / NavigationHelper (core, Phase 7) — interaction + logging + screenshot-on-failure
  ↓ use
WaitUtility (utils, Phase 5) — explicit waits
  ↓ use
DriverProvider (driver, Phase 4) — the one path to the live AppiumDriver
  ↓ configured by
ConfigReader (config, Phase 3) — environment/capabilities
```

Reporting/Logging/Screenshots attach across every layer as cross-cutting concerns (`reporting`, `logging`, `listeners` — Phase 6), not as a separate chain. Assertions (`assertions.CommonAssertions`, Phase 7) sit in the Test Class only, never in a Page Object (MA-FR-001 §2).

**Lifecycle, illustrated as the complete execution flow from TestNG execution to report generation:**

```
TestNG launches the suite
  → SuiteListener.onStart()         → ReportProvider.initializeReport()
  → BaseTest.initializeDriver()     → DriverProvider.initializeDriver() (@BeforeMethod)
  → TestListener.onTestStart()      → ReportProvider.createTest(name)
  → MethodListener.beforeInvocation → DEBUG log
  → Test method body executes:
        Page Object construction → ElementActions/NavigationHelper interactions
        → CommonAssertions.verify*() → pass/fail logged + reported (+ screenshot on failure)
  → MethodListener.afterInvocation  → DEBUG log (duration)
  → TestListener.onTestSuccess/onTestFailure → pass/fail reported; on failure, ScreenshotManager captures
  → BaseTest.quitDriver()           → DriverProvider.quitDriver() (@AfterMethod)
  → SuiteListener.onFinish()        → ReportProvider.flushReport() → .html file written
```

This is not new design — it is the literal, already-built lifecycle from `core.BaseTest` (MA-BF-001 §6) and `listeners.*` (MA-XCI-001 §6), restated here as the pilot's execution flow because no part of it needs to change for the pilot to run.

## 3. Package Structure Validation

| Pilot artifact | Package (already exists) | Confirmed sufficient? |
|---|---|---|
| `LoginPage`, `ProductsPage`, `ProductDetailsPage` | `src/test/java/.../pages` (currently empty — package-info only) | Yes — matches its own description exactly: "Page Object classes — one per screen" |
| `LoginTest`, `AddToCartTest` (see §9 for naming) | `src/test/java/.../tests` (currently empty) | Yes — "TestNG test classes containing test logic and assertions" |
| TestNG suite XML | `src/test/java/.../runners` (currently empty) — the package-info's own scope explicitly includes "XML suite definitions"; the physical file goes under `src/test/resources` (matching how `log4j2.xml`/`config.properties` already live in test resources, not test sources) | Yes — no new package needed |
| Login credentials dataset | `src/test/resources/testdata/<environment>/login/credentials.json` — **already exists** as a Phase 8 placeholder with generic sample values; content must be replaced with real, verified values (see §8, §16) | Yes, path-wise — content is not yet pilot-ready |
| Reusable components (if any) | `src/main/java/.../components` (10 existing classes) | See §6 — none of the ten are a clean fit for this app's actual header shape; no new component is created |
| Assertions | `src/test/java/.../assertions` — `CommonAssertions` (Phase 7) | Yes, as-is |

**No architectural change is required.** Every pilot artifact has an existing, correctly-scoped home.

## 4. Test Case Mapping

| | TC-004 — Login Outcome Verification | TC-012 — Add to Cart Action |
|---|---|---|
| Requirement | FR-004 (MA-RS-001) — "Should", acceptance criteria: post-login destination screen is capturable | FR-012 (MA-RS-001) — "Must", acceptance criteria: Add to Cart button interactable, resulting state change verifiable |
| Scenario | TS-004 (MA-TD-001) | TS-012 (MA-TD-001) |
| Page Objects | `LoginPage` (entry), `ProductsPage` (destination — see §16) | `ProductsPage` (navigate to a product), `ProductDetailsPage` (perform the action, verify state) |
| Components | None (see §6) | None (see §6) |
| Test Data | `LoginCredentials` (TD-001, **Verified** as of Phase 9.2 — see §8) | None — MA-TC-001 itself states "Not Applicable" |
| Assertions | Hard assertions via `CommonAssertions` (destination screen identity) | Hard assertions via `CommonAssertions` (verifiable state change — see §16 for what "verifiable" resolves to) |
| Reports | One ExtentTest node per test method, pass/fail per `CommonAssertions` call (MA-XCI-001 §4, MA-BF-001 §5) | Same |

## 5. Page Object Mapping

Only the three Page Objects the user's scope names — no more.

### `LoginPage`
- **Responsibilities:** enter username, enter password, tap the Login button, expose whatever is needed to identify the resulting screen (per TC-004 step 2).
- **Dependencies:** `core.BasePage` (extends), `models.LoginCredentials` (accepts as input), `locators` (not yet populated — see §7).
- **Components used:** none — the Login screen's layout (MA-LOC-001 §5) has no recurring widget shape shared with another screen; it is fully self-contained (title, two labeled fields, one button, an unused biometric/saved-credentials list out of pilot scope).

### `ProductsPage`
- **Responsibilities:** confirm the Catalog screen is displayed (used both as TC-004's destination-verification target and as TC-012's starting point), select a product to open its details (index-based, per §8), read the cart badge value (used only if TC-012's state-change verification resolves to a badge-count check — see §16).
- **Dependencies:** `core.BasePage`, `locators`.
- **Components used:** none (see §6) — cart badge/menu/sort are accessed directly via `ElementActions`, not wrapped in a component, for this narrow pilot.

### `ProductDetailsPage`
- **Responsibilities:** tap the Add to Cart button, expose whatever is needed to observe the resulting state change (per TC-012 step 2 and §16).
- **Dependencies:** `core.BasePage`, `locators`.
- **Components used:** none — color selector/quantity controls exist on this screen but are entirely out of scope for TC-012 (which only exercises the Add to Cart button itself, per its own two-step design in MA-TC-001).

No implementation of any of the above is performed in this phase.

## 6. Component Mapping

**Conclusion: no existing reusable component is instantiated by the pilot, and no new one is created.**

The app's actual global header (`menu_header_layout.xml`, MA-LOC-001 §6) has three independently-interactive icons (Menu, Sort — Catalog-only, Cart) plus a non-interactive logo — a shape that does not cleanly fit any single existing component (`HeaderComponent`/`ToolbarComponent` are both modeled as root+title+**one** action). Rather than force-fitting one of the ten existing components or building an eleventh (explicitly out of scope — "Do not create new components unless absolutely necessary"), the pilot's `ProductsPage` reads the cart badge directly via `core.ElementActions`, the same way every other screen-specific element is read. This is not a framework gap: none of TC-004/TC-012's steps involve a dialog, a loading indicator, a search bar, or bottom navigation — the only components that would apply to this app at all — so the Component Framework built in Phase 7 is validated by this pilot only at the level of "it compiles and is available," not exercised end-to-end. That is an honest, explicit limitation of a 2-test pilot, not a defect.

## 7. Locator Mapping

Every locator below is quoted directly from MA-LOC-001 — none is new, none is duplicated, none is hardcoded outside the future `locators` package.

| Screen | Element | Source (MA-LOC-001) |
|---|---|---|
| Login | Username Input (`nameET`) | §5 |
| Login | Password Input (`passwordET`) | §5 |
| Login | Login Button (accessibility id `Tap to login with given credentials`) | §5 |
| Login | Username/Password Error Text (if the resolved credential turns out invalid — see §16) | §5 |
| Product Catalog | Screen Title (accessibility id `title`) | §6 |
| Product Catalog | Product List / Card (`productRV`, index-based per §19.1) | §6 |
| Product Catalog | Cart Icon/Button (accessibility id `View cart`) | §6 |
| Product Catalog | Cart Badge Count (`cartTV`, conditionally rendered — §6's explicit warning: never assert on `cartTV` without first asserting presence/absence of `cartCircleRL`) | §6 |
| Product Details | Product Image/Name/Price (`productIV`/`productTV`/`priceTV`) | §8 |
| Product Details | Add To Cart Button (accessibility id `Tap to add product to cart`) | §8 |

**Confirmed: the Locator Repository is sufficient.** Every element either test case's steps touch is already documented, source-verified, and enterprise-named (MA-LOC-001 §21 mapping table already covers `nameET`→`loginUsernameField`, `passwordET`→`loginPasswordField`, `loginBtn`→`loginButton`, `cartBt` (Product Details)→`productAddToCartButton`, `cartTV`→`catalogCartBadge`, `cartRL`→`headerCartButton`). No locator gap exists for this pilot's scope.

## 8. Test Data Mapping

| Dataset | Test Case | Status | Notes |
|---|---|---|---|
| Valid Login Credentials | TC-004 | **Resolved (Phase 9.2, 2026-08-01).** Username `bod@example.com`, Password `10203040` — verified via the Manual Verification Phase (actual AUT execution), not GitHub source code or invented/sample data, per MA-TDD-001 §8.1.1. The Password value independently corroborates MA-LOC-001 §5's own source-level finding (`password1TV` = "10203040", Credential Row 1). MA-LOC-001 §5 separately confirmed Row 2's username as `alice@example.com (locked out)` — a **different, locked-out** account, not the one used here. |
| Product Selection | TC-012 | Not Applicable, per MA-TC-001 itself | No named product is required — MA-LOC-001 §19.1 explicitly directs index-based RecyclerView access ("never assume a fixed total count"); the pilot selects whichever product is at a fixed, arbitrary index (e.g. the first catalog card), not a specific product by name. |

No negative data, no random data, no dataset beyond these two — matching the pilot's explicit scope. The `LoginCredentials` model (`models.LoginCredentials`, Phase 8) and the `data.factory.LoginDataFactory.standardCredentials()` / `TestDataEnvironmentResolver` path (Phase 8) are reused exactly as built — no Test Data Framework change is needed, only new *content* in the existing JSON file location.

## 9. Test Execution Flow

```
BaseTest (@BeforeMethod)
  ↓
Driver Initialization (DriverProvider.initializeDriver() → AndroidDriverFactory → real Appium session)
  ↓
Configuration Loading (ConfigReader — already resolved at driver-creation time, e.g. app package/activity, capabilities)
  ↓
Page Object Creation (LoginPage / ProductsPage / ProductDetailsPage, constructed in the test method)
  ↓
Test Execution (Page Object interaction methods, driven by TestListener/MethodListener hooks around them)
  ↓
Assertions (CommonAssertions.verify* — hard assertions, one per MA-TC-001 expected result)
  ↓
Reporting (ExtentTest pass/fail entries via ReportProvider, attached automatically by CommonAssertions/TestListener)
  ↓
Screenshots (ScreenshotManager, automatic on any assertion or element-action failure — never on success, per MA-CS-001 §12)
  ↓
Driver Cleanup (BaseTest @AfterMethod → DriverProvider.quitDriver())
```

Two Test Class naming options exist under MA-CS-001 §2 ("Test classes are suffixed `Test` or `Tests`... pick one... do not mix"); this design adopts **`Test`** (singular) — `LoginTest`, `AddToCartTest` — as the pilot's first, and therefore precedent-setting, choice.

## 10. Reporting Flow

Unchanged from MA-XCI-001 §4 and MA-BF-001 §5 — restated as it applies to the pilot specifically: `SuiteListener.onStart` initializes one shared `ExtentReports` instance for the whole run; `TestListener.onTestStart` creates one `ExtentTest` node per test method (`LoginTest`, `AddToCartTest` each get their own); every `CommonAssertions.verify*` call inside those methods logs a pass or fail entry directly onto that node, with a screenshot attached on failure (`MediaEntityBuilder`, already wired — MA-BF-001 §5); `TestListener.onTestFailure` additionally captures a whole-test-method screenshot (distinct from the per-assertion one) if any *uncaught* exception (not an assertion) ends the test; `SuiteListener.onFinish` flushes the report to a timestamped `.html` file. No new reporting code is needed.

## 11. Screenshot Strategy

**Failure only — automatic, not manual.** This is not a new pilot decision; it is the existing, already-implemented framework behavior: `ElementActions` (MA-BF-001 §4) captures on any wrapped interaction failure, `CommonAssertions` (MA-BF-001 §5) captures on any assertion failure, `ScreenshotManager` (MA-XCI-001 §5) is the single path both use. Per MA-CS-001 §12 ("Screenshots are attached to the report on failure at minimum; attaching on every step is a later, explicit decision, not a default"), the pilot does not attach success-path screenshots — confirming alignment, not introducing a new rule.

## 12. Logging Flow

Also unchanged: every layer obtains its logger via `logging.LogManager.getLogger(...)` (never `LoggerFactory` directly, MA-CS-001 §11, confirmed 100%-compliant in MA-EFR-001 §9). Log levels follow the existing convention — `INFO` for step-level milestones (`ElementActions` action success, `TestListener` test start/end), `DEBUG` for fine-grained detail (`MethodListener` per-method timing, `ElementActions.isDisplayed` timeout-as-false), `WARN` for recoverable anomalies (`ScreenshotManager` capture failure, `RetryAnalyzer` retry attempts), `ERROR` for failures that abort a step (`ElementActions` wrapped failure, `CommonAssertions` failed verification, `TestListener.onTestFailure`). Integration with reporting is one-directional and already built: log statements do not themselves write to the report; `CommonAssertions`/`TestListener` write to both independently at the same call site.

## 13. Assertion Strategy

**Hard assertions only**, via the existing `assertions.CommonAssertions` (`verifyVisible`/`verifyText`/`verifyContains`, etc. — Phase 7). This was already determined not to require a soft-assertion companion for this exact reason in Phase 8.10 (MA-BF-001 §9): MA-CS-001 §14 only *prefers* soft-assertion grouping "when a single test step verifies multiple independent facts," and MA-TC-001 §4's own design rule — "Each step performs exactly one action and yields exactly one observable result" — means neither TC-004 nor TC-012 ever asks one step to verify more than one fact. Hard assertions are therefore not just sufficient but the better fit for these two test cases specifically; revisiting soft-assertion support remains appropriately deferred (as already decided) rather than reopened here.

## 14. Wait Strategy

`utils.WaitUtility` (Phase 5) — explicit, condition-based waits only, configurable timeout via `ConfigReader.getExplicitWaitTimeout()`, no `Thread.sleep()` anywhere — is confirmed sufficient for the pilot with **no redesign**. Every element either test case touches (login fields/button, catalog product card, cart badge, add-to-cart button) is a plain, non-custom-widget Android view per MA-LOC-001 (the only documented custom-widget risk in the whole app — `CreditCardNumberEditText`/`CreditCardDateEditText`, MA-LOC-001 §11 — belongs to the Payment screen, entirely out of pilot scope). No evidence demands a new wait condition beyond what `WaitUtility` already provides (`waitForVisibility`, `waitForClickable`, `waitForPresence`).

## 15. Framework Validation Matrix

| Layer | Validated by |
|---|---|
| Configuration Layer | Driver creation reading real capabilities (app package/activity, platform, automation name) from `ConfigReader` against a live Appium server for the first time |
| Driver Management | `DriverProvider.initializeDriver()`/`quitDriver()` executing a real session start/stop, not just the "no server reachable" exception path exercised in every prior phase's throwaway program |
| Core Utilities | `WaitUtility` resolving real explicit-wait conditions against real, rendering UI (not a mocked/absent driver) |
| Cross-Cutting Infrastructure | `LogManager`/`ReportProvider`/`ScreenshotManager`/`RetryAnalyzer`/listeners all firing in a real TestNG run for the first time (every prior validation was a standalone `main()` program, never an actual TestNG execution) |
| Base Framework | `BasePage`/`BaseTest`/`ElementActions`/`NavigationHelper` used by real Page Objects for the first time |
| Components | Available and compiled; **not** exercised end-to-end by this pilot (§6) |
| Assertions | `CommonAssertions` hard-assert path, including its reporting/screenshot integration, exercised against a real pass and (if any step fails) a real failure |
| Test Data Framework | `TestDataManager`/`LoginDataFactory`/environment resolution loading a real (not placeholder) credentials file for the first time |
| Locator Repository | Every locator this pilot touches resolved against the live app for the first time — the first genuine test of MA-LOC-001's accuracy |
| Overall Reusability | Whether `BasePage`/`ElementActions`/`CommonAssertions` need any change at all to support two unrelated screens is itself the reusability signal |

## 16. Pilot Risks

| Risk | Evidence | Mitigation |
|---|---|---|
| **TC-004's expected destination screen is formally unconfirmed** | MA-TC-001: "Automation Status: Blocked — TS-004 Expected Result contains 'Out of Current Observation'"; MA-RS-001 FR-004 acceptance criteria: "exact destination is Out of Current Observation" | Not a defect — MA-TD-001 itself states this resolves "through manual verification before or during execution of the scenarios that depend on them (TS-004...)." The Page Object list (`LoginPage`, `ProductsPage`, `ProductDetailsPage` — no separate post-login screen) strongly implies the destination is the Product Catalog screen itself (consistent with TC-001's confirmed Launch→Catalog behavior and the Drawer's Login/Logout state-toggle, MA-LOC-001 §14 row 10), but this must be confirmed against the live app during Phase 9.2/9.4, not assumed in this design. |
| **TC-012's expected state change is formally unconfirmed** | MA-TC-001: "Blocked — TS-012... Out of Current Observation"; MA-AA-001 §7: "Add to Cart destination screen Not Observed (may remain on same screen or update Cart state)" | Same resolution path as above. The deliberate exclusion of a `CartPage` from this pilot's Page Object scope strongly implies the expected behavior is "stays on Product Details, cart badge updates" rather than a screen transition — to be confirmed live, not assumed. |
| ~~TD-001's login credential values are formally unconfirmed~~ | **RESOLVED (Phase 9.2, 2026-08-01)** — MA-TDD-001 §8.1.1: Username `bod@example.com`, Password `10203040`, verified via the Manual Verification Phase | No longer a risk. Retained here, struck through, for traceability rather than deleted. |
| Cart badge is conditionally rendered, not merely hidden | MA-LOC-001 §6: `cartTV`'s parent subtree is absent from the tree entirely when the cart is empty | If TC-012's state-change verification resolves to a badge check, the Page Object must assert badge-container presence before reading its value — already a documented pattern (§19.1), not a new risk to solve, just to remember |
| RecyclerView / dynamic content | MA-LOC-001 §19: catalog size is data-driven, ids repeat per item | Mitigated by design (§8) — the pilot uses index-based selection, never a fixed product-name assumption |
| First-ever live TestNG/Appium execution | Every prior phase's validation used a standalone program, never a real TestNG run (§15) | This is the exact purpose of the pilot, not a risk to eliminate — but it does mean the *first* run should be treated as exploratory (see §17), not assumed green on the first attempt |
| Retry masking a real defect | `RetryAnalyzer` (Phase 6) is already wired via `execution.retryCount` | Confirm the configured retry count is appropriate for a pilot (a flaky-looking pass on retry should still be investigated, not silently accepted) — an execution-phase discipline note, not a framework change |

**No architectural blocker was found.** Every risk above is either an expected, already-anticipated resolution point (destination/state ambiguity) or a known, already-documented data-shape caveat (badge rendering, RecyclerView) — none requires a framework redesign.

## 17. Entry Criteria

Before Phase 9.2 (Page Objects) begins:

1. Foundation Status remains `APPROVED` / Pilot Automation remains `GO` (v1.0.0-foundation, MA-EFR-001, MA-FRR-001) — unchanged since Phase 8.10.
2. ~~A real, verified, working (non-locked-out) login credential pair for TC-004 must be obtained~~ — **MET (Phase 9.2, 2026-08-01)**: Username `bod@example.com`, Password `10203040`, verified via the Manual Verification Phase and recorded in MA-TDD-001 §8.1.1. See docs/framework/PRE_IMPLEMENTATION_DOCUMENTATION_RECONCILIATION.md.
3. An Appium server and an Android emulator or device must be reachable at the configured `appium.serverUrl` — every prior phase's own validation was explicitly limited by the absence of one (MA-DRV-001 §9 and every phase since). **Still to be confirmed** — not something a documentation reconciliation phase can verify.
4. `gradlew clean build` must remain green (confirmed as of Phase 8.10, MA-FRR-001 §5).

## 18. Exit Criteria

Before Phase 10 (or the next milestone) may begin:

1. TC-004 and TC-012 both pass consistently (§19).
2. The actual post-login destination and post-add-to-cart state are now documented facts, not "Pending Manual Verification" — MA-TC-001/MA-AA-001/MA-TDD-001 should be updated to reflect what was actually observed (a documentation follow-up, not part of this design phase's own deliverable).
3. No framework redesign was required to make either test pass (§32 in the Completion Requirement, tracked explicitly).
4. A Pilot Review (Phase 9.6, per §20) has been conducted and recorded.

## 19. Success Criteria

Restated from the phase brief, unchanged: TC-004 passes consistently; TC-012 passes consistently; no flaky failures; reporting works; logging works; screenshots work; assertions work; driver lifecycle is stable; configuration works; no framework redesign is required; framework remains reusable. Nothing in this design phase's findings (§16) contradicts any of these being achievable — the credential-value gap (§16, §17) was closed in Phase 9.2; the two remaining open items (the post-login destination screen, the post-add-to-cart state) are evidence gaps to close empirically during implementation, not framework capability gaps.

## 20. Phase 9 Implementation Plan

```
Phase 9.2 — Page Objects
  (LoginPage, ProductsPage, ProductDetailsPage; extends BasePage; no locators hardcoded outside `locators`)
        ↓
Phase 9.3 — Pilot Test Data
  (resolve TD-001's real credential values live; populate testdata/<environment>/login/credentials.json)
        ↓
Phase 9.4 — Pilot Test Classes
  (LoginTest, AddToCartTest; extend BaseTest; use CommonAssertions; no assertions in Page Objects)
        ↓
Phase 9.5 — Execution & Validation
  (first live TestNG/Appium run; resolve the two "Out of Current Observation" items empirically; repeat runs for stability per Success Criteria)
        ↓
Phase 9.6 — Pilot Review
  (Framework Validation Matrix re-checked against real evidence; Exit Criteria confirmed; recommendation for Phase 10 scope)
```

No other order is permitted, matching the phase brief exactly.

---

## Completion Report

### 1. Executive Summary

The Enterprise Framework Foundation (v1.0.0-foundation) already contains everything TC-004 and TC-012 need: correctly-scoped, currently-empty homes for the three required Page Objects, two Test Classes, and a TestNG suite (§3); every locator either test case touches, already documented and source-verified in MA-LOC-001 (§7); an existing, reusable Test Data Framework path for the one dataset needed (§8); and a fully-built execution/reporting/logging/screenshot/assertion chain that has never yet been exercised by a real TestNG run (§15). No architectural gap was found. Two genuine, evidence-based open items were surfaced — both are test-case/test-data readiness gaps already anticipated by this project's own test-design documentation, not framework defects (§16).

### 2. Pilot Architecture

See §2 above — TestNG → `BaseTest` → Page Objects (extending `BasePage`) → `ElementActions`/`NavigationHelper` → `WaitUtility` → `DriverProvider` → `ConfigReader`, with Reporting/Logging/Screenshots/Listeners cross-cutting throughout. No layer requires modification to support the pilot.

### 3. Test Mapping

See §4 — TC-004 maps to FR-004/TS-004, `LoginPage`+`ProductsPage`, one `LoginCredentials` dataset, hard assertions; TC-012 maps to FR-012/TS-012, `ProductsPage`+`ProductDetailsPage`, no dataset (per MA-TC-001 itself), hard assertions.

### 4. Page Object Mapping

`LoginPage`, `ProductsPage`, `ProductDetailsPage` — exactly the three named in scope, responsibilities and dependencies detailed in §5. No component is composed into any of them (§6).

### 5. Locator Mapping

Ten elements across Login/Catalog/Product-Details screens, all sourced directly from MA-LOC-001 §5/§6/§8 with no gap (§7).

### 6. Test Data Mapping

One dataset (Login Credentials, TD-001) whose *shape* is fully supported by the existing Test Data Framework and whose *values* are now known and verified as of Phase 9.2 (§8, §16, §17). Product selection needs no dataset at all.

### 7. Execution Flow

`BaseTest` → Driver Init → Configuration Loading → Page Object Creation → Test Execution → Assertions → Reporting → Screenshots → Driver Cleanup (§9) — the framework's existing lifecycle, unmodified.

### 8. Validation Matrix

Ten framework layers, each with a specific, named validation mechanism this pilot provides that no prior phase's throwaway program could (§15) — most importantly, the first-ever real TestNG/Appium execution.

### 9. Risk Assessment

Seven risks identified (§16); none requires a framework redesign. The two most material — TC-004/TC-012's formally unconfirmed expected results — are pre-existing, already-documented, already-anticipated-to-resolve-during-implementation items, not new findings this phase invented.

### 10. Entry Criteria

Foundation approval (met), a live Appium/device target (not yet confirmed reachable — outside a documentation phase's ability to verify), a real verified login credential pair (**met as of Phase 9.2** — `bod@example.com` / `10203040`, see §17), and a green build (met).

### 11. Exit Criteria

Both pilot tests passing consistently; the two remaining "Pending Manual Verification" items (post-login destination, post-add-to-cart state — the credential-value item was resolved in Phase 9.2) resolved and written back to MA-TC-001/MA-AA-001/MA-TDD-001; no framework redesign occurred; a Pilot Review conducted (§18).

### 12. Implementation Roadmap

Phase 9.2 (Page Objects) → 9.3 (Pilot Test Data) → 9.4 (Pilot Test Classes) → 9.5 (Execution & Validation) → 9.6 (Pilot Review), in that exact order (§20).

### 13. Final Recommendation

**Pilot Design Status: APPROVED**

The framework requires no architectural change to support this pilot. Implementation may proceed to Phase 9.2 once the one real, evidence-based prerequisite in §17 (a verified login credential pair) is available — this is a test-data/manual-verification prerequisite, not a design or framework deficiency, and does not change this design's approval status.

---

Stopping here. No implementation work was performed. Waiting for explicit authorization before beginning Phase 9.2 — Page Objects.
