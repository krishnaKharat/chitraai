package com.chitraai.app.manager

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
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
    private val db = try {
        FirebaseDatabase.getInstance().reference
    } catch (e: Exception) {
        null
    }

    fun initiate() {
        _statusLiveData.postValue(Status.CONNECTING)
        sessionId = "session_${System.currentTimeMillis()}"

        db?.child(SESSION_KEY)?.child(sessionId!!)?.setValue(
            mapOf(
                "status" to "connecting",
                "device" to android.os.Build.MODEL,
                "timestamp" to ServerValue.TIMESTAMP
            )
        )?.addOnSuccessListener {
            _statusLiveData.postValue(Status.CONNECTED)
            Log.d(TAG, "Session initiated: $sessionId")
        }?.addOnFailureListener {
            // Simulation mode if Firebase not configured
            Log.w(TAG, "Firebase not available, using simulation mode")
            simulateConnection()
        } ?: simulateConnection()
    }

    fun setConnected() {
        _statusLiveData.postValue(Status.CONNECTED)
        sessionId?.let { sid ->
            db?.child(SESSION_KEY)?.child(sid)?.child("status")?.setValue("screen_sharing")
        }
    }

    fun disconnect() {
        _statusLiveData.postValue(Status.DISCONNECTED)
        sessionId?.let { sid ->
            db?.child(SESSION_KEY)?.child(sid)?.child("status")?.setValue("ended")
            db?.child(SESSION_KEY)?.child(sid)?.child("frames")?.removeValue()
        }
        sessionId = null
    }

    fun reset() {
        _statusLiveData.postValue(Status.IDLE)
    }

    fun sendFrame(bitmap: Bitmap) {
        val sid = sessionId ?: return
        try {
            // Compress bitmap to JPEG
            val outputStream = ByteArrayOutputStream()
            // Scale down to reduce data
            val scaled = Bitmap.createScaledBitmap(bitmap, 480, 
                (bitmap.height * 480f / bitmap.width).toInt(), true)
            scaled.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            scaled.recycle()

            // Send to Firebase realtime node
            db?.child(SESSION_KEY)?.child(sid)?.child("latest_frame")?.setValue(
                mapOf("data" to base64, "ts" to System.currentTimeMillis())
            )
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
