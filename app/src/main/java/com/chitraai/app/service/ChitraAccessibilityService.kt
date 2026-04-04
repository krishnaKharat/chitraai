package com.chitraai.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class ChitraAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ChitraAccessibility"
        var isEnabled = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isEnabled = true
        Log.d(TAG, "Accessibility service connected")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Used to relay UI context during support sessions
        // In a full implementation, this would send UI tree to the helper
        event?.let {
            Log.d(TAG, "Event: ${it.eventType} | ${it.className}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        isEnabled = false
        super.onDestroy()
    }
}
