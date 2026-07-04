package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUrl: String,
    val noteText: String,
    val timestampMs: Long,
    val createdTime: Long = System.currentTimeMillis()
)
