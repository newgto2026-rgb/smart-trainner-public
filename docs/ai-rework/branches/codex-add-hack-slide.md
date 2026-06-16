# AI Rework Metrics: codex/add-hack-slide

## Branch Summary
- PR: Pending
- Base: `main`
- Initial PR Commit: `HEAD`
- Latest Follow-up Commit: None
- Scope: Hack Slide exercise catalog entry and image QA quarantine

## Rework Events

### USER-2026-06-16-hack-slide-image-qa
- Source: User correction in Codex thread before PR creation
- Severity: P1
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: final Hack Slide equipment and character identity still require human visual review before asset mapping
- Status: verified
- Finding: Generated Hack Slide image candidates did not preserve the accepted Smart Trainer character/background contract and showed incorrect machine geometry such as two foot contact surfaces, feet on the frame/internal support, or duplicated shoes instead of both feet on one large angled footplate.
- Fix Scope: Exercise media mapping and catalog documentation
- Fix Size: Small
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:domain:test :core:exercise-media:testDebugUnitTest :core:exercise-media:lintDebug`; `./gradlew :app:assembleDebug`; `./gradlew :app:lintDebug`
- Lesson: New exercise art should stay unmapped until it passes the fixed canvas/background, character identity, and exercise-specific equipment checks at original image size.

## External Event Coverage
- `USER-2026-06-16-hack-slide-image-qa`: covered by rework event above

## Non-Rework Follow-up Commits
- None
