package com.MyNote.leowu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.MyNote.leowu.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import com.MyNote.leowu.ui.theme.NoteCardBorder
import com.MyNote.leowu.ui.theme.NoteCardBorderDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    viewModel: NoteViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    val currentFilterOption by viewModel.filterOption.collectAsState()
    
    val isInSelectionMode by viewModel.isInSelectionMode.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()
    
    val filterOptions = mapOf(
        FilterOption.ALL to "全部筆記",
        FilterOption.RECENT to "最近更新",
        FilterOption.OLDEST to "最早創建"
    )
    
    val selectedFilterText = filterOptions[currentFilterOption] ?: "全部筆記"

    BackHandler(enabled = isInSelectionMode) {
        viewModel.exitSelectionMode()
    }

    Scaffold(
        topBar = {
            if (isInSelectionMode) {
                SelectionModeTopAppBar(
                    selectedCount = selectedNoteIds.size,
                    onCancelClick = { viewModel.exitSelectionMode() },
                    onDeleteClick = { viewModel.deleteSelectedNotes() }
                )
            } else {
                DefaultTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedFilterText = selectedFilterText,
                    showFilterMenu = showFilterMenu,
                    onFilterIconClick = { showFilterMenu = true },
                    onDismissFilterMenu = { showFilterMenu = false },
                    filterOptions = filterOptions,
                    onFilterOptionSelected = { option ->
                        viewModel.setFilterOption(option)
                        showFilterMenu = false
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isInSelectionMode) {
                FloatingActionButton(
                    onClick = onAddNoteClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新增筆記"
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                if (notes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "尚未有筆記",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 80.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            val isSelected = selectedNoteIds.contains(note.id)
                            NoteCard(
                                note = note,
                                searchQuery = searchQuery,
                                isSelected = isSelected,
                                isInSelectionMode = isInSelectionMode,
                                onClick = {
                                    if (isInSelectionMode) {
                                        viewModel.toggleNoteSelection(note.id)
                                    } else {
                                        onNoteClick(note)
                                    }
                                },
                                onLongClick = {
                                    if (!isInSelectionMode) {
                                        viewModel.enterSelectionMode()
                                        viewModel.toggleNoteSelection(note.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilterText: String,
    showFilterMenu: Boolean,
    onFilterIconClick: () -> Unit,
    onDismissFilterMenu: () -> Unit,
    filterOptions: Map<FilterOption, String>,
    onFilterOptionSelected: (FilterOption) -> Unit
) {
    TopAppBar(
        title = { Text("我的筆記") },
        actions = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("搜尋") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜尋")
                },
                singleLine = true
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedFilterText,
                    style = MaterialTheme.typography.labelMedium
                )
                IconButton(onClick = onFilterIconClick) {
                    Icon(Icons.Default.FilterList, contentDescription = "篩選")
                }
            }

            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = onDismissFilterMenu
            ) {
                filterOptions.forEach { (option, text) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = { onFilterOptionSelected(option) }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionModeTopAppBar(
    selectedCount: Int,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = { Text("已選取 $selectedCount 項") },
        navigationIcon = {
            IconButton(onClick = onCancelClick) {
                Icon(Icons.Default.Close, contentDescription = "取消")
            }
        },
        actions = {
            IconButton(onClick = onDeleteClick, enabled = selectedCount > 0) {
                Icon(Icons.Default.Delete, contentDescription = "刪除")
            }
        }
    )
}

private fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }
    return buildAnnotatedString {
        val lowerCaseText = text.lowercase(Locale.getDefault())
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        var currentIndex = 0
        var matchIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
        while (matchIndex >= 0) {
            append(text.substring(currentIndex, matchIndex))
            val endIndex = matchIndex + query.length
            withStyle(style = SpanStyle(background = Color(0xFFFFFF88))) {
                append(text.substring(matchIndex, endIndex))
            }
            currentIndex = endIndex
            matchIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
        }
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    searchQuery: String,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(note.timestamp))
    val isDarkTheme = isSystemInDarkTheme()
    
    val titleText = note.title.ifEmpty { "無標題筆記" }
    val highlightedTitle = highlightText(titleText, searchQuery)
    val highlightedContent = highlightText(note.content, searchQuery)
    
    Card(
        modifier = modifier
            .height(160.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary 
                    else if (isDarkTheme) NoteCardBorderDark else NoteCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (isInSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.align(Alignment.End).padding(bottom = 4.dp)
                )
            }
            
            Text(
                text = highlightedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = if (isInSelectionMode) Modifier.padding(top=0.dp) else Modifier.padding(top=26.dp)
            )
            
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = highlightedContent,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if(isInSelectionMode) 2 else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
} 