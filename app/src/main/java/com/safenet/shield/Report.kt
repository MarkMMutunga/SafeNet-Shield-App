package com.safenet.shield

data class Report(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val userId: String
) 