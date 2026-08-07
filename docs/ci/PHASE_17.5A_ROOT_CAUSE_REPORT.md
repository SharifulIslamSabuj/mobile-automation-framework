---
document_id: PHASE-17.5A
title: Second CI Failure Root Cause Analysis
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.4A, PHASE-17.4B, MA-CICD-001, MA-CICD-002]
classification: Internal
---

# Phase 17.5A — Second CI Failure Root Cause Analysis

| Field | Value |
|---|---|
| GitHub Actions Run | [31171896995](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31171896995) |
| Triggering Commit | `78ce988` — "fix: support POSIX shell in GitHub Actions emulator workflow" (the Phase 17.4B fix) |
| Job | `Build & Test (Android Emulator)` (ID `92845320720`) |
| Failed Step | `Android Emulator Provisioning, Build Validation & Test Execution` |
| Step Duration | 10:54:02Z → 10:55:36Z (1m34s) |
| Run Result | `failure`, 1m58s total |

---

## Executive Summary

Progress confirmed: the Phase 17.4B fix worked — `set -eu` executed without error, and the KVM/SDK/AVD/emulator-boot sequence all succeeded again (`Boot completed in 40451 ms`, `Emulator booted.`). The script then reached and printed `Starting Appium server (v3.6.0)...` — the first line of the script to ever execute in this pipeline. It failed on the **very next command**: the `appium server ...` invocation itself.

**Root cause:** the multi-line `appium server --log-level info --relaxed-security \` command relies on a trailing backslash-newline to continue onto the next line (`> "${RUNNER_TEMP}/appium-server.log" 2>&1 &`). In this action's script-execution context, that continuation is not honored the way it would be in an ordinary shell script file — the trailing `\` is passed through to the `appium` binary as a literal argument, and Appium's own CLI parser rejects it: `[ERROR] Unrecognized arguments: \`, exiting 1.

This is a **workflow configuration bug** (a second, distinct shell-portability defect, not a recurrence of the first) — not an Appium installation problem (the driver installed cleanly in an earlier step), not an emulator problem (boot succeeded again), not a Gradle/framework/AUT problem (neither was reached).

---

## Failed Step

`Android Emulator Provisioning, Build Validation & Test Execution` — same step as the first failure, different underlying command.

## Exact Command

```
appium server --log-level info --relaxed-security \
```
(the first physical line of the intended two-line command)

## Exit Code

`1`

## Complete Error

Quoted verbatim:

```
2026-08-07T10:55:34.4386272Z [command]/usr/bin/sh -c appium server --log-level info --relaxed-security \
2026-08-07T10:55:35.3884681Z [ERROR] Unrecognized arguments: \
2026-08-07T10:55:35.3981864Z ##[error]The process '/usr/bin/sh' failed with exit code 1
```

Preceded by confirmation the script did start correctly this time:

```
2026-08-07T10:55:34.4269373Z [command]/usr/bin/sh -c set -eu
2026-08-07T10:55:34.4333295Z [command]/usr/bin/sh -c echo "Starting Appium server (v${APPIUM_SERVER_VERSION})..."
2026-08-07T10:55:34.4363324Z Starting Appium server (v3.6.0)...
```

And the emulator boot, again successful, immediately before it:

```
2026-08-07T10:55:33.3401543Z INFO         | Boot completed in 40451 ms
2026-08-07T10:55:33.7668111Z Emulator booted.
```

---

## Root Cause

The log's own `[command]` entries reveal that each logical line of the `script:` block is logged (and, based on the resulting error, effectively executed) as its own unit — `[command]/usr/bin/sh -c set -eu`, then `[command]/usr/bin/sh -c echo "..."`, then `[command]/usr/bin/sh -c appium server --log-level info --relaxed-security \`. The trailing backslash on that last line — intended as a line-continuation joining it to `> "${RUNNER_TEMP}/appium-server.log" 2>&1 &` on the next physical line — is not consumed as a continuation in this execution context. Appium's own CLI (a Node.js/yargs-based parser) receives a literal `\` character as a fourth argument and rejects it outright: `[ERROR] Unrecognized arguments: \`.

This is evidenced directly by Appium's own error message, not inferred — the CLI is telling us exactly what it received. The fix does not require understanding the action's internal execution mechanism in full; it only requires not depending on a backslash line-continuation for this command, which is confirmed unsafe by this evidence.

**Classification: Workflow configuration bug.**
- **Not** an Appium installation/version problem — Appium 3.6.0 and the UiAutomator2 8.2.2 driver both installed cleanly in earlier steps (Phase 17.4B run and this run).
- **Not** an emulator problem — boot succeeded a second time, in a comparable ~40s.
- **Not** a Gradle/Framework/AUT problem — none of those were reached; the script died before Appium even started listening.
- **Not** a recurrence of the Phase 17.4A failure — that was `dash` rejecting `-o pipefail` at the `set` line; this is a completely different command, a completely different error class, at a different line.

---

## Evidence

Full command sequence showing per-line execution and the exact failure point, quoted directly from `gh run view 31171896995 --log-failed`:

```
2026-08-07T10:55:34.4269373Z [command]/usr/bin/sh -c set -eu
2026-08-07T10:55:34.4333295Z [command]/usr/bin/sh -c echo "Starting Appium server (v${APPIUM_SERVER_VERSION})..."
2026-08-07T10:55:34.4363324Z Starting Appium server (v3.6.0)...
2026-08-07T10:55:34.4386272Z [command]/usr/bin/sh -c appium server --log-level info --relaxed-security \
2026-08-07T10:55:35.3843339Z 
2026-08-07T10:55:35.3884681Z [ERROR] Unrecognized arguments: \
2026-08-07T10:55:35.3981864Z ##[error]The process '/usr/bin/sh' failed with exit code 1
2026-08-07T10:55:35.4043526Z ##[group]Terminate Emulator
```

---

## Impact

Blast radius is again total but shallow, and shallower than the first failure — the script now progresses one line further than before, but still dies before Appium is reachable, before Gradle runs, and before any artifact can be produced. All of Phase 17.4A's carried-forward unverified items remain unverified.

**New observation for the record, not yet actioned:** the `./gradlew test --no-daemon \` invocation later in the same script uses the identical multi-line backslash-continuation pattern across five lines. On the evidence gathered here, that block is very likely to exhibit the same defect if reached. Per this phase's "one targeted fix at a time" / "never guess" discipline, this is **not** being fixed now — it is flagged here so the next run's evidence can confirm or refute it directly rather than being patched pre-emptively on assumption.

---

## Confidence Level

**High.** The error is Appium's own CLI reporting the exact malformed argument it received (`\`), directly correlated with the exact line where the workflow's script uses a backslash line-continuation. No inference about the emulator, Gradle, or the AUT is required to explain this failure.

---

## Proposed Fix

*(Described only — not implemented in this report.)*

Rewrite the `appium server` invocation as a single physical line within the YAML `script:` block — combining the command, its arguments, its output redirection, and the trailing `&` (backgrounding) onto one line — so no backslash line-continuation is required for this command. This removes the one construct proven unsafe by this evidence, without altering the command's arguments, its redirection target, or its backgrounding behavior.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Document Status | Final — Root Cause Report | — | — |

---

**End of Document — Phase 17.5A Root Cause Report, v1.0**
