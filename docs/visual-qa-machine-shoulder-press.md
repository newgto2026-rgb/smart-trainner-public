# Machine Shoulder Press Visual QA

Date: 2026-05-21 KST

Scope: manual emulator screenshots in `tmp/manual-shots/`. This note is visual QA only; no app source or assets were edited.

Reviewed screenshots:

- `smart_exercises_machine_shoulder_visible.png`
- `smart_machine_shoulder_dialog_top.png`
- `smart_machine_shoulder_dialog_steps.png`
- Supporting scan: `smart_exercises_top.png`, `smart_exercises_scrolled.png`

## Verdict

Machine Shoulder Press is accepted for the current UI pass.

The list thumbnail is readable at card scale, the full seated machine is identifiable, and the detail dialog shows a coherent seated shoulder press with stable seat, back pad, guide rods, lever arms, handles, and right-side weight stack. The three step thumbnails communicate start, press, and overhead finish clearly enough for a beginner exercise guide.

## Acceptable For Machine Shoulder Press

- The exercise title, category tags, description, movement steps, safety cues, and primary CTA all render without obvious overlap.
- The hero image has enough room to show the trainer, seat, handles, frame, and weight stack.
- Step images are visually distinct: shoulder-height start, upward press, and overhead finish.
- The equipment reads as the same machine across the detail view and steps.
- The list-row thumbnail remains recognizable despite the small card size.

## Remaining Global Thumbnail/Framing Issues

- Several thumbnails still expose source sheet or crop artifacts: vertical panel boundary lines, tiny numbered badges, and small stray marks near image corners.
- Subject scale is inconsistent across exercise cards. Some figures are oversized or edge-cropped, while others are tiny with too much empty cream background.
- A few machine thumbnails clip important equipment edges, which makes the resistance path harder to read at list scale.
- Bottom-of-screen rows can be partly hidden by the navigation bar during scroll capture, so final QA should include a resting scroll position where the selected row is fully visible.
- Global acceptance should wait for a thumbnail normalization pass: remove sheet artifacts, enforce consistent safe padding, and spot-check every category at list-card and dialog-step sizes.
