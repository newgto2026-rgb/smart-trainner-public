# Training Analysis PRD

## 1. Product Intent

Smart Trainner should become more than a workout log. After a user records one or more routine cycles, the app should answer simple questions visually:

- Am I getting stronger?
- Did my total training work increase?
- Which body parts did I train enough?
- Which exercises are stalling?
- Is this cycle taking longer than usual?
- Am I accumulating fatigue?
- What should I pay attention to next?

The first implementation should not require AI. Rule-based calculations should produce stable metrics, charts, and short Korean insight text. AI can be added later as an explanation layer, but the source of truth must remain deterministic calculation logic.

## 2. User Promise

The user should understand their training status in 10 seconds without reading technical terms.

The product should say:

- "벤치프레스 힘이 최근 4주 동안 올랐어요."
- "이번 사이클은 지난 사이클보다 3일 더 오래 걸렸어요."
- "등 운동량이 가슴보다 부족해요."
- "운동량은 늘었지만 힘 지표는 정체되어 있어요."

The product should avoid saying:

- "e1RM slope is below threshold."
- "Volume load trend variance indicates monotony."
- "Mesocycle density deviation is high."

Advanced terms may exist in "자세히 보기", but primary cards must be visual and plain.

## 3. Data Foundation

### Required Now

- Exercise date and time
- Routine template id
- Routine cycle number
- Routine day index
- Cycle started at
- Last completed day
- Last completed at
- Last completed cycle duration days
- Exercise id
- Planned exercise id
- Sets, reps, weight
- Duration minutes for time-based exercises
- Exercise to muscle group mapping

### Recommended Later

- User body weight
- Session duration
- RPE or RIR
- Rest time per set
- Pain flag
- Sleep and condition
- Body measurements
- Progress photos

## 4. Key Concepts And Friendly Names

| Internal Concept | User-Facing Name | Meaning |
| --- | --- | --- |
| e1RM | 예상 최대 중량 | 지금 기록으로 추정한 최대 힘 |
| Volume Load | 총 운동량 | 중량 x 반복수의 합 |
| Hard Set | 성장 세트 | 성장을 만들 가능성이 높은 실제 운동 세트 |
| Intensity Zone | 강도 구간 | 가벼움, 보통, 무거움 |
| Fatigue Index | 세트 유지력 | 뒤 세트까지 반복수를 얼마나 유지했는지 |
| Plateau | 정체 신호 | 최근 몇 주간 힘 변화가 거의 없는 상태 |
| Muscle Balance | 부위 균형 | 가슴, 등, 하체 등 훈련 분포 |
| Relative Strength | 체중 대비 힘 | 체중을 고려한 힘 |
| Cycle Duration | 사이클 소요 일수 | 한 사이클을 끝내는 데 걸린 일수 |

## 5. Core Metrics

### 5.1 Exercise Strength Trend

Use estimated 1RM for rep-based strength exercises.

Formula:

```text
estimated max = weight x (1 + reps / 30)
```

Rules:

- Prefer 1-10 rep sets.
- Mark high-rep estimates as lower confidence.
- Show trend from best set per session and rolling 4-week average.

Visuals:

- Line chart: estimated max over time
- Dots: actual set records
- Badge: new PR
- Small card: recent 4-week change

### 5.2 Total Training Work

Formula:

```text
volume = sum(weight x reps)
```

Rules:

- Use per exercise, muscle group, routine day, cycle, and month.
- Do not compare unrelated exercises as if they are equal.

Visuals:

- Weekly stacked bar
- Cycle total bar
- Month-over-month comparison
- Muscle group volume heatmap

### 5.3 Growth Sets

Initial rule:

```text
growth set = completed rep set with 5-30 reps and a recorded load
```

Later rule with RIR:

```text
growth set = completed set with 0-4 RIR
```

Visuals:

- Muscle group weekly set bars
- "충분 / 부족 / 과다 가능" labels
- Body map heat color

### 5.4 Cycle Duration

A routine cycle is not always one week. A cycle ends when the user completes the last routine day and the next day index wraps to 0.

When a cycle ends:

```text
cycle duration days = ceil(elapsed time between cycle start and completed at, in days)
minimum = 1
```

Each completed cycle must be stored as its own history row, not only as a
latest value on the active progress state. This allows analysis like:

```text
cycle 1 = 7 days
cycle 2 = 14 days
cycle 3 = 7 days
total completed-cycle span = 28 days
```

Examples:

- Same-day completion: 1 day
- 6 days and 2 hours: 7 days
- 9 days exactly: 9 days

Visuals:

- Cycle timeline
- "이번 사이클 9일" badge
- Previous cycle comparison
- Bar chart: cycle 1, cycle 2, cycle 3 duration

User text:

- "이번 사이클은 9일 걸렸어요."
- "지난 사이클보다 2일 빠르게 완료했어요."
- "사이클 간격이 길어지고 있어요. 운동 빈도를 확인해보세요."

### 5.5 Fatigue And Set Retention

Formula:

```text
set retention = last set reps / first set reps
fatigue drop = 1 - set retention
```

Rules:

- Compare only same exercise and similar weight.
- Use as a signal, not a medical or injury claim.

Visuals:

- Set-by-set line chart
- Reps retention bar
- "안정 / 보통 / 피로 신호" badge

### 5.6 Plateau Signal

Initial rule:

```text
plateau = 4-6 weeks with low estimated max change and no PR
```

Supporting signals:

