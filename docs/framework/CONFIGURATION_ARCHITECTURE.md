# Configuration Architecture

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-CFG-001 |
| Title | Configuration Architecture |
| Version | v1.0 |
| Status | Draft |
| Phase | Configuration Layer (Phase 3) |
| Governed By | [MA-FR-001 — Framework Architecture Rules](FRAMEWORK_ARCHITECTURE_RULES.md), [MA-CS-001 — Coding Standards](../standards/CODING_STANDARDS.md) |

## 1. Overall Architecture

The Configuration Layer is the framework's single source of truth for every configurable value. It lives entirely in the `config` package (plus supporting constants in `constants` and a dedicated exception type in `exceptions`), and every other layer — Driver Management, Utilities, Reporting, Test Data, Base Framework — is required to obtain configuration exclusively through it, never by reading a file or an environment variable directly.

```
com.mobileautomation.framework
├── config
│   ├── ConfigReader              — the single entry point; loads & exposes every typed value
│   ├── Environment                — EMULATOR / REAL_DEVICE — selects which files load
│   ├── ExecutionMode               — LOCAL / EMULATOR / REAL_DEVICE — a config *value*, read from file
│   └── CapabilityConfiguration      — immutable POJO snapshot for the future Driver layer
├── constants
│   ├── ConfigurationKeys           — every property key / system-property name (no magic strings)
│   └── ConfigurationDefaults        — compiled-in fallback values only
└── exceptions
    └── ConfigurationException       — thrown for any load/parse/resolution failure
```

## 2. Configuration Loading Flow

```
1. ConfigReader.getInstance() is called (first call constructs the singleton)
2. resolveEnvironment()
      reads the JVM system property "env"
      → if absent, defaults to Environment.EMULATOR (via ConfigurationDefaults.DEFAULT_ENVIRONMENT_PROFILE)
      → if present but unrecognized, throws ConfigurationException immediately (fail fast)
3. loadPropertiesFile("config.properties")            — common values, loaded first
4. loadPropertiesFile("config-<profileName>.properties") — environment values, loaded second,
                                                            overwriting any key also present in
                                                            step 3 (java.util.Properties#load
                                                            merges into the same instance)
5. ConfigReader is now ready; every typed getter reads from the merged Properties,
   checking for a same-named JVM system property first (see Hierarchy, below)
```

Both properties files are read as classpath resources via `ClassLoader#getResourceAsStream`, resolved from `src/test/resources/config/` (Gradle copies this to the test runtime classpath — the same mechanism already used for `log4j2.xml` since Project Bootstrap). Framework infrastructure code lives in `src/main`, but because this repository is a test-execution framework, not a shipped application, the only classpath that ever actually *runs* is the merged main+test classpath produced by `gradlew test` — so `main`-side code reading `test/resources` content is the correct, intentional pattern here, not a layering violation.

If a properties file is missing from the classpath entirely, `ConfigReader`'s constructor throws `ConfigurationException` immediately — there is no silent "empty configuration" state.

## 3. Supported Environments

| `Environment` enum value | Profile name (`-Denv=...`) | Properties file loaded | Default? |
|---|---|---|---|
| `EMULATOR` | `emulator` | `config-emulator.properties` | Yes — used when `-Denv` is not supplied at all |
| `REAL_DEVICE` | `real-device` | `config-real-device.properties` | No |

Supplying any other value to `-Denv` (e.g. `-Denv=tablet`) throws `ConfigurationException` listing the supported profile names — confirmed by direct execution during this phase's validation pass (see §8).

## 4. Configuration Hierarchy (Precedence, Highest to Lowest)

| Tier | Source | Example |
|---|---|---|
| 1 (highest) | JVM system property matching the exact config key | `-Dapp.path=/local/build/app.apk` overrides whatever `app.path` resolves to below |
| 2 | Environment-specific properties file | `config-real-device.properties`'s `execution.mode=REAL_DEVICE` |
| 3 | Common properties file | `config.properties`'s `driver.explicitWaitTimeoutSeconds=15` |
| 4 (lowest) | Compiled-in default (`ConfigurationDefaults`) | Used only if the key is absent from **both** files — e.g. `getUdid()` falls back to `ConfigurationDefaults.DEFAULT_UDID` ("") |

