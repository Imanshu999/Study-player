package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos WHERE classLevel = :classLevel AND subject = :subject ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getVideosPaged(classLevel: Int, subject: String, limit: Int, offset: Int): List<Video>

    @Query("SELECT COUNT(*) FROM videos WHERE classLevel = :classLevel AND subject = :subject")
    suspend fun getVideosCount(classLevel: Int, subject: String): Int

    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideosFlow(): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video): Long

    @Delete
    suspend fun deleteVideo(video: Video)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    // Notes Queries
    @Query("SELECT * FROM notes WHERE videoUrl = :videoUrl ORDER BY timestampMs ASC")
    fun getNotesForVideoFlow(videoUrl: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // Bookmarks Queries
    @Query("SELECT * FROM bookmarks WHERE videoUrl = :videoUrl ORDER BY positionMs ASC")
    fun getBookmarksForVideoFlow(videoUrl: String): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    // Video Progress Queries
    @Query("SELECT * FROM video_progress WHERE videoUrl = :videoUrl")
    suspend fun getProgressForVideo(videoUrl: String): VideoProgress?

    @Query("SELECT * FROM video_progress WHERE videoUrl = :videoUrl")
    fun getProgressForVideoFlow(videoUrl: String): Flow<VideoProgress?>

    @Query("SELECT * FROM video_progress ORDER BY lastUpdated DESC")
    fun getAllProgressFlow(): Flow<List<VideoProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: VideoProgress)
}
