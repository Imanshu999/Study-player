package com.example.ui.registration

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyPlayerViewModel

@Composable
fun RegistrationScreen(
    viewModel: StudyPlayerViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Login, 1 = Register

    // Auth States from ViewModel
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    // Login Fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register Fields
    var classLevel by remember { mutableStateOf(10) }
    var gender by remember { mutableStateOf("Male") }
    var mobileNumber by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var showRegisterPassword by remember { mutableStateOf(false) }

    // Dropdowns
    var isClassDropdownExpanded by remember { mutableStateOf(false) }
    var isGenderDropdownExpanded by remember { mutableStateOf(false) }

    // Errors
    var mobileError by remember { mutableStateOf<String?>(null) }
    var registerEmailError by remember { mutableStateOf<String?>(null) }
    var registerPasswordError by remember { mutableStateOf<String?>(null) }
    var loginEmailError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }

    // Admin States
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminEmailError by remember { mutableStateOf<String?>(null) }
    var adminPasswordError by remember { mutableStateOf<String?>(null) }
    var showAdminPassword by remember { mutableStateOf(false) }

    val adminAuthLoading by viewModel.adminAuthLoading.collectAsState()
    val adminAuthError by viewModel.adminAuthError.collectAsState()

    // Password Reset State
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            onAuthSuccess()
        }
    }

    val classes = (6..12).toList()
    val genders = listOf("Male", "Female", "Other", "Prefer not to say")

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        // Hidden invisible, borderless clickable box (Modifier.size(48.dp)) acting as a hidden Admin trigger
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(48.dp)
                .clickable {
                    showAdminLoginDialog = true
                }
                .testTag("admin_trigger_box")
        )
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Large Header Icon and Title
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "App Logo Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Study Player Portal",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sign in or register to load personalized learning modules, syllabus, and track video lectures.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("registration_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column {
                        // Navigation Tab Bar for Login vs Register
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(48.dp).testTag("tab_login")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Register", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(48.dp).testTag("tab_register")
                            )
                        }

                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (authError != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = authError ?: "",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // ----------------- SIGN IN TAB -----------------
                            if (selectedTab == 0) {
                                Text(
                                    text = "Welcome Back",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                 // Email Input
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = {
                                        if (it.length <= 30) {
                                            loginEmail = it.trim()
                                            loginEmailError = if (android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) null else "Enter a valid email address"
                                        }
                                    },
                                    label = { Text("Email Address") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    isError = loginEmailError != null,
                                    supportingText = {
                                        if (loginEmailError != null) {
                                            Text(text = loginEmailError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                // Password Input
                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            loginPassword = it
                                            loginPasswordError = if (loginPassword.length >= 6) null else "Password must be at least 6 characters"
                                        }
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                            Icon(
                                                imageVector = if (showLoginPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = loginPasswordError != null,
                                    supportingText = {
                                        if (loginPasswordError != null) {
                                            Text(text = loginPasswordError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showResetDialog = true }) {
                                        Text("Forgot Password?", style = MaterialTheme.typography.labelLarge)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()
                                        val isPasswordValid = loginPassword.length >= 6

                                        if (!isEmailValid) loginEmailError = "Enter a valid email address"
                                        if (!isPasswordValid) loginPasswordError = "Password must be at least 6 characters"

                                        if (isEmailValid && isPasswordValid) {
                                            viewModel.loginStudent(loginEmail, loginPassword,
                                                onSuccess = {
                                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    enabled = !authLoading,
                                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("login_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (authLoading) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Sign In", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }

                            // ----------------- REGISTER TAB -----------------
                            if (selectedTab == 1) {
                                Text(
                                    text = "Create Profile",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                // Class Dropdown Selection
                                Column {
                                    Text(
                                        text = "Select Student Class",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { isClassDropdownExpanded = true }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                            .testTag("class_dropdown_trigger")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Class $classLevel",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Expand class dropdown",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = isClassDropdownExpanded,
                                            onDismissRequest = { isClassDropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.8f)
                                        ) {
                                            classes.forEach { num ->
                                                DropdownMenuItem(
                                                    text = { Text("Class $num", style = MaterialTheme.typography.bodyLarge) },
                                                    onClick = {
                                                        classLevel = num
                                                        isClassDropdownExpanded = false
                                                    },
                                                    modifier = Modifier.testTag("class_option_$num")
                                                )
                                            }
                                        }
                                    }
                                }

                                // Gender Selection Dropdown
                                Column {
                                    Text(
                                        text = "Gender",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { isGenderDropdownExpanded = true }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                            .testTag("gender_dropdown_trigger")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Transgender,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.size(8.dp))
                                                Text(
                                                    text = gender,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Expand gender dropdown",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = isGenderDropdownExpanded,
                                            onDismissRequest = { isGenderDropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.8f)
                                        ) {
                                            genders.forEach { gen ->
                                                DropdownMenuItem(
                                                    text = { Text(gen, style = MaterialTheme.typography.bodyLarge) },
                                                    onClick = {
                                                        gender = gen
                                                        isGenderDropdownExpanded = false
                                                    },
                                                    modifier = Modifier.testTag("gender_option_$gen")
                                                )
                                            }
                                        }
                                    }
                                }

                                // Mobile Number
                                OutlinedTextField(
                                    value = mobileNumber,
                                    onValueChange = {
                                        if (it.length <= 30) {
                                            mobileNumber = it.filter { char -> char.isDigit() }
                                            mobileError = if (mobileNumber.length in 8..15) null else "Enter a valid mobile number (8-15 digits)"
                                        }
                                    },
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("e.g. 9876543210") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    isError = mobileError != null,
                                    supportingText = {
                                        if (mobileError != null) {
                                            Text(text = mobileError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("mobile_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                // Email
                                OutlinedTextField(
                                    value = registerEmail,
                                    onValueChange = {
                                        if (it.length <= 30) {
                                            registerEmail = it.trim()
                                            registerEmailError = if (android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail).matches()) null else "Enter a valid email address"
                                        }
                                    },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("e.g. student@example.com") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    isError = registerEmailError != null,
                                    supportingText = {
                                        if (registerEmailError != null) {
                                            Text(text = registerEmailError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                // Password
                                OutlinedTextField(
                                    value = registerPassword,
                                    onValueChange = {
                                        if (it.length <= 50) {
                                            registerPassword = it
                                            registerPasswordError = if (registerPassword.length >= 6) null else "Password must be at least 6 characters"
                                        }
                                    },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showRegisterPassword = !showRegisterPassword }) {
                                            Icon(
                                                imageVector = if (showRegisterPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (showRegisterPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = registerPasswordError != null,
                                    supportingText = {
                                        if (registerPasswordError != null) {
                                            Text(text = registerPasswordError!!, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        val isMobileValid = mobileNumber.length in 8..15
                                        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail).matches()
                                        val isPasswordValid = registerPassword.length >= 6

                                        if (!isMobileValid) mobileError = "Enter a valid mobile number (8-15 digits)"
                                        if (!isEmailValid) registerEmailError = "Enter a valid email address"
                                        if (!isPasswordValid) registerPasswordError = "Password must be at least 6 characters"

                                        if (isMobileValid && isEmailValid && isPasswordValid) {
                                            viewModel.registerStudent(
                                                email = registerEmail,
                                                password = registerPassword,
                                                classLevel = classLevel,
                                                gender = gender,
                                                mobile = mobileNumber,
                                                onSuccess = {
                                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    enabled = !authLoading,
                                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("register_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (authLoading) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Start Studying", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Password Reset Dialog Dialog Box
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your email address below. We will send you a secure link to reset your account password.")
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it.trim() },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                            viewModel.resetPassword(resetEmail,
                                onSuccess = {
                                    Toast.makeText(context, "Password reset email sent successfully!", Toast.LENGTH_LONG).show()
                                    showResetDialog = false
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin Login Dialog
    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = { showAdminLoginDialog = false },
            title = { Text("Secure Admin Access", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This is a restricted portal for administrators. Enter authorized admin credentials to proceed.")
                    
                    if (adminAuthError != null) {
                        Text(
                            text = adminAuthError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = {
                            if (it.length <= 30) {
                                adminEmail = it.trim()
                                adminEmailError = if (android.util.Patterns.EMAIL_ADDRESS.matcher(adminEmail).matches()) null else "Enter a valid email address"
                            }
                        },
                        label = { Text("Admin Email") },
                        isError = adminEmailError != null,
                        supportingText = {
                            if (adminEmailError != null) {
                                Text(text = adminEmailError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = {
                            if (it.length <= 50) {
                                adminPassword = it
                                adminPasswordError = if (adminPassword.length >= 6) null else "Password must be at least 6 characters"
                            }
                        },
                        label = { Text("Admin Password") },
                        isError = adminPasswordError != null,
                        supportingText = {
                            if (adminPasswordError != null) {
                                Text(text = adminPasswordError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showAdminPassword = !showAdminPassword }) {
                                Icon(
                                    imageVector = if (showAdminPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (showAdminPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(adminEmail).matches()
                        val isPasswordValid = adminPassword.length >= 6

                        if (!isEmailValid) adminEmailError = "Enter a valid email address"
                        if (!isPasswordValid) adminPasswordError = "Password must be at least 6 characters"

                        if (isEmailValid && isPasswordValid) {
                            viewModel.authenticateAdmin(adminEmail, adminPassword,
                                onSuccess = {
                                    Toast.makeText(context, "Admin Access Granted", Toast.LENGTH_SHORT).show()
                                    showAdminLoginDialog = false
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !adminAuthLoading,
                    modifier = Modifier.testTag("admin_login_submit_button")
                ) {
                    if (adminAuthLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify & Enter")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
