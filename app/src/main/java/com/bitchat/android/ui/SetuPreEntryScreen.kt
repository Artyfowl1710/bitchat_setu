package com.bitchat.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.bitchat.android.features.sos.RescueBeaconManager
import com.bitchat.android.features.sos.SosManager
import com.bitchat.android.identity.SosContactStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Pre-Entry SOS Landing Screen for Setu.
 * Appears immediately after permissions are granted.
 * Allows emergency SOS, "I Am Safe" beacon, and Rescue Siren/Strobe BEFORE logging in or entering chat.
 */
@Composable
fun SetuPreEntryScreen(
    onEnterSetu: () -> Unit,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val sosContactStore = remember { SosContactStore.getInstance(context) }
    val sosManager = remember { SosManager.getInstance(context) }
    val rescueBeaconManager = remember { RescueBeaconManager.getInstance(context) }

    var showSosConfig by remember { mutableStateOf(false) }
    val sosStatus by sosManager.sosStatus.collectAsStateWithLifecycle()
    val isBeaconActive by rescueBeaconManager.isBeaconActive.collectAsStateWithLifecycle()
    var safeBeaconSent by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    // SOS Config Sheet
    if (showSosConfig) {
        SosConfigSheet(
            sosContactStore = sosContactStore,
            onDismiss = { showSosConfig = false },
            onSaved = { showSosConfig = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "🌉 " + stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.pre_entry_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Center Emergency Action Cluster
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Emergency SOS Button
                Text(
                    text = stringResource(R.string.intro_title_3),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))

                SosButton(
                    onTrigger = {
                        val contact = sosContactStore.getContact()
                        if (contact != null) {
                            sosManager.triggerSos(contact) { packet ->
                                viewModel.broadcastRawPacket(packet)
                            }
                        } else {
                            showSosConfig = true
                        }
                    },
                    onTap = { showSosConfig = true }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // SOS Status Indicator
                SosStatusIndicator(
                    state = sosStatus,
                    onDismiss = { sosManager.dismissStatus() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "I Am Safe" Button
                Button(
                    onClick = {
                        val contact = sosContactStore.getContact()
                        if (contact != null) {
                            sosManager.triggerSafeBeacon(contact) { packet ->
                                viewModel.broadcastRawPacket(packet)
                            }
                            safeBeaconSent = true
                        } else {
                            showSosConfig = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C851)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.i_am_safe),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (safeBeaconSent) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.safe_beacon_sent),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00C851),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rescue Siren & Strobe Button
                OutlinedButton(
                    onClick = { rescueBeaconManager.toggleBeacon() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isBeaconActive) Color.Red else colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = if (isBeaconActive) Icons.Default.VolumeUp else Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = if (isBeaconActive) Color.Red else colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBeaconActive) "STOP Siren & Strobe" else stringResource(R.string.rescue_siren),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Entry Button
            Button(
                onClick = onEnterSetu,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.enter_setu),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}
