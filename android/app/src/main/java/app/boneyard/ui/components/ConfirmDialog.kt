package app.boneyard.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.boneyard.ui.theme.BoneyardTheme

/**
 * Reusable confirmation dialog.
 *
 * [isDestructive] switches the confirm button text color to the error color
 * to signal a destructive action (e.g. delete, abandon game).
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "ConfirmDialog standard — Light", showBackground = true)
@Composable
private fun ConfirmDialogStandardLightPreview() {
    BoneyardTheme(darkTheme = false) {
        ConfirmDialog(
            title = "Pause Game?",
            message = "The game will be saved and you can resume it later from the home screen.",
            confirmText = "Pause",
            dismissText = "Keep Playing",
            onConfirm = {},
            onDismiss = {},
            isDestructive = false,
        )
    }
}

@Preview(name = "ConfirmDialog destructive — Dark", showBackground = true)
@Composable
private fun ConfirmDialogDestructiveDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        ConfirmDialog(
            title = "Abandon Game?",
            message = "This will permanently delete all scores for this game. This action cannot be undone.",
            confirmText = "Abandon",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}
