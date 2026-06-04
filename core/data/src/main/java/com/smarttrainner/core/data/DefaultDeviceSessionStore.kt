package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.DeviceSessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDeviceSessionStore @Inject constructor(
    private val preferences: TrainingPreferencesDataSource
) : DeviceSessionStore {
    @Volatile
    private var cachedDeviceId: String? = null

    override suspend fun installationDeviceId(): String =
        cachedDeviceId ?: preferences.installationDeviceId()
            .also { cachedDeviceId = it }

    override suspend fun clearActiveSession() {
        preferences.clearActiveSession()
    }
}
