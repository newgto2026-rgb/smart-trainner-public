package com.smarttrainner

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smarttrainner.app.MainActivity
import com.smarttrainner.app.di.CoreRepositoryBindingsModule
import com.smarttrainner.app.di.RoutineDataRepositoryBindingsModule
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.WeeklyPlanRepository
import com.smarttrainner.core.domain.WeeklySummaryRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(
    CoreRepositoryBindingsModule::class,
    RoutineDataRepositoryBindingsModule::class
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
    val weeklyPlanRepository: WeeklyPlanRepository = trainingRepository

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
    val weeklySummaryRepository: WeeklySummaryRepository = trainingRepository

    @BindValue
    @JvmField
    val sessionRepository: SessionRepository = InMemorySessionRepository()

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
        composeRule.onNodeWithTag("training_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("training_tab_analysis").assertIsDisplayed()
        composeRule.onAllNodesWithTag("training_tab_record").assertCountEquals(0)
    }

    @Test
    fun weeklySummaryOnlyAppearsOnAnalysisTab() {
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
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_record_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_record_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("training_record_selected_exercise").assertIsDisplayed()
        composeRule.onNodeWithTag("training_set_reps_input_0").performTextClearance()
        composeRule.onNodeWithTag("training_set_weight_input_0").performTextClearance()
        composeRule.onNodeWithTag("training_set_rest_input_0").performTextClearance()
        composeRule.onNodeWithTag("training_set_reps_input_0").performTextInput("10")
        composeRule.onNodeWithTag("training_set_weight_input_0").performTextInput("80")
        composeRule.onNodeWithTag("training_set_rest_input_0").performTextInput("75")
        composeRule.onNodeWithTag("training_set_weight_input_1").performTextInput("85")
        composeRule.onNodeWithTag("training_add_set_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_set_weight_input_3").performScrollTo().performTextInput("90")
        composeRule.onNodeWithTag("training_save_record").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_record_dialog").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onAllNodesWithTag("training_record_dialog").assertCountEquals(0)
    }

    @Test
    fun exerciseMethodDuringWorkoutHidesStartRecordAction() {
        continueFromLoginIfNeeded()
        waitForNodeWithTag("training_home_start_workout")
        scrollToNodeWithTag("training_home_start_workout")
        composeRule.onNodeWithTag("training_home_start_workout").performClick()
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
        scrollToNodeWithTag("training_routine_preview_intermediate-body-part-4day-60")
        composeRule.onNodeWithTag("training_routine_preview_intermediate-body-part-4day-60").assertIsDisplayed()
        composeRule.onNodeWithTag("training_start_preview_routine").assertIsDisplayed().performClick()

        waitForNodeWithTag("training_tab_home")
        composeRule.onNodeWithTag("training_tab_home").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_next_routine_day_1").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("training_next_routine_day_1").assertIsDisplayed()
        scrollToNodeWithTag("training_complete_routine_day")
        composeRule.onNodeWithTag("training_complete_routine_day").assertIsDisplayed().performClick()
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
        composeRule.onNodeWithTag("training_custom_exercise_goblet_squat_0").assertIsDisplayed()
        composeRule.onNodeWithTag("training_custom_exercise_leg_press_1").assertIsDisplayed()
        composeRule.onNodeWithTag("training_custom_move_down_0").performClick()
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
        scrollToNodeWithTag("training_complete_routine_day")
        composeRule.onNodeWithTag("training_complete_routine_day").assertIsDisplayed().performClick()
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
    }

    private fun resetTestState() {
        trainingRepository.reset()
        (sessionRepository as InMemorySessionRepository).reset()
    }

    private fun waitForNodeWithTag(testTag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun continueFromLoginIfNeeded() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("login_screen").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("training_tab_home").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("login_screen").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("login_continue_default").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_tab_home").fetchSemanticsNodes().isNotEmpty()
        }
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
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_exercise_row_leg_press").fetchSemanticsNodes().isNotEmpty()
        }
        val scrollContainerCount = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().size
        var lastFailure: AssertionError? = null
        for (index in 0 until scrollContainerCount) {
            try {
                composeRule.onAllNodes(hasScrollAction())[index]
                    .performScrollToNode(hasTestTag(testTag))
                lastFailure = null
                break
            } catch (error: AssertionError) {
                lastFailure = error
            }
        }
        lastFailure?.let { throw it }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
        }
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
                lastFailure = null
                break
            } catch (error: AssertionError) {
                lastFailure = error
            }
        }
        lastFailure?.let { throw it }
    }
}
