package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_progress")
data class VideoProgress(
    @PrimaryKey val videoUrl: String,
    val percentWatched: Int,
    val lastPositionMs: Long,
    val totalDurationMs: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
