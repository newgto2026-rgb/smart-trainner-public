package com.smarttrainner.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NetworkBuildConfigTest {
    @Test
    fun serverBaseUrlIsAbsoluteAndRetrofitReady() {
        val baseUrl = BuildConfig.SMART_TRAINNER_SERVER_BASE_URL

        assertThat(baseUrl).isNotEmpty()
        assertThat(baseUrl.endsWith("/")).isTrue()
        assertThat(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")).isTrue()
    }
}
