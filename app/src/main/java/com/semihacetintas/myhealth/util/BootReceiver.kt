package com.semihacetintas.myhealth.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver that gets notified when the device boots up.
 * This is used to restore scheduled notifications after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, restoring notifications")
            // In a real app, you would retrieve saved notifications from SharedPreferences or a database
            // and reschedule them using NotificationHelper
            
            // Example:
            // val notificationHelper = NotificationHelper(context)
            // val savedNotifications = getSavedNotifications(context)
            // for (notification in savedNotifications) {
            //     notificationHelper.scheduleNotification(
            //         notification.id,
            //         notification.timeInMillis,
            //         notification.title,
            //         notification.message
            //     )
            // }
        }
    }
} 