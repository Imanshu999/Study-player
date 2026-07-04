package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository(private val videoDao: VideoDao) {

    fun getAllVideosFlow(): Flow<List<Video>> = videoDao.getAllVideosFlow()

    suspend fun getVideosPaged(classLevel: Int, subject: String, limit: Int, offset: Int): List<Video> {
        return withContext(Dispatchers.IO) {
            videoDao.getVideosPaged(classLevel, subject, limit, offset)
        }
    }

    suspend fun getVideosCount(classLevel: Int, subject: String): Int {
        return withContext(Dispatchers.IO) {
            videoDao.getVideosCount(classLevel, subject)
        }
    }

    suspend fun insertVideo(video: Video): Long {
        return withContext(Dispatchers.IO) {
            videoDao.insertVideo(video)
        }
    }

    suspend fun deleteVideo(video: Video) {
        withContext(Dispatchers.IO) {
            videoDao.deleteVideo(video)
        }
    }

    suspend fun deleteVideoById(id: Long) {
        withContext(Dispatchers.IO) {
            videoDao.deleteVideoById(id)
        }
    }

    suspend fun prepopulateIfEmpty() {
        withContext(Dispatchers.IO) {
            // Check if database is empty by fetching count of a generic check
            val existing = videoDao.getVideosPaged(6, "Science", 1, 0)
            val existing2 = videoDao.getVideosPaged(10, "Mathematics", 1, 0)
            val existing3 = videoDao.getVideosPaged(12, "Physics", 1, 0)
            
            if (existing.isEmpty() && existing2.isEmpty() && existing3.isEmpty()) {
                val sampleVideos = listOf(
                    // Class 12 - Physics
                    Video(
                        title = "Electrostatics Lecture 1: Charge and Coulomb's Law",
                        description = "Complete masterclass on Electrostatics for Class 12. Understand electric force, charge properties, and solve foundational numerical problems.",
                        videoUrl = "https://www.youtube.com/watch?v=U36m05XGZp8",
                        classLevel = 12,
                        subject = "Physics",
                        language = "English"
                    ),
                    Video(
                        title = "Electrostatics Lecture 2: Electric Field & Dipoles",
                        description = "Deep dive into the electric field strength, field lines, and physical behavior of electric dipoles in uniform fields.",
                        videoUrl = "https://www.youtube.com/watch?v=Ror_SshV_S0",
                        classLevel = 12,
                        subject = "Physics",
                        language = "English"
                    ),
                    // Class 12 - Chemistry
                    Video(
                        title = "Chemical Kinetics: Rate Laws & Order of Reaction",
                        description = "Learn how temperature, concentration, and catalysts affect the rate of chemical reactions. Designed for Class 12 Boards preparation.",
                        videoUrl = "https://www.youtube.com/watch?v=S-R4Xz-f_pU",
                        classLevel = 12,
                        subject = "Chemistry",
                        language = "Hindi / English"
                    ),
                    // Class 12 - Biology
                    Video(
                        title = "DNA Replication Mechanisms & Enzymes",
                        description = "Visual step-by-step molecular explanation of leading/lagging strands, Helicase, DNA Polymerase, and Okazaki fragments.",
                        videoUrl = "https://www.youtube.com/watch?v=TNKWgcF5UPU",
                        classLevel = 12,
                        subject = "Biology",
                        language = "English"
                    ),
                    // Class 12 - Mathematics
                    Video(
                        title = "Calculus Masterclass: Limits, Continuity & Differentiability",
                        description = "Unlock limits and continuity in Calculus. Standard formulas, limit proofs, and derivative shortcuts.",
                        videoUrl = "https://www.youtube.com/watch?v=7uV87pUeWkE",
                        classLevel = 12,
                        subject = "Mathematics",
                        language = "English"
                    ),
                    // Class 12 - Economics
                    Video(
                        title = "Introductory Macroeconomics: Circular Flow of Income",
                        description = "Introduction to Macroeconomics for Class 12. Learn about real flows, money flows, and the 2-sector model.",
                        videoUrl = "https://www.youtube.com/watch?v=VzN32L7N1rM",
                        classLevel = 12,
                        subject = "Economics",
                        language = "English"
                    ),
                    // Class 10 - Mathematics
                    Video(
                        title = "Quadratic Equations: Complete Introduction & Solutions",
                        description = "Step-by-step tutorial on solving quadratic equations by factoring, completing the square, and using the quadratic formula.",
                        videoUrl = "https://www.youtube.com/watch?v=XyA6vH3Y8gI",
                        classLevel = 10,
                        subject = "Mathematics",
                        language = "Hindi"
                    ),
                    Video(
                        title = "Real Numbers: Euclid's Division Lemma & HCF/LCM",
                        description = "Master the first chapter of Class 10 Mathematics. Fully explains rational numbers, prime factorization, and key proof techniques.",
                        videoUrl = "https://www.youtube.com/watch?v=e_0p6jCg8Z8",
                        classLevel = 10,
                        subject = "Mathematics",
                        language = "Hindi / English"
                    ),
                    // Class 10 - Science
                    Video(
                        title = "Chemical Reactions and Equations: Full Chapter Review",
                        description = "An excellent overview of displacement, combination, decomposition, and redox reactions. Perfect for revision.",
                        videoUrl = "https://www.youtube.com/watch?v=8m9g_Zq5Xk8",
                        classLevel = 10,
                        subject = "Science",
                        language = "English"
                    ),
                    Video(
                        title = "Light: Reflection and Refraction - Lens Formula Explained",
                        description = "Detailed physics breakdown of convex and concave mirrors, lens formulas, magnification, and light propagation.",
                        videoUrl = "https://www.youtube.com/watch?v=Z4b4mHezS7o",
                        classLevel = 10,
                        subject = "Science",
                        language = "English"
                    ),
                    // Class 10 - English
                    Video(
                        title = "Tenses & Active-Passive Voice Crash Course",
                        description = "Brush up your English Grammar concepts with real-time exercises, rule explanations, and common board-exam patterns.",
                        videoUrl = "https://www.youtube.com/watch?v=N6Uj2X7-wQ0",
                        classLevel = 10,
                        subject = "English",
                        language = "English"
                    ),
                    // Class 8 - Science
                    Video(
                        title = "Crop Production & Management: Soil Preparation to Harvesting",
                        description = "Class 8 Science Chapter 1. Discusses traditional vs modern irrigation, fertilizers, harvesting tools, and storage practices.",
                        videoUrl = "https://www.youtube.com/watch?v=l_g2H9F9yFs",
                        classLevel = 8,
                        subject = "Science",
                        language = "English"
                    ),
                    // Class 8 - Social Science
                    Video(
                        title = "The Indian Constitution: Preamble, Key Features & Rights",
                        description = "Interactive civic lesson on why a country needs a constitution, fundamental rights of citizens, and key pillars of democracy.",
                        videoUrl = "https://www.youtube.com/watch?v=9_p5_N3t24s",
                        classLevel = 8,
                        subject = "Social Science",
                        language = "English"
                    ),
                    // Class 6 - Mathematics
                    Video(
                        title = "Knowing Our Numbers: Large Number Comparison & Estimation",
                        description = "Fun introduction to Indian and International place value systems, writing large numbers, and rounding off estimates.",
                        videoUrl = "https://www.youtube.com/watch?v=q6bKOnMbe90",
                        classLevel = 6,
                        subject = "Mathematics",
                        language = "English"
                    )
                )
                
                for (v in sampleVideos) {
                    videoDao.insertVideo(v)
                }
            }
        }
    }

    // Notes methods
    fun getNotesForVideo(videoUrl: String): Flow<List<Note>> = videoDao.getNotesForVideoFlow(videoUrl)

    suspend fun insertNote(note: Note): Long {
        return withContext(Dispatchers.IO) {
            videoDao.insertNote(note)
        }
    }

    suspend fun deleteNoteById(id: Long) {
        withContext(Dispatchers.IO) {
            videoDao.deleteNoteById(id)
        }
    }

    // Bookmarks methods
    fun getBookmarksForVideo(videoUrl: String): Flow<List<Bookmark>> = videoDao.getBookmarksForVideoFlow(videoUrl)

    suspend fun insertBookmark(bookmark: Bookmark): Long {
        return withContext(Dispatchers.IO) {
            videoDao.insertBookmark(bookmark)
        }
    }

    suspend fun deleteBookmarkById(id: Long) {
        withContext(Dispatchers.IO) {
            videoDao.deleteBookmarkById(id)
        }
    }

    // VideoProgress methods
    suspend fun getProgressForVideo(videoUrl: String): VideoProgress? {
        return withContext(Dispatchers.IO) {
            videoDao.getProgressForVideo(videoUrl)
        }
    }

    fun getProgressForVideoFlow(videoUrl: String): Flow<VideoProgress?> = videoDao.getProgressForVideoFlow(videoUrl)

    fun getAllProgressFlow(): Flow<List<VideoProgress>> = videoDao.getAllProgressFlow()

    suspend fun saveVideoProgress(progress: VideoProgress) {
        withContext(Dispatchers.IO) {
            videoDao.insertProgress(progress)
        }
    }
}
