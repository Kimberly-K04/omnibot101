package com.kwamboka.omnibot101.ui.theme.screens.Dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kwamboka.omnibot101.R

@Composable
fun DashboardScreen(
    onChatClick: () -> Unit,
    onMoodCheckClick: () -> Unit,
    onEcoAccessClick: () -> Unit,
    onStudyPlannerClick: () -> Unit
) {
    val softNavy = Color(0xFF0D1B2A)
    val neonPurple = Color(0xFF9D00FF)
    val neonBlue = Color(0xFF00CFFF)

    val featureButtons = listOf(
        DashboardFeature("Chatbot", R.drawable.chatbot, onChatClick),
        DashboardFeature("Mood Check", R.drawable.mood, onMoodCheckClick),
        DashboardFeature("Eco Access", R.drawable.eco, onEcoAccessClick),
        DashboardFeature("Study Planner", R.drawable.planner, onStudyPlannerClick)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(softNavy, Color(0xFF1E1B3A))))
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = neonBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hello", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Let’s make today better", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Big full-height grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            items(featureButtons) { feature ->
                DashboardButton(
                    text = feature.text,
                    iconRes = feature.iconRes,
                    glowColor = neonBlue,
                    accentColor = neonPurple,
                    onClick = feature.onClick
                )
            }
        }
    }
}

data class DashboardFeature(
    val text: String,
    val iconRes: Int,
    val onClick: () -> Unit
)

@Composable
fun DashboardButton(text: String, iconRes: Int, glowColor: Color, accentColor: Color, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition()
    val glowAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                Brush.verticalGradient(
                    listOf(glowColor.copy(alpha = 0.2f), accentColor.copy(alpha = 0.25f))
                )
            )
            .clickable { onClick() }
            .padding(vertical = 24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = glowAlpha))
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
