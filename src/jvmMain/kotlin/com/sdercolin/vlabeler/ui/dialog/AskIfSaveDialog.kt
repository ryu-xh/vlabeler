package com.sdercolin.vlabeler.ui.dialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sdercolin.vlabeler.ui.AppState
import com.sdercolin.vlabeler.ui.string.Strings
import com.sdercolin.vlabeler.ui.string.string

enum class AskIfSaveDialogPurpose(
    val stringKey: Strings,
    val action: AppState.PendingActionAfterSaved
) : EmbeddedDialogArgs {
    IsOpening(Strings.AskIfSaveBeforeOpenDialogDescription, AppState.PendingActionAfterSaved.Open),
    IsExporting(Strings.AskIfSaveBeforeExportDialogDescription, AppState.PendingActionAfterSaved.Export),
    IsClosing(Strings.AskIfSaveBeforeCloseDialogDescription, AppState.PendingActionAfterSaved.Close),
    IsExiting(Strings.AskIfSaveBeforeExitDialogDescription, AppState.PendingActionAfterSaved.Exit),
}

data class AskIfSaveDialogResult(
    val save: Boolean,
    val actionAfterSaved: AppState.PendingActionAfterSaved
) : EmbeddedDialogResult

@Composable
fun AskIfSaveDialog(
    args: AskIfSaveDialogPurpose,
    finish: (AskIfSaveDialogResult?) -> Unit,
) {
    val dismiss = { finish(null) }
    val submitYes = { finish(AskIfSaveDialogResult(true, args.action)) }
    val submitNo = { finish(AskIfSaveDialogResult(false, args.action)) }

    Column {
        Spacer(Modifier.height(15.dp))
        Text(
            text = string(args.stringKey),
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(25.dp))
        Row(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = { dismiss() }
            ) {
                Text(string(Strings.CommonCancel))
            }
            Spacer(Modifier.width(25.dp))
            TextButton(
                onClick = { submitNo() }
            ) {
                Text(string(Strings.CommonNo))
            }
            Spacer(Modifier.width(25.dp))
            Button(
                onClick = { submitYes() }
            ) {
                Text(string(Strings.CommonYes))
            }
        }
    }
}

@Composable
@Preview
private fun Preview() = AskIfSaveDialog(AskIfSaveDialogPurpose.IsClosing) {}