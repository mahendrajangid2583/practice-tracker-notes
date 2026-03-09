package com.aashu.privatesuite.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusSelector(
    currentStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Pending", "In Progress", "Done")

    Box {
        StatusBadge(status = currentStatus, onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    onClick: () -> Unit = {}
) {
    val (backgroundColor, textColor) = when (status) {
        "Done" -> Color(0xFFDCFCE7) to Color(0xFF166534) // Green
        "In Progress" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF) // Blue
        else -> Color(0xFFF3F4F6) to Color(0xFF374151) // Gray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun DifficultySelector(
    currentDifficulty: String,
    onDifficultySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Easy", "Medium", "Hard")

    Box {
        DifficultyBadge(difficulty = currentDifficulty, onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { difficulty ->
                DropdownMenuItem(
                    text = { Text(difficulty) },
                    onClick = {
                        onDifficultySelected(difficulty)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DifficultyBadge(
    difficulty: String,
    onClick: () -> Unit = {}
) {
    val (backgroundColor, textColor) = when (difficulty) {
        "Hard" -> Color(0xFFFEE2E2) to Color(0xFF991B1B) // Red
        "Medium" -> Color(0xFFFEF3C7) to Color(0xFF92400E) // Amber
        else -> Color(0xFFE0E7FF) to Color(0xFF3730A3) // Indigo (Easy)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = difficulty,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
