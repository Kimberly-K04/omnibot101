package com.kwamboka.omnibot101.ui.theme.screens.Register

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()

    val glowColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF00BFFF),
        targetValue = Color(0xFF1DE9B6),
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )

    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF001F3F), Color(0xFF000B1A)),
                    center = Offset.Zero,
                    radius = 700f
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Create Account",
            style = TextStyle(
                fontSize = 28.sp,
                color = glowColor.copy(alpha = glowAlpha),
                shadow = Shadow(
                    color = glowColor.copy(alpha = glowAlpha),
                    offset = Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = glowColor,
            focusedLabelColor = glowColor,
            unfocusedLabelColor = glowColor.copy(alpha = 0.6f),
            focusedIndicatorColor = glowColor,
            unfocusedIndicatorColor = glowColor.copy(alpha = 0.4f)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Text(
                    if (passwordVisible) "🙈" else "👁️",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Text(
                    if (confirmPasswordVisible) "🙈" else "👁️",
                    modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                )
            },
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                onRegisterSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Registration failed: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .scale(buttonScale)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = glowColor,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login", color = glowColor.copy(alpha = 0.8f))
        }
    }
}
