package dev.brodt.taskmanager

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dev.brodt.taskmanager.utils.AuthUtils
import dev.brodt.taskmanager.utils.Navigation

class ProfileActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailText: TextView
    private lateinit var progressBar: ProgressBar
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar views
        nameInput = findViewById(R.id.name_input)
        emailText = findViewById(R.id.email_text)
        progressBar = findViewById(R.id.progressBar)
        
        val homeBtn = findViewById<ImageView>(R.id.home)
        val logoutBtn = findViewById<ImageView>(R.id.logout)
        val saveBtn = findViewById<Button>(R.id.save_btn)

        // Carregar dados do usuário
        loadUserProfile()

        saveBtn.setOnClickListener {
            if (validateInputs()) {
                saveProfile()
            }
        }

        homeBtn.setOnClickListener {
            finish()
        }

        logoutBtn.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)
        currentUser?.let { user ->
            emailText.text = user.email
            
            // Carregar dados adicionais do usuário do Realtime Database
            dbRef.child("users").child(user.uid).child("profile").get()
                .addOnSuccessListener { snapshot ->
                    showLoading(false)
                    if (snapshot.exists()) {
                        nameInput.setText(snapshot.child("name").value as? String ?: "")
                    }
                }
                .addOnFailureListener {
                    showLoading(false)
                    showError("Erro ao carregar perfil")
                }
        }
    }

    private fun saveProfile() {
        showLoading(true)
        currentUser?.let { user ->
            val userData = hashMapOf(
                "name" to nameInput.text.toString().trim()
            )

            dbRef.child("users").child(user.uid).child("profile")
                .updateChildren(userData as Map<String, Any>)
                .addOnSuccessListener {
                    showLoading(false)
                    showSuccess("Perfil atualizado com sucesso")
                }
                .addOnFailureListener {
                    showLoading(false)
                    showError("Erro ao atualizar perfil")
                }
        }
    }

    private fun validateInputs(): Boolean {
        val name = nameInput.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            showError("Nome não pode estar vazio")
            return false
        }
        return true
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _, _ ->
                AuthUtils.logout(this)
            }
            .setNegativeButton("Não", null)
            .show()
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