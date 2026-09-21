package com.bitchat.android.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitchat.android.R
import com.bitchat.android.ui.theme.IndustrialButton
import com.bitchat.android.ui.theme.IndustrialColors
import com.bitchat.android.ui.theme.IndustrialPanel
import kotlinx.coroutines.launch

@Composable
fun WelcomeIntroScreen(modifier: Modifier = Modifier, onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val pages = listOf(
        IntroPage(Icons.Default.CellTower, R.string.intro_title_1, R.string.intro_desc_1, "01 / MESH NETWORK"),
        IntroPage(Icons.Default.Mic, R.string.intro_title_2, R.string.intro_desc_2, "02 / VOICE LINK"),
        IntroPage(Icons.Default.Sos, R.string.intro_title_3, R.string.intro_desc_3, "03 / RESCUE SIGNAL")
    )

    Column(
        modifier = modifier.fillMaxSize().background(IndustrialColors.Chassis).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(IndustrialColors.Online, CircleShape))
            Text("  SYSTEM READY", style = MaterialTheme.typography.labelSmall, color = IndustrialColors.MutedInk)
        }
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, color = IndustrialColors.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.intro_subtitle), style = MaterialTheme.typography.bodyMedium, color = IndustrialColors.MutedInk, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
            val item = pages[page]
            Box(Modifier.fillMaxSize().padding(7.dp), contentAlignment = Alignment.Center) {
                IndustrialPanel(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(item.serial, style = MaterialTheme.typography.labelSmall, color = IndustrialColors.MutedInk)
                        Box(Modifier.size(112.dp).background(IndustrialColors.Recessed, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, tint = IndustrialColors.Accent, modifier = Modifier.size(55.dp))
                        }
                        Text(stringResource(item.title), style = MaterialTheme.typography.headlineSmall, color = IndustrialColors.Ink, textAlign = TextAlign.Center)
                        Text(stringResource(item.description), style = MaterialTheme.typography.bodyLarge, color = IndustrialColors.MutedInk, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 24.dp)) {
            repeat(3) { index ->
                Box(Modifier.size(if (index == pagerState.currentPage) 11.dp else 8.dp).background(if (index == pagerState.currentPage) IndustrialColors.Accent else IndustrialColors.DeepShadow, CircleShape))
            }
        }
        IndustrialButton(
            text = stringResource(if (pagerState.currentPage == 2) R.string.intro_get_started else R.string.intro_next),
            onClick = {
                if (pagerState.currentPage < 2) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                else onGetStarted()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            accent = pagerState.currentPage == 2
        )
    }
}

private data class IntroPage(val icon: ImageVector, val title: Int, val description: Int, val serial: String)
