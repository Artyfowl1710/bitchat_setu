package com.bitchat.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.bitchat.android.identity.SosContact
import com.bitchat.android.identity.SosContactStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosConfigSheet(
    sosContactStore: SosContactStore,
    onDismiss: () -> Unit,
    onSaved: (SosContact) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val existing = remember { sosContactStore.getContact() }

    var phoneNumber by remember { mutableStateOf(existing?.phoneNumber ?: "+91") }
    var defaultMessage by remember {
        mutableStateOf(existing?.defaultMessage ?: "EMERGENCY: I need assistance. Please send help.")
    }
    var includeLocation by remember { mutableStateOf(existing?.includeLocation ?: true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.sos_config_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.sos_config_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    errorText = null
                },
                label = { Text(stringResource(R.string.sos_phone_label), fontFamily = FontFamily.Monospace) },
                placeholder = { Text("+91 9876543210", fontFamily = FontFamily.Monospace) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorText != null
            )

            OutlinedTextField(
                value = defaultMessage,
                onValueChange = { if (it.length <= 160) defaultMessage = it },
                label = { Text(stringResource(R.string.sos_message_label), fontFamily = FontFamily.Monospace) },
                supportingText = {
                    Text(
                        text = "${defaultMessage.length}/160",
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.sos_include_location),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = colorScheme.onSurface
                )
                Switch(
                    checked = includeLocation,
                    onCheckedChange = { includeLocation = it }
                )
            }

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel), fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = {
                        val cleanedPhone = phoneNumber.trim()
                        if (cleanedPhone.length < 8 || !cleanedPhone.startsWith("+")) {
                            errorText = "Please enter a valid phone number with country code (e.g. +91...)"
                            return@Button
                        }
                        val contact = SosContact(cleanedPhone, defaultMessage.trim(), includeLocation)
                        sosContactStore.saveContact(contact)
                        onSaved(contact)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text(
                        text = stringResource(R.string.sos_save_button),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
