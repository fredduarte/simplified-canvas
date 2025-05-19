package com.simplified.canvas.ui.viewmodel

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

    private val _viewState = MutableStateFlow(
        ViewState(
            strokeColor = Color.Black,
            minCanvasHeight = 0,
            currentTopZIndex = 0,
            rectangles = emptyList(),
        )
    )
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    init {
        _viewState.value = ViewState(
            strokeColor = Color.Black,
            minCanvasHeight = 400,
            currentTopZIndex = 2,
            rectangles = createRectangles(5, 200f, 200f),
        )
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
            zIndex = _viewState.value.currentTopZIndex + 1,
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

            currentX += rectangleWidth
            currentY += rectangleHeight
        }

        return rectangles
    }
}

data class ViewState(
    val strokeColor: Color,
    val minCanvasHeight: Int,
    val currentTopZIndex: Int, // Remove this if not needed
    val rectangles: List<Rectangle>,
)

data class Rectangle(
    val id: String,
    val rect: Rect,
    val zIndex: Int,
)
