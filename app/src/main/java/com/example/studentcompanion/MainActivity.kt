package com.example.studentcompanion

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.studentcompanion.database.StudentDatabase
import com.example.studentcompanion.model.Status
import com.example.studentcompanion.model.toAssignment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * MainActivity serves as the dashboard for the Student Companion app.
 * It displays summary statistics and providing navigation to different sections.
 */
class MainActivity : AppCompatActivity() {

    // Database and UI references
    private lateinit var database: StudentDatabase
    private lateinit var tvCoursesCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvDueTodayCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI Setup: Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // FUNCTION: Initialize Database Instance
        database = StudentDatabase.getDatabase(this)

        // Initialize UI components
        tvCoursesCount = findViewById(R.id.tvCoursesCount)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvDueTodayCount = findViewById(R.id.tvDueTodayCount)

        val cardCourses = findViewById<CardView>(R.id.cardCourses)
        val cardAssignments = findViewById<CardView>(R.id.cardAssignments)
        val cardTasks = findViewById<CardView>(R.id.cardTasks)
        val cardSchedule = findViewById<CardView>(R.id.cardSchedule)

        // CONDITIONAL LOGIC: Navigation handlers for dashboard cards
        cardCourses.setOnClickListener {
            startActivity(Intent(this, CoursesActivity::class.java))
        }

        cardAssignments.setOnClickListener {
            startActivity(Intent(this, AssignmentsActivity::class.java))
        }

        cardTasks.setOnClickListener {
             startActivity(Intent(this, TasksActivity::class.java))
        }

        cardSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh dashboard data whenever the activity becomes visible
        updateDashboard()
    }

    /**
     * FUNCTION: updateDashboard fetches real-time data from Room Database.
     * Uses Coroutines (lifecycleScope.launch) for background database operations.
     */
    private fun updateDashboard() {
        lifecycleScope.launch {
            // Fetch and display total courses
            val courses = database.courseDao().getAllCoursesSync()
            tvCoursesCount.text = courses.size.toString()

            // Fetch all assignments and convert them to domain models
            val assignments = database.assignmentDao().getAllAssignmentsSync().map { it.toAssignment() }
            
            // LOOP/FILTER: Count assignments with PENDING status
            val pendingCount = assignments.count { it.status == Status.PENDING }
            tvPendingCount.text = pendingCount.toString()

            // Date Handling
            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val todayStr = dateFormat.format(today.time)
            
            // LOOP/FILTER: Count assignments due on the current date
            val dueTodayCount = assignments.count { it.dueDate == todayStr }
            tvDueTodayCount.text = dueTodayCount.toString()
        }
    }
}