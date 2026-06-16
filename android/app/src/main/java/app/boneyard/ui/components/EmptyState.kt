package app.boneyard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.boneyard.ui.theme.BoneyardTheme

/**
 * Generic centered empty-state composable.
 *
 * [icon] is drawn at 64dp. [actionLabel] and [onAction] are optional — when
 * both are provided, a primary button is rendered below the subtitle.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(32.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // decorative — title conveys meaning
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "EmptyState with action — Light", showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun EmptyStateLightPreview() {
    BoneyardTheme(darkTheme = false) {
        EmptyState(
            icon = Icons.Outlined.SportsEsports,
            title = "No Active Games",
            subtitle = "Start a new game to begin tracking scores for you and your friends.",
            actionLabel = "New Game",
            onAction = {},
        )
    }
}

@Preview(name = "EmptyState no action — Dark", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun EmptyStateDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        EmptyState(
            icon = Icons.Outlined.SportsEsports,
            title = "No Game History",
            subtitle = "Completed games will appear here once you finish your first game.",
        )
    }
}
