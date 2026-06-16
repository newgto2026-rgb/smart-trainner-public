package com.smarttrainner.app

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class BrandSplashGateTest {
    @Before
    fun setUp() {
        BrandSplashGate.resetForTest()
    }

    @After
    fun tearDown() {
        BrandSplashGate.resetForTest()
    }

    @Test
    fun brandSplashShowsOnlyOncePerProcess() {
        assertThat(BrandSplashGate.shouldShow()).isTrue()
        assertThat(BrandSplashGate.markShownIfNeeded()).isTrue()
        assertThat(BrandSplashGate.shouldShow()).isFalse()
        assertThat(BrandSplashGate.markShownIfNeeded()).isFalse()
    }
}
