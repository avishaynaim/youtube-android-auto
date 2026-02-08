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
import com.google.android.gms.common.api.CommonStatusCodes
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
        val data = result.data
        if (data == null) {
            showError("Sign-in cancelled")
            return@registerForActivityResult
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            authManager.handleSignInResult(account)
            navigateToMain()
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                CommonStatusCodes.CANCELED,
                SIGN_IN_CANCELLED_CODE -> "Sign-in cancelled"
                CommonStatusCodes.NETWORK_ERROR -> "Network error. Please check your connection."
                else -> "Sign-in failed (code: ${e.statusCode})"
            }
            showError(message)
        } catch (e: Exception) {
            showError("Sign-in failed unexpectedly")
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
        findViewById<TextView>(R.id.tvError).visibility = View.GONE
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

    companion object {
        private const val SIGN_IN_CANCELLED_CODE = 12501
    }
}
