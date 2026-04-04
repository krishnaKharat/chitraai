package com.chitraai.app.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.chitraai.app.R
import com.chitraai.app.databinding.ActivitySessionBinding
import com.chitraai.app.manager.ConnectionManager
import com.chitraai.app.service.ScreenShareService

class SessionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
    }

    private lateinit var binding: ActivitySessionBinding
    private lateinit var connectionManager: ConnectionManager
    private var sessionDurationSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var screenShareService: ScreenShareService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ScreenShareService.LocalBinder
            screenShareService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectionManager = ConnectionManager(this)

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
        val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        startScreenShareService(resultCode, resultData)
        setupUI()
        animateConnected()
        startTimer()
    }

    private fun startScreenShareService(resultCode: Int, data: Intent?) {
        val serviceIntent = Intent(this, ScreenShareService::class.java).apply {
            putExtra(EXTRA_RESULT_CODE, resultCode)
            putExtra(EXTRA_RESULT_DATA, data)
        }
        startForegroundService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun setupUI() {
        binding.btnEndSession.setOnClickListener {
            endSession()
        }

        binding.btnShareFile.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        binding.tvSessionStatus.text = "Krish is helping you"
        binding.tvSessionDuration.text = "00:00"
    }

    private fun animateConnected() {
        // Pulse the connected indicator
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.ivConnectedIndicator.startAnimation(pulse)

        // Slide in cards
        binding.cardSession.alpha = 0f
        binding.cardSession.translationY = 50f
        binding.cardSession.animate()
            .alpha(1f).translationY(0f)
            .setDuration(600).setStartDelay(100).start()

        binding.cardActions.alpha = 0f
        binding.cardActions.animate()
            .alpha(1f).setDuration(600).setStartDelay(350).start()

        // Show "Connected!" toast-like animation
        binding.tvConnectedBadge.alpha = 0f
        binding.tvConnectedBadge.scaleX = 0.5f
        binding.tvConnectedBadge.scaleY = 0.5f
        binding.tvConnectedBadge.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(500).start()
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                sessionDurationSeconds++
                val minutes = sessionDurationSeconds / 60
                val seconds = sessionDurationSeconds % 60
                binding.tvSessionDuration.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(timerRunnable!!, 1000)
    }

    private fun endSession() {
        timerRunnable?.let { handler.removeCallbacks(it) }

        // Stop the screen share service
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        stopService(Intent(this, ScreenShareService::class.java))

        connectionManager.disconnect()

        // Animate out and finish
        binding.root.animate()
            .alpha(0f).setDuration(300)
            .withEndAction { finish() }
            .start()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerRunnable?.let { handler.removeCallbacks(it) }
        if (isBound) {
            unbindService(serviceConnection)
        }
    }

    override fun onBackPressed() {
        // Ask before ending session
        endSession()
    }
}
