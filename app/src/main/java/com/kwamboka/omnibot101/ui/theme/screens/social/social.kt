package com.kwamboka.omnibot101.ui.theme.screens.Socialize

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocializeScreen() {
    val neonBlue = Color(0xFF00BFFF)
    val darkNavy = Color(0xFF0B1225)
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    val db = FirebaseFirestore.getInstance()
    val messagesRef = db.collection("socialize")
    val dmRef = db.collection("dm")
    val usersRef = db.collection("users")

    var newMessage by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var showContacts by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedContact by remember { mutableStateOf<String?>(null) }
    var dmText by remember { mutableStateOf("") }
    var dmMessages by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Load public messages
    LaunchedEffect(true) {
        messagesRef.orderBy("timestamp").addSnapshotListener { snapshot, _ ->
            messages = snapshot?.documents?.map {
                val msg = it.getString("text") ?: ""
                val id = it.id
                val sender = it.getString("userId") ?: ""
                val date = it.getLong("timestamp")?.let {
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(it))
                } ?: ""
                Triple(date, msg, id)
            } ?: emptyList()
        }
    }

    // Load contacts from Firestore users collection
    LaunchedEffect(true) {
        usersRef.get().addOnSuccessListener { result ->
            contacts = result.documents.mapNotNull {
                val name = it.getString("username") ?: it.getString("email")
                val id = it.id
                if (id != userId) name else null
            }
        }
    }

    // Load DMs if contact is selected
    LaunchedEffect(selectedContact) {
        if (selectedContact != null) {
            dmRef.document(userId).collection(selectedContact!!).orderBy("timestamp")
                .addSnapshotListener { snapshot, _ ->
                    dmMessages = snapshot?.documents?.map {
                        val msg = it.getString("text") ?: ""
                        val id = it.id
                        val date = it.getLong("timestamp")?.let {
                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(it))
                        } ?: ""
                        Triple(date, msg, id)
                    } ?: emptyList()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkNavy)
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("\uD83D\uDCAC Social Space", color = neonBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showContacts = !showContacts }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Toggle Contacts", tint = neonBlue)
            }
        }

        if (showContacts) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("\uD83D\uDC65 Your Vibe List:", color = neonBlue, fontWeight = FontWeight.Medium)
            contacts.forEach { name ->
                Text(
                    text = "• $name",
                    color = if (selectedContact == name) neonBlue else Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 2.dp)
                        .clickable { selectedContact = name }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "+ Invite More Friends",
                color = Color.Cyan,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, top = 8.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Join me on OmniBot!")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Hey! I’m using OmniBot – a space to vibe, reflect, and connect \uD83C\uDF0C. Come join me: [YOUR_PLAYSTORE_LINK]"
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Share OmniBot via:"))
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedContact == null) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                label = { Text("Share something kind...", color = neonBlue) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonBlue,
                    unfocusedBorderColor = neonBlue.copy(alpha = 0.4f),
                    cursorColor = neonBlue
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        messagesRef.add(
                            mapOf(
                                "text" to newMessage.trim(),
                                "timestamp" to System.currentTimeMillis(),
                                "userId" to userId
                            )
                        )
                        newMessage = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
            ) {
                Text("Post", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("\uD83E\uDE90 Recent Posts", color = neonBlue, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages.reversed()) { (time, text, id) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = darkNavy.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    if (userId == userId) {
                                        messageToDelete = Triple(time, text, id)
                                        showDeleteDialog = true
                                    }
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = text, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = time, color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = dmText,
                onValueChange = { dmText = it },
                label = { Text("Message $selectedContact...", color = neonBlue) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonBlue,
                    unfocusedBorderColor = neonBlue.copy(alpha = 0.4f),
                    cursorColor = neonBlue
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (dmText.isNotBlank()) {
                        dmRef.document(userId).collection(selectedContact!!).add(
                            mapOf(
                                "text" to dmText.trim(),
                                "timestamp" to System.currentTimeMillis(),
                                "userId" to userId
                            )
                        )
                        dmText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
            ) {
                Text("Send", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("✉️ DMs with $selectedContact", color = neonBlue, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(dmMessages.reversed()) { (time, text, id) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = darkNavy.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    messageToDelete = Triple(time, text, id)
                                    showDeleteDialog = true
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = text, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = time, color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && messageToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        val (time, text, id) = messageToDelete!!
                        if (selectedContact == null) {
                            messagesRef.document(id).delete()
                        } else {
                            dmRef.document(userId).collection(selectedContact!!).document(id).delete()
                        }
                        showDeleteDialog = false
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Delete Message?") },
                text = { Text("Are you sure you want to delete this message?") },
                containerColor = Color.White
            )
        }
    }
}
