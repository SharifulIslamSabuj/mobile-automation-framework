---
document_id: PHASE-17.5D
title: Third CI Failure Remediation — Implementation Report
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.5C, PHASE-17.5A, PHASE-17.4A]
classification: Internal
---

# Phase 17.5D — Third CI Failure Remediation — Implementation Report

| Field | Value |
|---|---|
| Governing Document | [PHASE_17.5C_ROOT_CAUSE_REPORT.md](PHASE_17.5C_ROOT_CAUSE_REPORT.md) |
| Status | Final — Implementation Report |

## File Modified

`.github/workflows/mobile-automation.yml` — two multi-line shell constructs collapsed to single lines, plus explanatory comments. No other file touched.

## Exact Changes

**1. The Appium-reachability wait loop** — collapsed from an 11-line `for`/`if`/`done` block into one physical line, statements joined with `;`:

```
for i in $(seq 1 30); do if curl -sf http://127.0.0.1:4723/status > /dev/null; then echo "Appium server is reachable."; break; fi; if [ "$i" -eq 30 ]; then echo "::error::Appium server did not become reachable within 60 seconds."; exit 1; fi; sleep 2; done
```

**2. The `./gradlew test` invocation** — collapsed from a 5-line backslash-continued command into one physical line:

```
./gradlew test --no-daemon -Denv=emulator -Dplatform.version="${ANDROID_PLATFORM_VERSION}" -Ddevice.name="${EMULATOR_DEVICE_NAME}" -Dapp.path="${APK_PATH}"
```

## Why This Fixes the Issue

Phase 17.5C's evidence conclusively established the general mechanism behind all three failures to date: `reactivecircus/android-emulator-runner` executes each line of its `script:` input as an independent `sh -c` invocation, not as one cohesive script file. Any construct spanning multiple physical lines — a `for`/`do`/`done` block, or a backslash-continued command — is therefore syntactically incomplete the moment any one of its lines is executed in isolation. Collapsing each construct onto one physical line, with `;` as the statement separator (the standard POSIX way to write a compound command on one line), makes each one a complete, self-contained program that this action's per-line execution model can run correctly. No logic, ordering, or argument changed — only line layout.

**Change #2 (the `./gradlew test` line) is a pre-emptive application of the same confirmed root cause, not a separately guessed fix.** It uses the identical backslash-continuation pattern already proven broken twice in this same file (the Appium server line, Phase 17.5A) and now fully explained by Phase 17.5C's mechanism finding. Fixing it now, using the same evidence-backed mechanism rather than waiting for its own isolated failure, avoids burning a fourth CI run on a defect already conclusively understood.

## Validation Performed

- `npx js-yaml .github/workflows/mobile-automation.yml` — parses successfully (exit 0).
- `git diff` — confirms exactly these two constructs changed; no other line, trigger, artifact path, Appium/emulator configuration, or job/step structure differs.
- Local `sh -c` execution of an equivalent single-line `for ... ; do ... ; done` construct (with the same `if`/`break`/`sleep` shape) — confirmed it parses and runs correctly under a POSIX shell, not just visually reviewed.

## Compliance Check (per phase instructions)

- ✓ YAML validity — confirmed above.
- ✓ Gradle integrity — no `build.gradle` or Gradle task change; only the CLI invocation's line layout changed, not its arguments.
- ✓ Repository integrity — `git status` confirms only the workflow file and this cycle's `docs/ci/` reports changed.
- ✓ No regression — every previously-passing step (Checkout, JDK, Gradle setup, KVM, Appium/driver install, APK download, SDK install, AVD creation, emulator boot) is untouched.
- ✓ Still follows MA-CICD-001 — no trigger, permission, artifact, or retry-policy change.
- ✓ Still follows MA-CICD-002 — job/step design, Appium/emulator versions, and artifact paths are all unchanged.

## What Remains Unverified

Whether the Appium-reachability loop actually succeeds (i.e., Appium becomes reachable within 60 seconds), whether `./gradlew compileJava compileTestJava` and `./gradlew test` succeed under CI, whether the `-D` system-property forwarding reaches `ConfigReader`, whether the AUT installs, whether tests pass, and whether artifacts generate — none of these have been exercised by any run to date. This fix only removes the second confirmed class of syntax defect blocking the script from running at all.

---

**End of Document — Phase 17.5D Implementation Report, v1.0**
