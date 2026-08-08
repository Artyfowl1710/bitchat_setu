package com.bitchat.android.features.sos

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.bitchat.android.identity.SosContact
import com.bitchat.android.identity.SosContactStore
import com.bitchat.android.model.SosRelayPacket
import com.bitchat.android.protocol.BitchatPacket
import com.bitchat.android.protocol.MessageType
import com.bitchat.android.protocol.SpecialRecipients
import com.bitchat.android.ui.SosStatusState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Encapsulates SOS dispatch, gateway relay, data tracking, and delivery ACK tracking.
 */
class SosManager(private val context: Context) {

    companion object {
        private const val TAG = "SosManager"
        private const val PREFS_NAME = "bitchat_sos_stats"
        private const val KEY_RELAY_COUNT = "sos_relay_count"
        private const val KEY_RELAY_BYTES = "sos_relay_bytes"
        private const val KEY_DAILY_RESET_DATE = "sos_daily_reset_date"
        private const val DAILY_CAP_BYTES = 1_000_000L // 1MB daily relay cap per Section 4.4

        @Volatile
        private var instance: SosManager? = null

        fun getInstance(context: Context): SosManager {
            return instance ?: synchronized(this) {
                instance ?: SosManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _sosStatus = MutableStateFlow<SosStatusState?>(null)
    val sosStatus: StateFlow<SosStatusState?> = _sosStatus.asStateFlow()

    private var activeMessageId: String? = null

    /**
     * Dispatch SOS according to Section 4.2 dispatch order:
     * 1. Try local device connectivity first (HTTPS backend call).
     * 2. If local connectivity fails, broadcast SOS_RELAY into the mesh.
     */
    fun triggerSos(
        contact: SosContact,
        sendPacketToMesh: (BitchatPacket) -> Unit
    ) {
        dispatchRelay(contact, isSafe = false, sendPacketToMesh = sendPacketToMesh)
    }

    fun triggerSafeBeacon(
        contact: SosContact,
        sendPacketToMesh: (BitchatPacket) -> Unit
    ) {
        dispatchRelay(contact, isSafe = true, sendPacketToMesh = sendPacketToMesh)
    }

    private fun dispatchRelay(
        contact: SosContact,
        isSafe: Boolean,
        sendPacketToMesh: (BitchatPacket) -> Unit
    ) {
        val msgId = UUID.randomUUID().toString()
        activeMessageId = msgId

        _sosStatus.value = SosStatusState.TryingDirect

        scope.launch {
            val messageText = if (isSafe) "I am safe." else contact.defaultMessage
            val hasDirectConn = tryDirectBackendDispatch(msgId, contact, messageText, isSafe)
            if (hasDirectConn) {
                val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                _sosStatus.value = SosStatusState.Delivered(timestamp)
                Log.d(TAG, "Relay dispatched directly via local network (isSafe=$isSafe)!")
                return@launch
            }

            // Fallback to mesh relay
            _sosStatus.value = SosStatusState.SendingViaMesh
            Log.d(TAG, "Direct connection unavailable. Broadcasting SOS_RELAY (isSafe=$isSafe) into mesh...")

            val sosPacket = SosRelayPacket(
                messageId = msgId,
                destinationPhoneNumber = contact.phoneNumber,
                text = messageText,
                isSafeBeacon = isSafe
            )

            val payload = sosPacket.encode() ?: return@launch
            val packet = BitchatPacket(
                version = 1u,
                type = MessageType.SOS_RELAY.value,
                senderID = ByteArray(8) { 0 },
                recipientID = SpecialRecipients.BROADCAST,
                timestamp = System.currentTimeMillis().toULong(),
                payload = payload,
                signature = null,
                ttl = com.bitchat.android.util.AppConstants.MESSAGE_TTL_HOPS
            )

            sendPacketToMesh(packet)
            _sosStatus.value = SosStatusState.ReachedDevices(1)
        }
    }

    fun handleDeliveryAck(messageId: String) {
        if (activeMessageId == messageId) {
            val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            _sosStatus.value = SosStatusState.Delivered(timestamp)
            Log.d(TAG, "SOS Delivery ACK confirmed: $messageId")
        }
    }

    fun dismissStatus() {
        _sosStatus.value = null
        activeMessageId = null
    }

    /**
     * Data cost transparency tracking per Section 4.4
     */
    fun recordRelay(bytes: Long) {
        checkDailyReset()
        val currentCount = prefs.getInt(KEY_RELAY_COUNT, 0)
        val currentBytes = prefs.getLong(KEY_RELAY_BYTES, 0L)

        prefs.edit()
            .putInt(KEY_RELAY_COUNT, currentCount + 1)
            .putLong(KEY_RELAY_BYTES, currentBytes + bytes)
            .apply()
    }

    fun canRelayData(): Boolean {
        checkDailyReset()
        val currentBytes = prefs.getLong(KEY_RELAY_BYTES, 0L)
        return currentBytes < DAILY_CAP_BYTES
    }

    fun getRelayStats(): Pair<Int, Long> {
        checkDailyReset()
        val count = prefs.getInt(KEY_RELAY_COUNT, 0)
        val bytes = prefs.getLong(KEY_RELAY_BYTES, 0L)
        return Pair(count, bytes)
    }

    private fun checkDailyReset() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val lastReset = prefs.getString(KEY_DAILY_RESET_DATE, null)
        if (lastReset != today) {
            prefs.edit()
                .putString(KEY_DAILY_RESET_DATE, today)
                .putInt(KEY_RELAY_COUNT, 0)
                .putLong(KEY_RELAY_BYTES, 0L)
                .apply()
        }
    }

    private suspend fun tryDirectBackendDispatch(
        msgId: String,
        contact: SosContact,
        text: String,
        isSafe: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return@withContext false
            val caps = cm.getNetworkCapabilities(network) ?: return@withContext false
            val isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (!isConnected) return@withContext false

            // Try backend endpoint (local dev IP on Wi-Fi)
            val url = URL("http://10.205.39.73:3000/relay-sos")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; utf-8")
            conn.doOutput = true

            val json = JSONObject().apply {
                put("messageId", msgId)
                put("destinationPhoneNumber", contact.phoneNumber)
                put("text", text)
                put("isSafeBeacon", isSafe)
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray(Charsets.UTF_8))
            }

            conn.responseCode in 200..299
        } catch (e: Exception) {
            false
        }
    }
}
