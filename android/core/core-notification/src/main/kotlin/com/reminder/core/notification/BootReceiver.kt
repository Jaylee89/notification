package com.reminder.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminder.core.notification.FileLogger

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            FileLogger.debug("Boot", "手机重启，重新调度提醒")
            val notificationHelper = NotificationHelper(context)
            notificationHelper.rescheduleAfterBoot()
        }
    }
}
