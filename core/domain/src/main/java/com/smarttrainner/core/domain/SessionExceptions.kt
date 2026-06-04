package com.smarttrainner.core.domain

class DeviceLoginConflictException(
    val activeDeviceName: String?
) : IllegalStateException("Another device is already signed in.")
