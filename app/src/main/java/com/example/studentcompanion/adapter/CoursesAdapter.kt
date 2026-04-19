package com.example.studentcompanion.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.R
import com.example.studentcompanion.model.Course

class CoursesAdapter(
    private var courses: List<Course>,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<CoursesAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorIndicator: View = itemView.findViewById(R.id.courseColorIndicator)
        val courseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        val courseName: TextView = itemView.findViewById(R.id.tvCourseName)
        val instructor: TextView = itemView.findViewById(R.id.tvInstructor)
        val room: TextView = itemView.findViewById(R.id.tvRoom)
        val schedule: TextView = itemView.findViewById(R.id.tvSchedule)
        val credits: TextView = itemView.findViewById(R.id.tvCredits)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]

        // Set course color
        holder.colorIndicator.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(course.color))

        // Set text fields
        holder.courseCode.text = course.courseCode
        holder.courseName.text = course.courseName
        holder.instructor.text = course.instructor
        holder.room.text = course.room
        holder.schedule.text = course.schedule
        holder.credits.text = course.credits.toString()

        // Set click listener
        holder.itemView.setOnClickListener {
            onCourseClick(course)
        }
    }

    override fun getItemCount(): Int = courses.size

    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}