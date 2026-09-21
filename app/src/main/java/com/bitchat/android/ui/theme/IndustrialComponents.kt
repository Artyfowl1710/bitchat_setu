package com.bitchat.android.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** A raised, bolted module. The bolt heads are purely decorative. */
@Composable
fun IndustrialPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(modifier = modifier) {
        Box(Modifier.matchParentSize().offset(5.dp, 5.dp).background(IndustrialColors.Shadow, shape))
        Box(Modifier.matchParentSize().offset((-3).dp, (-3).dp).background(IndustrialColors.Highlight, shape))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialColors.Panel, shape)
                .border(1.dp, IndustrialColors.Highlight, shape)
        ) {
            Canvas(Modifier.matchParentSize()) {
                val inset = 13.dp.toPx()
                val bolt = 3.dp.toPx()
                val points = listOf(
                    Offset(inset, inset), Offset(size.width - inset, inset),
                    Offset(inset, size.height - inset), Offset(size.width - inset, size.height - inset)
                )
                points.forEach { point ->
                    drawCircle(IndustrialColors.DeepShadow, bolt, point)
                    drawCircle(IndustrialColors.Highlight, bolt * 0.45f, point - Offset(0.8.dp.toPx(), 0.8.dp.toPx()))
                }
            }
            Box(Modifier.padding(24.dp), content = content)
        }
    }
}

/** Physical key with pressed travel and a 48dp minimum touch target. */
@Composable
fun IndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val travel by animateDpAsState(if (pressed) 2.dp else 0.dp, label = "key_travel")
    val shape = RoundedCornerShape(14.dp)
    val face = if (accent) IndustrialColors.Accent else IndustrialColors.Chassis
    val ink = if (accent) Color.White else IndustrialColors.Ink
    Box(modifier = modifier.defaultMinSize(minHeight = 50.dp)) {
        Box(Modifier.matchParentSize().offset(4.dp, 4.dp).background(if (accent) Color(0xFFA51A2B) else IndustrialColors.Shadow, shape))
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(travel, travel)
                .background(face, shape)
                .border(1.dp, if (accent) IndustrialColors.AccentBright else IndustrialColors.Highlight, shape)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, color = ink, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}
