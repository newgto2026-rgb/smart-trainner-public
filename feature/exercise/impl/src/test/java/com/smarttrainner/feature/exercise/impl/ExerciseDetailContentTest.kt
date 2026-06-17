package com.smarttrainner.feature.exercise.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExerciseDetailContentTest {
    @Test
    fun formattedStepLabelDoesNotDuplicateExistingStepNumber() {
        assertThat(formattedStepLabel(1, "1단계")).isEqualTo("1단계")
        assertThat(formattedStepLabel(2, "Step 2")).isEqualTo("Step 2")
        assertThat(formattedStepLabel(3, "Line 3")).isEqualTo("Line 3")
    }

    @Test
    fun formattedStepLabelAddsNumberToDescriptiveLabels() {
        assertThat(formattedStepLabel(1, "Set up")).isEqualTo("1. Set up")
        assertThat(formattedStepLabel(2, "당기기")).isEqualTo("2. 당기기")
    }
}
