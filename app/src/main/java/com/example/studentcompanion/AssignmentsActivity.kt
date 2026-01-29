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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.adapter.AssignmentsAdapter
import com.example.studentcompanion.model.Assignment
import com.example.studentcompanion.model.Priority
import com.example.studentcompanion.model.Status
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AssignmentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AssignmentsAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddAssignment: FloatingActionButton

    // Filter chips
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var chipOverdue: Chip

    // Temporary in-memory storage
    private val assignmentsList = mutableListOf<Assignment>()
    private var currentFilter = "All"
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        // Initialize views
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.assignmentsRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        fabAddAssignment = findViewById(R.id.fabAddAssignment)

        chipAll = findViewById(R.id.chipAll)
        chipPending = findViewById(R.id.chipPending)
        chipCompleted = findViewById(R.id.chipCompleted)
        chipOverdue = findViewById(R.id.chipOverdue)

        // Setup toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup RecyclerView
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

        // Setup FAB
        fabAddAssignment.setOnClickListener {
            showAssignmentDialog(null)
        }

        // Setup filter chips
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

        // Update UI
        updateUI()
    }

    private fun showAssignmentDialog(assignment: Assignment?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_assignment, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Get views from dialog
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etAssignmentTitle)
        val etCourse = dialogView.findViewById<AutoCompleteTextView>(R.id.etCourse)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val etDueDate = dialogView.findViewById<TextInputEditText>(R.id.etDueDate)
        val priorityGroup = dialogView.findViewById<RadioGroup>(R.id.priorityGroup)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // Setup course dropdown with sample courses
        val courses = arrayOf("CS 101", "MATH 201", "ENG 105", "PHYS 201", "HIST 101")
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courses)
        etCourse.setAdapter(courseAdapter)

        // Setup date picker
        etDueDate.setOnClickListener {
            showDatePicker { date ->
                etDueDate.setText(date)
            }
        }

        // If editing, populate fields
        if (assignment != null) {
            dialogTitle.text = "Edit Assignment"
            etTitle.setText(assignment.title)
            etCourse.setText(assignment.courseCode, false)
            etDescription.setText(assignment.description)
            etDueDate.setText(assignment.dueDate)

            when (assignment.priority) {
                Priority.LOW -> priorityGroup.check(R.id.radioLow)
                Priority.MEDIUM -> priorityGroup.check(R.id.radioMedium)
                Priority.HIGH -> priorityGroup.check(R.id.radioHigh)
            }

            btnSave.text = "Update Assignment"
        } else {
            // Set default due date to today
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            etDueDate.setText(dateFormat.format(selectedDate.time))
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val courseCode = etCourse.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val dueDate = etDueDate.text.toString().trim()

            // Validate inputs
            if (title.isEmpty() || courseCode.isEmpty() || dueDate.isEmpty()) {
                // Show error
                return@setOnClickListener
            }

            val priority = when (priorityGroup.checkedRadioButtonId) {
                R.id.radioLow -> Priority.LOW
                R.id.radioHigh -> Priority.HIGH
                else -> Priority.MEDIUM
            }

            if (assignment == null) {
                // Add new assignment
                val newAssignment = Assignment(
                    id = System.currentTimeMillis(),
                    title = title,
                    courseCode = courseCode,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = Status.PENDING,
                    isCompleted = false
                )
                assignmentsList.add(newAssignment)
            } else {
                // Update existing assignment
                val index = assignmentsList.indexOfFirst { it.id == assignment.id }
                if (index != -1) {
                    assignmentsList[index] = assignment.copy(
                        title = title,
                        courseCode = courseCode,
                        description = description,
                        dueDate = dueDate,
                        priority = priority
                    )
                }
            }

            updateAssignmentsList()
            updateUI()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()

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

    private fun handleCheckboxChange(assignment: Assignment, isChecked: Boolean) {
        val index = assignmentsList.indexOfFirst { it.id == assignment.id }
        if (index != -1) {
            assignmentsList[index] = assignment.copy(
                isCompleted = isChecked,
                status = if (isChecked) Status.COMPLETED else Status.PENDING
            )
            updateAssignmentsList()
        }
    }

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