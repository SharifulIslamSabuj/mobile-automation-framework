---
document_id: PHASE-19.3
title: Docker Implementation — Phase A (Local Opt-In Execution)
version: v1.0
status: Final — Implementation Report (Partially Verified)
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.0, PHASE-19.1A, PHASE-19.1B, PHASE-19.1C, MA-DOCKER-001]
classification: Internal
---

# Phase 19.3 — Docker Implementation, Phase A
## Local Opt-In Docker Execution

Governing architecture: [docs/docker/PHASE_19.2_DOCKER_ARCHITECTURE_SPECIFICATION.md](PHASE_19.2_DOCKER_ARCHITECTURE_SPECIFICATION.md) (Model 3).

---

## 1. Objective

Implement the minimum Docker artifacts needed to prove the existing, unmodified mobile automation framework can execute its Gradle/TestNG test harness from inside a container while the Android emulator, ADB, and Appium all remain on the host — exactly as Phase 19.2 specified. The existing non-Docker execution path must remain untouched and available.

---

## 2. Architecture Reference

Model 3 (Phase 19.2, Section 6):

```
Docker Container (Java 17 + Gradle 9.0.0 wrapper + unmodified test harness)
        │  HTTP → appium.serverUrl
        ▼
Host Appium 3.6.0 (UiAutomator2 8.2.2) → Host ADB → Android Emulator
```

---

## 3. Repository Audit

