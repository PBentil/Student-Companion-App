package com.example.studentcompanion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize card views
        val cardCourses = findViewById<CardView>(R.id.cardCourses)
        val cardAssignments = findViewById<CardView>(R.id.cardAssignments)
        val cardTasks = findViewById<CardView>(R.id.cardTasks)
        val cardSchedule = findViewById<CardView>(R.id.cardSchedule)

        // Set click listeners
        cardCourses.setOnClickListener {
            // Navigate to Courses activity
            // startActivity(Intent(this, CoursesActivity::class.java))
        }

        cardAssignments.setOnClickListener {
            // Navigate to Assignments activity
            // startActivity(Intent(this, AssignmentsActivity::class.java))
        }

        cardTasks.setOnClickListener {
            // Navigate to Tasks activity
            // startActivity(Intent(this, TasksActivity::class.java))
        }

        cardSchedule.setOnClickListener {
            // Navigate to Schedule activity
            // startActivity(Intent(this, ScheduleActivity::class.java))
        }
    }
}