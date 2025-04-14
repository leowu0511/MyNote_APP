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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteRepository
    val allNotes: StateFlow<List<Note>>
    
    // 搜尋和篩選狀態
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption
    
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