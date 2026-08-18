# Phase B — Clean Allure Result Lifecycle + Local Validation

**Date:** 2026-08-15
**Type:** Implementation + controlled validation, tightly scoped. Not committed, not pushed, not tagged, no CI triggered.

---

## 1. Phase Objective

Establish a clean, deterministic local Allure reporting lifecycle: clean historical results → one controlled test execution → fresh Allure results → verify raw results → fresh Allure report → serve/open via the native Allure/Gradle workflow → confirm the report reflects only the controlled execution. GitHub Actions and Jenkins explicitly out of scope.

## 2. Part C — Physical Device Verification

- `adb devices -l` → `10BDAT2Y9U000DF  device product:I2301T model:I2301 device:I2301 transport_id:1`. Only this one device listed — no `emulator-5554`, no other entries.
- Appium server already running and healthy on port 4723: `curl http://127.0.0.1:4723/status` → `{"value":{"ready":true,"message":"The server is ready to accept new connections","build":{"version":"3.6.0"}}}` (PID 11304). **No second Appium instance was started.**

## 3. Part D — Controlled Test Execution

Command:
```
.\gradlew.bat test --tests "com.mobileautomation.framework.tests.LoginTest" --no-daemon -Denv=real-device -Ddevice.name=I2301 -Dplatform.version=15 -Ddevice.udid=10BDAT2Y9U000DF -Dapp.path=<scratchpad>\mda-2.2.0-25.apk
```
(`platform.version=15`/`device.name=I2301` were read directly from the connected device via `adb shell getprop ro.build.version.release`/`ro.product.model`, not guessed. `config-real-device.properties` documents exactly this invocation pattern.)

- **Gradle result:** `BUILD SUCCESSFUL in 56s`.
- **Test count:** 1 (`com.mobileautomation.framework.tests.LoginTest.loginOutcomeVerification`).
- **Pass/fail/skip:** 1 passed, 0 failed, 0 skipped — confirmed both from Gradle's own `build/test-results/test/TEST-com.mobileautomation.framework.tests.LoginTest.xml` (`tests="1" skipped="0" failures="0" errors="0" time="26.954"`) and `logs/automation.log`.
- **Physical-device evidence:** `adb devices` immediately after showed only `10BDAT2Y9U000DF`; the AUT package (`com.saucelabs.mydemoapp.android`) is installed on that exact device (`adb -s 10BDAT2Y9U000DF shell pm list packages`); 3 screenshots were captured with filesystem timestamps matching the run window exactly (22:53–22:54 local).
- **No retries, no manual reruns, no RetryAnalyzer invocation.** Test ran exactly once.

## 4. Part E — Fresh Raw Allure Result Verification

`build/allure-results` after the run:

| File type | Count |
|---|---|
| `*-result.json` | 1 |
| `*-container.json` | 5 |
| `*-attachment.*` | 0 (expected — see below) |
| `executor.json` | 1 |
| **Total** | **7 files, 14 KB** |

