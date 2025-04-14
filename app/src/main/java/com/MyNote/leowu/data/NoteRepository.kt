package com.MyNote.leowu.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    
    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }
    
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
    
    suspend fun insertOrUpdateNote(note: Note) {
        noteDao.insertOrUpdateNote(note)
    }
    
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
} 