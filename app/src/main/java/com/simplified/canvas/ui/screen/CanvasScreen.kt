package com.simplified.canvas.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.simplified.canvas.navigationgraph.MainGraph
import com.simplified.canvas.ui.viewmodel.CanvasViewModel

@Composable
@Destination<MainGraph>(start = true)
fun CanvasScreen(
    viewModel: CanvasViewModel = hiltViewModel(),
) {
}

@Preview
@Composable
fun CanvasScreenPreview() {
    CanvasScreen()
}
