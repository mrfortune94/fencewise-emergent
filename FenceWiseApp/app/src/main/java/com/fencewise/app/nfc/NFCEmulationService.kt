package com.fencewise.app.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.fencewise.app.data.NFCTagStorage

class NFCEmulationService : HostApduService() {
    
    private val TAG = "NFCEmulationService"
    
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d(TAG, "processCommandApdu: ${commandApdu?.toHexString()}")
        
        val storage = NFCTagStorage(applicationContext)
        val activeTag = storage.getActiveTag()
        
        if (activeTag != null && commandApdu != null) {
            // For simple emulation, we'll respond with the stored tag data
            // In a real implementation, you'd need to handle APDU commands properly
            return activeTag.data
        }
        
        // Default response: success status word
        return byteArrayOf(0x90.toByte(), 0x00.toByte())
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "onDeactivated: $reason")
    }
    
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}
