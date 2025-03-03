package dev.brodt.taskmanager

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dev.brodt.taskmanager.models.Task
import dev.brodt.taskmanager.adapters.TaskAdapter
import dev.brodt.taskmanager.utils.AuthUtils
import dev.brodt.taskmanager.utils.Navigation
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser
    private val dbRef = FirebaseDatabase.getInstance().reference
    
    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    private val firebaseDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!verifySession()) {
            return
        }

        initializeViews()
        setupListeners()
        setupAdapter()
        loadTasks()
    }

    private fun initializeViews() {
        listView = findViewById(R.id.tasks)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)
        
        taskAdapter = TaskAdapter(this, tasks)
        listView.adapter = taskAdapter
        listView.emptyView = emptyView

        // Setup toolbar buttons
        findViewById<ImageView>(R.id.logout).setOnClickListener {
            logout()
        }
        findViewById<ImageView>(R.id.profile).setOnClickListener {
            Navigation.goToScreen(this, ProfileActivity::class.java)
        }
    }

    private fun setupListeners() {
        val fabAddTask = findViewById<FloatingActionButton>(R.id.fab_add_task)
        val logoutBtn = findViewById<ImageView>(R.id.logout)
        val profileBtn = findViewById<ImageView>(R.id.profile)

        fabAddTask.setOnClickListener {
            Navigation.goToScreen(this, TaskActivity::class.java)
        }

        logoutBtn.setOnClickListener {
            showLogoutConfirmation()
        }

        profileBtn.setOnClickListener {
            Navigation.goToScreen(this, ProfileActivity::class.java)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val task = tasks[position]
            editTask(task)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteConfirmation(tasks[position])
            true
        }
    }

    private fun setupAdapter() {
        taskAdapter = TaskAdapter(this, tasks)
        listView.adapter = taskAdapter
    }

    private fun loadTasks() {
        showLoading(true)
        val userId = firebaseAuth.currentUser?.uid
        
        userId?.let { uid ->
            firebaseDb.collection("tasks")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    tasks.clear()
                    for (document in documents) {
                        val task = document.toObject(Task::class.java)
                        tasks.add(task)
                    }
                    taskAdapter.notifyDataSetChanged()
                    showLoading(false)
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    // Você pode adicionar um tratamento de erro aqui
                }
        }
    }

    private fun editTask(task: Task) {
        val intent = TaskActivity.createIntent(this, task.id)
        startActivity(intent)
    }

    private fun deleteTask(task: Task) {
        showLoading(true)
        currentUser?.let { user ->
            dbRef.child("users").child(user.uid).child("tasks").child(task.id)
                .removeValue()
                .addOnSuccessListener {
                    showSuccess("Tarefa deletada com sucesso")
                    showLoading(false)
                }
                .addOnFailureListener {
                    showError("Erro ao deletar tarefa")
                    showLoading(false)
                }
        }
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Deletar Tarefa")
            .setMessage("Tem certeza que deseja deletar esta tarefa?")
            .setPositiveButton("Sim") { _, _ -> deleteTask(task) }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Tem certeza que deseja sair?")
            .setPositiveButton("Sim") { _, _ -> logout() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun verifySession(): Boolean {
        if (currentUser == null) {
            Navigation.goToScreen(this, LoginActivity::class.java)
            finish()
            return false
        }
        return true
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

    private fun logout() {
        firebaseAuth.signOut()
        Navigation.goToScreen(this, LoginActivity::class.java)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadTasks() // Recarrega as tasks quando voltar para a tela
    }
}