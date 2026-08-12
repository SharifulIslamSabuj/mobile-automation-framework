---
document_id: PHASE-19-TRUE-PARALLEL
title: True Parallel Execution Architecture Design
version: v1.0
status: Final — Architecture Design Report (Read-Only, No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-11
last_updated: 2026-08-11
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.2, PHASE-19.3, PHASE-19.4L, PHASE-19.4M, PHASE-19.4O, PHASE-19.4P, PHASE-19.5, PHASE-19.5B, PHASE-19.5C]
classification: Internal
---

# Phase 19 — True Parallel Execution Architecture Design

---

## 1. Executive Summary

The current production workflow (`.github/workflows/mobile-automation.yml`, commit `cdc6c4c`) runs Native and Docker test execution **sequentially, inside one job, on one runner, sharing one emulator and one Appium server**. This was a deliberate, correctly-scoped design for Phase 19.5's own goal (additive, non-blocking *validation*), and it has been thoroughly evidenced as working exactly as intended (Phase 19.5B, Supplemental Run 5 Confirmation, Phase 19.5C). It is not, however, *true parallel execution* — the two paths never run at the same time, and total wall-clock time is their sum (~25–27 minutes observed), not their maximum.

This report defines what true parallel execution means for this project, evaluates four candidate architectures against the project's own established constraints (Model 3 Docker boundary, single hardcoded emulator serial, ISOLATED execution strategy, the Phase 19.4M failure-classification principles, the Phase 19.5C decision to keep native primary), and recommends **Option A — separate GitHub Actions jobs** (`native-tests`, `docker-tests`, `aggregate`), each provisioning its own emulator and Appium server on its own runner, with `docker-tests` marked `continue-on-error: true` at the job level so its result remains fully visible without gating the overall run.

This is a design document only. No file was modified, no CI run was triggered, and nothing described here has been implemented or measured in a truly-concurrent run.

---

## 2. Current Architecture

Read directly from `.github/workflows/mobile-automation.yml`, `Dockerfile`, `.dockerignore`, `build.gradle`, `AndroidDriverFactory.java`, and `config-emulator.properties` — all confirmed unmodified at the time of this review (`main` at `cdc6c4c`).

```
GitHub Actions — ONE job ("build-and-test"), ONE runner (ubuntu-24.04)
│
├─ Checkout, JDK 17, Gradle setup, KVM enable, Appium install,
│  UiAutomator2 driver install, AUT APK restore/download        (one-time)
│
├─ Build Docker Harness Image (docker build, Model 3 image)     (one-time)
│
└─ "Android Emulator Provisioning, Build Validation & Test
   Execution" — ONE composite step (reactivecircus/android-emulator-runner):
   │
   ├─ Boots ONE emulator (device.name = emulator-5554, hardcoded
   │  in config-emulator.properties and every environment profile)
   ├─ Starts ONE Appium server (backgrounded with `&`, port 4723)
   ├─ Waits for Appium reachability (polling loop)
   ├─ Compiles (./gradlew compileJava compileTestJava)
   ├─ [BLOCKING] Native: ./gradlew test ... (full 19-test suite)
   │     → exit code captured to file, native-test.log
   ├─ Copies build/test-results, build/reports, reports/, logs/
   │  to ci-results/native/  (MUST happen before Docker overwrites them)
   ├─ [BLOCKING, STARTS ONLY AFTER NATIVE FINISHES] Docker:
   │     docker run --network=host ... mobile-automation-harness:ci
   │     ./gradlew test ...  (full 19-test suite, same emulator,
   │     same Appium server, same APK, same platform version)
   │     → exit code captured to file, docker-test.log
   ├─ Copies build/test-results, build/reports, reports/, logs/
   │  to ci-results/docker/
   └─ Restores NATIVE_EXIT as the job's real exit code (sole gate)
```

**Key structural facts, all confirmed by direct inspection:**

- The Docker `docker run` line does not begin until the native `./gradlew test` line has fully returned — there is no `&`, no backgrounding, no `wait` around either test-execution command. They are two sequential foreground commands within the same shell script.
- Both paths target the identical emulator serial (`emulator-5554`), the identical already-running Appium server (started once, reused twice), and the identical `$GITHUB_WORKSPACE` filesystem — `build/`, `reports/`, and `logs/` are literally the same paths for both, which is exactly why the workflow must copy native's output to `ci-results/native/` **before** Docker's run overwrites those same paths.
- The two paths do not share Gradle caches: native uses the default `~/.gradle` (via `gradle/actions/setup-gradle`), Docker uses a dedicated `${RUNNER_TEMP}/docker-gradle-home` mounted as `GRADLE_USER_HOME`. This separation already exists and is preserved by every option evaluated below.
- `device.name=emulator-5554` is hardcoded across every environment profile (`config-emulator.properties`) and read via a single `ConfigurationKeys.DEVICE_NAME` key with no multi-device or `ANDROID_SERIAL` handling anywhere in `AndroidDriverFactory.java` or elsewhere in `src/`. The framework, as it exists today, assumes exactly one device per execution.
- `build.gradle`'s `test { useTestNG() ... }` block sets no `maxParallelForks`, no TestNG `parallel` attribute, and no `testng.xml` exists — tests execute sequentially within a single JVM fork, on both paths, today. This is an orthogonal axis to job-level parallelism and is not changed by anything recommended in this report.

