---
document_id: PHASE-19.1B
title: Docker-to-Host Android Emulator ADB Connectivity Proof
version: v1.0
status: Final — Technical Proof Report (No Implementation)
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.0, PHASE-19.1A]
classification: Internal
---

# Phase 19.1B — Docker-to-Host Android Emulator ADB Connectivity Proof

**Mobile Automation Framework**

No Dockerfile, docker-compose.yml, Java source, build.gradle, or GitHub Actions file was created or modified during this phase. No `remoteAdbHost`/`systemPort`/`adbPort` Appium capability was introduced anywhere. This report documents real, executed evidence only.

---

## 1. Objective

Prove, with real execution evidence rather than inference, whether a minimal Docker container can reach a host-managed Android emulator through the host's ADB server:

```
Docker Container → host.docker.internal → Host ADB Server (:5037) → Android Emulator
```

This is a connectivity proof only — no Appium, no Gradle, no test suite, no APK installation.

---

## 2. Phase 19.0 Environment Baseline

Verified at the start of this phase:

| Component | State |
|---|---|
| Docker Desktop | 4.69.0, healthy (recovered in Phase 19.0 from a stale-AF_UNIX-socket crash, [docker/desktop-feedback#460](https://github.com/docker/desktop-feedback/issues/460)) |
| Docker Engine | 29.4.0, healthy |
| `docker run --rm hello-world` | Succeeded |
| Local Android emulator infrastructure | **None** — no AVD, no system image, no `cmdline-tools` existed on this machine at phase start |
| Host ADB | Installed (platform-tools 36.0.2), no device attached |

Because no local emulator infrastructure existed, this phase first had to build a temporary one (Section 4) before any connectivity proof was possible.

---

## 3. Proof Scope

In scope: DNS resolution, TCP reachability, ADB server protocol connectivity, and read-only device queries (`get-state`, `getprop`) from a minimal container to a host-managed emulator.

Out of scope (and not touched): Dockerfile/docker-compose.yml, Java source, `build.gradle`, `.github/workflows/mobile-automation.yml`, Appium (server or capabilities), Gradle execution, the test suite, APK installation.

---

## 4. Host Environment

### 4.1 Temporary AVD construction

No AVD existed locally, so one was built matching the project's real, frozen CI pin (`ANDROID_API_LEVEL: 34`, `ANDROID_TARGET: google_apis`, `ANDROID_ARCH: x86_64`, `ANDROID_DEVICE_PROFILE: pixel` — from `.github/workflows/mobile-automation.yml`):

1. Downloaded `cmdline-tools` (official Google repository manifest, build 15859902 — the highest revision listed, not guessed).
2. Accepted SDK licenses non-interactively.
3. Installed `system-images;android-34;google_apis;x86_64` and `platforms;android-34` (~1.5GB download, verified via `sdkmanager --list_installed`).
4. Created AVD `phase19_1b_temp` via `avdmanager create avd -k "system-images;android-34;google_apis;x86_64" -d pixel`.
5. First boot attempt failed on a real, quantified disk-space check (`FATAL: Not enough space ... need 7372.80 MB`, only 2418MB free) — the machine's free disk had dropped from 16.8GB to ~2.4GB over the course of tooling downloads and Docker Desktop's WSL2 data disk growth. With the user's explicit approval, `docker image prune -a` reclaimed unused image layers, and the Docker Desktop WSL2 data disk (`docker_data.vhdx`, 20GB allocated) was compacted via `wsl --shutdown` + `diskpart compact vdisk` to actually return the reclaimed space to the Windows host filesystem — Docker's own `image prune` alone did **not** shrink the on-disk `.vhdx` file (a documented Docker-Desktop-on-Windows behavior, not a bug in this project). The AVD's `disk.dataPartition.size` and `sdcard.size` were also reduced (temporary AVD only, purely to fit available disk — not a project configuration change) since this proof does not install apps or run tests.
6. Second boot succeeded.

### 4.2 Booted emulator

```
$ adb devices -l
emulator-5554   device product:sdk_gphone64_x86_64 model:sdk_gphone64_x86_64 device:emu64xa transport_id:1

$ adb shell getprop ro.build.version.sdk
34
$ adb shell getprop ro.product.model
sdk_gphone64_x86_64
```

Serial `emulator-5554` matches the project's existing `EMULATOR_DEVICE_NAME` convention (`config-emulator.properties`, `.github/workflows/mobile-automation.yml`). **VERIFIED.**

### 4.3 Docker

```
$ docker info   → healthy, Docker Desktop 4.69.0, Engine 29.4.0
$ docker version → Server: Docker Desktop 4.69.0, Engine 29.4.0
```

**VERIFIED.**

---

## 5. ADB Server Configuration

| State | Address | Port | Owning process |
|---|---|---|---|
| **Original** (before any change) | `127.0.0.1` (loopback only) | 5037 | `adb.exe` |
| **Temporary change** | `0.0.0.0` (all interfaces) | 5037 | `adb.exe` |
| **Restored** (after proof) | `127.0.0.1` (loopback only) | 5037 | `adb.exe` |

**Exact commands used:**

```
adb kill-server
adb -a nodaemon server start   &   # backgrounded — makes the ADB SERVER (not the emulator's own port) listen on all interfaces
```

**Restoration:**

```
adb kill-server
adb start-server               # default behavior — binds 127.0.0.1 only
```

**Verification of restoration** (`netstat -ano | grep :5037`):

```
TCP    127.0.0.1:5037   0.0.0.0:0   LISTENING   10296
```

Confirmed restored to the exact original binding. No Windows Firewall rule was created — none was needed (Section 8). **VERIFIED.**

---

## 6. Docker Container Test

A bare `ubuntu:24.04` container was used — not a pre-built Android/emulator image (rejected `budtmo/docker-android` as unnecessary; it would duplicate emulator capability inside the container, contradicting the "emulator stays on the host" architecture and Phase 19.1A's minimum-tooling finding). `adb` (platform-tools, Linux build) was installed at container runtime via direct download — no Android SDK, no `sdkmanager`, no emulator binary inside the container.

---

## 7. DNS Connectivity

```
$ docker run --rm ubuntu:24.04 bash -c 'getent hosts host.docker.internal'
192.168.65.254   host.docker.internal
```

Resolved **without** the Linux-specific `--add-host=host.docker.internal:host-gateway` flag — confirming Phase 19.1A's prediction that Docker Desktop provides this natively, unlike native Linux Docker. **VERIFIED.**

---

## 8. TCP Connectivity

**First attempt** (installing `netcat-openbsd` via `apt-get` mid-command) was interrupted by a tool-use rejection before it produced a result — this was **not** a TCP failure, it never executed. Re-tested using bash's built-in `/dev/tcp` (no package install required):

```
$ docker run --rm ubuntu:24.04 bash -c \
    'timeout 5 bash -c "cat < /dev/null > /dev/tcp/host.docker.internal/5037" && echo "TCP CONNECTED"'
TCP CONNECTED
```

Cross-checked from the host itself to both non-loopback interfaces ADB was bound to, confirming the socket was genuinely reachable, not just from the container's specific network path:

```
Test-NetConnection 127.0.0.1     -Port 5037  → TcpTestSucceeded: True
Test-NetConnection 172.27.144.1  -Port 5037  → TcpTestSucceeded: True   (WSL vEthernet adapter)
Test-NetConnection 192.168.0.100 -Port 5037  → TcpTestSucceeded: True   (LAN adapter)
```

**Classification: TCP CONNECTED — VERIFIED.**

---

## 9. ADB Device Discovery

```
$ docker run --rm ubuntu:24.04 bash -c '
    <install adb via platform-tools-latest-linux.zip>
    export ANDROID_ADB_SERVER_ADDRESS=host.docker.internal
    export ANDROID_ADB_SERVER_PORT=5037
    adb devices -l'

List of devices attached
emulator-5554   device product:sdk_gphone64_x86_64 model:sdk_gphone64_x86_64 device:emu64xa transport_id:1
```

The existing (temporary) emulator was discovered from inside the container via the host's ADB server. **VERIFIED.**

---

## 10. Emulator Communication

Four read-only checks executed from inside the container, all against the real running emulator:

```
$ adb -s emulator-5554 get-state
device

$ adb -s emulator-5554 shell getprop ro.build.version.sdk
34

$ adb -s emulator-5554 shell getprop ro.product.model
sdk_gphone64_x86_64

$ adb -s emulator-5554 shell getprop ro.serialno
EMULATOR36X4X9X0
```

No APK was installed. No emulator state was modified. All four commands executed successfully from the container. **VERIFIED.**

---

## 11. Evidence Summary

| Layer | Result | Classification |
|---|---|---|
| DNS resolution (`host.docker.internal`) | Resolved to `192.168.65.254`, no extra flag needed | VERIFIED |
| TCP reachability to port 5037 | Connected (container and host-self both confirmed) | VERIFIED |
| ADB server protocol handshake | `adb devices -l` returned the real device | VERIFIED |
| Device state query | `get-state` → `device` | VERIFIED |
| Device property query (SDK) | `34` | VERIFIED |
| Device property query (model) | `sdk_gphone64_x86_64` | VERIFIED |
| Windows Firewall involvement | None required — no rule created, none needed | VERIFIED (absence confirmed, not assumed) |

---

## 12. Failure Analysis

The only apparent failure during this phase (Section 8's first TCP attempt) was traced to a **tool-execution interruption**, not a networking, firewall, or ADB defect — the `apt-get install netcat-openbsd` command was interrupted before it produced output, and was misread as a TCP failure. Re-running the same layer of the proof with a dependency-free method (`/dev/tcp`) immediately succeeded. No genuine failure boundary (DNS, TCP, firewall, or ADB binding) was found anywhere in this proof. **No blocker classification (A–H) applies — none of the failure conditions in the original task list were actually encountered.**

---

## 13. Windows Result

**ADB CONNECTIVITY: VERIFIED** for Windows + Docker Desktop, using:
- `host.docker.internal` (native Docker Desktop DNS, no extra container flags)
- Host ADB server rebound with `adb -a nodaemon server start` (temporary, fully restored afterward)
- No Windows Firewall rule required

This result is specific to Windows + Docker Desktop and is **not** claimed to apply to GitHub-hosted Linux runners (Section 14).

---

## 14. GitHub/Linux Assessment

The production `.github/workflows/mobile-automation.yml` was **not** modified and **no new CI run was triggered** for this phase, per explicit instruction. No container-to-host ADB connectivity was executed on a GitHub-hosted Linux runner in this phase.

What is available as evidence: the existing, already-proven v1.1.0 baseline runs the emulator and ADB directly on the runner (no containers) — [Phase 17 Final Report](PHASE_17_FINAL_CI_BASELINE_QUALIFICATION_REPORT.md) confirms 19/19 passing under that model. Phase 19.1A separately cited external, verifiable Docker/GitHub Actions documentation establishing that `--network=host` is a standard, supported mechanism for native Linux Docker containers to reach the runner's own host-bound ports (unlike `host.docker.internal`, which requires an explicit `--add-host` flag on native Linux, confirmed absent from this project's workflow).

Since no container was actually started and no ADB connection was actually attempted on a GitHub-hosted runner:

**Classification: NOT VERIFIED** (not "verified," not "failed" — genuinely untested). The Windows result in this report must not be read as covering GitHub Actions; they are distinct networking models (Section 6/7 of Phase 19.1A) and are kept separate here as instructed.

---

## 15. Model 1 Reassessment

```
Docker
├── Java
├── Gradle
├── Appium
└── ADB
       ↓
Host ADB
       ↓
Android Emulator
```

With real evidence now available:

- **Networking**: Proven to work on Windows (this report). Not yet proven on GitHub Actions (Section 14).
- **Framework changes**: Still requires `CapabilityConfiguration`/`UiAutomator2CapabilityBuilder` to gain `remoteAdbHost`/`systemPort`/`adbPort` support (unchanged finding from Phase 19.1A — not touched in this phase, per instruction).
- **Configuration changes**: Requires the host ADB server to be deliberately started with `-a` before any container-based run — a new operational step this architecture introduces that v1.1.0 never needed, on both Windows and (per Phase 19.1A) GitHub Actions.
- **Complexity**: Higher — the container must carry `adb`, Java, Gradle, Node.js, and Appium; the host must run a reconfigured ADB server; Appium session capabilities must be extended.
- **Debugging complexity**: Higher — a failure could originate in container DNS, container TCP, the host ADB rebind, Appium's remote-ADB capability wiring, or the emulator itself. This proof narrows that surface for the DNS/TCP/ADB-protocol layers specifically, but the Appium-capability layer remains completely unexercised.
- **Reproducibility**: The container's tool versions (Java/Gradle/Node/Appium) become fully reproducible; the host-side ADB rebind step does not benefit from containerization at all and remains a manual/scripted host prerequisite either way.
- **Future parallel execution / Grid**: No new evidence either way from this proof — Appium was never started, so nothing about multi-session behavior was exercised.

**Verdict on Model 1: technically viable on Windows (now proven at the ADB layer), still unproven at the Appium-capability layer and on GitHub Actions.**

---

## 16. Model 3 Reassessment

```
Docker
├── Java
└── Gradle
       ↓
Host Appium
       ↓
Host ADB
       ↓
Android Emulator
```

- **Networking**: Would only need the HTTP layer proven in Section 8 (a plain TCP connection to a host port) — and this proof already demonstrates that exact primitive works (port 5037 in this test stands in for what would be port 4723 for Appium; the mechanism — container → `host.docker.internal` → host port — is identical and already verified). Model 3 would not need the ADB-specific rebind (`adb -a`) at all, since Appium (not the container) would be the one talking to ADB, and Appium already runs directly on the host exactly as it does in the current, proven v1.1.0 pipeline.
- **Framework changes**: None — `appium.serverUrl` is already overridable via system property (existing mechanism, confirmed in Phase 19.1A's code tracing).
- **Configuration changes**: None beyond pointing the container's Gradle invocation at the host's Appium URL.
- **Complexity**: Lower — no ADB rebind step, no new Appium capabilities, no `adb` binary needed inside the container at all.
- **Debugging complexity**: Lower — the only new layer is a plain HTTP connection, already the exact mechanism proven working in this report.
- **Windows/GitHub Actions compatibility**: Both platforms only need the already-verified `host.docker.internal` (Windows) / `--network=host` (Linux) primitive — no ADB-specific networking asymmetry to manage.
- **Operational risk**: Lower — the proven, 19/19-verified Appium/ADB/emulator pairing from v1.1.0 is left completely untouched; only the Java/Gradle harness moves.

**Verdict on Model 3: lower complexity and lower risk, and the one networking primitive it actually depends on (container → host port) is the same primitive this proof already verified.**

---

## 17. Minimum Container Tooling

**Answer: B for Model 1 (platform-tools/ADB only, no full SDK), C for Model 3 (no Android tooling at all).**

This proof directly confirms Option B is sufficient for Model 1: the container never needed `sdkmanager`, `cmdline-tools`, the `emulator` binary, or any system image — only the `adb` binary (downloaded directly, ~budget-sized platform-tools zip) was required to complete every check in Sections 9–10. A full Android SDK (Option A) would have added tooling exercised nowhere in this proof.

For Model 3, this proof also confirms Option C: no ADB binary was needed at all for the primitive Model 3 actually depends on (Section 16) — only a plain TCP/HTTP client, which Java/Gradle already provide.

---

## 18. Risks

| Risk | Severity | Notes |
|---|---|---|
| GitHub Actions networking path unverified | Medium | Section 14 — this is now the single largest remaining gap before either model can be committed to for CI use |
| Host ADB rebind (`-a`) is a new manual/scripted prerequisite (Model 1 only) | Medium | Not needed at all under Model 3 (Section 16) |
| Docker Desktop WSL2 data-disk growth does not auto-shrink | Low, informational | Real, encountered behavior (Section 4.1) — worth noting for anyone else setting up this proof locally, not a risk to the architecture itself |
| `CapabilityConfiguration` gap (Model 1 only) | Low | Unchanged, additive, still deferred to implementation phase |

---

## 19. Remaining Unknowns

1. Whether `--network=host` on a GitHub-hosted Linux runner actually permits a container to reach the runner's own ADB/Appium port — **not executed in this phase** (Section 14).
2. Whether Appium's `remoteAdbHost`/`systemPort`/`adbPort` capabilities function correctly end-to-end (Model 1) — **never exercised**, since Appium was not started in this phase (explicitly out of scope).
3. Whether Model 3's plain HTTP path from container to a host-run Appium server behaves identically to the ADB path proven here — **inferred, not directly executed** (Section 16 reasoning is sound but Appium itself was never contacted from a container in this phase).

---

## 20. Recommended Architecture Direction

Given Section 15 vs. Section 16: **Model 3 is the lower-risk, lower-complexity path and depends on exactly the networking primitive this proof already verified.** Model 1 remains technically viable on Windows but carries strictly more unverified surface area (Appium capability wiring, GitHub Actions networking, an extra host-side operational step) for no proven benefit identified in this proof. This is a recommendation for the next phase's architecture decision, not a decision made here.

---

## 21. Final Verdict

# ADB CONNECTIVITY PARTIALLY VERIFIED — ADDITIONAL PROOF REQUIRED

The Windows + Docker Desktop path (DNS → TCP → ADB server → device discovery → device communication) is fully and directly verified with real execution evidence. What remains unresolved before a full architecture design can be committed to:

1. **GitHub Actions/Linux connectivity is genuinely untested** (Section 14) — the same class of proof performed here for Windows has not been performed for the `--network=host` Linux path.
2. **Model 3's exact primitive (container → host Appium HTTP port) was not directly exercised** — only inferred from the equivalent, already-proven container → host ADB TCP port result.

**Recommended next phase:** A narrowly-scoped **Phase 19.1C — GitHub Actions Container Networking Proof**, run as an isolated, throwaway workflow (not the production `mobile-automation.yml`) on a GitHub-hosted runner, proving `--network=host` container-to-runner-port reachability — mirroring exactly what this phase proved for Windows. Once both platforms are verified, Phase 19.2 (Docker Architecture Specification) can proceed with both Model 1 and Model 3 fully evidence-backed rather than partially inferred.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-08 |
| Document Status | Final — Technical Proof Report (No Implementation) | — | — |

---

**End of Document — Phase 19.1B Docker-to-Host Android Emulator ADB Connectivity Proof Report, v1.0**
