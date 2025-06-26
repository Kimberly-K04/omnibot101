package com.kwamboka.omnibot101.navigation

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kwamboka.omnibot101.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = EaseOutBack)
        )
        delay(3500)
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(ROUTE_SPLASH) { inclusive = true }
        }
    }

    SplashContent(scale.value)
}

@Composable
fun SplashContent(scale: Float) {
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
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Replaced spring with tween for dot bouncing
    val dotOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF001F3F), Color(0xFF000B1A)),
                    center = Offset.Zero,
                    radius = 500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "OmniBot",
                fontSize = 48.sp,
                fontFamily = FontFamily.Default,
                color = glowColor.copy(alpha = textAlpha),
                modifier = Modifier.scale(scale),
                style = TextStyle(
                    shadow = Shadow(
                        color = glowColor.copy(alpha = textAlpha),
                        offset = Offset.Zero,
                        blurRadius = 30f
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.splash2),
                contentDescription = "OmniBot Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, glowColor.copy(alpha = glowAlpha), CircleShape)
                    .scale(scale)
                    .shadow(
                        25.dp,
                        CircleShape,
                        ambientColor = glowColor.copy(alpha = glowAlpha),
                        spotColor = glowColor.copy(alpha = glowAlpha)
                    )
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .offset(y = dotOffsetY.dp)
                    .size(14.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = glowAlpha))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    SplashContent(scale = 1f)
}
