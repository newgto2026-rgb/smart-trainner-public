package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import org.junit.Test

class SeedTrainingContentTest {
    @Test
    fun exerciseCatalogIsLargeEnoughForMvpAndHasSafeInstructionData() {
        val exercises = SeedTrainingContent.exercises

        assertThat(exercises.size).isAtLeast(72)
        assertThat(exercises.map { it.id.value }.distinct()).hasSize(exercises.size)
        assertThat(exercises.map { it.muscleGroup }.toSet())
            .containsAtLeastElementsIn(concreteExerciseGroups())

        exercises.forEach { exercise ->
            assertThat(exercise.name.trim()).isNotEmpty()
            assertThat(exercise.summary.trim()).isNotEmpty()
            assertThat(exercise.instructions.size).isAtLeast(2)
            assertThat(exercise.instructions.size).isAtMost(5)
            assertThat(exercise.safetyCues.size).isAtLeast(2)
            assertThat(exercise.defaultSets).isAtLeast(1)
            assertThat(exercise.restSeconds).isAtLeast(30)
        }
        assertThat(exercises.map { it.instructions.size }.toSet()).containsAtLeast(2, 3, 4, 5)
    }

    @Test
    fun exerciseCatalogUsesDedicatedImageKeys() {
        SeedTrainingContent.exercises.forEach { exercise ->
            assertThat(exercise.imageKey).isEqualTo(exercise.id.value)
        }
    }

    @Test
    fun exerciseCatalogIncludesPopularMissingBasics() {
        val exerciseIds = SeedTrainingContent.exercises.map { it.id.value }

        assertThat(exerciseIds).containsAtLeast(
            "bodyweight_squat",
            "pullup",
            "dip",
            "bulgarian_split_squat",
            "barbell_romanian_deadlift"
        )
    }

    @Test
    fun kettlebellCatalogCoversSafeBeginnerAndIntermediatePatterns() {
        val kettlebellExercises = SeedTrainingContent.exercises.filter {
            it.equipment == EquipmentType.KETTLEBELL
        }

        assertThat(kettlebellExercises.map { it.id.value }).containsAtLeast(
            "kettlebell_deadlift",
            "kettlebell_goblet_squat",
            "kettlebell_floor_press",
            "one_arm_kettlebell_row",
            "kettlebell_suitcase_carry",
            "kettlebell_farmer_carry",
            "two_hand_kettlebell_swing"
        )
        assertThat(kettlebellExercises).hasSize(18)
        assertThat(kettlebellExercises.map { it.instructions.size }.toSet()).containsAtLeast(3, 4)
        assertThat(kettlebellExercises.map { it.id.value }).doesNotContain("kettlebell_snatch")
        assertThat(kettlebellExercises.map { it.id.value }).doesNotContain("turkish_get_up")
    }