Tiers 2 and 3 are merged into one `java.util.Properties` instance at load time (tier 2 overwriting tier 1... i.e. tier-3 values get overwritten by tier-2 values for the same key), so from the perspective of `ConfigReader`'s getters there are really only three effective tiers at read time: system property → merged properties → compiled default.

This satisfies "zero duplicated configuration" in the sense the architecture requirement intends: the same *value* is never independently hardcoded in two places that could silently drift apart. Having the same *key* appear in `config.properties` and being deliberately overridden in an environment file is the correct, intended overlay pattern — not the duplication being guarded against.

## 5. Environment Switching

Environment switching requires **zero source-code changes** — it is driven entirely by the `-Denv` JVM system property at execution time:

```bash
./gradlew test                          # -Denv not supplied -> EMULATOR (default)
./gradlew test -Denv=emulator           # explicit emulator
./gradlew test -Denv=real-device        # explicit real device
```

This was verified directly in this phase (not via a TestNG Test Class, which is out of scope for this phase — via a throwaway, uncommitted verification program compiled and run against the built classes): running with no `-Denv` resolved `Environment.EMULATOR` with `device.name=emulator-5554`; running with `-Denv=real-device` resolved `Environment.REAL_DEVICE` with `device.name=""` (real-device values are intentionally left blank in source control — see §7) — with no code recompiled or edited between the two runs.

## 6. Configuration Ownership

- **`ConfigReader` owns all configuration access.** No other class — present or future — is permitted to call `Properties#load`, read a `.properties`/`.yaml` file, or call `System.getProperty` for a configuration value directly. This is a standing rule for every future phase (Driver Management, Reporting, Test Data, Page Objects, Tests), not just a note about the current state.
- **`constants` owns key names and defaults**, never a live/current value.
- **`config` package owns *how* a value is obtained; it owns none of the AUT's business data.** No login credentials, product names, or other test data appear anywhere in this layer — that boundary was deliberately respected (see MA-TDD-001 for where that data actually lives).
- **The Driver layer (a later phase) will own turning a `CapabilityConfiguration` into an actual Appium session** — this phase stops at producing that plain data object; no `AppiumDriver`, no `UiAutomator2Options`, no session was created or referenced anywhere in this phase's code.

## 7. Configuration Properties Introduced

| Key | Section | Default (if unset) | Notes |
|---|---|---|---|
| `app.path` | Application | `""` | Left blank in source control — no `.apk` ships with this repository; supply via system property |
| `app.package` | Application | *(required — throws if missing)* | `com.saucelabs.mydemoapp.android` |
| `app.activity` | Application | *(required — throws if missing)* | `com.saucelabs.mydemoapp.android.view.activities.SplashActivity` |
| `device.name` | Device | `""` | Blank for real-device by design (device-specific, supplied per run) |
| `platform.name` | Device | `"Android"` | Frozen per MA-PV-001 §16 |
| `platform.version` | Device | `""` | Left blank; AVD/device-specific |
| `device.udid` | Device | `""` | Left blank; device-specific |
| `automation.name` | Device | `"UiAutomator2"` | Frozen per MA-PV-001 §16 |
| `driver.newCommandTimeoutSeconds` | Driver | `120` | — |
| `driver.implicitWaitSeconds` | Driver | `0` | Intentionally 0 — see §9 open decision |
| `driver.explicitWaitTimeoutSeconds` | Driver | `15` | — |
| `driver.pageLoadTimeoutSeconds` | Driver | `30` | — |
| `execution.mode` | Execution | current `Environment`'s name | See §9 for the `Environment` vs `ExecutionMode` relationship |
| `execution.noReset` | Execution | `true` | — |
| `execution.fullReset` | Execution | `false` | — |
| `execution.autoGrantPermissions` | Execution | `true` | — |
| `execution.retryCount` | Test Execution | `1` | — |
| `report.directory` | Reporting | `"reports"` | — |
| `report.screenshotDirectory` | Reporting | `"reports/screenshots"` | — |
| `log.level` | Logging | `"INFO"` | — |

