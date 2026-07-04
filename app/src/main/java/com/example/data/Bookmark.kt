package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUrl: String,
    val positionMs: Long,
    val label: String,
    val createdTime: Long = System.currentTimeMillis()
)
