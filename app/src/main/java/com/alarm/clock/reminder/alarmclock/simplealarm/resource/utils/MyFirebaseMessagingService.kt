package com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("SERVICE", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
        }
        if (remoteMessage.notification != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body)
        }

        // mục đích của việc này là thêm event uninstall cho AppFlyer
        if (remoteMessage.data
                .containsKey("af-uinstall-tracking")
        ) { // "uinstall" is not a typo
            return
        } else {
            // handleNotification(remoteMessage);
        }
    }

    companion object {
        private const val TAG = "SERVICE"
    }
}

