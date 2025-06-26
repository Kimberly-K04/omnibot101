package com.kwamboka.omnibot101.ui.theme.screens.Login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.getResult(ApiException::class.java)
            Toast.makeText(context, "Google Sign-In successful", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val neonBlue = Color(0xFF00CFFF)
    val neonPurple = Color(0xFFB132FF)
    val darkNavy = Color(0xFF001933)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome",
            fontSize = 30.sp,
            fontFamily = FontFamily.Cursive,
            color = neonBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = loginFieldColors(neonBlue, neonPurple)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                        tint = neonPurple
                    )
                }
            },
            colors = loginFieldColors(neonBlue, neonPurple)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Login", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign in with Google", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onForgotPassword) {
            Text("Forgot Password?", color = neonBlue)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register", color = neonPurple)
        }
    }
}

@Composable
fun loginFieldColors(primary: Color, accent: Color) = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = primary,
    focusedLabelColor = accent,
    unfocusedLabelColor = accent.copy(alpha = 0.7f),
    focusedIndicatorColor = accent,
    unfocusedIndicatorColor = accent.copy(alpha = 0.4f)
)
