package app.boneyard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.ui.theme.BoneyardTheme

// Gold color used for the winner trophy icon
private val TrophyGold = Color(0xFFFFC107)

/**
 * A single score-entry row rendered inside the active game screen.
 *
 * Layout: Avatar | Player name | Spacer | Score field (3-digit max) | Winner toggle
 *
 * - [scoreText] is the raw text value so the caller controls formatting/validation.
 * - [showError] triggers the outlined field into its error state.
 * - [isWinner] highlights the trophy icon in gold when true.
 * - [isReadOnly] disables all interactive elements (used for already-submitted rounds).
 */
@Composable
fun ScoreEntryRow(
    playerName: String,
    playerColor: Color,
    avatarIndex: Int,
    scoreText: String,
    isWinner: Boolean,
    showError: Boolean,
    onScoreChange: (String) -> Unit,
    onWinnerToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
) {
    val trophyTint by animateColorAsState(
        targetValue = if (isWinner) TrophyGold else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "trophy_tint",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        // Left: avatar
        PlayerAvatar(
            name = playerName,
            color = playerColor,
            avatarIndex = avatarIndex,
            size = AvatarSize.Small,
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Player name — takes up available space
        Text(
            text = playerName,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isReadOnly) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Score field — 3-digit numeric input.
        // Always provide supportingText so the field height stays stable whether or not
        // there is an error — avoids layout jumps when validation kicks in.
        // Disabled when the player is the round winner (winner always scores 0).
        OutlinedTextField(
            value = scoreText,
            onValueChange = { input ->
                // Accept only up to 3 digits
                if (input.length <= 3 && input.all { it.isDigit() }) {
                    onScoreChange(input)
                }
            },
            singleLine = true,
            enabled = !isReadOnly && !isWinner,
            isError = showError,
            supportingText = {
                // Always reserve the space; show the message only on error
                Text(
                    text = if (showError) "Required" else "",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(88.dp),
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Winner trophy toggle
        IconButton(
            onClick = onWinnerToggle,
            enabled = !isReadOnly,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = if (isWinner) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                contentDescription = if (isWinner) "Remove winner for $playerName"
                                     else "Mark $playerName as round winner",
                tint = trophyTint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "ScoreEntryRow — Light", showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun ScoreEntryRowLightPreview() {
    BoneyardTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ScoreEntryRow(
                playerName = "Alice",
                playerColor = Color(0xFF1E88E5),
                avatarIndex = 0,
                scoreText = "12",
                isWinner = true,
                showError = false,
                onScoreChange = {},
                onWinnerToggle = {},
            )
            ScoreEntryRow(
                playerName = "Bob",
                playerColor = Color(0xFF43A047),
                avatarIndex = 1,
                scoreText = "",
                isWinner = false,
                showError = true,
                onScoreChange = {},
                onWinnerToggle = {},
            )
            ScoreEntryRow(
                playerName = "Carol — Submitted",
                playerColor = Color(0xFFE53935),
                avatarIndex = -1,
                scoreText = "34",
                isWinner = false,
                showError = false,
                onScoreChange = {},
                onWinnerToggle = {},
                isReadOnly = true,
            )
        }
    }
}

@Preview(name = "ScoreEntryRow — Dark", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun ScoreEntryRowDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        ScoreEntryRow(
            playerName = "Dave",
            playerColor = Color(0xFF8E24AA),
            avatarIndex = 3,
            scoreText = "7",
            isWinner = false,
            showError = false,
            onScoreChange = {},
            onWinnerToggle = {},
        )
    }
}
