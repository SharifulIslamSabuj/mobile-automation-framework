# Test Data Framework

## Document Control

| Field | Value |
|---|---|
| Document ID | MA-TDF-001 |
| Title | Test Data Framework |
| Version | v1.0 |
| Status | Draft |
| Phase | Test Data Framework (Phase 8) |
| Governed By | [MA-FR-001 — Framework Architecture Rules](FRAMEWORK_ARCHITECTURE_RULES.md), [MA-CS-001 — Coding Standards](../standards/CODING_STANDARDS.md), [MA-CFG-001 — Configuration Architecture](CONFIGURATION_ARCHITECTURE.md), [MA-UTIL-001 — Core Utilities Architecture](CORE_UTILITIES_ARCHITECTURE.md), [MA-DEP-001 — Dependency Version Freeze](../architecture/DEPENDENCY_VERSION_FREEZE.md) |

## 1. Scope of This Phase

This phase built the reusable, strongly-typed, environment-aware Test Data Framework — the single source of truth every future test will use for data. It supports static, dynamic, environment-specific, negative, and random data, in JSON/YAML/Properties formats, mapped to immutable POJOs and validated after load. No Page Object, Test Class, business workflow, or application-specific test data value was written — every sample dataset and factory produces clearly generic/placeholder content, not this project's actual AUT data.

## 2. Package Structure

```
com.mobileautomation.framework
├── models                    (existing, top-level — see §9 for why POJOs live here, not under data)
│   ├── LoginCredentials       — record(username, password)
│   ├── UserProfile            — record(firstName, lastName, email, phone, username)
│   └── ProductItem            — record(name, sku, price, category)
└── data
    ├── manager   → TestDataManager                         (the single entry point)
    ├── loader    → DataLoader                                (read → deserialize → validate → cache)
    ├── reader    → DataReader, AbstractDataReader
    │   ├── json       → JsonDataReader
    │   ├── yaml       → YamlDataReader
    │   └── properties → PropertiesDataReader
    ├── validator → Required (annotation), DataValidator
    ├── environment → TestDataEnvironmentResolver
    ├── provider  → NegativeDataProvider
    ├── generator → FakerProvider, RandomDataGenerator, DynamicDataGenerator
    └── factory   → LoginDataFactory, UserDataFactory, ProductDataFactory
```

Two deliberate deviations from the phase prompt's suggested structure, both explained in §9:
- POJOs live in the existing top-level `models` package, not a new `data.model` sub-package.
- No `data.resources` Java package was created — the actual JSON/YAML/Properties files are classpath *resources*, not Java source, and live under `src/test/resources/testdata/` (see §8), matching how `config.properties` already lives under `src/test/resources/config/`.

## 3. Reader Architecture

All three readers implement one common interface, `DataReader#read(String resourcePath, Class<T> targetType)`, and share every line of plumbing through `AbstractDataReader`: it opens the classpath resource (throwing `TestDataException` if missing), calls the concrete reader's `ObjectMapper`, and wraps any parse failure in `TestDataException` with the cause preserved.

The key design choice: **all three formats are read through the same `ObjectMapper`-based POJO-binding model**, not three different parsing strategies bolted together —

| Reader | `ObjectMapper` subclass | Dependency |
|---|---|---|
| `JsonDataReader` | `ObjectMapper` (plain) | `jackson-databind` (already frozen, MA-DEP-001) |
| `YamlDataReader` | `YAMLMapper` | `jackson-dataformat-yaml` (added this phase) |
| `PropertiesDataReader` | `JavaPropsMapper` | `jackson-dataformat-properties` (added this phase) |

Because `YAMLMapper`/`JavaPropsMapper` both extend `ObjectMapper` (confirmed via `javap` against the real 2.18.0 jars), `AbstractDataReader`'s single `read(...)` method body works unchanged for all three — adding CSV/Excel/Database later means adding one new `AbstractDataReader` subclass (or, for a source with no natural Jackson mapper, a new `DataReader` implementation) and registering it in `DataLoader`'s extension map — no change to `DataLoader`'s or `TestDataManager`'s public API, satisfying "Future support should allow CSV/Excel/Database without changing consumer code."

