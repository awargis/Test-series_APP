package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPortalView(viewModel: TestViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentStudent by viewModel.currentStudent.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (currentScreen != "register" && currentScreen != "test_solve") {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                "home" -> "Dashboard"
                                "tests" -> "Test Library"
                                "test_results" -> "Exam Analytics"
                                "teacher_panel" -> "Teacher Monitoring"
                                "admin_panel" -> "Admin Test Builder"
                                else -> "Portal"
                            },
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    actions = {
                        if (currentStudent != null) {
                            Text(
                                text = "Hi, ${currentStudent?.name?.split(" ")?.firstOrNull() ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Log Out")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != "register" && currentScreen != "test_solve") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { viewModel.navigateTo("home") },
                        label = { Text("Home") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "tests",
                        onClick = { viewModel.navigateTo("tests") },
                        label = { Text("Tests") },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Test Series") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "teacher_panel",
                        onClick = { viewModel.navigateTo("teacher_panel") },
                        label = { Text("Monitor") },
                        icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "Teacher Mode") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "admin_panel",
                        onClick = { viewModel.navigateTo("admin_panel") },
                        label = { Text("Admin") },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Admin Builder") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "register" -> RegisterScreen(viewModel)
                "home" -> HomeScreen(viewModel)
                "tests" -> CoachingTestsScreen(viewModel)
                "test_solve" -> TestSolveScreen(viewModel)
                "test_results" -> TestResultsScreen(viewModel)
                "teacher_panel" -> TeacherMonitorPanel(viewModel)
                "admin_panel" -> AdminTestBuilder(viewModel)
            }
        }
    }

    // Auto navigate to registration if student isn't registered
    LaunchedEffect(currentStudent) {
        if (currentStudent == null && currentScreen != "register") {
            viewModel.navigateTo("register")
        }
    }
}

