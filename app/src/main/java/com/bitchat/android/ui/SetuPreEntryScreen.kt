package com.bitchat.android.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.R
import com.bitchat.android.features.sos.RescueBeaconManager
import com.bitchat.android.features.sos.SosManager
import com.bitchat.android.identity.SosContactStore
import com.bitchat.android.ui.theme.IndustrialButton
import com.bitchat.android.ui.theme.IndustrialColors
import com.bitchat.android.ui.theme.IndustrialPanel

@Composable
fun SetuPreEntryScreen(onEnterSetu: () -> Unit, viewModel: ChatViewModel) {
    val context = LocalContext.current
    val sosContactStore = remember { SosContactStore.getInstance(context) }
    val sosManager = remember { SosManager.getInstance(context) }
    val rescueBeaconManager = remember { RescueBeaconManager.getInstance(context) }
    var showSosConfig by remember { mutableStateOf(false) }
    val sosStatus by sosManager.sosStatus.collectAsStateWithLifecycle()
    val isBeaconActive by rescueBeaconManager.isBeaconActive.collectAsStateWithLifecycle()
    var safeBeaconSent by remember { mutableStateOf(false) }

    if (showSosConfig) {
        SosConfigSheet(sosContactStore = sosContactStore, onDismiss = { showSosConfig = false }, onSaved = { showSosConfig = false })
    }

    Column(
        modifier = Modifier.fillMaxSize().background(IndustrialColors.Chassis).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(IndustrialColors.Online, CircleShape))
            Text("  RESCUE CONSOLE / READY", style = MaterialTheme.typography.labelSmall, color = IndustrialColors.MutedInk)
        }
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, color = IndustrialColors.Ink)
        Spacer(Modifier.height(5.dp))
        Text(stringResource(R.string.pre_entry_subtitle), style = MaterialTheme.typography.bodyMedium, color = IndustrialColors.MutedInk, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))

        IndustrialPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(17.dp)) {
                Text("01 / EMERGENCY TRANSMIT", style = MaterialTheme.typography.labelSmall, color = IndustrialColors.MutedInk)
                Text(stringResource(R.string.intro_title_3), style = MaterialTheme.typography.headlineSmall, color = IndustrialColors.Ink, textAlign = TextAlign.Center)
                Text("HOLD TO SEND SOS", style = MaterialTheme.typography.labelMedium, color = IndustrialColors.Accent)
                SosButton(
                    modifier = Modifier.size(72.dp),
                    onTrigger = {
                        val contact = sosContactStore.getContact()
                        if (contact != null) sosManager.triggerSos(contact) { packet -> viewModel.broadcastRawPacket(packet) }
                        else showSosConfig = true
                    },
                    onTap = { showSosConfig = true }
                )
                SosStatusIndicator(state = sosStatus, onDismiss = { sosManager.dismissStatus() })
            }
        }
        Spacer(Modifier.height(26.dp))
        IndustrialButton(
            text = stringResource(R.string.i_am_safe),
            onClick = {
                val contact = sosContactStore.getContact()
                if (contact != null) {
                    sosManager.triggerSafeBeacon(contact) { packet -> viewModel.broadcastRawPacket(packet) }
                    safeBeaconSent = true
                } else showSosConfig = true
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (safeBeaconSent) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.safe_beacon_sent), style = MaterialTheme.typography.bodySmall, color = IndustrialColors.Online)
        }
        Spacer(Modifier.height(20.dp))
        IndustrialButton(
            text = if (isBeaconActive) "Stop siren & strobe" else stringResource(R.string.rescue_siren),
            onClick = { rescueBeaconManager.toggleBeacon() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(42.dp))
        Text("02 / MESH COMMUNICATION", style = MaterialTheme.typography.labelSmall, color = IndustrialColors.MutedInk)
        Spacer(Modifier.height(12.dp))
        IndustrialButton(text = stringResource(R.string.enter_setu), onClick = onEnterSetu, modifier = Modifier.fillMaxWidth(), accent = true)
        Spacer(Modifier.height(20.dp))
    }
}