Plus one JVM system property that is not a properties-file key at all: `env`, used solely to select the active `Environment` before any file is read.

## 8. Validation Performed This Phase

Per the phase's Validation checklist, all four items were exercised via a throwaway program (compiled and executed against the built `build/classes/java/main` output, then discarded — not committed, and deliberately not written as a TestNG `@Test` since Test Classes are out of scope for this phase):

1. **Every configuration value loads correctly** — all 19 properties printed correctly for both `EMULATOR` (default) and `REAL_DEVICE` environments, with environment-specific overrides (`device.name`, `execution.mode`) correctly taking precedence over the common file.
2. **Environment switching works without code changes** — confirmed by re-running the identical compiled class with only `-Denv=real-device` added; no recompilation, no source edit.
3. **Invalid configuration produces meaningful exceptions** — confirmed for both an invalid integer value (`ConfigurationException: Invalid integer value for key 'driver.pageLoadTimeoutSeconds': 'not-a-number'`) and an unrecognized environment profile (`ConfigurationException: Unsupported environment profile: 'tablet'. Supported profiles: emulator, real-device`).
4. **No framework component outside this layer owns configuration** — confirmed by inspection: no other package exists yet with any implementation (Driver Management and every other layer remain unimplemented per this phase's scope boundary), so there is nothing else that could own configuration at this point in the project.

## 9. Architectural Decisions Requiring Approval

- **`Environment` vs `ExecutionMode` are modeled as two distinct enums answering two different questions**, since the phase instructions listed "Environment Profiles" (`emulator`, `real-device`) and "Execution Modes" (`LOCAL`, `EMULATOR`, `REAL_DEVICE`) as separate sections with overlapping but not identical value sets. `Environment` controls *which properties files load*; `ExecutionMode` is a *configuration value* (`execution.mode`) read from within those files, defaulting to mirror the active `Environment`'s name but independently overridable (e.g., to introduce a future `LOCAL` orchestration mode without needing a third `Environment`). This is a judgment call made to honor both instruction sections literally rather than silently collapsing them into one — please confirm this interpretation is correct, or redirect if a different relationship was intended.
- **`driver.implicitWaitSeconds` defaults to `0`** because MA-FR-001 §5 forbids relying on implicit waits. The property still exists (per this phase's explicit instruction to include it "if retained"), but its presence creates a latent tension with the framework rule if a future contributor raises it above 0 "for convenience." Recommend the Driver Management phase treat any non-zero value here as a lint/review flag, or that this property be removed entirely in a future revision if it's confirmed to have no legitimate use.
- **Real-device `device.name`/`platform.version`/`device.udid` are left blank in source control** rather than populated with an example value, since no specific physical device is confirmed as this project's standing real-device target. Confirm this is acceptable, or supply the actual device details to bake in as the checked-in default.

## 10. Future Extension Guidelines

- **Adding a new configuration value:** add its key to `ConfigurationKeys`, its default (if any) to `ConfigurationDefaults`, a typed getter to `ConfigReader`, and the key/value to the relevant `.properties` file(s) — in that order, in the same change.
- **Adding a new environment:** add a new `Environment` enum constant with its profile name, create the corresponding `config-<profileName>.properties` file, and update the table in §3 of this document. No other class needs to change — `ConfigReader`'s loading logic is already environment-count-agnostic.
- **Adding a new execution mode:** add the constant to `ExecutionMode`; no properties-file schema change is required unless a new mode needs its own dedicated defaults.
- **Do not** add a configuration value directly to a Page Object, Test Class, or any future Driver Management class — route it through this layer first, per MA-FR-001 §2/§3/§4.
- **Do not** introduce a second configuration-reading mechanism (e.g., a YAML reader) without updating this document and explaining why `ConfigReader`'s existing properties-based approach is insufficient.
