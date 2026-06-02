# AI Rework Metrics: codex/workout-time-estimates

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/76
- Base: `main`
- Initial PR Commit: `ee31047`
- Latest Follow-up Commit: `HEAD`
- Scope: time-aware routine estimates, dynamic routine recommendation filters, balanced routine coverage, and latest main design merge

## Rework Events

### PRRT_kwDOSsEQm86GZAiF
- Source: GitHub review thread on `core/domain/src/main/java/com/smarttrainner/core/domain/SeedTrainingContent.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: source search can catch repeated list scans, but a project lint rule for this one seed catalog pattern would be disproportionate.
- Status: verified
- Finding: Generated template construction repeatedly performed linear exercise list scans instead of using an indexed lookup.
- Fix Scope: Pre-indexed seed exercises and routed template construction through O(1) lookup.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `rg -n "exercises\\.first|exerciseById|exercisesById|exerciseEstimateSeconds" core/domain/src/main/java/com/smarttrainner/core/domain/SeedTrainingContent.kt`; `./gradlew :core:domain:test :feature:routine:domain:test :app:assembleDebug`
- Lesson: Large generated seed catalogs should build lookup indexes before composing templates so generation cost scales with selected exercises, not the full catalog each time.

### PRRT_kwDOSsEQm86GZAiL
- Source: GitHub review thread on `core/domain/src/main/java/com/smarttrainner/core/domain/SeedTrainingContent.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: source search can catch this exact lookup, while behavior tests already cover the generated templates.
- Status: verified
- Finding: `exerciseEstimateSeconds()` used `exercises.first { ... }`, adding avoidable O(N) lookup cost inside generated coverage selection.
- Fix Scope: Replaced estimate lookup with shared `exerciseById()` map access.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `rg -n "exercises\\.first|exerciseById|exercisesById|exerciseEstimateSeconds" core/domain/src/main/java/com/smarttrainner/core/domain/SeedTrainingContent.kt`; `./gradlew :core:domain:test :feature:routine:domain:test :app:assembleDebug`
- Lesson: Helper methods used inside greedy template selection should avoid hidden linear scans.

## External Event Coverage
- `PRRT_kwDOSsEQm86GZAiF`: covered by rework event above
- `PRRT_kwDOSsEQm86GZAiL`: covered by rework event above

## Non-Rework Follow-up Commits
- `0b5f3da`: merged latest `origin/main` design, branding, account sync, and routine cycle runner changes before PR review feedback arrived.
