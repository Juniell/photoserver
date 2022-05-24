package fragments.photoEditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.burnoutcrew.reorderable.move

const val BACKSTACK_MAX_INDEX = 30 - 1

class BackStack {
    private val backStack = mutableListOf(emptyList<Layer>())
    private var currPosition = 0
    private var currLayers = mutableStateListOf<Layer>()
    var undoEnabled by mutableStateOf(currPosition > 0 && backStack.isNotEmpty())
    var redoEnabled by mutableStateOf(currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty())

    fun currLayers(): SnapshotStateList<Layer> = currLayers

    fun addToLayerList(layer: Layer) {
        currLayers.add(layer)
        saveLayerList()
    }

    fun deleteFromLayerList(layerIndex: Int) {
        currLayers.removeAt(layerIndex)
        saveLayerList()
    }

    fun moveInLayerList(firstLayerIndex: Int, secondLayerIndex: Int) {
        currLayers.move(firstLayerIndex, secondLayerIndex)
        saveLayerList()
    }

    fun saveLayerList() {
        addCurrLayersToBackStack()
        updateUndoRedoEnabled()
    }

    fun undo() {
        if (currPosition != 0) {
            currPosition--
            currLayers.clear()
            currLayers.addAll(backStack[currPosition])

            updateUndoRedoEnabled()
        }
    }

    fun redo() {
        if (currPosition != BACKSTACK_MAX_INDEX) {
            currPosition++
            currLayers.clear()
            currLayers.addAll(backStack[currPosition])

            updateUndoRedoEnabled()
        }
    }

    private fun addCurrLayersToBackStack() {
        if (currPosition < backStack.lastIndex) {
            var index = backStack.lastIndex
            while (index != currPosition) {
                backStack.removeLast()
                index--
            }
            currPosition = backStack.lastIndex
        }

        if (backStack.lastIndex == BACKSTACK_MAX_INDEX)
            backStack.removeAt(0)

        backStack.add(getCopyCurrList())
        currPosition++
    }

    private fun getCopyCurrList(): List<Layer> {
        val newList = mutableListOf<Layer>()
        for (layer in currLayers)
            newList.add(layer.makeCopy())
        return newList
    }

    private fun updateUndoRedoEnabled() {
        undoEnabled = currPosition > 0 && backStack.isNotEmpty()
        redoEnabled = currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty()
    }
}