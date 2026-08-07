---
document_id: PHASE-17.2A
title: GitHub Actions Implementation Readiness Report
version: v1.0
status: Final — Verification Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [MA-CICD-001, MA-CICD-002]
classification: Internal
---

# Phase 17.2A — GitHub Actions Implementation Readiness Report

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document Name | GitHub Actions Implementation Readiness Report |
| Version | v1.0 |
| Status | Final — Verification Report |
| Project | Mobile Automation Framework |
| Framework Version | 1.0.0 |
| Current Release | Foundation Release |
| Governing Documents | MA-CICD-001 (Architecture), MA-CICD-002 (Workflow Specification) |
| Classification | Internal |

---

## Method Note

This is a verification report, not an implementation. No project file was modified to produce it, and no YAML, shell command, or code was written. Every claim below is labeled either **Verified** (confirmed directly against the repository, a build artifact produced by a real local `gradle` run, or an external authoritative source, cited where external) or **Recommendation** (a judgment this report is making, not a fact already true of the project). Where a fact could not be established from available evidence, that is stated explicitly rather than assumed.

---

## 1. Executive Summary

**Overall Implementation Readiness: IMPLEMENTATION READY WITH MINOR PREPARATION** (justified in full in Section 11).

The framework's build, configuration, and reporting layers are structurally compatible with a GitHub Actions/GitHub-hosted-runner execution model, and MA-CICD-001/MA-CICD-002's architecture and job design require no changes. However, this assessment found **concrete, previously undocumented implementation gaps** that must be resolved before a first CI run can succeed — none of them architectural, all of them resolvable within Phase 17.3's own implementation work.

### Major Findings

