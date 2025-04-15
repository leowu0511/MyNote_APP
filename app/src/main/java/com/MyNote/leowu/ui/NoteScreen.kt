package com.MyNote.leowu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.MyNote.leowu.data.Note
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel,
    initialNote: Note?,
    onBackClick: () -> Unit
) {
    val isEditMode = initialNote != null
    var noteTitle by remember { mutableStateOf(initialNote?.title ?: "") }
    var noteContent by remember { mutableStateOf(initialNote?.content ?: "") }
    val screenTitle = if (isEditMode) "編輯筆記" else "新增筆記"
    
    // 用於追蹤內容是否已變更
    var contentChanged by remember { mutableStateOf(false) }
    var lastSavedTitle by remember { mutableStateOf(noteTitle) }
    var lastSavedContent by remember { mutableStateOf(noteContent) }
    
    // 用於顯示自動儲存訊息
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 最新的標題和內容狀態
    val currentTitle by rememberUpdatedState(noteTitle)
    val currentContent by rememberUpdatedState(noteContent)
    
    // 獲取自動儲存設定
    val isAutoSaveEnabled by viewModel.isAutoSaveEnabled.collectAsState()
    
    // 處理手機返回鍵 - 只在啟用自動儲存時才自動儲存和顯示通知
    BackHandler {
        // 使用 first() 方法獲取最新狀態
        val shouldAutoSave = try {
            viewModel.isAutoSaveEnabled.value && contentChanged && 
            (noteTitle.isNotBlank() || noteContent.isNotBlank())
        } catch (e: Exception) {
            false // 安全處理
        }
        
        if (shouldAutoSave) {
            if (isEditMode) {
                viewModel.updateNote(initialNote!!, noteTitle, noteContent)
            } else {
                viewModel.insertNote(noteTitle, noteContent)
            }
            // 僅在自動儲存啟用時顯示通知
            viewModel.showAutoSaveNotification()
        }
        onBackClick()
    }
    
    // 自動儲存功能 - 監聽設定狀態和內容變更
    LaunchedEffect(currentTitle, currentContent) {
        // 內容變更時，標記為已變更
        if ((currentTitle != lastSavedTitle || currentContent != lastSavedContent) &&
            (currentTitle.isNotBlank() || currentContent.isNotBlank())) {
            contentChanged = true
            
            // 如果自動儲存未啟用，則不執行自動儲存
            if (!viewModel.isAutoSaveEnabled.value) {
                return@LaunchedEffect
            }
            
            // 延遲 2 秒後自動儲存
            delay(2000)
            
            // 再次檢查自動儲存是否仍然啟用
            val autoSaveStillEnabled = try {
                viewModel.isAutoSaveEnabled.value
            } catch (e: Exception) {
                false // 安全處理
            }
            
            // 只有在自動儲存仍然啟用時才執行儲存
            if (contentChanged && autoSaveStillEnabled && 
                (currentTitle.isNotBlank() || currentContent.isNotBlank())) {
                if (isEditMode) {
                    viewModel.updateNote(initialNote!!, currentTitle, currentContent)
                } else {
                    viewModel.insertNote(currentTitle, currentContent)
                }
                lastSavedTitle = currentTitle
                lastSavedContent = currentContent
                contentChanged = false
                // 不在這裡顯示通知，避免干擾編輯
            }
        }
    }
    
    // 監聽自動儲存設定變更
    LaunchedEffect(isAutoSaveEnabled) {
        // 當自動儲存設定變為關閉時，主動清除任何可能的通知
        if (!isAutoSaveEnabled) {
            // 確保沒有通知會被顯示
            viewModel.clearSaveNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        // 同樣使用最新的設定狀態進行判斷
                        val shouldAutoSave = try {
                            viewModel.isAutoSaveEnabled.value && contentChanged && 
                            (noteTitle.isNotBlank() || noteContent.isNotBlank())
                        } catch (e: Exception) {
                            false // 安全處理
                        }
                        
                        if (shouldAutoSave) {
                            if (isEditMode) {
                                viewModel.updateNote(initialNote!!, noteTitle, noteContent)
                            } else {
                                viewModel.insertNote(noteTitle, noteContent)
                            }
                            // 僅在自動儲存啟用時顯示通知
                            viewModel.showAutoSaveNotification()
                        }
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { 
                            viewModel.deleteNote(initialNote!!)
                            onBackClick()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "刪除筆記"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isEditMode) {
                        viewModel.updateNote(initialNote!!, noteTitle, noteContent)
                    } else {
                        viewModel.insertNote(noteTitle, noteContent)
                    }
                    lastSavedTitle = noteTitle
                    lastSavedContent = noteContent
                    contentChanged = false
                    viewModel.showManualSaveNotification()
                    onBackClick()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "儲存"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 標題輸入框
            TextField(
                value = noteTitle,
                onValueChange = { 
                    noteTitle = it
                    contentChanged = true
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("標題") },
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 內容輸入框
            TextField(
                value = noteContent,
                onValueChange = { 
                    noteContent = it
                    contentChanged = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("寫下您的想法...") },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
} 