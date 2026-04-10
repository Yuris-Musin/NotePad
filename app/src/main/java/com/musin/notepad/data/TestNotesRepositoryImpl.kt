package com.musin.notepad.data

import com.musin.notepad.domain.Note
import com.musin.notepad.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.collections.filter

object TestNotesRepositoryImpl : NotesRepository {

    private val notesList = MutableStateFlow<List<Note>>(listOf())

    override fun addNote(note: Note) {
        notesList.update {
            it + note
        }
    }

    override fun deleteNote(noteId: Int) {
        notesList.update { oldList ->
            oldList.toMutableList().apply {
                removeIf {
                    it.id == noteId
                }
            }
        }
    }

    override fun editNote(note: Note) {
        notesList.update { oldList ->
            oldList.map {
                if (it.id == note.id) {
                    note
                } else {
                    it
                }
            }
        }
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesList.asStateFlow()
    }

    override fun getNote(noteId: Int): Note {
        return notesList.value.first { it.id == noteId }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesList.map { currentList ->
            currentList.filter {
                it.title.contains(query) || it.content.contains(query)
            }
        }
    }

    override fun switchPinnedStatus(noteId: Int) {
        notesList.update { oldList ->
            oldList.map {
                if (it.id == noteId) {
                    it.copy(isPinned = !it.isPinned)
                } else {
                    it
                }
            }
        }
    }
}