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

/**
 * CLASS: TasksActivity
 * INHERITANCE: Inherits from AppCompatActivity.
 * Manages the To-Do list functionality.
 */
class TasksActivity : AppCompatActivity() {

    // UI and Database references
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TasksAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var progressBar: ProgressBar

    // NULLABILITY: Filter chips are initialized as lateinit, ensured non-null before use in onCreate
    private lateinit var chipAll: Chip
    private lateinit var chipActive: Chip
    private lateinit var chipCompleted: Chip

    private lateinit var database: StudentDatabase
    private val tasksList = mutableListOf<Task>()
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        database = StudentDatabase.getDatabase(this)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // LAMBDA: Initializing the adapter with three different lambda expressions for callbacks
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

        // LAMBDA: click listener using a lambda
        fabAddTask.setOnClickListener {
            showTaskDialog(null)
        }

        // LAMBDA: Checkbox change listeners using lambdas
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

        loadTasks()
    }

    /**
     * FUNCTION: Loads tasks from Room database asynchronously.
     */
    private fun loadTasks() {
        // LAMBDA: Coroutine launch block
        lifecycleScope.launch {
            val entities = database.taskDao().getAllTasksSync()
            tasksList.clear()
            // LAMBDA: Using .map with a transformation lambda
            tasksList.addAll(entities.map { it.toTask() })
            updateTasksList()
            updateProgress()
            updateUI()
        }
    }

    /**
     * FUNCTION: Displays a dialog to either Add or Edit a task.
     * NULLABILITY: 'task' parameter is nullable. If null, we're adding; if not, we're editing.
     */
    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etTaskTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etTaskDescription)
        val categoryGroup = dialogView.findViewById<ChipGroup>(R.id.categoryChipGroup)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // CONDITIONAL LOGIC: Populate fields if editing an existing task
        if (task != null) {
            dialogTitle.text = "Edit Task"
            etTitle.setText(task.title)
            etDescription.setText(task.description)

            // CONDITIONAL LOGIC: Check appropriate category chip
            when (task.category) {
                "Study" -> categoryGroup.check(R.id.chipStudy)
                "Personal" -> categoryGroup.check(R.id.chipPersonal)
                "Work" -> categoryGroup.check(R.id.chipWork)
                "Other" -> categoryGroup.check(R.id.chipOther)
            }

            btnSave.text = "Update Task"
        }

        // LAMBDA: Click listener for cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // LAMBDA: Click listener for save button
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            // CONDITIONAL LOGIC: Basic validation for required fields
            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            // CONDITIONAL LOGIC: Resolve category from selected chip
            val category = when (categoryGroup.checkedChipId) {
                R.id.chipStudy -> "Study"
                R.id.chipPersonal -> "Personal"
                R.id.chipWork -> "Work"
                R.id.chipOther -> "Other"
                else -> ""
            }

            // CONDITIONAL LOGIC: Create new or update existing based on nullability of 'task'
            if (task == null) {
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

    /**
     * FUNCTION: Updates task completion status in database.
     */
    private fun handleCheckboxChange(task: Task, isChecked: Boolean) {
        val updatedTask = task.copy(isCompleted = isChecked)

        lifecycleScope.launch {
            database.taskDao().update(updatedTask.toEntity())
            loadTasks()
        }
    }

    /**
     * FUNCTION: Shows confirmation dialog before deletion.
     */
    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete \"${task.title}\"?")
            // LAMBDA: Dialog button listener using a lambda
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.taskDao().delete(task.toEntity())
                    loadTasks()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * FUNCTION: Filters the list of tasks based on the current filter selection.
     * CONDITIONAL LOGIC: Uses 'when' and 'filter' (loop-based filtering).
     */
    private fun getFilteredTasks(): List<Task> {
        // LAMBDA: .filter { ... } uses a predicate lambda to determine which items to keep
        return when (currentFilter) {
            "Active" -> tasksList.filter { !it.isCompleted }
            "Completed" -> tasksList.filter { it.isCompleted }
            else -> tasksList
        }
    }

    private fun updateTasksList() {
        adapter.updateTasks(getFilteredTasks())
    }

    /**
     * FUNCTION: Calculates and updates the overall progress bar.
     */
    private fun updateProgress() {
        // LAMBDA: .count { ... } uses a lambda for counting based on a condition
        val completed = tasksList.count { it.isCompleted }
        val total = tasksList.size

        tvCompletedCount.text = completed.toString()
        tvTotalCount.text = "of $total"

        // CONDITIONAL LOGIC: Avoid division by zero
        val progress = if (total > 0) (completed * 100) / total else 0
        progressBar.progress = progress
    }

    /**
     * FUNCTION: Controls visibility of empty state vs. recycler view.
     */
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