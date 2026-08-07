---
document_id: PHASE-17.2B
title: GitHub Actions Implementation Decision Report
version: v1.0
status: Final — Decision Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [MA-CICD-001, MA-CICD-002, PHASE-17.2A]
classification: Internal
---

# Phase 17.2B — GitHub Actions Implementation Decision Report

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document Name | GitHub Actions Implementation Decision Report |
| Version | v1.0 |
| Status | Final — Decision Report |
| Project | Mobile Automation Framework |
| Framework Version | 1.0.0 |
| Governing Documents | MA-CICD-001 (Architecture), MA-CICD-002 (Workflow Specification), Phase 17.2A (Readiness Report) |
| Classification | Internal |

---

## Method Note

This report makes concrete decisions, not architecture. It resolves the four gaps Phase 17.2A identified (APK sourcing, Appium server pinning, emulator specification, artifact path correction), plus two additional decisions the user requested (CI configuration strategy, known constraints). No YAML, shell command, or project file modification is produced here. Every decision is justified with evidence — either from this repository or from an external, cited source verified for this report. Where an external fact (a version number, a package name) could go stale between this report's writing and Phase 17.3's implementation, that risk is stated explicitly rather than hidden behind a false sense of permanence.

---

## Decision 1 — APK Strategy

### Options Evaluated

| Option | Advantages | Disadvantages | Maintenance | Enterprise Suitability | Future Scalability |
|---|---|---|---|---|---|
| **Commit APK into repository** | Simplest possible retrieval — no network dependency at CI run time | Permanently bloats repository size and history (binary diffs never shrink); misrepresents this repository's own scope, which owns none of the AUT's source or build; contradicts the project's existing discipline of committing no binaries anywhere else | High — every AUT update requires a manual commit and history grows unboundedly | Low — enterprise repositories generally treat committed binaries as a code-smell / anti-pattern for exactly this reason | Poor — does not extend cleanly to v2.0.0's iOS `.ipa`, doubling the anti-pattern |
| **Download APK during the workflow, from the AUT's own upstream source** | No binary ever enters this repository; provenance stays anchored to the exact upstream project (`saucelabs/my-demo-app-android`) that MA-LOC-001 already cites as its own source of truth; verified real and current — the upstream repository's [Releases page](https://github.com/saucelabs/my-demo-app-android/releases) publishes a `2.2.0` release (tagged, with build assets attached) matching this project's own documented `aut_version: 2.2.0` exactly | Introduces a run-time network dependency on a third-party repository's continued availability | Low — a version tag pin, re-verified only when the AUT version this project targets changes (an event that already requires updating documentation, per MA-LOC-001's own versioning discipline) | High — this is the standard, expected pattern for testing a third-party application: the test framework does not own or redistribute the AUT | Excellent — the same pattern extends to iOS (`.ipa` from the AUT's iOS repository) with zero structural change |
| **GitHub Release Asset (published on this repository)** | Would give this repository control over exactly which APK bytes are used | Would misattribute the AUT's provenance — publishing a third-party application as *this* repository's own release asset misrepresents ownership, and would require this repository to re-host and keep in sync a binary it does not build or control | Higher than the download option — someone must notice upstream AUT updates and re-publish | Low — re-hosting a third party's compiled binary under this project's own release namespace is not standard practice and could raise licensing/attribution concerns not otherwise present | Poor for the same re-hosting reason, worse at iOS scale |
| **GitHub Artifact** | N/A | Not applicable as a *source* — GitHub Actions artifacts are outputs of a prior workflow run within the *same* repository; this repository has no workflow that builds the AUT, so there is no artifact to draw from. This option answers a question ("how do I pass a file between jobs in one workflow") this decision is not asking | — | — | — |

### Final Decision

**Download the APK during the workflow, at run time, directly from the AUT's own GitHub Releases (`saucelabs/my-demo-app-android`), pinned to release tag `2.2.0`** — the exact version already cited as this project's `aut_version` and consistent with MA-LOC-001's cited source commit. Cache the downloaded file across workflow runs (keyed on the version tag) to avoid re-downloading on every run, but always source from the upstream release, never from a copy stored in this repository or on this repository's own release page.

**Resolves R1 (Phase 17.2A):** the downloaded APK's local path becomes the value supplied for `-Dapp.path=<path>` at CI invocation time, which — per `UiAutomator2CapabilityBuilder`'s verified behavior — causes Appium to install the AUT fresh each run, correctly matching the `ISOLATED` execution strategy's reset semantics instead of conflicting with them.

---

## Decision 2 — Appium Server Strategy

### Recommended Appium Server Version

Verified at the time of this report: the `appium` npm package's current stable line is **Appium 3.x** (generally available since August 2025), with `3.6.0` observed as the latest published version at the time of this research. **Recommendation: pin to the latest stable Appium 3.x patch release available at the moment Phase 17.3 is implemented**, re-verified against the npm registry at that time rather than trusting this report's snapshot indefinitely — Appium releases patch versions frequently, and this report's version observation is a point-in-time fact, not a permanent one. What must not change is the major line: **Appium 3.x**, because this project's own `DEPENDENCY_VERSION_FREEZE.md` already states Appium Java Client 9.4.0 targets Appium 3.x servers — pinning anything outside that major line would contradict an existing, frozen project decision.

### Installation Method

The Appium server is a Node.js application, installed via the Node package manager (`npm`), not via Gradle (which has no mechanism to install a non-JVM tool) and not preinstalled on the GitHub-hosted runner image (verified in Phase 17.2A — only Node.js itself is preinstalled, not the `appium` package). Two components must be installed explicitly:

1. The Appium server itself (the `appium` package), pinned to an exact version — not a floating tag like `latest`.
2. The UiAutomator2 driver, installed via Appium's own driver CLI (`appium driver install uiautomator2`) — verified as a required, separate installation step, since drivers are not bundled with the server in either Appium 2.x or 3.x. Verified externally: `appium-uiautomator2-driver` major version 5.0.0+ is compatible specifically with Appium 3 (source: the driver's own npm/GitHub documentation) — so the driver version installed must also be from its 5.x line or later, not an older 4.x driver release built against Appium 2.x.

