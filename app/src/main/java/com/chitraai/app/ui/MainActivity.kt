package com.chitraai.app.ui

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chitraai.app.R
import com.chitraai.app.databinding.ActivityMainBinding
import com.chitraai.app.manager.ConnectionManager
import com.chitraai.app.manager.PermissionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var connectionManager: ConnectionManager
    private lateinit var permManager: PermissionManager
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val screenCapturePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            // Permission granted — launch session
            val intent = Intent(this, SessionActivity::class.java).apply {
                putExtra(SessionActivity.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(SessionActivity.EXTRA_RESULT_DATA, result.data)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        } else {
            setButtonIdle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permManager = PermissionManager(this)
        connectionManager = ConnectionManager(this)
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        setupUI()
        animateEntrance()
        observeConnectionStatus()
    }

    private fun setupUI() {
        binding.btnConnect.setOnClickListener {
            startConnecting()
        }

        binding.btnGallery.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }

        binding.tvStatus.text = "Ready to connect"
        binding.indicatorDot.setBackgroundResource(R.drawable.dot_idle)
    }

    private fun animateEntrance() {
        binding.ivLogo.alpha = 0f
        binding.tvAppName.alpha = 0f
        binding.tvTagline.alpha = 0f
        binding.btnConnect.alpha = 0f
        binding.cardStatus.alpha = 0f

        binding.ivLogo.animate().alpha(1f).setDuration(600).setStartDelay(100).start()
        binding.tvAppName.animate().alpha(1f).setDuration(600).setStartDelay(250).start()
        binding.tvTagline.animate().alpha(1f).setDuration(600).setStartDelay(400).start()
        binding.btnConnect.animate().alpha(1f).translationY(0f).setDuration(700).setStartDelay(550).start()
        binding.cardStatus.animate().alpha(1f).setDuration(600).setStartDelay(700).start()

        binding.btnConnect.translationY = 60f

        // Pulse animation on button
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.btnConnect.postDelayed({
            binding.btnConnect.startAnimation(pulse)
        }, 1300)
    }

    private fun observeConnectionStatus() {
        connectionManager.statusLiveData.observe(this) { status ->
            when (status) {
                ConnectionManager.Status.IDLE -> {
                    binding.tvStatus.text = "Ready to connect"
                    binding.indicatorDot.setBackgroundResource(R.drawable.dot_idle)
                }
                ConnectionManager.Status.CONNECTING -> {
                    binding.tvStatus.text = "Connecting to Krish..."
                    binding.indicatorDot.setBackgroundResource(R.drawable.dot_connecting)
                }
                ConnectionManager.Status.CONNECTED -> {
                    binding.tvStatus.text = "Krish is connected"
                    binding.indicatorDot.setBackgroundResource(R.drawable.dot_connected)
                }
                ConnectionManager.Status.DISCONNECTED -> {
                    binding.tvStatus.text = "Session ended"
                    binding.indicatorDot.setBackgroundResource(R.drawable.dot_idle)
                    setButtonIdle()
                }
            }
        }
    }

    private fun startConnecting() {
        binding.btnConnect.isEnabled = false
        binding.btnConnect.text = "Connecting..."
        connectionManager.initiate()

        // Request MediaProjection permission
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCapturePermissionLauncher.launch(captureIntent)
    }

    private fun setButtonIdle() {
        binding.btnConnect.isEnabled = true
        binding.btnConnect.text = "Connect to Krish"
        connectionManager.reset()
    }

    override fun onResume() {
        super.onResume()
        // Reset button state when returning from session
        binding.btnConnect.isEnabled = true
        binding.btnConnect.text = "Connect to Krish"
    }
}
