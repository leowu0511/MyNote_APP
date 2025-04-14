package com.MyNote.leowu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.MyNote.leowu.data.Note
import com.MyNote.leowu.data.NoteDatabase
import com.MyNote.leowu.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteRepository
    val allNotes: StateFlow<List<Note>>
    
    // 搜尋和篩選狀態
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption

    // *** 多選狀態 ***
    private val _isInSelectionMode = MutableStateFlow(false)
    val isInSelectionMode: StateFlow<Boolean> = _isInSelectionMode.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNoteIds: StateFlow<Set<Int>> = _selectedNoteIds.asStateFlow()
    
    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        
        allNotes = combine(
            repository.allNotes,
            searchQuery,
            filterOption
        ) { notes, query, filter ->
            notes.filter { 
                if (query.isNotBlank()) {
                    it.title.contains(query, ignoreCase = true) || 
                    it.content.contains(query, ignoreCase = true)
                } else {
                    true
                }
            }.let { filteredNotes ->
                when (filter) {
                    FilterOption.ALL -> filteredNotes
                    FilterOption.RECENT -> filteredNotes.sortedByDescending { it.timestamp }
                    FilterOption.OLDEST -> filteredNotes.sortedBy { it.timestamp }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setFilterOption(option: FilterOption) {
        _filterOption.value = option
    }

    // *** 多選模式操作 ***
    fun enterSelectionMode() {
        _isInSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isInSelectionMode.value = false
        _selectedNoteIds.value = emptySet() // 退出時清空選項
    }

    fun toggleNoteSelection(noteId: Int) {
        _selectedNoteIds.update { currentIds ->
            if (currentIds.contains(noteId)) {
                currentIds - noteId
            } else {
                currentIds + noteId
            }
        }
        // 如果取消選擇後沒有選中的項目了，自動退出選擇模式
        if (_selectedNoteIds.value.isEmpty()) {
            exitSelectionMode()
        }
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val idsToDelete = _selectedNoteIds.value
            // 為了安全，獲取需要刪除的完整 Note 物件
            // 這裡假設 allNotes 包含所有可能的筆記，如果筆記很多可能需要優化
            val notesToDelete = allNotes.value.filter { idsToDelete.contains(it.id) }
            notesToDelete.forEach { repository.deleteNote(it) }
            exitSelectionMode() // 刪除後退出選擇模式
        }
    }
    
    // --- 現有筆記操作 ---
    fun insertNote(title: String, content: String) {
        if (title.isNotBlank() || content.isNotBlank()) {
            viewModelScope.launch {
                repository.insertNote(Note(
                    title = title,
                    content = content
                ))
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun updateNote(note: Note, newTitle: String, newContent: String) {
        if (newTitle.isNotBlank() || newContent.isNotBlank()) {
            viewModelScope.launch {
                val updatedNote = note.copy(
                    title = newTitle,
                    content = newContent,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
            }
        }
    }
}

enum class FilterOption {
    ALL, RECENT, OLDEST
} 