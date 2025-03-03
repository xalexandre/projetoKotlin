package dev.brodt.taskmanager

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dev.brodt.taskmanager.utils.Navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class RegisterActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.submitRegisterBtn)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val loginLink = findViewById<TextView>(R.id.login)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            val email = findViewById<TextView>(R.id.emailInput).text.toString().trim()
            val password = findViewById<TextView>(R.id.passwordInput).text.toString().trim()

            if (validateInputs(email, password)) {
                showLoading(true)
                register(email, password)
            }
        }

        forgotPassword.setOnClickListener {
            Navigation.goToScreen(this, ForgotPasswordActivity::class.java)
        }

        loginLink.setOnClickListener {
            Navigation.goToScreen(this, LoginActivity::class.java)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        when {
            TextUtils.isEmpty(email) -> {
                showError("Email não pode estar vazio")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Email inválido")
                return false
            }
            TextUtils.isEmpty(password) -> {
                showError("Senha não pode estar vazia")
                return false
            }
            password.length < 6 -> {
                showError("A senha deve ter pelo menos 6 caracteres")
                return false
            }
        }
        return true
    }

    private fun register(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    showSuccess("Usuário cadastrado com sucesso!")
                    Navigation.goToScreen(this, MainActivity::class.java)
                    finish()
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Senha muito fraca"
                        is FirebaseAuthInvalidCredentialsException -> "Email inválido"
                        is FirebaseAuthUserCollisionException -> "Este email já está em uso"
                        else -> "Erro ao registrar usuário: ${task.exception?.message}"
                    }
                    showError(errorMessage)
                }
            }
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