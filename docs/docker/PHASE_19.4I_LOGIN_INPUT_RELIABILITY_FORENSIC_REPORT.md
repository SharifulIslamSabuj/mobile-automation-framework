---
document_id: PHASE-19.4I
title: Login Input Reliability Forensic Investigation
version: v1.0
status: Final — Forensic Report (Target Failure Not Reproduced, No Fix Implemented)
author: Project Owner / Repository Maintainer
created_date: 2026-08-10
last_updated: 2026-08-10
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4B, PHASE-19.4C, PHASE-19.4D, PHASE-19.4E, PHASE-19.4F, PHASE-19.4G, PHASE-19.4H]
classification: Internal
---

# Phase 19.4I — Login Input Reliability Forensic Investigation

---

## 1. Objective

Determine why a `LoginTest` text-entry action can apparently complete while the AUT subsequently behaves as if the username field is empty (the "Username is required" validation failure incidentally discovered in Phase 19.4H, Run 1) — identify the exact point at which the intended input diverges from expected state, without implementing a fix.

---

## 2. Context

Phase 19.4H's Run 1 CI execution (`31324166317`) failed with the AUT's own Login screen showing "Username is required" and both the username and password fields empty, despite `automation.log` reporting that the framework's `clear`/`type` actions on both fields completed successfully before submission. This was flagged as a distinct, previously-unrecognized intermittent condition, unrelated to the AUT-visibility/launcher race that Phases 19.4A–H investigated, and set aside as its own follow-up (`task_d86a8820`).

---

## 3. Distinction from AUT Visibility Failure

This phase investigates **only** login input reliability. It does not touch, re-litigate, or draw unstated conclusions about the Phase 19.4B readiness fix, the Phase 19.4C verification failure, the Phase 19.4F/G/H AUT-visibility/launcher investigation, or `AndroidDriverFactory.java` (confirmed untouched throughout, Section 24). No evidence in this phase connects the two issues — none was found, and none is asserted.

---

## 4. Investigation Scope

In scope: temporary, evidence-only instrumentation in `LoginPage.java` (the only file governing the `login()` action under investigation) capturing the username field's actual value at four checkpoints (T0–T3), plus reuse of Phase 19.4H's independent host-side ADB observer for cross-layer corroboration (window/activity/IME/process state, continuous logcat) — all removed after the investigation.

Out of scope and untouched: Docker architecture, `Dockerfile`, `.dockerignore`, production `mobile-automation.yml`, Gradle configuration, test data, permanent Page Object or `LoginTest` changes, any fix.

---

## 5. Reproduction Strategy

Up to 5 designated sequential CI executions of the representative `LoginTest`, stopping immediately if the "Username is required" failure reproduced with sufficient evidence, or after 5 runs otherwise — per the phase's own bounded-sample instruction.

---

## 6. Instrumentation Design

Temporary changes to `LoginPage.login()` (`src/test/java/com/mobileautomation/framework/pages/LoginPage.java`), bounded by clear `// ===== PHASE 19.4I ... =====` comments:

- **T0** (immediately before input): `elementActions.getText(USERNAME_FIELD)`, logged via the class's own existing SLF4J `logger` (already used by every other framework class — no new logging mechanism), so checkpoint lines appear directly in `logs/automation.log` with the same timestamp format and precision as every other framework log line, with zero extra correlation work needed.
- **T1** (immediately after `enterUsername()`, i.e., right after the `type` command returns): same `getText()` call, compared against the expected value.
- **T2** (immediately before `tapLogin()`, i.e., right after `enterPassword()` returns): same `getText()` call again, to catch any late clearing between T1 and submission.
- **T3** (immediately after `tapLogin()`): a **non-waiting** check — `driver().findElements(USERNAME_FIELD)` (plural, returns immediately, empty list if absent) rather than `elementActions.getText()` — deliberately, because after a successful login the screen navigates away and the field no longer exists; using the normal waiting `getText()` here would have added the full ~15-second explicit-wait timeout to every single passing run.

