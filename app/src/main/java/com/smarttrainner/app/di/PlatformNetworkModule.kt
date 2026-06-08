package com.smarttrainner.app.di

import com.smarttrainner.core.domain.DeviceSessionStore
import com.smarttrainner.core.network.FriendNetworkApi
import com.smarttrainner.core.network.PushTokenNetworkApi
import com.smarttrainner.core.network.RoutineProgressNetworkApi
import com.smarttrainner.core.network.RoutineNetworkApi
import com.smarttrainner.core.network.SessionNetworkApi
import com.smarttrainner.core.network.WorkoutLogNetworkApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object PlatformNetworkModule {
    @Provides
    @Singleton
    @OptIn(ExperimentalSerializationApi::class)
    fun provideNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        deviceSessionStore: DeviceSessionStore
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val deviceId = runBlocking { deviceSessionStore.installationDeviceId() }
            val response = chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("x-smart-trainner-device-id", deviceId)
                    .build()
            )
            if (response.code != 401) return@addInterceptor response

            val body = response.body ?: return@addInterceptor response
            val contentType = body.contentType()
            val bodyText = body.string()
            if (INVALID_DEVICE_ERROR_CODES.any { bodyText.contains(it) }) {
                runBlocking { deviceSessionStore.clearActiveSession() }
            }
            response.newBuilder()
                .body(bodyText.toResponseBody(contentType))
                .build()
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        client: OkHttpClient
    ): Retrofit {
        val configuredBaseUrl = com.smarttrainner.core.network.BuildConfig.SMART_TRAINNER_SERVER_BASE_URL
        val baseUrl = if (configuredBaseUrl.endsWith('/')) configuredBaseUrl else "$configuredBaseUrl/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideRoutineProgressNetworkApi(retrofit: Retrofit): RoutineProgressNetworkApi =
        retrofit.create(RoutineProgressNetworkApi::class.java)

    @Provides
    @Singleton
    fun provideRoutineNetworkApi(retrofit: Retrofit): RoutineNetworkApi =
        retrofit.create(RoutineNetworkApi::class.java)

    @Provides
    @Singleton
    fun provideSessionNetworkApi(retrofit: Retrofit): SessionNetworkApi =
        retrofit.create(SessionNetworkApi::class.java)

    @Provides
    @Singleton
    fun provideWorkoutLogNetworkApi(retrofit: Retrofit): WorkoutLogNetworkApi =
        retrofit.create(WorkoutLogNetworkApi::class.java)

    @Provides
    @Singleton
    fun provideFriendNetworkApi(retrofit: Retrofit): FriendNetworkApi =
        retrofit.create(FriendNetworkApi::class.java)

    @Provides
    @Singleton
    fun providePushTokenNetworkApi(retrofit: Retrofit): PushTokenNetworkApi =
        retrofit.create(PushTokenNetworkApi::class.java)

    private val INVALID_DEVICE_ERROR_CODES = setOf(
        "DEVICE_REQUIRED",
        "DEVICE_SESSION_REPLACED"
    )
}
