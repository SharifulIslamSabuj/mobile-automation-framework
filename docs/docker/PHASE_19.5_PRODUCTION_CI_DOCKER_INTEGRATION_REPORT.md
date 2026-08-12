---
document_id: PHASE-19.5
title: Production CI Docker Integration
version: v1.0
status: Final — Implementation Report (Parallel, Non-Blocking Integration Live in Production CI)
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.3, PHASE-19.4, PHASE-19.4L, PHASE-19.4M, PHASE-19.4O, PHASE-19.4P]
classification: Internal
---

# Phase 19.5 — Production CI Docker Integration

---

## 1. Objective

Integrate the already-qualified Docker Model 3 execution path into the real production `.github/workflows/mobile-automation.yml`, running it in parallel with — and non-blocking relative to — the existing native (non-Docker) execution path, preserving that path as the sole quality gate and rollback baseline.

---

## 2. Scope

Production implementation. One file modified: `.github/workflows/mobile-automation.yml`. No Java source, test code, Page Object, `AndroidDriverFactory.java`, `Dockerfile`, `.dockerignore`, or AUT file was touched. No AUT patch/fork/replacement. No Phase 19.4N diagnostics implementation. No Phase 19.4B readiness behavior change.

---

## 3. Implementation Design

Within the existing single job (`build-and-test`), after the native full-suite run completes, the **same already-booted emulator and same already-running host Appium server** are reused to run the identical, unmodified test harness a second time inside the existing `Dockerfile`'s image. This required one hard constraint to be respected: `reactivecircus/android-emulator-runner@v2.38.0` executes each physical line of its `script:` input as an **independent** `sh -c` invocation (confirmed directly in this project's own prior forensic record, Phase 17.5C) — no shell variable, `set` option, or exit status implicitly carries from one line to the next. Both the native and Docker test commands are therefore written as single, self-contained lines that always return `0` to the action's own dispatcher, with their real exit codes captured to files (`${RUNNER_TEMP}/native-exit-code.txt`, `${RUNNER_TEMP}/docker-exit-code.txt`) instead. A final line reads both files back and `exit`s with the **native** exit code — restoring the exact pre-Phase-19.5 pass/fail semantics for the job as a whole.

---

## 4. Exact Workflow Changes

Single file: `.github/workflows/mobile-automation.yml`. Diff summary (full diff reviewed at Gate 2 before commit):

- `timeout-minutes`: `30` → `50` (two sequential full-suite runs plus one image build now share the job).
- New step **Build Docker Harness Image** (`docker build -t mobile-automation-harness:ci .`), placed before the emulator boots since it needs neither the emulator nor Appium.
- The existing `./gradlew test` line (native) is wrapped: `... > "${RUNNER_TEMP}/native-test.log" 2>&1; echo $? > "${RUNNER_TEMP}/native-exit-code.txt"; cat "${RUNNER_TEMP}/native-test.log"` — same command, same flags, unchanged.
- New lines: preserve native's `build/test-results/test`, `build/reports/tests/test`, `reports/`, `logs/` into `ci-results/native/` before Docker overwrites those same paths.
- New lines: `docker run --rm --network=host --user "$(id -u):$(id -g)" -v "${GITHUB_WORKSPACE}:/workspace" -v "${RUNNER_TEMP}/docker-gradle-home:/tmp/gradle-home" -e "GRADLE_USER_HOME=/tmp/gradle-home" -w /workspace mobile-automation-harness:ci ./gradlew test --no-daemon ...` (same test flags as native), same wrap-and-capture pattern.
- New lines: preserve Docker's own output into `ci-results/docker/`.
- New final line: restore the native exit code as the job's real outcome, after recording both to `$GITHUB_ENV` for later steps.
- **Verify Artifact Output** step: updated to inspect `ci-results/native/` and `ci-results/docker/` instead of the raw (now Docker-overwritten) paths.
- **Upload Artifacts** step: split into two — `mobile-automation-run-${{ github.run_number }}` (native, same name as before Phase 19.5, sourced from `ci-results/native/`) and `mobile-automation-docker-run-${{ github.run_number }}` (new, sourced from `ci-results/docker/`).
- **Publish Workflow Summary** step: adds native/Docker result rows, referencing `${{ env.native_exit }}`/`${{ env.docker_exit }}`.

No other step was modified. Checkout, JDK setup, Gradle setup, KVM enablement, Appium/UiAutomator2 installation, AUT APK retrieval, Appium server startup, the reachability wait, and the compile-validation step are all byte-identical to before this phase.

---

## 5. Why the Integration Is Parallel and Non-Blocking

