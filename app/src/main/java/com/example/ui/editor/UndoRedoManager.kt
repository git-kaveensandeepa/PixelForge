package com.example.ui.editor

import com.example.model.Project

class UndoRedoManager(private val maxHistory: Int = 40) {

    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun pushState(currentState: Project) {
        // Only push if different from last undo state
        if (undoStack.isNotEmpty() && undoStack.last() == currentState) {
            return
        }
        undoStack.add(currentState)
        if (undoStack.size > maxHistory) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
    }

    fun undo(currentState: Project): Project? {
        if (!canUndo) return null
        val previousState = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(currentState)
        return previousState
    }

    fun redo(currentState: Project): Project? {
        if (!canRedo) return null
        val nextState = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(currentState)
        return nextState
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
