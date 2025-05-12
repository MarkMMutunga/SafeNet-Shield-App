package com.example.myapplication

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
) 