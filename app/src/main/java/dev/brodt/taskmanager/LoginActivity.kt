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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dev.brodt.taskmanager.utils.Navigation

class LoginActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var emailInput: TextView
    private lateinit var passwordInput: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupListeners()
        checkCurrentUser()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        val btnLogin = findViewById<Button>(R.id.submitLoginBtn)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val registerLink = findViewById<TextView>(R.id.create_account)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                login(email, password)
            }
        }

        forgotPassword.setOnClickListener {
            Navigation.goToScreen(this, ForgotPasswordActivity::class.java)
        }

        registerLink.setOnClickListener {
            Navigation.goToScreen(this, RegisterActivity::class.java)
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
                showError("Senha deve ter pelo menos 6 caracteres")
                return false
            }
        }
        return true
    }

    private fun login(email: String, password: String) {
        showLoading(true)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        showSuccess("Seja bem-vindo ${user.email}")
                        goToMainActivity()
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "Usuário não encontrado"
                        is FirebaseAuthInvalidCredentialsException -> "Email ou senha inválidos"
                        else -> "Erro ao realizar login: ${task.exception?.message}"
                    }
                    showError(errorMessage)
                }
            }
    }

    private fun checkCurrentUser() {
        if (firebaseAuth.currentUser != null) {
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        Navigation.goToScreen(this, MainActivity::class.java)
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