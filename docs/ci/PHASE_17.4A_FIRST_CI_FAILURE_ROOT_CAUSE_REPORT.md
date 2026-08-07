---
document_id: PHASE-17.4A
title: First CI Failure Root Cause Analysis
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [MA-CICD-001, MA-CICD-002, PHASE-17.2A, PHASE-17.2B]
classification: Internal
---

# Phase 17.4A — First CI Failure Root Cause Analysis

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document Name | First GitHub Actions CI Failure — Root Cause Report |
| Version | v1.0 |
| Status | Final — Root Cause Report |
| GitHub Actions Run | [31167873866](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31167873866) |
| Triggering Commit | `b0b70c8` — "feat: implement GitHub Actions CI/CD for v1.1.0" |
| Run Result | `failure`, duration 2m46s (job duration 2m37s per job API) |
| Classification | Internal |

---

## Method Note

Every fact below was pulled directly from GitHub's own run data: `gh run view 31167873866`, `gh run view 31167873866 --log-failed`, `gh run view 31167873866 --log` (full log), and `gh api .../actions/jobs/92839290377` for per-step timestamps. No file was modified to produce this report. No fix is proposed as a change — only described, per this phase's instructions.

---

## 1. Executive Summary

The workflow failed at the **"Android Emulator Provisioning, Build Validation & Test Execution"** step. Every provisioning sub-stage inside that step succeeded — KVM enabled, Android SDK installed, AVD created, **the emulator booted successfully** ("Boot completed in 39660 ms", confirmed by `adb`). The failure happened immediately *after* the emulator was ready, at the very first line of the custom `script:` block this workflow supplies to the `reactivecircus/android-emulator-runner` action: `set -euo pipefail`.

**Root cause:** the `reactivecircus/android-emulator-runner` action executes its `script:` input via `/usr/bin/sh`, not `bash`. On this Ubuntu runner, `/bin/sh` is `dash`, which does not implement the `pipefail` option. `set -euo pipefail` is a bash-ism; under `dash` it is a syntax error the shell reports as "Illegal option," and `dash` exits with status 2 on an illegal option to `set`. This is a **workflow configuration bug** — a shell-portability assumption, not an environment, emulator, Appium, Gradle, or AUT problem. Appium was never started, Gradle was never invoked, and the AUT was never installed, because the script died on its first line, before any of those commands were reached.

---

## 2. Execution Timeline

Per-step timestamps from `gh api repos/.../actions/jobs/92839290377`:

| Step | Start (UTC) | End (UTC) | Duration | Result |
|---|---|---|---|---|
| Set up job | 10:24:37 | 10:24:40 | 3s | ✓ |
| Checkout Repository | 10:24:40 | 10:24:41 | ~1s | ✓ |
| Set Up JDK 17 | 10:24:41 | 10:24:41 | <1s | ✓ |
| Set Up Gradle | 10:24:41 | 10:24:43 | ~2s | ✓ ("Cache restored successfully") |
| Enable KVM Hardware Acceleration | 10:24:43 | 10:24:43 | <1s | ✓ |
| Install Appium Server | 10:24:43 | 10:25:13 | 30s | ✓ ("added 296 packages in 28s") |
| Install UiAutomator2 Driver | 10:25:13 | 10:25:20 | 7s | ✓ ("Driver uiautomator2@8.2.2 successfully installed") |
| Restore Cached AUT APK | 10:25:20 | 10:25:20 | <1s | ✓ (cache miss — first run, expected) |
| Download AUT APK | 10:25:20 | 10:25:20 | <1s | ✓ (17.1 MB at 35.7 MB/s) |
| Export APK Path | 10:25:20 | 10:25:20 | <1s | ✓ |
| **Android Emulator Provisioning, Build Validation & Test Execution** | 10:25:20 | 10:27:13 | **1m53s** | **✗ FAILED** |
| — sub-stage: Install Android SDK (licenses, build-tools, platform-tools, platform, emulator package, system image) | 10:25:20.96 | 10:26:22.43 | ~61.5s | ✓ |
| — sub-stage: Create AVD | 10:26:22.43 | 10:26:24.03 | ~1.6s | ✓ |
| — sub-stage: Launch Emulator (boot + disable-animations) | 10:26:24.04 | 10:27:11.45 | ~47.4s (boot itself: 39.66s per emulator's own log) | ✓ |
| — sub-stage: Run custom script | 10:27:11.46 | 10:27:11.47 | <1s | **✗ failed on line 1** |
| Verify Artifact Output | 10:27:13 | 10:27:13 | <1s | ✓ (ran; all 5 paths reported "not present") |
| Upload Artifacts | 10:27:13 | 10:27:13 | <1s | ✓ (ran; nothing to upload — 0 files matched) |
| Publish Workflow Summary | 10:27:13 | 10:27:13 | <1s | ✓ |
| Post-steps / Complete job | 10:27:13 | 10:27:14 | ~1s | ✓ |

**Total job duration:** 2m37s (job API) / 2m46s (run-level, includes dispatch overhead).

No retries occurred anywhere in this run. No timeout was hit — the failure occurred in under a second once the script began, nowhere near the 600-second `emulator-boot-timeout` or the 30-minute job-level `timeout-minutes`.

---

## 3. First Failed Step

**`Android Emulator Provisioning, Build Validation & Test Execution`** — the `reactivecircus/android-emulator-runner@v2.38.0` action step. This is both the first and only failed step in the run. Every step before it succeeded; every step after it (`Verify Artifact Output`, `Upload Artifacts`, `Publish Workflow Summary`) ran only because they are `if: always()` steps, and each reported empty/no-op results consistent with the failure happening before any test-related file could be produced.

---

## 4. Failed Command

```
/usr/bin/sh -c set -euo pipefail
```

This is the **first line** of the `script:` block supplied to `reactivecircus/android-emulator-runner` in `.github/workflows/mobile-automation.yml`. It is not `./gradlew compileJava compileTestJava` (never reached), not the Appium startup line (never reached), and not any adb/emulator command (all of those had already completed successfully in the action's own internal provisioning phase, which is separate from this custom script).

---

## 5. Complete Error Message

Quoted verbatim from the run log (`gh run view 31167873866 --log-failed`):

```
2026-08-07T10:27:11.4613184Z [command]/usr/bin/sh -c set -euo pipefail
2026-08-07T10:27:11.4615866Z /usr/bin/sh: 1: set: Illegal option -o pipefail
2026-08-07T10:27:11.4691146Z ##[error]The process '/usr/bin/sh' failed with exit code 2
```

Immediately followed by the action's own cleanup:

```
2026-08-07T10:27:11.4702329Z ##[group]Terminate Emulator
2026-08-07T10:27:11.4704318Z [command]/usr/local/lib/android/sdk/platform-tools/adb -s emulator-5554 emu kill
2026-08-07T10:27:11.4767467Z OK: killing emulator, bye bye
```

The generic Summary-page message the user already observed — `The process '/usr/bin/sh' failed with exit code 2` — is exactly this line, now traced to its real, underlying command.

---

## 6. Root Cause

`set -o pipefail` is not part of POSIX `sh`; it is a bash/ksh extension. `reactivecircus/android-emulator-runner` runs the user-supplied `script:` input through `/usr/bin/sh -c "<script>"` internally (confirmed directly in the log — see Section 7's quoted `[command]/usr/bin/sh -c set -euo pipefail` line). On this GitHub-hosted Ubuntu runner, `/bin/sh` is `dash`, which rejects `-o pipefail` as an "Illegal option" and exits with status 2.

This is distinct from — and easy to miss precisely because of — how this workflow's *own* `run:` steps behave: every native `run:` step in this same workflow (Checkout, Enable KVM, Install Appium Server, Install UiAutomator2 Driver, Download AUT APK, etc.) is confirmed in the log to execute via `shell: /usr/bin/bash -e {0}` — GitHub Actions' own default shell for `run:` steps on Linux runners *is* bash. The `script:` input of this third-party composite action does not follow that same default; it has its own, different execution contract. `set -euo pipefail` is valid, idiomatic bash and would have worked correctly in any of this workflow's native `run:` steps — it only fails inside this one action's `script:` input.

**Classification: Workflow configuration bug** (a shell-portability assumption written into the `script:` block), not:
- **Not** an Android Emulator problem — the emulator booted successfully in 39.66s and was confirmed ready by `adb` before the script ever ran.
- **Not** an Appium problem — Appium was never invoked; the script died on line 1, before the `appium server ...` line.
- **Not** a Gradle/Framework problem — `./gradlew` was never invoked.
- **Not** an AUT problem — the APK was downloaded successfully (17.1 MB, verified) but never installed, because installation only happens via Appium, which never started.
- **Not** a GitHub runner limitation — the runner, SDK, KVM, and network all performed correctly throughout.

---

## 7. Evidence

Emulator booted successfully, before the script ran:

```
2026-08-07T10:27:03.7422505Z INFO         | Boot completed in 39660 ms
2026-08-07T10:27:04.6217644Z 1
2026-08-07T10:27:04.6252121Z Emulator booted.
```

Animations disabled successfully (part of the action's own post-boot setup, still before the custom script):

```
2026-08-07T10:27:10.5775800Z [command]/usr/local/lib/android/sdk/platform-tools/adb -s emulator-5554 shell settings put global window_animation_scale 0.0
2026-08-07T10:27:11.2523751Z [command]/usr/local/lib/android/sdk/platform-tools/adb -s emulator-5554 shell settings put global transition_animation_scale 0.0
2026-08-07T10:27:11.3704617Z [command]/usr/local/lib/android/sdk/platform-tools/adb -s emulator-5554 shell settings put global animator_duration_scale 0.0
2026-08-07T10:27:11.4545329Z ##[endgroup]
```

The provisioning group ends, and the custom script begins and immediately fails:

```
2026-08-07T10:27:11.4613184Z [command]/usr/bin/sh -c set -euo pipefail
2026-08-07T10:27:11.4615866Z /usr/bin/sh: 1: set: Illegal option -o pipefail
2026-08-07T10:27:11.4691146Z ##[error]The process '/usr/bin/sh' failed with exit code 2
```

Every native `run:` step in this same workflow uses bash, confirmed directly in the log (example from "Install Appium Server"):

```
2026-08-07T10:24:43.9123536Z ##[group]Run npm install --global "appium@3.6.0"
2026-08-07T10:24:43.9171914Z shell: /usr/bin/bash -e {0}
```

Consequence — every artifact path was empty, confirming nothing downstream of the script's first line ever ran:

```
2026-08-07T10:27:13.3273554Z reports/:
2026-08-07T10:27:13.3288646Z   not present
2026-08-07T10:27:13.3289008Z reports/screenshots/:
2026-08-07T10:27:13.3303098Z   not present
2026-08-07T10:27:13.3303428Z logs/:
2026-08-07T10:27:13.3317522Z   not present
2026-08-07T10:27:13.3317941Z build/reports/tests/test/:
2026-08-07T10:27:13.3331331Z   not present
2026-08-07T10:27:13.3331714Z build/test-results/test/:
2026-08-07T10:27:13.3345701Z   not present
```

And the resulting artifact-upload annotation (a downstream *consequence*, per this phase's own instruction not to treat it as the root cause):

```
! No files were found with the provided path: reports/
logs/
build/reports/tests/test/
build/test-results/test/. No artifacts will be uploaded.
```

---

## 8. Impact Assessment

Total blast radius: **100% of Objectives 6/7 of Phase 17.4 are blocked** — no test executed, so there is nothing to compare against the real-device baseline yet:

| Verification (Phase 17.4 Objective 6) | Result |
|---|---|
| Emulator booted successfully | **Yes** — confirmed, 39.66s |
| Appium server started successfully | **No** — never reached |
| APK installed successfully | **No** — never attempted |
| Gradle build passed | **No** — never invoked |
| All `-D` overrides reached `ConfigReader` | **Not testable** — Gradle never ran |
| All expected artifacts generated | **No** — all five Required paths empty |

| Comparison vs. real-device baseline (Phase 17.4 Objective 7) | Result |
|---|---|
| Tests executed (CI) vs. 19 (real-device baseline, MA-TC-001) | 0 vs. 19 |
| Tests passed | 0 vs. baseline's documented pass count |
| Tests failed | 0 (none ran) |
| Runtime | N/A — suite never started |
| Screenshots | None generated |
| Reports | None generated |

The failure is total but shallow: it occurs before any test-relevant code runs, so it says nothing about whether the framework, Appium integration, or system-property forwarding actually work under CI — those remain completely unverified, not disproven.

---

## 9. Recommended Fix

*(Described only, per this phase's instructions — not implemented.)*

Remove `-o pipefail` from the `set` invocation at the top of the `script:` block, leaving `set -eu` (both POSIX-standard, both supported by `dash`). This preserves the intended "exit immediately on any command failure, and on any unset variable reference" behavior, which is what actually gates Build Validation before Test Execution (MA-CICD-002 §8) — `pipefail` specifically governs failure propagation through `cmd1 | cmd2` pipelines, and the script contains no such pipeline whose non-final command's failure would need to be caught. Dropping it is not a loss of the intended fail-fast guarantee for this specific script.

This is a one-line change confined entirely to the `script:` string inside the existing workflow step — it requires no change to the job/step structure, no change to any of the four frozen architecture/decision documents, and no change to `build.gradle` or any config file.

---

## 10. Confidence Level

**High.**

The failing command, its exact exit code, and its exact error text are all quoted directly from GitHub's own log output, not inferred. The mechanism (`dash` rejecting `set -o pipefail`) is a well-documented, unambiguous POSIX-shell behavior, further corroborated within this same log by the direct side-by-side contrast between this action's `/usr/bin/sh` execution and every other step's confirmed `/usr/bin/bash -e` execution. No part of this conclusion depends on an assumption about the emulator, Appium, Gradle, or the AUT — each of those was independently confirmed either working correctly (emulator boot) or simply never reached (everything after line 1 of the script).

---

## Potential Secondary Issues (Not Yet Investigated)

Listed for future attention only — none of these were investigated further, and none should be fixed in this phase:

- **`Set Up Gradle` reported "Cache restored successfully" on what should be this repository's first-ever cache write.** Not yet investigated whether this is a benign base/shared cache layer unrelated to this project's own dependencies, or something worth understanding before trusting cache behavior on later runs.
- **The repeated `adb: device offline` / `Unable to connect to adb daemon on port: 5037` lines during early emulator boot** are, on this evidence, normal transient states during a cold boot (the daemon starts fresh, and the emulator was not yet reachable) — verified here as *not* the failure, but not otherwise characterized or benchmarked against what a "normal" boot log should look like on this specific runner/image combination.
- **None of Phase 17.2B's six decisions (APK source, Appium server pin, emulator spec, artifact paths, CI config strategy, known constraints) were exercised far enough by this run to be verified or refuted** — the failure occurred before Appium started, before Gradle ran, and before any `-D` override was consumed. All of that verification work remains outstanding for the next run.

---

## Final Verdict

# FAIL — remediation required before continuing

The first CI execution did not reach test execution. One workflow-configuration defect (Section 9) must be corrected and a new run observed before Phase 17.4's remaining verification objectives (emulator/Appium/Gradle/artifact/baseline-comparison) can be attempted.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Final — Root Cause Report | — | — |

---

**End of Document — Phase 17.4A First CI Failure Root Cause Report, v1.0**