---

## 3. Current Execution Flow

**Why this is NOT true parallel execution — stated explicitly, per this phase's own instruction not to assume concurrency where the workflow is sequential:**

1. Native and Docker do not start independently — Docker's first command (`docker run`) is textually and causally *after* native's last preservation step in the same script.
2. They share the same runner, same emulator, same Appium server instance, same filesystem paths (requiring the copy-before-overwrite choreography described above) — this is deliberate **reuse**, not **isolation**.
3. Observed wall-clock time is consistent with summation, not concurrency: across all 7 real production runs reviewed in Phase 19.5B and its supplement, native test execution alone took ~10–12 minutes, Docker test execution alone took ~10–12 minutes, and total job duration was consistently their sum plus a smaller one-time setup cost (e.g. the Supplemental Run 5 Confirmation: native 11m33s + Docker 11m1s = 22m34s of test execution, against a 25m27s total job duration — the arithmetic of a sequential, not concurrent, execution).
4. A failure in native does not "let Docker keep running independently" in any meaningful concurrency sense — Docker only ever runs because native's line already finished (pass or fail); there is no scenario in the current design where Docker is mid-execution while native is also mid-execution.

This confirms the premise stated in this phase's own context: the current architecture is an **additive dual-path validation model**, correctly qualified and validated as such (Phase 19.5B/19.5C), but not **true concurrent execution**.

---

## 4. Definition of True Parallel Execution

For this project, **true parallel execution** requires all of the following, simultaneously:

1. Native and Docker execution are **independently started** — neither path's first command is causally gated on the other path's completion.
2. Both are **capable of executing at the same time** — their execution windows can overlap in wall-clock time.
3. **One path does not wait for the other to start.**
4. Each path has an **isolated execution environment** — its own emulator, its own Appium server instance, its own filesystem, such that neither path's in-progress state can be corrupted or overwritten by the other.
5. Each path produces **independently identifiable results** — already true today at the artifact-naming level, and preserved by every option below.
6. **A failure in one path does not prevent the other path from completing** — today this is trivially true only because Docker always runs after native regardless of native's outcome (proven directly, Phase 19.5B Run 5); true concurrency must preserve this same guarantee under actual overlap, not just sequencing.
7. **Final aggregation occurs only after both paths finish**, regardless of which finishes first or whether either failed.

