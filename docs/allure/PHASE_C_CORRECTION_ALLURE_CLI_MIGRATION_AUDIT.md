# Phase C-Correction — Allure CLI Migration Audit

**Date:** 2026-08-16
**Type:** Audit only. No file was modified. This report is the only file created.

---

## 1. Current State

`git status --short`:
```
 M .github/workflows/mobile-automation.yml
 M build.gradle
 M src/main/java/com/mobileautomation/framework/listeners/TestListener.java
 M src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java
 M src/test/java/com/mobileautomation/framework/pages/LoginPage.java
 M src/test/java/com/mobileautomation/framework/tests/LoginTest.java
?? docs/allure/
?? docs/jenkins/
?? src/test/resources/allure.properties
```
HEAD unchanged at `35173f7ee5902500764805f3def58d5511007e7e`. Identical to the state at the end of the prior `allureServe` root-cause investigation — nothing drifted between sessions.

## 2. Current Allure Architecture (from `build.gradle`, read in full this session)

1. **Allure Gradle plugin**: `id 'io.qameta.allure' version '4.1.0'`.
2. **Allure dependencies**: `testImplementation platform('io.qameta.allure:allure-bom:2.35.3')` + `testImplementation 'io.qameta.allure:allure-testng'`.
3. **`allure-testng` version**: pinned via the BOM to `2.35.3`.
4. **Allure configuration blocks**: one — added in the prior phase:
   ```groovy
   allure {
       report {
           singleFile = true
       }
   }
   ```
5. **`allureReport` task**: not manually defined — auto-provided by the plugin.
6. **`allureServe` task**: not manually defined — auto-provided by the plugin.
7. **Custom report tasks**: none.
8. **`test { }` configuration**: `useTestNG()`, `failOnNoDiscoveredTests = false`, `systemProperties(System.properties)`, `systemProperty 'file.encoding', 'UTF-8'`. No Allure-specific configuration inside this block — the plugin wires into `test` automatically.
9. **`finalizedBy`**: none present anywhere in `build.gradle`.
10. **`systemProperty`**: only `file.encoding` (twice, unrelated to Allure).
11. **`allure.results.directory`**: not set in `build.gradle` — set in `src/test/resources/allure.properties` instead (Section 3).
12. **`singleFile` configuration**: present, `true` (Section 4 above) — added in the prior phase specifically to fix `allureReport`'s output; confirmed in that same investigation to **not** be honored by `allureServe`.
13. **Custom output directory configuration**: none — `reportDir` was never set, so both `allureReport` and `allureServe` use the plugin's own default (`build/reports/allure-report/<taskName>/`).

## 3. Current `allure.properties`

```properties
# Allure reporting configuration (Reporting Enhancement phase).
# Gradle-relative results directory — never inside src/, never committed
# (build/ is already excluded via .gitignore). Matches the Allure Gradle
# plugin's own default, made explicit here for clarity.
allure.results.directory=build/allure-results
```
Present, confirmed, exactly one line of real configuration. `allure.results.directory=build/allure-results` **is present**, matching the plugin's own default.

## 4. Standalone Allure CLI

