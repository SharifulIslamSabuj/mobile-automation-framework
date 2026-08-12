---
document_id: PHASE-19.1A
title: Docker-to-Host Emulator ADB Connectivity Spike
version: v1.0
status: Final — Technical Spike Report (No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [MA-CICD-001, MA-CICD-002, PHASE-17_FINAL, PHASE-18]
classification: Internal
---

# Phase 19.1A — Docker-to-Host Emulator ADB Connectivity Spike

**Mobile Automation Framework**

No code was written, no file outside this report was created or modified, and no Docker artifact (Dockerfile, docker-compose.yml) exists as a result of this phase. This is research and repository-code-tracing only.

---

## 1. Objective

Remove uncertainty from exactly one question before any Docker architecture is designed: **how does Appium — running inside a container — reach an Android emulator that remains outside the container, on the host, on both of this project's real target environments (Windows + Docker Desktop locally, and the GitHub-hosted Ubuntu runner in CI)?**

---

## 2. v1.1.0 Baseline (What Already Works, Not to Be Destabilized)

The current, verified, 19/19-passing CI baseline runs the emulator and Appium server both directly on the GitHub-hosted runner (no containers involved) — `.github/workflows/mobile-automation.yml`, confirmed via `docs/ci/PHASE_17_FINAL_CI_BASELINE_QUALIFICATION_REPORT.md`. Appium listens on `http://127.0.0.1:4723` (the framework's own default, `config.properties` → `appium.serverUrl`). This spike does not touch that file, that workflow, or any test.

---

## 3. Repository Findings (Traced, Not Inferred)

### 3.1 How the Appium server URL is configured

`ConfigReader.getAppiumServerUrl()` (`src/main/java/.../config/ConfigReader.java:138-139`) reads the `appium.serverUrl` key via the framework's existing four-tier precedence (system property → environment file → common file → compiled default). The compiled default is `http://127.0.0.1:4723` (`ConfigurationDefaults.DEFAULT_APPIUM_SERVER_URL`). This value is **already externally overridable** via `-Dappium.serverUrl=...` at Gradle invocation time — no code change is required to point the framework at an Appium server running somewhere other than `127.0.0.1`.

### 3.2 How the driver connects to Appium — and, critically, how it does *not* touch ADB directly

`AndroidDriverFactory.createDriver()` (`src/main/java/.../driver/AndroidDriverFactory.java:36-49`) does exactly one thing to reach the device: `new AndroidDriver(serverUrl, capabilities)`. This is a plain HTTP/WebDriver-protocol client pointed at the Appium server's URL. Traced across the entire framework:

```
grep -rn "\"adb\"\|ProcessBuilder\|Runtime.getRuntime().exec" src/main/java src/test/java
→ zero matches
```

**The Java test harness never shells out to `adb`, anywhere, for any reason.** Every device interaction — including `ScrollUtility`'s `androidUIAutomator` expressions — is issued through the Appium HTTP API, which Appium's own server-side UiAutomator2 driver translates into ADB calls internally. This is the single most consequential finding of this spike: **ADB is entirely Appium's concern, not the Java harness's.** Whatever runs the Java/Gradle/TestNG code only ever needs plain HTTP reachability to wherever the Appium server's port is — it does not need `adb`, the Android SDK, or any device tooling of its own.

### 3.3 How device discovery / capabilities are resolved

`CapabilityConfiguration.fromConfigReader()` (`src/main/java/.../config/CapabilityConfiguration.java:40-55`) and `UiAutomator2CapabilityBuilder.build()` (`src/main/java/.../driver/UiAutomator2CapabilityBuilder.java`) assemble `platformName`, `platformVersion`, `deviceName`, `udid`, `appPackage`, `appActivity`, `appPath`, `noReset`/`fullReset`, `autoGrantPermissions`, and the new-command timeout — all sourced from `ConfigReader`, all already overridable via system property (exactly the mechanism `-Dplatform.version=...`/`-Ddevice.name=...` already use in the existing CI workflow). **No field for a remote-ADB-server capability exists today** — confirmed by reading the full class (Section 3.3 below expands on what this means for future implementation).

### 3.4 Where device/emulator identity is resolved

`ConfigReader.getDeviceName()` / `getPlatformVersion()` / `getUdid()` resolve from `device.name` / `platform.version` / `device.udid`, both intentionally blank in the committed `config-real-device.properties` and `config-emulator.properties` — supplied at execution time. The existing CI workflow already supplies `-Ddevice.name=emulator-5554` this way. No change to this mechanism is implied by containerizing the harness.

**Conclusion of Section 3:** the container boundary this spike must solve for is entirely between **wherever Appium's server process runs** and **the host's ADB/emulator** — never between the Java test code and ADB directly.

---

## 4. Container Boundary Analysis

Given Section 3's finding, two distinct container boundaries are possible, not one:

- **Boundary A (the direction stated for this phase):** Appium server *and* the Java/Gradle/TestNG harness both run inside the container; only the emulator (and therefore ADB's device-facing side) stays on the host.
- **Boundary B (an alternative this spike's own evidence surfaces):** only the Java/Gradle/TestNG harness runs inside the container; Appium server stays on the host, next to the emulator, exactly as it does today in CI. The container would then need only plain HTTP reachability to the host's port 4723 — no ADB, no Android SDK, no device tooling of any kind inside the container.

This report evaluates Boundary A, since that is this phase's stated direction, but Boundary B is evidence-backed and lower-risk, and is recorded here rather than discarded — see Section 10 and Section 19.

---

## 5. Minimum Required Android Tooling (Boundary A: Appium runs inside the container)

| Component | Required? | Why |
|---|---|---|
| `sdkmanager` | **No** | Nothing inside the container manages SDK package installation — the emulator, its system image, and the AVD are entirely host-owned |
| Android SDK command-line tools | **No** | Same reasoning — no SDK component management happens in the container |
| `platform-tools` (specifically the `adb` binary) | **Yes** | Appium's UiAutomator2 driver (`appium-adb`) shells out to a local `adb` executable regardless of whether that executable is pointed at a local or a remote ADB server (Section 8) — the binary itself must be present in the container's `PATH` even when redirected via `remoteAdbHost` |
| `emulator` binary | **No** | The emulator process itself never runs in the container under Boundary A |
| Android system images | **No** | Same reasoning |
| Java 17 | **No** | Java/Gradle/TestNG are not part of the Appium-server-only container under strict Boundary A; if the harness (Section 4, Boundary A includes "Tests should run inside Docker" per this phase's stated direction) also runs Java/Gradle in the *same* container, then yes — see note below |
| Gradle | Only if the test harness shares this container (this phase's stated direction says it does) | Needed to compile and run the suite |
| Node.js | **Yes** | Appium server itself is a Node.js application (already true for the host-based v1.1.0 workflow) |
| Appium (server) | **Yes** | This is the component whose placement inside the container is exactly what this phase specifies |
| UiAutomator2 driver | **Yes** | Required by Appium to drive an Android target at all |

**Recommendation: Option B (platform-tools/`adb` only) plus Node.js/Appium/UiAutomator2/Java/Gradle — never the full Android SDK, never `sdkmanager`, never system images, never the `emulator` binary.** Including any of those would mean the container silently duplicates emulator-lifecycle capability it will never use under this architecture, and would misleadingly suggest the container could someday manage its own emulator — a decision this phase's own direction explicitly defers (Section 8, "emulator remains OUTSIDE Docker").

---

## 6. Windows Connectivity Model

**Environment:** Windows host → Docker Desktop (WSL2 backend) → container → Android emulator (running as a Windows process, e.g. via Android Studio or a standalone AVD launch, on the Windows host).

Verified externally (search results cited in Section 20):

- **`host.docker.internal` is a real, working Docker Desktop feature** on Windows and Mac — a DNS name the Docker Desktop networking layer resolves to the host machine's IP, reachable from inside any container without extra configuration. This is *not* a Linux Docker feature by default (Section 7 below).
- **ADB's port forwarding — and, more importantly, the emulator's own ADB-facing port — binds to `127.0.0.1` (loopback) only, by default.** This is documented, longstanding ADB behavior, not specific to this project. Practically: even with `host.docker.internal` correctly resolving to the Windows host's IP, a plain `adb connect host.docker.internal:5555` from inside the container will **not** reach the emulator's adb port unless something on the host explicitly rebinds it to listen on all interfaces.
- **The documented, supported mechanism to fix this is on the ADB *server* side, not the emulator's device-facing port:** starting the host's ADB server with `adb -a nodaemon server start` makes the ADB **server** (port 5037 by default) listen on all interfaces instead of just loopback. A container's `adb` client can then be pointed at that host ADB server (which already has the emulator registered as a connected device) via `adb -H <host> -P <port>` or equivalently the `ANDROID_ADB_SERVER_ADDRESS`/`ANDROID_ADB_SERVER_PORT` environment variables — reaching the emulator *through* the host's already-running ADB server, not by contacting the emulator's own adbd port directly.
- **Appium has an official, documented capability for exactly this: `appium:remoteAdbHost`** — "the address of the host where ADB is running," used together with `systemPort` and `adbPort` capabilities. This is the correct, supported integration point for Boundary A — not a workaround this project would be inventing.

### Windows Model Summary

```
Windows Host
 ├── Android Emulator (adb-managed device, adbd port bound to 127.0.0.1 by default)
 └── ADB Server (host), started with `-a` → now listening on 0.0.0.0:5037
       │
       │  (Docker Desktop's host.docker.internal DNS resolution)
       ▼
Docker Desktop (WSL2) → Container
 └── Appium server, configured with appium:remoteAdbHost = host.docker.internal,
     systemPort / adbPort set per Appium's documented requirement for this capability
```

---

## 7. GitHub Actions Connectivity Model

**Environment:** GitHub-hosted Ubuntu runner (Linux, native Docker — not Docker Desktop) → emulator and its host-side ADB server run directly on the runner (exactly as v1.1.0 already does) → a container is started as an additional step in the same job.

Verified externally (Section 20):

- **`host.docker.internal` does *not* resolve automatically on native Linux Docker** (confirmed by multiple GitHub Actions community reports of exactly this failure) — this is the Docker-Desktop-specific convenience this project must **not** assume carries over, per this phase's own instruction.
- **Two documented, supported alternatives exist on Linux:**
  1. `docker run --network=host ...` — the container shares the runner's network namespace entirely. `localhost:5037` (or whatever port the host's ADB server listens on) is then reachable from inside the container exactly as if the container weren't there at all. This is the simpler of the two options and is fully supported on GitHub-hosted Linux runners (they are ordinary Linux hosts with a normal Docker Engine).
  2. `docker run --add-host=host.docker.internal:host-gateway ...` — makes `host.docker.internal` resolve on Linux too, for cases where `--network=host` is undesirable (e.g., if the container's own ports must stay isolated for some other reason).
- **GitHub's own service-container feature (`jobs.<job>.services`) is a separate mechanism** (containers managed by the workflow YAML itself, reachable via `localhost:<port>` when the job itself runs directly on the runner) and does not apply here, since the emulator is a host **process**, not a service container — noted so it is not confused with the mechanism actually needed.

### GitHub Actions Model Summary

```
GitHub-Hosted Ubuntu Runner (Linux)
 ├── Android Emulator (exactly as v1.1.0 today)
 └── ADB Server (host), started with `-a` → listening on 0.0.0.0:5037
       │
       │  (docker run --network=host, OR --add-host=host.docker.internal:host-gateway)
       ▼
Container (started as an additional workflow step)
 └── Appium server, appium:remoteAdbHost = localhost (with --network=host)
     or host.docker.internal (with --add-host)
```

**This is a materially simpler and more robust path than the Windows case** — `--network=host` has no Docker-Desktop-style VM boundary to cross on a native Linux runner.

---

## 8. ADB Connection Models — Evaluated

### Model 1 — Host Emulator → Host ADB Server → Container ADB Client → Appium

Container's `adb` binary is configured (`remoteAdbHost` / `ANDROID_ADB_SERVER_ADDRESS`) to talk to the **host's** ADB server, which already owns the connection to the emulator.

- **Network path:** Container → (Docker networking, per Section 6/7) → host ADB server (port 5037, rebound with `-a`) → emulator (already connected to that same host server).
- **Required ports:** Host ADB server port (default 5037, or a custom port via `ANDROID_ADB_SERVER_PORT`), reachable from the container.
- **ADB ownership:** Host. The container never runs its own ADB server — it acts purely as a client of the host's.
- **Appium ownership:** Container (per this phase's stated direction).
- **Advantages:** Uses ADB's own designed-for-this-purpose remote-server mechanism; no need to expose the emulator's own adbd port (5555) at all, which is the port most resistant to rebinding cleanly; one host-side ADB server remains the single source of truth for device state, avoiding any "two ADB servers, one device" conflict.
- **Disadvantages:** Requires the host's ADB server to be deliberately started with `-a` — a step outside the container that must be added to whatever process boots the emulator; requires Appium's `remoteAdbHost`/`systemPort`/`adbPort` capabilities, which are not modeled anywhere in this framework's `CapabilityConfiguration` today (Section 3.3) — a small, additive implementation gap, not a redesign.
- **Windows feasibility:** Yes, via `host.docker.internal` (Section 6).
- **GitHub Actions feasibility:** Yes, more simply via `--network=host` (Section 7).

### Model 2 — Host Emulator → Container ADB (own server) → Appium

The container runs its **own** ADB server, which attempts to `adb connect` directly to the emulator's own adbd port (5555 for `emulator-5554`).

- **Network path:** Container's own ADB server → emulator's adbd port directly.
- **Required ports:** The emulator's adb port (5555), which — per Section 6/7's verified evidence — is bound to loopback only by default and is not the officially-documented remote-access mechanism the way the ADB *server* port is.
- **ADB ownership:** Split — a second, independent ADB server inside the container, alongside whatever ADB server may already exist on the host. This is a well-known source of "device offline"/"multiple adb servers" conflicts (Section 14).
- **Advantages:** None identified over Model 1 that this spike's evidence supports.
- **Disadvantages:** Requires rebinding the emulator's own adbd-facing port specifically (less standard than rebinding the ADB *server* port); risks two independent ADB servers both claiming the same device.
- **Windows feasibility:** Unverified/unlikely without the same port-rebinding problem Section 6 already flags, applied to a less-supported port.
- **GitHub Actions feasibility:** Same caveat.
- **Not recommended** — Model 1 achieves the same outcome through ADB's own documented remote-server mechanism instead.

### Model 3 — Host Emulator → Host ADB Server → Host Appium → Container (Tests Only)

Appium stays on the host next to the emulator (exactly as v1.1.0 already runs it); only the Java/Gradle/TestNG harness moves into the container.

- **Network path:** Container → host Appium's HTTP port (4723) only. No ADB involvement in the container at all — confirmed directly by Section 3.2's code tracing (the Java harness never touches ADB).
- **Required ports:** Just 4723 (already the framework's own default), reachable from the container via the exact same `host.docker.internal` (Windows) / `--network=host` (Linux) mechanisms already described.
- **ADB ownership:** Host, entirely — unchanged from v1.1.0.
- **Appium ownership:** Host, entirely — unchanged from v1.1.0.
- **Advantages:** Zero new ADB-remoting complexity; zero new Appium capabilities to add; the container needs no Android tooling whatsoever (not even `platform-tools`) — only Java 17 and Gradle; the emulator/Appium pairing that is already proven at 19/19 in CI is left completely untouched, and the container is purely an *additional* execution path, not a parallel Appium/ADB stack that could drift from it.
- **Disadvantages:** Does not fully match this phase's stated direction ("Appium should run inside Docker"); container-based execution still depends on a host-managed Appium process being started first (true of Model 1 and Model 2 as well, since the emulator itself is always host-managed under every model this phase considers).
- **Windows feasibility:** Yes — simpler than Model 1, since only an HTTP port needs to be reachable, not an ADB server.
- **GitHub Actions feasibility:** Yes — same simplification applies.

### Model 4 — Another Architecture

Not identified as necessary. Every architecture this phase's own direction and this project's real constraints (KVM/emulator must stay on the host, per the prior architectural review) reduce to some arrangement of "where does Appium's server process live," which Models 1–3 already cover exhaustively.

---

## 9. Model Comparison

| | Model 1 (Container Appium, remote ADB) | Model 2 (Container Appium, own ADB) | Model 3 (Host Appium, container tests only) |
|---|---|---|---|
| Matches this phase's stated direction | Yes | Yes | No |
| Container needs `adb` binary | Yes | Yes | No |
| Container needs any Android tooling | `platform-tools` only | `platform-tools` only | None |
| New Appium capabilities required | Yes (`remoteAdbHost`, `systemPort`, `adbPort`) | Yes (same) | None |
| Risk of dual-ADB-server conflicts | Low (single host ADB server remains authoritative) | Higher (two independent ADB servers) | None (no ADB in container) |
| Windows verified mechanism | `host.docker.internal` + `adb -a` on host | Unverified/discouraged | `host.docker.internal` (HTTP only) |
| GitHub Actions verified mechanism | `--network=host` + `adb -a` on host | Unverified/discouraged | `--network=host` (HTTP only) |
| Framework code changes implied | `CapabilityConfiguration` gains 3 optional fields | Same | None |

---

## 10. Recommended Connection Architecture

**Model 1**, if this phase's stated direction (Appium inside the container) is to be honored. It is the only one of the two directionally-compliant options (Model 1 vs. Model 2) with a documented, supported ADB mechanism behind it (the ADB server's own `-a`/remote-server design, plus Appium's own `remoteAdbHost` capability) rather than an unsupported attempt to reach the emulator's own loopback-bound port directly.

**However, this spike's own evidence (Section 3.2, Section 9) supports recording Model 3 as a lower-risk alternative for the actual v1.2.0 implementation decision** — it achieves full harness reproducibility (the part of "containerize for reproducibility" that actually matters, since JDK/Gradle/Node/Appium version drift is the real problem, not where ADB happens to run) with zero new ADB-remoting surface area, zero new Appium capabilities, and zero risk to the proven Appium/ADB pairing already verified at 19/19. This is presented as evidence, not a decision this spike is authorized to make — Phase 19.2's architecture design should weigh Model 1 against Model 3 explicitly, informed by this comparison, rather than defaulting to Model 1 solely because it was this phase's starting assumption.

---

## 11. Required Ports

| Port | Owner | Purpose | Verified? |
|---|---|---|---|
| 4723 | Host (Model 3) or Container (Model 1/2) | Appium server HTTP API — already this framework's own default (`config.properties`) | Yes — repository evidence |
| 5037 | Host, rebound with `adb -a` (Model 1) | ADB server, made reachable to the container | Yes — externally verified (Section 20) |
| 5554 / 5555 | Host (emulator console / adb, `emulator-5554` per the existing CI workflow's `EMULATOR_DEVICE_NAME`) | Emulator's own device ports — not directly exposed to the container under Model 1 (reached indirectly via the host's rebound ADB server) | Yes — consistent with the already-verified v1.1.0 workflow |
| 8021 | Container (Model 1), if `systemPort` is used | UiAutomator2 server port, referenced alongside `remoteAdbHost` per Appium's own documentation | Externally verified as Appium's documented default; not yet exercised in this project |

No port value here is invented — each is either already present in this repository's own verified configuration or is drawn directly from the external sources cited in Section 20.

---

## 12. Required Environment Variables

| Variable | Where | Purpose | Status |
|---|---|---|---|
| `appium.serverUrl` | Container (Gradle `-D` override) | Already-existing framework config key; would point at `http://<container-Appium-host>:4723` if Model 3, or `http://127.0.0.1:4723` if Appium runs in the same container as the tests (Model 1) | Already implemented, no change needed |
| `ANDROID_ADB_SERVER_ADDRESS` / `ANDROID_ADB_SERVER_PORT`, or the `adb -H`/`-P` flags | Container, if Appium's own local `adb` client needs to be redirected (Model 1) | Points the container's `adb` binary at the host's rebound ADB server | Externally verified mechanism; not yet configured anywhere in this project |
| `appium:remoteAdbHost` / `appium:systemPort` / `appium:adbPort` | Appium session capabilities (Model 1 only) | Told to Appium at session-creation time, not a shell environment variable — would require new fields in `CapabilityConfiguration` (Section 3.3) | Not yet implemented — an identified, additive future change |

---

## 13. Version Considerations

| Component | Recommendation | Basis |
|---|---|---|
| Java | 17 (Temurin) | Already pinned project-wide (`build.gradle`, CI workflow) — no reason to diverge for a container |
| Gradle | 9.0.0 | Already wrapper-pinned; the container would invoke the same committed `gradlew`, not a separately-chosen version |
| Node.js | Match whatever is bundled with the chosen base image, or pin explicitly — **not yet decided** | The current CI workflow relies on the GitHub-hosted runner's preinstalled Node (verified 22.23.1 in Phase 17.2A); a container base image will need its own explicit choice, which this spike does not select |
| Appium (server) | 3.6.0 | Already pinned and verified in the CI workflow (`APPIUM_SERVER_VERSION`) — reuse, don't re-decide |
| UiAutomator2 driver | 8.2.2 | Same reasoning — already pinned and verified |
| Android `platform-tools` (for the `adb` binary only, Model 1/2) | **Not yet pinned** | The CI workflow's runner image ships `platform-tools 37.0.0` (verified, Phase 17.2A), but a container image is a separate artifact with its own base image choice — marked here as an explicit **implementation decision for Phase 19.2**, not guessed at |

Per this phase's own instruction, any value not already established elsewhere in this project's verified history is marked above as an open implementation decision, not a number invented for this report.

---

## 14. Failure Modes

| Scenario | Symptom | Likely Cause | Diagnostic | Mitigation |
|---|---|---|---|---|
| Emulator unavailable | Appium session creation times out / connection refused | Emulator not yet booted, or not started at all before the container attempts a session | `adb devices` on the host before starting the container step | Sequence the workflow/local script so emulator boot-completion is confirmed (exactly as the existing `reactivecircus/android-emulator-runner` action already does) before any container step runs |
| ADB cannot connect (Model 1) | Appium logs show `adb` errors reaching the remote server | Host ADB server not started with `-a`, still loopback-only | `adb -H <host> -P 5037 devices` from inside the container | Confirm the host-side `-a` step ran before the container starts; this is a new, explicit prerequisite step this architecture introduces |
| Wrong emulator port | Container connects but sees no devices, or the wrong one | Multiple emulator instances, or a port assumption that doesn't match `emulator-5554`'s actual assigned ports | `adb -H <host> -P 5037 devices` | Single-emulator-instance discipline, already true of the existing workflow (MA-CICD-002 §5's single-job design) |
| `host.docker.internal` unavailable | DNS resolution failure inside the container | Running on native Linux Docker without `--add-host`, wrongly assuming Docker-Desktop behavior | `getent hosts host.docker.internal` (or equivalent) inside the container | Use `--network=host` on GitHub Actions (Section 7) instead of relying on `host.docker.internal` there |
| ADB server unavailable | Container's `adb` client cannot reach port 5037 at all | Host firewall, or the `-a` server process was never started, or crashed | `adb -H <host> -P 5037 version` | Health-check the host ADB server as an explicit pre-flight step before the container runs any test |
| Appium cannot see device | Session created but no device found | ADB reachable but the emulator was never registered with that specific ADB server instance (e.g., a second, unrelated ADB server got started) | `adb -H <host> -P 5037 devices` should list exactly one device | Ensure only one ADB server is ever running on the host in this configuration — starting a second one (e.g., accidentally, via a tool that auto-starts its own) is the classic cause |
| Multiple devices detected | Appium error: "more than one device/emulator" | A stray second AVD or physical device attached to the same ADB server | `adb -H <host> -P 5037 devices` | Explicit `udid`/`deviceName` capability (already how this framework selects a target today) resolves ambiguity once devices are visible; the ambiguity itself should still be avoided by not running more than one target |
| Device goes offline mid-run | Appium commands start failing partway through a suite | Emulator crashed, or the host ADB server restarted (losing the `-a` binding) mid-run | `adb -H <host> -P 5037 devices` (state column) | No different from the existing real-device/emulator failure mode this framework already tolerates via `RetryAnalyzer` at the test-method level — not a new class of flakiness this architecture introduces, but one it inherits |
| Container networking failure (generic) | Timeouts reaching any host port at all | Wrong flag chosen for the platform (`--network=host` attempted on Docker Desktop, where it does not behave the same as on Linux) | Attempt a trivial `curl http://<resolved-host>:4723/status` from inside the container first, before anything Appium-specific | Use the platform-specific mechanism from Section 6 (Windows) or Section 7 (GitHub Actions) — never assume they're interchangeable, per this phase's own instruction |
| Windows/Linux behavior divergence (generic) | Works in one environment, fails in the other, with no code difference | `host.docker.internal` vs. `--network=host` are genuinely different mechanisms with different availability (Section 6 vs. Section 7) | N/A — this is the core finding of this spike | Any future implementation must branch its networking configuration by platform, not share one script/compose file unmodified across both |

---

## 15. Security Considerations

- Binding the host's ADB server to all interfaces (`adb -a`) widens its exposure beyond loopback-only — on a shared or multi-tenant machine this would be a real concern; on a GitHub-hosted, single-job, ephemeral runner (already the case for this project's entire CI model) and a local Windows development machine behind its own OS/network firewall, the exposure is materially lower, but not zero. This should be explicitly weighed, not silently accepted, in Phase 19.2.
- No new secret or credential is implied by anything in this spike — ADB and Appium's remote-host capabilities discussed here are unauthenticated-by-default, consistent with how this project's existing local Appium server already operates.

---

## 16. v1.1.0 Compatibility

Every model evaluated in Section 8 is strictly **additive**:

- The existing `.github/workflows/mobile-automation.yml` is not referenced as needing any change by this spike, and none was made.
- The existing `appium.serverUrl` / `device.name` / `platform.version` / `app.path` configuration mechanism (already override-capable via system property) is the same mechanism any Docker-based execution path would reuse — not a parallel or replacement configuration system.
- The 19-test baseline, existing ExtentReports/log4j2 reporting, and existing real-device execution path are untouched by anything in this report — no source file, config file, or test file was modified.
- `CapabilityConfiguration`'s three potential new fields (Section 3.3, Section 12) would be purely additive (new optional fields with safe defaults matching today's blank/unset behavior), not a restructuring of the existing class.

---

## 17. Technical Risks

| Risk | Severity | Notes |
|---|---|---|
| Reliance on `adb -a` as a new, host-side manual/scripted step | Medium | This is a new prerequisite this architecture introduces that v1.1.0 never needed — it must be automated reliably in both the local Windows workflow and the GitHub Actions workflow, or the container path will be flaky in a way the host-only path never was |
| Windows/Docker Desktop nested-virtualization and networking behavior not independently reproduced in this session | Medium | This spike relied on external, cited documentation (Section 20) for Docker Desktop networking behavior — it was not verified by actually running a container against a real Windows-hosted emulator in this session. Flagged explicitly per this phase's "identify what was verified externally" rule. |
| `CapabilityConfiguration` gap | Low | A known, small, additive implementation item, not a design risk |
| Divergence between Windows and GitHub Actions networking configuration | Low, if documented | Both mechanisms are individually well-documented (Section 20); the risk is only in accidentally sharing one hardcoded config across both, addressed by Section 14's explicit guidance |

---

## 18. Implementation Prerequisites

Before Phase 19.2 (architecture design) proceeds on the assumption of Model 1:

1. A real, hands-on verification that `adb -a nodaemon server start` on a Windows host, combined with `host.docker.internal`, actually allows a container's `adb` client to see a Windows-hosted emulator — this spike found strong, consistent, multi-source documentation support for this, but did not execute it.
2. A real, hands-on verification of the equivalent on a GitHub-hosted runner using `--network=host`.
3. A decision on the `CapabilityConfiguration` additive fields (`remoteAdbHost`, `systemPort`, `adbPort`) — small, but real implementation work belonging to Phase 19.2 or later, not this spike.
4. An explicit decision between Model 1 (matches this phase's original direction) and Model 3 (lower-risk, evidence-backed alternative this spike surfaced) — Section 10 provides the comparison; the decision itself belongs to Phase 19.2.

---

## 19. Final Decision

# READY WITH OPEN TECHNICAL QUESTIONS

The core uncertainty this spike was commissioned to resolve — "how would Appium inside Docker reach the host emulator" — now has a specific, evidence-backed, documented answer (Model 1, via a host-side ADB server rebind plus Appium's own `remoteAdbHost` capability), and a credible lower-risk alternative (Model 3) was surfaced directly from this repository's own code rather than assumed. Neither is yet **hands-on verified** in this project's real environments (Section 18, items 1–2) — that verification, not further research, is the correct next step, which is why this is "Ready with open technical questions" rather than an unconditional "Ready."

**Recommended next phase:** Phase 19.1B — a narrowly-scoped, hands-on connectivity proof (start an emulator on the Windows host, rebind ADB with `-a`, run a bare `adb`-in-a-container connectivity test — no Appium, no Gradle, no test execution) to convert Section 18's open items into verified facts, before Phase 19.2 commits to a full Docker architecture design around either Model 1 or Model 3.

---

## 20. Sources Consulted (External Verification)

The following claims in this report were verified against external sources, not invented or assumed, per this phase's evidence rule:

- Docker Desktop `host.docker.internal` behavior on Windows/Mac: [Bright Coding — Run a Full Android Device Inside a Docker Container](https://www.blog.brightcoding.dev/2025/09/03/how-to-run-a-full-android-device-inside-a-docker-container), [Docker Community Forums — connect adb container to emulator container](https://forums.docker.com/t/how-to-connect-adb-container-to-emulator-container/143632)
- GitHub Actions container-to-host networking (`--network=host`, `--add-host=host.docker.internal:host-gateway`, and the distinction from service containers): [GitHub Docs — Communicating with Docker service containers](https://docs.github.com/actions/using-containerized-services/about-service-containers), [Docker Community Forums — host.docker.internal doesn't work with GitHub Actions Linux](https://forums.docker.com/t/host-docker-internal-seems-doesnt-work-with-ci-cd-github-action-linux/119558), [Dash0 — How to Access a Host Port from Inside a Docker Container](https://www.dash0.com/faq/how-to-access-a-host-port-from-inside-a-docker-container)
- ADB's default loopback-only binding, and the `-a`/`nodaemon server start` remote-server mechanism: [ADB Manual Page (Android Open Source Project)](https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/master/docs/user/adb.1.md), [ADB: Listen on all interfaces (gist)](https://gist.github.com/makr0/060108f35f6cf17fe43381302cf9b936), [Google Issue Tracker — Allow adb to bind to all interfaces](https://issuetracker.google.com/issues/36944754)
- Appium's `remoteAdbHost`/`systemPort`/`adbPort` capabilities: [appium-uiautomator2-driver README (GitHub)](https://github.com/appium/appium-uiautomator2-driver/blob/master/README.md), [Appium — Running tests on a device connected to a remote system (Medium)](https://praveendavidmathew.medium.com/appium-running-tests-on-a-device-connected-to-a-remote-system-uiautomator2-cfd64139a35)

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-08 |
| Document Status | Final — Technical Spike Report (No Implementation) | — | — |

---

**End of Document — Phase 19.1A Docker-to-Host Emulator ADB Connectivity Spike Report, v1.0**
