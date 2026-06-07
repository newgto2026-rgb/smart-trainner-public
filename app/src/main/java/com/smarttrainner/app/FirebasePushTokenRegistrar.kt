package com.smarttrainner.app

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.smarttrainner.core.domain.RegisterPushTokenUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

interface PushTokenRegistrar {
    suspend fun registerCurrentTokenIfConfigured(): Result<Boolean>
    suspend fun registerToken(token: String): Result<Boolean>
}

@Singleton
class FirebasePushTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val registerPushTokenUseCase: RegisterPushTokenUseCase
) : PushTokenRegistrar {
    override suspend fun registerCurrentTokenIfConfigured(): Result<Boolean> {
        if (FirebaseApp.getApps(context).isEmpty()) return Result.success(false)
        return getCurrentToken().fold(
            onSuccess = { token -> registerToken(token) },
            onFailure = { error -> Result.failure(error) }
        )
    }

    override suspend fun registerToken(token: String): Result<Boolean> =
        registerPushTokenUseCase(token).map { true }

    private suspend fun getCurrentToken(): Result<String> =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener
                val token = task.result
                if (task.isSuccessful && !token.isNullOrBlank()) {
                    continuation.resume(Result.success(token))
                } else {
                    continuation.resume(
                        Result.failure(task.exception ?: IllegalStateException("FCM token unavailable."))
                    )
                }
            }
        }
}
