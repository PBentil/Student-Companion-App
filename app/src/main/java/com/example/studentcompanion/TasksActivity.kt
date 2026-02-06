package com.example.studentcompanion

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.adapter.TasksAdapter
import com.example.studentcompanion.database.StudentDatabase
import com.example.studentcompanion.model.Task
import com.example.studentcompanion.model.toTask
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class TasksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TasksAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var progressBar: ProgressBar

    // Filter chips
    private lateinit var chipAll: Chip
    private lateinit var chipActive: Chip
    private lateinit var chipCompleted: Chip

    // Database
    private lateinit var database: StudentDatabase
    private val tasksList = mutableListOf<Task>()
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle notch and status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_tasks)

        // Initialize views
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.tasksRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        fabAddTask = findViewById(R.id.fabAddTask)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvTotalCount = findViewById(R.id.tvTotalCount)
        progressBar = findViewById(R.id.progressBar)

        chipAll = findViewById(R.id.chipAll)
        chipActive = findViewById(R.id.chipActive)
        chipCompleted = findViewById(R.id.chipCompleted)

        // Initialize database
        database = StudentDatabase.getDatabase(this)

        // Setup toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = TasksAdapter(
            getFilteredTasks(),
            onTaskClick = { task ->
                showTaskDialog(task)
            },
            onCheckboxChange = { task, isChecked ->
                handleCheckboxChange(task, isChecked)
            },
            onDeleteClick = { task ->
                showDeleteConfirmation(task)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup FAB
        fabAddTask.setOnClickListener {
            showTaskDialog(null)
        }

        // Setup filter chips
        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "All"
                updateTasksList()
            }
        }

        chipActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "Active"
                updateTasksList()
            }
        }

        chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "Completed"
                updateTasksList()
            }
        }

        // Load tasks from database
        loadTasks()
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val entities = database.taskDao().getAllTasksSync()
            tasksList.clear()
            tasksList.addAll(entities.map { it.toTask() })
            updateTasksList()
            updateProgress()
            updateUI()
        }
    }

    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Get views from dialog
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etTaskTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etTaskDescription)
        val categoryGroup = dialogView.findViewById<ChipGroup>(R.id.categoryChipGroup)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // If editing, populate fields
        if (task != null) {
            dialogTitle.text = "Edit Task"
            etTitle.setText(task.title)
            etDescription.setText(task.description)

            // Select category chip
            when (task.category) {
                "Study" -> categoryGroup.check(R.id.chipStudy)
                "Personal" -> categoryGroup.check(R.id.chipPersonal)
                "Work" -> categoryGroup.check(R.id.chipWork)
                "Other" -> categoryGroup.check(R.id.chipOther)
            }

            btnSave.text = "Update Task"
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            // Validate
            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            // Get selected category
            val category = when (categoryGroup.checkedChipId) {
                R.id.chipStudy -> "Study"
                R.id.chipPersonal -> "Personal"
                R.id.chipWork -> "Work"
                R.id.chipOther -> "Other"
                else -> ""
            }

            if (task == null) {
                // Add new task
                val newTask = Task(
                    id = 0,
                    title = title,
                    description = description,
                    category = category,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                )

                lifecycleScope.launch {
                    database.taskDao().insert(newTask.toEntity())
                    loadTasks()
                }
            } else {
                // Update existing task
                val updatedTask = task.copy(
                    title = title,
                    description = description,
                    category = category
                )

                lifecycleScope.launch {
                    database.taskDao().update(updatedTask.toEntity())
                    loadTasks()
                }
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleCheckboxChange(task: Task, isChecked: Boolean) {
        val updatedTask = task.copy(isCompleted = isChecked)

        lifecycleScope.launch {
            database.taskDao().update(updatedTask.toEntity())
            loadTasks()
        }
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete \"${task.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.taskDao().delete(task.toEntity())
                    loadTasks()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getFilteredTasks(): List<Task> {
        return when (currentFilter) {
            "Active" -> tasksList.filter { !it.isCompleted }
            "Completed" -> tasksList.filter { it.isCompleted }
            else -> tasksList
        }
    }

    private fun updateTasksList() {
        adapter.updateTasks(getFilteredTasks())
    }

    private fun updateProgress() {
        val completed = tasksList.count { it.isCompleted }
        val total = tasksList.size

        tvCompletedCount.text = completed.toString()
        tvTotalCount.text = "of $total"

        val progress = if (total > 0) (completed * 100) / total else 0
        progressBar.progress = progress
    }

    private fun updateUI() {
        val filteredList = getFilteredTasks()
        if (filteredList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}