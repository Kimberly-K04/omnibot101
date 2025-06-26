package com.kwamboka.omnibot101.ui.theme.screens.EcoAccess

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kwamboka.omnibot101.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcoAccessScreen() {
    val neonGreen = Color(0xFF00FF9D)
    val darkNavy = Color(0xFF0D1B2A)
    val context = LocalContext.current
    val ecoPlaylistUrl = "https://open.spotify.com/playlist/37i9dQZF1DX0OaSJooo0zT"

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var activities by remember { mutableStateOf(mutableListOf(
        "Turned off unused lights",
        "Used public transport",
        "Recycled plastic",
        "Carried a reusable bottle"
    )) }
    var completed by remember { mutableStateOf(setOf<String>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newActivity by remember { mutableStateOf("") }
    var ecoPoints by remember { mutableStateOf(0) }
    var badgeMessage by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var historyLogs by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var todayLogged by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<String?>(null) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.drawable.leaf_float))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkNavy)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Eco Access", color = neonGreen, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            LottieAnimation(composition, progress, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Track your daily eco-saving habits 🌍", fontSize = 16.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(6.dp))
        Text("\"Small steps create big impact.\"", color = neonGreen.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(20.dp))

        activities.forEach { activity ->
            val isCompleted = completed.contains(activity)
            val pulseAnim = rememberInfiniteTransition()
            val scale by pulseAnim.animateFloat(initialValue = 1f, targetValue = 1.1f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .combinedClickable(
                        onClick = {
                            if (!isCompleted) {
                                completed += activity
                                ecoPoints++
                                if (ecoPoints % 5 == 0) badgeMessage = "🎉 Achievement: $ecoPoints Eco Points!"
                            } else {
                                completed -= activity
                                ecoPoints--
                            }
                        },
                        onLongClick = {
                            activityToDelete = activity
                        }
                    )
            ) {
                Text(activity, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                AnimatedVisibility(visible = isCompleted, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                    Image(
                        painter = painterResource(id = R.drawable.leaf),
                        contentDescription = "Leaf",
                        modifier = Modifier.size(28.dp).scale(scale).shadow(6.dp, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(neonGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("✅ Completed: ${completed.size}", color = Color.White, fontSize = 16.sp)
            Text("🌿 Points: $ecoPoints", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (badgeMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Snackbar(containerColor = neonGreen, contentColor = darkNavy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(badgeMessage, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = neonGreen)) {
                Text("➕ Add Activity", color = darkNavy, fontWeight = FontWeight.Bold)
            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ecoPlaylistUrl))
                context.startActivity(intent)
            }, colors = ButtonDefaults.buttonColors(containerColor = neonGreen)) {
                Text("🎧 Eco Mode", color = darkNavy, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (todayLogged) {
                    Toast.makeText(context, "You've already logged today 🌿", Toast.LENGTH_SHORT).show()
                } else if (userId != null) {
                    val activityData = mapOf(
                        "activities" to activities,
                        "completed" to completed.toList(),
                        "ecoPoints" to ecoPoints,
                        "timestamp" to Timestamp.now()
                    )
                    firestore.collection("users")
                        .document(userId)
                        .collection("ecoProgress")
                        .add(activityData)
                        .addOnSuccessListener {
                            todayLogged = true
                            Toast.makeText(context, "Progress saved! ☁️", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
        ) {
            Text("💾 Save Progress", color = darkNavy, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                showHistory = !showHistory
                if (showHistory) {
                    userId?.let {
                        firestore.collection("users")
                            .document(it)
                            .collection("ecoProgress")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                historyLogs = snapshot.documents.mapNotNull { it.data }
                            }
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
        ) {
            Text(if (showHistory) "🔽 Hide History" else "📜 View History", color = darkNavy, fontWeight = FontWeight.Bold)
        }

        AnimatedVisibility(visible = showHistory) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF122C3D), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                items(historyLogs) { log ->
                    val timestamp = (log["timestamp"] as? Timestamp)?.toDate()
                    val dateText = timestamp?.let {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
                    } ?: "Unknown Date"

                    val completedList = log["completed"] as? List<*>
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("📅 $dateText", color = neonGreen, fontWeight = FontWeight.Bold)
                        completedList?.forEach {
                            Text("✅ $it", color = Color.White, fontSize = 13.sp)
                        }
                        Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Keep going! Every small action makes a difference. 🌱", color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (newActivity.isNotBlank()) {
                        activities.add(newActivity.trim())
                        newActivity = ""
                        showDialog = false
                    }
                }) {
                    Text("Add", color = neonGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            title = { Text("New Eco Activity", color = Color.White) },
            text = {
                BasicTextField(
                    value = newActivity,
                    onValueChange = { newActivity = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier
                        .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            },
            containerColor = darkNavy,
            shape = RoundedCornerShape(12.dp)
        )
    }

    activityToDelete?.let { activity ->
        AlertDialog(
            onDismissRequest = { activityToDelete = null },
            title = { Text("Delete Activity", color = Color.White) },
            text = { Text("Are you sure you want to delete '$activity'?", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    activities.remove(activity)
                    completed = completed - activity
                    activityToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { activityToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = darkNavy
        )
    }
}
