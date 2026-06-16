package app.boneyard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.boneyard.ui.theme.BoneyardTheme

// Emoji options indexed 0–5; avatarIndex outside this range falls back to initials
private val AVATAR_EMOJIS = listOf("🎲", "🃏", "♟️", "🏆", "🎯", "⭐")

enum class AvatarSize(
    val dp: Dp,
    val fontSize: TextUnit,
    val borderWidth: Dp,
    val shadowElevation: Dp,
) {
    Small(32.dp, 13.sp, 1.5.dp, 2.dp),
    Medium(48.dp, 18.sp, 2.dp, 3.dp),
    Large(64.dp, 24.sp, 2.dp, 4.dp),
    XLarge(96.dp, 36.sp, 3.dp, 6.dp),
}

/**
 * Circular player avatar that shows either an emoji (by avatarIndex) or the
 * player's first-letter initial when the index is out of the emoji list range.
 *
 * [color] should be one of the PlayerColors defined in Color.kt or any
 * Color parsed from the stored hex string.
 */
@Composable
fun PlayerAvatar(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    avatarIndex: Int = -1,
    size: AvatarSize = AvatarSize.Medium,
) {
    val hasEmoji = avatarIndex in AVATAR_EMOJIS.indices
    val label = if (hasEmoji) {
        AVATAR_EMOJIS[avatarIndex]
    } else {
        name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }

    // Compute a readable text color — use white for dark backgrounds, black for light ones
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    val contentColor = if (luminance < 0.5f) Color.White else Color(0xFF1A1A1A)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .semantics { contentDescription = "$name avatar" }
            .shadow(
                elevation = size.shadowElevation,
                shape = CircleShape,
                ambientColor = color.copy(alpha = 0.3f),
                spotColor = color.copy(alpha = 0.4f),
            )
            .clip(CircleShape)
            .background(color)
            .border(
                width = size.borderWidth,
                color = Color.White.copy(alpha = 0.35f),
                shape = CircleShape,
            ),
    ) {
        Text(
            text = label,
            fontSize = size.fontSize,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Avatar sizes — Light", showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun PlayerAvatarSizesLightPreview() {
    BoneyardTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.then(Modifier),
        ) {
            PlayerAvatar(name = "Alice", color = Color(0xFF1E88E5), avatarIndex = 0, size = AvatarSize.Small)
            PlayerAvatar(name = "Bob", color = Color(0xFF43A047), avatarIndex = 1, size = AvatarSize.Medium)
            PlayerAvatar(name = "Carol", color = Color(0xFFE53935), avatarIndex = -1, size = AvatarSize.Large)
            PlayerAvatar(name = "Dave", color = Color(0xFF8E24AA), avatarIndex = 3, size = AvatarSize.XLarge)
        }
    }
}

@Preview(name = "Avatar sizes — Dark", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun PlayerAvatarSizesDarkPreview() {
    BoneyardTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerAvatar(name = "Alice", color = Color(0xFFFB8C00), avatarIndex = 4, size = AvatarSize.Small)
            PlayerAvatar(name = "Bob", color = Color(0xFF00ACC1), avatarIndex = 5, size = AvatarSize.Medium)
            PlayerAvatar(name = "Carol", color = Color(0xFFE91E63), avatarIndex = -1, size = AvatarSize.Large)
            PlayerAvatar(name = "Dave", color = Color(0xFF6D4C41), avatarIndex = 2, size = AvatarSize.XLarge)
        }
    }
}
