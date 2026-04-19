package com.example.studentcompanion

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.adapter.CoursesAdapter
import com.example.studentcompanion.database.StudentDatabase
import com.example.studentcompanion.model.Course
import com.example.studentcompanion.model.toCourse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * CLASS: CoursesActivity
 * INHERITANCE: Inherits from AppCompatActivity.
 * Manages the list of academic courses.
 */
class CoursesActivity : AppCompatActivity() {

    // UI and Database references
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CoursesAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddCourse: FloatingActionButton

    private lateinit var database: StudentDatabase
    private val coursesList = mutableListOf<Course>()
    private var selectedColor = "#6366F1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_courses)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.coursesRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        fabAddCourse = findViewById(R.id.fabAddCourse)

        database = StudentDatabase.getDatabase(this)

        // LAMBDA: Navigation click listener
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // LAMBDA: Initializing adapter with a click listener lambda for editing
        adapter = CoursesAdapter(coursesList) { course ->
            showCourseDialog(course)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // LAMBDA: Click listener for FAB
        fabAddCourse.setOnClickListener {
            showCourseDialog(null)
        }

        loadCourses()
    }

    /**
     * FUNCTION: Fetches courses from the database.
     */
    private fun loadCourses() {
        // LAMBDA: Coroutine launch
        lifecycleScope.launch {
            val entities = database.courseDao().getAllCoursesSync()
            coursesList.clear()
            // LAMBDA: .map { ... } transformation
            coursesList.addAll(entities.map { it.toCourse() })
            adapter.updateCourses(coursesList)
            updateUI()
        }
    }

    /**
     * FUNCTION: Shows a dialog to add or edit a course.
     * NULLABILITY: 'course' is nullable; if null, the dialog is for a new course.
     */
    private fun showCourseDialog(course: Course?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_course, null)
        
        // NULLABILITY/LOGIC: Use elvis operator to provide a default color
        selectedColor = course?.color ?: "#6366F1"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etCourseCode = dialogView.findViewById<TextInputEditText>(R.id.etCourseCode)
        val etCourseName = dialogView.findViewById<TextInputEditText>(R.id.etCourseName)
        val etInstructor = dialogView.findViewById<TextInputEditText>(R.id.etInstructor)
        val etRoom = dialogView.findViewById<TextInputEditText>(R.id.etRoom)
        val etSchedule = dialogView.findViewById<TextInputEditText>(R.id.etSchedule)
        val etCredits = dialogView.findViewById<TextInputEditText>(R.id.etCredits)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val colorBlue = dialogView.findViewById<View>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<View>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<View>(R.id.colorYellow)
        val colorRed = dialogView.findViewById<View>(R.id.colorRed)
        val colorPurple = dialogView.findViewById<View>(R.id.colorPurple)

        /**
         * FUNCTION: selectColor handles the logic for choosing a theme color for a course.
         */
        fun selectColor(color: String, view: View) {
            selectedColor = color
            // LAMBDA: .forEach { ... } to iterate and reset views
            listOf(colorBlue, colorGreen, colorYellow, colorRed, colorPurple).forEach { 
                it.scaleX = 1f
                it.scaleY = 1f
            }
            // Highlight selected view
            view.scaleX = 1.2f
            view.scaleY = 1.2f
        }

        // LAMBDA: Click listeners for color picking
        colorBlue.setOnClickListener { selectColor("#6366F1", it) }
        colorGreen.setOnClickListener { selectColor("#10B981", it) }
        colorYellow.setOnClickListener { selectColor("#F59E0B", it) }
        colorRed.setOnClickListener { selectColor("#EF4444", it) }
        colorPurple.setOnClickListener { selectColor("#A855F7", it) }

        // CONDITIONAL LOGIC: Populate fields if editing an existing course
        if (course != null) {
            dialogTitle.text = "Edit Course"
            etCourseCode.setText(course.courseCode)
            etCourseName.setText(course.courseName)
            etInstructor.setText(course.instructor)
            etRoom.setText(course.room)
            etSchedule.setText(course.schedule)
            etCredits.setText(course.credits.toString())
            btnSave.text = "Update Course"
        }

        // LAMBDA: Button click listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val courseCode = etCourseCode.text.toString().trim()
            val courseName = etCourseName.text.toString().trim()
            val instructor = etInstructor.text.toString().trim()
            val room = etRoom.text.toString().trim()
            val schedule = etSchedule.text.toString().trim()
            val creditsStr = etCredits.text.toString().trim()

            // CONDITIONAL LOGIC: Basic validation
            if (courseCode.isEmpty() || courseName.isEmpty()) {
                return@setOnClickListener
            }

            // NULLABILITY: safe conversion using toIntOrNull()
            val credits = creditsStr.toIntOrNull() ?: 0

            // CONDITIONAL LOGIC: Insert or Update based on 'course' nullability
            if (course == null) {
                val newCourse = Course(
                    id = 0,
                    courseCode = courseCode,
                    courseName = courseName,
                    instructor = instructor,
                    room = room,
                    schedule = schedule,
                    credits = credits,
                    color = selectedColor
                )

                lifecycleScope.launch {
                    database.courseDao().insert(newCourse.toEntity())
                    loadCourses()
                }
            } else {
                val updatedCourse = course.copy(
                    courseCode = courseCode,
                    courseName = courseName,
                    instructor = instructor,
                    room = room,
                    schedule = schedule,
                    credits = credits,
                    color = selectedColor
                )

                lifecycleScope.launch {
                    database.courseDao().update(updatedCourse.toEntity())
                    loadCourses()
                }
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUI() {
        if (coursesList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}