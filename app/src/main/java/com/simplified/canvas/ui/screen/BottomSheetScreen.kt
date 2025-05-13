package com.simplified.canvas.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.bottomsheet.spec.DestinationStyleBottomSheet
import com.simplified.canvas.navigationgraph.MainGraph

@Destination<MainGraph>(style = DestinationStyleBottomSheet::class)
@Composable
fun BottomSheetScreen() {
}

@Preview
@Composable
fun BottomSheetScreenPreview() {
    BottomSheetScreen()
}
