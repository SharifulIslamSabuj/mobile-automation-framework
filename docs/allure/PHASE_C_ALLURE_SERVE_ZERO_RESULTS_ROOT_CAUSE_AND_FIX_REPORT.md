# Phase C — Allure Serve "0 Results" — Root Cause and Fix Report

**Date:** 2026-08-16
**Type:** Diagnosis + minimal fix. Not committed, not pushed, GitHub Actions not touched, Docker not touched, test logic not touched.

---

## 1. Current Symptom

User-reported, observed in a real browser: `allureServe`'s served report showed `Results: 0`, `Total: 0`, `No results`, `Allure Report v3.9.0`, with the server itself confirmed running. This was reported despite a prior session having verified (via file-level and curl-status-code inspection) that `build/allure-results` held exactly 2 valid results and the generated report's on-disk `summary.json` showed `total:2, passed:2`.

## 2. Reproduction Steps

1. Confirmed `git status --short` unchanged from before (only the pre-existing 6-file diff from earlier phases).
2. Physical device `10BDAT2Y9U000DF` was **not connected** this session (`adb devices -l` empty, confirmed not a stale-daemon artifact via `adb kill-server`/`adb start-server`). This blocked Parts 3/8/9's fresh-execution/regression steps — see Section 13.
3. Used the existing, already-valid 2-result dataset in `build/allure-results` (LoginTest + NavigationTest, both passed, from the prior session) rather than fabricate new results.
4. Ran `./gradlew allureServe` against this data and, critically, **curled the live HTTP endpoint directly** (`curl http://localhost:<port>/summary.json`) — a check the prior session had not actually performed for its final "clean" state; it had only compared disk-file content, which turned out to be an insufficient proxy for what a real browser receives.

## 3. Raw Result Directory

`build/allure-results` — confirmed via `Get-ChildItem`/`node` JSON parsing: 2 `*-result.json`, 10 `*-container.json`, 1 `executor.json` = 13 files, matching the prior session's controlled 2-test dataset exactly, both `passed`, full Epic/Feature/Story/Severity/step data intact for LoginTest.

## 4. Report Generation Directory

`build/reports/allure-report/allureReport/` and `build/reports/allure-report/allureServe/` — both exist as separate output directories, one per task, matching `build.gradle`'s plugin defaults (no `reportDir` override was configured). No `allure { }` extension block existed in `build.gradle` prior to this investigation.

## 5. Serve Directory

`build/reports/allure-report/allureServe/` — confirmed this is genuinely what the live server serves from (its own static JS bundle returns HTTP 200 at the expected relative path), so the *directory* being served is correct. The defect is not "wrong directory served."

## 6. Exact Root Cause

**Category G — a specific mechanism not covered by the listed categories, proven with direct evidence, not inferred:**

The served `index.html`'s own embedded configuration (`window.allureReportOptions = {"id":"awesome", ..., "href":"awesome/", ...}` — read directly from the live HTTP response) tells the client-side JavaScript that the report's real data lives under an `/awesome/` subpath. But:

- **When the plugin's own generation happens to write data flat at the output root** (no nested `awesome/` folder — the more common case for a genuinely clean generation): the live `GET /summary.json` **always** returns a hardcoded, generic stub — `{"stats":{"total":0},...,"href":"awesome/"}` — **regardless of what the actual on-disk `summary.json` at that exact path contains.** Proven directly: on-disk `build/reports/allure-report/allureServe/summary.json` contained the full correct `{"stats":{"total":2,"passed":2},"newTests":[...]}`, while `curl http://localhost:<port>/summary.json` against the exact same running instance returned the stub. The live server is not serving that file's real content for this route.
- **When generation happens to nest data under an `awesome/` subfolder** (observed non-deterministically across multiple runs in this and the prior session): the files that folder contains are real and correct (confirmed by reading `awesome/summary.json` and `awesome/index.html` directly from disk), but `curl http://localhost:<port>/awesome/summary.json` and `/awesome/index.html` both return **404** — the bundled dev server does not serve anything under that subpath either.

