package com.kwamboka.omnibot101.ui.theme.screens.Mood

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FloatingEmoji(
    val emoji: String,
    var x: Float,
    var y: Float,
    val speedY: Float,
    val alpha: Float
)

fun generateEmojis(mood: String?): List<FloatingEmoji> {
    val moodEmojis = mapOf(
        "Happy" to listOf("🥰", "😊", "😁"),
        "Sad" to listOf("😢", "😭", "🥺"),
        "Angry" to listOf("😠", "😡", "😤"),
        "Anxious" to listOf("😰", "😨", "💜"),
        "Calm" to listOf("🧘", "🌿", "😌"),
        "Excited" to listOf("🤩", "🎉", "🔥")
    )
    val emojis = moodEmojis[mood] ?: listOf("✨", "🌟", "💫")
    return List(18) {
        FloatingEmoji(
            emoji = emojis.random(),
            x = (0..800).random().toFloat(),
            y = (0..1600).random().toFloat(),
            speedY = (0.3f..1.3f).random(),
            alpha = (0.4f..0.9f).random()
        )
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float {
    return (start + Math.random() * (endInclusive - start)).toFloat()
}

@Composable
fun MoodScreen(
    onChatbotClick: () -> Unit,
    onSocializeClick: () -> Unit,
    onMoodSubmit: (String) -> Unit,
    neonPurple: Color
) {
    val mutedNavy = Color(0xFF102542)
    val lighterBlue = Color(0xFF243B55)
    val contrastWhite = Color(0xFFEEEEEE)
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    var selectedMood by remember { mutableStateOf<String?>(null) }
    var submittedMood by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var journalText by remember { mutableStateOf("") }
    var currentMusicUrl by remember { mutableStateOf<String?>(null) }
    var journalEntries by remember { mutableStateOf(listOf<String>()) }
    var showJournalEntries by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var journalToDelete by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val emojis = remember { mutableStateListOf<FloatingEmoji>() }

    LaunchedEffect(selectedMood) {
        emojis.clear()
        emojis.addAll(generateEmojis(selectedMood))
    }

    val moodPlaylists = mapOf(
        "Happy" to "https://open.spotify.com/playlist/37i9dQZF1DX3rxVfibe1L0",
        "Sad" to "https://youtube.com/playlist?list=PLS0gTslZhzziJ59zUQzyYFZbWhP0d6WlV",
        "Angry" to "https://open.spotify.com/playlist/37i9dQZF1DWZ3xRu8ajLOe",
        "Anxious" to "https://youtube.com/playlist?list=PLsRNoUx8w3r0I-JiAqjqTTWB3N8eCw5Ht",
        "Calm" to "https://open.spotify.com/playlist/37i9dQZF1DWU0ScTcjJBdj",
        "Excited" to "https://youtube.com/playlist?list=PLAtI1vclF6KD5V-7vXooxAqh9M0wnc9IV"
    )

    suspend fun loadJournalEntries(): List<String> {
        return try {
            val snapshot = db.collection("moods")
                .document(userId)
                .collection("entries")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { it.getString("journal") }
        } catch (e: Exception) {
            Log.e("MoodScreen", "Failed to load journals", e)
            emptyList()
        }
    }

    fun saveMoodAndJournal(mood: String, journal: String) {
        val entry = hashMapOf(
            "mood" to mood,
            "journal" to journal,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("moods").document(userId).collection("entries").add(entry)
    }

    currentMusicUrl?.let { url ->
        LaunchedEffect(url) {
            delay(1500)
            showDialog = false
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            currentMusicUrl = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing)),
        label = "offset"
    )

    val animatedBrush = Brush.linearGradient(
        colors = listOf(mutedNavy, neonPurple.copy(alpha = 0.35f), lighterBlue),
        start = Offset.Zero,
        end = Offset(x = animatedOffset, y = animatedOffset)
    )

    LaunchedEffect(Unit) {
        while (true) {
            emojis.forEach {
                it.y -= it.speedY
                if (it.y < -50f) {
                    it.y = 1600f
                    it.x = (0..800).random().toFloat()
                }
            }
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            emojis.forEach {
                drawContext.canvas.nativeCanvas.drawText(
                    it.emoji,
                    it.x,
                    it.y,
                    android.graphics.Paint().apply {
                        textSize = 40f
                        alpha = (it.alpha * 255).toInt()
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 60.dp, start = 16.dp, end = 16.dp, bottom = 120.dp)
        ) {
            Text(
                "How are you feeling today?",
                color = contrastWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
 Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(220.dp)
            ) {
                val moods = listOf("Happy", "Sad", "Angry", "Anxious", "Calm", "Excited")
                items(moods) { mood ->
                    MoodOption(mood = mood, isSelected = selectedMood == mood, onClick = { selectedMood = mood })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                selectedMood?.let {
                    submittedMood = it
                    onMoodSubmit(it)
                    saveMoodAndJournal(it, journalText)
                    journalText = "" // clear after saving
                }
            }, enabled = selectedMood != null, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                Text("Submit Mood", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (submittedMood != null) {
                OutlinedTextField(
                    value = journalText,
                    onValueChange = { journalText = it },
                    label = { Text("Write how you're feeling...", color = contrastWhite.copy(alpha = 0.9f)) },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonPurple,
                        unfocusedBorderColor = contrastWhite.copy(alpha = 0.5f),
                        cursorColor = neonPurple
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    submittedMood?.let {
                        saveMoodAndJournal(it, journalText)
                        journalText = "" // clear after saving
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                    Text("Save Journal Entry", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                coroutineScope.launch {
                    journalEntries = loadJournalEntries()
                    showJournalEntries = !showJournalEntries
                }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                Text(if (showJournalEntries) "Hide Journal Entries" else "View Previous Journal Entries", color = Color.White)
            }

            if (showJournalEntries) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    journalEntries.forEach { entry ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("• $entry", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    journalToDelete = entry
                                    showDeleteDialog = true
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(neonPurple.copy(alpha = 0.2f), shape = CircleShape)
                                    .clip(CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = neonPurple)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(Icons.Default.MusicNote, "Play Music") {
                submittedMood?.let { mood ->
                    moodPlaylists[mood]?.let { url ->
                        showDialog = true
                        currentMusicUrl = url
                    }
                }
            }
            ActionButton(Icons.Default.Chat, "Chatbot", onChatbotClick)
            ActionButton(Icons.Default.People, "Socialize", onSocializeClick)
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    journalToDelete = null
                },
                confirmButton = {
                    TextButton(onClick = {
                        journalToDelete?.let { entryToDelete ->
                            coroutineScope.launch {
                                val querySnapshot = db.collection("moods")
                                    .document(userId)
                                    .collection("entries")
                                    .whereEqualTo("journal", entryToDelete)
                                    .get().await()

                                querySnapshot.documents.firstOrNull()?.reference?.delete()
                                journalEntries = journalEntries - entryToDelete
                                showDeleteDialog = false
                                journalToDelete = null
                            }
                        }
                    }) {
                        Text("Delete", color = neonPurple, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        journalToDelete = null
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                title = { Text("Delete Entry", color = neonPurple, fontWeight = FontWeight.SemiBold) },
                text = { Text("Are you sure you want to delete this journal entry?", color = Color.White) },
                containerColor = Color(0xFF102542),
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {},
                title = { Text("Opening playlist...", color = neonPurple) },
                containerColor = Color.White,
                text = { Text("Launching your music vibe 🌷", color = Color.Black) }
            )
        }
    }
}

@Composable
fun MoodOption(mood: String, isSelected: Boolean, onClick: () -> Unit) {
    val selectedBg = Color(0xFF375F9D)
    val textColor = if (isSelected) Color.White else Color(0xFFB0C4DE)
    val bgColor = if (isSelected) selectedBg else Color.White.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(mood, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    val iconColor = Color(0xFFADD8E6)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(34.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = iconColor, fontSize = 12.sp)
    }
}
