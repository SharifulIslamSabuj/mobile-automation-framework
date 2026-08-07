---
document_id: PHASE-17.4B
title: First CI Failure Remediation — Implementation Report
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.4A, MA-CICD-001, MA-CICD-002]
classification: Internal
---

# Phase 17.4B — First CI Failure Remediation — Implementation Report

**Mobile Automation Framework**

| Field | Value |
|---|---|
| Document Name | First CI Failure Remediation — Implementation Report |
| Version | v1.0 |
| Status | Final — Implementation Report |
| Governing Document | [PHASE_17.4A_FIRST_CI_FAILURE_ROOT_CAUSE_REPORT.md](PHASE_17.4A_FIRST_CI_FAILURE_ROOT_CAUSE_REPORT.md) |
| Classification | Internal |

---

## 1. Executive Summary

Applied exactly the fix Phase 17.4A's Section 9 described and no other change: `set -euo pipefail` → `set -eu` in the `script:` block of the `Android Emulator Provisioning, Build Validation & Test Execution` step in `.github/workflows/mobile-automation.yml`. This removes the bash-only `pipefail` option that caused `dash` (the `/bin/sh` this action's `script:` input actually executes under) to fail immediately with `Illegal option -o pipefail`, exit code 2. Fail-fast semantics are preserved via `-eu`, which is POSIX-standard and `dash`-compatible.

## 2. File Modified

| File | Change |
|---|---|
| [`.github/workflows/mobile-automation.yml`](../../.github/workflows/mobile-automation.yml) | One line changed (`set -euo pipefail` → `set -eu`), plus a 5-line explanatory comment added directly above it |

No other file was touched — `build.gradle`, `README.md`, Java source, and every config file remain exactly as they were.

## 3. Exact Change

```diff
           script: |
-            set -euo pipefail
+            # reactivecircus/android-emulator-runner executes this script via /bin/sh (dash on
+            # this runner), not bash — `pipefail` is a bash/ksh extension dash does not support
+            # (confirmed: Phase 17.4A root cause report, run 31167873866). `-eu` is POSIX and
+            # dash-compatible, and is sufficient here since this script contains no `cmd1 | cmd2`
+            # pipeline whose non-final command's failure would need `pipefail` to be caught.
+            set -eu
 
            echo "Starting Appium server (v${APPIUM_SERVER_VERSION})..."
```

## 4. Why This Fixes the Verified Root Cause

Phase 17.4A traced the failure to one exact log line: `/usr/bin/sh: 1: set: Illegal option -o pipefail`, produced because `reactivecircus/android-emulator-runner` invokes the `script:` input via `/usr/bin/sh -c "<script>"`, and this runner's `/bin/sh` is `dash` — which does not implement `-o pipefail` (a bash/ksh extension, not POSIX). `-e` (exit on any command failure) and `-u` (exit on any unset variable reference) are both POSIX-standard options `dash` fully supports. Removing only `-o pipefail` removes the one component of the original `set` invocation that `dash` cannot parse, while keeping the two components that gate this script's actual fail-fast requirement: a failing `curl`, a failing `./gradlew compileJava compileTestJava`, or a failing `./gradlew test` will still immediately stop the script and fail the step, exactly as MA-CICD-002 §8 and §12 require.

`pipefail` specifically changes how a `cmd1 | cmd2` pipeline's exit status is determined (using the last *failing* command in the pipe, not just the last command). This script contains no such pipeline — every command in it is a plain invocation or a `>`/`>>` redirection, never a `|` pipe — so `pipefail`'s absence changes no actual failure-detection behavior for this specific script. The fix is behaviorally complete, not a partial workaround.

## 5. What Intentionally Remains Unchanged

Per the phase's explicit rules, and verified by inspection of the diff (Section 3) and full file:

- **No other file modified** — `build.gradle`, `README.md`, Java source, and all config files (`config.properties`, `config-emulator.properties`, etc.) are untouched.
- **No trigger changed** — `push`/`pull_request`/`workflow_dispatch` on `main`, unchanged.
- **No command reordered** — Appium startup still follows the `set` line; Build Validation still precedes Test Execution; artifact collection still follows test execution. Only the `set` line's content changed.
- **No artifact configuration changed** — the five Required paths and the `Upload Artifacts`/`Verify Artifact Output` steps are byte-for-byte unchanged.
- **No Appium configuration changed** — server version (`3.6.0`), driver version (`8.2.2`), install commands, reachability-wait loop: all unchanged.
- **No emulator configuration changed** — API level 34, `google_apis`, `x86_64`, `pixel` profile, `emulator-boot-timeout: 600`, `disable-animations: true`: all unchanged.
- **No secondary issue addressed** — the three items Phase 17.4A listed under "Potential Secondary Issues (Not Yet Investigated)" (the Gradle cache-restore observation, the transient `adb: device offline` boot messages, and the still-unverified Phase 17.2B decisions) were left exactly as-is, per this phase's explicit instruction not to fix them here.

## 6. Validation Performed

- `npx js-yaml .github/workflows/mobile-automation.yml` — parses successfully (exit 0); the file remains syntactically valid YAML.
- `git diff .github/workflows/mobile-automation.yml` — confirms the change set is exactly the one line plus its explanatory comment; nothing else in the 200-line file differs from the version that produced run `31167873866`.
- `git status --porcelain` — confirms no file outside this phase's authorized scope (`.github/workflows/mobile-automation.yml`, plus this report and Phase 17.4A's report under `docs/ci/`) was touched.

No local Gradle or emulator execution was performed as part of this validation — this fix is specific to shell syntax parsed by `dash` inside a GitHub-hosted runner, which cannot be reproduced or validated from this local environment. Confirming it requires a real GitHub Actions run (Section 7).

## 7. Expected Outcome for the Next GitHub Actions Run

The `Android Emulator Provisioning, Build Validation & Test Execution` step should now progress past the point of failure: `set -eu` will execute without error under `dash`, and the script should proceed to actually start the Appium server, wait for its reachability, run `./gradlew compileJava compileTestJava`, and then `./gradlew test` — none of which were ever reached in run `31167873866`. This is the first point at which Phase 17.2B's six frozen decisions (APK install via `app.path`, Appium server/driver versions, emulator target, `config-emulator.properties` reuse with system-property overrides, artifact paths, sequential/no-retry execution) will actually be exercised for the first time.

This fix does not guarantee the run will pass end-to-end — only that it will progress beyond this specific, now-corrected failure point. Any further failure is a new, distinct finding requiring its own root-cause analysis, not an extension of this one.

## 8. Remaining Unverified Items Carried Forward from Phase 17.4A

All of the following were listed as unverifiable in Phase 17.4A precisely because the run never got far enough to test them, and remain unverified after this fix (they can only be confirmed by a new run):

- Whether the Appium server actually starts and becomes reachable at `http://127.0.0.1:4723` within the script's 60-second wait loop.
- Whether `./gradlew compileJava compileTestJava` succeeds under the CI environment.
- Whether the `-Dplatform.version`, `-Ddevice.name`, and `-Dapp.path` system properties supplied to `./gradlew test` actually reach `ConfigReader` at runtime — the very question the `build.gradle` `systemProperties(System.properties)` forwarding (added in Phase 17.3) was implemented to answer, still unobserved in a real run.
- Whether the AUT installs correctly onto the emulator via Appium's `app` capability.
- Whether any, all, or none of the 19 automated test cases pass under emulator execution, and how that compares to the real-device baseline in MA-TC-001.
- Whether the five Required artifact paths are populated and successfully uploaded.
- The three secondary observations from Phase 17.4A §"Potential Secondary Issues" (Gradle cache-restore behavior on a fresh cache, transient `adb: device offline` boot messages, and general first-run characterization) remain unexamined.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Reviewed By | Pending | Pending | — |
| Approved By | Pending | Pending | — |
| Document Status | Final — Implementation Report | — | — |

---

**End of Document — Phase 17.4B Implementation Report, v1.0**
