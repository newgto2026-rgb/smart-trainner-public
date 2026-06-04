package com.smarttrainner.core.network

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class NetworkBuildConfigTest {
    @Test
    fun serverBaseUrlIsAbsoluteAndRetrofitReady() {
        val baseUrl = BuildConfig.SMART_TRAINNER_SERVER_BASE_URL

        assertThat(baseUrl).isNotEmpty()
        assertThat(baseUrl.endsWith("/")).isTrue()
        assertThat(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")).isTrue()
    }

    @Test
    fun routineCycleCompletionsResponse_decodesCycleDurationHistory() {
        val response = Json.decodeFromString<RoutineCycleCompletionsResponse>(
            """
            {
              "data": [
                {
                  "id": "cycle-1",
                  "sessionId": "google-user-1",
                  "templateId": "beginner-full-body-3day",
                  "cycleNumber": 1,
                  "startedAt": "2026-06-01T00:00:00.000Z",
                  "completedAt": "2026-06-08T00:00:00.000Z",
                  "durationDays": 7,
                  "completedDayIndex": 2,
                  "createdAt": "2026-06-08T00:00:00.000Z",
                  "updatedAt": "2026-06-08T00:00:00.000Z"
                },
                {
                  "id": "cycle-2",
                  "sessionId": "google-user-1",
                  "templateId": "beginner-full-body-3day",
                  "cycleNumber": 2,
                  "startedAt": "2026-06-08T00:00:00.000Z",
                  "completedAt": "2026-06-22T00:00:00.000Z",
                  "durationDays": 14,
                  "completedDayIndex": 2,
                  "createdAt": "2026-06-22T00:00:00.000Z",
                  "updatedAt": "2026-06-22T00:00:00.000Z"
                }
              ],
              "count": 2
            }
            """.trimIndent()
        )

        assertThat(response.count).isEqualTo(2)
        assertThat(response.data.map { it.durationDays }).containsExactly(7, 14).inOrder()
    }
}
