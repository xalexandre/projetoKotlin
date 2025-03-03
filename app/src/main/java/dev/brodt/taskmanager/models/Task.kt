package dev.brodt.taskmanager.models

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val userId: String = ""
) 