- Volume increased but strength did not.
- Same weight reps decreased.
- Set retention decreased.

Visuals:

- Trend chart with flat band
- "정체 신호" card
- Strength vs volume comparison

### 5.7 Muscle Balance

Movement and muscle group categories:

- Push
- Pull
- Squat
- Hinge
- Lunge
- Core
- Carry
- Cardio

Visuals:

- Push/Pull balance bar
- Upper/Lower donut
- Squat/Hinge balance bar
- Muscle heatmap
- Radar chart for advanced view

## 6. Main Screens

### 6.1 Analysis Home

Purpose:

Show the user's current status at a glance.

Top cards:

- Strength change
- Total work
- Current cycle duration
- Warning signal

Primary visuals:

- Weekly training work bar chart
- Current cycle progress ring
- Muscle group heatmap
- PR timeline

### 6.2 Exercise Detail Analysis

Purpose:

Help the user understand one exercise deeply.

Visuals:

- Estimated max line chart
- Actual set scatter plot
- PR markers
- Set retention chart
- Volume trend

Plain text examples:

- "최근 4주 동안 예상 최대 중량이 5% 올랐어요."
- "같은 중량에서 반복수가 늘고 있어요."
- "운동량은 늘었지만 최고 기록은 정체되어 있어요."

### 6.3 Muscle Balance Analysis

Purpose:

Show what the user trained too much or too little.

Visuals:

- Muscle group weekly sets
- Body part heatmap
- Push/Pull bar
- Upper/Lower donut

Plain text examples:

- "등 운동이 가슴보다 적어요."
- "하체 운동 빈도가 낮아요."
- "상체와 하체 균형이 좋아요."

### 6.4 Cycle Report

Purpose:

Make routine cycles measurable even when they are not weekly.

Visuals:

- Cycle duration bar chart
- Cycle completion timeline
- Cycle volume comparison
- Cycle PR count
- Cycle muscle balance

Plain text examples:

- "3사이클은 8일 걸렸고, 2사이클보다 2일 빨랐어요."
- "이번 사이클은 총 운동량이 12% 늘었어요."
- "완료 속도는 좋아졌지만 하체 세트가 부족했어요."

### 6.5 Monthly Report

Purpose:

Summarize the user's month in a shareable and motivating way.

Visuals:

- Calendar heatmap
- PR timeline
- Muscle group distribution
- Top improved exercises
- Cycle duration trend

## 7. Phase Plan

### Phase 1: Data Foundation And Basic Strength Trend

Goal:

Make growth visible from existing logs.

Scope:

- Estimated max calculation
- Exercise PRs
- Weekly total work
- Basic analysis home cards
- Store completed cycle duration history rows

Success:

- User can see whether a main exercise improved.
- App can compare cycle 1, cycle 2, cycle 3 durations instead of keeping only the latest value.

### Phase 2: Muscle Balance

Goal:

Show whether training is balanced.

Scope:

- Exercise to muscle group mapping
- Muscle group set count
- Push/Pull and Upper/Lower visuals
- Heatmap

Success:

- User can identify weakly trained areas within 5 seconds.

### Phase 3: Cycle Report

Goal:

Treat routine cycles as first-class analysis units.

Scope:

- Current cycle summary
- Completed cycle duration chart
- Cycle volume comparison
- Cycle PR summary
- Cycle completion timeline

Success:

- User can compare cycle 1, 2, 3 even if each took different days.

### Phase 4: Plateau And Fatigue Signals

Goal:

Give useful warnings without overclaiming.

Scope:

- Plateau signal
- Set retention
- Strength vs volume comparison
- Simple recommendation status

Success:

- User understands whether they are progressing, stalling, or accumulating fatigue.

### Phase 5: Monthly Report

Goal:

Make the user's progress memorable.

Scope:

- Monthly summary screen
- PR timeline
- Calendar heatmap
- Best exercise and weakest area
- Share-friendly report layout

Success:

- User can review one month without opening each workout.

### Phase 6: Advanced Metrics

Goal:

Support serious lifters without overwhelming beginners.

Scope:

- Bodyweight-relative strength
- DOTS or Wilks-like score for squat, bench, deadlift
- Intensity zone distribution
- Training density
- RPE/RIR-based growth sets

Success:

- Advanced users can inspect deeper metrics from detail screens.

### Phase 7: Optional AI Coach

Goal:

Make deterministic analysis easier to understand.

Rules:

- AI must not invent metrics.
- AI only explains calculated results.
- Every AI sentence should be traceable to a metric card.

Success:

- User gets friendlier coaching text without losing trust.

## 8. Design Principles

- Every important metric needs a visual.
- Primary labels must be plain Korean.
- Red should mean "확인 필요", not panic.
- Charts should compare recent history, not isolated numbers.
- Cards should show "why" in a details drawer.
- Beginner screens should hide formulas.
- Advanced screens can reveal formulas and confidence notes.

## 9. First Implementation Checklist

- Add completed cycle duration history rows to server DB.
- Expose cycle duration history through app network DTOs and repository models.
- Add estimated max calculator.
- Add weekly volume calculator.
- Add exercise trend chart.
- Add muscle group mapping.
- Add cycle report data model.
- Add chart components for bars, lines, timeline, and heatmap.

## 10. Non-Goals For First Release

- No medical injury prediction.
- No claim that muscle mass directly increased from logs alone.
- No AI-generated analysis as source of truth.
- No complicated periodization editor.
- No leaderboards before strength data quality is stable.
