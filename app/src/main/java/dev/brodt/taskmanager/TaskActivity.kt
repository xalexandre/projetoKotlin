package dev.brodt.taskmanager

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.brodt.taskmanager.models.Task
import java.util.*

class TaskActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var progressBar: ProgressBar
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        taskId = intent.getStringExtra(EXTRA_TASK_ID)
        initializeViews()
        setupListeners()
        
        if (taskId != null) {
            loadTask()
        }
    }

    private fun initializeViews() {
        titleInput = findViewById(R.id.title_input)
        descriptionInput = findViewById(R.id.description_input)
        dateInput = findViewById(R.id.date_input)
        timeInput = findViewById(R.id.time_input)
        progressBar = findViewById(R.id.progressBar)

        findViewById<View>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        dateInput.setOnClickListener { showDatePicker() }
        timeInput.setOnClickListener { showTimePicker() }
        
        findViewById<Button>(R.id.save_btn).setOnClickListener {
            if (validateInputs()) {
                saveTask()
            }
        }
    }

    private fun loadTask() {
        showLoading(true)
        taskId?.let { id ->
            firebaseDb.collection("tasks").document(id)
                .get()
                .addOnSuccessListener { document ->
                    document.toObject(Task::class.java)?.let { task ->
                        titleInput.setText(task.title)
                        descriptionInput.setText(task.description)
                        dateInput.setText(task.date)
                        timeInput.setText(task.time)
                    }
                    showLoading(false)
                }
                .addOnFailureListener {
                    showError("Erro ao carregar tarefa")
                    showLoading(false)
                }
        }
    }

    private fun saveTask() {
        showLoading(true)
        val userId = firebaseAuth.currentUser?.uid ?: return

        val task = Task(
            id = taskId ?: UUID.randomUUID().toString(),
            title = titleInput.text.toString(),
            description = descriptionInput.text.toString(),
            date = dateInput.text.toString(),
            time = timeInput.text.toString(),
            userId = userId
        )

        firebaseDb.collection("tasks").document(task.id)
            .set(task)
            .addOnSuccessListener {
                showSuccess("Tarefa salva com sucesso")
                finish()
            }
            .addOnFailureListener {
                showError("Erro ao salvar tarefa")
                showLoading(false)
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                dateInput.setText(String.format("%02d/%02d/%d", day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                timeInput.setText(String.format("%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateInputs(): Boolean {
        when {
            titleInput.text.isNullOrEmpty() -> {
                showError(getString(R.string.error_empty_title))
                return false
            }
            descriptionInput.text.isNullOrEmpty() -> {
                showError(getString(R.string.error_empty_description))
                return false
            }
            dateInput.text.isNullOrEmpty() -> {
                showError(getString(R.string.error_empty_date))
                return false
            }
            timeInput.text.isNullOrEmpty() -> {
                showError(getString(R.string.error_empty_time))
                return false
            }
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

    companion object {
        private const val EXTRA_TASK_ID = "extra_task_id"

        fun createIntent(context: Context, taskId: String? = null): Intent {
            return Intent(context, TaskActivity::class.java).apply {
                taskId?.let { putExtra(EXTRA_TASK_ID, it) }
            }
        }
    }
}