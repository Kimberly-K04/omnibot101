package com.kwamboka.omnibot101.ui.theme.screens.ForgotPassword

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val neonBlue = Color(0xFF00CFFF)
    val neonPurple = Color(0xFFB132FF)
    val darkNavy = Color(0xFF001F3F)

    var email by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = neonBlue,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(snackbarMessage, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = darkNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 80.dp), // Top spacing added
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium,
                color = neonBlue,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = neonBlue,
                    focusedLabelColor = neonBlue,
                    unfocusedLabelColor = neonPurple.copy(alpha = 0.6f),
                    focusedIndicatorColor = neonPurple,
                    unfocusedIndicatorColor = neonPurple.copy(alpha = 0.4f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    snackbarMessage = "Reset link sent to your email"
                                } else {
                                    snackbarMessage = "Failed: ${task.exception?.message ?: "Try again"}"
                                }
                                showSnackbar = true
                            }
                    } else {
                        snackbarMessage = "Please enter a valid email"
                        showSnackbar = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send Reset Link", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Back to Login", color = neonPurple, fontWeight = FontWeight.Medium)
            }
        }
    }
}