**Net effect: in every configuration observed, the live-served report's client-side JS is directed to fetch data from a path the embedded server never correctly serves, so the SPA falls back to its empty "0 results" state — independent of whether the underlying result data is correct.** This fully explains the user-observed symptom and is not contradicted by any of the prior session's own file/status-code checks, because none of those checks had curled the *content* of the live `/summary.json` response — only its HTTP status code (200) or the on-disk file (correct), neither of which detects this specific mismatch.

## 7. Minimal Fix

Added a single, officially-documented Gradle DSL block to `build.gradle`, verified against the plugin's own source documentation (not guessed):

```groovy
allure {
    report {
        singleFile = true
    }
}
```

Verified via two independent fetches of `allure-framework/allure-gradle`'s own README (a search-engine-summarized version and the raw `README.md` source directly from GitHub) — both confirm `singleFile` as a real, documented property of the `report { }` extension block, whose purpose is producing one self-contained HTML file with all data embedded, rather than a directory of separately-fetched JSON/JS/CSS files. This directly removes the mechanism the defect depends on (a separate client-side fetch to a subpath) rather than patching a symptom of it.

**Sources:**
- [allure-framework/allure-gradle README](https://github.com/allure-framework/allure-gradle/blob/main/README.md)
- [Raw README.md](https://raw.githubusercontent.com/allure-framework/allure-gradle/main/README.md)

## 8. Files Modified

**`build.gradle` only** — one new 15-line commented block (`allure { report { singleFile = true } }`), added directly after the existing `plugins { }` block. No other file touched.

## 9. Before/After Behavior — Important Nuance, Not Fully Resolved

| | `allureReport` | `allureServe` |
|---|---|---|
| **Before fix** | Root `summary.json`/`index.html` sometimes stale or non-deterministically nested under `awesome/`; live serving affected by Section 6's defect | Same defect, confirmed live via curl |
| **After fix** | **Confirmed changed and fixed for the artifact itself**: output is now exactly 2 files (`index.html` at 3.6MB, `summary.json` at 610 bytes), `index.html` self-contained with the app bundle embedded as a `data:` URI, on-disk `summary.json` correctly shows `{"total":2,"passed":2}` | **Confirmed NOT fixed.** Re-ran `allureServe` after the `build.gradle` change: it wrote a completely different, multi-file output (`data/`, `widgets/`, separate JS bundles, a 5,430-byte `index.html`) — the `singleFile` setting was not honored by this task. Live `curl /summary.json` still returned the identical `total:0` stub. |

**This is an honest, important limitation to flag, not glossed over**: `singleFile` appears to apply only to `allureReport`'s output, not `allureServe`'s. This makes practical sense — a single self-contained file is meant to be opened directly (`file://` or as a downloaded artifact), not served by a dev HTTP server — but it means **`allureServe` itself remains broken and unfixed by this change.**

**A further, honest limitation**: I attempted to verify whether the fixed `allureReport` single-file artifact actually renders correctly when opened directly (no server, `file://`) — this is the natural next verification step and would have been definitive. This sandboxed environment's Browser pane refused the `file://` path (`"the file may be missing, unreadable, or the user declined access"`), the same class of restriction that has blocked all `http://127.0.0.1` verification throughout this entire multi-day engagement. **I could not independently confirm real browser rendering of the fixed artifact from my own tools.** I did not claim success based on file content or exit codes in place of this — I'm reporting the gap directly instead. Since you observed the original defect in your own real browser, you are able to test this specific artifact (`build/reports/allure-report/allureReport/index.html`, opened via `file://` directly, no server needed) in a way I cannot from here.

## 10. LoginTest Validation

**Not re-executed this session** — the physical device `10BDAT2Y9U000DF` was not connected (`adb devices -l` empty, confirmed not a stale-daemon issue). Part 3's "run one controlled test to reproduce with fresh data" and Part 8's full regression validation could not be completed. All raw-result inspection in this report uses the existing, already-verified 2-result dataset (LoginTest + NavigationTest) from the immediately preceding session, which remains on disk and intact.

## 11. Allure Feature Regression Validation

Confirmed directly from the existing `build/allure-results/*-result.json` (unaffected by the `build.gradle` change, since `singleFile` only governs report *generation*, not result *writing*):

| Feature | Status | Evidence |
|---|---|---|
| Epic = Authentication | ✅ | `labels` array, LoginTest result |
| Feature = Login | ✅ | same |
| Story = Valid Login | ✅ | same |
| Severity = critical | ✅ | same |
| Display name = "TC-004 — Login Outcome Verification" | ✅ | `name` field |
| Assertion steps | ✅ | 7 steps present, all `passed` |
| Nested LoginPage `@Step` hierarchy | ✅ | 3 nested steps under the login step |
| Status = passed | ✅ | `status` field |
| Duration | ✅ | `26270`ms |

None of this data changed as a result of the `build.gradle` fix — confirmed, not assumed, by direct re-inspection after the change.

## 12. ExtentReports Regression Validation

**Verified by code-path independence, not by a fresh execution** (device unavailable): `build.gradle`'s `singleFile` addition only configures the `allure { report { } }` extension, which governs the Allure Gradle plugin's own report-generation tasks. `ExtentReportManager`, `ReportProvider`, `TestListener`, `CommonAssertions`, and the AllureReports/ExtentReports dual-write code path were not touched by this change, and ExtentReports has no dependency on the Allure report-generation configuration at all — they are two structurally independent reporting mechanisms sharing only the same TestNG execution. No code was modified that could plausibly affect ExtentReports. I have not re-run a test to empirically re-confirm this in this session, and say so directly rather than implying I did.

## 13. Remaining Known Issues

1. **`allureServe` remains broken** — confirmed live, not fixed by this change. Local developers using `allureServe` directly will still see "0 results" in a real browser.
2. **The fixed `allureReport` single-file artifact's actual browser rendering is unverified** — blocked by this environment's `file://` restriction, not by any evidence the fix doesn't work. This needs your own confirmation.
3. **Physical-device regression validation (Parts 3, 8, 9) is incomplete** — the device was not connected this session. Nothing was fabricated to route around this; the gap is reported directly.
4. If `allureServe`'s live-browser defect needs a separate fix, that would require further investigation into why that specific task doesn't honor `singleFile`, or an entirely different local-viewing strategy (e.g., documenting `file://` access to the `allureReport` artifact as the supported local workflow instead of `allureServe`) — not attempted here, since it would go beyond "smallest correct fix" without first confirming whether the `allureReport` artifact alone is sufficient for your needs.
5. Minor, unrelated observation: the served page loads `https://www.googletagmanager.com/gtag/js` — the bundled Allure3 "Awesome" report plugin phones home to Google Analytics. Not something I can or should change; flagging for awareness only.

## 14. Final Verdict

**C. ROOT CAUSE IDENTIFIED BUT FIX REQUIRES FURTHER WORK**

The root cause is now proven with hard, direct evidence (live HTTP response vs. disk-file mismatch, plus the exact client-side config field driving it) — not a generic explanation. A real, minimally-scoped, officially-documented fix was applied and verified to correctly resolve the defect for `allureReport`'s own artifact (the same artifact this project's already-written Phase C CI workflow uploads — that pipeline benefits from this fix even though it wasn't the direct target). But `allureServe` — the specific command you were using when you observed the symptom — is confirmed **still broken** by this change, and the full physical-device regression validation this phase called for could not be completed because the device was unavailable this session. This is not yet a closed, fully-verified fix; it's a genuine, partial, evidence-backed improvement with a clearly bounded remaining gap.

**No commit. No push. No GitHub Actions changes. No Docker changes. No test-logic changes.** Stopping here per instruction, awaiting your direction — specifically your own confirmation of whether the `file://`-opened `allureReport` artifact renders correctly for you, and whether the device can be reconnected to complete the outstanding regression validation.
