package com.smarttrainner

import android.Manifest
import android.content.Intent
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
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.smarttrainner.app.MainActivity
import com.smarttrainner.app.di.AnalysisDataRepositoryBindingsModule
import com.smarttrainner.app.di.CoreRepositoryBindingsModule
import com.smarttrainner.app.di.FriendDataRepositoryBindingsModule
import com.smarttrainner.app.di.PlatformTimeModule
import com.smarttrainner.app.di.RoutineDataRepositoryBindingsModule
import com.smarttrainner.app.di.WorkoutDataRepositoryBindingsModule
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.NetworkStatusRepository
import com.smarttrainner.core.domain.PushTokenRepository
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.TrainingDataSyncer
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import com.smarttrainner.feature.friend.domain.FriendConnection
import com.smarttrainner.feature.friend.domain.FriendRepository
import com.smarttrainner.feature.friend.domain.FriendRequest
import com.smarttrainner.feature.friend.domain.FriendRequestId
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
    FriendDataRepositoryBindingsModule::class,
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

    @get:Rule(order = 2)
    val notificationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

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
    val friendRepository: FriendRepository = object : FriendRepository {
        override fun observeFriends(): Flow<List<FriendConnection>> = flowOf(emptyList())

        override fun observeIncomingRequests(): Flow<List<FriendRequest>> = flowOf(emptyList())

        override suspend fun refresh(): Result<Unit> = Result.success(Unit)

        override suspend fun sendRequest(nickname: String): Result<FriendRequest> =
            Result.failure(UnsupportedOperationException("Friend requests are not used in training UI tests."))

        override suspend fun acceptRequest(id: FriendRequestId): Result<FriendConnection> =
            Result.failure(UnsupportedOperationException("Friend requests are not used in training UI tests."))

        override suspend fun declineRequest(id: FriendRequestId): Result<Unit> =
            Result.failure(UnsupportedOperationException("Friend requests are not used in training UI tests."))

        override suspend fun removeFriend(friendSessionId: UserSessionId): Result<Unit> =
            Result.failure(UnsupportedOperationException("Friends are not used in training UI tests."))
    }

    @BindValue
    @JvmField
    val pushTokenRepository: PushTokenRepository = object : PushTokenRepository {
        override suspend fun registerToken(token: String): Result<Unit> = Result.success(Unit)
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
    fun friendsTabOpensFriendRoute() {
        continueFromLoginIfNeeded()

        composeRule.onNodeWithTag("training_tab_friends").performClick()
        waitForNodeWithTag("friend_add_card")

        composeRule.onNodeWithTag("friend_add_card").assertIsDisplayed()
        composeRule.onNodeWithTag("friend_nickname_input").assertIsDisplayed()
    }

    @Test
    fun backOnTopLevelTabsKeepsCurrentTabOnFirstPress() {
        continueFromLoginIfNeeded()

        assertTopLevelBackStaysOnScreen(
            tabTag = "training_tab_home",
            screenTag = "training_next_routine_day_card"
        )
        assertTopLevelBackStaysOnScreen(
            tabTag = "training_tab_plan",
            screenTag = "training_current_routine_card"
        )
        assertTopLevelBackStaysOnScreen(
            tabTag = "training_tab_exercises",
            screenTag = "training_exercise_search"
        )
        assertTopLevelBackStaysOnScreen(
            tabTag = "training_tab_calendar",
            screenTag = "calendar_month_grid"
        )
        assertTopLevelBackStaysOnScreen(
            tabTag = "training_tab_friends",
            screenTag = "friend_add_card"
        )
        assertTopLevelBackStaysOnScreen(
            tabTag = "training_tab_analysis",
            screenTag = "training_summary_band"
        )
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
    fun customExerciseFormShowsValidationErrorWhenMethodIsMissing() {
        continueFromLoginIfNeeded()
        openCustomExerciseForm()

        assertTaggedTextContainsAny(
            "training_custom_exercise_required_policy",
            "필수",
            "Required"
        )
        scrollToNodeWithTag("training_custom_exercise_image_policy")
        assertTaggedTextContainsAny(
            "training_custom_exercise_image_policy",
            "기본 이미지",
            "default image"
        )
        scrollToNodeWithTag("training_custom_exercise_image_picker")
        composeRule.onNodeWithTag("training_custom_exercise_image_picker").assertIsDisplayed()

        scrollToNodeWithTag("training_custom_exercise_name_input")
        composeRule.onNodeWithTag("training_custom_exercise_name_input")
            .performTextReplacement("Hotel Cable Row")
        scrollToNodeWithTag("training_custom_exercise_safety_0_input")
        composeRule.onNodeWithTag("training_custom_exercise_safety_0_input")
            .performTextReplacement("Keep the shoulders away from the ears.")
        composeRule.onNodeWithTag("training_custom_exercise_save").performClick()

        waitForNodeWithTag("training_custom_exercise_error")
        composeRule.onNodeWithTag("training_custom_exercise_form").assertIsDisplayed()
        assertTaggedTextContainsAny(
            "training_custom_exercise_error",
            "운동 방법",
            "exercise method"
        )
    }

    @Test
    fun customExerciseCreateAppearsInCatalogDetailAndRoutineBuilderWithDefaultImage() {
        continueFromLoginIfNeeded()
        createCustomExerciseFromExerciseTab()

        clickExerciseRow("training_exercise_row_custom_exercise_ui_1")
        waitForNodeWithTag("training_exercise_detail_dialog")
        assertAnyTextInsideTag("training_exercise_detail_dialog", "Hotel Cable Row")
        assertAnyTextInsideTagWithScroll(
            "training_exercise_detail_dialog",
            "기본 이미지",
            "Default image"
        )
        assertAnyTextInsideTag("training_exercise_detail_dialog", "Pull the handle toward the ribs.")
        assertAnyTextInsideTag("training_exercise_detail_dialog", "Keep the shoulders away from the ears.")
        composeRule.onNodeWithTag("training_close_exercise_detail").performClick()
        waitForNodeWithTagToDisappear("training_exercise_detail_dialog")

        composeRule.onNodeWithTag("training_tab_plan").performClick()
        composeRule.onNodeWithTag("training_create_custom_routine_button").performClick()
        waitForNodeWithTag("training_custom_routine_builder")
        selectCustomFocus("training_custom_focus_BACK")
        scrollToNodeWithTag("training_custom_exercise_group_BACK")
        composeRule.onNodeWithTag("training_custom_exercise_group_BACK").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_custom_exercise_ui_1")
        composeRule.onNodeWithTag("training_custom_add_exercise_custom_exercise_ui_1")
            .assertIsDisplayed()
    }

    @Test
    fun customExerciseCatalogIsScopedToActiveUserSession() {
        continueFromLoginIfNeeded()
        createCustomExerciseFromExerciseTab()
        composeRule.onNodeWithTag("training_exercise_row_custom_exercise_ui_1").assertIsDisplayed()

        trainingRepository.setActiveSessionForTest("other-user-session")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("training_exercise_row_custom_exercise_ui_1")
                .fetchSemanticsNodes()
                .isEmpty()
        }

        trainingRepository.setActiveSessionForTest("android-ui-test")
        waitForNodeWithTag("training_exercise_row_custom_exercise_ui_1")
        composeRule.onNodeWithTag("training_exercise_row_custom_exercise_ui_1").assertIsDisplayed()
    }

    @Test
    fun customExerciseDeleteWarnsAndRemovesRoutineAndWorkoutReferences() {
        continueFromLoginIfNeeded()
        createCustomExerciseFromExerciseTab()
        val customExerciseId = ExerciseId("custom_exercise_ui_1")
        trainingRepository.seedCustomRoutineAndLogForExerciseForTest(customExerciseId)

        assertTrue(trainingRepository.workoutLogsForTest().any { it.exerciseId == customExerciseId })
        assertTrue(
            trainingRepository.customTemplatesForTest().any { template ->
                template.days.any { day -> day.exercises.any { it.exerciseId == customExerciseId } }
            }
        )

        clickExerciseRow("training_exercise_row_custom_exercise_ui_1")
        waitForNodeWithTag("training_exercise_detail_dialog")
        composeRule.onNodeWithTag("training_custom_exercise_delete").performClick()
        waitForNodeWithTag("training_custom_exercise_delete_confirm_dialog")
        assertAnyTextInsideTag(
            "training_custom_exercise_delete_confirm_dialog",
            "루틴",
            "routine"
        )
        assertAnyTextInsideTag(
            "training_custom_exercise_delete_confirm_dialog",
            "운동 기록",
            "workout records"
        )

        composeRule.onNodeWithTag("training_custom_exercise_delete_cancel").performClick()
        waitForNodeWithTagToDisappear("training_custom_exercise_delete_confirm_dialog")
        composeRule.onNodeWithTag("training_exercise_detail_dialog").assertIsDisplayed()

        composeRule.onNodeWithTag("training_custom_exercise_delete").performClick()
        waitForNodeWithTag("training_custom_exercise_delete_confirm_dialog")
        composeRule.onNodeWithTag("training_custom_exercise_delete_confirm").performClick()
        waitForNodeWithTagToDisappear("training_exercise_detail_dialog")
        waitForNodeWithTagToDisappear("training_exercise_row_custom_exercise_ui_1")

        assertTrue(trainingRepository.workoutLogsForTest().none { it.exerciseId == customExerciseId })
        assertTrue(
            trainingRepository.customTemplatesForTest().none { template ->
                template.days.any { day -> day.exercises.any { it.exerciseId == customExerciseId } }
            }
        )
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
        assertRoutineChangePolicyTextVisibleInProfilePrompt()
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
    fun homeStartWorkoutSavingOneExerciseRefreshesRoutineProgress() {
        continueFromLoginIfNeeded()

        openHomeStartWorkout()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        saveVisibleRecordWithoutClosingContinuousDialog()
        waitForTextInsideTag("training_record_dialog", "레그 프레스", "Leg Press")
        closeRecordDialogIfVisible()
        scrollToNodeWithTag("training_next_routine_completion_progress")

        assertTaggedTextContainsAny("training_next_routine_completion_progress", "1/", "1개 기록")
    }

    @Test
    fun substitutingCurrentRoutineExerciseImmediatelyUpdatesRecordDialogAndSavedLog() {
        continueFromLoginIfNeeded()

        openHomeStartWorkout()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        waitForTextInsideTag("training_record_dialog", "트레드밀 걷기", "Treadmill Walk")

        substituteVisibleRoutineExerciseWithIndoorBike()

        waitForTextInsideTag("training_record_dialog", "실내 자전거", "Indoor Bike")
        saveVisibleRecordWithoutClosingContinuousDialog()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.workoutLogsForTest().size == 1
        }
        assertEquals("indoor_bike", trainingRepository.workoutLogsForTest().single().exerciseId.value)
    }

    @Test
    fun recordDialogKeepsEnteredValuesAfterActivityRecreation() {
        continueFromLoginIfNeeded()
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        clickPlanExercise("training_plan_exercise_leg_press")
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")

        selectSetWeight(index = 0, optionTagValue = "1_5")
        selectSetWeight(index = 1, optionTagValue = "2")
        assertTaggedTextContainsAny("training_set_weight_input_0", "1.5")
        assertTaggedTextContainsAny("training_set_weight_input_1", "2")

        scenario.recreate()
        waitForNodeWithTag("training_record_dialog")

        assertTaggedTextContainsAny("training_set_weight_input_0", "1.5")
        assertTaggedTextContainsAny("training_set_weight_input_1", "2")
    }

    @Test
    fun savingPlanWorkoutShowsSetWeightsInCalendarAndAnalysis() {
        continueFromLoginIfNeeded()

        recordPlanExercise("training_plan_exercise_leg_press")

        composeRule.onNodeWithTag("training_tab_calendar").performClick()
        waitForNodeWithTag("calendar_day_workout_sheet")
        assertAnyTextInsideTagWithScroll("calendar_day_workout_sheet", "1/1.5/2 kg")

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_recent_records_card")
        assertAnyTextInsideTagWithScroll("training_recent_records_card", "1/1.5/2 kg")
    }

    @Test
    fun assistedLoadWorkoutShowsAssistanceAndEffectiveLoadInCalendarAndAnalysis() {
        continueFromLoginIfNeeded()
        trainingRepository.seedAssistedPullupLogForTest()

        composeRule.onNodeWithTag("training_tab_calendar").performClick()
        waitForNodeWithTag("calendar_day_workout_sheet")
        assertAnyTextInsideTagWithScroll(
            "calendar_day_workout_sheet",
            "보조 62.5 kg",
            "62.5 kg assist"
        )
        assertAnyTextInsideTagWithScroll(
            "calendar_day_workout_sheet",
            "유효부하 20 kg",
            "20 kg effective"
        )
        assertAnyTextInsideTagWithScroll(
            "calendar_day_workout_sheet",
            "유효볼륨 100 kg",
            "100 kg effective volume"
        )

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_recent_records_card")
        assertAnyTextInsideTagWithScroll(
            "training_recent_records_card",
            "보조 62.5 kg",
            "62.5 kg assist"
        )
        assertAnyTextInsideTagWithScroll(
            "training_recent_records_card",
            "유효부하 20 kg",
            "20 kg effective"
        )
    }

    @Test
    fun completedRoutineDayMarksUnrecordedExerciseAsSkippedInPlan() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_routine_source_custom")
        waitForNodeWithTag("training_next_routine_day_1")
        completeRoutineDayWithConfirmation()
        waitForNodeWithTag("training_next_routine_day_2")

        composeRule.onNodeWithTag("training_tab_plan").performClick()
        scrollToNodeWithTag("training_plan_exercise_machine_chest_press")
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("training_plan_exercise_machine_chest_press")) and
                hasTestTag("training_plan_skipped_chip")
        ).assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("training_plan_exercise_machine_chest_press")) and
                (hasText("건너뜀", substring = true) or hasText("Skipped", substring = true))
        ).assertIsDisplayed()
    }

    @Test
    fun savingAllTodayExercisesRequiresConfirmationBeforeRoutineDayAdvances() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_next_routine_day_1")
        openHomeStartWorkout()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        saveDefaultWeightedRecord()

        waitForNodeWithTag("training_complete_day_confirmation_dialog")
        assertEquals(0, trainingRepository.progressForTest().dayIndex)
        assertTaggedTextContainsAny("training_complete_day_confirmation_dialog", "오늘 운동", "Complete today")

        confirmVisibleRoutineDayCompletion()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.progressForTest().dayIndex == 1
        }
        waitForNodeWithTag("training_next_routine_day_2")
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
        confirmRoutineSwitchIfRequested()

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
        confirmRoutineSwitchIfRequested()
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

    @Test
    fun switchingRoutineAfterCustomDayOneClearsOnlyCurrentCycleWorkoutHistory() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_routine_source_custom")
        waitForNodeWithTag("training_next_routine_day_1")
        composeRule.onNodeWithTag("training_next_routine_focus_CHEST").assertIsDisplayed()

        openHomeStartWorkout()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        saveDefaultWeightedRecord()

        waitForNodeWithTag("training_complete_day_confirmation_dialog")
        assertEquals(0, trainingRepository.progressForTest().dayIndex)
        confirmVisibleRoutineDayCompletion()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.progressForTest().dayIndex == 1
        }
        waitForNodeWithTag("training_next_routine_day_2")
        composeRule.onNodeWithTag("training_next_routine_focus_LOWER_BODY").assertIsDisplayed()
        assertEquals("custom-test", trainingRepository.progressForTest().templateId)
        assertEquals(1, trainingRepository.workoutLogsForTest().size)
        assertEquals(setOf(FIXED_UI_DATE), trainingRepository.routineDayDatesForTest().values.toSet())
        val customLog = trainingRepository.workoutLogsForTest().single()
        assertNotNull(customLog.routineDayInstanceId)
        assertTrue(customLog.routineDayInstanceId!!.contains("custom-test"))

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_summary_band")
        composeRule.onNodeWithTag("training_summary_scope").assertIsDisplayed()
        assertTextInsideTag("training_summary_completion_rate", "50%")
        assertTextInsideTag("training_summary_total_sets", "3")
        composeRule.onNodeWithTag("training_recent_records_card").assertIsDisplayed()
        composeRule.onNodeWithTag("training_recent_records_scope").assertIsDisplayed()
        assertChestPressRecordVisible()

        switchToDefaultRoutineFromPlanTab()

        val switchedProgress = trainingRepository.progressForTest()
        assertEquals("beginner-full-body-3day", switchedProgress.templateId)
        assertEquals(0, switchedProgress.dayIndex)
        assertNull(switchedProgress.lastCompletedDayIndex)
        assertTrue(trainingRepository.routineDayDatesForTest().isEmpty())
        assertTrue(trainingRepository.workoutLogsForTest().isEmpty())

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_routine_source_default")
        waitForNodeWithTag("training_next_routine_day_1")
        composeRule.onAllNodesWithTag("training_latest_routine_day_completion_card").assertCountEquals(0)
        assertCurrentRoutineDateUnassigned()

        composeRule.onNodeWithTag("training_tab_plan").performClick()
        waitForNodeWithTag("training_current_routine_card")
        scrollToNodeWithTag("training_current_routine_source_default")
        composeRule.onNodeWithTag("training_current_routine_source_default").assertIsDisplayed()
        scrollToNodeWithTag("training_plan_exercise_leg_press")
        composeRule.onNodeWithTag("training_plan_exercise_leg_press").assertIsDisplayed()

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_summary_band")
        composeRule.onNodeWithTag("training_summary_scope").assertIsDisplayed()
        assertTextInsideTag("training_summary_completion_rate", "0%")
        assertTextInsideTag("training_summary_total_sets", "0")
        assertAnyTextInsideTag("training_summary_streak", "0일", "0 d")
        composeRule.onAllNodesWithTag("training_recent_records_card").assertCountEquals(0)
        composeRule.onAllNodes(
            hasText("체스트 프레스", substring = true) or hasText("Chest Press", substring = true)
        ).assertCountEquals(0)
    }

    @Test
    fun editingActiveCustomRoutineCanKeepCompletedProgressAndApplyFutureExerciseChanges() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_next_routine_day_1")
        recordCurrentHomeWorkout()
        confirmVisibleRoutineDayCompletion()
        waitForNodeWithTag("training_next_routine_day_2")
        assertEquals(1, trainingRepository.progressForTest().dayIndex)
        assertEquals(1, trainingRepository.workoutLogsForTest().size)

        editCurrentCustomRoutineDayTwoToGobletAndSave()
        composeRule.onNodeWithTag("training_custom_routine_progress_dialog").assertIsDisplayed()
        assertTaggedTextContainsAny("training_custom_routine_progress_dialog", "현재 사이클", "current-cycle")
        composeRule.onNodeWithTag("training_keep_custom_routine_progress").performClick()
        waitForNodeWithTagToDisappear("training_custom_routine_progress_dialog")
        waitForNodeWithTagToDisappear("training_custom_routine_builder")

        assertEquals(1, trainingRepository.progressForTest().dayIndex)
        assertEquals(1, trainingRepository.workoutLogsForTest().size)
        assertEquals(1, trainingRepository.routineDayDatesForTest().size)
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_custom",
            exerciseTag = "training_plan_exercise_goblet_squat"
        )
        composeRule.onAllNodesWithTag("training_plan_exercise_leg_press").assertCountEquals(0)
    }

    @Test
    fun editingActiveCustomRoutineCanResetCurrentCycleFromConfirmation() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_next_routine_day_1")
        recordCurrentHomeWorkout()
        confirmVisibleRoutineDayCompletion()
        waitForNodeWithTag("training_next_routine_day_2")
        assertEquals(1, trainingRepository.progressForTest().dayIndex)
        assertEquals(1, trainingRepository.workoutLogsForTest().size)

        editCurrentCustomRoutineDayTwoToGobletAndSave()
        composeRule.onNodeWithTag("training_reset_custom_routine_progress").performClick()
        waitForNodeWithTagToDisappear("training_custom_routine_progress_dialog")
        waitForNodeWithTagToDisappear("training_custom_routine_builder")

        assertEquals(0, trainingRepository.progressForTest().dayIndex)
        assertNull(trainingRepository.progressForTest().lastCompletedDayIndex)
        assertTrue(trainingRepository.workoutLogsForTest().isEmpty())
        assertTrue(trainingRepository.routineDayDatesForTest().isEmpty())
        assertHomeShowsCustomRoutine(
            dayTag = "training_next_routine_day_1",
            focusTag = "training_next_routine_focus_CHEST"
        )
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_custom",
            exerciseTag = "training_plan_exercise_goblet_squat"
        )
    }

    @Test
    fun switchingRoutineKeepsCompletedPastCycleRecordsButClearsCurrentCycleRecords() {
        continueFromLoginIfNeeded()
        trainingRepository.seedCompletedPastCycleAndCurrentCycleLogForSwitchTest()
        assertEquals(3, trainingRepository.workoutLogsForTest().size)

        switchToDefaultRoutineFromPlanTab("intermediate-body-part-4day-60")

        val switchedProgress = trainingRepository.progressForTest()
        assertEquals("intermediate-body-part-4day-60", switchedProgress.templateId)
        assertEquals(4, switchedProgress.cycleNumber)
        assertEquals(0, switchedProgress.dayIndex)
        assertNull(switchedProgress.lastCompletedDayIndex)
        assertTrue(trainingRepository.routineDayDatesForTest().isEmpty())
        assertEquals(1, trainingRepository.workoutLogsForTest().size)
        assertTrue(
            trainingRepository.workoutLogsForTest()
                .single()
                .routineDayInstanceId
                ?.contains("cycle3") == true
        )

        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_next_routine_day_1")
        composeRule.onAllNodesWithTag("training_latest_routine_day_completion_card").assertCountEquals(0)

        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_summary_band")
        assertTextInsideTag("training_summary_completion_rate", "0%")
        assertTextInsideTag("training_summary_total_sets", "0")
        composeRule.onNodeWithTag("training_recent_records_card").assertIsDisplayed()
        val pastRecordText = hasText("트레드밀 걷기", substring = true) or
            hasText("Treadmill Walk", substring = true)
        scrollToNode(pastRecordText)
        composeRule.onNode(pastRecordText).assertIsDisplayed()
    }

    @Test
    fun complexRoutineSwitchesKeepHomePlanAnalysisAndCurrentCycleDataConsistent() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        assertHomeShowsCustomRoutine(dayTag = "training_next_routine_day_1", focusTag = "training_next_routine_focus_CHEST")
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_custom",
            exerciseTag = "training_plan_exercise_machine_chest_press"
        )
        assertAnalysisCurrentCycleEmpty()

        recordCurrentHomeWorkout()
        confirmVisibleRoutineDayCompletion()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.progressForTest().dayIndex == 1
        }
        assertCurrentCycleState(
            templateId = "custom-test",
            dayIndex = 1,
            lastCompletedDayIndex = 0,
            logCount = 1,
            routineDayDateCount = 1,
            logInstanceContains = "custom-test"
        )
        assertHomeShowsCustomRoutine(dayTag = "training_next_routine_day_2", focusTag = "training_next_routine_focus_LOWER_BODY")
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_custom",
            exerciseTag = "training_plan_exercise_leg_press"
        )
        assertAnalysisShowsRecordedExercise(
            koreanName = "체스트 프레스",
            englishName = "Chest Press",
            totalSets = "3",
            completionRate = "50%"
        )

        switchToDefaultRoutineFromPlanTab()
        assertCurrentCycleState(
            templateId = "beginner-full-body-3day",
            dayIndex = 0,
            logCount = 0,
            routineDayDateCount = 0,
            logInstanceContains = null
        )
        assertHomeShowsDefaultRoutine(dayTag = "training_next_routine_day_1")
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_default",
            exerciseTag = "training_plan_exercise_leg_press"
        )
        assertAnalysisCurrentCycleEmpty("체스트 프레스" to "Chest Press")

        recordPlanExercise("training_plan_exercise_leg_press")
        assertCurrentCycleState(
            templateId = "beginner-full-body-3day",
            dayIndex = 0,
            logCount = 1,
            routineDayDateCount = 1,
            logInstanceContains = "beginner-full-body-3day"
        )
        assertHomeShowsDefaultRoutine(dayTag = "training_next_routine_day_1")
        assertCurrentRoutineDateAssigned()
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_default",
            exerciseTag = "training_plan_exercise_leg_press"
        )
        assertAnalysisShowsRecordedExercise(
            koreanName = "레그 프레스",
            englishName = "Leg Press",
            totalSets = "3",
            completionRate = "4%"
        )

        switchToDefaultRoutineFromPlanTab("intermediate-body-part-4day-60")
        assertCurrentCycleState(
            templateId = "intermediate-body-part-4day-60",
            dayIndex = 0,
            logCount = 0,
            routineDayDateCount = 0,
            logInstanceContains = null
        )
        assertHomeShowsDefaultRoutine(dayTag = "training_next_routine_day_1", focusTag = "training_next_routine_focus_BACK")
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_default",
            exerciseTag = "training_plan_exercise_lat_pulldown"
        )
        assertAnalysisCurrentCycleEmpty("레그 프레스" to "Leg Press")

        recordPlanExercise("training_plan_exercise_lat_pulldown")
        assertCurrentCycleState(
            templateId = "intermediate-body-part-4day-60",
            dayIndex = 0,
            logCount = 1,
            routineDayDateCount = 1,
            logInstanceContains = "intermediate-body-part-4day-60"
        )
        assertHomeShowsDefaultRoutine(dayTag = "training_next_routine_day_1", focusTag = "training_next_routine_focus_BACK")
        assertAnalysisShowsRecordedExercise(
            koreanName = "랫 풀다운",
            englishName = "Lat Pulldown",
            totalSets = "3",
            completionRate = "3%"
        )

        switchToCustomRoutineFromPlanTab()
        assertCurrentCycleState(
            templateId = "custom-test",
            dayIndex = 0,
            logCount = 0,
            routineDayDateCount = 0,
            logInstanceContains = null
        )
        assertHomeShowsCustomRoutine(dayTag = "training_next_routine_day_1", focusTag = "training_next_routine_focus_CHEST")
        assertPlanShowsCurrentRoutine(
            sourceTag = "training_current_routine_source_custom",
            exerciseTag = "training_plan_exercise_machine_chest_press"
        )
        assertAnalysisCurrentCycleEmpty("랫 풀다운" to "Lat Pulldown")
    }

    @Test
    fun completingLastRoutineDayRequiresConfirmationBeforeNextCycleStarts() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        completeRoutineDayWithConfirmation()
        waitForNodeWithTag("training_next_routine_day_2")
        trainingRepository.assignCurrentRoutineDayDateForTest(FIXED_UI_DATE)
        assertEquals(1, trainingRepository.progressForTest().cycleNumber)
        assertEquals(1, trainingRepository.progressForTest().dayIndex)

        openHomeStartWorkout()
        waitForNodeWithTag("training_record_dialog")
        saveDefaultWeightedRecord()
        waitForNodeWithTag("training_complete_day_confirmation_dialog")

        assertEquals(1, trainingRepository.progressForTest().cycleNumber)
        assertEquals(1, trainingRepository.progressForTest().dayIndex)
        assertCycleCompletionTextVisible()

        composeRule.onNodeWithTag("training_confirm_complete_routine_day").assertIsDisplayed().performClick()
        waitForNodeWithTagToDisappear("training_complete_day_confirmation_dialog")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.progressForTest().cycleNumber == 2 &&
                trainingRepository.progressForTest().dayIndex == 0
        }
        assertNull(trainingRepository.progressForTest().lastCompletedDayIndex)
        waitForNodeWithTag("training_next_routine_day_1")
    }

    @Test
    fun completingLastRoutineDayWithUnrecordedExerciseStillShowsCycleConfirmation() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        completeRoutineDayWithConfirmation()
        waitForNodeWithTag("training_next_routine_day_2")
        trainingRepository.assignCurrentRoutineDayDateForTest(FIXED_UI_DATE)

        scrollToNodeWithTag("training_complete_routine_day")
        composeRule.onNodeWithTag("training_complete_routine_day").performClick()
        waitForNodeWithTag("training_complete_day_confirmation_dialog")

        assertEquals(1, trainingRepository.progressForTest().cycleNumber)
        assertEquals(1, trainingRepository.progressForTest().dayIndex)
        assertCycleCompletionTextVisible()
        composeRule.onNodeWithTag("training_unrecorded_exercise_leg_press").assertIsDisplayed()

        composeRule.onNodeWithTag("training_confirm_complete_routine_day").assertIsDisplayed().performClick()
        waitForNodeWithTagToDisappear("training_complete_day_confirmation_dialog")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.progressForTest().cycleNumber == 2 &&
                trainingRepository.progressForTest().dayIndex == 0
        }
        assertNull(trainingRepository.progressForTest().lastCompletedDayIndex)
    }

    @Test
    fun launcherReentryWhileCycleCompletionDialogIsOpenDoesNotStackMainActivity() {
        continueFromLoginIfNeeded()
        createAndSelectTwoDayCustomRoutineForSwitch()

        composeRule.onNodeWithTag("training_tab_home").performClick()
        completeRoutineDayWithConfirmation()
        waitForNodeWithTag("training_next_routine_day_2")
        trainingRepository.assignCurrentRoutineDayDateForTest(FIXED_UI_DATE)

        scrollToNodeWithTag("training_complete_routine_day")
        composeRule.onNodeWithTag("training_complete_routine_day").performClick()
        waitForNodeWithTag("training_complete_day_confirmation_dialog")
        assertCycleCompletionTextVisible()
        assertMainActivityInstanceCount(1)

        launchAppFromLauncher()

        waitForNodeWithTag("training_complete_day_confirmation_dialog")
        composeRule.onAllNodesWithTag("brand_splash").assertCountEquals(0)
        assertMainActivityInstanceCount(1)
    }

    private fun resetTestState() {
        trainingRepository.reset()
        (sessionRepository as InMemorySessionRepository).reset()
    }

    private fun waitForLoginScreen() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeWithTag("login_screen")
        }
    }

    private fun waitForNodeWithTag(testTag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasNodeWithTag(testTag)
        }
    }

    private fun waitForNodeWithTagToDisappear(testTag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeWithTag(testTag)
        }
    }

    private fun confirmRoutineSwitchIfRequested() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val confirmVisible = hasNodeWithTag("training_routine_switch_confirm_dialog")
            val selectionDialogsClosed =
                !hasNodeWithTag("training_routine_library_dialog") &&
                    !hasNodeWithTag("training_routine_recommendations_dialog")
            confirmVisible || selectionDialogsClosed
        }
        if (!hasNodeWithTag("training_routine_switch_confirm_dialog")) {
            return
        }
        composeRule.onNodeWithTag("training_routine_switch_confirm_dialog").assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("training_routine_switch_confirm_dialog")) and
                (hasText("주의", substring = true) or hasText("Warning", substring = true))
        ).assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("training_routine_switch_confirm_dialog")) and
                (hasText("완료되지 않은 현재 사이클", substring = true) or
                    hasText("unfinished current cycle", substring = true))
        ).assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("training_routine_switch_confirm_dialog")) and
                (hasText("완전히 삭제", substring = true) or hasText("permanently deleted", substring = true))
        ).assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("training_routine_switch_confirm_dialog")) and
                (hasText("완료된 이전 사이클", substring = true) or hasText("completed past cycles", substring = true))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("training_confirm_routine_switch").performClick()
        waitForNodeWithTagToDisappear("training_routine_switch_confirm_dialog")
    }

    private fun assertRoutineChangePolicyTextVisibleInProfilePrompt() {
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("profile_routine_change_prompt")) and
                (hasText("완료되지 않은 현재 사이클", substring = true) or
                    hasText("unfinished current cycle", substring = true))
        ).assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("profile_routine_change_prompt")) and
                (hasText("운동 기록은 삭제", substring = true) or
                    hasText("workout records", substring = true))
        ).assertIsDisplayed()
        composeRule.onNode(
            hasAnyAncestor(hasTestTag("profile_routine_change_prompt")) and
                (hasText("완료된 이전 사이클", substring = true) or
                    hasText("completed past cycles", substring = true))
        ).assertIsDisplayed()
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
            hasNodeWithTag("login_screen") || hasNodeWithTag("training_tab_home")
        }
        if (hasNodeWithTag("login_screen")) {
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
            hasNodeWithTag("training_tab_home")
        }
    }

    private fun assertLegPressRecordVisibleInPlan() {
        scrollToNodeWithTag("training_plan_exercise_leg_press")
        composeRule.onNodeWithTag("training_plan_exercise_leg_press").assertIsDisplayed()
    }

    private fun createAndSelectTwoDayCustomRoutineForSwitch() {
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        composeRule.onNodeWithTag("training_create_custom_routine_button").performClick()
        waitForNodeWithTag("training_custom_routine_builder")
        composeRule.onNodeWithTag("training_custom_routine_name")
            .performTextReplacement("Switch Check Custom")
        composeRule.onNodeWithTag("training_custom_routine_name").performImeAction()
        composeRule.waitForIdle()

        selectCustomFocus("training_custom_focus_CHEST")
        scrollToNodeWithTag("training_custom_exercise_group_CHEST")
        composeRule.onNodeWithTag("training_custom_exercise_group_CHEST").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_machine_chest_press")
        composeRule.onNodeWithTag("training_custom_add_exercise_machine_chest_press").performClick()

        composeRule.onNodeWithTag("training_add_custom_day").performScrollTo().performClick()
        composeRule.onNodeWithTag("training_custom_day_tab_1").performClick()
        selectCustomFocus("training_custom_focus_LOWER_BODY")
        scrollToNodeWithTag("training_custom_exercise_group_LOWER_BODY")
        composeRule.onNodeWithTag("training_custom_exercise_group_LOWER_BODY").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_leg_press")
        composeRule.onNodeWithTag("training_custom_add_exercise_leg_press").performClick()
        composeRule.onNodeWithTag("training_save_custom_routine").performClick()

        waitForNodeWithTagToDisappear("training_custom_routine_builder")
        scrollToNodeWithTag("training_find_routine_button")
        composeRule.onNodeWithTag("training_find_routine_button").performClick()
        waitForNodeWithTag("training_custom_template_card")
        composeRule.onNodeWithTag("training_custom_template_card").performClick()
        confirmRoutineSwitchIfRequested()
        waitForNodeWithTagToDisappear("training_routine_library_dialog")
    }

    private fun openCustomExerciseForm() {
        composeRule.onNodeWithTag("training_tab_exercises").performClick()
        waitForNodeWithTag("training_custom_exercise_add_cta")
        composeRule.onNodeWithTag("training_custom_exercise_add_cta").performClick()
        waitForNodeWithTag("training_custom_exercise_form")
    }

    private fun createCustomExerciseFromExerciseTab() {
        openCustomExerciseForm()
        composeRule.onNodeWithTag("training_custom_exercise_name_input")
            .performTextReplacement("Hotel Cable Row")
        selectCustomExerciseDropdown(
            selectorTag = "training_custom_exercise_category_selector",
            optionTag = "training_custom_exercise_category_option_BACK"
        )
        selectCustomExerciseDropdown(
            selectorTag = "training_custom_exercise_equipment_selector",
            optionTag = "training_custom_exercise_equipment_option_CABLE"
        )
        selectCustomExerciseDropdown(
            selectorTag = "training_custom_exercise_difficulty_selector",
            optionTag = "training_custom_exercise_difficulty_option_ADVANCED"
        )
        scrollToNodeWithTag("training_custom_exercise_summary_input")
        composeRule.onNodeWithTag("training_custom_exercise_summary_input")
            .performTextReplacement("A custom back exercise for travel days.")
        scrollToNodeWithTag("training_custom_exercise_instruction_0_input")
        composeRule.onNodeWithTag("training_custom_exercise_instruction_0_input")
            .performTextReplacement("Pull the handle toward the ribs.")
        scrollToNodeWithTag("training_custom_exercise_safety_0_input")
        composeRule.onNodeWithTag("training_custom_exercise_safety_0_input")
            .performTextReplacement("Keep the shoulders away from the ears.")
        composeRule.onNodeWithTag("training_custom_exercise_save").performClick()
        waitForNodeWithTagToDisappear("training_custom_exercise_form")
        scrollToNodeWithTag("training_exercise_search")
        composeRule.onNodeWithTag("training_exercise_search").performTextReplacement("Hotel Cable Row")
        waitForNodeWithTag("training_exercise_row_custom_exercise_ui_1")
    }

    private fun selectCustomExerciseDropdown(selectorTag: String, optionTag: String) {
        scrollToNodeWithTag(selectorTag)
        composeRule.onNodeWithTag(selectorTag).performClick()
        waitForNodeWithTag(optionTag)
        composeRule.onNodeWithTag(optionTag).performClick()
        composeRule.waitForIdle()
    }

    private fun substituteVisibleRoutineExerciseWithIndoorBike() {
        composeRule.onNodeWithTag("training_substitute_routine_exercise")
            .performScrollTo()
            .performClick()
        waitForNodeWithTag("training_routine_exercise_picker_dialog")
        scrollToNodeWithTag("training_pick_routine_exercise_group_CARDIO")
        composeRule.onNodeWithTag("training_pick_routine_exercise_group_CARDIO").performClick()
        scrollToNodeWithTag("training_pick_routine_exercise_indoor_bike")
        composeRule.onNodeWithTag("training_pick_routine_exercise_indoor_bike").performClick()
        waitForNodeWithTagToDisappear("training_routine_exercise_picker_dialog")
    }

    private fun editCurrentCustomRoutineDayTwoToGobletAndSave() {
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        scrollToNodeWithTag("training_edit_current_custom_routine")
        composeRule.onNodeWithTag("training_edit_current_custom_routine").performClick()
        waitForNodeWithTag("training_custom_routine_builder")
        composeRule.onNodeWithTag("training_custom_day_tab_1").performClick()
        scrollToNodeWithTag("training_remove_custom_exercise_0")
        composeRule.onNodeWithTag("training_remove_custom_exercise_0").performClick()
        scrollToNodeWithTag("training_custom_exercise_group_LOWER_BODY")
        composeRule.onNodeWithTag("training_custom_exercise_group_LOWER_BODY").performClick()
        scrollToNodeWithTag("training_custom_add_exercise_goblet_squat")
        composeRule.onNodeWithTag("training_custom_add_exercise_goblet_squat").performClick()
        composeRule.onNodeWithTag("training_save_custom_routine").performClick()
        waitForNodeWithTag("training_custom_routine_progress_dialog")
    }

    private fun switchToDefaultRoutineFromPlanTab(templateId: String = "beginner-full-body-3day") {
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        scrollToNodeWithTag("training_find_routine_button")
        composeRule.onNodeWithTag("training_find_routine_button").performClick()
        waitForNodeWithTag("training_routine_library_dialog")
        scrollToNodeWithTag("training_template_card_$templateId")
        composeRule.onNodeWithTag("training_template_card_$templateId").performClick()
        confirmRoutineSwitchIfRequested()
        waitForNodeWithTagToDisappear("training_routine_library_dialog")
    }

    private fun switchToCustomRoutineFromPlanTab() {
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        scrollToNodeWithTag("training_find_routine_button")
        composeRule.onNodeWithTag("training_find_routine_button").performClick()
        waitForNodeWithTag("training_routine_library_dialog")
        scrollToNodeWithTag("training_custom_template_card")
        composeRule.onNodeWithTag("training_custom_template_card").performClick()
        confirmRoutineSwitchIfRequested()
        waitForNodeWithTagToDisappear("training_routine_library_dialog")
    }

    private fun openHomeStartWorkout() {
        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_start_workout")
        scrollToNodeWithTag("training_home_start_workout")
        composeRule.onNodeWithTag("training_home_start_workout").performClick()
    }

    private fun assertTopLevelBackStaysOnScreen(tabTag: String, screenTag: String) {
        composeRule.onNodeWithTag(tabTag).performClick()
        waitForNodeWithTag(screenTag)
        scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag(screenTag).assertCountEquals(1)
    }

    private fun launchAppFromLauncher() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        val launchIntent = targetContext.packageManager
            .getLaunchIntentForPackage(targetContext.packageName)
        assertNotNull(launchIntent)
        targetContext.startActivity(
            launchIntent!!.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        instrumentation.waitForIdleSync()
        composeRule.waitForIdle()
    }

    private fun assertMainActivityInstanceCount(expected: Int) {
        assertEquals(expected, mainActivityInstanceCount())
    }

    private fun mainActivityInstanceCount(): Int {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var count = 0
        instrumentation.runOnMainSync {
            count = listOf(
                Stage.CREATED,
                Stage.STARTED,
                Stage.RESUMED,
                Stage.PAUSED,
                Stage.STOPPED
            )
                .flatMap { stage -> ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage) }
                .toSet()
                .count { activity -> activity is MainActivity }
        }
        return count
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
                hasText("선택", substring = true) or
                    hasText("Select", substring = true) or
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

    private fun saveVisibleRecordWithoutClosingContinuousDialog() {
        if (hasNodeWithTag("training_set_weight_input_0")) {
            selectSetWeight(index = 0, optionTagValue = "1")
            if (hasNodeWithTag("training_set_weight_input_1")) {
                selectSetWeight(index = 1, optionTagValue = "1_5")
            }
            if (hasNodeWithTag("training_set_weight_input_2")) {
                selectSetWeight(index = 2, optionTagValue = "2")
            }
        }
        composeRule.onNodeWithTag("training_save_record").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            trainingRepository.workoutLogsForTest().isNotEmpty()
        }
    }

    private fun closeRecordDialogIfVisible() {
        composeRule.waitForIdle()
        if (hasNodeWithTag("training_record_dialog")) {
            composeRule.onNodeWithTag("training_close_record_dialog").performClick()
            waitForNodeWithTagToDisappear("training_record_dialog")
        }
    }

    private fun recordCurrentHomeWorkout() {
        openHomeStartWorkout()
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        saveDefaultWeightedRecord()
    }

    private fun recordPlanExercise(exerciseTag: String) {
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        clickPlanExercise(exerciseTag)
        confirmRoutineDayDateIfNeeded()
        waitForNodeWithTag("training_record_dialog")
        saveDefaultWeightedRecord()
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

    private fun assertTaggedTextContainsAny(testTag: String, vararg texts: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            hasTaggedTextContainingAny(testTag, *texts)
        }
        runCatching {
            composeRule.onNodeWithTag(testTag, useUnmergedTree = true).performScrollTo()
        }
        composeRule.onAllNodes(taggedTextMatcher(testTag, *texts), useUnmergedTree = true)[0]
            .assertIsDisplayed()
    }

    private fun hasTaggedTextContainingAny(testTag: String, vararg texts: String): Boolean =
        runCatching {
            composeRule.onAllNodes(taggedTextMatcher(testTag, *texts), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }.getOrDefault(false)

    private fun taggedTextMatcher(testTag: String, vararg texts: String): SemanticsMatcher {
        val textMatcher = texts
            .map { text -> hasText(text, substring = true) }
            .reduce { left, right -> left or right }
        return (hasTestTag(testTag) and textMatcher) or
            (hasAnyAncestor(hasTestTag(testTag)) and textMatcher)
    }

    private fun assertAnyTextInsideTag(containerTag: String, vararg texts: String) {
        val matcher = texts
            .map { text -> hasText(text, substring = true) }
            .reduce { left, right -> left or right }
        composeRule.onNode(hasAnyAncestor(hasTestTag(containerTag)) and matcher)
            .assertIsDisplayed()
    }

    private fun waitForTextInsideTag(containerTag: String, vararg texts: String) {
        val matcher = hasAnyAncestor(hasTestTag(containerTag)) and texts
            .map { text -> hasText(text, substring = true) }
            .reduce { left, right -> left or right }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(matcher, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assertAnyTextInsideTagWithScroll(containerTag: String, vararg texts: String) {
        val matcher = hasAnyAncestor(hasTestTag(containerTag)) and texts
            .map { text -> hasText(text, substring = true) }
            .reduce { left, right -> left or right }
        scrollToNode(matcher)
        composeRule.onAllNodes(matcher)[0].assertIsDisplayed()
    }

    private fun assertCycleCompletionTextVisible() {
        val matcher = hasAnyAncestor(hasTestTag("training_complete_day_confirmation_dialog")) and
            (hasText("사이클", substring = true) or hasText("cycle", substring = true))
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertChestPressRecordVisible() {
        composeRule.onNode(
            hasText("체스트 프레스", substring = true) or hasText("Chest Press", substring = true)
        ).assertIsDisplayed()
    }

    private fun assertCurrentCycleState(
        templateId: String,
        dayIndex: Int,
        lastCompletedDayIndex: Int? = null,
        logCount: Int,
        routineDayDateCount: Int,
        logInstanceContains: String?
    ) {
        val progress = trainingRepository.progressForTest()
        assertEquals(templateId, progress.templateId)
        assertEquals(dayIndex, progress.dayIndex)
        assertEquals(lastCompletedDayIndex, progress.lastCompletedDayIndex)
        assertEquals(routineDayDateCount, trainingRepository.routineDayDatesForTest().size)
        assertEquals(logCount, trainingRepository.workoutLogsForTest().size)
        logInstanceContains?.let { expected ->
            assertTrue(
                trainingRepository.workoutLogsForTest().all { log ->
                    log.routineDayInstanceId?.contains(expected) == true
                }
            )
        }
    }

    private fun assertHomeShowsCustomRoutine(dayTag: String, focusTag: String) {
        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_routine_source_custom")
        waitForNodeWithTag(dayTag)
        composeRule.onNodeWithTag(focusTag).assertIsDisplayed()
    }

    private fun assertHomeShowsDefaultRoutine(dayTag: String, focusTag: String? = null) {
        composeRule.onNodeWithTag("training_tab_home").performClick()
        waitForNodeWithTag("training_home_routine_source_default")
        waitForNodeWithTag(dayTag)
        focusTag?.let { composeRule.onNodeWithTag(it).assertIsDisplayed() }
    }

    private fun assertPlanShowsCurrentRoutine(sourceTag: String, exerciseTag: String) {
        composeRule.onNodeWithTag("training_tab_plan").performClick()
        scrollToNodeWithTag("training_current_routine_card")
        scrollToNodeWithTag(sourceTag)
        composeRule.onNodeWithTag(sourceTag).assertIsDisplayed()
        scrollToNodeWithTag(exerciseTag)
        composeRule.onNodeWithTag(exerciseTag).assertIsDisplayed()
    }

    private fun assertAnalysisCurrentCycleEmpty(vararg absentExercises: Pair<String, String>) {
        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_summary_band")
        composeRule.onNodeWithTag("training_summary_scope").assertIsDisplayed()
        assertTextInsideTag("training_summary_completion_rate", "0%")
        assertTextInsideTag("training_summary_total_sets", "0")
        assertAnyTextInsideTag("training_summary_streak", "0일", "0 d")
        composeRule.onAllNodesWithTag("training_recent_records_card").assertCountEquals(0)
        absentExercises.forEach { (koreanName, englishName) ->
            composeRule.onAllNodes(
                hasText(koreanName, substring = true) or hasText(englishName, substring = true)
            ).assertCountEquals(0)
        }
    }

    private fun assertAnalysisShowsRecordedExercise(
        koreanName: String,
        englishName: String,
        totalSets: String,
        completionRate: String
    ) {
        composeRule.onNodeWithTag("training_tab_analysis").performClick()
        waitForNodeWithTag("training_summary_band")
        composeRule.onNodeWithTag("training_summary_scope").assertIsDisplayed()
        assertTextInsideTag("training_summary_completion_rate", completionRate)
        assertTextInsideTag("training_summary_total_sets", totalSets)
        composeRule.onNodeWithTag("training_recent_records_card").assertIsDisplayed()
        composeRule.onNodeWithTag("training_recent_records_scope").assertIsDisplayed()
        composeRule.onNode(
            hasText(koreanName, substring = true) or hasText(englishName, substring = true)
        ).assertIsDisplayed()
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
        confirmVisibleRoutineDayCompletion()
    }

    private fun confirmVisibleRoutineDayCompletion() {
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
            hasNodeWithTag("training_routine_day_date_dialog") ||
                hasNodeWithTag("training_record_dialog") ||
                hasNodeWithTag("training_complete_day_confirmation_dialog")
        }
        if (!hasNodeWithTag("training_routine_day_date_dialog")) return
        composeRule.onNodeWithTag("training_confirm_routine_day_date").assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            !hasNodeWithTag("training_routine_day_date_dialog")
        }
    }

    private fun hasNodeWithTag(testTag: String): Boolean =
        runCatching {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
        }.getOrDefault(false)

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
        scrollToNode(hasTestTag(testTag))
        waitForNodeWithTag(testTag)
    }

    private fun scrollToNode(matcher: SemanticsMatcher) {
        val scrollContainerCount = composeRule.onAllNodes(hasScrollAction()).fetchSemanticsNodes().size
        var lastFailure: AssertionError? = null
        for (index in 0 until scrollContainerCount) {
            try {
                composeRule.onAllNodes(hasScrollAction())[index]
                    .performScrollToNode(matcher)
                if (composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()) {
                    lastFailure = null
                    break
                }
            } catch (error: AssertionError) {
                lastFailure = error
            }
        }
        lastFailure?.let { throw it }
    }
}

private val FIXED_UI_DATE: LocalDate = LocalDate.of(2026, 5, 24)
