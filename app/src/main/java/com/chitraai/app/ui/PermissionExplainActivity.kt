package com.chitraai.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chitraai.app.databinding.ActivityPermissionExplainBinding
import com.chitraai.app.manager.PermissionManager

class PermissionExplainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionExplainBinding
    private lateinit var permManager: PermissionManager

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            proceedToMain()
        } else {
            // Show partial permission warning but still proceed
            proceedToMain()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        requestStoragePermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionExplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permManager = PermissionManager(this)

        setupUI()
        animateIn()
    }

    private fun setupUI() {
        binding.btnGrantPermissions.setOnClickListener {
            permManager.markPermissionExplanationSeen()
            requestPermissions()
        }

        binding.btnSkip.setOnClickListener {
            permManager.markPermissionExplanationSeen()
            proceedToMain()
        }
    }

    private fun animateIn() {
        binding.cardPermissions.alpha = 0f
        binding.cardPermissions.translationY = 80f
        binding.cardPermissions.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(200)
            .start()

        binding.tvTitle.alpha = 0f
        binding.tvTitle.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requestStoragePermissions()
        }
    }

    private fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        storagePermissionLauncher.launch(permissions)
    }

    private fun proceedToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