## 4. Loader Lifecycle

```
TestDataManager.loadData(path, Type)
        │
        ▼
DataLoader.load(path, Type)
        │
        ├─ cache hit  → return cached instance (ConcurrentHashMap, thread-safe)
        │
        └─ cache miss
                │
                ▼
           resolveReader(path)   — by file extension (json/yaml/yml/properties)
                │
                ▼
           reader.read(path, Type)   — throws TestDataException: missing file / malformed content
                │
                ▼
           DataValidator.validate(data)   — throws DataValidationException: blank @Required field
                │
                ▼
           cache.put(key, data) → return data
```

Every failure — missing file, malformed JSON/YAML/Properties, unsupported extension, blank required field — throws a specific `FrameworkException` subtype with a message naming the resource and the problem. Nothing is silently defaulted or swallowed (fail fast, per the phase's explicit requirement).

## 5. POJO Mapping Strategy

Models are immutable Java **records** (`LoginCredentials`, `UserProfile`, `ProductItem`), not Lombok-generated classes — records are natively Jackson-deserializable (since Jackson 2.12, via `RecordComponent` introspection, no extra module needed — confirmed empirically: all three loaded correctly) and give immutability, `equals`/`hashCode`/`toString`, and compile-time field declaration for free. Each implements `java.io.Serializable` per the phase's explicit checklist. Every field the framework itself needs to enforce as present is annotated `@Required` (from `data.validator`); optional fields (e.g. `UserProfile.phone`) are left unannotated.

No field or sample value is tied to the Sauce Labs demo app or any other real AUT — `LoginCredentials`/`UserProfile`/`ProductItem` are deliberately generic shapes any mobile app's login/registration/catalog screens could use, and the checked-in sample datasets (`src/test/resources/testdata/**`) use obviously placeholder values (`sample_user`, `Sample@Pass123`, `Sample Product`) rather than this project's actual test credentials or product names.

## 6. Factory Strategy

`LoginDataFactory`, `UserDataFactory`, `ProductDataFactory` each combine one or more of {static file data, dynamic/random data, Faker data, environment data} behind a small method set, without exposing which source a given method used:

| Factory | Method | Combines |
|---|---|---|
| `LoginDataFactory` | `standardCredentials()` | Environment data (via `TestDataManager.loadEnvironmentData`) |
| | `randomCredentials()` | Random data (`RandomDataGenerator`) |
| | `blankCredentials()` | Negative data (`NegativeDataProvider`) |
| `UserDataFactory` | `fakeUserProfile()` | Faker + Dynamic data (`FakerProvider` + `DynamicDataGenerator`) |
| `ProductDataFactory` | `fakeProduct()` | Faker data (`FakerProvider`) |

Factories are obtained through `TestDataManager` (`manager.loginData()`/`.userData()`/`.productData()`), never instantiated directly by a future test that only has `TestDataManager` in view — though see §9 for the one caveat on enforcement.

## 7. Faker Integration ↔ Dynamic Data ↔ RandomUtility

```
utils.RandomUtility (Phase 5)          net.datafaker.Faker
        │                                      │
        ▼                                      ▼
data.generator.RandomDataGenerator      data.generator.FakerProvider
 (numbers, strings, uuid,                (the ONLY `new Faker(...)` call
  random email/username)                  site in the whole framework)
        │                                      │
        └──────────────┬───────────────────────┘
                        ▼
          data.generator.DynamicDataGenerator
     (uniqueUsername/uniqueEmail compose RandomDataGenerator
      + utils.DateUtility's timestamp; phoneNumber() composes
      FakerProvider; uuid()/sessionValue()/timestamp() round out
      the runtime-context generator set)
                        │
                        ▼
                 data.factory.*
```

`FakerProvider.get()` is a double-checked-locking singleton `Faker` instance (matching `ConfigReader`'s established pattern) — every other class that needs Faker-generated data calls `FakerProvider.get()`, never `new Faker()` directly, satisfying "Never instantiate Faker throughout the framework." `FakerProvider.withSeed(long)` additionally supports the phase's "deterministic seed when required" requirement, returning an independently-seeded instance without disturbing the shared one. `RandomDataGenerator` reuses `utils.RandomUtility` rather than re-implementing randomness (per the explicit "reuse existing RandomUtility whenever appropriate" instruction), and `DynamicDataGenerator` composes both `RandomDataGenerator` and `utils.DateUtility` rather than duplicating either — this is the exact dependency shape the phase prompt specified (Dynamic Data → RandomUtility → DateUtility → Faker → Factories).

## 8. Environment Resolution

```
TestDataManager.loadEnvironmentData("login/credentials.json", LoginCredentials.class)
        │
        ▼
TestDataEnvironmentResolver.resolve("login/credentials.json")
        │
        ├─ ConfigReader.getInstance().getEnvironment()  →  e.g. EMULATOR ("emulator")
        │
        ├─ try  "testdata/emulator/login/credentials.json"   — exists on classpath? use it.
        │
        └─ else "testdata/common/login/credentials.json"     — environment-agnostic fallback
```

The environment segment is never hardcoded — it comes from the already-frozen `config.Environment` enum (`EMULATOR`/`REAL_DEVICE`, from Phase 3), not the illustrative `dev/qa/staging/production` folder names in the phase prompt (see §9 for why). `src/test/resources/testdata/` contains `common/` (environment-agnostic defaults) and `emulator/` (one override, `login/credentials.json`, added specifically to prove the resolution path empirically — see §10). No `real-device/` folder exists yet, which is exactly what proved the fallback path works: requesting data under `-Denv=real-device` correctly fell back to `common/`.

## 9. Architectural Decisions Requiring Approval

- **POJO models live in the existing top-level `models` package, not a new `data.model` sub-package.** The Project Bootstrap phase already froze `models` with exactly this description ("Plain data model / POJO classes representing domain entities used across the framework"), and `data`'s own frozen description was "Test data models **and** data-loading utilities" — creating a second, competing "model" location under `data.model` would have split one concept across two top-level packages for no benefit. Confirm this reconciliation, or redirect toward moving these three records into `data.model` for literal adherence to this phase's suggested structure.
- **Environment folders use the project's actual `Environment` enum values (`emulator`/`real-device`), not the prompt's illustrative `dev/qa/staging/production`.** Using fictional environments not modeled anywhere else in the framework would have been inconsistent with the Configuration Layer this phase must integrate with. Confirm this substitution.
- **No `data.resources` Java package was created** — the suggested structure's "resources" entry is interpreted as the physical `src/test/resources/testdata/` classpath layout (§8), not a Java package, since there is no Java source that belongs in it. Confirm this interpretation.
- **Readers, `DataLoader`, and the factories' constructors had to be `public`, not package-private**, breaking from the `DriverManager`/`ExtentReportManager` "package-private manager + public facade" compiler-enforced pattern used in Phases 4 and 6. That pattern requires the hidden implementation and its facade to share one package; this phase's requested structure deliberately splits `manager`/`loader`/`reader`/`factory` into separate sub-packages, which makes package-private cross-package access impossible. "Future tests must never directly use readers" is therefore enforced by convention and Javadoc here, not by the compiler. Confirm this tradeoff is acceptable, or redirect toward collapsing `reader`/`loader` into the `manager` package to restore compiler enforcement.
- **`net.datafaker:datafaker` was selected over `com.github.javafaker:javafaker`** — the latter is unmaintained since 2020 and carries a vulnerable transitive SnakeYAML; datafaker is its actively maintained successor with an equivalent API (confirmed via `javap` against the real jar). Confirm this substitution for "Java Faker (or maintained equivalent)."
- **`DataValidator`'s "schema validation" is Jackson's own POJO-shape binding**, not a separate JSON-Schema library — a type mismatch or structurally invalid document is already caught and wrapped as `TestDataException` by the reader layer; `DataValidator` itself only adds the one rule readers can't express: "a `@Required`-marked field must not be null/blank" (reflection-driven, model-agnostic). Confirm this two-layer split is sufficient, or redirect toward adding a dedicated schema-validation dependency if stronger structural guarantees are wanted.

## 10. Validation Performed This Phase

| Requirement | Result |
|---|---|
| JSON loads successfully | **Empirically verified** — `testdata/common/login/credentials.json` loaded into `LoginCredentials` with exact field values |
| YAML loads successfully | **Empirically verified** — `testdata/common/user/profile.yaml` loaded into `UserProfile` with exact field values |
| Properties load successfully | **Empirically verified** — `testdata/common/product/item.properties` loaded into `ProductItem`, including a `BigDecimal` price field |
| POJOs deserialize correctly | Covered by the three rows above; all three formats used the same `ObjectMapper`-based path |
| Validation detects malformed data | **Empirically verified** — a deliberately truncated JSON file threw `TestDataException` naming the resource and the parse error |
| Missing files handled correctly | **Empirically verified** — a nonexistent resource path threw `TestDataException` with a clear "not found on classpath" message |
| Environment switching loads correct datasets | **Empirically verified twice** — default environment (`EMULATOR`) resolved the `emulator/login/credentials.json` override; re-run with `-Denv=real-device` (no override file exists) correctly fell back to `common/login/credentials.json` |
| Faker generates valid data | **Empirically verified** — `UserDataFactory.fakeUserProfile()` and `ProductDataFactory.fakeProduct()` produced non-blank names and a syntactically valid email/positive price |
| Dynamic generators work correctly | **Empirically verified** — two `DynamicDataGenerator.uniqueUsername()` calls produced distinct values; `uuid()`/`timestamp()`/`sessionValue()`/`phoneNumber()` all returned well-formed values |
| Framework builds successfully | `gradlew clean build` (main) and `gradlew compileTestJava` (test) both BUILD SUCCESSFUL against the real `jackson-dataformat-yaml`/`jackson-dataformat-properties`/`datafaker` 2.4.3 APIs — verified via `javap` before writing dependent code, so no guessed method name needed correction |

Two additional checks beyond the required list were also run and passed: `DataValidator` correctly rejected a blank `password` field (`DataValidationException`), and `TestDataManager`'s cache returned the identical object reference (`==`) on a second load of the same resource, confirming caching actually occurs rather than re-reading the file.

**Explicit limitation:** validation ran entirely through a throwaway verification program (deleted after use, per this project's established practice), never an `@Test`-annotated automation test — consistent with "Do NOT create automation tests" for this phase.

## 11. Extension Strategy

- **A new data format** (CSV/Excel/Database) is added as a new `DataReader` implementation (extending `AbstractDataReader` where a Jackson mapper exists, or implementing `DataReader` directly otherwise) and registered in `DataLoader`'s extension map — no change to `TestDataManager`'s or any factory's public API.
- **A new model** is added to `models` as an immutable record, with `@Required` on whichever components must not be null/blank; no change to `DataValidator` is needed since it works reflectively.
- **A new factory** follows the existing pattern: obtained via a new `TestDataManager` accessor method, combining whichever of {static/dynamic/Faker/environment} data sources it needs, without exposing which source it used.
- **A new environment** is added the same way `REAL_DEVICE` already exists — as a new `config.Environment` enum value; `TestDataEnvironmentResolver` needs no code change, since it always reads `Environment.getProfileName()` rather than hardcoding a list.
- **Do not** let any class in `data` or `models` grow a dependency on `pages`, `tests`, `reporting`, `listeners`, or `assertions` — this phase's Integration Rules restrict it to Configuration Layer, Core Utilities, and Base Framework (the latter was not actually needed by anything built this phase).