**No `continue-on-error` is used anywhere in this change.** Non-blocking behavior is achieved entirely by the exit-code-capture design in Section 3/4: the Docker `docker run` line's own exit status, as seen by the action's line-by-line dispatcher, is always `0` (because the real exit code is diverted to a file via `; echo $? > ...` rather than being the line's own terminal status) — so a Docker failure cannot cause the action to fail-fast and abort the script. The job's *actual* outcome is decided by the script's own final line, which explicitly re-asserts the **native** result via `exit "${NATIVE_EXIT}"`. This is fully transparent and auditable: both real exit codes are printed in the raw job log (`NATIVE_TEST_EXIT=...`, `DOCKER_TEST_EXIT=...`), written to the step summary, and the Docker path's full JUnit/screenshot/log evidence is uploaded as its own artifact regardless of outcome (`if: always()`) — nothing is hidden, suppressed, or retried.

---

## 6. Ownership and Networking Handling

`--network=host` (GitHub-hosted Linux, matching every prior phase's proven configuration back to Phase 19.3) lets the container reach the host's Appium server at `127.0.0.1:4723` — the same address `appium.serverUrl` already defaults to in `config.properties`, so no `-Dappium.serverUrl` override was needed for the Docker invocation, keeping its flag set as close to the native invocation as possible (both use only `-Denv=emulator -Dplatform.version=... -Ddevice.name=... -Dapp.path=...`). `--user "$(id -u):$(id -g)"` plus a dedicated bind-mounted `GRADLE_USER_HOME` (`${RUNNER_TEMP}/docker-gradle-home`) reuses the exact fix Phase 19.4 established for the GitHub-hosted-runner uid/permission mismatch — not reinvented here.

---

## 7. Evidence from the Real CI Run

- **CI run**: [`31427933602`](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31427933602), triggered automatically by the push of commit `cdc6c4c` to `main`.
- **Job**: `Build & Test (Android Emulator)`, **conclusion: success**, duration **26m5s** (well inside the 50-minute budget).
- **Artifacts produced**: `mobile-automation-run-45` (native) and `mobile-automation-docker-run-45` (Docker), both present and downloaded successfully.

---

## 8. Native (Non-Docker) Result

`NATIVE_TEST_EXIT=0`. `./gradlew test` reported `BUILD SUCCESSFUL in 11m 48s`. JUnit XML, per test class:

| Class | Tests | Failures | Errors | Time |
|---|---|---|---|---|
| `CartTest` | 13 | 0 | 0 | 516.795s |
| `LoginTest` | 1 | 0 | 0 | 25.575s |
| `NavigationTest` | 1 | 0 | 0 | 31.467s |
| `ProductDetailsTest` | 4 | 0 | 0 | 88.971s |
| **Total** | **19** | **0** | **0** | — |

**19/19, 0 failures, 0 errors — VERIFIED** directly from the downloaded JUnit XML.

---

## 9. Docker Result

`DOCKER_TEST_EXIT=0`. `./gradlew test` (inside the container) reported `BUILD SUCCESSFUL in 11m 22s`. JUnit XML, per test class:

| Class | Tests | Failures | Errors | Time |
|---|---|---|---|---|
| `CartTest` | 13 | 0 | 0 | 496.001s |
| `LoginTest` | 1 | 0 | 0 | 25.205s |
| `NavigationTest` | 1 | 0 | 0 | 27.861s |
| `ProductDetailsTest` | 4 | 0 | 0 | 86.935s |
| **Total** | **19** | **0** | **0** | — |

**19/19, 0 failures, 0 errors — VERIFIED** directly from the downloaded JUnit XML.

---

## 10. Result Comparison

Both paths produced **identical outcomes**: 19/19, 0 failures, 0 errors, running the exact same 19 test cases across the same four test classes. Per-class timing was closely comparable (Docker within ~4% of native on every class, in both directions — not a consistent slowdown or speedup, consistent with normal run-to-run variance rather than a Docker-attributable overhead). Both artifacts contain full evidence: native produced 101 files (JUnit XML, Gradle HTML report, `automation.log`, ExtentReport HTML, screenshots); Docker produced 185 files (166 of them screenshots) — both complete, both independently downloadable, never overwriting each other.

---

## 11. AUT Limitation Impact — None Observed This Run

**No `EXTERNAL_AUT_CRASH`, and no failure of any kind, occurred in this validation run — on either path.** Per Phase 19.4M/19.4O/19.4P's own discipline, this is reported honestly as a clean pass on both paths, **not** as proof the known AUT limitation (Phase 19.4J/19.4K) is resolved or will not recur — the mechanism remains real, unfixed, and intermittent; this single run simply did not encounter it. Had a failure occurred, the design in Section 5 guarantees it would have been fully visible (in logs, the step summary, and the uploaded Docker artifact) without affecting the job's own pass/fail status, and would have been classified per the Phase 19.4M evidence standard rather than assumed.

---

## 12. Rollback Strategy

- **Which additions constitute Docker integration**: every change listed in Section 4, entirely contained in one file (`.github/workflows/mobile-automation.yml`), all clearly marked with `Phase 19.5` in their comments.
- **How to disable**: revert this single file to its pre-Phase-19.5 state (`git revert cdc6c4c` or `git checkout <parent-of-cdc6c4c> -- .github/workflows/mobile-automation.yml`) — no source, Dockerfile, or `.dockerignore` change is entangled with it.
- **Does the native path remain intact without any change?** Yes — even without any rollback, the native path's own command, flags, and pass/fail authority are unchanged from before this phase; rollback is only needed to remove the *additional* Docker step and its artifact, not to restore native functionality (which was never altered).
- **Does rollback require source/framework changes?** No — the entire integration lives in the workflow file only.

---

## 13. Gate-by-Gate Validation Record

- **Gate 1 (Static Review)**: YAML structure verified (uniform indentation, no tabs, all 15 steps correctly nested; block-scalar `script:` content uniformly indented). Confirmed native command content unchanged, Docker path additive, no framework/test/AUT files touched, correct `--network=host`/`--user`/`GRADLE_USER_HOME` handling, separate `ci-results/native`/`ci-results/docker` result trees.
- **Gate 2 (Diff Review)**: `git diff --name-only` confirmed exactly one file changed; every hunk in the diff traced to a specific, necessary Section 4 item; no unrelated file touched (`Dockerfile`, `.dockerignore`, `AndroidDriverFactory.java`, `LoginPage.java` all confirmed byte-identical to their established baselines).
- **Gate 3 (Commit/Push)**: committed as `cdc6c4c`, pushed to `main`.
- **Gate 4 (Real CI Validation)**: Sections 7–11 above — real, downloaded, directly-inspected evidence, not inferred from workflow startup or image build alone.

---

## 14. What Was Verified

- The production workflow now contains a working Docker execution path (**VERIFIED** — run `31427933602`).
- The native path remains functional and unchanged in intent, command, and authority over the job's pass/fail status (**VERIFIED** — same flags, `NATIVE_TEST_EXIT=0` correctly governs the overall job's `success` conclusion).
- Docker builds successfully in the production workflow (**VERIFIED** — `docker build` step completed without error before the emulator step began).
- Docker reaches the host Appium/emulator stack (**VERIFIED** — the Docker-side `./gradlew test` executed real Appium sessions against the real emulator, producing real, passing JUnit results across all four test classes).
- The real, unmodified test harness executes inside Docker (**VERIFIED** — identical test classes/methods, identical assertions, same source).
- Test results are collected independently for both paths (**VERIFIED** — two distinct artifacts, two distinct `ci-results/` subtrees, no overwrite).
- No failure was hidden this run (**VERIFIED** by design, Section 5 — trivially also true this run since neither path failed).
- The AUT limitation policy (Phase 19.4P) was preserved — no automatic `EXTERNAL_AUT_CRASH` classification logic was added, and none was needed this run.
- No framework, test, or AUT modification was introduced (**VERIFIED** — `git diff` confirms exactly one file changed).

---

## 15. What Was Not Verified

- Behavior when the Docker path *does* fail (this validation run had no failure on either path) — the non-blocking mechanism's *design* is verified (Section 5), but its behavior under an actual Docker-path failure has not yet been observed in production. A future occurrence (e.g., the known `EXTERNAL_AUT_CRASH` mechanism recurring on the Docker side only) would be the first live test of that path.
- Long-term run-to-run stability of the dual-path job (only one production run has occurred under this design).
- Whether the ~26-minute total job duration remains comfortably within the 50-minute budget under slower network/registry conditions for the Docker base image pull.

---

## 16. Risks and Limitations

- Every production CI run now takes roughly twice as long (native + Docker sequentially) — an accepted, deliberate cost of parallel qualification, not a defect.
- The `ci-results/` staging directories are workspace-local and not `.gitignore`d as a special case (they are transient build output, cleaned up implicitly by the ephemeral runner) — no repository pollution risk, since nothing in `ci-results/` is ever committed.
- The Docker image tag `mobile-automation-harness:ci` is static (not per-run); this is safe on an ephemeral GitHub-hosted runner (destroyed after the job) but would need a more careful tagging strategy if this workflow were ever run on a persistent, reused host.

---

## 17. Recommended Next Step

Continue observing the dual-path job across normal development activity (organic pushes/PRs) to accumulate more real-world evidence of both paths' comparative behavior — particularly to eventually observe how the non-blocking mechanism behaves during an actual Docker-side failure, and whether the AUT limitation recurs on one path, the other, both, or neither over a larger sample. Per the phase's own stop rule, no migration toward Docker-primary, no removal of the native path, and no further phase should proceed until this evidence has been reviewed.

---

## 18. Final Verdict

# PRODUCTION CI DOCKER INTEGRATION VERIFIED — READY FOR PARALLEL QUALIFICATION

The Docker execution path is live in production CI, running in parallel with and fully subordinate to the existing native path (which remains the sole quality gate, unchanged in behavior). A real production CI run (`31427933602`, commit `cdc6c4c`) directly confirmed: Docker builds successfully, reaches the host Appium/emulator stack, executes the full unmodified 19-test suite, and produces results (19/19, 0 failures, 0 errors) identical in outcome to the native path's own 19/19 result — both independently collected, both fully visible, neither hidden or combined with the other. No framework, test, or AUT file was modified. The non-blocking mechanism is implemented without `continue-on-error`, via explicit exit-code capture and restoration, fully documented inline in the workflow itself.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Document Status | Final — Implementation Report (Parallel, Non-Blocking Integration Live in Production CI) | — | — |

---

**End of Document — Phase 19.5 Production CI Docker Integration Report, v1.0**
