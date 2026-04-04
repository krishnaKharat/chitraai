package com.chitraai.app.manager

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.ByteArrayOutputStream

class ConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "ConnectionManager"
        private const val SESSION_KEY = "active_session"
    }

    enum class Status { IDLE, CONNECTING, CONNECTED, DISCONNECTED }

    private val _statusLiveData = MutableLiveData(Status.IDLE)
    val statusLiveData: LiveData<Status> = _statusLiveData

    private var sessionId: String? = null

    // Firebase disabled until google-services.json is configured
    // All calls run in simulation mode
    private val db = null

    fun initiate() {
        _statusLiveData.postValue(Status.CONNECTING)
        sessionId = "session_${System.currentTimeMillis()}"
        Log.d(TAG, "Firebase not configured, using simulation mode")
        simulateConnection()
    }

    fun setConnected() {
        _statusLiveData.postValue(Status.CONNECTED)
        Log.d(TAG, "Status set to CONNECTED (simulation)")
    }

    fun disconnect() {
        _statusLiveData.postValue(Status.DISCONNECTED)
        sessionId = null
        Log.d(TAG, "Disconnected (simulation)")
    }

    fun reset() {
        _statusLiveData.postValue(Status.IDLE)
    }

    fun sendFrame(bitmap: Bitmap) {
        val sid = sessionId ?: return
        try {
            val outputStream = ByteArrayOutputStream()
            val scaled = Bitmap.createScaledBitmap(
                bitmap,
                480,
                (bitmap.height * 480f / bitmap.width).toInt(),
                true
            )
            scaled.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            scaled.recycle()
            // Frame ready - will be sent to Firebase once configured
            Log.d(TAG, "Frame encoded for session $sid, size=${base64.length} chars (simulation)")
        } catch (e: Exception) {
            Log.e(TAG, "Frame send error: ${e.message}")
        }
    }

    private fun simulateConnection() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _statusLiveData.postValue(Status.CONNECTED)
            Log.d(TAG, "Simulated connection established")
        }, 1500)
    }
}
