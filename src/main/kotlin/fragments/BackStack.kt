package fragments

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.burnoutcrew.reorderable.move

const val BACKSTACK_MAX_INDEX = 30 - 1

class BackStack {
    val backStack = mutableListOf(emptyList<Layer>())
    var currPosition = 0
    private val currLayers =  mutableStateListOf<Layer>()
    var undoEnabled by  mutableStateOf(currPosition > 0 && backStack.isNotEmpty())
    var redoEnabled by  mutableStateOf(currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty())

    fun currLayers(): SnapshotStateList<Layer> = currLayers

    fun addCurrLayersToBackStack() {
        println("currPosition = $currPosition")
        println("backStack = $backStack")
        println("backStack.lastIndex = ${backStack.lastIndex}")

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

        backStack.add(currLayers.toList())
        currPosition++
    }

    fun addToLayerList(layer: Layer) {
        currLayers.add(layer)
        addCurrLayersToBackStack()
        updateUndoRedoEnabled()
    }

    fun deleteFromLayerList(layerIndex: Int) {
        currLayers.removeAt(layerIndex)
        addCurrLayersToBackStack()
        updateUndoRedoEnabled()
    }

    fun moveInLayerList(firstLayerIndex: Int, secondLayerIndex: Int) {
        currLayers.move(firstLayerIndex, secondLayerIndex)
        addCurrLayersToBackStack()
        updateUndoRedoEnabled()
    }

    fun undo() {
        if (currPosition != 0) {
            currPosition--
//            currLayers = backStack[currPosition].toMutableStateList()
            //todo: можно ли упростить??? как выше
            currLayers.clear()
            currLayers.addAll(backStack[currPosition])

            updateUndoRedoEnabled()
        }
    }

    fun redo() {
        if (currPosition != BACKSTACK_MAX_INDEX) {
            currPosition++
//            currLayers = backStack[currPosition].toMutableStateList()
            //todo: можно ли упростить??? как выше
            currLayers.clear()
            currLayers.addAll(backStack[currPosition])

            updateUndoRedoEnabled()
        }
    }

    fun updateUndoRedoEnabled() {
        undoEnabled = currPosition > 0 && backStack.isNotEmpty()
        redoEnabled = currPosition < backStack.size - 1 && currPosition < BACKSTACK_MAX_INDEX && backStack.isNotEmpty()
    }
}