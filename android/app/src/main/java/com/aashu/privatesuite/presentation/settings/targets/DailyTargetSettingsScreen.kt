package com.aashu.privatesuite.presentation.settings.targets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DailyTargetSettingsScreen(
    navController: NavController,
    viewModel: DailyTargetSettingsViewModel = hiltViewModel()
) {
    val slots by viewModel.slots.collectAsState()
    val collections by viewModel.collections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Target Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        viewModel.saveSettings()
                        navController.popBackStack() 
                    }) {
                        Text("Save")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addSlot() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Slot")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Configure Slots",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Each slot represents one task in your daily targets. If multiple collections are selected for a slot, one will be picked randomly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(slots) { slot ->
                SlotCard(
                    slot = slot,
                    allCollections = collections,
                    onRemove = { viewModel.removeSlot(slot.id) },
                    onToggleCollection = { collectionId -> viewModel.toggleCollection(slot.id, collectionId) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlotCard(
    slot: com.aashu.privatesuite.domain.model.TargetSlot,
    allCollections: List<com.aashu.privatesuite.data.local.entities.CollectionEntity>,
    onRemove: () -> Unit,
    onToggleCollection: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slot.label.ifEmpty { "Slot" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Remove Slot",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mode Indicator
            val modeText = if (slot.collectionIds.size > 1) "Random Mode (Last Pending)" else "Linear Mode (First Pending)"
            val modeColor = if (slot.collectionIds.size > 1) Color(0xFF9C27B0) else Color(0xFF2196F3)
            
            Text(
                text = modeText,
                style = MaterialTheme.typography.labelSmall,
                color = modeColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCollections.forEach { collection ->
                    val isSelected = slot.collectionIds.contains(collection.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleCollection(collection.id) },
                        label = { Text(collection.title) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}
