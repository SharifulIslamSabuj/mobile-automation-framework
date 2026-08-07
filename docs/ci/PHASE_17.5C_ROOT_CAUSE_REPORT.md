---
document_id: PHASE-17.5C
title: Third CI Failure Root Cause Analysis — Execution Model Discovery
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.5A, PHASE-17.5B, PHASE-17.4A, PHASE-17.4B]
classification: Internal
---

# Phase 17.5C — Third CI Failure Root Cause Analysis

| Field | Value |
|---|---|
| GitHub Actions Run | [31172844867](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31172844867) |
| Triggering Commit | `10c7dd8` — the Phase 17.5B fix |
| Job | `Build & Test (Android Emulator)` (ID `92848237857`) |
| Failed Step | `Android Emulator Provisioning, Build Validation & Test Execution` |
| Step Duration | 11:08:26Z → 11:09:58Z (1m32s) |

---

## Executive Summary

Further progress: the Phase 17.5B fix worked — the Appium server line executed without the "Unrecognized arguments" error, and the script reached the next line: `echo "Waiting for Appium server to become reachable..."`. It then failed on the `for i in $(seq 1 30); do` line with:

```
/usr/bin/sh: 1: Syntax error: end of file unexpected (expecting "done")
```

**This is not a third, isolated defect — it is direct, conclusive confirmation of the underlying mechanism that Phase 17.5A's fix worked around without fully explaining.** The `[command]` log entries for this action's `script:` execution show each physical line logged and executed as its own independent `/usr/bin/sh -c "<single line>"` invocation — not as one cohesive multi-line script file. A `for ... do` opened on one line, with its matching `done` on a later line, is by construction an incomplete program when that one line is handed to `sh -c` in isolation; `dash` correctly reports it as a syntax error.

This single finding retroactively explains Phase 17.5A's failure too: the `appium server ... \` line's trailing backslash was never a "continuation that this action doesn't honor" in the abstract — it was a stray trailing character in a genuinely standalone, single-line `sh -c` invocation, with nothing on the "next line" to join to, because there is no next line from that invocation's point of view.

**Root cause, now fully understood:** `reactivecircus/android-emulator-runner@v2.38.0` executes each line of its `script:` input as an independent shell invocation. Any multi-line shell construct — loops, conditionals spanning multiple lines, or backslash continuations — written under this workflow's `script:` block will fail, because no such construct is syntactically complete on a single line. This is a **workflow configuration bug**, specifically: the script was originally authored as an ordinary multi-line POSIX shell script, an assumption this action's actual execution model does not support.

---

## Failed Step / Command / Exit Code / Error

- **Step:** `Android Emulator Provisioning, Build Validation & Test Execution` (unchanged from prior two failures — the AVD/emulator/KVM/SDK provisioning phase again succeeded fully, and boot again completed in a comparable ~40.6s).
- **Command:** `for i in $(seq 1 30); do` (one physical line of the script, executed standalone).
- **Exit code:** `2`.
- **Error, quoted verbatim:**

```
2026-08-07T11:09:57.5226926Z [command]/usr/bin/sh -c for i in $(seq 1 30); do
2026-08-07T11:09:57.5267955Z /usr/bin/sh: 1: Syntax error: end of file unexpected (expecting "done")
2026-08-07T11:09:57.5369103Z ##[error]The process '/usr/bin/sh' failed with exit code 2
```

Preceded by confirmation the Phase 17.5B fix succeeded — the Appium server line ran cleanly this time, as its own standalone command, with no argument error:

```
2026-08-07T11:09:57.5130482Z [command]/usr/bin/sh -c appium server --log-level info --relaxed-security > "${RUNNER_TEMP}/appium-server.log" 2>&1 &
2026-08-07T11:09:57.5187467Z [command]/usr/bin/sh -c echo "Waiting for Appium server to become reachable at http://127.0.0.1:4723 ..."
2026-08-07T11:09:57.5197660Z Waiting for Appium server to become reachable at http://127.0.0.1:4723 ...
```

Emulator boot, again fully successful, immediately before this:

```
2026-08-07T11:09:56.8569205Z Emulator booted.
2026-08-07T11:09:57.0444509Z INFO         | Boot completed in 40646 ms
```

---

## Root Cause