1. **The current default execution configuration cannot run unmodified on a fresh CI emulator.** `app.path` is blank by default, and the `ISOLATED` execution strategy (the framework's default) sets `noReset=false` — which presumes the AUT is already installed on the target device. A freshly booted CI emulator has nothing installed. This is a real gap, not previously resolved by MA-CICD-002, which correctly identified APK sourcing as open but did not identify this specific incompatibility with `noReset=false`.
2. **No Appium *server* version is pinned anywhere in the repository.** `build.gradle` pins the Appium **Java client** (9.4.0) and its own documentation (`DEPENDENCY_VERSION_FREEZE.md`) states client 9.4.0 is compatible with "Appium 3.x servers" — but no specific server version is fixed in version control. This is a reproducibility gap relative to MA-CICD-001's own "Reproducible Builds" principle.
3. **Hardware-accelerated Android emulation on GitHub-hosted Linux runners is available but not automatic.** It requires an explicit KVM-permission-enabling step that was not identified in MA-CICD-002 §5/§14.
4. **MA-CICD-002 §11 misidentified the TestNG XML artifact location.** Empirical verification (a real local Gradle build, see Section 6) shows Gradle's TestNG integration writes to `build/test-results/test/` and `build/reports/tests/test/`, not `test-output/` as stated in MA-CICD-002. `test-output/` does not exist anywhere in this repository's build output.

None of these findings require revisiting MA-CICD-001 or MA-CICD-002's architecture, job design, trigger strategy, or artifact model. All four are implementation-level decisions or corrections that Phase 17.3 must incorporate.

---

## 2. GitHub Runner Compatibility

| Requirement | Framework Needs | GitHub-Hosted Runner (Ubuntu 24.04) Provides | Status |
|---|---|---|---|
| Java Version | Java 17 (verified: `build.gradle` — `JavaLanguageVersion.of(17)`) | JDK 17.0.19+10, installed and set as the **default** Java on the Ubuntu 24.04 image (Verified — GitHub `runner-images` documentation) | Compatible, no gap |
| Gradle Version | Gradle 9.0.0, wrapper-pinned (Verified: `gradle/wrapper/gradle-wrapper.properties`) | Not relevant — the project's own committed wrapper (`gradlew`) downloads and uses 9.0.0 regardless of any Gradle version the runner image ships | Compatible, no gap, provided the wrapper (not a runner-provided Gradle) is invoked |
| Operating System | No OS-specific code exists in the framework (pure Java/Gradle/Appium stack) | Ubuntu 24.04 LTS (Verified — GitHub `runner-images`) | Compatible, no gap |
| Android SDK — platform-tools / build-tools / platforms | `adb`, and Android platform SDKs matching the AUT's target API level | Platform-Tools 37.0.0, Build-Tools (34.0.0–36.1.0), SDK Platforms android-34 through android-37.1 preinstalled (Verified — GitHub `runner-images` documentation) | Compatible for `adb`/build tooling — **gap identified below** |
| Android Emulator system image + AVD | A bootable Android Virtual Device matching the framework's `platform.name=Android` / `automation.name=UiAutomator2` target | **Not preinstalled as a ready-to-boot AVD.** The SDK command-line tools are present, but a system image must still be downloaded (`sdkmanager`) and an AVD created (`avdmanager`) as an explicit step | **Gap — must be provisioned as a workflow step, already anticipated conceptually as "Android Emulator Provisioning" in MA-CICD-002 §7, now confirmed as a real (not just anticipated) requirement** |
| KVM Hardware Acceleration | Emulator boot time and stability at a scale usable for CI feedback | Available on GitHub-hosted Linux runners since April 2024, but **requires an explicit workflow step to enable KVM device permissions** — it is not on by default (Verified — [GitHub Actions Changelog, April 2024](https://github.blog/changelog/2024-04-02-github-actions-hardware-accelerated-android-virtualization-now-available/)) | **Gap — not identified in MA-CICD-002; must be added as an explicit provisioning step in Phase 17.3** |

**No missing requirement blocks the runner itself** — Java, Gradle (via wrapper), and base Android SDK tooling are all satisfied by the standard GitHub-hosted Ubuntu image. The gaps are in *emulator readiness* (Section 3) and *Appium readiness* (Section 5), not in the runner's base compatibility.

---

## 3. Android Emulator Readiness

Reviewed: `src/test/resources/config/config.properties`, `config-emulator.properties`, `CapabilityConfiguration.java`, `UiAutomator2CapabilityBuilder.java`.

| Property | Current Configuration (Verified) | CI Readiness |
|---|---|---|
| `platform.name` | `Android` (common `config.properties`) | Ready — no change needed |
| `automation.name` | `UiAutomator2` (common `config.properties`) | Ready — no change needed |
| `platform.version` | **Blank** in `config-emulator.properties`, by explicit design ("depends on which system image the local AVD was created with... Set explicitly before use") | **Gap** — must be supplied via `-Dplatform.version=<version>` at CI invocation time, matching whatever system image Phase 17.3 provisions. This is a configuration value to supply, not a code change |
| `device.name` | `emulator-5554` (the conventional ADB serial for the first AVD instance) | **Unverified assumption** — this value is correct only if the AVD-provisioning mechanism Phase 17.3 selects actually assigns this serial. Not yet confirmed against a specific tool choice |
| `app.path` | **Blank by default.** `UiAutomator2CapabilityBuilder` (Verified, line 40–42) only calls `options.setApp(...)` when `app.path` is non-blank — if left blank, Appium is never told to install anything | **Critical gap** — see Section 4 |
| Reset Behavior | `execution.strategy=ISOLATED` is the framework's default (Verified: `config.properties`), which `ExecutionStrategy.java` documents as producing `noReset=false` | **Compounds the `app.path` gap** — `noReset=false` instructs Appium to reset the AUT's existing state at session start, which presupposes the AUT is already installed. On a freshly booted, stateless CI emulator with nothing installed and no `app.path` supplied, this combination is expected to fail, not silently degrade |
| Boot Timeout | Not a framework configuration concern — `driver.newCommandTimeoutSeconds=120` governs the Appium *session* timeout, not emulator boot time. No emulator-boot-timeout value exists anywhere in the framework's configuration, because that responsibility belongs to whatever provisioning step boots the AVD, external to the framework itself | Not a framework gap — correctly out of the framework's scope; must be set in the provisioning step |

### Can the framework execute on a GitHub-hosted emulator without modification?

**No.** Not because the architecture is wrong, but because two configuration values that are *intentionally* left blank in source control (`platform.version`, `app.path`) must be supplied at CI invocation time — exactly as their own comments already anticipate (`config-emulator.properties`: "depends on which system image the local AVD was created with"; `config.properties`: "or override this key in a future cloud/CI-specific profile"). No framework code needs to change; two runtime values need to be supplied, and the `app.path`/`noReset` interaction needs a resolved strategy (Section 4).

---

## 4. APK Strategy Verification

### Where should the APK come from?

| Approach | Evaluation |
|---|---|
| **Committed to the repository** | Not recommended. Committing a binary APK to version control bloats repository size and history permanently, and this framework's own `.gitignore` and documentation discipline (no binaries committed anywhere else in the project) argues against introducing the first one here |
| **GitHub Release Asset (on this repository)** | Not recommended as the *source* — this repository does not build or own the AUT; publishing someone else's application as a release asset on this repository would misrepresent provenance |
| **Workflow download from the AUT's own public source** | **Recommended.** The AUT is the open-source Sauce Labs My Demo App (Android), whose own repository (`saucelabs/my-demo-app-android`) publishes APK releases. MA-LOC-001 already treats this exact upstream repository as its own source of truth, citing a specific source commit (`8cf5fac23ca6cedafe7be3c63fad8fe4ee6f5612`) for locator verification. Downloading a matching APK at workflow run time — from the same upstream source MA-LOC-001 already trusts — keeps the AUT's provenance consistent with the project's existing evidence chain, rather than introducing a second, independent source |
| **Generated/built by this repository** | Not applicable — this repository is a test automation framework, not the AUT's build system. It has no Android application module, `AndroidManifest.xml`, or app-side build configuration; building the AUT is outside this project's scope entirely |
| **Cached as a workflow-level artifact after first download** | A refinement of the "workflow download" approach, not a separate approach — reduces repeated downloads across runs but does not change where the APK originates |

### Recommendation

**Download the APK from the AUT's own public source at workflow run time, pinned to the same version/commit MA-LOC-001 already cites, and cache it across runs to avoid repeated downloads.** This is the only approach that: does not add binary weight to this repository, does not misattribute the AUT's provenance, and stays consistent with the exact upstream source this project's own locator evidence is already anchored to. The specific release asset URL and version pin is an implementation detail for Phase 17.3, not an architectural decision — but the *source* (the AUT's own upstream releases, matching MA-LOC-001's cited commit/version where possible) should not be substituted for a different or unverified APK.

This resolves cleanly with `app.path` supplied to the downloaded APK's local path at CI invocation time — which also resolves Section 3's `noReset=false` gap: with `app.path` set, Appium installs the AUT fresh each run, which is compatible with `ISOLATED`'s reset semantics rather than in conflict with them.

---

## 5. Appium Readiness

| Concern | Verified State | Assessment |
|---|---|---|
| Appium Java Client | 9.4.0, pinned in `build.gradle` (Verified) | Ready |
| Driver | UiAutomator2, declared via `automation.name=UiAutomator2` and built through `UiAutomator2Options` (Verified) | Ready |
| Server Version Compatibility | The project's own `DEPENDENCY_VERSION_FREEZE.md` states client 9.4.0 targets "Appium 3.x servers" (Verified quote) | Directionally clear, but... |
| **Appium Server Version Pin** | **No specific Appium server (npm package) version is pinned anywhere in this repository** — not in `build.gradle` (which cannot pin a Node.js package), not in any documentation, not in any config file | **Gap.** A CI run that installs "whatever the latest Appium 3.x is" on the day it happens to run is not reproducible in the sense MA-CICD-001 §4 requires. A specific server version should be selected and recorded before Phase 17.3, consistent with how every other tool in this project is version-pinned |
| Node.js Dependency | Appium server is a Node.js application; Node.js is required to install and run it | Node.js 22.23.1 is preinstalled on the GitHub-hosted Ubuntu 24.04 runner (Verified — GitHub `runner-images` documentation), comfortably exceeding Appium's minimum supported Node version | Ready — no additional Node.js provisioning step needed beyond confirming the preinstalled version is used |
| Appium CLI / Server Installation | Not preinstalled on the runner image — must be installed via the Node package manager as an explicit workflow step | **Gap** — anticipated conceptually by MA-CICD-002 §7's "Appium Server Startup" step, but that step's specification did not include installation, only "start and confirm reachable." Installation must be added explicitly in Phase 17.3 |
| UiAutomator2 Driver Installation | Appium 2.x/3.x servers require drivers to be installed separately from the server itself (`appium driver install uiautomator2` or equivalent) | **Gap** — same as above; not distinguished from server installation in MA-CICD-002's step description. This is a discrete sub-step Phase 17.3 must include |
| Server Reachability Assumption | Framework connects to `appium.serverUrl=http://127.0.0.1:4723` by default (Verified: `config.properties`) | Compatible with a same-runner Appium server — no framework change needed, provided the CI-started server binds to this address |

**Summary:** the Java-side Appium integration is fully ready. The gap is entirely on the *provisioning* side — installing a specific, pinned Appium server version and the UiAutomator2 driver on the runner before the framework's driver layer can connect to anything. This was under-specified, not wrong, in MA-CICD-002.

---

## 6. Gradle Execution Readiness

| Concern | Verified State |
|---|---|
| Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, and `gradle/wrapper/gradle-wrapper.properties` are all present and committed (Verified via direct file listing). The wrapper is fully self-contained and requires no runner-provided Gradle installation |
| Build Command | `./gradlew clean build` — already the project's own documented build command (README, Getting Started) |
| Compile Validation | Gradle's own task graph compiles `src/main` and `src/test` automatically ahead of the `test` task — no additional CI configuration is needed to enforce this; it is inherent Gradle behavior |
| Test Command | `./gradlew test` — already the project's own documented full-suite command; requires no CI-specific variant |
| `failOnNoDiscoveredTests` | Set to `false` in `build.gradle`'s `test {}` block, a leftover from the pre-automation Foundation phase (comment confirms this explicitly). Since real tests exist and are discovered today, this setting has no effect on CI behavior — it will not mask a genuine "zero tests ran" failure differently than it would locally | No gap |
| **Test Output Location** | **Empirically verified** by inspecting this repository's own local `build/` directory (produced by a prior real `gradle` run): Gradle writes compiled classes to `build/classes/`, test results to `build/test-results/test/`, and the HTML test report to `build/reports/tests/test/`. **No `test-output/` directory exists anywhere in this project's actual build output**, despite `.gitignore` listing it | **Correction to MA-CICD-002 §11** — the artifact source path documented there for "TestNG XML result output" is wrong. The correct paths are `build/test-results/test/` (XML) and `build/reports/tests/test/` (HTML), both currently excluded from version control via `.gitignore`'s `build/` entry |
| Expected CI Issue | None identified in the Gradle layer itself. The only CI-relevant risk is ensuring the Gradle dependency cache is keyed correctly (already addressed as a recommendation in MA-CICD-002 §9) so that a `build.gradle` change correctly invalidates a stale cache | No unresolved gap |

---

## 7. Artifact Readiness

| Artifact | Verified Output Location | Auto-Created on a Fresh Checkout? | Uploadable As-Is? |
|---|---|---|---|
| ExtentReports HTML | `reports/AutomationReport_<timestamp>_<id>.html` (Verified — `ExtentReportManager`, and confirmed by the 72 existing report files currently present locally) | Yes — `ExtentReportManager` explicitly calls `FileUtility.createDirectoryIfNotExists(...)` before writing (Verified, source read) | Yes |
| Failure Screenshots | `reports/screenshots/*.png` (Verified — `ScreenshotUtility.persistScreenshot` → `FileUtility.copyFile`, which creates the target's parent directory before copying) | Yes — confirmed via the same `FileUtility.createDirectoryIfNotExists` call path (Verified, source read) | Yes |
| Structured Logs | `logs/automation.log` (+ rolled `automation-<date>-<n>.log.gz` archives) | Yes — Log4j2's `RollingFileAppender` creates its target file's parent directory by default; this is standard Log4j2 behavior, not custom code in this repository | Yes |
| Gradle HTML Test Report | `build/reports/tests/test/index.html` | Yes — created automatically by the Gradle `test` task itself | Yes — but this path was **not previously documented** in MA-CICD-002; add it explicitly in Phase 17.3 |
| Gradle Test Result XML | `build/test-results/test/` | Yes — same as above | Yes — same correction as above |

**No directory is missing.** Every artifact-producing code path in this framework independently guarantees its own output directory exists before writing to it — there is no risk of an "output directory not found" failure on a completely clean CI checkout. The only readiness item here is a **documentation correction** (Section 6): MA-CICD-002 pointed at `test-output/`, which is not where Gradle actually writes TestNG results in this project.

Two directories referenced in earlier CI planning (`src/test/resources/reports/`, `src/test/resources/screenshots/`) were verified to be **unrelated, reserved-but-unused skeleton folders** for future static test resources (their own README files state this explicitly) — not the runtime output paths. This distinction is confirmed here to prevent Phase 17.3 from confusing the two.

---

## 8. Environment Configuration

| Item | Verified State | Readiness |
|---|---|---|
| `config.properties` (common) | Defines `execution.strategy=ISOLATED` as the framework-wide default, and leaves `app.path` blank with a comment already anticipating override "via `-Dapp.path=<absolute-path-to-apk>` for a local .apk, or override this key in a future cloud/CI-specific profile" | The configuration layer was already designed with a CI use case in mind — no structural change needed, only supplying the anticipated override |
| `config-emulator.properties` | Active by default when `-Denv` is not supplied at all (Verified — no `-Denv` flag is required to select this profile); leaves `platform.version` blank by design | Ready as a *base* profile; requires `platform.version` (and confirmation of `device.name`) at CI invocation time |
| Environment Switching Mechanism | Four-tier precedence: system property → environment file → common file → compiled default (Verified — `ConfigReader`) | Fully sufficient for CI — no new mechanism is needed; CI simply needs to supply the right system properties at invocation |
| **Future CI-Specific Profile** | **Does not exist yet.** No `config-ci.properties` or equivalent file is present in the repository | **Missing, but not blocking** — CI can run entirely on the existing `emulator` profile plus system-property overrides (`-Dplatform.version=...`, `-Dapp.path=...`, `-Ddevice.name=...` if needed) without a new profile file. Whether to introduce a dedicated CI profile file for readability is a Phase 17.3 implementation preference, not a prerequisite |

**Missing configuration, stated plainly:** `platform.version` and `app.path` have no value today and must be supplied at CI invocation time. No other configuration key is missing.

---

## 9. GitHub Secrets Assessment

| Category | Assessment |
|---|---|
| **Required today (v1.0.0 / v1.1.0 scope)** | **None.** Emulator-based, locally-hosted Appium execution requires no credential of any kind — no cloud device provider, no signing key, no API token. This is consistent with MA-CICD-002 §13 |
| **Unnecessary today** | Any credential for BrowserStack, Sauce Labs, or Azure DevOps — provisioning these now would create unused secrets with no consuming code, expanding the security surface for no present benefit (already the stated principle in MA-CICD-001 §12) |
| **Required in future releases** | BrowserStack username/access key (v1.7.0), Sauce Labs username/access key (v1.9.0), Azure DevOps service connection credentials (v1.5.0 — if Azure DevOps is used as an additional pipeline, its own credential model applies, separate from GitHub Secrets) |

No gap identified — this matches MA-CICD-002's existing assessment exactly, and this independent re-verification confirms no framework code path introduced a secret requirement that MA-CICD-002 missed.

---

## 10. Risk Assessment

| # | Risk | Classification | Mitigation |
|---|---|---|---|
| R1 | `app.path` blank + `noReset=false` (ISOLATED default) on a stateless CI emulator will fail session start, since Appium is given no app to install and nothing is pre-installed | **Critical** | Resolve via Section 4's recommendation: download and pin an APK from the AUT's own upstream source, supply `-Dapp.path=<downloaded-path>` at CI invocation. Must be resolved before the first CI run is attempted, not discovered by a failing first run |
| R2 | No Appium server version is pinned anywhere in the repository | **Major** | Select and record a specific Appium server version (npm package version, e.g. a pinned `appium@x.y.z` within the 3.x line already confirmed compatible) before Phase 17.3, and document it alongside the other pinned tool versions (`DEPENDENCY_VERSION_FREEZE.md` or the workflow file's own version pin) |
| R3 | KVM acceleration requires an explicit enablement step not yet documented anywhere in this project's CI documentation | **Major** | Add an explicit KVM-permission-enabling step to Phase 17.3's job design, informed by GitHub's own published guidance |
| R4 | Emulator system image and AVD are not preinstalled — must be created at runtime, adding to per-run setup time | **Major** | Anticipated conceptually in MA-CICD-002 §7 ("Android Emulator Provisioning"); confirmed here as a real, non-optional step requiring a specific system-image/API-level choice matching `platform.version` |
| R5 | MA-CICD-002's documented TestNG artifact path (`test-output/`) does not match actual Gradle output (`build/test-results/test/`, `build/reports/tests/test/`) | **Minor** | Correct the artifact path in Phase 17.3's implementation; no framework change needed |
| R6 | `device.name=emulator-5554` is an assumption, not yet confirmed against the specific AVD-provisioning tool Phase 17.3 will select | **Minor** | Confirm during Phase 17.3's first implementation pass; adjust the value only if the chosen provisioning method assigns a different serial |
| R7 | Emulator-based CI execution has never been run — all existing pass/fail evidence (MA-TC-001) is real-device only | **Observation** (already identified and correctly framed in MA-CICD-002 §14 as requiring parity verification, not assumed equivalence) | No new mitigation needed beyond what MA-CICD-002 already specifies — restated here only to confirm this report did not find a reason to weaken that existing caution |
| R8 | Runtime output directories (`logs/`, `reports/`, `reports/screenshots/`) do not exist on a fresh checkout | **Observation, not a risk** | Verified as self-mitigating — every relevant code path (`ExtentReportManager`, `ScreenshotUtility`/`FileUtility`, Log4j2's `RollingFileAppender`) creates its own output directory automatically. No action needed |

---

## 11. Final Readiness Decision

# IMPLEMENTATION READY WITH MINOR PREPARATION

### Evidence Supporting This Decision

**Why not "NOT READY":** No finding in this report requires revisiting MA-CICD-001's architecture or MA-CICD-002's job design, trigger strategy, permissions model, or artifact strategy. Every job/step MA-CICD-002 already defined (Repository Checkout, JDK Setup, Gradle Cache, Android Emulator Provisioning, Appium Server Startup, AUT Availability, Build Validation, Test Execution, Report Collection, Artifact Upload, Workflow Summary) remains correct as designed. The gaps found (R1–R6) are things to *decide and configure within those already-designed steps*, not reasons to redesign the pipeline.

**Why not "IMPLEMENTATION READY" (unconditionally):** R1 (the `app.path`/`noReset` interaction) is a genuine, verified blocker — attempting a first CI run without resolving it would fail at session start, not from an infrastructure problem but from a configuration/strategy gap this report found by tracing actual code (`UiAutomator2CapabilityBuilder`, `ExecutionStrategy`). R2 and R3 are real reproducibility and setup gaps this report found were not previously documented anywhere in MA-CICD-001 or MA-CICD-002. Declaring the project unconditionally ready would understate these.

**Why "WITH MINOR PREPARATION" is the accurate middle ground:** every gap identified (R1–R6) has a clear, bounded resolution already stated in this report (Sections 3–7, 10) — an APK source decision, a version pin, an explicit provisioning step, a path correction. None require new architecture, new documents, or reopening MA-CICD-001/MA-CICD-002. They are exactly the kind of concrete detail Phase 17.3's implementation work is expected to resolve.

### What Must Happen Before Phase 17.3 Produces a Working Workflow

1. Resolve R1: decide and pin the APK source (Section 4's recommendation), and confirm `app.path` will be supplied at CI invocation time.
2. Resolve R2: select and record a specific Appium server version.
3. Resolve R3: include explicit KVM-enablement in the emulator provisioning step.
4. Resolve R4: select a specific Android system image/API level for the AVD, consistent with the `platform.version` value that will be supplied.
5. Correct R5 in whatever document or workflow comment references artifact paths, so Phase 17.3 does not implement against the wrong path.
6. Confirm R6 empirically once a specific AVD-provisioning tool is chosen.

None of the above requires a new architecture document. Phase 17.3 may proceed once items 1–4 have concrete answers.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Final — Verification Report | — | — |

---

**End of Document — Phase 17.2A Implementation Readiness Report, v1.0**
