package com.bitchat.android.features.sos

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages physical search-and-rescue beacons:
 * 1. LED Flashlight strobe in SOS Morse pattern (... --- ...)
 * 2. High-decibel acoustic siren via Android ToneGenerator
 * 
 * Works 100% offline without network or nearby peers.
 */
class RescueBeaconManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "RescueBeaconManager"

        @Volatile
        private var instance: RescueBeaconManager? = null

        fun getInstance(context: Context): RescueBeaconManager {
            return instance ?: synchronized(this) {
                instance ?: RescueBeaconManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val _isBeaconActive = MutableStateFlow(false)
    val isBeaconActive: StateFlow<Boolean> = _isBeaconActive.asStateFlow()

    private var beaconJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun toggleBeacon() {
        if (_isBeaconActive.value) {
            stopBeacon()
        } else {
            startBeacon()
        }
    }

    fun startBeacon() {
        if (_isBeaconActive.value) return
        _isBeaconActive.value = true

        beaconJob = scope.launch {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = try {
                cameraManager?.cameraIdList?.firstOrNull { id ->
                    cameraManager.getCameraCharacteristics(id)
                        .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Camera flash unavailable: ${e.message}")
                null
            }

            var toneGenerator: ToneGenerator? = null
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            } catch (e: Exception) {
                Log.w(TAG, "ToneGenerator unavailable: ${e.message}")
            }

            // SOS Morse Code Timing: dot = 200ms, dash = 600ms
            // S (...) = 3 dots, O (---) = 3 dashes, S (...) = 3 dots
            try {
                while (isActive && _isBeaconActive.value) {
                    // S: ...
                    repeat(3) {
                        setFlash(cameraManager, cameraId, true)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 150)
                        delay(200)
                        setFlash(cameraManager, cameraId, false)
                        delay(200)
                    }
                    delay(400)

                    // O: ---
                    repeat(3) {
                        setFlash(cameraManager, cameraId, true)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)
                        delay(600)
                        setFlash(cameraManager, cameraId, false)
                        delay(200)
                    }
                    delay(400)

                    // S: ...
                    repeat(3) {
                        setFlash(cameraManager, cameraId, true)
                        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 150)
                        delay(200)
                        setFlash(cameraManager, cameraId, false)
                        delay(200)
                    }
                    delay(1200) // Pause between SOS cycles
                }
            } finally {
                setFlash(cameraManager, cameraId, false)
                toneGenerator?.release()
            }
        }
    }

    fun stopBeacon() {
        _isBeaconActive.value = false
        beaconJob?.cancel()
        beaconJob = null
    }

    private fun setFlash(cameraManager: CameraManager?, cameraId: String?, enabled: Boolean) {
        if (cameraManager == null || cameraId == null) return
        try {
            cameraManager.setTorchMode(cameraId, enabled)
        } catch (_: Exception) { }
    }
}
