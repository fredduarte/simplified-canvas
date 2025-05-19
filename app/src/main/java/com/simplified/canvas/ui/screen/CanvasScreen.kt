package com.simplified.canvas.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.simplified.canvas.navigationgraph.MainGraph
import com.simplified.canvas.ui.viewmodel.CanvasViewModel
import com.simplified.canvas.ui.viewmodel.ViewState

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .clipToBounds()
                .fillMaxWidth()
                .requiredHeight(viewState.minCanvasHeight.dp)
                .background(Color.White),
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
