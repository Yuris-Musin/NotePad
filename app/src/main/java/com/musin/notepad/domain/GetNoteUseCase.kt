package com.musin.notepad.domain

class GetNoteUseCase(
    private val repository: NotesRepository
) {

    operator fun invoke(noteId: Int): Note {
        return repository.getNote(noteId)
    }
}