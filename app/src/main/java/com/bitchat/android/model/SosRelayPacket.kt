package com.bitchat.android.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * SosRelayPacket: TLV-encoded SOS payload for mesh relay.
 * TLVs:
 *  - 0x01: messageId (UTF-8)
 *  - 0x02: destinationPhoneNumber (UTF-8)
 *  - 0x03: text (UTF-8)
 */
data class SosRelayPacket(
    val messageId: String,
    val destinationPhoneNumber: String,
    val text: String,
    val isSafeBeacon: Boolean = false
) {
    private enum class TLVType(val v: UByte) {
        MESSAGE_ID(0x01u), DESTINATION_PHONE(0x02u), TEXT(0x03u), IS_SAFE_BEACON(0x04u);
        companion object { fun from(value: UByte) = values().find { it.v == value } }
    }

    fun encode(): ByteArray? {
        try {
            val idBytes = messageId.toByteArray(Charsets.UTF_8)
            val phoneBytes = destinationPhoneNumber.toByteArray(Charsets.UTF_8)
            val textBytes = text.toByteArray(Charsets.UTF_8)
            val safeBytes = byteArrayOf(if (isSafeBeacon) 1 else 0)
            
            if (idBytes.size > 0xFFFF || phoneBytes.size > 0xFFFF || textBytes.size > 0xFFFF) {
                return null
            }
            
            val capacity = (1 + 2 + idBytes.size) + (1 + 2 + phoneBytes.size) + (1 + 2 + textBytes.size) + (1 + 2 + 1)
            val buf = ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN)

            // MESSAGE_ID
            buf.put(TLVType.MESSAGE_ID.v.toByte())
            buf.putShort(idBytes.size.toShort())
            buf.put(idBytes)

            // DESTINATION_PHONE
            buf.put(TLVType.DESTINATION_PHONE.v.toByte())
            buf.putShort(phoneBytes.size.toShort())
            buf.put(phoneBytes)

            // TEXT
            buf.put(TLVType.TEXT.v.toByte())
            buf.putShort(textBytes.size.toShort())
            buf.put(textBytes)

            // IS_SAFE_BEACON
            buf.put(TLVType.IS_SAFE_BEACON.v.toByte())
            buf.putShort(1.toShort())
            buf.put(safeBytes)

            return buf.array()
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        fun decode(data: ByteArray): SosRelayPacket? {
            try {
                var off = 0
                var id: String? = null
                var phone: String? = null
                var text: String? = null
                var isSafe = false
                
                while (off + 3 <= data.size) {
                    val t = TLVType.from(data[off].toUByte()) ?: return null
                    off += 1
                    
                    if (off + 2 > data.size) return null
                    val len = ((data[off].toInt() and 0xFF) shl 8) or (data[off + 1].toInt() and 0xFF)
                    off += 2
                    
                    if (len < 0 || off + len > data.size) return null
                    val value = data.copyOfRange(off, off + len)
                    off += len
                    
                    when (t) {
                        TLVType.MESSAGE_ID -> id = String(value, Charsets.UTF_8)
                        TLVType.DESTINATION_PHONE -> phone = String(value, Charsets.UTF_8)
                        TLVType.TEXT -> text = String(value, Charsets.UTF_8)
                        TLVType.IS_SAFE_BEACON -> isSafe = value.firstOrNull() == 1.toByte()
                    }
                }
                
                if (id == null || phone == null || text == null) return null
                return SosRelayPacket(id, phone, text, isSafe)
            } catch (e: Exception) {
                return null
            }
        }
    }
}
