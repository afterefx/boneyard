package app.boneyard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.boneyard.ui.theme.BoneyardTheme

// Canonical domino pip offset patterns — indexed by pip count 0-6.
// Values are (col, row) fractions of the half-tile face [0f..1f].
private val PIP_POSITIONS: Array<List<Pair<Float, Float>>> = arrayOf(
    // 0 — blank
    emptyList(),
    // 1
    listOf(0.5f to 0.5f),
    // 2
    listOf(0.25f to 0.3f, 0.75f to 0.7f),
    // 3
    listOf(0.25f to 0.25f, 0.5f to 0.5f, 0.75f to 0.75f),
    // 4
    listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.25f to 0.75f, 0.75f to 0.75f),
    // 5
    listOf(0.25f to 0.25f, 0.75f to 0.25f, 0.5f to 0.5f, 0.25f to 0.75f, 0.75f to 0.75f),
    // 6
    listOf(
        0.25f to 0.2f, 0.75f to 0.2f,
        0.25f to 0.5f, 0.75f to 0.5f,
        0.25f to 0.8f, 0.75f to 0.8f,
    ),
)

/**
 * Renders a domino tile with two halves showing pips 0-6.
 *
 * The tile is oriented vertically (top half = [topValue], bottom half = [bottomValue])
 * when [isVertical] is true (default), or horizontally otherwise.
 *
 * Uses Canvas for pixel-perfect pip rendering. Background is BoneyardBlack (#1A1A1A),
 * dots are ivory (#F5F0E8), matching the app's brand palette.
 *
 * @param topValue    pip count for the top/left half (0–6)
 * @param bottomValue pip count for the bottom/right half (0–6)
 * @param tileWidth   width of the tile; height is automatically 2× width for a
 *                    vertical tile (or vice-versa for horizontal)
 */
@Composable
fun DominoTile(
    topValue: Int,
    bottomValue: Int,
    modifier: Modifier = Modifier,
    tileWidth: Dp = 48.dp,
    isVertical: Boolean = true,
    backgroundColor: Color = Color(0xFF1A1A1A),
    pipColor: Color = Color(0xFFF5F0E8),
    dividerColor: Color = Color(0xFF3A3A3A),
) {
    val tileHeight = tileWidth * 2
    val tileBackground = backgroundColor

    val canvasModifier = if (isVertical) {
        modifier.size(width = tileWidth, height = tileHeight)
    } else {
        modifier.size(width = tileHeight, height = tileWidth)
    }

    Canvas(modifier = canvasModifier) {
        val w = size.width
        val h = size.height
        val cornerRadius = minOf(w, h) * 0.12f
        // Pip radius is proportional to the shorter dimension of a single face,
        // so it stays consistent regardless of vertical vs. horizontal orientation.
        val faceShortSide = if (isVertical) w else h
        val pipRadius = faceShortSide * 0.07f

        // Draw rounded rectangle tile background
        val tilePath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset.Zero, Size(w, h)),
                    cornerRadius = CornerRadius(cornerRadius),
                )
            )
        }
        drawPath(tilePath, tileBackground)

        // Divider line in the middle
        if (isVertical) {
            drawLine(
                color = dividerColor,
                start = Offset(w * 0.12f, h * 0.5f),
                end = Offset(w * 0.88f, h * 0.5f),
                strokeWidth = w * 0.03f,
            )
        } else {
            drawLine(
                color = dividerColor,
                start = Offset(w * 0.5f, h * 0.12f),
                end = Offset(w * 0.5f, h * 0.88f),
                strokeWidth = h * 0.03f,
            )
        }

        // Draw pips for each half
        fun drawPips(value: Int, xOffset: Float, yOffset: Float, faceW: Float, faceH: Float) {
            val pips = PIP_POSITIONS.getOrNull(value.coerceIn(0, 6)) ?: return
            val padding = faceW * 0.1f
            for ((xFrac, yFrac) in pips) {
                val px = xOffset + padding + xFrac * (faceW - 2 * padding)
                val py = yOffset + padding + yFrac * (faceH - 2 * padding)
                drawCircle(
                    color = pipColor,
                    radius = pipRadius,
                    center = Offset(px, py),
                )
            }
        }

        if (isVertical) {
            drawPips(topValue, 0f, 0f, w, h * 0.5f)
            drawPips(bottomValue, 0f, h * 0.5f, w, h * 0.5f)
        } else {
            drawPips(topValue, 0f, 0f, w * 0.5f, h)
            drawPips(bottomValue, w * 0.5f, 0f, w * 0.5f, h)
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "DominoTiles — Light", showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun DominoTilesPreview() {
    BoneyardTheme(darkTheme = false) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            DominoTile(topValue = 6, bottomValue = 6, tileWidth = 44.dp)
            DominoTile(topValue = 3, bottomValue = 3, tileWidth = 44.dp)
            DominoTile(topValue = 0, bottomValue = 0, tileWidth = 44.dp)
            DominoTile(topValue = 5, bottomValue = 2, tileWidth = 44.dp)
        }
    }
}

@Preview(name = "DominoTile horizontal — Dark", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun DominoTileHorizontalPreview() {
    BoneyardTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            DominoTile(topValue = 6, bottomValue = 6, tileWidth = 36.dp, isVertical = false)
            DominoTile(topValue = 4, bottomValue = 1, tileWidth = 36.dp, isVertical = false)
        }
    }
}
