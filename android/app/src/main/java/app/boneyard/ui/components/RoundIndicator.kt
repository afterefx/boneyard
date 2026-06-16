package app.boneyard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.ui.theme.BoneyardTheme

private const val TOTAL_ROUNDS = 14

/**
 * Returns the human-readable domino spinner label for a given round index (0-based).
 *
 * Rounds 0–6: double-6 down to double-0.
 * Rounds 7–13: double-1 back up to double-6.
 */
fun spinnerLabelForRound(roundIndex: Int): String {
    return when (roundIndex) {
        0 -> "Double-6"
        1 -> "Double-5"
        2 -> "Double-4"
        3 -> "Double-3"
        4 -> "Double-2"
        5 -> "Double-1"
        6 -> "Double-0"
        7 -> "Double-1"
        8 -> "Double-2"
        9 -> "Double-3"
        10 -> "Double-4"
        11 -> "Double-5"
        12 -> "Double-6"
        13 -> "Double-6" // final round
        else -> "Round ${roundIndex + 1}"
    }
}

/**
 * Visual progress indicator for the 14-round game.
 *
 * [completedRounds] is the number of fully submitted rounds (0–14).
 * The current round is [completedRounds] (0-based index).
 */
@Composable
fun RoundIndicator(
    completedRounds: Int,
    modifier: Modifier = Modifier,
) {
    val currentRoundIndex = completedRounds.coerceIn(0, TOTAL_ROUNDS - 1)
    val progress = completedRounds.toFloat() / TOTAL_ROUNDS.toFloat()
    val isGameComplete = completedRounds >= TOTAL_ROUNDS

    // Pulsing animation for the current-round dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Round label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (!isGameComplete) {
                Text(
                    text = "Round ${currentRoundIndex + 1} • ${spinnerLabelForRound(currentRoundIndex)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    text = "Game Complete",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "${completedRounds.coerceAtMost(TOTAL_ROUNDS)}/$TOTAL_ROUNDS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Linear progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row of 14 dot chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (i in 0 until TOTAL_ROUNDS) {
                val isFilled = i < completedRounds
                val isCurrent = i == currentRoundIndex && !isGameComplete

                RoundDot(
                    isFilled = isFilled,
                    isCurrent = isCurrent,
                    pulseAlpha = pulseAlpha,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RoundDot(
    isFilled: Boolean,
    isCurrent: Boolean,
    pulseAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val dotColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .height(8.dp)
            .clip(CircleShape)
            .then(
                when {
                    isFilled -> Modifier.background(dotColor)
                    isCurrent -> Modifier
                        .background(dotColor.copy(alpha = pulseAlpha))
                        .border(1.dp, dotColor, CircleShape)
                    else -> Modifier
                        .background(Color.Transparent)
                        .border(1.dp, outlineColor.copy(alpha = 0.5f), CircleShape)
                }
            ),
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "RoundIndicator mid-game — Light", showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun RoundIndicatorLightPreview() {
    BoneyardTheme(darkTheme = false) {
        RoundIndicator(
            completedRounds = 5,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "RoundIndicator complete — Dark", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun RoundIndicatorDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            RoundIndicator(completedRounds = 0)
            RoundIndicator(completedRounds = 8)
            RoundIndicator(completedRounds = 14)
        }
    }
}