All four checkpoints run on the **main test thread**, synchronously, using the framework's own existing, already-proven methods (`getText()`, `driver().findElements()`) — no background thread, no shared-session polling, explicitly not reintroducing Phase 19.4G's contention pattern.

---

## 7. Interference Controls

Test durations across the 5 runs: 31.505s (Run 1, which also failed for an unrelated reason), 28.031s, 28.187s, 30.819s, 29.616s (Runs 2–5). Average of all 5: **29.63s**, closely matching Phase 19.4F's healthy baseline (~30.0s) and Phase 19.4H's own validated low-interference figure (~29.4s). **VERIFIED**: this instrumentation caused no measurable timing distortion — each checkpoint is a single, already-proven-fast Appium command (`getText()`/`findElements()`), not a new observation mechanism.

---

## 8. Input Command Analysis

From `automation.log` (Run 1, representative): `clear` on `nameET` at 03:52:54.333, `type` on `nameET` at 03:52:54.974 (641ms later), both logged by `ElementActions.execute()` as "Performed" (i.e., no exception was thrown — Appium reported success for both). The command durations are unremarkable and consistent across all 5 runs; no Appium-side error, retry, or recovery was observed in any run's log for these specific commands.

---

## 9. Field State Before Input (T0)

In every one of the 5 runs, T0 showed the username field as empty (`value=""`) — the expected starting state for a freshly-displayed Login screen. **VERIFIED**, 5/5.

---

## 10. Field State After Input (T1)

In every one of the 5 runs, T1 showed `actual="bod@example.com"` exactly matching the intended value (`match=true`). **VERIFIED**, 5/5 — the `type` command's effect was correctly present in the field immediately after issuing it, in every run captured this phase.

---

## 11. Field State Before Submission (T2)

In every one of the 5 runs, T2 (captured after the password field was also cleared and typed, immediately before the login button tap) again showed `actual="bod@example.com"` matching (`match=true`) — the username value was still correctly retained through the password-entry step in every run. **VERIFIED**, 5/5.

---

## 12. Field State After Submission (T3)

In every one of the 5 runs, T3 showed `NOT_PRESENT (screen likely transitioned)` — the username field no longer existed, consistent with the app having navigated away from the Login screen following submission. This is true even in Run 1, the one run that ultimately failed (Section 18) — the failure in that run occurred **after** a clean, successful login transition, not because of it. **VERIFIED**, 5/5.

---

## 13. Appium Evidence

