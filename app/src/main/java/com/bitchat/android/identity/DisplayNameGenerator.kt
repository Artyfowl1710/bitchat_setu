package com.bitchat.android.identity

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

/**
 * Auto-generates friendly accountless display names (e.g. "🌊 Wave", "⭐ Star")
 * for non-technical users on first launch without requiring sign-up or phone numbers.
 */
object DisplayNameGenerator {
    private const val PREFS_NAME = "bitchat_identity_profile"
    private const val KEY_DISPLAY_NAME = "auto_display_name"

    private val EMOJIS = listOf(
        "🌊", "⭐", "🌱", "☀️", "🌙", "🔥", "⚡", "🍀", "🦅", "🦁",
        "🐯", "🐬", "🕊️", "🌸", "🏔️", "🌈", "☀️", "💎", "🚀", "🛡️"
    )

    private val WORDS = listOf(
        "Wave", "Star", "Leaf", "Sun", "Moon", "Flame", "Spark", "Clover", "Eagle", "Lion",
        "Tiger", "Dolphin", "Dove", "Bloom", "Peak", "Rainbow", "Ray", "Gem", "Rocket", "Shield"
    )

    fun getOrGenerateDisplayName(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_DISPLAY_NAME, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }

        val generated = generateRandomName()
        prefs.edit().putString(KEY_DISPLAY_NAME, generated).apply()
        return generated
    }

    fun saveDisplayName(context: Context, name: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DISPLAY_NAME, name.trim()).apply()
    }

    private fun generateRandomName(): String {
        val index = Random.nextInt(EMOJIS.size.coerceAtMost(WORDS.size))
        return "${EMOJIS[index]} ${WORDS[index]}"
    }
}
