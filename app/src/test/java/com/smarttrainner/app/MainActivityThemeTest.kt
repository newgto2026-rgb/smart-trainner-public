package com.smarttrainner.app

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import org.junit.Test

class MainActivityThemeTest {
    @Test
    fun windowBackgroundColorMatchesThemePaperColors() {
        assertThat(SmartTrainnerThemeTone.Black.windowBackgroundColor()).isEqualTo(0xFF070A0F.toInt())
        assertThat(SmartTrainnerThemeTone.Red.windowBackgroundColor()).isEqualTo(0xFFF9F6F7.toInt())
        assertThat(SmartTrainnerThemeTone.Blue.windowBackgroundColor()).isEqualTo(0xFFF6FAFC.toInt())
        assertThat(SmartTrainnerThemeTone.Green.windowBackgroundColor()).isEqualTo(0xFFF5F7FA.toInt())
    }
}
