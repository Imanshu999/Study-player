package com.takano3d.studyplayer

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    StudyPlayerApp()
                }
            }
        }
    }
}

@Composable
fun StudyPlayerApp() {
    var isRegistered by remember { mutableStateOf(false) }
    var isAdminMode by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf("Class 10") }
    var showAdminLogin by remember { mutableStateOf(false) }

    if (showAdminLogin) {
        AdminLoginDialog(
            onDismiss = { showAdminLogin = false },
            onLoginSuccess = {
                showAdminLogin = false
                isAdminMode = true
                isRegistered = true
            }
        )
    }

    if (!isRegistered) {
        RegistrationScreen(
            onRegister = { studentClass ->
                selectedClass = studentClass
                isRegistered = true
            },
            onHiddenAdminClick = { showAdminLogin = true }
        )
    } else if (isAdminMode) {
        AdminPanelScreen(onBackToStudent = { isAdminMode = false })
    } else {
        StudentDashboard(selectedClass = selectedClass, onLogout = { isRegistered = false })
    }
}

@Composable
fun RegistrationScreen(onRegister: (String) -> Unit, onHiddenAdminClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var chosenClass by remember { mutableStateOf("Class 10") }
    val classes = (6..12).map { "Class $it" }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 🔐 छिपा हुआ एडमिन ट्रिगर (Top-Left Invisible Box)
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopStart)
                .clickable { onHiddenAdminClick() }
        )

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Study Player Portal", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 30) name = it },
                label = { Text("Full Name") },
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = { if (it.length <= 15) mobile = it },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Your Class:")
            LazyRow {
                items(classes) { cls ->
                    FilterChip(
                        selected = (chosenClass == cls),
                        onClick = { chosenClass = cls },
                        label = { Text(cls) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { onRegister(chosenClass) }) {
                Text("Start Studying")
            }
        }
    }
}

@Composable
fun AdminLoginDialog(onDismiss: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Admin Authorization") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (it.length <= 30) email = it },
                    label = { Text("Admin Email") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (it.length <= 30) password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onLoginSuccess() }) { Text("Login") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StudentDashboard(selectedClass: String, onLogout: () -> Unit) {
    // 🧠 क्लास के हिसाब से डायनेमिक सब्जेक्ट्स की मैपिंग
    val subjects = when (selectedClass) {
        "Class 11", "Class 12" -> listOf("Physics", "Chemistry", "Mathematics", "Economics")
        else -> listOf("Science", "Social Studies", "Mathematics", "English")
    }
    var currentSubject by remember { mutableStateOf(subjects.first()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Study Player v2", style = MaterialTheme.typography.titleLarge)
            SuggestionChip(onClick = onLogout, label = { Text(selectedClass) })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Subjects", style = MaterialTheme.typography.titleMedium)
        
        LazyRow {
            items(subjects) { sub ->
                FilterChip(
                    selected = (currentSubject == sub),
                    onClick = { currentSubject = sub },
                    label = { Text(sub) },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // 📺 वीडियो प्लेयर और लिस्ट का ढांचा यहाँ लोड होगा
        Text("$currentSubject Lectures", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        // डमी प्लेयर का सिमुलेशन (गैलरी MP4 / YouTube WebView Fallback)
        UniversalVideoPlayer(videoUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ")
    }
}

@Composable
fun UniversalVideoPlayer(videoUrl: String) {
    Card(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        if (videoUrl.contains("youtube.com") || videoUrl.contains("embed")) {
            // WebView iframe Fix ताकि "Video Unavailable" एरर न आए
            AndroidView(factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadData("<iframe width=\"100%\" height=\"100%\" src=\"$videoUrl\" frameborder=\"0\" allowfullscreen></iframe>", "text/html", "utf-8")
                }
            }, modifier = Modifier.fillMaxSize())
        } else {
            // MP4/MP3 के लिए ExoPlayer का उपयोग
            AndroidView(factory = { context ->
                StyledPlayerView(context).apply {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(videoUrl))
                        prepare()
                    }
                }
            }, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun AdminPanelScreen(onBackToStudent = () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Admin Control Panel", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))
        Text("यहाँ से आप गैलरी या यूट्यूब का वीडियो अपलोड कर सकते हैं।")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBackToStudent) { Text("Switch to Student View") }
    }
}
