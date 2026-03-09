package com.aashu.privatesuite.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aashu.privatesuite.presentation.components.TaskItem
import com.aashu.privatesuite.presentation.dashboard.CollectionItem
import com.aashu.privatesuite.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    
    // Simple filter state: "ALL", "COLLECTIONS", "TASKS"
    var activeFilter by remember { mutableStateOf("ALL") }

    var noteToRead by remember { mutableStateOf<com.aashu.privatesuite.data.local.entities.TaskEntity?>(null) }

    if (noteToRead != null) {
        com.aashu.privatesuite.presentation.components.ReadNoteDialog(
            title = noteToRead!!.title,
            content = noteToRead!!.notes,
            onDismiss = { noteToRead = null }
        )
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    androidx.compose.material3.TextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Search...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
// ... existing list items ...
            // Tasks Section
            if ((activeFilter == "ALL" || activeFilter == "TASKS") && results.tasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )
                }
                items(results.tasks) { task ->
                    TaskItem(
                        task = task,
                        onStatusChange = { viewModel.toggleTask(task.id, task.status) },
                        onDelete = { /* Search view deletion might be risky or needs implementing. keeping silent for now as "Navigate to item" is main goal */ },
                        onClick = { navController.navigate(Screen.CollectionDetail.createRoute(task.collectionId, task.id)) },
                        onReadNotes = { noteToRead = task }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            if (results.collections.isEmpty() && results.tasks.isEmpty() && query.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No results found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
