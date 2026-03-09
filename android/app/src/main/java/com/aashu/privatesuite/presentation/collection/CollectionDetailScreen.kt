package com.aashu.privatesuite.presentation.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aashu.privatesuite.presentation.components.QuickAddTaskInput
import com.aashu.privatesuite.presentation.components.TaskItem
import com.aashu.privatesuite.presentation.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    navController: NavController,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
    highlightTaskId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncError by viewModel.syncError.collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToRead by remember { mutableStateOf<com.aashu.privatesuite.data.local.entities.TaskEntity?>(null) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    when (val state = uiState) {
        is CollectionUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is CollectionUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is CollectionUiState.Success -> {
            val collection = state.collection
            val tasks = state.tasks
            val completedCount = tasks.count { it.status == "Done" }
            val totalCount = tasks.size
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
            var showEditTitleDialog by remember { mutableStateOf(false) }
            
            // Scroll to highlighted task
            androidx.compose.runtime.LaunchedEffect(highlightTaskId, tasks) {
                if (highlightTaskId != null && tasks.isNotEmpty()) {
                    val index = tasks.indexOfFirst { it.id == highlightTaskId }
                    if (index >= 0) {
                        kotlinx.coroutines.delay(100) // Small delay to ensure layout is ready
                        listState.scrollToItem(index)
                    }
                }
            }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { }, // Title is in the header content
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showEditTitleDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Title", tint = MaterialTheme.colorScheme.onBackground)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Collection",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        )
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header Section
                    item {
                        Column {
                            // Title
                            Text(
                                text = collection.title,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Light,
                                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "$completedCount of $totalCount Milestones Achieved",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFFF59E0B), // Amber-500
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    // Quick Add Input
                    item {
                        QuickAddTaskInput(
                            onAddTask = { title, link, difficulty -> 
                                viewModel.addTask(title, link, difficulty) 
                            }
                        )
                    }

                    // Task List
                    if (tasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Silence is golden.\nAdd a task to begin.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        items(tasks) { task ->
                            TaskItem(
                                task = task,
                                onStatusChange = { viewModel.toggleTask(task.id, task.status) },
                                onDelete = { viewModel.deleteTask(task.id) },
                                onReadNotes = {
                                    navController.navigate(Screen.ReadNote.createRoute(task.id))
                                },
                                onClick = { navController.navigate(Screen.Editor.createRoute(task.id)) },
                                modifier = if (task.id == highlightTaskId) {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                } // End LazyColumn

                // Edit Title Dialog
                if (showEditTitleDialog) {
                    var newTitle by remember { mutableStateOf(collection.title) }
                    AlertDialog(
                        onDismissRequest = { showEditTitleDialog = false },
                        title = { Text("Edit Title") },
                        text = { 
                            androidx.compose.material3.OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.updateCollectionTitle(newTitle)
                                    showEditTitleDialog = false
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditTitleDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Delete Dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Collection") },
                        text = { Text("Are you sure you want to delete '${collection.title}'? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteCollection {
                                        showDeleteDialog = false
                                        navController.popBackStack()
                                    }
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            } // End Scaffold
        } // End Success
    } // End when
} // End Screen
