package com.smarttrainner.feature.training.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExerciseStepTextTest {
    @Test
    fun instructionWithoutRepeatedStepTitleRemovesMatchingTitlePrefix() {
        assertThat(
            instructionWithoutRepeatedStepTitle(
                label = "시작 자세",
                instruction = "시작 자세: 등을 바닥에 붙이고 준비합니다."
            )
        ).isEqualTo("등을 바닥에 붙이고 준비합니다.")

        assertThat(
            instructionWithoutRepeatedStepTitle(
                label = "발·코어 정렬",
                instruction = " 발·코어 정렬 ： 장비와 몸의 기준점을 맞춥니다."
            )
        ).isEqualTo("장비와 몸의 기준점을 맞춥니다.")
    }

    @Test
    fun instructionWithoutRepeatedStepTitleKeepsDifferentLeadingText() {
        assertThat(
            instructionWithoutRepeatedStepTitle(
                label = "시작 자세",
                instruction = "등을 바닥에 붙이고 준비합니다."
            )
        ).isEqualTo("등을 바닥에 붙이고 준비합니다.")
    }

    @Test
    fun instructionWithoutRepeatedStepTitleIgnoresPrefixCase() {
        assertThat(
            instructionWithoutRepeatedStepTitle(
                label = "Start Position",
                instruction = "start position: Brace the ribs before moving."
            )
        ).isEqualTo("Brace the ribs before moving.")
    }
}
