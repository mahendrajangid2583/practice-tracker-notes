package com.aashu.privatesuite.presentation.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aashu.privatesuite.data.local.entities.TaskEntity
import com.aashu.privatesuite.presentation.components.RichTextEditor
import com.aashu.privatesuite.presentation.components.StatusSelector
import com.aashu.privatesuite.presentation.components.DifficultySelector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    taskId: String,
    viewModel: EditorViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val taskState by viewModel.task.collectAsState()
    val task = taskState
    val scope = rememberCoroutineScope()

    // Mention State
    var showMentionPopup by remember { mutableStateOf(false) }
    var mentionQuery by remember { mutableStateOf("") }
    var mentionResults by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    var onMentionSelect: ((TaskEntity) -> Unit)? by remember { mutableStateOf(null) }
    var cursorPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Dialog States
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkDialogUrl by remember { mutableStateOf("") }
    var onLinkEnteredCallback: ((String) -> Unit)? by remember { mutableStateOf(null) }

    var showImageDialog by remember { mutableStateOf(false) }
    var imageDialogUrl by remember { mutableStateOf("") }
    var onImageEnteredCallback: ((String) -> Unit)? by remember { mutableStateOf(null) }

    // Back Handler to save on system back
    androidx.activity.compose.BackHandler {
        viewModel.saveTask()
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (task != null) {
                        TextField(
                            value = task.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            placeholder = { Text("Task Title") }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveTask()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TextButton(onClick = { 
                        viewModel.saveTask()
                        android.widget.Toast.makeText(context, "Saved", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                         Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (task != null) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Properties Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusSelector(
                            currentStatus = task.status,
                            onStatusSelected = { viewModel.updateStatus(it) }
                        )
                        
                        DifficultySelector(
                            currentDifficulty = task.difficulty,
                            onDifficultySelected = { viewModel.updateDifficulty(it) }
                        )
                    }

                    OutlinedTextField(
                        value = task.link ?: "",
                        onValueChange = { viewModel.updateLink(it) },
                        label = { Text("Link") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = task.rating?.toString() ?: "",
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                 viewModel.updateRating(it.toIntOrNull())
                            }
                        },
                        label = { Text("Rating") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    HorizontalDivider(
                         modifier = Modifier.padding(vertical = 8.dp),
                         color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        RichTextEditor(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            initialHtml = task.notes,
                            onContentChanged = { html ->
                                viewModel.updateNotes(html)
                            },
                            onMentionQuery = { query, onSelect ->
                                mentionQuery = query
                                onMentionSelect = onSelect
                                // Trigger search
                                scope.launch {
                                    mentionResults = viewModel.searchTasks(query)
                                    showMentionPopup = mentionResults.isNotEmpty()
                                }
                            },
                            onMentionDismiss = {
                                showMentionPopup = false
                            },
                            onRequestInsertLink = { current, callback ->
                                linkDialogUrl = current ?: ""
                                onLinkEnteredCallback = callback
                                showLinkDialog = true
                            },
                            onRequestInsertImage = { callback ->
                                imageDialogUrl = ""
                                onImageEnteredCallback = callback
                                showImageDialog = true
                            },
                            onCursorPositionChange = { x, y ->
                                cursorPosition = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                            }
                        )

                        // Mention Popup Overlay
                        if (showMentionPopup) {
                            // Convert standard params to density independent pixels if needed?
                            // The x, y from TextView are in pixels relative to the TextView.
                            // We need to position this popup relative to the RichTextEditor or its parent Box.
                            // Since we are inside a Box that contains the Editor, and the Editor fills the Box (mostly),
                            // we can try using `offset` with density conversion.
                            
                            val density = androidx.compose.ui.platform.LocalDensity.current
                            val offsetX = with(density) { cursorPosition.x.toDp() }
                            val offsetY = with(density) { cursorPosition.y.toDp() + 20.dp } // Slightly below
                            
                            Surface(
                                modifier = Modifier
                                    .padding(start = offsetX.coerceAtMost(300.dp), top = offsetY) // Simple constraint
                                    .fillMaxWidth(0.8f) // Not full width
                                    .height(200.dp), // Max height
                                shadowElevation = 8.dp,
                                tonalElevation = 8.dp
                            ) {
                                Column {
                                    Text(
                                        "Mentions",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    LazyColumn {
                                        items(mentionResults) { item ->
                                            ListItem(
                                                headlineContent = { Text(item.title) },
                                                supportingContent = { Text(item.status) },
                                                modifier = Modifier.clickable {
                                                    onMentionSelect?.invoke(item)
                                                    showMentionPopup = false
                                                }
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Link Dialog
                if (showLinkDialog) {
                    AlertDialog(
                        onDismissRequest = { showLinkDialog = false },
                        title = { Text("Insert Link") },
                        text = {
                            OutlinedTextField(
                                value = linkDialogUrl,
                                onValueChange = { linkDialogUrl = it },
                                label = { Text("URL") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                onLinkEnteredCallback?.invoke(linkDialogUrl)
                                showLinkDialog = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLinkDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                // Image Dialog
                if (showImageDialog) {
                    AlertDialog(
                        onDismissRequest = { showImageDialog = false },
                        title = { Text("Insert Image") },
                        text = {
                            OutlinedTextField(
                                value = imageDialogUrl,
                                onValueChange = { imageDialogUrl = it },
                                label = { Text("Image URL") },
                                singleLine = true,
                                placeholder = { Text("https://example.com/image.png") }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                onImageEnteredCallback?.invoke(imageDialogUrl)
                                showImageDialog = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showImageDialog = false }) { Text("Cancel") }
                        }
                    )
                }

            } else {
                 // Loading State
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     CircularProgressIndicator()
                 }
            }
        }
    }
}