The single result: `uuid=19874151-...`, `name="TC-004 — Login Outcome Verification"`, `fullName=com.mobileautomation.framework.tests.LoginTest.loginOutcomeVerification`, `status=passed`, `historyId=c4bb416989d760b51f8c75bc6d19db2` (deterministic per test identity — matches the same historyId this test always produces, confirming Allure's own identity computation, not contamination), `start=2026-08-15T16:53:47.323Z`, `stop=2026-08-15T16:54:14.213Z`. All 7 files carry this exact run's timestamp window — **zero historical files present**, confirmed by direct enumeration (no leftover UUIDs from the pre-cleanup 45-result dataset). Zero attachments is correct, not a defect: the screenshot→Allure attachment wiring is only triggered on assertion failure (by design, from the original implementation phase); since nothing failed, there was nothing to attach.

`executor.json` reappeared automatically (`{"name":"Gradle","type":"gradle","taskName":"test",...}`) — this resolves an item Phase A's audit had left as "NOT VERIFIED": it is written by the Gradle Allure plugin on every `test` invocation, not something CI-specific.

## 5. Part F — Fresh Report Generation

Ran `./gradlew allureReport`. First attempt exposed a genuine, reproducible defect (see Section 11 below): the task completed `BUILD SUCCESSFUL` but the output directory's top-level `index.html`/`summary.json`/`data/` were byte-for-byte unchanged from before the controlled run — because **`allureReport` never clears its own output directory before writing**, and this run's fresh output landed in a differently-named nested subfolder rather than overwriting the stale top level. Confirmed via direct evidence: `build/reports/allure-report/allureReport/awesome/summary.json` held the correct fresh `{"total":1,"passed":1}` while the top-level `summary.json` (the file a developer would actually open first) still showed the old `{"total":20,...}`.

**Corrective action taken** (justified by Part F's own explicit requirement that "the new report MUST be generated after the fresh controlled execution" — i.e., what's visible must be fresh): deleted only `build/reports/allure-report/allureReport/` (report **output**, not raw results, not config, not the CLI install), then re-ran `./gradlew allureReport` cleanly.

Result after the clean regeneration:
- `BUILD SUCCESSFUL in 37s`.
- `build/reports/allure-report/allureReport/index.html` — present.
- `build/reports/allure-report/allureReport/data/` — present, contains exactly **1** file under `data/test-results/`.
- `build/reports/allure-report/allureReport/widgets/` — present.
- `summary.json` → `{"total":1,"passed":1}` — matches the controlled execution exactly, no `awesome/` nesting this time, no stale artifacts remaining.

## 6. Part G — Native Allure Serving Verification

`./gradlew help --task allureServe` (Gradle's own task-help output, not assumed) confirms the task's real, registered options:
```
--config-file, --depends-on-tests, --no-depends-on-tests, --host, --port, --verbose, --no-verbose, --rerun
```
**`allureServe` exists. `--depends-on-tests` is genuinely supported** ("Execute the relevant test tasks before launching Allure") — verified via the tool's own help output, not assumed from documentation. It was **not exercised** in this run: doing so would re-trigger `test`, and one successful controlled execution + one successful native-serve validation was already sufficient per the phase's own instruction not to unnecessarily rerun the test.

Ran plain `./gradlew allureServe`. Result: `Allure is running on http://localhost:60207`, confirmed listening via `netstat` (PID bound to the port), and confirmed serving real content — its own static JS bundle returned HTTP 200, and the on-disk content the server root directory contains (`build/reports/allure-report/allureServe/summary.json` → `{"total":1,"passed":1}`, exactly 1 file under `data/test-results/`) is the same freshly-generated, single-test data verified in Part F. **No Python was used at any point.** The server was stopped cleanly after validation (`taskkill` on the bound PID).

**Known limitation, not a new defect:** the server's root-level `/summary.json` and `/awesome/*` HTTP paths return a stub (`{"total":0}`) / 404 rather than the real data — the same routing quirk already documented in the original Allure implementation phase (the SPA fetches its real data via a client-side-computed path this environment's Browser pane cannot exercise, since it refuses `http://127.0.0.1:*` navigation by policy). Verification was completed the same rigorous way as before: direct inspection of the exact files the server's own directory serves from, cross-checked against a working static-asset fetch to confirm the server root is correct.

## 7. Part H — Report Content Verification

All 14 required checks, verified directly against `build/reports/allure-report/allureReport/data/test-results/8585d50d230b39ca49e903fe1f5079ac.json`:

| # | Check | Result |
|---|---|---|
| 1 | Total count corresponds only to the controlled execution | ✅ `total:1` |
| 2 | No historical tests appear | ✅ exactly 1 test-result file exists |
| 3 | No stale failures appear | ✅ no failed entries |
| 4 | No stale skipped tests appear | ✅ no skipped entries |
| 5 | LoginTest present | ✅ `fullName=...LoginTest.loginOutcomeVerification` |
| 6 | Status matches actual Gradle execution | ✅ `status=passed`, matches `BUILD SUCCESSFUL` + JUnit XML |
| 7 | Epic present | ✅ `"Authentication"` |
| 8 | Feature present | ✅ `"Login"` |
| 9 | Story present | ✅ `"Valid Login"` |
| 10 | Severity present | ✅ `"critical"` |
| 11 | `@Step` hierarchy present | ✅ 7 top-level steps, matching all 7 `CommonAssertions` calls in test order |
| 12 | Nested Page Object `@Step` hierarchy present | ✅ `Enter username`/`Enter password`/`Tap Login button` nested under `Log in with username` |
| 13 | Attachments present if generated | ✅ `attachments: []` — correct, nothing failed, so nothing was attached (by design) |
| 14 | No unexpected sensitive values exposed beyond current implementation | ⚠️ the password value (`10203040`) is present, unmasked, in the nested step's `parameters` array — this is the **same pre-existing, already-documented limitation** from the original implementation phase (public Sauce Labs demo credentials; `@Step` argument-masking was never configured). Not new, not changed here, per the instruction not to alter metadata during validation. |

No metadata was changed to make the report "look better" — this validates the existing implementation as-is.

## 8. Part I — Results Lifecycle Assessment

| Question | Answer |
|---|---|
| A. Does one controlled execution produce fresh Allure results? | **Yes** — confirmed Part E: 7 files, all within this run's exact timestamp window, zero contamination. |
| B. Does the generated report contain only that execution? | **Yes**, but **only after clearing the report's own output directory first** (Section 5) — `allureReport` does not do this itself. |
| C. Does native `allureServe` work? | **Yes** — serves real, correct, fresh HTML/JS/data with no Python involved; one HTTP-routing quirk at the root path exists but does not block the workflow (Section 6). |
| D. Does the browser open/serve the report correctly? | **Partially verified.** The server itself is confirmed healthy and serving correct static content; end-to-end browser rendering could not be directly observed in this sandboxed environment (Browser pane policy blocks `127.0.0.1` navigation) — same limitation as the original implementation phase, worked around the same way (direct file/HTTP inspection). |
| E. Does the workflow require Python? | **No.** Confirmed — the entire lifecycle (clean → test → results → report → serve) was completed using only Gradle/Allure-native tooling. |
| F. Is an explicit cleanup step still required for local development? | **Yes, for both directories**, not just `build/allure-results`. This phase proved a second, previously-undocumented instance of the same gap: `build/reports/allure-report/allureReport/` also needs to be cleared before each `allureReport` run for the output to be trustworthy — confirmed directly in Section 5, not inferred. |
| G. Does `allureServe --depends-on-tests` provide the one-command workflow? | **Yes, per the tool's own documented option** ("Execute the relevant test tasks before launching Allure") — confirmed to exist and be genuinely supported (Section 6), but not exercised end-to-end in this run to avoid an unnecessary test rerun. Recommended as the logical next single-command validation, when approved. |

## 9. Part J — Storage Impact

| Location | Phase A baseline | End of Phase B | Change |
|---|---|---|---|
| `build/allure-results` | 0.59 MB / 110 files | **0.00 MB / 7 files** | Net reduction; now holds only the one controlled execution |
| `build/reports/allure-report` | 3.15 MB / 120 files | **4.81 MB / 114 files** | Size increased slightly (both `allureReport` and `allureServe` outputs now coexist, each freshly generated) despite fewer total files, since stale duplicate content was removed |
| `build/allure` (Allure3 CLI + Node, project-local) | 344.15 MB | 344.15 MB | Unchanged — no new install occurred; the `--rerun-tasks` diagnostic step re-verified/re-extracted the same tool version in place, not a new one |

No unrelated files were deleted. No cache or dependency was added, removed, or upgraded.

## 10. Problems / Risks

1. **`allureReport` does not clear its own output directory before writing** (Section 5) — the same category of gap Phase A found for `build/allure-results`, now proven to also apply to the report output itself. Left uncorrected, this can silently leave a stale, misleading `index.html` at the expected entry point even after "successfully" regenerating.
2. **`allureServe`'s HTTP root API returns a stub/404 rather than real data** (Section 6) — cosmetic/routing quirk of the bundled Allure3 tooling, not a defect introduced by this project's configuration; does not block the workflow, since the server correctly serves the real static assets and the on-disk data is provably correct.
3. **Password value still unmasked in `@Step` parameters** (Section 7, item 14) — pre-existing, not addressed in this phase per instruction.
4. **A `--rerun-tasks` invocation of `allureReport` took 17m32s** (forcing re-verification/re-extraction of the ~344 MB bundled Node/Allure3 toolchain) versus 37s for a normal run — worth knowing if `--rerun-tasks` is ever reached for for as a "fix" for stale output; the correct, fast fix is clearing just the report output directory (Section 5), not forcing a full dependency-chain rerun.

## 11. Remaining Improvements (not implemented — for future approval)

- Add an explicit, documented `clean`-before-`allureReport`/`allureServe` convention (or a small wrapper task) so a developer doesn't need to know to manually delete `build/reports/allure-report/allureReport/` first.
- Consider standardizing on `allureServe --depends-on-tests` as the documented one-command local workflow (test → results → report → serve), now that the option is confirmed to exist.
- Decide the fate of `RetryAnalyzer.java` (still unwired, per Phase A).

## 12. GitHub Actions — OUT OF SCOPE

Not touched. `.github/workflows/mobile-automation.yml` was not read or modified during this phase.

## 13. Jenkins — OUT OF SCOPE

Not touched. No Jenkins files exist in this repository; none were created.

## 14. Final Verdict

**B. VERIFIED WITH MINOR REMAINING IMPROVEMENTS**

The core objective — a deterministic local lifecycle producing a report that reflects only a single controlled execution, served natively without Python — is fully achieved and directly verified end-to-end. It required one corrective action beyond the plan's literal steps (clearing the stale report output directory in Section 5), which was necessary, minimal, scoped only to generated report output, and directly justified by Part F's own explicit requirement. The remaining items (Section 11) are real but non-blocking process/documentation gaps, not defects in the Allure integration's actual behavior.

---

## Summary — Answers to the Required End-of-Phase Questions

1. **Final verdict:** B — VERIFIED WITH MINOR REMAINING IMPROVEMENTS.
2. **Exact files modified:** none (no source, test, config, CI, or Docker file was modified in this phase).
3. **Exact files created:** `docs/allure/PHASE_ALLURE_CLEAN_RESULT_LIFECYCLE_VALIDATION_REPORT.md` (this file). `build/allure-results/*` and `build/reports/allure-report/*` were regenerated (gitignored, not source-controlled, not "created" in the repository sense).
4. **Exact commands executed:** `adb devices -l`; `curl http://127.0.0.1:4723/status`; `.\gradlew.bat test --tests "com.mobileautomation.framework.tests.LoginTest" --no-daemon -Denv=real-device -Ddevice.name=I2301 -Dplatform.version=15 -Ddevice.udid=10BDAT2Y9U000DF -Dapp.path=<apk>`; `./gradlew allureReport` (twice — first exposed the stale-output issue, second after clearing output); `./gradlew help --task allureServe`; `./gradlew allureServe`; `rm -rf build/allure-results/*` (prior turn, Phase B Part B); `rm -rf build/reports/allure-report/allureReport` (this turn, Part F corrective step); various read-only `adb`/`curl`/`netstat`/`find` inspection commands.
5. **Test result:** 1 test run, 1 passed, 0 failed, 0 skipped. `BUILD SUCCESSFUL in 56s`.
6. **Fresh Allure result count:** 7 files total — 1 result, 5 containers, 1 executor.json, 0 attachments (correct for an all-passing run).
7. **Fresh Allure report result count:** 1 (matches the 1 test executed).
8. **Native `allureServe` works:** Yes, confirmed — serves real fresh content, no Python.
9. **`allureServe --depends-on-tests` works:** Confirmed to exist and be genuinely supported by the installed plugin (via the tool's own `help --task` output); not executed in this run to avoid an unnecessary test rerun.
10. **Was Python required:** No.
11. **Is the local Allure workflow now deterministic:** Yes, provided the report output directory is cleared before each `allureReport`/`allureServe` run — this is not yet automatic (Section 10, item 1) and is the one concrete gap standing between "works when done correctly" and "fully self-cleaning by default."

**Per the phase's stop rule: no GitHub Actions changes, no CI artifact publishing, no Jenkins, no Docker/Appium/driver/test-logic changes, no commit, no push, no tag.** Awaiting explicit approval before Phase C.
