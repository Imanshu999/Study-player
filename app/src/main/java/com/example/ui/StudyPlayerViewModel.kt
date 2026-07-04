package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StudentProfile
import com.example.data.StudentProfileStore
import com.example.data.Video
import com.example.data.VideoDatabase
import com.example.data.VideoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class StudyPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VideoDatabase.getDatabase(application)
    private val videoRepository = VideoRepository(database.videoDao())
    private val profileStore = StudentProfileStore(application)
    private val auth = FirebaseAuth.getInstance()

    // Profile State
    private val _profileState = MutableStateFlow(StudentProfile(6, "Not Specified", "", "", false))
    val profileState: StateFlow<StudentProfile> = _profileState.asStateFlow()

    // Subject Filter
    private val _selectedSubject = MutableStateFlow("")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    // Pagination
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _pageSize = 4

    // Videos for selected class and subject
    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList: StateFlow<List<Video>> = _videoList.asStateFlow()

    private val _videoCount = MutableStateFlow(0)
    val videoCount: StateFlow<Int> = _videoCount.asStateFlow()

    private val _isLoadingVideos = MutableStateFlow(false)
    val isLoadingVideos: StateFlow<Boolean> = _isLoadingVideos.asStateFlow()

    // All Videos list for admin management
    private val _allVideosList = MutableStateFlow<List<Video>>(emptyList())
    val allVideosList: StateFlow<List<Video>> = _allVideosList.asStateFlow()

    // Overall Study progress mapping
    private val _allProgress = MutableStateFlow<List<com.example.data.VideoProgress>>(emptyList())
    val allProgress: StateFlow<List<com.example.data.VideoProgress>> = _allProgress.asStateFlow()

    // Admin panel login state (non-persistent, safe session)
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    // Firebase Auth States
    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        // Initialize Auth listener
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isUserLoggedIn.value = user != null
            if (user != null) {
                fetchProfileFromFirestore(user.email ?: "")
            }
        }

        viewModelScope.launch {
            // Check for pre-population in local Room database
            videoRepository.prepopulateIfEmpty()

            // Pre-populate Firestore with standard videos if Firestore is empty
            uploadPrepopulatedToFirestore()

            // Observe Profile Flow from local DataStore
            profileStore.profileFlow.collect { profile ->
                _profileState.value = profile
                
                // Set default subject if not already set or not compatible
                val subjects = getSubjectsForClass(profile.selectedClass)
                if (_selectedSubject.value.isEmpty() || !subjects.contains(_selectedSubject.value)) {
                    if (subjects.isNotEmpty()) {
                        _selectedSubject.value = subjects[0]
                    }
                }
                
                // Fetch videos for this class and subject
                fetchVideos()
            }
        }

        // Observe All Videos Flow
        viewModelScope.launch {
            videoRepository.getAllVideosFlow().collect { list ->
                _allVideosList.value = list
            }
        }

        // Observe All Progress Flow
        viewModelScope.launch {
            videoRepository.getAllProgressFlow().collect { progressList ->
                _allProgress.value = progressList
            }
        }
    }

    private fun fetchProfileFromFirestore(email: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(email.replace(".", "_")).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val classLevel = document.getLong("classLevel")?.toInt() ?: 10
                            val gender = document.getString("gender") ?: "Not Specified"
                            val mobile = document.getString("mobile") ?: ""
                            
                            viewModelScope.launch {
                                val updated = StudentProfile(
                                    selectedClass = classLevel,
                                    gender = gender,
                                    mobileNumber = mobile,
                                    email = email,
                                    isRegistered = true
                                )
                                profileStore.saveProfile(updated)
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadPrepopulatedToFirestore() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("videos").limit(1).get().addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        val sampleVideos = listOf(
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
                            Video(
                                title = "Chemical Kinetics: Rate Laws & Order of Reaction",
                                description = "Learn how temperature, concentration, and catalysts affect the rate of chemical reactions. Designed for Class 12 Boards preparation.",
                                videoUrl = "https://www.youtube.com/watch?v=S-R4Xz-f_pU",
                                classLevel = 12,
                                subject = "Chemistry",
                                language = "Hindi / English"
                            ),
                            Video(
                                title = "DNA Replication Mechanisms & Enzymes",
                                description = "Visual step-by-step molecular explanation of leading/lagging strands, Helicase, DNA Polymerase, and Okazaki fragments.",
                                videoUrl = "https://www.youtube.com/watch?v=TNKWgcF5UPU",
                                classLevel = 12,
                                subject = "Biology",
                                language = "English"
                            ),
                            Video(
                                title = "Calculus Masterclass: Limits, Continuity & Differentiability",
                                description = "Unlock limits and continuity in Calculus. Standard formulas, limit proofs, and derivative shortcuts.",
                                videoUrl = "https://www.youtube.com/watch?v=7uV87pUeWkE",
                                classLevel = 12,
                                subject = "Mathematics",
                                language = "English"
                            ),
                            Video(
                                title = "Quadratic Equations: Complete Introduction & Solutions",
                                description = "Step-by-step tutorial on solving quadratic equations by factoring, completing the square, and using the quadratic formula.",
                                videoUrl = "https://www.youtube.com/watch?v=XyA6vH3Y8gI",
                                classLevel = 10,
                                subject = "Mathematics",
                                language = "Hindi"
                            ),
                            Video(
                                title = "Chemical Reactions and Equations: Full Chapter Review",
                                description = "An excellent overview of displacement, combination, decomposition, and redox reactions. Perfect for revision.",
                                videoUrl = "https://www.youtube.com/watch?v=8m9g_Zq5Xk8",
                                classLevel = 10,
                                subject = "Science",
                                language = "English"
                            ),
                            Video(
                                title = "Crop Production & Management: Soil Preparation to Harvesting",
                                description = "Class 8 Science Chapter 1. Discusses traditional vs modern irrigation, fertilizers, harvesting tools, and storage practices.",
                                videoUrl = "https://www.youtube.com/watch?v=l_g2H9F9yFs",
                                classLevel = 8,
                                subject = "Science",
                                language = "English"
                            )
                        )
                        for (v in sampleVideos) {
                            val data = hashMapOf(
                                "title" to v.title,
                                "description" to v.description,
                                "videoUrl" to v.videoUrl,
                                "classLevel" to v.classLevel,
                                "subject" to v.subject,
                                "language" to v.language
                            )
                            db.collection("videos").add(data)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Firebase User Registration
    fun registerStudent(
        email: String,
        password: String,
        classLevel: Int,
        gender: String,
        mobile: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _authLoading.value = true
        _authError.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userMap = hashMapOf(
                        "classLevel" to classLevel,
                        "gender" to gender,
                        "mobile" to mobile,
                        "email" to email
                    )
                    db.collection("users").document(email.replace(".", "_")).set(userMap)
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                saveProfile(classLevel, gender, mobile, email)
                                _authLoading.value = false
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { e ->
                            viewModelScope.launch {
                                saveProfile(classLevel, gender, mobile, email)
                                _authLoading.value = false
                                onSuccess()
                            }
                        }
                } else {
                    _authLoading.value = false
                    _authError.value = "Registration failed"
                    onError("Registration failed")
                }
            }
            .addOnFailureListener { e ->
                _authLoading.value = false
                _authError.value = e.localizedMessage ?: "Registration failed"
                onError(e.localizedMessage ?: "Registration failed")
            }
    }

    // Firebase User Login
    fun loginStudent(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _authLoading.value = true
        _authError.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    fetchProfileFromFirestore(email)
                    _authLoading.value = false
                    onSuccess()
                } else {
                    _authLoading.value = false
                    _authError.value = "Login failed"
                    onError("Login failed")
                }
            }
            .addOnFailureListener { e ->
                _authLoading.value = false
                _authError.value = e.localizedMessage ?: "Login failed"
                onError(e.localizedMessage ?: "Login failed")
            }
    }

    // Firebase Password Reset Email
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _authLoading.value = true
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _authLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _authLoading.value = false
                onError(e.localizedMessage ?: "Failed to send reset email")
            }
    }

    fun getSubjectsForClass(classLevel: Int): List<String> {
        return when (classLevel) {
            6, 7, 8 -> listOf("Science", "Social Science", "Mathematics", "English")
            9, 10 -> listOf("Science", "Social Studies", "Mathematics", "English", "Hindi")
            11, 12 -> listOf("Physics", "Chemistry", "Biology", "Mathematics", "Economics", "Business Studies", "Accountancy", "History", "Political Science", "Geography")
            else -> listOf("Science", "Mathematics")
        }
    }

    fun saveProfile(classLevel: Int, gender: String, mobile: String, email: String) {
        viewModelScope.launch {
            val updatedProfile = StudentProfile(
                selectedClass = classLevel,
                gender = gender,
                mobileNumber = mobile,
                email = email,
                isRegistered = true
            )
            profileStore.saveProfile(updatedProfile)
            _currentPage.value = 0
            
            val subjects = getSubjectsForClass(classLevel)
            if (subjects.isNotEmpty()) {
                _selectedSubject.value = subjects[0]
            }
            fetchVideos()
        }
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        _currentPage.value = 0 // Reset pagination
        fetchVideos()
    }

    fun nextPage() {
        val totalCount = _videoCount.value
        val maxPage = (totalCount - 1) / _pageSize
        if (_currentPage.value < maxPage) {
            _currentPage.value += 1
            fetchVideos()
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            fetchVideos()
        }
    }

    fun fetchVideos() {
        viewModelScope.launch {
            _isLoadingVideos.value = true
            val profile = _profileState.value
            val subject = _selectedSubject.value
            val page = _currentPage.value
            val offset = page * _pageSize
            
            if (subject.isNotEmpty()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("videos")
                        .whereEqualTo("classLevel", profile.selectedClass)
                        .whereEqualTo("subject", subject)
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                val list = mutableListOf<Video>()
                                for (document in result) {
                                    val title = document.getString("title") ?: ""
                                    val description = document.getString("description") ?: ""
                                    val videoUrl = document.getString("videoUrl") ?: ""
                                    val classLvl = document.getLong("classLevel")?.toInt() ?: 0
                                    val subj = document.getString("subject") ?: ""
                                    val language = document.getString("language") ?: ""
                                    list.add(Video(
                                        id = document.id.hashCode().toLong(),
                                        title = title,
                                        description = description,
                                        videoUrl = videoUrl,
                                        classLevel = classLvl,
                                        subject = subj,
                                        language = language
                                    ))
                                }
                                _videoCount.value = list.size
                                val pagedList = list.drop(offset).take(_pageSize)
                                _videoList.value = pagedList
                                _isLoadingVideos.value = false
                            } else {
                                fetchLocalVideos(profile.selectedClass, subject, offset)
                            }
                        }
                        .addOnFailureListener {
                            fetchLocalVideos(profile.selectedClass, subject, offset)
                        }
                } catch (e: Exception) {
                    fetchLocalVideos(profile.selectedClass, subject, offset)
                }
            } else {
                _videoList.value = emptyList()
                _videoCount.value = 0
                _isLoadingVideos.value = false
            }
        }
    }

    private fun fetchLocalVideos(classLevel: Int, subject: String, offset: Int) {
        viewModelScope.launch {
            val videos = videoRepository.getVideosPaged(classLevel, subject, _pageSize, offset)
            val count = videoRepository.getVideosCount(classLevel, subject)
            _videoList.value = videos
            _videoCount.value = count
            _isLoadingVideos.value = false
        }
    }

    // Admin Operations
    private val _adminAuthError = MutableStateFlow<String?>(null)
    val adminAuthError: StateFlow<String?> = _adminAuthError.asStateFlow()

    private val _adminAuthLoading = MutableStateFlow(false)
    val adminAuthLoading: StateFlow<Boolean> = _adminAuthLoading.asStateFlow()

    fun authenticateAdmin(email: String, word: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _adminAuthLoading.value = true
        _adminAuthError.value = null

        // Try Firebase Auth first
        auth.signInWithEmailAndPassword(email, word)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("admin_config").document("config").get()
                        .addOnSuccessListener { doc ->
                            val adminEmails = doc.get("emails") as? List<*>
                            val isEmailAdmin = adminEmails?.contains(email) ?: (email == "admin@studyplayer.com" || email == user.email)
                            
                            if (isEmailAdmin) {
                                _isAdminAuthenticated.value = true
                                _adminAuthLoading.value = false
                                onSuccess()
                            } else {
                                // Fallback checking if config document has specific admin credentials matched
                                val configEmail = doc.getString("email")
                                val configPassword = doc.getString("password")
                                if (configEmail == email) {
                                    _isAdminAuthenticated.value = true
                                    _adminAuthLoading.value = false
                                    onSuccess()
                                } else {
                                    // Fallback to local admin
                                    if (email.trim().lowercase() == "admin@studyplayer.com" && word == "admin123") {
                                        _isAdminAuthenticated.value = true
                                        _adminAuthLoading.value = false
                                        onSuccess()
                                    } else {
                                        _adminAuthLoading.value = false
                                        _adminAuthError.value = "Not authorized as administrator."
                                        onError("Not authorized as administrator.")
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            if (email.trim().lowercase() == "admin@studyplayer.com" || email == user.email) {
                                _isAdminAuthenticated.value = true
                                _adminAuthLoading.value = false
                                onSuccess()
                            } else {
                                _adminAuthLoading.value = false
                                _adminAuthError.value = "Admin verification failed."
                                onError("Admin verification failed.")
                            }
                        }
                } else {
                    _adminAuthLoading.value = false
                    _adminAuthError.value = "Authentication failed."
                    onError("Authentication failed.")
                }
            }
            .addOnFailureListener { e ->
                // If Firebase Auth fails, check if Firestore config has custom offline/separate credentials
                val db = FirebaseFirestore.getInstance()
                db.collection("admin_config").document("config").get()
                    .addOnSuccessListener { doc ->
                        val configEmail = doc.getString("email") ?: "admin@studyplayer.com"
                        val configPassword = doc.getString("password") ?: "admin123"
                        if (email.trim().lowercase() == configEmail.trim().lowercase() && word == configPassword) {
                            _isAdminAuthenticated.value = true
                            _adminAuthLoading.value = false
                            onSuccess()
                        } else {
                            // Local fallback
                            if (email.trim().lowercase() == "admin@studyplayer.com" && word == "admin123") {
                                _isAdminAuthenticated.value = true
                                _adminAuthLoading.value = false
                                onSuccess()
                            } else {
                                _adminAuthLoading.value = false
                                _adminAuthError.value = e.localizedMessage ?: "Invalid admin credentials."
                                onError(e.localizedMessage ?: "Invalid admin credentials.")
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Strict local fallback
                        if (email.trim().lowercase() == "admin@studyplayer.com" && word == "admin123") {
                            _isAdminAuthenticated.value = true
                            _adminAuthLoading.value = false
                            onSuccess()
                        } else {
                            _adminAuthLoading.value = false
                            _adminAuthError.value = e.localizedMessage ?: "Invalid admin credentials."
                            onError(e.localizedMessage ?: "Invalid admin credentials.")
                        }
                    }
            }
    }

    // Direct synchronous validation fallback
    fun authenticateAdmin(email: String, word: String): Boolean {
        return if (email.trim().lowercase() == "admin@studyplayer.com" && word == "admin123") {
            _isAdminAuthenticated.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    // Ask Gemini AI about the lecture (reads key dynamically from BuildConfig)
    private val _geminiResponse = MutableStateFlow<String?>(null)
    val geminiResponse: StateFlow<String?> = _geminiResponse.asStateFlow()

    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    fun askGemini(lectureTitle: String, lectureDesc: String, question: String) {
        _isGeminiLoading.value = true
        _geminiResponse.value = null
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _geminiResponse.value = "Gemini API key is not configured in secrets. Please configure GEMINI_API_KEY."
                    _isGeminiLoading.value = false
                    return@launch
                }

                val prompt = """
                    You are "Study Player AI Assistant".
                    A student is watching a lecture:
                    Title: $lectureTitle
                    Description: $lectureDesc
                    
                    They have asked the following question:
                    "$question"
                    
                    Provide a helpful, precise, and encouraging response based on their question and the lecture context. Keep it under 200 words.
                """.trimIndent()

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val jsonBody = """
                    {
                      "contents": [{
                        "parts": [{
                          "text": ${org.json.JSONObject.quote(prompt)}
                        }]
                      }]
                    }
                """.trimIndent()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonBody.toRequestBody(mediaType)

                val request = okhttp3.Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        _geminiResponse.value = "Network Error: ${e.localizedMessage}"
                        _isGeminiLoading.value = false
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        response.use { resp ->
                            if (!resp.isSuccessful) {
                                _geminiResponse.value = "Error: API call returned code ${resp.code}"
                                _isGeminiLoading.value = false
                                return
                            }
                            val respBody = resp.body?.string()
                            if (respBody != null) {
                                try {
                                    val jsonObj = org.json.JSONObject(respBody)
                                    val candidates = jsonObj.getJSONArray("candidates")
                                    val firstCandidate = candidates.getJSONObject(0)
                                    val content = firstCandidate.getJSONObject("content")
                                    val parts = content.getJSONArray("parts")
                                    val textResult = parts.getJSONObject(0).getString("text")
                                    _geminiResponse.value = textResult
                                } catch (e: Exception) {
                                    _geminiResponse.value = "Parsing Error: Could not decode response."
                                }
                            } else {
                                _geminiResponse.value = "Received empty response from Gemini."
                            }
                            _isGeminiLoading.value = false
                        }
                    }
                })
            } catch (e: Exception) {
                _geminiResponse.value = "Unexpected Error: ${e.localizedMessage}"
                _isGeminiLoading.value = false
            }
        }
    }

    fun addNewVideo(
        title: String,
        description: String,
        url: String,
        classLevel: Int,
        subject: String,
        language: String
    ) {
        viewModelScope.launch {
            val newVid = Video(
                title = title,
                description = description,
                videoUrl = url,
                classLevel = classLevel,
                subject = subject,
                language = language
            )
            videoRepository.insertVideo(newVid)
            
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "videoUrl" to url,
                    "classLevel" to classLevel,
                    "subject" to subject,
                    "language" to language
                )
                db.collection("videos").add(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val profile = _profileState.value
            if (profile.selectedClass == classLevel && _selectedSubject.value == subject) {
                fetchVideos()
            }
        }
    }

    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            videoRepository.deleteVideo(video)
            
            val profile = _profileState.value
            if (profile.selectedClass == video.classLevel && _selectedSubject.value == video.subject) {
                fetchVideos()
            }
        }
    }

    fun clearStudentData() {
        viewModelScope.launch {
            auth.signOut()
            profileStore.clearProfile()
            _profileState.value = StudentProfile(6, "Not Specified", "", "", false)
            _selectedSubject.value = ""
            _currentPage.value = 0
            _videoList.value = emptyList()
            _videoCount.value = 0
        }
    }

    // Notes management
    fun getNotesForVideo(videoUrl: String): kotlinx.coroutines.flow.Flow<List<com.example.data.Note>> = 
        videoRepository.getNotesForVideo(videoUrl)

    fun addNote(videoUrl: String, noteText: String, timestampMs: Long) {
        viewModelScope.launch {
            val note = com.example.data.Note(
                videoUrl = videoUrl,
                noteText = noteText,
                timestampMs = timestampMs
            )
            videoRepository.insertNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            videoRepository.deleteNoteById(id)
        }
    }

    // Bookmarks management
    fun getBookmarksForVideo(videoUrl: String): kotlinx.coroutines.flow.Flow<List<com.example.data.Bookmark>> = 
        videoRepository.getBookmarksForVideo(videoUrl)

    fun addBookmark(videoUrl: String, positionMs: Long, label: String) {
        viewModelScope.launch {
            val bookmark = com.example.data.Bookmark(
                videoUrl = videoUrl,
                positionMs = positionMs,
                label = label
            )
            videoRepository.insertBookmark(bookmark)
        }
    }

    fun deleteBookmarkById(id: Long) {
        viewModelScope.launch {
            videoRepository.deleteBookmarkById(id)
        }
    }

    // Progress management
    fun getProgressForVideoFlow(videoUrl: String): kotlinx.coroutines.flow.Flow<com.example.data.VideoProgress?> = 
        videoRepository.getProgressForVideoFlow(videoUrl)

    fun saveVideoProgress(videoUrl: String, percentWatched: Int, lastPositionMs: Long, totalDurationMs: Long) {
        viewModelScope.launch {
            try {
                val progress = com.example.data.VideoProgress(
                    videoUrl = videoUrl,
                    percentWatched = percentWatched,
                    lastPositionMs = lastPositionMs,
                    totalDurationMs = totalDurationMs,
                    lastUpdated = System.currentTimeMillis()
                )
                videoRepository.saveVideoProgress(progress)

                // Save to Firestore if logged in
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val email = currentUser.email
                    if (!email.isNullOrEmpty()) {
                        val db = FirebaseFirestore.getInstance()
                        val userDocId = email.replace(".", "_")
                        val videoIdEncoded = videoUrl.replace("/", "_")
                            .replace(":", "_")
                            .replace("?", "_")
                            .replace("=", "_")
                            .replace("&", "_")
                        
                        val firestoreProgress = hashMapOf(
                            "videoUrl" to videoUrl,
                            "percentWatched" to percentWatched,
                            "lastPositionMs" to lastPositionMs,
                            "totalDurationMs" to totalDurationMs,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                        
                        db.collection("users").document(userDocId)
                            .collection("progress").document(videoIdEncoded)
                            .set(firestoreProgress)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
