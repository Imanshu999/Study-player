package com.example.ui.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.PlaybackParameters
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.Video
import com.example.ui.StudyPlayerViewModel
import com.example.ui.home.extractYoutubeVideoId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    video: Video,
    viewModel: StudyPlayerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVideoLoading by remember { mutableStateOf(true) }
    var questionText by remember { mutableStateOf("") }
    val videoId = remember(video.videoUrl) { extractYoutubeVideoId(video.videoUrl) }

    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isDataSaverEnabled by remember { mutableStateOf(false) }

    var currentPositionMs by remember { mutableStateOf(0L) }
    var videoDurationMs by remember { mutableStateOf(0L) }
    var seekRequestTime by remember { mutableStateOf<Long?>(null) }

    // Notes and Bookmarks flows collected reactively
    val notesList by viewModel.getNotesForVideo(video.videoUrl).collectAsState(initial = emptyList())
    val bookmarksList by viewModel.getBookmarksForVideo(video.videoUrl).collectAsState(initial = emptyList())
    val existingProgress by viewModel.getProgressForVideoFlow(video.videoUrl).collectAsState(initial = null)

    var hasRestoredProgress by remember { mutableStateOf(false) }

    // Notes state input
    var noteInputText by remember { mutableStateOf("") }
    var attachTimestampToNote by remember { mutableStateOf(true) }

    // Bookmark state input
    var bookmarkLabelInput by remember { mutableStateOf("") }

    // Restore saved play position when video is opened
    LaunchedEffect(existingProgress) {
        if (!hasRestoredProgress && existingProgress != null) {
            val lastPos = existingProgress?.lastPositionMs ?: 0L
            if (lastPos > 1000) { // Only resume if played for more than 1 second
                seekRequestTime = lastPos
            }
            hasRestoredProgress = true
        }
    }

    // Auto save play progress to DB periodically (whenever position updates)
    LaunchedEffect(currentPositionMs, videoDurationMs) {
        if (currentPositionMs > 0 && videoDurationMs > 0) {
            val percent = (currentPositionMs * 100 / videoDurationMs).toInt()
            viewModel.saveVideoProgress(video.videoUrl, percent, currentPositionMs, videoDurationMs)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Viewing Lecture",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Player viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (videoId != null) {
                    // YouTube Video: Use Fallback YouTube OEmbed API Player
                    YouTubeOEmbedPlayer(
                        videoUrl = video.videoUrl,
                        videoId = videoId,
                        isDataSaverEnabled = isDataSaverEnabled,
                        seekToPosition = seekRequestTime,
                        onLoadingChanged = { isVideoLoading = it },
                        modifier = Modifier.fillMaxSize().testTag("active_web_player")
                    )
                } else {
                    // Direct stream or local/remote MP4 URL: Play using native ExoPlayer!
                    ExoVideoPlayer(
                        videoUrl = video.videoUrl,
                        playbackSpeed = playbackSpeed,
                        isDataSaverEnabled = isDataSaverEnabled,
                        seekToPosition = seekRequestTime,
                        onPositionChanged = { pos, dur ->
                            currentPositionMs = pos
                            videoDurationMs = dur
                        },
                        onLoadingChanged = { isVideoLoading = it },
                        modifier = Modifier.fillMaxSize().testTag("active_exoplayer")
                    )
                }

                if (isVideoLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Description and details scrollable section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("player_video_title")
                )

                // Meta Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Class badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Class ${video.classLevel}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Subject Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = video.subject,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Language badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = video.language,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Description card
                Text(
                    text = "Lecture Description",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Player Controls & Data Settings Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("player_settings_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Playback Speed controls
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "प्लेबैक स्पीड (Playback Speed)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
                                speeds.forEach { speed ->
                                    val isSelected = playbackSpeed == speed
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { playbackSpeed = speed },
                                        label = { Text("${speed}x", style = MaterialTheme.typography.labelMedium) },
                                        modifier = Modifier.testTag("speed_chip_$speed")
                                    )
                                }
                            }
                        }

                        // Smart Data Saver controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "स्मार्ट डेटा सेवर (Data Saver Mode)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "कम नेटवर्क इलाकों के लिए लो-क्वालिटी एवं बफरिंग सीमा चालू करें (Optimizes bandwidth in rural zones)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isDataSaverEnabled,
                                onCheckedChange = { isDataSaverEnabled = it },
                                modifier = Modifier.testTag("data_saver_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bookmarks / Timestamps Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("bookmarks_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "बुकमार्क और टाइमस्टैम्प (Bookmarks & Timestamps)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Input fields to add a bookmark
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = bookmarkLabelInput,
                                onValueChange = { bookmarkLabelInput = it },
                                placeholder = { Text("e.g. Important Theorem, Exam tip", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f).testTag("bookmark_input_field"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (bookmarkLabelInput.trim().isNotEmpty()) {
                                        viewModel.addBookmark(
                                            videoUrl = video.videoUrl,
                                            positionMs = currentPositionMs,
                                            label = bookmarkLabelInput.trim()
                                        )
                                        bookmarkLabelInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_bookmark_button")
                            ) {
                                Text("बनाएं", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Play position helper hint
                        if (currentPositionMs > 0) {
                            Text(
                                text = "Current Position: ${formatDuration(currentPositionMs)} - Click 'बनाएं' to bookmark this exact moment.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Bookmarks list
                        if (bookmarksList.isEmpty()) {
                            Text(
                                text = "कोई बुकमार्क नहीं है। इस वीडियो में महत्वपूर्ण पल बुकमार्क करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                bookmarksList.forEach { bookmark ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                            .clickable {
                                                seekRequestTime = bookmark.positionMs
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Seek to bookmark",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "[${formatDuration(bookmark.positionMs)}] ${bookmark.label}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteBookmarkById(bookmark.id)
                                            },
                                            modifier = Modifier.size(24.dp).testTag("delete_bookmark_${bookmark.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete bookmark",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Video Notes Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("video_notes_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "इन-ऐप लेक्चर नोट्स (In-App Study Notes)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = noteInputText,
                            onValueChange = { noteInputText = it },
                            placeholder = { Text("वीडियो देखते समय महत्वपूर्ण पॉइंट्स यहां लिखें...", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth().testTag("note_input_field"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 4,
                            singleLine = false
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { attachTimestampToNote = !attachTimestampToNote }
                            ) {
                                Switch(
                                    checked = attachTimestampToNote,
                                    onCheckedChange = { attachTimestampToNote = it },
                                    modifier = Modifier.testTag("attach_timestamp_switch")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "समय जोड़ें (${formatDuration(currentPositionMs)})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    if (noteInputText.trim().isNotEmpty()) {
                                        viewModel.addNote(
                                            videoUrl = video.videoUrl,
                                            noteText = noteInputText.trim(),
                                            timestampMs = if (attachTimestampToNote) currentPositionMs else -1L
                                        )
                                        noteInputText = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                enabled = noteInputText.trim().isNotEmpty(),
                                modifier = Modifier.testTag("save_note_button")
                            ) {
                                Text("नोट सहेजें", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Notes list
                        if (notesList.isEmpty()) {
                            Text(
                                text = "अभी कोई नोट सहेजा नहीं गया है।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                notesList.forEach { note ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            if (note.timestampMs >= 0) {
                                                AssistChip(
                                                    onClick = {
                                                        seekRequestTime = note.timestampMs
                                                    },
                                                    label = { Text(formatDuration(note.timestampMs)) },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Play from timestamp",
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("seek_note_${note.id}")
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(1.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteNoteById(note.id)
                                                },
                                                modifier = Modifier.size(24.dp).testTag("delete_note_${note.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete note",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = note.noteText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini AI helper card
                Text(
                    text = "Gemini AI Study Helper",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("gemini_helper_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Need help understanding this lecture? Ask our advanced Gemini AI Study Assistant for explanations, concept breakdowns, or quick Q&A.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val geminiResponse by viewModel.geminiResponse.collectAsState()
                        val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()

                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            placeholder = { Text("Ask a question about this video...", style = MaterialTheme.typography.bodyMedium) },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("gemini_question_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGeminiLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp).testTag("gemini_loading_indicator")
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Consulting Gemini...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (questionText.trim().isNotEmpty()) {
                                            viewModel.askGemini(video.title, video.description, questionText)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = questionText.trim().isNotEmpty(),
                                    modifier = Modifier.testTag("ask_gemini_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ask AI")
                                }
                            }
                        }

                        geminiResponse?.let { response ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gemini Response:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    .padding(12.dp)
                                    .testTag("gemini_response_text")
                            ) {
                                Text(
                                    text = response,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Resilient fallback info block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This premium player natively leverages ExoPlayer core engines for direct video links, and seamlessly leverages YouTube's OEmbed API fallback layer for secure YouTube resource playback.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExoVideoPlayer(
    videoUrl: String,
    playbackSpeed: Float,
    isDataSaverEnabled: Boolean,
    seekToPosition: Long?,
    onPositionChanged: (Long, Long) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(isDataSaverEnabled) {
        val playerBuilder = ExoPlayer.Builder(context)
        if (isDataSaverEnabled) {
            val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    10000, // minBufferMs (10 seconds)
                    20000, // maxBufferMs (20 seconds)
                    2000,  // bufferForPlaybackMs
                    3000   // bufferForPlaybackAfterRebufferMs
                ).build()
            playerBuilder.setLoadControl(loadControl)
        }
        playerBuilder.build().apply {
            playWhenReady = true
        }
    }

    // Apply speed changes dynamically
    LaunchedEffect(playbackSpeed) {
        exoPlayer.setPlaybackSpeed(playbackSpeed)
    }

    // Apply seek requests dynamically
    LaunchedEffect(seekToPosition) {
        seekToPosition?.let { pos ->
            exoPlayer.seekTo(pos)
        }
    }

    // Update current position and duration periodically
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (dur > 0) {
                onPositionChanged(pos, dur)
            }
        }
    }

    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    androidx.media3.common.Player.STATE_BUFFERING -> onLoadingChanged(true)
                    androidx.media3.common.Player.STATE_READY -> onLoadingChanged(false)
                    else -> onLoadingChanged(false)
                }
            }
        })
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeOEmbedPlayer(
    videoUrl: String,
    videoId: String,
    isDataSaverEnabled: Boolean,
    seekToPosition: Long?,
    onLoadingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var oEmbedHtml by remember { mutableStateOf<String?>(null) }
    var currentSeekPosition by remember { mutableStateOf(0L) }

    LaunchedEffect(seekToPosition) {
        if (seekToPosition != null) {
            currentSeekPosition = seekToPosition
        }
    }

    LaunchedEffect(videoId, isDataSaverEnabled, currentSeekPosition) {
        onLoadingChanged(true)
        withContext(Dispatchers.IO) {
            try {
                val qualityParam = if (isDataSaverEnabled) "&vq=small" else ""
                val startParam = if (currentSeekPosition > 0) "&start=${currentSeekPosition / 1000}" else ""

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId$startParam$qualityParam&format=json")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val html = json.optString("html")
                            if (html.isNotEmpty()) {
                                val styledHtml = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                        <style>
                                            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                                            iframe { width: 100% !important; height: 100% !important; border: none; }
                                        </style>
                                    </head>
                                    <body>
                                        $html
                                    </body>
                                    </html>
                                """.trimIndent()
                                oEmbedHtml = styledHtml
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    onLoadingChanged(false)
                }
            }
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                    }
                }
            }
        },
        update = { webView ->
            val html = oEmbedHtml
            val qualityParam = if (isDataSaverEnabled) "&vq=small" else ""
            val startParam = if (currentSeekPosition > 0) "&start=${currentSeekPosition / 1000}" else ""

            if (html != null) {
                val htmlWithStart = if (currentSeekPosition > 0 && !html.contains("start=")) {
                    html.replace("?feature=oembed", "?feature=oembed$startParam$qualityParam")
                        .replace("&feature=oembed", "&feature=oembed$startParam$qualityParam")
                } else {
                    html
                }
                webView.loadDataWithBaseURL("https://www.youtube.com", htmlWithStart, "text/html", "UTF-8", null)
            } else {
                val fallbackHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                        <style>
                            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; overflow: hidden; }
                            #player { width: 100%; height: 100%; border: none; }
                        </style>
                    </head>
                    <body>
                        <iframe id="player" src="https://www.youtube.com/embed/$videoId?autoplay=1&mute=0&controls=1&fs=1$startParam$qualityParam" allow="autoplay; fullscreen" allowfullscreen></iframe>
                    </body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL("https://www.youtube.com", fallbackHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}

fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return if (hours > 0) {
        val hStr = hours.toString().padStart(2, '0')
        val mStr = minutes.toString().padStart(2, '0')
        val sStr = seconds.toString().padStart(2, '0')
        "$hStr:$mStr:$sStr"
    } else {
        val mStr = minutes.toString().padStart(2, '0')
        val sStr = seconds.toString().padStart(2, '0')
        "$mStr:$sStr"
    }
}
