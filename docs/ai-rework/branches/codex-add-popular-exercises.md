# AI Rework Metrics: codex/add-popular-exercises

## Branch Summary
- PR: https://github.com/newgto2026-rgb/smart-trainner-public/pull/68
- Base: `main`
- Initial PR Commit: `6ec46aafd20ffed43e04fec2ed903975a2aac5ed`
- Latest Follow-up Commit: `HEAD`
- Scope: popular exercise catalog entries and exercise art assets

## Rework Events

### USER-2026-05-31-image-framing-scale
- Source: User correction in Codex thread after PR creation
- Severity: P2
- Attribution: AI
- Automation Possible: Partial
- Automation Added: Not Added: visual centering still needs human review, but the background and canvas-size checks were rerun
- Status: verified
- Finding: Some generated exercise images had tight head framing, off-center character placement, or inconsistent character scale across cuts.
- Fix Scope: Exercise art extraction and framing
- Fix Size: Medium
- Rework Commit: `HEAD`
- Verification: `./gradlew :core:exercise-media:testDebugUnitTest`; image asset check confirmed 25 files, expected background `#FAF6ED`, bad=0
- Lesson: Generated exercise art should preserve one source-sheet scale per exercise, use overlap extraction for panel-boundary subjects, and be contact-sheet reviewed for top padding, centering, and character scale before the first PR commit.

## External Event Coverage
- `USER-2026-05-31-image-framing-scale`: covered by rework event above

## Non-Rework Follow-up Commits
- None
