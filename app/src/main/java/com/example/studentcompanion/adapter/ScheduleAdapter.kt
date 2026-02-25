package com.example.studentcompanion.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.R
import com.example.studentcompanion.model.Schedule

class ScheduleAdapter(
    private var schedules: List<Schedule>,
    private val onScheduleClick: (Schedule) -> Unit,
    private val onScheduleLongClick: (Schedule) -> Boolean
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeIndicator: View = itemView.findViewById(R.id.timeIndicator)
        val time: TextView = itemView.findViewById(R.id.tvTime)
        val duration: TextView = itemView.findViewById(R.id.tvDuration)
        val courseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        val courseName: TextView = itemView.findViewById(R.id.tvCourseName)
        val room: TextView = itemView.findViewById(R.id.tvRoom)
        val instructor: TextView = itemView.findViewById(R.id.tvInstructor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]

        holder.timeIndicator.setBackgroundColor(Color.parseColor(schedule.color))

        holder.time.text = "${schedule.startTime} - ${schedule.endTime}"

        val duration = schedule.getDurationMinutes()
        holder.duration.text = "$duration min"

        holder.courseCode.text = schedule.courseCode
        holder.courseName.text = schedule.courseName
        holder.room.text = schedule.room
        holder.instructor.text = schedule.instructor

        holder.itemView.setOnClickListener {
            onScheduleClick(schedule)
        }

        holder.itemView.setOnLongClickListener {
            onScheduleLongClick(schedule)
        }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateSchedules(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }
}