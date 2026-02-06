package com.example.studentcompanion.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.studentcompanion.R
import com.example.studentcompanion.model.Task

class TasksAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxChange: (Task, Boolean) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: AppCompatCheckBox = itemView.findViewById(R.id.checkboxTask)
        val title: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val description: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val categoryBadge: LinearLayout = itemView.findViewById(R.id.categoryBadge)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val deleteIcon: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.checkbox.isChecked = task.isCompleted

        holder.title.text = task.title

        if (task.isCompleted) {
            holder.title.paintFlags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.title.alpha = 0.5f
            holder.description.alpha = 0.5f
        } else {
            holder.title.paintFlags = holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.title.alpha = 1f
            holder.description.alpha = 1f
        }

        if (task.description.isNotEmpty()) {
            holder.description.text = task.description
            holder.description.visibility = View.VISIBLE
        } else {
            holder.description.visibility = View.GONE
        }

        if (task.category.isNotEmpty()) {
            holder.category.text = task.category
            holder.categoryBadge.visibility = View.VISIBLE
        } else {
            holder.categoryBadge.visibility = View.GONE
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckboxChange(task, isChecked)
        }

        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        holder.deleteIcon.setOnClickListener {
            onDeleteClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
