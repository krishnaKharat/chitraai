package com.chitraai.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.chitraai.app.R
import com.chitraai.app.databinding.ActivitySplashBinding
import com.chitraai.app.manager.PermissionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        binding.ivLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(slideUp)
        binding.tvTagline.startAnimation(slideUp)

        // Animate progress bar
        binding.progressBar.alpha = 0f
        binding.progressBar.animate()
            .alpha(1f)
            .setStartDelay(800)
            .setDuration(400)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2500)
    }

    private fun navigateNext() {
        val permManager = PermissionManager(this)
        val intent = if (!permManager.hasSeenPermissionExplanation()) {
            Intent(this, PermissionExplainActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
