package com.smarttrainner.core.domain

interface DeviceSessionStore {
    suspend fun installationDeviceId(): String
    suspend fun clearActiveSession()
}
