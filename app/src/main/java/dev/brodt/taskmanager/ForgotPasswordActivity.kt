package dev.brodt.taskmanager

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dev.brodt.taskmanager.utils.Navigation

class ForgotPasswordActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var emailInput: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        val forgotPasswordBtn = findViewById<Button>(R.id.forgotPasswordBtn)
        val loginLink = findViewById<TextView>(R.id.login)

        forgotPasswordBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (validateEmail(email)) {
                recoveryPassword(email)
            }
        }

        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        when {
            TextUtils.isEmpty(email) -> {
                showError("Email não pode estar vazio")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Email inválido")
                return false
            }
        }
        return true
    }

    private fun recoveryPassword(email: String) {
        showLoading(true)
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    showSuccess("Link de recuperação enviado com sucesso!")
                    goToLogin()
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "Email não cadastrado"
                        else -> "Erro ao enviar link de recuperação: ${task.exception?.message}"
                    }
                    showError(errorMessage)
                }
            }
    }

    private fun goToLogin() {
        Navigation.goToScreen(this, LoginActivity::class.java)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}