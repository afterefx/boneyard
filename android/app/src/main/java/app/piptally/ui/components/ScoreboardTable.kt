package app.piptally.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.piptally.ui.theme.PipTallyTheme

// Subtle tint color for the leading player row — green is universally understood as "winning"
// This is intentionally a fixed color (not the theme primary) because it conveys a specific
// semantic meaning (leader = lowest score) regardless of brand palette.
private val LeaderTint = Color(0xFF43A047).copy(alpha = 0.12f)

/**
 * Data representing a single player's standing in the scoreboard.
 *
 * [rank] is 1-based (1 = lowest score = winning).
 */
data class ScoreboardEntry(
    val playerId: Long,
    val playerName: String,
    val playerColor: Color,
    val avatarIndex: Int,
    val totalScore: Int,
    val rank: Int,
)

/**
 * Compact scoreboard table sorted by totalScore ascending (rank 1 = leader).
 * The leading player's row receives a subtle green tint highlight.
 *
 * Pass [entries] already sorted by totalScore ascending; this composable
 * respects whatever order it receives to allow the caller to control ties.
 */
@Composable
fun ScoreboardTable(
    entries: List<ScoreboardEntry>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            // Header row
            ScoreboardHeaderRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            entries.forEachIndexed { index, entry ->
                ScoreboardPlayerRow(
                    entry = entry,
                    isLeader = entry.rank == 1,
                )
                if (index < entries.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreboardHeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Player",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Score",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScoreboardPlayerRow(
    entry: ScoreboardEntry,
    isLeader: Boolean,
) {
    val rowBackground = if (isLeader) LeaderTint else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            // animateItem handles position transitions (reordering animation)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        // Rank badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isLeader) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Text(
                text = "${entry.rank}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isLeader) FontWeight.Bold else FontWeight.Normal,
                // Use theme-adaptive colors for rank text so it reads well in dark mode
                color = if (isLeader) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        PlayerAvatar(
            name = entry.playerName,
            color = entry.playerColor,
            avatarIndex = entry.avatarIndex,
            size = AvatarSize.Small,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = entry.playerName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isLeader) FontWeight.SemiBold else FontWeight.Normal,
            // Leader uses onSurface (full opacity) to stand out from the onSurfaceVariant
            // used by non-leader rows — no color needed beyond the weight difference
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        // Total score — leader score uses primary so it adapts to dark mode
        Text(
            text = "${entry.totalScore}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isLeader) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewEntries = listOf(
    ScoreboardEntry(1L, "Alice", Color(0xFF1E88E5), 0, 42, 1),
    ScoreboardEntry(2L, "Bob", Color(0xFF43A047), 1, 67, 2),
    ScoreboardEntry(3L, "Carol", Color(0xFFE53935), -1, 89, 3),
    ScoreboardEntry(4L, "Dave", Color(0xFF8E24AA), 3, 104, 4),
)

@Preview(name = "ScoreboardTable — Light", showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun ScoreboardTableLightPreview() {
    PipTallyTheme(darkTheme = false) {
        ScoreboardTable(
            entries = previewEntries,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ScoreboardTable — Dark", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun ScoreboardTableDarkPreview() {
    PipTallyTheme(darkTheme = true) {
        ScoreboardTable(
            entries = previewEntries,
            modifier = Modifier.padding(16.dp),
        )
    }
}
