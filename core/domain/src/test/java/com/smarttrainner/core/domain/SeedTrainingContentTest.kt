package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.core.model.ExerciseMovementPattern
import com.smarttrainner.core.model.ExerciseMuscleRole
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.estimatedSessionMinutes
import com.smarttrainner.core.model.estimatedTotalSeconds
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
            assertWithMessage(exercise.id.value).that(exercise.muscleGroups).isNotEmpty()
            assertWithMessage(exercise.id.value).that(exercise.muscleGroups).contains(exercise.muscleGroup)
            assertWithMessage(exercise.id.value)
                .that(exercise.muscleGroups.distinct())
                .containsExactlyElementsIn(exercise.muscleGroups)
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
    fun compoundExercisesStoreEveryTargetedMuscleGroup() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        assertThat(exercisesById.getValue("bodyweight_squat").muscleGroups)
            .containsAtLeast(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.CORE)
        assertThat(exercisesById.getValue("goblet_squat").muscleGroups)
            .containsAtLeast(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.CORE)
        assertThat(exercisesById.getValue("seated_cable_row").muscleGroups)
            .containsAtLeast(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
        assertThat(exercisesById.getValue("pushup").muscleGroups)
            .containsAtLeast(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.CORE)
        assertThat(exercisesById.getValue("rowing_machine").muscleGroups)
            .containsAtLeast(MuscleGroup.CARDIO, MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.ARMS)
    }

    @Test
    fun compoundExercisesExposePrimaryAndSecondaryMuscleRoles() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }
        val deadlift = exercisesById.getValue("conventional_deadlift")

        assertThat(deadlift.roleFor(MuscleGroup.FULL_BODY)).isEqualTo(ExerciseMuscleRole.PRIMARY)
        assertThat(deadlift.roleFor(MuscleGroup.BACK)).isEqualTo(ExerciseMuscleRole.SECONDARY)
        assertThat(deadlift.roleFor(MuscleGroup.LOWER_BODY)).isEqualTo(ExerciseMuscleRole.SECONDARY)
        assertThat(deadlift.roleFor(MuscleGroup.CORE)).isEqualTo(ExerciseMuscleRole.SECONDARY)
        assertThat(deadlift.involvedMuscleGroups)
            .containsAtLeast(MuscleGroup.FULL_BODY, MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.CORE)
    }

    @Test
    fun exerciseCatalogStoresMovementSortMetadata() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        assertThat(exercisesById.getValue("barbell_back_squat").movementPattern)
            .isEqualTo(ExerciseMovementPattern.SQUAT)
        assertThat(exercisesById.getValue("leg_press").movementPattern)
            .isEqualTo(ExerciseMovementPattern.LEG_PRESS)
        assertThat(exercisesById.getValue("conventional_deadlift").movementPattern)
            .isEqualTo(ExerciseMovementPattern.HINGE)
        assertThat(exercisesById.getValue("barbell_back_squat").variantRank)
            .isLessThan(exercisesById.getValue("box_squat").variantRank)
        assertThat(exercisesById.getValue("conventional_deadlift").variantRank)
            .isLessThan(exercisesById.getValue("romanian_deadlift").variantRank)
        assertThat(SeedTrainingContent.exercises.map { it.catalogOrder }.distinct())
            .hasSize(SeedTrainingContent.exercises.size)
    }

    @Test
    fun exerciseCatalogUsesDedicatedImageKeys() {
        SeedTrainingContent.exercises.forEach { exercise ->
            assertThat(exercise.imageKey).isEqualTo(exercise.id.value)
        }
    }

    @Test
    fun exerciseCatalogStoresRepDurationForRoutineTimeEstimates() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id }

        SeedTrainingContent.exercises
            .filter { it.defaultRepRange != null }
            .forEach { exercise ->
                assertThat(exercise.defaultRepDurationSeconds).isAtLeast(3)
                assertThat(exercise.defaultRepDurationSeconds).isAtMost(8)
            }

        SeedTrainingContent.templates.forEach { template ->
            template.days.forEach { day ->
                day.exercises.forEach { templateExercise ->
                    val exercise = requireNotNull(exercisesById[templateExercise.exerciseId])
                    assertThat(templateExercise.repDurationSeconds).isEqualTo(exercise.defaultRepDurationSeconds)
                }
            }
        }

        val legPress = exercisesById.getValue(com.smarttrainner.core.model.ExerciseId("leg_press"))
        val templateExercise = SeedTrainingContent.templates
            .flatMap { it.days }
            .flatMap { it.exercises }
            .first { it.exerciseId == legPress.id }

        assertThat(templateExercise.sets).isEqualTo(3)
        assertThat(templateExercise.repRange).isEqualTo(15..15)
        assertThat(templateExercise.estimatedTotalSeconds).isAtLeast(1)
    }

    @Test
    fun repBasedExerciseDefaultsUseBaselineThreeSetsAndFifteenReps() {
        SeedTrainingContent.exercises
            .filter { it.defaultRepRange != null }
            .forEach { exercise ->
                assertWithMessage(exercise.id.value).that(exercise.defaultSets).isEqualTo(3)
                assertWithMessage(exercise.id.value).that(exercise.defaultRepRange).isEqualTo(15..15)
            }

        SeedTrainingContent.templates
            .flatMap { it.days }
            .flatMap { it.exercises }
            .filter { it.repRange != null }
            .forEach { exercise ->
                assertWithMessage(exercise.exerciseId.value).that(exercise.sets).isEqualTo(3)
                assertWithMessage(exercise.exerciseId.value).that(exercise.repRange).isEqualTo(15..15)
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
    fun assistedBodyweightMachinesUseAssistanceLoadType() {
        val exercisesById = SeedTrainingContent.exercises.associateBy { it.id.value }

        assertThat(exercisesById.getValue("assisted_pullup").loadType)
            .isEqualTo(ExerciseLoadType.ASSISTANCE_LOAD)
        assertThat(exercisesById.getValue("assisted_dip").loadType)
            .isEqualTo(ExerciseLoadType.ASSISTANCE_LOAD)
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
            assertThat(template.days).hasSize(template.cycleLength)
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
        assertThat(fortyFive.days.map { it.exercises.size }.toSet()).containsExactly(5)
        assertThat(sixty.days.minOf { it.exercises.size }).isAtLeast(7)
    }

    @Test
    fun routineTemplatesStayWithinRecommendedSessionTolerance() {
        val templatesOutsideTolerance = SeedTrainingContent.templates.filterNot {
            it.isWithinSessionTolerance()
        }

        assertThat(
            templatesOutsideTolerance.map {
                "${it.id}: ${it.estimatedSessionMinutes}/${it.sessionMinutes}"
            }
        ).isEmpty()
    }

    @Test
    fun generatedRoutineCatalogCoversSafeDynamicRecommendationOptions() {
        val templates = SeedTrainingContent.templates

        assertThat(templates.hasOption(TrainingExperience.BEGINNER, cycleLength = 5, sessionMinutes = 45))
            .isTrue()
        assertThat(templates.hasOption(TrainingExperience.BEGINNER, cycleLength = 5, sessionMinutes = 60))
            .isFalse()
        assertThat(templates.hasOption(TrainingExperience.INTERMEDIATE, cycleLength = 2, sessionMinutes = 30))
            .isFalse()
        assertThat(templates.hasOption(TrainingExperience.INTERMEDIATE, cycleLength = 2, sessionMinutes = 45))
            .isTrue()
        assertThat(templates.hasOption(TrainingExperience.ADVANCED, cycleLength = 5, sessionMinutes = 60))
            .isTrue()
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
                        assertWithMessage("${template.id} ${day.title} missing $focus")
                            .that(groups.count { it in focus.allowedMuscleGroups() })
                            .isAtLeast(1)
                    }
                    assertWithMessage("${template.id} ${day.title} primary ratio")
                        .that(primaryCount.toDouble() / groups.size)
                        .isAtLeast(0.50)
                } else if (day.secondaryFocuses.isNotEmpty()) {
                    day.secondaryFocuses.forEach { focus ->
                        assertWithMessage("${template.id} ${day.title} missing $focus")
                            .that(groups.count { it in focus.allowedMuscleGroups() })
                            .isAtLeast(1)
                    }
                    assertWithMessage("${template.id} ${day.title} primary ratio")
                        .that(primaryCount.toDouble() / groups.size)
                        .isAtLeast(0.50)
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
    fun beginnerHighFrequencyBodyPartSplitsStayInSafeTimeOptions() {
        val beginnerHighFrequencyBodyPartTemplates = SeedTrainingContent.templates.filter {
            it.recommendedExperience == TrainingExperience.BEGINNER &&
                it.structure == RoutineStructure.BODY_PART_SPLIT &&
                it.cycleLength >= 5
        }

        assertThat(beginnerHighFrequencyBodyPartTemplates).isNotEmpty()
        assertThat(beginnerHighFrequencyBodyPartTemplates.map { it.sessionMinutes }.toSet())
            .containsExactly(30, 45)
        beginnerHighFrequencyBodyPartTemplates.forEach { template ->
            assertThat(template.isWithinSessionTolerance()).isTrue()
            template.days.forEach { day ->
                assertThat(day.exercises.size).isAtLeast(3)
                assertThat(day.minRecoveryHours).isAtLeast(24)
            }
        }
    }

    private fun List<PlanTemplate>.hasOption(
        experience: TrainingExperience,
        cycleLength: Int,
        sessionMinutes: Int
    ): Boolean = any {
        it.recommendedExperience == experience &&
            it.cycleLength == cycleLength &&
            it.sessionMinutes == sessionMinutes &&
            it.isWithinSessionTolerance()
    }

    private fun PlanTemplate.isWithinSessionTolerance(): Boolean =
        kotlin.math.abs(estimatedSessionMinutes - sessionMinutes) <= SESSION_TOLERANCE_MINUTES

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

    private companion object {
        const val SESSION_TOLERANCE_MINUTES = 10
    }
}
