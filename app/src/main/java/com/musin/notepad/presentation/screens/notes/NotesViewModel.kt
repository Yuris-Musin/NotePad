package com.musin.notepad.presentation.screens.notes

import androidx.lifecycle.ViewModel
import com.musin.notepad.data.TestNotesRepositoryImpl
import com.musin.notepad.domain.AddNoteUseCase
import com.musin.notepad.domain.DeleteNoteUseCase
import com.musin.notepad.domain.EditNoteUseCase
import com.musin.notepad.domain.GetAllNotesUseCase
import com.musin.notepad.domain.GetNoteUseCase
import com.musin.notepad.domain.Note
import com.musin.notepad.domain.SearchNotesUseCase
import com.musin.notepad.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(): ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val addNotesUseCase = AddNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val editNoteUseCase = EditNoteUseCase(repository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        query
            .flatMapLatest {
                if (it.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNotesUseCase(it)
                }
            }
            .onEach {
                val pinnedNotes = it.filter { it.isPinned}
                val otherNotes = it.filter { !it.isPinned}
                _state.update { it.copy(pinnedNotes = pinnedNotes, otherNotes = otherNotes) }
            }
            .launchIn(scope)
    }

    fun processCommand(command: NotesCommand) {
        when(command) {
            is NotesCommand.DeleteNote -> {
                deleteNoteUseCase(command.noteId)
            }
            is NotesCommand.EditNote -> {
                val title = command.note.title
                editNoteUseCase(command.note.copy(title = "$title edited"))
            }
            is NotesCommand.InputSearchQuery -> {  }
            is NotesCommand.SwitchPinnedStatus -> {
                switchPinnedStatusUseCase(command.noteId)
            }
        }
    }

}

sealed interface NotesCommand {

    data class InputSearchQuery(val query: String): NotesCommand

    data class SwitchPinnedStatus(val noteId: Int): NotesCommand

    //Temp

    data class DeleteNote(val noteId: Int): NotesCommand

    data class EditNote(val note: Note): NotesCommand
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf()
)