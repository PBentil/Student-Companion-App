package com.example.studentcompanion

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.adapter.CoursesAdapter
import com.example.studentcompanion.model.Course
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class CoursesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CoursesAdapter
    private lateinit var emptyState: LinearLayout
    private lateinit var fabAddCourse: FloatingActionButton

    private val coursesList = mutableListOf<Course>()
    private var selectedColor = "#6366F1" // Default color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        // Find root layout and apply window insets
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
        mainLayout.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                view.paddingLeft,
                insets.systemWindowInsetTop,    // Status bar height
                view.paddingRight,
                insets.systemWindowInsetBottom  // Navigation bar height
            )
            insets.consumeSystemWindowInsets()
        }

        // Initialize views
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = findViewById(R.id.coursesRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        fabAddCourse = findViewById(R.id.fabAddCourse)

        // Toolbar back button
        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = CoursesAdapter(coursesList) { course ->
            showCourseDialog(course)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // FAB to add course
        fabAddCourse.setOnClickListener { showCourseDialog(null) }

        // Add sample courses
        addSampleCourses()

        // Update UI
        updateUI()
    }

    private fun showCourseDialog(course: Course?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_course, null)
        selectedColor = course?.color ?: "#6366F1"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Dialog views
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etCourseCode = dialogView.findViewById<TextInputEditText>(R.id.etCourseCode)
        val etCourseName = dialogView.findViewById<TextInputEditText>(R.id.etCourseName)
        val etInstructor = dialogView.findViewById<TextInputEditText>(R.id.etInstructor)
        val etRoom = dialogView.findViewById<TextInputEditText>(R.id.etRoom)
        val etSchedule = dialogView.findViewById<TextInputEditText>(R.id.etSchedule)
        val etCredits = dialogView.findViewById<TextInputEditText>(R.id.etCredits)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // Color pickers
        val colorBlue = dialogView.findViewById<View>(R.id.colorBlue)
        val colorGreen = dialogView.findViewById<View>(R.id.colorGreen)
        val colorYellow = dialogView.findViewById<View>(R.id.colorYellow)
        val colorRed = dialogView.findViewById<View>(R.id.colorRed)
        val colorPurple = dialogView.findViewById<View>(R.id.colorPurple)

        fun selectColor(color: String, view: View) {
            selectedColor = color
            listOf(colorBlue, colorGreen, colorYellow, colorRed, colorPurple).forEach {
                it.scaleX = 1f
                it.scaleY = 1f
            }
            view.scaleX = 1.2f
            view.scaleY = 1.2f
        }

        colorBlue.setOnClickListener { selectColor("#6366F1", it) }
        colorGreen.setOnClickListener { selectColor("#10B981", it) }
        colorYellow.setOnClickListener { selectColor("#F59E0B", it) }
        colorRed.setOnClickListener { selectColor("#EF4444", it) }
        colorPurple.setOnClickListener { selectColor("#A855F7", it) }

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

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val courseCode = etCourseCode.text.toString().trim()
            val courseName = etCourseName.text.toString().trim()
            val instructor = etInstructor.text.toString().trim()
            val room = etRoom.text.toString().trim()
            val schedule = etSchedule.text.toString().trim()
            val creditsStr = etCredits.text.toString().trim()

            if (courseCode.isEmpty() || courseName.isEmpty()) return@setOnClickListener

            val credits = creditsStr.toIntOrNull() ?: 0

            if (course == null) {
                coursesList.add(
                    Course(
                        id = System.currentTimeMillis(),
                        courseCode = courseCode,
                        courseName = courseName,
                        instructor = instructor,
                        room = room,
                        schedule = schedule,
                        credits = credits,
                        color = selectedColor
                    )
                )
            } else {
                val index = coursesList.indexOfFirst { it.id == course.id }
                if (index != -1) {
                    coursesList[index] = course.copy(
                        courseCode = courseCode,
                        courseName = courseName,
                        instructor = instructor,
                        room = room,
                        schedule = schedule,
                        credits = credits,
                        color = selectedColor
                    )
                }
            }

            adapter.updateCourses(coursesList)
            updateUI()
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

    private fun addSampleCourses() {
        coursesList.add(
            Course(
                id = 1,
                courseCode = "CS 101",
                courseName = "Introduction to Computer Science",
                instructor = "Dr. Sarah Johnson",
                room = "Room 204, Science Building",
                schedule = "Mon, Wed, Fri • 10:00 AM - 11:30 AM",
                credits = 3,
                color = "#6366F1"
            )
        )
        coursesList.add(
            Course(
                id = 2,
                courseCode = "MATH 201",
                courseName = "Calculus II",
                instructor = "Prof. Michael Chen",
                room = "Room 301, Math Building",
                schedule = "Tue, Thu • 2:00 PM - 3:30 PM",
                credits = 4,
                color = "#10B981"
            )
        )
        coursesList.add(
            Course(
                id = 3,
                courseCode = "ENG 105",
                courseName = "English Composition",
                instructor = "Dr. Emily Davis",
                room = "Room 102, Liberal Arts",
                schedule = "Mon, Wed • 1:00 PM - 2:30 PM",
                credits = 3,
                color = "#F59E0B"
            )
        )
    }
}
