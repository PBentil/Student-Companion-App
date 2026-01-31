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

class MainActivity : AppCompatActivity() {

    private lateinit var database: StudentDatabase
    private lateinit var tvCoursesCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvDueTodayCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        database = StudentDatabase.getDatabase(this)

        tvCoursesCount = findViewById(R.id.tvCoursesCount)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvDueTodayCount = findViewById(R.id.tvDueTodayCount)

        val cardCourses = findViewById<CardView>(R.id.cardCourses)
        val cardAssignments = findViewById<CardView>(R.id.cardAssignments)
        val cardTasks = findViewById<CardView>(R.id.cardTasks)
        val cardSchedule = findViewById<CardView>(R.id.cardSchedule)

        cardCourses.setOnClickListener {
            startActivity(Intent(this, CoursesActivity::class.java))
        }

        cardAssignments.setOnClickListener {
            startActivity(Intent(this, AssignmentsActivity::class.java))
        }

        cardTasks.setOnClickListener {
            // startActivity(Intent(this, TasksActivity::class.java))
        }

        cardSchedule.setOnClickListener {
            // startActivity(Intent(this, ScheduleActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        lifecycleScope.launch {
            val courses = database.courseDao().getAllCoursesSync()
            tvCoursesCount.text = courses.size.toString()

            val assignments = database.assignmentDao().getAllAssignmentsSync().map { it.toAssignment() }
            
            val pendingCount = assignments.count { it.status == Status.PENDING }
            tvPendingCount.text = pendingCount.toString()

            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val todayStr = dateFormat.format(today.time)
            
            val dueTodayCount = assignments.count { it.dueDate == todayStr }
            tvDueTodayCount.text = dueTodayCount.toString()
        }
    }
}
