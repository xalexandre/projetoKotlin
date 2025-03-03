package dev.brodt.taskmanager.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import dev.brodt.taskmanager.models.Task
import dev.brodt.taskmanager.R

class TaskAdapter(context: Context, tasks: List<Task>) : ArrayAdapter<Task>(context, 0, tasks) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        }

        val currentTask = getItem(position)
        currentTask?.let { task ->
            val titleTextView = itemView?.findViewById<TextView>(R.id.task_title)
            val descriptionTextView = itemView?.findViewById<TextView>(R.id.task_description)
            val datetimeTextView = itemView?.findViewById<TextView>(R.id.task_datetime)

            titleTextView?.text = task.title
            descriptionTextView?.text = task.description
            datetimeTextView?.text = "${task.date} ${task.time}"
        }

        return itemView!!
    }
} 