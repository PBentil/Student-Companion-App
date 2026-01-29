package com.example.studentcompanion.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.R
import com.example.studentcompanion.model.Assignment
import com.example.studentcompanion.model.Priority
import com.example.studentcompanion.model.Status

class AssignmentsAdapter(
    private var assignments: List<Assignment>,
    private val onAssignmentClick: (Assignment) -> Unit,
    private val onCheckboxChange: (Assignment, Boolean) -> Unit
) : RecyclerView.Adapter<AssignmentsAdapter.AssignmentViewHolder>() {

    class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        val courseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        val title: TextView = itemView.findViewById(R.id.tvAssignmentTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val dueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val priority: TextView = itemView.findViewById(R.id.tvPriority)
        val priorityBadge: LinearLayout = itemView.findViewById(R.id.priorityBadge)
        val checkbox: AppCompatCheckBox = itemView.findViewById(R.id.checkboxComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = assignments[position]

        // Set text fields
        holder.courseCode.text = assignment.courseCode
        holder.title.text = assignment.title
        holder.description.text = assignment.description
        holder.dueDate.text = "Due: ${assignment.dueDate}"

        // Set priority
        when (assignment.priority) {
            Priority.LOW -> {
                holder.priority.text = "Low"
                holder.priority.setTextColor(Color.parseColor("#10B981"))
                holder.priorityBadge.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#D1FAE5"))
            }
            Priority.MEDIUM -> {
                holder.priority.text = "Medium"
                holder.priority.setTextColor(Color.parseColor("#F59E0B"))
                holder.priorityBadge.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
            }
            Priority.HIGH -> {
                holder.priority.text = "High"
                holder.priority.setTextColor(Color.parseColor("#EF4444"))
                holder.priorityBadge.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
            }
        }

        // Set status indicator color
        when (assignment.status) {
            Status.PENDING -> {
                holder.statusIndicator.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F59E0B"))
            }
            Status.COMPLETED -> {
                holder.statusIndicator.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981"))
            }
            Status.OVERDUE -> {
                holder.statusIndicator.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444"))
            }
        }

        // Set checkbox state
        holder.checkbox.isChecked = assignment.isCompleted

        // Apply strikethrough if completed
        if (assignment.isCompleted) {
            holder.title.paintFlags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.title.alpha = 0.6f
            holder.description.alpha = 0.6f
        } else {
            holder.title.paintFlags = holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.title.alpha = 1f
            holder.description.alpha = 1f
        }

        // Set checkbox listener
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckboxChange(assignment, isChecked)
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onAssignmentClick(assignment)
        }
    }

    override fun getItemCount(): Int = assignments.size

    fun updateAssignments(newAssignments: List<Assignment>) {
        assignments = newAssignments
        notifyDataSetChanged()
    }
}