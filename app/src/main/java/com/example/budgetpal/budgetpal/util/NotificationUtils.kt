package com.example.budgetpal.budgetpal.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.budgetpal.budgetpal.ui.dashboard.DashboardActivity
import com.example.budgetpal.budgetpal.R
import com.example.budgetpal.budgetpal.data.PreferenceManager

object NotificationUtils {
    private const val CHANNEL_ID = "budget_alerts"
    private const val CHANNEL_NAME = "Budget Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for budget alerts and updates"
    private const val NOTIFICATION_ID = 1

    fun showBudgetNotification(context: Context, title: String, message: String) {
        val preferenceManager = PreferenceManager(context)
        if (!preferenceManager.areNotificationsEnabled()) {
            return
        }

        createNotificationChannel(context)

        val intent = Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle permission denied
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 200, 300)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(defaultSoundUri, null)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}