Every `getText()`/`findElements()` diagnostic call across all 5 runs completed without exception (no `ERROR(...)` value was ever logged by the diagnostic's own error-handling path). Every underlying `clear`/`type`/`click` command in the actual login sequence was logged as "Performed" by `ElementActions.execute()`, meaning Appium's own layer reported success on every attempt, in every run, consistent with the field-value evidence in Sections 9–12: **VERIFIED**, this phase found no instance where Appium reported success while the field-level evidence showed otherwise.

---

## 14. UiAutomator Evidence

Not separately captured via a full page-source dump in this phase — the T0–T3 checkpoints already query the specific `WebElement`'s own `getText()`, which is itself resolved through the same UiAutomator2 accessibility layer; a redundant full-hierarchy dump was judged unnecessary overhead per the phase's own "use the lightest observation method" guidance, and no run's evidence created a need to escalate to one.

---

## 15. Host-Side ADB Evidence

Phase 19.4H's observer architecture was reused, extended with `dumpsys input_method` (`mCurFocusedWindow`, `mInputShown`). Representative data (Run 2, during the T0–T2 window, 04:20:06–04:20:11): `mFocusedApp`/`topResumedActivity` consistently show the AUT's `MainActivity`; `mInputShown=false` throughout (the on-screen soft keyboard was not shown during this interaction — this AUT/emulator combination accepts `sendKeys` without requiring a visible IME, consistent with prior phases' screenshots never showing a keyboard); `softInputMode` transitioned between `ADJUST_RESIZE` and `ADJUST_PAN` between samples, a normal, non-error window-attribute change. No anomaly was observed in any run's host-side window/activity/IME state during the input sequence.

---

## 16. Logcat Analysis

Logcat capture succeeded in every run (continuing Phase 19.4H's 100% success rate). During the input window, logcat shows routine `GoogleInputMethodService`/`InputMethodEntryManager` activity (the on-device Google keyboard service handling `onStartInput`/`onFinishInput` for the focused field) — normal IME lifecycle noise, no crash, no ANR, no unusual `EditText`/`WindowManager` event correlated with the input sequence in any of the 5 runs. **VERIFIED**: no crash/ANR/anomalous window event occurred during any captured input window.

---

## 17. Passing Run Evidence

Runs 2, 3, 4, and 5 all passed outright, with T0–T3 all showing correct, expected values in every case (Sections 9–12). These are clean, complete "healthy path" input-reliability evidence.

---

## 18. Failing Run Evidence

Run 1 failed, but **not** with the target symptom. `automation.log` shows: T0=`""`, T1=`"bod@example.com"` (match), T2=`"bod@example.com"` (match), T3=`NOT_PRESENT` (transitioned) — the login flow itself completed cleanly and the app returned to the Product Catalog screen (`"Assertion passed: verifyVisible [Product Catalog screen (post-login destination...)]"` at 03:52:59.543). The test failed **13.6 seconds later**, at a separate assertion (`LoginTest.java:73`): after reopening the Navigation Drawer, `isDrawerItemDisplayed("Log Out")` polled for the full ~15.47s explicit-wait window and never returned `true`. This is a third, distinct failure mode — unrelated to text-entry reliability (the input itself is proven correct in this same run) and unrelated to the AUT-visibility race (the AUT was never absent; the Product Catalog screen was confirmed visible). It has been flagged separately (`task_9dd1344d`) and is not further analyzed here, per this phase's explicit scope boundary (Section 3).

---

## 19. Passing vs Failing Comparison

| Checkpoint | Runs 2–5 (passing) | Run 1 (failing, different assertion) |
|---|---|---|
| T0 | field empty | field empty |
| T1 | `"bod@example.com"`, match | `"bod@example.com"`, match |
| T2 | `"bod@example.com"`, match | `"bod@example.com"`, match |
| T3 | field not present (transitioned) | field not present (transitioned) |
| Login submission | succeeds, Product Catalog visible | succeeds, Product Catalog visible |
| Ultimate outcome | Test passes | Test fails later — "Log Out" drawer item never appears |

There is **no divergence at any of the four input-reliability checkpoints** between the passing runs and Run 1 — input behavior is identical across all 5 runs. The only observed divergence in this sample occurs well after the scope of this investigation (the drawer-state check, Section 18).

---

## 20. First Observable Input Divergence

**None observed.** In every one of the 5 designated runs, the username field's value at T0/T1/T2/T3 matched the healthy, expected pattern exactly. No run in this bounded sample produced a divergence between intended and actual field content at any checkpoint.

---

## 21. Root Cause Assessment

**Not reached — the target failure did not reproduce.** None of the candidate mechanisms (A–K) can be confirmed or excluded against direct evidence, because no run exhibited the symptom under investigation. **NOT VERIFIED** as to whether the Phase 19.4H occurrence was a genuine, currently-live intermittent condition versus an artifact specific to that one run's circumstances (e.g., the buggy `dumpsys window windows` command active in that same run, though there is no evidence connecting that unrelated host-observer bug to the AUT-side text-entry symptom — this is noted only as an acknowledged coincidence, not a claimed cause).

---

## 22. What Is Ruled Out

For the 5 runs actually captured in this phase: a text-entry failure that is easily and frequently reproducible under this exact Docker Model 3 / Appium 3.6.0 / UiAutomator2 8.2.2 configuration is **ruled out** (**VERIFIED** — 5/5 clean). Candidate mechanisms requiring a *persistent* or *high-frequency* defect (e.g., a systematic UiAutomator2 `sendKeys` bug on this AUT/emulator combination) are **not supported** by this sample. Nothing else is ruled out — a genuinely rare, low-frequency condition (as Phase 19.4H's single occurrence suggests) is fully consistent with a 0-in-5 result in a fresh sample.

---

## 23. Remaining Unknowns

- Whether the Phase 19.4H occurrence will recur at some low, currently unquantified frequency — this phase's sample is too small to estimate a rate, and per Phase 19.4F's own binomial reasoning, a low-frequency intermittent condition can easily produce zero occurrences in 5 fresh trials.
- The mechanism, if it does recur — none of the candidate hypotheses (A–K) were tested against a live occurrence in this phase.
- Whether the Phase 19.4H occurrence's un-corrected `dumpsys window windows` observer bug in that same run had any relationship to the AUT-side symptom — not evidenced either way, and not assumed.
- The separately-discovered "Log Out" drawer-item issue (Section 18) — explicitly out of scope here, tracked as `task_9dd1344d`.

---

## 24. Recommended Next Step

Not a fix (target not reproduced). Two options, in order of cost: (1) treat the Phase 19.4H occurrence as a low-frequency, currently-unreproduced anomaly and monitor for recurrence via normal CI operation, now that this phase's T0–T3 checkpoint pattern is a proven, low-interference template that could be re-applied quickly if it recurs; or (2) run a substantially larger bounded sample (e.g., 20–30 runs, following the same low-interference design validated here and in Phase 19.4H) if establishing a frequency estimate is worth the CI cost — this phase does not recommend which, deferring that cost/value judgment to the user.

**Cleanup verification**: `git checkout f0728ed -- src/test/java/.../LoginPage.java` applied; diff against that baseline is empty (**VERIFIED**). `./gradlew compileJava compileTestJava --no-daemon` after revert: `BUILD SUCCESSFUL in 44s` (**VERIFIED**). `.github/workflows/phase-19-4i-input-diag.yml` removed via `git rm`; `.github/workflows/` now contains only `mobile-automation.yml` (**VERIFIED**). `git diff e4d8175` for `AndroidDriverFactory.java`/`Dockerfile`/`.dockerignore`/`mobile-automation.yml` is empty — none were touched this phase (**VERIFIED**). Cleanup committed as `aa8982f` and pushed to `origin/main` (**VERIFIED**). `git status` shows a clean tree except the pre-existing, deliberately-untracked `docs/docker/` directory (**VERIFIED**).

---

## 25. Final Verdict

# INPUT FAILURE NOT REPRODUCED WITHIN BOUNDED SAMPLE

Across 5 designated sequential CI runs, the username field's actual value matched the expected, healthy pattern at every one of the T0–T3 checkpoints in every run — no divergence between intended and actual input state was observed. The "Username is required" failure first seen in Phase 19.4H did not recur. This result is not evidence the underlying condition is fixed or resolved — no code change was made, and a low-frequency intermittent condition (as the single Phase 19.4H occurrence suggests) can readily produce zero occurrences across 5 fresh trials by chance alone, consistent with this engagement's established statistical reasoning (Phase 19.4F, Section 20). This phase's bounded sample did, however, produce a complete, clean input-reliability baseline across multiple layers (framework, Appium field-value, host-side window/activity/IME, logcat) with negligible instrumentation interference, and incidentally surfaced a third, separately-tracked LoginTest failure mode (the "Log Out" drawer item not appearing post-login, `task_9dd1344d`) unrelated to this investigation's scope. No fix was implemented; `LoginPage.java`, `AndroidDriverFactory.java`, and all production files are unchanged.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-10 |
| Document Status | Final — Forensic Report (Target Failure Not Reproduced, No Fix Implemented) | — | — |

---

**End of Document — Phase 19.4I Login Input Reliability Forensic Report, v1.0**