### Version Pinning Strategy

Both the Appium server version and the UiAutomator2 driver version must be pinned to exact values (not `latest`), for the same reason this project already pins every other dependency (`build.gradle`'s exact version numbers, `gradle-wrapper.properties`'s exact Gradle version): an unpinned CI dependency is a reproducibility gap, which is exactly the gap Phase 17.2A (R2) identified. The two version numbers (server, driver) should be recorded wherever Phase 17.3's implementation records its other environment decisions — this report does not mandate a specific file, since editing project documentation is outside this report's scope, but the recording location should be visible and version-controlled, not left only inside a workflow file's own text.

### Future Maintenance Strategy

Appium server and driver versions should be treated the same way `build.gradle`'s dependencies are already treated: reviewed and deliberately bumped, not auto-updated silently. A version bump should be a visible, reviewed change (consistent with MA-CICD-001 §4's "Pipeline as Code" principle — a version pin is part of the pipeline's own code and should be reviewed like any other change), not something a `latest` tag does invisibly between runs.

### Final Decision

Pin Appium server to the latest stable **Appium 3.x** patch release verified at implementation time, install via `npm`, and separately install the UiAutomator2 driver from its 5.x-or-later line via `appium driver install uiautomator2`. Both versions are recorded explicitly, not left floating.

---

## Decision 3 — Android Emulator Strategy

