package com.example.studentcompanion

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find root layout and apply window insets
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
        mainLayout.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                view.paddingLeft,
                insets.systemWindowInsetTop,    // Status bar
                view.paddingRight,
                insets.systemWindowInsetBottom  // Navigation bar
            )
            insets.consumeSystemWindowInsets()
        }

        // Initialize card views
        val cardCourses = findViewById<CardView>(R.id.cardCourses)
        val cardAssignments = findViewById<CardView>(R.id.cardAssignments)
        val cardTasks = findViewById<CardView>(R.id.cardTasks)
        val cardSchedule = findViewById<CardView>(R.id.cardSchedule)

        // Set click listeners
        cardCourses.setOnClickListener {
            startActivity(Intent(this, CoursesActivity::class.java))
        }

        cardAssignments.setOnClickListener {
            // startActivity(Intent(this, AssignmentsActivity::class.java))
        }

        cardTasks.setOnClickListener {
            // startActivity(Intent(this, TasksActivity::class.java))
        }

        cardSchedule.setOnClickListener {
            // startActivity(Intent(this, ScheduleActivity::class.java))
        }
    }
}
