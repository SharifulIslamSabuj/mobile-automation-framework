---
document_id: PHASE-19.4K
title: AUT Version & Crash Remediation Investigation
version: v1.0
status: Final — Investigation Report (No Fix Implemented, Decision Pending)
author: Project Owner / Repository Maintainer
created_date: 2026-08-10
last_updated: 2026-08-10
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-19.4A, PHASE-19.4J]
classification: Internal
---

# Phase 19.4K — AUT Version & Crash Remediation Investigation

---

## 1. Objective

Determine, using authoritative evidence only, whether the Sauce Labs demo AUT crash verified in Phase 19.4J (an uncaught `NoSuchMethodException` during Android's Fragment state-restoration, crashing the app and leaving the launcher in the foreground) already has an available remediation — an official newer version, an official source-level fix, or another evidence-based path — without implementing anything.

---

## 2. Scope

Investigation and decision-support only. No file in this repository was modified. No Docker, workflow, framework, test, Page Object, capability, or Gradle configuration change was made. No local Android/Appium/Docker environment was rebuilt — all evidence was gathered via repository inspection and the GitHub API against the official `saucelabs/my-demo-app-android` repository.

---

## 3. Current AUT Identification

From `.github/workflows/mobile-automation.yml`:

```
AUT_RELEASE_TAG: "2.2.0"
AUT_APK_ASSET: "mda-2.2.0-25.apk"
AUT_DOWNLOAD_URL: "https://github.com/saucelabs/my-demo-app-android/releases/download/2.2.0/mda-2.2.0-25.apk"
```

From `src/test/resources/config/config.properties`:

```
app.package=com.saucelabs.mydemoapp.android
app.activity=com.saucelabs.mydemoapp.android.view.activities.SplashActivity
```

From `README.md`: the AUT is identified as "[Sauce Labs My Demo App (Android)](https://github.com/saucelabs/my-demo-app-android)". No local `.apk` file exists in the repository (confirmed via filesystem search) — the APK is downloaded fresh at CI runtime from the official GitHub Releases URL above. **VERIFIED**, all facts, directly from repository source.

---

## 4. Current APK Metadata

Package: `com.saucelabs.mydemoapp.android`. Release tag: `2.2.0`. Asset filename: `mda-2.2.0-25.apk` (the `25` is a build-artifact identifier, not independently decoded from an actual APK binary since none is present in the repo — **NOT VERIFIED** as a specific `versionCode` without direct APK inspection, which was not performed per the phase's own "no local environment rebuild unless required" instruction; the filename convention alone is sufficient to identify the release unambiguously via the download URL, which was used instead). Origin: official Sauce Labs GitHub repository, official Releases page, published `2024-11-14T09:36:59Z` (**VERIFIED** via GitHub API, Section 6).

---

## 5. Verified Crash Signature

From Phase 19.4J (re-stated, not re-investigated here): `java.lang.RuntimeException: Unable to start activity ComponentInfo{com.saucelabs.mydemoapp.android/....MainActivity}: androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment com.saucelabs.mydemoapp.android.view.fragments.ProductCatalogFragment: could not find Fragment constructor`, caused by `java.lang.NoSuchMethodException: com.saucelabs.mydemoapp.android.view.fragments.ProductCatalogFragment.<init> []`, occurring inside `MainActivity.onCreate(MainActivity.java:105)` → `BaseActivity.onCreate(BaseActivity.java:46)`, triggered by an Android-system-initiated Activity relaunch (`ActivityRelaunchItem.execute` → `handleRelaunchActivity` → `handleRelaunchActivityInner`), i.e., Android's `FragmentManager` attempting to reflectively reinstate a previously-saved `ProductCatalogFragment` instance and finding no accessible no-argument constructor.

---

## 6. Official AUT Source and Release Investigation

The repository is public, active, and not archived (`archived: false`, `pushed_at: 2026-07-13T14:38:19Z` — commits exist on the default branch after the last tagged release). Full release history retrieved via `gh api repos/saucelabs/my-demo-app-android/releases`:

| Tag | Published |
|---|---|
| **2.2.0** (currently used) | 2024-11-14 |
| 2.1.0 | 2024-09-16 |
| 2.0.2 | 2024-06-04 |
| 2.0.1 | 2024-02-02 |
| 2.0.0 | 2023-07-08 |
| 1.0.17 → 1.0.0 | 2021-09-22 → 2023-03-16 |

**VERIFIED**: `2.2.0` is the **most recent official tagged release** — there is no newer official version.

---

## 7. Newer Version Investigation

Since `2.2.0` is already the latest tagged release (Section 6), "Option A" (use an official newer APK) has no candidate to evaluate. The unreleased `main` branch (23 commits/20 months ahead of the `2.2.0` tag per `pushed_at`) was checked directly for this specific defect (Section 8) — it does **not** contain a fix either. **VERIFIED**: no newer official version exists, released or unreleased, that addresses this defect.

---

## 8. Exact Source Bug Investigation

Fetched directly from the official repository via the GitHub Contents API, at the **exact `2.2.0` tag** (the release currently in use):

```java
// app/src/main/java/com/saucelabs/mydemoapp/android/view/fragments/ProductCatalogFragment.java, tag 2.2.0
public class ProductCatalogFragment extends BaseFragment implements View.OnClickListener {
    ...
    public ProductCatalogFragment(boolean addVisualChanges) {
        this.addVisualChanges = addVisualChanges;
    }
    // no public no-argument constructor exists anywhere in this class
}
```

This is the **only** constructor in the class — confirmed by fetching the full file and searching for every `public ProductCatalogFragment` declaration. **VERIFIED**, directly from the official 2.2.0 source, exactly matching the `NoSuchMethodException: ...ProductCatalogFragment.<init> []` signature from Phase 19.4J.

Cross-referencing the exact crash-trace line numbers against the official 2.2.0 source:

```java
// MainActivity.java, line 105 (tag 2.2.0)
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);   // <-- line 105, matches the stack trace exactly
    ...
}

// BaseActivity.java, line 46 (tag 2.2.0)
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);   // <-- line 46, matches the stack trace exactly
    ...
}
```

Both stack-trace line numbers correspond precisely to the `super.onCreate(savedInstanceState)` calls — exactly where `FragmentActivity`'s own `onCreate()` performs Fragment state restoration before the subclass body executes. **VERIFIED**, line-for-line, against the exact release in use.

**Commit history for `ProductCatalogFragment.java`** (full history, oldest to newest): `First commit` (2021-09-13) → `Added intentional bugs to some products` (2021-10-27) → `Indentation / removed dead code / fixed typo` (2022-04-30) → `Change margin` (2024-07-16) → `Add super` (2024-07-17) → `Remove dm` (2024-07-17) → `Remove super()` (2024-07-22). **VERIFIED**: no commit in this file's entire history, from the project's first commit through the present, has ever added a no-argument constructor to this class. The single-argument-only constructor pattern has been present since the file was first created, and remains present in the currently-unreleased `main` branch.

---

## 9. Known-Issue Investigation

Searched the official repository's issue tracker (all issues and PRs, open and closed, `gh api repos/saucelabs/my-demo-app-android/issues`) and GitHub code/issue search for `NoSuchMethodException`, `Fragment crash`, and `crash` scoped to this repository. **No issue or pull request in the official tracker documents, references, or fixes this specific defect.** The only tangentially related finding is an open, automatically-generated static-analysis issue (#47, "[LeakScope] 17 Android lifecycle/memory violations detected") from an external academic research tool (unaffiliated with Sauce Labs) — it does flag `ProductCatalogFragment` (among 13 other Fragments) for a **different** issue category (`FragmentViewFieldRetentionLeak` — a View-binding memory-leak pattern in `onDestroyView()`), not the missing-constructor/`NoSuchMethodException` defect this investigation is about. **VERIFIED**: this exact defect is not officially acknowledged anywhere in the project's public issue tracker.

Additionally **VERIFIED**: the repository has **no software license** — `gh api repos/saucelabs/my-demo-app-android --jq '.license'` returns `null`, no `LICENSE` file exists in the repository root, and an open issue (#31, "Add a license") requesting one remains unresolved. This is a material fact for any remediation option involving modifying and redistributing a derivative build (Section 10, Option C).

---

## 10. Candidate Remediation Options

| Option | Evidence Strength | Implementation Effort | Risk | Test Compatibility | Reproducibility | Maintenance Burden | Docker Qualification Effect |
|---|---|---|---|---|---|---|---|
| **A — Official newer APK** | N/A — no candidate exists | N/A | N/A | N/A | N/A | N/A | N/A — not applicable |
| **B — Different official stable release** | Weak (**INFERRED**, not verified): the defect predates every tagged release (Section 8's commit history covers 1.0.0 through 2.2.0), so an older release is not expected to avoid it, though not independently source-checked at each older tag | Low (just change the download URL) | Medium — could reintroduce other, already-fixed defects from 2.2.0's own changelog, and locator/UI compatibility with this framework's Page Objects (built against 2.2.0 specifically) is unverified for any other tag | **NOT VERIFIED** — this framework's locators (`MA-LOC-001`) were derived from 2.2.0; an older/different UI could break them | Unknown | Low, but ongoing exposure to a different, unassessed defect surface | None expected, but unverified |
| **C — Build a controlled patched APK from official source** | Strong for feasibility (exact file, exact fix — add one public no-arg constructor — is precisely known and trivial in isolation); weak for legal clarity (no license, Section 9) | Medium — fork, patch, build via Gradle, host the artifact, update `AUT_DOWNLOAD_URL` | Medium — unlicensed-source redistribution ambiguity; a self-built artifact is no longer the "official" AUT, which affects the professional/portfolio framing of "testing an official demo app" | High if done correctly — same UI, one behavioral difference (no crash on relaunch) | High — directly removes the verified defect | Ongoing — a fork must be tracked against upstream, rebuilt, and re-hosted | None to the Docker layer itself; changes what artifact CI downloads |
| **D — Keep current APK, treat as external limitation** | Strongest — directly matches all verified evidence; no assumption required | None | Low — status quo | Full — nothing changes | The intermittent failure will continue at its現 (unquantified) rate | None | None — Docker architecture remains independently verified regardless (Section 12) |
| **E — Add framework-level AUT crash diagnostics (no AUT change)** | Strong precedent: directly analogous to the Phase 19.4B readiness-check pattern, now informed by this phase's evidence | Medium — new future phase of work | Low | Full — no behavior change to the AUT or test logic itself | Does not increase reproduction; improves diagnosis when it recurs | Low-to-medium, ordinary framework maintenance | None |

**Per the phase's explicit instruction, no option is recommended here as "the" fix to implement** — Section 15 lays out the evidence-based path without deciding on behalf of the user.

---

## 11. Compatibility Considerations

This framework's Page Objects and locator repository (`MA-LOC-001`) were built and verified specifically against `2.2.0`'s UI (per this repository's own documentation, Section 3). Any version change (Options A, B, or C with a materially different UI) would require re-verifying every locator against the new build — **NOT VERIFIED** whether `2.2.0`'s UI structure is stable across other releases, since this was not independently checked release-by-release (out of scope: Section 2 explicitly disallows rebuilding a local environment to test compatibility). Option C, if the patch is scoped to only adding the missing constructor (no UI/layout change), would carry the **lowest** compatibility risk of the version-changing options, since the rest of the app would be byte-identical to the already-verified `2.2.0`.

---

## 12. Docker Architecture Impact

**Question 1 — Is the Docker architecture technically verified?** **Yes.** Phases 19.1B, 19.1C, 19.2, 19.3, and 19.4 independently established Docker Model 3 connectivity, execution, and a full 19/19 passing run on GitHub-hosted Linux runners. Phase 19.4J further confirmed the target intermittent failure is unrelated to Docker, Docker networking, the Dockerfile, ADB connectivity, Appium networking, emulator connectivity, or `AndroidDriverFactory`'s readiness logic — all of which remain independently verified and untouched by this finding.

**Question 2 — Is production-level end-to-end reproducibility blocked by AUT instability?** **Yes, separately.** The AUT's own defect (Section 5, 8) can crash the app mid-test regardless of how correctly the Docker/Appium/readiness layers behave, so a fully green, repeatable CI run cannot be guaranteed by any change to those layers alone. These two questions have different, independently-verified answers — the Docker implementation is not "failed," but full reproducibility is separately gated by a defect this project does not own the source of.

---

## 13. Reproducibility Qualification Impact

The original Phase 19.5 "qualification" question (can this framework reliably run on Docker CI) now has a more precise answer than any prior phase could give: the framework and Docker path are sound, but a genuine, external, third-party AUT defect can intermittently crash the app independent of anything this project controls. Whether that is an acceptable, documented risk for qualification purposes (Option D) or something to actively remediate first (Option C or E) is the decision this phase surfaces but does not make.

---

## 14. Risk Assessment

- **Doing nothing (Option D) alone**: the intermittent CI failure persists at an unquantified rate; every future occurrence will need to be manually recognized as "the known AUT crash" rather than investigated fresh (a documentation/tribal-knowledge risk, mitigated by this report and Phase 19.4J's own report existing).
- **Patching without approval**: explicitly disallowed this phase; also carries the license-ambiguity risk noted in Section 9/10 if pursued without deliberate legal consideration.
- **Switching AUT version without compatibility verification (Option B)**: highest-risk option — could silently break locators or introduce a different, unassessed defect, undermining the multi-phase evidence base this framework's Page Objects are built on.
- **Adding crash diagnostics without addressing the crash itself (Option E)**: no risk to correctness, but does not reduce the underlying failure rate — a diagnosis improvement, not a cure.

---

## 15. Recommended Path

Not a decision (this phase does not decide on the user's behalf, per its own instruction), but a factual synthesis to support one: **no official remediation exists** (Options A and B are not viable — 2.2.0 is the latest release, and the defect predates every prior release too). The only path that actually removes the defect is **Option C** (a controlled, source-patched build), which is technically well-evidenced (exact file, exact one-line fix) but requires explicit approval given its cost (a forked/rebuilt/hosted artifact, ongoing maintenance) and an open legal question (no license on the upstream source). **Option D** (accept and document) and **Option E** (improve diagnostics without fixing the AUT) are the lowest-risk, immediately-available paths and are not mutually exclusive with revisiting Option C later.

---

## 16. Evidence Classification

| Claim | Classification |
|---|---|
| Current AUT is `2.2.0`, package `com.saucelabs.mydemoapp.android` | VERIFIED |
| `2.2.0` is the latest official tagged release | VERIFIED |
| `ProductCatalogFragment` lacks a public no-arg constructor in the exact `2.2.0` tag | VERIFIED |
| This exact gap has existed since the file's first commit and remains in the unreleased `main` branch | VERIFIED |
| No official issue/PR documents this specific defect | VERIFIED |
| The repository has no software license | VERIFIED |
| An older official release would avoid this defect | INFERRED (evidence points against it, not independently confirmed per-tag) |
| Any newer/different release is UI-compatible with this framework's existing locators | NOT VERIFIED |
| A self-built patched APK would fully resolve the crash without side effects | INFERRED (the fix is narrowly scoped and precisely known, but not built or tested) |
| The exact system event that triggers the Activity relaunch in CI | NOT VERIFIED (carried over from Phase 19.4J, not re-investigated here) |

---

## 17. Remaining Unknowns

- Whether Sauce Labs is actively maintaining this repository for defect fixes (no response to the open license issue #31, or any other open issue, was found dated recently enough to indicate active issue triage) — **NOT VERIFIED**.
- The `versionCode`/precise build metadata of the `mda-2.2.0-25.apk` binary itself (not independently extracted from the APK; inferred only from the filename and release tag) — **NOT VERIFIED**.
- Whether other Fragments in this AUT share the same missing-no-arg-constructor pattern (only `ProductCatalogFragment` was checked, since it is the one implicated in the verified crash) — **NOT VERIFIED**, out of scope for this phase.
- Whether Sauce Labs would accept a contribution/PR fixing this defect upstream, which would be the cleanest possible remediation path (an official fix) but was not attempted (out of scope: no code changes this phase).

---

## 18. Final Verdict

# AUT SOURCE PATCH PATH AVAILABLE — EXPLICIT APPROVAL REQUIRED

No official remediation exists: `2.2.0` (currently in use) is the latest tagged release, the defect has been present in every release since `1.0.0`, remains unfixed in the unreleased `main` branch, and is not acknowledged in any official issue or pull request. However, a concrete, evidence-based source-patch path is available and precisely scoped: the official source of `ProductCatalogFragment.java` is public, the missing element (a public no-argument constructor) is exactly identified, and no other code in the crash path requires modification. Pursuing this path would mean building and hosting a self-maintained, patched fork of a third-party demo application — a real cost and an open licensing question (the upstream repository has no license) — and is therefore a decision requiring the user's explicit approval, not something this investigation implements or recommends unilaterally. Options D (accept and document as an external limitation) and E (add framework-level crash diagnostics without altering the AUT) remain immediately available, lower-cost alternatives that do not require that approval.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-10 |
| Document Status | Final — Investigation Report (No Fix Implemented, Decision Pending) | — | — |

---

**End of Document — Phase 19.4K AUT Version & Crash Remediation Investigation Report, v1.0**
