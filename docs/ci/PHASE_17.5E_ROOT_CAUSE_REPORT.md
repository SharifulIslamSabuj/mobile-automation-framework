---
document_id: PHASE-17.5E
title: Fourth CI Failure Root Cause Analysis — Wrapper Executable Bit
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.5C, PHASE-17.5D]
classification: Internal
---

# Phase 17.5E — Fourth CI Failure Root Cause Analysis

| Field | Value |
|---|---|
| GitHub Actions Run | [31173533143](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31173533143) |
| Triggering Commit | `49f13cf` — the Phase 17.5D fix |
| Failed Step | `Android Emulator Provisioning, Build Validation & Test Execution` |
| Step Duration | 11:18:51Z → 11:20:27Z (1m36s) |

---

## Executive Summary

Major progress: **the Appium-reachability loop succeeded** — `Appium server is reachable.` is printed at 11:20:26.79, roughly 2 seconds after the wait loop began, confirming both the Phase 17.5D fix and (for the first time) that Appium itself starts and responds correctly under CI. The script then reached `Build Validation: compiling framework and test sources...` and attempted `./gradlew compileJava compileTestJava --no-daemon`, which failed immediately:

```
/usr/bin/sh: 1: ./gradlew: Permission denied
```

Exit code `126` (the standard shell exit code for "command found but not executable").

**Root cause, verified directly against this repository's own git metadata (not the CI log alone):** `git ls-files -s gradlew` shows the file is tracked with mode `100644` (non-executable), not `100755` (executable). This is a pre-existing repository defect — the Gradle wrapper script was never committed with its executable bit set — that has been dormant and invisible in this project's Windows-based local development environment (where the Unix executable bit is not enforced the same way) and only manifests on a real Linux filesystem, such as this GitHub Actions runner, which does enforce it.

This is a **workflow/repository configuration bug**, not an emulator, Appium, Gradle, framework, or AUT defect — Appium is now confirmed working; the failure is purely a file-permission attribute on a tracked file, unrelated to anything in the workflow YAML itself.

---

## Failed Step / Command / Exit Code / Error

- **Step:** `Android Emulator Provisioning, Build Validation & Test Execution` (emulator boot again succeeded, ~40.3s; Appium startup and reachability now also succeeded — first time either has been confirmed working).
- **Command:** `./gradlew compileJava compileTestJava --no-daemon` (the Build Validation line).
- **Exit code:** `126`.
- **Error, quoted verbatim:**

```
2026-08-07T11:20:26.8001966Z [command]/usr/bin/sh -c echo "Build Validation: compiling framework and test sources..."
2026-08-07T11:20:26.8058148Z Build Validation: compiling framework and test sources...
2026-08-07T11:20:26.8097039Z /usr/bin/sh: 1: ./gradlew: Permission denied
2026-08-07T11:20:26.8137481Z ##[error]The process '/usr/bin/sh' failed with exit code 126
```

Immediately preceded by the first-ever confirmed successful Appium reachability check:

```
2026-08-07T11:20:24.7423112Z [command]/usr/bin/sh -c for i in $(seq 1 30); do if curl -sf http://127.0.0.1:4723/status > /dev/null; then echo "Appium server is reachable."; break; fi; if [ "$i" -eq 30 ]; then echo "::error::Appium server did not become reachable within 60 seconds."; exit 1; fi; sleep 2; done
2026-08-07T11:20:26.7920459Z Appium server is reachable.
```

---

## Root Cause

Confirmed directly against the repository, independent of the CI log:

```
$ git ls-files -s gradlew gradlew.bat
100644 ef07e0162b183eb9d19a2c9ba7035c283af9f8dd 0	gradlew
100644 db3a6ac207e507b0bc1635a9f2c18d3b174e682e 0	gradlew.bat
```

Git tracks a file's Unix executable bit as part of its mode (`100755` = executable, `100644` = not). `gradlew` is tracked as `100644`. On checkout, the runner's filesystem sets the file's actual permissions to match this tracked mode — so the checked-out `gradlew` has no execute permission, and `sh -c "./gradlew ..."` fails with `Permission denied` (exit 126) before `gradlew`'s own contents (a valid POSIX shell script) are ever interpreted.

This defect predates this CI implementation entirely — it has existed since `gradlew` was first committed to this repository, in a Windows development environment where the Unix executable bit is not meaningfully enforced locally, so `./gradlew` continued to "work" for every local run in this session. It was invisible until the first Linux-filesystem checkout: this GitHub Actions run.

**Classification: Repository configuration bug** (a tracked-file-mode defect), not a workflow-YAML, emulator, Appium, Gradle-logic, framework, or AUT problem.

---

## Evidence

CI log showing the exact failure, quoted verbatim (already reproduced above in full under "Failed Step").

Repository-level confirmation of the tracked file mode, independent of any CI run:

```
$ git ls-files -s gradlew
100644 ef07e0162b183eb9d19a2c9ba7035c283af9f8dd 0	gradlew
```

(For contrast, a correctly-tracked executable wrapper would show `100755`.)

---

## Impact

This is the furthest any run has progressed: emulator boot, KVM, SDK/AVD provisioning, Appium server startup, and Appium reachability are now all confirmed working under CI for the first time. The failure is now isolated entirely to Build Validation's first command, before any Gradle task, compilation, or test executes. Everything downstream (compile validation, test execution, `-D` property forwarding, AUT installation, artifact generation) remains unverified, blocked solely by this one file-permission defect.

---

## Confidence Level

**High.** The error (`Permission denied`, exit 126) is the standard, unambiguous shell response to attempting to execute a file lacking the execute permission bit, and is independently and conclusively corroborated by this repository's own git-tracked file mode (`100644`) for `gradlew` — not inferred from the CI log alone.

---

## Proposed Fix

*(Described only — not implemented in this report.)*

Update the executable bit on the git-tracked `gradlew` file (via `git update-index --chmod=+x gradlew`, which changes only the tracked file mode from `100644` to `100755` — no change to the file's actual script content, checksum aside from the mode bit, or behavior on any platform that already tolerates it). This is a repository-level fix, not a workflow-YAML fix: adding a `chmod +x ./gradlew` step to the workflow would work around the symptom on every CI run but would leave the same defect in place for any future Linux/macOS-based clone of this repository, which is the more correct scope for the fix given the defect is in the repository's own tracked file mode, not in the CI configuration.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Document Status | Final — Root Cause Report | — | — |

---

**End of Document — Phase 17.5E Root Cause Report, v1.0**
