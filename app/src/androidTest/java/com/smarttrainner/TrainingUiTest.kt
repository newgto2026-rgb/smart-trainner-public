package com.smarttrainner

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smarttrainner.app.MainActivity
import com.smarttrainner.app.di.AnalysisDataRepositoryBindingsModule
import com.smarttrainner.app.di.CoreRepositoryBindingsModule
import com.smarttrainner.app.di.PlatformTimeModule
import com.smarttrainner.app.di.RoutineDataRepositoryBindingsModule
import com.smarttrainner.app.di.WorkoutDataRepositoryBindingsModule
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.NetworkStatusRepository
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.workout.domain.WorkoutRecordingRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(
    AnalysisDataRepositoryBindingsModule::class,
    CoreRepositoryBindingsModule::class,
    PlatformTimeModule::class,
    RoutineDataRepositoryBindingsModule::class,
    WorkoutDataRepositoryBindingsModule::class
)
@RunWith(AndroidJUnit4::class)
class TrainingUiTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    private val trainingRepository = InMemoryTrainingRepository()

    @BindValue
    @JvmField
    val exerciseRepository: ExerciseRepository = trainingRepository

    @BindValue
    @JvmField
    val cyclePlanRepository: CyclePlanRepository = trainingRepository

    @BindValue
    @JvmField
    val routinePlanCatalogRepository: RoutinePlanCatalogRepository = trainingRepository

    @BindValue
    @JvmField
    val routinePlanCommandRepository: RoutinePlanCommandRepository = trainingRepository

    @BindValue
    @JvmField
    val routineProgressRepository: RoutineProgressRepository = trainingRepository

    @BindValue
    @JvmField
    val routineProgressCommandRepository: RoutineProgressCommandRepository = trainingRepository

    @BindValue
    @JvmField
    val workoutLogRepository: WorkoutLogRepository = trainingRepository

    @BindValue
    @JvmField
    val workoutRecordingRepository: WorkoutRecordingRepository = trainingRepository

    @BindValue
    @JvmField
    val cycleSummaryRepository: CycleSummaryRepository = trainingRepository

    @BindValue
    @JvmField
    val sessionRepository: SessionRepository = InMemorySessionRepository()

    @BindValue
    @JvmField
    val networkStatusRepository: NetworkStatusRepository = object : NetworkStatusRepository {
        override fun observeOnline(): Flow<Boolean> = flowOf(true)
    }

    @BindValue
    @JvmField
    val trainingDataSyncers: Set<@JvmSuppressWildcards TrainingDataSyncer> = emptySet()

    @BindValue
    @JvmField
    val clock: Clock = Clock.fixed(Instant.parse("2026-05-24T12:00:00Z"), ZoneOffset.UTC)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        resetTestState()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun homeShowsTodayTrainingAndAnalysisTabs() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_app_title").assertIsDisplayed()
        composeRule.onNodeWithTag("training_section_title_today")
            .assertIsDisplayed()
        waitForNodeWithTag("training_next_routine_day_card")
        scrollToNodeWithTag("training_next_routine_day_card")
        composeRule.onNodeWithTag("training_next_routine_day_card").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_next_routine_time_estimate").assertCountEquals(0)
        composeRule.onNodeWithTag("training_next_routine_badge_duration").assertIsDisplayed()
        composeRule.onNodeWithTag("training_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("training_tab_analysis").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_tab_record").assertCountEquals(0)
    }

    @Test
    fun profileSetupCompletesNewGoogleUserAndLogoutReturnsToLogin() {
        waitForLoginScreen()
        signInGoogleUserWithoutProfile("Google Draft")
        waitForNodeWithTag("profile_setup_screen")
        composeRule.onNodeWithTag("profile_setup_save").assertIsNotEnabled()

        composeRule.onNodeWithTag("profile_setup_nickname_input").performTextReplacement("Fresh Runner")
        composeRule.onNodeWithTag("profile_setup_check_nickname").performClick()
        waitForNodeWithTag("login_nickname_message")
        composeRule.onNodeWithTag("login_gender_female").performClick()
        composeRule.onNodeWithTag("login_height_input").performTextReplacement("171")
        composeRule.onNodeWithTag("login_weight_input").performTextReplacement("64.5")
        composeRule.onNodeWithTag("profile_setup_save")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        waitForNodeWithTag("training_tab_home")
        composeRule.onNodeWithTag("profile_button").performClick()
        composeRule.onNode(hasText("Fresh Runner")).assertIsDisplayed()
        composeRule.onNodeWithTag("profile_logout").performClick()
        waitForLoginScreen()
        composeRule.onNodeWithTag("login_screen").assertIsDisplayed()
    }

    @Test
    fun cycleSummaryOnlyAppearsOnAnalysisTab() {
        continueFromLoginIfNeeded()
        composeRule.onAllNodesWithTag("training_summary_band").assertCountEquals(0)

        composeRule.onNodeWithTag("training_tab_plan").performClick()
        composeRule.onAllNodesWithTag("training_summary_band").assertCountEquals(0)

        composeRule.onNodeWithTag("training_tab_exercises").performClick()
        composeRule.onAllNodesWithTag("training_summary_band").assertCountEquals(0)

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        composeRule.onNodeWithTag("training_summary_band").assertIsDisplayed()
    }

    @Test
    fun exerciseSearchFiltersCatalogAndClearRestoresRows() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_exercises").performClick()
        waitForNodeWithTag("training_exercise_search")

        composeRule.onNodeWithTag("training_exercise_search").performTextReplacement("leg press")
        waitForNodeWithTag("training_exercise_row_leg_press")
        composeRule.onNodeWithTag("training_exercise_row_leg_press").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_exercise_row_machine_chest_press")
            .assertCountEquals(0)

        composeRule.onNodeWithTag("training_exercise_search_clear").performClick()
        waitForNodeWithTagToDisappear("training_exercise_search_clear")
        scrollToNodeWithTag("training_exercise_row_machine_chest_press")
        composeRule.onNodeWithTag("training_exercise_row_machine_chest_press").assertIsDisplayed()
    }

    @Test
    fun trainingLevelChangePromptsBeforeOpeningRoutineLibrary() {
        continueFromLoginIfNeeded()

        composeRule.onNodeWithTag("profile_button").performClick()
        composeRule.onNodeWithTag("profile_training_level_entry").performClick()
        composeRule.onNodeWithTag("profile_training_level_intermediate").performClick()
        waitForNodeWithTag("profile_routine_change_prompt")
        composeRule.onNodeWithTag("profile_routine_change_prompt").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_routine_library_dialog").assertCountEquals(0)

        composeRule.onNodeWithTag("profile_keep_current_routine").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("profile_routine_change_prompt").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onAllNodesWithTag("training_routine_library_dialog").assertCountEquals(0)

        composeRule.onNodeWithTag("profile_button").performClick()
        composeRule.onNodeWithTag("profile_training_level_entry").performClick()
        composeRule.onNodeWithTag("profile_training_level_advanced").performClick()
        waitForNodeWithTag("profile_routine_change_prompt")
        composeRule.onNodeWithTag("profile_confirm_routine_change").performClick()
        waitForNodeWithTag("training_routine_library_dialog")
        composeRule.onNodeWithTag("training_routine_library_dialog").assertIsDisplayed()
    }

    @Test
    fun exerciseDetailShowsStepImages() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_exercises").performClick()
        clickExerciseRow("training_exercise_row_romanian_deadlift")
        composeRule.onNodeWithTag("training_step_image_0").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("training_step_image_1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("training_step_image_2").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("training_step_image_3").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("training_step_image_4").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_step_image_5").assertCountEquals(0)
        composeRule.onNodeWithTag("training_step_image_0").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_exercise_image_viewer").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_exercise_image_viewer").assertIsDisplayed()
        composeRule.onNodeWithTag("training_exercise_image_viewer_image").assertIsDisplayed()
        composeRule.onNodeWithTag("training_close_exercise_image_viewer").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_exercise_image_viewer").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun customRoutineBuilderIncludesSecondaryBackMatchesWithRoleBadge() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        composeRule.onNodeWithTag("training_create_custom_routine_button").performClick()
        composeRule.onNodeWithTag("training_custom_routine_builder").assertIsDisplayed()
        selectCustomFocus("training_custom_focus_BACK")
        scrollToNodeWithTag("training_custom_exercise_group_BACK")
        composeRule.onNodeWithTag("training_custom_exercise_group_BACK").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_conventional_deadlift")

        composeRule.onNodeWithTag("training_custom_add_exercise_conventional_deadlift")
            .assertIsDisplayed()
        composeRule.onNodeWithTag(
            "training_custom_exercise_role_BACK_conventional_deadlift",
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    @Test
    fun machineShoulderPressDetailShowsExactlyThreeStepImages() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_exercises").performClick()
        clickExerciseRow("training_exercise_row_machine_shoulder_press")
        composeRule.onNodeWithTag("training_step_image_0").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("training_step_image_1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("training_step_image_2").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_step_image_3").assertCountEquals(0)
    }

    @Test
    fun recordFlowSavesWorkout() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        clickPlanExercise("training_plan_exercise_leg_press")
        confirmRoutineDayDateIfNeeded()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_record_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_record_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("training_record_selected_exercise").assertIsDisplayed()
        composeRule.onNodeWithTag("training_set_reps_input_0").performClick()
        composeRule.onNodeWithTag("training_set_reps_option_0_5").performClick()
        composeRule.onNodeWithTag("training_set_weight_input_0").performClick()
        composeRule.onNodeWithTag("training_set_weight_option_0_1").performClick()
        composeRule.onNodeWithTag("training_set_rest_input_0").performClick()
        composeRule.onNodeWithTag("training_set_rest_option_0_90").performClick()
        composeRule.onNodeWithTag("training_set_weight_input_1").performClick()
        composeRule.onNodeWithTag("training_set_weight_option_1_1_5").performClick()
        composeRule.onNodeWithTag("training_add_set_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_set_weight_input_3").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_set_weight_option_3_2").performClick()
        composeRule.onNodeWithTag("training_save_record").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_record_dialog").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onAllNodesWithTag("training_record_dialog").assertCountEquals(0)
        assertLegPressRecordVisibleInPlan()

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_recent_records_card")
        composeRule.onNodeWithTag("training_recent_records_card").assertIsDisplayed()
        composeRule.onNode(
            hasText("레그 프레스", substring = true) or hasText("Leg Press", substring = true)
        ).assertIsDisplayed()
    }

    @Test
    fun routineDayDateCancelKeepsScheduleUnassignedAndDoesNotStartWorkout() {
        continueFromLoginIfNeeded()

        openHomeStartWorkout()
        waitForNodeWithTag("training_routine_day_date_dialog")
        composeRule.onNodeWithTag("training_cancel_routine_day_date").performClick()
        waitForNodeWithTagToDisappear("training_routine_day_date_dialog")

        composeRule.onAllNodesWithTag("training_record_dialog").assertCountEquals(0)
        assertTrue(trainingRepository.routineDayDatesForTest().isEmpty())
        assertCurrentRoutineDateUnassigned()
    }

    @Test
    fun routineDayDateSelectionIsRememberedAcrossHomeAndPlanTabs() {
        continueFromLoginIfNeeded()

        assignCurrentRoutineDayDate()

        assertEquals(setOf(FIXED_UI_DATE), trainingRepository.routineDayDatesForTest().values.toSet())
        assertCurrentRoutineDateAssigned()

        composeRule.onNodeWithTag("training_tab_plan").performClick()
        scrollToNodeWithTag("training_plan_exercise_leg_press")
        composeRule.onNodeWithTag("training_tab_home").performClick()
        assertCurrentRoutineDateAssigned()
    }

    @Test
    fun assignedDateSurvivesDismissedRecordDialogWithoutCreatingWorkoutLog() {
        continueFromLoginIfNeeded()

        openHomeStartWorkout()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        composeRule.onNodeWithTag("training_close_record_dialog").performClick()
        waitForNodeWithTagToDisappear("training_record_dialog")

        assertEquals(setOf(FIXED_UI_DATE), trainingRepository.routineDayDatesForTest().values.toSet())
        assertTrue(trainingRepository.workoutLogsForTest().isEmpty())
        assertCurrentRoutineDateAssigned()

        openHomeStartWorkout()
        waitForNodeWithTag("training_record_dialog")
        composeRule.onAllNodesWithTag("training_routine_day_date_dialog").assertCountEquals(0)
    }

    @Test
    fun analysisReflectsRecordedCycleExerciseWithAssignedDate() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        clickPlanExercise("training_plan_exercise_leg_press")
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")

        saveDefaultWeightedRecord()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.workoutLogsForTest().size == 1
        }
        val log = trainingRepository.workoutLogsForTest().single()
        assertEquals(FIXED_UI_DATE, log.performedAt.toLocalDate())
        assertNotNull(log.routineDayInstanceId)

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_summary_band")
        assertTextInsideTag("training_summary_completion_rate", "4%")
        assertTextInsideTag("training_summary_total_sets", "3")
        assertAnyTextInsideTag("training_summary_streak", "1일", "1 d")
        composeRule.onNodeWithTag("training_recent_records_card").assertIsDisplayed()
        composeRule.onNode(
            hasText("레그 프레스", substring = true) or hasText("Leg Press", substring = true)
        ).assertIsDisplayed()
        composeRule.onNode(
            hasText("5월 24일", substring = true) or hasText("5/24", substring = true)
        ).assertIsDisplayed()
    }

    @Test
    fun cancelLatestCompletionCanBeKeptThenConfirmedAndRestoresRoutineDay() {
        continueFromLoginIfNeeded()

        completeRoutineDayWithConfirmation()
        waitForNodeWithTag("training_next_routine_day_2")
        scrollToNodeWithTag("training_latest_routine_day_completion_card")
        composeRule.onNodeWithTag("training_latest_routine_day_completion_card").assertIsDisplayed()
        assertEquals(1, trainingRepository.progressForTest().dayIndex)
        assertEquals(0, trainingRepository.progressForTest().lastCompletedDayIndex)

        scrollToNodeWithTag("training_cancel_latest_routine_day")
        composeRule.onNodeWithTag("training_cancel_latest_routine_day").performClick()
        waitForNodeWithTag("training_cancel_latest_day_dialog")
        composeRule.onNodeWithTag("training_keep_latest_day_completion").performClick()
        waitForNodeWithTagToDisappear("training_cancel_latest_day_dialog")
        scrollToNodeWithTag("training_next_routine_day_2")
        composeRule.onNodeWithTag("training_next_routine_day_2").assertIsDisplayed()
        assertEquals(1, trainingRepository.progressForTest().dayIndex)

        scrollToNodeWithTag("training_cancel_latest_routine_day")
        composeRule.onNodeWithTag("training_cancel_latest_routine_day").performClick()
        waitForNodeWithTag("training_cancel_latest_day_dialog")
        composeRule.onNodeWithTag("training_confirm_cancel_latest_day").performClick()
        waitForNodeWithTagToDisappear("training_cancel_latest_day_dialog")
        scrollToNodeWithTag("training_next_routine_day_1")
        composeRule.onAllNodesWithTag("training_latest_routine_day_completion_card").assertCountEquals(0)
        assertEquals(0, trainingRepository.progressForTest().dayIndex)
        assertNull(trainingRepository.progressForTest().lastCompletedDayIndex)
    }

    @Test
    fun exerciseMethodDuringWorkoutHidesStartRecordAction() {
        continueFromLoginIfNeeded()
        waitForNodeWithTag("training_home_start_workout")
        scrollToNodeWithTag("training_home_start_workout")
        composeRule.onNodeWithTag("training_home_start_workout").performClick()
        confirmRoutineDayDateIfNeeded()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_record_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_show_exercise_method").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_exercise_detail_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_exercise_detail_dialog").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_detail_start_record").assertCountEquals(0)
    }

    @Test
    fun focusedRoutineSelectionAndCompletionAdvancesNextDay() {
        continueFromLoginIfNeeded()
        (sessionRepository as InMemorySessionRepository)
            .setTrainingExperienceForTest(TrainingExperience.ADVANCED)
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        composeRule.onAllNodesWithTag("training_template_card_intermediate-body-part-4day-60")
            .assertCountEquals(0)
        scrollToNodeWithTag("training_find_routine_button")
        composeRule.onNodeWithTag("training_find_routine_button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_routine_library_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_routine_library_dialog").assertIsDisplayed()
        scrollToNodeWithTag("training_template_card_intermediate-body-part-4day-60")
        composeRule.onNodeWithTag("training_template_card_intermediate-body-part-4day-60")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("training_find_recommended_routine_button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_routine_settings_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_routine_settings_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("training_show_recommendations").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("training_routine_recommendations_dialog").assertIsDisplayed()
        scrollToNodeWithTag("training_routine_preview_advanced-body-part-5day-60m")
        composeRule.onNodeWithTag("training_routine_preview_advanced-body-part-5day-60m").assertIsDisplayed()
        composeRule.onNodeWithTag("training_start_preview_routine").assertIsDisplayed().performClick()

        waitForNodeWithTag("training_tab_home")
        composeRule.onNodeWithTag("training_tab_home").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_next_routine_day_1").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_next_routine_day_1").assertIsDisplayed()
        completeRoutineDayWithConfirmation()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_next_routine_day_2").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_next_routine_day_2").assertIsDisplayed()
    }

    @Test
    fun customRoutineBuilderSavesSelectsEditsAndAdvancesFourDayRoutine() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        composeRule.onNodeWithTag("training_create_custom_routine_button").performClick()
        composeRule.onNodeWithTag("training_custom_routine_builder").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_save_start_custom_routine").assertCountEquals(0)
        composeRule.onAllNodesWithTag("training_custom_exercise_leg_press_0").assertCountEquals(0)
        composeRule.onNodeWithTag("training_custom_day_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("training_custom_focus_selector").assertIsDisplayed()
        composeRule.onNodeWithTag("training_custom_focus_selected_none", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("training_custom_focus_selector").performClick()
        composeRule.onNodeWithTag("training_custom_focus_none").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_custom_focus_FULL_BODY").assertCountEquals(0)

        composeRule.onNodeWithTag("training_custom_routine_name").performTextReplacement("My 4 Day Split")
        composeRule.onNodeWithTag("training_custom_routine_name").performImeAction()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("training_custom_focus_CHEST").performClick()
        composeRule.onAllNodesWithTag("training_custom_exercise_group_BACK").assertCountEquals(0)
        composeRule.onAllNodesWithTag("training_custom_exercise_group_LOWER_BODY").assertCountEquals(0)
        scrollToNodeWithTag("training_custom_exercise_group_CHEST")
        composeRule.onNodeWithTag("training_custom_exercise_group_CHEST").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_machine_chest_press")
        composeRule.onNodeWithTag("training_custom_add_exercise_machine_chest_press").performClick()
        composeRule.onAllNodesWithTag("training_custom_add_exercise_machine_chest_press").assertCountEquals(0)
        composeRule.onNodeWithTag("training_add_custom_day").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_custom_day_tab_1").performClick()
        composeRule.onNodeWithTag("training_custom_day_empty").assertIsDisplayed()
        selectCustomFocus("training_custom_focus_LOWER_BODY")
        scrollToNodeWithTag("training_custom_exercise_group_LOWER_BODY")
        composeRule.onNodeWithTag("training_custom_exercise_group_LOWER_BODY").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_leg_press")
        composeRule.onNodeWithTag("training_custom_add_exercise_leg_press").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_goblet_squat")
        composeRule.onNodeWithTag("training_custom_add_exercise_goblet_squat").performClick()
        selectCustomFocus("training_custom_focus_CHEST")
        composeRule.onAllNodesWithTag("training_custom_exercise_leg_press_0").assertCountEquals(0)
        composeRule.onAllNodesWithTag("training_custom_exercise_goblet_squat_1").assertCountEquals(0)
        selectCustomFocus("training_custom_focus_LOWER_BODY")
        scrollToNodeWithTag("training_custom_exercise_group_LOWER_BODY")
        composeRule.onNodeWithTag("training_custom_exercise_group_LOWER_BODY").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_leg_press")
        composeRule.onNodeWithTag("training_custom_add_exercise_leg_press").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_goblet_squat")
        composeRule.onNodeWithTag("training_custom_add_exercise_goblet_squat").performClick()
        scrollToNodeWithTag("training_custom_move_up_1")
        composeRule.onNodeWithTag("training_custom_move_up_1").performClick()
        scrollToNodeWithTag("training_custom_exercise_goblet_squat_0")
        composeRule.onNodeWithTag("training_custom_exercise_goblet_squat_0").assertIsDisplayed()
        scrollToNodeWithTag("training_custom_exercise_leg_press_1")
        composeRule.onNodeWithTag("training_custom_exercise_leg_press_1").assertIsDisplayed()
        scrollToNodeWithTag("training_custom_move_down_0")
        composeRule.onNodeWithTag("training_custom_move_down_0").performClick()
        scrollToNodeWithTag("training_custom_exercise_leg_press_0")
        composeRule.onNodeWithTag("training_custom_exercise_leg_press_0").assertIsDisplayed()
        composeRule.onNodeWithTag("training_add_custom_day").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_custom_day_tab_2").performClick()
        selectCustomFocus("training_custom_focus_BACK")
        scrollToNodeWithTag("training_custom_exercise_group_BACK")
        composeRule.onNodeWithTag("training_custom_exercise_group_BACK").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_lat_pulldown")
        composeRule.onNodeWithTag("training_custom_add_exercise_lat_pulldown").performClick()
        composeRule.onNodeWithTag("training_add_custom_day").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_custom_day_tab_3").performClick()
        selectCustomFocus("training_custom_focus_SHOULDERS")
        scrollToNodeWithTag("training_custom_exercise_group_SHOULDERS")
        composeRule.onNodeWithTag("training_custom_exercise_group_SHOULDERS").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_machine_shoulder_press")
        composeRule.onNodeWithTag("training_custom_add_exercise_machine_shoulder_press").performClick()
        composeRule.onNodeWithTag("training_save_custom_routine").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_routine_builder").fetchSemanticsNodes().isEmpty()
        }
        scrollToNodeWithTag("training_find_routine_button")
        composeRule.onNodeWithTag("training_find_routine_button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_template_card").fetchSemanticsNodes().isNotEmpty()
        }
        assertCustomRoutineFlowDaysAreVisible("training_custom_template_card")
        composeRule.onNodeWithTag("training_edit_custom_template_card").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_routine_builder").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_custom_focus_selected_CHEST", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("training_custom_day_tab_1").performClick()
        composeRule.onNodeWithTag("training_custom_focus_selected_LOWER_BODY", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("training_save_custom_routine").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_routine_builder").fetchSemanticsNodes().isEmpty()
        }
        scrollToNodeWithTag("training_find_routine_button")
        composeRule.onNodeWithTag("training_find_routine_button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_template_card").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_custom_template_card").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_routine_library_dialog").fetchSemanticsNodes().isEmpty()
        }
        assertCustomRoutineFlowDaysAreVisible("training_current_routine_card")
        composeRule.onNodeWithTag("training_edit_current_custom_routine").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_routine_builder").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_custom_focus_selected_CHEST", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("training_save_custom_routine").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_routine_builder").fetchSemanticsNodes().isEmpty()
        }
        waitForNodeWithTag("training_tab_home")
        composeRule.onNodeWithTag("training_tab_home").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_home_routine_source_custom").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_home_routine_source_custom").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_next_routine_day_1").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_next_routine_day_1").assertIsDisplayed()
        composeRule.onNodeWithTag("training_next_routine_focus_CHEST").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_next_routine_time_estimate").assertCountEquals(0)
        composeRule.onNodeWithTag("training_next_routine_badge_duration").assertIsDisplayed()
        completeRoutineDayWithConfirmation()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_next_routine_day_2").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_next_routine_day_2").assertIsDisplayed()
        composeRule.onNodeWithTag("training_next_routine_focus_LOWER_BODY").assertIsDisplayed()
        composeRule.onNodeWithTag("training_next_routine_plan_title").assertIsDisplayed()
        composeRule.onNodeWithTag("training_next_routine_plan_exercises")
            .assert(hasText("레그 프레스", substring = true) or hasText("Leg Press", substring = true))
            .assert(hasText("고블릿 스쿼트", substring = true) or hasText("Goblet Squat", substring = true))
        composeRule.onAllNodesWithTag("training_next_routine_time_estimate").assertCountEquals(0)
        composeRule.onNodeWithTag("training_next_routine_badge_duration").assertIsDisplayed()
    }

    private fun resetTestState() {
        trainingRepository.reset()
        (sessionRepository as InMemorySessionRepository).reset()
    }

    private fun waitForLoginScreen() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("login_screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForNodeWithTag(testTag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForNodeWithTagToDisappear(testTag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun signInGoogleUserWithoutProfile(nickname: String) {
        runBlocking {
            (sessionRepository as InMemorySessionRepository).signInWithGoogle(
                idToken = "ui-test-token-without-profile",
                nickname = nickname,
                profileSetup = null,
                forceDeviceLogin = false
            )
        }
    }

    private fun continueFromLoginIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("login_screen").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("training_tab_home").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("login_screen").fetchSemanticsNodes().isNotEmpty()) {
            runBlocking {
                (sessionRepository as InMemorySessionRepository).signInWithGoogle(
                    idToken = "ui-test-token",
                    nickname = "UI Tester",
                    profileSetup = ProfileSetup(ProfileGender.MALE, heightCm = 180, weightKg = 82.5),
                    forceDeviceLogin = false
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_tab_home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertLegPressRecordVisibleInPlan() {
        scrollToNodeWithTag("training_plan_exercise_leg_press")
        composeRule.onNodeWithTag("training_plan_exercise_leg_press").assertIsDisplayed()
    }

    private fun openHomeStartWorkout() {
        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_start_workout")
        scrollToNodeWithTag("training_home_start_workout")
        composeRule.onNodeWithTag("training_home_start_workout").performClick()
    }

    private fun assignCurrentRoutineDayDate() {
        composeRule.onNodeWithTag("training_tab_home").performClick()
        scrollToNodeWithTag("training_routine_day_date")
        composeRule.onNodeWithTag("training_routine_day_date").performClick()
        waitForNodeWithTag("training_routine_day_date_dialog")
        composeRule.onNodeWithTag("training_confirm_routine_day_date").performClick()
        waitForNodeWithTagToDisappear("training_routine_day_date_dialog")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.routineDayDatesForTest().isNotEmpty()
        }
    }

    private fun assertCurrentRoutineDateAssigned() {
        scrollToNodeWithTag("training_routine_day_date")
        composeRule.onNodeWithTag("training_routine_day_date")
            .assert(hasText(FIXED_UI_DATE.toString(), substring = true))
    }

    private fun assertCurrentRoutineDateUnassigned() {
        scrollToNodeWithTag("training_routine_day_date")
        composeRule.onNodeWithTag("training_routine_day_date")
            .assert(
                hasText("수행 날짜 선택", substring = true) or
                    hasText("Choose workout date", substring = true)
            )
    }

    private fun saveDefaultWeightedRecord() {
        selectSetWeight(index = 0, optionTagValue = "1")
        selectSetWeight(index = 1, optionTagValue = "1_5")
        selectSetWeight(index = 2, optionTagValue = "2")
        composeRule.onNodeWithTag("training_save_record").performScrollTo().performClick()
        waitForNodeWithTagToDisappear("training_record_dialog")
    }

    private fun selectSetWeight(index: Int, optionTagValue: String) {
        composeRule.onNodeWithTag("training_set_weight_input_$index")
            .performScrollTo()
            .performClick()
        val optionTag = "training_set_weight_option_${index}_$optionTagValue"
        waitForNodeWithTag(optionTag)
        composeRule.onNodeWithTag(optionTag).performClick()
    }

    private fun assertTextInsideTag(containerTag: String, text: String) {
        composeRule.onNode(
            hasAnyAncestor(hasTestTag(containerTag)) and hasText(text, substring = true)
        ).assertIsDisplayed()
    }

    private fun assertAnyTextInsideTag(containerTag: String, vararg texts: String) {
        val matcher = texts
            .map { text -> hasText(text, substring = true) }
            .reduce { left, right -> left or right }
        composeRule.onNode(hasAnyAncestor(hasTestTag(containerTag)) and matcher)
            .assertIsDisplayed()
    }

    private fun selectCustomFocus(optionTag: String) {
        scrollToNodeWithTag("training_custom_focus_selector")
        composeRule.onNodeWithTag("training_custom_focus_selector").performClick()
        waitForNodeWithTag(optionTag)
        composeRule.onNodeWithTag(optionTag).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_custom_focus_menu").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun completeRoutineDayWithConfirmation() {
        scrollToNodeWithTag("training_complete_routine_day")
        composeRule.onNodeWithTag("training_complete_routine_day").assertIsDisplayed().performClick()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_complete_day_confirmation_dialog")
        composeRule.onNodeWithTag("training_confirm_complete_routine_day").assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_complete_day_confirmation_dialog")
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    private fun confirmRoutineDayDateIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_routine_day_date_dialog").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("training_record_dialog").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("training_complete_day_confirmation_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("training_routine_day_date_dialog").fetchSemanticsNodes().isEmpty()) return
        composeRule.onNodeWithTag("training_confirm_routine_day_date").assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_routine_day_date_dialog").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun assertCustomRoutineFlowDaysAreVisible(containerTag: String) {
        scrollToNodeWithTag(containerTag)
        composeRule.onNodeWithTag("training_routine_flow_custom-test", useUnmergedTree = true)
            .assertIsDisplayed()
        (1..4).forEach { dayNumber ->
            val tag = "training_routine_flow_custom-test_day_$dayNumber"
            composeRule.onNodeWithTag(tag, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    private fun clickExerciseRow(testTag: String) {
        scrollToNodeWithTag(testTag)
        composeRule.onNodeWithTag(testTag).assertIsDisplayed().performClick()
    }

    private fun clickPlanExercise(testTag: String) {
        scrollToNodeWithTag(testTag)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(testTag).performClick()
    }

    private fun scrollToNodeWithTag(testTag: String) {
        val scrollContainerCount = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().size
        var lastFailure: AssertionError? = null
        for (index in 0 until scrollContainerCount) {
            try {
                composeRule.onAllNodes(hasScrollAction())[index]
                    .performScrollToNode(hasTestTag(testTag))
                if (composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()) {
                    lastFailure = null
                    break
                }
            } catch (error: AssertionError) {
                lastFailure = error
            }
        }
        lastFailure?.let { throw it }
        waitForNodeWithTag(testTag)
    }
}

private val FIXED_UI_DATE: LocalDate = LocalDate.of(2026, 5, 24)
