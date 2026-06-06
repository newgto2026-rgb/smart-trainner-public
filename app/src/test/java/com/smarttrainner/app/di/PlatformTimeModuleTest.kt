package com.smarttrainner.app.di

import com.google.common.truth.Truth.assertThat
import java.time.ZoneId
import org.junit.Test

class PlatformTimeModuleTest {
    @Test
    fun provideClock_usesDeviceDefaultZoneForUserFacingDates() {
        val clock = PlatformTimeModule.provideClock()

        assertThat(clock.zone).isEqualTo(ZoneId.systemDefault())
    }
}
