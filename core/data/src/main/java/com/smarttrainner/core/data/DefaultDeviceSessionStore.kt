package com.smarttrainner.core.data

import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.domain.DeviceSessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDeviceSessionStore @Inject constructor(
    private val preferences: TrainingPreferencesDataSource
) : DeviceSessionStore {
    override suspend fun installationDeviceId(): String =
        preferences.installationDeviceId()

    override suspend fun clearActiveSession() {
        preferences.clearActiveSession()
    }
}