Confirmed directly from the log's own `[command]` entries across all three runs to date: every distinct line of the `script:` YAML block is prefixed with its own `[command]/usr/bin/sh -c "<that one line>"` entry, and each is evidently executed as an independent shell process — not accumulated into one script file and executed once. A `for`/`do`/`done` block, split across three lines in the YAML source, is handed to `sh -c` one line at a time; the first line (`for i in $(seq 1 30); do`) is, by itself, a syntactically incomplete program, which `dash` rejects.

**Classification: Workflow configuration bug** — specifically, an incorrect assumption (carried since Phase 17.3's original implementation) that `reactivecircus/android-emulator-runner`'s `script:` input behaves like an ordinary shell script file. It does not. This is not an emulator, Appium, Gradle, framework, or AUT defect — the emulator boot succeeded a third time, and Appium's own binary was never even reached as a target of complaint this time; the failure is purely in how the *next* line of orchestration script was structured.

---

## Evidence

Full sequence from `set -eu` through the failure, quoted directly from `gh run view 31172844867 --log-failed`:

```
2026-08-07T11:09:57.4970579Z [command]/usr/bin/sh -c set -eu
2026-08-07T11:09:57.5043599Z [command]/usr/bin/sh -c echo "Starting Appium server (v${APPIUM_SERVER_VERSION})..."
2026-08-07T11:09:57.5099667Z Starting Appium server (v3.6.0)...
2026-08-07T11:09:57.5130482Z [command]/usr/bin/sh -c appium server --log-level info --relaxed-security > "${RUNNER_TEMP}/appium-server.log" 2>&1 &
2026-08-07T11:09:57.5187467Z [command]/usr/bin/sh -c echo "Waiting for Appium server to become reachable at http://127.0.0.1:4723 ..."
2026-08-07T11:09:57.5197660Z Waiting for Appium server to become reachable at http://127.0.0.1:4723 ...
2026-08-07T11:09:57.5226926Z [command]/usr/bin/sh -c for i in $(seq 1 30); do
2026-08-07T11:09:57.5267955Z /usr/bin/sh: 1: Syntax error: end of file unexpected (expecting "done")
2026-08-07T11:09:57.5369103Z ##[error]The process '/usr/bin/sh' failed with exit code 2
```

Note the pattern across all three runs to date is now unambiguous: **every successfully executed line was a single, complete, self-contained shell statement**; **every failure occurred on a line that was only a fragment of a larger construct** (a `set -o pipefail` flag dash rejects outright in run 1; a two-line command split by a continuation in run 2; a three-line `for` loop's opening line in run 3).

---

## Impact

Still shallow but now further than before: the emulator boots reliably (3/3 runs), Appium's binary starts and does not itself error (this run), but the reachability-wait loop — and, by the same now-confirmed mechanism, the remaining multi-line `./gradlew test --no-daemon \ ... ` invocation later in the same script — cannot execute as currently written.

**This finding is not confined to the one `for` loop.** The general mechanism (each script line runs as an independent `sh -c` invocation) is now confirmed by direct, repeated evidence across three separate runs, not inferred from a single occurrence. The `./gradlew test --no-daemon \` block, spanning five lines via backslash continuation, is structurally the same category of construct already proven broken twice (the appium line in run 2, and this `for` loop in run 3) — it is not a new, separately-guessed issue; it is the same confirmed defect class, present at a second known location in the same file.

---

## Confidence Level

**High.** The error is a `dash` syntax error naming exactly what it expected and did not receive (`"done"`), directly attributable to the one-line-at-a-time `[command]` log pattern visible across all three runs. The general execution-model conclusion is corroborated by three independent data points (a rejected flag, a rejected literal backslash argument, and now an incomplete-block syntax error), not a single anecdote.

---

## Proposed Fix

*(Described only — not implemented in this report.)*

Rewrite every multi-line shell construct in the `script:` block so that each logical command is fully self-contained on one physical line, using `;` to separate statements within what would otherwise be a multi-line block:

1. Collapse the `for ... do ... if ... fi ... if ... fi ... done` reachability-wait loop into one physical line.
2. Collapse the `./gradlew test --no-daemon \ -Denv=... -D... -D... -D...` invocation into one physical line — the same construct class just proven broken twice elsewhere in this file, at a location no run has reached yet only because the script has not gotten that far.

No other line in the script currently uses a multi-line construct (`set -eu`, the `echo` lines, and `./gradlew compileJava compileTestJava --no-daemon` are each already single, complete lines).

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Document Status | Final — Root Cause Report | — | — |

---

**End of Document — Phase 17.5C Root Cause Report, v1.0**