@Composable
fun RegisterScreen(viewModel: TestViewModel) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedCoaching by remember { mutableStateOf("PW") }
    var nameError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }

    val coachings = listOf("PW", "Allen", "FIITJEE", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High-fidelity vector layout header
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "JEE Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = "JEE Test Portal & OMR Pro",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Empowering students with realistic brain-training feedback & immediate coaching OMR evaluation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Student Registration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Full Name (Mandatory)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input")
                )
                if (nameError) {
                    Text("Name is mandatory to proceed.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        // Numeric filter
                        if (it.all { char -> char.isDigit() }) {
                            mobile = it
                            mobileError = false
                        }
                    },
                    label = { Text("Mobile Number (Mandatory)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Mobile") },
                    isError = mobileError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mobile_input")
                )
                if (mobileError) {
                    Text("Please enter a valid mandatory 10-digit mobile number.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                // Coaching Selector dropdown/row
                Column {
                    Text(
                        text = "Select Enrolled Coaching",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        coachings.forEach { coaching ->
                            val selected = selectedCoaching == coaching
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCoaching = coaching },
                                label = { Text(coaching) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val hasNameError = name.isBlank()
                val hasMobileError = mobile.length < 10
                nameError = hasNameError
                mobileError = hasMobileError

                if (!hasNameError && !hasMobileError) {
                    viewModel.loginOrRegister(name, mobile, selectedCoaching)
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_button")
        ) {
            Text("Enter Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}

@Composable
fun HomeScreen(viewModel: TestViewModel) {
    val student by viewModel.currentStudent.collectAsStateWithLifecycle()
    val attempts by viewModel.studentAttempts.collectAsStateWithLifecycle()
    val testPapers by viewModel.filteredTestPapers.collectAsStateWithLifecycle()

    val totalTestsTaken = attempts.filter { it.isSubmitted }.size
    val averageAccuracy = if (totalTestsTaken > 0) {
        attempts.filter { it.isSubmitted }.map { it.accuracy }.average().toFloat()
    } else 0f
    val averageScorePercent = if (totalTestsTaken > 0) {
        attempts.filter { it.isSubmitted }.map { it.score.toFloat() / max(1, it.maxScore) }.average().toFloat()
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Student Welcome Dashboard
            student?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Enrolled: ${it.enrolledCoaching}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }

        // Animated Brain section with dynamic values
        item {
            BrainAnimation(
                testCount = totalTestsTaken,
                averageAccuracy = averageAccuracy,
                averageScorePercent = averageScorePercent
            )
        }

        // Stats strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Attempts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalTestsTaken", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Avg Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(averageAccuracy * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Mean Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(averageScorePercent * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }

        // Quick Recommended Tests Title
        item {
            Text(
                text = "Recommended Test Series",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        if (testPapers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "No tests recommended at the moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(testPapers.take(3)) { test ->
                val hasAttempt = attempts.find { it.testId == test.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { viewModel.startTest(test) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (test.coaching.uppercase()) {
                                        "PW" -> Color(0xFFE8F5E9)
                                        "ALLEN" -> Color(0xFFE3F2FD)
                                        else -> Color(0xFFFFF3E0)
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = test.coaching,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when (test.coaching.uppercase()) {
                                            "PW" -> Color(0xFF2E7D32)
                                            "ALLEN" -> Color(0xFF1565C0)
                                            else -> Color(0xFFEF6C00)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = test.testType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = test.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "⏱️ ${test.durationMinutes} mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "❓ ${test.totalQuestions} Questions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Right CTA Status
                        if (hasAttempt != null && hasAttempt.isSubmitted) {
                            IconButton(onClick = { viewModel.viewAttemptResult(hasAttempt) }) {
                                Icon(Icons.Default.Assessment, contentDescription = "Results", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            }
                        } else {
                            IconButton(onClick = { viewModel.startTest(test) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start Test", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CoachingTestsScreen(viewModel: TestViewModel) {
    val selectedCoaching by viewModel.selectedCoaching.collectAsStateWithLifecycle()
    val testPapers by viewModel.filteredTestPapers.collectAsStateWithLifecycle()
    val attempts by viewModel.studentAttempts.collectAsStateWithLifecycle()

    val coachings = listOf("All", "PW", "Allen", "FIITJEE")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Enrolled Test Series Library",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = coachings.indexOf(selectedCoaching).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            coachings.forEach { coaching ->
                Tab(
                    selected = selectedCoaching == coaching,
                    onClick = { viewModel.selectCoaching(coaching) },
                    text = { Text(coaching, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (testPapers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MenuBook, contentDescription = "No test", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No tests found for this filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(testPapers) { test ->
                    val hasAttempt = attempts.find { it.testId == test.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.startTest(test) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (test.coaching.uppercase()) {
                                            "PW" -> Color(0xFFE8F5E9)
                                            "ALLEN" -> Color(0xFFE3F2FD)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    ) {
                                        Text(
                                            text = test.coaching,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (test.coaching.uppercase()) {
                                                "PW" -> Color(0xFF2E7D32)
                                                "ALLEN" -> Color(0xFF1565C0)
                                                else -> Color(0xFFEF6C00)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (test.testType == "MAINS") "JEE Mains Format" else "JEE Advanced Format",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (hasAttempt != null && hasAttempt.isSubmitted) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Submitted", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Completed", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = test.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${test.totalQuestions}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Max Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${test.maxMarks}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Time Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${test.durationMinutes}m", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (hasAttempt != null && hasAttempt.isSubmitted) {
                                    Button(
                                        onClick = { viewModel.viewAttemptResult(hasAttempt) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("View Score")
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.startTest(test) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Start OMR")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestSolveScreen(viewModel: TestViewModel) {
    val testPaper by viewModel.activeTest.collectAsStateWithLifecycle()
    val answers by viewModel.activeAnswers.collectAsStateWithLifecycle()
    val timeRemaining by viewModel.testTimeRemaining.collectAsStateWithLifecycle()

    val test = testPaper ?: return

    val questionConfigs = remember(test) {
        JsonParser.fromJsonQuestionConfigs(test.questionStructureJson)
    }

    // Split questions by subject
    val physicsQuestions = remember(questionConfigs) { questionConfigs.filter { it.subject == "Physics" } }
    val chemistryQuestions = remember(questionConfigs) { questionConfigs.filter { it.subject == "Chemistry" } }
    val mathQuestions = remember(questionConfigs) { questionConfigs.filter { it.subject == "Mathematics" } }

    var selectedTab by remember { mutableStateOf(0) } // 0: Physics, 1: Chemistry, 2: Maths
    val activeSubjectQuestions = when (selectedTab) {
        0 -> physicsQuestions
        1 -> chemistryQuestions
        else -> mathQuestions
    }

    var showSubmitDialog by remember { mutableStateOf(false) }

    // Format timer
    val hours = timeRemaining / 3600
    val minutes = (timeRemaining % 3600) / 60
    val seconds = timeRemaining % 60
    val timerString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = test.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (test.testType == "MAINS") "JEE Mains Simulation" else "JEE Advanced Simulation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Timer badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (timeRemaining < 300) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (timeRemaining < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timerString,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (timeRemaining < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSubmitDialog = true },
                icon = { Icon(Icons.Default.Check, contentDescription = "Submit") },
                text = { Text("Submit Exam") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("submit_exam_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subject Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Physics (${physicsQuestions.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Chemistry (${chemistryQuestions.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Mathematics (${mathQuestions.size})") })
            }

            // Dual pane implementation: Top simulated question, Bottom interactive OMR
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Simulated Question paper panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Paper context", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulated Exam Booklet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sitting at home? Open your coaching paper PDF. Mark your corresponding answers below on this digital OMR sheet. This scanner automatically registers and evaluates your response using the pre-programmed backend answers keys.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. OMR Bubble Sheet Grid
                Text(
                    text = "Interactive OMR Grid",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 80.dp)
                ) {
                    activeSubjectQuestions.forEach { question ->
                        val currentAnswer = answers[question.qNo] ?: ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Question indicator header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${question.qNo}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = question.chapter,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Type Tag
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ) {
                                        Text(
                                            text = when (question.type) {
                                                QuestionType.SINGLE_CORRECT -> "Single Choice"
                                                QuestionType.MULTI_CORRECT -> "Multi Correct"
                                                QuestionType.NUMERICAL_INTEGER -> "Numeric Value"
                                                QuestionType.PASSAGE -> "Passage Q"
                                                QuestionType.MATCHING -> "Matching"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Render different OMR widgets based on question type
                                when (question.type) {
                                    QuestionType.SINGLE_CORRECT, QuestionType.PASSAGE -> {
                                        // A B C D Circular bubble row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            listOf("A", "B", "C", "D").forEach { option ->
                                                val isSelected = currentAnswer == option
                                                Surface(
                                                    shape = CircleShape,
                                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clickable {
                                                            val newVal = if (isSelected) "" else option
                                                            viewModel.saveAnswerChoice(question.qNo, newVal)
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = option,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    QuestionType.MULTI_CORRECT -> {
                                        // A B C D Square check bubble row
                                        val activeOptions = remember(currentAnswer) {
                                            if (currentAnswer.isBlank()) emptySet() else currentAnswer.split(",").toSet()
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            listOf("A", "B", "C", "D").forEach { option ->
                                                val isSelected = activeOptions.contains(option)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline),
                                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clickable {
                                                            val newSet = activeOptions.toMutableSet()
                                                            if (isSelected) newSet.remove(option) else newSet.add(option)
                                                            viewModel.saveAnswerChoice(question.qNo, newSet.sorted().joinToString(","))
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = option,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    QuestionType.NUMERICAL_INTEGER -> {
                                        // Numeric value input
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = currentAnswer,
                                                onValueChange = {
                                                    if (it.isEmpty() || it == "-" || it.all { char -> char.isDigit() || char == '-' }) {
                                                        viewModel.saveAnswerChoice(question.qNo, it)
                                                    }
                                                },
                                                label = { Text("Enter Numeric Answer") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // Reset
                                            IconButton(onClick = { viewModel.saveAnswerChoice(question.qNo, "") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    }

                                    QuestionType.MATCHING -> {
                                        // Matching selector row grid
                                        val matches = remember(currentAnswer) {
                                            if (currentAnswer.isBlank()) emptyMap() else {
                                                currentAnswer.split(",").associate {
                                                    val parts = it.split("-")
                                                    parts[0] to parts[1]
                                                }
                                            }
                                        }
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf("1", "2", "3", "4").forEach { num ->
                                                val selectedLetter = matches[num] ?: ""
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = "Row $num  ➜ ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        listOf("P", "Q", "R", "S").forEach { letter ->
                                                            val isSelected = selectedLetter == letter
                                                            Surface(
                                                                shape = CircleShape,
                                                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline),
                                                                color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clickable {
                                                                        val newMap = matches.toMutableMap()
                                                                        if (isSelected) newMap.remove(num) else newMap[num] = letter
                                                                        viewModel.saveAnswerChoice(
                                                                            question.qNo,
                                                                            newMap.map { "${it.key}-${it.value}" }.sorted().joinToString(",")
                                                                        )
                                                                    }
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Text(
                                                                        text = letter,
                                                                        style = MaterialTheme.typography.labelMedium,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Exam Paper?") },
            text = { Text("Are you sure you want to finalize and submit? Remember, once submitted, you can NOT resubmit. You will only be able to view details & reports of this test.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitTest()
                    },
                    modifier = Modifier.testTag("confirm_submit_button")
                ) {
                    Text("Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TestResultsScreen(viewModel: TestViewModel) {
    val attemptState by viewModel.viewedAttempt.collectAsStateWithLifecycle()
    val testPaper by viewModel.activeTest.collectAsStateWithLifecycle()

    val attempt = attemptState ?: return
    val test = testPaper ?: return

    val correctAnswers = remember(test) { JsonParser.fromJsonAnswerKeys(test.answerKeysJson) }
    val studentAnswers = remember(attempt) { JsonParser.fromJsonAnswerKeys(attempt.selectedAnswersJson) }
    val questionConfigs = remember(test) { JsonParser.fromJsonQuestionConfigs(test.questionStructureJson) }
    val subjectScores = remember(attempt) { JsonParser.fromJsonMap(attempt.subjectScoresJson) }
    val weakChapters = remember(attempt) { JsonParser.fromJsonStringList(attempt.weakChaptersJson) }
    val strongChapters = remember(attempt) { JsonParser.fromJsonStringList(attempt.strongChaptersJson) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("tests") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Performance Report",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Score Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${attempt.score} / ${attempt.maxScore}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${(attempt.accuracy * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress improvement text depending on score
                val message = when {
                    attempt.score.toFloat() / attempt.maxScore >= 0.8f -> "Outstanding performance! You are on track for JEE top rank."
                    attempt.score.toFloat() / attempt.maxScore >= 0.5f -> "Good work! Keep resolving weak chapters to cross the cutoff."
                    else -> "Keep practicing! Review your errors in the matrix below to build confidence."
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // 1. Comparative Subject scores Drawn programmatically on Canvas
        Text(
            text = "Subject-Wise Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Programmatic canvas chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val subjects = listOf("Physics", "Chemistry", "Mathematics")
                    val subjectColors = listOf(Color(0xFF64B5F6), Color(0xFF81C784), Color(0xFFBA68C8))

                    val maxSubjScore = max(1, subjectScores.values.maxOrNull() ?: 10)

                    val barWidth = 40.dp.toPx()
                    val spacing = (w - (barWidth * 3)) / 4

                    subjects.forEachIndexed { index, subject ->
                        val score = subjectScores[subject] ?: 0
                        val barHeight = max(5f, (score.toFloat() / maxSubjScore) * (h - 40f))

                        val x = spacing + index * (barWidth + spacing)
                        val y = h - barHeight - 20f

                        // Draw rounded bars
                        drawRoundRect(
                            color = subjectColors[index],
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val subjects = listOf("Physics", "Chemistry", "Mathematics")
                    val subjectColors = listOf(Color(0xFF64B5F6), Color(0xFF81C784), Color(0xFFBA68C8))

                    subjects.forEachIndexed { index, s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(subjectColors[index])
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$s (${subjectScores[s] ?: 0}m)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Cognitive Chapter Insights (Weak vs Strong)
        Text(
            text = "Cognitive Learning Insights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Strong", tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Strong Areas", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (strongChapters.isEmpty()) {
                        Text("Attempt more questions to categorize.", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                    } else {
                        strongChapters.forEach { chapter ->
                            Text("• $chapter", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingDown, contentDescription = "Weak", tint = Color(0xFFC62828))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Weak Areas", fontWeight = FontWeight.Bold, color = Color(0xFFC62828), style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (weakChapters.isEmpty()) {
                        Text("Great! No critical weak chapters identified.", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                    } else {
                        weakChapters.forEach { chapter ->
                            Text("• $chapter", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 3. Complete OMR matrix detailed matching (student answer vs key)
        Text(
            text = "OMR Response Evaluation Matrix",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Q.No", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("Your Bubble", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2.5f), textAlign = TextAlign.Center)
                    Text("Answer Key", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2.5f), textAlign = TextAlign.Center)
                    Text("Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                questionConfigs.forEach { config ->
                    val qNo = config.qNo
                    val studentChoice = studentAnswers[qNo] ?: ""
                    val correctChoice = correctAnswers[qNo] ?: ""

                    val isCorrect = studentChoice.isNotBlank() && when (config.type) {
                        QuestionType.SINGLE_CORRECT, QuestionType.PASSAGE, QuestionType.NUMERICAL_INTEGER -> {
                            studentChoice.trim().lowercase() == correctChoice.trim().lowercase()
                        }
                        QuestionType.MULTI_CORRECT, QuestionType.MATCHING -> {
                            val studentSet = studentChoice.split(",").map { it.trim().uppercase() }.toSet()
                            val correctSet = correctChoice.split(",").map { it.trim().uppercase() }.toSet()
                            studentSet == correctSet
                        }
                    }

                    val isUnattempted = studentChoice.isBlank()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$qNo (${config.subject.first()})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))

                        // Student Choice Bubble
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnattempted) {
                                Text("-", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                ) {
                                    Text(
                                        text = studentChoice,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Correct Choice Bubble
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = correctChoice,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Icon status
                        Box(
                            modifier = Modifier.weight(1.5f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (isUnattempted) {
                                Text("Unsaved", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            } else if (isCorrect) {
                                Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Clear, contentDescription = "Wrong", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMonitorPanel(viewModel: TestViewModel) {
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val allAttempts by viewModel.allAttempts.collectAsStateWithLifecycle()

    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Teacher Progress Center",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Monitor learning improvements, weak chapters, & OMR accuracy rates across coaching centers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No registered students to display.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(students) { student ->
                    val studAttempts = allAttempts.filter { it.studentMobile == student.mobile && it.isSubmitted }
                    val avgAccuracy = if (studAttempts.isNotEmpty()) studAttempts.map { it.accuracy }.average().toFloat() else 0f
                    val totalScore = if (studAttempts.isNotEmpty()) studAttempts.sumOf { it.score } else 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStudent = student },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mobile: ${student.mobile}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = student.enrolledCoaching,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Tests: ${studAttempts.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Acc: ${(avgAccuracy * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail student view sheet Dialog
    if (selectedStudent != null) {
        val stud = selectedStudent!!
        val studAttempts = allAttempts.filter { it.studentMobile == stud.mobile && it.isSubmitted }
        val avgAccuracy = if (studAttempts.isNotEmpty()) studAttempts.map { it.accuracy }.average().toFloat() else 0f

        AlertDialog(
            onDismissRequest = { selectedStudent = null },
            title = {
                Text(
                    text = "${stud.name}'s Analytics",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Coaching Center: ${stud.enrolledCoaching}", fontWeight = FontWeight.Bold)
                            Text("Mobile: ${stud.mobile}")
                            Text("Registered: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(stud.registrationTime))}")
                        }
                    }

                    Divider()

                    Text("Performance Tracker", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Exams Given", style = MaterialTheme.typography.labelSmall)
                            Text("${studAttempts.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("OMR Accuracy", style = MaterialTheme.typography.labelSmall)
                            Text("${(avgAccuracy * 100).toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Divider()

                    Text("Completed Exams & Marks:", fontWeight = FontWeight.Bold)

                    if (studAttempts.isEmpty()) {
                        Text("No tests submitted yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        studAttempts.forEach { attempt ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• Test: ${attempt.testId.split("_").lastOrNull() ?: attempt.testId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${attempt.score}m  |  ${(attempt.accuracy * 100).toInt()}% acc",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedStudent = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdminTestBuilder(viewModel: TestViewModel) {
    val adminTitle by viewModel.adminTitle.collectAsStateWithLifecycle()
    val adminCoaching by viewModel.adminCoaching.collectAsStateWithLifecycle()
    val adminType by viewModel.adminType.collectAsStateWithLifecycle()
    val adminQuestionCount by viewModel.adminQuestionCount.collectAsStateWithLifecycle()
    val adminAnswerKeyText by viewModel.adminAnswerKeyText.collectAsStateWithLifecycle()

    val coachings = listOf("PW", "Allen", "FIITJEE", "Other")
    val types = listOf("MAINS", "ADVANCED")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Create Customizable Exam Paper",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "As a teacher, design custom OMR worksheets by setting up coaching-specific keys.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = adminTitle,
                    onValueChange = { viewModel.adminTitle.value = it },
                    label = { Text("Test Title (e.g. PW Practice Test 5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Coaching
                Column {
                    Text("Coaching Center", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        coachings.forEach { c ->
                            val selected = adminCoaching == c
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.adminCoaching.value = c },
                                label = { Text(c) }
                            )
                        }
                    }
                }

                // Type
                Column {
                    Text("Pattern", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { t ->
                            val selected = adminType == t
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.adminType.value = t },
                                label = { Text(t) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = adminQuestionCount,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            viewModel.adminQuestionCount.value = it
                        }
                    },
                    label = { Text("Total Number of Questions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Answer Key Text Instructions
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("💡 Answer Key Instructions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "Format: 'QNo: Answer'. Enter one per line. If left blank, keys will be auto-generated for testing purposes.\n" +
                                    "• Single Choice: '1: A'\n" +
                                    "• Multi Correct: '2: A,C'\n" +
                                    "• Integer Value: '3: 15'\n" +
                                    "• Matching type: '4: 1-P,2-Q,3-R,4-S'",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = adminAnswerKeyText,
                    onValueChange = { viewModel.adminAnswerKeyText.value = it },
                    label = { Text("Paste Answer Key List") },
                    placeholder = { Text("1: A\n2: B,D\n3: 25\n4: 1-P,2-Q,3-R,4-S") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Button(
                    onClick = { viewModel.createCustomTestPaper() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assemble & Publish Test", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
