package com.smarttrainner.core.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object SmartTrainnerNetwork {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun createSessionApi(
        baseUrl: String = BuildConfig.SMART_TRAINNER_SERVER_BASE_URL
    ): SessionNetworkApi = retrofit(baseUrl).create(SessionNetworkApi::class.java)

    fun createWorkoutLogApi(
        baseUrl: String = BuildConfig.SMART_TRAINNER_SERVER_BASE_URL
    ): WorkoutLogNetworkApi = retrofit(baseUrl).create(WorkoutLogNetworkApi::class.java)

    @OptIn(ExperimentalSerializationApi::class)
    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
