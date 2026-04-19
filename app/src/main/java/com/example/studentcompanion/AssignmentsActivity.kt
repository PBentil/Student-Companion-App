package com.example.studentcompanion

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.adapter.AssignmentsAdapter
import com.example.studentcompanion.database.StudentDatabase
import com.example.studentcompanion.model.Assignment
import com.example.studentcompanion.model.Priority
import com.example.studentcompanion.model.Status
import com.example.studentcompanion.model.toAssignment
import com.example.studentcompanion.model.toCourse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * CLASS: AssignmentsActivity
 * INHERITANCE: Inherits from AppCompatActivity.
 * Handles the tracking of academic assignments.
 */
class AssignmentsActivity : AppCompatActivity() {

    // UI Components
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AssignmentsAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddAssignment: FloatingActionButton

    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var chipOverdue: Chip

    private lateinit var database: StudentDatabase
    private val assignmentsList = mutableListOf<Assignment>()
    private var currentFilter = "All"
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_assignments)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.assignmentsRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        fabAddAssignment = findViewById(R.id.fabAddAssignment)

        chipAll = findViewById(R.id.chipAll)
        chipPending = findViewById(R.id.chipPending)
        chipCompleted = findViewById(R.id.chipCompleted)
        chipOverdue = findViewById(R.id.chipOverdue)

        database = StudentDatabase.getDatabase(this)

        // LAMBDA: Toolbar navigation listener
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // LAMBDA: Initializing adapter with callback lambdas for click and status change
        adapter = AssignmentsAdapter(
            getFilteredAssignments(),
            onAssignmentClick = { assignment ->
                showAssignmentDialog(assignment)
            },
            onCheckboxChange = { assignment, isChecked ->
                handleCheckboxChange(assignment, isChecked)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // LAMBDA: FAB click listener
        fabAddAssignment.setOnClickListener {
            showAssignmentDialog(null)
        }

        // LAMBDA: Filtering logic triggered by chip state changes
        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "All"
                updateAssignmentsList()
            }
        }

        chipPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "Pending"
                updateAssignmentsList()
            }
        }

        chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "Completed"
                updateAssignmentsList()
            }
        }

        chipOverdue.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "Overdue"
                updateAssignmentsList()
            }
        }

        loadAssignments()
    }

    /**
     * FUNCTION: Asynchronously loads assignments from database.
     */
    private fun loadAssignments() {
        // LAMBDA: Coroutine launch
        lifecycleScope.launch {
            val entities = database.assignmentDao().getAllAssignmentsSync()
            assignmentsList.clear()
            // LAMBDA: .map { ... } for data transformation
            assignmentsList.addAll(entities.map { it.toAssignment() })
            updateAssignmentsList()
            updateUI()
        }
    }

    /**
     * FUNCTION: Shows a dialog to create or update an assignment.
     * NULLABILITY: 'assignment' is nullable; null indicates a new entry.
     */
    private fun showAssignmentDialog(assignment: Assignment?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_assignment, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etAssignmentTitle)
        val etCourse = dialogView.findViewById<AutoCompleteTextView>(R.id.etCourse)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<TextInputEditText>(R.id.etDueDate)
        val priorityGroup = dialogView.findViewById<RadioGroup>(R.id.priorityGroup)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // LAMBDA: Fetching and mapping courses to strings for the dropdown
        lifecycleScope.launch {
            val courseEntities = database.courseDao().getAllCoursesSync()
            val courseCodes = courseEntities.map { it.courseCode }
            val courseAdapter = ArrayAdapter(this@AssignmentsActivity, android.R.layout.simple_dropdown_item_1line, courseCodes)
            etCourse.setAdapter(courseAdapter)
        }

        // LAMBDA: Date picker trigger
        etDueDate.setOnClickListener {
            showDatePicker { date ->
                etDueDate.setText(date)
            }
        }

        // CONDITIONAL LOGIC: Pre-fill fields if editing an existing assignment
        if (assignment != null) {
            dialogTitle.text = "Edit Assignment"
            etTitle.setText(assignment.title)
            etCourse.setText(assignment.courseCode, false)
            etDescription.setText(assignment.description)
            etDueDate.setText(assignment.dueDate)

            // CONDITIONAL LOGIC: Map priority enum to radio buttons
            when (assignment.priority) {
                Priority.LOW -> priorityGroup.check(R.id.radioLow)
                Priority.MEDIUM -> priorityGroup.check(R.id.radioMedium)
                Priority.HIGH -> priorityGroup.check(R.id.radioHigh)
            }

            btnSave.text = "Update Assignment"
        } else {
            // Default date for new assignments
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            etDueDate.setText(dateFormat.format(selectedDate.time))
        }

        // LAMBDA: Dialog dismissal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // LAMBDA: Save/Update logic inside click listener
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val courseCode = etCourse.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val dueDate = etDueDate.text.toString().trim()

            // CONDITIONAL LOGIC: Required field validation
            if (title.isEmpty() || courseCode.isEmpty() || dueDate.isEmpty()) {
                return@setOnClickListener
            }

            // CONDITIONAL LOGIC: Determine priority from radio selection
            val priority = when (priorityGroup.checkedRadioButtonId) {
                R.id.radioLow -> Priority.LOW
                R.id.radioHigh -> Priority.HIGH
                else -> Priority.MEDIUM
            }

            // CONDITIONAL LOGIC: Branch for Insert or Update
            if (assignment == null) {
                val newAssignment = Assignment(
                    id = 0,
                    title = title,
                    courseCode = courseCode,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = Status.PENDING,
                    isCompleted = false
                )

                lifecycleScope.launch {
                    database.assignmentDao().insert(newAssignment.toEntity())
                    loadAssignments()
                }
            } else {
                val updatedAssignment = assignment.copy(
                    title = title,
                    courseCode = courseCode,
                    description = description,
                    dueDate = dueDate,
                    priority = priority
                )

                lifecycleScope.launch {
                    database.assignmentDao().update(updatedAssignment.toEntity())
                    loadAssignments()
                }
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * FUNCTION: showDatePicker
     * USES LAMBDA: Accepts a lambda 'onDateSelected' to pass the result back to the caller
     */
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        // LAMBDA: DatePickerDialog listener
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                onDateSelected(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    /**
     * FUNCTION: handleCheckboxChange
     */
    private fun handleCheckboxChange(assignment: Assignment, isChecked: Boolean) {
        val updatedAssignment = assignment.copy(
            isCompleted = isChecked,
            status = if (isChecked) Status.COMPLETED else Status.PENDING
        )

        lifecycleScope.launch {
            database.assignmentDao().update(updatedAssignment.toEntity())
            loadAssignments()
        }
    }

    /**
     * FUNCTION: getFilteredAssignments
     * USES LAMBDA: .filter { ... }
     */
    private fun getFilteredAssignments(): List<Assignment> {
        return when (currentFilter) {
            "Pending" -> assignmentsList.filter { it.status == Status.PENDING && !it.isCompleted }
            "Completed" -> assignmentsList.filter { it.isCompleted }
            "Overdue" -> assignmentsList.filter { it.status == Status.OVERDUE }
            else -> assignmentsList
        }
    }

    private fun updateAssignmentsList() {
        adapter.updateAssignments(getFilteredAssignments())
    }

    private fun updateUI() {
        val filteredList = getFilteredAssignments()
        if (filteredList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}