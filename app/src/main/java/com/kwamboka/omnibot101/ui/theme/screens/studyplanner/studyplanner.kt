package com.kwamboka.omnibot101.ui.theme.screens.StudyPlanner

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudyPlannerScreen(){
    val neonBlue = Color(0xFF00FFFF)
    val darkNavy = Color(0xFF0A1128)
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var selectedDate by remember { mutableStateOf("") }
    var taskText by remember { mutableStateOf("") }
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }
    var selectedStudyMethod by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(listOf<StudyTask>()) }

    val suggestions = listOf(
        "Pomodoro: 25 min study, 5 min break",
        "Feynman Technique: Explain like you're teaching",
        "Mind Mapping: Visualize concepts",
        "Active Recall: Test yourself",
        "Spaced Repetition: Review over intervals",
        "Blurting: Recall everything on paper",
        "Past Papers: Practice real exam questions",
        "SQ3R: Survey, Question, Read, Recite, Review",
        "Cornell Notes: Structured note-taking",
        "Interleaving: Mix multiple subjects",
        "Teaching Someone Else",
        "Self-Quizzing",
        "Visual Learning: Charts & Diagrams"
    )

    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("studyTasks")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        tasks = snapshot.documents.mapNotNull { doc ->
                            val date = doc.getString("date") ?: return@mapNotNull null
                            val task = doc.getString("task") ?: return@mapNotNull null
                            val time = doc.getString("time") ?: ""
                            val method = doc.getString("method") ?: ""
                            val isDone = doc.getBoolean("isDone") ?: false
                            StudyTask(doc.id, date, task, time, method, isDone)
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkNavy)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("\uD83D\uDCDA Study Planner", fontSize = 24.sp, color = neonBlue, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { showDateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = neonBlue)) {
            Text("Pick Study Date", color = Color.Black)
        }

        if (selectedDate.isNotBlank()) {
            Text("\uD83D\uDCC5 Selected Date: $selectedDate", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = taskText,
            onValueChange = { taskText = it },
            label = {
                Text("Study Task", color = neonBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            },
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonBlue,
                unfocusedBorderColor = neonBlue.copy(alpha = 0.4f),
                cursorColor = neonBlue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            showTimePicker(context) { time -> selectedTime = time }
        }, colors = ButtonDefaults.buttonColors(containerColor = neonBlue)) {
            Text("Set Alarm Time", color = Color.Black)
        }

        if (selectedTime.isNotBlank()) {
            Text("⏰ Alarm: $selectedTime", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        AutoCompleteStudyMethod(
            allMethods = suggestions,
            selectedMethod = selectedStudyMethod,
            onMethodSelected = { selectedStudyMethod = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(neonBlue, Color.Cyan, neonBlue)),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(2.dp)
        ) {
            Button(
                onClick = {
                    if (taskText.isNotBlank() && selectedDate.isNotBlank()) {
                        saveTaskToFirestore(userId, selectedDate, taskText, selectedTime, selectedStudyMethod)
                        Toast.makeText(context, "✅ Task Saved!", Toast.LENGTH_SHORT).show()
                        taskText = ""
                        selectedTime = ""
                        selectedStudyMethod = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = darkNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Task", color = neonBlue)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("\uD83D\uDCDA Your Tasks", color = neonBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/book.json"))
                val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                var showDeleteDialog by remember { mutableStateOf(false) }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Task?", color = Color.White) },
                        text = { Text("Are you sure you want to delete this study task?", color = Color.LightGray) },
                        confirmButton = {
                            TextButton(onClick = {
                                userId?.let {
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(it)
                                        .collection("studyTasks")
                                        .document(task.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "🗑️ Task deleted", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                showDeleteDialog = false
                            }) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        },
                        containerColor = Color(0xFF1A1A1A)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showDeleteDialog = true }
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(neonBlue.copy(alpha = 0.5f), Color.Transparent, neonBlue.copy(alpha = 0.5f))
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LottieAnimation(
                        composition,
                        progress,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 12.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("• ${task.date} - ${task.task}", color = if (task.isDone) Color.Gray else Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (task.method.isNotBlank()) {
                            Text(task.method, color = Color.LightGray, fontSize = 12.sp)
                        }
                    }

                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { isChecked ->
                            userId?.let {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(it)
                                    .collection("studyTasks")
                                    .document(task.id)
                                    .update("isDone", isChecked)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = neonBlue, uncheckedColor = Color.White)
                    )
                }
            }
        }
    }

    if (showDateDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val formatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
                        selectedDate = formatted
                    }
                    showDateDialog = false
                }) {
                    Text("Select", color = neonBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AutoCompleteStudyMethod(
    allMethods: List<String>,
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    var query by remember { mutableStateOf(selectedMethod) }
    var expanded by remember { mutableStateOf(false) }

    val filtered = allMethods.filter {
        it.contains(query, ignoreCase = true) && it != query
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = {
                Text("Study Method", color = Color(0xFF00FFFF), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            },
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFFF),
                unfocusedBorderColor = Color(0xFF00FFFF).copy(alpha = 0.4f),
                cursorColor = Color(0xFF00FFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF0A1128))
        ) {
            filtered.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method, color = Color.White, fontSize = 14.sp) },
                    onClick = {
                        onMethodSelected(method)
                        query = method
                        expanded = false
                    }
                )
            }
        }
    }

    onMethodSelected(query)
}

fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            val time = String.format("%02d:%02d", hour, minute)
            onTimeSelected(time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

fun saveTaskToFirestore(
    userId: String?,
    date: String,
    task: String,
    time: String,
    method: String
) {
    if (userId == null) return

    val taskMap = mapOf(
        "date" to date,
        "task" to task,
        "time" to time,
        "method" to method,
        "isDone" to false,
        "timestamp" to Timestamp.now()
    )

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("studyTasks")
        .add(taskMap)
}

data class StudyTask(
    val id: String,
    val date: String,
    val task: String,
    val time: String,
    val method: String,
    val isDone: Boolean = false
)
