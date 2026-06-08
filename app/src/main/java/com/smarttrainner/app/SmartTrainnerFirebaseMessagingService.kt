package com.smarttrainner.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.smarttrainner.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmartTrainnerFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var tokenRegistrar: PushTokenRegistrar

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            tokenRegistrar.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        showNotification(title, body, message.data)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    FRIEND_CHANNEL_ID,
                    getString(R.string.friend_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val notificationKey = notificationKey(data)
        val contentIntent = PendingIntent.getActivity(
            this,
            notificationKey.requestCode,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_OPEN_DESTINATION, OPEN_DESTINATION_FRIENDS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, FRIEND_CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_empty_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        if (notificationKey.tag == null) {
            manager.notify(notificationKey.id, notification)
        } else {
            manager.notify(notificationKey.tag, notificationKey.id, notification)
        }
    }

    private fun notificationKey(data: Map<String, String>): NotificationKey {
        val eventTag = notificationEventTag(data)
        val requestCode = nextNotificationId()
        return if (eventTag == null) {
            NotificationKey(tag = null, id = requestCode, requestCode = requestCode)
        } else {
            NotificationKey(tag = eventTag, id = FRIEND_NOTIFICATION_ID, requestCode = requestCode)
        }
    }

    private fun notificationEventTag(data: Map<String, String>): String? =
        listOf("notificationEventId", "notificationId", "friendRequestId", "friendshipId")
            .firstNotNullOfOrNull { key -> data[key]?.takeIf { value -> value.isNotBlank() } }

    private fun nextNotificationId(): Int =
        nextNotificationSequence.updateAndGet { id -> if (id == Int.MAX_VALUE) 1 else id + 1 }

    private companion object {
        const val FRIEND_CHANNEL_ID = "friend_alerts"
        const val FRIEND_NOTIFICATION_ID = 1001
        val nextNotificationSequence = AtomicInteger(0)
    }
}

private data class NotificationKey(
    val tag: String?,
    val id: Int,
    val requestCode: Int
)
