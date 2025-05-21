package com.simplified.canvas.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class CanvasViewModel @Inject constructor(
): ViewModel() {

    var currentTopZIndex: Int = 0

    private val _viewState = MutableStateFlow(
        ViewState(
            strokeColor = Color.Black,
            minCanvasHeight = 0,
            rectangles = emptyList(),
            selectedId = null,
        )
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    init {
        _viewState.value = ViewState(
            strokeColor = Color.Black,
            minCanvasHeight = 400,
            rectangles = createRectangles(5, 200f, 200f),
            selectedId = null,
        )
    }

    fun onAction(action: CanvasAction) {
        when (action) {
            is CanvasAction.Tap -> handleTap(canvasTapOffset = action.offset)
            is CanvasAction.MultiTouchRectangle -> handleMultiTouchRectangle(action)
        }
    }

    private fun handleTap(canvasTapOffset: Offset) {
        val found = _viewState.value.rectangles
            .sortedByDescending { it.zIndex }
            .find { rect ->
                val local = mapPointToRectLocalSpace(canvasTapOffset, rect)
                rect.rect.contains(local)
            }

        _viewState.value = _viewState.value.copy(selectedId = found?.id)
    }

    private fun mapPointToRectLocalSpace(point: Offset, rect: Rectangle): Offset {
        // Inverse translation
        var local = point - rect.translation
        // Inverse rotation (around rect center)
        local = rotatePoint(local, -rect.rotation, rect.rect.center)
        // Inverse scale (around rect center)
        local = scalePoint(local, if (rect.scale != 0f) 1f / rect.scale else 1f, rect.rect.center)
        return local
    }

    private fun rotatePoint(point: Offset, degrees: Float, pivot: Offset): Offset {
        val rad = Math.toRadians(degrees.toDouble())
        val cos = kotlin.math.cos(rad)
        val sin = kotlin.math.sin(rad)
        val dx = point.x - pivot.x
        val dy = point.y - pivot.y
        val x = dx * cos - dy * sin + pivot.x
        val y = dx * sin + dy * cos + pivot.y
        return Offset(x.toFloat(), y.toFloat())
    }

    private fun scalePoint(point: Offset, scale: Float, pivot: Offset): Offset {
        val dx = point.x - pivot.x
        val dy = point.y - pivot.y
        return Offset(pivot.x + dx * scale, pivot.y + dy * scale)
    }

    private fun handleMultiTouchRectangle(action: CanvasAction.MultiTouchRectangle) {
        val selectedId = _viewState.value.selectedId ?: return

        val updatedRectangles = _viewState.value.rectangles.map { rect ->
            if (rect.id == selectedId) {
                val minZoom = 0.2f
                val maxZoom = 5f

                rect.copy(
                    translation = rect.translation + action.pan,
                    scale = (rect.scale * action.zoom).coerceIn(minZoom, maxZoom),
                    rotation = rect.rotation + action.rotation,
                )
            } else {
                rect
            }
        }

        _viewState.value = _viewState.value.copy(rectangles = updatedRectangles)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createNewRectangle(
        topLeftX: Float,
        topLeftY: Float,
        width: Float,
        height: Float,
    ): Rectangle {
        return Rectangle(
            id = Uuid.random().toString(),
            rect = Rect(
                topLeftX,
                topLeftY,
                topLeftX + width,
                topLeftY + height,
            ),
            zIndex = ++currentTopZIndex,
            translation = Offset.Zero,
            scale = 1f,
            rotation = 0f,
        )
    }

    private fun createRectangles(
        n: Int = 3,
        rectangleWidth: Float = 100f,
        rectangleHeight: Float = 100f,
    ): List<Rectangle> {
        val rectangles = mutableListOf<Rectangle>()
        var currentX = 0f
        var currentY = 0f

        for (i in 1..n) {
            rectangles.add(
                createNewRectangle(
                    topLeftX = currentX,
                    topLeftY = currentY,
                    width = rectangleWidth,
                    height = rectangleHeight,
                )
            )

            currentX += rectangleWidth / 1.5f
            currentY += rectangleHeight / 1.5f
        }

        return rectangles
    }
}

data class ViewState(
    val strokeColor: Color,
    val minCanvasHeight: Int,
    val rectangles: List<Rectangle>,
    val selectedId: String?,
)

sealed interface CanvasAction {
    data class Tap(val offset: Offset) : CanvasAction
    data class MultiTouchRectangle(
        val pan: Offset,
        val zoom: Float,
        val rotation: Float
    ) : CanvasAction
}

data class Rectangle(
    val id: String,
    val rect: Rect,
    val zIndex: Int,
    val translation: Offset,
    val scale: Float,
    val rotation: Float,
)