- `allure --version` → **`2.39.0`**
- `where.exe allure` → `C:\Users\DELL\scoop\shims\allure` and `C:\Users\DELL\scoop\shims\allure.cmd`
- Installed via Scoop, usable directly from PowerShell (confirmed by the version check itself succeeding).
- **Important distinction, confirmed by version number alone**: this is the **classic Allure 2.x CLI** (`generate`/`serve`/`open`/`plugin` commands) — a structurally different tool from the Allure **3**.x "Awesome" report generator the Gradle plugin bundles and downloads separately (confirmed `allureVersion":"3.9.0"` in that generator's own output in the prior investigation). These are not two versions of the same thing; they are two different report-rendering architectures that happen to share the "Allure" brand name and consume the same `allure-results` JSON format.
- `allure generate --help` confirms real, usable flags: `-c/--clean`, `-o/--report-dir/--output` (default `allure-report`), `--single-file`, `--name`, `--lang`.
- `allure open --help` confirms: starts its own local web server directly against an already-generated static report directory, with `-h/--host` and `-p/--port` options.

## 5. Current Result Lifecycle

`build/allure-results`: **13 files** — **2** `*-result.json`, **10** `*-container.json`, **0** attachments, **1** `executor.json`. Both result files parsed as valid JSON and confirmed to represent genuine, correctly-structured test outcomes (`LoginTest.loginOutcomeVerification` and `NavigationTest.accessDrawerItems`, both `passed`). **This directly confirms the `allure-testng` integration itself generates valid results — the defect investigated previously was entirely in report *generation/serving*, never in result *writing*.** No tests were run to produce this — this is the same dataset left over from the immediately preceding investigation.

## 6. Current Report Outputs

| File | Path |
|---|---|
| `summary.json` | `build/reports/allure-report/allureReport/summary.json` |
| `summary.json` | `build/reports/allure-report/allureServe/summary.json` |
| `index.html` | `build/reports/allure-report/allureReport/index.html` |
| `index.html` | `build/reports/allure-report/allureServe/index.html` |

| Directory | total | passed | failed | broken | skipped | `meta.singleFile` |
|---|---|---|---|---|---|---|
| `allureReport` | 2 | 2 | 0 | 0 | 0 | `true` |
| `allureServe` | 2 | 2 | 0 | 0 | 0 | `false` |

No historical or nested (`awesome/`) subdirectories currently exist under either report path — both are in the clean state left at the end of the prior investigation. (The prior investigation directly documented that such nesting *can* recur non-deterministically on a subsequent `allureReport`/`allureServe` invocation against a non-empty output directory — that risk is unchanged and applies regardless of which report-generation tool is used going forward, since it was proven to be a property of how the Allure3-plugin-bundled generator's own incremental output behaves, not something either the current or proposed architecture fixes on its own unless the output directory is cleaned first.)

## 7. Evidence-Based Current Data-Flow Diagram

```
TESTNG (org.testng:testng:7.10.2, wired via test { useTestNG() })
   ↓  [allure-testng's AllureTestNg listener self-registers via META-INF/services — no @Listeners change needed]
allure-testng 2.35.3 (io.qameta.allure:allure-testng, via allure-bom:2.35.3)
   ↓  [writes to the path set by allure.results.directory in src/test/resources/allure.properties]
build/allure-results/  (RAW RESULT DIRECTORY — confirmed populated, 13 files, Section 5)
   ↓  [read by: Gradle plugin `io.qameta.allure` v4.1.0's auto-provided tasks]
GRADLE ALLURE PLUGIN  (downloads/bundles its own separate Allure 3.9.0 "Awesome" generator + Node.js runtime under build/allure/, 344.15 MB — confirmed this session via PowerShell recursive size)
   ↓  [writes to build/reports/allure-report/<taskName>/, per-task, no reportDir override configured]
build/reports/allure-report/allureReport/  and  build/reports/allure-report/allureServe/  (REPORT DIRECTORY — confirmed populated, Section 6)
   ↓  [./gradlew allureReport — confirmed this session: singleFile=true honored, produces one self-contained 3.6MB index.html]
allureReport (static artifact — this is what the already-implemented Phase C CI workflow uploads)
   ↓  [./gradlew allureServe — confirmed this session: singleFile NOT honored, produces multi-file output, starts a bundled Node HTTP server]
allureServe (live server — confirmed in the prior investigation, via direct curl of the live endpoint vs. the on-disk file at the identical path, to always return a stub "0 results" response regardless of correct underlying data)
   ↓
BROWSER  → confirmed broken for allureServe (user-observed and independently reproduced via curl); the allureReport single-file artifact's actual browser rendering remains unverified from this environment (file:// access is blocked in this sandbox, same restriction that has blocked all localhost verification throughout this engagement) — an open item explicitly flagged in the prior report, not resolved here.
```

## 8. Comparison With the Proven Previous Architecture

Reference model:
```
allure-testng 2.32.0
   ↓
build/allure-results
   ↓
allure generate build/allure-results --clean -o allure-report
   ↓
allure open allure-report
```

**Already matches:**
- `allure-testng` as the TestNG↔Allure adapter — present, one minor version newer (2.35.3 vs. 2.32.0), same mechanism.
- `build/allure-results` as the raw result directory — identical path and identical role in both architectures; **no change would be needed here at all**, confirmed by direct inspection (Section 5) — the standalone CLI's `generate` subcommand takes this exact directory as a plain positional argument, so it can point at this project's existing `build/allure-results` with zero reconfiguration.
- TestNG as the underlying framework, `allure-testng` as the adapter, both proven to produce valid results in this project (Section 5) — the reference architecture and this project's current architecture write results **identically**; they only diverge in what reads and renders those results afterward.

**Differs:**
- **Report generator**: current project uses the Gradle-plugin-bundled Allure **3**.9.0 "Awesome" generator; the reference architecture uses the standalone Allure **2**.x CLI's classic generator (this project's installed standalone CLI is 2.39.0, close to the reference's 2.32.0 — same generation, same report format).
- **Invocation mechanism**: current project invokes generation via Gradle tasks (`allureReport`/`allureServe`); the reference invokes a separate, already-installed CLI binary directly (`allure generate`/`allure open`), decoupled from the Gradle build entirely.
- **Serving mechanism**: current project's `allureServe` is proven broken (prior investigation); the reference's `allure open` uses the mature, long-established Allure 2.x static-report-plus-simple-server model, which does not share the specific defect mechanism diagnosed previously (that defect was tied to the Allure 3 "Awesome" plugin's own SPA/subpath-routing design, not to the underlying result format).

**Evaluation, not assumption**: the reference architecture is not automatically superior in the abstract — it is superior *specifically for the one proven-broken capability* (live local serving), based on direct evidence gathered this session (a real, versioned, working alternate tool already installed and confirmed functional via `--help`) rather than on the reference project's reputation alone.

## 9. Existing Features Confirmed Present (Must Not Be Lost)

Verified directly from the current `build/allure-results/0eb472ef-...-result.json` (LoginTest), re-confirmed this session, not assumed from a prior report:

| Feature | Present |
|---|---|
| Epic | ✅ `Authentication` |
| Feature | ✅ `Login` |
| Story | ✅ `Valid Login` |
| Severity | ✅ `critical` |
| Display name | ✅ (`name` field, populated from `@Test(description=...)`) |
| `@Step` | ✅ 7 top-level steps |
| Nested Page Object `@Step` | ✅ 3 nested steps under the login step |
| `CommonAssertions` Allure steps | ✅ every `verifyVisible`/`verifyHidden`/`verifyText` call produces its own step |
| Screenshot attachments | ✅ mechanism confirmed present in `CommonAssertions.evaluate()`/`TestListener.onTestFailure()` (0 in this specific dataset since nothing failed — correct, by design) |
| Test status | ✅ `passed` |
| Duration | ✅ `26270`ms |
| Parameters | ✅ present, including the already-documented unmasked-password nuance |
| ExtentReports coexistence | ✅ unaffected — confirmed the code paths are structurally independent (`ExtentReportManager` has no dependency on Allure report generation) |

None of this depends on which report *generator* is used — all of it is written by `allure-testng` into `build/allure-results` before either generator ever runs. **This is the single most important fact for the migration decision: every one of these features lives entirely upstream of the exact defect being evaluated for migration.** Nothing here is touched by this audit.

## 10. Migration Feasibility

**Would `allure generate build/allure-results --clean -o build/allure-report` followed by `allure open build/allure-report` be sufficient?** Based on the evidence gathered:

- **Yes, structurally sufficient** for local report generation and viewing. `build/allure-results` already exists at exactly the path the reference architecture expects (Section 8) — no `allure.properties` change needed. `allure generate --help` (Section 4) confirms `--clean`/`-o` work exactly as the reference architecture specifies. `allure open` starts its own server directly against the generated static directory — a fundamentally different, simpler serving model than the one proven broken.
- **One concrete adjustment worth noting, not requiring any file change now**: the reference architecture's literal `-o allure-report` places the report at the **project root**, which is **not** covered by the existing `.gitignore` (confirmed this session: `.gitignore` has no `allure` entry, only the general `build/` rule). Using `-o build/allure-report` instead keeps the output inside the already-ignored `build/` directory with zero `.gitignore` change required. This is a parameter choice, not a code change.
- **Gradle plugin disposition — A, B, or C**: recommend **A: remain**, for reasons in Section 14. The plugin is what makes `allure-testng`'s wiring automatic and is what the already-implemented (not-yet-committed) Phase C CI artifact upload (`allureReport`'s single-file output) currently depends on — removing it would be a strictly larger, riskier change than the proven defect requires fixing.

## 11. Proposed Local Developer Workflow (NOT implemented)

```
.\gradlew.bat test --tests "com.mobileautomation.framework.tests.LoginTest"
allure generate build/allure-results --clean -o build/allure-report
allure open build/allure-report
```

A convenience Gradle task (e.g. `allureLocalReport`) wrapping the two CLI invocations via `Exec`/`CommandLine` task types is plausible and would let a developer type one `./gradlew` command instead of remembering the separate CLI invocation — but this would require either assuming the CLI is present on every developer machine (not currently guaranteed — it's a manually Scoop-installed tool, not a project-managed dependency) or adding a provisioning step, which is exactly the kind of "unnecessary dependency" scope the current phase's own instructions caution against introducing. **Not designed further here, per instruction not to implement.**

## 12. CI Consideration (Analysis Only — No Workflow Change)

**Local reporting** (this audit's actual subject) and **CI report generation** (the already-written, not-yet-committed Phase C `aggregate` job step) are architecturally separate concerns and should be evaluated separately:

- The existing, uncommitted Phase C CI workflow diff runs `./gradlew allureReport` (not `allureServe`) inside the `aggregate` job and uploads its output directory as a GitHub Actions artifact — it never serves anything live. **This CI path does not exercise the specific defect investigated previously at all** (that defect is in live serving, and CI never serves — it only generates and uploads a static artifact for a human to download and open afterward).
- A CI migration to the standalone CLI would mean installing Allure 2.x CLI on the GitHub Actions runner (not currently present there — `where.exe allure`'s result reflects only this local Windows machine's Scoop installation) and replacing `./gradlew allureReport` with `allure generate ... --clean -o ...` plus an `upload-artifact` step pointed at that output instead. This is a real, evaluable option but is explicitly **not** analyzed further or implemented here, since GitHub Actions is out of scope for this phase by instruction.

## 13. Risk Analysis

| # | Risk area | Assessment |
|---|---|---|
| 1 | Existing Allure metadata (Epic/Feature/Story/Severity) | **No risk** — written by `allure-testng` before either generator runs (Section 9); migration only changes the generator. |
| 2 | `@Step` hierarchy | **No risk** — same reasoning. |
| 3 | Attachments | **No risk** — same reasoning; attachment files live in `build/allure-results` regardless of generator. |
| 4 | ExtentReports | **No risk** — structurally independent code path, confirmed unaffected by the prior `build.gradle` change and would be equally unaffected by a generator swap. |
| 5 | TestNG integration | **No risk** — `allure-testng`'s `ServiceLoader` registration is unrelated to which report generator later reads its output. |
| 6 | Result directory | **No risk** — `build/allure-results` is already the shared, correctly-configured path for both architectures (Section 8). |
| 7 | CI compatibility | **Out of scope this phase** (Section 12) — a real, separate decision, not free, not yet evaluated in depth. |
| 8 | Windows compatibility | **Low risk, evidenced** — the standalone CLI is already installed and functional on this exact Windows machine (Section 4), via Scoop shims that work from PowerShell. |
| 9 | Linux compatibility | **Not evidenced this session** — this audit only confirms the Windows/Scoop installation; CI runners are Linux (`ubuntu-24.04`) and would need their own separate installation step, not verified here (ties to Section 12/7). |
| 10 | Dependency duplication | **Real, concrete, evidenced**: the Gradle plugin already downloads its own separate 344.15 MB Allure3+Node runtime under `build/allure/` (confirmed this session) purely to power `allureReport`/`allureServe`. If the Gradle plugin is *kept* (Section 10's "A") specifically for its `allure-testng` wiring convenience while the standalone CLI is *also* used for local serving, both toolchains would coexist — a real, non-trivial storage/maintenance cost, not a blocker, but not free either. |
| 11 | Stale report prevention | **Improvement, not regression**: the standalone CLI's own `--clean` flag directly addresses the exact "stale output directory" issue the prior investigation had to work around manually (`rm -rf` before each generation) — this is a genuine, concrete advantage of the reference architecture over the current one. |
| 12 | Developer usability | **Currently ambiguous**: requires either every developer to have the CLI installed (a new manual setup step, not currently documented anywhere in this project) or a provisioning mechanism (not yet designed, Section 11). |

## 14. Recommendation

**B. MIGRATE TO STANDALONE ALLURE CLI ARCHITECTURE — for local report generation/serving only.**

Reasoning, grounded directly in this session's evidence: the specific, proven defect (`allureServe` showing 0 results) lives entirely in the Allure 3 "Awesome" plugin's live-serving mechanism (prior investigation, reconfirmed as still-broken in Section 6/7 here) — a defect the reference architecture's `allure open` does not share, because it is a structurally different, more mature serving model, and the standalone CLI capable of running it is **already installed and confirmed working** on this machine (Section 4), requiring no new installation to evaluate further. Every feature that must be preserved (Section 9) lives upstream of the generator and is unaffected either way. The `--clean` flag also directly resolves the separate, real "stale output directory" issue documented in the prior investigation, which the current Gradle-task-based workflow has no equivalent for.

**This recommendation is scoped to local developer workflow only** (Section 12) — it does **not** extend to a CI recommendation, which is a separate, unaddressed decision requiring its own evidence (Linux CLI availability/installation, artifact-upload path changes) not gathered in this audit.

**Smallest possible implementation plan** (NOT implemented — for future approval):
1. Document (not enforce via Gradle) the three-command local workflow in Section 11 — likely in a README/CONTRIBUTING note, not a build.gradle change, since the CLI's presence cannot be assumed without a provisioning decision.
2. Leave `build.gradle`'s Allure plugin block and the `singleFile = true` addition untouched — the plugin remains solely responsible for `allure-testng` wiring and for the CI artifact (`allureReport`, already fixed and already what CI uses per the uncommitted Phase C workflow).
3. Do not touch `allureServe` usage guidance beyond noting it is unreliable locally; recommend `allure open` as the documented replacement for local viewing.
4. Any CI-side change is a distinct, future, separately-scoped decision — not part of this plan.

## 15. Exact Files That WOULD Need Modification (if this recommendation is later approved and implemented)

- A documentation file (e.g. `README.md` or a new `docs/` note) describing the three-command local workflow — **not yet identified as a specific existing file to edit; would likely be a new, small addition**.
- Possibly `.gitignore`, only if `-o build/allure-report` were NOT used (Section 10) — avoidable by that parameter choice alone, so likely **zero change needed** even here.
- **No change to**: `build.gradle`, `allure.properties`, any Java source, `TestListener.java`, `CommonAssertions.java`, any Page Object, any test class, `.github/workflows/mobile-automation.yml`, `Dockerfile`, `.dockerignore`.

## 16. Files That MUST Remain Untouched

`build.gradle`, `src/test/resources/allure.properties`, `src/main/java/com/mobileautomation/framework/listeners/TestListener.java`, `src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java`, all Page Objects, all test classes, all Allure annotations (`@Epic`/`@Feature`/`@Story`/`@Severity`/`@Step`), ExtentReports code (`ExtentReportManager.java`, `ReportProvider.java`), `.github/workflows/mobile-automation.yml`, `Dockerfile`, `.dockerignore` — confirmed via this audit that none of these need to change for the recommended local-only migration, and none were touched during this audit itself.

---

## Final Safety Check

`git status --short` (re-run at the end of this audit):
```
 M .github/workflows/mobile-automation.yml
 M build.gradle
 M src/main/java/com/mobileautomation/framework/listeners/TestListener.java
 M src/test/java/com/mobileautomation/framework/assertions/CommonAssertions.java
 M src/test/java/com/mobileautomation/framework/pages/LoginPage.java
 M src/test/java/com/mobileautomation/framework/tests/LoginTest.java
?? docs/allure/
?? docs/jenkins/
?? src/test/resources/allure.properties
```
Identical to Section 1 — **no source, test, `build.gradle`, workflow, or Docker file was modified during this audit; no test was executed; only this report was created.**

**STOP — no migration implemented. No commit. No push. Awaiting explicit approval.**
