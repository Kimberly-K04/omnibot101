package com.kwamboka.omnibot101.ui.theme.screens.Chatbot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class ChatMessage(val text: String = "", val isUser: Boolean = true, val timestamp: Long = System.currentTimeMillis())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(navigateToMoodScreen: () -> Unit = {}) {
    val darkNavy = Color(0xFF000E1E)
    val neonBlue = Color(0xFF00CFFF)
    val softNeon = Color(0xFFB3EFFF)

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    val chatRef = db.collection("chats").document(userId).collection("messages")

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var userInput by remember { mutableStateOf("") }
    var showWelcome by remember { mutableStateOf(true) }
    var aiModeEnabled by remember { mutableStateOf(false) }
    var isBotTyping by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun loadChatHistory() {
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING).get().addOnSuccessListener { snapshot ->
            messages = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
        }
    }

    fun saveMessageToFirestore(message: ChatMessage) {
        chatRef.add(message)
    }

    LaunchedEffect(Unit) {
        delay(2000)
        showWelcome = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "OmniBot",
                            color = neonBlue,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.shadow(4.dp, shape = RoundedCornerShape(8.dp))
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (aiModeEnabled) "⚛ Quantum AI Online" else "👤 Human Assist Mode",
                                color = softNeon,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = aiModeEnabled,
                                onCheckedChange = { aiModeEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = neonBlue,
                                    uncheckedThumbColor = Color.Gray
                                )
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            showHistory = !showHistory
                            if (showHistory) loadChatHistory() else {
                                val tenSecondsAgo = System.currentTimeMillis() - 10_000
                                messages = messages.filter { it.timestamp >= tenSecondsAgo }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showHistory) "Hide Logs" else "Show Logs",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkNavy)
            )
        },
        containerColor = darkNavy,
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(darkNavy)
                    .padding(8.dp)
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Send a transmission...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = neonBlue,
                        unfocusedIndicatorColor = neonBlue.copy(alpha = 0.3f),
                        cursorColor = neonBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            val input = userInput.trim()
                            val userMessage = ChatMessage(input, true)
                            messages = messages + userMessage
                            saveMessageToFirestore(userMessage)
                            userInput = ""

                            coroutineScope.launch {
                                isBotTyping = true
                                delay(1500)
                                val reply = if (aiModeEnabled) generateSciFiAIReply(input) else generateBotReply(input)
                                val botMessage = ChatMessage(reply, false)
                                messages = messages + botMessage
                                saveMessageToFirestore(botMessage)
                                isBotTyping = false

                                if (detectMood(input) == "sad") {
                                    val moodMsg = ChatMessage("⚠️ Emotional anomaly detected. Redirecting to Mood Space...", false)
                                    messages = messages + moodMsg
                                    saveMessageToFirestore(moodMsg)
                                    delay(1000)
                                    navigateToMoodScreen()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(neonBlue.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(darkNavy, Color.Black)))
                .padding(8.dp)
        ) {
            AnimatedVisibility(visible = showWelcome, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = "🌌 OmniBot activated. Awaiting your command...",
                    color = softNeon,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages) { message ->
                    SciFiBubble(message)
                }

                if (isBotTyping) {
                    item {
                        SciFiBubble(ChatMessage("⌛ Calculating response...", false))
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size)
    }
}

@Composable
fun SciFiBubble(message: ChatMessage) {
    val isUser = message.isUser
    val bubbleColor = if (isUser) Color(0xFF002A4A) else Color(0xFF004466)
    val textColor = if (isUser) Color.White else Color(0xFFE0FFFF)

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bubbleColor)
                .padding(14.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 14.sp,
                textAlign = if (isUser) TextAlign.End else TextAlign.Start
            )
        }
    }
}

fun generateSciFiAIReply(userMessage: String): String {
    return when {
        "hello" in userMessage.lowercase() -> "👾 Greetings, traveler. How may the OmniBot assist your journey?"
        "study" in userMessage -> "📡 Initiating learning protocol... try the Pomodoro technique to optimize data absorption."
        "help" in userMessage -> "🧠 I possess adaptive knowledge modules. Ask anything, I shall respond."
        "bye" in userMessage -> "🌌 Logging out from this timeline. Stay stellar."
        else -> "💬 Processing... your message is under deep space analysis."
    }
}

fun generateBotReply(userMessage: String): String {
    return "Got it! (psst... switch to AI Mode for a smarter reply)"
}

fun detectMood(userMessage: String): String {
    val sadWords = listOf("sad", "tired", "lonely", "depressed", "hopeless", "down")
    return if (sadWords.any { it in userMessage.lowercase() }) "sad" else "neutral"
}
