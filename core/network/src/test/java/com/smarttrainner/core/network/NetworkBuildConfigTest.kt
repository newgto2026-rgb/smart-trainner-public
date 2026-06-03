package com.smarttrainner.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NetworkBuildConfigTest {
    @Test
    fun debugServerBaseUrlDefaultsToCurrentCloudflareHost() {
        assertThat(BuildConfig.SMART_TRAINNER_SERVER_BASE_URL)
            .isEqualTo("https://cute-lookup-dangerous-promotes.trycloudflare.com/")
    }

    @Test
    fun debugServerBaseUrlIsHttpsAndRetrofitReady() {
        assertThat(BuildConfig.SMART_TRAINNER_SERVER_BASE_URL).startsWith("https://")
        assertThat(BuildConfig.SMART_TRAINNER_SERVER_BASE_URL).endsWith("/")
    }
}
