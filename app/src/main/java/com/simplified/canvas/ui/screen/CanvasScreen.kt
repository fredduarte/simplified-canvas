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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.simplified.canvas.navigationgraph.MainGraph
import com.simplified.canvas.ui.viewmodel.CanvasAction
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
        onAction = viewModel::onAction,
    )
}

@Composable
fun CanvasScreenContent(
    modifier: Modifier = Modifier,
    viewState: ViewState,
    onAction: (CanvasAction) -> Unit,
) {
    val minScale = 1f
    val maxScale = 4f
    val panMargin = 50f

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
            .pointerInput(viewState.selectedId) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    if (viewState.selectedId != null) {
                        onAction(
                            CanvasAction.MultiTouchRectangle(
                                pan = pan / scale,
                                zoom = zoom,
                                rotation = rotation,
                            )
                        )
                    } else {
                        // Apply zoom first
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                        // Calculate the focus point (centroid) in canvas coordinates
                        val focusPoint = (centroid - offset) / scale

                        // Calculate the new offset that keeps the focus point under the finger
                        val newOffset = centroid - focusPoint * newScale

                        // Calculate scaled canvas size for bounds checking
                        val scaledCanvasWidth = canvasSize.width * newScale
                        val scaledCanvasHeight = canvasSize.height * newScale

                        // Calculate max pan offsets with margin
                        val maxX = max((scaledCanvasWidth - boxSize.width) / 2f + panMargin, 0f)
                        val maxY = max((scaledCanvasHeight - boxSize.height) / 2f + panMargin, 0f)

                        // Update scale first, then offset
                        scale = newScale

                        // Apply pan and clamp
                        offset = Offset(
                            x = (newOffset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (newOffset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    }
                }
            }
            .pointerInput(scale, offset, boxSize, canvasSize) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset scale and position
                        scale = 1f
                        offset = Offset.Zero
                    },
                    onTap = { tapOffset ->
                        // Map tap to canvas coordinates
                        val boxCenter = Offset(boxSize.width / 2, boxSize.height / 2)
                        val canvasCenter = Offset(canvasSize.width / 2, canvasSize.height / 2)
                        val canvasTapOffset = (tapOffset - boxCenter - offset) / scale + canvasCenter

                        onAction(CanvasAction.Tap(offset = canvasTapOffset))
                    },
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
                withTransform({
                    translate(left = rectangle.translation.x, top = rectangle.translation.y)
                    rotate(degrees = rectangle.rotation, pivot = rectangle.rect.center)
                    scale(
                        scaleX = rectangle.scale,
                        scaleY = rectangle.scale,
                        pivot = rectangle.rect.center,
                    )
                }) {
                    drawRect(
                        color = viewState.strokeColor,
                        topLeft = rectangle.rect.topLeft,
                        size = rectangle.rect.size,
                    )
                    if (viewState.selectedId == rectangle.id) {
                        drawRect(
                            color = Color.Red,
                            topLeft = rectangle.rect.topLeft,
                            size = rectangle.rect.size,
                            style = Stroke(width = 4f),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CanvasScreenPreview() {
    CanvasScreen()
}
