# Phase 19.3 (v1.2.0) — Local opt-in Docker test-harness image.
#
# Scope, per docs/docker/PHASE_19.2_DOCKER_ARCHITECTURE_SPECIFICATION.md (Model 3):
# this image contains ONLY the Java/Gradle test harness. The Android emulator,
# ADB, and Appium all remain on the host and are never containerized. The
# image never bakes in project source — it is run with the repository
# bind-mounted at /workspace (Section 5 of the architecture spec: Option B,
# chosen for fast local iteration and a clean image/runtime separation).
#
# Base image pinned by digest (not just the "17-jdk-jammy" tag) so the exact
# same bytes are used on every build, consistent with this project's existing
# no-floating-version discipline (see .github/workflows/mobile-automation.yml,
# which pins every GitHub Action to an exact version for the same reason).
# Verified Java version inside this exact digest: OpenJDK Temurin 17.0.19,
# matching the project's own pinned toolchain (build.gradle: JavaLanguageVersion.of(17)).
FROM eclipse-temurin:17-jdk-jammy@sha256:29467857e8bde40ab1f7befecbda0ea764b95afec1cc7f89aa90f7a766577e19

# Non-root execution — the harness needs no elevated privileges (it only
# compiles/runs Gradle and speaks HTTP to the host's Appium server).
RUN groupadd --gid 1000 harness \
    && useradd --uid 1000 --gid harness --create-home --shell /bin/bash harness

WORKDIR /workspace
USER harness

# No ENTRYPOINT/CMD baked in on purpose: the actual ./gradlew invocation
# (with its -D flags for env/appium.serverUrl/app.path/etc.) is supplied
# explicitly at `docker run` time — see docs/docker/PHASE_19.3_DOCKER_IMPLEMENTATION_REPORT.md
# Section 9 for the exact command. Keeping it out of the image keeps the
# image itself a plain, reusable "Java 17 + shell" runtime.
