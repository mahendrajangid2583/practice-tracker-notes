package com.aashu.privatesuite.presentation.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aashu.privatesuite.presentation.components.RichTextRenderer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadNoteScreen(
    taskId: String,
    onBack: () -> Unit,
    onNavigateToTask: (String, String) -> Unit, // collectionId, taskId
    viewModel: ReadNoteViewModel = hiltViewModel()
) {
    val task by viewModel.task
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = task?.title ?: "Note",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (task != null) {
                RichTextRenderer(
                    content = task!!.notes,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    onLinkClick = { link ->
                        // Handle relative URLs from Web App
                        if (link.startsWith("/collection/")) {
                            val uri = android.net.Uri.parse("https://dummy.com$link") // Parse as full URI to extract params easily
                            val pathSegments = uri.pathSegments
                            val collectionId = pathSegments.lastOrNull() // "collection" is first, id is second
                            val taskId = uri.getQueryParameter("focus")

                            if (!collectionId.isNullOrEmpty() && !taskId.isNullOrEmpty()) {
                                // Navigate to task
                                onNavigateToTask(collectionId, taskId)
                            } else {
                                // Handle collection navigation if needed in future
                                // For now, we only support task navigation via mentions in this screen context
                            }
                        } else if (link.startsWith("app://task/")) {
                           // Legacy support
                            val linkedTaskId = link.removePrefix("app://task/")
                            scope.launch {
                                val linkedTask = viewModel.getTask(linkedTaskId)
                                if (linkedTask != null) {
                                    onNavigateToTask(linkedTask.collectionId, linkedTaskId)
                                }
                            }
                        } else {
                            // Open in browser
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            } else {
                 androidx.compose.material3.CircularProgressIndicator(
                     modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                 )
            }
        }
    }
}
