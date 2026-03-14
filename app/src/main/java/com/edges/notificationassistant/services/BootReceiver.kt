package com.edges.notificationassistant.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver restarts the notification listener service after device reboot.
 * 
 * **Validates: Requirements 1.7, 16.3, 16.4**
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted - EDGES service will restart automatically")
            // NotificationListenerService restarts automatically after boot
            // No manual restart needed
        }
    }
}