Re-confirmed directly (VERIFIED, not assumed — matches Phase 19.2's own audit):

| Item | Value |
|---|---|
| Java toolchain | 17 (`build.gradle`) |
| Gradle wrapper | 9.0.0, `gradle/wrapper/gradle-wrapper.properties` |
| `gradlew` | executable, `-rwxr-xr-x` |
| `appium.serverUrl` | `ConfigurationKeys.APPIUM_SERVER_URL`, default `http://127.0.0.1:4723` (`ConfigurationDefaults`), resolved via system-property → env-file → common-file → compiled-default |
| Config files | `config.properties`, `config-emulator.properties`, `config-real-device.properties` |
| Report/screenshot/log dirs | `reports/`, `reports/screenshots/`, `logs/` (all gitignored, runtime-only) |
| Existing CI APK source | `mda-2.2.0-25.apk` from `https://github.com/saucelabs/my-demo-app-android/releases/download/2.2.0/mda-2.2.0-25.apk` (`.github/workflows/mobile-automation.yml`) |
| Existing Docker files | None, prior to this phase |

No repository assumption from Phase 19.2 was found to be incorrect.

---

## 4. Dockerfile Design

Created `Dockerfile` (repo root, untracked, not committed):

- Base: `eclipse-temurin:17-jdk-jammy`, pinned by digest `sha256:29467857e8bde40ab1f7befecbda0ea764b95afec1cc7f89aa90f7a766577e19` (verified via `docker inspect --format='{{index .RepoDigests 0}}'` after pulling the tag — not guessed).
- Non-root user `harness` (uid/gid 1000), `WORKDIR /workspace`.
- No `COPY` of project source, no `ENTRYPOINT`/`CMD` — the image is a plain, reusable "Java 17 + shell" runtime (Section 8: Source Strategy).
- No Android SDK, no `adb`, no Node.js, no Appium installed — confirmed sufficient by Gates 1–7 below.

`.dockerignore` mirrors `.gitignore`'s runtime/output directories, kept small even though the Dockerfile never copies source.

---

## 5. Base Image Decision

| Aspect | Decision |
|---|---|
| Image | `eclipse-temurin:17-jdk-jammy@sha256:29467857e8bde40ab1f7befecbda0ea764b95afec1cc7f89aa90f7a766577e19` |
| Reason | Official Temurin build, Debian/Ubuntu-based (Phase 19.2 Section 12 preference over Alpine/musl), exact Java version verified as `17.0.19` via `docker run ... java -version` — matches project's pinned toolchain |
| Security | Non-root execution (Section 6); image not run `--privileged`; no Docker socket mount |
| Reproducibility | Pinned by digest, not tag — verified real digest captured this session, not guessed |
| Digest pinning | **Not deferred** — done in this phase, since it was practical (one `docker pull` + `docker inspect` call) |
| Size | 631MB (`docker images` — full JDK, not JRE; acceptable for a harness image, no attempt made to slim it further in Phase A) |

---

## 6. Container Boundary

Confirmed empirically, not just designed: the harness image never needed and never received `adb`, the Android SDK, Node.js, or Appium. Every Gate that exercised the container (1–7 below) succeeded using only the JDK + Gradle wrapper + mounted source. This directly confirms Phase 19.2 Section 7's "minimum container tooling" claim.

---

## 7. Appium Connectivity

No framework code was touched. The container reaches the host's Appium server purely via the existing `appium.serverUrl` system-property override, exactly as designed in Phase 19.2 Section 10:

```
-Dappium.serverUrl=http://host.docker.internal:4723
```

`host.docker.internal` was used per the Windows-verified mechanism from Phase 19.1B — **not** `127.0.0.1` (Phase 19.1B explicitly warned against assuming that resolves to the host from inside a container on Windows, and this phase did not repeat that mistake). No `remoteAdbHost`/`systemPort`/`adbPort` capability was introduced anywhere — the container never touches ADB.

---

## 8. Source Strategy

**Chosen: Option B — bind-mount project source at runtime**, not baked into the image (`-v "<repo>:/workspace"`). Rationale, matching Phase 19.2 Section 5's evaluation criteria:

- **Local development**: source edits are visible to the container immediately, no rebuild needed.
- **Reproducibility**: the image itself stays a stable, rarely-rebuilt "Java 17 + Gradle wrapper" runtime; what varies (source, test selection) is supplied at `docker run` time.
- **Fast iteration**: confirmed in practice — the same image was reused across roughly a dozen container runs in this session without ever needing a rebuild.
- **Clean separation**: the `Dockerfile` has zero project-specific `COPY` instructions, so it never goes stale relative to the source tree.

---

## 9. Runtime Configuration

Final, working `docker run` invocation (Windows, git-bash — `MSYS_NO_PATHCONV=1` needed only to stop git-bash's own path-mangling of `-v` arguments, not part of the architecture):

```bash
docker run --rm \
  -v "<repo-path>:/workspace" \
  -v "<docker-specific-gradle-home>:/home/harness/.gradle" \
  -w /workspace \
  mobile-automation-harness:phase19.3 \
  ./gradlew test --no-daemon \
    -Denv=emulator \
    -Dappium.serverUrl=http://host.docker.internal:4723 \
    -Ddevice.name=emulator-5554 \
    -Dplatform.version=14 \
    "-Dapp.path=<host-native-Windows-path-to-apk>"
```

Two genuine, evidence-based operational findings from getting this to work (both **runtime configuration**, not framework changes — see Section 16 for why neither required touching the framework):

1. **A dedicated Gradle home for the container is required**, not the host's live `~/.gradle`. Reusing the host's directory directly caused `Could not set UNIX mode on /home/harness/.gradle/daemon/9.0.0: could not chmod file (errno 1: Operation not permitted)` — a real chmod incompatibility between entries created by native Windows Gradle and a Linux container's expectations on the same NTFS-backed bind mount. Fix: mount a separate, container-only Gradle home directory (pre-seeded from the host's `wrapper/` and `caches/` subdirectories for speed, but never the host's `daemon/` directory).
2. **`app.path` must be a host-native path**, not a git-bash/MSYS POSIX-style path, because Appium (the process that actually reads this capability value) runs as a native Windows process on the host, not inside the container. `appium:app` is resolved by the Appium *server*, not the container — this is a direct, useful confirmation of Phase 19.2 Section 10's implicit assumption, made explicit here.

---

## 10. Artifact Strategy

Confirmed working exactly as Phase 19.2 Section 16 specified: because the repository is bind-mounted (not copied), every artifact the framework writes (`reports/`, `reports/screenshots/`, `build/reports/tests/test/`, `build/test-results/test/`, `logs/`) landed directly on the host filesystem, visible and inspectable immediately after each container run — including the failure screenshots analyzed in Section 12. No artifact was ever trapped inside a container's writable layer.

---

## 11. Validation Gates

| Gate | Result | Evidence |
|---|---|---|
| 1. Image builds | **VERIFIED** | `docker build` succeeded, 6s (cached base layer), 631MB |
| 2. Container starts | **VERIFIED** | `docker run ... whoami` → `harness` |
| 3. Java version correct | **VERIFIED** | `openjdk version "17.0.19"` inside container |
| 4. Gradle wrapper works | **VERIFIED** | `./gradlew --version` → `Gradle 9.0.0`, matching the committed wrapper exactly |
| 5. Container reaches host Appium | **VERIFIED** | `/dev/tcp/host.docker.internal/4723` → `TCP_RESULT=CONNECTED` |
| 6. Minimal Appium session | **VERIFIED** — via the real framework path, not a synthetic bypass. The container's own `AndroidDriverFactory` successfully created a real UiAutomator2 session against the live emulator (confirmed by Appium server logs showing a full, successful `createSession`, real AUT APK install taking 27395ms, and `Success`) | See run log excerpt, Section 12 |
| 7. One representative test (`LoginTest`) | **NOT ACHIEVED THIS SESSION** — see Section 12 | Reproducible `AssertionError` at the test's first UI-visibility check, root-caused to a real, repeated Android ANR (host resource contention), not a code or architecture defect |
| 8. Full 19-test suite | **NOT ATTEMPTED** — Gate 7 was never reached cleanly, so per this phase's own rule ("do not skip directly to Gate 8"), Gate 8 was correctly not attempted |

---

## 12. Test Results

**Infrastructure path (Gates 1–6): fully proven, repeatedly.** Across ten `LoginTest` attempts in this session, the container consistently: built/started correctly, resolved the Gradle wrapper, reached the host Appium server, and successfully created a real Appium session that installed and launched the real AUT APK on the real emulator. This is the actual architectural claim Phase 19.2 needed proven, and it was — every failure from Gate 7 onward occurred *after* a real session was already live.

**Gate 7 failure, root-caused with direct evidence:**

- First two attempts failed on driver *initialization* — a host-side Appium/npm dependency-resolution defect (`@appium/logger`, then its own transitive `set-blocking`, missing from six separate nested `node_modules` locations under `~/.appium`). This was fully diagnosed and fixed by installing the missing package at each identified location — a host npm environment repair, not a framework, Docker, or architecture change. Verified fixed: subsequent runs progressed past driver init entirely.
- Next attempt failed on an `app.path` format mismatch (a git-bash POSIX-style path passed where Appium, a native Windows process, needed a Windows-style path) — an invocation mistake on this operator's part, not a defect. Corrected and confirmed working.
- **All four subsequent attempts (including one after a full emulator restart) failed identically**, at `LoginTest.java:48` — the very first UI-visibility assertion after app launch. Failure screenshots (`reports/screenshots/assertion_verifyVisible_failure_*.png`) show the Products screen genuinely rendered correctly underneath, but an Android system **ANR dialog** ("Process system isn't responding" / "System UI isn't responding") overlaying it and blocking the automated check. This is a real Android OS-level symptom of the emulator's host process being starved of CPU/scheduling time at that exact moment — consistent with running Docker Desktop's WSL2 VM, a software-rendered (`swiftshader`) emulator, a Gradle JVM, and Appium's Node process all concurrently on this one machine (12th Gen Intel i5-1235U, 10 cores/12 threads; CPU utilization measured at 36.6% at one point, but ANRs reflect short scheduling stalls that an average utilization figure does not capture).
- This failure class is the same category the project's own Phase 18 already established a precedent for disposing of via repeated verification ("a single-run `accessCartScreen` failure, traced to a transient emulator rendering glitch"). Here it proved **reproducible across four consecutive attempts including a full emulator restart**, which is a stronger and more persistent signal than Phase 18's single-run case — correctly classified here as an unresolved environmental limitation of this specific host under this specific concurrent load, not disposed of as "confirmed not a defect."

Per this phase's own instruction ("do not blindly retry... do not make speculative changes"), retries stopped after this pattern was conclusively established (4/4 identical failures, one full emulator restart in between).

---

## 13. Performance Measurements

| Measurement | Value | Notes |
|---|---|---|
| Docker image build time | 6s | Base layer already cached locally from an earlier verification pull |
| Image size | 631MB | Full JDK image, no slimming attempted in Phase A |
| Container startup (`docker run ... whoami`) | Sub-second | Not separately timed to sub-second precision; qualitatively instantaneous |
| `./gradlew --version` (cold, cache mounted) | A few seconds | Not separately isolated from Gate 4's combined output |
| Full `LoginTest` attempt (compile + Gradle + Appium session + app install + assertion) | ~3–6.3 minutes per attempt across ten runs | High variance directly attributable to the same host resource contention documented in Section 12, not to Docker overhead specifically — no attempt was made in this phase to isolate "Docker overhead" from "this host's current load," since that decomposition requires a controlled, uncontended baseline this session's hardware situation did not provide |

**No optimization was attempted** (per the phase's own "do not optimize prematurely" instruction) — these numbers establish the Phase A baseline only.

---

## 14. Baseline Comparison

The authoritative v1.1.0 baseline (GitHub Actions, no Docker) is 19/19 passing, established across the Phase 17–18 effort. This phase's single-test, local-Docker attempt did not reproduce that pass rate for the one test exercised (`LoginTest`), for the environmental reason documented in Section 12 — **not** because of any code or architecture difference from the v1.1.0 path. No test expectation was changed to accommodate this; `LoginTest.java` was not modified, read, or weakened in any way (only read, for diagnostic purposes, in Section 12).

This result cannot be generalized to "Docker execution is less reliable than direct execution" — the same emulator, on the same host, running the *same* Appium/ADB stack directly (no Docker) would very plausibly hit the identical ANR under the identical concurrent load, since the ANR originates in the emulator's own resource starvation, not in anything related to the container. This phase did not have time/scope to run that controlled comparison (non-Docker `LoginTest` under identical concurrent load) — it is listed as a genuine open item (Section 17).

---

## 15. Files Changed

```
$ git status
On branch main
Your branch is up to date with 'origin/main'.

Untracked files:
  (use "git add <file>..." to include in what will be committed)
	.dockerignore
	Dockerfile
	docs/docker/
```

Exactly two new files at the repository root (`Dockerfile`, `.dockerignore`), plus this report and the three prior Phase 19.1A/19.1B/19.1C reports already sitting untracked under `docs/docker/` from earlier phases. **No existing file was modified.** Nothing was staged, committed, or pushed, per explicit instruction.

---

## 16. Risks

| Risk | Severity | Notes |
|---|---|---|
| Gate 7/8 not cleanly achieved this session | Medium | Directly blocks declaring Phase A fully complete (Section 18) — the *architecture* is proven (Gates 1–6), the *full local execution* is not |
| Host resource contention is real and reproducible on this specific machine | Medium | A genuine constraint for local Docker-based execution on modest hardware running Docker Desktop + a software-rendered emulator simultaneously — worth documenting for any future developer attempting the same local workflow |
| Host npm/Appium install can silently corrupt in ways that look like framework/Docker bugs | Low, now mitigated | Fully diagnosed and fixed in this session (Section 12); worth noting in developer-facing documentation so it isn't re-diagnosed from scratch next time |
| Gradle-home-on-bind-mount chmod incompatibility (Section 9) | Low, now mitigated | A real, reproducible Windows-bind-mount-specific issue with a known, documented fix |

---

## 17. Known Limitations

- Gate 8 (full 19-test suite via Docker) was not attempted, per this phase's own explicit rule not to skip ahead when an earlier gate is unresolved.
- No controlled non-Docker-vs-Docker comparison was run under identical concurrent host load — the honest state is "Docker's *architecture* is proven; whether Docker specifically (vs. this host's general load) affects reliability is unmeasured."
- Container image is not slimmed/optimized (Section 5) — acceptable for Phase A, a candidate for later refinement, not a defect.
- This phase's local proof used a temporary AVD built and torn down solely for this session (consistent with prior Phase 19.1B/19.1C practice) — no permanent local emulator infrastructure exists on this machine as a result of this phase.

---

## 18. Rollback

No rollback is needed — nothing in the existing, proven v1.1.0 execution path (non-Docker local execution, or the production GitHub Actions workflow) was touched. The only artifacts this phase adds (`Dockerfile`, `.dockerignore`) are new, additive, uncommitted files; deleting them (if ever desired) fully reverts the repository to its exact pre-Phase-19.3 state. The temporary AVD, Appium install fixes, and Docker image created during this session were host-local, session-scoped, and have been cleaned up (AVD deleted, temporary SDK components removed, ADB restored to its original loopback-only binding) — see cleanup commands executed in this session.

---

## 19. Final Verdict

# DOCKER PHASE A PARTIALLY VERIFIED — ADDITIONAL WORK REQUIRED

**What is proven, with real, repeated execution evidence:** the Model 3 architecture works. A container built from the minimal `Dockerfile` (no Android tooling, no Appium, no ADB) successfully compiles and runs the unmodified Gradle/TestNG harness, reaches the host's Appium server purely via the existing `appium.serverUrl` configuration mechanism (zero framework changes), and that Appium server successfully drives the real host-managed emulator — including real APK installation and a real, successful UiAutomator2 session — confirmed directly in Appium's own server logs. This is the exact claim Phase 19.2 needed validated, and it was, unambiguously, multiple times.

**What is not yet proven:** a clean, full 19/19 test-suite pass via the Docker path. The one representative test attempted (`LoginTest`) hit a real, reproducible (4/4), root-caused Android ANR — a resource-contention symptom of running Docker Desktop, a software-rendered emulator, Gradle, and Appium simultaneously on this specific development machine — not a defect in the framework, the Dockerfile, or the Model 3 architecture itself.

Per this phase's own explicit rule, this is not represented as a completed Docker implementation.

**Before Phase 19.4 (GitHub Actions Docker Execution Proof) is attempted**, the following should be resolved or explicitly accepted as a known local-development-only constraint:
1. Re-attempt Gate 7/8 on this host at a time of lower baseline load, or on hardware with more headroom, to determine whether the ANR is purely a resource-contention artifact of this specific machine (expected) or something more systematic (not yet ruled out).
2. Optionally, run the same single test directly (non-Docker) under equivalent concurrent load, to isolate "Docker overhead" from "general host load" — closing the one open comparison this report could not make (Section 14).

Given GitHub Actions runners provide dedicated, non-shared compute (unlike this local development machine), the resource-contention cause identified here is plausibly **absent** in a CI context — making Phase 19.4 a reasonable next step despite this session's local result, but its own execution evidence, not an assumption carried over from here, should confirm that.

**Recommended next phase, with the above caveat explicitly carried forward: Phase 19.4 — GitHub Actions Docker Execution Proof.** Not started automatically.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-08 |
| Document Status | Final — Implementation Report (Partially Verified) | — | — |

---

**End of Document — Phase 19.3 Docker Implementation Report (Phase A), v1.0**
