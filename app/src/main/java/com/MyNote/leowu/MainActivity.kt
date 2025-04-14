package com.MyNote.leowu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.MyNote.leowu.data.Note
import com.MyNote.leowu.ui.FilterOption
import com.MyNote.leowu.ui.HomeScreen
import com.MyNote.leowu.ui.NoteScreen
import com.MyNote.leowu.ui.NoteViewModel
import com.MyNote.leowu.ui.theme.MyNoteTheme

class MainActivity : ComponentActivity() {
    
    private val noteViewModel: NoteViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val notes by noteViewModel.allNotes.collectAsState()
                    var currentScreen by remember { mutableStateOf(Screen.HOME) }
                    var selectedNote by remember { mutableStateOf<Note?>(null) }
                    
                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                notes = notes,
                                viewModel = noteViewModel,
                                onAddNoteClick = { 
                                    selectedNote = null
                                    currentScreen = Screen.EDITOR 
                                },
                                onNoteClick = { note ->
                                    selectedNote = note
                                    currentScreen = Screen.EDITOR
                                }
                            )
                        }
                        Screen.EDITOR -> {
                            NoteScreen(
                                viewModel = noteViewModel,
                                initialNote = selectedNote,
                                onBackClick = {
                                    currentScreen = Screen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    HOME, EDITOR
}