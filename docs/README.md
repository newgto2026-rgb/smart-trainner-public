# Smart Trainner 문서 인덱스

이 폴더는 Smart Trainner의 제품 의도, 기술 설계, QA, 브랜드/운동 이미지 기준, AI-assisted development 기록을 보관한다.

문서 성격은 둘로 나뉜다. `SMART-TRAINNER-*` 문서는 처음 읽기 좋은 대표 소개서이고, 기존 PRD/TRD/QA 문서는 기능 설계와 작업 이력을 보존하는 내부 문서다.

## 빠른 시작

| 문서 | 목적 |
|---|---|
| [루트 README](../README.md) | 프로젝트 목적, 앱 개요, 아키텍처, 작업 정책 요약 |
| [앱 구조 소개서](SMART-TRAINNER-APP-OVERVIEW.md) | 제품 경험, 모듈 구조, 데이터 흐름, 테스트 기반 설명 |
| [AI 기반 개발 사례 소개서](SMART-TRAINNER-AI-DEVELOPMENT-STORY.md) | AI-assisted 작업 방식과 저장소 하네스 정리 |
| [현재 사이클 데이터 구조 분석 PDF](cycle-data-structure-analysis.pdf) | Home/Routine/Analysis가 공유하는 현재 사이클 데이터 구조와 정책 분석 |
| [품질 게이트](agent/quality-gates.md) | 로컬/CI 검증 명령과 언제 무엇을 돌릴지 |
| [Agent 작업 가이드](../AGENTS.md) | Codex/agent가 따라야 할 저장소 규칙 |

## 기능 설계 문서

| 문서 | 범위 |
|---|---|
| [커스텀 루틴 PRD](custom-routines-prd.md) | 커스텀 루틴 제품 요구사항 |
| [커스텀 루틴 TRD](custom-routines-trd.md) | 커스텀 루틴 기술 설계 |
| [루틴 진행 PRD](routine-progression-prd.md) | 루틴 사이클/일차 진행 정책 |
| [루틴 진행 TRD](routine-progression-trd.md) | 루틴 진행 repository/use case/data 설계 |
| [운동 분석 PRD](training-analysis-prd.md) | 분석 화면과 요약 지표 요구사항 |

## 아키텍처/UX/QA 기록

| 문서 | 범위 |
|---|---|
| [모듈화 단계](architecture/modularization-phases.md) | feature/core 모듈화 진행 단계 |
| [아키텍처 라운드](architecture-rounds.md) | 구조 검토 기록 |
| [UX 라운드](ux-rounds.md) | 화면/경험 검토 기록 |
| [현재 앱 Visual QA](visual-qa-current-app.md) | 주요 화면 시각 QA |
| [머신 숄더 프레스 Visual QA](visual-qa-machine-shoulder-press.md) | 특정 운동 이미지 회귀 QA |

## 운동 이미지와 브랜드 기록

| 문서 | 범위 |
|---|---|
| [운동 이미지 메인 기준](exercise-image-main-standard.md) | 운동 이미지 기본 스타일/품질 기준 |
| [운동 이미지 품질 게이트](exercise-art-quality-gate.md) | 이미지 검수 기준 |
| [운동 이미지 방향](exercise-art-direction.md) | 이미지 제작 방향 |
| [운동 이미지 전체 감사](exercise-image-full-audit-20260521.md) | 운동 이미지 전수 점검 기록 |
| [운동 이미지 QA](exercise-image-qa-20260521.md) | 이미지 QA 결과 |
| [브랜드 assets](brand) | 로고/워드마크/런처 심볼 시안과 선택본 |

## AI 재작업 관측성 기록

| 문서 | 범위 |
|---|---|
| [브랜치별 재작업 기록](ai-rework/branches) | 리뷰/CI/후속 커밋을 브랜치 단위로 기록 |

## 문서 작성 원칙

- 제품 정책은 PRD에 먼저 고정하고, 구현 세부사항은 TRD에 둔다.
- Android/server API 계약이 바뀌면 Android 문서와 companion server 문서를 함께 확인한다.
- UI 문서는 화면 설명만 쓰지 말고 어떤 domain 정책을 사용자에게 보여주는지까지 적는다.
- QA 문서는 "통과"만 쓰지 말고 재현 조건, 기대 결과, 남은 리스크를 남긴다.
- AI-assisted 작업에서 리뷰나 CI로 재작업이 생기면 `docs/ai-rework/branches`에 원인과 조치를 기록한다.
