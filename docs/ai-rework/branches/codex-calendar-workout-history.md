# AI Rework Metrics: codex/calendar-workout-history

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/100
- Base: `main`
- Initial PR Commit: `d36e3b45a5cf9e6a61c2a1205f5f0ef2da21cc71`
- Latest Follow-up Commit: `HEAD`
- Scope: workout history calendar feature modules, navigation tab, month grid, agenda, and collapse controls

## Rework Events

### PRRT_kwDOSsEQm86Ho7mt
- Source: GitHub review thread on `feature/calendar/domain/src/main/java/com/smarttrainner/feature/calendar/domain/WorkoutCalendarUseCases.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: existing aggregation tests cover behavior, but allocation-level efficiency is better reviewed through implementation.
- Status: verified
- Finding: Month filtering allocated extra date objects and sorted the full month list before per-date descending sorts.
- Fix Scope: Calendar domain month filtering and redundant full-list sort removal.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Calendar aggregation should avoid whole-list ordering when each grouped date owns the final display order.

### PRRT_kwDOSsEQm86Ho7mv
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/CalendarViewModel.kt`
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: Month and selected-date state could update before the database-backed month flow emitted current data, briefly rendering a new month with old summaries.
- Fix Scope: Calendar ViewModel stale-month guard and transition regression test.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Combined UI state that mixes immediate state and repository state needs an identity guard before rendering derived data.

### PRRT_kwDOSsEQm86Ho7mx
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/components/CalendarMonthGrid.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: locale-sensitive accessibility text is better verified by resource/formatter usage and manual smoke unless repeated.
- Status: verified
- Finding: Calendar day accessibility text used a hard-coded date pattern instead of localized full-date formatting.
- Fix Scope: Calendar day accessibility date formatter.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Accessibility labels should use localized formatters instead of app-owned display shortcuts.

### PRRT_kwDOSsEQm86Ho7mz
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/components/CalendarMonthGrid.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: No
- Automation Added: Not Added: redundant helper removal is a local maintainability cleanup.
- Status: verified
- Finding: Calendar grid defined a redundant `DayOfWeek.plus` helper even though `java.time.DayOfWeek` already provides `plus`.
- Fix Scope: Calendar grid helper cleanup.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Prefer standard library date helpers unless a custom policy is explicitly different.

### PRRT_kwDOSsEQm86Ho7m2
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/components/CalendarAgendaSection.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: resource-backed date pattern usage is directly visible in implementation and lint covers resource consistency.
- Status: verified
- Finding: Selected-date agenda labels hard-coded locale-specific date patterns in Kotlin.
- Fix Scope: Resource-backed selected-date format pattern for agenda labels.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Locale-specific display patterns belong in resources so translation changes do not require Kotlin edits.

### PRRT_kwDOSsEQm86Ho7m4
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/components/CalendarAgendaSection.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: No
- Automation Added: Not Added: helper removal is coupled to the resource-backed date pattern fix.
- Status: verified
- Finding: The agenda selected-date helper would become unnecessary after moving the pattern to resources.
- Fix Scope: Agenda selected-date helper removal.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Once a formatting policy is resource-backed, keep Kotlin helpers limited to behavior that cannot live in resources.

### PRRT_kwDOSsEQm86Ho7m5
- Source: GitHub review thread on `feature/calendar/impl/src/main/java/com/smarttrainner/feature/calendar/impl/components/CalendarTopHeader.kt`
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: month/year ordering is locale-sensitive UI polish verified through localized formatter use and emulator smoke.
- Status: verified
- Finding: Calendar header rendered month then year in a fixed order, producing incorrect ordering for Korean.
- Fix Scope: Localized month/year header label using Android best-date pattern.
- Fix Size: Small
- Rework Commit: `4df1381e`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Month headers should be formatted as localized date strings, not assembled from separately ordered fragments.

### USER-20260607-calendar-latest-main-collapse-ui
- Source: User correction in Codex thread: latest MyFirstApp `origin/main` was not referenced, calendar tab/collapse/UI needed emulator-level verification, and workout time information should not be shown as meaningful history.
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Yes
- Status: verified
- Finding: Calendar implementation missed MyFirstApp latest month/week collapse persistence, rendered cramped numeric day indicators, and exposed workout time/duration information that can imply real-time logging accuracy.
- Fix Scope: Added feature-owned calendar preferences domain/data separation, persisted month/week expansion, MyFirstApp-style selected-week rendering, stable dot-only day indicators, shorter header copy, and removed time/duration display from calendar agenda items.
- Fix Size: Medium
- Rework Commit: `eeb5a1d0`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest :core:datastore:testDebugUnitTest`; `./gradlew :feature:calendar:data:testDebugUnitTest :feature:calendar:api:lintDebug :feature:calendar:data:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :feature:calendar:impl:lintDebug :app:assembleDebug`; guarded `./gradlew :app:installDebug` on emulator-5556 with screenshots `tmp/manual-shots/calendar_recheck_04_final_week.png` and `tmp/manual-shots/calendar_recheck_05_workout_item.png`; guarded `./gradlew connectedDebugAndroidTest`.
- Lesson: Reference projects must be fetched and read from latest `origin/main`, and visual acceptance for tab/collapse work needs actual emulator smoke screenshots before PR updates.

### GHA-27088507005-ui-excluded-coverage
- Source: GitHub Actions run `27088507005`, check `Build, Unit Test, Lint`, step `Run JVM unit tests`.
- Severity: P1
- Attribution: AI
- Automation Possible: Yes
- Automation Added: Yes
- Status: verified
- Finding: The CI-only `uiExcludedTestCoverageVerification` gate failed because `UpdateCalendarMonthExpandedUseCase.invoke` was represented as an uncovered expression-body line in the JaCoCo gate report.
- Fix Scope: Calendar preferences update use case coverage stabilization and repository failure delegation test.
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew --stacktrace :feature:calendar:domain:test uiExcludedTestCoverageVerification -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`; `CI=true ./gradlew --stacktrace test -Psmarttrainner.serverBaseUrl=https://ci.smart-trainner.invalid/`.
- Lesson: New domain use cases included in the 100% UI-excluded coverage gate need the full CI `test` coverage gate, not only module tests, before push.

## External Event Coverage
- `PRRT_kwDOSsEQm86Ho7mt`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7mv`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7mx`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7mz`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7m2`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7m4`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7m5`: covered by rework event above
- `USER-20260607-calendar-latest-main-collapse-ui`: covered by rework event above
- `GHA-27088507005-ui-excluded-coverage`: covered by rework event above

## Non-Rework Follow-up Commits
- None
