package com.bitchat.android.identity

/**
 * Data model for the user's optional emergency contact.
 * STRICT IDENTITY CONSTRAINT: This information must ONLY be used for explicitly triggered
 * SOS emergency relays and MUST NOT leak into presence broadcasts or chat packets.
 */
data class SosContact(
    val phoneNumber: String,
    val defaultMessage: String = "EMERGENCY: I need assistance. Please send help.",
    val includeLocation: Boolean = true
)
