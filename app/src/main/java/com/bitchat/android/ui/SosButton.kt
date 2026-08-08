package com.bitchat.android.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun SosButton(
    modifier: Modifier = Modifier,
    onTrigger: () -> Unit,
    onTap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    var showTooltip by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        label = "sos_hold_progress"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF3B30).copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        holdProgress = 0f
                        val startTime = System.currentTimeMillis()
                        val durationMs = 1500L

                        val job = scope.launch {
                            var lastHapticTime = 0L
                            while (isActive && isPressed) {
                                val elapsed = System.currentTimeMillis() - startTime
                                holdProgress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)

                                if (elapsed - lastHapticTime >= 300) {
                                    lastHapticTime = elapsed
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                }

                                if (elapsed >= durationMs) {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (_: Exception) {}
                                    onTrigger()
                                    break
                                }
                                delay(50)
                            }
                        }

                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                            job.cancel()
                            val totalTime = System.currentTimeMillis() - startTime
                            if (totalTime < 400) {
                                onTap()
                                scope.launch {
                                    showTooltip = true
                                    delay(2000)
                                    showTooltip = false
                                }
                            }
                            holdProgress = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Progress ring background
        if (animatedProgress > 0f) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFF3B30),
                strokeWidth = 4.dp
            )
        }

        // Inner solid button
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF3B30)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sos,
                contentDescription = stringResource(R.string.cd_sos_button),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        if (showTooltip) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-40).dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Text(
                    text = stringResource(R.string.sos_hold_hint),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
