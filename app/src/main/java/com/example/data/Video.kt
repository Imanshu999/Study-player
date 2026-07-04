package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val videoUrl: String,
    val classLevel: Int, // 6 to 12
    val subject: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
