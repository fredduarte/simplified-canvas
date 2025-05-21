package com.simplified.canvas.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.simplified.canvas.navigationgraph.MainGraph
import com.simplified.canvas.ui.viewmodel.CanvasViewModel
import com.simplified.canvas.ui.viewmodel.ViewState
import kotlin.math.max

@Composable
@Destination<MainGraph>(start = true)
fun CanvasScreen(
    viewModel: CanvasViewModel = hiltViewModel(),
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    CanvasScreenContent(
        viewState = viewState,
    )
}

@Composable
fun CanvasScreenContent(
    modifier: Modifier = Modifier,
    viewState: ViewState,
) {
    val minScale = 1f
    val maxScale = 4f
    val panMargin = 50f // Extra margin in pixels

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var boxSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { newSize ->
                boxSize = Size(newSize.width.toFloat(), newSize.height.toFloat())
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Clamp scale
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    // Calculate scaled canvas size
                    val scaledCanvasWidth = canvasSize.width * newScale
                    val scaledCanvasHeight = canvasSize.height * newScale

                    // Calculate max pan offsets with margin
                    val maxX = max((scaledCanvasWidth - boxSize.width) / 2f + panMargin, 0f)
                    val maxY = max((scaledCanvasHeight - boxSize.height) / 2f + panMargin, 0f)

                    // Update offset and clamp
                    val newOffset = offset + pan
                    offset = Offset(
                        x = newOffset.x.coerceIn(-maxX, maxX),
                        y = newOffset.y.coerceIn(-maxY, maxY)
                    )
                    scale = newScale
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset scale and position
                        scale = 1f
                        offset = Offset.Zero
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .clipToBounds()
                .fillMaxWidth()
                .requiredHeight(viewState.minCanvasHeight.dp)
                .background(Color.White)
                .onSizeChanged { newSize ->
                    canvasSize = Size(newSize.width.toFloat(), newSize.height.toFloat())
                },
        ) {
            for (rectangle in viewState.rectangles) {
                drawRect(
                    color = viewState.strokeColor,
                    topLeft = rectangle.rect.topLeft,
                    size = rectangle.rect.size,
                )
            }
        }
    }
}

@Preview
@Composable
fun CanvasScreenPreview() {
    CanvasScreen()
}
