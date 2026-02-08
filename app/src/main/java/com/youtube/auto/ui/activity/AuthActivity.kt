package com.youtube.auto.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.youtube.auto.R
import com.youtube.auto.auth.GoogleAuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @Inject lateinit var authManager: GoogleAuthManager

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            authManager.handleSignInResult(account)
            navigateToMain()
        } catch (e: ApiException) {
            showError("Sign-in failed: ${e.statusCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        authManager.checkExistingSignIn()
        if (authManager.isSignedIn()) {
            navigateToMain()
            return
        }

        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            startSignIn()
        }

        findViewById<Button>(R.id.btnSkip).setOnClickListener {
            navigateToMain()
        }
    }

    private fun startSignIn() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnSignIn).isEnabled = false
        signInLauncher.launch(authManager.getSignInIntent())
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
        findViewById<Button>(R.id.btnSignIn).isEnabled = true
        findViewById<TextView>(R.id.tvError).apply {
            text = message
            visibility = View.VISIBLE
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