This is explicitly distinct from "parallel validation" (today's model: both paths run, in some order, and both results are visible) — true parallel execution additionally requires overlapping wall-clock execution windows and environment-level isolation, neither of which the current design provides or needs, given its own narrower goal.

---

## 5. Candidate Architecture Evaluation

### Option A — Separate GitHub Actions Jobs

```yaml
jobs:
  native-tests:
    runs-on: ubuntu-24.04
    # boots its own emulator, starts its own Appium server, runs native suite
  docker-tests:
    runs-on: ubuntu-24.04
    continue-on-error: true   # job may fail without failing the overall run
    # boots its own emulator, starts its own Appium server, builds+runs Docker (Model 3)
  aggregate:
    needs: [native-tests, docker-tests]
    if: always()
    # publishes a combined summary; does not itself compute pass/fail
```

| Factor | Assessment |
|---|---|
| True concurrency | Yes — GitHub Actions jobs without a `needs:` dependency between them are scheduled independently and run concurrently (scheduler/runner-availability permitting), which is the platform's own native concurrency mechanism, not something this project has to build. |
| Emulator isolation | Complete — each job gets its own runner VM, hence its own emulator boot. `device.name=emulator-5554` needs no change: each runner's own local ADB has exactly one device with that serial, and the two runners cannot collide since they are different machines. |
| Appium isolation | Complete — each job starts its own Appium server on its own runner's `localhost:4723`. Identical port *number* on two different machines is not a collision. |
| ADB isolation | Complete, and free — a direct consequence of separate VMs, not a new mechanism to design. |
| Artifact isolation | Complete — each job has its own `$GITHUB_WORKSPACE`; the current copy-before-overwrite choreography (`ci-results/native/` vs `ci-results/docker/`) becomes unnecessary, since `build/`, `reports/`, `logs/` never collide across two separate workspaces. |
| Failure handling | Clean — `native-tests` ungated (its failure fails the run, preserving the sole quality gate); `docker-tests` uses job-level `continue-on-error: true` (Section 9 explains why this is the correct, narrowly-scoped mechanism here). |
| Implementation complexity | Moderate — duplicates the emulator-boot + Appium-start steps into a second job body; requires an `aggregate` job for a combined summary. No new infrastructure primitives are needed. |
| CI cost | Two runner-VMs consumed concurrently instead of one sequentially — total compute-minutes billed is comparable (roughly the same total work, parallelized rather than summed), with a modest increase from duplicating one-time setup (checkout, JDK, Gradle setup, KVM enable, Appium install, APK restore) across two runners instead of one. |
| Execution time | Wall-clock expected to drop toward `max(native job, docker job)` instead of their sum (Section 11) — estimate only, not yet measured. |
| Maintainability | Two job bodies to keep in sync (e.g. Android target version bumps) instead of one script — a real but modest cost, mitigated by keeping shared values in workflow-level `env:` as today. |
| Debugging | Improved over today — each path's steps and logs appear as their own distinct job in the Actions UI, rather than interleaved within one script's combined stdout. |
| Rollback | Simple — delete the `docker-tests` job and the `aggregate` job's dependency on it; `native-tests`' own steps are untouched by this design, unlike today's single script where Docker's lines are interleaved with native's. |
| Suitability for this repository | High — preserves the Model 3 boundary (Phase 19.2/19.3) exactly: the container still holds only Java/Gradle, and the Android SDK/ADB/Appium stack still lives entirely on the (now per-job) host runner. Nothing about Model 3 itself is reopened. |

### Option B — Matrix-Based Execution

`strategy: matrix: path: [native, docker]` collapsed into one job body, branching internally on `matrix.path`.

| Factor | Assessment |
|---|---|
| Isolation | Identical to Option A under the hood — a matrix job is still a separate runner/job instance per cell; GitHub Actions' concurrency guarantee is unchanged. |
| Readability | Native and Docker are **not symmetric** in this repository: Docker needs an image-build step and a `docker run` wrapper that native has no equivalent of; native has no counterpart to Model 3's container boundary at all. Representing this asymmetry inside one matrix-conditioned job body would require `if: matrix.path == 'docker'` branching scattered through otherwise-shared steps — less readable here than Option A's two plainly-separate job bodies, given how different the two paths' actual step sequences are. |
| Artifact handling | Same naming pattern as Option A, parameterized by `matrix.path` — functionally equivalent. |
| Conditional behavior | The very asymmetry noted above means "conditional behavior" is the dominant feature of this option's job body, not an edge case — a sign the two paths are better modeled as genuinely separate jobs. |
| Failure aggregation | Same mechanism as Option A (`continue-on-error` on the Docker matrix cell, an `aggregate` job reading `needs.<job>.result`). |
| Future scalability | This is Option B's genuine strength — extending to a device/OS matrix later (Section 12) is more natural with `strategy: matrix:` than with two hand-written job bodies. Worth revisiting **if and when** a true multi-dimensional matrix (e.g. API level × execution-path) is actually needed; not decisive for today's two-path, asymmetric case. |

### Option C — Same GitHub Runner, Two Concurrent Processes

Running native and Docker execution *simultaneously* on the **same** single runner (e.g. backgrounding both with `&` and `wait`ing on both).

| Factor | Assessment |
|---|---|
| Emulator conflicts | Direct conflict if both paths target the same single emulator (`emulator-5554`) concurrently — two simultaneous Appium sessions against the same device is exactly the kind of contention this engagement's own evidence (Phase 19.4G's contention finding, cited in this project's prior diagnostic history) already found problematic. Running two *separate* emulators on one runner is technically possible but was never attempted or evidenced anywhere in this engagement. |
| ADB conflicts | Two AVDs on one host cannot both claim the default `emulator-5554` serial — this would require a source-level device-targeting change (explicitly out of scope for this design-only phase, and an added complexity Option A does not need at all). |
| Appium port conflicts | Two Appium servers on one runner would need distinct ports, again requiring configuration changes Option A avoids entirely by using separate machines instead. |
| CPU/RAM contention | GitHub-hosted `ubuntu-24.04` standard runners have fixed, limited CPU/RAM. Running two KVM-accelerated emulators plus two Appium servers plus a Docker container simultaneously on that same constrained hardware is a materially heavier load than the same total work spread across two runners (Option A), with no corresponding benefit. |
| Reliability risk | Elevated and unproven — nothing in this engagement's evidence base has ever tested concurrent multi-emulator or multi-Appium-server operation on one runner. |

**This option is technically possible but is explicitly rejected**, per this phase's own instruction not to recommend it merely because it is possible — it introduces the isolation problems Option A avoids for free, without any offsetting benefit.

### Option D — Separate Self-Contained Runners/Environments

| Factor | Assessment |
|---|---|
| Relationship to Option A | Standard GitHub Actions jobs (Option A) already execute on separate, self-contained runner VMs by default — this *is* "separate runners," achieved through the platform's own native job mechanism, at no additional operational cost. |
| Cost / complexity beyond Option A | Anything beyond GitHub-hosted job VMs (e.g. dedicated self-hosted runners, a separate CI provider) would add real operational burden (provisioning, patching, availability) with no isolation benefit Option A does not already provide today. |
| Future Grid/cloud compatibility | This is where a genuinely distinct "Option D" becomes relevant later — if a future phase needs Selenium/Appium Grid nodes, BrowserStack, or Sauce Labs execution, that would naturally take the form of replacing a job's local emulator+Appium step with a cloud/Grid endpoint, without requiring a rearchitecture of the job-per-path skeleton recommended here (Section 12). |

**Conclusion**: Option D, in the form of GitHub-hosted job isolation, is already subsumed by Option A. A stronger form of "separate environments" is not justified by current evidence or need, though it remains the natural on-ramp for future cloud/Grid execution.

---

## 6. Emulator Architecture

**Recommendation: each execution path provisions its own dedicated emulator instance, on its own runner, via the same `reactivecircus/android-emulator-runner` action already in use today.**

Evidence and reasoning:

- `device.name=emulator-5554` is hardcoded, framework-wide, with no multi-device handling anywhere in the codebase (Section 2). **Sharing one emulator between two concurrently-running paths is not safe** to assume, and this report does not assume it — it is explicitly evaluated and rejected (Option C, Section 5) on the strength of this engagement's own prior contention evidence.
- Under Option A (separate jobs/runners), the identical hardcoded serial `emulator-5554` requires **no change at all** — each runner's own local ADB sees exactly one device with that name, and the two runners' devices cannot collide because they are different machines. This is a meaningful design elegance point in favor of Option A specifically: it achieves emulator isolation without touching `AndroidDriverFactory.java`, `ConfigurationKeys.java`, or any `config-*.properties` file.
- Docker should continue using the established Phase 18/19 **Model 3** architecture unchanged: the container holds only Java/Gradle, and reaches its *own runner's* Appium server via `--network=host`, exactly as it does today against the shared runner — the only thing that changes is which runner it's reaching (its own, not native's).

---

## 7. Appium Architecture

**Recommendation: one Appium server per execution path, each on its own runner, each on port 4723 (unchanged), each serving exactly one session at a time (matches `execution.strategy=ISOLATED`, which is unaffected by this design).**

- No shared Appium server between paths — each job starts and owns its own instance, using the same startup/reachability-polling pattern already proven across 7 production runs.
- Identical port numbers on two separate runners is not a collision; no port renumbering is required.
- Docker-to-host networking is unchanged from today: `--network=host` inside `docker-tests`' own runner, reaching that same runner's own Appium server.
- Session isolation is unaffected — this design does not introduce concurrent sessions *within* a path (TestNG/Gradle still runs tests sequentially per Section 2); it only makes the two *paths'* single sessions run concurrently with each other.
- Future Grid compatibility: a real Appium/Selenium Grid hub-node model would sit above this per-runner-per-path pattern. Nothing in this design forecloses that — a job's local Appium server could later be replaced by a Grid node registration without altering the surrounding job-parallelism structure (Section 12).

---

## 8. ADB Architecture

**Recommendation: no explicit ADB disambiguation mechanism is needed.**

- Under Option A, ADB isolation is a free consequence of job/runner separation — each runner has exactly one connected/emulated device, exactly matching the framework's existing single-device assumption (Section 2: no `ANDROID_SERIAL` references exist anywhere in `src/`).
- Docker does not need direct ADB access under Model 3 — it only speaks HTTP to its own runner's Appium server, which internally handles device targeting via the existing `device.name`/UiAutomator2 capability path, unchanged.
- This is the safest model precisely because it requires **zero new device-targeting logic**: the existing single-device assumption remains true from each runner's own point of view.

---

## 9. Artifact Architecture

**Namespace** (unchanged in spirit from today, now produced by two independent jobs instead of one):

```
native-tests job  → ci-results/native/  → uploaded as mobile-automation-run-${{ github.run_number }}
docker-tests job  → ci-results/docker/  → uploaded as mobile-automation-docker-run-${{ github.run_number }}
```

Because each job now has its own `$GITHUB_WORKSPACE`, the current copy-before-overwrite choreography (necessary today only because both paths share one filesystem) becomes unnecessary — `build/test-results`, `build/reports/tests/test`, `reports/`, and `logs/` never collide across two separate workspaces, so each job can upload directly from its own paths.

Each path continues to preserve, independently: JUnit XML, screenshots, ExtentReports HTML, and execution logs — no change to *what* is captured, only to *where* it physically lives during the run.

**Final aggregation:**

```
native-tests.result  ─┐
                       ├─→ aggregate job (needs: both, if: always())
docker-tests.result  ─┘        │
                                ↓
                    Publishes combined summary
                    (does not itself compute pass/fail —
                     see Section 10 for why)
```

No result may overwrite another path's artifacts — guaranteed structurally (separate workspaces), not by convention as today.

---

## 10. Failure Semantics

**Native passes + Docker passes → `workflow = SUCCESS`.** `native-tests` succeeds (ungated); `docker-tests` succeeds; `aggregate` runs and reports both as clean. Unchanged from today's outcome.

**Native fails + Docker passes → `workflow = FAILURE`.** `native-tests` is not marked `continue-on-error` — GitHub Actions computes the overall run's conclusion from all non-continue-on-error jobs' conclusions, so `native-tests` failing fails the run regardless of `docker-tests`' result. This preserves native as the sole, unweakened quality gate — identical policy to Phase 19.5/19.5B/19.5C, just enforced by GitHub's own job-dependency semantics instead of the current file-based exit-code restoration trick.

**Native passes + Docker fails → `workflow = ?` — decided explicitly here, not silently:**

**Recommendation: `workflow = SUCCESS`. Docker remains non-blocking.** This is not a new decision — it is the direct, unchanged continuation of Phase 19.5's original design intent (Docker as additive qualification, not yet a co-equal gate) and Phase 19.5C's explicit, evidence-based recommendation (`A. KEEP NATIVE PRIMARY + DOCKER PERMANENT PARALLEL`, precisely because 7 production runs was judged insufficient to reassign blocking authority to Docker). Phase 19 changes the *execution model* (sequential → concurrent); it introduces no new evidence about Docker's reliability that would justify also changing the *gating policy*. Mechanically, this is achieved by marking the `docker-tests` job `continue-on-error: true`:

- This is the standard, GitHub-native mechanism for "this job's failure must not gate the overall run" — architecturally distinct from the step-level `continue-on-error` that Phase 19.5's own rules cautioned against (that constraint concerned masking one line's exit status inside a single `sh -c` dispatcher within one job; this is a job-level primitive governing how GitHub computes the *run's* conclusion from its *jobs'* conclusions).
- It is arguably an **improvement** in failure visibility over today: a failing `docker-tests` job shows as its own distinct red job in the Actions UI (a dedicated status icon), rather than being buried as a table row inside one job's step summary as today.
- `aggregate`'s `if: always()` step reads `needs.docker-tests.result` (which correctly reports `'failure'` even though `continue-on-error` prevented that failure from dragging down the overall run) and republishes it in the summary — same reporting fidelity as today's `docker_exit` value, at job granularity instead of file granularity.

**Both fail → `workflow = FAILURE`.** Guaranteed by `native-tests` alone, regardless of `docker-tests`.

**Infrastructure failure vs. test failure:** preserved by step-boundary evidence, exactly as today (Phase 19.4M's principle was never a special flag — it was about *which step's log* shows the failure). Each job's own setup steps (emulator boot via `reactivecircus/android-emulator-runner`, the Appium-reachability wait loop, `set -eu` around compilation) continue to hard-fail that job immediately and distinguishably from a soft test-content failure inside the `./gradlew test` / `docker run` step — this distinction is preserved per-job under this design, not newly invented.

---

## 11. Performance Analysis

**CURRENT OBSERVED** (from real production evidence, Phase 19.5B and its Supplemental Confirmation, 7 runs): native test execution ≈ 10–12 min; Docker test execution ≈ 10–12 min; one-time setup+compile overhead (checkout → JDK → Gradle setup → KVM → Appium install → APK restore → Docker image build → emulator boot → Appium wait → compile) ≈ 3 min, computed directly from the Supplemental Run 5 Confirmation's own numbers (25m27s total job duration − 11m33s native − 11m1s Docker ≈ 2m53s). Total observed job duration: consistently ~25–27 minutes, matching a sequential (summed) model, not a concurrent one.

**EXPECTED AFTER IMPLEMENTATION** (Option A, not yet measured): each job pays its own ~3 min one-time setup plus its own ~11–12 min test execution ≈ 14–15 min per job. Since `native-tests` and `docker-tests` run concurrently on separate runners, overall workflow wall-clock is expected to approach `max(native job, docker job)` ≈ **roughly 14–16 minutes**, rather than the current ~25–27 minute sum.

This is an **estimate derived from existing sequential timing evidence, not a new measurement**, and is explicitly not claimed as verified. Specific caveats:

- Duplicating emulator boot and Docker image build across two *simultaneous* runners has never been observed in this engagement — only ever measured sequentially, on one runner.
- GitHub Actions' own concurrent job-scheduling/start latency (queueing behavior when two jobs in the same workflow both request a runner) is not captured by this arithmetic and could add variance in either direction.
- No performance improvement should be treated as confirmed until Phase 19 is actually implemented and measured across real runs, consistent with this phase's own instruction.

---

## 12. Scalability Analysis

| Future direction | Compatibility with the recommended architecture |
|---|---|
| TestNG parallel execution (methods/classes) | Orthogonal axis, not solved by this design and not blocked by it — could be layered inside either job later (e.g. `maxParallelForks`, TestNG `parallel="methods"`), but would need its own dedicated evaluation (multi-session contention against one emulator is a similar risk class to the one this report already declines to assume-safe in Section 6/Option C) — a distinct future phase, not assumed here. |
| Multiple Android emulators | Natural extension of Option A/B's job-per-cell pattern — additional jobs or matrix entries, each independently isolated exactly as `docker-tests`/`native-tests` are today. |
| Selenium/Appium Grid | Compatible — a job's local Appium server could later be replaced by a Grid node registration without restructuring the job-parallelism skeleton recommended here. |
| BrowserStack | Compatible in structure (a job could swap its local emulator+Appium step for a cloud capability set), but actual BrowserStack compatibility remains `NOT VERIFIED` per Phase 19.5C — this design does not change that status, it simply avoids foreclosing it. |
| Sauce Labs | Same reasoning as BrowserStack — structurally compatible, not itself verified by this design. |
| Additional Docker workers | Natural extension — additional matrix/job entries, each an independent Docker execution against its own runner/emulator, following the same pattern as `docker-tests`. |
| Device/OS matrices | This is where Option B (matrix) becomes genuinely attractive (Section 5) — `strategy: matrix:` scaling to multiple API levels/device profiles later, reusing the same per-cell isolation pattern recommended in this report. |

The recommended architecture does not create a dead end for Phase 21 Grid or later cloud execution — it establishes job-per-path isolation as the baseline pattern that those future phases would extend, not replace.

---

## 13. Recommended Architecture

**Option A — separate GitHub Actions jobs** (`native-tests`, `docker-tests` with `continue-on-error: true`, `aggregate` with `needs: [native-tests, docker-tests]` and `if: always()`), each independently provisioning its own emulator and Appium server via the already-proven `reactivecircus/android-emulator-runner` action, with Docker retaining the Model 3 architecture unchanged (`--network=host` against its own runner's own Appium server).

**Why the others are rejected:**

- **Option B (matrix)** is rejected for the *current* two-path, structurally asymmetric case — native and Docker have genuinely different step sequences (Docker uniquely needs an image-build step and a `docker run` wrapper), and forcing that asymmetry into one matrix-conditioned job body would reduce readability rather than improve it. Its scalability advantage is real and is explicitly preserved as a future option (Section 12) if a genuine multi-dimensional matrix (e.g. device × path) is later needed.
- **Option C (same runner, concurrent processes)** is rejected because it reintroduces exactly the isolation problems (emulator contention, ADB serial collision, Appium port collision, resource contention on fixed runner hardware) that Option A avoids for free, with no evidence-based justification for accepting that risk when a cost-comparable alternative (Option A) does not carry it. It is not recommended merely because it is technically possible.
- **Option D (separate self-contained environments)**, beyond what standard GitHub-hosted jobs already provide, is rejected as unjustified additional operational cost at this time — Option A already delivers full runner-level isolation through the platform's own native mechanism. Option D's stronger form remains the correct future direction if/when Grid or self-hosted infrastructure is actually needed (Section 12), not a requirement today.

This recommendation preserves the Model 3 Docker boundary (Phase 19.2/19.3) without reopening it, and is consistent with Phase 19.5C's explicit decision to keep native primary — Phase 19 changes *how* the two paths execute, not *which* one gates the pipeline.

---

## 14. Implementation Blueprint

*(Design only — not implemented. Sufficient detail for a future implementation phase to follow without re-deriving the architecture.)*

1. **Job structure**: three jobs — `native-tests`, `docker-tests`, `aggregate` — replacing the current single `build-and-test` job.
2. **Job dependencies**: `native-tests` and `docker-tests` have no `needs:` between them (enables concurrent scheduling). `aggregate` has `needs: [native-tests, docker-tests]` and `if: always()` (runs regardless of either job's outcome).
3. **Runner allocation**: `runs-on: ubuntu-24.04` for both `native-tests` and `docker-tests` (unchanged runner image from today); `aggregate` can run on the same or a minimal runner, since it performs no test execution.
4. **Emulator provisioning**: each of `native-tests` and `docker-tests` independently includes its own KVM-enable step and its own `reactivecircus/android-emulator-runner` invocation with the same pinned `api-level`/`target`/`arch`/`profile` values currently in workflow-level `env:` (unchanged, shared via the same `env:` block at the top of the file).
5. **Appium lifecycle**: each job's own emulator-runner `script:` independently starts its own Appium server (same version-pinned `npm install --global appium@...`, same reachability-polling pattern) — duplicated, not shared, matching Section 7.
6. **Docker lifecycle**: `docker-tests` alone retains the "Build Docker Harness Image" step (`docker build -t mobile-automation-harness:ci .`) and the `docker run --network=host --user "$(id -u):$(id -g)" ...` invocation, unchanged in form from today, now running against its own job's own emulator/Appium.
7. **Networking**: unchanged — `--network=host` inside `docker-tests`' own runner, reaching that same runner's `127.0.0.1:4723`.
8. **ADB/device targeting**: unchanged — `device.name=emulator-5554` in both jobs, safe per Section 6/8 since each runner has exactly one device.
9. **Environment variables**: the shared workflow-level `env:` block (Appium version, AUT release tag/URL, Android target parameters) is unchanged and referenced identically by both jobs — this is configuration parity, not a concurrency mechanism, exactly as today.
10. **Artifact paths**: `native-tests` uploads directly from its own `build/test-results`, `build/reports/tests/test`, `reports/`, `logs/` (no `ci-results/native/` staging copy needed — no collision risk exists once workspaces are separate); `docker-tests` does the same from its own paths. Artifact names unchanged: `mobile-automation-run-${{ github.run_number }}` / `mobile-automation-docker-run-${{ github.run_number }}`.
11. **Exit-code handling**: `native-tests`' own step exit code is its natural job conclusion (no file-based capture/restoration needed — GitHub already tracks this per job). `docker-tests` is marked `continue-on-error: true` at the job level; its own step exit code is likewise its natural conclusion, simply prevented from affecting the overall run's conclusion by that flag.
12. **Final result aggregation**: `aggregate` job reads `needs.native-tests.result` and `needs.docker-tests.result` (both are GitHub-populated, no custom file/env-var propagation needed) and publishes the same kind of step-summary table already in use today (Section 9) — it does not itself set an exit code to gate the run; the run's conclusion is already correctly determined by `native-tests` (ungated) and `docker-tests` (`continue-on-error: true`), per Section 10.
13. **Failure semantics**: exactly as defined in Section 10 — no silent decisions, all four combinations enumerated and justified.
14. **Rollback strategy**: delete the `docker-tests` job and `aggregate`'s dependency on it; `native-tests`' own steps require no further changes, since they are not interleaved with Docker's steps in this design (unlike today's single script) — a cleaner rollback than the current architecture's own.

---

## 15. Risk Register

| Risk | Likelihood | Impact | Mitigation | Status |
|---|---|---|---|---|
| Emulator boot flakiness (per job, independently) | Low–Medium | That job fails independently; does not affect the other path (a genuine improvement over today, where an emulator problem would affect both paths at once) | Existing `emulator-boot-timeout: 600` reused unchanged | FUTURE — duplicating two concurrent emulator boots has never been observed together |
| Appium startup race (per job, independently) | Low | Same isolation benefit as above | Existing 30×2s reachability polling loop reused unchanged | FUTURE — not yet exercised concurrently across two jobs |
| Port collision (4723 on both jobs) | None | N/A | Structurally impossible — separate runner VMs, not merely separate processes | VERIFIED — a platform-level GitHub Actions guarantee, not a project-specific claim |
| ADB/device collision | None | N/A | Structurally impossible — separate runner VMs, each with exactly one device | VERIFIED — same platform-level guarantee |
| Docker networking (`--network=host`) | Low | Already proven reliable within a single job across 7 production runs | Same flag, now applied within `docker-tests`' own runner | Largely carried over as VERIFIED, but the exact combined shape ("this job also boots its own emulator+Appium before `docker run`") is new and untested together — FUTURE for that specific combination |
| Resource exhaustion per runner | Low | Each runner now hosts one emulator + one Appium + (for `docker-tests`) one Docker container — a lighter load per machine than today's single runner, which already ran this same total resource set sequentially | Splitting the load across two machines should reduce, not increase, per-machine pressure | FUTURE — not yet measured concurrently |
| Artifact collision | None | N/A | Structurally impossible — separate `$GITHUB_WORKSPACE`s | VERIFIED by design (Section 9) |
| Exit-code propagation / job-level `continue-on-error` misconfiguration | Low likelihood, **high impact if it occurs** | Misplacing `continue-on-error` on `native-tests` instead of only `docker-tests` would silently weaken the core safety requirement (native as sole gate) | Explicit design constraint stated in Section 10: `continue-on-error` belongs only on `docker-tests`, never `native-tests` | FUTURE — flagged as a high-care item for implementation-time review, no live-run evidence yet |
| AUT instability (the accepted third-party limitation) | Same as today (Phase 19.4M/O/P) | Unchanged — orthogonal to this architecture change | Phase 19.4P's Option A policy carries over unchanged (Section 10) | VERIFIED as already-understood; not newly introduced or newly mitigated by this design |
| Test-data collision between paths | Was possible today only in a limited sense (ISOLATED strategy resets per test method on the *shared* emulator) | With separate emulators per job, this cross-path dependency is **removed entirely** — a genuine isolation improvement over today | Each job's AUT starts fresh on its own emulator, no shared state ever exists between paths | Structurally VERIFIED by design; not yet exercised in a live run |
| Gradle cache contention | None (already isolated today: separate `GRADLE_USER_HOME` for Docker) | N/A | Separate runners make this separation even more absolute — no meaningful change | VERIFIED, carried over unchanged |

---

## 16. Phase 19 Acceptance Criteria

Objective, evidence-based criteria that would demonstrate **true parallel execution**, not merely dual-path validation (which is already proven):

1. `native-tests` and `docker-tests` job start timestamps overlap — both begin within the same short window, rather than one job's steps beginning only after the other job's steps have already completed.
2. Both jobs execute the full 19-test suite independently, each producing its own complete JUnit XML set (matching today's per-path completeness, now on separate runners).
3. Both jobs produce independently named, independently collected artifacts, with zero missing or corrupted files in either.
4. A failure in one path (naturally occurring or deliberately forced during implementation-time testing) does not prevent the other path from completing, and does not prevent `aggregate` from publishing a complete summary — this must be observed directly under the new architecture, not merely inferred from today's sequential-model evidence.
5. `aggregate`'s summary correctly and completely reflects both jobs' real results in every observed run, with no result ever hidden, suppressed, or silently converted.
6. The overall workflow conclusion is driven solely by `native-tests`' result — confirmed by at least one real run where `docker-tests` fails while `native-tests` passes, and the overall run still shows `success` (directly validating the Section 10 design, not just asserting it).
7. Total workflow wall-clock duration is measurably and repeatably less than the current sequential baseline (~25–27 minutes, Section 11) — demonstrating actual overlap rather than merely re-timed sequential execution.
8. No cross-path contamination is observed — each path's AUT state, screenshots, and logs are traceable to exactly one path, with no evidence of one path's execution affecting the other's environment.
9. The existing native quality gate remains intact and unweakened throughout — no run in the acceptance sample shows a native failure being masked, ignored, or overridden by Docker's result.

No arbitrary pass-count thresholds are introduced here; criteria 1–9 are structural/behavioral proofs of concurrency and safety, not sample-size claims — sample-size-based confidence (e.g. "how many runs before this is considered stable") is a separate question already addressed by Phase 19.5C's own future-qualification conditions and is not restated here.

---

## 17. Rejected Alternatives

Summarized from Section 5/13 for quick reference:

- **Option B (matrix-based execution)** — rejected for the current two-path asymmetric case; its scalability advantage is preserved as a future option if a genuine multi-dimensional matrix is later needed.
- **Option C (same runner, concurrent processes)** — rejected; reintroduces emulator/ADB/Appium-port contention risks that Option A avoids for free, and is not recommended merely because it is technically possible.
- **Option D (separate self-contained runners/environments), beyond standard GitHub-hosted jobs** — rejected as unjustified additional operational cost today; Option A's GitHub-hosted job VMs already provide this isolation natively. Remains the natural future direction for Grid/cloud execution, not a current requirement.

---

## 18. Final Recommendation

**Adopt Option A** — three GitHub Actions jobs (`native-tests`, `docker-tests` with job-level `continue-on-error: true`, `aggregate` with `needs: [native-tests, docker-tests]` and `if: always()`) — as the target architecture for a future Phase 19 implementation. This achieves true concurrent execution using GitHub Actions' own native job-scheduling and job-dependency mechanisms, requires no change to the framework's existing single-device assumption, preserves the Model 3 Docker boundary and the native-primary quality-gate policy exactly as already established and evidenced (Phase 19.5/19.5B/19.5C), and does not foreclose future TestNG-level parallelism, additional emulators, or Grid/cloud execution (Section 12).

This report makes no claim that this architecture has been implemented, run, or measured — it defines what a future implementation phase should build and how it should be evaluated (Section 16), consistent with this phase's explicit design-only, read-only scope.

---

## 19. Final Verdict

# A. TRUE PARALLEL ARCHITECTURE DEFINED — READY FOR IMPLEMENTATION

The current architecture has been read directly from the repository and confirmed sequential, not concurrent (Sections 2–3). A precise, project-specific definition of true parallel execution has been established (Section 4). Four candidate architectures were evaluated against that definition and against this project's own real constraints — the hardcoded single-device assumption, the Model 3 Docker boundary, the ISOLATED execution strategy, and the Phase 19.5C native-primary decision — with one (Option A) selected and three explicitly rejected with reasoning (Sections 5, 13, 17). Emulator, Appium, ADB, and artifact architectures are each defined with evidence-based reasoning, not assumption (Sections 6–9). Failure semantics are decided explicitly, not silently, including the one genuinely new policy question this phase raised (Docker-fails/native-passes → non-blocking, Section 10) with justification tied directly to Phase 19.5C's own findings. A concrete implementation blueprint (Section 14), risk register with explicit VERIFIED/FUTURE labeling (Section 15), and objective, non-arbitrary acceptance criteria (Section 16) are all provided. No file was modified, no CI was triggered, and no performance claim is presented as measured rather than estimated (Section 11). The architecture is sufficiently defined for a future implementation phase to proceed without re-deriving these decisions.

---

## Repository Safety Verification

- `git status`: only `docs/docker/` is untracked (this report and its Phase 19.1–19.5C predecessors); no tracked file is modified.
- `git diff`: empty (no tracked file changed).
- `main` remains at commit `cdc6c4c` — unchanged throughout this phase.
- No workflow file (`.github/workflows/mobile-automation.yml`), Dockerfile, `.dockerignore`, Java/test source file, or Gradle configuration file was modified — all were only read.
- No CI run was triggered; no `workflow_dispatch` call was made.
- No commit or push was made.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-11 |
| Document Status | Final — Architecture Design Report (Read-Only, No Implementation) | — | — |

---

**End of Document — Phase 19 True Parallel Execution Architecture Design, v1.0**
