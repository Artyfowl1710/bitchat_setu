package com.bitchat.android.identity

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages secure storage of the user's SOS contact information.
 * STRICT IDENTITY CONSTRAINT: This is the ONLY storage location permitted for emergency phone numbers.
 */
class SosContactStore(context: Context) {

    companion object {
        private const val TAG = "SosContactStore"
        private const val PREFS_NAME = "bitchat_sos_identity"
        private const val KEY_PHONE_NUMBER = "sos_phone_number"
        private const val KEY_DEFAULT_MESSAGE = "sos_default_message"
        private const val KEY_INCLUDE_LOCATION = "sos_include_location"

        @Volatile
        private var instance: SosContactStore? = null

        fun getInstance(context: Context): SosContactStore {
            return instance ?: synchronized(this) {
                instance ?: SosContactStore(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveContact(contact: SosContact) {
        try {
            prefs.edit()
                .putString(KEY_PHONE_NUMBER, contact.phoneNumber.trim())
                .putString(KEY_DEFAULT_MESSAGE, contact.defaultMessage.trim())
                .putBoolean(KEY_INCLUDE_LOCATION, contact.includeLocation)
                .apply()
            Log.d(TAG, "SOS contact saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save SOS contact", e)
        }
    }

    fun getContact(): SosContact? {
        return try {
            val phone = prefs.getString(KEY_PHONE_NUMBER, null)
            if (phone.isNullOrBlank()) return null
            val message = prefs.getString(KEY_DEFAULT_MESSAGE, "EMERGENCY: I need assistance. Please send help.")
                ?: "EMERGENCY: I need assistance. Please send help."
            val includeLocation = prefs.getBoolean(KEY_INCLUDE_LOCATION, true)
            SosContact(phone, message, includeLocation)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load SOS contact", e)
            null
        }
    }

    fun clearContact() {
        try {
            prefs.edit().clear().apply()
            Log.d(TAG, "SOS contact cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear SOS contact", e)
        }
    }

    fun hasContact(): Boolean {
        val phone = prefs.getString(KEY_PHONE_NUMBER, null)
        return !phone.isNullOrBlank()
    }
}
