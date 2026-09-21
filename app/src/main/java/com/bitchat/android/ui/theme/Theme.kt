package com.bitchat.android.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

/** Semantic material palette for the industrial, light-only UI. */
object IndustrialColors {
    val Chassis = Color(0xFFE0E5EC)
    val Panel = Color(0xFFF0F2F5)
    val Recessed = Color(0xFFD1D9E6)
    val Ink = Color(0xFF2D3436)
    val MutedInk = Color(0xFF4A5568)
    val Accent = Color(0xFFCE2638) // Darker red keeps white labels legible.
    val AccentBright = Color(0xFFFF4757)
    val Shadow = Color(0xFFBABECC)
    val DeepShadow = Color(0xFFA3B1C6)
    val Highlight = Color.White
    val Online = Color(0xFF147D50)
}

private val IndustrialColorScheme = lightColorScheme(
    primary = IndustrialColors.Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDE1),
    onPrimaryContainer = IndustrialColors.Ink,
    secondary = IndustrialColors.Ink,
    onSecondary = Color.White,
    secondaryContainer = IndustrialColors.Recessed,
    onSecondaryContainer = IndustrialColors.Ink,
    background = IndustrialColors.Chassis,
    onBackground = IndustrialColors.Ink,
    surface = IndustrialColors.Panel,
    onSurface = IndustrialColors.Ink,
    surfaceVariant = IndustrialColors.Recessed,
    onSurfaceVariant = IndustrialColors.MutedInk,
    outline = IndustrialColors.DeepShadow,
    error = IndustrialColors.Accent,
    onError = Color.White
)

private val IndustrialShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun BitchatTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // This visual system is intentionally light-only; retain the old parameter for callers.
    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window.navigationBarColor = IndustrialColors.Chassis.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = IndustrialColorScheme,
        typography = Typography,
        shapes = IndustrialShapes,
        content = content
    )
}
