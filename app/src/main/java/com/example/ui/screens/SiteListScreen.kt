package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SiteEntity
import com.example.ui.theme.ContractorBackground
import com.example.ui.theme.ContractorBorder
import com.example.ui.theme.ContractorDarkText
import com.example.ui.theme.ContractorGreen
import com.example.ui.theme.ContractorGreenBright
import com.example.ui.theme.ContractorGreenDeep
import com.example.ui.theme.ContractorMintContainer
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.ElectricGoldLight
import com.example.ui.theme.ShinyCardGradient
import com.example.ui.theme.ShinyGoldBadgeGradient
import com.example.ui.theme.ShinyHeaderGradient
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun SiteListScreen(
    sites: List<SiteEntity>,
    language: AppLanguage,
    onOpenSite: (Long) -> Unit,
    onCreateSite: (String, String, String, String) -> Unit,
    onUpdateSite: (SiteEntity) -> Unit,
    onDeleteSite: (Long) -> Unit,
    onSeedDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var siteToEdit by remember { mutableStateOf<SiteEntity?>(null) }
    var siteToDelete by remember { mutableStateOf<SiteEntity?>(null) }

    val filteredSites = remember(sites, searchQuery) {
        if (searchQuery.isBlank()) sites
        else sites.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.clientName.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(ContractorBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Shining Header Banner
            SiteHeaderBanner(
                totalSites = sites.size,
                language = language,
                onAddClick = { showCreateDialog = true }
            )

            // Crisp & Bright Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ContractorGreen
                    )
                },
                placeholder = { Text(Localization.get("search_hint", language), color = Color(0xFF64748B)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = ContractorGreen,
                    unfocusedBorderColor = ContractorBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .testTag("site_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredSites.isEmpty()) {
                EmptySitesView(
                    language = language,
                    onCreateClick = { showCreateDialog = true },
                    onSeedDemo = onSeedDemo
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().testTag("sites_list")
                ) {
                    items(filteredSites, key = { it.id }) { site ->
                        SiteCard(
                            site = site,
                            language = language,
                            onOpen = { onOpenSite(site.id) },
                            onEdit = { siteToEdit = site },
                            onDelete = { siteToDelete = site }
                        )
                    }
                }
            }
        }

        // Shining Floating Action Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Color.Transparent,
            contentColor = Color.White,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ShinyHeaderGradient)
                .testTag("add_site_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Site",
                    tint = ElectricGoldLight,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = Localization.get("new_site", language),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        SiteDialog(
            title = Localization.get("new_site", language),
            language = language,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, location, client, notes ->
                onCreateSite(name, location, client, notes)
                showCreateDialog = false
            }
        )
    }

    siteToEdit?.let { site ->
        SiteDialog(
            title = Localization.get("edit_site", language),
            initialName = site.name,
            initialLocation = site.location,
            initialClient = site.clientName,
            initialNotes = site.notes,
            language = language,
            onDismiss = { siteToEdit = null },
            onConfirm = { name, location, client, notes ->
                onUpdateSite(site.copy(name = name, location = location, clientName = client, notes = notes))
                siteToEdit = null
            }
        )
    }

    siteToDelete?.let { site ->
        AlertDialog(
            onDismissRequest = { siteToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(Localization.get("delete_site", language), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = Localization.get("delete_site_confirm", language))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = site.name,
                        fontWeight = FontWeight.Bold,
                        color = ContractorGreen
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSite(site.id)
                        siteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_site_button")
                ) {
                    Text(Localization.get("delete", language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { siteToDelete = null }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }
}

@Composable
fun SiteHeaderBanner(
    totalSites: Int,
    language: AppLanguage,
    onAddClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFF0FDF4),
                            Color(0xFFDCFCE7),
                            Color(0xFFD1FAE5)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Shining Golden Badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ShinyGoldBadgeGradient)
                            .border(1.5.dp, Color.White, CircleShape)
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = Localization.get("sites_count", language),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ContractorDarkText
                        )
                        Text(
                            text = Localization.formatNumber(totalSites, language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = ContractorGreenDeep
                        )
                    }
                }

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ShinyHeaderGradient)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = ElectricGoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.get("create", language),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SiteCard(
    site: SiteEntity,
    language: AppLanguage,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("site_card_${site.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )

                    if (site.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ContractorGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = site.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    if (site.clientName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ContractorGreenBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = site.clientName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_site_btn_${site.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = ContractorGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_site_btn_${site.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shining Gradient "Open Accounting" button
            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ShinyHeaderGradient)
                    .testTag("open_site_btn_${site.id}")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = ElectricGoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.get("open", language),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySitesView(
    language: AppLanguage,
    onCreateClick: () -> Unit,
    onSeedDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(ShinyGoldBadgeGradient)
                .border(2.dp, Color.White, CircleShape)
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Localization.get("empty_sites", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = Localization.get("empty_sites_sub", language),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ShinyHeaderGradient)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricGoldLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text(Localization.get("new_site", language), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onSeedDemo,
            border = BorderStroke(1.dp, ContractorGreen)
        ) {
            Text(Localization.get("load_sample", language), color = ContractorGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SiteDialog(
    title: String,
    initialName: String = "",
    initialLocation: String = "",
    initialClient: String = "",
    initialNotes: String = "",
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var location by remember { mutableStateOf(initialLocation) }
    var client by remember { mutableStateOf(initialClient) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Localization.get("site_name", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("site_name_input")
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(Localization.get("site_location", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("site_location_input")
                )
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text(Localization.get("client_name", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("client_name_input")
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Localization.get("note", language)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("site_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), location.trim(), client.trim(), notes.trim())
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ContractorGreen),
                modifier = Modifier.testTag("save_site_dialog_button")
            ) {
                Text(Localization.get("save", language), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.get("cancel", language))
            }
        }
    )
}
