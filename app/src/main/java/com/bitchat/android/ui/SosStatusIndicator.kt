package com.bitchat.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R

/**
 * SOS Status States per Section 2.3:
 * 1. TRYING_DIRECT ("Trying direct connection…")
 * 2. SENDING_VIA_MESH ("No signal — sending through nearby phones…")
 * 3. REACHED_DEVICES ("Reached N device(s), still trying to get out…")
 * 4. DELIVERED ("Delivered ✓ (confirmed [timestamp])")
 */
sealed class SosStatusState {
    object TryingDirect : SosStatusState()
    object SendingViaMesh : SosStatusState()
    data class ReachedDevices(val count: Int) : SosStatusState()
    data class Delivered(val timestamp: String) : SosStatusState()
}

@Composable
fun SosStatusIndicator(
    state: SosStatusState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        if (state == null) return@AnimatedVisibility

        val (bgColor, icon, text) = when (state) {
            is SosStatusState.TryingDirect -> Triple(
                Color(0xFFFFCC00),
                Icons.Default.CellTower,
                stringResource(R.string.sos_state_trying_direct)
            )
            is SosStatusState.SendingViaMesh -> Triple(
                Color(0xFFFF9500),
                Icons.Default.Radio,
                stringResource(R.string.sos_state_sending_mesh)
            )
            is SosStatusState.ReachedDevices -> Triple(
                Color(0xFF007AFF),
                Icons.Default.Radio,
                stringResource(R.string.sos_state_reached_devices, state.count)
            )
            is SosStatusState.Delivered -> Triple(
                Color(0xFF34C759),
                Icons.Default.CheckCircle,
                stringResource(R.string.sos_state_delivered, state.timestamp)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = bgColor,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                if (state is SosStatusState.Delivered) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.dismiss),
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
