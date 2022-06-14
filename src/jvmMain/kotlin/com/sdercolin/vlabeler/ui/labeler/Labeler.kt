package com.sdercolin.vlabeler.ui.labeler

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sdercolin.vlabeler.audio.PlayerState
import com.sdercolin.vlabeler.env.KeyboardState
import com.sdercolin.vlabeler.model.AppConf
import com.sdercolin.vlabeler.model.LabelerConf
import com.sdercolin.vlabeler.model.Sample
import com.sdercolin.vlabeler.ui.dialog.SetResolutionDialog
import com.sdercolin.vlabeler.util.update

@Composable
fun Labeler(
    sample: Sample,
    appConf: AppConf,
    labelerConf: LabelerConf,
    playerState: PlayerState,
    playSampleSection: (Float, Float) -> Unit,
    keyboardState: KeyboardState
) {
    val scrollState = rememberScrollState(0)
    val currentDensity = LocalDensity.current
    val resolutionRange = CanvasParams.ResolutionRange(appConf.painter.canvasResolution)
    val canvasParamsState = remember {
        mutableStateOf(
            CanvasParams(
                sample.wave.length,
                appConf.painter.canvasResolution.default,
                currentDensity
            )
        )
    }
    var setResolutionDialogShown by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Scroller(
                sample = sample,
                appConf = appConf,
                labelerConf = labelerConf,
                playerState = playerState,
                playSampleSection = playSampleSection,
                keyboardState = keyboardState,
                canvasParams = canvasParamsState.value,
                horizontalScrollState = scrollState
            )
        }
        HorizontalScrollbar(
            modifier = Modifier.fillMaxWidth().height(20.dp),
            adapter = rememberScrollbarAdapter(scrollState)
        )
        StatusBar(
            resolutionRange = resolutionRange,
            openSetResolutionDialog = { setResolutionDialogShown = true },
            resolution = canvasParamsState.value.resolution
        ) { canvasParamsState.update { copy(resolution = it) } }
    }
    if (setResolutionDialogShown) {
        SetResolutionDialog(
            current = canvasParamsState.value.resolution,
            min = resolutionRange.min,
            max = resolutionRange.max,
            dismiss = {
                setResolutionDialogShown = false
            },
            submit = {
                setResolutionDialogShown = false
                canvasParamsState.update { copy(resolution = it) }
            }
        )
    }
}

@Composable
private fun StatusBar(
    resolutionRange: CanvasParams.ResolutionRange,
    openSetResolutionDialog: () -> Unit,
    resolution: Int,
    onChangeResolution: (Int) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth().height(30.dp).padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(30.dp)
                .clickable(
                    enabled = resolutionRange.canIncrease(resolution),
                    onClick = { onChangeResolution(resolutionRange.increaseFrom(resolution)) }
                )
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "-"
            )
        }
        Text(
            modifier = Modifier.clickable { openSetResolutionDialog() }
                .defaultMinSize(minWidth = 55.dp),
            text = "1/$resolution",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Box(
            Modifier.size(30.dp)
                .clickable(
                    enabled = resolutionRange.canDecrease(resolution),
                    onClick = { onChangeResolution(resolutionRange.decreaseFrom(resolution)) }
                )
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "+"
            )
        }
    }
}

@Composable
@Preview
private fun StatusBarPreview() = StatusBar(CanvasParams.ResolutionRange(AppConf.CanvasResolution()), {}, 500) {}