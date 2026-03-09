package com.aashu.privatesuite.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aashu.privatesuite.data.local.entities.CollectionEntity
import com.aashu.privatesuite.data.local.entities.DailyTargetEntity
import com.aashu.privatesuite.presentation.collection.CreateCollectionDialog
import com.aashu.privatesuite.presentation.util.FadeInEffect
import com.aashu.privatesuite.presentation.navigation.Screen

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val collections by viewModel.collections.collectAsState()
    val dailyTargets by viewModel.dailyTargets.collectAsState()
    val streak by viewModel.streakFlow.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState(initial = false)
    val currentFilter by viewModel.filter.collectAsState()
    val syncError by viewModel.syncError.collectAsState(initial = null)

    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // noteToRead state removed in favor of ReadNoteScreen navigation

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Collection")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                FadeInEffect(delay = 0) {
                    HeaderSection(
                        isSyncing = isSyncing,
                        onSearchClick = { navController.navigate(Screen.Search.route) },
                        onSyncClick = { viewModel.triggerSync() },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
            }

            item {
                FadeInEffect(delay = 50) {
                    StreakSection(streak = streak, onClick = { navController.navigate(Screen.Streak.route) })
                }
            }

            item {
                FadeInEffect(delay = 100) {
                    DailyTargetSection(
                        dailyTargets = dailyTargets,
                        onSettingsClick = { navController.navigate(Screen.DailyTargetSettings.route) },
                        onToggleTask = { id, status -> viewModel.toggleTask(id, status) },
                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                        onReadNotes = { task -> navController.navigate(Screen.ReadNote.createRoute(task.id)) },
                        onTaskClick = { collectionId, taskId -> 
                            navController.navigate(Screen.CollectionDetail.createRoute(collectionId, taskId)) 
                        }
                    )
                }
            }

            item {
                FadeInEffect(delay = 150) {
                    FilterSection(
                        currentFilter = currentFilter,
                        onFilterSelected = { viewModel.setFilter(it) }
                    )
                }
            }

            item {
                FadeInEffect(delay = 200) {
                    Text(
                        text = "Collections",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            items(collections) { item ->
                FadeInEffect(delay = 300) {
                    CollectionItem(
                        collection = item.collection,
                        actualTotal = item.actualTotal,
                        actualCompleted = item.actualCompleted,
                        onClick = { 
                           navController.navigate(Screen.CollectionDetail.createRoute(item.collection.id))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (collections.isEmpty()) {
                item {
                    Text(
                        text = "No collections found. Create one to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 32.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, type, theme ->
                viewModel.createCollection(title, type, theme)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun FilterSection(
    currentFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("ALL", "DSA", "PROJECT", "LEARNING", "NOTES")
    
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == currentFilter
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun HeaderSection(isSyncing: Boolean = false, onSearchClick: () -> Unit, onSyncClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aashu",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                 if (isSyncing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        modifier = Modifier
                            .clickable { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onSyncClick() 
                            }
                            .padding(8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .clickable { onSearchClick() }
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .clickable { onSettingsClick() }
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = "Aashutosh Singh",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "Design your life. Track your progress.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun StreakSection(streak: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$streak Day Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keep showing up every day!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DailyTargetSection(
    dailyTargets: List<com.aashu.privatesuite.data.local.entities.TaskEntity>,
    onSettingsClick: () -> Unit,
    onToggleTask: (String, String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onReadNotes: (com.aashu.privatesuite.data.local.entities.TaskEntity) -> Unit,
    onTaskClick: (String, String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Targets",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configure Targets",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onSettingsClick() },
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        if (dailyTargets.isEmpty()) {
            Text(
                text = "No targets set for today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dailyTargets.forEach { task ->
                    com.aashu.privatesuite.presentation.components.TaskItem(
                        task = task,
                        onStatusChange = { onToggleTask(task.id, task.status) },
                        onClick = { onTaskClick(task.collectionId, task.id) },
                        onDelete = { onDeleteTask(task.id) },
                        onReadNotes = { onReadNotes(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionItem(
    collection: CollectionEntity,
    actualTotal: Int,
    actualCompleted: Int,
    onClick: () -> Unit
) {
    val progress = if (actualTotal > 0) {
        actualCompleted.toFloat() / actualTotal.toFloat()
    } else 0f

    val accentColor = when(collection.theme) {
        "blue" -> Color(0xFF3B82F6)
        "amber" -> Color(0xFFF59E0B)
        "emerald" -> Color(0xFF10B981)
        "rose" -> Color(0xFFF43F5E)
        "purple" -> Color(0xFF8B5CF6)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = collection.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${actualCompleted} / ${actualTotal} Tasks",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