| Property | Decision | Why This Is Appropriate for a GitHub-Hosted Runner |
|---|---|---|
| **Android API Level** | **API 34 (Android 14)** | Within this project's own documented supported baseline — MA-PV-001 states "Android 10 (API 29) and above" is the supported baseline — and is a mature, generally-available (non-preview) release with broad tooling support. It is also one of the Android SDK Platforms already preinstalled on the GitHub-hosted Ubuntu 24.04 runner image (verified in Phase 17.2A: platforms android-34 through android-37.1 are present), reducing what must be freshly downloaded |
| **System Image** | **`system-images;android-34;google_apis;x86_64`** | This exact package identifier was independently verified as a real, published Android SDK package, installable via `sdkmanager` and consumable by `avdmanager` to create an AVD. The `google_apis` variant (rather than `google_apis_playstore`) is the correct choice for an automated CI emulator — it provides Google Play Services APIs without the Play Store application itself, which is unnecessary overhead this project's tests do not exercise (no test in this suite interacts with the Play Store) |
| **Architecture** | **x86_64** | GitHub-hosted Linux runners are x86_64 hosts. An x86_64 system image allows the emulator to run under KVM hardware-accelerated virtualization, matching the host's own architecture. An ARM-architecture system image on an x86_64 host would require software-level instruction translation, which is dramatically slower and would defeat the purpose of enabling KVM at all (Phase 17.2A, R3) |
| **Device Profile** | **`pixel`** (a standard, SDK-bundled AVD hardware profile) | Verified as a real, directly usable `avdmanager -d` argument. A standard Google-defined profile is appropriate here because this framework's locator strategy (MA-LOC-001) is resource-id-based, not coordinate- or screen-density-based — no test in this suite depends on a specific screen size or DPI, so there is no technical reason to define a custom device profile |
| **Boot Timeout** | **A generous, explicit ceiling (on the order of several minutes) applied by the provisioning step itself, waiting for `adb`'s boot-completed signal — not left to Appium's own session timeout** | Emulator cold boot on a shared CI runner is measurably slower than a warm local boot. This is a provisioning-step concern, external to the framework (correctly identified as out of the framework's own configuration scope in Phase 17.2A, Section 3) — the framework's `driver.newCommandTimeoutSeconds=120` governs the Appium *session*, not emulator boot, and must not be conflated with it. A boot that does not complete within the ceiling should fail the provisioning step outright, not be silently retried, consistent with MA-CICD-001 §11's "no pipeline-level retry beyond the framework's own `RetryAnalyzer`" |
| **KVM Enablement** | **An explicit step that grants the runner's default user group permission to the `/dev/kvm` device, executed before the emulator is booted** | Verified externally (Phase 17.2A, R3; [GitHub Actions Changelog, April 2024](https://github.blog/changelog/2024-04-02-github-actions-hardware-accelerated-android-virtualization-now-available/)): hardware-accelerated emulation is available on GitHub's standard, default 2-vCPU-hosted Linux runners — no larger/paid runner tier is required — but the KVM device permission is not granted automatically and must be enabled explicitly as its own step |

### Final Decision

An x86_64 emulator running the `system-images;android-34;google_apis;x86_64` system image, on a `pixel` device profile, booted with KVM acceleration explicitly enabled beforehand and a hard, explicit boot-completion timeout — all on the GitHub-hosted runner's default (no larger runner tier needed). `platform.version=14` (matching API 34) is the value to be supplied to the framework's existing `platform.version` configuration key at CI invocation time.

---

## Decision 4 — Artifact Strategy Correction

Correcting Phase 17.2A's Section 6/7 findings, the verified, current artifact locations are:

| Artifact | Verified Path | Classification |
|---|---|---|
| ExtentReports HTML | `reports/AutomationReport_*.html` | **Required** — the framework's primary, human-readable execution evidence |
| Failure screenshots | `reports/screenshots/*.png` | **Required** — the only visual evidence of an assertion/execution failure this framework produces |
| Structured execution logs | `logs/automation.log` (+ rolled `.log.gz` archives) | **Required** — the only step-level diagnostic record available when the HTML report alone doesn't explain a failure |
| Gradle HTML test report | `build/reports/tests/test/` | **Required** — corrected location (Phase 17.2A, R5); this is Gradle's own native report, independent of and complementary to ExtentReports |
| Gradle test result data | `build/test-results/test/` | **Required** — corrected location (Phase 17.2A, R5); machine-readable result data, useful if any future tooling needs to parse outcomes without parsing HTML |
| `test-output/` | Does not exist in this project's actual build output (empirically verified — Phase 17.2A, Section 6) | **Not applicable** — remove this path from any future workflow implementation; it was a documentation error in MA-CICD-002, not a real artifact source |
| Allure results | Not produced — Allure is a v1.8.0 capability | **Future** — not part of v1.1.0's artifact set |
| Execution videos | Not produced — no recording capability exists in the framework | **Future / Not on the current roadmap** |

**No artifact in this project's current output is "optional."** Every one of the five Required rows above is produced automatically by an already-existing, already-verified code path or Gradle behavior (Phase 17.2A, Section 7) — none require new framework code, and none should be silently dropped from the upload set, since MA-CICD-001 §11's principle is that a failing run's evidence is more valuable than a passing run's, not less.

---

## Decision 5 — CI Configuration Strategy

### Options Considered

| Option | Assessment |
|---|---|
| **Introduce `config-ci.properties`** | Would duplicate the vast majority of `config-emulator.properties`'s existing content (platform, automation name, driver timeouts, reporting/logging paths are all already correct for CI) to override just two or three values (`platform.version`, `app.path`, possibly `device.name`). A near-duplicate file is a maintenance liability — a future change to `config-emulator.properties` would need to be remembered and mirrored into `config-ci.properties`, which directly contradicts MA-CICD-001 §4's "Single Source of Truth" principle |
| **Reuse `config-emulator.properties`, supplying the missing values via system property at CI invocation time** | The framework's own four-tier configuration precedence (system property → environment file → common file → compiled default, verified in `ConfigReader`) was already built to support exactly this pattern — `config-real-device.properties`'s own header comment already documents supplying `device.name`/`platform.version`/`device.udid` via system property at execution time for a real device; CI is the same pattern applied to the `emulator` profile instead |
| **Another approach (e.g. environment variables consumed outside the configuration layer)** | Not recommended — would bypass the framework's existing, already-correct configuration precedence entirely, introducing a second, parallel configuration mechanism the framework does not otherwise have anywhere |

### Technical Justification

`config-emulator.properties` is **already** the framework's default profile (active whenever `-Denv` is not supplied at all — verified in Phase 17.2A) and already gets everything right except the two values that are, by the file's own design, meant to be supplied per-environment (`platform.version`, and, via the common `config.properties` file, `app.path`). Introducing a new file to override values a system property can already override is unnecessary duplication, not a missing capability.

### Final Decision

**Reuse `config-emulator.properties` as the CI execution profile, supplying `-Dplatform.version=14` and `-Dapp.path=<downloaded-APK-path>` (Decision 1) as system properties at the point the workflow invokes Gradle.** No new configuration profile file is introduced by this decision. If `device.name=emulator-5554` (the profile's committed default) does not match the actual serial the chosen AVD-provisioning tooling assigns, that alone should be overridden the same way — via system property — not by editing the committed file.

---

## Decision 6 — Known Constraints

The following limitations are intentional and will remain true immediately after v1.1.0 ships. None are defects; each is a deliberate scope boundary consistent with MA-CICD-001's "no redesign for future releases" requirement — every one of them is already accounted for as a *later* roadmap item, not an oversight of v1.1.0.

| Constraint | Why It Remains |
|---|---|
| CI tests against exactly one Android API level (34) | No matrix/multi-version execution strategy exists yet; introducing one is a test-execution-strategy decision outside this report's scope, and MA-CICD-002 §9 already scoped v1.1.0 to a single target |
| CI execution is emulator-only — no real device in the loop | GitHub-hosted runners have no physical device access; real-device coverage remains a local developer activity, with cloud device coverage deferred to v1.7.0/v1.9.0 (MA-CICD-001 §14) |
| A CI pass does not, by itself, reconfirm the real-device evidence in MA-TC-001 | Per MA-CICD-002 §14's explicit framing — CI results must be treated as a parity-verification exercise, especially for the first several runs, not an assumption of equivalence |
| No smoke/regression subset — every run executes all 19 automated test cases | No TestNG groups exist in the framework today (verified, Phase 17.2A/MA-CICD-002 §9); introducing them is a framework-level test-design change, not a CI change |
| No parallel execution — the suite runs sequentially | Scoped to v1.3.0 (MA-CICD-001 §14); the `ThreadLocal` driver design already anticipates it without requiring this workflow to be redesigned when it arrives |
| Appium server/driver versions require manual, deliberate re-pinning | Consistent with how every other dependency in this project is already managed (`build.gradle`) — no automated dependency-update bot is in scope for v1.1.0 |
| APK retrieval depends on the AUT's upstream repository remaining available and its release tagging convention remaining stable | An accepted external dependency, identical in kind to how `mavenCentral()` is already an accepted external dependency for every other library this project uses |
| No signing, publishing, or release-artifact pipeline | This workflow builds and tests; it does not produce or publish a distributable artifact of its own (this framework is not a distributed library) |
| No self-hosted or elastic device-farm capacity | Not anticipated as needed by the current roadmap — cloud device coverage is routed through dedicated providers (BrowserStack, Sauce Labs) rather than self-hosted infrastructure (MA-CICD-001 §5) |

---

## Final Implementation Checklist

Every item below is written to be directly translatable into one GitHub Actions workflow step in Phase 17.3, in execution order.

- [ ] Checkout the repository at the triggering commit.
- [ ] Set up JDK 17 (pin explicitly rather than relying on the runner image's default, per MA-CICD-002 §5).
- [ ] Restore/save the Gradle dependency and wrapper cache, keyed on `build.gradle` and `gradle-wrapper.properties`.
- [ ] Enable KVM device permissions on the runner (Decision 3).
- [ ] Install the Android system image `system-images;android-34;google_apis;x86_64` via `sdkmanager`.
- [ ] Create an AVD using the `pixel` device profile and the above system image via `avdmanager`.
- [ ] Boot the emulator and wait for `adb`'s boot-completed signal, enforcing an explicit timeout ceiling (Decision 3); fail the step, do not retry silently, if the ceiling is exceeded.
- [ ] Install the pinned Appium server version via `npm` (Decision 2).
- [ ] Install the UiAutomator2 driver (5.x-or-later line) via `appium driver install uiautomator2` (Decision 2).
- [ ] Start the Appium server and confirm it is reachable at `http://127.0.0.1:4723` before proceeding.
- [ ] Download the AUT APK from `saucelabs/my-demo-app-android` release tag `2.2.0` (Decision 1), restoring from cache if already present for this tag.
- [ ] Resolve the downloaded APK's local path for use as `-Dapp.path=<path>`.
- [ ] Run Gradle's build/compile validation via the committed wrapper (`./gradlew`); halt the workflow here on any compile failure.
- [ ] Run the full test suite via `./gradlew test`, supplying `-Dplatform.version=14` and `-Dapp.path=<downloaded-APK-path>` as system properties (Decisions 3 and 5) — no other test command, no group/tag filtering (Decision 6).
- [ ] Confirm the five Required artifact locations (Decision 4) contain the current run's output: `reports/`, `reports/screenshots/`, `logs/`, `build/reports/tests/test/`, `build/test-results/test/`.
- [ ] Upload all five Required artifact locations, unconditionally — whether the run passed or failed.
- [ ] Publish a workflow summary showing pass/fail status and a link to the uploaded artifacts.

Nothing on this checklist requires a decision this report or its predecessors (MA-CICD-001, MA-CICD-002, Phase 17.2A) has not already made. Phase 17.3 may proceed directly to implementation.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Final — Decision Report | — | — |

---

**End of Document — Phase 17.2B Implementation Decision Report, v1.0**