    @Test
    fun unilateralKettlebellExercisesNameOppositeSideCompletion() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        listOf(
            "kettlebell_floor_press",
            "kettlebell_shoulder_press",
            "half_kneeling_kettlebell_press",
            "kettlebell_suitcase_carry",
            "kettlebell_rack_carry"
        ).forEach { exerciseId ->
            val exercise = requireNotNull(exercisesById[exerciseId])
            assertThat(exercise.instructions.joinToString(" ")).contains("반대쪽")
        }
    }

    @Test
    fun kettlebellDeadliftVariantsUseDedicatedImageKeys() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        listOf(
            "kettlebell_deadlift",
            "kettlebell_romanian_deadlift",
            "kettlebell_sumo_deadlift"
        ).forEach { exerciseId ->
            val exercise = requireNotNull(exercisesById[exerciseId])

            assertThat(exercise.imageKey).isEqualTo(exerciseId)
        }
    }

    @Test
    fun correctedCoreExercisesUseDedicatedImageKeys() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        listOf("dead_bug", "pallof_press", "cable_crunch").forEach { exerciseId ->
            val exercise = requireNotNull(exercisesById[exerciseId])

            assertThat(exercise.imageKey).isEqualTo(exerciseId)
        }
    }

    @Test
    fun armExercisesUseSpecificMuscleGroupsWhileKeepingArmsAsRoutineCategory() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        assertThat(exercisesById.getValue("dumbbell_curl").muscleGroup).isEqualTo(MuscleGroup.BICEPS)
        assertThat(exercisesById.getValue("cable_curl").muscleGroup).isEqualTo(MuscleGroup.BICEPS)
        assertThat(exercisesById.getValue("hammer_curl").muscleGroup).isEqualTo(MuscleGroup.BICEPS)
        assertThat(exercisesById.getValue("preacher_curl_machine").muscleGroup).isEqualTo(MuscleGroup.BICEPS)
        assertThat(exercisesById.getValue("triceps_pushdown").muscleGroup).isEqualTo(MuscleGroup.TRICEPS)
        assertThat(exercisesById.getValue("overhead_triceps_extension").muscleGroup)
            .isEqualTo(MuscleGroup.TRICEPS)
        assertThat(exercisesById.getValue("rope_overhead_triceps").muscleGroup)
            .isEqualTo(MuscleGroup.TRICEPS)
        assertThat(exercisesById.getValue("reverse_curl").muscleGroup).isEqualTo(MuscleGroup.FOREARMS)

        val armsDay = SeedTrainingContent.templates
            .first { it.id == "intermediate-body-part-5day" }
            .days
            .first { it.primaryFocus == RoutineFocus.ARMS }

        assertThat(armsDay.secondaryFocuses).containsAtLeast(
            RoutineFocus.BICEPS,
            RoutineFocus.TRICEPS
        )
    }

    @Test
    fun planTemplatesReferenceExistingExercisesOnly() {
        val exerciseIds = SeedTrainingContent.exercises.map { it.id }.toSet()

        SeedTrainingContent.templates.forEach { template ->
            assertThat(template.days).hasSize(template.daysPerWeek)
            assertThat(template.cycleLength).isEqualTo(template.days.size)
            template.days.forEach { day ->
                assertThat(day.exercises).isNotEmpty()
                day.exercises.forEach { planned ->
                    assertThat(exerciseIds).contains(planned.exerciseId)
                }
            }
        }
    }

    @Test
    fun routinePresetIdsAndStructuresCoverMvp() {
        val templatesById = SeedTrainingContent.templates.associateBy { it.id }

        assertThat(templatesById.keys).containsAtLeast(
            "beginner-full-body-2day",
            "beginner-full-body-3day",
            "intermediate-balanced-4day",
            "intermediate-body-part-4day-30",
            "intermediate-body-part-4day-60",
            "intermediate-body-part-4day"
        )
        assertThat(templatesById.getValue("beginner-full-body-2day").structure)
            .isEqualTo(RoutineStructure.FULL_BODY)
        assertThat(templatesById.getValue("beginner-full-body-3day").structure)
            .isEqualTo(RoutineStructure.FULL_BODY)
        assertThat(templatesById.getValue("intermediate-balanced-4day").structure)
            .isEqualTo(RoutineStructure.BALANCED_SPLIT)
        assertThat(templatesById.getValue("intermediate-body-part-4day").structure)
            .isEqualTo(RoutineStructure.BODY_PART_SPLIT)
    }

    @Test
    fun bodyPartSplitDurationVariantsChangeDailyExerciseCount() {
        val thirty = SeedTrainingContent.templates.first { it.id == "intermediate-body-part-4day-30" }
        val fortyFive = SeedTrainingContent.templates.first { it.id == "intermediate-body-part-4day" }
        val sixty = SeedTrainingContent.templates.first { it.id == "intermediate-body-part-4day-60" }

        assertThat(thirty.sessionMinutes).isEqualTo(30)
        assertThat(fortyFive.sessionMinutes).isEqualTo(45)
        assertThat(sixty.sessionMinutes).isEqualTo(60)
        assertThat(thirty.days.map { it.exercises.size }.toSet()).containsExactly(4)
        assertThat(fortyFive.days.map { it.exercises.size }.toSet()).containsExactly(6)
        assertThat(sixty.days.map { it.exercises.size }.toSet()).containsExactly(7)
    }

    @Test
    fun bodyPartSplitPresetsHaveFocusedDaysAndRequiredCoverage() {
        val bodyPartTemplates = SeedTrainingContent.templates.filter {
            it.structure == RoutineStructure.BODY_PART_SPLIT
        }
        val fourDay = SeedTrainingContent.templates.first { it.id == "intermediate-body-part-4day" }

        bodyPartTemplates.forEach { template ->
            template.days.forEach { day ->
                assertThat(day.primaryFocus).isNotEqualTo(RoutineFocus.FULL_BODY)
                assertThat(day.minRecoveryHours).isAtLeast(24)
                val bodyPartSecondaryFocuses = day.secondaryFocuses.filterNot {
                    it.isMovementDirectionFocus()
                }
                if (day.isCombinedFocusDay()) {
                    assertThat(bodyPartSecondaryFocuses).isNotEmpty()
                } else {
                    assertThat(bodyPartSecondaryFocuses).isEmpty()
                }
            }
            template.days.filter { it.primaryFocus == RoutineFocus.CHEST }.forEach { day ->
                assertThat(day.secondaryFocuses).contains(RoutineFocus.PUSH)
            }
            template.days.filter { it.primaryFocus == RoutineFocus.BACK }.forEach { day ->
                assertThat(day.secondaryFocuses).contains(RoutineFocus.PULL)
            }
        }
        assertThat(fourDay.days.map { it.primaryFocus }).containsAtLeast(
            RoutineFocus.BACK,
            RoutineFocus.CHEST,
            RoutineFocus.LOWER_BODY,
            RoutineFocus.SHOULDERS
        )
        assertThat(fourDay.focusSummary).containsAtLeast(
            RoutineFocus.ARMS,
            RoutineFocus.BICEPS,
            RoutineFocus.TRICEPS,
            RoutineFocus.PUSH,
            RoutineFocus.PULL
        )
    }

    @Test
    fun bodyPartSplitExercisesMatchDeclaredFocusesAndFocusedRatio() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id }
        val bodyPartTemplates = SeedTrainingContent.templates.filter {
            it.structure == RoutineStructure.BODY_PART_SPLIT
        }

        bodyPartTemplates.forEach { template ->
            template.days.forEach { day ->
                val primaryFocus = requireNotNull(day.primaryFocus)
                val declaredFocuses = (listOf(primaryFocus) + day.secondaryFocuses).toSet()
                val allowedGroups = declaredFocuses.flatMap { it.allowedMuscleGroups() }.toSet()
                val groups = day.exercises.map { planned ->
                    requireNotNull(exercisesById[planned.exerciseId]).muscleGroup
                }
                val primaryGroups = primaryFocus.allowedMuscleGroups()
                val primaryCount = groups.count { it in primaryGroups }

                assertThat(groups).containsNoneIn(MuscleGroup.entries.filterNot { it in allowedGroups })
                if (day.isCombinedFocusDay()) {
                    day.secondaryFocuses.forEach { focus ->
                        assertThat(groups.count { it in focus.allowedMuscleGroups() }).isAtLeast(1)
                    }
                    assertThat(primaryCount.toDouble() / groups.size).isAtLeast(0.50)
                } else {
                    assertThat(primaryCount).isEqualTo(groups.size)
                }
            }
        }
    }

    @Test
    fun balancedSplitUpperDaysUseUpperBodyFocusInsteadOfSingleBodyPartFocus() {
        val balanced = SeedTrainingContent.templates.first { it.id == "intermediate-balanced-4day" }
        val upperDays = balanced.days.filter { it.title.startsWith("상체") }

        assertThat(upperDays.map { it.primaryFocus }).containsExactly(
            RoutineFocus.UPPER_BODY,
            RoutineFocus.UPPER_BODY
        )
    }

    @Test
    fun beginnerTemplatesDoNotIncludeHighFrequencyBodyPartSplit() {
        val riskyBeginnerTemplates = SeedTrainingContent.templates.filter {
            it.recommendedExperience == TrainingExperience.BEGINNER &&
                it.structure == RoutineStructure.BODY_PART_SPLIT &&
                it.daysPerWeek >= 5
        }

        assertThat(riskyBeginnerTemplates).isEmpty()
    }

    private fun PlanTemplateDay.isCombinedFocusDay(): Boolean = title.contains("+")

    private fun RoutineFocus.allowedMuscleGroups(): Set<MuscleGroup> = when (this) {
        RoutineFocus.FULL_BODY -> MuscleGroup.entries.toSet()
        RoutineFocus.UPPER_BODY -> setOf(
            MuscleGroup.CHEST,
            MuscleGroup.BACK,
            MuscleGroup.SHOULDERS,
            MuscleGroup.ARMS,
            MuscleGroup.BICEPS,
            MuscleGroup.TRICEPS,
            MuscleGroup.FOREARMS
        )
        RoutineFocus.PUSH -> setOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
        RoutineFocus.PULL -> setOf(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
        RoutineFocus.CHEST -> setOf(MuscleGroup.CHEST)
        RoutineFocus.BACK -> setOf(MuscleGroup.BACK)
        RoutineFocus.LOWER_BODY -> setOf(MuscleGroup.LOWER_BODY)
        RoutineFocus.SHOULDERS -> setOf(MuscleGroup.SHOULDERS)
        RoutineFocus.ARMS -> setOf(
            MuscleGroup.ARMS,
            MuscleGroup.BICEPS,
            MuscleGroup.TRICEPS,
            MuscleGroup.FOREARMS
        )
        RoutineFocus.BICEPS -> setOf(MuscleGroup.BICEPS)
        RoutineFocus.TRICEPS -> setOf(MuscleGroup.TRICEPS)
        RoutineFocus.FOREARMS -> setOf(MuscleGroup.FOREARMS)
        RoutineFocus.CARDIO_CONDITIONING -> setOf(MuscleGroup.CARDIO)
        RoutineFocus.CORE -> setOf(MuscleGroup.CORE)
    }

    private fun concreteExerciseGroups(): Set<MuscleGroup> =
        MuscleGroup.entries.toSet() - MuscleGroup.ARMS

    private fun RoutineFocus.isMovementDirectionFocus(): Boolean =
        this == RoutineFocus.PUSH || this == RoutineFocus.PULL
}
