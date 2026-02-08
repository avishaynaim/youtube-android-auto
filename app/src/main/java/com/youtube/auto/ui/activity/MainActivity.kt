package com.youtube.auto.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.youtube.auto.R
import com.youtube.auto.auth.AuthState
import com.youtube.auto.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.loadTrending()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            // Re-authentication required (e.g. token expired and unrecoverable)
                            navigateToAuth()
                        }
                        else -> {
                            // Unauthenticated is allowed (guest mode via "Skip" button)
                            // Unknown is transient initial state
                            // Authenticated is normal signed-in state
                        }
                    }
                }
            }
        }
    }

    fun signOutAndNavigateToAuth() {
        viewModel.signOut()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
