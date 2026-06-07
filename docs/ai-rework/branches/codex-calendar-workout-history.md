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
- Rework Commit: `HEAD`
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
- Rework Commit: `HEAD`
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
- Rework Commit: `HEAD`
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
- Rework Commit: `HEAD`
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
- Rework Commit: `HEAD`
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
- Rework Commit: `HEAD`
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
- Rework Commit: `HEAD`
- Verification: `./gradlew :feature:calendar:domain:test :feature:calendar:impl:testDebugUnitTest`; `./gradlew :feature:calendar:api:lintDebug :feature:calendar:impl:lintDebug :app:lintDebug :app:assembleDebug`; `./gradlew :app:installDebug` on emulator-5556 with calendar tab smoke.
- Lesson: Month headers should be formatted as localized date strings, not assembled from separately ordered fragments.

## External Event Coverage
- `PRRT_kwDOSsEQm86Ho7mt`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7mv`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7mx`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7mz`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7m2`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7m4`: covered by rework event above
- `PRRT_kwDOSsEQm86Ho7m5`: covered by rework event above

## Non-Rework Follow-up Commits
- None
