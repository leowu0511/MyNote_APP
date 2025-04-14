package com.MyNote.leowu.ui

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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    
    val filterOptions = mapOf(
        FilterOption.ALL to "全部筆記",
        FilterOption.RECENT to "最近更新",
        FilterOption.OLDEST to "最早創建"
    )
    
    val selectedFilterText = filterOptions[currentFilterOption] ?: "全部筆記"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的筆記") }
            )
        },
        floatingActionButton = {
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
                    .padding(16.dp)
            ) {
                // 搜尋欄和篩選選單
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("搜尋筆記") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "搜尋")
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 篩選按鈕與下拉選單
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedFilterText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "篩選")
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        filterOptions.forEach { (option, text) ->
                            DropdownMenuItem(
                                text = { Text(text) },
                                onClick = {
                                    viewModel.setFilterOption(option)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 筆記網格
                if (notes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
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
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notes) { note ->
                            NoteCard(
                                note = note, 
                                searchQuery = searchQuery,
                                onClick = { onNoteClick(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 產生帶有高亮效果的 AnnotatedString
private fun highlightText(text: String, query: String): AnnotatedString {
    // 如果搜尋關鍵字為空，直接返回原文本
    if (query.isBlank()) {
        return AnnotatedString(text)
    }
    
    return buildAnnotatedString {
        val lowerCaseText = text.lowercase(Locale.getDefault())
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        
        // 跟踪當前處理到的索引位置
        var currentIndex = 0
        
        // 找出所有匹配的索引
        var matchIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
        while (matchIndex >= 0) {
            // 添加匹配前的一般文本
            append(text.substring(currentIndex, matchIndex))
            
            // 添加高亮的匹配文本
            val endIndex = matchIndex + query.length
            withStyle(
                style = SpanStyle(
                    background = Color(0xFFFFFF88)  // 淡黃色背景
                )
            ) {
                append(text.substring(matchIndex, endIndex))
            }
            
            // 更新當前處理位置
            currentIndex = endIndex
            
            // 查找下一個匹配
            matchIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
        }
        
        // 添加剩餘的文本
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    searchQuery: String,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(note.timestamp))
    val isDarkTheme = isSystemInDarkTheme()
    
    // 計算高亮後的標題文本
    val titleText = note.title.ifEmpty { "無標題筆記" }
    val highlightedTitle = highlightText(titleText, searchQuery)
    
    // 計算高亮後的內容文本
    val highlightedContent = highlightText(note.content, searchQuery)
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 0.5.dp, 
            color = if (isDarkTheme) NoteCardBorderDark else NoteCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 標題
            Text(
                text = highlightedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // 時間
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 內容
            Text(
                text = highlightedContent,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 